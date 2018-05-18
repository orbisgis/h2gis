/*
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
    private static final Pattern POSTGRE_SPECIAL_NAME_PATTERN = Pattern.compile("^[a-z]{1,1}[a-z0-9_]*$");
    private static final Pattern H2_SPECIAL_NAME_PATTERN = Pattern.compile("^[A-Z]{1,1}[A-Z0-9_]*$");
    private String defaultSchema = "PUBLIC";

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
        this.schema = schema  == null || schema.isEmpty() ? "" : schema;
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
        if((isH2DataBase && (Constants.H2_RESERVED_WORDS.contains(identifier.toUpperCase())
                        || !H2_SPECIAL_NAME_PATTERN.matcher(identifier).find())) ||
                (!isH2DataBase && (Constants.POSTGIS_RESERVED_WORDS.contains(identifier.toUpperCase())
                        || !POSTGRE_SPECIAL_NAME_PATTERN.matcher(identifier).find()))) {
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
     * @param defaultValue Return this value if this attribute is not defined.
     * @return Table catalog name (database)
     */
    public String getCatalog(String defaultValue) {
        return catalog.isEmpty() ? defaultValue : catalog;
    }

    /**
     * Convert catalog.schema.table, schema.table or table into a TableLocation
     * instance. Non-specified schema or catalogs are converted to the empty
     * string.
     *
     * @param concatenatedTableLocation Table location [[Catalog.]Schema.]Table
     * @return Java beans for table location
     */
    public static TableLocation parse(String concatenatedTableLocation) {
        return parse(concatenatedTableLocation, null);
    }

    /**
     * Convert catalog.schema.table, schema.table or table into a TableLocation
     * instance. Non-specified schema or catalogs are converted to the empty
     * string.
     *
     * @param concatenatedTableLocation Table location [[Catalog.]Schema.]Table
     * @param isH2Database              True if H2, False if PostGreSQL, null if unknown
     * @return Java beans for table location
     */
    public static TableLocation parse(String concatenatedTableLocation, Boolean isH2Database) {
        List<String> parts = new LinkedList<String>();
        String catalog,schema,table;
        catalog = table = schema = "";
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
                if(!openQuote && isH2Database != null) {
                    token = capsIdentifier(token, isH2Database);
                }
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
     * Change case of parameters to make it more user-friendly.
     *
     * @param identifier   Table, Catalog, Schema, or column name
     * @param isH2Database True if H2, False if PostGreSQL, null if unknown
     * @return Upper or lower case version of identifier
     */
    public static String capsIdentifier(String identifier, Boolean isH2Database) {
        if(isH2Database != null) {
            if(isH2Database) {
                return identifier.toUpperCase();
            } else {
                return identifier.toLowerCase();
            }
        } else {
            return identifier;
        }
    }

    /**
     * @return Table schema name
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param defaultValue Return this value if this attribute is not defined.
     * @return Table schema name
     */
    public String getSchema(String defaultValue) {
        return schema.isEmpty() ? defaultValue : schema;
    }
    /**
     * @return Table name
     */
    public String getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableLocation)) return false;

        TableLocation that = (TableLocation) o;

        return  (catalog.equals(that.catalog) || catalog.isEmpty() || that.catalog.isEmpty()) &&
                (schema.equals(that.schema) || (schema.equals(defaultSchema) && that.schema.isEmpty()) ||
                (that.schema.equals(defaultSchema) && schema.isEmpty())) &&
                table.equals(that.table);
    }

    @Override
    public int hashCode() {
        int result = catalog.hashCode();
        result = 31 * result + schema.hashCode();
        result = 31 * result + table.hashCode();
        return result;
    }

    /**
     * @param defaultSchema Default connection schema, used for table location equality test.
     */
    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }
}
