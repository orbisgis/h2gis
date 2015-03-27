package org.h2gis.h2spatialext.function.spatial.processing;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class ProcessingFunctionTest {
    private static Connection connection;
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(ProcessingFunctionTest.class.getSimpleName(), false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void testLineMerger() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('MULTILINESTRING((0 0, 10 15), (56 50, 10 15))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING((0 0, 10 15, 56 50))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('MULTILINESTRING((10 20, 10 10), (20 20,10 10)," +
                " (30 10,20 10), (30 20,20 20), (30 30,30 20), (20 10,20 20))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((10 20, 10 10, 20 20), (30 30, 30 20, 20 20), (30 10, 20 10, 20 20)) ",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('MULTILINESTRING((20 10,20 20))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING((20 10,20 20))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('LINESTRING(20 10,20 20)');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((20 10, 20 20))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('POINT(20 20)');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING EMPTY", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('POLYGON((20 20, 40 20, 40 40, 20 40, 20 20))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING EMPTY", rs.getBytes(1));
        rs.close();
    }
}
