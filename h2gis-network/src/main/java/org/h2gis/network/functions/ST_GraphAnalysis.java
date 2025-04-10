/**
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
package org.h2gis.network.functions;

import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.javanetworkanalyzer.analyzers.GraphAnalyzer;
import org.javanetworkanalyzer.analyzers.UnweightedGraphAnalyzer;
import org.javanetworkanalyzer.analyzers.WeightedGraphAnalyzer;
import org.javanetworkanalyzer.data.VCent;
import org.javanetworkanalyzer.data.VUCent;
import org.javanetworkanalyzer.data.VWCent;
import org.javanetworkanalyzer.model.EdgeCent;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.javanetworkanalyzer.progress.DefaultProgressMonitor;
import org.jgrapht.WeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.h2gis.network.functions.GraphConstants.*;

/**
 * Calculates closeness and betweenness centrality for nodes, as well as
 * betweenness centrality for edges.
 *
 * @author Adam Gouge
 */
public class ST_GraphAnalysis extends GraphFunction implements ScalarFunction {

    protected static final int BATCH_SIZE = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_GraphAnalysis.class);

    public static final String REMARKS =
            "`ST_GraphAnalysis` calculates closeness and betweenness centrality for nodes,\n" +
            "as well as betweenness centrality for edges. Possible signatures:\n" +
            "* `ST_GraphAnalysis('input_edges', 'o[ - eo]')`\n" +
            "* `ST_GraphAnalysis('input_edges', 'o[ - eo]', 'w')`\n" +
            "\n" +
            "where\n" +
            "* `input_edges` = Edges table produced by `ST_Graph` from table `input`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected).\n" +
            "  Required if global orientation is directed or reversed.\n" +
            "* `w` = Name of column containing edge weights as doubles\n" +
            "\n" +
            "**WARNING**: If ST_GraphAnalysis is called on a graph with more than one\n" +
            "(strongly) connected component, all closeness centrality scores will be zero.\n" +
            "See ST_ConnectedComponents.\n";

    /**
     * Constructor
     */
    public ST_GraphAnalysis() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "doGraphAnalysis";
    }

    /**
     * Calculate centrality indices on the nodes and edges of a graph
     * constructed from the input table.
     *
     * @param connection  Connection
     * @param inputTable  Input table
     * @param orientation Global orientation
     * @return True if the calculation was successful
     */
    public static boolean doGraphAnalysis(Connection connection,
                                          String inputTable,
                                          String orientation)
            throws SQLException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        return doGraphAnalysis(connection, inputTable, orientation, null);
    }

    /**
     * Calculate centrality indices on the nodes and edges of a graph
     * constructed from the input table.
     *
     * @param connection  Connection
     * @param inputTable  Input table
     * @param orientation Global orientation
     * @param weight      Edge weight column name
     * @return True if the calculation was successful
     */
    public static boolean doGraphAnalysis(Connection connection,
                                          String inputTable,
                                          String orientation,
                                          String weight)
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        final TableLocation tableName = TableUtilities.parseInputTable(connection, inputTable);
        final TableLocation nodesName = TableUtilities.suffixTableLocation(tableName, NODE_CENT_SUFFIX);
        final TableLocation edgesName = TableUtilities.suffixTableLocation(tableName, EDGE_CENT_SUFFIX);
        try {
            createTables(connection, nodesName, edgesName);
            final KeyedGraph graph =
                    doAnalysisAndReturnGraph(connection, inputTable, orientation, weight);
            storeNodeCentrality(connection, nodesName, graph);
            storeEdgeCentrality(connection, edgesName, graph);
        } catch (SQLException e) {
            LOGGER.error("Problem creating centrality tables.");
            final Statement statement = connection.createStatement();
            try {
                statement.execute("DROP TABLE IF EXISTS " + nodesName);
                statement.execute("DROP TABLE IF EXISTS " + edgesName);
            } finally {
                statement.close();
            }
            return false;
        }
        return true;
    }

    private static KeyedGraph doAnalysisAndReturnGraph(Connection connection,
                                                       String inputTable,
                                                       String orientation,
                                                       String weight)
            throws SQLException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        final KeyedGraph graph = prepareGraph(connection, inputTable, orientation, weight,
                (weight == null) ? VUCent.class : VWCent.class, EdgeCent.class);
        final DefaultProgressMonitor pm = new DefaultProgressMonitor();
        GraphAnalyzer analyzer = (weight == null) ?
                new UnweightedGraphAnalyzer(graph, pm) :
                new WeightedGraphAnalyzer((WeightedGraph) graph, pm);
        analyzer.computeAll();
        return graph;
    }

    private static void createTables(Connection connection,
                                     TableLocation nodesName,
                                     TableLocation edgesName) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE " + nodesName + "(" +
                    NODE_ID + " INTEGER PRIMARY KEY, " +
                    BETWEENNESS + " DOUBLE, " +
                    CLOSENESS + " DOUBLE);");
            st.execute("CREATE TABLE " + edgesName + "(" +
                    EDGE_ID + " INTEGER PRIMARY KEY, " +
                    BETWEENNESS + " DOUBLE);");
        } finally {
            st.close();
        }
    }

    private static void storeNodeCentrality(Connection connection,
                                            TableLocation nodesName,
                                            KeyedGraph graph) throws SQLException {
        final PreparedStatement nodeSt =
                connection.prepareStatement("INSERT INTO " + nodesName + " VALUES(?,?,?)");
        try {
            connection.setAutoCommit(false);
            int count = 0;
            for (VCent v : (Set<VCent>) graph.vertexSet()) {
                nodeSt.setInt(1, v.getID());
                nodeSt.setDouble(2, v.getBetweenness());
                nodeSt.setDouble(3, v.getCloseness());
                nodeSt.addBatch();
                count++;
                if (count >= BATCH_SIZE) {
                    nodeSt.executeBatch();
                    connection.commit();
                    nodeSt.clearBatch();
                    count = 0;
                }
            }
            if (count > 0) {
                nodeSt.executeBatch();
                connection.commit();
                nodeSt.clearBatch();
            }
        } finally {
            connection.setAutoCommit(true);
            nodeSt.close();
        }
    }

    private static void storeEdgeCentrality(Connection connection,
                                            TableLocation edgesName,
                                            KeyedGraph graph) throws SQLException {
        final PreparedStatement edgeSt =
                connection.prepareStatement("INSERT INTO " + edgesName + " VALUES(?,?)");
        try {
            connection.setAutoCommit(false);
            int count = 0;
            for (EdgeCent e : (Set<EdgeCent>) graph.edgeSet()) {
                edgeSt.setInt(1, e.getID());
                edgeSt.setDouble(2, e.getBetweenness());
                edgeSt.addBatch();
                count++;
                if (count >= BATCH_SIZE) {
                    edgeSt.executeBatch();
                    connection.commit();
                    edgeSt.clearBatch();
                    count = 0;
                }
            }
            if (count > 0) {
                edgeSt.executeBatch();
                connection.commit();
                edgeSt.clearBatch();
            }
        } finally {
            connection.setAutoCommit(true);
            edgeSt.close();
        }
    }
}
