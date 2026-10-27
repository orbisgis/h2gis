/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.asc;

import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.*;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 * Driver to import ESRI ASCII Raster file as polygons
 *
 * This class is written to directly access the ESRI ascii grid format.
 *
 * The ASCII grid data file format comprises a few lines of header data followed
 * by lists of cell values. The header data includes the following keywords and
 * values:
 *
 * ncols : number of columns in the data set.
 *
 * nrows : number of rows in the data set.
 *
 * xllcorner : x-coordinate of the west border of the LowerLeft corner.
 *
 * yllcorner : y-coordinate of the south border of the LowerLeft corner.
 *
 * cellsize : size of the square cell of the data set.
 *
 * NODATA_value : arbitrary value assigned to unknown cells.
 *
 * @author Nicolas Fortin (Université Gustave Eiffel 2020)
 * @author Erwan Bocher, CNRS, 2020
 */
public class AscReaderDriver {

    private static final int BATCH_MAX_SIZE = 100;
    private static final int BUFFER_SIZE = 16384;
    private boolean as3DPoint = true;
    private Envelope extractEnvelope = null;
    private int downScale = 1;
    private String lastWord = "";

    private int nrows;
    private int ncols;
    private double cellSize;
    private double yValue;
    private double xValue;
    private boolean readFirst;
    private double noData;
    private int zType = 2;
    private boolean deleteTable = false;
    private String encoding = "UTF-8";
    private boolean importNodata = false;

    /**
     * @return If true ASC is imported as 3D points cloud, Raster is imported in
     * pixel polygons otherwise.
     */
    public boolean isAs3DPoint() {
        return as3DPoint;
    }

    /**
     * @param as3DPoint If true ASC is imported as 3D points cloud, Raster is
     * imported in pixel polygons otherwise.
     */
    public void setAs3DPoint(boolean as3DPoint) {
        this.as3DPoint = as3DPoint;
    }

    /**
     * @return Imported geometries are filtered using this optional envelope
     */
    public Envelope getExtractEnvelope() {
        return extractEnvelope;
    }

    /**
     * @param extractEnvelope Imported geometries are filtered using this
     * optional envelope. Set Null object for no filtering.
     */
    public void setExtractEnvelope(Envelope extractEnvelope) {
        this.extractEnvelope = extractEnvelope;
    }

    /**
     * @return Coefficient used for exporting less cells (1 all cells, 2 for
     * size / 2)
     */
    public int getDownScale() {
        return downScale;
    }

    /**
     * @param downScale Coefficient used for exporting less cells (1 all cells,
     * 2 for size / 2)
     */
    public void setDownScale(int downScale) {
        this.downScale = downScale;
    }

    private void readHeader(Scanner scanner) throws IOException {
        // NCOLS
        lastWord = scanner.next();
        if (!lastWord.equalsIgnoreCase("NCOLS")) {
            throw new IOException("Unexpected word " + lastWord);
        }
        // XXX
        lastWord = scanner.next();
        ncols = Integer.parseInt(lastWord);
        if (ncols <= 0) {
            throw new IOException("NCOLS <= 0");
        }
        // NROWS
        lastWord = scanner.next();
        if (!lastWord.equalsIgnoreCase("NROWS")) {
            throw new IOException("Unexpected word " + lastWord);
        }
        // XXX
        lastWord = scanner.next();
        nrows = Integer.parseInt(lastWord);
        if (nrows <= 0) {
            throw new IOException("NROWS <= 0");
        }
        // XLLCENTER or XLLCORNER
        lastWord = scanner.next();
        if (!(lastWord.equalsIgnoreCase("XLLCENTER") || lastWord.equalsIgnoreCase("XLLCORNER"))) {
            throw new IOException("Unexpected word " + lastWord);
        }
        boolean isXCenter = lastWord.equalsIgnoreCase("XLLCENTER");
        // XXX
        lastWord = scanner.next();
        xValue = Double.parseDouble(lastWord);

        // YLLCENTER or YLLCORNER
        lastWord = scanner.next();
        if (!(lastWord.equalsIgnoreCase("YLLCENTER") || lastWord.equalsIgnoreCase("YLLCORNER"))) {
            throw new IOException("Unexpected word " + lastWord);
        }
        boolean isYCenter = lastWord.equalsIgnoreCase("YLLCENTER");
        // XXX
        lastWord = scanner.next();
        yValue = Double.parseDouble(lastWord);

        // CELLSIZE
        lastWord = scanner.next();
        if (!lastWord.equalsIgnoreCase("CELLSIZE")) {
            throw new IOException("Unexpected word " + lastWord);
        }
        // XXX
        lastWord = scanner.next();
        cellSize = Double.parseDouble(lastWord);
        // Compute offsets
        if (isXCenter) {
            xValue = xValue - cellSize / 2;
        }
        if (isYCenter) {
            yValue = yValue + cellSize * nrows - cellSize / 2;
        } else {
            yValue = yValue + cellSize * nrows;
        }
        // Optional NODATA_VALUE
        lastWord = scanner.next();
        readFirst = false;
        noData = -9999;
        if (lastWord.equalsIgnoreCase("NODATA_VALUE")) {
            readFirst = true;
            // XXX
            lastWord = scanner.next();
            noData = Double.parseDouble(lastWord);

        }
    }

    /**
     * Read asc file
     *
     * @param connection database connection
     * @param fileName input file
     * @param progress Progress visitor following the execution.
     * @param tableReference output table name
     * @param srid the espg code of the input file
     * @throws IOException Throw exception is the file cannot be accessed
     * @throws SQLException Throw exception is the file name contains unsupported characters
     */
    public String[] read(Connection connection, File fileName, ProgressVisitor progress, String tableReference,
                         int srid) throws SQLException, IOException {
        if (fileName != null && fileName.getName().toLowerCase().endsWith(".asc")) {
            if (!fileName.exists()) {
                throw new SQLException("The file " + tableReference + " doesn't exist ");
            }
            final DBTypes dbType = DBUtils.getDBType(connection);
            TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
            String outputTableName = requestedTable.toString();
            if (deleteTable) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + outputTableName);
                stmt.close();
            }
            try (FileInputStream inputStream = new FileInputStream(fileName)) {
                connection.setAutoCommit(false);
                outputTableName = readAsc(connection, inputStream, progress, outputTableName, srid);
                connection.commit();
            } finally {
                connection.setAutoCommit(true);
            }
            return new String[]{outputTableName};
        } else if (fileName != null && fileName.getName().toLowerCase().endsWith(".gz")) {
            if (!fileName.exists()) {
                throw new SQLException("The file " + tableReference + " doesn't exist ");
            }
            final DBTypes dbType = DBUtils.getDBType(connection);
            TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
            String outputTableName = requestedTable.toString();
            if (deleteTable) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + outputTableName);
                stmt.close();
            }
            FileInputStream fis = new FileInputStream(fileName);
            try {
                connection.setAutoCommit(false);
                outputTableName = readAsc(connection, new GZIPInputStream(fis), progress, outputTableName, srid);
            } finally {
                connection.setAutoCommit(true);
            }
            return new String[]{outputTableName};
        } else {
            throw new SQLException("The asc read driver supports only asc or gz extensions");
        }
    }

    /**
     * Read the ascii file from inpustream
     *
     * @param connection database connection
     * @param inputStream {@link InputStream}
     * @param progress Progress visitor following the execution.
     * @param outputTable output table name
     * @param srid output srid
     * @throws UnsupportedEncodingException  Throw exception is the encoding file is not supported
     * @throws SQLException Throw exception is the file name contains unsupported characters
     * @return output table name
     */

    private String readAsc(Connection connection, InputStream inputStream, ProgressVisitor progress, String outputTable,
                           int srid) throws UnsupportedEncodingException, SQLException {
        try (CellReader cells = new CellReader(inputStream, srid)) {
            // Read values
            Statement st = connection.createStatement();
            PreparedStatement preparedStatement;

            if (!JDBCUtilities.tableExists(connection,outputTable)) {
                if (as3DPoint) {
                    if (zType == 1) {
                        st.execute("CREATE TABLE " + outputTable + "(PK INT PRIMARY KEY, THE_GEOM GEOMETRY(POINTZ, " + srid + "), Z integer)");
                    } else {
                        st.execute("CREATE TABLE " + outputTable + "(PK INT PRIMARY KEY, THE_GEOM GEOMETRY(POINTZ, " + srid + "), Z double precision)");
                    }
                } else {
                    if (zType == 1) {
                        st.execute("CREATE TABLE " + outputTable + "(PK INT PRIMARY KEY, THE_GEOM GEOMETRY(POLYGONZ, " + srid + "),Z integer)");
                    } else {
                        st.execute("CREATE TABLE " + outputTable + "(PK INT PRIMARY KEY, THE_GEOM GEOMETRY(POLYGONZ, " + srid + "),Z double precision)");
                    }
                }
            } else {
                // restore the incremental index from the existing table
                try(ResultSet rs = st.executeQuery("SELECT MAX(PK) FROM " +  outputTable)) {
                    if(rs.next()) {
                        cells.setFirstIndex(rs.getInt(1) + 1);
                    }
                }
            }
            preparedStatement = connection.prepareStatement("INSERT INTO " + outputTable
                    + "(PK, the_geom, Z) VALUES (?, ?, ?)");

            // Read data
            ProgressVisitor cellProgress = new EmptyProgressVisitor();
            if (progress != null) {
                cellProgress = progress.subProcess(cells.getLastRow());
            }
            int rowsDone = 0;
            int batchSize = 0;
            Object[] cell;
            while ((cell = cells.next()) != null) {
                for (; rowsDone < cells.getRowsRead(); rowsDone++) {
                    cellProgress.endStep();
                }
                preparedStatement.setObject(1, cell[0]);
                preparedStatement.setObject(2, cell[1]);
                preparedStatement.setObject(3, cell[2]);
                preparedStatement.addBatch();
                batchSize++;
                if (batchSize >= BATCH_MAX_SIZE) {
                    preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                    batchSize = 0;
                }
            }
            if (batchSize > 0) {
                preparedStatement.executeBatch();
            }
            for (; rowsDone < cells.getRowsRead(); rowsDone++) {
                cellProgress.endStep();
            }
            return outputTable;
        } catch (NoSuchElementException | NumberFormatException | IOException | SQLException ex) {
            throw new SQLException("Unexpected word " + lastWord, ex);
        }
    }

    /**
     * Opens a file to read its cells one by one, with the options of this
     * driver: envelope, downscale, points or polygons, no data values.
     *
     * @param inputStream content of the .asc file, closed by the reader
     * @param srid SRID of the cells
     * @return the reader, to close
     */
    public CellReader openCells(InputStream inputStream, int srid) throws IOException {
        try {
            return new CellReader(inputStream, srid);
        } catch (NoSuchElementException | NumberFormatException ex) {
            inputStream.close();
            throw new IOException("Unexpected word " + lastWord, ex);
        } catch (IOException ex) {
            inputStream.close();
            throw ex;
        }
    }

    /**
     * Reads the cells of an Esri ASCII grid one by one. A cell is
     * {PK (Integer), the point or the polygon of the cell, Z (Double)}.
     */
    public final class CellReader implements Closeable {
        private final Scanner scanner;
        private final GeometryFactory factory;
        private final int srid;
        private final int firstRow;
        private final int firstCol;
        private final int lastRow;
        private final int lastCol;
        /** next cell to read */
        private int row = 0;
        private int col = 0;
        private int index = 0;
        private boolean done = false;

        private CellReader(InputStream inputStream, int srid) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream, BUFFER_SIZE), encoding));
            this.scanner = new Scanner(reader);
            this.srid = srid;
            // Read HEADER
            readHeader(scanner);
            this.factory = new GeometryFactory(new PrecisionModel(), srid);
            // Compute envelope
            if (extractEnvelope != null) {
                firstCol = (int) Math.floor((extractEnvelope.getMinX() - xValue) / cellSize);
                lastCol = (int) Math.ceil((extractEnvelope.getMaxX() - xValue) / cellSize);
                firstRow = nrows - (int) Math.ceil((extractEnvelope.getMaxY() - (yValue - cellSize * nrows)) / cellSize);
                lastRow = nrows - (int) Math.ceil((extractEnvelope.getMinY() - (yValue - cellSize * nrows)) / cellSize);
            } else {
                firstCol = 0;
                lastCol = ncols;
                firstRow = 0;
                lastRow = nrows;
            }
        }

        /**
         * @param index PK of the first cell
         */
        public void setFirstIndex(int index) {
            this.index = index;
        }

        /**
         * @return the last row of the cells to read
         */
        public int getLastRow() {
            return lastRow;
        }

        /**
         * @return the number of rows of the grid read so far
         */
        public int getRowsRead() {
            return row;
        }

        /**
         * @return the next cell {PK, geometry, Z}, null at the end
         */
        public Object[] next() throws IOException {
            try {
                while (!done) {
                    if (row >= nrows) {
                        done = true;
                        break;
                    }
                    if (readFirst) {
                        lastWord = scanner.next();
                    } else {
                        readFirst = true;
                    }
                    int i = row;
                    int j = col;
                    col++;
                    boolean endOfRow = col >= ncols;
                    if (endOfRow) {
                        col = 0;
                        row++;
                    }
                    Object[] cell = null;
                    if ((downScale == 1 || (i % downScale == 0 && j % downScale == 0)) && (extractEnvelope == null || (i >= firstRow && i <= lastRow && j >= firstCol && j <= lastCol))) {
                        double z = Double.parseDouble(lastWord);
                        double x = xValue + j * cellSize;
                        double y = yValue - i * cellSize;
                        Geometry geometry;
                        if (as3DPoint) {
                            geometry = factory.createPoint(new Coordinate(x + cellSize / 2, y - cellSize / 2, z));
                        } else {
                            geometry = factory.createPolygon(new Coordinate[]{new Coordinate(x, y, z), new Coordinate(x, y - cellSize * downScale, z), new Coordinate(x + cellSize * downScale, y - cellSize * downScale, z), new Coordinate(x + cellSize * downScale, y, z), new Coordinate(x, y, z)});
                        }
                        geometry.setSRID(srid);
                        int pk = index++;
                        if (Math.abs(noData - z) != 0) {
                            cell = new Object[]{pk, geometry, z};
                        } else if (importNodata) {
                            cell = new Object[]{pk, geometry, noData};
                        }
                    }
                    if (endOfRow && i > lastRow) {
                        done = true;
                    }
                    if (cell != null) {
                        return cell;
                    }
                }
                return null;
            } catch (NoSuchElementException | NumberFormatException ex) {
                throw new IOException("Unexpected word " + lastWord, ex);
            }
        }

        @Override
        public void close() {
            scanner.close();
        }
    }

    /**
     * Use to set the z conversion type 1 = integer 2 = double
     *
     * @param zType value type to manage the z value
     */
    public void setZType(int zType) {
        this.zType = zType;
    }

    /**
     * Set true to delete the input table if exists
     *
     * @param deleteTable true to delete the table
     */
    public void setDeleteTable(boolean deleteTable) {
        this.deleteTable = deleteTable;
    }

    /**
     * Set encoding
     *
     * @param encoding file encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Set to true if nodata must be imported. Default is false
     *
     * @param importNodata true to read to data value
     */
    public void setImportNodata(boolean importNodata) {
        this.importNodata = importNodata;
    }
}