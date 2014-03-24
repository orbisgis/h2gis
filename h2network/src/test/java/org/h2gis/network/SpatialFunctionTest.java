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
package org.h2gis.network;

import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.network.graph_creator.GraphCreatorTest;
import org.h2gis.network.graph_creator.ST_Graph;
import org.h2gis.network.graph_creator.ST_ShortestPathLength;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Adam Gouge
 */
public class SpatialFunctionTest {

    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "SpatialFunctionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPathLength(), "");
        GraphCreatorTest.registerCormenGraph(connection);
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

    private static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) {
        assertTrue(Arrays.equals(ValueGeometry.get(expectedWKT).getBytes(), valueWKB));
    }

    @Test
    public void test_ST_Graph() throws Exception {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1'),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2'),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3'),"
                + "('LINESTRING (4 3, 5 2)', 'road4'),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5'),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6');");

        // Make sure everything went OK.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

        // Test nodes table.
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 2)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (4 3)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(4, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (5 2)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(5, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (4.05 4.1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(6, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (7 5)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(7, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (8 4)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());

        // Test edges table.
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        // This is a copy of the original table with three columns added.
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 2, 2 3, 4 3)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 4 4, 1 4, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road3", edgesResult.getString("description"));
        assertEquals(3, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 5 2)", edgesResult.getBytes("road"));
        assertEquals("road4", edgesResult.getString("description"));
        assertEquals(4, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(4, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4.05 4.1, 7 5)", edgesResult.getBytes("road"));
        assertEquals("road5", edgesResult.getString("description"));
        assertEquals(5, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(5, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(6, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (7.1 5, 8 4)", edgesResult.getBytes("road"));
        assertEquals("road6", edgesResult.getString("description"));
        assertEquals(6, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(6, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(7, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
    }

    @Test
    public void test_ST_Graph_GeometryColumnDetection() throws Exception {
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, way LINESTRING);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', 'LINESTRING (1 1, 2 2, 3 1)'),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', 'LINESTRING (3 1, 2 0, 1 1)'),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', 'LINESTRING (1 1, 2 1)'),"
                + "('LINESTRING (4 3, 5 2)', 'road4', 'LINESTRING (2 1, 3 1)');");

        // This should detect the 'road' column since it is the first geometry column.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 2)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (4 3)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(4, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (5 2)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(3 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 2, 2 3, 4 3)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 4 4, 1 4, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road3", edgesResult.getString("description"));
        assertEquals(3, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 5 2)", edgesResult.getBytes("road"));
        assertEquals("road4", edgesResult.getString("description"));
        assertEquals(4, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(4, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());

        // Here we specify the 'way' column.
        st.execute("DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'way')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (3 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (2 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(3 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 1, 2 2, 3 1)", edgesResult.getBytes("way"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (3 1, 2 0, 1 1)", edgesResult.getBytes("way"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(1, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 1, 2 1)", edgesResult.getBytes("way"));
        assertEquals("road3", edgesResult.getString("description"));
        assertEquals(3, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (2 1, 3 1)", edgesResult.getBytes("way"));
        assertEquals("road4", edgesResult.getString("description"));
        assertEquals(4, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
    }

    @Test
    public void test_ST_Graph_Tolerance() throws Exception {
        // This first test shows that nodes within a tolerance of 0.05 of each
        // other are considered to be a single node.
        // Note, however, that edge geometries are left untouched.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 0)', 'road1'),"
                + "('LINESTRING (1.05 0, 2 0)', 'road2'),"
                + "('LINESTRING (2.05 0, 3 0)', 'road3'),"
                + "('LINESTRING (1 0.1, 1 1)', 'road4'),"
                + "('LINESTRING (2 0.05, 2 1)', 'road5');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (2 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(4, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (3 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(5, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0.1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(6, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(7, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (2 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0, 1 0)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1.05 0, 2 0)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (2.05 0, 3 0)", edgesResult.getBytes("road"));
        assertEquals("road3", edgesResult.getString("description"));
        assertEquals(3, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(4, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 0.1, 1 1)", edgesResult.getBytes("road"));
        assertEquals("road4", edgesResult.getString("description"));
        assertEquals(4, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(5, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(6, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (2 0.05, 2 1)", edgesResult.getBytes("road"));
        assertEquals("road5", edgesResult.getString("description"));
        assertEquals(5, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(7, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());

        // This test shows that _coordinates_ within a given tolerance of each
        // other are not necessarily snapped together. Only the first and last
        // coordinates of a geometry are considered to be potential nodes, and
        // only _nodes_ within a given tolerance of each other are snapped
        // together.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 1, 1 1, 1 0)', 'road1'),"
                + "('LINESTRING (1.05 1, 2 1)', 'road2');");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1.05 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(4, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (2 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 1, 1 1, 1 0)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1.05 1, 2 1)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(3, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(4, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());

        // This test shows that geometry intersections are not automatically
        // considered to be potential nodes.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 1, 2 1)', 'road1'),"
                + "('LINESTRING (2 1, 1 0, 1 2)', 'road2');");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (2 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 2)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 1, 2 1)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (2 1, 1 0, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
    }

    @Test
    public void test_ST_Graph_BigTolerance() throws Exception {
        // This test shows that the results from using a large tolerance value
        // (3.1 rather than 0.1) can be very different.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1'),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2'),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3'),"
                + "('LINESTRING (4 3, 5 2)', 'road4'),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5'),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 3.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (4 3)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (8 4)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(1, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 2, 2 3, 4 3)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 4 4, 1 4, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road3", edgesResult.getString("description"));
        assertEquals(3, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(1, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 5 2)", edgesResult.getBytes("road"));
        assertEquals("road4", edgesResult.getString("description"));
        assertEquals(4, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4.05 4.1, 7 5)", edgesResult.getBytes("road"));
        assertEquals("road5", edgesResult.getString("description"));
        assertEquals(5, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (7.1 5, 8 4)", edgesResult.getBytes("road"));
        assertEquals("road6", edgesResult.getString("description"));
        assertEquals(6, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
    }

    @Test
    public void test_ST_Graph_OrientBySlope() throws Exception {
        // This test proves that orientation by slope works. Three cases:
        // 1. first.z == last.z -- Orient first --> last
        // 2. first.z > last.z -- Orient first --> last
        // 3. first.z < last.z -- Orient last --> first

        // Case 1.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 0)', 'road1');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0 0, 1 0 0)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        // Orient first --> last
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());

        // Case 2.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 1, 1 0 0)', 'road1');");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0 1, 1 0 0)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        // Orient first --> last
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());

        // Case 3.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 1)', 'road1');");
        rs = st.executeQuery("SELECT ST_Graph('TEST', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0 0, 1 0 1)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        // Orient last --> first
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(1, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
    }

    @Test
    public void test_ST_Graph_ErrorWithNoLINESTRINGOrMULTILINESTRING() throws Exception {
        // Prepare the input table.
        st.execute("DROP TABLE IF EXISTS TEST; DROP TABLE IF EXISTS TEST_NODES; DROP TABLE IF EXISTS TEST_EDGES");
        st.execute("CREATE TABLE test(road POINT, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('POINT (0 0)', 'road1');");

        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST')");
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
        assertFalse(rs.next());

        assertFalse(connection.getMetaData().getTables(null, null, "TEST_NODES", null).last());
        assertFalse(connection.getMetaData().getTables(null, null, "TEST_EDGES", null).last());
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
        st.execute("CREATE TABLE test(road MULTILINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('MULTILINESTRING ((0 0, 1 2), (1 2, 2 3, 4 3))', 'road1'),"
                + "('MULTILINESTRING ((4 3, 4 4, 1 4, 1 2), (4 3, 5 2))', 'road2');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('TEST')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM TEST_NODES");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (4 3)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (5 2)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        ResultSet edgesResult = st.executeQuery("SELECT * FROM TEST_EDGES");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("MULTILINESTRING ((0 0, 1 2), (1 2, 2 3, 4 3))", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertTrue(edgesResult.next());
        assertGeometryEquals("MULTILINESTRING ((4 3, 4 4, 1 4, 1 2), (4 3, 5 2))", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt(ST_Graph.EDGE_ID));
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(3, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
    }
}
