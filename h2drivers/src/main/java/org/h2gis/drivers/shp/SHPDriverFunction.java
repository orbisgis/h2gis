/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.shp;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.h2.table.Column;
import org.h2gis.drivers.dbf.DBFDriverFunction;
import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.drivers.file_table.FileEngine;
import org.h2gis.drivers.file_table.H2TableIndex;
import org.h2gis.drivers.shp.internal.SHPDriver;
import org.h2gis.drivers.shp.internal.ShapeType;
import org.h2gis.drivers.shp.internal.ShapefileHeader;
import org.h2gis.drivers.utility.FileUtil;
import org.h2gis.drivers.utility.PRJUtil;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.jts_utils.GeometryMetaData;

/**
 * Read/Write Shape files
 * @author Nicolas Fortin
 */
public class SHPDriverFunction implements DriverFunction {
    public static String DESCRIPTION = "ESRI shapefile";
    private static final int BATCH_MAX_SIZE = 100;
    

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        exportTable(connection, tableReference, fileName, progress, null);
    }

    /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to write, if exists it may be replaced
     * @param progress to display the IO progress
     * @param encoding File encoding, null will use default encoding
     * @throws SQLException
     * @throws IOException
     */
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress,String encoding) throws SQLException, IOException {
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        if (FileUtil.isExtensionWellFormated(fileName, "shp")) {
            TableLocation location = TableLocation.parse(tableReference, isH2);
            int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
            ProgressVisitor copyProgress = progress.subProcess(recordCount);
            // Read Geometry Index and type
            List<String> spatialFieldNames = SFSUtilities.getGeometryFields(connection, TableLocation.parse(tableReference, isH2));
            if (spatialFieldNames.isEmpty()) {
                throw new SQLException(String.format("The table %s does not contain a geometry field", tableReference));
            }
            ShapeType shapeType = null;
            // Read table content
            Statement st = connection.createStatement();
            try {
                ResultSet rs = st.executeQuery(String.format("select * from %s", location.toString()));
                try {
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    int geoFieldIndex = JDBCUtilities.getFieldIndex(resultSetMetaData, spatialFieldNames.get(0));
                    ArrayList<Integer> columnIndexes = new ArrayList<Integer>();                    
                    DbaseFileHeader header = DBFDriverFunction.dBaseHeaderFromMetaData(resultSetMetaData, columnIndexes);
                    columnIndexes.add(0, geoFieldIndex);
                    if (encoding != null) {
                        header.setEncoding(encoding);
                    }
                    header.setNumRecords(recordCount);
                    SHPDriver shpDriver = null;
                    Object[] row = new Object[header.getNumFields() + 1];
                    while (rs.next()) {
                        int i = 0;
                        for (Integer index : columnIndexes) {
                            row[i++] = rs.getObject(index);
                        }
                        if (shpDriver == null) {
                            // If there is not shape type constraint read the first geometry and use the same type
                            byte[] wkb = rs.getBytes(geoFieldIndex);
                            if (wkb != null) {
                                shapeType = getShapeTypeFromGeometryMetaData(GeometryMetaData.getMetaDataFromWKB(wkb));
                            }
                            if (shapeType != null) {
                                shpDriver = new SHPDriver();
                                shpDriver.setGeometryFieldIndex(0);
                                shpDriver.initDriver(fileName, shapeType, header);
                            }
                            else{
                                throw new SQLException("Unsupported geometry type.");
                            }
                        }
                        if (shpDriver != null) {
                            shpDriver.insertRow(row);
                        }
                        copyProgress.endStep();
                    }
                    if (shpDriver != null) {
                        shpDriver.close();
                    }
                } finally {
                    rs.close();
                }
            } finally {
                st.close();
            }
            String path = fileName.getAbsolutePath();
            String nameWithoutExt = path.substring(0, path.lastIndexOf('.'));
            PRJUtil.writePRJ(connection, location, spatialFieldNames.get(0), new File(nameWithoutExt + ".prj"));
            copyProgress.endOfProgress();
        }
        else{
            throw new SQLException("Only .shp extension is supported");
        }
    }


    @Override
    public String getFormatDescription(String format) {
        if(format.equalsIgnoreCase("shp")) {
            return DESCRIPTION;
        } else {
            return "";
        }
    }

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[] {"shp"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[] {"shp"};
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equalsIgnoreCase("shp");
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress, null);
    }

    /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param progress
     * @param forceEncoding If defined use this encoding instead of the one defined in dbf header.
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,String forceEncoding) throws SQLException, IOException {
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(fileName, forceEncoding);
        ProgressVisitor copyProgress = progress.subProcess((int)(shpDriver.getRowCount() / BATCH_MAX_SIZE));
        // PostGIS does not show sql
        String lastSql = "";
        try {
            DbaseFileHeader dbfHeader = shpDriver.getDbaseFileHeader();
            ShapefileHeader shpHeader = shpDriver.getShapeFileHeader();
            // Build CREATE TABLE sql request
            Statement st = connection.createStatement();
            String types = DBFDriverFunction.getSQLColumnTypes(dbfHeader, isH2);
            if(!types.isEmpty()) {
                types = ", " + types;
            }
            final TableLocation parse = TableLocation.parse(tableReference, isH2);
            List<Column> otherCols = new ArrayList<Column>(dbfHeader.getNumFields() + 1);
            otherCols.add(new Column("THE_GEOM", 0));
            for(int idColumn = 0; idColumn < dbfHeader.getNumFields(); idColumn++) {
                otherCols.add(new Column(dbfHeader.getFieldName(idColumn), 0));
            }
            String pkColName = FileEngine.getUniqueColumnName(H2TableIndex.PK_COLUMN_NAME, otherCols);
            int srid = PRJUtil.getSRID(connection, shpDriver.prjFile);
            shpDriver.setSRID(srid);
            if(isH2) {                
                //H2 Syntax
                st.execute(String.format("CREATE TABLE %s ("+ pkColName + " SERIAL ,the_geom %s %s)", parse,
                    getSFSGeometryType(shpHeader), types));
            } else {
                // PostgreSQL Syntax
                lastSql = String.format("CREATE TABLE %s ("+ pkColName + " SERIAL PRIMARY KEY, the_geom GEOMETRY(%s, %d) %s)", parse,
                        getPostGISSFSGeometryType(shpHeader),srid, types);
                st.execute(lastSql);
            }
            st.close();
            try {
                        lastSql = String.format("INSERT INTO %s VALUES (DEFAULT, %s )", parse,
                                DBFDriverFunction.getQuestionMark(dbfHeader.getNumFields() + 1));
                        PreparedStatement preparedStatement = connection.prepareStatement(lastSql);
                try {
                    long batchSize = 0;
                    for (int rowId = 0; rowId < shpDriver.getRowCount(); rowId++) {
                        Object[] values = shpDriver.getRow(rowId);
                        for (int columnId = 0; columnId < values.length; columnId++) {
                            preparedStatement.setObject(columnId + 1, values[columnId]);
                        }
                        preparedStatement.addBatch();
                        batchSize++;
                        if (batchSize >= BATCH_MAX_SIZE) {
                            preparedStatement.executeBatch();
                            preparedStatement.clearBatch();
                            batchSize = 0;
                            copyProgress.endStep();
                        }
                    }
                    if(batchSize > 0) {
                        preparedStatement.executeBatch();
                    }
                } finally {
                    preparedStatement.close();
                }
                //Alter table to set the SRID constraint
                if(isH2){
                    SFSUtilities.addTableSRIDConstraint(connection, parse, srid);
                }                
                //TODO create spatial index on the_geom ?
            } catch (Exception ex) {
                connection.createStatement().execute("DROP TABLE IF EXISTS " + tableReference);
                throw new SQLException(ex.getLocalizedMessage(), ex);
            }
        } catch (SQLException ex) {
            throw new SQLException(lastSql+"\n"+ex.getLocalizedMessage(), ex);
        } finally {
            shpDriver.close();
            copyProgress.endOfProgress();
        }
    }

    /**
     * Return the shape type supported by the shapefile format
     * @param meta
     * @return
     * @throws SQLException 
     */
    private static ShapeType getShapeTypeFromGeometryMetaData(GeometryMetaData meta) throws SQLException {
        ShapeType shapeType;
        switch (meta.geometryType) {
            case GeometryTypeCodes.MULTILINESTRING:
            case GeometryTypeCodes.LINESTRING:
            case GeometryTypeCodes.MULTILINESTRINGM:
            case GeometryTypeCodes.LINESTRINGM:
            case GeometryTypeCodes.MULTILINESTRINGZ:
            case GeometryTypeCodes.LINESTRINGZ:
                shapeType = meta.hasZ ? ShapeType.ARCZ : ShapeType.ARC;
                break;
            case GeometryTypeCodes.POINT:
                shapeType = meta.hasZ ? ShapeType.POINTZ  : ShapeType.POINT;
                break;
            case GeometryTypeCodes.MULTIPOINT:
                shapeType = meta.hasZ ? ShapeType.MULTIPOINTZ : ShapeType.MULTIPOINT;
                break;
            case GeometryTypeCodes.POLYGON:
            case GeometryTypeCodes.MULTIPOLYGON:
                shapeType = meta.hasZ ? ShapeType.POLYGONZ : ShapeType.POLYGON;
                break;
            default:
                return null;
        }
        return shapeType;
    }

    private static String getSFSGeometryType(ShapefileHeader header) {
        switch(header.getShapeType().id) {
            case 1:
            case 11:
            case 21:
                return "POINT";
            case 3:
            case 13:
            case 23:
                return "MULTILINESTRING";
            case 5:
            case 15:
            case 25:
                return "MULTIPOLYGON";
            case 8:
            case 18:
            case 28:
                return "MULTIPOINT";
            default:
                return "GEOMETRY";
        }
    }

    private static String getPostGISSFSGeometryType(ShapefileHeader header) {
        switch(header.getShapeType().id) {
            case 1:
                return "POINT";
            case 11:
            case 21:
                return "POINTZ";
            case 3:
                return "MULTILINESTRING";
            case 13:
            case 23:
                return "MULTILINESTRINGZ";
            case 5:
                return "MULTIPOLYGON";
            case 15:
            case 25:
                return "MULTIPOLYGONZ";
            case 8:
                return "MULTIPOINT";
            case 18:
            case 28:
                return "MULTIPOINTZ";
            default:
                return "GEOMETRY";
        }
    }    
}
