/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.mesh;


import org.h2gis.utilities.GeometryMetaData;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinfour.common.*;
import org.tinfour.standard.IncrementalTin;
import org.tinfour.utils.TriangleCollector;

import java.util.*;

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
    private GeometryFactory gf;
    private boolean isInput2D;

    /**
     * merge of Vertex instances below this distance
     */
    private double epsilon = 1e-5;

    /**
     * Accelerating structure to merge input points
     */
    Quadtree ptsIndex = new Quadtree();

    /**
     * Input data for Tinfour
     */

    List<IConstraint> constraints = new ArrayList<>();
    List<Integer> constraintIndex = new ArrayList<>();

    private boolean computeNeighbors = false;


    // Output data
    private List<Coordinate> vertices = new ArrayList<Coordinate>();
    private List<Triangle> triangles = new ArrayList<Triangle>();

    /**
     * Create a mesh data structure to collect points and edges that will be
     * used by the Delaunay Triangulation
     */
    public DelaunayData() {
    }

    /**
     * Put a geometry into the data array. Set true to populate the list of
     * points and edges, needed for the ContrainedDelaunayTriangulation. Set
     * false to populate only the list of points. Note the z-value is forced to
     * O when it's equal to NaN.
     *
     * @param geom Geometry
     * @param mode Delaunay mode
     */
    public void put(Geometry geom, MODE mode) throws IllegalArgumentException {
        gf = geom.getFactory();
        if(mode == MODE.TESSELLATION && !(geom instanceof Polygon || geom instanceof MultiPolygon)) {
            throw new IllegalArgumentException("Only Polygon(s) are accepted for tessellation");
        } else {
            int dim = GeometryMetaData.getMetaData(geom).dimension;
            isInput2D = dim == 2;
            addGeometry(geom, 1);
        }
    }

    /**
     *
     * @param coordinate
     * @param index
     * @return
     */
    private Vertex addCoordinate(Coordinate coordinate, int index) {
        final Envelope env = new Envelope(coordinate);
        env.expandBy(epsilon);
        List result = ptsIndex.query(env);
        Vertex found = null;
        for(Object vertex : result) {
            if(vertex instanceof Vertex) {
                if(((Vertex) vertex).getDistance(coordinate.x, coordinate.y) < epsilon) {
                    found = (Vertex) vertex;
                    break;
                }
            }
        }
        if(found == null) {
            found = new Vertex(coordinate.x, coordinate.y, Double.isNaN(coordinate.z) ? 0 : coordinate.z, index);
            ptsIndex.insert(new Envelope(coordinate),  found);
        }
        return found;
    }

    /**
     * Append a polygon into the triangulation
     *
     * @param newPoly Polygon to append into the mesh, internal rings willb be inserted as holes.
     * @param attribute Polygon attribute. {@link Triangle#getAttribute()}
     */
    public void addPolygon(Polygon newPoly, int attribute) {
        final Coordinate[] coordinates = newPoly.getExteriorRing().getCoordinates();
        // Exterior ring must be CCW
        if(!Orientation.isCCW(coordinates)) {
            CoordinateArrays.reverse(coordinates);
        }
        if (coordinates.length >= 4) {
            fillVerticesList(attribute, coordinates);
        }
        // Append holes
        final int holeCount = newPoly.getNumInteriorRing();
        for (int holeIndex = 0; holeIndex < holeCount; holeIndex++) {
            LineString holeLine = newPoly.getInteriorRingN(holeIndex);
            final Coordinate[] hCoordinates = holeLine.getCoordinates();
            // Holes must be CW
            if(Orientation.isCCW(hCoordinates)) {
                CoordinateArrays.reverse(hCoordinates);
            }
            fillVerticesList(attribute, hCoordinates);
        }
    }

    /**
     * @param epsilon Merge vertices with this distance between
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Add vertices to internal vertices structure (remove duplicates)
     * @param attribute
     * @param hCoordinates
     */
    private void fillVerticesList(int attribute, Coordinate[] hCoordinates) {
        // Polygons start with the same coordinate as the last coordinate
        if(hCoordinates.length > 1 && hCoordinates[0].equals2D(hCoordinates[hCoordinates.length - 1])) {
            List<Vertex> vertexList = new ArrayList<>(hCoordinates.length - 1);
            for(int vId = 0; vId < hCoordinates.length - 1 ; vId++) {
                vertexList.add(addCoordinate(hCoordinates[vId], attribute));
            }
            PolygonConstraint polygonConstraint = new PolygonConstraint(vertexList);
            polygonConstraint.complete();
            if(polygonConstraint.isValid()) {
                constraints.add(polygonConstraint);
                constraintIndex.add(attribute);
            }
        } else {
            List<Vertex> vertexList = new ArrayList<>(hCoordinates.length);
            for (Coordinate hCoordinate : hCoordinates) {
                vertexList.add(addCoordinate(hCoordinate, attribute));
            }
            LinearConstraint linearConstraint = new LinearConstraint(vertexList);
            linearConstraint.complete();
            if(linearConstraint.isValid()) {
                constraints.add(linearConstraint);
                constraintIndex.add(attribute);
            }
        }
    }

    private void addLineString(LineString geom, int attribute) {
        fillVerticesList(attribute, geom.getCoordinates());
    }

    private void addGeometry(Geometry geom, int attribute) {
        if (geom instanceof GeometryCollection) {
            // Manage multi polygon, multi linestring and multi point
            for (int j = 0; j < geom.getNumGeometries(); j++) {
                Geometry subGeom = geom.getGeometryN(j);
                addGeometry(subGeom, attribute);
            }
        } else if(geom instanceof Polygon && !geom.isEmpty()) {
            addPolygon((Polygon) geom, attribute);
        } else if(geom instanceof LineString && !geom.isEmpty()) {
            addLineString((LineString) geom, attribute);
        } else if(geom instanceof Point) {
            addCoordinate(geom.getCoordinate(), attribute);
        }
    }

    private List<SimpleTriangle> computeTriangles(IncrementalTin incrementalTin) {
        ArrayList<SimpleTriangle> triangles = new ArrayList<>(incrementalTin.countTriangles().getCount());
        Triangle.TriangleBuilder triangleBuilder = new Triangle.TriangleBuilder(triangles);
        TriangleCollector.visitSimpleTriangles(incrementalTin, triangleBuilder);
        return triangles;
    }

    private static Coordinate toCoordinate(Vertex v, boolean isInput2D) {
        if(isInput2D) {
            return new Coordinate(v.getX(), v.getY());
        } else {
            return new Coordinate(v.getX(), v.getY(), v.getZ());
        }
    }

    public void triangulate() {
        triangles.clear();
        vertices.clear();

        List<Vertex> meshPoints = ptsIndex.queryAll();

        List<SimpleTriangle> simpleTriangles = new ArrayList<>();
        IncrementalTin tin = new IncrementalTin();

        // Add points
        tin.add(meshPoints, null);
        // Add constraints
        tin.addConstraints(constraints, false);

        simpleTriangles = computeTriangles(tin);
        List<Vertex> verts = tin.getVertices();
        vertices = new ArrayList<>(verts.size());
        Map<Vertex, Integer> vertIndex = new HashMap<>();
        for(Vertex v : verts) {
            vertIndex.put(v, vertices.size());
            vertices.add(toCoordinate(v, isInput2D));
        }
        Map<Integer, Integer> edgeIndexToTriangleIndex = new HashMap<>();
        for(SimpleTriangle t : simpleTriangles) {
            int triangleAttribute = 0;
            if(t.getContainingRegion() != null) {
                if(t.getContainingRegion().getConstraintIndex() < constraintIndex.size()) {
                    triangleAttribute = constraintIndex.get(t.getContainingRegion().getConstraintIndex());
                }
            }
            triangles.add(new Triangle(vertIndex.get(t.getVertexA()), vertIndex.get(t.getVertexB()),vertIndex.get(t.getVertexC()), triangleAttribute));
            edgeIndexToTriangleIndex.put(t.getEdgeA().getIndex(), triangles.size() - 1);
            edgeIndexToTriangleIndex.put(t.getEdgeB().getIndex(), triangles.size() - 1);
            edgeIndexToTriangleIndex.put(t.getEdgeC().getIndex(), triangles.size() - 1);
        }
    }

    public MultiPolygon getTrianglesAsMultiPolygon() {
        if(!triangles.isEmpty()) {
            // Convert into multi polygon
            Polygon[] polygons = new Polygon[triangles.size()];
            for (int idTriangle = 0; idTriangle < polygons.length; idTriangle++) {
                final Triangle triangle = triangles.get(idTriangle);
                polygons[idTriangle] = gf.createPolygon(new Coordinate[]{vertices.get(triangle.getA()),
                        vertices.get(triangle.getB()), vertices.get(triangle.getC()), vertices.get(triangle.getA())});
            }
            return gf.createMultiPolygon(polygons);
        } else {
            return gf.createMultiPolygon(new Polygon[0]);
        }
    }

    /**
     * Return the 3D area of all triangles
     * @return the area of the triangles in 3D
     */
    public double get3DArea(){
        double cumulatedArea = 0;
        for (final Triangle triangle : triangles) {
            cumulatedArea += computeTriangleArea3D(vertices.get(triangle.getA()),
                    vertices.get(triangle.getB()), vertices.get(triangle.getC()));
        }
        return cumulatedArea;
    }

    /**
     * Computes the 3D area of a triangle.
     * Uses the formula 1/2 * | u x v | where u,v are the side vectors of
     * the triangle x is the vector cross-product
     * @param p1 First vertex
     * @param p2 Second vertex
     * @param p3 Third vertex
     * @return triangle area
     */
    public static double computeTriangleArea3D(Coordinate p1, Coordinate p2, Coordinate p3) {
        // side vectors u and v
        double ux = p2.getX() - p1.getX();
        double uy = p2.getY() - p1.getY();
        double uz = Double.isNaN(p1.z) || Double.isNaN(p2.z) ? 0 : p2.getZ() - p1.getZ();

        double vx = p3.getX() - p1.getX();
        double vy = p3.getY() - p1.getY();
        double vz = Double.isNaN(p1.z) || Double.isNaN(p3.z) ? 0 : p3.getZ() - p1.getZ();

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

    /**
     * Populate hashmap with provided segment
     * @param segmentHashMap
     * @param a Start point
     * @param b End point
     */
    private void addSegment(Set<LineSegment> segmentHashMap, Coordinate a, Coordinate b) {
        LineSegment lineSegment = new LineSegment(a, b);
        lineSegment.normalize();
        segmentHashMap.add(lineSegment);
    }

    /**
     * @return Unique triangles edges as a MultiLineString
     */
    public MultiLineString getTrianglesSides() {
        // Remove duplicates edges thanks to this hash map of normalized line segments
        Set<LineSegment> segmentHashMap = new HashSet<LineSegment>(triangles.size());
        for(Triangle triangle : triangles) {
            addSegment(segmentHashMap, vertices.get(triangle.getA()), vertices.get(triangle.getB()));
            addSegment(segmentHashMap, vertices.get(triangle.getB()),vertices.get(triangle.getC()));
            addSegment(segmentHashMap, vertices.get(triangle.getC()),vertices.get(triangle.getA()));
        }
        LineString[] lineStrings = new LineString[segmentHashMap.size()];
        int i = 0;
        for(LineSegment lineSegment : segmentHashMap) {
            lineStrings[i++] = lineSegment.toGeometry(gf);
        }
        return gf.createMultiLineString(lineStrings);
    }

}
