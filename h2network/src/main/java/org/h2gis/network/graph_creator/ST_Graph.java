/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
 * or contact directly: info_at_orbisgis.org
 */

package org.h2gis.network.graph_creator;

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
import org.h2gis.utilities.TableLocation;

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
 * <p/>
 * If the specified geometry column of the input table contains geometries
 * other than LINESTRINGs or MULTILINESTRINGs, the operation will fail.
 * <p/>
 * A tolerance value may be given to specify the side length of a square
 * Envelope around each node used to snap together other nodes within the same
 * Envelope. Note, however, that edge geometries are left untouched.  Note also
 * that coordinates within a given tolerance of each other are not necessarily
 * snapped together. Only the first and last coordinates of a geometry are
 * considered to be potential nodes, and only nodes within a given tolerance of
 * each other are snapped together. The tolerance works only in metric units.
 * <p/>
 * A boolean value may be set to true to specify that edges should be oriented
 * by the z-value of their first and last coordinates (decreasing).
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_Graph extends AbstractFunction implements ScalarFunction {

    private static Connection connection;
    private static final GeometryFactory GF = new GeometryFactory();

    public static final String NODE_ID = "node_id";
    private static final int nodeIDIndex = 1;
    public static final String THE_GEOM = "the_geom";
    private static final int nodeGeomIndex = 2;
    public static final String EDGE_ID = "edge_id";
    public static final String START_NODE = "start_node";
    public static final String END_NODE = "end_node";
    private static final int BATCH_MAX_SIZE = 100;

    private TableLocation tableName;
    private TableLocation nodesName;
    private TableLocation edgesName;
    private Integer spatialFieldIndex;
    private Quadtree quadtree;
    private final List<Node> nearbyIntersectingNodes = new ArrayList<Node>();
    private double tolerance;
    private boolean orientBySlope;

    private int columnCount = 0;
    private int startNodeIndex = -1;
    private int endNodeIndex = -1;

    public ST_Graph() {
        this(null, null, 0.0, false);
    }

    /**
     * Constructor
     *
     * @param connection    Connection
     * @param inputTable    Input table name
     * @param tolerance     Tolerance
     * @param orientBySlope True if edges should be oriented by the z-value of
     *                      their first and last coordinates (decreasing)
     */
    public ST_Graph(Connection connection,
                    String inputTable,
                    double tolerance,
                    boolean orientBySlope) {
        if (connection != null) {
            this.connection = SFSUtilities.wrapConnection(connection);
        }
        if (inputTable != null) {
            this.tableName = TableLocation.parse(inputTable);
            this.nodesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + "_NODES");
            this.edgesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + "_EDGES");
        }
        this.tolerance = tolerance;
        this.orientBySlope = orientBySlope;
        this.quadtree = new Quadtree();
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
     * <p/>
     * Since no column is specified in this signature, we take the first
     * geometry column we find.
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName  Input table containing LINESTRINGs or MULTILINESTRINGs
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
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs or
     *                         MULTILINESTRINGs
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
     * <p/>
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs or
     *                         MULTILINESTRINGs
     * @param tolerance        Tolerance
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
     * <p/>
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * <p/>
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs or
     *                         MULTILINESTRINGs
     * @param tolerance        Tolerance
     * @param orientBySlope    True if edges should be oriented by the z-value of
     *                         their first and last coordinates (decreasing)
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope) throws SQLException {
        ST_Graph f = new ST_Graph(connection, tableName, tolerance, orientBySlope);
        f.setupTables(spatialFieldName);
        return f.updateTables();
    }

    /**
     * Get the column index of the given spatial field, or the first one found
     * if none is given (specified by null).
     *
     * @param spatialFieldName Spatial field name
     * @throws SQLException
     */
    private void setupTables(String spatialFieldName) throws SQLException {
        // Find the name of the first geometry column if not provided by the user.
        if (spatialFieldName == null) {
            List<String> geomFields = SFSUtilities.getGeometryFields(connection, tableName);
            if (!geomFields.isEmpty()) {
                spatialFieldName = geomFields.get(0);
            } else {
                throw new SQLException("Table " + tableName + " does not contain a geometry field.");
            }
        }
        // Set up tables
        final Statement st = connection.createStatement();
        try {
            // Recover useful informtation from the input table.
            final ResultSet inputRS = st.executeQuery("SELECT * FROM " + tableName + " LIMIT 0");
            spatialFieldIndex = inputRS.findColumn(spatialFieldName);
            columnCount = inputRS.getMetaData().getColumnCount();
            startNodeIndex = columnCount + 2;
            endNodeIndex = columnCount + 3;
            // Set up the edges table
            st.execute("CREATE TABLE " + edgesName + " AS SELECT * FROM " + tableName + " LIMIT 0");
            st.execute("ALTER TABLE " + edgesName + " ADD COLUMN " + EDGE_ID + " INT IDENTITY;" +
                    "ALTER TABLE " + edgesName + " ADD COLUMN " + START_NODE + " INTEGER;" +
                    "ALTER TABLE " + edgesName + " ADD COLUMN " + END_NODE + " INTEGER;");
            // Set up the nodes table
            st.execute("CREATE TABLE " + nodesName + " (" + NODE_ID + " INT PRIMARY KEY, " + THE_GEOM + " POINT);");
        } finally {
            st.close();
        }
        if (spatialFieldIndex == null) {
            throw new SQLException("Geometry field " + spatialFieldName + " of table " + tableName + " not found");
        }
    }

    /**
     * Go through the input table, identify nodes and edges,
     * and update the values in the nodes and edges tables appropriately.
     * <p/>
     * If a Geometry is found which is not a LINESTRING or a MULTILINESTRING,
     * then the nodes and edges tables that were being constructed are deleted.
     *
     * @return True if the tables were updated.
     * @throws SQLException
     */
    private boolean updateTables() throws SQLException {
        connection.setAutoCommit(false);

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + edgesName + " VALUES(");
        for (int i = 1; i <= columnCount; i++) {
            sb.append("?, ");
        }
        sb.append("DEFAULT, ?, ?)");
        final PreparedStatement edgeSt = connection.prepareStatement(sb.toString());

        String nodeSQL = "INSERT INTO " + nodesName + " VALUES(?, ?)";
        final PreparedStatement nodeSt = connection.prepareStatement(nodeSQL);

        final Statement inputSt = connection.createStatement();

        try {
            SpatialResultSet inputTable = inputSt.
                            executeQuery("SELECT * FROM " + tableName).
                            unwrap(SpatialResultSet.class);
            try {
                int nodeID = 0;
                int batchSize = 0;
                while (inputTable.next()) {
                    final Geometry geom = inputTable.getGeometry(spatialFieldIndex);
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
                        final boolean switchCoords = (orientBySlope && firstCoord.z < lastCoord.z) ? true : false;

                        // Copy over original data
                        for (int i = 1; i <= columnCount; i++) {
                            edgeSt.setObject(i, inputTable.getObject(i));
                        }
                        nodeID = insertNode(nodeSt, edgeSt, nodeID, firstCoord, switchCoords ? endNodeIndex : startNodeIndex);
                        nodeID = insertNode(nodeSt, edgeSt, nodeID, lastCoord, switchCoords ? startNodeIndex : endNodeIndex);
                        edgeSt.addBatch();

                        // Execute a batch if needed.
                        batchSize++;
                        if (batchSize >= BATCH_MAX_SIZE) {
                            nodeSt.executeBatch();
                            nodeSt.clearBatch();
                            edgeSt.executeBatch();
                            edgeSt.clearBatch();
                            batchSize = 0;
                        }
                    }
                }
                if (batchSize > 0) {
                    nodeSt.executeBatch();
                    edgeSt.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                final Statement statement = connection.createStatement();
                try {
                    statement.execute("DROP TABLE " + nodesName);
                    statement.execute("DROP TABLE " + edgesName);
                    return false;
                } finally {
                    statement.close();
                }
            } finally {
                inputTable.close();
            }
        } finally {
            nodeSt.close();
            edgeSt.close();
            inputSt.close();
        }
        connection.setAutoCommit(true);
        return true;
    }

    /**
     * Insert the node in the nodes table if it is a new node, and update the
     * edges table appropriately.
     *
     *
     * @param nodeSt         Nodes statement
     * @param edgeSt         Edges statement
     * @param nodeID         Current node ID
     * @param coord          Current coordinate
     * @param edgeColIndex   Index of column to update in edges table
     * @return Node ID
     * @throws SQLException
     */
    private int insertNode(PreparedStatement nodeSt,
                           PreparedStatement edgeSt,
                           int nodeID,
                           Coordinate coord,
                           int edgeColIndex) throws SQLException {
        Envelope envelope = new Envelope(coord);
        envelope.expandBy(tolerance);
        // Because of the DEFAULT field, there is one less parameter
        // in the prepared statement.
        final int adjustedIndex = edgeColIndex - 1;
        final Node nodeToSnapTo = findNodeToSnapTo(coord, envelope);
        if (nodeToSnapTo != null) {
            edgeSt.setInt(adjustedIndex, nodeToSnapTo.getId());
        } else {
            nodeSt.setInt(nodeIDIndex, ++nodeID);
            nodeSt.setObject(nodeGeomIndex, GF.createPoint(coord));
            nodeSt.addBatch();
            quadtree.insert(envelope, new Node(nodeID, coord));
            edgeSt.setInt(adjustedIndex, nodeID);
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
    private Node findNodeToSnapTo(Coordinate coord, Envelope envelope)
            throws SQLException {
        nearbyIntersectingNodes.clear();
        for (Node node : (List<Node>) quadtree.query(envelope)) {
            if (envelope.contains(node.getCoordinate())) {
                nearbyIntersectingNodes.add(node);
            }
        }
        final int numIntersectingNodes = nearbyIntersectingNodes.size();
        if (numIntersectingNodes > 0) {
            if (numIntersectingNodes == 1) {
                // If there is only one intersecting node, then snap this coordinate
                // to that node and return it.
                final Node nodeToSnapTo = nearbyIntersectingNodes.get(0);
                nodeToSnapTo.setSnappedCoordinate(coord);
                return nodeToSnapTo;
            } else {
                // If there is more than one, then return the first intersecting
                // node which has a snapped coordinate equal to this coordinate.
                for (Node node : nearbyIntersectingNodes) {
                    Coordinate snappedCoordinate = node.getSnappedCoordinate();
                    if (snappedCoordinate != null) {
                        if (snappedCoordinate.equals3D(coord)) {
                            return node;
                        }
                    }
                }
            }
        }
        // Either there were no intersecting nodes, or none which had a snapped
        // coordinate equal to this coordinate.
        return null;
    }

    /**
     * Node class for snapping nodes.
     *
     * @author Adam Gouge
     */
    private static class Node {

        private int id;
        private Coordinate coordinate;
        private Coordinate snappedCoordinate;

        /**
         * Constructor
         *
         * @param id         ID
         * @param coordinate Coordinate
         */
        public Node(int id, Coordinate coordinate) {
            this.id = id;
            this.coordinate = coordinate;
        }

        public int getId() {
            return id;
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }

        public Coordinate getSnappedCoordinate() {
            return snappedCoordinate;
        }

        public void setSnappedCoordinate(Coordinate snappedCoordinate) {
            this.snappedCoordinate = snappedCoordinate;
        }

    }
}
