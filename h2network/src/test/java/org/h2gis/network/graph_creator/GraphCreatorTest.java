/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_orbisgis.org
 */
package org.h2gis.network.graph_creator;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.network.SpatialFunctionTest;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.DirectedPseudoG;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests the graph creators under all possible configurations.
 *
 * @author Adam Gouge
 */
public class GraphCreatorTest {

    private static Connection connection;
    private static final String DB_NAME = "GraphCreatorTest";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphCreatorTest.class);
    private static final int[] EDGE_ORIENTATIONS =
            new int[]{GraphCreator.DIRECTED_EDGE,
                      GraphCreator.REVERSED_EDGE,
                      GraphCreator.UNDIRECTED_EDGE};
    private static final double[] EDGE_WEIGHTS = new double[]{2.3, 4.2, 5.7};
    private static final double TOLERANCE = 0.0;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
        SpatialFunctionTest.registerCormenGraph(connection);
    }

    private void checkEdge(KeyedGraph<VDijkstra, Edge> graph, int id, int source, int dest) {
        checkEdge(graph, id, source, dest, 1.0);
    }

    private void checkEdge(KeyedGraph<VDijkstra, Edge> graph, int id, int source, int dest, double weight) {
        final Edge edge = graph.getEdge(graph.getVertex(source), graph.getVertex(dest));
        assertEquals(id, edge.getID());
        assertTrue(graph.containsEdge(edge));
        assertEquals(weight, graph.getEdgeWeight(edge), TOLERANCE);
    }

    private void checkVertices(KeyedGraph<VDijkstra, Edge> graph, int... vertices) {
        for (int i : vertices) {
            assertTrue(graph.containsVertex(graph.getVertex(i)));
        }
    }

    @Test
    public void testD() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        null,
                        GraphFunctionParser.DIRECTED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2);
        checkEdge(graph, 2, 2, 3);
        checkEdge(graph, 3, 2, 4);
        checkEdge(graph, 4, 4, 2);
        checkEdge(graph, 5, 1, 4);
        checkEdge(graph, 6, 4, 3);
        checkEdge(graph, 7, 4, 5);
        checkEdge(graph, 8, 3, 5);
        checkEdge(graph, 9, 5, 3);
        checkEdge(graph, 10, 5, 1);
    }

    @Test
    public void testWD() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "cormen_edges",
                        "weight",
                        GraphFunctionParser.DIRECTED,
                        null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2, 10.0);
        checkEdge(graph, 2, 2, 3, 1.0);
        checkEdge(graph, 3, 2, 4, 2.0);
        checkEdge(graph, 4, 4, 2, 3.0);
        checkEdge(graph, 5, 1, 4, 5.0);
        checkEdge(graph, 6, 4, 3, 9.0);
        checkEdge(graph, 7, 4, 5, 2.0);
        checkEdge(graph, 8, 3, 5, 4.0);
        checkEdge(graph, 9, 5, 3, 6.0);
        checkEdge(graph, 10, 5, 1, 7.0);
    }
}
