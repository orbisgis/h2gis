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
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.*;
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
    public static final int GEOM_INDEX = 1;
    public static final String EDGE_ID = "EDGE_ID";
    public static final int EDGE_ID_INDEX = 2;
    public static final String PATH_ID = "PATH_ID";
    public static final int PATH_ID_INDEX = 3;
    public static final String PATH_EDGE_ID = "PATH_EDGE_ID";
    public static final int PATH_EDGE_ID_INDEX = 4;
    public static final String SOURCE = "SOURCE";
    public static final int SOURCE_INDEX = 5;
    public static final String DESTINATION = "DESTINATION";
    public static final int DESTINATION_INDEX = 6;
    public static final String WEIGHT = "WEIGHT";
    public static final int WEIGHT_INDEX = 7;
    private int globalID = 1;

    private static Connection connection;
    private TableLocation tableName;

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
        this(null, null);
    }

    public ST_ShortestPath(Connection connection,
                           String inputTable) {
        if (connection != null) {
            this.connection = SFSUtilities.wrapConnection(connection);
        }
        if (inputTable != null) {
            this.tableName = TableLocation.parse(inputTable);
        }
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
        return getShortestPath(connection, inputTable, orientation, null, source, destination);
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
        // If we only want the column names, there is no need to do the calculation.
        // This is a hack. See: https://groups.google.com/forum/#!topic/h2-database/NHH0rDeU258
        if (connection.getMetaData().getURL().equals("jdbc:columnlist:connection")) {
            return prepareResultSet();
        }
        // Do the calculation.
        final KeyedGraph<VDijkstra, Edge> graph = prepareGraph(connection, inputTable, orientation, weight);
        final Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        final VDijkstra vDestination = graph.getVertex(destination);
        dijkstra.oneToOne(graph.getVertex(source), vDestination);

        // Create index on table if it doesn't already exist.
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE INDEX IF NOT EXISTS edgeIDIndex ON " + TableLocation.parse(inputTable)
                    + "(" + ST_Graph.EDGE_ID + ")");
        } finally {
            st.close();
        }

        // Record the results.
        ST_ShortestPath f = new ST_ShortestPath(connection, inputTable);
        final SimpleResultSet output = prepareResultSet();

        final PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + f.tableName + " WHERE " + ST_Graph.EDGE_ID + "=?");

        try {
            f.addPredEdges(graph, vDestination, output, ps, 1);
        } finally {
            ps.close();
        }
        return output;
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

    private Geometry getEdgeGeometry(PreparedStatement ps, int edgeID) throws SQLException {
        final Geometry geom;
        ps.setInt(1, Math.abs(edgeID));
        ResultSet edgesTable = ps.executeQuery();
        try {
            edgesTable.next();
            // TODO: Recover the spatial field index properly.
            geom = (Geometry) edgesTable.getObject(1);
            // Should contain a unique result.
            assert !edgesTable.next();
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
        output.addColumn(GEOM, Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        output.addColumn(EDGE_ID, Types.INTEGER, 10, 0);
        output.addColumn(PATH_ID, Types.INTEGER, 10, 0);
        output.addColumn(PATH_EDGE_ID, Types.INTEGER, 10, 0);
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
