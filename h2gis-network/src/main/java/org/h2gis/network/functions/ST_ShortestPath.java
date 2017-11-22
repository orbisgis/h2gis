/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.network.functions;

import org.locationtech.jts.geom.Geometry;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.h2.tools.SimpleResultSet;
import org.h2gis.api.ScalarFunction;
import static org.h2gis.network.functions.GraphConstants.DESTINATION;
import static org.h2gis.network.functions.GraphConstants.EDGE_ID;
import static org.h2gis.network.functions.GraphConstants.PATH_EDGE_ID;
import static org.h2gis.network.functions.GraphConstants.PATH_ID;
import static org.h2gis.network.functions.GraphConstants.SOURCE;
import static org.h2gis.network.functions.GraphConstants.THE_GEOM;
import static org.h2gis.network.functions.GraphConstants.WEIGHT;
import static org.h2gis.utilities.TableUtilities.isColumnListConnection;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

/**
 * Calculates the shortest path(s) between vertices in a JGraphT graph produced
 * from the input_edges table produced by ST_Graph.
 *
 * @author Adam Gouge
 */
public class ST_ShortestPath extends GraphFunction implements ScalarFunction {

    private int globalID = 1;

    public static final String NO_GEOM_FIELD_ERROR = "The input table must contain a geometry field.";

    public static final String REMARKS =
            "`ST_ShortestPath` calculates the shortest path(s) between vertices in a graph.\n" +
            "Possible signatures:\n" +
            "* `ST_ShortestPath('input_edges', 'o[ - eo]', s, d)`  - One-to-One\n" +
            "* `ST_ShortestPath('input_edges', 'o[ - eo]', 'w', s, d)`  - One-to-One weighted\n" +
            "\n" +
            "where\n" +
            "* `input_edges` = Edges table produced by `ST_Graph` from table `input`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected). Required\n" +
            "  if global orientation is directed or reversed.\n" +
            "* `w` = Name of column containing edge weights as doubles\n" +
            "* `s` = Source vertex id\n" +
            "* `d` = Destination vertex id\n";

    /**
     * Constructor
     */
    public ST_ShortestPath() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPath";
    }

    /**
     * @param connection  connection
     * @param inputTable  Edges table produced by ST_Graph
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
        return getShortestPath(connection, inputTable, orientation, null, source, destination);
    }

    /**
     * @param connection  connection
     * @param inputTable  Edges table produced by ST_Graph
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
        final TableLocation tableName = TableUtilities.parseInputTable(connection, inputTable);
        final String firstGeometryField =
                getFirstGeometryField(connection, tableName);
        final boolean containsGeomField = firstGeometryField != null;
        final SimpleResultSet output = prepareResultSet(containsGeomField);
        if (isColumnListConnection(connection)) {
            return output;
        }
        // Do the calculation.
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);
        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        final VDijkstra vDestination = graph.getVertex(destination);
        final double distance = dijkstra.oneToOne(graph.getVertex(source), vDestination);

        if (distance != Double.POSITIVE_INFINITY) {           
            // Need to create an object for the globalID recursion.
            final ST_ShortestPath f = new ST_ShortestPath();
            if (containsGeomField) {
                final Map<Integer, Geometry> edgeGeometryMap =
                        getEdgeGeometryMap(connection, tableName, firstGeometryField);
                f.addPredEdges(graph, vDestination, output, edgeGeometryMap, 1);
            } else {
                f.addPredEdges(graph, vDestination, output, 1);
            }
        }
        return output;
    }

    private void addPredEdges(KeyedGraph<VDijkstra, Edge> graph, VDijkstra dest, SimpleResultSet output,
                              Map<Integer, Geometry> edgeGeomMap, int localID) throws SQLException {
        // Rebuild the shortest path(s). (Yes, there could be more than
        // one if they have the same distance!)
        final Set<Edge> predEdges = dest.getPredecessorEdges();
        // The only vertex with no predecessors is the source vertex, so we can
        // start renumbering here.
        if (predEdges.isEmpty()) {
            globalID++;
        }
        // Recursively add the predecessor edges.
        for (Edge e : predEdges) {
            final VDijkstra edgeSource = graph.getEdgeSource(e);
            final VDijkstra edgeDestination = graph.getEdgeTarget(e);
            final Geometry geometry = edgeGeomMap.get(Math.abs(e.getID()));
            // Right order
            if (edgeDestination.equals(dest)) {
                output.addRow(geometry, e.getID(), globalID, localID,
                        edgeSource.getID(), edgeDestination.getID(), graph.getEdgeWeight(e));
                addPredEdges(graph, edgeSource, output, edgeGeomMap, localID + 1);
            } // Wrong order
            else {
                output.addRow(geometry, e.getID(), globalID, localID,
                        edgeDestination.getID(), edgeSource.getID(), graph.getEdgeWeight(e));
                addPredEdges(graph, edgeDestination, output, edgeGeomMap, localID + 1);
            }
        }
    }

    private void addPredEdges(KeyedGraph<VDijkstra, Edge> graph, VDijkstra dest, SimpleResultSet output,
                              int localID) throws SQLException {
        // Rebuild the shortest path(s). (Yes, there could be more than
        // one if they have the same distance!)
        final Set<Edge> predEdges = dest.getPredecessorEdges();
        // The only vertex with no predecessors is the source vertex, so we can
        // start renumbering here.
        if (predEdges.isEmpty()) {
            globalID++;
        }
        // Recursively add the predecessor edges.
        for (Edge e : predEdges) {
            final VDijkstra edgeSource = graph.getEdgeSource(e);
            final VDijkstra edgeDestination = graph.getEdgeTarget(e);
            // Right order
            if (edgeDestination.equals(dest)) {
                output.addRow(e.getID(), globalID, localID,
                        edgeSource.getID(), edgeDestination.getID(), graph.getEdgeWeight(e));
                addPredEdges(graph, edgeSource, output, localID + 1);
            } // Wrong order
            else {
                output.addRow(e.getID(), globalID, localID,
                        edgeDestination.getID(), edgeSource.getID(), graph.getEdgeWeight(e));
                addPredEdges(graph, edgeDestination, output, localID + 1);
           }
        }
    }

    /**
     * Return the first geometry field of tableName or null if it contains none.
     *
     * @param connection Connection
     * @param tableName  TableLocation
     * @return The first geometry field of tableName or null if it contains none
     * @throws SQLException
     */
    protected static String getFirstGeometryField(Connection connection, TableLocation tableName)
            throws SQLException {
        final List<String> geometryFields = SFSUtilities.getGeometryFields(connection, tableName);
        if (geometryFields.isEmpty()) {
            return null;
        }
        return geometryFields.get(0);
    }

    /**
     * Return a map of edge ids to edge geometries, or null if the input table
     * contains no geometry fields.
     *
     * @param connection Connection
     * @param tableName  TableLocation
     * @param firstGeometryField
     * @return A map of edge ids to edge geometries, or null if the input table
     * contains no geometry fields
     * @throws SQLException
     */
    protected static Map<Integer, Geometry> getEdgeGeometryMap(Connection connection,
                                                               TableLocation tableName,
                                                               String firstGeometryField)
            throws SQLException {
        if (firstGeometryField == null) {
            return null;
        }
        final Statement st = connection.createStatement();
        try {
            final ResultSet resultSet = st.executeQuery(
                    "SELECT " + EDGE_ID + ", "
                              + firstGeometryField +
                    " FROM " + tableName);
            try {
                Map<Integer, Geometry> edgeGeomMap = new HashMap<Integer, Geometry>();
                while (resultSet.next()) {
                    final int edgeID = resultSet.getInt(1);
                    final Geometry geom = (Geometry) resultSet.getObject(2);
                    edgeGeomMap.put(edgeID, geom);
                }
                return edgeGeomMap;
            } finally {
                resultSet.close();
            }
        } finally {
            st.close();
        }
    }

    /**
     * Return a new {@link org.h2.tools.SimpleResultSet} with SOURCE,
     * DESTINATION and DISTANCE columns.
     * @return a new {@link org.h2.tools.SimpleResultSet} with SOURCE,
     * DESTINATION and DISTANCE columns
     *
     * @param includeGeomColumn True if we include a Geometry column
     */
    private static SimpleResultSet prepareResultSet(boolean includeGeomColumn) {
        SimpleResultSet output = new SimpleResultSet();
        if (includeGeomColumn) {
            output.addColumn(THE_GEOM, Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        }
        output.addColumn(EDGE_ID, Types.INTEGER, 10, 0);
        output.addColumn(PATH_ID, Types.INTEGER, 10, 0);
        output.addColumn(PATH_EDGE_ID, Types.INTEGER, 10, 0);
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(WEIGHT, Types.DOUBLE, 10, 0);
        return output;
    }
}
