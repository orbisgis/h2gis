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

package org.h2gis.network.graph_creator;

import org.h2.tools.SimpleResultSet;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ST_ShortestPathLength
 *
 * @author Adam Gouge
 */
public class ST_ShortestPathLength extends AbstractFunction implements ScalarFunction {

    public static final int SOURCE_INDEX = 1;
    public static final int DESTINATION_INDEX = 2;
    public static final int DISTANCE_INDEX = 3;

    public ST_ShortestPathLength() {
        addProperty(PROP_REMARKS, "ST_ShortestPathLength ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPathLength";
    }

    /**
     * 4. One-to-One unweighted
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  int source,
                                                  int destination) throws SQLException {
        return getShortestPathLength(connection, inputTable, orientation, null, source, destination);
    }

    /**
     * 5. One-to-One weighted
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  String weight,
                                                  int source,
                                                  int destination) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);
        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        return oneToOne(graph, dijkstra, output, source, destination);
    }

    private static ResultSet oneToOne(KeyedGraph<VDijkstra, Edge> graph,
                                      Dijkstra<VDijkstra, Edge> dijkstra,
                                      SimpleResultSet output,
                                      int source,
                                      int destination) {
        final double distance = dijkstra.oneToOne(graph.getVertex(source), graph.getVertex(destination));
        output.addRow(source, destination, distance);
        return output;
    }

    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn("SOURCE", Types.INTEGER, 10, 0);
        output.addColumn("DESTINATION", Types.INTEGER, 10, 0);
        output.addColumn("DISTANCE", Types.DOUBLE, 10, 0);
        return output;
    }

    private static KeyedGraph<VDijkstra, Edge> prepareGraph(Connection connection,
                                                            String inputTable,
                                                            String orientation,
                                                            String weight) throws SQLException {
        GraphFunctionParser parser = new GraphFunctionParser();
        parser.parseWeightAndOrientation(orientation, weight);

        return new GraphCreator<VDijkstra, Edge>(connection,
                inputTable,
                parser.getWeightColumn(),
                parser.getGlobalOrientation(),
                parser.getEdgeOrientation(),
                VDijkstra.class,
                Edge.class).prepareGraph();
    }
}
