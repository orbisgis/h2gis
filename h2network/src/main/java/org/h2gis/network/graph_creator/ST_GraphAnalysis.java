package org.h2gis.network.graph_creator;

import org.h2gis.h2spatialapi.ScalarFunction;
import org.javanetworkanalyzer.analyzers.GraphAnalyzer;
import org.javanetworkanalyzer.analyzers.UnweightedGraphAnalyzer;
import org.javanetworkanalyzer.analyzers.WeightedGraphAnalyzer;
import org.javanetworkanalyzer.data.VWCent;
import org.javanetworkanalyzer.model.EdgeCent;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.jgrapht.WeightedGraph;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * Created by adam on 5/6/14.
 */
public class ST_GraphAnalysis extends GraphFunction implements ScalarFunction {

    @Override
    public String getJavaStaticMethod() {
        return "doGraphAnalysis";
    }

//    public static void doGraphAnalysis(Connection connection,
//                                        String inputTable,
//                                        String orientation) throws SQLException {
//        getGraphAnalysis(connection, inputTable, orientation, null);
//    }

    public static void doGraphAnalysis(Connection connection,
                                        String inputTable,
                                        String orientation,
                                        String weight)
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        final KeyedGraph graph = prepareGraph(connection, inputTable, orientation, weight, VWCent.class);
        GraphAnalyzer analyzer = (weight == null) ?
                new UnweightedGraphAnalyzer(graph) :
                new WeightedGraphAnalyzer((WeightedGraph) graph);
        analyzer.computeAll();
        for (VWCent v : (Set<VWCent>) graph.vertexSet()) {
            v.getID();
            v.getBetweenness();
            v.getCloseness();
        }
        for (EdgeCent e : (Set<EdgeCent>) graph.edgeSet()) {
            e.getID();
            e.getBetweenness();
        }
    }
}
