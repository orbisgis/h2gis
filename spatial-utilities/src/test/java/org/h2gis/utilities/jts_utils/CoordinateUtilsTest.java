package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.math.Vector3D;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Nicolas Fortin
 */
public class CoordinateUtilsTest {


    /**
     * Test intersection of two lines with infinite ends
     */
    @Test
    public void testIntersection() {
        Triangle tri = new Triangle(new Coordinate(2, 2, 1), new Coordinate(6, 1, 2), new Coordinate(4, 4, 0));
        Coordinate i = new Coordinate(4,2);
        assertEquals(new Coordinate(4, 1.5), CoordinateUtils.vectorIntersection(tri.p0, new Vector3D(tri.p0, tri.p1),i, new Vector3D(0, -0.01, 0)));
        // Line behind other vector
        assertNull(CoordinateUtils.vectorIntersection(tri.p1, new Vector3D(tri.p1, tri.p2),i, new Vector3D(0, -0.01, 0)));
        // Reverse i direction
        assertEquals(new Coordinate(4, 4), CoordinateUtils.vectorIntersection(tri.p1, new Vector3D(tri.p1, tri.p2),i, new Vector3D(0, 0.01, 0)));
        // Line behind other vector
        assertNull(CoordinateUtils.vectorIntersection(tri.p0, new Vector3D(tri.p0, tri.p1), i, new Vector3D(0.1, 0.01, 0)));
        assertEquals(tri.p0, CoordinateUtils.vectorIntersection(tri.p1, new Vector3D(tri.p1, tri.p0), tri.p2, new Vector3D(tri.p2, tri.p0)));
    }

    @Test
    public void testTriangeSlope() {
        Triangle tri = new Triangle(new Coordinate(2, 2, 2), new Coordinate(6, 1, 2), new Coordinate(4, 4, 0));
        Vector3D slope = TriMarkers.getSteepestVector(TriMarkers.getNormalVector(tri), 1e-12);
        assertEquals(new Vector3D(0,0 ,0), slope);
    }
}
