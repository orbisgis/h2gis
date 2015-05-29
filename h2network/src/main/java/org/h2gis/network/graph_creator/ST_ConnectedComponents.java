/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.network.graph_creator;

import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.javanetworkanalyzer.data.VUCent;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.h2gis.network.graph_creator.GraphFunctionParser.Orientation.UNDIRECTED;
import static org.h2gis.network.graph_creator.GraphFunctionParser.*;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates the connected components (for undirected graphs) or strongly
 * connected components (for directed graphs) of a graph.
 *
 * @author Adam Gouge
 */
public class ST_ConnectedComponents  extends GraphFunction implements ScalarFunction {

    protected static final int BATCH_SIZE = 100;
    public static final int NULL_CONNECTED_COMPONENT_NUMBER = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger("gui." + ST_ConnectedComponents.class);
    public static final String REMARKS =
            "`ST_ConnectedComponents` calculates the connected components (for undirected\n" +
            "graphs) or strongly connected components (for directed graphs) of a graph.  It\n" +
            "produces two tables (nodes and edges) containing a node or edge id and a\n" +
            "connected component id. Signature: \n" +
            "* `ST_ConnectedComponents('input_edges', 'o[ - eo]')`\n" +
            "\n" +
            "where \n" +
            "* `input_edges` = Edges table produced by `ST_Graph` from table `input`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected).\n";

    /**
     * Constructor
     */
    public ST_ConnectedComponents() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getConnectedComponents";
    }

    /**
     * Calculate the node and edge connected component tables.
     *
     * @param connection  Connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @return True if the calculation was successful
     * @throws SQLException
     */
    public static boolean getConnectedComponents(Connection connection,
                                                 String inputTable,
                                                 String orientation) throws SQLException {
        KeyedGraph graph = prepareGraph(connection, inputTable, orientation, null,
                VUCent.class, Edge.class);
        if (graph == null) {
            return false;
        }
        final List<Set<VUCent>> componentsList = getConnectedComponents(graph, orientation);

        final TableLocation tableName = parseInputTable(connection, inputTable);
        final TableLocation nodesName = suffixTableLocation(tableName, NODE_COMP_SUFFIX);
        final TableLocation edgesName = suffixTableLocation(tableName, EDGE_COMP_SUFFIX);

        if (storeNodeConnectedComponents(connection, nodesName, edgesName, componentsList)) {
            if (storeEdgeConnectedComponents(connection, tableName, nodesName, edgesName)) {
                return true;
            }
        }
        return false;
    }

    private static void cancel(Connection connection,
                               TableLocation nodesName,
                               TableLocation edgesName,
                               SQLException e,
                               String msg)
            throws SQLException {
        LOGGER.error(msg, e);
        final Statement statement = connection.createStatement();
        try {
            statement.execute("DROP TABLE IF EXISTS " + nodesName);
            statement.execute("DROP TABLE IF EXISTS " + edgesName);
        } finally {
            statement.close();
        }
    }

    private static List<Set<VUCent>> getConnectedComponents(Graph<VUCent, Edge> graph,
                                                            String orientation) {
        LOGGER.info("Calculating connected components... ");
        final long start = System.currentTimeMillis();
        List<Set<VUCent>> sets;
        if (parseGlobalOrientation(orientation).equals(UNDIRECTED)) {
            sets = new ConnectivityInspector<VUCent, Edge>(
                    (UndirectedGraph<VUCent, Edge>) graph).connectedSets();
        } else {
            sets = new StrongConnectivityInspector<VUCent, Edge>(
                    (DirectedGraph) graph).stronglyConnectedSets();
        }
        logTime(LOGGER, start);
        return sets;
    }

    private static boolean storeNodeConnectedComponents(Connection connection,
                                                        TableLocation nodesName,
                                                        TableLocation edgesName,
                                                        List<Set<VUCent>> componentsList)
            throws SQLException {
        LOGGER.info("Storing node connected components... ");
        final long start = System.currentTimeMillis();
        createNodeTable(connection, nodesName);
        final PreparedStatement nodeSt =
                connection.prepareStatement("INSERT INTO " + nodesName + " VALUES(?,?)");
        try {
            final boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            int componentNumber = 0;
            for (Set<VUCent> component : componentsList) {
                componentNumber++;
                int count = 0;
                for (VUCent v : component) {
                    nodeSt.setInt(1, v.getID());
                    nodeSt.setInt(2, componentNumber);
                    nodeSt.addBatch();
                    count++;
                    if (count >= BATCH_SIZE) {
                        nodeSt.executeBatch();
                        nodeSt.clearBatch();
                        count = 0;
                    }
                }
                if (count > 0) {
                    nodeSt.executeBatch();
                    nodeSt.clearBatch();
                }
                connection.commit();
            }
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            cancel(connection, nodesName, edgesName, e, "Could not store node connected components.");
            return false;
        } finally {
            nodeSt.close();
        }
        logTime(LOGGER, start);
        return true;
    }

    private static void createNodeTable(Connection connection,
                                        TableLocation nodesName) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE " + nodesName + "(" +
                    NODE_ID + " INTEGER PRIMARY KEY, " +
                    CONNECTED_COMPONENT + " INTEGER);");
        } finally {
            st.close();
        }
    }

    private static boolean storeEdgeConnectedComponents(Connection connection,
                                                        TableLocation tableName,
                                                        TableLocation nodesName,
                                                        TableLocation edgesName) throws SQLException {
        LOGGER.info("Storing edge connected components...");
        final long start = System.currentTimeMillis();
        final Statement st = connection.createStatement();
        try {
            final String tmpName = "TMP" + System.currentTimeMillis();
            final String startNodeCC = "SN_CC";
            final String endNodeCC = "EN_CC";
            st.execute(
                // Create a temporary table containing the connected component
                // of each start node.
                "CREATE INDEX ON " + tableName + "(" + START_NODE + ");" +
                "CREATE INDEX ON " + nodesName + "(" + NODE_ID + ");" +
                "CREATE TEMPORARY TABLE " + tmpName +
                "(" + EDGE_ID + " INT PRIMARY KEY, " + startNodeCC + " INT, " + endNodeCC + " INT) " +
                "AS SELECT A." + EDGE_ID + ", B." + CONNECTED_COMPONENT + ", NULL " +
                "FROM " + tableName + " A, " + nodesName + " B " +
                "WHERE A." + START_NODE + "=B." + NODE_ID + ";" +
                // Add indices to speed up the UPDATE.
                "CREATE INDEX ON " + tableName + "(" + END_NODE + ");" +
                "CREATE INDEX ON " + tableName + "(" + EDGE_ID + ");" +
                "CREATE INDEX ON " + tmpName + "(" + EDGE_ID + ");" +
                // Update the temporary table with the connected component
                // of each end node.
                "UPDATE " + tmpName + " C " +
                "SET " + endNodeCC + "=(" +
                "SELECT B." + CONNECTED_COMPONENT + " " +
                "FROM " + tableName + " A, " + nodesName + " B " +
                "WHERE A." + END_NODE + "=B." + NODE_ID + " AND C." + EDGE_ID + "=A." + EDGE_ID + ");" +
                // Use this temporary table to deduce the connected component
                // of each edge. If the start and end node are in the same
                // connected component, then so is the edge. If they are in
                // different connected components (this is only possible for
                // directed graphs), then we consider that this edge is not in
                // a strongly connected component and so assign a connected
                // component id of NULL_CONNECTED_COMPONENT_NUMBER.
                "CREATE TABLE " + edgesName +
                "(" + EDGE_ID + " INT PRIMARY KEY, " + CONNECTED_COMPONENT + " INT) AS " +
                "SELECT " + EDGE_ID + ", " + startNodeCC + " " +
                "FROM " + tmpName + " WHERE " + startNodeCC + "=" + endNodeCC + "; " +
                "INSERT INTO " + edgesName + "(" + EDGE_ID + ", " + CONNECTED_COMPONENT + ") " +
                "SELECT " + EDGE_ID + ", " + NULL_CONNECTED_COMPONENT_NUMBER +
                " FROM " + tmpName + " WHERE " + startNodeCC + "!=" + endNodeCC + ";" +
                // Drop the temporary table.
                "DROP TABLE IF EXISTS " + tmpName + ";");
        } catch (SQLException e) {
            cancel(connection, nodesName, edgesName, e, "Could not store edge connected components.");
            return false;
        } finally {
            st.close();
        }
        logTime(LOGGER, start);
        return true;
    }
}
