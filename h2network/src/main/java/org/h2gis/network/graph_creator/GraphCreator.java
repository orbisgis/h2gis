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
 * or contact directly: info_at_orbisgis.org
 */

package org.h2gis.network.graph_creator;

import org.javanetworkanalyzer.data.VId;
import org.javanetworkanalyzer.model.*;
import org.jgrapht.WeightedGraph;

import java.sql.*;

/**
 * Creates a JGraphT graph from an edges table produced by {@link
 * org.h2gis.network.graph_creator.ST_Graph}. The graph has the vertex and edge
 * classes passed to the constructor. Its global and edge orientations and
 * weights are specified by the strings passed to the constructor.
 *
 * @author Adam Gouge
 */
public class GraphCreator<V extends VId, E extends Edge> {

    private final Class<? extends V> vertexClass;
    private final Class<? extends E> edgeClass;
    private final Connection connection;

    private int startNodeIndex = -1;
    private int endNodeIndex = -1;
    private int edgeIDIndex = -1;
    private int weightColumnIndex = -1;
    private int edgeOrientationIndex = -1;

    private final String inputTable;
    private final String weightColumn;
    private final GraphFunctionParser.Orientation globalOrientation;
    private final String edgeOrientationColumnName;

    public static final int DIRECTED_EDGE = 1;
    public static final int REVERSED_EDGE = -DIRECTED_EDGE;
    public static final int UNDIRECTED_EDGE = DIRECTED_EDGE + REVERSED_EDGE;

    /**
     * Constructor.
     *
     * @param connection                Connection
     * @param inputTable                Name of edges table from {@link org.h2gis.network.graph_creator.ST_Graph}
     * @param globalOrientation         Global orientation
     * @param edgeOrientationColumnName Edge orientation
     * @param weightColumn              Weight column name
     * @param vertexClass               Vertex class
     * @param edgeClass                 Edge class
     * @throws SQLException If the input table is not found
     */
    public GraphCreator(Connection connection,
                        String inputTable,
                        GraphFunctionParser.Orientation globalOrientation,
                        String edgeOrientationColumnName,
                        String weightColumn,
                        Class<? extends V> vertexClass,
                        Class<? extends E> edgeClass) throws SQLException {
        this.connection = connection;
        this.inputTable = inputTable;
        this.weightColumn = weightColumn;
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
        final Statement st = connection.createStatement();
        final ResultSet edges = st.executeQuery("SELECT * FROM " + inputTable);
        try {
            // Initialize the indices.
            initIndices(edges);
            // Initialize the graph.
            KeyedGraph<V, E> graph;
            if (!globalOrientation.equals(GraphFunctionParser.Orientation.UNDIRECTED)) {
                if (weightColumn != null) {
                    graph = new DirectedWeightedPseudoG<V, E>(vertexClass, edgeClass);
                } else {
                    graph = new DirectedPseudoG<V, E>(vertexClass, edgeClass);
                }
            } else {
                if (weightColumn != null) {
                    graph = new WeightedPseudoG<V, E>(vertexClass, edgeClass);
                } else {
                    graph = new PseudoG<V, E>(vertexClass, edgeClass);
                }
            }
            // Add the edges.
            while (edges.next()) {
                loadEdge(graph, edges);
            }
            return graph;
        } finally {
            edges.close();
            st.close();
        }
    }

    /**
     * Recovers the indices from the metadata.
     */
    private void initIndices(ResultSet edges) throws SQLException {
        ResultSetMetaData metaData = edges.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            final String columnName = metaData.getColumnName(i);
            if (columnName.equalsIgnoreCase(ST_Graph.START_NODE)) startNodeIndex = i;
            if (columnName.equalsIgnoreCase(ST_Graph.END_NODE)) endNodeIndex = i;
            if (columnName.equalsIgnoreCase(ST_Graph.EDGE_ID)) edgeIDIndex = i;
            if (columnName.equalsIgnoreCase(edgeOrientationColumnName)) edgeOrientationIndex = i;
            if (columnName.equalsIgnoreCase(weightColumn)) weightColumnIndex = i;
        }
        verifyIndex(startNodeIndex, ST_Graph.START_NODE);
        verifyIndex(endNodeIndex, ST_Graph.START_NODE);
        verifyIndex(edgeIDIndex, ST_Graph.START_NODE);
        if (!globalOrientation.equals(GraphFunctionParser.Orientation.UNDIRECTED)) {
            verifyIndex(edgeOrientationIndex, edgeOrientationColumnName);
        }
        if (weightColumn != null) {
            verifyIndex(weightColumnIndex, weightColumn);
        }
    }

    /**
     * Verifies that the given index is not equal to -1; if it is, then throws
     * an exception saying that the given field is missing.
     *
     * @param index        The index.
     * @param missingField The field.
     */
    private static void verifyIndex(int index, String missingField) {
        if (index == -1) {
            throw new IndexOutOfBoundsException("Column \"" + missingField + "\" not found.");
        }
    }

    /**
     * Loads an edge into the graph from the current row.
     *
     * @param graph The graph to which the edges will be added.
     *
     * @return The newly loaded edge.
     */
    private E loadEdge(KeyedGraph<V, E> graph, ResultSet edges) throws SQLException {
        final int startNode = edges.getInt(startNodeIndex);
        final int endNode = edges.getInt(endNodeIndex);
        final int edgeID = edges.getInt(edgeIDIndex);
        double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;
        if (weightColumnIndex != -1) {
            weight = edges.getDouble(weightColumnIndex);
        }
        E edge;
        // Undirected graphs are either pseudographs or weighted pseudographs,
        // so there is no need to add edges in both directions.
        if (globalOrientation.equals(GraphFunctionParser.Orientation.UNDIRECTED)) {
            edge = graph.addEdge(endNode, startNode, edgeID);
        } else {
            // Directed graphs are either directed pseudographs or directed
            // weighted pseudographs and must specify an orientation for each
            // individual edge. If no orientations are specified, every edge
            // is considered to be directed with orientation given by the
            // geometry.
            int edgeOrientation = (edgeOrientationIndex == -1)
                    ? DIRECTED_EDGE
                    : edges.getInt(edgeOrientationIndex);
            if (edges.wasNull()) {
                throw new IllegalArgumentException("Invalid edge orientation: NULL.");
            }
            if (edgeOrientation == UNDIRECTED_EDGE) {
                if (globalOrientation.equals(GraphFunctionParser.Orientation.DIRECTED)) {
                    edge = loadDoubleEdge(graph, startNode, endNode, edgeID, weight);
                } // globalOrientation == Orientation.REVERSED
                else {
                    edge = loadDoubleEdge(graph, endNode, startNode, edgeID, weight);
                }
            } else if (edgeOrientation == DIRECTED_EDGE) {
                // Reverse a directed edge (global).
                if (globalOrientation.equals(GraphFunctionParser.Orientation.REVERSED)) {
                    edge = graph.addEdge(endNode, startNode, edgeID);
                } // No reversal.
                else {
                    edge = graph.addEdge(startNode, endNode, edgeID);
                }
            } else if (edgeOrientation == REVERSED_EDGE) {
                // Reversing twice is the same as no reversal.
                if (globalOrientation.equals(GraphFunctionParser.Orientation.REVERSED)) {
                    edge = graph.addEdge(startNode, endNode, edgeID);
                } // Otherwise reverse just once (local).
                else {
                    edge = graph.addEdge(endNode, startNode, edgeID);
                }
            } else {
                throw new IllegalArgumentException("Invalid edge orientation: " + edgeOrientation);
            }
        }
        setEdgeWeight(edge, weight);
        return edge;
    }

    /**
     * In directed graphs, undirected edges are represented by directed edges
     * in both directions. The edges are assigned ids with opposite signs.
     *
     * @param graph     The graph to which the edges will be added.
     * @param startNode Start node id
     * @param endNode   End Node id
     * @param edgeID    Edge id
     *
     * @return One of the two directed edges used to represent an undirected
     * edge in a directed graph (the one with a negative id).
     */
    private E loadDoubleEdge(KeyedGraph<V, E> graph,
                             final int startNode,
                             final int endNode,
                             final int edgeID,
                             final double weight) throws SQLException {

        // Note: row is ignored since we only need it for weighted graphs.
        final E edgeTo = graph.addEdge(startNode, endNode, edgeID);
        setEdgeWeight(edgeTo, weight);
        final E edgeFrom = graph.addEdge(endNode, startNode, -edgeID);
        setEdgeWeight(edgeFrom, weight);
        return edgeFrom;
    }

    /**
     * Set this edge's weight to the weight contained in the current row.
     *
     * @param edge Edge
     * @throws SQLException If the weight cannot be retrieved
     */
    private void setEdgeWeight(E edge, final double weight) throws SQLException {
        if (edge != null && weightColumnIndex != -1) {
            edge.setWeight(weight);
        }
    }
}
