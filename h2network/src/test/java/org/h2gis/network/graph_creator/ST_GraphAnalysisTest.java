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

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GraphConstants;
import org.junit.*;

import java.sql.*;

import static org.h2gis.network.graph_creator.ST_GraphAnalysis.BATCH_SIZE;
import static org.h2gis.utilities.GraphConstants.*;
import static org.junit.Assert.*;

/**
 * @author Adam Gouge
 */
public class ST_GraphAnalysisTest {

    public static final double[] DO_RO_NODE_BETWEENNESS =
            new double[]{1./3, 1./6, 1., 0., 1.};
    public static final double[] WDO_WRO_NODE_BETWEENNESS =
            new double[]{0., 1./3, 5./6, 1./3, 1.};
    public static final double[] DO_RO_EDGE_BETWEENNESS =
            new double[]{1./9, 1./3, 8./9, 0., 1./3, 1./3, 2./3, 2./9, 2./9, 1., 1./9};
    public static final double[] WDO_WRO_EDGE_BETWEENNESS =
            new double[]{0., 4./7, 6./7, 2./7, 3./7, 0., 1., 2./7, 6./7, 4./7, 1./7};
    private static Connection connection;
    private Statement st;
    private static final double TOLERANCE = 10E-16;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String W = "'weight'";
    public static final String LINE_GRAPH_TABLE = "LINE_GRAPH_EDGES";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_GraphAnalysisTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_GraphAnalysis(), "");
        GraphCreatorTest.registerCormenGraph(connection);
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void DO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'directed - edge_orientation')
        checkBoolean(compute(DO));

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|000 00|10 000|0 0001| 11121
        // |0000 -|--- 10|11 000|0 1000| 11111
        // |0000 0|000 --|-- 000|0 1000| 11111
        // |0000 0|100 00|00 ---|- 1000| 11111
        // |1100 0|000 00|00 010|0 ----| 12111
        //
        // 1: 1+1/2 = 3/2
        // 2:         1
        // 3: 1/2+3 = 7/2
        // 4:         1/2
        // 5: 1/2+3 = 7/2
        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX),
                new double[]{
                        4.0 / (0.0 + 1.0 + 1.0 + 2.0 + 1.0),
                        4.0 / (3.0 + 0.0 + 1.0 + 2.0 + 2.0),
                        4.0 / (2.0 + 1.0 + 0.0 + 1.0 + 1.0),
                        4.0 / (2.0 + 1.0 + 2.0 + 0.0 + 1.0),
                        4.0 / (1.0 + 2.0 + 2.0 + 1.0 + 0.0)},
                DO_RO_NODE_BETWEENNESS
        );
        //   1: 1+1/2 = 3/2
        //   2: 2+1/2 = 5/2
        //   3:         5
        //   4:         1
        //   5: 2+1/2 = 5/2
        //   6: 2+1/2 = 5/2
        //   7:         4
        //   8:         2
        //   9: 1+2/2 = 2
        //  10: 5+1/2 = 11/2
        // -10: 1+1/2 = 3/2
        checkEdges(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX),
                DO_RO_EDGE_BETWEENNESS);
    }

    @Test
    public void WDO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'directed - edge_orientation', 'weight')
        checkBoolean(compute(DO, W));

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|000 01|11 000|0 0002| 11122
        // |0000 -|--- 10|11 000|0 1001| 11111
        // |0000 0|000 --|-- 000|0 1001| 11111
        // |0000 0|100 00|00 ---|- 1000| 11111
        // |0000 0|100 00|00 011|0 ----| 11111
        //
        // 1:            0
        // 2:            2
        // 3: 4+2(1/2) = 5
        // 4:            2
        // 5: 5+2/2 =    6
        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX),
                new double[]{
                        4.0 / (0.0 + 8.0 + 5.0 + 13.0 + 7.0),
                        4.0 / (11.0 + 0.0 + 2.0 + 10.0 + 4.0),
                        4.0 / (9.0 + 3.0 + 0.0 + 8.0 + 2.0),
                        4.0 / (11.0 + 1.0 + 3.0 + 0.0 + 4.0),
                        4.0 / (7.0 + 7.0 + 9.0 + 6.0 + 0.0)},
                WDO_WRO_NODE_BETWEENNESS
        );
        //   1:         0
        //   2:         4
        //   3:         6
        //   4:         2
        //   5: 2+2/2 = 3
        //   6:         0
        //   7: 6+2/2 = 7
        //   8:         2
        //   9: 5+2/2 = 6
        //  10:         4
        // -10: 2/2   = 1
        checkEdges(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX),
                WDO_WRO_EDGE_BETWEENNESS);
    }

    @Test
    public void RO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'reversed - edge_orientation')
        checkBoolean(compute(RO));

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|000 01|00 000|0 0111| 11111
        // |0001 -|--- 00|00 000|1 0000| 11112
        // |0001 0|010 --|-- 000|0 0000| 11111
        // |0000 0|000 11|00 ---|- 1000| 21111
        // |0000 0|000 01|00 000|0 ----| 11111
        //
        // 1: 1/2+1 = 3/2
        // 2:         1
        // 3: 1/2+3 = 7/2
        // 4:         1/2
        // 5: 3+1/2 = 7/2
        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX),
                new double[]{
                        4.0 / (0.0 + 3.0 + 2.0 + 2.0 + 1.0),
                        4.0 / (1.0 + 0.0 + 1.0 + 1.0 + 2.0),
                        4.0 / (1.0 + 1.0 + 0.0 + 2.0 + 2.0),
                        4.0 / (2.0 + 2.0 + 1.0 + 0.0 + 1.0),
                        4.0 / (1.0 + 2.0 + 1.0 + 1.0 + 0.0)},
                DO_RO_NODE_BETWEENNESS
        );
        //   1: 1+1/2 = 3/2
        //   2: 2+1/2 = 5/2
        //   3:       = 5
        //   4:       = 1
        //   5: 2+1/2 = 5/2
        //   6: 2+1/2 = 5/2
        //   7:       = 4
        //   8:       = 2
        //   9: 1+2/2 = 2
        //  10: 5+1/2 = 11/2
        // -10: 1+1/2 = 3/2
        checkEdges(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX),
                DO_RO_EDGE_BETWEENNESS);
    }

    @Test
    public void WRO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'reversed - edge_orientation', 'weight')
        checkBoolean(compute(RO, W));

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|000 01|00 000|0 0111| 11111
        // |0000 -|--- 10|00 000|1 0000| 11111
        // |0000 0|011 --|-- 000|1 0000| 11111
        // |0000 0|000 11|00 ---|- 2110| 21111
        // |0000 0|000 11|00 000|0 ----| 21111
        //
        // 1:         0
        // 2:         2
        // 3: 4+2/2 = 5
        // 4:         2
        // 5: 5+2/2 = 6
        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX),
                new double[]{
                        4.0 / (0.0 + 11.0 + 9.0 + 11.0 + 7.0),
                        4.0 / (8.0 + 0.0 + 3.0 + 1.0 + 7.0),
                        4.0 / (5.0 + 2.0 + 0.0 + 3.0 + 9.0),
                        4.0 / (13.0 + 10.0 + 8.0 + 0.0 + 6.0),
                        4.0 / (7.0 + 4.0 + 2.0 + 4.0 + 0.0)},
                WDO_WRO_NODE_BETWEENNESS
        );
        //   1:         0
        //   2:         4
        //   3:         6
        //   4:         2
        //   5: 2+2/2 = 3
        //   6:         0
        //   7: 6+2/2 = 7
        //   8:         2
        //   9: 5+2/2 = 6
        //  10:         4
        // -10: 2/2   = 1
        checkEdges(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX),
                WDO_WRO_EDGE_BETWEENNESS);
    }

    @Test
    public void U() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'undirected')
        checkBoolean(compute(U));

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|010 00|10 000|0 0002| 11141
        // |0001 -|--- 00|02 000|2 0000| 11215
        // |0000 0|000 --|-- 000|0 0000| 12111
        // |0000 1|000 10|00 ---|- 2000| 41112
        // |1000 0|000 02|00 020|0 ----| 15121
        //
        // We don't put the calculation here because of Brande's assumption
        // about unique edges from v to w in Theorem 6.
        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX),
                new double[]{
                        4.0 / (0.0 + 1.0 + 1.0 + 2.0 + 1.0),
                        4.0 / (1.0 + 0.0 + 1.0 + 1.0 + 2.0),
                        4.0 / (1.0 + 1.0 + 0.0 + 1.0 + 1.0),
                        4.0 / (2.0 + 1.0 + 1.0 + 0.0 + 1.0),
                        4.0 / (1.0 + 2.0 + 1.0 + 1.0 + 0.0)},
                new double[]{0., 1./7, 1., 2./7, 1./2}
        );
        //   1: 2+2/4+2/5   = 29/10
        //   2: 2+2/4+4/5   = 33/10
        //   3: 2/2+2/5     = 7/5
        //   4: 2/2+2/5     = 7/5
        //   5: 2+2/4       = 5/2
        //   6: 2+2/4       = 5/2
        //   7: 2+4/5       = 14/5
        //   8: 2/2+2/4+2/5 = 19/10
        //   9: 2/2+2/4+2/5 = 19/10
        //  10: 2+4/4+2/5   = 17/5
        // Note: Edge -10 does not exist in this unweighted undirected graph.
        checkEdges(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX),
                new double[]{3./4, 19./20, 0., 0., 11./20, 11./20, 7./10, 1./4, 1./4, 1.0});
    }

    @Test
    public void WU() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'undirected', 'weight')
        checkBoolean(compute(U, W));

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|010 01|11 000|0 0000| 11112
        // |0000 -|--- 10|01 000|0 0000| 11111
        // |0000 0|010 --|-- 000|0 0000| 11111
        // |0000 1|100 10|00 ---|- 0000| 11111
        // |0000 0|000 11|00 000|0 ----| 21111
        //
        // 1: 0
        // 2: 4
        // 3: 7
        // 4: 0
        // 5: 0
        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX),
                new double[]{
                        4.0 / (0.0 + 7.0 + 5.0 + 8.0 + 7.0),
                        4.0 / (7.0 + 0.0 + 2.0 + 1.0 + 4.0),
                        4.0 / (5.0 + 2.0 + 0.0 + 3.0 + 2.0),
                        4.0 / (8.0 + 1.0 + 3.0 + 0.0 + 4.0),
                        4.0 / (7.0 + 4.0 + 2.0 + 4.0 + 0.0)},
                new double[]{0., 4./7, 1., 0., 0.}
        );
        //   1:         0
        //   2:         6
        //   3:         10
        //   4:         0
        //   5: 6+2/2 = 7
        //   6:         0
        //   7: 4+2/2 = 5
        //   8:         2
        //   9:         0
        //  10: 2/2   = 1
        // -10:         0
        checkEdges(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX),
                new double[]{0., 3./5, 1., 0., 7./10, 0., 1./2, 1./5, 0., 1./10, 0.});
    }

    @Test
    public void testDisconnectedGraph() throws Exception {
        st.execute("DROP TABLE IF EXISTS COPY_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS COPY_EDGES_ALL" + EDGE_CENT_SUFFIX);

        checkBoolean(st.executeQuery(
                "SELECT ST_GraphAnalysis('COPY_EDGES_ALL', " +
                        "'directed - edge_orientation', 'weight');"));
        // σ
        // 11122000
        // 11111000
        // 11111000
        // 11111000
        // 11111000
        // 00000111
        // 00000011
        // 00000001
        //
        // Nodes
        // | 1         | 2       | 3         | 4                  | 5              | 6 | 7     | 8       |
        // |-----------|---------|-----------|--------------------|----------------|---|-------|---------|
        // | *         | (1,3,2) | (1,3)     | (1,3,5,4), (1,5,4) | (1,5), (1,3,5) | - | -     | -       |
        // | (2,3,5,1) | *       | (2,3)     | (2,3,5,4)          | (2,3,5)        | - | -     | -       |
        // | (3,5,1)   | (3,2)   | *         | (3,5,4)            | (3,5)          | - | -     | -       |
        // | (4,5,1)   | (4,2)   | (4,2,3)   | *                  | (4,5)          | - | -     | -       |
        // | (5,1)     | (5,4,2) | (5,4,2,3) | (5,4)              | *              | - | -     | -       |
        // | -         | -       | -         | -                  | -              | * | (6,7) | (6,7,8) |
        // | -         | -       | -         | -                  | -              | - | *     | (7,8)   |
        // | -         | -       | -         | -                  | -              | - | -     | *       |
        //
        // 6: 0
        // 7: 1
        // 8: 0
        checkNodes(st.executeQuery("SELECT * FROM COPY_EDGES_ALL" + NODE_CENT_SUFFIX),
                // All closeness values are zero since the graph is disconnected!
                // For every node i, there exists a node j which is unreachable from i.
                // That is, d(i,j)=Infinity, so C_C(i)=0. I.e., C_C(v)=0 for all v in V.
                new double[]{0., 0., 0., 0., 0., 0., 0., 0.},
                // The betweenness values for nodes 1 to 5 remain the same as
                // in the original WDO case.
                // Of nodes 6 to 8, only node 7 is between other nodes on
                // shortest paths. Notice its normalized betweenness value is
                // reasonable relative to the other betweenness values.
                new double[]{0., 1./3, 5./6, 1./3, 1., 0., 1./6, 0.}
        );
        // Edges
        //  | 1        | 2     | 3       | 4                | 5            | 6 | 7    | 8        |
        //  |----------|-------|---------|------------------|--------------|---|------|----------|
        //  | *        | (5,4) | (5)     | (5,7,9), (-10,9) | (5,7), (-10) | - | -    | -        |
        //  | (3,7,10) | *     | (3)     | (3,7,9)          | (3,7)        | - | -    | -        |
        //  | (7,10)   | (4)   | *       | (7,9)            | (7)          | - | -    | -        |
        //  | (8,10)   | (2)   | (2,3)   | *                | (8)          | - | -    | -        |
        //  | (10)     | (9,2) | (9,2,3) | (9)              | *            | - | -    | -        |
        //  | -        | -     | -       | -                | -            | * | (11) | (11, 12) |
        //  | -        | -     | -       | -                | -            | - | *    | (12)     |
        //  | -        | -     | -       | -                | -            | - | -    | *        |
        //
        // 11: 2
        // 12: 2
        checkEdges(st.executeQuery("SELECT * FROM COPY_EDGES_ALL" + EDGE_CENT_SUFFIX),
                // The betweenness values for edges 1 to 10 and -10 remain the
                // same as in the original WDO case.
                // Of edges 11 and 12 are on two shortest paths each.
                // Notice their normalized betweenness values are reasonable
                // relative to the other betweenness values.
                new double[]{0., 4./7, 6./7, 2./7, 3./7, 0., 1., 2./7, 6./7, 4./7, 1./7, 2./7, 2./7});
    }

    @Test
    public void testLineGraphOdd() throws Exception {
        testBatchComputation(5 * BATCH_SIZE + 1);
    }

    @Test
    public void testLineGraphEven() throws Exception {
        testBatchComputation(5 * BATCH_SIZE);
    }

    private void testBatchComputation(final int n) throws SQLException {
        // Here we test the closeness and betweenness centrality computations
        // on a line graph.
        final String tableName = createLineGraphTable(connection, n);
        // The formulas for unnormalized centrality are simple to work out:
        //
        // C_C(v) = 2(k(k-1)+(n-k)(n-k+1)
        // C_B(v) = 2(k-1)(n-k)
        // C_B(e) = 2k(n-k)
        //
        // To normalize betweenness, just find the extreme values of these
        // functions and consider whether n is even or odd. Normalizing
        // closeness amounts to multiplying by (n-1).
        checkBoolean(st.executeQuery("SELECT ST_GraphAnalysis('" + tableName + "', 'undirected');"));
            ResultSet nodeCent = st.executeQuery("SELECT * FROM " + tableName + "_NODE_CENT");
            try {
                // The minimum betweenness value is zero.
                final double max = (n % 2 == 0) ? n * (n - 2.) / 2 : (n - 1.) * (n - 1) / 2;
                while (nodeCent.next()) {
                    final int k = nodeCent.getInt(GraphConstants.NODE_ID);
                    assertEquals(2. * (n - 1) / (k * (k - 1) + (n - k) * (n - k + 1)),
                            nodeCent.getDouble(GraphConstants.CLOSENESS), TOLERANCE);
                    assertEquals(2. * (k - 1) * (n - k) / max,
                            nodeCent.getDouble(GraphConstants.BETWEENNESS), TOLERANCE);
                }
            } finally {
                nodeCent.close();
            }
            ResultSet edgeCent = st.executeQuery("SELECT * FROM " + tableName + "_EDGE_CENT");
            try {
                final double min = 2. * (n - 1);
                final double max = (n % 2 == 0) ? n * n / 2 : (n - 1.) * (n + 1) / 2;
                while (edgeCent.next()) {
                    final int k = edgeCent.getInt(GraphConstants.EDGE_ID);
                    assertEquals((2. * k * (n - k) - min) / (max - min),
                            edgeCent.getDouble(GraphConstants.BETWEENNESS), TOLERANCE);
                }
            } finally {
                edgeCent.close();
            }
    }

    protected static String createLineGraphTable(Connection connection, int n) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            // Here we create an an undirected unweighted graph, with nodes n and
            // edges (e) numbered as follows:
            //
            //        (1)             (2)                                 (n-1)
            // 1 <-----------> 2 <-----------> 3 <---- ... ----> n-1 <-------------> n
            //
            final String tableName = LINE_GRAPH_TABLE + n;
            st.execute("DROP TABLE IF EXISTS " + tableName + "");
            st.execute("DROP TABLE IF EXISTS " + tableName + "" + NODE_CENT_SUFFIX);
            st.execute("DROP TABLE IF EXISTS " + tableName + "" + EDGE_CENT_SUFFIX);
            st.execute("CREATE TEMPORARY TABLE " + tableName + "(" +
                    EDGE_ID + " INT, " +
                    START_NODE + " INT, " +
                    END_NODE + " INT)");
            final PreparedStatement ps =
                    connection.prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?);");
            try {
                for (int i = 1; i < n; i++) {
                    ps.setInt(1, i);
                    ps.setInt(2, i);
                    ps.setInt(3, i + 1);
                    ps.addBatch();
                }
                ps.executeBatch();
            } finally {
                ps.close();
            }
            return tableName;
        } finally {
            st.close();
        }
    }

    private ResultSet compute(String orientation, String weight) throws SQLException {
        return st.executeQuery(
                "SELECT ST_GraphAnalysis('CORMEN_EDGES_ALL', "
                        + orientation + ((weight != null) ? ", " + weight : "") + ")");
    }

    private ResultSet compute(String orientation) throws SQLException {
        return compute(orientation, null);
    }

    private void checkBoolean(ResultSet rs) throws SQLException {
        try{
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
    }

    private void checkNodes(ResultSet nodeCent,
                            double[] closeness,
                            double[] betweenness) throws SQLException {
        try {
            while (nodeCent.next()) {
                final int nodeID = nodeCent.getInt(GraphConstants.NODE_ID);
                assertEquals(closeness[nodeID - 1], nodeCent.getDouble(GraphConstants.CLOSENESS), TOLERANCE);
                assertEquals(betweenness[nodeID - 1], nodeCent.getDouble(GraphConstants.BETWEENNESS), TOLERANCE);
            }
        } finally {
            nodeCent.close();
        }
    }

    private void checkEdges(ResultSet edgeCent,
                            double[] betweenness) throws SQLException {
        try {
            while (edgeCent.next()) {
                final int edgeID = edgeCent.getInt(EDGE_ID);
                assertEquals(betweenness[(edgeID > 0) ? ((edgeID > 10) ? edgeID : edgeID - 1) : -edgeID],
                        edgeCent.getDouble(GraphConstants.BETWEENNESS), TOLERANCE);
            }
        } finally {
            edgeCent.close();
        }
    }
}
