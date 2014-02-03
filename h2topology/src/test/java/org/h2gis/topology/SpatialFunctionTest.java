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
package org.h2gis.topology;

import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.topology.graph_creator.ST_Graph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    private static final String DB_NAME = "SpatialFunctionTest";
    public static final double TOLERANCE = 10E-10;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
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
                + "('LINESTRING (4 3, 5 2)', 'road4');");

        // Make sure everything went OK.
        ResultSet rs = st.executeQuery("SELECT ST_Graph('test')");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.next());

        // Test nodes table.
        ResultSet nodesResult = st.executeQuery("SELECT * FROM test_nodes");
        assertEquals(2, nodesResult.getMetaData().getColumnCount());
        assertTrue(nodesResult.next());
        assertEquals(1, nodesResult.getInt("node_id"));
        assertGeometryEquals("POINT (0 0)", nodesResult.getBytes("the_geom"));
        assertTrue(nodesResult.next());
        assertEquals(2, nodesResult.getInt("node_id"));
        assertGeometryEquals("POINT (1 2)", nodesResult.getBytes("the_geom"));
        assertTrue(nodesResult.next());
        assertEquals(3, nodesResult.getInt("node_id"));
        assertGeometryEquals("POINT (4 3)", nodesResult.getBytes("the_geom"));
        assertTrue(nodesResult.next());
        assertEquals(4, nodesResult.getInt("node_id"));
        assertGeometryEquals("POINT (5 2)", nodesResult.getBytes("the_geom"));
        assertFalse(nodesResult.next());

        // Test edges table.
        ResultSet edgesResult = st.executeQuery("SELECT * FROM test_edges");
        // This is a copy of the original table with three columns added.
        assertEquals(2 + 3, edgesResult.getMetaData().getColumnCount());
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (0 0, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road1", edgesResult.getString("description"));
        assertEquals(1, edgesResult.getInt("edge_id"));
        assertEquals(1, edgesResult.getInt("start_node"));
        assertEquals(2, edgesResult.getInt("end_node"));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (1 2, 2 3, 4 3)", edgesResult.getBytes("road"));
        assertEquals("road2", edgesResult.getString("description"));
        assertEquals(2, edgesResult.getInt("edge_id"));
        assertEquals(2, edgesResult.getInt("start_node"));
        assertEquals(3, edgesResult.getInt("end_node"));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 4 4, 1 4, 1 2)", edgesResult.getBytes("road"));
        assertEquals("road3", edgesResult.getString("description"));
        assertEquals(3, edgesResult.getInt("edge_id"));
        assertEquals(3, edgesResult.getInt("start_node"));
        assertEquals(2, edgesResult.getInt("end_node"));
        assertTrue(edgesResult.next());
        assertGeometryEquals("LINESTRING (4 3, 5 2)", edgesResult.getBytes("road"));
        assertEquals("road4", edgesResult.getString("description"));
        assertEquals(4, edgesResult.getInt("edge_id"));
        assertEquals(3, edgesResult.getInt("start_node"));
        assertEquals(4, edgesResult.getInt("end_node"));
        assertFalse(edgesResult.next());

        st.execute("DROP TABLE test");
        st.execute("DROP TABLE test_nodes");
        st.execute("DROP TABLE test_edges");
    }
}
