package org.h2gis.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Just a class used to split Catalog Schema and Table. Theses components are a unique table identifier.
 * @author Nicolas Fortin
 */
public class TableLocation {
    private String catalog,schema,table;
    /** Recognized by H2 and Postgres */
    private static final String QUOTE_CHAR = "\"";
    private static final Pattern POSTGRE_SPECIAL_NAME_PATTERN = Pattern.compile("[^a-z0-9_]");
    private static final Pattern H2_SPECIAL_NAME_PATTERN = Pattern.compile("[^A-Z0-9_]");
    private static final String DEFAULT_SCHEMA = "PUBLIC";

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
        this.schema = schema  == null || schema.isEmpty() ? DEFAULT_SCHEMA : schema;
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

    /**
     * Always Quote string for both H2 and Postgre compatibility
     * @param identifier Catalog,Schema,Table or Field name
     * @return Quoted Identifier
     */
    public static String quoteIdentifier(String identifier) {
        return QUOTE_CHAR+identifier.replace("\"","\"\"")+QUOTE_CHAR;
    }


    /**
     * Quote identifier only if necessary. Require database knowledge.
     * @param identifier Catalog,Schema,Table or Field name
     * @param isH2DataBase True if the quote is for H2, false if for POSTGRE
     * @return Quoted Identifier
     */
    public static String quoteIdentifier(String identifier, boolean isH2DataBase) {
        if((isH2DataBase && H2_SPECIAL_NAME_PATTERN.matcher(identifier).find()) ||
                (!isH2DataBase && POSTGRE_SPECIAL_NAME_PATTERN.matcher(identifier).find())) {
            return quoteIdentifier(identifier);
        } else {
            return identifier;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(!catalog.isEmpty()) {
            sb.append(quoteIdentifier(catalog));
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            sb.append(quoteIdentifier(schema));
            sb.append(".");
        }
        sb.append(quoteIdentifier(table));
        return sb.toString();
    }

    /**
     * String representation of Table location, for insertion in SQL statement.
     * This function try to do not quote unnecessary components; require database type.
     * @param isH2 True if H2, false if
     * @return String representation of Table location
     */
    public String toString(boolean isH2) {
        StringBuilder sb = new StringBuilder();
        if(!catalog.isEmpty()) {
            sb.append(quoteIdentifier(catalog, isH2));
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            sb.append(quoteIdentifier(schema, isH2));
            sb.append(".");
        }
        sb.append(quoteIdentifier(table, isH2));
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
