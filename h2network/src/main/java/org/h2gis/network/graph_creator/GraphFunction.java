package org.h2gis.network.graph_creator;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper class for Graph Functions.
 *
 * @author Adam Gouge
 */
public class GraphFunction extends AbstractFunction {

    /**
     * Return a JGraphT graph from the input edges table.
     *
     * @param connection  Connection
     * @param inputTable  Input table name
     * @param orientation Orientation string
     * @param weight      Weight column name, null for unweighted graphs
     * @return Graph
     * @throws SQLException
     */
    protected static KeyedGraph<VDijkstra, Edge> prepareGraph(Connection connection,
                                                              String inputTable,
                                                              String orientation,
                                                              String weight) throws SQLException {
        GraphFunctionParser parser = new GraphFunctionParser();
        parser.parseWeightAndOrientation(orientation, weight);

        return new GraphCreator<VDijkstra, Edge>(connection,
                inputTable,
                parser.getGlobalOrientation(), parser.getEdgeOrientation(), parser.getWeightColumn(),
                VDijkstra.class,
                Edge.class).prepareGraph();
    }

    /**
     * Return true if this connection only wants the list of columns.
     * This is a hack. See: https://groups.google.com/forum/#!topic/h2-database/NHH0rDeU258
     *
     * @param connection Connection
     * @return True if this connection only wants the list of columns
     * @throws SQLException
     */
    protected static boolean justAskingForColumns(Connection connection) throws SQLException {
        return connection.getMetaData().getURL().equals("jdbc:columnlist:connection");
    }
}
