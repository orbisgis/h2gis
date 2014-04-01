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

import org.h2.jdbc.JdbcSQLException;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.network.SpatialFunctionTest;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by adam on 3/24/14.
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
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
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
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES',
        //     'directed - edge_orientation', i, j)
        check(oneToOne(DO, st, 1, 1), EMPTY);
        check(oneToOne(DO, st, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0)});
        final ResultSet rs13 = oneToOne(DO, st, 1, 3);
        try {
            check(rs13, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 4, 3, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 4, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 5, 3, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 2, 1, 5, 1.0)});
        } catch (AssertionError e) {
            rs13.beforeFirst();
            check(rs13, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 2, 1, 5, 1.0),
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 1, 4, 3, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 4, 1.0)});
        }
        check(oneToOne(DO, st, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 4, 1.0)});
        check(oneToOne(DO, st, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 1, 5, 1.0)});
        check(oneToOne(DO, st, 2, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 1.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 2, 4, 1.0)});
        check(oneToOne(DO, st, 2, 2), EMPTY);
        check(oneToOne(DO, st, 2, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 4, 3, 1.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 4, 1.0)});
        check(oneToOne(DO, st, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 1.0)});
        check(oneToOne(DO, st, 2, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 1.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 4, 1.0)});
        check(oneToOne(DO, st, 3, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0),
                        new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 2, 3, 5, 1.0)});
        check(oneToOne(DO, st, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 3, 2, 1.0)});
        check(oneToOne(DO, st, 3, 3), EMPTY);
        check(oneToOne(DO, st, 3, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 1.0),
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 3, 2, 1.0)});
        check(oneToOne(DO, st, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 3, 5, 1.0)});
        check(oneToOne(DO, st, 4, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 1.0)});
        check(oneToOne(DO, st, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 4, 2, 1.0)});
        check(oneToOne(DO, st, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 4, 3, 1.0)});
        check(oneToOne(DO, st, 4, 4), EMPTY);
        check(oneToOne(DO, st, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 1.0)});
        check(oneToOne(DO, st, 5, 1), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0)});
        final ResultSet rs52 = oneToOne(DO, st, 5, 2);
        try {
            check(rs52, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 5, 1, 1.0),
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 2, 1, 3, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 2, 5, 3, 1.0)});
        } catch (AssertionError e) {
            rs52.beforeFirst();
            check(rs52, new PathEdge[]{
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 3, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 5, 3, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 2, 1, 1, 2, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 2, 5, 1, 1.0)});
        }
        check(oneToOne(DO, st, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 1.0)});
        check(oneToOne(DO, st, 5, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 4, 1.0),
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 5, 1, 1.0)});
        check(oneToOne(DO, st, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneWDO() throws Exception {
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES',
        //     'directed - edge_orientation', 'weight', i, j)
        check(oneToOne(DO, W, st, 1, 1), EMPTY);
        check(oneToOne(DO, W, st, 1, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 4, 2, 3.0),
                        new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 4, 5.0)});
        final ResultSet rs13 = oneToOne(DO, W, st, 1, 3);
        try {
            check(rs13, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 6.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 3, 1, 4, 5.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 2, 1, 5, 7.0)});
        } catch (AssertionError e) {
            rs13.beforeFirst();
            check(rs13, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 6.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 2, 1, 5, 7.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 4, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 3, 1, 4, 5.0)});
        }
        check(oneToOne(DO, W, st, 1, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 4, 5.0)});
        final ResultSet rs15 = oneToOne(DO, W, st, 1, 5);
        try {
            check(rs15, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 1, 5, 7.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 1, 4, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 4, 5.0)});
        } catch (AssertionError e) {
            rs15.beforeFirst();
            check(rs15, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 2.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 4, 5.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 1, 5, 7.0)});
        }
        check(oneToOne(DO, W, st, 2, 1),
                new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 2.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 2, 4, 2.0)});
        check(oneToOne(DO, W, st, 2, 2), EMPTY);
        check(oneToOne(DO, W, st, 2, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 6.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 2.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 2, 4, 2.0)});
        check(oneToOne(DO, W, st, 2, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 2.0)});
        check(oneToOne(DO, W, st, 2, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 2.0),
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 4, 2.0)});
        check(oneToOne(DO, W, st, 3, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                        new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 2, 3, 5, 4.0)});
        check(oneToOne(DO, W, st, 3, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 3, 2, 1.0)});
        check(oneToOne(DO, W, st, 3, 3), EMPTY);
        check(oneToOne(DO, W, st, 3, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 2.0),
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 3, 2, 1.0)});
        check(oneToOne(DO, W, st, 3, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 3, 5, 4.0)});
        check(oneToOne(DO, W, st, 4, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 2.0)});
        check(oneToOne(DO, W, st, 4, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 4, 2, 3.0)});
        check(oneToOne(DO, W, st, 4, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 6.0),
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 4, 5, 2.0)});
        check(oneToOne(DO, W, st, 4, 4), EMPTY);
        check(oneToOne(DO, W, st, 4, 5), new PathEdge[]{
                        new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 2.0)});
        check(oneToOne(DO, W, st, 5, 1), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0)});
        check(oneToOne(DO, W, st, 5, 2), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 3, 2, 1.0),
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 5, 3, 6.0)});
        check(oneToOne(DO, W, st, 5, 3), new PathEdge[]{
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 6.0)});
        check(oneToOne(DO, W, st, 5, 4), new PathEdge[]{
                        new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 2.0),
                        new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 3, 2, 1.0),
                        new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 3, 5, 3, 6.0)});
        check(oneToOne(DO, W, st, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneRO() throws Exception {
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES',
        //     'reversed - edge_orientation', i, j)
        check(oneToOne(RO, st, 1, 1), EMPTY);
        check(oneToOne(RO, st, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 1.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 1.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 3, 1, 5, 1.0)});
        check(oneToOne(RO, st, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 1.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 1.0)});
        check(oneToOne(RO, st, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 1.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 1.0)});
        check(oneToOne(RO, st, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0)});
        check(oneToOne(RO, st, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 2, 1, 1.0)});
        check(oneToOne(RO, st, 2, 2), EMPTY);
        check(oneToOne(RO, st, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0)});
        check(oneToOne(RO, st, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 2, 4, 1.0)});
        final ResultSet rs25 = oneToOne(RO, st, 2, 5);
        try {
            check(rs25, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 3, 5, 1.0),
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 2, 3, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 1, 1, 5, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 2, 2, 2, 1, 1.0)});
        } catch (AssertionError e) {
            rs25.beforeFirst();
            check(rs25, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 2, 2, 1, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 3, 5, 1.0),
                    new PathEdge("LINESTRING (1 2, 2 2)", 2, 2, 2, 2, 3, 1.0)});
        }
        final ResultSet rs31 = oneToOne(RO, st, 3, 1);
        try {
            check(rs31, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 3, 5, 1.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 4, 1, 1.0),
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 2, 3, 4, 1.0)});
        } catch (AssertionError e) {
            rs31.beforeFirst();
            check(rs31, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 1.0),
                    new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 2, 3, 4, 1.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 5, 1, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 2, 3, 5, 1.0)});
        }
        check(oneToOne(RO, st, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 1.0),
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 2, 3, 4, 1.0)});
        check(oneToOne(RO, st, 3, 3), EMPTY);
        check(oneToOne(RO, st, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 3, 4, 1.0)});
        check(oneToOne(RO, st, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 3, 5, 1.0)});
        check(oneToOne(RO, st, 4, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 1.0)});
        check(oneToOne(RO, st, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 1.0)});
        check(oneToOne(RO, st, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 4, 2, 1.0)});
        check(oneToOne(RO, st, 4, 4), EMPTY);
        check(oneToOne(RO, st, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 4, 1, 1.0)});
        check(oneToOne(RO, st, 5, 1), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 1.0)});
        check(oneToOne(RO, st, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 1.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 1.0)});
        check(oneToOne(RO, st, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 1.0)});
        check(oneToOne(RO, st, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 1.0)});
        check(oneToOne(RO, st, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneWRO() throws Exception {
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES',
        //     'reversed - edge_orientation', 'weight', i, j)
        check(oneToOne(RO, W, st, 1, 1), EMPTY);
        check(oneToOne(RO, W, st, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 3, 1, 5, 7.0)});
        check(oneToOne(RO, W, st, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 4.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 7.0)});
        check(oneToOne(RO, W, st, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 2.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 7.0)});
        check(oneToOne(RO, W, st, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 7.0)});
        check(oneToOne(RO, W, st, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0),
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 2, 2, 4, 3.0)});
        check(oneToOne(RO, W, st, 2, 2), EMPTY);
        check(oneToOne(RO, W, st, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0)});
        check(oneToOne(RO, W, st, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 2, 4, 3.0)});
        check(oneToOne(RO, W, st, 2, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 3, 5, 6.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 2, 3, 1.0)});
        final ResultSet rs31 = oneToOne(RO, W, st, 3, 1);
        try {
            check(rs31, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 3, 3, 5, 6.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 5, 1, 7.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 2, 3, 5, 6.0)});
        } catch (AssertionError e) {
            rs31.beforeFirst();
            check(rs31, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 7.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 3, 5, 6.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 4, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 4, 2.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 3, 3, 5, 6.0)});
        }
        check(oneToOne(RO, W, st, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0),
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 3, 3, 5, 6.0)});
        check(oneToOne(RO, W, st, 3, 3), EMPTY);
        check(oneToOne(RO, W, st, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 2.0),
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 2, 3, 5, 6.0)});
        check(oneToOne(RO, W, st, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 3, 5, 6.0)});
        check(oneToOne(RO, W, st, 4, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0)});
        check(oneToOne(RO, W, st, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0)});
        check(oneToOne(RO, W, st, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 4, 2, 2.0)});
        check(oneToOne(RO, W, st, 4, 4), EMPTY);
        check(oneToOne(RO, W, st, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 3, 5, 6.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 2, 3, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 3, 4, 2, 2.0)});
        final ResultSet rs51 = oneToOne(RO, W, st, 5, 1);
        try {
            check(rs51, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 1, 1, 5, 1, 7.0),
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 4, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 4, 2.0)});
        } catch (AssertionError e) {
            rs51.beforeFirst();
            check(rs51, new PathEdge[]{
                    new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0),
                    new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0),
                    new PathEdge("LINESTRING (2 0, 0 1)", -10, 2, 1, 5, 1, 7.0)});
        }
        check(oneToOne(RO, W, st, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0)});
        check(oneToOne(RO, W, st, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 4.0)});
        check(oneToOne(RO, W, st, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 2.0)});
        check(oneToOne(RO, W, st, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneU() throws Exception {
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES',
        //     'undirected', i, j)
        check(oneToOne(U, st, 1, 1), EMPTY);
        check(oneToOne(U, st, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0),
        });
//        // Note: This test has several possible (4!) numberings of the shortest paths.
//        // We run it until the following numbering is given.
//        check(oneToOne(U, st, 1, 3), new PathEdge[]{
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 1, 5, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 1, 4, 3, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 4, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 3, 1, 2, 3, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 3, 2, 1, 2, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 4, 1, 5, 3, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 4, 2, 1, 5, 1.0)
//        });
        check(oneToOne(U, st, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 4, 1.0)});
        check(oneToOne(U, st, 1, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0)});
        check(oneToOne(U, st, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 2, 1, 1.0)});
        check(oneToOne(U, st, 2, 2), EMPTY);
        check(oneToOne(U, st, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0)});
        ResultSet rs24 = oneToOne(U, st, 2, 4);
        try {
            check(rs24, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 2, 4, 1.0),
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1, 2, 4, 1.0)});
        } catch (AssertionError e) {
            rs24.beforeFirst();
            check(rs24, new PathEdge[]{
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 1.0),
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 2, 1, 2, 4, 1.0)});
        }
        // Note: This test has several possible (5!) numberings of the shortest paths.
//        check(oneToOne(U, st, 2, 5), new PathEdge[]{
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 2, 2, 1, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 1, 4, 5, 1.0),
//                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 2, 2, 2, 4, 1.0),
//                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 3, 2, 2, 4, 1.0),
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 4, 1, 3, 5, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 4, 2, 2, 3, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 5, 1, 3, 5, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 5, 2, 2, 3, 1.0)});
//        // Note: This test has several possible (4!) numberings of the shortest paths.
//        check(oneToOne(U, st, 3, 1), new PathEdge[]{
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 2, 1, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 3, 2, 1.0),
//                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 4, 1, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 2)", 6, 2, 2, 3, 4, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 3, 1, 5, 1, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 3, 2, 3, 5, 1.0),
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 4, 2, 3, 5, 1.0)});
        check(oneToOne(U, st, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 3, 2, 1.0)});
        check(oneToOne(U, st, 3, 3), EMPTY);
        check(oneToOne(U, st, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 3, 4, 1.0)});
        ResultSet rs35 = oneToOne(U, st, 3, 5);
        try {
            check(rs35, new PathEdge[]{
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 3, 5, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 3, 5, 1.0)});
        } catch (AssertionError e) {
            rs35.beforeFirst();
            check(rs35, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 3, 5, 1.0),
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 2, 1, 3, 5, 1.0)});
        }
        check(oneToOne(U, st, 4, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 1.0)});
        ResultSet rs42 = oneToOne(U, st, 4, 2);
        try {
            check(rs42, new PathEdge[]{
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 1.0),
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 2, 1, 4, 2, 1.0)});
        } catch (AssertionError e) {
            rs42.beforeFirst();
            check(rs42, new PathEdge[]{
                    new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 1, 1, 4, 2, 1.0),
                    new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1, 4, 2, 1.0)});
        }
        check(oneToOne(U, st, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 2)", 6, 1, 1, 4, 3, 1.0)});
        check(oneToOne(U, st, 4, 4), EMPTY);
        check(oneToOne(U, st, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 1.0)});
        check(oneToOne(U, st, 5, 1), new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 1.0)});
//        // Note: This test has several possible (5!) numberings of the shortest paths.
//        check(oneToOne(U, st, 5, 2), new PathEdge[]{
//                new PathEdge("LINESTRING (0 1, 1 2)", 1, 1, 1, 1, 2, 1.0),
//                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 2, 5, 1, 1.0),
//                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 2, 1, 4, 2, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 4, 1.0),
//                new PathEdge("LINESTRING (1 2, 2 2)", 2, 3, 1, 3, 2, 1.0),
//                new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 3, 2, 5, 3, 1.0),
//                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 4, 2, 5, 3, 1.0),
//                new PathEdge("LINESTRING (1 0, 1.25 1, 1 2)", 4, 5, 1, 4, 2, 1.0),
//                new PathEdge("LINESTRING (1 0, 2 0)", 7, 5, 2, 5, 4, 1.0)});
        ResultSet rs53 = oneToOne(U, st, 5, 3);
        try {
            check(rs53, new PathEdge[]{
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 1, 1, 5, 3, 1.0),
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 2, 1, 5, 3, 1.0)});
        } catch (AssertionError e) {
            rs53.beforeFirst();
            check(rs53, new PathEdge[]{
                    new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 1.0),
                    new PathEdge("LINESTRING (2 0, 2.25 1, 2 2)", 9, 2, 1, 5, 3, 1.0)});
        }
        check(oneToOne(U, st, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 1.0)});
        check(oneToOne(U, st, 5, 5), EMPTY);
    }

    @Test
    public void oneToOneWU() throws Exception {
        // SELECT * FROM ST_ShortestPath('CORMEN_EDGES',
        //     'undirected', 'weight', i, j)
        check(oneToOne(U, W, st, 1, 1), EMPTY);
        check(oneToOne(U, W, st, 1, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 4, 5.0)});
        check(oneToOne(U, W, st, 1, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 4, 2, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 3, 1, 4, 5.0)});
        check(oneToOne(U, W, st, 1, 4), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 1, 4, 5.0)});
        ResultSet rs15 = oneToOne(U, W, st, 1, 5);
        try {
            check(rs15, new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 1, 5, 7.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 1, 4, 5, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 2, 1, 4, 5.0)});
        } catch (AssertionError e) {
            rs15.beforeFirst();
            check(rs15, new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 2.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 2, 1, 4, 5.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 1, 1, 5, 7.0)});
        }
        check(oneToOne(U, W, st, 2, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 4, 2.0)});
        check(oneToOne(U, W, st, 2, 2), EMPTY);
        check(oneToOne(U, W, st, 2, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0)});
        check(oneToOne(U, W, st, 2, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 2.0)});
        check(oneToOne(U, W, st, 2, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 2.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 4, 2.0)});
        check(oneToOne(U, W, st, 3, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 2, 4, 2.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 3, 3, 2, 1.0)});
        check(oneToOne(U, W, st, 3, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 3, 2, 1.0)});
        check(oneToOne(U, W, st, 3, 3), EMPTY);
        check(oneToOne(U, W, st, 3, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 2, 4, 2.0),
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 2, 3, 2, 1.0)});
        check(oneToOne(U, W, st, 3, 5), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 3, 5, 4.0)});
        check(oneToOne(U, W, st, 4, 1), new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0)});
        check(oneToOne(U, W, st, 4, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0)});
        check(oneToOne(U, W, st, 4, 3), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 2 2)", 2, 1, 1, 2, 3, 1.0),
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 2, 4, 2, 2.0)});
        check(oneToOne(U, W, st, 4, 4), EMPTY);
        check(oneToOne(U, W, st, 4, 5), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 4, 5, 2.0)});
        ResultSet rs51 = oneToOne(U, W, st, 5, 1);
        try {
            check(rs51, new PathEdge[]{
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 1, 1, 4, 1, 5.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0),
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 2, 1, 5, 1, 7.0)});
        } catch (AssertionError e) {
            rs51.beforeFirst();
            check(rs51, new PathEdge[]{
                new PathEdge("LINESTRING (2 0, 0 1)", 10, 1, 1, 5, 1, 7.0),
                new PathEdge("LINESTRING (0 1, 1 0)", 5, 2, 1, 4, 1, 5.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 2, 2, 5, 4, 2.0)});
        }
        check(oneToOne(U, W, st, 5, 2), new PathEdge[]{
                new PathEdge("LINESTRING (1 2, 0.75 1, 1 0)", 3, 1, 1, 4, 2, 2.0),
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 2, 5, 4, 2.0)});
        check(oneToOne(U, W, st, 5, 3), new PathEdge[]{
                new PathEdge("LINESTRING (2 2, 1.75 1, 2 0)", 8, 1, 1, 5, 3, 4.0)});
        check(oneToOne(U, W, st, 5, 4), new PathEdge[]{
                new PathEdge("LINESTRING (1 0, 2 0)", 7, 1, 1, 5, 4, 2.0)});
        check(oneToOne(U, W, st, 5, 5), EMPTY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonexistantSourceVertex() throws Throwable {
        try {
            // The graph does not contain vertex 6.
            check(oneToOne(U, W, st, 6, 1), null);
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Source vertex not found"));
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonexistantDestinationVertex() throws Throwable {
        try {
            // The graph does not contain vertex 6.
            check(oneToOne(U, W, st, 1, 6), null);
        } catch (JdbcSQLException e) {
            assertTrue(e.getMessage().contains("Target vertex not found"));
            throw e.getOriginalCause();
        }
    }

    @Test
    public void testUnreachableVertices() throws SQLException {
        st.execute("DROP TABLE IF EXISTS copy");
        st.execute("DROP TABLE IF EXISTS copy_nodes");
        st.execute("DROP TABLE IF EXISTS copy_edges");
        st.execute("CREATE TABLE copy AS SELECT * FROM cormen");
        // We add another connected component consisting of the edge w(6, 7)=1.0.
        st.execute("INSERT INTO copy VALUES ('LINESTRING (3 1, 4 2)', 1.0, 1)");
        st.execute("CALL ST_Graph('COPY', 'road')");
        // Vertices 3 and 6 are in different connected components.
        check(oneToOne("COPY_EDGES", DO, W, st, 3, 6), new PathEdge[]{
                new PathEdge(null, -1, -1, -1, 3, 6, Double.POSITIVE_INFINITY)});
        // 7 is reachable from 6.
        check(oneToOne("COPY_EDGES", DO, W, st, 6, 7), new PathEdge[]{
                new PathEdge("LINESTRING (3 1, 4 2)", 11, 1, 1, 6, 7, 1.0)});
        // But 6 is not reachable from 7 in a directed graph.
        check(oneToOne("COPY_EDGES", DO, W, st, 7, 6), new PathEdge[]{
                new PathEdge(null, -1, -1, -1, 7, 6, Double.POSITIVE_INFINITY)});
        // It is, however, in an undirected graph.
        check(oneToOne("COPY_EDGES", U, W, st, 7, 6), new PathEdge[]{
                new PathEdge("LINESTRING (3 1, 4 2)", 11, 1, 1, 7, 6, 1.0)});
        st.execute("DROP TABLE copy");
        st.execute("DROP TABLE copy_nodes");
        st.execute("DROP TABLE copy_edges");
    }

    private ResultSet oneToOne(String table, String orientation, String weight, Statement st,
                               int source, int destination) throws SQLException {
        return st.executeQuery(
                "SELECT * FROM ST_ShortestPath('" + table + "', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + source + ", " + destination + ")");
    }

    private ResultSet oneToOne(String orientation, String weight, Statement st,
                               int source, int destination) throws SQLException {
        return oneToOne("CORMEN_EDGES", orientation, weight, st, source, destination);
    }

    private ResultSet oneToOne(String orientation, Statement st, int source, int destination) throws SQLException {
        return oneToOne(orientation, null, st, source, destination);
    }

    private void check(ResultSet rs, PathEdge[] pathEdges) throws SQLException {
        for (int i = 0; i < pathEdges.length; i++) {
            assertTrue(rs.next());
            PathEdge e = pathEdges[i];
            SpatialFunctionTest.assertGeometryEquals(e.getGeom(), rs.getBytes(ST_ShortestPath.GEOM_INDEX));
            assertEquals(e.getEdgeID(), rs.getInt(ST_ShortestPath.EDGE_ID_INDEX));
            assertEquals(e.getPathID(), rs.getInt(ST_ShortestPath.PATH_ID_INDEX));
            assertEquals(e.getPathedgeID(), rs.getInt(ST_ShortestPath.PATH_EDGE_ID_INDEX));
            assertEquals(e.getSource(), rs.getInt(ST_ShortestPath.SOURCE_INDEX));
            assertEquals(e.getDestination(), rs.getInt(ST_ShortestPath.DESTINATION_INDEX));
            assertEquals(e.getWeight(), rs.getDouble(ST_ShortestPath.WEIGHT_INDEX), TOLERANCE);
        }
        assertFalse(rs.next());
    }

    private class PathEdge {
        private String geom;
        private int edgeID;
        private int pathID;
        private int pathedgeID;
        private int source;
        private int destination;
        private double weight;

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
