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
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.h2gis.utilities.GraphConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by adam on 4/22/14.
 */
public class ST_AccessibilityTest {

    private static Connection connection;
    private Statement st;
    private static final double TOLERANCE = 0.0;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String W = "'weight'";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_AccessibilityTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Accessibility(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPathLength(), "");
        GraphCreatorTest.registerCormenGraph(connection);
        registerDestinationTables(connection);
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

    private static void registerDestinationTables(Connection connection) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE dest15(destination INT);" +
                    "INSERT INTO dest15 VALUES (1), (5);" +
                    "CREATE TABLE dest234(destination INT);" +
                    "INSERT INTO dest234 VALUES (2), (3), (4);");
        } finally {
            st.close();
        }
    }

    @Test
    public void DO() throws Exception {
        final int[] closestDests15 = new int[]{1, 5, 5, 5, 5};
        final double[] dists15 = new double[]{0.0, 2.0, 1.0, 1.0, 0.0};
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', '1, 5')
        check(compute(DO, "'1, 5'"), closestDests15, dists15);
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'dest15')
        check(compute(DO, "'dest15'"), closestDests15, dists15);
        final double[] dists234 = new double[]{1.0, 0.0, 0.0, 0.0, 1.0};
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', '2, 3, 4')
        check234DO(compute(DO, "'2, 3, 4'"), dists234);
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', '2, 3, 4')
        check234DO(compute(DO, "'dest234'"), dists234);
    }

    private void check234DO(ResultSet rs234, double[] dists234) throws SQLException {
        // d(1,2)=d(1,3)=1.0.
        try {
            check(rs234, new int[]{2, 2, 3, 4, 4}, dists234);
        } catch (AssertionError e) {
            rs234.beforeFirst();
            check(rs234, new int[]{3, 2, 3, 4, 4}, dists234);
        }
    }

    @Test
    public void WDO() throws Exception {
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight', '1, 5')
        check(compute(DO, W, "'1, 5'"), new int[]{1, 5, 5, 5, 5}, new double[]{0.0, 4.0, 2.0, 4.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight', 'dest15')
        check(compute(DO, W, "'dest15'"), new int[]{1, 5, 5, 5, 5}, new double[]{0.0, 4.0, 2.0, 4.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight', '2, 3, 4')
        check(compute(DO, W, "'2, 3, 4'"), new int[]{3, 2, 3, 4, 4}, new double[]{5.0, 0.0, 0.0, 0.0, 6.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight', 'dest234')
        check(compute(DO, W, "'dest234'"), new int[]{3, 2, 3, 4, 4}, new double[]{5.0, 0.0, 0.0, 0.0, 6.0});
    }

    @Test
    public void RO() throws Exception {
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', '1, 5')
        check(compute(RO, "'1, 5'"), new int[]{1, 1, 1, 5, 5}, new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'dest15')
        check(compute(RO, "'dest15'"), new int[]{1, 1, 1, 5, 5}, new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
        final double[] dist234 = new double[]{2.0, 0.0, 0.0, 0.0, 1.0};
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', '2, 3, 4')
        check234RO(compute(RO, "'2, 3, 4'"), dist234);
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'dest234')
        check234RO(compute(RO, "'dest234'"), dist234);
    }

    private void check234RO(ResultSet rs234, double[] dist234) throws SQLException {
        // d(1,3)=d(1,4)=2.0, d(5,3)=d(5,4)=1.0.
        try {
            check(rs234, new int[]{3, 2, 3, 4, 3}, dist234);
        } catch (AssertionError e) {
            rs234.beforeFirst();
            try {
                check(rs234, new int[]{3, 2, 3, 4, 4}, dist234);
            } catch (AssertionError e1) {
                rs234.beforeFirst();
                try {
                    check(rs234, new int[]{4, 2, 3, 4, 3}, dist234);
                } catch (AssertionError e2) {
                    rs234.beforeFirst();
                    check(rs234, new int[]{4, 2, 3, 4, 4}, dist234);
                }
            }
        }
    }

    @Test
    public void WRO() throws Exception {
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', '1, 5')
        check(compute(RO, W, "'1, 5'"), new int[]{1, 5, 1, 5, 5}, new double[]{0.0, 7.0, 5.0, 6.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', 'dest15')
        check(compute(RO, W, "'dest15'"), new int[]{1, 5, 1, 5, 5}, new double[]{0.0, 7.0, 5.0, 6.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', '2, 3, 4')
        check(compute(RO, W, "'2, 3, 4'"), new int[]{3, 2, 3, 4, 3}, new double[]{9.0, 0.0, 0.0, 0.0, 2.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', 'dest234')
        check(compute(RO, W, "'dest234'"), new int[]{3, 2, 3, 4, 3}, new double[]{9.0, 0.0, 0.0, 0.0, 2.0});
    }

    @Test
    public void U() throws Exception {
        final double[] dist15 = new double[]{0.0, 1.0, 1.0, 1.0, 0.0};
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', '1, 5')
        check15U(compute(U, "'1, 5'"), dist15);
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', 'dest15')
        check15U(compute(U, "'dest15'"), dist15);
        final double[] dist234 = new double[]{1.0, 0.0, 0.0, 0.0, 1.0};
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', '2, 3, 4')
        check234U(compute(U, "'2, 3, 4'"), dist234);
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', 'dest234')
        check234U(compute(U, "'dest234'"), dist234);
    }

    private void check15U(ResultSet rs15, double[] dist15) throws SQLException {
        // d(3,1)=d(3,5)=1.0.
        try {
            check(rs15, new int[]{1, 1, 1, 5, 5}, dist15);
        } catch (AssertionError e) {
            rs15.beforeFirst();
            check(rs15, new int[]{1, 1, 5, 5, 5}, dist15);
        }
    }

    private void check234U(ResultSet rs234, double[] dist234) throws SQLException {
        // d(1,2)=d(1,3)=1.0, d(5,3)=d(5,4)=1.0.
        try {
            check(rs234, new int[]{2, 2, 3, 4, 3}, dist234);
        } catch (AssertionError e) {
            rs234.beforeFirst();
            try {
                check(rs234, new int[]{2, 2, 3, 4, 4}, dist234);
            } catch (AssertionError e1) {
                rs234.beforeFirst();
                try {
                    check(rs234, new int[]{3, 2, 3, 4, 3}, dist234);
                } catch (AssertionError e2) {
                    rs234.beforeFirst();
                    check(rs234, new int[]{3, 2, 3, 4, 4}, dist234);
                }
            }
        }
    }

    @Test
    public void WU() throws Exception {
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', '1, 5')
        check(compute(U, W, "'1, 5'"), new int[]{1, 5, 5, 5, 5}, new double[]{0.0, 4.0, 2.0, 4.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', 'dest15')
        check(compute(U, W, "'dest15'"), new int[]{1, 5, 5, 5, 5}, new double[]{0.0, 4.0, 2.0, 4.0, 0.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', '2, 3, 4')
        check(compute(U, W, "'2, 3, 4'"), new int[]{3, 2, 3, 4, 3}, new double[]{5.0, 0.0, 0.0, 0.0, 2.0});
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', 'dest234')
        check(compute(U, W, "'dest234'"), new int[]{3, 2, 3, 4, 3}, new double[]{5.0, 0.0, 0.0, 0.0, 2.0});
    }

    @Test
    public void DOSingleDestination() throws Exception {
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', '5')
        check(compute(DO, "'5'"), new int[]{5, 5, 5, 5, 5}, new double[]{1.0, 2.0, 1.0, 1.0, 0.0});
    }

    @Test
    public void WDOSingleDestination() throws Exception {
        // SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight', '5')
        check(compute(DO, W, "'5'"), new int[]{5, 5, 5, 5, 5}, new double[]{7.0, 4.0, 2.0, 4.0, 0.0});
    }

    @Test
    public void testST_AccST_SPLSingleDestEquivalence() throws Exception {
        final ResultSet sPL = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('CORMEN_EDGES_ALL', " +
                "'reversed - edge_orientation', 'weight', 5)");
        final Map<Integer, Double> distancesMap = new HashMap<Integer, Double>();
        try {
            while (sPL.next()) {
                assertEquals(5, sPL.getInt(SOURCE));
                distancesMap.put(sPL.getInt(DESTINATION),
                        sPL.getDouble(DISTANCE));
            }
        } finally {
            sPL.close();
        }
        final ResultSet aCC = st.executeQuery(
                "SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL', " +
                "'directed - edge_orientation', 'weight', '5')");
        try {
            while (aCC.next()) {
                assertEquals(5, aCC.getInt(CLOSEST_DEST));
                assertEquals(distancesMap.get(aCC.getInt(SOURCE)),
                        aCC.getDouble(DISTANCE), TOLERANCE);
            }
        } finally {
            aCC.close();
        }
    }

    private ResultSet compute(String orientation, String weight, String destinationString) throws SQLException {
        return st.executeQuery(
                "SELECT * FROM ST_Accessibility('CORMEN_EDGES_ALL', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + destinationString + ")");
    }

    private ResultSet compute(String orientation, String destinationString) throws SQLException {
        return compute(orientation, null, destinationString);
    }

    private void check(ResultSet rs, int[] closestDests, double[] distances) throws SQLException {
        int count = 0;
        while (rs.next()) {
            final int returnedSource = rs.getInt(SOURCE);
            final int closestDestination = rs.getInt(CLOSEST_DEST);
            assertEquals(closestDests[returnedSource - 1], closestDestination);
            final double distance = rs.getDouble(DISTANCE);
            assertEquals(distances[returnedSource - 1], distance, TOLERANCE);
            count++;
        }
        assertEquals(distances.length, count);
        rs.close();
    }
}
