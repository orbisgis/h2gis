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
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.SpatialResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ST_Graph produces two tables (nodes and edges) from an input table
 * containing LINESTRINGs or MULTILINESTRINGs in the given column and using the
 * given tolerance, and potentially orienting edges by slope. If the input
 * table has name 'input', then the output tables are named 'input_nodes' and
 * 'input_edges'. The nodes table consists of an integer node_id and a POINT
 * geometry representing each node. The edges table is a copy of the input
 * table with three extra columns: edge_id, start_node, and end_node. The
 * start_node and end_node correspond to the node_ids in the nodes table.
 *
 * If the specified geometry column of the input table contains geometries
 * other than LINESTRINGs or MULTILINESTRINGs, the operation will fail.
 *
 * A tolerance value may be given to specify the side length of a square
 * Envelope around each node used to snap together other nodes within the same
 * Envelope. Note, however, that edge geometries are left untouched.  Note also
 * that coordinates within a given tolerance of each other are not necessarily
 * snapped together. Only the first and last coordinates of a geometry are
 * considered to be potential nodes, and only nodes within a given tolerance of
 * each other are snapped together. The tolerance works only in metric units.
 *
 * A boolean value may be set to true to specify that edges should be oriented
 * by the z-value of their first and last coordinates (decreasing).
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_Graph extends AbstractFunction implements ScalarFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ST_Graph.class);
    private static final GeometryFactory GF = new GeometryFactory();
    public static final String THE_GEOM = "the_geom";
    public static final String NODE_ID = "node_id";
    public static final String EDGE_ID = "edge_id";
    public static final String START_NODE = "start_node";
    public static final String END_NODE = "end_node";
    private static Connection connection;
    private static Quadtree quadtree;
    private static Integer spatialFieldIndex;
    private static String tableName;
    private static String nodesName;
    private static String edgesName;
    private static double tolerance;
    private static boolean orientBySlope;

    public ST_Graph() {
        addProperty(PROP_REMARKS, "ST_Graph produces two tables (nodes and edges) from an input table " +
                "containing LINESTRINGs or MULTILINESTRINGs in the given column and using the " +
                "given tolerance, and potentially orienting edges by slope. If the input " +
                "table has name 'input', then the output tables are named 'input_nodes' and " +
                "'input_edges'. The nodes table consists of an integer node_id and a POINT " +
                "geometry representing each node. The edges table is a copy of the input " +
                "table with three extra columns: edge_id, start_node, and end_node. The " +
                "start_node and end_node correspond to the node_ids in the nodes table.\n" +

                "If the specified geometry column of the input table contains geometries " +
                "other than LINESTRINGs or MULTILINESTRINGs, the operation will fail.\n" +

                "A tolerance value may be given to specify the side length of a square " +
                "Envelope around each node used to snap together other nodes within the same " +
                "Envelope. Note, however, that edge geometries are left untouched.  Note also " +
                "that coordinates within a given tolerance of each other are not necessarily " +
                "snapped together. Only the first and last coordinates of a geometry are " +
                "considered to be potential nodes, and only nodes within a given tolerance of " +
                "each other are snapped together. The tolerance works only in metric units.\n" +

                "A boolean value may be set to true to specify that edges should be oriented " +
                "by the z-value of their first and last coordinates (decreasing). "
        );
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGraph";
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs or MULTILINESTRINGs.
     *
     * Since no column is specified in this signature, we take the first
     * geometry column we find.
     *
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName Input table containing LINESTRINGs or MULTILINESTRINGs
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName) throws SQLException {
        return createGraph(connection, tableName, null);
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs or MULTILINESTRINGs in the given column.
     *
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName Input table
     * @param spatialFieldName Name of column containing LINESTRINGs or
     * MULTILINESTRINGs
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName) throws SQLException {
        // The default tolerance is zero.
        return createGraph(connection, tableName, spatialFieldName, 0.0);
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs or MULTILINESTRINGs in the given column and using the given
     * tolerance.
     *
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     *
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName Input table
     * @param spatialFieldName Name of column containing LINESTRINGs or
     * MULTILINESTRINGs
     * @param tolerance Tolerance
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName,
                                      double tolerance) throws SQLException {
        // By default we do not orient by slope.
        return createGraph(connection, tableName, spatialFieldName, tolerance, false);
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs or MULTILINESTRINGs in the given column and using the given
     * tolerance, and potentially orienting edges by slope.
     *
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     *
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     *
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName Input table
     * @param spatialFieldName Name of column containing LINESTRINGs or
     * MULTILINESTRINGs
     * @param tolerance Tolerance
     * @param orientBySlope True if edges should be oriented by the z-value of
     * their first and last coordinates (decreasing)
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope) throws SQLException {
        ST_Graph.tableName = tableName;
        ST_Graph.nodesName = tableName + "_nodes";
        ST_Graph.edgesName = tableName + "_edges";
        ST_Graph.connection = SFSUtilities.wrapConnection(connection);
        ST_Graph.quadtree = new Quadtree();
        ST_Graph.tolerance = tolerance;
        ST_Graph.orientBySlope = orientBySlope;

        getSpatialFieldIndex(spatialFieldName);
        setupOutputTables();
        return updateTables();
    }

    /**
     * Get the column index of the given spatial field, or the first one found
     * if none is given (specified by null).
     *
     * @param spatialFieldName Spatial field name
     * @throws SQLException
     */
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

    /**
     * Create the nodes and edges tables.
     *
     * @throws SQLException
     */
    private static void setupOutputTables() throws SQLException {
        final Statement st = connection.createStatement();
        st.execute("CREATE TABLE " + nodesName + " (" + NODE_ID + " INT PRIMARY KEY, " + THE_GEOM + " POINT);");

        st.execute("CREATE TABLE " + edgesName + " AS SELECT * FROM " + tableName + ";" +
                "ALTER TABLE " + edgesName + " ADD COLUMN " + EDGE_ID + " INT IDENTITY;" +
                "ALTER TABLE " + edgesName + " ADD COLUMN " + START_NODE + " INTEGER;" +
                "ALTER TABLE " + edgesName + " ADD COLUMN " + END_NODE + " INTEGER;");
    }

    /**
     * Go through the input table, identify nodes and edges,
     * and update the values in the nodes and edges tables appropriately.
     *
     * If a Geometry is found which is not a LINESTRING or a MULTILINESTRING,
     * then the nodes and edges tables that were being constructed are deleted.
     *
     * @return True if the tables were updated.
     * @throws SQLException
     */
    private static boolean updateTables() throws SQLException {
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
                    final int type = SFSUtilities.getGeometryTypeFromGeometry(geom);
                    if (type != GeometryTypeCodes.LINESTRING
                            && type != GeometryTypeCodes.MULTILINESTRING) {
                        throw new SQLException("Only LINESTRINGS and MULTILINESTRINGS are accepted. " +
                                "Found: " + geom.getGeometryType());
                    }
                    final Coordinate[] coordinates = geom.getCoordinates();

                    final Coordinate firstCoord = coordinates[0];
                    final Coordinate lastCoord = coordinates[coordinates.length - 1];
                    final boolean switchCoords = (orientBySlope && firstCoord.z < lastCoord.z)? true : false;

                    nodeID = insertNode(nodesTable, edgesTable, nodeID, firstCoord, switchCoords ? END_NODE : START_NODE);
                    nodeID = insertNode(nodesTable, edgesTable, nodeID, lastCoord, switchCoords ? START_NODE : END_NODE);
                    edgesTable.updateRow();
                }
            }
        } catch (SQLException e) {
            final Statement statement = connection.createStatement();
            statement.execute("DROP TABLE " + nodesName);
            statement.execute("DROP TABLE " + edgesName);
            return false;
        } finally {
            nodesTable.close();
            edgesTable.close();
        }
        return true;
    }

    /**
     * Insert the node in the nodes table if it is a new node, and update the
     * edges table appropriately.
     *
     * @param nodesTable Nodes table
     * @param edgesTable Edges table
     * @param nodeID Current node ID
     * @param coord Current coordinate
     * @param edgeColumnName Name of column to update in edges table
     * @return Node ID
     * @throws SQLException
     */
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

    /**
     * Return a list of nodes that intersect the given Envelope.
     *
     * @param envelope Envelope
     * @return A list of nodes that intersect the given Envelope
     * @throws SQLException
     */
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
