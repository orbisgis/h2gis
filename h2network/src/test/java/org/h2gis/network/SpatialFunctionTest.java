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
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.network;

import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.network.graph_creator.ST_Graph;
import org.h2gis.network.graph_creator.ST_ShortestPathLength;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Adam Gouge
 */
public class SpatialFunctionTest {

    private static Connection connection;
    private static final String DB_NAME = "SpatialFunctionTest";
    private static final double TOLERANCE = 0.0;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPathLength(), "");
        registerCormenGraph(connection);
    }

    public static void registerCormenGraph(Connection connection) throws SQLException {
        final Statement st = connection.createStatement();
//                   1
//           >2 ------------>3
//          / |^           ->|^
//       10/ / |      9   / / |
//        / 2| |3    -----  | |
//       /   | |    /      4| |6
//      1<---------------   | |
//       \   | |  /     7\  | |
//       5\  | / /        \ | /
//         \ v| /    2     \v|
//          > 4 -----------> 5
//               CORMEN
        st.execute("CREATE TABLE cormen(road LINESTRING, weight DOUBLE, edge_orientation INT);" +
                "INSERT INTO cormen VALUES "
                + "('LINESTRING (0 1, 1 2)', 10.0, 1),"
                + "('LINESTRING (1 2, 2 2)', 1.0, -1),"
                + "('LINESTRING (1 2, 1 0)', 2.0, 1),"
                + "('LINESTRING (1 0, 1 2)', 3.0, 1),"
                + "('LINESTRING (0 1, 1 0)', 5.0, 1),"
                + "('LINESTRING (1 0, 2 2)', 9.0, 1),"
                + "('LINESTRING (1 0, 2 0)', 2.0, 1),"
                + "('LINESTRING (2 2, 2 0)', 4.0, 1),"
                + "('LINESTRING (2 0, 2 2)', 6.0, 1),"
                + "('LINESTRING (2 0, 0 1)', 7.0, 0);");

        st.executeQuery("SELECT ST_Graph('cormen', 'road')");
//        cormen_nodes
//        NODE_ID  THE_GEOM
//        1        POINT (0 1)
//        2        POINT (1 2)
//        3        POINT (2 2)
//        4        POINT (1 0)
//        5        POINT (2 0)
//
//        cormen_edges:
//        ROAD                   WEIGHT  EDGE_ID   START_NODE   END_NODE
//        LINESTRING (0 1, 1 2)  10.0    1         1            2
//        LINESTRING (1 2, 2 2)  1.0     2         2            3
//        LINESTRING (1 2, 1 0)  2.0     3         2            4
//        LINESTRING (1 0, 1 2)  3.0     4         4            2
//        LINESTRING (0 1, 1 0)  5.0     5         1            4
//        LINESTRING (1 0, 2 2)  9.0     6         4            3
//        LINESTRING (1 0, 2 0)  2.0     7         4            5
//        LINESTRING (2 2, 2 0)  4.0     8         3            5
//        LINESTRING (2 0, 2 2)  6.0     9         5            3
//        LINESTRING (2 0, 0 1)  7.0     10        5            1
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
        Statement st = connection.createStatement();

        // Prepare the input table.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1'),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2'),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3'),"
                + "('LINESTRING (4 3, 5 2)', 'road4'),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5'),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6');");

        // Make sure everything went OK.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

        // Test nodes table.
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
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

        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }

    @Test
    public void test_ST_Graph_GeometryColumnDetection() throws Exception {
        Statement st = connection.createStatement();

        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR, way LINESTRING);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1', 'LINESTRING (1 1, 2 2, 3 1)'),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2', 'LINESTRING (3 1, 2 0, 1 1)'),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3', 'LINESTRING (1 1, 2 1)'),"
                + "('LINESTRING (4 3, 5 2)', 'road4', 'LINESTRING (2 1, 3 1)');");

        // This should detect the 'road' column since it is the first geometry column.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
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
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");

        // Here we specify the 'way' column.
        rs = st.executeQuery("SELECT ST_Graph('test', 'way')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        edgesResult = st.executeQuery("SELECT * FROM test_edges");
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
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }

    @Test
    public void test_ST_Graph_Tolerance() throws Exception {
        Statement st = connection.createStatement();

        // This first test shows that nodes within a tolerance of 0.05 of each
        // other are considered to be a single node.
        // Note, however, that edge geometries are left untouched.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 0)', 'road1'),"
                + "('LINESTRING (1.05 0, 2 0)', 'road2'),"
                + "('LINESTRING (2.05 0, 3 0)', 'road3'),"
                + "('LINESTRING (1 0.1, 1 1)', 'road4'),"
                + "('LINESTRING (2 0.05, 2 1)', 'road5');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
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
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");

        // This test shows that _coordinates_ within a given tolerance of each
        // other are not necessarily snapped together. Only the first and last
        // coordinates of a geometry are considered to be potential nodes, and
        // only _nodes_ within a given tolerance of each other are snapped
        // together.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 1, 1 1, 1 0)', 'road1'),"
                + "('LINESTRING (1.05 1, 2 1)', 'road2');");
        rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        edgesResult = st.executeQuery("SELECT * FROM test_edges");
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
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");

        // This test shows that geometry intersections are not automatically
        // considered to be potential nodes.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 1, 2 1)', 'road1'),"
                + "('LINESTRING (2 1, 1 0, 1 2)', 'road2');");
        rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.05)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        edgesResult = st.executeQuery("SELECT * FROM test_edges");
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
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }

    @Test
    public void test_ST_Graph_BigTolerance() throws Exception {
        Statement st = connection.createStatement();
        // This test shows that the results from using a large tolerance value
        // (3.1 rather than 0.1) can be very different.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('LINESTRING (0 0, 1 2)', 'road1'),"
                + "('LINESTRING (1 2, 2 3, 4 3)', 'road2'),"
                + "('LINESTRING (4 3, 4 4, 1 4, 1 2)', 'road3'),"
                + "('LINESTRING (4 3, 5 2)', 'road4'),"
                + "('LINESTRING (4.05 4.1, 7 5)', 'road5'),"
                + "('LINESTRING (7.1 5, 8 4)', 'road6');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test', 'road', 3.1, false)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
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

        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }

    @Test
    public void test_ST_Graph_OrientBySlope() throws Exception {
        Statement st = connection.createStatement();

        // This test proves that orientation by slope works. Three cases:
        // 1. first.z == last.z -- Orient first --> last
        // 2. first.z > last.z -- Orient first --> last
        // 3. first.z < last.z -- Orient last --> first

        // Case 1.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 0)', 'road1');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0 0, 1 0 0)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        // Orient first --> last
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");

        // Case 2.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 1, 1 0 0)', 'road1');");
        rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM test_nodes");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM test_edges");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0 1, 1 0 0)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        // Orient first --> last
        assertEquals(1, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(2, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");

        // Case 3.
        st.execute("CREATE TABLE test(road LINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES " +
                "('LINESTRING (0 0 0, 1 0 1)', 'road1');");
        rs = st.executeQuery("SELECT ST_Graph('test', 'road', 0.0, true)");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        nodesResult = st.executeQuery("SELECT * FROM test_nodes");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (0 0 0)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt(ST_Graph.NODE_ID));
        assertGeometryEquals("POINT (1 0 1)", nodesResult.getBytes(ST_Graph.THE_GEOM));
        assertFalse(nodesResult.next());
        edgesResult = st.executeQuery("SELECT * FROM test_edges");
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0 0, 1 0 1)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt(ST_Graph.EDGE_ID));
        // Orient last --> first
        assertEquals(2, edgesResult.getInt(ST_Graph.START_NODE));
        assertEquals(1, edgesResult.getInt(ST_Graph.END_NODE));
        assertFalse(edgesResult.next());
        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }

    @Test
    public void test_ST_Graph_ErrorWithNoLINESTRINGOrMULTILINESTRING() throws Exception {
        Statement st = connection.createStatement();

        // Prepare the input table.
        st.execute("CREATE TABLE test(road POINT, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('POINT (0 0)', 'road1');");

        ResultSet rs = st.executeQuery("SELECT ST_Graph('test')");
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
        assertFalse(rs.next());

        assertFalse(connection.getMetaData().getTables(null, null, "TEST_NODES", null).last());
        assertFalse(connection.getMetaData().getTables(null, null, "TEST_EDGES", null).last());

        st.execute("DROP TABLE test");
    }

    @Test
    public void test_ST_Graph_MULTILINESTRING() throws Exception {
        Statement st = connection.createStatement();

        // This test shows that the coordinate (1 2) is not considered to be
        // a node, even though it would be if we had used ST_Explode to split
        // the MULTILINESTRINGs into LINESTRINGs.
        // Note also that the last coordinate of the first LINESTRING of road2 (1 2)
        // is not equal to the first coordinate of the second LINESTRING of road2 (4 3),
        // so this MULTILINESTRING is considered to be an edge from node 2=(4 3) to
        // node 3=(5 2).
        st.execute("CREATE TABLE test(road MULTILINESTRING, description VARCHAR);" +
                "INSERT INTO test VALUES "
                + "('MULTILINESTRING ((0 0, 1 2), (1 2, 2 3, 4 3))', 'road1'),"
                + "('MULTILINESTRING ((4 3, 4 4, 1 4, 1 2), (4 3, 5 2))', 'road2');");
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
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
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
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

        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }

    @Test
    public void test_ST_ShortestPathLength_WDOneToOne() throws Exception {
        Statement st = connection.createStatement();
        checkWD(st, 1, 1, 0.0);
        checkWD(st, 1, 2, 8.0);
        checkWD(st, 1, 3, 9.0);
        checkWD(st, 1, 4, 5.0);
        checkWD(st, 1, 5, 7.0);
        checkWD(st, 2, 1, 11.0);
        checkWD(st, 2, 2, 0.0);
        checkWD(st, 2, 3, 1.0);
        checkWD(st, 2, 4, 2.0);
        checkWD(st, 2, 5, 4.0);
        checkWD(st, 3, 1, 11.0);
        checkWD(st, 3, 2, 19.0);
        checkWD(st, 3, 3, 0.0);
        checkWD(st, 3, 4, 16.0);
        checkWD(st, 3, 5, 4.0);
        checkWD(st, 4, 1, 9.0);
        checkWD(st, 4, 2, 3.0);
        checkWD(st, 4, 3, 4.0);
        checkWD(st, 4, 4, 0.0);
        checkWD(st, 4, 5, 2.0);
        checkWD(st, 5, 1, 7.0);
        checkWD(st, 5, 2, 15.0);
        checkWD(st, 5, 3, 6.0);
        checkWD(st, 5, 4, 12.0);
        checkWD(st, 5, 5, 0.0);
    }

    @Test
    public void test_ST_ShortestPathLength_DOneToOne() throws Exception {
        Statement st = connection.createStatement();
        checkD(st, 1, 1, 0.0);
        checkD(st, 1, 2, 1.0);
        checkD(st, 1, 3, 2.0);
        checkD(st, 1, 4, 1.0);
        checkD(st, 1, 5, 2.0);
        checkD(st, 2, 1, 3.0);
        checkD(st, 2, 2, 0.0);
        checkD(st, 2, 3, 1.0);
        checkD(st, 2, 4, 1.0);
        checkD(st, 2, 5, 2.0);
        checkD(st, 3, 1, 2.0);
        checkD(st, 3, 2, 3.0);
        checkD(st, 3, 3, 0.0);
        checkD(st, 3, 4, 3.0);
        checkD(st, 3, 5, 1.0);
        checkD(st, 4, 1, 2.0);
        checkD(st, 4, 2, 1.0);
        checkD(st, 4, 3, 1.0);
        checkD(st, 4, 4, 0.0);
        checkD(st, 4, 5, 1.0);
        checkD(st, 5, 1, 1.0);
        checkD(st, 5, 2, 2.0);
        checkD(st, 5, 3, 1.0);
        checkD(st, 5, 4, 2.0);
        checkD(st, 5, 5, 0.0);
    }

    @Test
    public void test_ST_ShortestPathLength_DOOneToOne() throws Exception {
        Statement st = connection.createStatement();
        checkDO(st, 1, 1, 0.0);
        checkDO(st, 1, 2, 1.0);
        checkDO(st, 1, 3, 2.0);
        checkDO(st, 1, 4, 1.0);
        checkDO(st, 1, 5, 1.0); // (1,5) now bidirectional
        checkDO(st, 2, 1, 3.0);
        checkDO(st, 2, 2, 0.0);
        checkDO(st, 2, 3, 2.0); // (2,3) reversed
        checkDO(st, 2, 4, 1.0);
        checkDO(st, 2, 5, 2.0);
        checkDO(st, 3, 1, 2.0);
        checkDO(st, 3, 2, 1.0); // (2,3) reversed
        checkDO(st, 3, 3, 0.0);
        checkDO(st, 3, 4, 2.0); // (2,3) reversed
        checkDO(st, 3, 5, 1.0);
        checkDO(st, 4, 1, 2.0);
        checkDO(st, 4, 2, 1.0);
        checkDO(st, 4, 3, 1.0);
        checkDO(st, 4, 4, 0.0);
        checkDO(st, 4, 5, 1.0);
        checkDO(st, 5, 1, 1.0);
        checkDO(st, 5, 2, 2.0);
        checkDO(st, 5, 3, 1.0);
        checkDO(st, 5, 4, 2.0);
        checkDO(st, 5, 5, 0.0);
    }

    private void check(String request, Statement st, int source, int destination, double distance) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + source + ", " + destination
                        + request + ")");
        assertTrue(rs.next());
        assertEquals(source, rs.getInt(ST_ShortestPathLength.SOURCE_INDEX));
        assertEquals(destination, rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX));
        assertEquals(distance, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
    }

    private void checkD(Statement st, int source, int destination, double distance) throws SQLException {
        check("", st, source, destination, distance);
    }

    private void checkDO(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'directed - edge_orientation'", st, source, destination, distance);
    }

    private void checkRO(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'reversed - edge_orientation'", st, source, destination, distance);
    }

    private void checkU(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'undirected'", st, source, destination, distance);
    }

    private void checkWD(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'weight'", st, source, destination, distance);
    }

    private void checkWDO(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'weight', 'directed - edge_orientation'", st, source, destination, distance);
        check(", 'directed - edge_orientation', 'weight'", st, source, destination, distance);
    }

    private void checkWRO(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'weight', 'reversed - edge_orientation'", st, source, destination, distance);
        check(", 'reversed - edge_orientation', 'weight'", st, source, destination, distance);
    }

    private void checkWU(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'weight', 'undirected'", st, source, destination, distance);
        check(", 'undirected', 'weight'", st, source, destination, distance);
    }
}
