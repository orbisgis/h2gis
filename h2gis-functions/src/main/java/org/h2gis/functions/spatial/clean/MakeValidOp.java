/*
 * This program extends Java Topology Suite (JTS) capability and is made
 * available to any Software already using Java Topology Suite.
 *
 * Copyright (C) Michaël Michaud (2017)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.functions.spatial.clean;

import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.*;

import static org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.PointExtracter;

/**
 * Operator to make a geometry valid.
 * <br/>
 * Making a geometry valid will remove duplicate points although duplicate points
 * do not make a geometry invalid.
 *
 * @author Michaël Michaud
 */
public class MakeValidOp {

    private static final Coordinate[] EMPTY_COORD_ARRAY = new Coordinate[0];
    private static final LinearRing[] EMPTY_RING_ARRAY = new LinearRing[0];

    // If preserveGeomDim is true, the geometry dimension returned by MakeValidOp
    // must be the same as the inputGeometryType (degenerate components of lower
    // dimension are removed).
    // If preserveGeomDim is false MakeValidOp will preserve as much coordinates
    // as possible and may return a geometry of lower dimension or a
    // GeometryCollection if input geometry or geometry components have not the
    // required number of points.
    private boolean preserveGeomDim = true;

    // If preserveCoordDim is true, MakeValidOp preserves third and fourth ordinates.
    // If preserveCoordDim is false, third dimension is preserved but not fourth one.
    private boolean preserveCoordDim = true;

    // If preserveDuplicateCoord is true, MakeValidOp will preserve duplicate
    // coordinates as much as possible. Generally, duplicate coordinates can be
    // preserved for linear geometries but not for areal geometries (overlay
    // operations used to repair polygons remove duplicate points).
    // If preserveDuplicateCoord is false, all duplicated coordinates are removed.
    private boolean preserveDuplicateCoord = true;

    public MakeValidOp() {
    }

    public MakeValidOp setPreserveGeomDim(boolean preserveGeomDim) {
        this.preserveGeomDim = preserveGeomDim;
        return this;
    }

    public MakeValidOp setPreserveCoordDim(boolean preserveCoordDim) {
        this.preserveCoordDim = preserveCoordDim;
        return this;
    }

    public MakeValidOp setPreserveDuplicateCoord(boolean preserveDuplicateCoord) {
        this.preserveDuplicateCoord = preserveDuplicateCoord;
        return this;
    }

    /**
     * Decompose a geometry recursively into simple components.
     *
     * @param geometry input geometry
     * @param list a list of simple components (Point, LineString or Polygon)
     */
    private static void decompose(Geometry geometry, Collection<Geometry> list) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry component = geometry.getGeometryN(i);
            if (component instanceof GeometryCollection) {
                decompose(component, list);
            } else {
                list.add(component);
            }
        }
    }

    /**
     * Repair an invalid geometry.
     * <br/>
     * If preserveGeomDim is true, makeValid will remove degenerated geometries
     * from the result, i.e geometries which dimension is lower than the input
     * geometry dimension (except for mixed GeometryCollection).
     * <br/>
     * A multi-geometry will always produce a multi-geometry (eventually empty
     * or made of a single component). A simple geometry may produce a
     * multi-geometry (ex. polygon with self-intersection will generally produce
     * a multi-polygon). In this case, it is up to the client to explode
     * multi-geometries if he needs to.
     * <br/>
     * If preserveGeomDim is off, it is up to the client to filter degenerate
     * geometries.
     * <br/>
     * WARNING : for geometries of dimension 1 (linear), duplicate coordinates
     * are preserved as much as possible. For geometries of dimension 2 (areal),
     * duplicate coordinates are generally removed due to the use of overlay
     * operations.
     *
     * @param geometry input geometry
     * @return a valid Geometry
     */
    public Geometry makeValid(Geometry geometry) {

        // Input geometry is recursively exploded into a list of simple components
        List<Geometry> list = new ArrayList<>(geometry.getNumGeometries());
        decompose(geometry, list);

        // Each single component is made valid
        Collection<Geometry> list2 = new ArrayList<>();
        for (Geometry component : list) {
            if (component instanceof Point) {
                Point p = makePointValid((Point) component);
                if (!p.isEmpty()) {
                    list2.add(p);
                }
            } else if (component instanceof LineString) {
                Geometry geom = makeLineStringValid((LineString) component);
                for (int i = 0; i < geom.getNumGeometries(); i++) {
                    if (!geom.getGeometryN(i).isEmpty()) {
                        list2.add(geom.getGeometryN(i));
                    }
                }
            } else if (component instanceof Polygon) {
                Geometry geom = makePolygonValid((Polygon) component);
                for (int i = 0; i < geom.getNumGeometries(); i++) {
                    if (!geom.getGeometryN(i).isEmpty()) {
                        list2.add(geom.getGeometryN(i));
                    }
                }
            } else {
                assert false : "Should never reach here";
            }
        }

        list.clear();
        for (Geometry g : list2) {
            // If preserveGeomDim is true and original input geometry is not a GeometryCollection
            // components with a lower dimension than input geometry are removed
            if (preserveGeomDim && !geometry.getClass().getSimpleName().equals("GeometryCollection")) {
                removeLowerDimension(g, list, geometry.getDimension());
            } else {
                decompose(g, list);
            }
        }
        list2 = list;

        // In a MultiPolygon, polygons cannot touch or overlap each other
        // (adjacent polygons are not merged in the context of a mixed GeometryCollection)
        if (list2.size() > 1) {
            boolean multiPolygon = true;
            for (Geometry geom : list2) {
                if (geom.getDimension() < 2) {
                    multiPolygon = false;
                }
            }
            if (multiPolygon) {
                list2 = unionAdjacentPolygons(list2);
            }
        }
        if (list2.isEmpty()) {
            GeometryFactory factory = geometry.getFactory();
            if (geometry instanceof Point) {
                return factory.createPoint((Coordinate) null);
            } else if (geometry instanceof LinearRing) {
                return factory.createLinearRing(EMPTY_COORD_ARRAY);
            } else if (geometry instanceof LineString) {
                return factory.createLineString(EMPTY_COORD_ARRAY);
            } else if (geometry instanceof Polygon) {
                return factory.createPolygon(factory.createLinearRing(EMPTY_COORD_ARRAY), EMPTY_RING_ARRAY);
            } else if (geometry instanceof MultiPoint) {
                return factory.createMultiPoint(new Point[0]);
            } else if (geometry instanceof MultiLineString) {
                return factory.createMultiLineString(new LineString[0]);
            } else if (geometry instanceof MultiPolygon) {
                return factory.createMultiPolygon(new Polygon[0]);
            } else {
                return factory.createGeometryCollection(new Geometry[0]);
            }
        } else {
            CoordinateSequenceFactory csFactory = geometry.getFactory().getCoordinateSequenceFactory();
            // Preserve 4th coordinate dimension as much as possible if preserveCoordDim is true
            if (preserveCoordDim && csFactory instanceof PackedCoordinateSequenceFactory
                    && ((PackedCoordinateSequenceFactory) csFactory).getDimension() == 4) {
                Map<Coordinate, Double> map = new HashMap<>();
                gatherDim4(geometry, map);
                list2 = restoreDim4(list2, map);
            }

            Geometry result = geometry.getFactory().buildGeometry(list2);
            // If input geometry was a GeometryCollection and result is a simple geometry
            // create a multi-geometry made of a single component
            if (geometry instanceof GeometryCollection && !(result instanceof GeometryCollection)) {
                if (geometry instanceof MultiPoint && result instanceof Point) {
                    result = geometry.getFactory().createMultiPoint(new Point[]{(Point) result});
                } else if (geometry instanceof MultiLineString && result instanceof LineString) {
                    result = geometry.getFactory().createMultiLineString(new LineString[]{(LineString) result});
                } else if (geometry instanceof MultiPolygon && result instanceof Polygon) {
                    result = geometry.getFactory().createMultiPolygon(new Polygon[]{(Polygon) result});
                }
            }
            return result;
        }
    }

    // Reursively remove geometries with a dimension less than dimension parameter
    private void removeLowerDimension(Geometry geometry, List<Geometry> result, int dimension) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry g = geometry.getGeometryN(i);
            if (g instanceof GeometryCollection) {
                removeLowerDimension(g, result, dimension);
            } else if (g.getDimension() >= dimension) {
                result.add(g);
            }
        }
    }

    // Union adjacent polygons to make an invalid MultiPolygon valid
    private Collection<Geometry> unionAdjacentPolygons(Collection<Geometry> list) {
        UnaryUnionOp op = new UnaryUnionOp(list);
        Geometry result = op.union();
        if (result.getNumGeometries() < list.size()) {
            list.clear();
            for (int i = 0; i < result.getNumGeometries(); i++) {
                list.add(result.getGeometryN(i));
            }
        }
        return list;
    }

    // If X or Y is null, return an empty Point
    private Point makePointValid(Point point) {
        CoordinateSequence sequence = point.getCoordinateSequence();
        GeometryFactory factory = point.getFactory();
        CoordinateSequenceFactory csFactory = factory.getCoordinateSequenceFactory();
        if (sequence.size() == 0) {
            return point;
        } else if (Double.isNaN(sequence.getOrdinate(0, 0)) || Double.isNaN(sequence.getOrdinate(0, 1))) {
            return factory.createPoint(csFactory.create(0, sequence.getDimension()));
        } else if (sequence.size() == 1) {
            return point;
        } else {
            throw new RuntimeException("JTS cannot create a point from a CoordinateSequence containing several points");
        }
    }

    /**
     * Returns a coordinateSequence free of Coordinates with X or Y NaN value,
     * and if desired, free of duplicated coordinates. makeSequenceValid keeps
     * the original dimension of input sequence.
     *
     * @param sequence input sequence of coordinates
     * @param preserveDuplicateCoord if duplicate coordinates must be preserved
     * @param close if the sequence must be closed
     * @return a new CoordinateSequence with valid XY values
     */
    private static CoordinateSequence makeSequenceValid(CoordinateSequence sequence,
            boolean preserveDuplicateCoord, boolean close) {
        int dim = sequence.getDimension();
        // we add 1 to the sequence size for the case where we have to close the linear ring
        double[] array = new double[(sequence.size() + 1) * sequence.getDimension()];
        boolean modified = false;
        int count = 0;
        // Iterate through coordinates, skip points with x=NaN, y=NaN or duplicate
        for (int i = 0; i < sequence.size(); i++) {
            if (Double.isNaN(sequence.getOrdinate(i, 0)) || Double.isNaN(sequence.getOrdinate(i, 1))) {
                modified = true;
                continue;
            }
            if (!preserveDuplicateCoord && count > 0 && sequence.getCoordinate(i).equals(sequence.getCoordinate(i - 1))) {
                modified = true;
                continue;
            }
            for (int j = 0; j < dim; j++) {
                array[count * dim + j] = sequence.getOrdinate(i, j);
                if (j == dim - 1) {
                    count++;
                }
            }
        }
        // Close the sequence if it is not closed and there is already 3 distinct coordinates
        if (close && count > 2 && (array[0] != array[(count - 1) * dim] || array[1] != array[(count - 1) * dim + 1])) {
            System.arraycopy(array, 0, array, count * dim, dim);
            modified = true;
            count++;
        }
        // Close z, m dimension if needed
        if (close && count > 3 && dim > 2) {
            for (int d = 2; d < dim; d++) {
                if (array[(count - 1) * dim + d] != array[d]) {
                    modified = true;
                }
                array[(count - 1) * dim + d] = array[d];
            }
        }
        if (modified) {
            double[] shrinkedArray = new double[count * dim];
            System.arraycopy(array, 0, shrinkedArray, 0, count * dim);
            return PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(shrinkedArray, dim);
        } else {
            return sequence;
        }
    }

    /**
     * Returns
     * <ul>
     * <li>an empty LineString if input CoordinateSequence has no valid
     * point</li>
     * <li>a Point if input CoordinateSequence has a single valid Point</li>
     * </ul>
     * makeLineStringValid keeps the original dimension of input sequence.
     *
     * @param lineString the LineString to make valid
     * @return a valid LineString or a Point if lineString length equals 0
     */
    private Geometry makeLineStringValid(LineString lineString) {
        CoordinateSequence sequence = lineString.getCoordinateSequence();
        CoordinateSequence sequenceWithoutDuplicates = makeSequenceValid(sequence, false, false);
        GeometryFactory factory = lineString.getFactory();
        if (sequenceWithoutDuplicates.size() == 0) {
            // no valid point -> empty LineString
            return factory.createLineString(factory.getCoordinateSequenceFactory().create(0, sequence.getDimension()));
        } else if (sequenceWithoutDuplicates.size() == 1) {
            // a single valid point -> returns a Point
            if (preserveGeomDim) {
                return factory.createLineString(factory.getCoordinateSequenceFactory().create(0, sequence.getDimension()));
            } else {
                return factory.createPoint(sequenceWithoutDuplicates);
            }
        } else if (preserveDuplicateCoord) {
            return factory.createLineString(makeSequenceValid(sequence, true, false));
        } else {
            return factory.createLineString(sequenceWithoutDuplicates);
        }
    }

    /**
     * Making a Polygon valid may creates
     * <ul>
     * <li>an Empty Polygon if input has no valid coordinate</li>
     * <li>a Point if input has only one valid coordinate</li>
     * <li>a LineString if input has only a valid segment</li>
     * <li>a Polygon in most cases</li>
     * <li>a MultiPolygon if input has a self-intersection</li>
     * <li>a GeometryCollection if input has degenerate parts (ex. degenerate
     * holes)</li>
     * </ul>
     *
     * @param polygon the Polygon to make valid
     * @return a valid Geometry which may be of any type if the source geometry
     * is not valid.
     */
    private Geometry makePolygonValid(Polygon polygon) {
        //This first step analyze linear components and create degenerate geometries
        //of dimension 0 or 1 if they do not form valid LinearRings
        //If degenerate geometries are found, it may produce a GeometryCollection with
        //heterogeneous dimension
        Geometry geom = makePolygonComponentsValid(polygon);
        List<Geometry> list = new ArrayList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry component = geom.getGeometryN(i);
            if (component instanceof Polygon) {
                Geometry nodedPolygon = nodePolygon((Polygon) component);
                for (int j = 0; j < nodedPolygon.getNumGeometries(); j++) {
                    list.add(nodedPolygon.getGeometryN(j));
                }
            } else {
                list.add(component);
            }
        }
        return polygon.getFactory().buildGeometry(list);
    }

    /**
     * The method makes sure that outer and inner rings form valid LinearRings.
     * <p>
     * If outerRing is not a valid LinearRing, every linear component is
     * considered as a degenerated geometry of lower dimension (0 or 1)
     * </p>
     * <p>
     * If outerRing is a valid LinearRing but some innerRings are not, invalid
     * innerRings are transformed into LineString (or Point) and the returned
     * geometry may be a GeometryCollection of heterogeneous dimension.
     * </p>
     *
     * @param polygon simple Polygon to make valid
     * @return a Geometry which may not be a Polygon if the source Polygon is
     * invalid
     */
    private Geometry makePolygonComponentsValid(Polygon polygon) {
        GeometryFactory factory = polygon.getFactory();
        CoordinateSequence outerRingSeq = makeSequenceValid(polygon.getExteriorRing().getCoordinateSequence(), false, true);
        // The validated sequence of the outerRing does not form a valid LinearRing
        // -> build valid 0-dim or 1-dim geometry from all the rings
        if (outerRingSeq.size() == 0 || outerRingSeq.size() < 4) {
            List<Geometry> list = new ArrayList<>();
            if (outerRingSeq.size() > 0) {
                list.add(makeLineStringValid(polygon.getExteriorRing()));
            }
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                Geometry g = makeLineStringValid(polygon.getInteriorRingN(i));
                if (!g.isEmpty()) {
                    list.add(g);
                }
            }
            if (list.isEmpty()) {
                return factory.createPolygon(outerRingSeq);
            } else {
                return factory.buildGeometry(list);
            }
        } // OuterRing forms a valid ring.
        // Inner rings may be degenerated
        else {
            List<LinearRing> innerRings = new ArrayList<>();
            List<Geometry> degeneratedRings = new ArrayList<>();
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                CoordinateSequence seq = makeSequenceValid(polygon.getInteriorRingN(i).getCoordinateSequence(), false, true);
                if (seq.size() > 3) {
                    innerRings.add(factory.createLinearRing(seq));
                } else if (seq.size() > 1) {
                    degeneratedRings.add(factory.createLineString(seq));
                } else if (seq.size() == 1) {
                    degeneratedRings.add(factory.createPoint(seq));
                }
                // seq.size == 0
            }
            Polygon poly = factory.createPolygon(factory.createLinearRing(outerRingSeq),
                    innerRings.toArray(new LinearRing[innerRings.size()]));
            if (degeneratedRings.isEmpty()) {
                return poly;
            } else {
                degeneratedRings.add(0, poly);
                return factory.buildGeometry(degeneratedRings);
            }
        }
    }

    /**
     * Computes a valid Geometry from a Polygon which may not be valid
     * (auto-intersecting ring or overlapping holes).
     * <ul>
     * <li>creates a Geometry from the <em>noded</em> exterior boundary</li>
     * <li>remove Geometries computed from noded interior boundaries</li>
     * </ul>
     */
    private Geometry nodePolygon(Polygon polygon) {
        LinearRing exteriorRing = (LinearRing) polygon.getExteriorRing();
        Geometry geom = getArealGeometryFromLinearRing(exteriorRing);
        // geom can be a GeometryCollection
        // extract polygonal areas because symDifference cannot process GeometryCollections
        List<Geometry> polys = new ArrayList<>();
        List<Geometry> lines = new ArrayList<>();
        List<Geometry> points = new ArrayList<>();
        geom.apply(new PolygonExtracter(polys));
        geom.apply(new LineStringExtracter(lines));
        geom.apply(new PointExtracter(points));
        geom = geom.getFactory().buildGeometry(polys);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LinearRing interiorRing = (LinearRing) polygon.getInteriorRingN(i);
            // extract polygonal areas because symDifference cannot process GeometryCollections
            polys.clear();
            getArealGeometryFromLinearRing(interiorRing).apply(new PolygonExtracter(polys));
            // TODO avoid the use of difference operator
            geom = geom.symDifference(geom.getFactory().buildGeometry(polys));
        }
        List<Geometry> result = new ArrayList<>();
        result.add(geom);
        result.addAll(lines);
        result.addAll(points);
        return geom.getFactory().buildGeometry(result);
    }

    /**
     * Node a LinearRing and return a MultiPolygon containing
     * <ul>
     * <li>a single Polygon if the LinearRing is simple</li>
     * <li>several Polygons if the LinearRing auto-intersects</li>
     * </ul>
     * This is used to repair auto-intersecting Polygons
     */
    private Geometry getArealGeometryFromLinearRing(LinearRing ring) {
        if (ring.isSimple()) {
            return ring.getFactory().createMultiPolygon(new Polygon[]{
                ring.getFactory().createPolygon(ring, EMPTY_RING_ARRAY)
            });
        } else {
            // Node input LinearRing and extract unique segments
            Set<LineString> lines = nodeLineString(ring.getCoordinates(), ring.getFactory());
            lines = getSegments(lines);

            // Polygonize the line network
            Polygonizer polygonizer = new Polygonizer();
            polygonizer.add(lines);

            // Computes intersections to determine the status of each polygon
            Collection<Geometry> geoms = new ArrayList();
            for (Object object : polygonizer.getPolygons()) {
                Polygon polygon = (Polygon) object;
                Coordinate p = polygon.getInteriorPoint().getCoordinate();
                int location = RayCrossingCounter.locatePointInRing(p, ring.getCoordinateSequence());
                if (location == Location.INTERIOR) {
                    geoms.add(polygon);
                }
            }
            Geometry unionPoly = UnaryUnionOp.union(geoms);
            Geometry unionLines = UnaryUnionOp.union(lines).difference(unionPoly.getBoundary());
            geoms.clear();
            decompose(unionPoly, geoms);
            decompose(unionLines, geoms);
            return ring.getFactory().buildGeometry(geoms);
        }
    }

    /**
     * Return a set of segments from a linestring
     *
     * @param lines
     * @return
     */
    private Set<LineString> getSegments(Collection<LineString> lines) {
        Set<LineString> set = new HashSet<>();
        for (LineString line : lines) {
            Coordinate[] cc = line.getCoordinates();
            for (int i = 1; i < cc.length; i++) {
                if (!cc[i - 1].equals(cc[i])) {
                    LineString segment = line.getFactory().createLineString(
                            new Coordinate[]{new Coordinate(cc[i - 1]), new Coordinate(cc[i])});
                    set.add(segment);
                }
            }
        }
        return set;
    }

    // Use ring to restore M values on geoms
    private Collection<Geometry> restoreDim4(Collection<Geometry> geoms, Map<Coordinate, Double> map) {
        GeometryFactory factory = new GeometryFactory(
                new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, 4));
        Collection<Geometry> result = new ArrayList<>();
        for (Geometry geom : geoms) {
            if (geom instanceof Point) {
                result.add(factory.createPoint(restoreDim4(
                        ((Point) geom).getCoordinateSequence(), map)));
            } else if (geom instanceof LineString) {
                result.add(factory.createLineString(restoreDim4(
                        ((LineString) geom).getCoordinateSequence(), map)));
            } else if (geom instanceof Polygon) {
                LinearRing outer = factory.createLinearRing(restoreDim4(
                        ((Polygon) geom).getExteriorRing().getCoordinateSequence(), map));
                LinearRing[] inner = new LinearRing[((Polygon) geom).getNumInteriorRing()];
                for (int i = 0; i < ((Polygon) geom).getNumInteriorRing(); i++) {
                    inner[i] = factory.createLinearRing(restoreDim4(
                            ((Polygon) geom).getInteriorRingN(i).getCoordinateSequence(), map));
                }
                result.add(factory.createPolygon(outer, inner));
            } else {
                for (int i = 0; i < geom.getNumGeometries(); i++) {
                    result.addAll(restoreDim4(Collections.singleton(geom.getGeometryN(i)), map));
                }
            }
        }
        return result;
    }

    private void gatherDim4(Geometry geometry, Map<Coordinate, Double> map) {

        if (geometry instanceof Point) {
            gatherDim4(((Point) geometry).getCoordinateSequence(), map);
        } else if (geometry instanceof LineString) {
            gatherDim4(((LineString) geometry).getCoordinateSequence(), map);
        } else if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            gatherDim4(polygon.getExteriorRing().getCoordinateSequence(), map);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                gatherDim4(polygon.getInteriorRingN(i).getCoordinateSequence(), map);
            }
        } else {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                gatherDim4(geometry.getGeometryN(i), map);
            }
        }
    }

    private void gatherDim4(CoordinateSequence cs, Map<Coordinate, Double> map) {
        if (cs.getDimension() == 4) {
            for (int i = 0; i < cs.size(); i++) {
                map.put(cs.getCoordinate(i), cs.getOrdinate(i, 3));
            }
        }
    }

    // Use map to restore M values on the coordinate array
    private CoordinateSequence restoreDim4(CoordinateSequence cs, Map<Coordinate, Double> map) {
        CoordinateSequence seq = new PackedCoordinateSequenceFactory(DOUBLE, 4).create(cs.size(), 4);
        for (int i = 0; i < cs.size(); i++) {
            seq.setOrdinate(i, 0, cs.getOrdinate(i, 0));
            seq.setOrdinate(i, 1, cs.getOrdinate(i, 1));
            seq.setOrdinate(i, 2, cs.getOrdinate(i, 2));
            Double d = map.get(cs.getCoordinate(i));
            seq.setOrdinate(i, 3, d == null ? Double.NaN : d);
        }
        return seq;
    }

    /**
     * Nodes a LineString and returns a List of Noded LineString's. Used to
     * repare auto-intersecting LineString and Polygons. This method cannot
     * process CoordinateSequence. The noding process is limited to 3d
     * geometries.<br/>
     * Preserves duplicate coordinates.
     *
     * @param coords coordinate array to be noded
     * @param gf geometryFactory to use
     * @return a list of noded LineStrings
     */
    private Set<LineString> nodeLineString(Coordinate[] coords, GeometryFactory gf) {
        MCIndexNoder noder = new MCIndexNoder();
        noder.setSegmentIntersector(new IntersectionAdder(new RobustLineIntersector()));
        List<NodedSegmentString> list = new ArrayList<>();
        list.add(new NodedSegmentString(coords, null));
        noder.computeNodes(list);
        List<LineString> lineStringList = new ArrayList<>();
        for (Object segmentString : noder.getNodedSubstrings()) {
            lineStringList.add(gf.createLineString(
                    ((NodedSegmentString) segmentString).getCoordinates()
            ));
        }

        // WARNING : merger loose original linestrings
        // It is useful for LinearRings but should not be used for (Multi)LineStrings
        LineMerger merger = new LineMerger();
        merger.add(lineStringList);
        lineStringList = (List<LineString>) merger.getMergedLineStrings();

        // Remove duplicate linestrings preserving main orientation
        Set<LineString> lineStringSet = new HashSet<>();
        for (LineString line : lineStringList) {
            // TODO as equals makes a topological comparison, comparison with line.reverse maybe useless
            if (!lineStringSet.contains(line) && !lineStringSet.contains(line.reverse())) {
                lineStringSet.add(line);
            }
        }
        return lineStringSet;
    }

}
