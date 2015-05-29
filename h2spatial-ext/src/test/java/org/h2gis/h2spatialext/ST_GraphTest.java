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
package org.h2gis.h2spatialext;

import org.h2.jdbc.JdbcSQLException;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.function.spatial.graph.ST_Graph;
import org.h2gis.utilities.GraphConstants;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.h2gis.utilities.GraphConstants.*;
import static org.junit.Assert.*;

/**
 * @author Adam Gouge
 */
public class ST_GraphTest {

    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "ST_GraphTest";
    private static final int NUMBER_OF_NODE_COLS = 2;
    private static final int NUMBER_OF_EDGE_COLS = 3;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    private void checkNode(ResultSet nodesResult, int nodeID, String nodeGeom) throws SQLException {
        assertTrue(nodesResult.next());
        assertEquals(nodeID, nodesResult.getInt(GraphConstants.NODE_ID));
        assertGeometryEquals(nodeGeom, nodesResult.getBytes(GraphConstants.THE_GEOM));
    }

    private void checkEdge(ResultSet edgesResult, int gid, int startNode, int endNode) throws SQLException {
        assertTrue(edgesResult.next());
        assertEquals(gid, edgesResult.getInt(EDGE_ID));
        assertEquals(startNode, edgesResult.getInt(START_NODE));
        assertEquals(endNode, edgesResult.getInt(END_NODE));
    }

    @Test
    public void test_ST_Graph() throws Exception {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', DEFAULT),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', DEFAULT),"
                + "('LINESTRING (4 3, 5 2)', 'road4', DEFAULT),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5', DEFAULT),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6', DEFAULT);");

        // Make sure everything went OK.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

        // Test nodes table.
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (1 2)");
        checkNode(nodesResult, 3, "POINT (4 3)");
        checkNode(nodesResult, 4, "POINT (4.05 4.1)");
        checkNode(nodesResult, 5, "POINT (7.1 5)");
        checkNode(nodesResult, 6, "POINT (5 2)");
        checkNode(nodesResult, 7, "POINT (8 4)");
        assertFalse(nodesResult.next());
        nodesResult.close();

        // Test edges table.
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        // This is a copy of the original table with three columns added.
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        checkEdge(edgesResult, 3, 3, 2);
        checkEdge(edgesResult, 4, 3, 6);
        checkEdge(edgesResult, 5, 4, 5);
        checkEdge(edgesResult, 6, 5, 7);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test
    public void test_ST_Graph_GeometryColumnDetection() throws Exception {
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, way LINESTRING, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', 'LINESTRING (1 1, 2 2, 3 1)', DEFAULT),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', 'LINESTRING (3 1, 2 0, 1 1)', DEFAULT),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', 'LINESTRING (1 1, 2 1)', DEFAULT),"
                + "('LINESTRING (4 3, 5 2)', 'road4', 'LINESTRING (2 1, 3 1)', DEFAULT);");

        // This should detect the 'road' column since it is the first geometry column.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (1 2)");
        checkNode(nodesResult, 3, "POINT (4 3)");
        checkNode(nodesResult, 4, "POINT (5 2)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        checkEdge(edgesResult, 3, 3, 2);
        checkEdge(edgesResult, 4, 3, 4);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();

        // Here we specify the 'way' column.
        st.execute("DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'way')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (1 1)");
        checkNode(nodesResult, 2, "POINT (3 1)");
        checkNode(nodesResult, 3, "POINT (2 1)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 1);
        checkEdge(edgesResult, 3, 1, 3);
        checkEdge(edgesResult, 4, 3, 2);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test
    public void test_ST_Graph_Tolerance() throws Exception {
        // This first test shows that nodes within a tolerance of 0.05 of each
        // other are considered to be a single node.
        // Note, however, that edge geometries are left untouched.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 0)', 'road1', DEFAULT),"
                + "('LINESTRING (1.05 0, 2 0)', 'road2', DEFAULT),"
                + "('LINESTRING (2.05 0, 3 0)', 'road3', DEFAULT),"
                + "('LINESTRING (1 0.1, 1 1)', 'road4', DEFAULT),"
                + "('LINESTRING (2 0.05, 2 1)', 'road5', DEFAULT);");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.049)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (1.05 0)");
        checkNode(nodesResult, 3, "POINT (2.05 0)");
        checkNode(nodesResult, 4, "POINT (1 0.1)");
        checkNode(nodesResult, 5, "POINT (3 0)");
        checkNode(nodesResult, 6, "POINT (1 1)");
        checkNode(nodesResult, 7, "POINT (2 1)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        checkEdge(edgesResult, 3, 3, 5);
        checkEdge(edgesResult, 4, 4, 6);
        checkEdge(edgesResult, 5, 3, 7);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();

        // This test shows that _coordinates_ within a given tolerance of each
        // other are not necessarily snapped together. Only the first and last
        // coordinates of a geometry are considered to be potential nodes, and
        // only _nodes_ within a given tolerance of each other are snapped
        // together.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 1, 1 1, 1 0)', 'road1', DEFAULT),"
                + "('LINESTRING (1.05 1, 2 1)', 'road2', DEFAULT);");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 1)");
        checkNode(nodesResult, 2, "POINT (1.05 1)");
        checkNode(nodesResult, 3, "POINT (1 0)");
        checkNode(nodesResult, 4, "POINT (2 1)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 3);
        checkEdge(edgesResult, 2, 2, 4);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();

        // This test shows that geometry intersections are not automatically
        // considered to be potential nodes.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 1, 2 1)', 'road1', DEFAULT),"
                + "('LINESTRING (2 1, 1 0, 1 2)', 'road2', DEFAULT);");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 1)");
        checkNode(nodesResult, 2, "POINT (2 1)");
        checkNode(nodesResult, 3, "POINT (1 2)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test
    public void test_ST_Graph_BigTolerance() throws Exception {
        // This test shows that the results from using a large tolerance value
        // (1.1 rather than 0.1) can be very different.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', DEFAULT),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', DEFAULT),"
                + "('LINESTRING (4 3, 5 2)', 'road4', DEFAULT),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5', DEFAULT),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6', DEFAULT);");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 1.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (4 3)");
        checkNode(nodesResult, 3, "POINT (7.1 5)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 1);
        checkEdge(edgesResult, 2, 1, 2);
        checkEdge(edgesResult, 3, 2, 1);
        checkEdge(edgesResult, 4, 2, 2);
        checkEdge(edgesResult, 5, 2, 3);
        checkEdge(edgesResult, 6, 3, 3);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test
    public void test_ST_Graph_OrientBySlope() throws Exception {
        // This test proves that orientation by slope works. Three cases:
        // 1. first.z == last.z -- Orient first --> last
        // 2. first.z > last.z -- Orient first --> last
        // 3. first.z < last.z -- Orient last --> first

        // Case 1.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 0)', 'road1', DEFAULT);");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0 0)");
        checkNode(nodesResult, 2, "POINT (1 0 0)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        // Orient first --> last
        checkEdge(edgesResult, 1, 1, 2);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();

        // Case 2.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 1, 1 0 0)', 'road1', DEFAULT);");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(GraphConstants.NODE_ID));
        assertGeometryEquals("POINT (0 0 1)", nodesResult.getBytes(GraphConstants.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(GraphConstants.NODE_ID));
        assertGeometryEquals("POINT (1 0 0)", nodesResult.getBytes(GraphConstants.THE_GEOM));
        assertFalse(nodesResult.next());
        nodesResult.close();
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        // Orient first --> last
        checkEdge(edgesResult, 1, 1, 2);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();

        // Case 3.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 1)', 'road1', DEFAULT);");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0 0)");
        checkNode(nodesResult, 2, "POINT (1 0 1)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        // Orient last --> first
        checkEdge(edgesResult, 1, 2, 1);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test
    public void test_ST_GraphCase() throws SQLException {
        // Prepare the input table.
        multiTestPrep();
        checkMultiTest(st.executeQuery("SELECT ST_Graph('test')"));
        st.execute("DROP TABLE IF EXISTS test_nodes; DROP TABLE IF EXISTS test_edges");
        checkMultiTest(st.executeQuery("SELECT ST_Graph('TeST')"));
        st.execute("DROP TABLE IF EXISTS test_nodes; DROP TABLE IF EXISTS test_edges");
        checkMultiTest(st.executeQuery("SELECT ST_Graph('TEST')"));
    }

    @Test(expected = SQLException.class)
    public void test_ST_GraphCaseError() throws Throwable {
        multiTestPrep();
        try {
            st.executeQuery("SELECT ST_Graph('\"TeST\"')");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Table TeST not found"));
            throw e.getOriginalCause();
        }
    }

    private void multiTestPrep() throws SQLException {
        st.execute("DROP TABLE IF EXISTS test; DROP TABLE IF EXISTS test_nodes; DROP TABLE IF EXISTS test_edges");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR NOT NULL, " +
                "id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0, 0 2)', 'road1', DEFAULT)," +
                "('LINESTRING (1 0, 1 2)', 'road2', DEFAULT)," +
                "('LINESTRING (2 0, 2 2)', 'road3', DEFAULT);");
    }

    private void checkMultiTest(ResultSet rs) throws SQLException {
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertFalse(rs.next());
        // Test nodes table.
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (1 0)");
        checkNode(nodesResult, 3, "POINT (2 0)");
        checkNode(nodesResult, 4, "POINT (0 2)");
        checkNode(nodesResult, 5, "POINT (1 2)");
        checkNode(nodesResult, 6, "POINT (2 2)");
        assertFalse(nodesResult.next());
        nodesResult.close();

        // Test edges table.
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        // This is a copy of the original table with three columns added.
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 4);
        checkEdge(edgesResult, 2, 2, 5);
        checkEdge(edgesResult, 3, 3, 6);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_Graph_ErrorWithNoLINESTRINGOrMULTILINESTRING() throws Throwable {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road POINT, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('POINT (0 0)', 'road1', DEFAULT);");
        try {
            st.executeQuery("SELECT ST_Graph('TEST')");
        } catch (JdbcSQLException e) {
            final Throwable originalCause = e.getOriginalCause();
            assertTrue(originalCause.getMessage().equals(ST_Graph.TYPE_ERROR + "POINT"));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_Graph_ErrorWithNegativeTolerance() throws Throwable {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 1)', 'road1', DEFAULT);");
        try {
            st.executeQuery("SELECT ST_Graph('TEST', 'road', -1.0)");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Only positive tolerances are allowed."));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void test_ST_Graph_ErrorWithNoPrimaryKey() throws Throwable {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 1)', 'road1');");
        try {
            st.executeQuery("SELECT ST_Graph('TEST')");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("must contain a single integer primary key"));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void test_ST_Graph_ErrorWithCompositePrimaryKey() throws Throwable {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR NOT NULL, " +
                "id INT AUTO_INCREMENT);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 1)', 'road1', DEFAULT);" +
                "CREATE PRIMARY KEY ON TEST(DESCRIPTION, ID);");
        try {
            st.executeQuery("SELECT ST_Graph('TEST')");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("must contain a single integer primary key"));
            throw e.getOriginalCause();
        }
    }

    @Test
    public void test_ST_Graph_MULTILINESTRING() throws Exception {
        // This test shows that the coordinate (1 2) is not considered to be
        // a node, even though it would be if we had used ST_Explode to split
        // the MULTILINESTRINGs into LINESTRINGs.
        // Note also that the last coordinate of the first LINESTRING of road2 (1 2)
        // is not equal to the first coordinate of the second LINESTRING of road2 (4 3),
        // so this MULTILINESTRING is considered to be an edge from node 2=(4 3) to
        // node 3=(5 2).
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road MULTILINESTRING, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('MULTILINESTRING ((0 0, 1 2), (1 2, 2 3, 4 3))', 'road1', DEFAULT),"
                + "('MULTILINESTRING ((4 3, 4 4, 1 4, 1 2), (4 3, 5 2))', 'road2', DEFAULT);");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (4 3)");
        checkNode(nodesResult, 3, "POINT (5 2)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test(expected = IllegalStateException.class)
    public void test_ST_Graph_ErrorWithNullEdgeEndpoints() throws Throwable {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR NOT NULL, " +
                "id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0, 0 2)', 'road1', DEFAULT)," +
                "('LINESTRING (1 0, 1 2)', 'road2', DEFAULT)," +
                "('LINESTRING (2 0, 2 2)', 'road3', DEFAULT);");
        try {
            st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.5)");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Try using a slightly smaller tolerance."));
            throw e.getOriginalCause();
        }
    }

    @Test
    public void test_ST_GraphMixedLINESTRINGSandMULTILINESTRINGS() throws Throwable {
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('MULTILINESTRING((1 2, 2 3, 4 3))', 'road2', DEFAULT);");
        final ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

        // Test nodes table.
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (1 2)");
        checkNode(nodesResult, 3, "POINT (4 3)");
        assertFalse(nodesResult.next());
        nodesResult.close();

        // Test edges table.
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        // This is a copy of the original table with three columns added.
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        assertFalse(edgesResult.next());
        edgesResult.close();
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_GraphErrorWithNonLINESTRINGSandMULTILINESTRINGS() throws Throwable {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('MULTILINESTRING((1 2, 2 3, 4 3))', 'road2', DEFAULT),"
                + "('POINT(4 3)', 'road3', DEFAULT);");

        // Make sure everything went OK.
        try {
            st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)");
        } catch (JdbcSQLException e) {
            final Throwable originalCause = e.getOriginalCause();
            assertTrue(originalCause.getMessage().equals(ST_Graph.TYPE_ERROR + "POINT"));
            throw originalCause;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_GraphErrorWhenCalledTwice() throws Throwable {
        // Prepare the input table.
        multiTestPrep();
        try {
            st.executeQuery("CALL ST_Graph('TEST', 'road', 0.1, false)");
            st.executeQuery("CALL ST_Graph('TEST', 'road', 0.1, false)");
        } catch (JdbcSQLException e) {
            final Throwable originalCause = e.getOriginalCause();
            assertTrue(originalCause.getMessage().equals(ST_Graph.ALREADY_RUN_ERROR + "TEST"));
            throw originalCause;
        }
    }
}
