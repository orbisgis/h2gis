package org.orbisgis.sputilities;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Just a class used to split Catalog Schema and Table. Theses components are a unique table identifier.
 * @author Nicolas Fortin
 */
public class TableLocation {
    private String catalog,schema,table;

    /**
     *
     * @param catalog Catalog name without quotes
     * @param schema Schema name without quotes
     * @param table Table name without quotes
     */
    public TableLocation(String catalog, String schema, String table) {
        this.catalog = catalog;
        this.schema = schema;
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
            sb.append("`");
            sb.append(catalog);
            sb.append("`");
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            sb.append("`");
            sb.append(schema);
            sb.append("`");
            sb.append(".");
        }
        sb.append("`");
        sb.append(table);
        sb.append("`");
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
        StringTokenizer st = new StringTokenizer(concatenatedTableLocation, ".`", true);
        boolean openQuote = false;
        StringBuilder sb = new StringBuilder();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token.equals("`")) {
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
