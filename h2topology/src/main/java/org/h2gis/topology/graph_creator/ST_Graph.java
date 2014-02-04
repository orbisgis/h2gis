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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ST_Graph produces two tables (nodes and edges) from an input table
 * containing a column of one-dimensional geometries.
 *
 * @author Adam Gouge
 */
public class ST_Graph extends AbstractFunction implements ScalarFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ST_Graph.class);
    private static Integer spatialFieldIndex;
    private static final GeometryFactory GF = new GeometryFactory();
    private static Quadtree quadtree;
    private static Connection connection;
    private static String tableName;
    private static String nodesName;
    private static String edgesName;
    public static final String THE_GEOM = "the_geom";
    public static final String NODE_ID = "node_id";
    public static final String EDGE_ID = "edge_id";
    public static final String START_NODE = "start_node";
    public static final String END_NODE = "end_node";
    private static double tolerance;
    private static boolean orientBySlope;

    public ST_Graph() {
        addProperty(PROP_REMARKS, "produces two tables (nodes and edges) "
                + "from an input table containing a column of one-dimensional "
                + "geometries");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGraph";
    }

    public static boolean createGraph(Connection connection,
                                      String inputTable) throws SQLException {
        return createGraph(connection, inputTable, null);
    }

    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName) throws SQLException {
        // The default tolerance is zero.
        return createGraph(connection, tableName, spatialFieldName, 0.0);
    }

    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName,
                                      double tolerance) throws SQLException {
        // By default we do not orient by slope.
        return createGraph(connection, tableName, spatialFieldName, tolerance, false);
    }

    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope) throws SQLException {
        ST_Graph.tableName = tableName;
        nodesName = tableName + "_nodes";
        edgesName = tableName + "_edges";
        ST_Graph.connection = SFSUtilities.wrapConnection(connection);
        ST_Graph.quadtree = new Quadtree();
        ST_Graph.tolerance = tolerance;
        ST_Graph.orientBySlope = orientBySlope;

        getSpatialFieldIndex(spatialFieldName);
        setupOutputTables();
        updateTables();

        // If we made it this far, the output tables were successfully created.
        return true;
    }

    private static void getSpatialFieldIndex(String spatialFieldName) throws SQLException {
        // OBTAIN THE SPATIAL FIELD INDEX.
        ResultSet tableQuery = connection.createStatement().
                executeQuery("SELECT * FROM " + tableName + " LIMIT 0;");
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

    private static void setupOutputTables() throws SQLException {
        final Statement st = connection.createStatement();
        st.execute("CREATE TABLE " + nodesName + " (" + NODE_ID + " INT PRIMARY KEY, " + THE_GEOM + " POINT);");

        st.execute("CREATE TABLE " + edgesName + " AS SELECT * FROM " + tableName + ";" +
                "ALTER TABLE " + edgesName + " ADD COLUMN " + EDGE_ID + " INT IDENTITY;" +
                "ALTER TABLE " + edgesName + " ADD COLUMN " + START_NODE + " INTEGER;" +
                "ALTER TABLE " + edgesName + " ADD COLUMN " + END_NODE + " INTEGER;");
    }

    private static void updateTables() throws SQLException {
        SpatialResultSet nodesTable =
                connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).
                        executeQuery("SELECT * FROM " + nodesName).
                        unwrap(SpatialResultSet.class);
        SpatialResultSet edgesTable =
                connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).
                        executeQuery("SELECT * FROM " + edgesName).
                        unwrap(SpatialResultSet.class);
        try {
            int nodeID = 0;
            while (edgesTable.next()) {
                final Geometry geom = edgesTable.getGeometry(spatialFieldIndex);
                if (geom != null) {
                    final Coordinate[] coordinates = geom.getCoordinates();

                    final Coordinate firstCoord = coordinates[0];
                    final Coordinate lastCoord = coordinates[coordinates.length - 1];
                    final boolean switchCoords = (orientBySlope && firstCoord.z < lastCoord.z)? true : false;

                    nodeID = insertNode(nodesTable, edgesTable, nodeID, firstCoord, switchCoords ? END_NODE : START_NODE);
                    nodeID = insertNode(nodesTable, edgesTable, nodeID, lastCoord, switchCoords ? START_NODE : END_NODE);
                    edgesTable.updateRow();
                }
            }
        } finally {
            nodesTable.close();
            edgesTable.close();
        }
    }

    private static int insertNode(SpatialResultSet nodesTable,
                                  SpatialResultSet edgesTable,
                                  int nodeID,
                                  Coordinate coord,
                                  String edgeColumnName) throws SQLException {
        Envelope envelope = new Envelope(coord);
        envelope.expandBy(tolerance);

        final List<Integer> nearbyIntersectingNodes = findNearbyIntersectingNodes(envelope);
        if (nearbyIntersectingNodes.size() == 1) {
            edgesTable.updateInt(edgeColumnName, nearbyIntersectingNodes.get(0));
        } else {
            nodesTable.moveToInsertRow();
            nodesTable.updateInt(NODE_ID, ++nodeID);
            nodesTable.updateGeometry(THE_GEOM, GF.createPoint(coord));
            nodesTable.insertRow();
            quadtree.insert(envelope, nodeID);
            edgesTable.updateInt(edgeColumnName, nodeID);
        }
        return nodeID;
    }


    private static List<Integer> findNearbyIntersectingNodes(Envelope envelope) throws SQLException {
        final List<Integer> nearbyNodes = quadtree.query(envelope);
        final List<Integer> nearbyIntersectingNodes = new ArrayList<Integer>();
        for (Integer id : nearbyNodes) {
            SpatialResultSet matchingNodeRS =
                    connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).
                            executeQuery("SELECT " + THE_GEOM + " FROM " + nodesName + " WHERE " + NODE_ID + "=" + id).
                            unwrap(SpatialResultSet.class);
            matchingNodeRS.next();
            if (envelope.contains(matchingNodeRS.getGeometry(1).getEnvelopeInternal())) {
                nearbyIntersectingNodes.add(id);
            }
        }
        if (nearbyIntersectingNodes.size() > 1) {
            LOGGER.warn("Found {} nearby intersecting nodes.", nearbyNodes.size());
        }
        return nearbyIntersectingNodes;
    }
}
