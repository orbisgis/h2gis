/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.utilities;

import org.h2gis.utilities.dbtypes.DBTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Just a class used to split Catalog Schema and Table. Theses components are a unique table identifier.
 * @author Nicolas Fortin
 */
public class TableLocation {
    private String catalog,schema,table;
    /** Recognized by H2 and Postgres */
    public static final String QUOTE_CHAR = "\"";
    private String defaultSchema = "public";
    private DBTypes dbTypes = null;
    public static final Pattern QUOTE_PATTERN = Pattern.compile("\\\"([^\\\"]+)\\\"|'([^']+)'|\\\\S+");

    /**
     * @param rs result set obtained through {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     */
    public TableLocation(ResultSet rs) throws SQLException {
        this(rs.getString("TABLE_CAT"),rs.getString("TABLE_SCHEM"),rs.getString("TABLE_NAME"));
    }

    /**
     *
     * @param catalog Catalog name without quotes
     * @param schema Schema name without quotes
     * @param table Table name without quotes
     * @param dbTypes The database type e.g : H2GIS, POSTGIS...
     */
    public TableLocation(String catalog, String schema, String table, DBTypes dbTypes) {
        if(table == null) {
            throw new IllegalArgumentException("Cannot construct table location with null table");
        }
        this.catalog = catalog == null ? "" : catalog;
        this.schema = schema  == null || schema.isEmpty() ? "" : schema;
        this.table = table;
        this.dbTypes=dbTypes;
    }

    /**
     *
     * @param catalog Catalog name without quotes
     * @param schema Schema name without quotes
     * @param table Table name without quotes
     */
    public TableLocation(String catalog, String schema, String table) {
        if(table == null || table.isEmpty()) {
            throw new IllegalArgumentException("Cannot construct table location with null or empty table");
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
     * @param schema Schema name without quotes
     * @param table Table name without quotes
     * @param dbTypes The database type e.g : H2GIS, POSTGIS...
     */
    public TableLocation(String schema, String table, DBTypes dbTypes) {
        this("",schema,table, dbTypes);
    }

    /**
     * @param table Table name without quotes
     * @param dbTypes The database type e.g : H2GIS, POSTGIS...
     */
    public TableLocation(String table, DBTypes dbTypes) {
        this("", table,dbTypes);
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

    public static String unFormat(String identifier, DBTypes dbTypes) {
        if (identifier == null || identifier.isEmpty() || dbTypes==null) {
            return identifier;
        }
        String identifierWithoutQuote = identifier.replace(QUOTE_CHAR, "");
        if(QUOTE_PATTERN.matcher(identifierWithoutQuote).find()){
            return identifierWithoutQuote;
        }
        if(dbTypes.getReservedWords().contains(identifierWithoutQuote.toUpperCase())){
            return identifierWithoutQuote;
        }
        if(!Objects.requireNonNull(dbTypes.specialNamePattern()).matcher(identifierWithoutQuote).find()) {
            return identifierWithoutQuote;
        }else {
            return identifier;
        }
    }

    /**
     * Format the identifier is necessary. Require database knowledge.
     * @param identifier Catalog,Schema,Table or Field name.
     * @param dbTypes    Type of the database.
     * @return Quoted identifier.
     */
    public static String format(String identifier, DBTypes dbTypes) {
        if (identifier == null|| identifier.isEmpty() || dbTypes==null) {
            return identifier;
        }
        if(QUOTE_PATTERN.matcher(identifier).find()){
            return identifier;
        }
        if(dbTypes.getReservedWords().contains(identifier.toUpperCase())){
            return quoteIdentifier(identifier);
        }
        if(!Objects.requireNonNull(dbTypes.specialNamePattern()).matcher(identifier).find()) {
            return quoteIdentifier(identifier);
        }else {
            return identifier;
        }
    }

    /**
     * Quote identifier only if necessary. Require database knowledge.
     * @param identifier Catalog,Schema,Table or Field name.
     * @param dbTypes    Type of the database.
     * @return Quoted identifier.
     */
    public static String quoteIdentifier(String identifier, DBTypes dbTypes) {
        if (identifier == null|| identifier.isEmpty() || dbTypes==null) {
            return identifier;
        }
        if(dbTypes.getReservedWords().contains(identifier.toUpperCase()) ||
                !Objects.requireNonNull(dbTypes.specialNamePattern()).matcher(identifier).find()) {
            return quoteIdentifier(identifier);
        } else {
            return identifier;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(!catalog.isEmpty()) {
            sb.append(format(catalog,getDbTypes()));
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            sb.append(format(schema,getDbTypes()));
            sb.append(".");
        }
        sb.append(format(table, getDbTypes()));
        return sb.toString();
    }

    /**
     * String representation of Table location, for insertion in SQL statement.
     * This function try to do not quote unnecessary components; require database type.
     * @param dbTypes Database type.
     * @return String representation of Table location
     */
    public String toString(DBTypes dbTypes) {
        StringBuilder sb = new StringBuilder();
        if(!catalog.isEmpty()) {
            sb.append(format(catalog, dbTypes));
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            sb.append(format(schema, dbTypes));
            sb.append(".");
        }
        sb.append(format(table, dbTypes));
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
        return parse(concatenatedTableLocation, DBTypes.H2);
    }


    /**
     * Convert catalog.schema.table, schema.table or table into a String array
     * instance. Non-specified schema or catalogs are converted to the empty string.
     *
     * @param tableName in the form [[Catalog.]Schema.]Table
     * @return a String array with
     * [0] = Catalog
     * [1] = Schema
     * [2] = Table
     */
    public static String[] split(String tableName) {
        List<String> parts = new LinkedList<String>();
        String catalog = "",schema = "",table = "";
        StringTokenizer st = new StringTokenizer(tableName, ".`\"", true);
        boolean openQuote = false;
        StringBuilder sb = new StringBuilder();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token.equals("`") || token.equals("\"")) {
                sb.append("\"");
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
        String[] values = parts.toArray(new String[0]);
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
        return new String[]{catalog,schema,table};
    }

    /**
     * Convert catalog.schema.table, schema.table or table into a TableLocation
     * instance. Non-specified schema or catalogs are converted to the empty
     * string.
     *
     * @param concatenatedTableLocation Table location [[Catalog.]Schema.]Table
     * @param dbTypes                   Database type.
     * @return Java beans for table location
     */
    public static TableLocation parse(String concatenatedTableLocation, DBTypes dbTypes) {
        List<String> parts = new LinkedList<String>();
        String catalog,schema,table;
        catalog = table = schema = "";
        StringTokenizer st = new StringTokenizer(concatenatedTableLocation, ".`\"", true);
        boolean openQuote = false;
        StringBuilder sb = new StringBuilder();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token.equals("`") || token.equals("\"")) {
                sb.append("\"");
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
                if(!openQuote && dbTypes != null) {
                    token = capsIdentifier(token, dbTypes);
                }
                sb.append(token);
            }
        }
        if(sb.length() != 0) {
            parts.add(sb.toString());
        }
        String[] values = parts.toArray(new String[0]);
        switch (values.length) {
            case 1:
                table = unFormat(values[0].trim(), dbTypes);
                break;
            case 2:
                schema = unFormat(values[0].trim(), dbTypes);
                table = unFormat(values[1].trim(), dbTypes);
                break;
            case 3:
                catalog = unFormat(values[0].trim(), dbTypes);
                schema = unFormat(values[1].trim(), dbTypes);
                table = unFormat(values[2].trim(), dbTypes);
        }
        return new TableLocation(catalog, schema, table, dbTypes);
    }

    /**
     * Change case of parameters to make it more user-friendly.
     *
     * @param identifier   Table, Catalog, Schema, or column name
     * @param dbTypes      Database type.
     * @return Upper or lower case version of identifier
     */
    public static String capsIdentifier(String identifier, DBTypes dbTypes) {
        if(dbTypes != null) {
            if(dbTypes == DBTypes.H2 || dbTypes == DBTypes.H2GIS) {
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

        boolean atomicEqual =  (catalog.equals(that.catalog) || catalog.isEmpty() || that.catalog.isEmpty()) &&
                (schema.equals(that.schema) || (schema.equals(defaultSchema) && that.schema.isEmpty()) ||
                        (that.schema.equals(defaultSchema) && schema.isEmpty())) &&
                table.equals(that.table);
        return  atomicEqual || toString().equals(o.toString());
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


    /**
     * Return the dbtype used by tablelocation.
     * Default is H2
     * @return the {@link DBTypes}
     */
    public DBTypes getDbTypes() {
        return dbTypes;
    }
}