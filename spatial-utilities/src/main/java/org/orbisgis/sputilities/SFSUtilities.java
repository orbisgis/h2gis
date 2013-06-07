/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.orbisgis.sputilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic utilities function to retrieve spatial metadata trough SFS specification
 * @author Nicolas Fortin
 */
public class SFSUtilities {
    /**
     * Convert catalog.schema.table, schema.table or table into TableLocation instance.
     * Not specified schema or catalog are converted into an empty string.
     * @param concatenatedTableLocation Table location
     * @return Java beans for table location
     */
    public static TableLocation splitCatalogSchemaTableName(String concatenatedTableLocation) {
        String[] values = concatenatedTableLocation.split(".");
        String catalog,schema,table;
        catalog = schema = table = "";
        switch (values.length) {
            case 0:
                table = concatenatedTableLocation;
                break;
            case 2:
                schema = values[0];
                table = values[1];
                break;
            case 3:
                catalog = values[0];
                schema = values[1];
                table = values[2];
        }
        return new TableLocation(catalog,schema,table);
    }


    /**
     * Find geometry fields name of a table.
     * @param connection Active connection
     * @param location Table location
     * @return A list of Geometry fields name
     * @throws SQLException
     */
    public static List<String> getGeometryFields(Connection connection,TableLocation location) throws SQLException {
        return getGeometryFields(connection, location.getCatalog(), location.getSchema(), location.getTable());
    }

    /**
     * Find geometry fields name of a table.
     * @param connection Active connection
     * @param catalog Catalog that contain schema (empty for default catalog)
     * @param schema Schema that contain table (empty for default schema)
     * @param table Table name (case insensitive)
     * @return A list of Geometry fields name
     * @throws SQLException
     */
    public static List<String> getGeometryFields(Connection connection,String catalog, String schema, String table) throws SQLException {
        List<String> fieldsName = new LinkedList<String>();
        Integer catalogIndex = null;
        Integer schemaIndex = null;
        Integer tableIndex = 1;
        StringBuilder sb = new StringBuilder("SELECT f_geometry_column from geometry_columns where ");
        if(!catalog.isEmpty()) {
            sb.append("UPPER(f_catalog_name) = ? AND ");
            catalogIndex = 1;
            tableIndex++;
        }
        if(!schema.isEmpty()) {
            sb.append("UPPER(f_schema_name) = ? AND ");
            schemaIndex = tableIndex.intValue();
            tableIndex++;
        }
        sb.append("UPPER(f_table_name) = ?");
        PreparedStatement geomStatement = connection.prepareStatement(sb.toString());
        if(catalogIndex!=null) {
            geomStatement.setString(catalogIndex,catalog.toUpperCase());
        }
        if(schemaIndex!=null) {
            geomStatement.setString(schemaIndex,schema.toUpperCase());
        }
        geomStatement.setString(tableIndex,table.toUpperCase());
        ResultSet geomResultSet = geomStatement.executeQuery();
        while (geomResultSet.next()) {
            fieldsName.add(geomResultSet.getString(1));
        }
        return fieldsName;
    }

    /**
     * Define table location
     */
    public static class TableLocation {
        private String catalog,schema,table;
        public TableLocation(String catalog, String schema, String table) {
            this.catalog = catalog;
            this.schema = schema;
            this.table = table;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if(!catalog.isEmpty()) {
                sb.append(catalog);
                sb.append(".");
            }
            if(!schema.isEmpty()) {
                sb.append(schema);
                sb.append(".");
            }
            sb.append(table);
            return sb.toString();
        }

        public String getCatalog() {
            return catalog;
        }

        public String getSchema() {
            return schema;
        }

        public String getTable() {
            return table;
        }
    }
}
