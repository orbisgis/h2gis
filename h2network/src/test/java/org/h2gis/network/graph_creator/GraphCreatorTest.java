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

import junit.framework.Assert;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests the graph creators under all possible configurations.
 *
 * @author Adam Gouge
 */
public class GraphCreatorTest {

    private static Connection connection;
    private static final double TOLERANCE = 0.0;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("GraphCreatorTest", true);
        registerCormenGraph(connection);
    }

    public static void registerCormenGraph(Connection connection) throws SQLException {
        final Statement st = connection.createStatement();
//                 2:1
//           >2 <----------- 4
//          / |^           ->|^
//     1:10/ / |    6:9   / / |
//        /  | |     -----  | |
//       /3:2| |4:3 /    8:4| |9:6
//      1<---------------   | |
//       \   | |  /  10:7\  | |
//     5:5\  | / /        \ | /
//         \ v| /  7:2     >v|
//          > 3 -----------> 5
//               CORMEN
        st.execute("CREATE TABLE cormen(road LINESTRING, id INT AUTO_INCREMENT PRIMARY KEY, weight DOUBLE, edge_orientation INT);" +
                "INSERT INTO cormen VALUES "
                + "('LINESTRING (0 1, 1 2)', DEFAULT, 10.0, 1),"
                + "('LINESTRING (1 2, 2 2)', DEFAULT, 1.0, -1),"
                + "('LINESTRING (1 2, 0.75 1, 1 0)', DEFAULT, 2.0,  1),"
                + "('LINESTRING (1 0, 1.25 1, 1 2)', DEFAULT, 3.0,  1),"
                + "('LINESTRING (0 1, 1 0)', DEFAULT, 5.0,  1),"
                + "('LINESTRING (1 0, 2 2)', DEFAULT, 9.0,  1),"
                + "('LINESTRING (1 0, 2 0)', DEFAULT, 2.0,  1),"
                + "('LINESTRING (2 2, 1.75 1, 2 0)', DEFAULT, 4.0,  1),"
                + "('LINESTRING (2 0, 2.25 1, 2 2)', DEFAULT, 6.0,  1),"
                + "('LINESTRING (2 0, 0 1)', DEFAULT, 7.0,  0);");

        // In order to not depend on ST_Graph, we simply simulate the output of ST_Graph
        // on the Cormen graph.
        st.execute("CREATE TABLE cormen_nodes(node_id int auto_increment primary key, the_geom point);" +
                "INSERT INTO cormen_nodes(the_geom) VALUES "
                + "('POINT (0 1)'),"
                + "('POINT (1 2)'),"
                + "('POINT (2 2)'),"
                + "('POINT (1 0)'),"
                + "('POINT (2 0)');");
//        cormen_nodes
//        NODE_ID  THE_GEOM
//        1        POINT (0 1)
//        2        POINT (1 2)
//        3        POINT (2 2)
//        4        POINT (1 0)
//        5        POINT (2 0)
        st.execute("CREATE TABLE CORMEN_EDGES(EDGE_ID INT AUTO_INCREMENT PRIMARY KEY, START_NODE INT, END_NODE INT);" +
                "INSERT INTO CORMEN_EDGES(START_NODE, END_NODE) VALUES "
                + "(1, 2),"
                + "(2, 4),"
                + "(2, 3),"
                + "(3, 2),"
                + "(1, 3),"
                + "(3, 4),"
                + "(3, 5),"
                + "(4, 5),"
                + "(5, 4),"
                + "(5, 1);");
//        CORMEN_EDGES:
//        EDGE_ID   START_NODE   END_NODE
//            1         1            2
//            2         2            4
//            3         2            3
//            4         3            2
//            5         1            3
//            6         3            4
//            7         3            5
//            8         4            5
//            9         5            4
//            10        5            1
        // Quick fix to recover other columns:
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL;" +
                "CREATE TABLE CORMEN_EDGES_ALL AS SELECT " +
                "A.*, B.* FROM CORMEN A, CORMEN_EDGES B WHERE A.ID=B.EDGE_ID;");
//        CORMEN_EDGES_ALL:
//        ROAD                           ID  EIGHT  EDGE_ORIENTATION   EDGE_ID   START_NODE   END_NODE
//        LINESTRING (0 1, 1 2)           1       0.0      1               1         1            2
//        LINESTRING (1 2, 2 2)           2       1.0     -1               2         2            4
//        LINESTRING (1 2, 0.75 1, 1 0)   3       2.0      1               3         2            3
//        LINESTRING (1 0, 1.25 1, 1 2)   4       3.0      1               4         3            2
//        LINESTRING (0 1, 1 0)           5       5.0      1               5         1            3
//        LINESTRING (1 0, 2 2)           6       9.0      1               6         3            4
//        LINESTRING (1 0, 2 0)           7       2.0      1               7         3            5
//        LINESTRING (2 2, 1.75 1, 2 0)   8       4.0      1               8         4            5
//        LINESTRING (2 0, 2.25 1, 2 2)   9       6.0      1               9         5            4
//        LINESTRING (2 0, 0 1)          10       7.0      0               10        5            1
        // We add another connected component consisting of edges w(6, 7)=1.0 and w(7, 8) = 2.0.
        // (Simulating ST_Graph).
        st.execute("DROP TABLE IF EXISTS copy_nodes");
        st.execute("CREATE TABLE copy_nodes AS SELECT * FROM cormen_nodes");
        st.execute("INSERT INTO copy_nodes VALUES " +
                "(6, 'POINT (3 1)')," +
                "(7, 'POINT (4 2)')," +
                "(8, 'POINT (5 2)');");
        st.execute("DROP TABLE IF EXISTS COPY_EDGES_ALL");
        st.execute("CREATE TABLE COPY_EDGES_ALL AS SELECT * FROM CORMEN_EDGES_ALL");
        st.execute("INSERT INTO COPY_EDGES_ALL VALUES ('LINESTRING (3 1, 4 2)', 11, 1.0, 1, 11, 6, 7)," +
                "('LINESTRING (4 2, 5 2)', 12, 2.0, 1, 12, 7, 8)");
        st.execute("ALTER TABLE COPY_EDGES_ALL ALTER COLUMN ID SET NOT NULL");
        st.execute("CREATE PRIMARY KEY ON COPY_EDGES_ALL(ID)");
        // Here we create a copy with edges 3, 4, 6, 8, 10 having a weight of
        // Infinity. Setting an edge's weight to Infinity is equivalent to
        // deleting the edge from the graph.
//                 2:1
//           >2 <----------- 4
//          /                 ^
//     1:10/                  |
//        /                   |
//       /                    |9:6
//      1                     |
//       \                    |
//     5:5\                   /
//         \       7:2       |
//          > 3 -----------> 5
//             INF_EDGES_ALL
        st.execute("DROP TABLE IF EXISTS INF_EDGES_ALL");
        st.execute("CREATE TABLE INF_EDGES_ALL AS SELECT * FROM CORMEN_EDGES_ALL");
        st.execute("UPDATE INF_EDGES_ALL SET WEIGHT=POWER(0, -1) WHERE " +
                "EDGE_ID=3 OR EDGE_ID=4 OR EDGE_ID=6 OR EDGE_ID=8 OR EDGE_ID=10;");
        st.execute("ALTER TABLE INF_EDGES_ALL ALTER COLUMN ID SET NOT NULL");
        st.execute("CREATE PRIMARY KEY ON INF_EDGES_ALL(ID)");
    }

    @Test
    public void testDO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "CORMEN_EDGES_ALL",
                        GraphFunctionParser.Orientation.DIRECTED, "edge_orientation", null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2);
        checkEdge(graph, 2, 4, 2);
        checkEdge(graph, 3, 2, 3);
        checkEdge(graph, 4, 3, 2);
        checkEdge(graph, 5, 1, 3);
        checkEdge(graph, 6, 3, 4);
        checkEdge(graph, 7, 3, 5);
        checkEdge(graph, 8, 4, 5);
        checkEdge(graph, 9, 5, 4);
        checkEdge(graph, 10, 5, 1);
        checkEdge(graph, -10, 1, 5);
    }

    @Test
    public void testWDO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "CORMEN_EDGES_ALL",
                        GraphFunctionParser.Orientation.DIRECTED, "edge_orientation", "weight",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedWeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2, 10.0);
        checkEdge(graph, 2, 4, 2, 1.0);
        checkEdge(graph, 3, 2, 3, 2.0);
        checkEdge(graph, 4, 3, 2, 3.0);
        checkEdge(graph, 5, 1, 3, 5.0);
        checkEdge(graph, 6, 3, 4, 9.0);
        checkEdge(graph, 7, 3, 5, 2.0);
        checkEdge(graph, 8, 4, 5, 4.0);
        checkEdge(graph, 9, 5, 4, 6.0);
        checkEdge(graph, 10, 5, 1, 7.0);
        checkEdge(graph, -10, 1, 5, 7.0);
    }

    @Test
    public void testRO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "CORMEN_EDGES_ALL",
                        GraphFunctionParser.Orientation.REVERSED, "edge_orientation", null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 2, 1);
        checkEdge(graph, 2, 2, 4);
        checkEdge(graph, 3, 3, 2);
        checkEdge(graph, 4, 2, 3);
        checkEdge(graph, 5, 3, 1);
        checkEdge(graph, 6, 4, 3);
        checkEdge(graph, 7, 5, 3);
        checkEdge(graph, 8, 5, 4);
        checkEdge(graph, 9, 4, 5);
        checkEdge(graph, 10, 1, 5);
        checkEdge(graph, -10, 5, 1);
    }

    @Test
    public void testWRO() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "CORMEN_EDGES_ALL",
                        GraphFunctionParser.Orientation.REVERSED, "edge_orientation", "weight",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof DirectedWeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(11, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 2, 1, 10.0);
        checkEdge(graph, 2, 2, 4, 1.0);
        checkEdge(graph, 3, 3, 2, 2.0);
        checkEdge(graph, 4, 2, 3, 3.0);
        checkEdge(graph, 5, 3, 1, 5.0);
        checkEdge(graph, 6, 4, 3, 9.0);
        checkEdge(graph, 7, 5, 3, 2.0);
        checkEdge(graph, 8, 5, 4, 4.0);
        checkEdge(graph, 9, 4, 5, 6.0);
        checkEdge(graph, 10, 1, 5, 7.0);
        checkEdge(graph, -10, 5, 1, 7.0);
    }

    @Test
    public void testU() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "CORMEN_EDGES_ALL",
                        GraphFunctionParser.Orientation.UNDIRECTED, null, null,
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof PseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2);
        checkEdge(graph, 2, 2, 4);
        final Set<Edge> edges23 = graph.getAllEdges(graph.getVertex(2), graph.getVertex(3));
        Assert.assertEquals(2, edges23.size());
        for (Edge e : edges23) {
            if (e.getID() != 4) {
                assertEquals(3, e.getID());
            }
        }
        checkEdge(graph, 5, 1, 3);
        checkEdge(graph, 6, 3, 4);
        checkEdge(graph, 7, 3, 5);
        final Set<Edge> edges45 = graph.getAllEdges(graph.getVertex(4), graph.getVertex(5));
        Assert.assertEquals(2, edges45.size());
        for (Edge e : edges45) {
            if (e.getID() != 8) {
                assertEquals(9, e.getID());
            }
        }
        checkEdge(graph, 10, 5, 1);
    }

    @Test
    public void testWU() throws SQLException {
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "CORMEN_EDGES_ALL",
                        GraphFunctionParser.Orientation.UNDIRECTED, null, "weight",
                        VDijkstra.class, Edge.class);
        final KeyedGraph<VDijkstra,Edge> graph = graphCreator.prepareGraph();
        assertTrue(graph instanceof WeightedPseudoG);
        assertEquals(5, graph.vertexSet().size());
        Assert.assertEquals(10, graph.edgeSet().size());
        checkVertices(graph, 1, 2, 3, 4, 5);
        checkEdge(graph, 1, 1, 2, 10.0);
        checkEdge(graph, 2, 2, 4, 1.0);
        final Set<Edge> edges23 = graph.getAllEdges(graph.getVertex(2), graph.getVertex(3));
        Assert.assertEquals(2, edges23.size());
        for (Edge e : edges23) {
            if (e.getID() == 4) {
                assertEquals(3.0, graph.getEdgeWeight(e), TOLERANCE);
            } else {
                assertEquals(3, e.getID());
                assertEquals(2.0, graph.getEdgeWeight(e), TOLERANCE);
            }
        }
        checkEdge(graph, 5, 1, 3, 5.0);
        checkEdge(graph, 6, 3, 4, 9.0);
        checkEdge(graph, 7, 3, 5, 2.0);
        final Set<Edge> edges45 = graph.getAllEdges(graph.getVertex(4), graph.getVertex(5));
        Assert.assertEquals(2, edges45.size());
        for (Edge e : edges45) {
            if (e.getID() == 8) {
                assertEquals(4.0, graph.getEdgeWeight(e), TOLERANCE);
            } else {
                assertEquals(9, e.getID());
                assertEquals(6.0, graph.getEdgeWeight(e), TOLERANCE);
            }
        }
        checkEdge(graph, 10, 5, 1, 7.0);
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

    @Test(expected = IllegalArgumentException.class)
    public void testNullOrientation() throws SQLException {
        testOrientation("NULL");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOrientation() throws SQLException {
        testOrientation("2");
    }

    private void testOrientation(String newOrientation) throws SQLException {
        final Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS COPY; CREATE TABLE COPY AS SELECT * FROM CORMEN_EDGES_ALL");
        st.execute("UPDATE COPY SET edge_orientation=" + newOrientation + " WHERE edge_id=1");
        GraphCreator<VDijkstra, Edge> graphCreator =
                new GraphCreator<VDijkstra, Edge>(connection,
                        "COPY",
                        GraphFunctionParser.Orientation.DIRECTED, "edge_orientation", "weight",
                        VDijkstra.class, Edge.class);
        try {
            graphCreator.prepareGraph();
        } finally {
            st.execute("DROP TABLE COPY");
            st.close();
        }
    }
}
