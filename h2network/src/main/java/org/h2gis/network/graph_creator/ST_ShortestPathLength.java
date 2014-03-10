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
 * ST_ShortestPathLength
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

    public ST_ShortestPathLength() {
        addProperty(PROP_REMARKS, "ST_ShortestPathLength ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPathLength";
    }

    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value sourceOrTable) throws SQLException {
        if (sourceOrTable instanceof ValueInt) {
            int source = sourceOrTable.getInt();
            // 1: (o, s) = 5(null)
            return oneToAll(connection, inputTable, orientation, null, source);
        } else if (sourceOrTable instanceof ValueString) {
            String table = sourceOrTable.getString();
            // 2: (o, sdt) = 6(null)
            return manyToMany(connection, inputTable, orientation, null, table);
        }
        return null;
    }

    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value sourceOrWeight,
                                                  Value destinationOrSourceOrTableOrDestString) throws SQLException {
        if (sourceOrWeight instanceof ValueInt) {
            int source = sourceOrWeight.getInt();
            if (destinationOrSourceOrTableOrDestString instanceof ValueInt) {
                int destination = destinationOrSourceOrTableOrDestString.getInt();
                // 3: (o, s, d) = 7(null)
                return oneToOne(connection, inputTable, orientation, null, source, destination);
            } else if (destinationOrSourceOrTableOrDestString instanceof ValueString) {
                String destinationString = destinationOrSourceOrTableOrDestString.getString();
                // 2: (o, s, ds) = 8(null)
                return oneToSeveral(connection, inputTable, orientation, null, source, destinationString);
            }
        } else if (sourceOrWeight instanceof ValueString) {
            String weight = sourceOrWeight.getString();
            if (destinationOrSourceOrTableOrDestString instanceof ValueInt) {
                int source = destinationOrSourceOrTableOrDestString.getInt();
                // 5: (o, w, s)
                return oneToAll(connection, inputTable, orientation, weight, source);
            } else if (destinationOrSourceOrTableOrDestString instanceof ValueString) {
                String table = destinationOrSourceOrTableOrDestString.getString();
                // 6: (o, w, sdt).
                return manyToMany(connection, inputTable, orientation, weight, table);
            } // TODO: else.
        } else {
            throw new IllegalArgumentException("Unrecognized argument.");
        }
        return null;
    }

    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  String weight,
                                                  int source,
                                                  Value destinationOrDestString) throws SQLException {
        if (destinationOrDestString instanceof ValueInt) {
            int destination = destinationOrDestString.getInt();
            // 7: (o, w, s, d)
            return oneToOne(connection, inputTable, orientation, weight, source, destination);
        } else if (destinationOrDestString instanceof ValueString) {
            String destinationString = destinationOrDestString.getString();
            // 8: (o, w, s, ds)
            return oneToSeveral(connection, inputTable, orientation, weight, source, destinationString);
        }
        return null;
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
            final ResultSet sourceDestinationRS =
                    st.executeQuery("SELECT * FROM " + sourceDestinationTable);
            // Make sure the source-destination table has columns named
            // SOURCE and DESTINATION. An SQLException is thrown if not.
            final int sourceIndex = sourceDestinationRS.findColumn(SOURCE);
            final int destinationIndex = sourceDestinationRS.findColumn(DESTINATION);

            // Prepare the source-destination map from the source-destination table.
            Map<VDijkstra, Set<VDijkstra>> sourceDestinationMap =
                    prepareSourceDestinationMap(sourceDestinationRS,
                            graph,
                            sourceIndex,
                            destinationIndex);
            if (sourceDestinationMap.isEmpty()) {
                throw new IllegalArgumentException("No sources/destinations requested.");
            }

            // 6: (o, w, sdt).
            // Do One-to-Many many times and store the results.
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

    private static Map<VDijkstra, Set<VDijkstra>> prepareSourceDestinationMap(
            ResultSet sourceDestinationRS,
            KeyedGraph<VDijkstra, Edge> graph,
            int sourceIndex,
            int destinationIndex) throws SQLException {
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
        return map;
    }

    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(DISTANCE, Types.DOUBLE, 10, 0);
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
