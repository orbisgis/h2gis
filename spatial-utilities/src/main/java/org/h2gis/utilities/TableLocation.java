package org.h2gis.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Just a class used to split Catalog Schema and Table. Theses components are a unique table identifier.
 * @author Nicolas Fortin
 */
public class TableLocation {
    private String catalog,schema,table;
    /** Recognized by H2 and Postgres */
    private static final String QUOTE_CHAR = "\"";

    /**
     * @param rs result set obtained through {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     * @throws SQLException
     */
    public TableLocation(ResultSet rs) throws SQLException {
        this(rs.getString("TABLE_CAT"),rs.getString("TABLE_SCHEM"),rs.getString("TABLE_NAME"));
    }

    /**
     *
     * @param catalog Catalog name without quotes
     * @param schema Schema name without quotes
     * @param table Table name without quotes
     */
    public TableLocation(String catalog, String schema, String table) {
        if(table == null) {
            throw new IllegalArgumentException("Cannot construct table location with null table");
        }
        this.catalog = catalog == null ? "" : catalog;
        this.schema = schema  == null ? "" : schema;
        this.table = table;
    }

    /**
     * @param schema Schema name without quotes
     * @param table Table name without quotes
     */
    public TableLocation(String schema, String table) {
        this("",schema,table);
    }

    /**
     * @param table Table name without quotes
     */
    public TableLocation(String table) {
        this("", table);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(!catalog.isEmpty()) {
            boolean doQuote = catalog.contains(" ");
            if(doQuote) {
                sb.append(QUOTE_CHAR);
            }
            sb.append(catalog);
            if(doQuote) {
                sb.append(QUOTE_CHAR);
            }
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            boolean doQuote = schema.contains(" ");
            if(doQuote) {
                sb.append(QUOTE_CHAR);
            }
            sb.append(schema);
            if(doQuote) {
                sb.append(QUOTE_CHAR);
            }
            sb.append(".");
        }
        boolean doQuote= table.contains(" ") || !table.equals(table.toLowerCase());
        if(doQuote) {
            sb.append(QUOTE_CHAR);
        }
        sb.append(table);
        if(doQuote) {
            sb.append(QUOTE_CHAR);
        }
        return sb.toString();
    }

    /**
     * @return Table catalog name (database)
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Convert catalog.schema.table, schema.table or table into TableLocation instance.
     * Not specified schema or catalog are converted into an empty string.
     * @param concatenatedTableLocation Table location [[Catalog.]Schema.]Table
     * @return Java beans for table location   Sample Text
     */
    public static TableLocation parse(String concatenatedTableLocation) {
        List<String> parts = new LinkedList<String>();
        String catalog,schema,table;
        catalog = schema = table = "";
        StringTokenizer st = new StringTokenizer(concatenatedTableLocation, ".`\"", true);
        boolean openQuote = false;
        StringBuilder sb = new StringBuilder();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token.equals("`") || token.equals("\"")) {
                openQuote = !openQuote;
            } else if(token.equals(".")) {
                if(openQuote) {
                    // Still in part
                    sb.append(token);
                } else {
                    // end of part
                    parts.add(sb.toString());
                    sb = new StringBuilder();
                }
            } else {
                sb.append(token);
            }
        }
        if(sb.length() != 0) {
            parts.add(sb.toString());
        }
        String[] values = parts.toArray(new String[parts.size()]);

        switch (values.length) {
            case 1:
                table = values[0].trim();
                break;
            case 2:
                schema = values[0].trim();
                table = values[1].trim();
                break;
            case 3:
                catalog = values[0].trim();
                schema = values[1].trim();
                table = values[2].trim();
        }
        return new TableLocation(catalog,schema,table);
    }

    /**
     * @return Table schema name
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @return Table name
     */
    public String getTable() {
        return table;
    }
}
