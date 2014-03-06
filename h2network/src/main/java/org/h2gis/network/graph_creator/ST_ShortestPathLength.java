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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

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
        }
        return null;
    }

    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  String orientation,
                                                  Value sourceOrWeight,
                                                  Value destinationOrSource) throws SQLException {
        if (sourceOrWeight instanceof ValueInt) {
            int source = sourceOrWeight.getInt();
            if (destinationOrSource instanceof ValueInt) {
                int destination = destinationOrSource.getInt();
                // 3: (o, s, d) = 7(null)
                return oneToOne(connection, inputTable, orientation, null, source, destination);
            } // TODO: else.
        } else if (sourceOrWeight instanceof ValueString) {
            String weight = sourceOrWeight.getString();
            if (destinationOrSource instanceof ValueInt) {
                int source = destinationOrSource.getInt();
                // 5: (o, w, s)
                return oneToAll(connection, inputTable, orientation, weight, source);
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
                                                  int destination) throws SQLException {
        // 7: (o, w, s, d)
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
        // 7: (o, w, s, d)
        final double distance = dijkstra.oneToOne(graph.getVertex(source), graph.getVertex(destination));
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
        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        // 5: (o, w, s)
        final Map<VDijkstra,Double> distances =
                dijkstra.oneToMany(graph.getVertex(source), graph.vertexSet());
        for (Map.Entry<VDijkstra, Double> e : distances.entrySet()) {
            output.addRow(source, e.getKey().getID(), e.getValue());
        }
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
