package org.h2gis.network.graph_creator;

import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.TableLocation;
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

import static org.h2gis.utilities.GraphConstants.*;

/**
 * @author Adam Gouge
 */
public class ST_GraphAnalysis extends GraphFunction implements ScalarFunction {

    private static Connection connection;
    private TableLocation tableName;
    private TableLocation nodesName;
    private TableLocation edgesName;
    private static final int BATCH_SIZE = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_GraphAnalysis.class);

    public ST_GraphAnalysis() {
        this(null, null);
    }

    public ST_GraphAnalysis(Connection connection,
                            String inputTable) {
        if (connection != null) {
            this.connection = connection;
        }
        if (inputTable != null) {
            this.tableName = TableLocation.parse(inputTable);
            this.nodesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + NODE_CENT_SUFFIX);
            this.edgesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + EDGE_CENT_SUFFIX);
        }
    }
    @Override
    public String getJavaStaticMethod() {
        return "doGraphAnalysis";
    }

    public static boolean doGraphAnalysis(Connection connection,
                                       String inputTable,
                                       String orientation)
            throws SQLException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        return doGraphAnalysis(connection, inputTable, orientation, null);
    }

    public static boolean doGraphAnalysis(Connection connection,
                                          String inputTable,
                                          String orientation,
                                          String weight)
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        ST_GraphAnalysis f = new ST_GraphAnalysis(connection, inputTable);
        createTables(f);

        final KeyedGraph graph =
                doAnalysisAndReturnGraph(connection, inputTable, orientation, weight);

        final boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        storeNodeCentrality(f, graph);
        storeEdgeCentrality(f, graph);

        connection.setAutoCommit(previousAutoCommit);
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

    private static void createTables(ST_GraphAnalysis f) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE " + f.nodesName + "(" +
                    NODE_ID + " INTEGER PRIMARY KEY, " +
                    BETWEENNESS + " DOUBLE, " +
                    CLOSENESS + " DOUBLE);");
            st.execute("CREATE TABLE " + f.edgesName + "(" +
                    EDGE_ID + " INTEGER PRIMARY KEY, " +
                    BETWEENNESS + " DOUBLE);");
        } finally {
            st.close();
        }
    }

    private static void storeNodeCentrality(ST_GraphAnalysis f, KeyedGraph graph) throws SQLException {
        final PreparedStatement nodeSt =
                connection.prepareStatement("INSERT INTO " + f.nodesName + " VALUES(?,?,?)");
        try {
            int count = 0;
            for (VCent v : (Set<VCent>) graph.vertexSet()) {
                nodeSt.setInt(1, v.getID());
                nodeSt.setDouble(2, v.getBetweenness());
                nodeSt.setDouble(3, v.getCloseness());
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
        } catch (SQLException e) {
            LOGGER.error("Problem creating node centrality table.");
            final Statement statement = connection.createStatement();
            try {
                statement.execute("DROP TABLE " + f.nodesName);
            } finally {
                statement.close();
            }
        } finally {
            nodeSt.close();
        }
    }

    private static void storeEdgeCentrality(ST_GraphAnalysis f, KeyedGraph graph) throws SQLException {
        final PreparedStatement edgeSt =
                connection.prepareStatement("INSERT INTO " + f.edgesName + " VALUES(?,?)");
        try {
            int count = 0;
            for (EdgeCent e : (Set<EdgeCent>) graph.edgeSet()) {
                edgeSt.setInt(1, e.getID());
                edgeSt.setDouble(2, e.getBetweenness());
                edgeSt.addBatch();
                count++;
                if (count >= BATCH_SIZE) {
                    edgeSt.executeBatch();
                    edgeSt.clearBatch();
                    count = 0;
                }
            }
            if (count > 0) {
                edgeSt.executeBatch();
                edgeSt.clearBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Problem creating edge centrality table.");
            final Statement statement = connection.createStatement();
            try {
                statement.execute("DROP TABLE " + f.edgesName);
            } finally {
                statement.close();
            }
        } finally {
            edgeSt.close();
        }
    }
}
