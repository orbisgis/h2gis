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
    // Bound of Voronoy, may be null
    private Geometry envelope;
    // In order to compute triangle neighbors we have to set a unique id to points.
    private Quadtree ptQuad = new Quadtree();
    private List<EnvelopeWithIndex> triVertex;
    private double maxDist = 1e-12;
    private Triple[] triangleNeighbors = new Triple[0];

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
     * @return Graph of triangles
     */
    public Triple[] getTriangleNeighbors() {
        return triangleNeighbors;
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

    public Geometry computeVoronoy(Geometry geometry) throws TopologyException {
        GeometryFactory geometryFactory = geometry.getFactory();
        ptQuad = new Quadtree();
        // In order to compute triangle neighbors we have to set a unique id to points.
        Triple[] triangleVertex = new Triple[geometry.getNumGeometries()];
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
        Triple[] neighbors = new Triple[geometry.getNumGeometries()];
        // Second loop make an index of triangle neighbors
        ptQuad = null;
        triangleNeighbors = new Triple[geometry.getNumGeometries()];
        for(int triId = 0; triId< triangleVertex.length; triId++) {
            Triple triangleIndex = triangleVertex[triId];
            triangleNeighbors[triId] = new Triple(commonEdge(triId,triVertex.get(triangleIndex.a), triVertex.get(triangleIndex.b)),
                    commonEdge(triId,triVertex.get(triangleIndex.b), triVertex.get(triangleIndex.c)),
                    commonEdge(triId,triVertex.get(triangleIndex.c), triVertex.get(triangleIndex.a)));
        }
        triVertex.clear();
        List<Coordinate> trianglesCircumcentre = new ArrayList<Coordinate>(geometry.getNumGeometries());
        return geometry;
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
