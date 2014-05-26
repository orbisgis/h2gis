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
 * or contact directly:
 * info_at_ orbisgis.org
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
import static org.h2gis.network.graph_creator.GraphFunctionParser.parseGlobalOrientation;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates the connected components (for undirected graphs) or strongly
 * connected components (for directed graphs) of a graph.
 *
 * @author Adam Gouge
 */
public class ST_ConnectedComponents  extends GraphFunction implements ScalarFunction {

    private static Connection connection;
    private final String orientation;
    private TableLocation tableName;
    private TableLocation nodesName;
    private TableLocation edgesName;
    protected static final int BATCH_SIZE = 100;
    public static final int NULL_CONNECTED_COMPONENT_NUMBER = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_ConnectedComponents.class);
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
        this(null, null, null);
    }

    /**
     * Constructor
     *
     * @param connection  Connection
     * @param inputTable  Input table
     * @param orientation Orientation string
     */
    public ST_ConnectedComponents(Connection connection,
                                  String inputTable,
                                  String orientation) {
        addProperty(PROP_REMARKS, REMARKS);
        if (connection != null) {
            this.connection = connection;
        }
        if (inputTable != null) {
            this.tableName = TableLocation.parse(inputTable);
            this.nodesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + NODE_COMP_SUFFIX);
            this.edgesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + EDGE_COMP_SUFFIX);
        }
        this.orientation = orientation;
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
        ST_ConnectedComponents f = new ST_ConnectedComponents(connection, inputTable, orientation);
        KeyedGraph graph = prepareGraph(connection, inputTable, orientation, null,
                VUCent.class, Edge.class);
        if (graph == null) {
            return false;
        }
        final List<Set<VUCent>> componentsList = getConnectedComponents(f, graph);
        if (storeNodeConnectedComponents(f, componentsList)) {
            if (storeEdgeConnectedComponents(f)) {
                return true;
            }
        }
        return false;
    }

    private static void cancel(ST_ConnectedComponents f, SQLException e, String msg)
            throws SQLException {
        LOGGER.error(msg, e);
        final Statement statement = connection.createStatement();
        try {
            statement.execute("DROP TABLE IF EXISTS " + f.nodesName);
            statement.execute("DROP TABLE IF EXISTS " + f.edgesName);
        } finally {
            statement.close();
        }
    }

    private static List<Set<VUCent>> getConnectedComponents(ST_ConnectedComponents f,
                                                            Graph<VUCent, Edge> graph) {
        if (parseGlobalOrientation(f.orientation).equals(UNDIRECTED)) {
            return new ConnectivityInspector<VUCent, Edge>(
                    (UndirectedGraph<VUCent, Edge>) graph).connectedSets();
        } else {
            return new StrongConnectivityInspector<VUCent, Edge>(
                    (DirectedGraph) graph).stronglyConnectedSets();
        }
    }

    private static boolean storeNodeConnectedComponents(ST_ConnectedComponents f,
                                                        List<Set<VUCent>> componentsList)
            throws SQLException {
        createNodeTable(f);
        final PreparedStatement nodeSt =
                connection.prepareStatement("INSERT INTO " + f.nodesName + " VALUES(?,?)");
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
            cancel(f, e, "Could not store node connected components.");
            return false;
        } finally {
            nodeSt.close();
        }
        return true;
    }

    private static void createNodeTable(ST_ConnectedComponents f) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE " + f.nodesName + "(" +
                    NODE_ID + " INTEGER PRIMARY KEY, " +
                    CONNECTED_COMPONENT + " INTEGER);");
        } finally {
            st.close();
        }
    }

    private static boolean storeEdgeConnectedComponents(ST_ConnectedComponents f) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            final String tmpName = "TMP" + System.currentTimeMillis();
            final String startNodeCC = "SN_CC";
            final String endNodeCC = "EN_CC";
            st.execute(
                // Create a temporary table containing the connected component
                // of each start and end node.
                "CREATE TEMPORARY TABLE " + tmpName +
                "(" + EDGE_ID + " INT PRIMARY KEY, " + startNodeCC + " INT, " + endNodeCC + " INT) " +
                "AS SELECT A." + EDGE_ID + ", B." + CONNECTED_COMPONENT + ", NULL " +
                "FROM " + f.tableName + " A, " + f.nodesName + " B " +
                "WHERE A." + START_NODE + "=B." + NODE_ID + ";" +
                "UPDATE " + tmpName + " C " +
                "SET " + endNodeCC + "=(" +
                "SELECT B." + CONNECTED_COMPONENT + " " +
                "FROM " + f.tableName + " A, " + f.nodesName + " B " +
                "WHERE A." + END_NODE + "=B." + NODE_ID + " AND C." + EDGE_ID + "=A." + EDGE_ID + ");" +
                // Use this temporary table to deduce the connected component
                // of each edge. If the start and end node are in the same
                // connected component, then so is the edge. If they are in
                // different connected components (this is only possible for
                // directed graphs), then we consider that this edge is not in
                // a strongly connected component and so assign a connected
                // component id of NULL_CONNECTED_COMPONENT_NUMBER.
                "CREATE TABLE " + f.edgesName +
                "(" + EDGE_ID + " INT PRIMARY KEY, " + CONNECTED_COMPONENT + " INT) AS " +
                "SELECT " + EDGE_ID + ", " + startNodeCC + " " +
                "FROM " + tmpName + " WHERE " + startNodeCC + "=" + endNodeCC + "; " +
                "INSERT INTO " + f.edgesName + "(" + EDGE_ID + ", " + CONNECTED_COMPONENT + ") " +
                "SELECT " + EDGE_ID + ", " + NULL_CONNECTED_COMPONENT_NUMBER +
                " FROM " + tmpName + " WHERE " + startNodeCC + "!=" + endNodeCC + ";" +
                // Drop the temporary table.
                "DROP TABLE IF EXISTS " + tmpName + ";");
        } catch (SQLException e) {
            cancel(f, e, "Could not store edge connected components.");
            return false;
        } finally {
            st.close();
        }
        return true;
    }
}
