/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.unitTest;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.h2.value.ValueGeometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
     * @param valueWKB    Test value, in WKB ex rs.getBytes()
     */
    public static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) {
        if (expectedWKT == null) {
            assertNull(valueWKB);
        } else {
            assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKB).getObject());
        }
    }

    /**
     * Check Geometry type,X,Y,Z
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueObject Test value geometry ex rs.getObject(i)
     */
    public static void assertGeometryEquals(String expectedWKT, Object valueObject) {
        assertGeometryEquals(expectedWKT, 0, valueObject);
    }


    /**
     * Check Geometry type,X,Y,Z and SRID
     *
     * @param expectedWKT Expected value, in WKT
     * @param expectedSRID Expected SRID code,
     * @param valueObject Test value geometry ex rs.getObject(i)
     */
    public static void assertGeometryEquals(String expectedWKT,int expectedSRID, Object valueObject) {
        if (expectedWKT == null) {
            assertNull(valueObject);
        } else {
            ValueGeometry expected = ValueGeometry.get(expectedWKT, expectedSRID);
            ValueGeometry actual = ValueGeometry.getFromGeometry(((Geometry)valueObject).norm());
            expected = ValueGeometry.getFromGeometry(expected.getGeometry().norm());
            String moreInfo = "";
            if(!actual.equals(expected)) {
                if(!GeometryCollection.class.getName().equals(expected.getGeometry().getClass().getName()) &&
                        !GeometryCollection.class.getName().equals(actual.getGeometry().getClass().getName()) &&
                        expected.getGeometry().equals(actual.getGeometry())) {
                    moreInfo = "\n But are topologically equals";
                }
            }
            assertEquals("Expected:\n" + expected.getWKT() + "\nActual:\n" + actual.getWKT()+moreInfo, expected, actual);
        }
    }
    /**
     * Check only X,Y and geometry type
     *
     * @param expectedWKT Expected value, in WKT
     * @param valueWKT    Test value, in WKT ex rs.getString()
     */
    public static void assertGeometryEquals(String expectedWKT, String valueWKT) {
        assertGeometryEquals(expectedWKT, ValueGeometry.get(valueWKT).getBytes());
    }

    /**
     * Equals test with epsilon error acceptance.
     *
     * @param expectedWKT     Expected value, in WKT
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
        assertGeometryBarelyEquals(expectedWKT, 0, resultSetObject, epsilon);
    }

    /**
     * Equals test with epsilon error acceptance and SRID.
     *
     * @param expectedWKT Expected value, in WKT
     * @param expectedSRID Expected SRID Value
     * @param resultSetObject
     * @param epsilon epsilon error acceptance
     */
    public static void assertGeometryBarelyEquals(String expectedWKT,int expectedSRID, Object resultSetObject, double epsilon) {
        assertTrue(resultSetObject instanceof Geometry);
        Geometry expectedGeometry = ValueGeometry.get(expectedWKT, expectedSRID).getGeometry();
        Geometry result = (Geometry) resultSetObject;
        assertEquals(expectedGeometry.getGeometryType(), result.getGeometryType());
        assertEquals(expectedGeometry.getNumPoints(), result.getNumPoints());
        Coordinate[] expectedCoordinates = expectedGeometry.getCoordinates();
        Coordinate[] resultCoordinates = result.getCoordinates();
        for (int idPoint = 0; idPoint < expectedCoordinates.length; idPoint++) {
            assertEquals(expectedCoordinates[idPoint].x, resultCoordinates[idPoint].x, epsilon);
            assertEquals(expectedCoordinates[idPoint].y, resultCoordinates[idPoint].y, epsilon);
            assertEquals(expectedCoordinates[idPoint].z, resultCoordinates[idPoint].z, epsilon);
        }
    }
}
