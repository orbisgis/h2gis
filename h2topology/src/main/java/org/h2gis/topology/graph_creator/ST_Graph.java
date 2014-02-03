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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.SpatialResultSet;

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
    private static final GeometryFactory GF = new GeometryFactory();
    private static String nodesName;
    private static String edgesName;

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

        nodesName = tableName + "_nodes";
        edgesName = tableName + "_edges";

        Connection wrappedConnection = SFSUtilities.wrapConnection(connection);

        Statement st = wrappedConnection.createStatement();
        getSpatialFieldIndex(connection, tableName, spatialFieldName, st);
        setupOutputTables(tableName, st);

        updateTables(wrappedConnection);

        // If we made it this far, the output tables were successfully created.
        return true;
    }

    private static void getSpatialFieldIndex(Connection connection, String tableName, String spatialFieldName, Statement st) throws SQLException {
        // OBTAIN THE SPATIAL FIELD INDEX.
        ResultSet tableQuery = st.executeQuery("SELECT * FROM " + tableName + " LIMIT 0;");
        try {
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
        } finally {
            tableQuery.close();
        }
        if (spatialFieldIndex == null) {
            throw new SQLException("Geometry field " + spatialFieldName + " of table " + tableName + " not found");
        }
    }

    private static void setupOutputTables(String tableName, Statement st) throws SQLException {
        st.execute("CREATE TABLE " + nodesName + " (node_id INT PRIMARY KEY, the_geom POINT);");

        st.execute("CREATE TABLE " + edgesName + " AS SELECT * FROM " + tableName + ";" +
                "ALTER TABLE " + edgesName + " ADD COLUMN edge_id INT IDENTITY;" +
                "ALTER TABLE " + edgesName + " ADD COLUMN start_node INTEGER;" +
                "ALTER TABLE " + edgesName + " ADD COLUMN end_node INTEGER;");
    }

    private static void updateTables(Connection conn) throws SQLException {
        final Quadtree quadtree = new Quadtree();

        SpatialResultSet nodesTable =
                conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).
                        executeQuery("SELECT * FROM " + nodesName).
                        unwrap(SpatialResultSet.class);
        SpatialResultSet edgesTable =
                conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).
                        executeQuery("SELECT * FROM " + edgesName).
                        unwrap(SpatialResultSet.class);
        try {
            int node_id = 0;
            while (edgesTable.next()) {
                final Geometry geom = edgesTable.getGeometry(spatialFieldIndex);
                final Coordinate[] coordinates = geom.getCoordinates();

                node_id = insertNode(nodesTable, edgesTable, node_id, coordinates[0], quadtree, "start_node");
                node_id = insertNode(nodesTable, edgesTable, node_id, coordinates[coordinates.length - 1], quadtree, "end_node");
                edgesTable.updateRow();
            }
        } finally {
            nodesTable.close();
            edgesTable.close();
        }
    }

    private static int insertNode(SpatialResultSet nodesTable,
                                  SpatialResultSet edgesTable,
                                  int node_id,
                                  Coordinate coord,
                                  Quadtree quadtree,
                                  String edgeColumnName) throws SQLException {
        Envelope envelope = new Envelope(coord);
        final List nearbyNodes = quadtree.query(envelope);
        if (nearbyNodes.size() > 0) {
            edgesTable.updateInt(edgeColumnName, (Integer) nearbyNodes.get(0));
        } else {
            nodesTable.moveToInsertRow();
            nodesTable.updateInt("node_id", ++node_id);
            nodesTable.updateGeometry("the_geom", GF.createPoint(coord));
            nodesTable.insertRow();
            quadtree.insert(envelope, node_id);
            edgesTable.updateInt(edgeColumnName, node_id);
        }
        return node_id;
    }
}
