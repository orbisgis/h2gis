package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.TopologyException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for contouring
 * @author Nicolas Fortin
 */
public class ContouringTest {

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
        assertTrue(subdividedTri==5);
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
        triangleToDriver.get(0);
        for(Map.Entry<Short,Deque<TriMarkers>> entry : triangleToDriver.entrySet()) {
            subdividedTri+=entry.getValue().size();
        }
        assertTrue(subdividedTri==2);
    }
}
