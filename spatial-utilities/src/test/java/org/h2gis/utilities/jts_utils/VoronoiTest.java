package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class voronoi
 * @author Nicolas Fortin
 */
public class VoronoiTest {
    private GeometryFactory gf = new GeometryFactory();

    private Polygon tri(Triangle triangle) {
        return gf.createPolygon(new Coordinate[]{triangle.p0, triangle.p1, triangle.p2, triangle.p0});
    }

    private Geometry getTestDelaunayA() {
        Collection<Coordinate> coords = Arrays.asList(
                new Coordinate(2,2),
                new Coordinate(3,5),
                new Coordinate(6,3),
                new Coordinate(9,1),
                new Coordinate(7,7),
                new Coordinate(8,5),
                new Coordinate(11,3),
                new Coordinate(9, 11),
                new Coordinate(4, 7),
                new Coordinate(2, 8),
                new Coordinate(1, 6),
                new Coordinate(8, 9),
                new Coordinate(5, 9),
                new Coordinate(6, 12)
        );
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(coords);
        return builder.getTriangles(gf);
    }

    @Test
    public void testNeighborsComputation() {
        Geometry mesh = getTestDelaunayA();
        Voronoi voronoi = new Voronoi();
        Voronoi.Triple[] neigh = voronoi.generateTriangleNeighbors(mesh);
        assertEquals(mesh.getNumGeometries(), neigh.length);
        // Check if Neighbor tri B of tri A have tri A as neighbor
        for(int aIndex = 0; aIndex < neigh.length; aIndex++) {
            Voronoi.Triple triA = neigh[aIndex];
            if(triA.getA() != -1) {
                assertTrue(neigh[triA.getA()].contains(aIndex));
            }
            if(triA.getB() != -1) {
                assertTrue(neigh[triA.getB()].contains(aIndex));
            }
            if(triA.getC() != -1) {
                assertTrue(neigh[triA.getC()].contains(aIndex));
            }
            // There is at least two neighbors
            assertTrue(triA.toString(), triA.getNeighCount() >= 2);
        }
    }

    @Test
    public void testVoronoiPolygon() throws TopologyException {
        Geometry mesh = getTestDelaunayA();
        Voronoi voronoi = new Voronoi();
        voronoi.generateTriangleNeighbors(mesh);
        // Generate voronoi polygons without boundary
        Geometry voronoiPoly = voronoi.generateVoronoi(true);
        assertEquals(7, voronoiPoly.getNumGeometries());
        // Generate voronoi edges without boundary
    }
}
