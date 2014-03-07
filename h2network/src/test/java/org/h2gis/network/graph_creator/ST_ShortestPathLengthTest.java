package org.h2gis.network.graph_creator;

import junit.framework.Assert;
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
            final int destination = rs.getInt(ST_ShortestPathLength.DESTINATION_INDEX);
            final double distance = rs.getDouble(ST_ShortestPathLength.DISTANCE_INDEX);
            Assert.assertEquals(distances[destination - 1], distance, TOLERANCE);
            count++;
        }
        Assert.assertEquals(5, count);
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
            Assert.assertEquals(distances[source - 1][destination - 1], distance, TOLERANCE);
            count++;
        }
        Assert.assertEquals(25, count);
    }

    private void manyToMany(String orientation, Statement st,
                            String sourceDestinationTable, double[][] distances) throws SQLException {
        manyToMany(orientation, null, st, sourceDestinationTable, distances);
    }
}
