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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.utilities.JDBCUtilities;

/**
 * JSON class to write a table or a resultset to a file
 * 
 * @author Erwan Bocher (CNRS)
 */
public class JsonWriteDriver {
    
    private final String tableName;
    private final File fileName;
    private final Connection connection;

    /**
     * A JSON driver to write a  table to a JSON file.
     *
     * @param connection
     * @param tableName
     * @param fileName
     */
    public JsonWriteDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }
    /**
     * Write a table to JSON format.
     *
     * @param progress
     * @throws SQLException
     * @throws java.io.IOException
     */
    public void write(ProgressVisitor progress) throws SQLException, IOException {        
        if (FileUtil.isExtensionWellFormated(fileName, "json")) {
            writeJson(progress);
        } else {
            throw new SQLException("Only .json extension is supported");
        }
    }
    
    /**
     * Write the table to JSON format.
     *
     * @param progress
     * @throws SQLException
     */
    private void writeJson(ProgressVisitor progress) throws SQLException, IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            int recordCount = JDBCUtilities.getRowCount(connection, tableName);
            if (recordCount > 0) {
                // Read table content
                Statement st = connection.createStatement();
                try {
                    JsonFactory jsonFactory = new JsonFactory();
                    JsonGenerator jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), JsonEncoding.UTF8);
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
                } finally {
                    st.close();
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
    }
}
