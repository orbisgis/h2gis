package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.TopologyException;
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
}
