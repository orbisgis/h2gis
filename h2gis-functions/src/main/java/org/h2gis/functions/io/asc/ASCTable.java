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

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.value.TypeInfo;
import org.h2.value.ValueDouble;
import org.h2.value.ValueGeometry;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.URIUtilities;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.zip.GZIPInputStream;

/**
 * Reads the cells of an Esri ASCII grid as a table, without importing it:
 *
 * <pre>
 * SELECT AVG(Z), MAX(Z) FROM ASCTable('/data/dem.asc');
 * SELECT * FROM ASCTable('/data/dem.asc.gz', ST_MakeEnvelope(10, 10, 20, 20), 2, true);
 * </pre>
 *
 * The rows are the ones ASCRead imports with the same options: PK, THE_GEOM (a
 * POINTZ at the center of the cell, or the POLYGONZ of the cell) and Z. The
 * envelope and the downscale are applied while reading the file.
 *
 * @author Erwan Bocher, CNRS
 */
public class ASCTable extends AbstractFunction implements ScalarFunction {

    public ASCTable() {
        addProperty(PROP_REMARKS, "Read the cells of an Esri ASCII grid (.asc or .gz) as a table, without importing it.\n"
                + "SELECT * FROM ASCTable('/data/dem.asc');\n"
                + "SELECT * FROM ASCTable('/data/dem.asc', zType) with zType 1 for an INTEGER Z, 2 for a DOUBLE Z (default);\n"
                + "SELECT * FROM ASCTable('/data/dem.asc', envelope, downScale, asPolygons [, zType]): only the cells\n"
                + "intersecting the envelope (NULL for all), one cell out of downScale in each direction (1 for all),\n"
                + "the cells as polygons instead of points.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readTable";
    }

    /**
     * @param connection database connection
     * @param fileName path of the .asc or .gz file
     * @return the cells as points, Z as DOUBLE
     */
    public static ResultSet readTable(Connection connection, String fileName) throws SQLException, IOException {
        return readTable(connection, fileName, null, 1, false, 2);
    }

    /**
     * @param connection database connection
     * @param fileName path of the .asc or .gz file
     * @param zType 1 for an INTEGER Z, 2 for a DOUBLE Z
     * @return the cells as points
     */
    public static ResultSet readTable(Connection connection, String fileName, int zType) throws SQLException, IOException {
        return readTable(connection, fileName, null, 1, false, zType);
    }

    /**
     * @param connection database connection
     * @param fileName path of the .asc or .gz file
     * @param envelope only the cells intersecting the envelope of this geometry, null for all
     * @param downScale one cell out of downScale in each direction, 1 for all
     * @param asPolygons true for the polygons of the cells, false for their centers
     * @return the cells, Z as DOUBLE
     */
    public static ResultSet readTable(Connection connection, String fileName, Geometry envelope, int downScale,
                                      boolean asPolygons) throws SQLException, IOException {
        return readTable(connection, fileName, envelope, downScale, asPolygons, 2);
    }

    /**
     * @param connection database connection
     * @param fileName path of the .asc or .gz file
     * @param envelope only the cells intersecting the envelope of this geometry, null for all
     * @param downScale one cell out of downScale in each direction, 1 for all
     * @param asPolygons true for the polygons of the cells, false for their centers
     * @param zType 1 for an INTEGER Z, 2 for a DOUBLE Z
     * @return the cells
     */
    public static ResultSet readTable(Connection connection, String fileName, Geometry envelope, int downScale,
                                      boolean asPolygons, int zType) throws SQLException, IOException {
        File file = URIUtilities.fileFromString(fileName);
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".asc") && !name.endsWith(".gz")) {
            throw new SQLException("ASCTable supports only asc or gz extensions");
        }
        if (!file.isFile()) {
            throw new SQLException("The file " + file + " doesn't exist ");
        }
        if (downScale < 1) {
            throw new SQLException("The downscale must be 1 or more");
        }
        if (zType != 1 && zType != 2) {
            throw new SQLException("The Z type must be 1 (INTEGER) or 2 (DOUBLE)");
        }
        SimpleResultSet rs;
        if (TableUtilities.isColumnListConnection(connection)) {
            // H2 only wants the columns: they do not depend on the file
            rs = new SimpleResultSet();
        } else {
            AscReaderDriver driver = new AscReaderDriver();
            if (envelope != null && !envelope.isEmpty()) {
                driver.setExtractEnvelope(envelope.getEnvelopeInternal());
            }
            driver.setDownScale(downScale);
            driver.setAs3DPoint(!asPolygons);
            driver.setZType(zType);
            rs = new SimpleResultSet(new CellRowSource(driver, file, AscDriverFunction.getSRID(file), zType == 1));
        }
        rs.addColumn("PK", Types.INTEGER, 10, 0);
        rs.addColumn("THE_GEOM", Types.OTHER, "GEOMETRY", 0, 0);
        if (zType == 1) {
            rs.addColumn("Z", Types.INTEGER, 10, 0);
        } else {
            rs.addColumn("Z", Types.DOUBLE, 17, 0);
        }
        return rs;
    }

    /**
     * Rows of the result set, read from the file when H2 asks for them.
     */
    private static final class CellRowSource implements SimpleRowSource {
        private final AscReaderDriver driver;
        private final File file;
        private final int srid;
        private final boolean integerZ;
        private AscReaderDriver.CellReader reader;

        private CellRowSource(AscReaderDriver driver, File file, int srid, boolean integerZ) {
            this.driver = driver;
            this.file = file;
            this.srid = srid;
            this.integerZ = integerZ;
        }

        @Override
        public Object[] readRow() throws SQLException {
            try {
                if (reader == null) {
                    InputStream inputStream = new FileInputStream(file);
                    if (file.getName().toLowerCase().endsWith(".gz")) {
                        try {
                            inputStream = new GZIPInputStream(inputStream);
                        } catch (IOException ex) {
                            inputStream.close();
                            throw ex;
                        }
                    }
                    reader = driver.openCells(inputStream, srid);
                }
                Object[] cell = reader.next();
                if (cell == null) {
                    close();
                    return null;
                }
                double z = (Double) cell[2];
                return new Object[]{cell[0], ValueGeometry.getFromGeometry(cell[1]),
                        // same conversion as the INTEGER column of ASCRead
                        integerZ ? ValueDouble.get(z).convertTo(TypeInfo.TYPE_INTEGER) : ValueDouble.get(z)};
            } catch (IOException ex) {
                close();
                throw new SQLException("Cannot read the file " + file + ": " + ex.getMessage(), ex);
            }
        }

        @Override
        public void close() {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }

        @Override
        public void reset() {
            // the next row will be the first one again
            close();
        }
    }
}