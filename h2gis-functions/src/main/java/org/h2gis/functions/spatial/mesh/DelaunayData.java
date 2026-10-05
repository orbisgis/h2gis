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
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinfour.common.*;
import org.tinfour.refinement.RuppertRefiner;
import org.tinfour.standard.IncrementalTin;
import org.tinfour.utils.TriangleCollector;

import java.util.*;

/**
 * This class is used to collect all data used to compute a mesh based on a
 * Delaunay triangulation
 *
 * @author Erwan Bocher, CNRS
 * @author Nicolas Fortin, Université Gustave Eiffel
 */
public class DelaunayData {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelaunayData.class);
    public enum MODE {DELAUNAY, CONSTRAINED, TESSELLATION}
    private GeometryFactory gf;
    private boolean isInput2D;

    // double type gives from 15 to 17 significant decimal digits precision
    public static double DEFAULT_EPSILON = 1e-12;
    /**
     * merge of Vertex instances below this distance
     */
    private double epsilon = DEFAULT_EPSILON;

    /**
     * Accelerating structure to merge input points
     */
    Quadtree ptsIndex = new Quadtree();

    /**
     * Input data for Tinfour
     */

    List<IConstraint> constraints = new ArrayList<>();
    List<Integer> constraintIndex = new ArrayList<>();

    // Output data
    private List<Coordinate> vertices = new ArrayList<Coordinate>();
    private List<Triangle> triangles = new ArrayList<Triangle>();

    private MODE mode = MODE.DELAUNAY;

    /**
     * Minimum internal angle (degrees) targeted by the Delaunay refinement.
     * 0 means that no refinement is applied.
     */
    private double minAngle = 0;

    /**
     * Triangles with an area lower than this value are not refined.
     * A negative value means that the threshold is computed from the input data.
     */
    private double minTriangleArea = MIN_TRIANGLE_AREA;

    /**
     * Value of minTriangleArea asking for a threshold computed from the input data
     */
    public static final double MIN_TRIANGLE_AREA = -1;

    /**
     * Factor applied to the squared length of the shortest edge of the unrefined triangulation
     * to compute the default minimum triangle area of the refinement.
     */
    static final double MIN_AREA_FACTOR = 0.1;

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
        this.mode = mode;
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
     * Enable the Delaunay refinement (Ruppert algorithm) of the triangulation.
     * Steiner points are inserted until all triangles have an internal angle
     * greater than or equal to {@code minAngle}. Constraints are preserved:
     * constrained edges may be split but never removed.
     * Must be called before {@link #put(Geometry, MODE)}.
     *
     * @param minAngle minimum internal angle in degrees, in the range ]0, 60[.
     *                 Set 0 to disable the refinement.
     * @param minTriangleArea triangles with an area lower than this value are not refined
     *                         unit of the coordinates)
     * @throws IllegalArgumentException if a parameter is outside its valid range
     */
    public void setRefinement(double minAngle, double minTriangleArea) {
        if (!(minAngle == 0 || (minAngle > 0 && minAngle < 60))) {
            throw new IllegalArgumentException("The minimum angle must be 0 (no refinement) or in the range ]0, 60[ degrees");
        }
        if (!Double.isFinite(minTriangleArea) || (minTriangleArea < 0 && minTriangleArea != MIN_TRIANGLE_AREA)) {
            throw new IllegalArgumentException("The minimum triangle area must be a finite value >= 0");
        }
        this.minAngle = minAngle;
        this.minTriangleArea = minTriangleArea;
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
        Coordinate[] coordinates = geom.getCoordinates();
        if (minAngle > 0 && geom.isClosed() && coordinates.length >= 4 && !Orientation.isCCW(coordinates)) {
            // A closed line is inserted as a polygon constraint: the refinement needs a CCW ring,
            // a CW ring being considered as a hole by Tinfour.
            coordinates = coordinates.clone();
            CoordinateArrays.reverse(coordinates);
        }
        fillVerticesList(attribute, coordinates);
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
        IncrementalTin tin = new IncrementalTin(epsilon);

        // Add points
        tin.add(meshPoints, null);
        // Add constraints
        List<IConstraint> tinConstraints = constraints;
        PreparedGeometry preparedHullOfPoints = null;
        if (minAngle > 0) {
            Geometry hullOfPoints = convexHull(meshPoints);
            if (hullOfPoints instanceof Polygon) {
                preparedHullOfPoints = PreparedGeometryFactory.prepare(hullOfPoints);
                if (mode != MODE.TESSELLATION && !constraints.isEmpty()) {
                    // Tinfour only refines the triangles located inside a polygon constraint.
                    PolygonConstraint hull = convexHullConstraint((Polygon) hullOfPoints, meshPoints);
                    if (hull != null) {
                        tinConstraints = new ArrayList<>(constraints);
                        tinConstraints.add(hull);
                    }
                }
            }
        }
        tin.addConstraints(tinConstraints, false);

        STRtree originalTriangles = null;
        if (minAngle > 0 && tin.isBootstrapped()) {
            originalTriangles = refine(tin);
        }

        simpleTriangles = computeTriangles(tin);
        List<Vertex> verts = tin.getVertices();
        vertices = new ArrayList<>(verts.size());
        Map<Vertex, Integer> vertIndex = new HashMap<>();
        for(Vertex v : verts) {
            vertIndex.put(v, vertices.size());
            Coordinate coordinate = toCoordinate(v, isInput2D);
            if (originalTriangles != null && (v.isRefinementProduct() || Double.isNaN(coordinate.z))) {
                // Steiner point inserted by the refinement (see Rupper). We must recompute the z
                double z = interpolateZ(originalTriangles, v.getX(), v.getY());
                coordinate.setZ(Double.isNaN(z) ? 0 : z);
            }
            vertices.add(coordinate);
        }
        for(SimpleTriangle t : simpleTriangles) {
            int triangleAttribute = 0;
            if(t.getContainingRegion() != null) {
                if(t.getContainingRegion().getConstraintIndex() < constraintIndex.size()) {
                    triangleAttribute = constraintIndex.get(t.getContainingRegion().getConstraintIndex());
                }
            }
            if (minAngle > 0) {
                // Flat triangle created by the split of a constrained edge due to rounding
                if (isDegenerate(t)) {
                    continue;
                }
                // Steiner points may be inserted outside of the convexhull of the input points
                if (preparedHullOfPoints != null && !isInside(preparedHullOfPoints, t)) {
                    continue;
                }
            }
            if(mode != MODE.TESSELLATION || triangleAttribute == 1) {
                // With tesselation mode, only triangles in the preparedHullOfPoints of constraints polygons are kept
                triangles.add(new Triangle(vertIndex.get(t.getVertexA()), vertIndex.get(t.getVertexB()),vertIndex.get(t.getVertexC()), triangleAttribute));
            }
        }
    }

    /**
     * Check if the triangle is degenerated. Some triangle can have an angle equals to 0°
     * @param t triangle
     * @return true if the triangle is flat (its 3 vertices are collinear up to rounding errors)
     */
    private static boolean isDegenerate(SimpleTriangle t) {
        Coordinate a = new Coordinate(t.getVertexA().getX(), t.getVertexA().getY());
        Coordinate b = new Coordinate(t.getVertexB().getX(), t.getVertexB().getY());
        Coordinate c = new Coordinate(t.getVertexC().getX(), t.getVertexC().getY());
        double longestSide = org.locationtech.jts.geom.Triangle.longestSideLength(a, b, c);
        return org.locationtech.jts.geom.Triangle.area(a, b, c) <= 0.5e-10 * longestSide * longestSide;
    }

    /**
     * @param meshPoints vertices of the triangulation
     * @return the convex hull of the vertices
     */
    private static Geometry convexHull(List<Vertex> meshPoints) {
        Coordinate[] coordinates = new Coordinate[meshPoints.size()];
        for (int i = 0; i < coordinates.length; i++) {
            Vertex v = meshPoints.get(i);
            coordinates[i] = new Coordinate(v.getX(), v.getY());
        }
        return new ConvexHull(coordinates, new GeometryFactory()).getConvexHull();
    }

    /**
     * @param convexHullPoints prepared convex hull of the input points
     * @param t triangle
     * @return true if the centroid of the triangle is inside the domain
     */
    private boolean isInside(PreparedGeometry convexHullPoints, SimpleTriangle t) {
        Vertex a = t.getVertexA(), b = t.getVertexB(), c = t.getVertexC();
        Coordinate centroid = new Coordinate((a.getX() + b.getX() + c.getX()) / 3,
                (a.getY() + b.getY() + c.getY()) / 3);
        return convexHullPoints.covers(convexHullPoints.getGeometry().getFactory().createPoint(centroid));
    }

    /**
     * Build a polygon constraint from the convex hull, reusing the existing vertex instances.
     *
     * @param hull convex hull of the vertices
     * @param meshPoints vertices of the triangulation
     * @return the convex hull constraint or null if it is not valid
     */
    private static PolygonConstraint convexHullConstraint(Polygon hull, List<Vertex> meshPoints) {
        Map<Coordinate, Vertex> vertexByCoordinate = new HashMap<>();
        for (Vertex v : meshPoints) {
            vertexByCoordinate.put(new Coordinate(v.getX(), v.getY()), v);
        }
        Coordinate[] ring = hull.getExteriorRing().getCoordinates().clone();
        if (!Orientation.isCCW(ring)) {
            CoordinateArrays.reverse(ring);
        }
        List<Vertex> hullVertices = new ArrayList<>(ring.length - 1);
        for (int i = 0; i < ring.length - 1; i++) {
            Vertex v = vertexByCoordinate.get(ring[i]);
            if (v == null) {
                return null;
            }
            hullVertices.add(v);
        }
        PolygonConstraint polygonConstraint = new PolygonConstraint(hullVertices);
        polygonConstraint.complete();
        return polygonConstraint.isValid() ? polygonConstraint : null;
    }

    /**
     * Apply the Ruppert Delaunay refinement on the triangulation.
     *
     * @param tin triangulation to refine in place
     * @return for 3D inputs, a spatial index of the unrefined triangles
     * used to compute the Z of the Steiner points, null otherwise
     */
    private STRtree refine(IncrementalTin tin) {
        //Use it to perform triangles search
        STRtree originalTriangles = null;
        if (!isInput2D) {
            originalTriangles = new STRtree();
            for (SimpleTriangle t : computeTriangles(tin)) {
                org.locationtech.jts.geom.Triangle tri = new org.locationtech.jts.geom.Triangle(
                        toCoordinate(t.getVertexA(), false), toCoordinate(t.getVertexB(), false),
                        toCoordinate(t.getVertexC(), false));
                Envelope env = new Envelope(tri.p0, tri.p1);
                env.expandToInclude(tri.p2);
                originalTriangles.insert(env, tri);
            }
            originalTriangles.build();
        }
        double areaThreshold = minTriangleArea;
        if (areaThreshold == MIN_TRIANGLE_AREA) {
            double shortestEdge = Double.POSITIVE_INFINITY;
            for (IQuadEdge edge : tin.edges()) {
                if (edge.getB() != null) {
                    shortestEdge = Math.min(shortestEdge, edge.getLength());
                }
            }
            areaThreshold = Double.isFinite(shortestEdge) ? MIN_AREA_FACTOR * shortestEdge * shortestEdge : 0;
        }
        try {
            //Here the rupper algorithm to refine the triangulation
            RuppertRefiner refiner = new RuppertRefiner(tin, minAngle, areaThreshold);
            if (!refiner.refine()) {
                LOGGER.warn("The Delaunay refinement stopped before reaching the minimum angle of {} degrees", minAngle);
            }
        } catch (RuntimeException ex) {
            LOGGER.warn("The Delaunay refinement has been stopped: {}", ex.getMessage());
        }
        return originalTriangles;
    }

    /**
     * Compute the Z value of a location on the plane of the original triangle that contains it.
     * If no triangle contains the location, the closest triangle is used.
     *
     * @param originalTriangles index of the unrefined triangles
     * @param x x coordinate
     * @param y y coordinate
     * @return the interpolated Z, or NaN if no triangle is found
     */
    static double interpolateZ(STRtree originalTriangles, double x, double y) {
        Coordinate p = new Coordinate(x, y);
        org.locationtech.jts.geom.Triangle closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Object item : originalTriangles.query(new Envelope(p))) {
            org.locationtech.jts.geom.Triangle tri = (org.locationtech.jts.geom.Triangle) item;
            if (tri.area() == 0) {
                continue;
            }
            if (org.locationtech.jts.geom.Triangle.intersects(tri.p0, tri.p1, tri.p2, p)) {
                return tri.interpolateZ(p);
            }
            double distance = Math.min(Distance.pointToSegment(p, tri.p0, tri.p1),
                    Math.min(Distance.pointToSegment(p, tri.p1, tri.p2), Distance.pointToSegment(p, tri.p2, tri.p0)));
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = tri;
            }
        }
        return closest == null ? Double.NaN : closest.interpolateZ(p);
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