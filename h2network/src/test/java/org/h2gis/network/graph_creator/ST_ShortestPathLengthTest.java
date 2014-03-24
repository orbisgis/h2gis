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
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by adam on 3/6/14.
 */
public class ST_ShortestPathLengthTest {

    private static Connection connection;
    private Statement st;
    private static final double TOLERANCE = 0.0;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String W = "'weight'";
    private static final String SOURCE_DEST_TABLE = "'source_dest'";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ShortestPathLengthTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPathLength(), "");
        GraphCreatorTest.registerCormenGraph(connection);
        registerSourceDestinationTable(connection);
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

    private static void registerSourceDestinationTable(Connection connection) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE source_dest(source INT, destination INT);" +
                    "INSERT INTO source_dest VALUES "
                    + "(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),"
                    + "(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),"
                    + "(3, 1), (3, 2), (3, 3), (3, 4), (3, 5),"
                    + "(4, 1), (4, 2), (4, 3), (4, 4), (4, 5),"
                    + "(5, 1), (5, 2), (5, 3), (5, 4), (5, 5);");
        } finally {
            st.close();
        }
    }

    // ************************** One-to-One ****************************************

    @Test
    public void oneToOneDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', i, j)
        oneToOne(DO, st, 1, 1, 0.0);
        oneToOne(DO, st, 1, 2, 1.0);
        oneToOne(DO, st, 1, 3, 2.0);
        oneToOne(DO, st, 1, 4, 1.0);
        oneToOne(DO, st, 1, 5, 1.0);
        oneToOne(DO, st, 2, 1, 3.0);
        oneToOne(DO, st, 2, 2, 0.0);
        oneToOne(DO, st, 2, 3, 2.0);
        oneToOne(DO, st, 2, 4, 1.0);
        oneToOne(DO, st, 2, 5, 2.0);
        oneToOne(DO, st, 3, 1, 2.0);
        oneToOne(DO, st, 3, 2, 1.0);
        oneToOne(DO, st, 3, 3, 0.0);
        oneToOne(DO, st, 3, 4, 2.0);
        oneToOne(DO, st, 3, 5, 1.0);
        oneToOne(DO, st, 4, 1, 2.0);
        oneToOne(DO, st, 4, 2, 1.0);
        oneToOne(DO, st, 4, 3, 1.0);
        oneToOne(DO, st, 4, 4, 0.0);
        oneToOne(DO, st, 4, 5, 1.0);
        oneToOne(DO, st, 5, 1, 1.0);
        oneToOne(DO, st, 5, 2, 2.0);
        oneToOne(DO, st, 5, 3, 1.0);
        oneToOne(DO, st, 5, 4, 2.0);
        oneToOne(DO, st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'directed - edge_orientation', i, j)
        oneToOne(DO, W, st, 1, 1, 0.0);
        oneToOne(DO, W, st, 1, 2, 8.0);
        oneToOne(DO, W, st, 1, 3, 13.0);
        oneToOne(DO, W, st, 1, 4, 5.0);
        oneToOne(DO, W, st, 1, 5, 7.0);
        oneToOne(DO, W, st, 2, 1, 11.0);
        oneToOne(DO, W, st, 2, 2, 0.0);
        oneToOne(DO, W, st, 2, 3, 10.0);
        oneToOne(DO, W, st, 2, 4, 2.0);
        oneToOne(DO, W, st, 2, 5, 4.0);
        oneToOne(DO, W, st, 3, 1, 11.0);
        oneToOne(DO, W, st, 3, 2, 1.0);
        oneToOne(DO, W, st, 3, 3, 0.0);
        oneToOne(DO, W, st, 3, 4, 3.0);
        oneToOne(DO, W, st, 3, 5, 4.0);
        oneToOne(DO, W, st, 4, 1, 9.0);
        oneToOne(DO, W, st, 4, 2, 3.0);
        oneToOne(DO, W, st, 4, 3, 8.0);
        oneToOne(DO, W, st, 4, 4, 0.0);
        oneToOne(DO, W, st, 4, 5, 2.0);
        oneToOne(DO, W, st, 5, 1, 7.0);
        oneToOne(DO, W, st, 5, 2, 7.0);
        oneToOne(DO, W, st, 5, 3, 6.0);
        oneToOne(DO, W, st, 5, 4, 9.0);
        oneToOne(DO, W, st, 5, 5, 0.0);

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', 'weight', i, j)
        oneToOne(W, DO, st, 1, 1, 0.0);
        oneToOne(W, DO, st, 1, 2, 8.0);
        oneToOne(W, DO, st, 1, 3, 13.0);
        oneToOne(W, DO, st, 1, 4, 5.0);
        oneToOne(W, DO, st, 1, 5, 7.0);
        oneToOne(W, DO, st, 2, 1, 11.0);
        oneToOne(W, DO, st, 2, 2, 0.0);
        oneToOne(W, DO, st, 2, 3, 10.0);
        oneToOne(W, DO, st, 2, 4, 2.0);
        oneToOne(W, DO, st, 2, 5, 4.0);
        oneToOne(W, DO, st, 3, 1, 11.0);
        oneToOne(W, DO, st, 3, 2, 1.0);
        oneToOne(W, DO, st, 3, 3, 0.0);
        oneToOne(W, DO, st, 3, 4, 3.0);
        oneToOne(W, DO, st, 3, 5, 4.0);
        oneToOne(W, DO, st, 4, 1, 9.0);
        oneToOne(W, DO, st, 4, 2, 3.0);
        oneToOne(W, DO, st, 4, 3, 8.0);
        oneToOne(W, DO, st, 4, 4, 0.0);
        oneToOne(W, DO, st, 4, 5, 2.0);
        oneToOne(W, DO, st, 5, 1, 7.0);
        oneToOne(W, DO, st, 5, 2, 7.0);
        oneToOne(W, DO, st, 5, 3, 6.0);
        oneToOne(W, DO, st, 5, 4, 9.0);
        oneToOne(W, DO, st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', i, j)
        oneToOne(RO, st, 1, 1, 0.0);
        oneToOne(RO, st, 2, 1, 1.0);
        oneToOne(RO, st, 3, 1, 2.0);
        oneToOne(RO, st, 4, 1, 1.0);
        oneToOne(RO, st, 5, 1, 1.0);
        oneToOne(RO, st, 1, 2, 3.0);
        oneToOne(RO, st, 2, 2, 0.0);
        oneToOne(RO, st, 3, 2, 2.0);
        oneToOne(RO, st, 4, 2, 1.0);
        oneToOne(RO, st, 5, 2, 2.0);
        oneToOne(RO, st, 1, 3, 2.0);
        oneToOne(RO, st, 2, 3, 1.0);
        oneToOne(RO, st, 3, 3, 0.0);
        oneToOne(RO, st, 4, 3, 2.0);
        oneToOne(RO, st, 5, 3, 1.0);
        oneToOne(RO, st, 1, 4, 2.0);
        oneToOne(RO, st, 2, 4, 1.0);
        oneToOne(RO, st, 3, 4, 1.0);
        oneToOne(RO, st, 4, 4, 0.0);
        oneToOne(RO, st, 5, 4, 1.0);
        oneToOne(RO, st, 1, 5, 1.0);
        oneToOne(RO, st, 2, 5, 2.0);
        oneToOne(RO, st, 3, 5, 1.0);
        oneToOne(RO, st, 4, 5, 2.0);
        oneToOne(RO, st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', 'weight', i, j)
        oneToOne(RO, W, st, 1, 1, 0.0);
        oneToOne(RO, W, st, 2, 1, 8.0);
        oneToOne(RO, W, st, 3, 1, 13.0);
        oneToOne(RO, W, st, 4, 1, 5.0);
        oneToOne(RO, W, st, 5, 1, 7.0);
        oneToOne(RO, W, st, 1, 2, 11.0);
        oneToOne(RO, W, st, 2, 2, 0.0);
        oneToOne(RO, W, st, 3, 2, 10.0);
        oneToOne(RO, W, st, 4, 2, 2.0);
        oneToOne(RO, W, st, 5, 2, 4.0);
        oneToOne(RO, W, st, 1, 3, 11.0);
        oneToOne(RO, W, st, 2, 3, 1.0);
        oneToOne(RO, W, st, 3, 3, 0.0);
        oneToOne(RO, W, st, 4, 3, 3.0);
        oneToOne(RO, W, st, 5, 3, 4.0);
        oneToOne(RO, W, st, 1, 4, 9.0);
        oneToOne(RO, W, st, 2, 4, 3.0);
        oneToOne(RO, W, st, 3, 4, 8.0);
        oneToOne(RO, W, st, 4, 4, 0.0);
        oneToOne(RO, W, st, 5, 4, 2.0);
        oneToOne(RO, W, st, 1, 5, 7.0);
        oneToOne(RO, W, st, 2, 5, 7.0);
        oneToOne(RO, W, st, 3, 5, 6.0);
        oneToOne(RO, W, st, 4, 5, 9.0);
        oneToOne(RO, W, st, 5, 5, 0.0);

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'reversed - edge_orientation', i, j)
        oneToOne(W, RO, st, 1, 1, 0.0);
        oneToOne(W, RO, st, 2, 1, 8.0);
        oneToOne(W, RO, st, 3, 1, 13.0);
        oneToOne(W, RO, st, 4, 1, 5.0);
        oneToOne(W, RO, st, 5, 1, 7.0);
        oneToOne(W, RO, st, 1, 2, 11.0);
        oneToOne(W, RO, st, 2, 2, 0.0);
        oneToOne(W, RO, st, 3, 2, 10.0);
        oneToOne(W, RO, st, 4, 2, 2.0);
        oneToOne(W, RO, st, 5, 2, 4.0);
        oneToOne(W, RO, st, 1, 3, 11.0);
        oneToOne(W, RO, st, 2, 3, 1.0);
        oneToOne(W, RO, st, 3, 3, 0.0);
        oneToOne(W, RO, st, 4, 3, 3.0);
        oneToOne(W, RO, st, 5, 3, 4.0);
        oneToOne(W, RO, st, 1, 4, 9.0);
        oneToOne(W, RO, st, 2, 4, 3.0);
        oneToOne(W, RO, st, 3, 4, 8.0);
        oneToOne(W, RO, st, 4, 4, 0.0);
        oneToOne(W, RO, st, 5, 4, 2.0);
        oneToOne(W, RO, st, 1, 5, 7.0);
        oneToOne(W, RO, st, 2, 5, 7.0);
        oneToOne(W, RO, st, 3, 5, 6.0);
        oneToOne(W, RO, st, 4, 5, 9.0);
        oneToOne(W, RO, st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', i, j)
        oneToOne(U, st, 1, 1, 0.0);
        oneToOne(U, st, 1, 2, 1.0);
        oneToOne(U, st, 1, 3, 2.0);
        oneToOne(U, st, 1, 4, 1.0);
        oneToOne(U, st, 1, 5, 1.0);
        oneToOne(U, st, 2, 1, 1.0);
        oneToOne(U, st, 2, 2, 0.0);
        oneToOne(U, st, 2, 3, 1.0);
        oneToOne(U, st, 2, 4, 1.0);
        oneToOne(U, st, 2, 5, 2.0);
        oneToOne(U, st, 3, 1, 2.0);
        oneToOne(U, st, 3, 2, 1.0);
        oneToOne(U, st, 3, 3, 0.0);
        oneToOne(U, st, 3, 4, 1.0);
        oneToOne(U, st, 3, 5, 1.0);
        oneToOne(U, st, 4, 1, 1.0);
        oneToOne(U, st, 4, 2, 1.0);
        oneToOne(U, st, 4, 3, 1.0);
        oneToOne(U, st, 4, 4, 0.0);
        oneToOne(U, st, 4, 5, 1.0);
        oneToOne(U, st, 5, 1, 1.0);
        oneToOne(U, st, 5, 2, 2.0);
        oneToOne(U, st, 5, 3, 1.0);
        oneToOne(U, st, 5, 4, 1.0);
        oneToOne(U, st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', 'weight', i, j)
        oneToOne(U, W, st, 1, 1, 0.0);
        oneToOne(U, W, st, 1, 2, 7.0);
        oneToOne(U, W, st, 1, 3, 8.0);
        oneToOne(U, W, st, 1, 4, 5.0);
        oneToOne(U, W, st, 1, 5, 7.0);
        oneToOne(U, W, st, 2, 1, 7.0);
        oneToOne(U, W, st, 2, 2, 0.0);
        oneToOne(U, W, st, 2, 3, 1.0);
        oneToOne(U, W, st, 2, 4, 2.0);
        oneToOne(U, W, st, 2, 5, 4.0);
        oneToOne(U, W, st, 3, 1, 8.0);
        oneToOne(U, W, st, 3, 2, 1.0);
        oneToOne(U, W, st, 3, 3, 0.0);
        oneToOne(U, W, st, 3, 4, 3.0);
        oneToOne(U, W, st, 3, 5, 4.0);
        oneToOne(U, W, st, 4, 1, 5.0);
        oneToOne(U, W, st, 4, 2, 2.0);
        oneToOne(U, W, st, 4, 3, 3.0);
        oneToOne(U, W, st, 4, 4, 0.0);
        oneToOne(U, W, st, 4, 5, 2.0);
        oneToOne(U, W, st, 5, 1, 7.0);
        oneToOne(U, W, st, 5, 2, 4.0);
        oneToOne(U, W, st, 5, 3, 4.0);
        oneToOne(U, W, st, 5, 4, 2.0);
        oneToOne(U, W, st, 5, 5, 0.0);

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'undirected', i, j)
        oneToOne(W, U, st, 1, 1, 0.0);
        oneToOne(W, U, st, 1, 2, 7.0);
        oneToOne(W, U, st, 1, 3, 8.0);
        oneToOne(W, U, st, 1, 4, 5.0);
        oneToOne(W, U, st, 1, 5, 7.0);
        oneToOne(W, U, st, 2, 1, 7.0);
        oneToOne(W, U, st, 2, 2, 0.0);
        oneToOne(W, U, st, 2, 3, 1.0);
        oneToOne(W, U, st, 2, 4, 2.0);
        oneToOne(W, U, st, 2, 5, 4.0);
        oneToOne(W, U, st, 3, 1, 8.0);
        oneToOne(W, U, st, 3, 2, 1.0);
        oneToOne(W, U, st, 3, 3, 0.0);
        oneToOne(W, U, st, 3, 4, 3.0);
        oneToOne(W, U, st, 3, 5, 4.0);
        oneToOne(W, U, st, 4, 1, 5.0);
        oneToOne(W, U, st, 4, 2, 2.0);
        oneToOne(W, U, st, 4, 3, 3.0);
        oneToOne(W, U, st, 4, 4, 0.0);
        oneToOne(W, U, st, 4, 5, 2.0);
        oneToOne(W, U, st, 5, 1, 7.0);
        oneToOne(W, U, st, 5, 2, 4.0);
        oneToOne(W, U, st, 5, 3, 4.0);
        oneToOne(W, U, st, 5, 4, 2.0);
        oneToOne(W, U, st, 5, 5, 0.0);
    }

    private void oneToOne(String orientation, String weight, Statement st, int source, int destination, double distance) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + source + ", " + destination + ")");
        assertTrue(rs.next());
        assertEquals(source, rs.getInt(ST_ShortestPathLength.SOURCE_INDEX));
        assertEquals(destination, rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX));
        assertEquals(distance, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
    }

    private void oneToOne(String orientation, Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne(orientation, null, st, source, destination, distance);
    }

    // ************************** One-to-All ****************************************

    @Test
    public void oneToAllDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', i)
        oneToAll(DO, st, 1, new double[]{0.0, 1.0, 2.0, 1.0, 1.0});
        oneToAll(DO, st, 2, new double[]{3.0, 0.0, 2.0, 1.0, 2.0});
        oneToAll(DO, st, 3, new double[]{2.0, 1.0, 0.0, 2.0, 1.0});
        oneToAll(DO, st, 4, new double[]{2.0, 1.0, 1.0, 0.0, 1.0});
        oneToAll(DO, st, 5, new double[]{1.0, 2.0, 1.0, 2.0, 0.0});
    }

    @Test
    public void oneToAllWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', 'weight', i)
        oneToAll(DO, W, st, 1, new double[]{0.0, 8.0, 13.0, 5.0, 7.0});
        oneToAll(DO, W, st, 2, new double[]{11.0, 0.0, 10.0, 2.0, 4.0});
        oneToAll(DO, W, st, 3, new double[]{11.0, 1.0, 0.0, 3.0, 4.0});
        oneToAll(DO, W, st, 4, new double[]{9.0, 3.0, 8.0, 0.0, 2.0});
        oneToAll(DO, W, st, 5, new double[]{7.0, 7.0, 6.0, 9.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'directed - edge_orientation', i)
        oneToAll(W, DO, st, 1, new double[]{0.0, 8.0, 13.0, 5.0, 7.0});
        oneToAll(W, DO, st, 2, new double[]{11.0, 0.0, 10.0, 2.0, 4.0});
        oneToAll(W, DO, st, 3, new double[]{11.0, 1.0, 0.0, 3.0, 4.0});
        oneToAll(W, DO, st, 4, new double[]{9.0, 3.0, 8.0, 0.0, 2.0});
        oneToAll(W, DO, st, 5, new double[]{7.0, 7.0, 6.0, 9.0, 0.0});
    }

    @Test
    public void oneToAllRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', i)
        oneToAll(RO, st, 1, new double[]{0.0, 3.0, 2.0, 2.0, 1.0});
        oneToAll(RO, st, 2, new double[]{1.0, 0.0, 1.0, 1.0, 2.0});
        oneToAll(RO, st, 3, new double[]{2.0, 2.0, 0.0, 1.0, 1.0});
        oneToAll(RO, st, 4, new double[]{1.0, 1.0, 2.0, 0.0, 2.0});
        oneToAll(RO, st, 5, new double[]{1.0, 2.0, 1.0, 1.0, 0.0});
    }

    @Test
    public void oneToAllWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', 'weight', i)
        oneToAll(RO, W, st, 1, new double[]{0.0, 11.0, 11.0, 9.0, 7.0});
        oneToAll(RO, W, st, 2, new double[]{8.0, 0.0, 1.0, 3.0, 7.0});
        oneToAll(RO, W, st, 3, new double[]{13.0, 10.0, 0.0, 8.0, 6.0});
        oneToAll(RO, W, st, 4, new double[]{5.0, 2.0, 3.0, 0.0, 9.0});
        oneToAll(RO, W, st, 5, new double[]{7.0, 4.0, 4.0, 2.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'reversed - edge_orientation', i)
        oneToAll(W, RO, st, 1, new double[]{0.0, 11.0, 11.0, 9.0, 7.0});
        oneToAll(W, RO, st, 2, new double[]{8.0, 0.0, 1.0, 3.0, 7.0});
        oneToAll(W, RO, st, 3, new double[]{13.0, 10.0, 0.0, 8.0, 6.0});
        oneToAll(W, RO, st, 4, new double[]{5.0, 2.0, 3.0, 0.0, 9.0});
        oneToAll(W, RO, st, 5, new double[]{7.0, 4.0, 4.0, 2.0, 0.0});
    }

    @Test
    public void oneToAllU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', i)
        oneToAll(U, st, 1, new double[]{0.0,  1.0,  2.0,  1.0,  1.0});
        oneToAll(U, st, 2, new double[]{1.0,  0.0,  1.0,  1.0,  2.0});
        oneToAll(U, st, 3, new double[]{2.0,  1.0,  0.0,  1.0,  1.0});
        oneToAll(U, st, 4, new double[]{1.0,  1.0,  1.0,  0.0,  1.0});
        oneToAll(U, st, 5, new double[]{1.0,  2.0,  1.0,  1.0,  0.0});
    }

    @Test
    public void oneToAllWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', 'weight', i)
        oneToAll(U, W, st, 1, new double[]{0.0, 7.0, 8.0, 5.0, 7.0});
        oneToAll(U, W, st, 2, new double[]{7.0, 0.0, 1.0, 2.0, 4.0});
        oneToAll(U, W, st, 3, new double[]{8.0, 1.0, 0.0, 3.0, 4.0});
        oneToAll(U, W, st, 4, new double[]{5.0, 2.0, 3.0, 0.0, 2.0});
        oneToAll(U, W, st, 5, new double[]{7.0, 4.0, 4.0, 2.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'undirected', i)
        oneToAll(W, U, st, 1, new double[]{0.0, 7.0, 8.0, 5.0, 7.0});
        oneToAll(W, U, st, 2, new double[]{7.0, 0.0, 1.0, 2.0, 4.0});
        oneToAll(W, U, st, 3, new double[]{8.0, 1.0, 0.0, 3.0, 4.0});
        oneToAll(W, U, st, 4, new double[]{5.0, 2.0, 3.0, 0.0, 2.0});
        oneToAll(W, U, st, 5, new double[]{7.0, 4.0, 4.0, 2.0, 0.0});
    }

    private void oneToAll(String orientation, String weight, Statement st, int source, double[] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + source + ")");
        int count = 0;
        while (rs.next()) {
            final int returnedSource = rs.getInt(ST_ShortestPathLength.SOURCE_INDEX);
            assertEquals(source, returnedSource);
            final int destination = rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX);
            final double distance = rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX);
            assertEquals(distances[destination - 1], distance, TOLERANCE);
            count++;
        }
        assertEquals(5, count);
    }

    private void oneToAll(String orientation, Statement st, int source, double[] distances) throws SQLException {
        oneToAll(orientation, null, st, source, distances);
    }

    // ************************** Many-to-Many ****************************************

    @Test
    public void manyToManyDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', 'source_dest')
        final double[][] distances = {{0.0, 1.0, 2.0, 1.0, 1.0},
                                      {3.0, 0.0, 2.0, 1.0, 2.0},
                                      {2.0, 1.0, 0.0, 2.0, 1.0},
                                      {2.0, 1.0, 1.0, 0.0, 1.0},
                                      {1.0, 2.0, 1.0, 2.0, 0.0}};
        manyToMany(DO, st, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', 'weight', 'source_dest')
        final double[][] distances = {{0.0, 8.0, 13.0, 5.0, 7.0},
                                      {11.0, 0.0, 10.0, 2.0, 4.0},
                                      {11.0, 1.0, 0.0, 3.0, 4.0},
                                      {9.0, 3.0, 8.0, 0.0, 2.0},
                                      {7.0, 7.0, 6.0, 9.0, 0.0}};
        manyToMany(DO, W, st, SOURCE_DEST_TABLE, distances);
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'directed - edge_orientation', 'source_dest')
        manyToMany(W, DO, st, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', 'source_dest')
        final double[][] distances = {{0.0, 3.0, 2.0, 2.0, 1.0},
                                      {1.0, 0.0, 1.0, 1.0, 2.0},
                                      {2.0, 2.0, 0.0, 1.0, 1.0},
                                      {1.0, 1.0, 2.0, 0.0, 2.0},
                                      {1.0, 2.0, 1.0, 1.0, 0.0}};
        manyToMany(RO, st, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', 'weight', 'source_dest')
        final double[][] distances = {{0.0, 11.0, 11.0, 9.0, 7.0},
                                      {8.0, 0.0, 1.0, 3.0, 7.0},
                                      {13.0, 10.0, 0.0, 8.0, 6.0},
                                      {5.0, 2.0, 3.0, 0.0, 9.0},
                                      {7.0, 4.0, 4.0, 2.0, 0.0}};
        manyToMany(RO, W, st, SOURCE_DEST_TABLE, distances);
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'reversed - edge_orientation', 'source_dest')
        manyToMany(W, RO, st, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', 'source_dest')
        final double[][] distances = {{0.0,  1.0,  2.0,  1.0,  1.0},
                                      {1.0,  0.0,  1.0,  1.0,  2.0},
                                      {2.0,  1.0,  0.0,  1.0,  1.0},
                                      {1.0,  1.0,  1.0,  0.0,  1.0},
                                      {1.0,  2.0,  1.0,  1.0,  0.0}};
        manyToMany(U, st, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', 'weight', 'source_dest')
        final double[][] distances = {{0.0, 7.0, 8.0, 5.0, 7.0},
                                      {7.0, 0.0, 1.0, 2.0, 4.0},
                                      {8.0, 1.0, 0.0, 3.0, 4.0},
                                      {5.0, 2.0, 3.0, 0.0, 2.0},
                                      {7.0, 4.0, 4.0, 2.0, 0.0}};
        manyToMany(U, W, st, SOURCE_DEST_TABLE, distances);
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'undirected', 'source_dest')
        manyToMany(W, U, st, SOURCE_DEST_TABLE, distances);
    }

    private void manyToMany(String orientation, String weight, Statement st,
                            String sourceDestinationTable, double[][] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + sourceDestinationTable + ")");
        int count = 0;
        while (rs.next()) {
            final int source = rs.getInt(ST_ShortestPathLength.SOURCE_INDEX);
            final int destination = rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX);
            final double distance = rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX);
            assertEquals(distances[source - 1][destination - 1], distance, TOLERANCE);
            count++;
        }
        assertEquals(25, count);
    }

    private void manyToMany(String orientation, Statement st,
                            String sourceDestinationTable, double[][] distances) throws SQLException {
        manyToMany(orientation, null, st, sourceDestinationTable, distances);
    }

    // ************************** Many-to-Many ****************************************

    @Test
    public void oneToSeveralDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(DO, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 1.0, 2.0, 1.0, 1.0});
        oneToSeveral(DO, st, 2, "'1, 2, 3, 4, 5'", new double[]{3.0, 0.0, 2.0, 1.0, 2.0});
        oneToSeveral(DO, st, 3, "'1, 2, 3, 4, 5'", new double[]{2.0, 1.0, 0.0, 2.0, 1.0});
        oneToSeveral(DO, st, 4, "'1, 2, 3, 4, 5'", new double[]{2.0, 1.0, 1.0, 0.0, 1.0});
        oneToSeveral(DO, st, 5, "'1, 2, 3, 4, 5'", new double[]{1.0, 2.0, 1.0, 2.0, 0.0});
    }

    @Test
    public void oneToSeveralWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'directed - edge_orientation', 'weight', i, '1, 2, 3, 4, 5')
        oneToSeveral(DO, W, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 8.0, 13.0, 5.0, 7.0});
        oneToSeveral(DO, W, st, 2, "'1, 2, 3, 4, 5'", new double[]{11.0, 0.0, 10.0, 2.0, 4.0});
        oneToSeveral(DO, W, st, 3, "'1, 2, 3, 4, 5'", new double[]{11.0, 1.0, 0.0, 3.0, 4.0});
        oneToSeveral(DO, W, st, 4, "'1, 2, 3, 4, 5'", new double[]{9.0, 3.0, 8.0, 0.0, 2.0});
        oneToSeveral(DO, W, st, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 7.0, 6.0, 9.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'directed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(W, DO, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 8.0, 13.0, 5.0, 7.0});
        oneToSeveral(W, DO, st, 2, "'1, 2, 3, 4, 5'", new double[]{11.0, 0.0, 10.0, 2.0, 4.0});
        oneToSeveral(W, DO, st, 3, "'1, 2, 3, 4, 5'", new double[]{11.0, 1.0, 0.0, 3.0, 4.0});
        oneToSeveral(W, DO, st, 4, "'1, 2, 3, 4, 5'", new double[]{9.0, 3.0, 8.0, 0.0, 2.0});
        oneToSeveral(W, DO, st, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 7.0, 6.0, 9.0, 0.0});
    }

    @Test
    public void oneToSeveralRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(RO, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 3.0, 2.0, 2.0, 1.0});
        oneToSeveral(RO, st, 2, "'1, 2, 3, 4, 5'", new double[]{1.0, 0.0, 1.0, 1.0, 2.0});
        oneToSeveral(RO, st, 3, "'1, 2, 3, 4, 5'", new double[]{2.0, 2.0, 0.0, 1.0, 1.0});
        oneToSeveral(RO, st, 4, "'1, 2, 3, 4, 5'", new double[]{1.0, 1.0, 2.0, 0.0, 2.0});
        oneToSeveral(RO, st, 5, "'1, 2, 3, 4, 5'", new double[]{1.0, 2.0, 1.0, 1.0, 0.0});
    }

    @Test
    public void oneToSeveralWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'reversed - edge_orientation', 'weight', i, '1, 2, 3, 4, 5')
        oneToSeveral(RO, W, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 11.0, 11.0, 9.0, 7.0});
        oneToSeveral(RO, W, st, 2, "'1, 2, 3, 4, 5'", new double[]{8.0, 0.0, 1.0, 3.0, 7.0});
        oneToSeveral(RO, W, st, 3, "'1, 2, 3, 4, 5'", new double[]{13.0, 10.0, 0.0, 8.0, 6.0});
        oneToSeveral(RO, W, st, 4, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 3.0, 0.0, 9.0});
        oneToSeveral(RO, W, st, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 4.0, 2.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'reversed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(W, RO, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 11.0, 11.0, 9.0, 7.0});
        oneToSeveral(W, RO, st, 2, "'1, 2, 3, 4, 5'", new double[]{8.0, 0.0, 1.0, 3.0, 7.0});
        oneToSeveral(W, RO, st, 3, "'1, 2, 3, 4, 5'", new double[]{13.0, 10.0, 0.0, 8.0, 6.0});
        oneToSeveral(W, RO, st, 4, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 3.0, 0.0, 9.0});
        oneToSeveral(W, RO, st, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 4.0, 2.0, 0.0});
    }

    @Test
    public void oneToSeveralU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', i, '1, 2, 3, 4, 5')
        oneToSeveral(U, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0,  1.0,  2.0,  1.0,  1.0});
        oneToSeveral(U, st, 2, "'1, 2, 3, 4, 5'", new double[]{1.0,  0.0,  1.0,  1.0,  2.0});
        oneToSeveral(U, st, 3, "'1, 2, 3, 4, 5'", new double[]{2.0,  1.0,  0.0,  1.0,  1.0});
        oneToSeveral(U, st, 4, "'1, 2, 3, 4, 5'", new double[]{1.0,  1.0,  1.0,  0.0,  1.0});
        oneToSeveral(U, st, 5, "'1, 2, 3, 4, 5'", new double[]{1.0,  2.0,  1.0,  1.0,  0.0});
    }

    @Test
    public void oneToSeveralWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'undirected', 'weight', i, '1, 2, 3, 4, 5')
        oneToSeveral(U, W, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 7.0, 8.0, 5.0, 7.0});
        oneToSeveral(U, W, st, 2, "'1, 2, 3, 4, 5'", new double[]{7.0, 0.0, 1.0, 2.0, 4.0});
        oneToSeveral(U, W, st, 3, "'1, 2, 3, 4, 5'", new double[]{8.0, 1.0, 0.0, 3.0, 4.0});
        oneToSeveral(U, W, st, 4, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 3.0, 0.0, 2.0});
        oneToSeveral(U, W, st, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 4.0, 2.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges',
        //     'weight', 'undirected', i, '1, 2, 3, 4, 5')
        oneToSeveral(W, U, st, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 7.0, 8.0, 5.0, 7.0});
        oneToSeveral(W, U, st, 2, "'1, 2, 3, 4, 5'", new double[]{7.0, 0.0, 1.0, 2.0, 4.0});
        oneToSeveral(W, U, st, 3, "'1, 2, 3, 4, 5'", new double[]{8.0, 1.0, 0.0, 3.0, 4.0});
        oneToSeveral(W, U, st, 4, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 3.0, 0.0, 2.0});
        oneToSeveral(W, U, st, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 4.0, 2.0, 0.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneToSeveralFail() throws Throwable {
        try {
            // The graph does not contain vertex 7.
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges', 'undirected', 1, '2, 7')");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    private void oneToSeveral(String orientation, String weight, Statement st, int source, String destinationString, double[] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + source + ", " + destinationString + ")");
        int count = 0;
        while (rs.next()) {
            final int returnedSource = rs.getInt(ST_ShortestPathLength.SOURCE_INDEX);
            assertEquals(source, returnedSource);
            final int destination = rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX);
            final double distance = rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX);
            assertEquals(distances[destination - 1], distance, TOLERANCE);
            count++;
        }
        assertEquals(distances.length, count);
    }

    private void oneToSeveral(String orientation, Statement st, int source, String destinationString, double[] distances) throws SQLException {
        oneToSeveral(orientation, null, st, source, destinationString, distances);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arg3Fail() throws Throwable {
        try {
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges', 'undirected', 2.0)");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void arg4Fail() throws Throwable {
        try {
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges', 'undirected', 1, 2.0)");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void arg5Fail() throws Throwable {
        try {
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges', 'undirected', 'weight', 1, 2.0)");
        } catch (JdbcSQLException e) {
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
        // Vertices 3 and 6 are in different connected components.
        st.execute("CALL ST_Graph('COPY', 'road')");
        ResultSet rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges', " +
                "'undirected', 3, 6)");
        assertTrue(rs.next());
        assertEquals(Double.POSITIVE_INFINITY, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        // 7 is reachable from 6.
        rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges', " +
                "'directed - edge_orientation', 6, 7)");
        assertTrue(rs.next());
        assertEquals(1.0, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        // But 6 is not reachable from 7 in a directed graph.
        rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges', " +
                "'directed - edge_orientation', 7, 6)");
        assertTrue(rs.next());
        assertEquals(Double.POSITIVE_INFINITY, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        // It is, however, in an undirected graph.
        rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges', " +
                "'undirected', 7, 6)");
        assertTrue(rs.next());
        assertEquals(1.0, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        st.execute("DROP TABLE copy");
        st.execute("DROP TABLE copy_nodes");
        st.execute("DROP TABLE copy_edges");
    }
}
