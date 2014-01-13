/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.utilities.jts_utils;

import org.junit.Test;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.GeometryLocation;
import java.util.List;
import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GeometryEditTest {

    public WKTReader wKTReader = new WKTReader();

    @Test
    public void testLinearInterpolation() throws Exception {
        LineString line = (LineString) wKTReader.read("LINESTRING(0 8, 1 8 , 3 8)");
        LineString result = GeometryEdit.linearZInterpolation(line);
        assertTrue(Double.isNaN(result.getStartPoint().getCoordinate().z));
        assertTrue(Double.isNaN(result.getEndPoint().getCoordinate().z));
        line = (LineString) wKTReader.read("LINESTRING(0 0 0, 5 0 , 10 0 10)");
        result = GeometryEdit.linearZInterpolation(line);
        assertTrue(result.getStartPoint().getCoordinate().z == 0);
        assertTrue(result.getEndPoint().getCoordinate().z == 10);
        assertTrue(result.getCoordinates()[1].z == 5);
    }

    /**
     * Test to split a linestring according a point
     *
     * @throws Exception
     */
    @Test
    public void testSplitLineString() throws Exception {
        LineString line = (LineString) wKTReader.read("LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)");
        Point point = (Point) wKTReader.read("POINT(1.5 4 )");
        LineString[] results = GeometryEdit.splitLineStringWithPoint(line, point, 4);
        assertEquals(results[0], wKTReader.read("LINESTRING(0 8, 1 8 , 1.5 8)"));
        assertEquals(results[1], wKTReader.read("LINESTRING(1.5 8 , 3 8,  8  8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)"));
    }

    /**
     * Test to split a linestring according another linestring
     *
     * @throws Exception
     */
    @Test
    public void testSplitLineStringByLineString() throws Exception {
        LineString lineA = (LineString) wKTReader.read("LINESTRING(0 0, 100 0)");
        LineString lineB = (LineString) wKTReader.read("LINESTRING(50 -50, 50 50)");
        assertTrue(GeometryEdit.splitLineStringWithLine(lineA, lineB).
                equals(wKTReader.read("MULTILINESTRING((0 0, 50 0), (50 0 , 100 0))")));
    }

    /**
     * Test to split a linestring according another linestring
     *
     * @throws Exception
     */
    @Test
    public void testSplitLineStringByLineString2() throws Exception {
        LineString lineA = (LineString) wKTReader.read("LINESTRING(50 0, 100 0)");
        LineString lineB = (LineString) wKTReader.read("LINESTRING(50 50, 100 50)");
        assertTrue(GeometryEdit.splitLineStringWithLine(lineA, lineB).equals(wKTReader.read("LINESTRING(50 0, 100 0)")));
    }

    /**
     * Find the closet point to a linestring based on distance
     *
     * @throws Exception
     */
    @Test
    public void testSnappedPoint() throws Exception {
        LineString line = (LineString) wKTReader.read("LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)");
        Point point = (Point) wKTReader.read("POINT(1.5 4 )");
        //Test a point in a segment
        GeometryLocation geomLocation = GeometryEdit.getVertexToSnap(line, point, 4);
        assertEquals(geomLocation.getSegmentIndex(), 1);
        assertTrue(geomLocation.getCoordinate().equals2D(new Coordinate(1.5, 8)));
        //Test a point on an existing coordinate
        point = (Point) wKTReader.read("POINT(1 4 )");
        geomLocation = GeometryEdit.getVertexToSnap(line, point, 4);
        assertEquals(geomLocation.getSegmentIndex(), 0);
        assertTrue(geomLocation.getCoordinate().equals2D(new Coordinate(1, 8)));
        //Test a point on an existing coordinate
        point = (Point) wKTReader.read("POINT(1 4 )");
        geomLocation = GeometryEdit.getVertexToSnap(line, point, 1);
        assertNull(geomLocation);
    }

    @Test
    public void testSnappedPoint2() throws Exception {
        LineString line = (LineString) wKTReader.read("LINESTRING (102.91254820528033 205.68116285968554, 345 204)");
        Point point = (Point) wKTReader.read("POINT ( 243.56500711237553 204.70440967283074 )");
        //Test a point in a segment
        GeometryLocation geomLocation = GeometryEdit.getVertexToSnap(line, point);
        assertEquals(geomLocation.getSegmentIndex(), 0);
        assertTrue(geomLocation.getCoordinate().equals2D(new Coordinate(243.56500711237555, 204.7044096728307)));
    }

    /**
     * Insert a vertex into a lineString
     *
     * @throws Exception
     */
    @Test
    public void testInsertVertexInLineString() throws Exception {
        LineString lineString = (LineString) wKTReader.read("LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8)");
        Point point = (Point) wKTReader.read("POINT(1.5 4 )");
        //Test a point in a segment
        LineString result = GeometryEdit.insertVertexInLineString(lineString, point, 4);
        assertEquals(result, wKTReader.read("LINESTRING(0 8, 1 8 , 1.5 8, 3 8,  8  8, 10 8, 20 8)"));
        //Test a point on an existing coordinate
        point = (Point) wKTReader.read("POINT(1 4 )");
        result = GeometryEdit.insertVertexInLineString(lineString, point, 4);
        //Because the geometry is not modified
        assertNull(result);
    }

    /**
     * Insert a vertex into a linearring
     *
     * @throws Exception
     */
    @Test
    public void testInsertVertexInLinearRing() throws Exception {
        LinearRing linearRing = (LinearRing) wKTReader.read("LINEARRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 0 8)");
        Point point = (Point) wKTReader.read("POINT(1.5 4 )");
        //Test a point in a segment
        LinearRing result = GeometryEdit.insertVertexInLinearRing(linearRing, point, 4);
        assertEquals(result, wKTReader.read("LINEARRING(0 8, 1 8 , 1.5 8, 3 8,  8  8, 10 8, 20 8, 0 8)"));
        //Test a point on an existing coordinate
        point = (Point) wKTReader.read("POINT(1 4 )");
        result = GeometryEdit.insertVertexInLinearRing(linearRing, point, 4);
        //Because the geometry is not modified
        assertNull(result);
    }

    /**
     * Insert a vertex into a linearring
     *
     * @throws Exception
     */
    @Test
    public void testInsertVertexInPolygon() throws Exception {
        Polygon polygon = (Polygon) wKTReader.read("POLYGON ((118 134, 118 278, 266 278, 266 134, 118 134 ))");
        Point point = (Point) wKTReader.read("POINT(196 278 )");
        //Test a point in a segment
        Polygon result = GeometryEdit.insertVertexInPolygon(polygon, point, 4);
        assertEquals(result, wKTReader.read("POLYGON ((118 134, 118 278,196 278, 266 278, 266 134, 118 134 ))"));
        //Test a point on an existing coordinate
        point = (Point) wKTReader.read("POINT(196 300 )");
        result = GeometryEdit.insertVertexInPolygon(polygon, point, 4);
        //Because the geometry is not modified
        assertNull(result);
    }

    /**
     * Test to split a polygon with a linestring
     *
     * @throws Exception
     */
    @Test
    public void testSplitPolygon() throws Exception {
        //Line intersects polygon
        Polygon polygon = (Polygon) wKTReader.read("POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))");
        LineString line = (LineString) wKTReader.read("LINESTRING (5 0, 5 10)");
        Geometry pols = GeometryEdit.splitPolygonWithLine(polygon, line);
        assertEquals(pols.getNumGeometries(), 2);
        Polygon pol1 = (Polygon) wKTReader.read("POLYGON (( 0 0, 5 0, 5 10 , 0 10, 0 0))");
        Polygon pol2 = (Polygon) wKTReader.read("POLYGON ((5 0, 10 0 , 10 10, 5 10, 5 0))");

        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            if (!pol.getEnvelopeInternal().equals(pol1.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol2.getEnvelopeInternal())) {
                fail();
            }
        }

        //Line within the polygon
        line = (LineString) wKTReader.read("LINESTRING (5 1, 5 8)");
        pols = GeometryEdit.splitPolygonWithLine(polygon, line);
        assertNull(pols);

        //Line with one point intersection
        line = (LineString) wKTReader.read("LINESTRING (5 1, 5 12)");
        pols = GeometryEdit.splitPolygonWithLine(polygon, line);
        assertNull(pols);

        //Line intersects a polygon with a hole
        polygon = (Polygon) wKTReader.read("POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0), (2 2, 7 2, 7 7, 2 7, 2 2))");
        line = (LineString) wKTReader.read("LINESTRING (5 0, 5 10)");
        pols = GeometryEdit.splitPolygonWithLine(polygon, line);

        pol1 = (Polygon) wKTReader.read("POLYGON (( 0 0, 5 0, 5 2 ,2 2, 2 7, 5 7,  5 10, 0 10, 0 0))");
        pol2 = (Polygon) wKTReader.read("POLYGON ((5 0, 5 2, 7 2, 7 7 , 5 7, 5 10, 10 10, 10 0, 5 0))");
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            if (!pol.getEnvelopeInternal().equals(pol1.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol2.getEnvelopeInternal())) {
                fail();
            }
        }

        //Line intersects 2,5 polygon
        //Test if z values already exist
        polygon = (Polygon) wKTReader.read("POLYGON (( 0 0 1, 10 0 5, 10 10 8 , 0 10 12, 0 0 12))");
        line = (LineString) wKTReader.read("LINESTRING (5 0, 5 10)");
        pols = GeometryEdit.splitPolygonWithLine(polygon, line);
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            assertTrue(is25Geometry(pol));
        }
    }

    /**
     * Move a geometry to a new coordinate
     *
     * @throws Exception
     */
    @Test
    public void testMoveGeometry() throws Exception {
        Geometry geom = (Polygon) wKTReader.read("POLYGON (( 0 0 ,10 0, 10 10, 0 10, 0 0 ))");
        Point point = (Point) wKTReader.read("POINT (20 10)");
        //Test move a polygon
        Geometry result = GeometryEdit.moveGeometry(geom, new Coordinate(0, 0), point.getCoordinate());
        assertTrue(result.getCoordinates()[0].equals2D(point.getCoordinate()));
    }

    /**
     * Test cut a polygon
     *
     * @throws Exception
     */
    @Test
    public void testCutPolygon() throws Exception {
        Polygon polygon = (Polygon) wKTReader.read("POLYGON (( 0 0 ,10 0, 10 10, 0 10, 0 0 ))");
        Polygon cutter = (Polygon) wKTReader.read("POLYGON (( 2 2  ,7 2, 7 7, 2 7, 2 2))");
        //Test cut a polygon inside
        List<Polygon> result = GeometryEdit.cutPolygonWithPolygon(polygon, cutter);
        assertEquals(result.get(0).getNumInteriorRing(), 1);
        assertEquals(result.get(0).getInteriorRingN(0).getEnvelopeInternal(), cutter.getEnvelopeInternal());

        //Test cut a polygon outside
        cutter = (Polygon) wKTReader.read("POLYGON (( 2 -1.8153735632183903, 7.177873563218391 -1.8153735632183903, 7.177873563218391 7, 2 7, 2 -1.8153735632183903 ))");
        result = GeometryEdit.cutPolygonWithPolygon(polygon, cutter);
        assertEquals(result.get(0), wKTReader.read("POLYGON (( 2 0, 0 0, 0 10, 10 10, 10 0, 7.177873563218391 0, 7.177873563218391 7, 2 7, 2 0 ))"));
    }

    @Test
    public void testST_RemoveHoles() throws Exception {
        Polygon polygon = (Polygon) wKTReader.read("POLYGON (( 112 68, 112 307, 318 307, 318 68, 112 68 ), "
                + "( 184 169, 247 197, 242 247, 167 258, 184 169 ))");
        Polygon expected = (Polygon) wKTReader.read("POLYGON (( 112 68, 112 307, 318 307, 318 68, 112 68 ))");
        Geometry result = GeometryEdit.removeHoles(polygon);
        assertTrue(result.equals(expected));
    }

    @Test
    public void testST_Remove3Holes() throws Exception {
        Polygon polygon = (Polygon) wKTReader.read("POLYGON (( 112 68, 112 307, 318 307, 318 68, 112 68 ), "
                + "( 184 169, 247 197, 242 247, 167 258, 184 169 ), "
                + "( 235 107, 277 120, 267 167, 221 161, 235 107 ), "
                + "( 277 280, 266 255, 281 249, 300 270, 277 280 ))");
        Polygon expected = (Polygon) wKTReader.read("POLYGON (( 112 68, 112 307, 318 307, 318 68, 112 68 ))");
        Geometry result = GeometryEdit.removeHoles(polygon);
        assertTrue(result.equals(expected));
    }

    /**
     * Returns true is a geometry contains at least one z value.
     *
     * @param geometry
     * @return
     */
    private boolean is25Geometry(Geometry geom) {
        CoordinateSequenceDimensionFilter cf = new CoordinateSequenceDimensionFilter();
        geom.apply(cf);
        return cf.getDimension() >= CoordinateSequenceDimensionFilter.XYZ;
    }
}
