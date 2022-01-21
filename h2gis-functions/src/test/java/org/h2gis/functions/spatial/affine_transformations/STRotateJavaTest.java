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

package org.h2gis.functions.spatial.affine_transformations;

import org.h2gis.functions.IJavaTest;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import static java.lang.Double.NaN;
import static java.lang.Math.PI;
import static org.h2gis.functions.IJavaTest.*;
import static org.h2gis.functions.spatial.affine_transformations.ST_Rotate.rotate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Java test for class for {@link ST_Rotate}.
 *
 * @author Sylvain PALOMINOS
 */
public class STRotateJavaTest implements IJavaTest {

    /**
     * Test using null values as parameter.
     */
    @Test
    public void nullValueTest() {
        Point pt = FACTORY.createPoint(new Coordinate(0, 0));
        assertNull(rotate(null, 0));
        assertNull(rotate(null, 0, pt));
        assertNull(rotate(null, 0, 0, 0));
        assertNull(rotate(null, 0, 0, 0));
        assertNull(rotate(pt, 0, null));
    }

    /**
     * Test using Double.NaN as parameter.
     */
    @Test
    public void nanValueTest() {
        Point pt = FACTORY.createPoint(new Coordinate(0, 0));
        Point ptNan = FACTORY.createPoint(new Coordinate(NaN, NaN));
        Point geom = FACTORY.createPoint(new Coordinate(5, 5));

        assertSameXY(ptNan.getCoordinate(), rotate(pt, NaN).getCoordinate());
        assertSameXY(ptNan.getCoordinate(), rotate(geom, 0, NaN, 0).getCoordinate());
        assertSameXY(ptNan.getCoordinate(), rotate(geom, 0, 0, NaN).getCoordinate());
    }

    /**
     * Check point rotation of an angle of PI and ensure the coordinates and SRID are kept
     */
    @Test
    public void pointTest() {
        Point pt2D = FACTORY.createPoint(new Coordinate(1, 2));
        Point pt3D = FACTORY.createPoint(new Coordinate(1, 2, 3));

        Point ptXY = FACTORY.createPoint(new CoordinateXY(1, 2));
        Point ptXYM = FACTORY.createPoint(new CoordinateXYM(1, 2, 4));
        Point ptXYZM = FACTORY.createPoint(new CoordinateXYZM(1, 2, 3, 4));

        Point ptSrid = FACTORY.createPoint(new Coordinate(1, 2));
        ptSrid.setSRID(4326);

        double x0 = 5;
        double y0 = 8;
        Point center = FACTORY.createPoint(new Coordinate(x0, y0));

        Coordinate expPt2D = new Coordinate(9, 14);
        Coordinate expPt3D = new Coordinate(9, 14, 3);

        Coordinate expPtXY = new CoordinateXY(9, 14);
        Coordinate expPtXYM = new CoordinateXYM(9, 14, 4);
        Coordinate expPtXYZM = new CoordinateXYZM(9, 14, 3, 4);

        // Rotate around center with PI angle
        assertSameXY(pt2D.getCoordinate(), rotate(pt2D, PI).getCoordinate());
        assertSameXYZ(pt3D.getCoordinate(), rotate(pt3D, PI).getCoordinate());

        assertSameXY(ptXY.getCoordinate(), rotate(ptXY, PI).getCoordinate());
        assertSameXYM(ptXYM.getCoordinate(), rotate(ptXYM, PI).getCoordinate());
        assertSameXYZM(ptXYZM.getCoordinate(), rotate(ptXYZM, PI).getCoordinate());

        assertEquals(4326, rotate(ptSrid, PI).getSRID());

        
        // Rotate around point(5,8) with PI angle
        assertSameXY(expPt2D, rotate(pt2D, PI, center).getCoordinate());
        assertSameXYZ(expPt3D, rotate(pt3D, PI, center).getCoordinate());

        assertSameXY(expPtXY, rotate(ptXY, PI, center).getCoordinate());
        assertSameXYM(expPtXYM, rotate(ptXYM, PI, center).getCoordinate());
        assertSameXYZM(expPtXYZM, rotate(ptXYZM, PI, center).getCoordinate());

        assertEquals(4326, rotate(ptSrid, PI, center).getSRID());


        // Rotate around coordinates (5,8) with PI angle
        assertSameXY(expPt2D, rotate(pt2D, PI, 5, 8).getCoordinate());
        assertSameXYZ(expPt3D, rotate(pt3D, PI, 5, 8).getCoordinate());

        assertSameXY(expPtXY, rotate(ptXY, PI, 5, 8).getCoordinate());
        assertSameXYM(expPtXYM, rotate(ptXYM, PI, 5, 8).getCoordinate());
        assertSameXYZM(expPtXYZM, rotate(ptXYZM, PI, 5, 8).getCoordinate());

        assertEquals(4326, rotate(ptSrid, PI, 5, 8).getSRID());
    }
}
