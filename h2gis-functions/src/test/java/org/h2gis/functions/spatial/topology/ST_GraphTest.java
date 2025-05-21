/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.topology;

import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_GraphTest {

    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "ST_GraphTest";
    private static final int NUMBER_OF_NODE_COLS = 2;
    private static final int NUMBER_OF_EDGE_COLS = 3;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME, true);
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_Graph(), "");
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    private void checkNode(ResultSet nodesResult, int nodeID, String nodeGeom) throws SQLException {
        assertTrue(nodesResult.next());
        assertEquals(nodeID, nodesResult.getInt("NODE_ID"));
        assertGeometryEquals(nodeGeom, nodesResult.getObject("THE_GEOM"));
    }

    private void checkEdge(ResultSet edgesResult, int gid, int startNode, int endNode) throws SQLException {
        assertTrue(edgesResult.next());
        assertEquals(gid, edgesResult.getInt("EDGE_ID"));
        assertEquals(startNode, edgesResult.getInt("START_NODE"));
        assertEquals(endNode, edgesResult.getInt("END_NODE"));
    }

    @Test
    public void test_ST_Graph() throws Exception {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', DEFAULT),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', DEFAULT),"
                + "('LINESTRING (4 3, 5 2)', 'road4', DEFAULT),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5', DEFAULT),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6', DEFAULT),"
                + "('LINESTRING EMPTY', 'road7', DEFAULT);");

        try ( // Make sure everything went OK.
                ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)")) {
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
        }
    }

    @Test
    public void test_ST_Graph_GeometryColumnDetection() throws Exception {
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, way GEOMETRY(LINESTRING), id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', 'LINESTRING (1 1, 2 2, 3 1)', 1),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', 'LINESTRING (3 1, 2 0, 1 1)', 2),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', 'LINESTRING (1 1, 2 1)', 3),"
                + "('LINESTRING (4 3, 5 2)', 'road4', 'LINESTRING (2 1, 3 1)', 4);");

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
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES ORDER BY NODE_ID");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINT (0 0)");
        checkNode(nodesResult, 2, "POINT (1 2)");
        checkNode(nodesResult, 3, "POINT (4 3)");
        checkNode(nodesResult, 4, "POINT (5 2)");
        assertFalse(nodesResult.next());
        nodesResult.close();
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES ORDER BY EDGE_ID");
        assertEquals(NUMBER_OF_EDGE_COLS, edgesResult.getMetaData().getColumnCount());
        checkEdge(edgesResult, 1, 1, 2);
        checkEdge(edgesResult, 2, 2, 3);
        checkEdge(edgesResult, 3, 3, 2);
        checkEdge(edgesResult, 4, 3, 4);
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
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
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
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES ORDER BY EDGE_ID");
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
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
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
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
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
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', DEFAULT),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', DEFAULT),"
                + "('LINESTRING (4 3, 5 2)', 'road4', DEFAULT),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5', DEFAULT),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6', DEFAULT);");
        try (ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 1.1, false)")) {
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
        }
    }

    @Test
    public void test_ST_Graph_OrientBySlope() throws Exception {
        // This test proves that orientation by slope works. Three cases:
        // 1. first.z == last.z -- Orient first --> last
        // 2. first.z > last.z -- Orient first --> last
        // 3. first.z < last.z -- Orient last --> first

        // Case 1.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING Z), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRINGZ (0 0 0, 1 0 0)', 'road1', DEFAULT);");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINTZ (0 0 0)");
        checkNode(nodesResult, 2, "POINTZ (1 0 0)");
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
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING Z), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRINGZ (0 0 1, 1 0 0)', 'road1', DEFAULT);");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt("NODE_ID"));
        assertGeometryEquals("POINTZ (0 0 1)", nodesResult.getBytes("THE_GEOM"));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt("NODE_ID"));
        assertGeometryEquals("POINTZ (1 0 0)", nodesResult.getBytes("THE_GEOM"));
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
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING Z), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRINGZ (0 0 0, 1 0 1)', 'road1', DEFAULT);");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
        checkNode(nodesResult, 1, "POINTZ (0 0 0)");
        checkNode(nodesResult, 2, "POINTZ (1 0 1)");
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

    @Test
    public void test_ST_GraphCaseError() {
        assertThrows(SQLException.class, () -> {
            multiTestPrep();
            st.executeQuery("SELECT ST_Graph('\"TeST\"')");
        });
    }

    private void multiTestPrep() throws SQLException {
        st.execute("DROP TABLE IF EXISTS test; DROP TABLE IF EXISTS test_nodes; DROP TABLE IF EXISTS test_edges");
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR NOT NULL, "
                + "id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 0 2)', 'road1', DEFAULT),"
                + "('LINESTRING (1 0, 1 2)', 'road2', DEFAULT),"
                + "('LINESTRING (2 0, 2 2)', 'road3', DEFAULT);");
    }

    private void checkMultiTest(ResultSet rs) throws SQLException {
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        try ( // Test nodes table.
                ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES ORDER BY NODE_ID")) {
            assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
            checkNode(nodesResult, 1, "POINT (0 0)");
            checkNode(nodesResult, 2, "POINT (1 0)");
            checkNode(nodesResult, 3, "POINT (2 0)");
            checkNode(nodesResult, 4, "POINT (0 2)");
            checkNode(nodesResult, 5, "POINT (1 2)");
            checkNode(nodesResult, 6, "POINT (2 2)");
            assertFalse(nodesResult.next());
        }

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

    @Test
    public void test_ST_Graph_ErrorWithNoLINESTRINGOrMULTILINESTRING() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            // Prepare the input table.
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road POINT, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                    + "INSERT INTO test VALUES "
                    + "('POINT (0 0)', 'road1', DEFAULT);");
            try {
                st.executeQuery("SELECT ST_Graph('TEST')");
            } catch (JdbcSQLException e) {
                final Throwable originalCause = e.getCause();
                assertEquals(originalCause.getMessage(), ST_Graph.TYPE_ERROR + "POINT");
                throw e.getCause();
            }
        });
    }

    public void test_ST_Graph_ErrorWithNegativeTolerance() {
        assertThrows(JdbcSQLIntegrityConstraintViolationException.class, () -> {
            // Prepare the input table.
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                    + "INSERT INTO test VALUES "
                    + "('LINESTRING (0 0 0, 1 0 1)', 'road1', DEFAULT);");
            try {
                st.executeQuery("SELECT ST_Graph('TEST', 'road', -1.0)");
            } catch (JdbcSQLException e) {
                assertTrue(e.getMessage().contains("Only positive tolerances are allowed."));
                throw e.getCause();
            }
        });
    }

    public void test_ST_Graph_ErrorWithNoPrimaryKey() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            // Prepare the input table.
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road LINESTRINGZ, description VARCHAR);"
                    + "INSERT INTO test VALUES "
                    + "('LINESTRING (0 0 0, 1 0 1)', 'road1');");
            try {
                st.executeQuery("SELECT ST_Graph('TEST')");
            } catch (JdbcSQLException e) {
                assertTrue(e.getMessage().contains("must contain a single integer primary key"));
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_Graph_ErrorWithCompositePrimaryKey() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            // Prepare the input table.
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road LINESTRINGZ, description VARCHAR NOT NULL, "
                    + "id INT AUTO_INCREMENT);"
                    + "INSERT INTO test VALUES "
                    + "('LINESTRING (0 0 0, 1 0 1)', 'road1', DEFAULT);"
                    + "CREATE PRIMARY KEY ON TEST(DESCRIPTION, ID);");
            try {
                st.executeQuery("SELECT ST_Graph('TEST')");
            } catch (JdbcSQLException e) {
                assertTrue(e.getMessage().contains("must contain a single integer primary key"));
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_Graph_ErrorWithNullEdgeEndpoints() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            // Prepare the input table.
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR NOT NULL, "
                    + "id INT AUTO_INCREMENT PRIMARY KEY);"
                    + "INSERT INTO test VALUES "
                    + "('LINESTRING (0 0, 0 2)', 'road1', DEFAULT),"
                    + "('LINESTRING (1 0, 1 2)', 'road2', DEFAULT),"
                    + "('LINESTRING (2 0, 2 2)', 'road3', DEFAULT);");
            try {
                st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.5)");
            } catch (JdbcSQLException e) {
                assertTrue(e.getMessage().contains("Try using a slightly smaller tolerance."));
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_GraphMixedLINESTRINGSandMULTILINESTRINGS() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road GEOMETRY, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                    + "INSERT INTO test VALUES "
                    + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                    + "('MULTILINESTRING((1 2, 2 3, 4 3))', 'road2', DEFAULT);");
            try {
                st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)");
            } catch (JdbcSQLException e) {
                final Throwable originalCause = e.getCause();
                assertEquals(originalCause.getMessage(), ST_Graph.TYPE_ERROR + "GEOMETRY");
                throw originalCause;
            }
        });
    }

    @Test
    public void test_ST_GraphErrorWithNonLINESTRINGSandMULTILINESTRINGS() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            // Prepare the input table.
            st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
            st.execute("CREATE TABLE test(road GEOMETRY, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                    + "INSERT INTO test VALUES "
                    + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                    + "('MULTILINESTRING((1 2, 2 3, 4 3))', 'road2', DEFAULT),"
                    + "('POINT(4 3)', 'road3', DEFAULT);");

            // Make sure everything went OK.
            try {
                st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)");
            } catch (JdbcSQLException e) {
                final Throwable originalCause = e.getCause();
                assertEquals(originalCause.getMessage(), ST_Graph.TYPE_ERROR + "GEOMETRY");
                throw originalCause;
            }
        });
    }

    @Test
    public void test_ST_GraphErrorWhenCalledTwice() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            // Prepare the input table.
            multiTestPrep();
            try {
                st.executeQuery("CALL ST_Graph('TEST', 'road', 0.1, false)");
                st.executeQuery("CALL ST_Graph('TEST', 'road', 0.1, false)");
            } catch (JdbcSQLException e) {
                final Throwable originalCause = e.getCause();
                assertEquals(originalCause.getMessage(), ST_Graph.ALREADY_RUN_ERROR + "TEST");
                throw originalCause;
            }
        });
    }

    @Test
    public void test_ST_Graph_DeleteTables() throws Throwable {
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('LINESTRING(1 2, 2 3, 4 3)', 'road2', DEFAULT);");
        st.execute("SELECT ST_Graph('TEST', 'road', 0.1, false)");
        try (ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false, true)")) {
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
        }
    }

    @Test
    public void test_ST_GraphColumns() throws Exception {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST;");
        st.execute("CREATE TABLE test(the_geom GEOMETRY(LINESTRING),weight float, description VARCHAR, id INT AUTO_INCREMENT PRIMARY KEY);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 12, 'road1', 1),"
                + "('LINESTRING (1 2, 2 3, 4 3)',1,  'road2', 2),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 23, 'road3', 3),"
                + "('LINESTRING (4 3, 5 2)', 12, 'road4', 4),"
                + "('LINESTRING (4.05 4.1, 7 5)', 1, 'road5', 5),"
                + "('LINESTRING (7.1 5, 8 4)', 5, 'road6', 6);");

        try ( // Make sure everything went OK.
              ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', ARRAY['weight'], true)")) {
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
            assertFalse(rs.next());

            // Test nodes table.
            ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES limit 1");
            assertEquals(NUMBER_OF_NODE_COLS, nodesResult.getMetaData().getColumnCount());
            nodesResult.close();

            // Test edges table.
            ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES limit 1");
            // This is a copy of the original table with three columns added.
            assertEquals(4, edgesResult.getMetaData().getColumnCount());
            edgesResult.close();

            ResultSet edgesCount = st.executeQuery("SELECT count(*) FROM TEST_EDGES where weight is not null");
            assertTrue(edgesCount.next());
            assertEquals(6, edgesCount.getInt(1));
            edgesCount.close();
        }
    }

    @Test
    public void test_ST_GraphSerialID() throws Exception {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road GEOMETRY(LINESTRING), description VARCHAR, id SERIAL);"
                + "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', DEFAULT),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', DEFAULT),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', DEFAULT),"
                + "('LINESTRING (4 3, 5 2)', 'road4', DEFAULT),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5', DEFAULT),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6', DEFAULT),"
                + "('LINESTRING EMPTY', 'road7', DEFAULT);");

        try ( // Make sure everything went OK.
              ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)")) {
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
        }
    }

}
