/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.dbf;

import org.h2.table.Column;
import org.h2.util.JdbcUtils;
import org.h2.value.TypeInfo;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.dbf.internal.DBFDriver;
import org.h2gis.functions.io.dbf.internal.DbaseFileException;
import org.h2gis.functions.io.dbf.internal.DbaseFileHeader;
import org.h2gis.functions.io.file_table.FileEngine;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2gis.utilities.FileUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

/**
 * @author Erwan Bocher, CNRS
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class DBFDriverFunction implements DriverFunction {

    public static String DESCRIPTION = "dBase III format";
    private static final int BATCH_MAX_SIZE = 200;

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName,  null,false,progress);
    }

    @Override
    public String[]  exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName,  null, deleteFiles,progress);
    }

    @Override
    public String[]  exportTable(Connection connection, String tableReference, File fileName, String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection, tableReference, fileName, progress);
        if (!FileUtilities.isExtensionWellFormated(fileName, "dbf")) {
            throw new SQLException("Only .dbf extension is supported");
        }
        if(deleteFiles){
            Files.deleteIfExists(fileName.toPath());
        }
        else if (fileName.exists()) {
            throw new IOException("The dbf file already exist.");
        }
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableReference);
        if (matcher.find()) {
            if (tableReference.startsWith("(") && tableReference.endsWith(")")) {
                    PreparedStatement ps = connection.prepareStatement(tableReference, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    JDBCUtilities.attachCancelResultSet(ps, progress);
                    ResultSet rs = ps.executeQuery();
                    int recordCount = 0;
                    rs.last();
                    recordCount = rs.getRow();
                    rs.beforeFirst();
                    ProgressVisitor copyProgress = progress.subProcess(recordCount);
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
                    DbaseFileHeader header = dBaseHeaderFromMetaData(resultSetMetaData, columnIndexes);
                    if (options != null && !options.isEmpty()) {
                        header.setEncoding(options);
                    }
                    header.setNumRecords(recordCount);
                    DBFDriver dbfDriver = new DBFDriver();
                    dbfDriver.initDriver(fileName, header);
                    Object[] row = new Object[header.getNumFields()];
                    while (rs.next()) {
                        int i = 0;
                        for (Integer index : columnIndexes) {
                            row[i++] = rs.getObject(index);
                        }
                        dbfDriver.insertRow(row);
                        if (copyProgress != null) {
                            copyProgress.endStep();
                        }
                    }
                    dbfDriver.close();
                    return new String[]{tableReference};

            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }

        } else {
                final DBTypes dbType = DBUtils.getDBType(connection);
                String outputTable = TableLocation.parse(tableReference, dbType).toString();
                int recordCount = JDBCUtilities.getRowCount(connection, outputTable);

                // Read table content
                Statement st = connection.createStatement();
                JDBCUtilities.attachCancelResultSet(st, progress);
                ProgressVisitor lineProgress = null;
                if (!(progress instanceof EmptyProgressVisitor)) {
                    try (ResultSet rs = st.executeQuery(String.format("select count(*) from %s", outputTable))) {
                        if (rs.next()) {
                            lineProgress = progress.subProcess(rs.getInt(1));
                        }
                    }
                }
                try {
                    try (ResultSet rs = st.executeQuery(String.format("select * from %s", outputTable))) {
                        ResultSetMetaData resultSetMetaData = rs.getMetaData();
                        ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
                        DbaseFileHeader header = dBaseHeaderFromMetaData(resultSetMetaData, columnIndexes);
                        if (options != null&& !options.isEmpty()) {
                            header.setEncoding(options);
                        }
                        header.setNumRecords(recordCount);
                        DBFDriver dbfDriver = new DBFDriver();
                        dbfDriver.initDriver(fileName, header);
                        Object[] row = new Object[header.getNumFields()];
                        while (rs.next()) {
                            int i = 0;
                            for (Integer index : columnIndexes) {
                                row[i++] = rs.getObject(index);
                            }
                            dbfDriver.insertRow(row);
                            if (lineProgress != null) {
                                lineProgress.endStep();
                            }
                        }
                        dbfDriver.close();
                        return new String[]{outputTable};
                    }
                } finally {
                    st.close();
                }
        }
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName,String encoding, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName,  encoding, false,progress);
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
        return new String[] {"dbf", "dbf.gz"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[] {"dbf", "dbf.gz"};
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return false;
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
       return importFile(connection, tableReference, fileName, null, progress);
    }

    @Override
    public  String[]  importFile(Connection connection, String tableReference, File fileName,String forceFileEncoding, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName,forceFileEncoding, false, progress);
    }

    @Override
    public  String[]  importFile(Connection connection, String tableReference, File fileName,
                           boolean deleteTables,ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName,null, deleteTables, progress);
    }

    @Override
    public  String[]  importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection, tableReference,fileName,progress);
        if (FileUtilities.isFileImportable(fileName, "dbf")) {
            final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
            final DBTypes dbType = DBUtils.getDBType(connection);
            TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
            String outputTable = requestedTable.toString();

            if (deleteTables) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + outputTable);

                stmt.close();
            }
            DBFDriver dbfDriver = new DBFDriver();
            dbfDriver.initDriverFromFile(fileName, options);
            DbaseFileHeader dbfHeader = dbfDriver.getDbaseFileHeader();
            ProgressVisitor copyProgress = progress.subProcess((int) (dbfDriver.getRowCount() / BATCH_MAX_SIZE));
            if (dbfHeader.getNumFields() == 0) {
                JDBCUtilities.createEmptyTable(connection, outputTable);
            } else {
                try {
                    try ( // Build CREATE TABLE sql request
                          Statement st = connection.createStatement()) {
                        List<Column> otherCols = new ArrayList<>(dbfHeader.getNumFields() + 1);
                        String types = getSQLColumnTypes(dbfHeader, DBUtils.getDBType(connection), otherCols);
                        String pkColName = FileEngine.getUniqueColumnName(H2TableIndex.PK_COLUMN_NAME, otherCols);
                        st.execute(String.format("CREATE TABLE %s (" + pkColName + " SERIAL PRIMARY KEY, %s)", outputTable,
                                types));
                    }
                    try {
                        connection.setAutoCommit(false);
                        int columnCount = dbfDriver.getFieldCount();
                        try (PreparedStatement preparedStatement = connection.prepareStatement(
                                String.format("INSERT INTO %s VALUES ( %s )", outputTable,
                                        getQuestionMark(dbfHeader.getNumFields() + 1)))) {
                            JDBCUtilities.attachCancelResultSet(preparedStatement, progress);
                            long batchSize = 0;
                            for (int rowId = 0; rowId < dbfDriver.getRowCount(); rowId++) {
                                preparedStatement.setObject(1, rowId + 1);
                                for (int columnId = 0; columnId < columnCount; columnId++) {
                                    JdbcUtils.set(preparedStatement,columnId + 2, dbfDriver.getField(rowId, columnId), null);
                                }
                                preparedStatement.addBatch();
                                batchSize++;
                                if (batchSize >= BATCH_MAX_SIZE) {
                                    preparedStatement.executeBatch();
                                    connection.commit();
                                    preparedStatement.clearBatch();
                                    batchSize = 0;
                                    copyProgress.endStep();
                                }
                            }
                            if (batchSize > 0) {
                                preparedStatement.executeBatch();
                                connection.commit();
                                preparedStatement.clearBatch();
                            }
                            connection.setAutoCommit(true);
                        }
                    } catch (Exception ex) {
                        connection.createStatement().execute("DROP TABLE IF EXISTS " + outputTable);
                        throw new SQLException(ex.getLocalizedMessage(), ex);
                    }
                } finally {
                    dbfDriver.close();
                    copyProgress.endOfProgress();
                }
                return new String[]{outputTable};
            }
        }
        return null;
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
     * @param cols array columns that will be populated
     * @return Array of columns ex: ["id INTEGER", "len DOUBLE"]
     * @throws IOException
     */
    public static String getSQLColumnTypes(DbaseFileHeader header, DBTypes dbTypes, List<Column> cols) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(int idColumn = 0; idColumn < header.getNumFields(); idColumn++) {
            if(idColumn > 0) {
                stringBuilder.append(", ");
            }
            String columnName = header.getFieldName(idColumn);
            String fieldName = TableLocation.capsIdentifier(columnName, dbTypes);
            stringBuilder.append(TableLocation.quoteIdentifier(fieldName,dbTypes));
            stringBuilder.append(" ");
            switch (header.getFieldType(idColumn)) {
                // (L)logical (T,t,F,f,Y,y,N,n)
                case 'l':
                case 'L':
                    stringBuilder.append("BOOLEAN");
                    cols.add(new Column(columnName, TypeInfo.TYPE_BOOLEAN));
                    break;
                // (C)character (String)
                case 'c':
                case 'C':
                    cols.add(new Column(columnName, TypeInfo.TYPE_VARCHAR));
                    stringBuilder.append("VARCHAR(");
                    // Append size
                    int length = header.getFieldLength(idColumn);
                    stringBuilder.append(length);
                    stringBuilder.append(")");
                    break;
                // (D)date (Date)
                case 'd':
                case 'D':
                    cols.add(new Column(columnName, TypeInfo.TYPE_DATE));
                    stringBuilder.append("DATE");
                    break;
                // (F)floating (Double)
                case 'n':
                case 'N':
                    if ((header.getFieldDecimalCount(idColumn) == 0)) {
                        if ((header.getFieldLength(idColumn) >= 0)
                                && (header.getFieldLength(idColumn) < 10)) {
                            stringBuilder.append("INT4");
                            cols.add(new Column(columnName, TypeInfo.TYPE_INTEGER));
                        } else {
                            stringBuilder.append("INT8");
                            cols.add(new Column(columnName, TypeInfo.TYPE_BIGINT));
                        }
                    } else {
                        stringBuilder.append("FLOAT8");
                        cols.add(new Column(columnName, TypeInfo.TYPE_DOUBLE));
                    }
                    break;
                case 'f':
                case 'F': // floating point number
                case 'o':
                case 'O': // floating point number
                    stringBuilder.append("FLOAT8");
                    cols.add(new Column(columnName, TypeInfo.TYPE_DOUBLE));
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
     * @param retainedColumns list of column indexes
     * @return DbfaseFileHeader instance.
     * @throws SQLException If one or more type are not supported by DBF
     */
    public static DbaseFileHeader dBaseHeaderFromMetaData(ResultSetMetaData metaData, List<Integer> retainedColumns) throws SQLException {
        DbaseFileHeader dbaseFileHeader = new DbaseFileHeader();
        for(int fieldId= 1; fieldId <= metaData.getColumnCount(); fieldId++) {
            final String fieldTypeName = metaData.getColumnTypeName(fieldId);
            // TODO postgis check field type
            if(!fieldTypeName.equalsIgnoreCase("geometry")) {
                DBFType dbfType = getDBFType(metaData.getColumnType(fieldId), fieldTypeName, metaData.getColumnDisplaySize(fieldId), metaData.getPrecision(fieldId));
                try {
                    dbaseFileHeader.addColumn(metaData.getColumnName(fieldId),dbfType.type, dbfType.fieldLength, dbfType.decimalCount);
                    retainedColumns.add(fieldId);
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
            case Types.DECIMAL:
            case Types.REAL:
                // +1 because Field length is including the decimal separator
                return new DBFType('f', Math.min(20, length + 1), Math.min(18,
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
