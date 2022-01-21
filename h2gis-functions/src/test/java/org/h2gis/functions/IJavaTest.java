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
package org.h2gis.functions;

import org.locationtech.jts.geom.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class with utilities to simplify Java test writing.
 *
 * @author Sylvain PALOMINOS
 */
public interface IJavaTest {
    GeometryFactory FACTORY = new GeometryFactory();

    /**
     * Assert that the coordinates have the same X,Y values.
     *
     * @param c1 Expected value.
     * @param c2 Actual value.
     */
    static void assertSameXY(Coordinate c1, Coordinate c2) {
        assertEquals(c1.x, c2.x, 1e-8);
        assertEquals(c1.y, c2.y, 1e-8);
    }

    /**
     * Assert that the coordinates have the same X,Y,Z values.
     *
     * @param c1 Expected value.
     * @param c2 Actual value.
     */
    static void assertSameXYZ(Coordinate c1, Coordinate c2) {
        assertFalse(c1 instanceof CoordinateXY || c1 instanceof CoordinateXYM);
        assertFalse(c2 instanceof CoordinateXY || c2 instanceof CoordinateXYM);

        assertEquals(c1.x, c2.x, 1e-8);
        assertEquals(c1.y, c2.y, 1e-8);
        assertEquals(c1.z, c2.z, 1e-8);
    }

    /**
     * Assert that the coordinates have the same X,Y,M values.
     *
     * @param c1 Expected value.
     * @param c2 Actual value.
     */
    static void assertSameXYM(Coordinate c1, Coordinate c2) {
        assertTrue(c1 instanceof CoordinateXYM || c1 instanceof CoordinateXYZM);
        assertTrue(c2 instanceof CoordinateXYM || c2 instanceof CoordinateXYZM);

        assertEquals(c1.x, c2.x, 1e-8);
        assertEquals(c1.y, c2.y, 1e-8);
        assertEquals(c1.getM(), c2.getM(), 1e-8);
    }

    /**
     * Assert that the coordinates have the same X,Y,Z,M values.
     *
     * @param c1 Expected value.
     * @param c2 Actual value.
     */
    static void assertSameXYZM(Coordinate c1, Coordinate c2) {
        assertTrue(c1 instanceof CoordinateXYZM);
        assertTrue(c2 instanceof CoordinateXYZM);

        assertEquals(c1.x, c2.x, 1e-8);
        assertEquals(c1.y, c2.y, 1e-8);
        assertEquals(c1.z, c2.z, 1e-8);
        assertEquals(c1.getM(), c2.getM(), 1e-8);
    }
}
