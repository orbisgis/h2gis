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

import org.h2.tools.SimpleResultSet;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueString;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.JDBCUtilities;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.h2gis.h2spatial.TableFunctionUtil.isColumnListConnection;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates the length(s) of shortest path(s) between vertices in a JGraphT
 * graph produced from the input_edges table produced by ST_Graph.
 *
 * @author Adam Gouge
 */
public class ST_ShortestPathLength extends GraphFunction implements ScalarFunction {

    public static final int SOURCE_INDEX = 1;
    public static final int DESTINATION_INDEX = 2;
    public static final int DISTANCE_INDEX = 3;

    public static final String REMARKS =
            "`ST_ShortestPathLength` calculates the length(s) of shortest path(s) among\n" +
            "vertices in a graph. Possible signatures:\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', s)` - One-to-All\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', 'sdt')` - Many-to-Many\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', s, d)` - One-to-One\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', s, 'ds')` - One-to-Several\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s)` - One-to-All weighted\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', 'sdt')` - Many-to-Many weighted\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s, d)` - One-to-One weighted\n" +
            "* `ST_ShortestPathLength('input_edges', 'o[ - eo]', 'w', s, 'ds')` - One-to-Several weighted\n" +
            "\n" +
            "where\n" +
            "* `input_edges` = Edges table produced by `ST_Graph` from table `input`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected).\n" +
            "  Required if global orientation is directed or reversed.\n" +
            "* `w` = Name of column containing edge weights as doubles\n" +
            "* `s` = Source vertex id\n" +
            "* `d` = Destination vertex id\n" +
            "* `sdt` = Source-Destination table name (must contain columns\n" +
            "  " + SOURCE + " and " + DESTINATION + " containing integer vertex ids)\n" +
            "* `ds` = Comma-separated Destination string ('dest1, dest2, ...')\n";


    /**
     * Constructor
     */
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
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param arg3        Source vertex id -OR- Source-Destination table
     * @return Distances table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value arg3) throws SQLException {
        if (isColumnListConnection(connection)) {
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
     * <li> Many-to-Many unweighted: <code>(arg3, arg4) = (st, dt)</code>.</li>
     * <li> Many-to-Many weighted: <code>(arg3, arg4) = (w, sdt)</code>.</li>
     * </ol>
     *
     * <p>The Source-Destination table must contain a column named SOURCE and a
     * column named DESTINATION, both consisting of integer IDs.
     *
     * @param connection  connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param arg3        Source vertex id -OR- Weight column name -OR- Source table
     * @param arg4        Destination vertex id -OR- Destination string -OR-
     *                    Source vertex id -OR- Source-Destination table -OR-
     *                    Destination table
     * @return Distances table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value arg3,
                                                  Value arg4) throws SQLException {
        if (isColumnListConnection(connection)) {
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
            final String arg3String = arg3.getString();
            if (JDBCUtilities.hasField(connection, inputTable, arg3String)) {
                final String weight = arg3String;
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
                final String sourceTable = arg3String;
                if (arg4 instanceof ValueString) {
                    final String destTable = arg4.getString();
                    return manyToManySeparateTables(connection, inputTable, orientation, null, sourceTable, destTable);
                } else {
                    throw new IllegalArgumentException(ARG_ERROR + arg4);
                }
            }
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg3);
        }
    }

    /**
     * Calculate distances for
     * <ol>
     * <li> One-to-One weighted: <code>(arg4, arg5) = (w, d) </code>,</li>
     * <li> One-to-Several weighted: <code>(arg4, arg5) = (w, ds)</code>.</li>
     * <li> Many-to-Many weighted: <code>(arg4, arg5) = (st, dt)</code>.</li>
     * </ol>
     *
     * @param connection  Connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param weight      Weight column name, null for unweighted graphs
     * @param arg4        Source vertex id -OR- Source table
     * @param arg5        Destination vertex id -OR- Destination string -OR- Destination table
     * @return Distances table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  String weight,
                                                  Value arg4,
                                                  Value arg5) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        if (arg4 instanceof ValueInt) {
            final int source = arg4.getInt();
            if (arg5 instanceof ValueInt) {
                int destination = arg5.getInt();
                return oneToOne(connection, inputTable, orientation, weight, source, destination);
            } else if (arg5 instanceof ValueString) {
                String destinationString = arg5.getString();
                return oneToSeveral(connection, inputTable, orientation, weight, source, destinationString);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg5);
            }
        } else if (arg4 instanceof ValueString) {
            final String sourceTable = arg4.getString();
            if (arg5 instanceof ValueString) {
                final String destTable = arg5.getString();
                return manyToManySeparateTables(connection, inputTable, orientation, weight, sourceTable, destTable);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg4);
            }
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg4);
        }
    }

    private static ResultSet oneToOne(Connection connection,
                                     String inputTable,
                                     String orientation,
                                     String weight,
                                     int source,
                                     int destination) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);
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
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);
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
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);
        final Statement st = connection.createStatement();
        try {
            // Prepare the source-destination map from the source-destination table.
            Map<VDijkstra, Set<VDijkstra>> sourceDestinationMap =
                    prepareSourceDestinationMap(st, sourceDestinationTable, graph);

            // Reusable Dijkstra object.
            final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);

            // 6: (o, w, sdt). Do One-to-Many many times and store the results.
            for (Map.Entry<VDijkstra, Set<VDijkstra>> sourceToDestSetMap : sourceDestinationMap.entrySet()) {
                Map<VDijkstra, Double> distances =
                        dijkstra.oneToMany(sourceToDestSetMap.getKey(),
                                           sourceToDestSetMap.getValue());
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

    private static ResultSet manyToManySeparateTables(
            Connection connection,
            String inputTable,
            String orientation,
            String weight,
            String sourceTable,
            String destTable) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);
        final Statement st = connection.createStatement();
        try {
            final Set<VDijkstra> destSet = getSet(st, graph, destTable);
            final Set<VDijkstra> sourceSet = getSet(st, graph, sourceTable);
            final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
            for (VDijkstra source : sourceSet) {
                Map<VDijkstra, Double> distances =
                        dijkstra.oneToMany(source, destSet);
                for (Map.Entry<VDijkstra, Double> destToDistMap : distances.entrySet()) {
                    output.addRow(source.getID(),
                            destToDistMap.getKey().getID(), destToDistMap.getValue());
                }
            }
        } finally {
            st.close();
        }
        return output;
    }

    /**
     * Puts the integers contained in the first column of the table in a Set of
     * corresponding VDijkstra.
     *
     * @param st        Statement
     * @param graph     Graph
     * @param tableName Table
     * @return Set of VDijkstra
     * @throws SQLException
     */
    private static Set<VDijkstra> getSet(Statement st,
            KeyedGraph<VDijkstra, Edge> graph, String tableName) throws SQLException {
        final ResultSet intSet =
                st.executeQuery("SELECT * FROM " + tableName);
        try {
            final Set<VDijkstra> set = new HashSet<VDijkstra>();
            while (intSet.next()) {
                final int vertexID = intSet.getInt(1);
                final VDijkstra vertex = graph.getVertex(vertexID);
                if (vertex == null) {
                    throw new IllegalArgumentException("The graph does not contain vertex " + vertexID);
                }
                set.add(vertex);
            }
            if (set.isEmpty()) {
                throw new IllegalArgumentException("Table " + tableName + " was empty.");
            }
            return set;
        } finally {
            intSet.close();
        }
    }

    private static ResultSet oneToSeveral(Connection connection,
                                          String inputTable,
                                          String orientation,
                                          String weight,
                                          int source,
                                          String destString) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);

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
                st.executeQuery("SELECT " +
                        SOURCE + ", " + DESTINATION +
                        " FROM " + sourceDestinationTable);
        try {
            // Make sure the source-destination table has columns named
            // SOURCE and DESTINATION. An SQLException is thrown if not.
            Map<VDijkstra, Set<VDijkstra>> map = new HashMap<VDijkstra, Set<VDijkstra>>();
            while (sourceDestinationRS.next()) {
                final VDijkstra source = graph.getVertex(sourceDestinationRS.getInt(SOURCE_INDEX));
                final VDijkstra destination = graph.getVertex(sourceDestinationRS.getInt(DESTINATION_INDEX));
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
        } finally {
            sourceDestinationRS.close();
        }
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
}
