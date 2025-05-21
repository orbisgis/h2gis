/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.unitTest;

import org.h2.value.ValueGeometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assert with Geometry type
 *
 * @author Nicolas Fortin
 */
public class GeometryAsserts {

    /**
     * Default, Epsilon value for metric projections unit test
     */
    private static final double EPSILON = .01;

    /**
     * Check Geometry type,X,Y,Z and SRID
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueWKB Test value, in WKB ex rs.getBytes()
     */
    public static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) {
        if (expectedWKT == null) {
            assertNull(valueWKB);
        } else {
            assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKB).getGeometry());
        }
    }
    
     /**
     * Check Geometry type,X,Y,Z
     *
     * @param expectedWKT Expected value, in WKT
     * @param object Test value geometry
     */
    public static void assertGeometryEquals(String expectedWKT, Object object) {
        if (expectedWKT == null) {
            assertNull(object);
        }
        if(object instanceof ValueGeometry){
            assertGeometryEquals( expectedWKT, (ValueGeometry) object);
        }
        else if(object instanceof Geometry){
            assertGeometryEquals(expectedWKT,(Geometry) object);
        }
        else{
            assertNull(object);
        }
    }

    /**
     * Check Geometry type,X,Y,Z
     *
     * @param expectedWKT Expected value, in WKT
     * @param geometry Test value geometry
     */
    public static void assertGeometryEquals(String expectedWKT, Geometry geometry) {
        if (expectedWKT == null) {
            assertNull(geometry);
        } else {
            ValueGeometry expected = ValueGeometry.get(expectedWKT);
            ValueGeometry actual = ValueGeometry.getFromGeometry(geometry.norm());
            expected = ValueGeometry.getFromGeometry(expected.getGeometry().norm());
            String moreInfo = "";
            if (!actual.equals(expected)) {
                if (!GeometryCollection.class.getName().equals(expected.getGeometry().getClass().getName())
                        && !GeometryCollection.class.getName().equals(actual.getGeometry().getClass().getName())
                        && expected.getGeometry().equals(actual.getGeometry())) {
                    moreInfo = "\n But are topologically equals";
                }
            }
            assertEquals(expected, actual, "Expected:\n" + expected.getString() + "\nActual:\n" + actual.getString() + moreInfo);
        }
    }

    /**
     * Check Geometry type,X,Y,Z
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueObject Test value geometry ex rs.getObject(i)
     */
    public static void assertGeometryEquals(String expectedWKT, ValueGeometry valueObject) {
        if (expectedWKT == null) {
            assertNull(valueObject);
        } else {
            ValueGeometry expected = ValueGeometry.get(expectedWKT);
            ValueGeometry actual = ValueGeometry.getFromGeometry(valueObject.getGeometry().norm());
            expected = ValueGeometry.getFromGeometry(expected.getGeometry().norm());
            String moreInfo = "";
            if (!actual.equals(expected)) {
                if (!GeometryCollection.class.getName().equals(expected.getGeometry().getClass().getName())
                        && !GeometryCollection.class.getName().equals(actual.getGeometry().getClass().getName())
                        && expected.getGeometry().equals(actual.getGeometry())) {
                    moreInfo = "\n But are topologically equals";
                }
            }
            assertEquals(expected, actual, "Expected:\n" + expected.getString() + "\nActual:\n" + actual.getString() + moreInfo);
        }
    }

    /**
     * Check only X,Y and geometry type
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueWKT Test value, in WKT ex rs.getString()
     */
    public static void assertGeometryEquals(String expectedWKT, String valueWKT) {
        assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKT).getBytes());
    }

    /**
     * Equals test with epsilon error acceptance.
     *
     * @param expectedWKT Expected value, in WKT
     * @param resultSetObject Geometry, rs.getObject(i)
     */
    public static void assertGeometryBarelyEquals(String expectedWKT, Object resultSetObject) {
        assertGeometryBarelyEquals(expectedWKT, resultSetObject, EPSILON);
    }

    /**
     * Equals test with epsilon error acceptance.
     *
     * @param expectedWKT Expected value, in WKT
     * @param resultSetObject Geometry, rs.getObject(i)
     * @param epsilon epsilon error acceptance
     */
    public static void assertGeometryBarelyEquals(String expectedWKT, Object resultSetObject, double epsilon) {
        assertTrue(resultSetObject instanceof Geometry);
        Geometry expectedGeometry = ValueGeometry.get(expectedWKT).getGeometry();
        Geometry result = (Geometry) resultSetObject;
        assertEquals(expectedGeometry.getGeometryType(), result.getGeometryType());
        assertEquals(expectedGeometry.getNumPoints(), result.getNumPoints());
        Coordinate[] expectedCoordinates = expectedGeometry.getCoordinates();
        Coordinate[] resultCoordinates = result.getCoordinates();
        for (int idPoint = 0; idPoint < expectedCoordinates.length; idPoint++) {
            assertEquals(expectedCoordinates[idPoint].x, resultCoordinates[idPoint].x, epsilon);
            assertEquals(expectedCoordinates[idPoint].y, resultCoordinates[idPoint].y, epsilon);
            assertEquals(expectedCoordinates[idPoint].getZ(), resultCoordinates[idPoint].getZ(), epsilon);
        }
    }

    /**
     * Print a geometry content
     * @param geometry {@link Geometry}
     */
    public static void printGeometry(Object geometry){
        System.out.println(ValueGeometry.getFromGeometry(geometry).getString());
    }
}
