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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Adam Gouge
 */
public class ST_ShortestPathTest {

    private static Connection connection;
    private Statement st;
    private static final double TOLERANCE = 0.0;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String W = "'weight'";
    private static final PathEdge[] EMPTY = new PathEdge[]{};

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ShortestPathTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPath(), "");
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

    // ************************** One-to-One ****************************************

    @Test
    public void oneToOneDO() throws Exception {
        // Nodes
        // | 1         | 2                | 3       | 4                | 5       |
        // |-----------|------------------|---------|------------------|---------|
        // | *         | (1,2)            | (1,3)   | (1,3,4), (1,5,4) | (1,5)   |
        // | (2,3,5,1) | *                | (2,3)   | (2,3,4)          | (2,3,5) |
        // | (3,5,1)   | (3,2)            | *       | (3,4)            | (3,5)   |
        // | (4,5,1)   | (4,2)            | (4,2,3) | *                | (4,5)   |
        // | (5,1)     | (5,1,2), (5,4,2) | (5,1,3) | (5,4)            | *       |
        //
        // Edges
        // | 1        | 2             | 3      | 4              | 5     |
        // |----------|---------------|--------|----------------|-------|
        // | *        | (1)           | (5)    | (5,6), (-10,9) | (-10) |
        // | (3,7,10) | *             | (3)    | (3,6)          | (3,7) |
        // | (7,10)   | (4)           | *      | (6)            | (7)   |
        // | (8,10)   | (2)           | (2,3)  | *              | (8)   |
        // | (10)     | (10,1), (9,2) | (10,5) | (9)            | *     |
        //
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', i, j)
        check(oneToOne(DO, 1, 1), EMPTY);
        check(oneToOne(DO, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0)});
        check(oneToOne(DO, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 3, 1.0)});
        final ResultSet rs14 = oneToOne(DO, 1, 4);
        try {
            check(rs14, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 3, 4, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 3, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 5, 4, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 2, 1, 5, 1.0)});
        } catch (AssertionError e) {
            rs14.beforeFirst();
            check(rs14, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 2, 1, 5, 1.0),
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 1, 3, 4, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 3, 1.0)});
        }
        rs14.close();
        check(oneToOne(DO, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 1, 5, 1.0)});
        check(oneToOne(DO, 2, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 1.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 2, 3, 1.0)});
        check(oneToOne(DO, 2, 2), EMPTY);
        check(oneToOne(DO, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 1.0)});
        check(oneToOne(DO, 2, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 3, 4, 1.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 3, 1.0)});
        check(oneToOne(DO, 2, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 1.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 3, 1.0)});
        check(oneToOne(DO, 3, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 1.0)});
        check(oneToOne(DO, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 3, 2, 1.0)});
        check(oneToOne(DO, 3, 3), EMPTY);
        check(oneToOne(DO, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 3, 4, 1.0)});
        check(oneToOne(DO, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 1.0)});
        check(oneToOne(DO, 4, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0),
                        new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 2, 4, 5, 1.0)});
        check(oneToOne(DO, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 4, 2, 1.0)});
        check(oneToOne(DO, 4, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 1.0),
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 4, 2, 1.0)});
        check(oneToOne(DO, 4, 4), EMPTY);
        check(oneToOne(DO, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 4, 5, 1.0)});
        check(oneToOne(DO, 5, 1), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0)});
        final ResultSet rs52 = oneToOne(DO, 5, 2);
        try {
            check(rs52, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 5, 1, 1.0),
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 2, 1, 4, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 2, 5, 4, 1.0)});
        } catch (AssertionError e) {
            rs52.beforeFirst();
            check(rs52, new PathEdge[]{
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 4, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 5, 4, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 2, 1, 1, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 2, 5, 1, 1.0)});
        }
        rs52.close();
        check(oneToOne(DO, 5, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 3, 1.0),
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 5, 1, 1.0)});
        check(oneToOne(DO, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 1.0)});
        check(oneToOne(DO, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneWDO() throws Exception {
        // Nodes
        // | 1         | 2       | 3         | 4                  | 5              |
        // |-----------|---------|-----------|--------------------|----------------|
        // | *         | (1,3,2) | (1,3)     | (1,3,5,4), (1,5,4) | (1,5), (1,3,5) |
        // | (2,3,5,1) | *       | (2,3)     | (2,3,5,4)          | (2,3,5)        |
        // | (3,5,1)   | (3,2)   | *         | (3,5,4)            | (3,5)          |
        // | (4,5,1)   | (4,2)   | (4,2,3)   | *                  | (4,5)          |
        // | (5,1)     | (5,4,2) | (5,4,2,3) | (5,4)              | *              |
        //
        // Edges
        // | 1        | 2     | 3       | 4                | 5            |
        // |----------|-------|---------|------------------|--------------|
        // | *        | (5,4) | (5)     | (5,7,9), (-10,9) | (5,7), (-10) |
        // | (3,7,10) | *     | (3)     | (3,7,9)          | (3,7)        |
        // | (7,10)   | (4)   | *       | (7,9)            | (7)          |
        // | (8,10)   | (2)   | (2,3)   | *                | (8)          |
        // | (10)     | (9,2) | (9,2,3) | (9)              | *            |
        //
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES_ALL',
        //     'directed - edge_orientation', 'weight', i, j)
        check(oneToOne(DO, W, 1, 1), EMPTY);
        check(oneToOne(DO, W, 1, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 3, 2, 3.0),
                        new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 3, 5.0)});
        check(oneToOne(DO, W, 1, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 3, 5.0)});
        final ResultSet rs14 = oneToOne(DO, W, 1, 4);
        try {
            check(rs14, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 6.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 3, 1, 3, 5.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 2, 1, 5, 7.0)});
        } catch (AssertionError e) {
            rs14.beforeFirst();
            check(rs14, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 6.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 2, 1, 5, 7.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 3, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 3, 1, 3, 5.0)});
        }
        rs14.close();
        final ResultSet rs15 = oneToOne(DO, W, 1, 5);
        try {
            check(rs15, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 1, 5, 7.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 1, 3, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 3, 5.0)});
        } catch (AssertionError e) {
            rs15.beforeFirst();
            check(rs15, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 3, 5.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 1, 5, 7.0)});
        }
        rs15.close();
        check(oneToOne(DO, W, 2, 1),
                new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 2.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 2, 3, 2.0)});
        check(oneToOne(DO, W, 2, 2), EMPTY);
        check(oneToOne(DO, W, 2, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 2.0)});
        check(oneToOne(DO, W, 2, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 6.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 2.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 2, 3, 2.0)});
        check(oneToOne(DO, W, 2, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 2.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 3, 2.0)});
        check(oneToOne(DO, W, 3, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 2.0)});
        check(oneToOne(DO, W, 3, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 3, 2, 3.0)});
        check(oneToOne(DO, W, 3, 3), EMPTY);
        check(oneToOne(DO, W, 3, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 6.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 3, 5, 2.0)});
        check(oneToOne(DO, W, 3, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 2.0)});
        check(oneToOne(DO, W, 4, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                        new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 2, 4, 5, 4.0)});
        check(oneToOne(DO, W, 4, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 4, 2, 1.0)});
        check(oneToOne(DO, W, 4, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 2.0),
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 4, 2, 1.0)});
        check(oneToOne(DO, W, 4, 4), EMPTY);
        check(oneToOne(DO, W, 4, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 4, 5, 4.0)});
        check(oneToOne(DO, W, 5, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0)});
        check(oneToOne(DO, W, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 4, 2, 1.0),
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 5, 4, 6.0)});
        check(oneToOne(DO, W, 5, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 6.0)});
        check(oneToOne(DO, W, 5, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 2.0),
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 4, 2, 1.0),
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 3, 5, 4, 6.0)});
        check(oneToOne(DO, W, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneRO() throws Exception {
        // Nodes
        // | 1                | 2         | 3       | 4       | 5                |
        // |------------------|-----------|---------|---------|------------------|
        // | *                | (1,5,3,2) | (1,5,3) | (1,5,4) | (1,5)            |
        // | (2,1)            | *         | (2,3)   | (2,4)   | (2,1,5), (2,4,5) |
        // | (3,1)            | (3,2)     | *       | (3,2,4) | (3,1,5)          |
        // | (4,3,1), (4,5,1) | (4,3,2)   | (4,3)   | *       | (4,5)            |
        // | (5,1)            | (5,3,2)   | (5,3)   | (5,4)   | *                |
        //
        // Edges
        // | 1              | 2        | 3      | 4      | 5             |
        // |----------------|----------|--------|--------|---------------|
        // | *              | (10,7,3) | (10,7) | (10,8) | (10)          |
        // | (1)            | *        | (4)    | (2)    | (1,10), (2,9) |
        // | (5)            | (3)      | *      | (3,2)  | (5,10)        |
        // | (6,5), (9,-10) | (6,3)    | (6)    | *      | (9)           |
        // | (-10)          | (7,3)    | (7)    | (8)    | *             |
        //
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', i, j)
        check(oneToOne(RO, 1, 1), EMPTY);
        check(oneToOne(RO, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 1.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 1.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 3, 1, 5, 1.0)});
        check(oneToOne(RO, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 1.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 1.0)});
        check(oneToOne(RO, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 1.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 1.0)});
        check(oneToOne(RO, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0)});
        check(oneToOne(RO, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 2, 1, 1.0)});
        check(oneToOne(RO, 2, 2), EMPTY);
        check(oneToOne(RO, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 2, 3, 1.0)});
        check(oneToOne(RO, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0)});
        final ResultSet rs25 = oneToOne(RO, 2, 5);
        try {
            check(rs25, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 4, 5, 1.0),
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 2, 4, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 1, 1, 5, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 2, 2, 2, 1, 1.0)});
        } catch (AssertionError e) {
            rs25.beforeFirst();
            check(rs25, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 2, 2, 1, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 4, 5, 1.0),
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 2, 2, 2, 4, 1.0)});
        }
        rs25.close();
        check(oneToOne(RO, 3, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 1.0)});
        check(oneToOne(RO, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 1.0)});
        check(oneToOne(RO, 3, 3), EMPTY);
        check(oneToOne(RO, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 3, 2, 1.0)});
        check(oneToOne(RO, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 3, 1, 1.0)});
        final ResultSet rs41 = oneToOne(RO, 4, 1);
        try {
            check(rs41, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 4, 5, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 3, 1, 1.0),
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 2, 4, 3, 1.0)});
        } catch (AssertionError e) {
            rs41.beforeFirst();
            check(rs41, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 1.0),
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 2, 4, 3, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 5, 1, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 2, 4, 5, 1.0)});
        }
        rs41.close();
        check(oneToOne(RO, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 1.0),
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 2, 4, 3, 1.0)});
        check(oneToOne(RO, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 4, 3, 1.0)});
        check(oneToOne(RO, 4, 4), EMPTY);
        check(oneToOne(RO, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 4, 5, 1.0)});
        check(oneToOne(RO, 5, 1), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 1.0)});
        check(oneToOne(RO, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 1.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 1.0)});
        check(oneToOne(RO, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 1.0)});
        check(oneToOne(RO, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 1.0)});
        check(oneToOne(RO, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneWRO() throws Exception {
        // Nodes
        // | 1                  | 2         | 3       | 4       | 5         |
        // |--------------------|-----------|---------|---------|-----------|
        // | *                  | (1,5,3,2) | (1,5,3) | (1,5,4) | (1,5)     |
        // | (2,3,1)            | *         | (2,3)   | (2,4)   | (2,4,5)   |
        // | (3,1)              | (3,2)     | *       | (3,2,4) | (3,2,4,5) |
        // | (4,5,1), (4,5,3,1) | (4,5,3,2) | (4,5,3) | *       | (4,5)     |
        // | (5,1), (5,3,1)     | (5,3,2)   | (5,3)   | (5,4)   | *         |
        //
        // Edges
        // | 1                | 2        | 3      | 4      | 5       |
        // |------------------|----------|--------|--------|---------|
        // | *                | (10,7,3) | (10,7) | (10,8) | (10)    |
        // | (4,5)            | *        | (4)    | (2)    | (2,9)   |
        // | (5)              | (3)      | *      | (3,2)  | (3,2,9) |
        // | (9,-10), (9,7,5) | (9,7,3)  | (9,7)  | *      | (9)     |
        // | (-10), (7,5)     | (7,3)    | (7)    | (8)    | *       |
        //
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES_ALL',
        //     'reversed - edge_orientation', 'weight', i, j)
        check(oneToOne(RO, W, 1, 1), EMPTY);
        check(oneToOne(RO, W, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 3, 1, 5, 7.0)});
        check(oneToOne(RO, W, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 2.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 7.0)});
        check(oneToOne(RO, W, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 4.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 7.0)});
        check(oneToOne(RO, W, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 7.0)});
        check(oneToOne(RO, W, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0),
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 2, 2, 3, 3.0)});
        check(oneToOne(RO, W, 2, 2), EMPTY);
        check(oneToOne(RO, W, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 2, 3, 3.0)});
        check(oneToOne(RO, W, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0)});
        check(oneToOne(RO, W, 2, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 4, 5, 6.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 2, 4, 1.0)});
        check(oneToOne(RO, W, 3, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0)});
        check(oneToOne(RO, W, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0)});
        check(oneToOne(RO, W, 3, 3), EMPTY);
        check(oneToOne(RO, W, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 3, 2, 2.0)});
        check(oneToOne(RO, W, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 4, 5, 6.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 2, 4, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 3, 2, 2.0)});
        final ResultSet rs41 = oneToOne(RO, W, 4, 1);
        try {
            check(rs41, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 3, 4, 5, 6.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 5, 1, 7.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 2, 4, 5, 6.0)});
        } catch (AssertionError e) {
            rs41.beforeFirst();
            check(rs41, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 7.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 4, 5, 6.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 3, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 3, 2.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 3, 4, 5, 6.0)});
        }
        rs41.close();
        check(oneToOne(RO, W, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0),
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 3, 4, 5, 6.0)});
        check(oneToOne(RO, W, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 2.0),
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 4, 5, 6.0)});
        check(oneToOne(RO, W, 4, 4), EMPTY);
        check(oneToOne(RO, W, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 4, 5, 6.0)});
        final ResultSet rs51 = oneToOne(RO, W, 5, 1);
        try {
            check(rs51, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 7.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 3, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 3, 2.0)});
        } catch (AssertionError e) {
            rs51.beforeFirst();
            check(rs51, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 5, 1, 7.0)});
        }
        rs51.close();
        check(oneToOne(RO, W, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0)});
        check(oneToOne(RO, W, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 2.0)});
        check(oneToOne(RO, W, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 4.0)});
        check(oneToOne(RO, W, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneU() throws Exception {
        // Nodes
        // | 1                                  | 2                                           |
        // |------------------------------------|---------------------------------------------|
        // | *                                  | (1,2)                                       |
        // | (2,1)                              | *                                           |
        // | (3,1)                              | (3,2)                                       |
        // | (4,2,1), (4,3,1), (4,5,1), (4,5,1) | (4,2)                                       |
        // | (5,1)                              | (5,1,2), (5,3,2), (5,3,2), (5,4,2), (5,4,2) |
        //
        // | 3            | 4                                  | 5                                           |
        // |--------------|------------------------------------|---------------------------------------------|
        // | (1,3)        | (1,2,4), (1,3,4), (1,5,4), (1,5,4) | (1,5)                                       |
        // | (2,3), (2,3) | (2,4)                              | (2,1,5), (2,3,5), (2,3,5), (2,4,5), (2,4,5) |
        // | *            | (3,4)                              | (3,5)                                       |
        // | (4,3)        | *                                  | (4,5), (4,5)                                |
        // | (5,3)        | (5,4), (5,4)                       | *                                           |
        //
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
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES_ALL',
        //     'undirected', i, j)
        check(oneToOne(U, 1, 1), EMPTY);
        check(oneToOne(U, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0),
        });
        check(oneToOne(U, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 3, 1.0)});
//        // Note: This test has several possible (4!) numberings of the shortest paths.
//        // We run it until the following numbering is given.
//        check(oneToOne(U, st, 1, 4), new PathEdge[]{
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 1, 3, 4, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 3, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 3, 1, 2, 4, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 3, 2, 1, 2, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 4, 1, 5, 4, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 4, 2, 1, 5, 1.0)
//        });
        check(oneToOne(U, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0)});
        check(oneToOne(U, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 2, 1, 1.0)});
        check(oneToOne(U, 2, 2), EMPTY);
        ResultSet rs23 = oneToOne(U, 2, 3);
        try {
            check(rs23, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 2, 3, 1.0),
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1, 2, 3, 1.0)});
        } catch (AssertionError e) {
            rs23.beforeFirst();
            check(rs23, new PathEdge[]{
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 1.0),
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 2, 1, 2, 3, 1.0)});
        }
        rs23.close();
        check(oneToOne(U, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0)});
        // Note: This test has several possible (5!) numberings of the shortest paths.
//        check(oneToOne(U, st, 2, 5), new PathEdge[]{
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 2, 2, 1, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 1, 3, 5, 1.0),
//                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 2, 2, 2, 3, 1.0),
//                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 3, 2, 2, 3, 1.0),
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 4, 1, 4, 5, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 4, 2, 2, 4, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 5, 1, 4, 5, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 5, 2, 2, 4, 1.0)});
        check(oneToOne(U, 3, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 1.0)});
        ResultSet rs32 = oneToOne(U, 3, 2);
        try {
            check(rs32, new PathEdge[]{
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 1.0),
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 2, 1, 3, 2, 1.0)});
        } catch (AssertionError e) {
            rs32.beforeFirst();
            check(rs32, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 3, 2, 1.0),
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1, 3, 2, 1.0)});
        }
        rs32.close();
        check(oneToOne(U, 3, 3), EMPTY);
        check(oneToOne(U, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 3, 4, 1.0)});
        check(oneToOne(U, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 1.0)});
//        // Note: This test has several possible (4!) numberings of the shortest paths.
//        check(oneToOne(U, st, 4, 1), new PathEdge[]{
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 2, 1, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 4, 2, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 3, 1, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 2, 4, 3, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 3, 1, 5, 1, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 3, 2, 4, 5, 1.0),
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 4, 2, 4, 5, 1.0)});
        check(oneToOne(U, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 4, 2, 1.0)});
        check(oneToOne(U, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 4, 3, 1.0)});
        check(oneToOne(U, 4, 4), EMPTY);
        ResultSet rs45 = oneToOne(U, 4, 5);
        try {
            check(rs45, new PathEdge[]{
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 4, 5, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 4, 5, 1.0)});
        } catch (AssertionError e) {
            rs45.beforeFirst();
            check(rs45, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 4, 5, 1.0),
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 2, 1, 4, 5, 1.0)});
        }
        rs45.close();
        check(oneToOne(U, 5, 1), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0)});
//        // Note: This test has several possible (5!) numberings of the shortest paths.
//        check(oneToOne(U, st, 5, 2), new PathEdge[]{
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 5, 1, 1.0),
//                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1, 3, 2, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 3, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 3, 1, 4, 2, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 3, 2, 5, 4, 1.0),
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 4, 2, 5, 4, 1.0),
//                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 5, 1, 3, 2, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 0)", 7, 5, 2, 5, 3, 1.0)});
        check(oneToOne(U, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 1.0)});
        ResultSet rs54 = oneToOne(U, 5, 4);
        try {
            check(rs54, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 4, 1.0),
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 2, 1, 5, 4, 1.0)});
        } catch (AssertionError e) {
            rs54.beforeFirst();
            check(rs54, new PathEdge[]{
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 5, 4, 1.0)});
        }
        rs54.close();
        check(oneToOne(U, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneWU() throws Exception {
        // Nodes
        // | 1              | 2       | 3       | 4         | 5              |
        // |----------------|---------|---------|-----------|----------------|
        // | *              | (1,3,2) | (1,3)   | (1,3,2,4) | (1,5), (1,3,5) |
        // | (2,3,1)        | *       | (2,3)   | (2,4)     | (2,3,5)        |
        // | (3,1)          | (3,2)   | *       | (3,2,4)   | (3,5)          |
        // | (4,2,3,1)      | (4,2)   | (4,2,3) | *         | (4,5)          |
        // | (5,1), (5,3,1) | (5,3,2) | (5,3)   | (5,4)     | *              |
        //
        // Edges
        // | 1           | 2     | 3     | 4       | 5           |
        // |-------------|-------|-------|---------|-------------|
        // | *           | (5,3) | (5)   | (5,3,2) | (10), (5,7) |
        // | (3,5)       | *     | (3)   | (2)     | (3,7)       |
        // | (5)         | (3)   | *     | (3,2)   | (7)         |
        // | (2,3,5)     | (2)   | (2,3) | *       | (8)         |
        // | (10), (7,5) | (7,3) | (7)   | (8)     | *           |
        //
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES_ALL',
        //     'undirected', 'weight', i, j)
        check(oneToOne(U, W, 1, 1), EMPTY);
        check(oneToOne(U, W, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 3, 5.0)});
        check(oneToOne(U, W, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 3, 5.0)});
        check(oneToOne(U, W, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 3, 2, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 3, 1, 3, 5.0)});
        ResultSet rs15 = oneToOne(U, W, 1, 5);
        try {
            check(rs15, new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 7.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 1, 3, 5, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 3, 5.0)});
        } catch (AssertionError e) {
            rs15.beforeFirst();
            check(rs15, new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 3, 5.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 1, 1, 5, 7.0)});
        }
        rs15.close();
        check(oneToOne(U, W, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 3, 2.0)});
        check(oneToOne(U, W, 2, 2), EMPTY);
        check(oneToOne(U, W, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 2.0)});
        check(oneToOne(U, W, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0)});
        check(oneToOne(U, W, 2, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 2.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 3, 2.0)});
        check(oneToOne(U, W, 3, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0)});
        check(oneToOne(U, W, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0)});
        check(oneToOne(U, W, 3, 3), EMPTY);
        check(oneToOne(U, W, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 4, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 3, 2, 2.0)});
        check(oneToOne(U, W, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 3, 5, 2.0)});
        check(oneToOne(U, W, 4, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 3, 2.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 3, 4, 2, 1.0)});
        check(oneToOne(U, W, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 4, 2, 1.0)});
        check(oneToOne(U, W, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 3, 2.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 4, 2, 1.0)});
        check(oneToOne(U, W, 4, 4), EMPTY);
        check(oneToOne(U, W, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 4, 5, 4.0)});
        ResultSet rs51 = oneToOne(U, W, 5, 1);
        try {
            check(rs51, new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 3, 1, 5.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 1, 5, 1, 7.0)});
        } catch (AssertionError e) {
            rs51.beforeFirst();
            check(rs51, new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 3, 1, 5.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 3, 2.0)});
        }
        rs51.close();
        check(oneToOne(U, W, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 3, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 3, 2.0)});
        check(oneToOne(U, W, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 3, 2.0)});
        check(oneToOne(U, W, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 4, 4.0)});
        check(oneToOne(U, W, 5, 5), EMPTY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonexistantSourceVertex() throws Throwable {
        try {
            // The graph does not contain vertex 6.
            check(oneToOne(U, W, 6, 1), null);
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Source vertex not found"));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonexistantDestinationVertex() throws Throwable {
        try {
            // The graph does not contain vertex 6.
            check(oneToOne(U, W, 1, 6), null);
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Target vertex not found"));
            throw e.getOriginalCause();
        }
    }

    @Test
    public void testUnreachableVertices() throws SQLException {
        // Vertices 3 and 6 are in different connected components.
        assertTrue(!oneToOne("COPY_EDGES_ALL", DO, W, 3, 6).next());
        // 7 is reachable from 6.
        check(oneToOne("COPY_EDGES_ALL", DO, W, 6, 7), new PathEdge[]{
                new PathEdge("LINESTRING (3 1, 4 2)", 11, 1, 1, 6, 7, 1.0)});
        // But 6 is not reachable from 7 in a directed graph.
        assertTrue(!oneToOne("COPY_EDGES_ALL", DO, W, 7, 6).next());
        // It is, however, in an undirected graph.
        check(oneToOne("COPY_EDGES_ALL", U, W, 7, 6), new PathEdge[]{
                new PathEdge("LINESTRING (3 1, 4 2)", 11, 1, 1, 7, 6, 1.0)});
    }

    private ResultSet oneToOne(String table, String orientation, String weight,
                               int source, int destination) throws SQLException {
        return st.executeQuery(
                "SELECT * FROM ST_ShortestPath('" + table + "', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + source + ", " + destination + ")");
    }

    private ResultSet oneToOne(String orientation, String weight,
                               int source, int destination) throws SQLException {
        return oneToOne("CORMEN_EDGES_ALL", orientation, weight, source, destination);
    }

    private ResultSet oneToOne(String orientation, int source, int destination) throws SQLException {
        return oneToOne(orientation, null, source, destination);
    }

    private void check(ResultSet rs, PathEdge[] pathEdges) throws SQLException {
        check(rs, pathEdges, true);
    }

    private void checkNoGeom(ResultSet rs, PathEdge[] pathEdges) throws SQLException {
        check(rs, pathEdges, false);
    }

    private void check(ResultSet rs, PathEdge[] pathEdges, boolean checkGeom) throws SQLException {
        for (int i = 0; i < pathEdges.length; i++) {
            assertTrue(rs.next());
            PathEdge e = pathEdges[i];
            if (checkGeom) {
                assertGeometryEquals(e.getGeom(), rs.getBytes(GraphConstants.THE_GEOM));
            }
            assertEquals(e.getEdgeID(), rs.getInt(GraphConstants.EDGE_ID));
            assertEquals(e.getPathID(), rs.getInt(GraphConstants.PATH_ID));
            assertEquals(e.getPathedgeID(), rs.getInt(GraphConstants.PATH_EDGE_ID));
            assertEquals(e.getSource(), rs.getInt(GraphConstants.SOURCE));
            assertEquals(e.getDestination(), rs.getInt(GraphConstants.DESTINATION));
            assertEquals(e.getWeight(), rs.getDouble(GraphConstants.WEIGHT), TOLERANCE);
        }
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void testNoGeometryField() throws Throwable {
        // This test shows that if the input has no geometry column,
        // neither will the output.
        st.execute("DROP TABLE IF EXISTS NO_GEOM;" +
                "CREATE TABLE NO_GEOM AS " +
                "SELECT EDGE_ID, START_NODE, END_NODE, WEIGHT " +
                "FROM CORMEN_EDGES_ALL;");
        final ResultSet resultSet = st.executeQuery("SELECT * FROM ST_ShortestPath(" +
                "'NO_GEOM', 'UNDIRECTED', 3, 4);");
        checkNoGeom(resultSet, new PathEdge[]{
                new PathEdge(6, 1, 1, 3, 4, 1.0)});
    }

    @Test
    public void testNoGeometryFieldDifferentCCs() throws Throwable {
        // This test shows that if the input has no geometry column,
        // neither will the output.
        st.execute("DROP TABLE IF EXISTS NO_GEOM;" +
                "CREATE TABLE NO_GEOM AS " +
                "SELECT EDGE_ID, START_NODE, END_NODE, WEIGHT " +
                "FROM COPY_EDGES_ALL;");
        final ResultSet resultSet = st.executeQuery("SELECT * FROM ST_ShortestPath(" +
                "'NO_GEOM', 'UNDIRECTED', 1, 8);");
        assertTrue(!resultSet.next());
    }

    private class PathEdge {
        private String geom;
        private int edgeID;
        private int pathID;
        private int pathedgeID;
        private int source;
        private int destination;
        private double weight;

        public PathEdge(int edgeID, int pathID, int pathedgeID,
                        int source, int destination, double weight) {
            this(null, edgeID, pathID, pathedgeID, source, destination, weight);
        }

        public PathEdge(String geom, int edgeID, int pathID, int pathedgeID,
                         int source, int destination, double weight) {
            this.geom = geom;
            this.edgeID = edgeID;
            this.pathID = pathID;
            this.pathedgeID = pathedgeID;
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        public String getGeom() {
            return geom;
        }

        public int getEdgeID() {
            return edgeID;
        }

        public int getPathID() {
            return pathID;
        }

        public int getPathedgeID() {
            return pathedgeID;
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
