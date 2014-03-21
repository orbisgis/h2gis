package org.h2gis.h2spatialext;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class TableFunctionTest {
    private static final double EPSILON = .01;
    private static Connection connection;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(TableFunctionTest.class.getSimpleName(), false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    public static void assertGeometryEquals(String expectedWKT, Object resultSetObject) {
        assertEquals(ValueGeometry.get(expectedWKT), ValueGeometry.getFromGeometry(resultSetObject));
    }
    public static void assertGeometryBarelyEquals(String expectedWKT, Object resultSetObject) {
        assertGeometryBarelyEquals(expectedWKT, resultSetObject, EPSILON);
    }

    public static void assertGeometryBarelyEquals(String expectedWKT, Object resultSetObject, double epsilon) {
        assertTrue(resultSetObject instanceof Geometry);
        Geometry expectedGeometry = ValueGeometry.get(expectedWKT).getGeometry();
        Geometry result = (Geometry) resultSetObject;
        assertEquals(expectedGeometry.getGeometryType(), result.getGeometryType());
        assertEquals(expectedGeometry.getNumPoints(), result.getNumPoints());
        Coordinate[] expectedCoordinates = expectedGeometry.getCoordinates();
        Coordinate[] resultCoordinates = result.getCoordinates();
        for(int idPoint = 0; idPoint < expectedCoordinates.length; idPoint++) {
            assertEquals(expectedCoordinates[idPoint].x, resultCoordinates[idPoint].x, epsilon);
            assertEquals(expectedCoordinates[idPoint].y, resultCoordinates[idPoint].y, epsilon);
            assertEquals(expectedCoordinates[idPoint].z, resultCoordinates[idPoint].z, epsilon);
        }
    }

    @Test
    public void testST_TriangleContouringWithZ() throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS TIN");
            st.execute("CREATE TABLE TIN AS SELECT 'POLYGON ((-9.19 3.7 1, 0.3 1.41 4.4, -5.7 -4.15 4, -9.19 3.7 1))'::geometry the_geom");
            ResultSet rs = st.executeQuery("select * from ST_TriangleContouring('TIN', 2,3,4,5)");
            assertEquals(2, rs.getMetaData().getColumnCount());
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.4 3.03 2, -9.19 3.7 1, -8.02 1.09 2, -6.4 3.03 2))", rs.getObject(1));
            assertEquals(0, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.05 -0.56 3, -6.4 3.03 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.4 3.03 2, -8.02 1.09 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.05 -0.56 3, -3.61 2.35 3, -6.4 3.03 2, -6.05 -0.56 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -5.7 -4.15 4, -6.05 -0.56 3, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -0.82 1.68 4, -6.05 -0.56 3, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-0.82 1.68 4, -3.61 2.35 3, -6.05 -0.56 3, -0.82 1.68 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -6.05 -0.56 3, -5.7 -4.15 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, 0.3 1.41 4.4, -0.82 1.68 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-5.7 -4.15 4, 0.3 1.41 4.4, -1.52 0.85 4, -5.7 -4.15 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertFalse(rs.next());
        } finally {
            st.close();
        }
    }
}
