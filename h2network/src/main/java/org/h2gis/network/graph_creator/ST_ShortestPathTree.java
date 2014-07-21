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

import com.vividsolutions.jts.geom.Geometry;
import org.h2.tools.SimpleResultSet;
import org.h2.value.*;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.javanetworkanalyzer.model.TraversalGraph;

import java.sql.*;
import java.util.Map;

import static org.h2gis.h2spatial.TableFunctionUtil.isColumnListConnection;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates the shortest path(s) between vertices in a JGraphT graph produced
 * from the input_edges table produced by ST_Graph.
 *
 * @author Adam Gouge
 */
public class ST_ShortestPathTree extends GraphFunction implements ScalarFunction {

    public static final int GEOM_INDEX = 1;
    public static final int EDGE_ID_INDEX = 2;
    public static final int TREE_ID_INDEX = 3;
    public static final int SOURCE_INDEX = 4;
    public static final int DESTINATION_INDEX = 5;
    public static final int WEIGHT_INDEX = 6;

    public static final String NO_GEOM_FIELD_ERROR = "The input table must contain a geometry field.";

    public static final String REMARKS =
            "Calculates the shortest path tree from a given vertex of a\n" +
            "graph using Dijkstra's algorithm.\n" +
            "Possible signatures:\n" +
            "* `ST_ShortestPathTree('INPUT_EDGES', 'o[ - eo]', s)`\n" +
            "* `ST_ShortestPathTree('INPUT_EDGES', 'o[ - eo]', s, r)`\n" +
            "* `ST_ShortestPathTree('INPUT_EDGES', 'o[ - eo]', 'w', s)`\n" +
            "* `ST_ShortestPathTree('INPUT_EDGES', 'o[ - eo]', 'w', s, r)`\n" +
            "\n" +
            "where\n" +
            "* `INPUT_EDGES` = Edges table produced by `ST_Graph` from table `INPUT`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected).\n" +
            "   Required if global orientation is directed or reversed.\n" +
            "* `s` = Source vertex id\n" +
            "* `r` = Radius by which to limit the search (a `DOUBLE`)\n" +
            "* `w` = Name of column containing edge weights as `DOUBLES`\n";

    /**
     * Constructor
     */
    public ST_ShortestPathTree() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPathTree";
    }

    /**
     * @param connection  connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param source      Source vertex id
     * @return Shortest path tree
     * @throws SQLException
     */
    public static ResultSet getShortestPathTree(Connection connection,
                                            String inputTable,
                                            String orientation,
                                            int source) throws SQLException {
        return oneToAll(connection, inputTable, orientation, null, source, Double.POSITIVE_INFINITY);
    }

    /**
     * @param connection  connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param arg4        Source vertex id or Weight
     * @param arg5        Search radius or Source vertex id
     * @return Shortest path tree
     * @throws SQLException
     */
    public static ResultSet getShortestPathTree(Connection connection,
                                            String inputTable,
                                            String orientation,
                                            Value arg4,
                                            Value arg5) throws SQLException {
        if (arg4 instanceof ValueInt) {
            final int source = arg4.getInt();
            if (arg5 instanceof ValueDecimal) {
                final double radius = arg5.getDouble();
                return oneToAll(connection, inputTable, orientation, null, source, radius);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg5);
            }
        } else if (arg4 instanceof ValueString) {
            final String weight = arg4.getString();
            if (arg5 instanceof ValueInt) {
                final int source = arg5.getInt();
                return oneToAll(connection, inputTable, orientation, weight, source, Double.POSITIVE_INFINITY);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg5);
            }
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg4);
        }
    }

    /**
     * @param connection  connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param weight      Weight
     * @param source      Source vertex id
     * @param radius      Search radius
     * @return Shortest path tree
     * @throws SQLException
     */
    public static ResultSet getShortestPathTree(Connection connection,
                                            String inputTable,
                                            String orientation,
                                            String weight,
                                            int source,
                                            double radius) throws SQLException {
        return oneToAll(connection, inputTable, orientation, weight, source, radius);
    }

    private static ResultSet oneToAll(Connection connection,
                                      String inputTable,
                                      String orientation,
                                      String weight,
                                      int source,
                                      double radius) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        // Do the calculation.
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);

        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        final VDijkstra vSource = graph.getVertex(source);
        final TraversalGraph<VDijkstra, Edge> shortestPathTree;

        if (radius < Double.POSITIVE_INFINITY) {
            dijkstra.calculate(vSource, radius);
            shortestPathTree = dijkstra.reconstructTraversalGraph(radius);
        } else {
            dijkstra.calculate(vSource);
            shortestPathTree = dijkstra.reconstructTraversalGraph();
        }

        final SimpleResultSet output = prepareResultSet();
        final Map<Integer, Geometry> edgeGeometryMap = ST_ShortestPath.getEdgeGeometryMap(connection, inputTable);
        int newID = 1;
        for (Edge e : shortestPathTree.edgeSet()) {
            final Edge baseGraphEdge = e.getBaseGraphEdge();
            final int id = baseGraphEdge.getID();
            output.addRow(edgeGeometryMap.get(Math.abs(id)),
                    id,
                    newID++,
                    shortestPathTree.getEdgeSource(e).getID(),
                    shortestPathTree.getEdgeTarget(e).getID(),
                    graph.getEdgeWeight(baseGraphEdge));
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
        output.addColumn(THE_GEOM, Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        output.addColumn(EDGE_ID, Types.INTEGER, 10, 0);
        output.addColumn(TREE_ID, Types.INTEGER, 10, 0);
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(WEIGHT, Types.DOUBLE, 10, 0);
        return output;
    }
}
