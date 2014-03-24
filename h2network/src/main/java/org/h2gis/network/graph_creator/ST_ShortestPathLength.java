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
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueString;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ST_ShortestPathLength calculates the length(s) of shortest path(s) among
 * vertices in a JGraphT graph produced from an edges table produced by {@link
 * org.h2gis.network.graph_creator.ST_Graph}.
 *
 * <p>Possible signatures:
 * <ol>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', s) </code> - One-to-All</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'sdt') </code> - Many-to-Many</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', s, d) </code> - One-to-One</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', s, 'ds') </code> - One-to-Several</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s) </code> - One-to-All weighted</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', 'sdt') </code> - Many-to-Many weighted</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s, d) </code> - One-to-One weighted</li>
 * <li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s, 'ds') </code> - One-to-Several weighted</li>
 * </ol>
 * where
 * <ul>
 * <li><code>input_edges</code> = Edges table produced by <code>ST_Graph</code> from table <code>input</code></li>
 * <li><code>o</code> = Global orientation (directed, reversed or undirected)</li>
 * <li><code>eo</code> = Edge orientation (1 = directed, -1 = reversed, 0 =
 * undirected). Required if global orientation is directed or reversed.</li>
 * <li><code>s</code> = Source vertex id</li>
 * <li><code>d</code> = Destination vertex id</li>
 * <li><code>sdt</code> = Source-Destination table name (must contain columns
 * SOURCE and DESTINATION containing integer vertex ids)</li>
 * <li><code>ds</code> = Comma-separated Destination string ("dest1, dest2, ...")</li>
 * </ul>
 *
 * @author Adam Gouge
 */
public class ST_ShortestPathLength extends AbstractFunction implements ScalarFunction {

    public static final int SOURCE_INDEX = 1;
    public static final int DESTINATION_INDEX = 2;
    public static final int DISTANCE_INDEX = 3;
    public static final String SOURCE  = "SOURCE";
    public static final String DESTINATION  = "DESTINATION";
    public static final String DISTANCE  = "DISTANCE";

    private static final String hackURL = "jdbc:columnlist:connection";

    private static final String ARG_ERROR  = "Unrecognized argument: ";
    public static final String REMARKS =
            "ST_ShortestPathLength calculates the length(s) of shortest path(s) among " +
            "vertices in a JGraphT graph produced from an edges table produced by {@link " +
            "org.h2gis.network.graph_creator.ST_Graph}. " +
            "<p>Possible signatures: " +
            "<ol> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', s) </code> - One-to-All</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'sdt') </code> - Many-to-Many</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', s, d) </code> - One-to-One</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', s, 'ds') </code> - One-to-Several</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s) </code> - One-to-All weighted</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', 'sdt') </code> - Many-to-Many weighted</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s, d) </code> - One-to-One weighted</li> " +
            "<li><code> ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s, 'ds') </code> - One-to-Several weighted</li> " +
            "</ol> " +
            "where " +
            "<ul> " +
            "<li><code>input_edges</code> = Edges table produced by <code>ST_Graph</code> from table <code>input</code></li> " +
            "<li><code>o</code> = Global orientation (directed, reversed or undirected)</li> " +
            "<li><code>eo</code> = Edge orientation (1 = directed, -1 = reversed, 0 = " +
            "undirected). Required if global orientation is directed or reversed.</li> " +
            "<li><code>s</code> = Source vertex id</li> " +
            "<li><code>d</code> = Destination vertex id</li> " +
            "<li><code>sdt</code> = Source-Destination table name (must contain columns " +
            "SOURCE and DESTINATION containing integer vertex ids)</li> " +
            "<li><code>ds</code> = Comma-separated Destination string ('dest1, dest2, ...')</li> " +
            "</ul> ";

    public ST_ShortestPathLength() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPathLength";
    }

    /**
     * Calculate distances for
     * <ol>
     * <li> One-to-All: <code>arg3 = s</code>,</li>
     * <li> Many-to-Many: <code>arg3 = sdt</code>.</li>
     * </ol>
     *
     * <p>The Source-Destination table must contain a column named SOURCE and a
     * column named DESTINATION, both consisting of integer IDs.
     *
     * @param connection  Connection
     * @param inputTable  Input table
     * @param orientation Orientation string
     * @param arg3        Source vertex id -OR- Source-Destination table
     * @return Distances table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value arg3) throws SQLException {
        if (connection.getMetaData().getURL().equals(hackURL)) {
            return prepareResultSet();
        }
        if (arg3 instanceof ValueInt) {
            int source = arg3.getInt();
            return oneToAll(connection, inputTable, orientation, null, source);
        } else if (arg3 instanceof ValueString) {
            String table = arg3.getString();
            return manyToMany(connection, inputTable, orientation, null, table);
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg3);
        }
    }

    /**
     * Calculate distances for
     * <ol>
     * <li> One-to-One: <code>(arg3, arg4) = (s, d)</code>,</li>
     * <li> One-to-Several: <code>(arg3, arg4) = (s, ds)</code>,</li>
     * <li> One-to-All weighted: <code>(arg3, arg4) = (w, s)</code>,</li>
     * <li> Many-to-Many weighted: <code>(arg3, arg4) = (w, sdt)</code>.</li>
     * </ol>
     *
     * <p>The Source-Destination table must contain a column named SOURCE and a
     * column named DESTINATION, both consisting of integer IDs.
     *
     * @param connection  connection
     * @param inputTable  Input table
     * @param orientation Orientation string
     * @param arg3        Source vertex id -OR- Weight column name
     * @param arg4        Destination vertex id -OR- Destination string -OR-
     *                    Source vertex id -OR- Source-Destination table
     * @return Distances table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value arg3,
                                                  Value arg4) throws SQLException {
        if (connection.getMetaData().getURL().equals(hackURL)) {
            return prepareResultSet();
        }
        if (arg3 instanceof ValueInt) {
            int source = arg3.getInt();
            if (arg4 instanceof ValueInt) {
                int destination = arg4.getInt();
                return oneToOne(connection, inputTable, orientation, null, source, destination);
            } else if (arg4 instanceof ValueString) {
                String destinationString = arg4.getString();
                return oneToSeveral(connection, inputTable, orientation, null, source, destinationString);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg4);
            }
        } else if (arg3 instanceof ValueString) {
            String weight = arg3.getString();
            if (arg4 instanceof ValueInt) {
                int source = arg4.getInt();
                return oneToAll(connection, inputTable, orientation, weight, source);
            } else if (arg4 instanceof ValueString) {
                String table = arg4.getString();
                return manyToMany(connection, inputTable, orientation, weight, table);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg4);
            }
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg3);
        }
    }

    /**
     * Calculate distances for
     * <ol>
     * <li> One-to-One weighted: <code>arg5 = d</code>,</li>
     * <li> One-to-Several weighted: <code>arg5 = ds</code>.</li>
     * </ol>
     *
     * @param connection  Connection
     * @param inputTable  Input table
     * @param orientation Orientation string
     * @param weight      Weight column name, null for unweighted graphs
     * @param source      Source vertex id
     * @param arg5        Destination vertex id -OR- Destination string
     * @return Distances table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  String weight,
                                                  int source,
                                                  Value arg5) throws SQLException {
        if (connection.getMetaData().getURL().equals(hackURL)) {
            return prepareResultSet();
        }
        if (arg5 instanceof ValueInt) {
            int destination = arg5.getInt();
            return oneToOne(connection, inputTable, orientation, weight, source, destination);
        } else if (arg5 instanceof ValueString) {
            String destinationString = arg5.getString();
            return oneToSeveral(connection, inputTable, orientation, weight, source, destinationString);
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg5);
        }
    }

    private static ResultSet oneToOne(Connection connection,
                                     String inputTable,
                                     String orientation,
                                     String weight,
                                     int source,
                                     int destination) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);
        // 7: (o, w, s, d)
        final double distance = new Dijkstra<VDijkstra, Edge>(graph)
                .oneToOne(graph.getVertex(source), graph.getVertex(destination));
        output.addRow(source, destination, distance);
        return output;
    }

    private static ResultSet oneToAll(Connection connection,
                                      String inputTable,
                                      String orientation,
                                      String weight,
                                      int source) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);
        // 5: (o, w, s)
        final Map<VDijkstra,Double> distances = new Dijkstra<VDijkstra, Edge>(graph)
                        .oneToMany(graph.getVertex(source), graph.vertexSet());
        for (Map.Entry<VDijkstra, Double> e : distances.entrySet()) {
            output.addRow(source, e.getKey().getID(), e.getValue());
        }
        return output;
    }

    private static ResultSet manyToMany(Connection connection,
                                        String inputTable,
                                        String orientation,
                                        String weight,
                                        String sourceDestinationTable) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);
        final Statement st = connection.createStatement();
        try {
            // Prepare the source-destination map from the source-destination table.
            Map<VDijkstra, Set<VDijkstra>> sourceDestinationMap =
                    prepareSourceDestinationMap(st, sourceDestinationTable, graph);

            // 6: (o, w, sdt). Do One-to-Many many times and store the results.
            for (Map.Entry<VDijkstra, Set<VDijkstra>> sourceToDestSetMap : sourceDestinationMap.entrySet()) {
                Map<VDijkstra, Double> distances = new Dijkstra<VDijkstra, Edge>(graph)
                        .oneToMany(sourceToDestSetMap.getKey(), sourceToDestSetMap.getValue());
                for (Map.Entry<VDijkstra, Double> destToDistMap : distances.entrySet()) {
                    output.addRow(sourceToDestSetMap.getKey().getID(),
                            destToDistMap.getKey().getID(), destToDistMap.getValue());
                }
            }
        } finally {
            st.close();
        }
        return output;
    }

    private static ResultSet oneToSeveral(Connection connection,
                                          String inputTable,
                                          String orientation,
                                          String weight,
                                          int source,
                                          String destString) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);

        final int[] destIDs = GraphFunctionParser.parseDestinationsString(destString);
        Set<VDijkstra> destSet = new HashSet<VDijkstra>();
        for (int d : destIDs)  {
            final VDijkstra dest = graph.getVertex(d);
            if (dest == null) {
                throw new IllegalArgumentException("The graph does not contain vertex " + d);
            }
            destSet.add(dest);
        }
        // 8: (o, w, s, ds)
        final Map<VDijkstra, Double> distances = new Dijkstra<VDijkstra, Edge>(graph)
                .oneToMany(graph.getVertex(source), destSet);
        for (Map.Entry<VDijkstra, Double> e : distances.entrySet()) {
            output.addRow(source, e.getKey().getID(), e.getValue());
        }
        return output;
    }

    /**
     * Prepare the source-destination map (to which we will apply Dijkstra) from
     * the source-destination table.
     *
     * @param sourceDestinationTable Source-Destination table name
     * @param graph                  Graph
     * @return Source-Destination map
     * @throws SQLException
     */
    private static Map<VDijkstra, Set<VDijkstra>> prepareSourceDestinationMap(
            Statement st,
            String sourceDestinationTable,
            KeyedGraph<VDijkstra, Edge> graph) throws SQLException {
        final ResultSet sourceDestinationRS =
                st.executeQuery("SELECT * FROM " + sourceDestinationTable);
        // Make sure the source-destination table has columns named
        // SOURCE and DESTINATION. An SQLException is thrown if not.
        final int sourceIndex = sourceDestinationRS.findColumn(SOURCE);
        final int destinationIndex = sourceDestinationRS.findColumn(DESTINATION);
        Map<VDijkstra, Set<VDijkstra>> map = new HashMap<VDijkstra, Set<VDijkstra>>();
        while (sourceDestinationRS.next()) {
            final VDijkstra source = graph.getVertex(sourceDestinationRS.getInt(sourceIndex));
            final VDijkstra destination = graph.getVertex(sourceDestinationRS.getInt(destinationIndex));
            Set<VDijkstra> targets = map.get(source);
            // Lazy initialize if the destinations set is null.
            if (targets == null) {
                targets = new HashSet<VDijkstra>();
                map.put(source, targets);
            }
            // Add the destination.
            targets.add(destination);
        }
        if (map.isEmpty()) {
            throw new IllegalArgumentException("No sources/destinations requested.");
        }
        return map;
    }

    /**
     * Return a new {@link org.h2.tools.SimpleResultSet} with SOURCE,
     * DESTINATION and DISTANCE columns.
     * @return a new {@link org.h2.tools.SimpleResultSet} with SOURCE,
     * DESTINATION and DISTANCE columns
     */
    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(DISTANCE, Types.DOUBLE, 10, 0);
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
