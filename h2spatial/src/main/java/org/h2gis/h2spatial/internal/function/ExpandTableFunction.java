package org.h2gis.h2spatial.internal.function;

import org.h2.tools.SimpleResultSet;
import org.h2gis.utilities.TableLocation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility in order to simplify table function usage
 * @author Nicolas Fortin
 */
public class ExpandTableFunction {
    public static void copyFields(Connection connection, SimpleResultSet rs, TableLocation tableLocation) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet columnsRs = meta.getColumns(tableLocation.getCatalog(), tableLocation.getSchema(),
                tableLocation.getTable().toUpperCase(), null);
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
}
