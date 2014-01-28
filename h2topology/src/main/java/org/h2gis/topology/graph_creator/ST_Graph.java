/**
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

package org.h2gis.topology.graph_creator;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.SFSUtilities;

import java.sql.*;
import java.util.List;

/**
 * ST_Graph produces two tables (nodes and edges) from an input table
 * containing a column of one-dimensional geometries.
 *
 * @author Adam Gouge
 */
public class ST_Graph extends AbstractFunction implements ScalarFunction {

    private static Integer spatialFieldIndex;

    public ST_Graph() {
        addProperty(PROP_REMARKS, "produces two tables (nodes and edges) "
                + "from an input table containing a column of one-dimensional "
                + "geometries");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGraph";
    }

    public static boolean createGraph(Connection connection, String inputTable) throws SQLException {
        return createGraph(connection, inputTable, null);
    }

    public static boolean createGraph(Connection connection,
                               String tableName,
                               String spatialFieldName) throws SQLException {
        Statement st = connection.createStatement();

        // OBTAIN THE SPATIAL FIELD INDEX.
        ResultSet tableQuery = st.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData metaData = tableQuery.getMetaData();
        int columnCount = metaData.getColumnCount();
        // Find the name of the first geometry column if not provided by the user.
        if (spatialFieldName == null) {
            List<String> geomFields = SFSUtilities.getGeometryFields(
                    connection, SFSUtilities.splitCatalogSchemaTableName(tableName));
            if (!geomFields.isEmpty()) {
                spatialFieldName = geomFields.get(0);
            } else {
                throw new SQLException("Table " + tableName + " does not contain a geometry field.");
            }
        }
        // Find the index of the spatial field.
        for (int i = 1; i <= columnCount; i++) {
            if (metaData.getColumnName(i).equalsIgnoreCase(spatialFieldName)) {
                spatialFieldIndex = i;
                break;
            }
        }
        if (spatialFieldIndex == null) {
            throw new SQLException("Geometry field " + spatialFieldName + " of table " + tableName + " not found");
        }

        // If we made it this far, the output tables were successfully created.
        return true;
    }
}
