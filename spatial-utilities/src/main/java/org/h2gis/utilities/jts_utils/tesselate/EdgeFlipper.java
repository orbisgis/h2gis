package org.h2gis.utilities.jts_utils.tesselate;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import java.util.Collections;
import java.util.List;

/**
 * One problem with ear-clipping is that it produces sub-optimal triangulations in the the sense that it creates lots
 * of very skinny triangles. Which are visually and computationally unappealing.
 * Add a refinement step based on "flipping triangles" to improve the quality of the output triangle mesh.
 * @link {http://lin-ear-th-inking.blogspot.fr/2011/04/polygon-triangulation-via-ear-clipping.html}
 * @author Michael Bedward
 * @author Nicolas Fortin, FR CNRS 2488
 */
public class EdgeFlipper {
    private static final double SQRT3 = Math.sqrt(3.);

    private final List<Coordinate> shellCoords;

    EdgeFlipper(List<Coordinate> shellCoords) {
        this.shellCoords = Collections.unmodifiableList(shellCoords);
    }

    public boolean flip(Triangle ear0, Triangle ear1, int[] sharedVertices) {
        if (sharedVertices == null || sharedVertices.length != 2) {
            return false;
        }
        
        Coordinate shared0 = shellCoords.get(sharedVertices[0]);
        Coordinate shared1 = shellCoords.get(sharedVertices[1]);

        /*
         * Find the unshared vertex of each ear
         */
        int[] vertices = ear0.getVertices();
        int i = 0;
        while (vertices[i] == sharedVertices[0] || vertices[i] == sharedVertices[1]) {
            i++ ;
        }
        int v0 = vertices[i];
        Coordinate c0 = shellCoords.get(v0);

        i = 0;
        vertices = ear1.getVertices();
        while (vertices[i] == sharedVertices[0] || vertices[i] == sharedVertices[1]) {
            i++ ;
        }
        int v1 = vertices[i];
        Coordinate c1 = shellCoords.get(v1);

        int dir0 = CGAlgorithms.orientationIndex(c0, c1, shared0);
        int dir1 = CGAlgorithms.orientationIndex(c0, c1, shared1);
        if (dir0 == -dir1) {
            // The candidate edge is inside. Compare quality of triangles
            // and swap them if the candidate is better.
            Triangle swapResult1 = new Triangle(sharedVertices[0], v0, v1);
            Triangle swapResult2 = new Triangle(v1, v0, sharedVertices[1]);
            double minQualityBefore = Math.min(evaluateQuality(ear0), evaluateQuality(ear1));
            double minQualityAfter = Math.min(evaluateQuality(swapResult1), evaluateQuality(swapResult2));
            if (minQualityAfter > minQualityBefore) {
                ear0.setVertices(sharedVertices[0], v0, v1);
                ear1.setVertices(v1, v0, sharedVertices[1]);
                return true;
            }
        }
        return false;
    }


    /**
     *  Triangle quality evaluation.
     *  @see "Bank, Randolph E., PLTMG: A Software Package for Solving Elliptic Partial Differential Equations, User's Guide 6.0,
     *  Society for Industrial and Applied Mathematics, Philadelphia, PA, 1990."
     *  @param jtsTri Triangle vertex
     *  @return Triangle quality [0-1] (1 for isosceles triangle)
     */
    public static double evaluateQuality(com.vividsolutions.jts.geom.Triangle jtsTri) {
        return (4 * jtsTri.area() * SQRT3) / (
                Math.pow(new LineSegment(jtsTri.p0, jtsTri.p1).getLength(), 2) +
                        Math.pow(new LineSegment(jtsTri.p1, jtsTri.p2).getLength(), 2) +
                        Math.pow(new LineSegment(jtsTri.p2, jtsTri.p0).getLength(), 2));
    }


    private double evaluateQuality(Triangle triangle) {
        final int[] vertex = triangle.getVertices();
        com.vividsolutions.jts.geom.Triangle jtsTri  = new com.vividsolutions.jts.geom.Triangle(
                shellCoords.get(vertex[0]), shellCoords.get(vertex[1]), shellCoords.get(vertex[2]));
        return evaluateQuality(jtsTri);
    }

}
