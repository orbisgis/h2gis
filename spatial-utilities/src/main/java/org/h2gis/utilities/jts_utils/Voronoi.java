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

    private Polygon generateVoronoiPolygon(int idTri, int idVertex, Coordinate[] triangleCircumcenter) {
        GeometryFactory gf = inputTriangles.getFactory();
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
        if(loop) {
            // Can build voronoi directly
            // Duplicate begin and end to close the loop
            triangleIndexPath.add(triangleIndexPath.get(0));
            Coordinate[] polygonVertex = new Coordinate[triangleIndexPath.size()];
            for(int pathIndex = 0; pathIndex < triangleIndexPath.size(); pathIndex ++) {
                polygonVertex[pathIndex] = getCircumcenter(triangleIndexPath.get(pathIndex), triangleCircumcenter);
            }
            return gf.createPolygon(polygonVertex);
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
                    Triple neigh = triangleNeighbors[idgeom];
                    for(int side = 0;side < 3; side ++) {
                        int neighIndex = neigh.get(side);
                        if (neighIndex != -1 && !processedVertex.contains(neighIndex)) {
                            // Add voronoi edge between circumcentre of A and current triangle circumcenter
                            Polygon result = generateVoronoiPolygon(idgeom, triangleVertex[idgeom].get(side), triangleCircumcenter);
                            if(result != null) {
                                polygons.add(result);
                            }
                            processedVertex.add(neighIndex);
                        }
                    }
                } else {
                    throw new TopologyException("Voronoi method accept only polygons");
                }
            }
            return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        } else if(outputDimension == 1) {
            //.. later
            List<LineString> lineStrings = new ArrayList<LineString>(triangleCircumcenter.length);
            for (int idgeom = 0; idgeom < triangleCircumcenter.length; idgeom++) {
                Geometry geomItem = inputTriangles.getGeometryN(idgeom);
                if (geomItem instanceof Polygon) {
                    Triple neigh = triangleNeighbors[idgeom];
                    for(int side = 0;side < 3; side ++) {
                        int neighIndex = neigh.get(side);
                        // If segment not already processed
                        if (neighIndex > idgeom) {
                            lineStrings.add(geometryFactory.createLineString(new Coordinate[]{getCircumcenter(idgeom,
                                    triangleCircumcenter), getCircumcenter(neighIndex, triangleCircumcenter)}));
                        } else if(neighIndex == -1 && envelope != null) {
                            //TODO create intersection linestring
                        }
                    }
                }
            }
            return geometryFactory.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
        } else {
            Coordinate[] circumcenters = new Coordinate[inputTriangles.getNumGeometries()];
            for (int idgeom = 0; idgeom < triangleCircumcenter.length; idgeom++) {
                Geometry geomItem = inputTriangles.getGeometryN(idgeom);
                if (geomItem instanceof Polygon) {
                    circumcenters[idgeom] = getCircumcenter(idgeom, triangleCircumcenter);
                }
            }
            return geometryFactory.createMultiPoint(circumcenters);
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
