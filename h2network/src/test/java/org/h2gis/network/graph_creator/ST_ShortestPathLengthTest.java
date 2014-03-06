package org.h2gis.network.graph_creator;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    private static final double TOLERANCE = 0.0;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ShortestPathLengthTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_Graph(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPathLength(), "");
        GraphCreatorTest.registerCormenGraph(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void DO() throws Exception {
        Statement st = connection.createStatement();
        checkDO(st, 1, 1, 0.0);
        checkDO(st, 1, 2, 1.0);
        checkDO(st, 1, 3, 2.0);
        checkDO(st, 1, 4, 1.0);
        checkDO(st, 1, 5, 1.0); // (1,5) bidirectional
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

    @Test
    public void RO() throws Exception {
        Statement st = connection.createStatement();
        checkRO(st, 1, 1, 0.0);
        checkRO(st, 2, 1, 1.0);
        checkRO(st, 3, 1, 2.0);
        checkRO(st, 4, 1, 1.0);
        checkRO(st, 5, 1, 1.0); // (1,5) bidirectional
        checkRO(st, 1, 2, 3.0);
        checkRO(st, 2, 2, 0.0);
        checkRO(st, 3, 2, 2.0); // (2,3) reversed
        checkRO(st, 4, 2, 1.0);
        checkRO(st, 5, 2, 2.0);
        checkRO(st, 1, 3, 2.0);
        checkRO(st, 2, 3, 1.0); // (2,3) reversed
        checkRO(st, 3, 3, 0.0);
        checkRO(st, 4, 3, 2.0); // (2,3) reversed
        checkRO(st, 5, 3, 1.0);
        checkRO(st, 1, 4, 2.0);
        checkRO(st, 2, 4, 1.0);
        checkRO(st, 3, 4, 1.0);
        checkRO(st, 4, 4, 0.0);
        checkRO(st, 5, 4, 1.0);
        checkRO(st, 1, 5, 1.0);
        checkRO(st, 2, 5, 2.0);
        checkRO(st, 3, 5, 1.0);
        checkRO(st, 4, 5, 2.0);
        checkRO(st, 5, 5, 0.0);
    }

    @Test
    public void U() throws Exception {
        Statement st = connection.createStatement();
        checkU(st, 1, 1, 0.0);
        checkU(st, 1, 2, 1.0);
        checkU(st, 1, 3, 2.0);
        checkU(st, 1, 4, 1.0);
        checkU(st, 1, 5, 1.0);
        checkU(st, 2, 1, 1.0);
        checkU(st, 2, 2, 0.0);
        checkU(st, 2, 3, 1.0);
        checkU(st, 2, 4, 1.0);
        checkU(st, 2, 5, 2.0);
        checkU(st, 3, 1, 2.0);
        checkU(st, 3, 2, 1.0);
        checkU(st, 3, 3, 0.0);
        checkU(st, 3, 4, 1.0);
        checkU(st, 3, 5, 1.0);
        checkU(st, 4, 1, 1.0);
        checkU(st, 4, 2, 1.0);
        checkU(st, 4, 3, 1.0);
        checkU(st, 4, 4, 0.0);
        checkU(st, 4, 5, 1.0);
        checkU(st, 5, 1, 1.0);
        checkU(st, 5, 2, 2.0);
        checkU(st, 5, 3, 1.0);
        checkU(st, 5, 4, 1.0);
        checkU(st, 5, 5, 0.0);
    }

    @Test
    public void WDO() throws Exception {
        Statement st = connection.createStatement();
        checkWDO(st, 1, 1, 0.0);
        checkWDO(st, 1, 2, 8.0);
        checkWDO(st, 1, 3, 13.0);
        checkWDO(st, 1, 4, 5.0);
        checkWDO(st, 1, 5, 7.0);
        checkWDO(st, 2, 1, 11.0);
        checkWDO(st, 2, 2, 0.0);
        checkWDO(st, 2, 3, 10.0);
        checkWDO(st, 2, 4, 2.0);
        checkWDO(st, 2, 5, 4.0);
        checkWDO(st, 3, 1, 11.0);
        checkWDO(st, 3, 2, 1.0);
        checkWDO(st, 3, 3, 0.0);
        checkWDO(st, 3, 4, 3.0);
        checkWDO(st, 3, 5, 4.0);
        checkWDO(st, 4, 1, 9.0);
        checkWDO(st, 4, 2, 3.0);
        checkWDO(st, 4, 3, 8.0);
        checkWDO(st, 4, 4, 0.0);
        checkWDO(st, 4, 5, 2.0);
        checkWDO(st, 5, 1, 7.0);
        checkWDO(st, 5, 2, 7.0);
        checkWDO(st, 5, 3, 6.0);
        checkWDO(st, 5, 4, 9.0);
        checkWDO(st, 5, 5, 0.0);
    }

    @Test
    public void WRO() throws Exception {
        Statement st = connection.createStatement();
        checkWRO(st, 1, 1, 0.0);
        checkWRO(st, 2, 1, 8.0);
        checkWRO(st, 3, 1, 13.0);
        checkWRO(st, 4, 1, 5.0);
        checkWRO(st, 5, 1, 7.0);
        checkWRO(st, 1, 2, 11.0);
        checkWRO(st, 2, 2, 0.0);
        checkWRO(st, 3, 2, 10.0);
        checkWRO(st, 4, 2, 2.0);
        checkWRO(st, 5, 2, 4.0);
        checkWRO(st, 1, 3, 11.0);
        checkWRO(st, 2, 3, 1.0);
        checkWRO(st, 3, 3, 0.0);
        checkWRO(st, 4, 3, 3.0);
        checkWRO(st, 5, 3, 4.0);
        checkWRO(st, 1, 4, 9.0);
        checkWRO(st, 2, 4, 3.0);
        checkWRO(st, 3, 4, 8.0);
        checkWRO(st, 4, 4, 0.0);
        checkWRO(st, 5, 4, 2.0);
        checkWRO(st, 1, 5, 7.0);
        checkWRO(st, 2, 5, 7.0);
        checkWRO(st, 3, 5, 6.0);
        checkWRO(st, 4, 5, 9.0);
        checkWRO(st, 5, 5, 0.0);
    }

    @Test
    public void WU() throws Exception {
        Statement st = connection.createStatement();
        checkWU(st, 1, 1, 0.0);
        checkWU(st, 1, 2, 7.0);
        checkWU(st, 1, 3, 8.0);
        checkWU(st, 1, 4, 5.0);
        checkWU(st, 1, 5, 7.0);
        checkWU(st, 2, 1, 7.0);
        checkWU(st, 2, 2, 0.0);
        checkWU(st, 2, 3, 1.0);
        checkWU(st, 2, 4, 2.0);
        checkWU(st, 2, 5, 4.0);
        checkWU(st, 3, 1, 8.0);
        checkWU(st, 3, 2, 1.0);
        checkWU(st, 3, 3, 0.0);
        checkWU(st, 3, 4, 3.0);
        checkWU(st, 3, 5, 4.0);
        checkWU(st, 4, 1, 5.0);
        checkWU(st, 4, 2, 2.0);
        checkWU(st, 4, 3, 3.0);
        checkWU(st, 4, 4, 0.0);
        checkWU(st, 4, 5, 2.0);
        checkWU(st, 5, 1, 7.0);
        checkWU(st, 5, 2, 4.0);
        checkWU(st, 5, 3, 4.0);
        checkWU(st, 5, 4, 2.0);
        checkWU(st, 5, 5, 0.0);
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

    private void checkDO(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'directed - edge_orientation'", st, source, destination, distance);
    }

    private void checkRO(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'reversed - edge_orientation'", st, source, destination, distance);
    }

    private void checkU(Statement st, int source, int destination, double distance) throws SQLException {
        check(", 'undirected'", st, source, destination, distance);
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
