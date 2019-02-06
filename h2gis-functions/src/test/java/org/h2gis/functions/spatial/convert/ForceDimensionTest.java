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

package org.h2gis.functions.spatial.convert;

import java.sql.SQLException;
import org.h2gis.functions.spatial.edit.ST_UpdateZ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKTReader;


/**
 * @author Sylvain Palominos
 * @author Erwan Bocher
 * 
 */
public class ForceDimensionTest {
    private static final GeometryFactory FACTORY = new GeometryFactory();

    @Test
    public void forcePointDimensions() throws SQLException {
        //Create a 3D point
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(1, 3);
        coordinateSequence.setOrdinate(0, 0, 10.0);
        coordinateSequence.setOrdinate(0, 1, 11.0);
        coordinateSequence.setOrdinate(0, 2, 1);
        Point point = FACTORY.createPoint(coordinateSequence);

        //Assert that input data are 3D geometries
        assertEquals(3, point.getCoordinateSequence().getDimension());
        assertEquals(10.0, point.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(11.0, point.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(1, point.getCoordinateSequence().getOrdinate(0, 2), 0);
        
        //Call ST_Force2D
        Point newPoint = (Point) ST_Force2D.force2D(point);
        //Assert that the new Point is a 2D one     
        assertEquals(2, newPoint.getCoordinateSequence().getDimension());
        assertEquals(10.0, newPoint.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(11.0, newPoint.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(Double.NaN, newPoint.getCoordinateSequence().getOrdinate(0, 2), 0);
        
        //Call ST_Force3D
        Point newPoint3D = (Point) ST_Force3D.force3D(newPoint);
        //Assert that the new Point is a 3D one    
        assertEquals(3, newPoint3D.getCoordinateSequence().getDimension());
        assertEquals(10.0, newPoint3D.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(11.0, newPoint3D.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(0, newPoint.getCoordinateSequence().getOrdinate(0, 2), 0);
        
        //Create a 3D with NaN z
        coordinateSequence = new CoordinateArraySequence(1, 3);
        coordinateSequence.setOrdinate(0, 0, 10.0);
        coordinateSequence.setOrdinate(0, 1, 11.0);
        coordinateSequence.setOrdinate(0, 2, Double.NaN);
        Point point3DNaN = FACTORY.createPoint(coordinateSequence);
        assertEquals(3, point3DNaN.getCoordinateSequence().getDimension());
        assertEquals(0, newPoint.getCoordinateSequence().getOrdinate(0, 2), 0);
        
    }

    @Test
    public void multiPoint2DTest() throws SQLException {
        //Create a 2D POINT
        CoordinateSequence coordinateSequence1 = new CoordinateArraySequence(1, 2);
        coordinateSequence1.setOrdinate(0, 0, 10.0);
        coordinateSequence1.setOrdinate(0, 1, 11.0);
        Point point1 = FACTORY.createPoint(coordinateSequence1);

        CoordinateSequence coordinateSequence2 = new CoordinateArraySequence(1, 2);
        coordinateSequence2.setOrdinate(0, 0, 20.0);
        coordinateSequence2.setOrdinate(0, 1, 21.0);
        Point point2 = FACTORY.createPoint(coordinateSequence2);

        CoordinateSequence coordinateSequence3 = new CoordinateArraySequence(1, 2);
        coordinateSequence3.setOrdinate(0, 0, 30.0);
        coordinateSequence3.setOrdinate(0, 1, 31.0);
        Point point3 = FACTORY.createPoint(coordinateSequence3);
        
        MultiPoint multiPoint = FACTORY.createMultiPoint(new Point[]{point1, point2, point3});

        //Assert that input data are 2D geometries
        assertEquals(10.0, point1.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(11.0, point1.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(Double.NaN, point1.getCoordinateSequence().getOrdinate(0, 2), 0);
        assertEquals(2, point1.getCoordinateSequence().getDimension());

        assertEquals(20.0, point2.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(21.0, point2.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(Double.NaN, point2.getCoordinateSequence().getOrdinate(0, 2), 0);
        assertEquals(2, point2.getCoordinateSequence().getDimension());

        assertEquals(30.0, point3.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(31.0, point3.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(Double.NaN, point3.getCoordinateSequence().getOrdinate(0, 2), 0);
        assertEquals(2, point3.getCoordinateSequence().getDimension());

        assertEquals(3, multiPoint.getNumPoints());

        //Convert to a 3D point
        Geometry geom = ST_Force3D.force3D(multiPoint);

        //Assert that the new Point is a 3D one
        assertTrue(geom instanceof MultiPoint);
        MultiPoint newMultiPoint = (MultiPoint)geom;
        assertEquals(3, newMultiPoint.getNumPoints());

        Point newPoint1 = (Point) newMultiPoint.getGeometryN(0);       
        assertEquals(10.0, newPoint1.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(11.0, newPoint1.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(0, newPoint1.getCoordinateSequence().getOrdinate(0, 2), 0);
        assertEquals(3, newPoint1.getCoordinateSequence().getDimension());

        Point newPoint2 = (Point) newMultiPoint.getGeometryN(1);
        assertEquals(20.0, newPoint2.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(21.0, newPoint2.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(0, newPoint2.getCoordinateSequence().getOrdinate(0, 2), 0);
        assertEquals(3, newPoint2.getCoordinateSequence().getDimension());

        Point newPoint3 = (Point) newMultiPoint.getGeometryN(2);
        assertEquals(30.0, newPoint3.getCoordinateSequence().getOrdinate(0, 0), 0);
        assertEquals(31.0, newPoint3.getCoordinateSequence().getOrdinate(0, 1), 0);
        assertEquals(0, newPoint3.getCoordinateSequence().getOrdinate(0, 2), 0);
        assertEquals(3, newPoint3.getCoordinateSequence().getDimension());
    }
    
}
