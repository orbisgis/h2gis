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

package org.h2gis.utilities;

import org.h2.tools.SimpleResultSet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility in order to simplify table function usage
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class TableUtilities {

   private TableUtilities() {
       // This is a utility class.
   }

    /**
     * Copy fields from table into a {@link org.h2.tools.SimpleResultSet}
     *
     * @param connection Active connection
     * @param rs Result set that will receive columns
     * @param tableLocation Import columns from this table
     *
     * @throws SQLException Error
     */
    public static void copyFields(Connection connection, SimpleResultSet rs, TableLocation tableLocation) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet columnsRs = meta.getColumns(tableLocation.getCatalog(null), tableLocation.getSchema(null),
                tableLocation.getTable(), null);
        Map<Integer, Object[]> columns = new HashMap<Integer, Object[]>();
        int COLUMN_NAME = 0, COLUMN_TYPE = 1, COLUMN_TYPENAME = 2, COLUMN_PRECISION = 3, COLUMN_SCALE = 4;
        try {
            while (columnsRs.next()) {
                Object[] columnInfoObjects = new Object[COLUMN_SCALE + 1];
                columnInfoObjects[COLUMN_NAME] = columnsRs.getString("COLUMN_NAME");
                columnInfoObjects[COLUMN_TYPE] = columnsRs.getInt("DATA_TYPE");
                columnInfoObjects[COLUMN_TYPENAME] = columnsRs.getString("TYPE_NAME");
                columnInfoObjects[COLUMN_PRECISION] = columnsRs.getInt("COLUMN_SIZE");
                columnInfoObjects[COLUMN_SCALE] = columnsRs.getInt("DECIMAL_DIGITS");
                columns.put(columnsRs.getInt("ORDINAL_POSITION"), columnInfoObjects);
            }
        } finally {
            columnsRs.close();
        }
        for(int i=1;i<=columns.size();i++) {
            Object[] columnInfoObjects = columns.get(i);
            rs.addColumn((String)columnInfoObjects[COLUMN_NAME], (Integer)columnInfoObjects[COLUMN_TYPE],
                    (String)columnInfoObjects[COLUMN_TYPENAME], (Integer)columnInfoObjects[COLUMN_PRECISION]
                    , (Integer)columnInfoObjects[COLUMN_SCALE]);
        }
    }

    /**
     * Return true if this connection only wants the list of columns.
     * This is a hack. See: https://groups.google.com/forum/#!topic/h2-database/NHH0rDeU258
     *
     * @param connection Connection
     *
     * @return True if this connection only wants the list of columns
     *
     * @throws java.sql.SQLException
     */
    public static boolean isColumnListConnection(Connection connection) throws SQLException {
        return connection.getMetaData().getURL().equals("jdbc:columnlist:connection");
    }
    
    /**
     * Convert an input table String to a TableLocation
     *
     * @param connection Connection
     * @param inputTable Input table
     *
     * @return corresponding TableLocation
     *
     * @throws SQLException
     */
    public static TableLocation parseInputTable(Connection connection,
                                                String inputTable) throws SQLException {
       return TableLocation.parse(inputTable, JDBCUtilities.isH2DataBase(connection.getMetaData()));
    }
    
    
    /**
     * Suffix a TableLocation
     *
     * @param inputTable Input table
     * @param suffix     Suffix
     *
     * @return suffixed TableLocation
     */
    public static TableLocation suffixTableLocation(TableLocation inputTable,
                                                    String suffix) {
        return new TableLocation(inputTable.getCatalog(), inputTable.getSchema(),
                inputTable.getTable() + suffix);
    }
    
    /**
     * Return the table identifier in the best fit depending on database type
     *
     * @param requestedTable Catalog and schema used
     * @param tableName Table without quotes
     * @param isH2 True if H2, false if PostGRES
     *
     * @return Find table identifier
     */
    public static String caseIdentifier(TableLocation requestedTable, String tableName, boolean isH2) {
        return new TableLocation(requestedTable.getCatalog(), requestedTable.getSchema(),
                TableLocation.parse(tableName, isH2).getTable()).toString();
    }
}
