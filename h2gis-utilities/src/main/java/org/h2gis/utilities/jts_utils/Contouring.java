/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.TopologyException;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Split triangle into area within the specified range values.
 * *********************************
 * ANR EvalPDU
 * IFSTTAR 11_05_2011
 *
 * @author Nicolas FORTIN
 * @author Judicaël PICAUT
 **********************************
 */
public class Contouring {
    private static final double EPSILON = 1E-15;
    private static final boolean CHECK_RESULT = true;

    private static boolean isoEqual(double isoValue1, double isoValue2) {
        return Math.abs(isoValue1 - isoValue2) < EPSILON * isoValue2;
    }

    private static boolean computeSplitPositionOrdered(double marker1, double marker2,
                                                       double isoValue, Coordinate p1, Coordinate p2,
                                                       Coordinate splitPosition) {
        if (marker1 < isoValue && isoValue < marker2) {
            double interval = (isoValue - marker1) / (marker2 - marker1);
            splitPosition.setCoordinate(new Coordinate(p1.x + (p2.x - p1.x)
                    * interval, p1.y + (p2.y - p1.y) * interval, p1.z
                    + (p2.z - p1.z) * interval));
            return true;
        } else {
            return false;
        }
    }

    private static boolean computeSplitPosition(double marker1, double marker2,
                                                double isoValue, Coordinate p1, Coordinate p2,
                                                Coordinate splitPosition) {
        if (marker1 < marker2) {
            return computeSplitPositionOrdered(marker1, marker2, isoValue, p1,
                    p2, splitPosition);
        } else {
            return computeSplitPositionOrdered(marker2, marker1, isoValue, p2,
                    p1, splitPosition);
        }
    }

    private static short findTriangleSide(TriMarkers currentTriangle, double isoValue,
                                          short sideException, Coordinate splitPosition) {
        if (sideException != 0
                && computeSplitPosition(currentTriangle.m1, currentTriangle.m2,
                isoValue, currentTriangle.p0, currentTriangle.p1,
                splitPosition)) {
            return 0;
        } else if (sideException != 1
                && computeSplitPosition(currentTriangle.m2, currentTriangle.m3,
                isoValue, currentTriangle.p1, currentTriangle.p2,
                splitPosition)) {
            return 1;
        } else if (sideException != 2
                && computeSplitPosition(currentTriangle.m3, currentTriangle.m1,
                isoValue, currentTriangle.p2, currentTriangle.p0,
                splitPosition)) {
            return 2;
        } else {
            return -1;
        }
    }

    /*
     * Return the splitting of a triangle in three parts. This function return
     * always triangles in the counter-clockwise orientation of vertices (if the
     * triangle provided is in the same orientation)
     *
     * @param sideStart Start side of the splitting segment [0-2]
     * @param sideStop End side of the splitting segment [0-2] (must be > sideStart)
     * @param posIsoStart Start coordinate of the splitting segment
     * @param posIsoStop End coordinate of the splitting segment
     * @param isoLvl Iso value of the splitting segment
     * @param currentTriangle Input triangle
     * @param[out] aloneTri Splitted triangle, the side of the shared vertex of sideStart and sideStop
     * @param[out] firstTwinTri Splitted triangle
     * @param[out] secondTwinTri Splitted triangle
     * @return The shared vertex index [0-2]
     */
    private static short getSplittedTriangle(short sideStart, short sideStop,
                                             Coordinate posIsoStart, Coordinate posIsoStop, double isoLvl,
                                             TriMarkers currentTriangle, TriMarkers aloneTri,
                                             TriMarkers firstTwinTri, TriMarkers secondTwinTri)
            throws TopologyException {
        short sharedVertex;
        short secondVertex;
        short thirdVertex;
        if (sideStart == 0 && sideStop == 2) {
            sharedVertex = 0;
            secondVertex = 1;
            thirdVertex = 2;
            aloneTri.setAll(posIsoStart, posIsoStop,
                    currentTriangle.getVertice(sharedVertex), isoLvl, isoLvl,
                    currentTriangle.getMarker(sharedVertex));
            firstTwinTri.setAll(posIsoStart,
                    currentTriangle.getVertice(thirdVertex), posIsoStop,
                    isoLvl, currentTriangle.getMarker(thirdVertex), isoLvl);
            secondTwinTri.setAll(posIsoStart,
                    currentTriangle.getVertice(secondVertex),
                    currentTriangle.getVertice(thirdVertex), isoLvl,
                    currentTriangle.getMarker(secondVertex),
                    currentTriangle.getMarker(thirdVertex));
        } else if (sideStart == 0 && sideStop == 1) {
            sharedVertex = 1;
            secondVertex = 2;
            thirdVertex = 0;
            aloneTri.setAll(posIsoStart,
                    currentTriangle.getVertice(sharedVertex), posIsoStop,
                    isoLvl, currentTriangle.getMarker(sharedVertex), isoLvl);
            firstTwinTri.setAll(posIsoStart, posIsoStop,
                    currentTriangle.getVertice(thirdVertex), isoLvl, isoLvl,
                    currentTriangle.getMarker(thirdVertex));
            secondTwinTri.setAll(posIsoStop,
                    currentTriangle.getVertice(secondVertex),
                    currentTriangle.getVertice(thirdVertex), isoLvl,
                    currentTriangle.getMarker(secondVertex),
                    currentTriangle.getMarker(thirdVertex));
        } else if (sideStart == 1 && sideStop == 2) {
            sharedVertex = 2;
            secondVertex = 0;
            thirdVertex = 1;
            aloneTri.setAll(posIsoStart,
                    currentTriangle.getVertice(sharedVertex), posIsoStop,
                    isoLvl, currentTriangle.getMarker(sharedVertex), isoLvl);
            firstTwinTri.setAll(posIsoStart, posIsoStop,
                    currentTriangle.getVertice(secondVertex), isoLvl, isoLvl,
                    currentTriangle.getMarker(secondVertex));
            secondTwinTri.setAll(posIsoStart,
                    currentTriangle.getVertice(secondVertex),
                    currentTriangle.getVertice(thirdVertex), isoLvl,
                    currentTriangle.getMarker(secondVertex),
                    currentTriangle.getMarker(thirdVertex));
        } else {
            throw new TopologyException("Can't find shared vertex");
        }
        return sharedVertex;
    }

    /**
     * Use interval to split the triangle into severals ones
     *
     * @param[in] beginIncluded Begin of iso level value
     * @param[in] endExcluded End of iso level value
     * @param[in] currentTriangle Triangle to process
     * @param[out] outsideTriangles Split triangles outside of the region
     * @param[out] intervalTriangles Split triangles covered by the region
     * @return False if the entire geometry is outside of the region, true if
     * outsideTriangles or intervalTriangles has been updated.
     */
    public static boolean splitInterval(double beginIncluded, double endExcluded,
                                         TriMarkers currentTriangle,
                                         Deque<TriMarkers> outsideTriangles,
                                         Deque<TriMarkers> intervalTriangles) throws TopologyException {
        if(beginIncluded > currentTriangle.getMaxMarker() || endExcluded < currentTriangle.getMinMarker()) {
            return false;
        }
        short vertIso1Start = -1, vertIso1Stop = -1; // for beginIncluded -
        // Vertice of the
        // triangle where the
        // split Origin and
        // destination will be
        // done
        short vertIso2Start = -1, vertIso2Stop = -1; // for endExcluded -
        // Vertice of the triangle where the split Origin and destination will be done
        short sideIso1Start = -1, sideIso1Stop = -1; // for beginIncluded - Side
        // of the triangle where
        // the split Origin and
        // destination will be
        // done
        short sideIso2Start = -1, sideIso2Stop = -1; // for endExcluded - Side
        // of the triangle where
        // the split Origin and
        // destination will be
        // done
        Coordinate posIso1Start = new Coordinate(), posIso1Stop = new Coordinate();
        Coordinate posIso2Start = new Coordinate(), posIso2Stop = new Coordinate();
        // Process ISO 1 beginIncluded
        // Find if we include some vertices
        if (isoEqual(currentTriangle.m1, beginIncluded)) {
            vertIso1Start = 0;
        }
        if (isoEqual(currentTriangle.m2, beginIncluded)) {
            if (vertIso1Start == -1) {
                vertIso1Start = 1;
            } else {
                vertIso1Stop = 1;
            }
        }
        if (isoEqual(currentTriangle.m3, beginIncluded)) {
            if (vertIso1Start == -1) {
                vertIso1Start = 2;
            } else {
                vertIso1Stop = 2;
            }
        }
        // Find if we need to split a side (interval between two points)
        if (vertIso1Start == -1 || vertIso1Stop == -1) {
            sideIso1Start = findTriangleSide(currentTriangle, beginIncluded,
                    sideIso1Start, posIso1Start);
            if (sideIso1Start != -1) {
                sideIso1Stop = findTriangleSide(currentTriangle, beginIncluded,
                        sideIso1Start, posIso1Stop);
            }
        }
        // Process ISO 2 endExcluded
        // Find if we include some vertices
        if (isoEqual(currentTriangle.m1, endExcluded)) {
            vertIso2Start = 0;
        }
        if (isoEqual(currentTriangle.m2, endExcluded)) {
            if (vertIso2Start == -1) {
                vertIso2Start = 1;
            } else {
                vertIso2Stop = 1;
            }
        }
        if (isoEqual(currentTriangle.m3, endExcluded)) {
            if (vertIso2Start == -1) {
                vertIso2Start = 2;
            } else {
                vertIso2Stop = 2;
            }
        }
        // Find if we need to split a side (interval between two points)
        if (vertIso2Start == -1 || vertIso2Stop == -1) {
            sideIso2Start = findTriangleSide(currentTriangle, endExcluded,
                    sideIso2Start, posIso2Start);
            if (sideIso2Start != -1) {
                sideIso2Stop = findTriangleSide(currentTriangle, endExcluded,
                        sideIso2Start, posIso2Stop);
            }
        }

        // /////////////////////////////
        // Split by specified parameters
        // Final possibilities
        if ((sideIso1Start == -1 && sideIso2Start == -1)
                && ((vertIso1Start != -1 && vertIso1Stop == -1) || (vertIso2Start != -1 && vertIso2Stop == -1))) {
            // Only one vertex in the range domain
            if ((vertIso1Start != -1 && currentTriangle.getMaxMarker(vertIso1Start) < beginIncluded)
                    || (vertIso2Start != -1 && currentTriangle.getMinMarker(vertIso2Start) > endExcluded)) {
                return false;
            } else {
                // Covered totally by the range
                intervalTriangles.add(currentTriangle);
                return true;
            }
        } else if ((vertIso1Start == -1 && sideIso1Start == -1
                && vertIso2Start == -1 && sideIso2Start == -1)
                || // No iso limits inside the triangle
                (vertIso1Start != -1 && vertIso1Stop != -1 && vertIso2Start != -1)
                || // Side == Iso 1 and the third vertice == Iso2
                (vertIso2Start != -1 && vertIso2Stop != -1 && vertIso1Start != -1) // Side
            // ==
            // Iso
            // 2
            // and
            // the
            // third
            // vertice
            // ==
            // Iso1
                ) { // Covered totally by the range
            intervalTriangles.add(currentTriangle);
            return true;
        } else if (((vertIso1Start != -1 || sideIso1Start != -1) && !((vertIso2Start != -1 || sideIso2Start != -1))) ||
                (!(vertIso1Start != -1 || sideIso1Start != -1))) // Range
        // begin notfound but
        {
            // Side to side
            if (sideIso1Start != -1 && sideIso1Stop != -1) {
                // Split triangle in three
                // ///////////////////////////////////
                // First triangle in the shared vertex side
                // Find the shared vertex between each

                TriMarkers aloneTri = new TriMarkers(), firstTwinTri = new TriMarkers(), secondTwinTri = new TriMarkers();
                short sharedVertex = getSplittedTriangle(sideIso1Start,
                        sideIso1Stop, posIso1Start, posIso1Stop, beginIncluded,
                        currentTriangle, aloneTri, firstTwinTri, secondTwinTri);

                if (currentTriangle.getMarker(sharedVertex) < beginIncluded) {
                    outsideTriangles.add(aloneTri);

                    // ///////////////////////////////////
                    // Second and third triangle, at the other side as interval
                    // surface

                    intervalTriangles.add(firstTwinTri);
                    intervalTriangles.add(secondTwinTri);
                    return true;
                } else {
                    intervalTriangles.add(aloneTri);

                    // ///////////////////////////////////
                    // Second and third triangle, at the other side as external
                    // surface
                    outsideTriangles.add(firstTwinTri);
                    outsideTriangles.add(secondTwinTri);
                    return true;
                }
            } else if (sideIso2Start != -1 && sideIso2Stop != -1) {
                // Split triangle in three
                // ///////////////////////////////////
                // First triangle in the shared vertex side
                // Find the shared vertex between each
                TriMarkers aloneTri = new TriMarkers(), firstTwinTri = new TriMarkers(), secondTwinTri = new TriMarkers();
                short sharedVertex = getSplittedTriangle(sideIso2Start,
                        sideIso2Stop, posIso2Start, posIso2Stop, endExcluded,
                        currentTriangle, aloneTri, firstTwinTri, secondTwinTri);
                if (currentTriangle.getMarker(sharedVertex) > endExcluded) {
                    outsideTriangles.add(aloneTri);

                    // ///////////////////////////////////
                    // Second and third triangle, at the other side as interval
                    // surface

                    intervalTriangles.add(firstTwinTri);
                    intervalTriangles.add(secondTwinTri);
                    return true;
                } else {
                    intervalTriangles.add(aloneTri);

                    // ///////////////////////////////////
                    // Second and third triangle, at the other side as external
                    // surface
                    outsideTriangles.add(firstTwinTri);
                    outsideTriangles.add(secondTwinTri);
                    return true;
                }
            }
            // Only One range found
            if ((vertIso1Start != -1 && vertIso1Stop != -1)
                    || (vertIso2Start != -1 && vertIso2Stop != -1)) {
                // Case side covered by iso
                short thirdVert = -1;
                if (vertIso1Start != 0 && vertIso2Start != 0) {
                    thirdVert = 0;
                }
                if (vertIso1Start != 1 && vertIso1Stop != 1
                        && vertIso2Start != 1 && vertIso2Stop != 1) {
                    thirdVert = 1;
                }
                if (vertIso1Stop != 2 && vertIso2Stop != 2) {
                    thirdVert = 2;
                }
                if (currentTriangle.getMarker(thirdVert) >= beginIncluded
                        && currentTriangle.getMarker(thirdVert) < endExcluded) {
                    intervalTriangles.add(currentTriangle);
                    return true;
                } else {
                    // Triangle is out of range
                    return false;
                }
            }
            // Side to vertice
            if (vertIso1Start != -1) {
                // Split triangle in two
                // TODO check this conditional branch
                short vertOutside = -1, vertInside = -1;
                if (currentTriangle.m1 < beginIncluded) {
                    vertOutside = 0;
                    if (vertIso1Start == 1) {
                        vertInside = 2;
                    } else {
                        vertInside = 1;
                    }
                } else if (currentTriangle.m2 < beginIncluded) {
                    vertOutside = 1;
                    if (vertIso1Start == 0) {
                        vertInside = 2;
                    } else {
                        vertInside = 0;
                    }
                } else if (currentTriangle.m3 < beginIncluded) {
                    vertOutside = 2;
                    if (vertIso1Start == 0) {
                        vertInside = 1;
                    } else {
                        vertInside = 0;
                    }
                }

                outsideTriangles.add(new TriMarkers(currentTriangle.getVertice(vertIso1Start), currentTriangle.getVertice(vertOutside), posIso1Start, beginIncluded,
                        currentTriangle.getMarker(vertOutside), beginIncluded));
                intervalTriangles.add(new TriMarkers(currentTriangle.getVertice(vertIso1Start), currentTriangle.getVertice(vertInside), posIso1Start, beginIncluded,
                        currentTriangle.getMarker(vertInside), beginIncluded));
                return true;
            } else if (vertIso2Start != -1) {
                // Split triangle in two
                short vertOutside = -1, vertInside = -1;
                double maxMarker = currentTriangle.getMaxMarker();
                if (isoEqual(currentTriangle.m1, maxMarker)) {
                    vertOutside = 0;
                    if (vertIso2Start == 1) {
                        vertInside = 2;
                    } else {
                        vertInside = 1;
                    }
                } else if (isoEqual(currentTriangle.m2, maxMarker)) {
                    vertOutside = 1;
                    if (vertIso2Start == 0) {
                        vertInside = 2;
                    } else {
                        vertInside = 0;
                    }
                } else {
                    vertOutside = 2;
                    if (vertIso2Start == 0) {
                        vertInside = 1;
                    } else {
                        vertInside = 0;
                    }
                }
                TriMarkers outsideTriangle = new TriMarkers(
                        currentTriangle.getVertice(vertIso2Start), currentTriangle.getVertice(vertOutside), posIso2Start,
                        endExcluded, currentTriangle.getMarker(vertOutside), endExcluded);
                TriMarkers intervalTriangle = new TriMarkers(
                        currentTriangle.getVertice(vertIso2Start), currentTriangle.getVertice(vertInside), posIso2Start,
                        endExcluded, currentTriangle.getMarker(vertInside), endExcluded);
                if(CHECK_RESULT) {
                    // Check that new triangle are in the right side
                    if(!(intervalTriangle.getMinMarker() >= beginIncluded &&  intervalTriangle.getMaxMarker() <= endExcluded)) {
                        throw new TopologyException("Computation error out of bound triangle");
                    }
                    if(outsideTriangle.getMaxMarker() < endExcluded) {
                        throw new TopologyException("Computation error out of bound triangle");
                    }
                }
                outsideTriangles.add(outsideTriangle);
                intervalTriangles.add(intervalTriangle);
                return true;
            }

        } else {
            // Begin and end range inside the triangle

            // First step, make outside inferior triangle
            Deque<TriMarkers> insideTriangles = new LinkedList<TriMarkers>();
            splitInterval(beginIncluded, Double.POSITIVE_INFINITY,
                    currentTriangle, outsideTriangles, insideTriangles);
            // distribute inside and outside superior triangle from the end iso
            for (TriMarkers insideTri : insideTriangles) {
                splitInterval(Double.NEGATIVE_INFINITY, endExcluded, insideTri,
                        outsideTriangles, intervalTriangles);
            }
            return true;
        }
        // Unknown case throw
        throw new TopologyException(
                "Unhandled triangle splitting case :\n vertIso1Start("
                        + vertIso1Start + "), vertIso1Stop(" + vertIso1Stop
                        + "), vertIso2Start(" + vertIso2Start
                        + "), vertIso2Stop(" + vertIso2Stop
                        + "), sideIso1Start(" + sideIso1Start
                        + "), sideIso1Stop(" + sideIso1Stop
                        + "), sideIso2Start(" + sideIso2Start
                        + "), sideIso2Stop(" + sideIso2Stop + ")");
    }

    /**
     *
     * @param triangleData Triangle Coordinates and Marker values
     * @param isoLvls Iso level to extract.
     * @return processedTriangles Return sub-triangle corresponding to iso levels. iso level are stored in markers (same for m0,m1,m2)
     * @throws TopologyException
     */
    public static Map<Short, Deque<TriMarkers>> processTriangle(TriMarkers triangleData, List<Double> isoLvls) throws TopologyException {
        TriMarkers currentTriangle = triangleData;
        Map<Short, Deque<TriMarkers>> toDriver = new HashMap<Short, Deque<TriMarkers>>();
        // For each iso interval
        Deque<TriMarkers> triangleToProcess = new LinkedList<TriMarkers>();
        triangleToProcess.add(currentTriangle);

        do {
            currentTriangle = triangleToProcess.pop();
            Double beginInterval = Double.NEGATIVE_INFINITY;
            short isolvl = 0;
            for (Double endInterval : isoLvls) {
                Deque<TriMarkers> triangleToDriver;

                if (!toDriver.containsKey(isolvl)) {
                    triangleToDriver = new LinkedList<TriMarkers>();
                    toDriver.put(isolvl, triangleToDriver);
                } else {
                    triangleToDriver = toDriver.get(isolvl);
                }
                if (splitInterval(beginInterval, endInterval,
                        currentTriangle, triangleToProcess,
                        triangleToDriver)) {
                    break;
                }
                beginInterval = endInterval;
                isolvl++;
            }
        } while (!triangleToProcess.isEmpty());
        return toDriver;
    }
}
