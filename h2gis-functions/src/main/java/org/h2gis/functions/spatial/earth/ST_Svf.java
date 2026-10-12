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
package org.h2gis.functions.spatial.earth;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Compute the Sky View Factor (SVF) of a point from a set of obstacles with Z values.
 *
 * For each of the {@code rayCount} horizontal directions, the highest elevation angle
 * of the obstacles met within {@code distance} is kept: tan(gamma) = max((z - z0) / d).
 * SVF = 1 - sum(dTheta * sin^2(gamma)) / 2PI.
 *
 * Implementation: angular sweep. Each obstacle segment is visited once and intersected,
 * with a JTS {@link RobustLineIntersector}, only with the rays whose direction lies within
 * its angular span seen from the point. The intersections do not depend on the observer height,
 * so the same sweep computes the SVF for several heights at once: with a sixth argument (an array of
 * heights relative to the point Z) the function returns one SVF per height, for about the cost of one.
 *
 * @author Erwan Bocher, CNRS
 * @author Jérémy Bernard, CNRS
 */
public class ST_Svf extends DeterministicScalarFunction {

    public ST_Svf() {
        addProperty(PROP_REMARKS, "Return the Sky View Factor (SVF) for a given point.\n"
                + "pt = Point coordinates (x, y, z) - the SVF is calculated from this point\n"
                + "geoms = Geometries used as sky obstacles (z coordinates should be given and not NaN)\n"
                + "distance = Only obstacles located within this distance from pt are considered in the calculation (double - in meters)\n"
                + "rayCount = Number of ray considered for the calculation (integer - number of direction of calculation)\n"
                + "Optional arguments:\n"
                + "heights = Array of observer heights relative to the z of the point (0 if the point is 2D),\n"
                + "e.g. ST_SVF(pt, geoms, 100, 60, ARRAY[0, 1.5, 10]). An array of SVF values is then returned,\n"
                + "one for each height, in the same order.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeSvf";
    }

    /**
     * The method to compute the Sky View Factor
     *
     * @param pt            {@link Point}
     * @param geoms         mask geometries
     * @param distance      distance
     * @param rayCount      number of rays
     * @return svf value
     */
    public static Double computeSvf(Point pt, Geometry geoms, double distance, int rayCount) throws SQLException {
        double[] svf = compute(pt, geoms, distance, rayCount, new double[]{0});
        return svf == null ? null : svf[0];
    }

    /**
     * The method to compute the Sky View Factor for several observer heights
     *
     * @param pt            {@link Point}, its Z (0 if the point is 2D) is the base level of the heights
     * @param geoms         mask geometries
     * @param distance      distance
     * @param rayCount      number of rays
     * @param heights       observer heights relative to the point Z
     * @return one svf value per height, in the order of the heights
     */
    public static Double[] computeSvf(Point pt, Geometry geoms, double distance, int rayCount,
                                      Double[] heights) throws SQLException {
        if (heights == null) {
            return null;
        }
        if (heights.length == 0) {
            throw new IllegalArgumentException("The array of heights cannot be empty");
        }
        double[] h = new double[heights.length];
        for (int k = 0; k < heights.length; k++) {
            if (heights[k] == null || heights[k].isNaN()) {
                throw new IllegalArgumentException("The heights cannot contain null or NaN values");
            }
            h[k] = heights[k];
        }
        double[] svf = compute(pt, geoms, distance, rayCount, h);
        if (svf == null) {
            return null;
        }
        Double[] result = new Double[svf.length];
        for (int k = 0; k < svf.length; k++) {
            result[k] = svf[k];
        }
        return result;
    }

    /**
     * Compute the SVF for several observer heights.
     *
     * @param pt       observation point, its Z (0 if NaN) is the base level
     * @param geoms    mask geometries
     * @param distance only obstacles within this distance are considered
     * @param rayCount number of rays
     * @param heights  heights above the point Z
     * @return one SVF per height, or null when the point or the obstacles are null or empty
     */
    private static double[] compute(Point pt, Geometry geoms, double distance, int rayCount,
                                    double[] heights) throws SQLException {
        if (pt == null || pt.isEmpty() || geoms == null || geoms.isEmpty()) {
            return null;
        }
        if (pt.getSRID() != geoms.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        if (distance <= 0) {
            throw new IllegalArgumentException("The distance value must be greater than 0");
        }
        if (rayCount < 4) {
            throw new IllegalArgumentException("The number of rays must be greater than or equal to 4");
        }
        if (geoms.getDimension() == 0) {
            return null;
        }
        Coordinate start = pt.getCoordinate();
        double baseZ = Double.isNaN(start.z) ? 0 : start.z;
        double[] z0 = new double[heights.length];
        for (int k = 0; k < heights.length; k++) {
            z0[k] = baseZ + heights[k];
        }
        //All data needed to compute the intersection are initialized
        SegmentScan segmentScan = new SegmentScan(start.x, start.y, z0, distance, rayCount);
        int nbGeoms = geoms.getNumGeometries();
        //Extract segments and compute the intersection during each iteration
        // This scan reduces the data collected and avoid overlay operation
        for (int i = 0; i < nbGeoms; i++) {
            Geometry subGeom = geoms.getGeometryN(i);
            if (subGeom instanceof LineString) {
                segmentScan.addSegments(((LineString) subGeom).getCoordinateSequence());
            } else if (subGeom instanceof Polygon) {
                Polygon p = (Polygon) subGeom;
                segmentScan.addSegments(p.getExteriorRing().getCoordinateSequence());
                int nbInterior = p.getNumInteriorRing();
                for (int j = 0; j < nbInterior; j++) {
                    segmentScan.addSegments(p.getInteriorRingN(j).getCoordinateSequence());
                }
            }
        }
        double[] svf = new double[heights.length];
        for (int k = 0; k < heights.length; k++) {
            svf[k] = segmentScan.segmentCount == 0 ? 1D : segmentScan.svf(k);
        }
        return svf;
    }

    /**
     * Highest elevation angle per ray and per observer height, updated wall segment by wall segment.
     */
    private static final class SegmentScan {
        // Starting point of the rays (the observation point), z is not used
        private final Coordinate startPointRay;
        // Only the walls within this distance from the starting point of the rays are kept
        private final double distance;
        // Angle between two consecutive rays, in radians: 2PI / rayCount
        private final double angleBetweenRays;
        private final int rayCount;
        // Elevation of the starting point of the rays for each height
        private final double[] raysStartZ;
        // Lowest elevation of the starting point of the rays
        private final double minRayStartZ;
        // End point of each ray, at the search distance
        private final Coordinate[] endPointRays;
        // Tan of the highest elevation angle for each height and each ray
        // maxElevationTan[height index][ray index]
        private final double[][] maxElevationTanPerHeightAndRay;
        // Square of side 2 * distance centred on the starting point of the rays, used as a fast distance pre-filter
        private final Envelope searchEnvelope;
        // Current wall segment, reused for every wall (no object created per wall)
        private final LineSegment wall = new LineSegment();
        // JTS class to compute the intersection between a ray and a wall
        private final LineIntersector lineIntersector = new RobustLineIntersector();
        // Number of wall segments with a z
        private int segmentCount;


        /**
         * Prepare the rays
         *
         * @param point_x   x coordinate of the starting point of the rays
         * @param point_y   y coordinate of the starting point of the rays
         * @param raysStartZ elevation of the starting point of the rays for each height
         * @param distance  search distance (length of the rays)
         * @param rayCount  number of rays
         */
        SegmentScan(double point_x, double point_y, double[] raysStartZ, double distance, int rayCount) {
            this.startPointRay = new Coordinate(point_x, point_y);
            //Envelope to filter the wall
            this.searchEnvelope = new Envelope(startPointRay);
            this.searchEnvelope.expandBy(distance);
            this.raysStartZ = raysStartZ;
            this.minRayStartZ = Arrays.stream(raysStartZ).min().getAsDouble();
            this.distance = distance;
            this.rayCount = rayCount;
            this.angleBetweenRays = 2 * Math.PI / rayCount;
            this.maxElevationTanPerHeightAndRay = new double[raysStartZ.length][rayCount];
            this.endPointRays = new Coordinate[rayCount];
            // End point of each ray: ray i has the direction i * angleBetweenRays
            for (int i = 0; i < rayCount; i++) {
                double angle = angleBetweenRays * i;
                endPointRays[i] = new Coordinate(point_x + distance * Math.cos(angle),
                        point_y + distance * Math.sin(angle));
            }
        }

        /**
         * Add the wall segments of a coordinate sequence. Segments with a NaN z are ignored.
         */
        void addSegments(CoordinateSequence seq) {
            int n = seq.size();
            for (int j = 0; j < n - 1; j++) {
                // Update the wall with two consecutive vertices j and j + 1
                wall.p0.setCoordinate(seq.getCoordinate(j));
                wall.p1.setCoordinate(seq.getCoordinate(j + 1));
                if (Double.isNaN(wall.p0.getZ()) || Double.isNaN(wall.p1.getZ())) {
                    continue;
                }
                segmentCount++;
                addSegment();
            }
        }

        /**
         * Adds the current wall segment: finds the rays that can hit it and updates
         * their highest elevation angle.
         */
        private void addSegment() {
            // Height filter: if the top of the wall is not above the lowest starting point
            //    of the rays, it cannot mask any part of the sky
            if (Math.max(wall.p0.getZ(), wall.p1.getZ()) <= minRayStartZ) {
                return;
            }
            // Test if the bounding box of the wall is outside the search envelope
            // It reduces the number of candidates
            if (!searchEnvelope.intersects(wall.p0, wall.p1)) {
                return;
            }
            // Test if the closest point of the wall is farther than the search distance
            if (wall.distance(startPointRay) > distance) {
                return;
            }
            // The starting point of the rays lies on the wall.
            if (PointLocation.isOnSegment(startPointRay, wall.p0, wall.p1)) {
                for (int i = 0; i < rayCount; i++) {
                    intersect(i);
                }
                return;
            }

            // Direction (radians) of the start point of the wall
            double angleStartPointWall = Angle.angle(startPointRay, wall.p0);
            // Signed angle from the start point to the end point of the wall
            double wallViewAngle = Angle.angleBetweenOriented(wall.p0, startPointRay, wall.p1);
            // Range of directions covered by the wall, from minAngle to maxAngle (counterclockwise),
            // whatever the order of its vertices:
            // - wallViewAngle >= 0: the wall goes counterclockwise from wall.p0, so it starts at wall.p0
            // - wallViewAngle <  0: it goes clockwise from wall.p0, so it starts at wall.p1
            double minAngle = wallViewAngle >= 0 ? angleStartPointWall : angleStartPointWall + wallViewAngle;
            double maxAngle = minAngle + Math.abs(wallViewAngle);
            //Here we want to filter only the ray that are between min and max angles
            int firstRay = (int) Math.floor(minAngle / angleBetweenRays) - 1;
            int lastRay = (int) Math.ceil(maxAngle / angleBetweenRays) + 1;
            for (int ray = firstRay; ray <= lastRay; ray++) {
                intersect(Math.floorMod(ray, rayCount));
            }
        }

        /**
         * Intersect the ray i with the current wall and update the highest elevation
         * of the ray for every height.
         *
         * @param i index of the ray
         */
        private void intersect(int i) {
            lineIntersector.computeIntersection(startPointRay, endPointRays[i], wall.p0, wall.p1);
            // No intersection, or the ray runs along the wall
            if (lineIntersector.getIntersectionNum() != LineIntersector.POINT_INTERSECTION) {
                return;
            }
            Coordinate intersection = lineIntersector.getIntersection(0);
            // Height of the wall at the intersection point
            double z = CoordinateUtils.interpolate(wall.p0, wall.p1, intersection);
            if (Double.isNaN(z)) {
                // zero length wall
                z = wall.p0.getZ();
            }
            // Horizontal distance from the starting point of the rays to the intersection
            double d = intersection.distance(startPointRay);
            for (int k = 0; k < raysStartZ.length; k++) {
                // Tangent of the elevation angle of the wall top
                // (height difference) / (horizontal distance)
                double elevationTan = (z - raysStartZ[k]) / d;
                if (elevationTan > maxElevationTanPerHeightAndRay[k][i]) {
                    maxElevationTanPerHeightAndRay[k][i] = elevationTan;
                }
            }
        }

        /**
         * SVF for the current height (k), from its horizon line
         * It will be calculed with the formula
         * SVF = 1 - sum(angleBetweenRays * sin^2(gamma_i)) / 2PI
         * where gamma_i is the elevation angle of the horizon along the ray i.
         * Each ray stands for a sector of the sky of width angleBetweenRays.
         * 0 for a clear horizon, 0.5 at 45 degrees, 1 at 90 degrees.
         *
         * @param k index of the height
         * @return the SVF value, between 0 (sky fully hidden) and 1 (clear sky)
         */
        double svf(int k) {
            double[] horizonTanK = maxElevationTanPerHeightAndRay[k];
            double visibleSky = 2 * Math.PI;
            for (int i = 0; i < rayCount; i++) {
                // Elevation angle gamma of the horizon along the ray i
                double sinGamma = Math.sin(Math.atan(horizonTanK[i]));
                // Hidden part of the sector of the ray i
                visibleSky -= angleBetweenRays * sinGamma * sinGamma;
            }
            // Fraction of the sky that is visible
            return visibleSky / (2 * Math.PI);
        }
    }
}