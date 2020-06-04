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
package org.h2gis.functions.io.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.utilities.JDBCUtilities;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON class to write a table or a resultset to a file
 * 
 * @author Erwan Bocher (CNRS)
 */
public class JsonWriteDriver {
    
    private final Connection connection;
    private final boolean deleteFile;

    /**
     * A JSON driver to write a  table to a JSON file.
     *
     * @param connection
     */
    public JsonWriteDriver(Connection connection, boolean deleteFile) {
        this.connection = connection;
        this.deleteFile=deleteFile;
    }
    
    /**
     * Write a resulset to a json file
     * 
     * @param progress
     * @param resultSet
     * @param file
     * @throws SQLException
     * @throws IOException
     */
    public void write(ProgressVisitor progress, ResultSet resultSet, File file) throws SQLException, IOException {
        write(progress, resultSet, file, null);
    }
    /**
     * Write a resulset to a json file
     *
     * @param progress
     * @param rs input resulset
     * @param fileName the output file
     * @param encoding
     * @throws SQLException
     * @throws java.io.IOException
     */
    public void write(ProgressVisitor progress, ResultSet rs, File fileName, String encoding) throws SQLException, IOException {
        if (FileUtil.isExtensionWellFormated(fileName, "json")) {
            JsonEncoding jsonEncoding = JsonEncoding.UTF8;
            if (encoding != null) {
                try {
                    jsonEncoding = JsonEncoding.valueOf(encoding);
                } catch (IllegalArgumentException ex) {
                    throw new SQLException("Only UTF-8, UTF-16BE, UTF-16LE, UTF-32BE, UTF-32LE encoding is supported");
                }
            }
            FileOutputStream fos = null;
            try {
            fos = new FileOutputStream(fileName);
            int rowCount = 0;
                int type = rs.getType();
                if (type == ResultSet.TYPE_SCROLL_INSENSITIVE || type == ResultSet.TYPE_SCROLL_SENSITIVE) {
                    rs.last();
                    rowCount = rs.getRow();
                    rs.beforeFirst();
                }
                ProgressVisitor copyProgress = progress.subProcess(rowCount);   
                try ( // Read table content
                    Statement st = connection.createStatement()) {
                    JsonFactory jsonFactory = new JsonFactory();
                    JsonGenerator jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), jsonEncoding);                    
                    try {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int numColumns = rsmd.getColumnCount();
                        while (rs.next()) {
                            jsonGenerator.writeStartObject();
                            for (int i = 1; i < numColumns + 1; i++) {
                                String column_name = rsmd.getColumnName(i);
                                switch (rsmd.getColumnType(i)) {
                                    case java.sql.Types.ARRAY:
                                        Object[] values = (Object[]) rs.getArray(i).getArray();
                                        if(values !=null){
                                            jsonGenerator.writeArrayFieldStart(column_name);
                                            for (Object value : values) {
                                                jsonGenerator.writeObject(value);
                                            }
                                            jsonGenerator.writeEndArray();
                                        }
                                        break;
                                    case java.sql.Types.BIGINT:
                                        jsonGenerator.writeObjectField(column_name, rs.getLong(i));
                                        break;
                                    case java.sql.Types.REAL:
                                        jsonGenerator.writeObjectField(column_name, rs.getFloat(i));
                                        break;
                                    case java.sql.Types.BOOLEAN:
                                        jsonGenerator.writeObjectField(column_name, rs.getBoolean(i));
                                        break;
                                    case java.sql.Types.BLOB:
                                        jsonGenerator.writeObjectField(column_name, rs.getBlob(i));
                                        break;
                                    case java.sql.Types.DOUBLE:
                                        jsonGenerator.writeObjectField(column_name, rs.getDouble(i));
                                        break;
                                    case java.sql.Types.FLOAT:
                                        jsonGenerator.writeObjectField(column_name, rs.getDouble(i));
                                        break;
                                    case java.sql.Types.INTEGER:
                                        jsonGenerator.writeObjectField(column_name, rs.getInt(i));
                                        break;
                                    case java.sql.Types.NVARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getNString(i));
                                        break;
                                    case java.sql.Types.VARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                    case java.sql.Types.CHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                    case java.sql.Types.NCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getNString(i));
                                        break;
                                    case java.sql.Types.LONGNVARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getNString(i));
                                        break;
                                    case java.sql.Types.LONGVARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                    case java.sql.Types.TINYINT:
                                        jsonGenerator.writeObjectField(column_name, rs.getByte(i));
                                        break;
                                    case java.sql.Types.SMALLINT:
                                        jsonGenerator.writeObjectField(column_name, rs.getShort(i));
                                        break;
                                    case java.sql.Types.DATE:
                                        jsonGenerator.writeObjectField(column_name, rs.getDate(i));
                                        break;
                                    case java.sql.Types.TIME:
                                        jsonGenerator.writeObjectField(column_name, rs.getTime(i));
                                        break;
                                    case java.sql.Types.TIMESTAMP:
                                        jsonGenerator.writeObjectField(column_name, rs.getTimestamp(i));
                                        break;
                                    case java.sql.Types.BINARY:
                                        jsonGenerator.writeObjectField(column_name, rs.getBytes(i));
                                        break;
                                    case java.sql.Types.VARBINARY:
                                        jsonGenerator.writeObjectField(column_name, rs.getBytes(i));
                                        break;
                                    case java.sql.Types.LONGVARBINARY:
                                        jsonGenerator.writeObjectField(column_name, rs.getBinaryStream(i));
                                        break;
                                    case java.sql.Types.BIT:
                                        jsonGenerator.writeObjectField(column_name, rs.getBoolean(i));
                                        break;
                                    case java.sql.Types.CLOB:
                                        jsonGenerator.writeObjectField(column_name, rs.getClob(i));
                                        break;
                                    case java.sql.Types.NUMERIC:
                                        jsonGenerator.writeObjectField(column_name, rs.getBigDecimal(i));
                                        break;
                                    case java.sql.Types.DECIMAL:
                                        jsonGenerator.writeObjectField(column_name, rs.getBigDecimal(i));
                                        break;
                                    case java.sql.Types.DATALINK:
                                        jsonGenerator.writeObjectField(column_name, rs.getURL(i));
                                        break;
                                    case java.sql.Types.REF:
                                        jsonGenerator.writeObjectField(column_name, rs.getRef(i));
                                        break;
                                    case java.sql.Types.STRUCT:
                                        jsonGenerator.writeObjectField(column_name, rs.getObject(i));
                                        break;
                                    case java.sql.Types.DISTINCT:
                                        jsonGenerator.writeObjectField(column_name, rs.getObject(i));
                                        break;
                                    case java.sql.Types.JAVA_OBJECT:
                                        jsonGenerator.writeObjectField(column_name, rs.getObject(i));
                                        break;
                                    default:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                }
                            }
                            jsonGenerator.writeEndObject();

                            copyProgress.endStep();
                        }
                        copyProgress.endOfProgress();
                        jsonGenerator.flush();
                        jsonGenerator.close();
                    } finally {
                        rs.close();
                    }
                }
            
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);

        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                } catch (IOException ex) {
                    throw new SQLException(ex);
                }
            }
        } else {
            throw new SQLException("Only .json extension is supported");
        }
    }    
    
    /**
     * Write the table to JSON format.
     *
     * @param progress
     * @param tableName
     * @param fileName
     * @throws SQLException
     * @throws java.io.IOException
     */
    public void write(ProgressVisitor progress,String tableName, File fileName, String encoding) throws SQLException, IOException {
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableName);
        if (matcher.find()) {
            if (tableName.startsWith("(") && tableName.endsWith(")")) {
                PreparedStatement ps = connection.prepareStatement(tableName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = ps.executeQuery();
                write(progress, resultSet, fileName, encoding);
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }        
        } else {
        if (FileUtil.isExtensionWellFormated(fileName, "json")) {
            if(deleteFile){
                Files.deleteIfExists(fileName.toPath());
            }
        FileOutputStream fos = null;
        try {
            JsonEncoding jsonEncoding = JsonEncoding.UTF8;
            if (encoding != null) {
                try {
                    jsonEncoding = JsonEncoding.valueOf(encoding);
                } catch (IllegalArgumentException ex) {
                    throw new SQLException("Only UTF-8, UTF-16BE, UTF-16LE, UTF-32BE, UTF-32LE encoding is supported");
                }
            }
            fos = new FileOutputStream(fileName);
            int recordCount = JDBCUtilities.getRowCount(connection, tableName);
            if (recordCount > 0) {
                try ( // Read table content
                    Statement st = connection.createStatement()) {
                    JsonFactory jsonFactory = new JsonFactory();
                    JsonGenerator jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), jsonEncoding);
                    ResultSet rs = st.executeQuery(String.format("select * from %s", tableName));
                    try {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int numColumns = rsmd.getColumnCount();
                        ProgressVisitor copyProgress = progress.subProcess(recordCount);
                        while (rs.next()) {
                            jsonGenerator.writeStartObject();
                            for (int i = 1; i < numColumns + 1; i++) {
                                String column_name = rsmd.getColumnName(i);
                                switch (rsmd.getColumnType(i)) {
                                    case java.sql.Types.ARRAY:
                                        Object[] values = (Object[]) rs.getArray(i).getArray();
                                        if(values !=null){
                                            jsonGenerator.writeArrayFieldStart(column_name);
                                            for (Object value : values) {
                                                jsonGenerator.writeObject(value);
                                            }
                                            jsonGenerator.writeEndArray();
                                        }
                                        break;
                                    case java.sql.Types.BIGINT:
                                        jsonGenerator.writeObjectField(column_name, rs.getLong(i));
                                        break;
                                    case java.sql.Types.REAL:
                                        jsonGenerator.writeObjectField(column_name, rs.getFloat(i));
                                        break;
                                    case java.sql.Types.BOOLEAN:
                                        jsonGenerator.writeObjectField(column_name, rs.getBoolean(i));
                                        break;
                                    case java.sql.Types.BLOB:
                                        jsonGenerator.writeObjectField(column_name, rs.getBlob(i));
                                        break;
                                    case java.sql.Types.DOUBLE:
                                        jsonGenerator.writeObjectField(column_name, rs.getDouble(i));
                                        break;
                                    case java.sql.Types.FLOAT:
                                        jsonGenerator.writeObjectField(column_name, rs.getDouble(i));
                                        break;
                                    case java.sql.Types.INTEGER:
                                        jsonGenerator.writeObjectField(column_name, rs.getInt(i));
                                        break;
                                    case java.sql.Types.NVARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getNString(i));
                                        break;
                                    case java.sql.Types.VARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                    case java.sql.Types.CHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                    case java.sql.Types.NCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getNString(i));
                                        break;
                                    case java.sql.Types.LONGNVARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getNString(i));
                                        break;
                                    case java.sql.Types.LONGVARCHAR:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                    case java.sql.Types.TINYINT:
                                        jsonGenerator.writeObjectField(column_name, rs.getByte(i));
                                        break;
                                    case java.sql.Types.SMALLINT:
                                        jsonGenerator.writeObjectField(column_name, rs.getShort(i));
                                        break;
                                    case java.sql.Types.DATE:
                                        jsonGenerator.writeObjectField(column_name, rs.getDate(i));
                                        break;
                                    case java.sql.Types.TIME:
                                        jsonGenerator.writeObjectField(column_name, rs.getTime(i));
                                        break;
                                    case java.sql.Types.TIMESTAMP:
                                        jsonGenerator.writeObjectField(column_name, rs.getTimestamp(i));
                                        break;
                                    case java.sql.Types.BINARY:
                                        jsonGenerator.writeObjectField(column_name, rs.getBytes(i));
                                        break;
                                    case java.sql.Types.VARBINARY:
                                        jsonGenerator.writeObjectField(column_name, rs.getBytes(i));
                                        break;
                                    case java.sql.Types.LONGVARBINARY:
                                        jsonGenerator.writeObjectField(column_name, rs.getBinaryStream(i));
                                        break;
                                    case java.sql.Types.BIT:
                                        jsonGenerator.writeObjectField(column_name, rs.getBoolean(i));
                                        break;
                                    case java.sql.Types.CLOB:
                                        jsonGenerator.writeObjectField(column_name, rs.getClob(i));
                                        break;
                                    case java.sql.Types.NUMERIC:
                                        jsonGenerator.writeObjectField(column_name, rs.getBigDecimal(i));
                                        break;
                                    case java.sql.Types.DECIMAL:
                                        jsonGenerator.writeObjectField(column_name, rs.getBigDecimal(i));
                                        break;
                                    case java.sql.Types.DATALINK:
                                        jsonGenerator.writeObjectField(column_name, rs.getURL(i));
                                        break;
                                    case java.sql.Types.REF:
                                        jsonGenerator.writeObjectField(column_name, rs.getRef(i));
                                        break;
                                    case java.sql.Types.STRUCT:
                                        jsonGenerator.writeObjectField(column_name, rs.getObject(i));
                                        break;
                                    case java.sql.Types.DISTINCT:
                                        jsonGenerator.writeObjectField(column_name, rs.getObject(i));
                                        break;
                                    case java.sql.Types.JAVA_OBJECT:
                                        jsonGenerator.writeObjectField(column_name, rs.getObject(i));
                                        break;
                                    default:
                                        jsonGenerator.writeObjectField(column_name, rs.getString(i));
                                        break;
                                }
                            }
                            jsonGenerator.writeEndObject();

                            copyProgress.endStep();
                        }
                        copyProgress.endOfProgress();
                        jsonGenerator.flush();
                        jsonGenerator.close();
                    } finally {
                        rs.close();
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);

        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                } catch (IOException ex) {
                    throw new SQLException(ex);
                }
            }
        } else {
            throw new SQLException("Only .json extension is supported");
        }
    }
    }   
}
