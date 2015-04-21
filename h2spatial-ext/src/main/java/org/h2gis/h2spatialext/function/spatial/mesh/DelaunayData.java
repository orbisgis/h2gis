/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.mesh;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import org.h2gis.utilities.jts_utils.CoordinateSequenceDimensionFilter;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.Triangulatable;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.delaunay.sweep.DTSweepContext;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.ConstrainedPointSet;
import org.poly2tri.triangulation.sets.PointSet;

/**
 * This class is used to collect all data used to compute a mesh based on a
 * Delaunay triangulation
 *
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class DelaunayData {
    public enum MODE {DELAUNAY, CONSTRAINED, TESSELLATION}
    private boolean isInput2D;
    private GeometryFactory gf;
    private boolean isMixedDimension;
    private int dimension;
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


    /**
     * Put a geometry into the data array. Set true to populate the list of
     * points and edges, needed for the ContrainedDelaunayTriangulation. Set
     * false to populate only the list of points. Note the z-value is forced to
     * O when it's equal to NaN.
     *
     * @param geom Geometry
     * @param mode Delaunay mode
     * @throws DelaunayError
     */
    public void put(Geometry geom, MODE mode) throws IllegalArgumentException {
        CoordinateSequenceDimensionFilter info = CoordinateSequenceDimensionFilter.apply(geom);
        gf = geom.getFactory();
        isInput2D = info.is2D();
        isMixedDimension = info.isMixed();
        dimension = info.getDimension();
        convertedInput = null;
        if (mode == MODE.DELAUNAY || geom instanceof Point || geom instanceof MultiPoint) {
            setCoordinates(geom);
        } else {
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
    }

    public void triangulate() {
        Poly2Tri.triangulate(TriangulationAlgorithm.DTSweep, convertedInput);
    }

    public MultiPolygon getTriangles() {
        List<DelaunayTriangle> delaunayTriangle = convertedInput.getTriangles();
        // Convert into multi polygon
        Polygon[] polygons = new Polygon[delaunayTriangle.size()];
        for(int idTriangle=0; idTriangle < polygons.length; idTriangle++) {
            TriangulationPoint[] pts = delaunayTriangle.get(idTriangle).points;
            polygons[idTriangle] = gf.createPolygon(new Coordinate[]{toJts(isInput2D , pts[0]),toJts(isInput2D, pts[1]),
                    toJts(isInput2D, pts[2]), toJts(isInput2D, pts[0])});
        }
        return gf.createMultiPolygon(polygons);
    }

    /**
     * Add a geometry to the list of points and edges used by the triangulation.
     * @param geom
     * @throws DelaunayError
     */
    private void addGeometry(Geometry geom) throws IllegalArgumentException {
        if(!geom.isValid()) {
            throw new IllegalArgumentException("Provided geometry is not valid !");
        }
        if(geom instanceof GeometryCollection) {
            List<TriangulationPoint> pts = new ArrayList<TriangulationPoint>(geom.getNumPoints());
            List<Integer> segments = null;
            if(!isMixedDimension && dimension != 0) {
                segments = new ArrayList<Integer>(pts.size());
            }
            PointHandler pointHandler = new PointHandler(this, pts);
            LineStringHandler lineStringHandler = new LineStringHandler(this, pts, segments);
            for(int geomId = 0; geomId < geom.getNumGeometries(); geomId++) {
                addSimpleGeometry(geom.getGeometryN(geomId), pointHandler, lineStringHandler);
            }
            if(segments != null) {
                int[] index = new int[segments.size()];
                for(int i = 0; i < index.length; i++) {
                    index[i] = segments.get(i);
                }
                convertedInput = new ConstrainedPointSet(pts, index);
            } else {
                convertedInput = new PointSet(pts);
            }
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
    /**
     * Add all coordinates of the geometry to the list of points
     *
     * @param geom
     * @throws DelaunayError
     */
    private void setCoordinates(Geometry geom) throws IllegalArgumentException {
        List<TriangulationPoint> pts = new ArrayList<TriangulationPoint>(geom.getNumPoints());
        PointHandler pointHandler = new PointHandler(this, pts);
        geom.apply(pointHandler);
        convertedInput = new PointSet(pts);
    }

    private static class PointHandler implements CoordinateFilter {
        private DelaunayData delaunayData;
        private List<TriangulationPoint> pts;

        public PointHandler(DelaunayData delaunayData, List<TriangulationPoint> pts) {
            this.delaunayData = delaunayData;
            this.pts = pts;
        }

        @Override
        public void filter(Coordinate pt) {
            pts.add(new TPoint(delaunayData.r(pt.x), delaunayData.r(pt.y),
                    Double.isNaN(pt.z) ? 0 : delaunayData.r(pt.z)));
        }
    }
    private static class LineStringHandler implements CoordinateFilter {
        private DelaunayData delaunayData;
        private List<TriangulationPoint> pts;
        List<Integer> segments;
        private int index = 0;
        private Coordinate firstPt = null;


        public LineStringHandler(DelaunayData delaunayData, List<TriangulationPoint> pts, List<Integer> segments) {
            this.delaunayData = delaunayData;
            this.pts = pts;
            this.segments = segments;
        }

        public void reset() {
            index = 0;
            firstPt = null;
        }

        @Override
        public void filter(Coordinate pt) {
            if(index > 0 && index % 2 == 0) {
                // If new couple then start with same index
                segments.add(index - 1);
            }
            segments.add(index);
            if(!pt.equals(firstPt)) {
                pts.add(new TPoint(delaunayData.r(pt.x), delaunayData.r(pt.y), Double.isNaN(pt.z) ? 0 : delaunayData.r(pt.z)));
                if(index == 0) {
                    firstPt = pt;
                }
                index++;
            }
        }
    }
}
