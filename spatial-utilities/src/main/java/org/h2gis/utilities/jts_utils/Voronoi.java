package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import java.util.*;


/**
 * Voronoi algorithms
 * @author Nicolas Fortin
 */
public class Voronoi {
    // Bound of Voronoi, may be null
    private Geometry envelope;
    // In order to compute triangle neighbors we have to set a unique id to points.
    private Quadtree ptQuad = new Quadtree();
    private Geometry inputTriangles;
    private List<EnvelopeWithIndex> triVertex;
    private double maxDist = 1e-12;

    // Triangle graph
    // Neighbor (Side)
    // Vertex (Uppercase)
    //
    //        A
    //        ^
    //       / \
    //      c   b
    //     /     \
    //   B(___a___)C
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
     * @param maxDist When merging triangles vertex, snap tolerance of points
     */
    public Voronoi(double maxDist) {
        this.maxDist = maxDist;
    }

    /**
     * Optional Voronoi envelope.
     * @param envelope LineString or MultiLineString
     */
    public void setEnvelope(Geometry envelope) throws TopologyException {
        if(envelope instanceof LineString || envelope instanceof MultiLineString) {
            this.envelope = envelope;
        } else if(envelope instanceof Polygon) {
            this.envelope = ((Polygon) envelope).getExteriorRing();
        } else if(envelope instanceof MultiPolygon || envelope instanceof GeometryCollection) {
            GeometryFactory geometryFactory = envelope.getFactory();
            LineString[] mls = new LineString[envelope.getNumGeometries()];
            for(int n = 0; n < mls.length; n++) {
                Geometry subGeometry = envelope.getGeometryN(n);
                if(subGeometry instanceof Polygon) {
                    mls[n] = ((Polygon) subGeometry).getExteriorRing();
                }
            }
            this.envelope = geometryFactory.createMultiLineString(mls);
        } else {
            throw new TopologyException("Only (Multi)LineString is accepted as voronoi envelope");
        }
    }

    /**
     * Compute unique index for the coordinate
     * Index count from 0 to n
     * If the new vertex is closer than distMerge with an another vertex then it will return its index.
     * @return The index of the vertex
     */
    private int getOrAppendVertex(Coordinate newCoord) {
        Envelope queryEnv = new Envelope(newCoord);
        queryEnv.expandBy(maxDist);
        QuadTreeVisitor visitor = new QuadTreeVisitor(maxDist, newCoord);
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
                triVertex.get(triangleVertex[idgeom].a).addSharingTriangle(idgeom);
                triVertex.get(triangleVertex[idgeom].b).addSharingTriangle(idgeom);
                triVertex.get(triangleVertex[idgeom].c).addSharingTriangle(idgeom);
            } else {
                throw new TopologyException("Voronoi method accept only polygons");
            }
        }
        // Second loop make an index of triangle neighbors
        ptQuad = null;
        triangleNeighbors = new Triple[geometry.getNumGeometries()];
        for(int triId = 0; triId< triangleVertex.length; triId++) {
            Triple triangleIndex = triangleVertex[triId];
            triangleNeighbors[triId] = new Triple(commonEdge(triId,triVertex.get(triangleIndex.b), triVertex.get(triangleIndex.c)),
                    commonEdge(triId,triVertex.get(triangleIndex.a), triVertex.get(triangleIndex.c)),
                    commonEdge(triId,triVertex.get(triangleIndex.b), triVertex.get(triangleIndex.a)));
        }
        triVertex.clear();
        return triangleNeighbors;
    }

    private Coordinate getCircumcenter(int idgeom, Coordinate[] triangleCircumcenter) {
        Coordinate circumcenter = triangleCircumcenter[idgeom];
        if(circumcenter == null) {
            Coordinate[] coordinates = inputTriangles.getGeometryN(idgeom).getCoordinates();
            circumcenter = new Triangle(coordinates[0], coordinates[1], coordinates[2]).circumcentre();
            triangleCircumcenter[idgeom] = circumcenter;
        }
        return circumcenter;
    }

    private List<Integer> navigateTriangleNeigh(int idTri, int idVertex, int excludeTri) {
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
            if(neigh.contains(idTri)) {
                // Loop is done around the vertex
                return neigh;
            }
            neigh.add(idTri);
        }
        return neigh;
    }

    private LineString generateVoronoiEdge(int idTri, int idVertex, Coordinate[] triangleCircumcenter) {
        // Generate Voronoi path around a vertex using the same path as given by the graph of triangle neighbors
        List<Integer> triangleIndexPath = navigateTriangleNeigh(idTri, idVertex, -1);
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
                List<Integer> otherSidePath = navigateTriangleNeigh(idTri, idVertex, triangleIndexPath.get(1));
                if (!otherSidePath.isEmpty()) {
                    Collections.reverse(otherSidePath);
                    triangleIndexPath.addAll(0, otherSidePath);
                    loop = triangleNeighbors[triangleIndexPath.get(0)].contains(triangleIndexPath.get(triangleIndexPath.size() - 1));
                }
            }
        }
        return new GeometryFactory().createLineString(new Coordinate[0]);
    }

    private Coordinate getVertexCoordinate(int idTri, int idVertex) {
        return inputTriangles.getGeometryN(idTri).getCoordinates()[triangleVertex[idTri].getArrayIndex(idVertex)];
    }

    /**
     * Generate Voronoi using the graph of triangle computed by {@link #generateTriangleNeighbors(com.vividsolutions.jts.geom.Geometry)}
     * @return Collection of LineString (edges of Voronoi)
     */
    public Geometry generateVoronoi(boolean generatePolygon) throws TopologyException {
        GeometryFactory geometryFactory = inputTriangles.getFactory();
        if(triangleNeighbors == null || triangleNeighbors.length == 0) {
            return geometryFactory.createMultiLineString(new LineString[0]);
        }
        Coordinate[] triangleCircumcenter = new Coordinate[inputTriangles.getNumGeometries()];
        List<LineString> lineStrings = new ArrayList<LineString>(triangleCircumcenter.length);
        if(generatePolygon) {
            Set<Integer> processedVertex = new HashSet<Integer>();
            for (int idgeom = 0; idgeom < triangleCircumcenter.length; idgeom++) {
                Geometry geomItem = inputTriangles.getGeometryN(idgeom);
                if (geomItem instanceof Polygon) {
                    Triple neigh = triangleNeighbors[idgeom];
                    if (neigh.getA() != -1 && !processedVertex.contains(neigh.getA())) {
                        // Add voronoi edge between circumcentre of A and current triangle circumcenter
                        lineStrings.add(generateVoronoiEdge(idgeom, triangleVertex[idgeom].getA(), triangleCircumcenter));
                        processedVertex.add(neigh.getA());
                    }
                    if (neigh.getB() != -1 && !processedVertex.contains(neigh.getB())) {
                        // Add voronoi edge between circumcentre of A and current triangle circumcenter
                        lineStrings.add(generateVoronoiEdge(idgeom, triangleVertex[idgeom].getB(), triangleCircumcenter));
                        processedVertex.add(neigh.getB());
                    }
                    if (neigh.getC() != -1 && !processedVertex.contains(neigh.getC())) {
                        // Add voronoi edge between circumcentre of A and current triangle circumcenter
                        lineStrings.add(generateVoronoiEdge(idgeom, triangleVertex[idgeom].getC(), triangleCircumcenter));
                        processedVertex.add(neigh.getC());
                    }
                } else {
                    throw new TopologyException("Voronoi method accept only polygons");
                }
            }
        } else {
            //.. later
        }
        return geometryFactory.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
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
        private final int a;
        private final int b;
        private final int c;

        public Triple() {
            a = -1;
            b = -1;
            c = -1;
        }


        public Triple(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public int getC() {
            return c;
        }

        int getNeighCount() {
            int neigh = 0;
            if(a != -1) {
                neigh++;
            }
            if(b != -1) {
                neigh++;
            }
            if(c != -1) {
                neigh++;
            }
            return neigh;
        }

        int getArrayIndex(int value) {
            if(a == value) {
                return 0;
            } else if(b == value) {
                return 1;
            } else if(c == value) {
                return 2;
            } else {
                return -1;
            }
        }

        @Override
        public String toString() {
            return "Triangle("+a+","+b+","+c+")";
        }

        public boolean contains(int value) {
            return value == a || value == b || value == c;
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
