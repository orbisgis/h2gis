package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

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
        voronoi.computeVoronoy(mesh);
        Voronoi.Triple[] neigh = voronoi.getTriangleNeighbors();
        assertEquals(15, neigh.length);
    }
}
