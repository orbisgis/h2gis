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
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', i, j)
        oneToOne(DO, 1, 1, 0.0);
        oneToOne(DO, 1, 2, 1.0);
        oneToOne(DO, 1, 3, 1.0);
        oneToOne(DO, 1, 4, 2.0);
        oneToOne(DO, 1, 5, 1.0);
        oneToOne(DO, 2, 1, 3.0);
        oneToOne(DO, 2, 2, 0.0);
        oneToOne(DO, 2, 3, 1.0);
        oneToOne(DO, 2, 4, 2.0);
        oneToOne(DO, 2, 5, 2.0);
        oneToOne(DO, 3, 1, 2.0);
        oneToOne(DO, 3, 2, 1.0);
        oneToOne(DO, 3, 3, 0.0);
        oneToOne(DO, 3, 4, 1.0);
        oneToOne(DO, 3, 5, 1.0);
        oneToOne(DO, 4, 1, 2.0);
        oneToOne(DO, 4, 2, 1.0);
        oneToOne(DO, 4, 3, 2.0);
        oneToOne(DO, 4, 4, 0.0);
        oneToOne(DO, 4, 5, 1.0);
        oneToOne(DO, 5, 1, 1.0);
        oneToOne(DO, 5, 2, 2.0);
        oneToOne(DO, 5, 3, 2.0);
        oneToOne(DO, 5, 4, 1.0);
        oneToOne(DO, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'directed - edge_orientation', i, j)
        oneToOne(DO, W, 1, 1, 0.0);
        oneToOne(DO, W, 1, 2, 8.0);
        oneToOne(DO, W, 1, 3, 5.0);
        oneToOne(DO, W, 1, 4, 13.0);
        oneToOne(DO, W, 1, 5, 7.0);
        oneToOne(DO, W, 2, 1, 11.0);
        oneToOne(DO, W, 2, 2, 0.0);
        oneToOne(DO, W, 2, 3, 2.0);
        oneToOne(DO, W, 2, 4, 10.0);
        oneToOne(DO, W, 2, 5, 4.0);
        oneToOne(DO, W, 3, 1, 9.0);
        oneToOne(DO, W, 3, 2, 3.0);
        oneToOne(DO, W, 3, 3, 0.0);
        oneToOne(DO, W, 3, 4, 8.0);
        oneToOne(DO, W, 3, 5, 2.0);
        oneToOne(DO, W, 4, 1, 11.0);
        oneToOne(DO, W, 4, 2, 1.0);
        oneToOne(DO, W, 4, 3, 3.0);
        oneToOne(DO, W, 4, 4, 0.0);
        oneToOne(DO, W, 4, 5, 4.0);
        oneToOne(DO, W, 5, 1, 7.0);
        oneToOne(DO, W, 5, 2, 7.0);
        oneToOne(DO, W, 5, 3, 9.0);
        oneToOne(DO, W, 5, 4, 6.0);
        oneToOne(DO, W, 5, 5, 0.0);

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', 'weight', i, j)
        oneToOne(W, DO, 1, 1, 0.0);
        oneToOne(W, DO, 1, 2, 8.0);
        oneToOne(W, DO, 1, 3, 5.0);
        oneToOne(W, DO, 1, 4, 13.0);
        oneToOne(W, DO, 1, 5, 7.0);
        oneToOne(W, DO, 2, 1, 11.0);
        oneToOne(W, DO, 2, 2, 0.0);
        oneToOne(W, DO, 2, 3, 2.0);
        oneToOne(W, DO, 2, 4, 10.0);
        oneToOne(W, DO, 2, 5, 4.0);
        oneToOne(W, DO, 3, 1, 9.0);
        oneToOne(W, DO, 3, 2, 3.0);
        oneToOne(W, DO, 3, 3, 0.0);
        oneToOne(W, DO, 3, 4, 8.0);
        oneToOne(W, DO, 3, 5, 2.0);
        oneToOne(W, DO, 4, 1, 11.0);
        oneToOne(W, DO, 4, 2, 1.0);
        oneToOne(W, DO, 4, 3, 3.0);
        oneToOne(W, DO, 4, 4, 0.0);
        oneToOne(W, DO, 4, 5, 4.0);
        oneToOne(W, DO, 5, 1, 7.0);
        oneToOne(W, DO, 5, 2, 7.0);
        oneToOne(W, DO, 5, 3, 9.0);
        oneToOne(W, DO, 5, 4, 6.0);
        oneToOne(W, DO, 5, 5, 0.0);
    }

    @Test
    public void oneToOneRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', i, j)
        oneToOne(RO, 1, 1, 0.0);
        oneToOne(RO, 2, 1, 1.0);
        oneToOne(RO, 3, 1, 1.0);
        oneToOne(RO, 4, 1, 2.0);
        oneToOne(RO, 5, 1, 1.0);
        oneToOne(RO, 1, 2, 3.0);
        oneToOne(RO, 2, 2, 0.0);
        oneToOne(RO, 3, 2, 1.0);
        oneToOne(RO, 4, 2, 2.0);
        oneToOne(RO, 5, 2, 2.0);
        oneToOne(RO, 1, 3, 2.0);
        oneToOne(RO, 2, 3, 1.0);
        oneToOne(RO, 3, 3, 0.0);
        oneToOne(RO, 4, 3, 1.0);
        oneToOne(RO, 5, 3, 1.0);
        oneToOne(RO, 1, 4, 2.0);
        oneToOne(RO, 2, 4, 1.0);
        oneToOne(RO, 3, 4, 2.0);
        oneToOne(RO, 4, 4, 0.0);
        oneToOne(RO, 5, 4, 1.0);
        oneToOne(RO, 1, 5, 1.0);
        oneToOne(RO, 2, 5, 2.0);
        oneToOne(RO, 3, 5, 2.0);
        oneToOne(RO, 4, 5, 1.0);
        oneToOne(RO, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', 'weight', i, j)
        oneToOne(RO, W, 1, 1, 0.0);
        oneToOne(RO, W, 2, 1, 8.0);
        oneToOne(RO, W, 3, 1, 5.0);
        oneToOne(RO, W, 4, 1, 13.0);
        oneToOne(RO, W, 5, 1, 7.0);
        oneToOne(RO, W, 1, 2, 11.0);
        oneToOne(RO, W, 2, 2, 0.0);
        oneToOne(RO, W, 3, 2, 2.0);
        oneToOne(RO, W, 4, 2, 10.0);
        oneToOne(RO, W, 5, 2, 4.0);
        oneToOne(RO, W, 1, 3, 9.0);
        oneToOne(RO, W, 2, 3, 3.0);
        oneToOne(RO, W, 3, 3, 0.0);
        oneToOne(RO, W, 4, 3, 8.0);
        oneToOne(RO, W, 5, 3, 2.0);
        oneToOne(RO, W, 1, 4, 11.0);
        oneToOne(RO, W, 2, 4, 1.0);
        oneToOne(RO, W, 3, 4, 3.0);
        oneToOne(RO, W, 4, 4, 0.0);
        oneToOne(RO, W, 5, 4, 4.0);
        oneToOne(RO, W, 1, 5, 7.0);
        oneToOne(RO, W, 2, 5, 7.0);
        oneToOne(RO, W, 3, 5, 9.0);
        oneToOne(RO, W, 4, 5, 6.0);
        oneToOne(RO, W, 5, 5, 0.0);

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'reversed - edge_orientation', i, j)
        oneToOne(W, RO, 1, 1, 0.0);
        oneToOne(W, RO, 2, 1, 8.0);
        oneToOne(W, RO, 3, 1, 5.0);
        oneToOne(W, RO, 4, 1, 13.0);
        oneToOne(W, RO, 5, 1, 7.0);
        oneToOne(W, RO, 1, 2, 11.0);
        oneToOne(W, RO, 2, 2, 0.0);
        oneToOne(W, RO, 3, 2, 2.0);
        oneToOne(W, RO, 4, 2, 10.0);
        oneToOne(W, RO, 5, 2, 4.0);
        oneToOne(W, RO, 1, 3, 9.0);
        oneToOne(W, RO, 2, 3, 3.0);
        oneToOne(W, RO, 3, 3, 0.0);
        oneToOne(W, RO, 4, 3, 8.0);
        oneToOne(W, RO, 5, 3, 2.0);
        oneToOne(W, RO, 1, 4, 11.0);
        oneToOne(W, RO, 2, 4, 1.0);
        oneToOne(W, RO, 3, 4, 3.0);
        oneToOne(W, RO, 4, 4, 0.0);
        oneToOne(W, RO, 5, 4, 4.0);
        oneToOne(W, RO, 1, 5, 7.0);
        oneToOne(W, RO, 2, 5, 7.0);
        oneToOne(W, RO, 3, 5, 9.0);
        oneToOne(W, RO, 4, 5, 6.0);
        oneToOne(W, RO, 5, 5, 0.0);
    }

    @Test
    public void oneToOneU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', i, j)
        oneToOne(U, 1, 1, 0.0);
        oneToOne(U, 1, 2, 1.0);
        oneToOne(U, 1, 3, 1.0);
        oneToOne(U, 1, 4, 2.0);
        oneToOne(U, 1, 5, 1.0);
        oneToOne(U, 2, 1, 1.0);
        oneToOne(U, 2, 2, 0.0);
        oneToOne(U, 2, 3, 1.0);
        oneToOne(U, 2, 4, 1.0);
        oneToOne(U, 2, 5, 2.0);
        oneToOne(U, 3, 1, 1.0);
        oneToOne(U, 3, 2, 1.0);
        oneToOne(U, 3, 3, 0.0);
        oneToOne(U, 3, 4, 1.0);
        oneToOne(U, 3, 5, 1.0);
        oneToOne(U, 4, 1, 2.0);
        oneToOne(U, 4, 2, 1.0);
        oneToOne(U, 4, 3, 1.0);
        oneToOne(U, 4, 4, 0.0);
        oneToOne(U, 4, 5, 1.0);
        oneToOne(U, 5, 1, 1.0);
        oneToOne(U, 5, 2, 2.0);
        oneToOne(U, 5, 3, 1.0);
        oneToOne(U, 5, 4, 1.0);
        oneToOne(U, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', 'weight', i, j)
        oneToOne(U, W, 1, 1, 0.0);
        oneToOne(U, W, 1, 2, 7.0);
        oneToOne(U, W, 1, 3, 5.0);
        oneToOne(U, W, 1, 4, 8.0);
        oneToOne(U, W, 1, 5, 7.0);
        oneToOne(U, W, 2, 1, 7.0);
        oneToOne(U, W, 2, 2, 0.0);
        oneToOne(U, W, 2, 3, 2.0);
        oneToOne(U, W, 2, 4, 1.0);
        oneToOne(U, W, 2, 5, 4.0);
        oneToOne(U, W, 3, 1, 5.0);
        oneToOne(U, W, 3, 2, 2.0);
        oneToOne(U, W, 3, 3, 0.0);
        oneToOne(U, W, 3, 4, 3.0);
        oneToOne(U, W, 3, 5, 2.0);
        oneToOne(U, W, 4, 1, 8.0);
        oneToOne(U, W, 4, 2, 1.0);
        oneToOne(U, W, 4, 3, 3.0);
        oneToOne(U, W, 4, 4, 0.0);
        oneToOne(U, W, 4, 5, 4.0);
        oneToOne(U, W, 5, 1, 7.0);
        oneToOne(U, W, 5, 2, 4.0);
        oneToOne(U, W, 5, 3, 2.0);
        oneToOne(U, W, 5, 4, 4.0);
        oneToOne(U, W, 5, 5, 0.0);

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'undirected', i, j)
        oneToOne(W, U, 1, 1, 0.0);
        oneToOne(W, U, 1, 2, 7.0);
        oneToOne(W, U, 1, 3, 5.0);
        oneToOne(W, U, 1, 4, 8.0);
        oneToOne(W, U, 1, 5, 7.0);
        oneToOne(W, U, 2, 1, 7.0);
        oneToOne(W, U, 2, 2, 0.0);
        oneToOne(W, U, 2, 3, 2.0);
        oneToOne(W, U, 2, 4, 1.0);
        oneToOne(W, U, 2, 5, 4.0);
        oneToOne(W, U, 3, 1, 5.0);
        oneToOne(W, U, 3, 2, 2.0);
        oneToOne(W, U, 3, 3, 0.0);
        oneToOne(W, U, 3, 4, 3.0);
        oneToOne(W, U, 3, 5, 2.0);
        oneToOne(W, U, 4, 1, 8.0);
        oneToOne(W, U, 4, 2, 1.0);
        oneToOne(W, U, 4, 3, 3.0);
        oneToOne(W, U, 4, 4, 0.0);
        oneToOne(W, U, 4, 5, 4.0);
        oneToOne(W, U, 5, 1, 7.0);
        oneToOne(W, U, 5, 2, 4.0);
        oneToOne(W, U, 5, 3, 2.0);
        oneToOne(W, U, 5, 4, 4.0);
        oneToOne(W, U, 5, 5, 0.0);
    }

    private void oneToOne(String orientation, String weight, int source, int destination, double distance) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges_all', "
                        + orientation + ((weight != null) ? ", " + weight : "")
                        + ", " + source + ", " + destination + ")");
        assertTrue(rs.next());
        assertEquals(source, rs.getInt(ST_ShortestPathLength.SOURCE_INDEX));
        assertEquals(destination, rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX));
        assertEquals(distance, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        rs.close();
    }

    private void oneToOne(String orientation, int source, int destination, double distance) throws SQLException {
        oneToOne(orientation, null, source, destination, distance);
    }

    // ************************** One-to-All ****************************************

    @Test
    public void oneToAllDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', i)
        oneToAll(DO, 1, new double[]{0.0, 1.0, 1.0, 2.0, 1.0});
        oneToAll(DO, 2, new double[]{3.0, 0.0, 1.0, 2.0, 2.0});
        oneToAll(DO, 3, new double[]{2.0, 1.0, 0.0, 1.0, 1.0});
        oneToAll(DO, 4, new double[]{2.0, 1.0, 2.0, 0.0, 1.0});
        oneToAll(DO, 5, new double[]{1.0, 2.0, 2.0, 1.0, 0.0});
    }

    @Test
    public void oneToAllWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', 'weight', i)
        oneToAll(DO, W, 1, new double[]{0.0, 8.0, 5.0, 13.0, 7.0});
        oneToAll(DO, W, 2, new double[]{11.0, 0.0, 2.0, 10.0, 4.0});
        oneToAll(DO, W, 3, new double[]{9.0, 3.0, 0.0, 8.0, 2.0});
        oneToAll(DO, W, 4, new double[]{11.0, 1.0, 3.0, 0.0, 4.0});
        oneToAll(DO, W, 5, new double[]{7.0, 7.0, 9.0, 6.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'directed - edge_orientation', i)
        oneToAll(W, DO, 1, new double[]{0.0, 8.0, 5.0, 13.0, 7.0});
        oneToAll(W, DO, 2, new double[]{11.0, 0.0, 2.0, 10.0, 4.0});
        oneToAll(W, DO, 3, new double[]{9.0, 3.0, 0.0, 8.0, 2.0});
        oneToAll(W, DO, 4, new double[]{11.0, 1.0, 3.0, 0.0, 4.0});
        oneToAll(W, DO, 5, new double[]{7.0, 7.0, 9.0, 6.0, 0.0});
    }

    @Test
    public void oneToAllRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', i)
        oneToAll(RO, 1, new double[]{0.0, 3.0, 2.0, 2.0, 1.0});
        oneToAll(RO, 2, new double[]{1.0, 0.0, 1.0, 1.0, 2.0});
        oneToAll(RO, 3, new double[]{1.0, 1.0, 0.0, 2.0, 2.0});
        oneToAll(RO, 4, new double[]{2.0, 2.0, 1.0, 0.0, 1.0});
        oneToAll(RO, 5, new double[]{1.0, 2.0, 1.0, 1.0, 0.0});
    }

    @Test
    public void oneToAllWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', 'weight', i)
        oneToAll(RO, W, 1, new double[]{0.0, 11.0, 9.0, 11.0, 7.0});
        oneToAll(RO, W, 2, new double[]{8.0, 0.0, 3.0, 1.0, 7.0});
        oneToAll(RO, W, 3, new double[]{5.0, 2.0, 0.0, 3.0, 9.0});
        oneToAll(RO, W, 4, new double[]{13.0, 10.0, 8.0, 0.0, 6.0});
        oneToAll(RO, W, 5, new double[]{7.0, 4.0, 2.0, 4.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'reversed - edge_orientation', i)
        oneToAll(W, RO, 1, new double[]{0.0, 11.0, 9.0, 11.0, 7.0});
        oneToAll(W, RO, 2, new double[]{8.0, 0.0, 3.0, 1.0, 7.0});
        oneToAll(W, RO, 3, new double[]{5.0, 2.0, 0.0, 3.0, 9.0});
        oneToAll(W, RO, 4, new double[]{13.0, 10.0, 8.0, 0.0, 6.0});
        oneToAll(W, RO, 5, new double[]{7.0, 4.0, 2.0, 4.0, 0.0});
    }

    @Test
    public void oneToAllU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', i)
        oneToAll(U, 1, new double[]{0.0,  1.0,  1.0,  2.0,  1.0});
        oneToAll(U, 2, new double[]{1.0,  0.0,  1.0,  1.0,  2.0});
        oneToAll(U, 3, new double[]{1.0,  1.0,  0.0,  1.0,  1.0});
        oneToAll(U, 4, new double[]{2.0,  1.0,  1.0,  0.0,  1.0});
        oneToAll(U, 5, new double[]{1.0,  2.0,  1.0,  1.0,  0.0});
    }

    @Test
    public void oneToAllWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', 'weight', i)
        oneToAll(U, W, 1, new double[]{0.0, 7.0, 5.0, 8.0, 7.0});
        oneToAll(U, W, 2, new double[]{7.0, 0.0, 2.0, 1.0, 4.0});
        oneToAll(U, W, 3, new double[]{5.0, 2.0, 0.0, 3.0, 2.0});
        oneToAll(U, W, 4, new double[]{8.0, 1.0, 3.0, 0.0, 4.0});
        oneToAll(U, W, 5, new double[]{7.0, 4.0, 2.0, 4.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'undirected', i)
        oneToAll(W, U, 1, new double[]{0.0, 7.0, 5.0, 8.0, 7.0});
        oneToAll(W, U, 2, new double[]{7.0, 0.0, 2.0, 1.0, 4.0});
        oneToAll(W, U, 3, new double[]{5.0, 2.0, 0.0, 3.0, 2.0});
        oneToAll(W, U, 4, new double[]{8.0, 1.0, 3.0, 0.0, 4.0});
        oneToAll(W, U, 5, new double[]{7.0, 4.0, 2.0, 4.0, 0.0});
    }

    private void oneToAll(String orientation, String weight, int source, double[] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges_all', "
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
        rs.close();
    }

    private void oneToAll(String orientation, int source, double[] distances) throws SQLException {
        oneToAll(orientation, null, source, distances);
    }

    // ************************** Many-to-Many ****************************************

    @Test
    public void manyToManyDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', 'source_dest')
        final double[][] distances = {{0.0, 1.0, 1.0, 2.0, 1.0},
                                      {3.0, 0.0, 1.0, 2.0, 2.0},
                                      {2.0, 1.0, 0.0, 1.0, 1.0},
                                      {2.0, 1.0, 2.0, 0.0, 1.0},
                                      {1.0, 2.0, 2.0, 1.0, 0.0}};
        manyToMany(DO, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', 'weight', 'source_dest')
        final double[][] distances = {{0.0, 8.0, 5.0, 13.0, 7.0},
                                      {11.0, 0.0, 2.0, 10.0, 4.0},
                                      {9.0, 3.0, 0.0, 8.0, 2.0},
                                      {11.0, 1.0, 3.0, 0.0, 4.0},
                                      {7.0, 7.0, 9.0, 6.0, 0.0}};
        manyToMany(DO, W, SOURCE_DEST_TABLE, distances);
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'directed - edge_orientation', 'source_dest')
        manyToMany(W, DO, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', 'source_dest')
        final double[][] distances = {{0.0, 3.0, 2.0, 2.0, 1.0},
                                      {1.0, 0.0, 1.0, 1.0, 2.0},
                                      {1.0, 1.0, 0.0, 2.0, 2.0},
                                      {2.0, 2.0, 1.0, 0.0, 1.0},
                                      {1.0, 2.0, 1.0, 1.0, 0.0}};
        manyToMany(RO, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', 'weight', 'source_dest')
        final double[][] distances = {{0.0, 11.0, 9.0, 11.0, 7.0},
                                      {8.0, 0.0, 3.0, 1.0, 7.0},
                                      {5.0, 2.0, 0.0, 3.0, 9.0},
                                      {13.0, 10.0, 8.0, 0.0, 6.0},
                                      {7.0, 4.0, 2.0, 4.0, 0.0}};
        manyToMany(RO, W, SOURCE_DEST_TABLE, distances);
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'reversed - edge_orientation', 'source_dest')
        manyToMany(W, RO, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', 'source_dest')
        final double[][] distances = {{0.0,  1.0,  1.0,  2.0,  1.0},
                                      {1.0,  0.0,  1.0,  1.0,  2.0},
                                      {1.0,  1.0,  0.0,  1.0,  1.0},
                                      {2.0,  1.0,  1.0,  0.0,  1.0},
                                      {1.0,  2.0,  1.0,  1.0,  0.0}};
        manyToMany(U, SOURCE_DEST_TABLE, distances);
    }

    @Test
    public void manyToManyWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', 'weight', 'source_dest')
        final double[][] distances = {{0.0, 7.0, 5.0, 8.0, 7.0},
                                      {7.0, 0.0, 2.0, 1.0, 4.0},
                                      {5.0, 2.0, 0.0, 3.0, 2.0},
                                      {8.0, 1.0, 3.0, 0.0, 4.0},
                                      {7.0, 4.0, 2.0, 4.0, 0.0}};
        manyToMany(U, W, SOURCE_DEST_TABLE, distances);
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'undirected', 'source_dest')
        manyToMany(W, U, SOURCE_DEST_TABLE, distances);
    }

    private void manyToMany(String orientation, String weight,
                            String sourceDestinationTable, double[][] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges_all', "
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
        rs.close();
    }

    private void manyToMany(String orientation,
                            String sourceDestinationTable, double[][] distances) throws SQLException {
        manyToMany(orientation, null, sourceDestinationTable, distances);
    }

    // ************************** Many-to-Many ****************************************

    @Test
    public void oneToSeveralDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(DO, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 1.0, 1.0, 2.0, 1.0});
        oneToSeveral(DO, 2, "'1, 2, 3, 4, 5'", new double[]{3.0, 0.0, 1.0, 2.0, 2.0});
        oneToSeveral(DO, 3, "'1, 2, 3, 4, 5'", new double[]{2.0, 1.0, 0.0, 1.0, 1.0});
        oneToSeveral(DO, 4, "'1, 2, 3, 4, 5'", new double[]{2.0, 1.0, 2.0, 0.0, 1.0});
        oneToSeveral(DO, 5, "'1, 2, 3, 4, 5'", new double[]{1.0, 2.0, 2.0, 1.0, 0.0});
    }

    @Test
    public void oneToSeveralWDO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'directed - edge_orientation', 'weight', i, '1, 2, 3, 4, 5')
        oneToSeveral(DO, W, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 8.0, 5.0, 13.0, 7.0});
        oneToSeveral(DO, W, 2, "'1, 2, 3, 4, 5'", new double[]{11.0, 0.0, 2.0, 10.0, 4.0});
        oneToSeveral(DO, W, 3, "'1, 2, 3, 4, 5'", new double[]{9.0, 3.0, 0.0, 8.0, 2.0});
        oneToSeveral(DO, W, 4, "'1, 2, 3, 4, 5'", new double[]{11.0, 1.0, 3.0, 0.0, 4.0});
        oneToSeveral(DO, W, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 7.0, 9.0, 6.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'directed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(W, DO, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 8.0, 5.0, 13.0, 7.0});
        oneToSeveral(W, DO, 2, "'1, 2, 3, 4, 5'", new double[]{11.0, 0.0, 2.0, 10.0, 4.0});
        oneToSeveral(W, DO, 3, "'1, 2, 3, 4, 5'", new double[]{9.0, 3.0, 0.0, 8.0, 2.0});
        oneToSeveral(W, DO, 4, "'1, 2, 3, 4, 5'", new double[]{11.0, 1.0, 3.0, 0.0, 4.0});
        oneToSeveral(W, DO, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 7.0, 9.0, 6.0, 0.0});
    }

    @Test
    public void oneToSeveralRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(RO, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 3.0, 2.0, 2.0, 1.0});
        oneToSeveral(RO, 2, "'1, 2, 3, 4, 5'", new double[]{1.0, 0.0, 1.0, 1.0, 2.0});
        oneToSeveral(RO, 3, "'1, 2, 3, 4, 5'", new double[]{1.0, 1.0, 0.0, 2.0, 2.0});
        oneToSeveral(RO, 4, "'1, 2, 3, 4, 5'", new double[]{2.0, 2.0, 1.0, 0.0, 1.0});
        oneToSeveral(RO, 5, "'1, 2, 3, 4, 5'", new double[]{1.0, 2.0, 1.0, 1.0, 0.0});
    }

    @Test
    public void oneToSeveralWRO() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'reversed - edge_orientation', 'weight', i, '1, 2, 3, 4, 5')
        oneToSeveral(RO, W, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 11.0, 9.0, 11.0, 7.0});
        oneToSeveral(RO, W, 2, "'1, 2, 3, 4, 5'", new double[]{8.0, 0.0, 3.0, 1.0, 7.0});
        oneToSeveral(RO, W, 3, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 0.0, 3.0, 9.0});
        oneToSeveral(RO, W, 4, "'1, 2, 3, 4, 5'", new double[]{13.0, 10.0, 8.0, 0.0, 6.0});
        oneToSeveral(RO, W, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 2.0, 4.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'reversed - edge_orientation', i, '1, 2, 3, 4, 5')
        oneToSeveral(W, RO, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 11.0, 9.0, 11.0, 7.0});
        oneToSeveral(W, RO, 2, "'1, 2, 3, 4, 5'", new double[]{8.0, 0.0, 3.0, 1.0, 7.0});
        oneToSeveral(W, RO, 3, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 0.0, 3.0, 9.0});
        oneToSeveral(W, RO, 4, "'1, 2, 3, 4, 5'", new double[]{13.0, 10.0, 8.0, 0.0, 6.0});
        oneToSeveral(W, RO, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 2.0, 4.0, 0.0});
    }

    @Test
    public void oneToSeveralU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', i, '1, 2, 3, 4, 5')
        oneToSeveral(U, 1, "'1, 2, 3, 4, 5'", new double[]{0.0,  1.0,  1.0,  2.0,  1.0});
        oneToSeveral(U, 2, "'1, 2, 3, 4, 5'", new double[]{1.0,  0.0,  1.0,  1.0,  2.0});
        oneToSeveral(U, 3, "'1, 2, 3, 4, 5'", new double[]{1.0,  1.0,  0.0,  1.0,  1.0});
        oneToSeveral(U, 4, "'1, 2, 3, 4, 5'", new double[]{2.0,  1.0,  1.0,  0.0,  1.0});
        oneToSeveral(U, 5, "'1, 2, 3, 4, 5'", new double[]{1.0,  2.0,  1.0,  1.0,  0.0});
    }

    @Test
    public void oneToSeveralWU() throws Exception {
        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'undirected', 'weight', i, '1, 2, 3, 4, 5')
        oneToSeveral(U, W, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 7.0, 5.0, 8.0, 7.0});
        oneToSeveral(U, W, 2, "'1, 2, 3, 4, 5'", new double[]{7.0, 0.0, 2.0, 1.0, 4.0});
        oneToSeveral(U, W, 3, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 0.0, 3.0, 2.0});
        oneToSeveral(U, W, 4, "'1, 2, 3, 4, 5'", new double[]{8.0, 1.0, 3.0, 0.0, 4.0});
        oneToSeveral(U, W, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 2.0, 4.0, 0.0});

        // SELECT * FROM ST_ShortestPathLength('cormen_edges_all',
        //     'weight', 'undirected', i, '1, 2, 3, 4, 5')
        oneToSeveral(W, U, 1, "'1, 2, 3, 4, 5'", new double[]{0.0, 7.0, 5.0, 8.0, 7.0});
        oneToSeveral(W, U, 2, "'1, 2, 3, 4, 5'", new double[]{7.0, 0.0, 2.0, 1.0, 4.0});
        oneToSeveral(W, U, 3, "'1, 2, 3, 4, 5'", new double[]{5.0, 2.0, 0.0, 3.0, 2.0});
        oneToSeveral(W, U, 4, "'1, 2, 3, 4, 5'", new double[]{8.0, 1.0, 3.0, 0.0, 4.0});
        oneToSeveral(W, U, 5, "'1, 2, 3, 4, 5'", new double[]{7.0, 4.0, 2.0, 4.0, 0.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneToSeveralFail() throws Throwable {
        try {
            // The graph does not contain vertex 7.
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges_all', 'undirected', 1, '2, 7')");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    private void oneToSeveral(String orientation, String weight, int source, String destinationString, double[] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges_all', "
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
        rs.close();
    }

    private void oneToSeveral(String orientation, int source, String destinationString, double[] distances) throws SQLException {
        oneToSeveral(orientation, null, source, destinationString, distances);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arg3Fail() throws Throwable {
        try {
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges_all', 'undirected', 2.0)");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void arg4Fail() throws Throwable {
        try {
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges_all', 'undirected', 1, 2.0)");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void arg5Fail() throws Throwable {
        try {
            st.executeQuery("SELECT * FROM ST_ShortestPathLength('cormen_edges_all', 'undirected', 'weight', 1, 2.0)");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test
    public void testUnreachableVertices() throws SQLException {
        // We add another connected component consisting of the edge w(6, 7)=1.0.
        // (Simulating ST_Graph).
        st.execute("DROP TABLE IF EXISTS copy_nodes");
        st.execute("CREATE TABLE copy_nodes AS SELECT * FROM cormen_nodes");
        st.execute("INSERT INTO copy_nodes VALUES " +
                "(6, 'POINT (3 1)')," +
                "(7, 'POINT (4 2)'),");
        st.execute("DROP TABLE IF EXISTS copy_edges_all");
        st.execute("CREATE TABLE copy_edges_all AS SELECT * FROM cormen_edges_all");
        st.execute("INSERT INTO copy_edges_all VALUES ('LINESTRING (3 1, 4 2)', 11, 1.0, 1, 11, 6, 7)");
        st.execute("ALTER TABLE copy_edges_all ALTER COLUMN ID SET NOT NULL");
        st.execute("CREATE PRIMARY KEY ON copy_edges_all(ID)");
        // Vertices 3 and 6 are in different connected components.
        ResultSet rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges_all', " +
                "'undirected', 3, 6)");
        assertTrue(rs.next());
        assertEquals(Double.POSITIVE_INFINITY, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        rs.close();
        // 7 is reachable from 6.
        rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges_all', " +
                "'directed - edge_orientation', 6, 7)");
        assertTrue(rs.next());
        assertEquals(1.0, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        rs.close();
        // But 6 is not reachable from 7 in a directed graph.
        rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges_all', " +
                "'directed - edge_orientation', 7, 6)");
        assertTrue(rs.next());
        assertEquals(Double.POSITIVE_INFINITY, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        rs.close();
        // It is, however, in an undirected graph.
        rs = st.executeQuery("SELECT * FROM ST_ShortestPathLength('copy_edges_all', " +
                "'undirected', 7, 6)");
        assertTrue(rs.next());
        assertEquals(1.0, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE copy_nodes");
        st.execute("DROP TABLE copy_edges_all");
    }
}
