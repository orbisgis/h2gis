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

package org.h2gis.functions.io.shp.internal;

import org.h2gis.functions.io.utility.CoordinatesUtils;
import org.h2gis.functions.io.utility.ReadBufferManager;
import org.h2gis.functions.io.utility.WriteBufferManager;
import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.algorithm.RobustDeterminant;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a Shapefile polygon.
 * 
 * @author aaime
 * @author Ian Schneider
 * @see "http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/PolygonHandler.java"
 * @version $Id: PolygonHandler.java 22264 2006-10-19 10:10:35Z acuster $
 */
public class PolygonHandler implements ShapeHandler {

        GeometryFactory geometryFactory = new GeometryFactory();
        final ShapeType shapeType;

        public PolygonHandler() {
                shapeType = ShapeType.POLYGON;
        }

        public PolygonHandler(ShapeType type) throws ShapefileException {
                if ((type != ShapeType.POLYGON) && (type != ShapeType.POLYGONM)
                        && (type != ShapeType.POLYGONZ)) {
                        throw new ShapefileException(
                                "PolygonHandler constructor - expected type to be 5, 15, or 25.");
                }
                shapeType = type;
        }

        @Override
        public ShapeType getShapeType() {
                return shapeType;
        }

        @Override
        public int getLength(Geometry geometry) {
                if (geometry.isEmpty())     return 2;
                int nrings = 0;
                int size = geometry.getNumGeometries();
                for (int t = 0; t < size; t++) {
                        Polygon p = (Polygon) geometry.getGeometryN(t);
                        nrings += nrings + 1 + p.getNumInteriorRing();
                }

                int npoints = geometry.getNumPoints();
                int length;

                if (shapeType == ShapeType.POLYGONZ) {
                        length = 44 + (4 * nrings) + (16 * npoints) + (8 * npoints) + 16
                                + (8 * npoints) + 16;
                } else if (shapeType == ShapeType.POLYGONM) {
                        length = 44 + (4 * nrings) + (16 * npoints) + (8 * npoints) + 16;
                } else if (shapeType == ShapeType.POLYGON) {
                        length = 44 + (4 * nrings) + (16 * npoints);
                } else {
                        throw new IllegalStateException(
                                "Expected ShapeType of Polygon, got " + shapeType);
                }
                return length;
        }

        @Override
        public Geometry read(ReadBufferManager buffer, ShapeType type)
                throws IOException {
                if (type == ShapeType.NULL) {
                        return createNull();
                }
                // bounds
                buffer.skip(4 * 8);

                int[] partOffsets;

                int numParts = buffer.getInt();
                int numPoints = buffer.getInt();
                int dimensions = shapeType == ShapeType.POLYGONZ ? 3 : 2;

                partOffsets = new int[numParts];

                for (int i = 0; i < numParts; i++) {
                        partOffsets[i] = buffer.getInt();
                }

                List<LinearRing> shells = new ArrayList<LinearRing>(numParts);
                List<LinearRing> holes = new ArrayList<LinearRing>(numParts);
                CoordinateSequence coords = readCoordinates(buffer, numPoints,
                        dimensions);

                if (shapeType == ShapeType.POLYGONZ) {
                        // z
                        buffer.skip(2 * 8);

                        for (int t = 0; t < numPoints; t++) {
                                coords.setOrdinate(t, 2, buffer.getDouble());
                        }
                }

                int offset = 0;
                int start;
                int finish;
                int length;

                for (int part = 0; part < numParts; part++) {
                        start = partOffsets[part];

                        if (part == (numParts - 1)) {
                                finish = numPoints;
                        } else {
                                finish = partOffsets[part + 1];
                        }
                        length = finish - start;

                        int ringLength;
                        // If the polygon is closed (first vertices equal to the last one)
                        if(coords.getCoordinate(offset).equals(coords.getCoordinate(offset+length-1))) {
                            ringLength = length;
                        } else {
                            // The polygon is open, need to add an additional coordinate
                            ringLength = length + 1;
                        }

                        CoordinateSequence csRing = geometryFactory.getCoordinateSequenceFactory().create(ringLength, 3);
                        // double area = 0;
                        // int sx = offset;
                        for (int i = 0; i < length; i++) {
                                csRing.setOrdinate(i, 0, coords.getOrdinate(offset, 0));
                                csRing.setOrdinate(i, 1, coords.getOrdinate(offset, 1));
                                if (dimensions == 3) {
                                        csRing.setOrdinate(i, 2, coords.getOrdinate(offset, 2));
                                }
                                offset++;
                        }
                        // Close the ring if necessary
                        if(ringLength>length) {
                            csRing.setOrdinate(length, 0, csRing.getOrdinate(0, 0));
                            csRing.setOrdinate(length, 1, csRing.getOrdinate(0, 1));
                            if (dimensions == 3) {
                                csRing.setOrdinate(length, 2, csRing.getOrdinate(0, 2));
                            }
                        }
                        // REVISIT: polygons with only 1 or 2 points are not polygons -
                        // geometryFactory will bomb so we skip if we find one.
                        if (csRing.size() == 0 || csRing.size() > 3) {
                                LinearRing ring = geometryFactory.createLinearRing(csRing);

                                if (isCCW(csRing)) {
                                        // counter-clockwise
                                        holes.add(ring);
                                } else {
                                        // clockwise
                                        shells.add(ring);
                                }
                        }
                }

                // quick optimization: if there's only one shell no need to check
                // for holes inclusion
                if (shells.size() == 1) {
                        return createMulti(shells.get(0), holes);
                } // if for some reason, there is only one hole, we just reverse it and
                // carry on.
                else if (holes.size() == 1 && shells.isEmpty()) {
                        return createMulti((LinearRing) holes.get(0).reverse());
                } else {
                        // build an association between shells and holes
                        final List<List<LinearRing>> holesForShells = assignHolesToShells(shells, holes);

                        return buildGeometries(shells, holes, holesForShells);
                }
        }

        private MultiPolygon createNull() {
                return geometryFactory.createMultiPolygon(null);
        }

        /**
         * Computes whether a ring defined by an array of {@link Coordinate}s is
         * oriented counter-clockwise.
         * <ul>
         * <li>The list of points is assumed to have the first and last points
         * equal.
         * <li>This will handle coordinate lists which contain repeated points.
         * </ul>
         * This algorithm is <b>only</b> guaranteed to work with valid rings. If the
         * ring is invalid (e.g. self-crosses or touches), the computed result may
         * not be correct.
         *
         * @param ring
         *            an array of Coordinates forming a ring
         * @return true if the ring is oriented counter-clockwise.
         */
        public static boolean isCCW(CoordinateSequence ring) {
                // # of points without closing endpoint
                int nPts = ring.size() - 1;

                // find highest point
                double hiy = ring.getOrdinate(0, 1);
                int hiIndex = 0;
                for (int i = 1; i <= nPts; i++) {
                        if (ring.getOrdinate(i, 1) > hiy) {
                                hiy = ring.getOrdinate(i, 1);
                                hiIndex = i;
                        }
                }

                // find distinct point before highest point
                int iPrev = hiIndex;
                do {
                        iPrev -= 1;
                        if (iPrev < 0) {
                                iPrev = nPts;
                        }
                } while (equals2D(ring, iPrev, hiIndex) && iPrev != hiIndex);

                // find distinct point after highest point
                int iNext = hiIndex;
                do {
                        iNext = (iNext + 1) % nPts;
                } while (equals2D(ring, iNext, hiIndex) && iNext != hiIndex);

                /**
                 * This check catches cases where the ring contains an A-B-A
                 * configuration of points. This can happen if the ring does not contain
                 * 3 distinct points (including the case where the input array has fewer
                 * than 4 elements), or it contains coincident line segments.
                 */
                if (equals2D(ring, iPrev, hiIndex) || equals2D(ring, iNext, hiIndex)
                        || equals2D(ring, iPrev, iNext)) {
                        return false;
                }

                int disc = computeOrientation(ring, iPrev, hiIndex, iNext);

                /**
                 * If disc is exactly 0, lines are collinear. There are two possible
                 * cases: (1) the lines lie along the x axis in opposite directions (2)
                 * the lines lie on top of one another
                 *
                 * (1) is handled by checking if next is left of prev ==> CCW (2) will
                 * never happen if the ring is valid, so don't check for it (Might want
                 * to assert this)
                 */
                boolean isCCW = false;
                if (disc == 0) {
                        // poly is CCW if prev x is right of next x
                        isCCW = (ring.getOrdinate(iPrev, 0) > ring.getOrdinate(iNext, 0));
                } else {
                        // if area is positive, points are ordered CCW
                        isCCW = (disc > 0);
                }
                return isCCW;
        }

        private static boolean equals2D(CoordinateSequence cs, int i, int j) {
                return cs.getOrdinate(i, 0) == cs.getOrdinate(j, 0)
                        && cs.getOrdinate(i, 1) == cs.getOrdinate(j, 1);
        }

        public static int computeOrientation(CoordinateSequence cs, int p1, int p2,
                int q) {
                // travelling along p1->p2, turn counter clockwise to get to q return 1,
                // travelling along p1->p2, turn clockwise to get to q return -1,
                // p1, p2 and q are colinear return 0.
                double p1x = cs.getOrdinate(p1, 0);
                double p1y = cs.getOrdinate(p1, 1);
                double p2x = cs.getOrdinate(p2, 0);
                double p2y = cs.getOrdinate(p2, 1);
                double qx = cs.getOrdinate(q, 0);
                double qy = cs.getOrdinate(q, 1);
                double dx1 = p2x - p1x;
                double dy1 = p2y - p1y;
                double dx2 = qx - p2x;
                double dy2 = qy - p2y;
                return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
        }

        /**
         * @param buffer data buffer to read
         * @param numPoints number of points
         */
        private CoordinateSequence readCoordinates(final ReadBufferManager buffer,
                final int numPoints, final int dimensions) throws IOException {
                CoordinateSequence cs = geometryFactory.getCoordinateSequenceFactory().create(numPoints, dimensions);
                for (int t = 0; t < numPoints; t++) {
                        cs.setOrdinate(t, 0, buffer.getDouble());
                        cs.setOrdinate(t, 1, buffer.getDouble());
                }
                return cs;
        }

        /**
         * @param shells
         * @param holes
         * @param holesForShells
         */
        private Geometry buildGeometries(final List<LinearRing> shells,
                final List<LinearRing> holes,
                final List<List<LinearRing>> holesForShells) {
                Polygon[] polygons;

                // if we have shells, lets use them
                if (!shells.isEmpty()) {
                        polygons = new Polygon[shells.size()];
                        // oh, this is a bad record with only holes
                } else {
                        polygons = new Polygon[holes.size()];
                }

                // this will do nothing for the "only holes case"
                for (int i = 0; i < shells.size(); i++) {
                        final List<LinearRing> hole = holesForShells.get(i);
                        polygons[i] = geometryFactory.createPolygon(shells.get(i),
                                hole.toArray(new LinearRing[0]));
                }

                // this will take care of the "only holes case"
                // we just reverse each hole
                if (shells.isEmpty()) {
                        for (int i = 0, ii = holes.size(); i < ii; i++) {
                                LinearRing hole = holes.get(i);
                                polygons[i] = geometryFactory.createPolygon((LinearRing) hole.reverse(), new LinearRing[0]);
                        }
                }

                return geometryFactory.createMultiPolygon(polygons);
        }

        /**
         * <b>Package private for testing</b>
         *
         * @param shells
         * @param holes
         */
        List<List<LinearRing>> assignHolesToShells(
                final List<LinearRing> shells,
                final List<LinearRing> holes) {
                List<List<LinearRing>> holesForShells = new ArrayList<List<LinearRing>>(
                        shells.size());
                for (int i = 0; i < shells.size(); i++) {
                        holesForShells.add(new ArrayList<LinearRing>());
                }

                // find homes
                for (int i = 0; i < holes.size(); i++) {
                        LinearRing testRing = holes.get(i);
                        LinearRing minShell = null;
                        Envelope minEnv = null;
                        Envelope testEnv = testRing.getEnvelopeInternal();
                        Coordinate testPt = testRing.getCoordinateN(0);
                        LinearRing tryRing;

                        for (int j = 0; j < shells.size(); j++) {
                                tryRing = shells.get(j);

                                Envelope tryEnv = tryRing.getEnvelopeInternal();
                                if (minShell != null) {
                                        minEnv = minShell.getEnvelopeInternal();
                                }

                                boolean isContained = false;
                                Coordinate[] coordList = tryRing.getCoordinates();

                                if (tryEnv.contains(testEnv)
                                        && (CGAlgorithms.isPointInRing(testPt, coordList) || (CoordinatesUtils.contains(
                                        coordList, testPt)))) {
                                        isContained = true;
                                }

                                // check if this new containing ring is smaller than the current
                                // minimum ring
                                if (isContained && ((minShell == null) || minEnv.contains(tryEnv))) {
                                        minShell = tryRing;
                                }
                        }

                        if (minShell == null) {
                                // Logger.getLogger("org.geotools.data.shapefile").warning(
                                // "polygon found with a hole thats not inside a shell");
                                // now reverse this bad "hole" and turn it into a shell
                                shells.add((LinearRing) testRing.reverse());
                                holesForShells.add(new ArrayList<LinearRing>());
                        } else {
                                (holesForShells.get(shells.indexOf(minShell))).add(testRing);

                        }
                }

                return holesForShells;
        }

        private MultiPolygon createMulti(LinearRing single) {
                return createMulti(single, new ArrayList<LinearRing>(0));
        }

        private MultiPolygon createMulti(LinearRing single, List<LinearRing> holes) {
                return geometryFactory.createMultiPolygon(new Polygon[]{geometryFactory.createPolygon(single, holes.toArray(new LinearRing[0]))});
        }

        @Override
        public void write(WriteBufferManager buffer, Geometry geometry)
                throws IOException {
                if (geometry.getDimension() != 2) {
                        throw new IllegalArgumentException("Only Polygon and MultiPolygon are managed by the shapefile driver");
                }
                Envelope box = geometry.getEnvelopeInternal();
                buffer.putDouble(box.getMinX());
                buffer.putDouble(box.getMinY());
                buffer.putDouble(box.getMaxX());
                buffer.putDouble(box.getMaxY());

                // need to find the total number of rings and points
                int nrings = 0;

                //Normalize the geometry to be sure to use a clockwise orientation for their exterior ring,
                // and a counter-clockwise orientation for their interior rings
                geometry.normalize();
                int size = geometry.getNumGeometries();
                for (int t = 0; t < size; t++) {
                        Polygon p = (Polygon) geometry.getGeometryN(t);
                        nrings = nrings + 1 + p.getNumInteriorRing();
                }

                int u = 0;
                int[] pointsPerRing = new int[nrings];

                for (int t = 0; t < size; t++) {
                        Polygon p = (Polygon) geometry.getGeometryN(t);
                        pointsPerRing[u] = p.getExteriorRing().getNumPoints();
                        u++;

                        for (int v = 0; v < p.getNumInteriorRing(); v++) {
                                pointsPerRing[u] = p.getInteriorRingN(v).getNumPoints();
                                u++;
                        }
                }

                int npoints = geometry.getNumPoints();

                buffer.putInt(nrings);
                buffer.putInt(npoints);

                int count = 0;

                for (int t = 0; t < nrings; t++) {
                        buffer.putInt(count);
                        count  = count + pointsPerRing[t];
                }

                // write out points here!
                Coordinate[] coords = geometry.getCoordinates();

                for (int t = 0; t < coords.length; t++) {
                        buffer.putDouble(coords[t].x);
                        buffer.putDouble(coords[t].y);
                }

                if (shapeType == ShapeType.POLYGONZ) {
                        // z
                        double[] zExtreame = CoordinatesUtils.zMinMax(coords);

                        if (Double.isNaN(zExtreame[0])) {
                                buffer.putDouble(0.0);
                                buffer.putDouble(0.0);
                        } else {
                                buffer.putDouble(zExtreame[0]);
                                buffer.putDouble(zExtreame[1]);
                        }

                        for (int t = 0; t < npoints; t++) {
                                double z = coords[t].z;

                                if (Double.isNaN(z)) {
                                        buffer.putDouble(0.0);
                                } else {
                                        buffer.putDouble(z);
                                }
                        }
                }

                if (shapeType == ShapeType.POLYGONM || shapeType == ShapeType.POLYGONZ) {
                        // m
                        buffer.putDouble(-10E40);
                        buffer.putDouble(-10E40);

                        for (int t = 0; t < npoints; t++) {
                                buffer.putDouble(-10E40);
                        }
                }
        }
}