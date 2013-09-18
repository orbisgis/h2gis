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

import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.drivers.shp.internal.SHPDriver;
import org.h2gis.drivers.shp.internal.ShapefileHeader;
import org.h2gis.h2spatialapi.DriverFunction;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Read/Write Shape files
 * @author Nicolas Fortin
 */
public class SHPDriverFunction implements DriverFunction {
    private static final int BATCH_MAX_SIZE = 100;

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName) throws SQLException, IOException {

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
            st.execute(String.format("CREATE TABLE `%s` (the_geom %s, %s)", tableReference, getSFSGeometryType(shpHeader), getSQLColumnTypes(dbfHeader)));
            st.close();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        String.format("INSERT INTO `%s` VALUES ( " + getQuestionMark(dbfHeader.getNumFields() + 1) + ")", tableReference));
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

    private static String getQuestionMark(int count) {
        StringBuilder qMark = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if(i > 0) {
                qMark.append(", ");
            }
            qMark.append("?");
        }
        return qMark.toString();
    }

    /**
     * Return SQL Columns declaration
     * @param header DBAse file header
     * @return Array of columns ex: ["id INTEGER", "len DOUBLE"]
     */
    private String getSQLColumnTypes(DbaseFileHeader header) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(int idColumn = 0; idColumn < header.getNumFields(); idColumn++) {
            if(idColumn > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(header.getFieldName(idColumn));
            stringBuilder.append(" ");
            switch (header.getFieldType(idColumn)) {
                // (L)logical (T,t,F,f,Y,y,N,n)
                case 'l':
                case 'L':
                    stringBuilder.append("BOOLEAN");
                    break;
                // (C)character (String)
                case 'c':
                case 'C':
                    stringBuilder.append("CHAR(");
                    // Append size
                    int length = header.getFieldLength(idColumn);
                    stringBuilder.append(String.valueOf(length));
                    stringBuilder.append(")");
                    break;
                // (D)date (Date)
                case 'd':
                case 'D':
                    stringBuilder.append("DATE");
                    break;
                // (F)floating (Double)
                case 'n':
                case 'N':
                    if ((header.getFieldDecimalCount(idColumn) == 0)) {
                        if ((header.getFieldLength(idColumn) >= 0)
                                && (header.getFieldLength(idColumn) < 10)) {
                            stringBuilder.append("INT4");
                        } else {
                            stringBuilder.append("INT8");
                        }
                    } else {
                        stringBuilder.append("FLOAT8");
                    }
                    break;
                case 'f':
                case 'F': // floating point number
                case 'o':
                case 'O': // floating point number
                    stringBuilder.append("FLOAT8");
                    break;
                default:
                    throw new IOException("Unknown DBF field type " + header.getFieldType(idColumn));
            }
        }
        return stringBuilder.toString();
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
