package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.overlay.snap.GeometrySnapper;
import com.vividsolutions.jts.triangulate.Segment;

import java.util.*;


/**
 * Voronoi algorithms
 * @author Nicolas Fortin
 */
public class Voronoi {
    // Bound of Voronoi, may be null
    private Envelope envelope;
    // In order to compute triangle neighbors we have to set a unique id to points.
    private Quadtree ptQuad = new Quadtree();
    private Geometry inputTriangles;
    private List<EnvelopeWithIndex> triVertex;
    private double epsilon = 1e-12;

    // Triangle graph
    // Neighbor (Side)
    // Vertex (Uppercase)
    //                    A
    //                   /\
    //                  /  \
    //                 /    \
    //                /c     \b
    //               /        \
    //              /          \
    //             /            \
    //            /______a_______\
    //           B                C
    //
    private Triple[] triangleNeighbors = new Triple[0];
    private Triple[] triangleVertex;

    /**
     * Constructor
     */
    public Voronoi() {
    }

    /**
     * Constructor
     * @param epsilon When merging triangles vertex, snap tolerance of points. Snap also when creating output voronoi.
     */
    public Voronoi(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Optional Voronoi envelope.
     * @param envelope LineString or MultiLineString
     */
    public void setEnvelope(Envelope envelope) throws TopologyException {
        this.envelope = envelope;
    }

    /**
     * Compute unique index for the coordinate
     * Index count from 0 to n
     * If the new vertex is closer than distMerge with an another vertex then it will return its index.
     * @return The index of the vertex
     */
    private int getOrAppendVertex(Coordinate newCoord) {
        Envelope queryEnv = new Envelope(newCoord);
        queryEnv.expandBy(epsilon);
        QuadTreeVisitor visitor = new QuadTreeVisitor(epsilon, newCoord);
        try {
            ptQuad.query(queryEnv, visitor);
        } catch (RuntimeException ex) {
            //ignore
        }
        if (visitor.getNearest() != null) {
            return visitor.getNearest().index;
        }
        // Not found then
        // Append to the list and QuadTree
        EnvelopeWithIndex ret = new EnvelopeWithIndex(triVertex.size(), newCoord);
        ptQuad.insert(queryEnv, ret);
        triVertex.add(ret);
        return ret.index;
    }

    /**
     * Given the input TIN, construct a graph of triangle.
     * @param geometry Collection of Polygon with 3 vertex.
     * @return Array of triangle neighbors. Order and count is the same of the input array.
     * @throws TopologyException If incompatible type geometry is given
     */
    public Triple[] generateTriangleNeighbors(Geometry geometry) throws TopologyException {
        inputTriangles = geometry;
        ptQuad = new Quadtree();
        // In order to compute triangle neighbors we have to set a unique id to points.
        triangleVertex = new Triple[geometry.getNumGeometries()];
        // Final size of tri vertex is not known at the moment. Give just an hint
        triVertex = new ArrayList<EnvelopeWithIndex>(triangleVertex.length);
        // First Loop make an index of triangle vertex
        for(int idgeom = 0; idgeom < triangleVertex.length; idgeom++) {
            Geometry geomItem = geometry.getGeometryN(idgeom);
            if(geomItem instanceof Polygon) {
                Coordinate[] coords = geomItem.getCoordinates();
                if(coords.length != 4) {
                    throw new TopologyException("Voronoi method accept only triangles");
                }
                triangleVertex[idgeom] = new Triple(getOrAppendVertex(coords[0]), getOrAppendVertex(coords[1]),
                        getOrAppendVertex(coords[2]));
                for(int triVertexIndex : triangleVertex[idgeom].toArray()) {
                    triVertex.get(triVertexIndex).addSharingTriangle(idgeom);
                }
            } else {
                throw new TopologyException("Voronoi method accept only polygons");
            }
        }
        // Second loop make an index of triangle neighbors
        ptQuad = null;
        triangleNeighbors = new Triple[geometry.getNumGeometries()];
        for(int triId = 0; triId< triangleVertex.length; triId++) {
            Triple triangleIndex = triangleVertex[triId];
            triangleNeighbors[triId] = new Triple(commonEdge(triId,triVertex.get(triangleIndex.getB()), triVertex.get(triangleIndex.getC())),
                    commonEdge(triId,triVertex.get(triangleIndex.getA()), triVertex.get(triangleIndex.getC())),
                    commonEdge(triId,triVertex.get(triangleIndex.getB()), triVertex.get(triangleIndex.getA())));
        }
        triVertex.clear();
        return triangleNeighbors;
    }

    private Coordinate getCircumcenter(int idgeom, Coordinate[] triangleCircumcenter) {
        Coordinate circumcenter = triangleCircumcenter[idgeom];
        if(circumcenter == null) {
            circumcenter = getTriangle(idgeom).circumcentre();
            triangleCircumcenter[idgeom] = circumcenter;
        }
        return circumcenter;
    }

    private List<Integer> navigateTriangleNeigh(int idTri, int idVertex, int excludeTri, Coordinate[] triangleCircumcenter) {
        List<Integer> neigh = new ArrayList<Integer>();
        while (idTri != -1) {
            Triple triNeigh = triangleNeighbors[idTri];
            if (triNeigh.getA() != -1 && triNeigh.getA() != excludeTri && triangleVertex[triNeigh.getA()].contains(idVertex)) {
                excludeTri = idTri;
                idTri = triNeigh.getA();
            } else if (triNeigh.getB() != -1 && triNeigh.getB() != excludeTri && triangleVertex[triNeigh.getB()].contains(idVertex)) {
                excludeTri = idTri;
                idTri = triNeigh.getB();
            } else if (triNeigh.getC() != -1 && triNeigh.getC() != excludeTri && triangleVertex[triNeigh.getC()].contains(idVertex)) {
                excludeTri = idTri;
                idTri = triNeigh.getC();
            } else {
                break;
            }
            if(neigh.contains(idTri) || !doProcessTriangle(idTri, triangleCircumcenter)) {
                // Loop is done around the vertex
                return neigh;
            }
            neigh.add(idTri);
        }
        return neigh;
    }

    private Polygon generateVoronoiPolygon(int idTri, int idVertex, Coordinate[] triangleCircumcenter) {
        GeometryFactory gf = inputTriangles.getFactory();
        // Generate Voronoi path around a vertex using the same path as given by the graph of triangle neighbors
        List<Integer> triangleIndexPath = navigateTriangleNeigh(idTri, idVertex, -1, triangleCircumcenter);
        boolean loop = true;
        if(!triangleIndexPath.contains(idTri)) {
            triangleIndexPath.add(0, idTri);
            // Does the last triangle share the same segment as the first triangle ?
            loop = triangleIndexPath.size() > 2 &&
                    triangleNeighbors[triangleIndexPath.get(0)].contains(triangleIndexPath.get(triangleIndexPath.size() - 1));
            if (!loop && triangleIndexPath.size() > 2) {
                // Complete the chain of triangles in the other side
                // idTri->(+1)->(+2)->(+3)
                // reverse and concatenate to obtain
                // (-3)<-(-2)<-(-1)<-idTri->(+1)->(+2)->(+3)
                List<Integer> otherSidePath = navigateTriangleNeigh(idTri, idVertex, triangleIndexPath.get(1), triangleCircumcenter);
                if (!otherSidePath.isEmpty()) {
                    Collections.reverse(otherSidePath);
                    triangleIndexPath.addAll(0, otherSidePath);
                    loop = triangleNeighbors[triangleIndexPath.get(0)].contains(triangleIndexPath.get(triangleIndexPath.size() - 1));
                }
            }
        }
        if(loop) {
            // Can build voronoi directly
            // Duplicate begin and end to close the loop
            triangleIndexPath.add(triangleIndexPath.get(0));
            List<Coordinate> polygonVertex = new ArrayList<Coordinate>(triangleIndexPath.size());
            Coordinate lastCoord = null;
            for (Integer aTriangleIndexPath : triangleIndexPath) {
                Coordinate circumCenter = getCircumcenter(aTriangleIndexPath, triangleCircumcenter);
                if(lastCoord == null || lastCoord.distance(circumCenter) > epsilon) {
                    polygonVertex.add(circumCenter);
                    lastCoord = circumCenter;
                }
            }
            return gf.createPolygon(polygonVertex.toArray(new Coordinate[polygonVertex.size()]));
        } else {
            // Must complete with boundary
            if(envelope == null) {
                return null;
            } else {
                return null;
            }
        }
    }

    private Coordinate getVertexCoordinate(int idTri, int idVertex) {
        return inputTriangles.getGeometryN(idTri).getCoordinates()[triangleVertex[idTri].getArrayIndex(idVertex)];
    }

    private boolean isCCW(int idTri) {
        return CGAlgorithms.isCCW(inputTriangles.getGeometryN(idTri).getCoordinates());
    }

    private Triangle getTriangle(int idTri) {
        Coordinate[] coordinates = inputTriangles.getGeometryN(idTri).getCoordinates();
        return new Triangle(coordinates[0], coordinates[1], coordinates[2]);
    }
    private LineSegment getTriangleSegment(int idTri, int idSegment) {
        Coordinate[] coordinates = inputTriangles.getGeometryN(idTri).getCoordinates();
        int a,b;
        switch (idSegment) {
            case 0:
                a = 1;
                b = 2;
                break;
            case 1:
                a = 2;
                b = 0;
                break;
            default:
                a = 0;
                b = 1;
        }
        return new LineSegment(coordinates[a], coordinates[b]);
    }

    private LineString voronoiSide(int idgeom, int side,GeometryFactory geometryFactory, Coordinate circumcenter) {
        boolean triangleCCW = isCCW(idgeom);
        // Create linestring to envelope
        LineSegment sideGeom = getTriangleSegment(idgeom, side);
        Vector2D direction = new Vector2D(sideGeom.p0, sideGeom.p1);
        direction = direction.normalize().rotate(triangleCCW ? - Math.PI / 2 : Math.PI / 2).multiply(envelope.maxExtent());
        LineSegment voronoiLine = new LineSegment(circumcenter, new Coordinate(direction.getX() +
                circumcenter.x, direction.getY() + circumcenter.y));
        Geometry lineString = voronoiLine.toGeometry(geometryFactory).intersection(geometryFactory.toGeometry(envelope));
        if(lineString instanceof LineString && lineString.getLength() > epsilon) {
            return (LineString)lineString;
        } else {
            return null;
        }
    }

    private boolean doProcessTriangle(int idGeom, Coordinate[] triangleCircumcenter) {
        return envelope == null || envelope.contains(getCircumcenter(idGeom, triangleCircumcenter));
    }

    /**
     * Generate Voronoi using the graph of triangle computed by {@link #generateTriangleNeighbors(com.vividsolutions.jts.geom.Geometry)}
     * @return Collection of LineString (edges of Voronoi)
     */
    public GeometryCollection generateVoronoi(int outputDimension) throws TopologyException {
        GeometryFactory geometryFactory = inputTriangles.getFactory();
        if(triangleNeighbors == null || triangleNeighbors.length == 0) {
            return geometryFactory.createMultiLineString(new LineString[0]);
        }
        Coordinate[] triangleCircumcenter = new Coordinate[inputTriangles.getNumGeometries()];
        if(outputDimension == 2) {
            List<Polygon> polygons = new ArrayList<Polygon>(triangleCircumcenter.length);
            Set<Integer> processedVertex = new HashSet<Integer>();
            for (int idgeom = 0; idgeom < triangleCircumcenter.length; idgeom++) {
                    Geometry geomItem = inputTriangles.getGeometryN(idgeom);
                    if (geomItem instanceof Polygon) {
                        if(doProcessTriangle(idgeom, triangleCircumcenter)) {
                            Triple neigh = triangleNeighbors[idgeom];
                            for (int sideNeigh = 0; sideNeigh < 3; sideNeigh++) {
                                int neighIndex = neigh.get(sideNeigh);
                                for (int vertexSide = 0; vertexSide < 3; vertexSide++) {
                                    // If vertex is shared by this neighbor (see ascii art of triangle)
                                    if (vertexSide != sideNeigh) {
                                        int vertexIndex = triangleVertex[idgeom].get(vertexSide);
                                        if (neighIndex != -1 && !processedVertex.contains(vertexIndex)) {
                                            // Add voronoi edge between circumcentre of A and current triangle circumcenter
                                            Polygon result = generateVoronoiPolygon(idgeom, vertexIndex, triangleCircumcenter);
                                            if (result != null) {
                                                polygons.add(result);
                                            }
                                            processedVertex.add(vertexIndex);
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    throw new TopologyException("Voronoi method accept only polygons");
                }
            }
            MultiPolygon result = geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
            if(envelope == null) {
                return result;
            } else {
                return (GeometryCollection)geometryFactory.toGeometry(envelope).intersection(result);
            }
        } else if(outputDimension == 1) {
            //.. later
            List<LineString> lineStrings = new ArrayList<LineString>(triangleCircumcenter.length);
            List<Point> envelopeIntersectionPoints = new ArrayList<Point>();
            for (int idgeom = 0; idgeom < triangleCircumcenter.length; idgeom++) {
                Geometry geomItem = inputTriangles.getGeometryN(idgeom);
                if (geomItem instanceof Polygon) {
                    if(doProcessTriangle(idgeom, triangleCircumcenter))  {
                        Triple neigh = triangleNeighbors[idgeom];
                        for(int side = 0;side < 3; side ++) {
                            int neighIndex = neigh.get(side);
                            if(neighIndex >= 0 && !doProcessTriangle(neighIndex, triangleCircumcenter)) {
                                neighIndex = -1;
                            }
                            // If segment not already processed
                            if (neighIndex > idgeom) {
                                LineString lineString = geometryFactory.createLineString(new Coordinate[]{getCircumcenter(idgeom,
                                        triangleCircumcenter), getCircumcenter(neighIndex, triangleCircumcenter)});
                                if(lineString.getLength() > epsilon) {
                                    lineStrings.add(lineString);
                                }
                            } else if(neighIndex == -1 && envelope != null) {
                                LineString lineString = voronoiSide(idgeom, side, geometryFactory,
                                        getCircumcenter(idgeom, triangleCircumcenter));
                                if(lineString != null) {
                                    lineStrings.add(lineString);
                                    // Add intersection point on border
                                    envelopeIntersectionPoints.add((Point)((Polygon)geometryFactory.toGeometry(envelope))
                                            .getExteriorRing().intersection(lineString));
                                }
                            }
                        }
                    }
                } else {
                    throw new TopologyException("Voronoi method accept only polygons");
                }
            }
            // Generate envelope segments
            if(envelope != null) {
                for(Coordinate coordinate : geometryFactory.toGeometry(envelope).getCoordinates()) {
                    envelopeIntersectionPoints.add(geometryFactory.createPoint(coordinate));
                }
                lineStrings.add(((Polygon)geometryFactory.toGeometry(envelope).convexHull()).getExteriorRing());
            }
            MultiLineString result = geometryFactory.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
            if(envelope == null) {
                return result;
            } else {
                return (GeometryCollection)geometryFactory.toGeometry(envelope).intersection(result);
            }
        } else {
            Coordinate[] circumcenters = new Coordinate[inputTriangles.getNumGeometries()];
            for (int idgeom = 0; idgeom < triangleCircumcenter.length; idgeom++) {
                Geometry geomItem = inputTriangles.getGeometryN(idgeom);
                if (geomItem instanceof Polygon) {
                    circumcenters[idgeom] = getCircumcenter(idgeom, triangleCircumcenter);
                }
            }
            MultiPoint result = geometryFactory.createMultiPoint(circumcenters);
            if(envelope == null) {
                return result;
            } else {
                return (GeometryCollection)geometryFactory.toGeometry(envelope).intersection(result);
            }
        }
    }

    private int commonEdge(int originTriangle, EnvelopeWithIndex vert1, EnvelopeWithIndex vert2) {
        Set<Integer> commonTriangle = new HashSet<Integer>(vert1.getSharingTriangles());
        commonTriangle.retainAll(vert2.getSharingTriangles());
        commonTriangle.remove(originTriangle);
        if(commonTriangle.isEmpty()) {
            return -1;
        } else {
            return commonTriangle.iterator().next();
        }
    }

    /** Triangle vertex and neighbors information.*/
    public static class Triple {
        private final int values[] = new int[3];

        public Triple() {
            values[0] = -1;
            values[1] = -1;
            values[2] = -1;
        }


        public Triple(int a, int b, int c) {
            values[0] = a;
            values[1] = b;
            values[2] = c;
        }

        public int getA() {
            return values[0];
        }

        public int getB() {
            return values[1];
        }

        public int getC() {
            return values[2];
        }

        int getNeighCount() {
            int neigh = 0;
            for(int value : values) {
                if(value != -1) {
                    neigh++;
                }
            }
            return neigh;
        }

        int getArrayIndex(int value) {
            for(int val : values) {
                if(val == value) {
                    return value;
                }
            }
            return -1;
        }

        public int[] toArray() {
            return values;
        }

        public int get(int i) {
            return values[i];
        }

        @Override
        public String toString() {
            return "Triangle("+values[0]+","+values[1]+","+values[2]+")";
        }

        public boolean contains(int value) {
            return getArrayIndex(value) != -1;
        }
    }

    private static class EnvelopeWithIndex {
        private int index;
        private Coordinate position;
        private Set<Integer> sharingTriangles = new HashSet<Integer>();

        public EnvelopeWithIndex(int index, Coordinate position) {
            this.index = index;
            this.position = position;
        }

        public void addSharingTriangle(int triangleId) {
            sharingTriangles.add(triangleId);
        }

        public Set<Integer> getSharingTriangles() {
            return Collections.unmodifiableSet(sharingTriangles);
        }
    }
    private static class QuadTreeVisitor implements ItemVisitor {
        private EnvelopeWithIndex nearest = null;
        private double nearestDistance;
        private final double maxDist;
        private final Coordinate goal;

        public QuadTreeVisitor(double maxDist, Coordinate goal) {
            this.maxDist = maxDist;
            this.goal = goal;
        }

        public EnvelopeWithIndex getNearest() {
            return nearest;
        }

        @Override
        public void visitItem(Object item) {
            EnvelopeWithIndex idx = (EnvelopeWithIndex)item;
            if(goal == idx.position) {
                nearest = idx;
                throw new RuntimeException("Found..");
            } else {
                double itemDistance = idx.position.distance(goal);
                if(itemDistance < maxDist && (nearest == null || nearestDistance > itemDistance)) {
                    nearest = idx;
                    nearestDistance = itemDistance;
                }
            }
        }
    }
}
