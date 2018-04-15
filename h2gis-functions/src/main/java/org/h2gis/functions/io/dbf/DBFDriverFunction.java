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
import org.h2gis.functions.io.dbf.internal.DBFDriver;
import org.h2gis.functions.io.dbf.internal.DbaseFileException;
import org.h2gis.functions.io.dbf.internal.DbaseFileHeader;
import org.h2gis.functions.io.file_table.FileEngine;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
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
import java.util.ArrayList;
import java.util.List;
import org.h2gis.functions.io.utility.FileUtil;

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
        if (FileUtil.isExtensionWellFormated(fileName, "dbf")) {
            int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
            final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
            String tableName = TableLocation.parse(tableReference, isH2).toString(isH2);
            // Read table content
            Statement st = connection.createStatement();
            ProgressVisitor lineProgress = null;
            if (!(progress instanceof EmptyProgressVisitor)) {
                ResultSet rs = st.executeQuery(String.format("select count(*) from %s", tableName));
                try {
                    if (rs.next()) {
                        lineProgress = progress.subProcess(rs.getInt(1));
                    }
                } finally {
                    rs.close();
                }
            }
            try {
                ResultSet rs = st.executeQuery(String.format("select * from %s", tableName));
                try {
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();                    
                    ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
                    DbaseFileHeader header = dBaseHeaderFromMetaData(resultSetMetaData, columnIndexes);
                    if (encoding != null) {
                        header.setEncoding(encoding);
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
                } finally {
                    rs.close();
                }
            } finally {
                st.close();
            }
        } else {
            throw new SQLException("Only .dbf extension is supported");
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
    public boolean isSpatialFormat(String extension) {
        return false;
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress, null);
    }

    /**
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param progress monitor
     * @param forceFileEncoding File encoding to use, null will use the provided file encoding in file header.
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,String forceFileEncoding) throws SQLException, IOException {
        if (FileUtil.isFileImportable(fileName, "dbf")) {
            DBFDriver dbfDriver = new DBFDriver();
            dbfDriver.initDriverFromFile(fileName, forceFileEncoding);
            final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
            String parsedTable = TableLocation.parse(tableReference, isH2).toString(isH2);
            DbaseFileHeader dbfHeader = dbfDriver.getDbaseFileHeader();
            ProgressVisitor copyProgress = progress.subProcess((int) (dbfDriver.getRowCount() / BATCH_MAX_SIZE));
            if (dbfHeader.getNumFields() == 0) {
                JDBCUtilities.createEmptyTable(connection, parsedTable);
            } else {
                try {
                    // Build CREATE TABLE sql request
                    Statement st = connection.createStatement();
                    List<Column> otherCols = new ArrayList<Column>(dbfHeader.getNumFields() + 1);
                    for (int idColumn = 0; idColumn < dbfHeader.getNumFields(); idColumn++) {
                        otherCols.add(new Column(dbfHeader.getFieldName(idColumn), 0));
                    }
                    String pkColName = FileEngine.getUniqueColumnName(H2TableIndex.PK_COLUMN_NAME, otherCols);
                    st.execute(String.format("CREATE TABLE %s (" + pkColName + " SERIAL PRIMARY KEY, %s)", parsedTable,
                            getSQLColumnTypes(dbfHeader, isH2)));
                    st.close();
                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement(
                                String.format("INSERT INTO %s VALUES ( %s )", parsedTable,
                                        getQuestionMark(dbfHeader.getNumFields() + 1)));
                        try {
                            long batchSize = 0;
                            for (int rowId = 0; rowId < dbfDriver.getRowCount(); rowId++) {
                                preparedStatement.setObject(1, rowId + 1);
                                Object[] values = dbfDriver.getRow(rowId);
                                for (int columnId = 0; columnId < values.length; columnId++) {
                                    preparedStatement.setObject(columnId + 2, values[columnId]);
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
                            if (batchSize > 0) {
                                preparedStatement.executeBatch();
                            }
                        } finally {
                            preparedStatement.close();
                        }
                    } catch (Exception ex) {
                        connection.createStatement().execute("DROP TABLE IF EXISTS " + parsedTable);
                        throw new SQLException(ex.getLocalizedMessage(), ex);
                    }
                } finally {
                    dbfDriver.close();
                    copyProgress.endOfProgress();
                }
            }
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
     * @param isH2Database true if H2 database
     * @return Array of columns ex: ["id INTEGER", "len DOUBLE"]
     * @throws IOException
     */
    public static String getSQLColumnTypes(DbaseFileHeader header, boolean isH2Database) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(int idColumn = 0; idColumn < header.getNumFields(); idColumn++) {
            if(idColumn > 0) {
                stringBuilder.append(", ");
            }
            String fieldName = TableLocation.capsIdentifier(header.getFieldName(idColumn), isH2Database);
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
