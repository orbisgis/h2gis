/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
package org.h2gis.drivers.dbf;

import org.h2gis.drivers.dbf.internal.DBFDriver;
import org.h2gis.drivers.dbf.internal.DbaseFileException;
import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * @author Nicolas Fortin
 */
public class DBFDriverFunction implements DriverFunction {
    public static String DESCRIPTION = "dBase III format";
    private static final int BATCH_MAX_SIZE = 100;
    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        exportTable(connection, tableReference, fileName, progress, null);
    }

    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress,String encoding) throws SQLException, IOException {
        int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
        // Read table content
        Statement st = connection.createStatement();
        ProgressVisitor lineProgress = null;
        if(!(progress instanceof EmptyProgressVisitor)) {
            ResultSet rs = st.executeQuery(String.format("select count(*) from %s", TableLocation.parse(tableReference)));
            try {
                if(rs.next()) {
                    lineProgress = progress.subProcess(rs.getInt(1));
                }
            } finally {
                rs.close();
            }
        }
        try {
            ResultSet rs = st.executeQuery(String.format("select * from %s", TableLocation.parse(tableReference)));
            try {
                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                DbaseFileHeader header = dBaseHeaderFromMetaData(resultSetMetaData);
                if(encoding != null) {
                    header.setEncoding(encoding);
                }
                header.setNumRecords(recordCount);
                DBFDriver dbfDriver = new DBFDriver();
                dbfDriver.initDriver(fileName, header);
                Object[] row = new Object[header.getNumFields()];
                while (rs.next()) {
                    for(int columnId = 0; columnId < row.length; columnId++) {
                        row[columnId] = rs.getObject(columnId + 1);
                    }
                    dbfDriver.insertRow(row);
                    if(lineProgress != null) {
                        lineProgress.endStep();
                    }
                }
                dbfDriver.close();
            } finally {
                rs.close();
            }
        } finally {
            st.close();
        }
    }

    @Override
    public String getFormatDescription(String format) {
        if(format.equalsIgnoreCase("dbf")) {
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
        return new String[] {"dbf"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[] {"dbf"};
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress, null);
    }

    /**
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param forceFileEncoding File encoding to use, null will use the provided file encoding in file header.
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,String forceFileEncoding) throws SQLException, IOException {
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(fileName, forceFileEncoding);
        try {
            DbaseFileHeader dbfHeader = dbfDriver.getDbaseFileHeader();
            // Build CREATE TABLE sql request
            Statement st = connection.createStatement();
            st.execute(String.format("CREATE TABLE %s (%s)", TableLocation.parse(tableReference),
                    getSQLColumnTypes(dbfHeader, JDBCUtilities.isH2DataBase(connection.getMetaData()))));
            st.close();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        String.format("INSERT INTO %s VALUES ( %s )", TableLocation.parse(tableReference),
                                getQuestionMark(dbfHeader.getNumFields())));
                try {
                    long batchSize = 0;
                    for (int rowId = 0; rowId < dbfDriver.getRowCount(); rowId++) {
                        Object[] values = dbfDriver.getRow(rowId);
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
            dbfDriver.close();
        }
    }

    private static class DBFType {

        char type;
        int fieldLength;
        int decimalCount;

        DBFType(char type, int fieldLength, int decimalCount) {
            super();
            this.type = type;
            this.fieldLength = fieldLength;
            this.decimalCount = decimalCount;
        }
    }

    /**
     * Generate the concatenation of ? characters. Used by PreparedStatement.
     * @param count Number of ? character to generation
     * @return Value ex: "?, ?, ?"
     */
    public static String getQuestionMark(int count) {
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
    public static String getSQLColumnTypes(DbaseFileHeader header, boolean isH2Database) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(int idColumn = 0; idColumn < header.getNumFields(); idColumn++) {
            if(idColumn > 0) {
                stringBuilder.append(", ");
            }
            String fieldName = header.getFieldName(idColumn);
            if(isH2Database) {
                //In h2 all field must be upper case in order to avoid user have to use double quotes
                fieldName = fieldName.toUpperCase();
            }
            stringBuilder.append(TableLocation.quoteIdentifier(fieldName,isH2Database));
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
                    stringBuilder.append("VARCHAR(");
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

    /**
     * Create a DBF header from the columns specified in parameter.
     * @param metaData SQL ResultSetMetadata
     * @return DbfaseFileHeader instance.
     * @throws SQLException If one or more type are not supported by DBF
     */
    public static DbaseFileHeader dBaseHeaderFromMetaData(ResultSetMetaData metaData) throws SQLException {
        DbaseFileHeader dbaseFileHeader = new DbaseFileHeader();
        for(int fieldId= 1; fieldId <= metaData.getColumnCount(); fieldId++) {
            final String fieldTypeName = metaData.getColumnTypeName(fieldId);
            // TODO postgis check field type
            if(!fieldTypeName.equalsIgnoreCase("geometry")) {
                DBFType dbfType = getDBFType(metaData.getColumnType(fieldId), fieldTypeName, metaData.getColumnDisplaySize(fieldId), metaData.getPrecision(fieldId));
                try {
                    dbaseFileHeader.addColumn(metaData.getColumnName(fieldId),dbfType.type, dbfType.fieldLength, dbfType.decimalCount);
                } catch (DbaseFileException ex) {
                    throw new SQLException(ex.getLocalizedMessage(), ex);
                }
            }
        }
        return dbaseFileHeader;
    }


    private static DBFType getDBFType(int sqlTypeId, String sqlTypeName,int length, int precision) throws SQLException {
        switch (sqlTypeId) {
            case Types.BOOLEAN:
                return new DBFType('l', 1, 0);
            case Types.BIT:
                return new DBFType('n', Math.min(3, length), 0);
            case Types.DATE:
                return new DBFType('d', 8, 0);
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
                return new DBFType('f', Math.min(20, length), Math.min(18,
                        precision));
            case Types.INTEGER:
                return new DBFType('n', Math.min(10, length), 0);
            case Types.BIGINT:
                return new DBFType('n', Math.min(18, length), 0);
            case Types.SMALLINT:
                return new DBFType('n', Math.min(5, length), 0);
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
                return new DBFType('c', Math.min(254, length), 0);
            default:
                throw new SQLException("Field type not supported by DBF : " + sqlTypeName);
        }
    }
}
