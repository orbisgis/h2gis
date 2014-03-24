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
import java.util.HashSet;
import java.util.Set;

/**
 * ST_ShortestPath calculates the shortest path(s) between
 * vertices in a JGraphT graph produced from an edges table produced by {@link
 * org.h2gis.network.graph_creator.ST_Graph}.
 *
 * <p>Possible signatures:
 * <ol>
 * <li><code> ST_ShortestPath('input_edges', 'o[ - eo]', s, d) </code> - One-to-One</li>
 * <li><code> ST_ShortestPath('input_edges', 'o[ - eo]', 'w', s, d) </code> - One-to-One weighted</li>
 * </ol>
 * where
 * <ul>
 * <li><code>input_edges</code> = Edges table produced by <code>ST_Graph</code> from table <code>input</code></li>
 * <li><code>o</code> = Global orientation (directed, reversed or undirected)</li>
 * <li><code>eo</code> = Edge orientation (1 = directed, -1 = reversed, 0 =
 * undirected). Required if global orientation is directed or reversed.</li>
 * <li><code>w</code> = Name of column containing edge weights as doubles</li>
 * <li><code>s</code> = Source vertex id</li>
 * <li><code>d</code> = Destination vertex id</li>
 * </ul>
 *
 * @author Adam Gouge
 */
public class ST_ShortestPath extends AbstractFunction implements ScalarFunction {

    public static final String GEOM = "THE_GEOM";
    public static final String EDGE_ID = "EDGE_ID";
    public static final String PATH_ID = "PATH_ID";
    public static final String SOURCE = "SOURCE";
    public static final String DESTINATION = "DESTINATION";
    public static final String WEIGHT = "WEIGHT";

    private static final String ARG_ERROR  = "Unrecognized argument: ";
    public static final String REMARKS =
            "ST_ShortestPath calculates the shortest path(s) between " +
            "vertices in a JGraphT graph produced from an edges table produced by {@link " +
            "org.h2gis.network.graph_creator.ST_Graph}. " +
            "<p>Possible signatures: " +
            "<ol> " +
            "<li><code> ST_ShortestPath('input_edges', 'o[ - eo]', s, d) </code> - One-to-One</li> " +
            "<li><code> ST_ShortestPath('input_edges', 'o[ - eo]', 'w', s, d) </code> - One-to-One weighted</li> " +
            "</ol> " +
            "where " +
            "<ul> " +
            "<li><code>input_edges</code> = Edges table produced by <code>ST_Graph</code> from table <code>input</code></li> " +
            "<li><code>o</code> = Global orientation (directed, reversed or undirected)</li> " +
            "<li><code>eo</code> = Edge orientation (1 = directed, -1 = reversed, 0 = " +
            "undirected). Required if global orientation is directed or reversed.</li> " +
            "<li><code>w</code> = Name of column containing edge weights as doubles</li> " +
            "<li><code>s</code> = Source vertex id</li> " +
            "<li><code>d</code> = Destination vertex id</li> " +
            "</ul> ";

    public ST_ShortestPath() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPath";
    }

    /**
     * @param connection  connection
     * @param inputTable  Input table
     * @param orientation Orientation string
     * @param source      Source vertex id
     * @param destination Destination vertex id
     * @return Shortest path
     * @throws SQLException
     */
    public static ResultSet getShortestPath(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  int source,
                                                  int destination) throws SQLException {
        return oneToOne(connection, inputTable, orientation, null, source, destination);
    }

    /**
     * @param connection  connection
     * @param inputTable  Input table
     * @param orientation Orientation string
     * @param weight      Weight
     * @param source      Source vertex id
     * @param destination Destination vertex id
     * @return Shortest path
     * @throws SQLException
     */
    public static ResultSet getShortestPath(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  String weight,
                                                  int source,
                                                  int destination) throws SQLException {
        return oneToOne(connection, inputTable, orientation, weight, source, destination);
    }

    private static ResultSet oneToOne(Connection connection,
                                     String inputTable,
                                     String orientation,
                                     String weight,
                                     int source,
                                     int destination) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);
        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        final VDijkstra vDestination = graph.getVertex(destination);
        dijkstra.oneToOne(graph.getVertex(source), vDestination);
        // Rebuild the shortest path(s). (Yes, there could be more than
        // one if they have the same distance!)
        VDijkstra previousDestination = vDestination;
        Set<Edge> predecessorEdges = vDestination.getPredecessorEdges();
        final Set<Edge> nextPredecessorEdges = new HashSet<Edge>();
        int newID = 1;
        while (!predecessorEdges.isEmpty()) {
            nextPredecessorEdges.clear();
            for (Edge e : predecessorEdges) {
                final VDijkstra eSource = graph.getEdgeSource(e);
                final VDijkstra eDestination = graph.getEdgeTarget(e);
                int sourceID;
                int destinationID;
                // With undirected graphs, the source and target could
                // be switched. This is JGraphT's fault. Here we make
                // sure they are in the right order.
                if (previousDestination.equals(eDestination)) {
                    sourceID = eSource.getID();
                    destinationID = eDestination.getID();
                    nextPredecessorEdges.addAll(eSource.getPredecessorEdges());
                    previousDestination = eSource;
                } else if (previousDestination.equals(eSource)) {
                    sourceID = eDestination.getID();
                    destinationID = eSource.getID();
                    nextPredecessorEdges.addAll(eDestination.getPredecessorEdges());
                    previousDestination = eDestination;
                } else {
                    throw new IllegalStateException("A vertex has a predecessor " +
                            "edge not ending on itself.");
                }
                // TODO: Add the edge geometry.
                output.addRow(null, e.getID(), newID++, sourceID, destinationID, graph.getEdgeWeight(e));
            }
            predecessorEdges = nextPredecessorEdges;
        }
        return output;
    }

    /**
     * Return a new {@link org.h2.tools.SimpleResultSet} with SOURCE,
     * DESTINATION and DISTANCE columns.
     * @return a new {@link org.h2.tools.SimpleResultSet} with SOURCE,
     * DESTINATION and DISTANCE columns
     */
    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn(GEOM, Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        output.addColumn(EDGE_ID, Types.INTEGER, 10, 0);
        output.addColumn(PATH_ID, Types.INTEGER, 10, 0);
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(WEIGHT, Types.DOUBLE, 10, 0);
        return output;
    }

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
    private static KeyedGraph<VDijkstra, Edge> prepareGraph(Connection connection,
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
}
