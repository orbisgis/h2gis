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
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.*;
import java.util.List;
import java.util.Set;

import static org.h2gis.h2spatial.TableFunctionUtil.isColumnListConnection;
import static org.h2gis.network.graph_creator.GraphFunctionParser.parseInputTable;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates the shortest path(s) between vertices in a JGraphT graph produced
 * from the input_edges table produced by ST_Graph.
 *
 * @author Adam Gouge
 */
public class ST_ShortestPath extends GraphFunction implements ScalarFunction {

    public static final int GEOM_INDEX = 1;
    public static final int EDGE_ID_INDEX = 2;
    public static final int PATH_ID_INDEX = 3;
    public static final int PATH_EDGE_ID_INDEX = 4;
    public static final int SOURCE_INDEX = 5;
    public static final int DESTINATION_INDEX = 6;
    public static final int WEIGHT_INDEX = 7;
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
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        // Do the calculation.
        final KeyedGraph<VDijkstra, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight,
                        VDijkstra.class, Edge.class);
        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        final VDijkstra vDestination = graph.getVertex(destination);
        final double distance = dijkstra.oneToOne(graph.getVertex(source), vDestination);

        final SimpleResultSet output = prepareResultSet();
        if (distance == Double.POSITIVE_INFINITY) {
            output.addRow(null, -1, -1, -1, source, destination, distance);
        } else {
            final PreparedStatement ps = prepareEdgeGeomStatement(connection, inputTable);
            try {
                // Need to create an object for the globalID recursion.
                new ST_ShortestPath().addPredEdges(graph, vDestination, output, ps, 1);
            } finally {
                ps.close();
            }
        }
        return output;
    }

    /**
     * Returns a PreparedStatement for selecting edge geometries by edge ids.
     *
     * @param connection Connection
     * @param inputTable Input table
     * @return PreparedStatement
     * @throws SQLException
     */
    protected static PreparedStatement prepareEdgeGeomStatement(Connection connection, String inputTable) throws SQLException {
        // Record the results.
        final TableLocation tableName = parseInputTable(connection, inputTable);
        // TODO: Warn the user if there are multiple geometry fields.
        final List<String> geometryFields = SFSUtilities.getGeometryFields(connection, tableName);
        if (geometryFields.isEmpty()) {
            throw new IllegalArgumentException(NO_GEOM_FIELD_ERROR);
        }
        // Create index on table if it doesn't already exist.
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE INDEX IF NOT EXISTS edgeIDIndex ON " + tableName
                    + "(" + EDGE_ID + ")");
        } finally {
            st.close();
        }
        return connection.prepareStatement("SELECT " +
                geometryFields.get(0) +
                " FROM " + tableName +
                " WHERE " + EDGE_ID + "=? LIMIT 1");
    }

    private void addPredEdges(KeyedGraph<VDijkstra, Edge> graph, VDijkstra dest, SimpleResultSet output,
                              PreparedStatement ps, int localID) throws SQLException {
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
                output.addRow(getEdgeGeometry(ps, e.getID()), e.getID(), globalID, localID,
                        edgeSource.getID(), edgeDestination.getID(), graph.getEdgeWeight(e));
                addPredEdges(graph, edgeSource, output, ps, localID + 1);
            } // Wrong order
            else {
                output.addRow(getEdgeGeometry(ps, e.getID()), e.getID(), globalID, localID,
                        edgeDestination.getID(), edgeSource.getID(), graph.getEdgeWeight(e));
                addPredEdges(graph, edgeDestination, output, ps, localID + 1);
            }
        }
    }

    /**
     * Gets the edge geometry corresponding to edgeID
     * @param ps     Prepared statement
     * @param edgeID Edge ID
     * @return The edge geometry corresponding to edgeID
     * @see #prepareEdgeGeomStatement(java.sql.Connection, String)
     * @throws SQLException
     */
    protected static Geometry getEdgeGeometry(PreparedStatement ps, int edgeID) throws SQLException {
        final Geometry geom;
        ps.setInt(1, Math.abs(edgeID));
        ResultSet edgesTable = ps.executeQuery();
        try {
            edgesTable.next();
            geom = (Geometry) edgesTable.getObject(1);
        } finally {
            edgesTable.close();
        }
        return geom;
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
        output.addColumn(PATH_ID, Types.INTEGER, 10, 0);
        output.addColumn(PATH_EDGE_ID, Types.INTEGER, 10, 0);
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(WEIGHT, Types.DOUBLE, 10, 0);
        return output;
    }
}
