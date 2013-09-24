/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.drivers.shp;

import org.h2gis.drivers.dbf.DBFDriverFunction;
import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.drivers.shp.internal.SHPDriver;
import org.h2gis.drivers.shp.internal.ShapeType;
import org.h2gis.drivers.shp.internal.ShapefileHeader;
import org.h2gis.h2spatialapi.DriverFunction;
import org.orbisgis.sputilities.GeometryTypeCodes;
import org.orbisgis.sputilities.JDBCUtilities;
import org.orbisgis.sputilities.SFSUtilities;
import org.orbisgis.sputilities.TableLocation;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Read/Write Shape files
 * @author Nicolas Fortin
 */
public class SHPDriverFunction implements DriverFunction {
    private static final int BATCH_MAX_SIZE = 100;

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName) throws SQLException, IOException {
        int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
        //
        // Read Geometry Index and type
        List<String> spatialFieldNames = SFSUtilities.getGeometryFields(connection, TableLocation.parse(tableReference));
        if(spatialFieldNames.isEmpty()) {
            throw new SQLException(String.format("The table %s does not contain a geometry field", tableReference));
        }
        int geometryType = SFSUtilities.getGeometryType(connection, TableLocation.parse(tableReference), spatialFieldNames.get(0));
        ShapeType shapeType = getShapeTypeFromSFSGeometryTypeCode(geometryType, spatialFieldNames.get(0));
        // Read table content
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery(String.format("select * from `%s`", tableReference));
            try {
                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                DbaseFileHeader header = DBFDriverFunction.dBaseHeaderFromMetaData(resultSetMetaData);
                header.setNumRecords(recordCount);
                SHPDriver shpDriver = new SHPDriver();
                shpDriver.setGeometryFieldIndex(JDBCUtilities.getFieldIndex(resultSetMetaData, spatialFieldNames.get(0)) - 1);
                shpDriver.initDriver(fileName,shapeType , header);
                Object[] row = new Object[header.getNumFields() + 1];
                while (rs.next()) {
                    for(int columnId = 0; columnId < row.length; columnId++) {
                        row[columnId] = rs.getObject(columnId + 1);
                    }
                    shpDriver.insertRow(row);
                }
                shpDriver.close();
            } finally {
                rs.close();
            }
        } finally {
            st.close();
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
    public void importFile(Connection connection, String tableReference, File fileName) throws SQLException, IOException {
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(fileName);
        try {
            DbaseFileHeader dbfHeader = shpDriver.getDbaseFileHeader();
            ShapefileHeader shpHeader = shpDriver.getShapeFileHeader();
            // Build CREATE TABLE sql request
            Statement st = connection.createStatement();
            st.execute(String.format("CREATE TABLE %s (the_geom %s, %s)", TableLocation.parse(tableReference),
                    getSFSGeometryType(shpHeader), DBFDriverFunction.getSQLColumnTypes(dbfHeader)));
            st.close();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        String.format("INSERT INTO %s VALUES ( %s )", TableLocation.parse(tableReference),
                                DBFDriverFunction.getQuestionMark(dbfHeader.getNumFields() + 1)));
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
                        }
                    }
                    if(batchSize > 0) {
                        preparedStatement.executeBatch();
                    }
                } finally {
                    preparedStatement.close();
                }
                //TODO create spatial index on the_geom ?
            } catch (Exception ex) {
                connection.createStatement().execute("DROP TABLE IF EXISTS " + tableReference);
                throw new SQLException(ex.getLocalizedMessage(), ex);
            }
        } finally {
            shpDriver.close();
        }
    }

    private static ShapeType getShapeTypeFromSFSGeometryTypeCode(int sfsGeometryTypeCode, String fieldName) throws SQLException {
        ShapeType shapeType;
        switch (sfsGeometryTypeCode) {
            case GeometryTypeCodes.MULTILINESTRING:
            case GeometryTypeCodes.LINESTRING:
                shapeType = ShapeType.ARC;
                break;
            case GeometryTypeCodes.MULTILINESTRINGM:
            case GeometryTypeCodes.LINESTRINGM:
                shapeType = ShapeType.ARCM;
                break;
            case GeometryTypeCodes.MULTILINESTRINGZ:
            case GeometryTypeCodes.LINESTRINGZ:
                shapeType = ShapeType.ARCZ;
                break;
            case GeometryTypeCodes.POINT:
            case GeometryTypeCodes.MULTIPOINT:
                shapeType = ShapeType.MULTIPOINT;
                break;
            case GeometryTypeCodes.POINTM:
            case GeometryTypeCodes.MULTIPOINTM:
                shapeType = ShapeType.MULTIPOINTM;
                break;
            case GeometryTypeCodes.POINTZ:
            case GeometryTypeCodes.MULTIPOINTZ:
                shapeType = ShapeType.MULTIPOINTZ;
                break;
            case GeometryTypeCodes.POLYGON:
            case GeometryTypeCodes.MULTIPOLYGON:
                shapeType = ShapeType.POLYGON;
                break;
            case GeometryTypeCodes.POLYGONM:
            case GeometryTypeCodes.MULTIPOLYGONM:
                shapeType = ShapeType.POLYGONM;
                break;
            case GeometryTypeCodes.POLYGONZ:
            case GeometryTypeCodes.MULTIPOLYGONZ:
                shapeType = ShapeType.POLYGONZ;
                break;
            default:
                throw new SQLException(String.format("Geometry type of the field %s incompatible with ShapeFile," +
                        " please use (Multi)Point, (Multi)Polygon or (Multi)LineString constraint", fieldName));
        }
        return shapeType;
    }

    private static String getSFSGeometryType(ShapefileHeader header) {
        switch(header.getShapeType().id) {
            case 1:
            case 11:
            case 21:
                return "MULTIPOINT";
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
}
