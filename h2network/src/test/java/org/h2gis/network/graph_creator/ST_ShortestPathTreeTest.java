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

import org.h2.jdbc.JdbcSQLException;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GraphConstants;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
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
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 1)
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
    public void DOlimitBy1point1() throws Exception {
        // Edges
        // | 1        | 2             | 3      | 4              | 5     |
        // |----------|---------------|--------|----------------|-------|
        // | *        | (1)           | (5)    | (5,6), (-10,9) | (-10) |
        // | (3,7,10) | *             | (3)    | (3,6)          | (3,7) |
        // | (7,10)   | (4)           | *      | (6)            | (7)   |
        // | (8,10)   | (2)           | (2,3)  | *              | (8)   |
        // | (10)     | (10,1), (9,2) | (10,5) | (9)            | *     |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 1, 1.1)
        check(oneToAll(CORMEN, DO, 1, 1.1),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 1, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 1.0))
        );
        check(oneToAll(CORMEN, DO, 2, 1.1),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 1.0))
        );
        check(oneToAll(CORMEN, DO, 3, 1.1),
                new Tree()
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 1.0))
        );
        check(oneToAll(CORMEN, DO, 4, 1.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 1.0))
        );
        check(oneToAll(CORMEN, DO, 5, 1.1),
                new Tree()
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
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight, 1)
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
    public void WDOlimitedBy6point1() throws SQLException {
        // Edges
        // | 1        | 2     | 3       | 4                | 5            |
        // |----------|-------|---------|------------------|--------------|
        // | *        | (5,4) | (5)     | (5,7,9), (-10,9) | (5,7), (-10) |
        // | (3,7,10) | *     | (3)     | (3,7,9)          | (3,7)        |
        // | (7,10)   | (4)   | *       | (7,9)            | (7)          |
        // | (8,10)   | (2)   | (2,3)   | *                | (8)          |
        // | (10)     | (9,2) | (9,2,3) | (9)              | *            |
        // Distances:
        // {0.0, 8.0, 5.0, 13.0, 7.0}
        // {11.0, 0.0, 2.0, 10.0, 4.0}
        // {9.0, 3.0, 0.0, 8.0, 2.0}
        // {11.0, 1.0, 3.0, 0.0, 4.0}
        // {7.0, 7.0, 9.0, 6.0, 0.0}
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight, 1, 6.1)
        check(oneToAll(CORMEN, DO, W, 1, 6.1),
                new Tree()
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 5.0))
        );
        check(oneToAll(CORMEN, DO, W, 2, 6.1),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
        );
        check(oneToAll(CORMEN, DO, W, 3, 6.1),
                new Tree()
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 3.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
        );
        check(oneToAll(CORMEN, DO, W, 4, 6.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 4.0))
        );
        check(oneToAll(CORMEN, DO, W, 5, 6.1),
                new Tree()
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 6.0))
        );
    }

    @Test
    public void RO() throws Exception {
        // Edges
        // | 1              | 2        | 3      | 4      | 5             |
        // |----------------|----------|--------|--------|---------------|
        // | *              | (10,7,3) | (10,7) | (10,8) | (10)          |
        // | (1)            | *        | (4)    | (2)    | (1,10), (2,9) |
        // | (5)            | (3)      | *      | (3,2)  | (5,10)        |
        // | (6,5), (9,-10) | (6,3)    | (6)    | *      | (9)           |
        // | (-10)          | (7,3)    | (7)    | (8)    | *             |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 1)
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

    @Test
    public void ROlimitedBy1point1() throws Exception {
        // Edges
        // | 1              | 2        | 3      | 4      | 5             |
        // |----------------|----------|--------|--------|---------------|
        // | *              | (10,7,3) | (10,7) | (10,8) | (10)          |
        // | (1)            | *        | (4)    | (2)    | (1,10), (2,9) |
        // | (5)            | (3)      | *      | (3,2)  | (5,10)        |
        // | (6,5), (9,-10) | (6,3)    | (6)    | *      | (9)           |
        // | (-10)          | (7,3)    | (7)    | (8)    | *             |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 1, 1.1)
        check(oneToAll(CORMEN, RO, 1, 1.1),
                new Tree()
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, RO, 2, 1.1),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 2, 1, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 2, 3, 1.0))
        );
        check(oneToAll(CORMEN, RO, 3, 1.1),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 1.0))
        );
        check(oneToAll(CORMEN, RO, 4, 1.1),
                new Tree()
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 4, 3, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 1.0))
        );
        check(oneToAll(CORMEN, RO, 5, 1.1),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 1.0))
        );
    }

    @Test
    public void WRO() throws Exception {
        // Edges
        // | 1                | 2        | 3      | 4      | 5       |
        // |------------------|----------|--------|--------|---------|
        // | *                | (10,7,3) | (10,7) | (10,8) | (10)    |
        // | (4,5)            | *        | (4)    | (2)    | (2,9)   |
        // | (5)              | (3)      | *      | (3,2)  | (3,2,9) |
        // | (9,-10), (9,7,5) | (9,7,3)  | (9,7)  | *      | (9)     |
        // | (-10), (7,5)     | (7,3)    | (7)    | (8)    | *       |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', 1)
        check(oneToAll(CORMEN, RO, W, 1),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 4.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 7.0))
        );
        check(oneToAll(CORMEN, RO, W, 2),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 2, 3, 3.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 6.0))
        );
        check(oneToAll(CORMEN, RO, W, 3),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 6.0))
        );
        check(oneToAll(CORMEN, RO, W, 4),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 2.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 6.0))
        );
        check(oneToAll(CORMEN, RO, W, 5),
                new Tree()
                        .add(-10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 4.0))
        );
    }

    @Test
    public void WROlimitedBy6point1() throws Exception {
        // Edges
        // | 1                | 2        | 3      | 4      | 5       |
        // |------------------|----------|--------|--------|---------|
        // | *                | (10,7,3) | (10,7) | (10,8) | (10)    |
        // | (4,5)            | *        | (4)    | (2)    | (2,9)   |
        // | (5)              | (3)      | *      | (3,2)  | (3,2,9) |
        // | (9,-10), (9,7,5) | (9,7,3)  | (9,7)  | *      | (9)     |
        // | (-10), (7,5)     | (7,3)    | (7)    | (8)    | *       |
        // Distances:
        // {0.0, 11.0, 9.0, 11.0, 7.0}
        // {8.0, 0.0, 3.0, 1.0, 7.0}
        // {5.0, 2.0, 0.0, 3.0, 9.0}
        // {13.0, 10.0, 8.0, 0.0, 6.0}
        // {7.0, 4.0, 2.0, 4.0, 0.0}
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', 1, 6.1)
        check(oneToAll(CORMEN, RO, W, 1, 6.1),
                new Tree()
        );
        check(oneToAll(CORMEN, RO, W, 2, 6.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 2, 3, 3.0))
        );
        check(oneToAll(CORMEN, RO, W, 3, 6.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
        );
        check(oneToAll(CORMEN, RO, W, 4, 6.1),
                new Tree()
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 6.0))
        );
        // We might expect edge 3 to be included in the SPT but it is not.
        // This is because when we limit Dijkstra by a radius of 6.1,
        // it stops after its first iteration after finding vertices
        // 3 (distance 2.0), 4 (distance 4.0) and 1 (distance 7.0)
        // since 7.0 > 6.1.
        // TODO: Rethink how we limit Dijkstra to give the expected result?
        check(oneToAll(CORMEN, RO, W, 5, 6.1),
                new Tree()
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 4.0))
        );
    }

    @Test
    public void U() throws Exception {
        // Edges
        // | 1                            | 2                                  |
        // |------------------------------|------------------------------------|
        // | *                            | (1)                                |
        // | (1)                          | *                                  |
        // | (5)                          | (3), (4)                           |
        // | (2,1), (6,5), (8,10), (9,10) | (2)                                |
        // | (10)                         | (10,1), (7,3), (7,4), (8,2), (9,2) |
        //
        // | 3        | 4                            | 5                                  |
        // |----------|------------------------------|------------------------------------|
        // | (5)      | (1,2), (5,6), (10,8), (10,9) | (10)                               |
        // | (3), (4) | (2)                          | (1,10), (3,7), (4,7), (2,8), (2,9) |
        // | *        | (6)                          | (7)                                |
        // | (6)      | *                            | (8), (9)                           |
        // | (7)      | (8), (9)                     | *                                  |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'undirected', 1)
        check(oneToAll(CORMEN, U, 1),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 1, 2, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, U, 2),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 2, 1, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 2, 3, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, U, 3),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 1.0))
        );
        check(oneToAll(CORMEN, U, 4),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 2, 1, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 4, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
        check(oneToAll(CORMEN, U, 5),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 1, 2, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
    }

    @Test
    public void UlimitedBy1point1() throws Exception {
        // Edges
        // | 1                            | 2                                  |
        // |------------------------------|------------------------------------|
        // | *                            | (1)                                |
        // | (1)                          | *                                  |
        // | (5)                          | (3), (4)                           |
        // | (2,1), (6,5), (8,10), (9,10) | (2)                                |
        // | (10)                         | (10,1), (7,3), (7,4), (8,2), (9,2) |
        //
        // | 3        | 4                            | 5                                  |
        // |----------|------------------------------|------------------------------------|
        // | (5)      | (1,2), (5,6), (10,8), (10,9) | (10)                               |
        // | (3), (4) | (2)                          | (1,10), (3,7), (4,7), (2,8), (2,9) |
        // | *        | (6)                          | (7)                                |
        // | (6)      | *                            | (8), (9)                           |
        // | (7)      | (8), (9)                     | *                                  |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'undirected', 1, 1.1)
        check(oneToAll(CORMEN, U, 1, 1.1),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 1, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 1.0))
        );
        check(oneToAll(CORMEN, U, 2, 1.1),
                new Tree()
                        .add(1, new TreeEdge("LINESTRING (0 1, 1 2)", 2, 1, 1.0))
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 2, 3, 1.0))
        );
        check(oneToAll(CORMEN, U, 3, 1.1),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1.0))
                        .add(4, new TreeEdge("LINESTRING (1 0, 1.25 1, 1 2)", 3, 2, 1.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 3, 4, 1.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 1.0))
        );
        check(oneToAll(CORMEN, U, 4, 1.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(6, new TreeEdge("LINESTRING (1 0, 2 2)", 4, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 4, 5, 1.0))
        );
        check(oneToAll(CORMEN, U, 5, 1.1),
                new Tree()
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 1.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 1.0))
                        .add(9, new TreeEdge("LINESTRING (2 0, 2.25 1, 2 2)", 5, 4, 1.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 1.0))
        );
    }

    @Test
    public void WU() throws Exception {
        // Edges
        // | 1           | 2     | 3     | 4       | 5           |
        // |-------------|-------|-------|---------|-------------|
        // | *           | (5,3) | (5)   | (5,3,2) | (10), (5,7) |
        // | (3,5)       | *     | (3)   | (2)     | (3,7)       |
        // | (5)         | (3)   | *     | (3,2)   | (7)         |
        // | (2,3,5)     | (2)   | (2,3) | *       | (8)         |
        // | (10), (7,5) | (7,3) | (7)   | (8)     | *           |
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', 1)
        check(oneToAll(CORMEN, U, W, 1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 1, 5, 7.0))
        );
        check(oneToAll(CORMEN, U, W, 2),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
        );
        check(oneToAll(CORMEN, U, W, 3),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
        );
        check(oneToAll(CORMEN, U, W, 4),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 4.0))
        );
        check(oneToAll(CORMEN, U, W, 5),
                new Tree()
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 4.0))
                        .add(10, new TreeEdge("LINESTRING (2 0, 0 1)", 5, 1, 7.0))
        );
    }

    @Test
    public void WUlimitedBy6point1() throws Exception {
        // Edges
        // | 1           | 2     | 3     | 4       | 5           |
        // |-------------|-------|-------|---------|-------------|
        // | *           | (5,3) | (5)   | (5,3,2) | (10), (5,7) |
        // | (3,5)       | *     | (3)   | (2)     | (3,7)       |
        // | (5)         | (3)   | *     | (3,2)   | (7)         |
        // | (2,3,5)     | (2)   | (2,3) | *       | (8)         |
        // | (10), (7,5) | (7,3) | (7)   | (8)     | *           |
        // Distances:
        // {0.0, 7.0, 5.0, 8.0, 7.0}
        // {7.0, 0.0, 2.0, 1.0, 4.0}
        // {5.0, 2.0, 0.0, 3.0, 2.0}
        // {8.0, 1.0, 3.0, 0.0, 4.0}
        // {7.0, 4.0, 2.0, 4.0, 0.0}
        //
        // SELECT * FROM ST_ShortestPathTree('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', 1, 6.1)
        check(oneToAll(CORMEN, U, W, 1, 6.1),
                new Tree()
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 1, 3, 5.0))
        );
        check(oneToAll(CORMEN, U, W, 2, 6.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
        );
        check(oneToAll(CORMEN, U, W, 3, 6.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 2, 4, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 2.0))
                        .add(5, new TreeEdge("LINESTRING (0 1, 1 0)", 3, 1, 5.0))
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 3, 5, 2.0))
        );
        check(oneToAll(CORMEN, U, W, 4, 6.1),
                new Tree()
                        .add(2, new TreeEdge("LINESTRING (1 2, 2 2)", 4, 2, 1.0))
                        .add(3, new TreeEdge("LINESTRING (1 2, 0.75 1, 1 0)", 2, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 4, 5, 4.0))
        );
        // Note: Edge 3 is not included here for the same reason as in
        // {@link #WROlimitedBy6point1}.
        check(oneToAll(CORMEN, U, W, 5, 6.1),
                new Tree()
                        .add(7, new TreeEdge("LINESTRING (1 0, 2 0)", 5, 3, 2.0))
                        .add(8, new TreeEdge("LINESTRING (2 2, 1.75 1, 2 0)", 5, 4, 4.0))
        );
    }

    @Test
    public void WDODisconnectedGraph() throws SQLException {
        // This test shows that when ST_ShortestPathTree is called on a graph
        // that is not connected, it will create the largest SPT it can while
        // staying in the same (strongly) connected component as the source
        // vertex.
        check(oneToAll("COPY_EDGES_ALL", DO, W, 6),
                new Tree()
                        .add(11, new TreeEdge("LINESTRING (3 1, 4 2)", 6, 7, 1.0))
                        .add(12, new TreeEdge("LINESTRING (4 2, 5 2)", 7, 8, 2.0))
        );
    }

    @Test
    public void WDODisconnectedGraphLimitedBy1point1() throws SQLException {
        check(oneToAll("COPY_EDGES_ALL", DO, W, 6, 1.1),
                new Tree()
                        .add(11, new TreeEdge("LINESTRING (3 1, 4 2)", 6, 7, 1.0))
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void unrecognizedArg4() throws Throwable {
        // The source vertex must be given as an INT and not a DOUBLE.
        try {
            st.executeQuery(
                "SELECT * FROM ST_ShortestPathTree('COPY_EDGES_ALL', " +
                        DO + ", 1.0, 1)");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains(GraphFunction.ARG_ERROR + "1.0"));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unrecognizedArg5() throws Throwable {
        // The radius must be given as a DOUBLE and not an INT.
        try {
            st.executeQuery(
                "SELECT * FROM ST_ShortestPathTree('COPY_EDGES_ALL', " +
                        DO + ", 1, 5)");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains(GraphFunction.ARG_ERROR + "5"));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void otherUnrecognizedArg5() throws Throwable {
        // The source vertex must be given as an INT and not a DOUBLE.
        try {
            st.executeQuery(
                "SELECT * FROM ST_ShortestPathTree('COPY_EDGES_ALL', " +
                        DO + ", " + W + ", 1.0)");
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains(GraphFunction.ARG_ERROR + "1.0"));
            throw e.getOriginalCause();
        }
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

    @Test
    public void testNoGeometryField() throws Throwable {
        // This test shows that if the input has no geometry column,
        // neither will the output.
        st.execute("DROP TABLE IF EXISTS NO_GEOM;" +
                "CREATE TABLE NO_GEOM AS " +
                "SELECT EDGE_ID, START_NODE, END_NODE, WEIGHT " +
                "FROM CORMEN_EDGES_ALL;");
        final ResultSet resultSet = st.executeQuery("SELECT * FROM ST_ShortestPathTree(" +
                "'NO_GEOM', 'UNDIRECTED', 3);");
        checkNoGeom(resultSet,
                new Tree()
                        .add(3, new TreeEdge(3, 2, 1.0))
                        .add(4, new TreeEdge(3, 2, 1.0))
                        .add(5, new TreeEdge(3, 1, 1.0))
                        .add(6, new TreeEdge(3, 4, 1.0))
                        .add(7, new TreeEdge(3, 5, 1.0))
        );
    }

    private void check(ResultSet rs, Tree tree) throws SQLException {
        check(rs, tree, true);
    }

    private void checkNoGeom(ResultSet rs, Tree tree) throws SQLException {
        check(rs, tree, false);
    }

    private void check(ResultSet rs, Tree tree, boolean checkGeom) throws SQLException {
        int count = 0;
        while (rs.next()) {
            count++;
            TreeEdge e = tree.get(rs.getInt(GraphConstants.EDGE_ID));
            if (checkGeom) {
                assertGeometryEquals(e.getGeom(), rs.getBytes(GraphConstants.THE_GEOM));
            }
            assertEquals(e.getSource(), rs.getInt(GraphConstants.SOURCE));
            assertEquals(e.getDestination(), rs.getInt(GraphConstants.DESTINATION));
            assertEquals(e.getWeight(), rs.getDouble(GraphConstants.WEIGHT), TOLERANCE);
        }
        assertEquals(tree.size(), count);
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

        public TreeEdge(int source, int destination, double weight) {
            this(null, source, destination, weight);
        }

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
