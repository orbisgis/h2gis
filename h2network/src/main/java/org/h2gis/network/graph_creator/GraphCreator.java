package org.h2gis.network.graph_creator;

import org.javanetworkanalyzer.data.VId;
import org.javanetworkanalyzer.model.*;

import java.sql.*;

/**
 * Created by adam on 3/4/14.
 */
public class GraphCreator<V extends VId, E extends Edge> {

    private final Connection connection;
    private final Class<? extends V> vertexClass;
    private final Class<? extends E> edgeClass;

    private int startNodeIndex = -1;
    private int endNodeIndex = -1;
    private int edgeIDIndex = -1;
    private int weightColumnIndex = -1;
    private int edgeOrientationIndex = -1;

    private final String inputTable;
    private final Orientation globalOrientation;
    private final String edgeOrientationColumnName;

    public static final int DIRECTED_EDGE = 1;
    public static final int REVERSED_EDGE = -DIRECTED_EDGE;
    public static final int UNDIRECTED_EDGE = DIRECTED_EDGE + REVERSED_EDGE;

    public enum Orientation {
        DIRECTED, REVERSED, UNDIRECTED
    }

    /**
     * Constructs a new {@link GraphCreator}.
     */
    public GraphCreator(Connection connection,
                        String inputTable,
                        Orientation globalOrientation,
                        String edgeOrientationColumnName,
                        Class<? extends V> vertexClass,
                        Class<? extends E> edgeClass) {
        this.connection = connection;
        this.inputTable = inputTable;
        this.globalOrientation = globalOrientation;
        this.edgeOrientationColumnName = edgeOrientationColumnName;
        this.vertexClass = vertexClass;
        this.edgeClass = edgeClass;
    }

    /**
     * Prepares a graph.
     *
     * @return The newly prepared graph.
     *
     * @throws java.sql.SQLException
     */
    protected KeyedGraph<V, E> prepareGraph() throws SQLException {

        // Initialize the indices.
        initIndices();
        // Initialize the graph.
        KeyedGraph<V, E> graph =
                (globalOrientation != Orientation.UNDIRECTED)
                        ? new DirectedPseudoG<V, E>(vertexClass, edgeClass)
                        : new PseudoG<V, E>(vertexClass, edgeClass);
        // Add the edges.
        final ResultSet edges = connection.createStatement().executeQuery("SELECT * FROM " + inputTable);
        while (edges.next()) {
            Edge edge = loadEdge(edges, graph);
            if (weightColumnIndex != -1) {
                edge.setWeight(edges.getDouble(weightColumnIndex));
            }
        }
        return graph;
    }

    private void initIndices() throws SQLException {
        final Statement st = connection.createStatement();
        final ResultSet edgesTable = st.executeQuery("SELECT * FROM " + inputTable);
        try {
            ResultSetMetaData metaData = edgesTable.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                final String columnName = metaData.getColumnName(i);
                if (columnName.equalsIgnoreCase(ST_Graph.START_NODE)) startNodeIndex = i;
                if (columnName.equalsIgnoreCase(ST_Graph.END_NODE)) endNodeIndex = i;
                if (columnName.equalsIgnoreCase(ST_Graph.EDGE_ID)) edgeIDIndex = i;
                if (columnName.equalsIgnoreCase(edgeOrientationColumnName)) edgeOrientationIndex = i;
//                if (columnName.equalsIgnoreCase(weightColumn)) weightColumnIndex = i;
            }
            verifyIndex(startNodeIndex, ST_Graph.START_NODE);
            verifyIndex(endNodeIndex, ST_Graph.START_NODE);
            verifyIndex(edgeIDIndex, ST_Graph.START_NODE);
        } finally {
            edgesTable.close();
        }
    }

    private static void verifyIndex(int index, String missingField) {
        if (index == -1) {
            throw new IndexOutOfBoundsException("Column " + missingField + " not found.");
        }
    }

    protected E loadEdge(ResultSet edges, KeyedGraph<V, E> graph) throws SQLException {
        final int startNode = edges.getInt(startNodeIndex);
        final int endNode = edges.getInt(endNodeIndex);
        final int edgeID = edges.getInt(edgeIDIndex);
        // Undirected graphs are either pseudographs or weighted pseudographs,
        // so there is no need to add edges in both directions.
        if (globalOrientation == Orientation.UNDIRECTED) {
            return graph.addEdge(endNode, startNode, edgeID);
        } else {
            // Directed graphs are either directed pseudographs or directed
            // weighted pseudographs and must specify an orientation for each
            // individual edge. If no orientations are specified, every edge
            // is considered to be directed with orientation given by the
            // geometry.
            int edgeOrientation = (edgeOrientationIndex == -1)
                    ? DIRECTED_EDGE
                    : edges.getInt(edgeOrientationIndex);
            if (edgeOrientation == UNDIRECTED_EDGE) {
                // Note: row is ignored since we only need it for weighted graphs.
                return loadDoubleEdge(graph, startNode, endNode, edgeID);
            } else if (edgeOrientation == DIRECTED_EDGE) {
                // Reverse a directed edge (global).
                if (globalOrientation == Orientation.REVERSED) {
                    return graph.addEdge(endNode, startNode, edgeID);
                } // No reversal.
                else {
                    return graph.addEdge(startNode, endNode, edgeID);
                }
            } else if (edgeOrientation == REVERSED_EDGE) {
                // Reversing twice is the same as no reversal.
                if (globalOrientation == Orientation.REVERSED) {
                    return graph.addEdge(startNode, endNode, edgeID);
                } // Otherwise reverse just once (local).
                else {
                    return graph.addEdge(endNode, startNode, edgeID);
                }
            } else {
//                LOGGER.warn("Edge ({},{}) ignored since {} is not a valid "
//                        + "edge orientation.", startNode, endNode, edgeOrientation);
                return null;
            }
        }
    }

    /**
     * In directed graphs, undirected edges are represented by directed edges
     * in both directions. The edges are assigned ids with opposite signs.
     *
     * @param graph     The graph to which the edges will be added.
     * @param startNode Start node
     * @param endNode   End Node
     * @param edgeID    Edge id
     *
     * @return One of the two directed edges used to represent an undirected
     * edge in a directed graph (the one with a negative id).
     */
    protected E loadDoubleEdge(KeyedGraph<V, E> graph,
                               final int startNode,
                               final int endNode,
                               final int edgeID) {

        // Note: row is ignored since we only need it for weighted graphs.
        final E edgeTo = graph.addEdge(startNode, endNode, edgeID);
        final E edgeFrom = graph.addEdge(endNode, startNode, -edgeID);
        return edgeFrom;
    }
}
