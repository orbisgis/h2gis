package org.h2gis.network.graph_creator;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper class for Graph Functions.
 *
 * @author Adam Gouge
 */
public class GraphFunction extends AbstractFunction {

    public static final String ARG_ERROR  = "Unrecognized argument: ";

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
    protected static KeyedGraph prepareGraph(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String weight,
                                             Class vertexClass,
                                             Class edgeClass) throws SQLException {
        GraphFunctionParser parser = new GraphFunctionParser();
        parser.parseWeightAndOrientation(orientation, weight);

        return new GraphCreator(connection,
                inputTable,
                parser.getGlobalOrientation(), parser.getEdgeOrientation(), parser.getWeightColumn(),
                vertexClass,
                edgeClass).prepareGraph();
    }
}
