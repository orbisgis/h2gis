package org.h2gis.network.graph_creator;

import junit.framework.Assert;
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

    // ************************** One-to-One ****************************************

    @Test
    public void oneToOneDO() throws Exception {
        Statement st = connection.createStatement();
        oneToOneDO(st, 1, 1, 0.0);
        oneToOneDO(st, 1, 2, 1.0);
        oneToOneDO(st, 1, 3, 2.0);
        oneToOneDO(st, 1, 4, 1.0);
        oneToOneDO(st, 1, 5, 1.0);
        oneToOneDO(st, 2, 1, 3.0);
        oneToOneDO(st, 2, 2, 0.0);
        oneToOneDO(st, 2, 3, 2.0);
        oneToOneDO(st, 2, 4, 1.0);
        oneToOneDO(st, 2, 5, 2.0);
        oneToOneDO(st, 3, 1, 2.0);
        oneToOneDO(st, 3, 2, 1.0);
        oneToOneDO(st, 3, 3, 0.0);
        oneToOneDO(st, 3, 4, 2.0);
        oneToOneDO(st, 3, 5, 1.0);
        oneToOneDO(st, 4, 1, 2.0);
        oneToOneDO(st, 4, 2, 1.0);
        oneToOneDO(st, 4, 3, 1.0);
        oneToOneDO(st, 4, 4, 0.0);
        oneToOneDO(st, 4, 5, 1.0);
        oneToOneDO(st, 5, 1, 1.0);
        oneToOneDO(st, 5, 2, 2.0);
        oneToOneDO(st, 5, 3, 1.0);
        oneToOneDO(st, 5, 4, 2.0);
        oneToOneDO(st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneRO() throws Exception {
        Statement st = connection.createStatement();
        oneToOneRO(st, 1, 1, 0.0);
        oneToOneRO(st, 2, 1, 1.0);
        oneToOneRO(st, 3, 1, 2.0);
        oneToOneRO(st, 4, 1, 1.0);
        oneToOneRO(st, 5, 1, 1.0);
        oneToOneRO(st, 1, 2, 3.0);
        oneToOneRO(st, 2, 2, 0.0);
        oneToOneRO(st, 3, 2, 2.0);
        oneToOneRO(st, 4, 2, 1.0);
        oneToOneRO(st, 5, 2, 2.0);
        oneToOneRO(st, 1, 3, 2.0);
        oneToOneRO(st, 2, 3, 1.0);
        oneToOneRO(st, 3, 3, 0.0);
        oneToOneRO(st, 4, 3, 2.0);
        oneToOneRO(st, 5, 3, 1.0);
        oneToOneRO(st, 1, 4, 2.0);
        oneToOneRO(st, 2, 4, 1.0);
        oneToOneRO(st, 3, 4, 1.0);
        oneToOneRO(st, 4, 4, 0.0);
        oneToOneRO(st, 5, 4, 1.0);
        oneToOneRO(st, 1, 5, 1.0);
        oneToOneRO(st, 2, 5, 2.0);
        oneToOneRO(st, 3, 5, 1.0);
        oneToOneRO(st, 4, 5, 2.0);
        oneToOneRO(st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneU() throws Exception {
        Statement st = connection.createStatement();
        oneToOneU(st, 1, 1, 0.0);
        oneToOneU(st, 1, 2, 1.0);
        oneToOneU(st, 1, 3, 2.0);
        oneToOneU(st, 1, 4, 1.0);
        oneToOneU(st, 1, 5, 1.0);
        oneToOneU(st, 2, 1, 1.0);
        oneToOneU(st, 2, 2, 0.0);
        oneToOneU(st, 2, 3, 1.0);
        oneToOneU(st, 2, 4, 1.0);
        oneToOneU(st, 2, 5, 2.0);
        oneToOneU(st, 3, 1, 2.0);
        oneToOneU(st, 3, 2, 1.0);
        oneToOneU(st, 3, 3, 0.0);
        oneToOneU(st, 3, 4, 1.0);
        oneToOneU(st, 3, 5, 1.0);
        oneToOneU(st, 4, 1, 1.0);
        oneToOneU(st, 4, 2, 1.0);
        oneToOneU(st, 4, 3, 1.0);
        oneToOneU(st, 4, 4, 0.0);
        oneToOneU(st, 4, 5, 1.0);
        oneToOneU(st, 5, 1, 1.0);
        oneToOneU(st, 5, 2, 2.0);
        oneToOneU(st, 5, 3, 1.0);
        oneToOneU(st, 5, 4, 1.0);
        oneToOneU(st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWDO() throws Exception {
        Statement st = connection.createStatement();
        oneToOneWDO(st, 1, 1, 0.0);
        oneToOneWDO(st, 1, 2, 8.0);
        oneToOneWDO(st, 1, 3, 13.0);
        oneToOneWDO(st, 1, 4, 5.0);
        oneToOneWDO(st, 1, 5, 7.0);
        oneToOneWDO(st, 2, 1, 11.0);
        oneToOneWDO(st, 2, 2, 0.0);
        oneToOneWDO(st, 2, 3, 10.0);
        oneToOneWDO(st, 2, 4, 2.0);
        oneToOneWDO(st, 2, 5, 4.0);
        oneToOneWDO(st, 3, 1, 11.0);
        oneToOneWDO(st, 3, 2, 1.0);
        oneToOneWDO(st, 3, 3, 0.0);
        oneToOneWDO(st, 3, 4, 3.0);
        oneToOneWDO(st, 3, 5, 4.0);
        oneToOneWDO(st, 4, 1, 9.0);
        oneToOneWDO(st, 4, 2, 3.0);
        oneToOneWDO(st, 4, 3, 8.0);
        oneToOneWDO(st, 4, 4, 0.0);
        oneToOneWDO(st, 4, 5, 2.0);
        oneToOneWDO(st, 5, 1, 7.0);
        oneToOneWDO(st, 5, 2, 7.0);
        oneToOneWDO(st, 5, 3, 6.0);
        oneToOneWDO(st, 5, 4, 9.0);
        oneToOneWDO(st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWRO() throws Exception {
        Statement st = connection.createStatement();
        oneToOneWRO(st, 1, 1, 0.0);
        oneToOneWRO(st, 2, 1, 8.0);
        oneToOneWRO(st, 3, 1, 13.0);
        oneToOneWRO(st, 4, 1, 5.0);
        oneToOneWRO(st, 5, 1, 7.0);
        oneToOneWRO(st, 1, 2, 11.0);
        oneToOneWRO(st, 2, 2, 0.0);
        oneToOneWRO(st, 3, 2, 10.0);
        oneToOneWRO(st, 4, 2, 2.0);
        oneToOneWRO(st, 5, 2, 4.0);
        oneToOneWRO(st, 1, 3, 11.0);
        oneToOneWRO(st, 2, 3, 1.0);
        oneToOneWRO(st, 3, 3, 0.0);
        oneToOneWRO(st, 4, 3, 3.0);
        oneToOneWRO(st, 5, 3, 4.0);
        oneToOneWRO(st, 1, 4, 9.0);
        oneToOneWRO(st, 2, 4, 3.0);
        oneToOneWRO(st, 3, 4, 8.0);
        oneToOneWRO(st, 4, 4, 0.0);
        oneToOneWRO(st, 5, 4, 2.0);
        oneToOneWRO(st, 1, 5, 7.0);
        oneToOneWRO(st, 2, 5, 7.0);
        oneToOneWRO(st, 3, 5, 6.0);
        oneToOneWRO(st, 4, 5, 9.0);
        oneToOneWRO(st, 5, 5, 0.0);
    }

    @Test
    public void oneToOneWU() throws Exception {
        Statement st = connection.createStatement();
        oneToOneWU(st, 1, 1, 0.0);
        oneToOneWU(st, 1, 2, 7.0);
        oneToOneWU(st, 1, 3, 8.0);
        oneToOneWU(st, 1, 4, 5.0);
        oneToOneWU(st, 1, 5, 7.0);
        oneToOneWU(st, 2, 1, 7.0);
        oneToOneWU(st, 2, 2, 0.0);
        oneToOneWU(st, 2, 3, 1.0);
        oneToOneWU(st, 2, 4, 2.0);
        oneToOneWU(st, 2, 5, 4.0);
        oneToOneWU(st, 3, 1, 8.0);
        oneToOneWU(st, 3, 2, 1.0);
        oneToOneWU(st, 3, 3, 0.0);
        oneToOneWU(st, 3, 4, 3.0);
        oneToOneWU(st, 3, 5, 4.0);
        oneToOneWU(st, 4, 1, 5.0);
        oneToOneWU(st, 4, 2, 2.0);
        oneToOneWU(st, 4, 3, 3.0);
        oneToOneWU(st, 4, 4, 0.0);
        oneToOneWU(st, 4, 5, 2.0);
        oneToOneWU(st, 5, 1, 7.0);
        oneToOneWU(st, 5, 2, 4.0);
        oneToOneWU(st, 5, 3, 4.0);
        oneToOneWU(st, 5, 4, 2.0);
        oneToOneWU(st, 5, 5, 0.0);
    }

    private void oneToOne(String orientation, Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne(orientation, "", st, source, destination, distance);
    }

    private void oneToOne(String orientation, String weight, Statement st, int source, int destination, double distance) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + orientation + ((!weight.isEmpty()) ? ", " + weight : "")
                        + ", " + source + ", " + destination + ")");
        assertTrue(rs.next());
        assertEquals(source, rs.getInt(ST_ShortestPathLength.SOURCE_INDEX));
        assertEquals(destination, rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX));
        assertEquals(distance, rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX), TOLERANCE);
        assertFalse(rs.next());
    }

    private void oneToOneDO(Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne("'directed - edge_orientation'", st, source, destination, distance);
    }

    private void oneToOneWDO(Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne("'directed - edge_orientation'", "'weight'", st, source, destination, distance);
        oneToOne("'weight'", "'directed - edge_orientation'", st, source, destination, distance);
    }

    private void oneToOneRO(Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne("'reversed - edge_orientation'", st, source, destination, distance);
    }

    private void oneToOneWRO(Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne("'reversed - edge_orientation'", "'weight'", st, source, destination, distance);
        oneToOne("'weight'", "'reversed - edge_orientation'", st, source, destination, distance);
    }

    private void oneToOneU(Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne("'undirected'", st, source, destination, distance);
    }

    private void oneToOneWU(Statement st, int source, int destination, double distance) throws SQLException {
        oneToOne("'undirected'", "'weight'", st, source, destination, distance);
        oneToOne("'weight'", "'undirected'", st, source, destination, distance);
    }

    // ************************** One-to-All ****************************************

    @Test
    public void oneToAllDO() throws Exception {
        Statement st = connection.createStatement();
        oneToAllDO(st, 1, new double[]{0.0, 1.0, 2.0, 1.0, 1.0});
        oneToAllDO(st, 2, new double[]{3.0, 0.0, 2.0, 1.0, 2.0});
        oneToAllDO(st, 3, new double[]{2.0, 1.0, 0.0, 2.0, 1.0});
        oneToAllDO(st, 4, new double[]{2.0, 1.0, 1.0, 0.0, 1.0});
        oneToAllDO(st, 5, new double[]{1.0, 2.0, 1.0, 2.0, 0.0});
    }

    @Test
    public void oneToAllWDO() throws Exception {
        Statement st = connection.createStatement();
        oneToAllWDO(st, 1, new double[]{0.0, 8.0, 13.0, 5.0, 7.0});
        oneToAllWDO(st, 2, new double[]{11.0, 0.0, 10.0, 2.0, 4.0});
        oneToAllWDO(st, 3, new double[]{11.0, 1.0, 0.0, 3.0, 4.0});
        oneToAllWDO(st, 4, new double[]{9.0, 3.0, 8.0, 0.0, 2.0});
        oneToAllWDO(st, 5, new double[]{7.0, 7.0, 6.0, 9.0, 0.0});
    }

    private void oneToAll(String orientation, Statement st, int source, double[] distances) throws SQLException {
        oneToAll(orientation, "", st, source, distances);
    }

    private void oneToAll(String orientation, String weight, Statement st, int source, double[] distances) throws SQLException {
        ResultSet rs = st.executeQuery(
                "SELECT * FROM ST_ShortestPathLength('cormen_edges', "
                        + orientation + ((!weight.isEmpty()) ? ", " + weight : "")
                        + ", " + source + ")");
        int count = 0;
        while (rs.next()) {
            final int destination = rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX);
            final double distance = rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX);
            Assert.assertEquals(distances[destination - 1], distance, TOLERANCE);
            count++;
        }
        Assert.assertEquals(5, count);
    }

    private void oneToAllDO(Statement st, int source, double[] distances) throws SQLException {
        oneToAll("'directed - edge_orientation'", st, source, distances);
    }

    private void oneToAllWDO(Statement st, int source, double[] distances) throws SQLException {
        oneToAll("'weight'", "'directed - edge_orientation'", st, source, distances);
        oneToAll("'directed - edge_orientation'", "'weight'", st, source, distances);
    }

    private void oneToAllRO(Statement st, int source, double[] distances) throws SQLException {
        oneToAll("'reversed - edge_orientation'", st, source, distances);
    }

    private void oneToAllWRO(Statement st, int source, double[] distances) throws SQLException {
        oneToAll("'weight'", "'reversed - edge_orientation'", st, source, distances);
        oneToAll("'reversed - edge_orientation'", "'weight'", st, source, distances);
    }

    private void oneToAllU(Statement st, int source, double[] distances) throws SQLException {
        oneToAll("'undirected'", st, source, distances);
    }

    private void oneToAllWU(Statement st, int source, double[] distances) throws SQLException {
        oneToAll("'weight'", "'undirected'", st, source, distances);
        oneToAll("'undirected'", "'weight'", st, source, distances);
    }
}
