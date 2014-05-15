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

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GraphConstants;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.utilities.GraphConstants.EDGE_CENT_SUFFIX;
import static org.h2gis.utilities.GraphConstants.NODE_CENT_SUFFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation')
        final ResultSet rs = compute(DO);
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

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
        final ResultSet nodeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        checkNodes(nodeCent,
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
        final ResultSet edgeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);
        checkEdges(edgeCent, DO_RO_EDGE_BETWEENNESS);
    }

    @Test
    public void WDO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight')
        final ResultSet rs = compute(DO, W);
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

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
        final ResultSet nodeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        checkNodes(nodeCent,
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
        final ResultSet edgeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);
        checkEdges(edgeCent, WDO_WRO_EDGE_BETWEENNESS);
    }

    @Test
    public void RO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation')
        final ResultSet rs = compute(RO);
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

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
        final ResultSet nodeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        checkNodes(nodeCent,
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
        final ResultSet edgeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);
        checkEdges(edgeCent, DO_RO_EDGE_BETWEENNESS);
    }

    @Test
    public void WRO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight')
        final ResultSet rs = compute(RO, W);
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

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
        final ResultSet nodeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        checkNodes(nodeCent,
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
        final ResultSet edgeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);
        checkEdges(edgeCent, WDO_WRO_EDGE_BETWEENNESS);
    }

    @Test
    public void U() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL',
        //     'undirected')
        final ResultSet rs = compute(U);
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

        // σ(1)  σ(2)  σ(3)  σ(4)  σ(5)  σ
        // |---- 0|010 00|10 000|0 0002| 11141
        // |0001 -|--- 00|02 000|2 0000| 11215
        // |0000 0|000 --|-- 000|0 0000| 12111
        // |0000 1|000 10|00 ---|- 2000| 41112
        // |1000 0|000 02|00 020|0 ----| 15121
        //
        // We don't put the calculation here because of Brande's assumption
        // about unique edges from v to w in Theorem 6.
        final ResultSet nodeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        checkNodes(nodeCent,
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
        final ResultSet edgeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);
        checkEdges(edgeCent, new double[]{3./4, 19./20, 0., 0., 11./20, 11./20, 7./10, 1./4, 1./4, 1.0});
    }

    @Test
    public void WU() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL',
        //     'undirected', 'weight')
        final ResultSet rs = compute(U, W);
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

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
        final ResultSet nodeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_CENT_SUFFIX);
        checkNodes(nodeCent,
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
        final ResultSet edgeCent = st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + EDGE_CENT_SUFFIX);
        checkEdges(edgeCent, new double[]{0., 3./5, 1., 0., 7./10, 0., 1./2, 1./5, 0., 1./10, 0.});
    }

    private ResultSet compute(String orientation, String weight) throws SQLException {
        return st.executeQuery(
                "SELECT ST_GraphAnalysis('CORMEN_EDGES_ALL', "
                        + orientation + ((weight != null) ? ", " + weight : "") + ")");
    }

    private ResultSet compute(String orientation) throws SQLException {
        return compute(orientation, null);
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
                final int edgeID = edgeCent.getInt(GraphConstants.EDGE_ID);
                assertEquals(betweenness[(edgeID > 0) ? edgeID - 1 : -edgeID],
                        edgeCent.getDouble(GraphConstants.BETWEENNESS), TOLERANCE);
            }
        } finally {
            edgeCent.close();
        }
    }
}
