/**
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

package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.math.Vector3D;
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
    public void testTriangleSlope() {
        Triangle tri = new Triangle(new Coordinate(2, 2, 0), new Coordinate(6, 1, 0), new Coordinate(4, 4, 0));
        Vector3D slope = TriMarkers.getSteepestVector(TriMarkers.getNormalVector(tri), 1e-12);
        assertEquals(0, slope.getZ(), 1e-12);
        tri = new Triangle(new Coordinate(0, 0, 0), new Coordinate(4, 0, 0), new Coordinate(2, 3, 1));
        slope = TriMarkers.getSteepestVector(TriMarkers.getNormalVector(tri), 1e-12);
        assertEquals(0, slope.getX(), 0.001);
        assertEquals(-0.948, slope.getY(), 0.001);
        assertEquals(-0.316, slope.getZ(), 0.001);
    }
}
