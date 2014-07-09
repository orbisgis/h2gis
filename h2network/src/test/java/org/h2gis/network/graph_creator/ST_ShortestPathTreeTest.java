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
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static junit.framework.Assert.*;
import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;

/**
 * @author Adam Gouge
 */
public class ST_ShortestPathTreeTest {

    private static Connection connection;
    private Statement st;
    private static final double TOLERANCE = 0.0;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String W = "'weight'";
    private static final String CORMEN = "CORMEN_EDGES_ALL";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ShortestPathTreeTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPathTree(), "");
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
        // Edges
        // | 1        | 2             | 3      | 4              | 5     |
        // |----------|---------------|--------|----------------|-------|
        // | *        | (1)           | (5)    | (5,6), (-10,9) | (-10) |
        // | (3,7,10) | *             | (3)    | (3,6)          | (3,7) |
        // | (7,10)   | (4)           | *      | (6)            | (7)   |
        // | (8,10)   | (2)           | (2,3)  | *              | (8)   |
        // | (10)     | (10,1), (9,2) | (10,5) | (9)            | *     |
        check(oneToAll(CORMEN, DO, 1),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 1, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 1.0))
        );
        check(oneToAll(CORMEN, DO, 2),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
        check(oneToAll(CORMEN, DO, 3),
                new Tree()
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
        check(oneToAll(CORMEN, DO, 4),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
        check(oneToAll(CORMEN, DO, 5),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 1, 2, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
    }

    @Test
    public void WDO() throws SQLException {
        // Edges
        // | 1        | 2     | 3       | 4                | 5            |
        // |----------|-------|---------|------------------|--------------|
        // | *        | (5,4) | (5)     | (5,7,9), (-10,9) | (5,7), (-10) |
        // | (3,7,10) | *     | (3)     | (3,7,9)          | (3,7)        |
        // | (7,10)   | (4)   | *       | (7,9)            | (7)          |
        // | (8,10)   | (2)   | (2,3)   | *                | (8)          |
        // | (10)     | (9,2) | (9,2,3) | (9)              | *            |
        check(oneToAll(CORMEN, DO, W, 1),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 7.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 3.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 6.0))
        );
        check(oneToAll(CORMEN, DO, W, 2),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 6.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
        );
        check(oneToAll(CORMEN, DO, W, 3),
                new Tree()
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 3.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 6.0))
        );
        check(oneToAll(CORMEN, DO, W, 4),
                new Tree()
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 4.0))
        );
        check(oneToAll(CORMEN, DO, W, 5),
                new Tree()
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 6.0))
        );
    }

    @Test
    public void oneToOneRO() throws Exception {
        // Edges
        // | 1              | 2        | 3      | 4      | 5             |
        // |----------------|----------|--------|--------|---------------|
        // | *              | (10,7,3) | (10,7) | (10,8) | (10)          |
        // | (1)            | *        | (4)    | (2)    | (1,10), (2,9) |
        // | (5)            | (3)      | *      | (3,2)  | (5,10)        |
        // | (6,5), (9,-10) | (6,3)    | (6)    | *      | (9)           |
        // | (-10)          | (7,3)    | (7)    | (8)    | *             |
        check(oneToAll(CORMEN, RO, 1),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, RO, 2),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 2, 1, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 2, 3, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, RO, 3),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, RO, 4),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 4, 3, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 1.0))
        );
        check(oneToAll(CORMEN, RO, 5),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 1.0))
        );
    }

    private ResultSet oneToAll(String table, String orientation, int source) throws SQLException {
        return oneToAll(table, orientation, null, source, Double.POSITIVE_INFINITY);
    }

    private ResultSet oneToAll(String table, String orientation, int source, double radius) throws SQLException {
        return oneToAll(table, orientation, null, source, radius);
    }

    private ResultSet oneToAll(String table, String orientation, String weight, int source) throws SQLException {
        return oneToAll(table, orientation, weight, source, Double.POSITIVE_INFINITY);
    }

    private ResultSet oneToAll(String table, String orientation, String weight,
                               int source, double radius) throws SQLException {
        return st.executeQuery(
                "SELECT * FROM ST_ShortestPathTree('" + table + "', "
                        + orientation
                        + ((weight != null) ? ", " + weight : "") + ", "
                        + source
                        + ((radius < Double.POSITIVE_INFINITY) ? ", " + radius : "") + ")"
        );
    }

    private void check(ResultSet rs, Tree tree) throws SQLException {
        int count = 0;
        while (rs.next()) {
            count++;
            // Here we check the edge id, but we never check the tree id, as it could vary.
            TreeEdge e = tree.get(rs.getInt(ST_ShortestPathTree.EDGE_ID_INDEX));
            assertGeometryEquals(e.getGeom(), rs.getBytes(ST_ShortestPathTree.GEOM_INDEX));
            assertEquals(e.getSource(), rs.getInt(ST_ShortestPathTree.SOURCE_INDEX));
            assertEquals(e.getDestination(), rs.getInt(ST_ShortestPathTree.DESTINATION_INDEX));
            assertEquals(e.getWeight(), rs.getDouble(ST_ShortestPathTree.WEIGHT_INDEX), TOLERANCE);
        }
        assertEquals(count, tree.size());
        rs.close();
    }

    private class Tree extends HashMap<Integer, TreeEdge> {
        public Tree add(Integer i, TreeEdge e) {
            super.put(i, e);
            return this;
        }
    }

    private class TreeEdge {
        private String geom;
        private int source;
        private int destination;
        private double weight;

        public TreeEdge(String geom, int source, int destination, double weight) {
            this.geom = geom;
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        public String getGeom() {
            return geom;
        }

        public int getSource() {
            return source;
        }

        public int getDestination() {
            return destination;
        }

        public double getWeight() {
            return weight;
        }
    }
}
