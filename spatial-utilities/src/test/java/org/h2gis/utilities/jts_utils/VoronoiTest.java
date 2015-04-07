package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.*;

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

    private Geometry getTestDelaunayB() {
        Collection<Coordinate> coords = Arrays.asList(
                new Coordinate(2,2),
                new Coordinate(6,3),
                new Coordinate(4, 7),
                new Coordinate(2, 8),
                new Coordinate(1, 6),
                new Coordinate(3,5)
        );
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(coords);
        return builder.getTriangles(gf);
    }

    private void testNeighbor(Geometry mesh) {
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
    public void testNeighborsComputationA() {
        testNeighbor(getTestDelaunayA());
    }

    @Test
    public void testNeighborsComputationB() {
        testNeighbor(getTestDelaunayB());
    }

    @Test
    public void testVoronoiNoEnvelopeA() throws TopologyException {
        Geometry mesh = getTestDelaunayA();
        Voronoi voronoi = new Voronoi();
        Voronoi.Triple[] neigh = voronoi.generateTriangleNeighbors(mesh);
        // Generate voronoi polygons without boundary
        Geometry voronoiPoly = voronoi.generateVoronoi(2);
        assertEquals(7, voronoiPoly.getNumGeometries());
        // Generate voronoi edges without boundary
        Geometry voronoiLines = voronoi.generateVoronoi(1);
        assertEquals(24, voronoiLines.getNumGeometries());
        // Generate voronoi vertex
        Geometry voronoiPoints = voronoi.generateVoronoi(0);
        assertEquals(neigh.length, voronoiPoints.getNumGeometries());
    }

    @Test
    public void testVoronoiNoEnvelopeB() throws TopologyException {
        Geometry mesh = getTestDelaunayB();
        Voronoi voronoi = new Voronoi();
        Voronoi.Triple[] neigh = voronoi.generateTriangleNeighbors(mesh);
        // Generate voronoi polygons without boundary
        Geometry voronoiPoly = voronoi.generateVoronoi(2);
        assertEquals(1, voronoiPoly.getNumGeometries());
        // Generate voronoi edges without boundary
        Geometry voronoiLines = voronoi.generateVoronoi(1);
        // Check zero length
        for (int i = 0; i < voronoiLines.getNumGeometries(); i++) {
            Geometry line = voronoiLines.getGeometryN(i);
            assertNotEquals(line.getLength(), 0., 1e-12);
        }
        assertEquals(4, voronoiLines.getNumGeometries());
        // Generate voronoi vertex
        Geometry voronoiPoints = voronoi.generateVoronoi(0);
        assertEquals(neigh.length, voronoiPoints.getNumGeometries());
    }

    @Test
    public void testVoronoiEnvelope() throws TopologyException,SQLException {
        Geometry mesh = getTestDelaunayB();
        Voronoi voronoi = new Voronoi();
        voronoi.setEnvelope(mesh.getEnvelopeInternal());
        voronoi.generateTriangleNeighbors(mesh);
        // Generate voronoi polygons with boundaries
        Geometry voronoiPoly = voronoi.generateVoronoi(2);
        assertEquals(6, voronoiPoly.getNumGeometries());
        assertGeometryEquals(mesh.getEnvelope().toString(), voronoiPoly.getEnvelope());
        // Generate voronoi lines with boundaries
        Geometry voronoiLines = voronoi.generateVoronoi(1);
        assertEquals(15, voronoiLines.getNumGeometries());
    }

}
