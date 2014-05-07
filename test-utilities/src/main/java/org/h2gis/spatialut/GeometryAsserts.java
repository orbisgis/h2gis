package org.h2gis.spatialut;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.h2.value.ValueGeometry;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Assert with Geometry type
 * @author Nicolas Fortin
 */
public class GeometryAsserts {
    /** Default, Epsilon value for metric projections unit test */
    private static final double EPSILON = .01;
    /**
     * Check Geometry type,X,Y,Z and SRID
     * @param expectedWKT Expected value, in WKT
     * @param valueWKB Test value, in WKB ex rs.getBytes()
     * @throws SQLException If WKT or WKB is not valid
     */
    public static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) throws SQLException {
        if(expectedWKT == null) {
            assertNull(valueWKB);
        } else {
            assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKB).getObject());
        }
    }

    /**
     * Check Geometry type,X,Y,Z and SRID
     * @param expectedWKT Expected value, in WKT
     * @param valueObject Test value geometry ex rs.getObject(i)
     * @throws SQLException If WKT or WKB is not valid
     */
    public static void assertGeometryEquals(String expectedWKT, Object valueObject) throws SQLException {
        if(expectedWKT == null) {
            assertNull(valueObject);
        } else {
            ValueGeometry expected = ValueGeometry.get(expectedWKT);
            ValueGeometry actual = ValueGeometry.getFromGeometry(valueObject);
            assertEquals("Expected:\n" + expected.getWKT() + "\nActual:\n" + actual.getWKT(), expected, actual);
        }
    }

    /**
     * Check only X,Y and geometry type
     * @param expectedWKT Expected value, in WKT
     * @param valueWKT Test value, in WKT ex rs.getString()
     * @throws SQLException
     */
    public static void assertGeometryEquals(String expectedWKT, String valueWKT) throws SQLException {
        assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKT).getBytes());
    }

    /**
     * Equals test with epsilon error acceptance.
     * @param expectedWKT Expected value, in WKT
     * @param resultSetObject Geometry, rs.getObject(i)
     */
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
}
