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

package org.h2gis.functions.spatial.mesh;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.h2gis.functions.spatial.aggregate.ST_Accum;
import org.h2gis.functions.spatial.convert.ST_ToMultiLine;
import org.h2gis.utilities.jts_utils.CoordinateSequenceDimensionFilter;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.Triangulatable;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.ConstrainedPointSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to collect all data used to compute a mesh based on a
 * Delaunay triangulation
 *
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class DelaunayData {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelaunayData.class);
    public enum MODE {DELAUNAY, CONSTRAINED, TESSELLATION}
    private boolean isInput2D;
    private GeometryFactory gf;
    private Triangulatable convertedInput = null;
    // Precision
    private MathContext mathContext = MathContext.DECIMAL64;

    /**
     * Create a mesh data structure to collect points and edges that will be
     * used by the Delaunay Triangulation
     */
    public DelaunayData() {
    }


    private double r(double v) {
        return new BigDecimal(v).round(mathContext).doubleValue();
    }

    private org.poly2tri.geometry.polygon.Polygon makePolygon(LineString lineString) {
        PolygonPoint[] points = new PolygonPoint[lineString.getNumPoints() - 1];
        for(int idPoint=0; idPoint < points.length; idPoint++) {
            Coordinate point = lineString.getCoordinateN(idPoint);
            points[idPoint] = new PolygonPoint(r(point.x), r(point.y), Double.isNaN(point.z) ? 0 : r(point.z));
        }
        return new org.poly2tri.geometry.polygon.Polygon(points);
    }

    private org.poly2tri.geometry.polygon.Polygon makePolygon(Polygon polygon) {
        org.poly2tri.geometry.polygon.Polygon poly = makePolygon(polygon.getExteriorRing());
        // Add holes
        for(int idHole = 0; idHole < polygon.getNumInteriorRing(); idHole++) {
            poly.addHole(makePolygon(polygon.getInteriorRingN(idHole)));
        }
        return poly;
    }

    private static Coordinate toJts(boolean is2d, org.poly2tri.geometry.primitives.Point pt) {
        if(is2d) {
            return new Coordinate(pt.getX(), pt.getY());
        } else {
            return new Coordinate(pt.getX(), pt.getY(), pt.getZ());
        }
    }
    private int getMinDimension(GeometryCollection geometries) {
        int dimension = Integer.MAX_VALUE;
        for (int i = 0; i < geometries.getNumGeometries(); i++) {
            dimension = Math.min(dimension, geometries.getGeometryN(i).getDimension());
        }
        if(dimension == Integer.MAX_VALUE) {
            dimension = -1;
        }
        return dimension;
    }
    /**
     * Put a geometry into the data array. Set true to populate the list of
     * points and edges, needed for the ContrainedDelaunayTriangulation. Set
     * false to populate only the list of points. Note the z-value is forced to
     * O when it's equal to NaN.
     *
     * @param geom Geometry
     * @param mode Delaunay mode
     * @throws IllegalArgumentException
     */
    public void put(Geometry geom, MODE mode) throws IllegalArgumentException {
        gf = geom.getFactory();
        convertedInput = null;
        // Does not use instanceof here as we must not match for overload of GeometryCollection
        int dimension;
        if(geom.getClass().getName().equals(GeometryCollection.class.getName())) {
            dimension = getMinDimension((GeometryCollection)geom);
        } else {
            dimension = geom.getDimension();
        }
        // Workaround for issue 105 "Poly2Tri does not make a valid convexHull for points and linestrings delaunay
        // https://code.google.com/p/poly2tri/issues/detail?id=105
        if(mode != MODE.TESSELLATION) {
            Geometry convexHull = new FullConvexHull(geom).getConvexHull();
            if(convexHull instanceof Polygon && convexHull.isValid()) {
                // Does not use instanceof here as we must not match for overload of GeometryCollection
                if(geom.getClass().getName().equals(GeometryCollection.class.getName())) {
                    if(dimension > 0) {
                        // Mixed geometry, try to unify sub-types
                        try {
                            geom = ST_ToMultiLine.createMultiLineString(geom).union();
                        } catch (SQLException ex) {
                            throw new IllegalArgumentException(ex);
                        }
                        if(geom.getClass().getName().equals(GeometryCollection.class.getName())) {
                            throw new IllegalArgumentException("Delaunay does not support mixed geometry type");
                        }
                    }
                }
                if(dimension > 0) {
                    geom = ((Polygon) convexHull).getExteriorRing().union(geom);
                } else {
                    ST_Accum accum = new ST_Accum();
                    try {
                        accum.add(geom);
                        accum.add(convexHull);
                        geom = accum.getResult();
                    } catch (SQLException ex) {
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                    }
                }
            } else {
                return;
            }
        }
        // end workaround
        CoordinateSequenceDimensionFilter info = CoordinateSequenceDimensionFilter.apply(geom);
        isInput2D = info.is2D();
        convertedInput = null;
        if(mode == MODE.TESSELLATION) {
            if(geom instanceof Polygon) {
                convertedInput = makePolygon((Polygon) geom);
            } else {
                throw new IllegalArgumentException("Only Polygon are accepted for tessellation");
            }
        } else {
            // Constraint delaunay of segments
            addGeometry(geom);
        }
    }

    public void triangulate() {
        if(convertedInput != null) {
            Poly2Tri.triangulate(TriangulationAlgorithm.DTSweep, convertedInput);
        }
    }

    public MultiPolygon getTriangles() {
        if(convertedInput != null) {
            List<DelaunayTriangle> delaunayTriangle = convertedInput.getTriangles();
            // Convert into multi polygon
            Polygon[] polygons = new Polygon[delaunayTriangle.size()];
            for (int idTriangle = 0; idTriangle < polygons.length; idTriangle++) {
                TriangulationPoint[] pts = delaunayTriangle.get(idTriangle).points;
                polygons[idTriangle] = gf.createPolygon(new Coordinate[]{toJts(isInput2D, pts[0]), toJts(isInput2D, pts[1]), toJts(isInput2D, pts[2]), toJts(isInput2D, pts[0])});
            }
            return gf.createMultiPolygon(polygons);
        } else {
            return gf.createMultiPolygon(new Polygon[0]);
        }
    }
    
    
    /**
     * Return the 3D area of all triangles
     * @return 
     */
    public double get3DArea(){
        if(convertedInput != null) {
            List<DelaunayTriangle> delaunayTriangle = convertedInput.getTriangles();
            double sum = 0;
            for (DelaunayTriangle triangle : delaunayTriangle) {
                sum += computeTriangleArea3D(triangle);                
            }
            return sum;
        } else {
            return 0;
        }
    }
    
    /**
     * Computes the 3D area of a triangle.
     *
     * @param triangle
     * @return
     */
    private double computeTriangleArea3D(DelaunayTriangle triangle) {
        TriangulationPoint[] points = triangle.points;       
        TriangulationPoint p1 = points[0];
        TriangulationPoint p2 = points[1];
        TriangulationPoint p3 = points[2];        
        /**
         * Uses the formula 1/2 * | u x v | where u,v are the side vectors of
         * the triangle x is the vector cross-product
         */
        // side vectors u and v
        double ux = p2.getX() - p1.getX();
        double uy = p2.getY() - p1.getY();
        double uz = p2.getZ() - p1.getZ();

        double vx = p3.getX() - p1.getX();
        double vy = p3.getY() - p1.getY();
        double vz = p3.getZ() - p1.getZ();
        
        if (Double.isNaN(uz) || Double.isNaN(vz)) {
            uz=1;
            vz=1;
        }

        // cross-product = u x v
        double crossx = uy * vz - uz * vy;
        double crossy = uz * vx - ux * vz;
        double crossz = ux * vy - uy * vx;

        // tri area = 1/2 * | u x v |
        double absSq = crossx * crossx + crossy * crossy + crossz * crossz;
        return Math.sqrt(absSq) / 2;
    }

    private void addSegment(Set<LineSegment> segmentHashMap, TriangulationPoint a, TriangulationPoint b) {
        LineSegment lineSegment = new LineSegment(toJts(isInput2D, a), toJts(isInput2D, b));
        lineSegment.normalize();
        segmentHashMap.add(lineSegment);
    }

    /**
     * @return Unique triangles edges
     */
    public MultiLineString getTrianglesSides() {
        List<DelaunayTriangle> delaunayTriangle = convertedInput.getTriangles();
        // Remove duplicates edges thanks to this hash map of normalized line segments
        Set<LineSegment> segmentHashMap = new HashSet<LineSegment>(delaunayTriangle.size());
        for(DelaunayTriangle triangle : delaunayTriangle) {
            TriangulationPoint[] pts = triangle.points;
            addSegment(segmentHashMap, pts[0], pts[1]);
            addSegment(segmentHashMap, pts[1], pts[2]);
            addSegment(segmentHashMap, pts[2], pts[0]);
        }
        LineString[] lineStrings = new LineString[segmentHashMap.size()];
        int i = 0;
        for(LineSegment lineSegment : segmentHashMap) {
            lineStrings[i++] = lineSegment.toGeometry(gf);
        }
        return gf.createMultiLineString(lineStrings);
    }

    /**
     * Add a geometry to the list of points and edges used by the triangulation.
     * @param geom Any geometry
     * @throws IllegalArgumentException
     */
    private void addGeometry(Geometry geom) throws IllegalArgumentException {
        if(!geom.isValid()) {
            throw new IllegalArgumentException("Provided geometry is not valid !");
        }
        if(geom instanceof GeometryCollection) {
            Map<TriangulationPoint, Integer> pts = new HashMap<TriangulationPoint, Integer>(geom.getNumPoints());
            List<Integer> segments = new ArrayList<Integer>(pts.size());
            AtomicInteger pointsCount = new AtomicInteger(0);
            PointHandler pointHandler = new PointHandler(this, pts, pointsCount);
            LineStringHandler lineStringHandler = new LineStringHandler(this, pts, pointsCount, segments);
            for(int geomId = 0; geomId < geom.getNumGeometries(); geomId++) {
                addSimpleGeometry(geom.getGeometryN(geomId), pointHandler, lineStringHandler);
            }
            int[] index = new int[segments.size()];
            for(int i = 0; i < index.length; i++) {
                index[i] = segments.get(i);
            }
            // Construct final points array by reversing key,value of hash map
            TriangulationPoint[] ptsArray = new TriangulationPoint[pointsCount.get()];
            for(Map.Entry<TriangulationPoint, Integer> entry : pts.entrySet()) {
                ptsArray[entry.getValue()] = entry.getKey();
            }
            pts.clear();
            convertedInput = new ConstrainedPointSet(Arrays.asList(ptsArray), index);
        } else {
            addGeometry(geom.getFactory().createGeometryCollection(new Geometry[]{geom}));
        }
    }

    private void addSimpleGeometry(Geometry geom, PointHandler pointHandler, LineStringHandler lineStringHandler) throws IllegalArgumentException {
        if(geom instanceof Point) {
            geom.apply(pointHandler);
        } else if(geom instanceof LineString) {
            lineStringHandler.reset();
            geom.apply(lineStringHandler);
        } else if(geom instanceof Polygon) {
            Polygon polygon = (Polygon) geom;
            lineStringHandler.reset();
            polygon.getExteriorRing().apply(lineStringHandler);
            for(int idHole = 0; idHole < polygon.getNumInteriorRing(); idHole++) {
                lineStringHandler.reset();
                polygon.getInteriorRingN(idHole).apply(lineStringHandler);
            }
        }
    }

    private static class PointHandler implements CoordinateFilter {
        private DelaunayData delaunayData;
        private Map<TriangulationPoint, Integer> pts;
        private AtomicInteger maxIndex;

        public PointHandler(DelaunayData delaunayData, Map<TriangulationPoint, Integer> pts, AtomicInteger maxIndex) {
            this.delaunayData = delaunayData;
            this.pts = pts;
            this.maxIndex = maxIndex;
        }

        protected int addPt(Coordinate coordinate) {
            TPoint pt = new TPoint(delaunayData.r(coordinate.x), delaunayData.r(coordinate.y),
                    Double.isNaN(coordinate.z) ? 0 : delaunayData.r(coordinate.z));
            Integer index = pts.get(pt);
            if(index == null) {
                index = maxIndex.getAndAdd(1);
                pts.put(pt, index);
            }
            return index;
        }

        @Override
        public void filter(Coordinate pt) {
            addPt(pt);
        }
    }

    private static class LineStringHandler extends PointHandler {
        private List<Integer> segments;
        private int firstPtIndex = -1;

        public LineStringHandler(DelaunayData delaunayData, Map<TriangulationPoint, Integer> pts,
                                 AtomicInteger maxIndex, List<Integer> segments) {
            super(delaunayData, pts, maxIndex);
            this.segments = segments;
        }

        /**
         * New line string
         */
        public void reset() {
            firstPtIndex = -1;
        }

        @Override
        public void filter(Coordinate pt) {
            if (firstPtIndex == -1) {
                firstPtIndex = addPt(pt);
            } else {
                int secondPt = addPt(pt);
                if (secondPt != firstPtIndex) {
                    segments.add(firstPtIndex);
                    segments.add(secondPt);
                    firstPtIndex = secondPt;
                }
            }
        }
    }
}
