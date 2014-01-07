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

import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.noding.IntersectionAdder;
import com.vividsolutions.jts.noding.MCIndexNoder;
import com.vividsolutions.jts.noding.NodedSegmentString;
import com.vividsolutions.jts.noding.Noder;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.distance.GeometryLocation;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

/**
 * This utility class contains editing methods for JTS {@link Geometry} objects.
 *
 * Geometry objects are unmodifiable; this class allows you to "modify" a
 * Geometry in a sense -- the modified Geometry is returned as a new Geometry.
 * The new method
 * <code>Geometry.isValid()</code> of the returned objects should be checked.
 *
 * @author Erwan bocher
 */
public final class GeometryEdit extends GeometryUtils {

    public static final double PRECISION = 10E-6;

    /**
     * Interpolate a linestring according start and end coordinates z value. If
     * the z is NaN return the input linestring
     *
     * @param lineString
     * @param startz
     * @param endz
     * @return linestring
     */
    public static LineString linearZInterpolation(LineString lineString, double startz, double endz) {
        if (Double.isNaN(startz) || Double.isNaN(endz)) {
            return lineString;
        } else {
            double length = lineString.getLength();
            lineString.apply(new LinearZInterpolationFilter(startz, endz, length));
            return lineString;
        }
    }

    /**
     * Interpolate a linestring according start and end coordinates z value. If
     * the z is NaN return the input linestring
     *
     * @param lineString
     * @return
     */
    public static LineString linearZInterpolation(LineString lineString) {
        double startz = lineString.getStartPoint().getCoordinate().z;
        double endz = lineString.getEndPoint().getCoordinate().z;
        return linearZInterpolation(lineString, startz, endz);
    }

    public static MultiLineString linearZInterpolation(MultiLineString multiLineString) {
        int nbGeom = multiLineString.getNumGeometries();
        LineString[] lines = new LineString[nbGeom];
        for (int i = 0; i < nbGeom; i++) {
            LineString subGeom = (LineString) multiLineString.getGeometryN(i);
            double startz = subGeom.getStartPoint().getCoordinates()[0].z;
            double endz = subGeom.getEndPoint().getCoordinates()[0].z;
            double length = subGeom.getLength();
            subGeom.apply(new LinearZInterpolationFilter(startz, endz, length));
            lines[i] = subGeom;

        }
        return FACTORY.createMultiLineString(lines);
    }

    private static class LinearZInterpolationFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private double startZ = 0;
        private double endZ = 0;
        private double dZ = 0;
        private final double length;
        private int seqSize = 0;
        private double sumLenght = 0;

        LinearZInterpolationFilter(double startZ, double endZ, double length) {
            this.startZ = startZ;
            this.endZ = endZ;
            this.length = length;

        }

        @Override
        public void filter(CoordinateSequence seq, int i) {
            if (i == 0) {
                seqSize = seq.size();
                dZ = endZ - startZ;
            } else if (i == seqSize) {
                done = true;
            } else {
                Coordinate coord = seq.getCoordinate(i);
                Coordinate previousCoord = seq.getCoordinate(i - 1);
                sumLenght += coord.distance(previousCoord);
                seq.setOrdinate(i, 2, startZ + dZ * sumLenght / length);
            }

        }

        @Override
        public boolean isGeometryChanged() {
            return true;
        }

        @Override
        public boolean isDone() {
            return done;
        }
    }

    /**
     * Updates all z values by a new value using the specified first and the
     * last coordinates.
     *
     * @param geom
     * @param startZ
     * @param endZ
     * @return
     */
    public static Geometry force3DStartEnd(Geometry geom, final double startZ,
            final double endZ) {

        final double bigD = geom.getLength();

        final double z = endZ - startZ;

        final Coordinate coordEnd = geom.getCoordinates()[geom.getCoordinates().length - 1];

        geom.apply(new CoordinateSequenceFilter() {
            boolean done = false;

            @Override
            public boolean isGeometryChanged() {
                return true;
            }

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                double x = seq.getX(i);
                double y = seq.getY(i);
                if (i == 0) {
                    seq.setOrdinate(i, 0, x);
                    seq.setOrdinate(i, 1, y);
                    seq.setOrdinate(i, 2, startZ);
                } else if (i == seq.size() - 1) {
                    seq.setOrdinate(i, 0, x);
                    seq.setOrdinate(i, 1, y);
                    seq.setOrdinate(i, 2, endZ);
                } else {

                    double smallD = seq.getCoordinate(i).distance(coordEnd);
                    double factor = smallD / bigD;
                    seq.setOrdinate(i, 0, x);
                    seq.setOrdinate(i, 1, y);
                    seq.setOrdinate(i, 2, startZ + (factor * z));
                }

                if (i == seq.size()) {
                    done = true;
                }
            }
        });

        return geom;

    }

    /**
     * Reverses a multilinestring according to z value. The z first point must
     * be greater than the z end point
     *
     * @param multiLineString
     * @return
     */
    public static MultiLineString reverse3D(MultiLineString multiLineString) {
        int num = multiLineString.getNumGeometries();
        LineString[] lineStrings = new LineString[num];
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            lineStrings[i] = reverse3D((LineString) multiLineString.getGeometryN(i));

        }
        return FACTORY.createMultiLineString(lineStrings);
    }

    /**
     * Reverses a LineString according to the z value. The z of the first point
     * must be lower than the z of the end point.
     *
     * @param lineString
     * @return
     */
    public static LineString reverse3D(LineString lineString) {
        CoordinateSequence seq = lineString.getCoordinateSequence();
        double startZ = seq.getCoordinate(0).z;
        double endZ = seq.getCoordinate(seq.size() - 1).z;
        if (!Double.isNaN(startZ) && !Double.isNaN(endZ) && startZ < endZ) {
            CoordinateSequences.reverse(seq);
            return FACTORY.createLineString(seq);
        }

        return lineString;
    }

    /**
     * Reverse a linestring or a multilinetring according to z value. The z
     * first point must be greater than the z end point
     *
     * @param geometry
     * @return
     */
    public static Geometry reverse3D(Geometry geometry) {
        if (geometry instanceof MultiLineString) {
            return reverse3D((MultiLineString) geometry);
        } else if (geometry instanceof LineString) {
            return reverse3D((LineString) geometry);
        }
        return geometry;
    }

    /**
     * Converts a xyz geometry to xy.
     *
     * @param geom
     * @return
     */
    public static Geometry force2D(Geometry geom) {

        // return new Geometry2DTransformer().transform(geom);  
        geom.apply(new CoordinateSequenceFilter() {
            private boolean done = false;

            @Override
            public boolean isGeometryChanged() {
                return true;
            }

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                seq.setOrdinate(i, 2, Double.NaN);

                if (i == seq.size()) {
                    done = true;
                }
            }
        });

        return geom;

    }

    /**
     * Adds a new z value to each vertex of the Geometry.
     *
     * If the geometry doesn't contain a z (ie NaN value) a z equal to zero is
     * added. The boolean argument is used to set if the z value must be added
     * (true) or if the z value must replace all existing z (false).
     *
     * @param geom
     * @param value
     * @param addZ
     * @return
     */
    public static Geometry force3D(Geometry geom, final double value, final boolean addZ) {

        geom.apply(new CoordinateSequenceFilter() {
            private boolean done = false;

            @Override
            public boolean isGeometryChanged() {
                return true;
            }

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                Coordinate coord = seq.getCoordinate(i);
                double z = coord.z;
                if (addZ) {
                    if (Double.isNaN(z)) {
                        z = 0;
                    }
                    seq.setOrdinate(i, 2, z + value);
                } else {
                    seq.setOrdinate(i, 2, value);

                }
                if (i == seq.size()) {
                    done = true;
                }
            }
        });
        return geom;
    }

    /**
     * Split a linestring with a point The point must be on the linestring
     *
     * @param line
     * @param pointToSplit
     * @return
     */
    public static MultiLineString splitLineWithPoint(LineString line, Point pointToSplit) {
        return FACTORY.createMultiLineString(splitLineStringWithPoint(line, pointToSplit, PRECISION));
    }

    /**
     * Splits a LineString using a Point.
     *
     * @param line
     * @param pointToSplit
     * @return
     */
    public static MultiLineString splitLineWithPoint(LineString line, Point pointToSplit, double tolerance) {
        return FACTORY.createMultiLineString(splitLineStringWithPoint(line, pointToSplit, tolerance));
    }

    /**
     * Splits a LineString using a Point.
     *
     * @param line
     * @param pointToSplit
     * @return
     */
    public static LineString[] splitLineStringWithPoint(LineString line, Point pointToSplit) {
        return splitLineStringWithPoint(line, pointToSplit, PRECISION);
    }

    /**
     * Splits a LineString using a Point, with a distance tolerance.
     *
     * @param line
     * @param pointToSplit
     * @param tolerance
     * @return
     */
    public static LineString[] splitLineStringWithPoint(LineString line, Point pointToSplit, double tolerance) {
        Coordinate[] coords = line.getCoordinates();
        Coordinate firstCoord = coords[0];
        Coordinate lastCoord = coords[coords.length - 1];
        Coordinate coordToSplit = pointToSplit.getCoordinate();
        if ((coordToSplit.distance(firstCoord) <= PRECISION) || (coordToSplit.distance(lastCoord) <= PRECISION)) {
            return new LineString[]{line};
        } else {
            ArrayList<Coordinate> firstLine = new ArrayList<Coordinate>();
            firstLine.add(coords[0]);
            ArrayList<Coordinate> secondLine = new ArrayList<Coordinate>();
            GeometryLocation geometryLocation = getVertexToSnap(line, pointToSplit, tolerance);
            if (geometryLocation != null) {
                int segmentIndex = geometryLocation.getSegmentIndex();
                Coordinate coord = geometryLocation.getCoordinate();
                int index = -1;
                for (int i = 1; i < coords.length; i++) {
                    index = i - 1;
                    if (index < segmentIndex) {
                        firstLine.add(coords[i]);
                    } else if (index == segmentIndex) {
                        coord.z = CoordinateUtils.interpolate(coords[i - 1], coords[i], coord);
                        firstLine.add(coord);
                        secondLine.add(coord);
                        if (!coord.equals2D(coords[i])) {
                            secondLine.add(coords[i]);
                        }
                    } else {
                        secondLine.add(coords[i]);
                    }
                }
                LineString lineString1 = FACTORY.createLineString(firstLine.toArray(new Coordinate[firstLine.size()]));
                LineString lineString2 = FACTORY.createLineString(secondLine.toArray(new Coordinate[secondLine.size()]));
                return new LineString[]{lineString1, lineString2};
            }
        }
        return null;
    }

    /**
     * Splits a MultilineString using a point.
     *
     * @param multiLineString
     * @param pointToSplit
     * @return
     */
    public static MultiLineString splitMultiLineStringWithPoint(MultiLineString multiLineString, Point pointToSplit) {
        return splitMultiLineStringWithPoint(multiLineString, pointToSplit, PRECISION);
    }

    /**
     * Splits a MultilineString using a point.
     *
     * @param multiLineString
     * @param pointToSplit
     * @param tolerance
     * @return
     */
    public static MultiLineString splitMultiLineStringWithPoint(MultiLineString multiLineString, Point pointToSplit, double tolerance) {
        ArrayList<LineString> linestrings = new ArrayList<LineString>();
        boolean notChanged = true;
        int nb = multiLineString.getNumGeometries();
        for (int i = 0; i < nb; i++) {
            LineString subGeom = (LineString) multiLineString.getGeometryN(i);
            LineString[] result = splitLineStringWithPoint(subGeom, pointToSplit, tolerance);
            if (result != null) {
                Collections.addAll(linestrings, result);
                notChanged = false;
            } else {
                linestrings.add(subGeom);
            }
        }
        if (!notChanged) {
            return FACTORY.createMultiLineString(linestrings.toArray(new LineString[linestrings.size()]));
        }
        return null;
    }

    /**
     * Split a geometry at intersections and return a list of sublinestrings
     *
     * @param lineString
     *
     */
    public static Geometry geometryNoders(Geometry tobeNodes) {
        Noder noder = new MCIndexNoder(new IntersectionAdder(new RobustLineIntersector()));
        noder.computeNodes(createNodedSegmentStrings(tobeNodes));
        Collection<NodedSegmentString> nodedSegStrings = noder.getNodedSubstrings();
        return fromSegmentStrings(nodedSegStrings);
    }

    /**
     * Extracts all the 1-dimensional ({@link LineString}) components from a
     * {@link Geometry}. Store them as a list of({@link NodedSegmentString})
     *
     * @param geom
     * @return
     */
    public static List<NodedSegmentString> createNodedSegmentStrings(Geometry geom) {
        List<NodedSegmentString> segs = new ArrayList<NodedSegmentString>();
        List<LineString> lines = LinearComponentExtracter.getLines(geom);
        for (LineString line : lines) {
            segs.add(new NodedSegmentString(line.getCoordinates(), null));
        }
        return segs;
    }

    /**
     * Convert a list of {@link NodedSegmentString})to a
     * ({@link MultiLineString})
     *
     * @param segStrings
     * @return
     */
    public static Geometry fromSegmentStrings(Collection<NodedSegmentString> segStrings) {
        LineString[] lines = new LineString[segStrings.size()];
        int index = 0;
        for (NodedSegmentString ss : segStrings) {
            LineString line = FACTORY.createLineString(ss.getCoordinates());
            lines[index++] = line;
        }
        return FACTORY.createMultiLineString(lines);
    }

    /**
     * Splits the specified lineString with another lineString.
     *
     * @param lineString
     * @param lineString
     *
     */
    public static Geometry splitLineStringWithLine(LineString input, LineString cut) {
        return input.difference(cut);
    }

    /**
     * Splits the specified MultiLineString with another lineString.
     *
     * @param MultiLineString
     * @param lineString
     *
     */
    public static MultiLineString splitMultiLineStringWithLine(MultiLineString input, LineString cut) {
        ArrayList<Geometry> geometries = new ArrayList<Geometry>();
        Geometry lines = input.difference(cut);
        for (int i = 0; i < lines.getNumGeometries(); i++) {
            geometries.add(lines.getGeometryN(i));
        }
        return FACTORY.createMultiLineString(geometries.toArray(new LineString[geometries.size()]));
    }

    /**
     * Gets the coordinate of a Geometry that is the nearest of a given Point,
     * with a distance tolerance.
     *
     * @param g
     * @param p
     * @param tolerance
     * @return
     */
    public static GeometryLocation getVertexToSnap(Geometry g, Point p, double tolerance) {
        DistanceOp distanceOp = new DistanceOp(g, p);
        GeometryLocation snapedPoint = distanceOp.nearestLocations()[0];
        if (tolerance == 0 || snapedPoint.getCoordinate().distance(p.getCoordinate()) <= tolerance) {
            return snapedPoint;
        }
        return null;

    }

    /**
     * Gets the coordinate of a Geometry that is the nearest of a given Point.
     *
     * @param g
     * @param p
     * @return
     */
    public static GeometryLocation getVertexToSnap(Geometry g, Point p) {
        return getVertexToSnap(g, p, PRECISION);
    }

    /**
     * Inserts a vertex into a LineString.
     *
     * @param lineString
     * @param vertexPoint
     * @return
     * @throws GeometryException
     */
    public static Geometry insertVertexInLineString(LineString lineString, Point vertexPoint) throws GeometryException {
        return insertVertexInLineString(lineString, vertexPoint, -1);
    }

    /**
     * Inserts a vertex into a LineString with a given tolerance.
     *
     * @param lineString
     * @param vertexPoint
     * @param tolerance
     * @return
     * @throws GeometryException
     */
    public static LineString insertVertexInLineString(LineString lineString, Point vertexPoint,
            double tolerance) throws GeometryException {
        GeometryLocation geomLocation = getVertexToSnap(lineString, vertexPoint, tolerance);
        if (geomLocation != null) {
            Coordinate[] coords = lineString.getCoordinates();
            int index = geomLocation.getSegmentIndex();
            Coordinate coord = geomLocation.getCoordinate();
            if (!CoordinateUtils.contains2D(coords, coord)) {
                Coordinate[] ret = new Coordinate[coords.length + 1];
                System.arraycopy(coords, 0, ret, 0, index + 1);
                ret[index + 1] = coord;
                System.arraycopy(coords, index + 1, ret, index + 2, coords.length
                        - (index + 1));
                return FACTORY.createLineString(ret);
            }
            return null;
        } else {
            return null;
        }


    }

    /**
     * Inserts a vertex into a linearRing.
     *
     * @param lineString
     * @param vertexPoint
     * @return
     */
    public static LinearRing insertVertexInLinearRing(LineString lineString,
            Point vertexPoint) {
        return insertVertexInLinearRing(lineString, vertexPoint, -1);

    }

    /**
     * Inserts a vertex into a LinearRing with a given tolerance.
     *
     * @param lineString
     * @param vertexPoint
     * @param tolerance
     * @return
     */
    public static LinearRing insertVertexInLinearRing(LineString lineString,
            Point vertexPoint, double tolerance) {
        GeometryLocation geomLocation = getVertexToSnap(lineString, vertexPoint, tolerance);
        if (geomLocation != null) {
            Coordinate[] coords = lineString.getCoordinates();
            int index = geomLocation.getSegmentIndex();
            Coordinate coord = geomLocation.getCoordinate();
            if (!CoordinateUtils.contains2D(coords, coord)) {
                Coordinate[] ret = new Coordinate[coords.length + 1];
                System.arraycopy(coords, 0, ret, 0, index + 1);
                ret[index + 1] = coord;
                System.arraycopy(coords, index + 1, ret, index + 2, coords.length
                        - (index + 1));
                return FACTORY.createLinearRing(ret);
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Inserts a vertex into a polygon.
     *
     * @param polygon
     * @param vertexPoint
     * @return
     * @throws GeometryException
     */
    public static Geometry insertVertexInPolygon(Polygon polygon,
            Point vertexPoint) throws GeometryException {
        return insertVertexInPolygon(polygon, vertexPoint, -1);

    }

    /**
     * Inserts a vertex into a Polygon with a given tolerance.
     *
     * @param polygon
     * @param vertexPoint
     * @param tolerance
     * @return
     * @throws GeometryException
     */
    public static Polygon insertVertexInPolygon(Polygon polygon,
            Point vertexPoint, double tolerance) throws GeometryException {
        LinearRing inserted = insertVertexInLinearRing(polygon.getExteriorRing(), vertexPoint, tolerance);
        if (inserted != null) {
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < holes.length; i++) {
                holes[i] = FACTORY.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
            }
            Polygon ret = FACTORY.createPolygon(inserted, holes);

            if (!ret.isValid()) {
                throw new GeometryException("This geometry is not valid.");
            }

            return ret;
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            inserted = insertVertexInLinearRing(polygon.getInteriorRingN(i), vertexPoint, tolerance);
            if (inserted != null) {
                LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
                for (int h = 0; h < holes.length; h++) {
                    if (h == i) {
                        holes[h] = inserted;
                    } else {
                        holes[h] = FACTORY.createLinearRing(polygon.getInteriorRingN(h).getCoordinates());
                    }
                }

                Polygon ret = FACTORY.createPolygon(FACTORY.createLinearRing(polygon.getExteriorRing().getCoordinates()), holes);

                if (!ret.isValid()) {
                    throw new GeometryException("This geometry is not valid.");
                }

                return ret;
            }
        }

        return null;
    }

    /**
     * Inserts a Point into a MultiPoint geometry.
     *
     * @param g
     * @param vertexPoint
     * @return
     */
    public static Geometry insertVertexInMultipoint(Geometry g, Point vertexPoint) {
        ArrayList<Point> geoms = new ArrayList<Point>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Point geom = (Point) g.getGeometryN(i);
            geoms.add(geom);
        }
        geoms.add(FACTORY.createPoint(new Coordinate(vertexPoint.getX(), vertexPoint.getY())));
        return FACTORY.createMultiPoint(GeometryFactory.toPointArray(geoms));
    }

    /**
     * Inserts a Point into a geometry.
     *
     * @param geom
     * @param point
     * @return
     * @throws GeometryException
     */
    public static Geometry insertVertex(Geometry geom, Point point) throws GeometryException {
        return insertVertex(geom, point, -1);
    }

    /**
     * Returns a new geometry based on an existing one, with a specific point as
     * a new vertex.
     *
     * @param geometry
     * @param vertexPoint
     * @param tolerance
     * @return Null if the vertex cannot be inserted
     * @throws GeometryException If the vertex can be inserted but it makes the
     * geometry to be in an invalid shape
     */
    public static Geometry insertVertex(Geometry geometry, Point vertexPoint, double tolerance)
            throws GeometryException {
        if (geometry instanceof MultiPoint) {
            return insertVertexInMultipoint(geometry, vertexPoint);
        } else if (geometry instanceof LineString) {
            return insertVertexInLineString((LineString) geometry, vertexPoint, tolerance);
        } else if (geometry instanceof MultiLineString) {
            LineString[] linestrings = new LineString[geometry.getNumGeometries()];
            boolean any = false;
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                LineString line = (LineString) geometry.getGeometryN(i);

                LineString inserted = insertVertexInLineString(line, vertexPoint, tolerance);
                if (inserted != null) {
                    linestrings[i] = inserted;
                    any = true;
                } else {
                    linestrings[i] = line;
                }
            }
            if (any) {
                return FACTORY.createMultiLineString(linestrings);
            } else {
                return null;
            }
        } else if (geometry instanceof Polygon) {
            return insertVertexInPolygon((Polygon) geometry, vertexPoint, tolerance);
        } else if (geometry instanceof MultiPolygon) {
            Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
            boolean any = false;
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) geometry.getGeometryN(i);

                Polygon inserted = insertVertexInPolygon(polygon, vertexPoint, tolerance);
                if (inserted != null) {
                    any = true;
                    polygons[i] = inserted;
                } else {
                    polygons[i] = polygon;
                }
            }
            if (any) {
                return FACTORY.createMultiPolygon(polygons);
            } else {
                return null;
            }
        }
        throw new UnsupportedOperationException("Unknown type : " + geometry.getGeometryType());
    }

    /**
     * Splits a Polygon with a LineString.
     *
     * @param polygon
     * @param lineString
     * @return
     */
    public static Collection<Polygon> splitPolygonizer(Polygon polygon, LineString lineString) {
        Set<LineString> segments = GeometryConvert.toSegmentsLineString(polygon.getExteriorRing());
        segments.add(lineString);
        int holes = polygon.getNumInteriorRing();

        for (int i = 0; i < holes; i++) {
            segments.addAll(GeometryConvert.toSegmentsLineString(polygon.getInteriorRingN(i)));
        }

        // Perform union of all extracted LineStrings (the edge-noding process)  
        UnaryUnionOp uOp = new UnaryUnionOp(segments);
        Geometry union = uOp.union();

        // Create polygons from unioned LineStrings  
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(union);
        Collection<Polygon> polygons = polygonizer.getPolygons();

        if (polygons.size() > 1) {
            return polygons;
        }
        return null;
    }

    /**
     * Splits a Polygon using a LineString.
     *
     * @param polygon
     * @param lineString
     * @return
     */
    public static Geometry splitPolygonWithLine(Polygon polygon, LineString lineString) {
        Collection<Polygon> pols = polygonWithLineSplitter(polygon, lineString);
        if (pols != null) {
            return FACTORY.buildGeometry(pols);
        }
        return null;
    }

    /**
     * Splits a Polygon using a LineString.
     *
     * @param polygon
     * @param lineString
     * @return
     */
    public static Collection<Polygon> polygonWithLineSplitter(Polygon polygon, LineString lineString) {
        Collection<Polygon> polygons = splitPolygonizer(polygon, lineString);
        if (polygons != null && polygons.size() > 1) {
            List<Polygon> pols = new ArrayList<Polygon>();
            for (Polygon pol : polygons) {
                if (polygon.contains(pol.getInteriorPoint())) {
                    pols.add(pol);
                }
            }
            return pols;
        }
        return null;
    }

    /**
     * Splits a MultiPolygon using a LineString.
     *
     * @param multiPolygon
     * @param lineString
     * @return
     */
    public static Geometry splitMultiPolygonWithLine(MultiPolygon multiPolygon, LineString lineString) {
        ArrayList<Polygon> allPolygons = new ArrayList<Polygon>();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Collection<Polygon> polygons = splitPolygonizer((Polygon) multiPolygon.getGeometryN(i), lineString);
            if (polygons != null) {
                allPolygons.addAll(polygons);
            }
        }
        if (!allPolygons.isEmpty()) {
            return FACTORY.buildGeometry(allPolygons);
        }
        return null;
    }

    /**
     * Removes a vertex from a JTS geometry.
     *
     * @param vertexIndex
     * @param g
     * @param minNumVertex
     * @return
     *
     * @throws GeometryException
     */
    public static Coordinate[] removeVertex(int vertexIndex,
            Geometry g, int minNumVertex)
            throws GeometryException {
        Coordinate[] coords = g.getCoordinates();
        if (coords.length <= minNumVertex) {
            throw new GeometryException("This geometry is not valid. To few vertex");
        }
        Coordinate[] newCoords = new Coordinate[coords.length - 1];
        for (int i = 0; i < vertexIndex; i++) {
            newCoords[i] = new Coordinate(coords[i].x, coords[i].y, coords[i].z);
        }
        if (vertexIndex != coords.length - 1) {
            for (int i = vertexIndex + 1; i < coords.length; i++) {
                newCoords[i - 1] = new Coordinate(coords[i].x, coords[i].y, coords[i].z);
            }
        }

        return newCoords;
    }

    /**
     * Removes a vertex from a MultiPoint.
     *
     * @param geometry
     * @param vertexIndex
     * @return
     * @throws GeometryException
     */
    public static MultiPoint removeVertex(MultiPoint geometry, int vertexIndex) throws GeometryException {
        return FACTORY.createMultiPoint(removeVertex(vertexIndex, geometry, 1));
    }

    /**
     * Removes a vertex from a LineString.
     *
     * @param geometry
     * @param vertexIndex
     * @return
     * @throws GeometryException
     */
    public static LineString removeVertex(LineString geometry, int vertexIndex) throws GeometryException {
        return FACTORY.createLineString(removeVertex(vertexIndex, geometry, 2));
    }

    /**
     * Moves a geometry according to a distance displacement in x and y.
     *
     * @param geometry
     * @param displacement
     * @return
     */
    public static Geometry moveGeometry(Geometry geometry, final double[] displacement) {
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                coordinate.x += displacement[0];
                coordinate.y += displacement[1];
            }
        });
        return geometry;
    }

    /**
     * Moves a geometry according to start and end coordinates.
     *
     * @param geometry
     * @param start
     * @param end
     * @return
     */
    public static Geometry moveGeometry(Geometry geometry, Coordinate start, Coordinate end) {
        double xDisplacement = end.x - start.x;
        double yDisplacement = end.y - start.y;
        return moveGeometry(geometry, new double[]{xDisplacement, yDisplacement});
    }

    /**
     * Cuts a Polygon with a Polygon.
     *
     * @param polygon
     * @param extrudePolygon
     * @return
     */
    public static List<Polygon> cutPolygonWithPolygon(Polygon polygon, Polygon extrudePolygon) {
        Geometry geom = polygon.difference(extrudePolygon);
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Polygon subGeom = (Polygon) geom.getGeometryN(i);
            polygons.add(subGeom);

        }
        return polygons;
    }

    /**
     * Cut a MultiPolygon with a Polygon.
     *
     * @param multiPolygon
     * @param extrudePolygon
     * @return
     */
    public static MultiPolygon cutMultiPolygonWithPolygon(MultiPolygon multiPolygon, Polygon extrudePolygon) {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon subGeom = (Polygon) multiPolygon.getGeometryN(i);
            if (extrudePolygon.intersects(subGeom)) {
                List<Polygon> result = cutPolygonWithPolygon(subGeom, extrudePolygon);
                polygons.addAll(result);
            } else {
                polygons.add(subGeom);
            }
        }

        return FACTORY.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }

    /**
     * Remove holes in a geometry.
     *
     * @param geometry
     * @return
     */
    public static Geometry removeHoles(Geometry geometry) {
        if (geometry instanceof Polygon) {
            return removeHolesPolygon((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            return removeHolesMultiPolygon((MultiPolygon) geometry);
        } else if (geometry instanceof GeometryCollection) {
            Geometry[] geometries = new Geometry[geometry.getNumGeometries()];
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry geom = geometry.getGeometryN(i);
                if (geometry instanceof Polygon) {
                    geometries[i] = removeHolesPolygon((Polygon) geom);
                } else if (geometry instanceof MultiPolygon) {
                    geometries[i] = removeHolesMultiPolygon((MultiPolygon) geom);
                } else {
                    geometries[i] = geom;
                }
            }
            return FACTORY.createGeometryCollection(geometries);
        }
        return geometry;
    }

    /**
     * Create a new multiPolygon without hole.
     *
     * @param multiPolygon
     * @return
     */
    public static MultiPolygon removeHolesMultiPolygon(MultiPolygon multiPolygon) {
        int num = multiPolygon.getNumGeometries();
        Polygon[] polygons = new Polygon[num];
        for (int i = 0; i < num; i++) {
            polygons[i] = removeHolesPolygon((Polygon) multiPolygon.getGeometryN(i));
        }
        return multiPolygon.getFactory().createMultiPolygon(polygons);
    }

    /**
     * Create a new polygon without hole.
     *
     * @param polygon
     * @return
     */
    public static Polygon removeHolesPolygon(Polygon polygon) {
        return new Polygon((LinearRing) polygon.getExteriorRing(), null, polygon.getFactory());
    }

    /**
     * Private constructor for utility class.
     */
    private GeometryEdit() {
    }
}
