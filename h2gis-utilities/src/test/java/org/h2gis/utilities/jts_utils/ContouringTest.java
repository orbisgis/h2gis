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
import org.locationtech.jts.geom.TopologyException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for contouring
 * @author Nicolas Fortin
 */
public class ContouringTest {
    private static final double EPSILON = .01;


    @Test
    public void testWithoutIso() throws TopologyException {
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(new Coordinate(7,2),
                new Coordinate(13,4),
                new Coordinate(5,7),
                2885245,2765123,12711064
        );
        Contouring.processTriangle(triangleData, new ArrayList<Double>());
    }

    @Test
    public void testContouringTriangle() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(new Coordinate(7,2),
                new Coordinate(13,4),
                new Coordinate(5,7),
                2885245,2765123,12711064
        );
        //Iso ranges
        String isolevels_str = "31622, 100000, 316227, 1000000, 3162277, 1e+7, 31622776, 1e+20";
        LinkedList<Double> iso_lvls = new LinkedList<Double>();
        for (String isolvl : isolevels_str.split(",")) {
            iso_lvls.add(Double.valueOf(isolvl));
        }
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver= Contouring.processTriangle(triangleData, iso_lvls);
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(5, subdividedTri);
    }

    @Test
    public void testContouringTriangle2() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(new Coordinate(-6.04, -0.56, 3),
                new Coordinate(-5.7,-4.15, 4),
                new Coordinate(0.3,1.41, 4.4),
                3,4,4.4
        );
        //Iso ranges
        LinkedList<Double> iso_lvls = new LinkedList<Double>(Arrays.asList(4.,5.));
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver= Contouring.processTriangle(triangleData, iso_lvls);
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertTrue(subdividedTri == 2);
        // Internal
        TriMarkers tri1 = triangleToDriver.get((short) 0).getFirst();
        TriMarkers tri2 = triangleToDriver.get((short) 1).getFirst();
        assertEquals(4, tri1.getMarker(0), EPSILON);
        assertEquals(3, tri1.getMarker(1), EPSILON);
        assertEquals(4, tri1.getMarker(2), EPSILON);
        // External
        assertEquals(4, tri2.getMarker(0), EPSILON);
        assertEquals(4.4, tri2.getMarker(1), EPSILON);
        assertEquals(4, tri2.getMarker(2), EPSILON);
    }

    @Test
    public void testContouringTriangle3() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(new Coordinate(-9.19, 3.7, 3),
                new Coordinate(0.3,1.41, 4.4),
                new Coordinate(-5.7,-4.15, 1),
                3,4.4,1
        );
        //Iso ranges
        LinkedList<Double> iso_lvls = new LinkedList<Double>(Arrays.asList(3.,4.,5.));
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver= Contouring.processTriangle(triangleData, iso_lvls);
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }

    }

    @Test
    public void testContouringTriangle4() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                0, 0, 1
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(0.5, Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(3, subdividedTri);
        // 0-0.5
        TriMarkers tri1 = triangleToDriver.get((short) 0).pop();
        TriMarkers tri2 = triangleToDriver.get((short) 0).pop();
        TriMarkers tri3 = triangleToDriver.get((short) 1).pop();
        assertEquals(0.5, tri1.getMarker(0), EPSILON);
        assertEquals(0, tri1.getMarker(1), EPSILON);
        assertEquals(0.5, tri1.getMarker(2), EPSILON);
        assertEquals(0.5, tri2.getMarker(0), EPSILON);
        assertEquals(0, tri2.getMarker(1), EPSILON);
        assertEquals(0, tri2.getMarker(2), EPSILON);
        // 0.5-1
        assertEquals(.5, tri3.getMarker(0), EPSILON);
        assertEquals(.5, tri3.getMarker(1), EPSILON);
        assertEquals(1, tri3.getMarker(2), EPSILON);
    }

    @Test
    public void testContouringTriangle5() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                0.5, 0, 1
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(0.5, Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(2, subdividedTri);
        // 0-0.5
        TriMarkers tri1 = triangleToDriver.get((short) 0).pop();
        TriMarkers tri2 = triangleToDriver.get((short) 1).pop();
        assertEquals(0.5, tri1.getMarker(0), EPSILON);
        assertEquals(0, tri1.getMarker(1), EPSILON);
        assertEquals(0.5, tri1.getMarker(2), EPSILON);
        // 0.5-1
        assertEquals(.5, tri2.getMarker(0), EPSILON);
        assertEquals(1, tri2.getMarker(1), EPSILON);
        assertEquals(0.5, tri2.getMarker(2), EPSILON);
    }

    @Test
    public void testContouringTriangle6() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                0, 0.5, 1
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(0.5, Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(2, subdividedTri);
        // 0-0.5
        TriMarkers tri1 = triangleToDriver.get((short) 0).pop();
        TriMarkers tri2 = triangleToDriver.get((short) 1).pop();
        assertEquals(0.5, tri1.getMarker(0), EPSILON);
        assertEquals(0, tri1.getMarker(1), EPSILON);
        assertEquals(0.5, tri1.getMarker(2), EPSILON);
        // 0.5-1
        assertEquals(.5, tri2.getMarker(0), EPSILON);
        assertEquals(1, tri2.getMarker(1), EPSILON);
        assertEquals(0.5, tri2.getMarker(2), EPSILON);
    }

    @Test
    public void testContouringTriangle7() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                0, 1, 0.5
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(0.5, Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(2, subdividedTri);
        // 0-0.5
        TriMarkers tri1 = triangleToDriver.get((short) 0).pop();
        TriMarkers tri2 = triangleToDriver.get((short) 1).pop();
        assertEquals(0.5, tri1.getMarker(0), EPSILON);
        assertEquals(0, tri1.getMarker(1), EPSILON);
        assertEquals(0.5, tri1.getMarker(2), EPSILON);
        // 0.5-1
        assertEquals(.5, tri2.getMarker(0), EPSILON);
        assertEquals(1, tri2.getMarker(1), EPSILON);
        assertEquals(0.5, tri2.getMarker(2), EPSILON);
    }

    @Test
    public void testContouringTriangle8() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                1, 0, 0.5
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(0.5, Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(2, subdividedTri);
        // 0-0.5
        TriMarkers tri1 = triangleToDriver.get((short) 0).pop();
        TriMarkers tri2 = triangleToDriver.get((short) 1).pop();
        assertEquals(0.5, tri1.getMarker(0), EPSILON);
        assertEquals(0, tri1.getMarker(1), EPSILON);
        assertEquals(0.5, tri1.getMarker(2), EPSILON);
        // 0.5-1
        assertEquals(.5, tri2.getMarker(0), EPSILON);
        assertEquals(1, tri2.getMarker(1), EPSILON);
        assertEquals(0.5, tri2.getMarker(2), EPSILON);
    }

    @Test
    public void testContouringTriangle9() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                1, 0, 0.5
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(1., Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(1, subdividedTri);
    }

    @Test
    public void testContouringTriangle10() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                1, 0, 0.5
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(-0.5, Double.MAX_VALUE));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(1, subdividedTri);
    }

    @Test
    public void testContouringTriangle11() throws TopologyException {
        int subdividedTri = 0;
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                1, 0, 0.5
        );
        //Split the triangle into multiple triangles
        Map<Short,Deque<TriMarkers>> triangleToDriver = Contouring.processTriangle(triangleData,
                Arrays.asList(5.));
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertEquals(1, subdividedTri);
    }

    @Test
    public void testContouringTriangle12() throws TopologyException {
        //Input Data, a Triangle
        TriMarkers triangleData = new TriMarkers(
                new Coordinate(1, 1),
                new Coordinate(1, 7),
                new Coordinate(4, 4),
                2, 1, 1
        );
        //Split the triangle into multiple triangles
        Deque<TriMarkers> inside = new LinkedList<TriMarkers>();
        Deque<TriMarkers> outside = new LinkedList<TriMarkers>();
        Contouring.splitInterval (1.5, 2,triangleData,inside,outside);
        assertEquals(2, inside.size());
        assertEquals(1, outside.size());
        // -inf 0.5
        TriMarkers tri1 = inside.pop();
        TriMarkers tri2 = inside.pop();
        TriMarkers tri3 = outside.pop();

        assertEquals(1.5, tri1.getMarker(0), EPSILON);
        assertEquals(1.5, tri1.getMarker(1), EPSILON);
        assertEquals(1, tri2.getMarker(2), EPSILON);
        assertEquals(1.5, tri2.getMarker(0), EPSILON);
        assertEquals(1, tri2.getMarker(1), EPSILON);
        assertEquals(1, tri2.getMarker(2), EPSILON);

        // 0.5 +inf
        assertEquals(1.5, tri3.getMarker(0), EPSILON);
        assertEquals(2, tri3.getMarker(1), EPSILON);
        assertEquals(1.5, tri3.getMarker(2), EPSILON);
    }
}
