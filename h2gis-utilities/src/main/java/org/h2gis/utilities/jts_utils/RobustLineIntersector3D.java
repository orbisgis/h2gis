/*
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


package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * This class has been extracted from {@link RobustLineIntersector} from JTS in order to fix the Z coordinate.
 * It should be removed once fixed on JTS.
 */
public class RobustLineIntersector3D extends RobustLineIntersector{

    @Override
    protected int computeIntersect(
            Coordinate p1, Coordinate p2,
            Coordinate q1, Coordinate q2  ) {
        isProper = false;

        // first try a fast test to see if the envelopes of the lines intersect
        if (! Envelope.intersects(p1, p2, q1, q2))
            return NO_INTERSECTION;

        // for each endpoint, compute which side of the other segment it lies
        // if both endpoints lie on the same side of the other segment,
        // the segments do not intersect
        int Pq1 = Orientation.index(p1, p2, q1);
        int Pq2 = Orientation.index(p1, p2, q2);

        if ((Pq1>0 && Pq2>0) || (Pq1<0 && Pq2<0)) {
            return NO_INTERSECTION;
        }

        int Qp1 = Orientation.index(q1, q2, p1);
        int Qp2 = Orientation.index(q1, q2, p2);

        if ((Qp1>0 && Qp2>0) || (Qp1<0 && Qp2<0)) {
            return NO_INTERSECTION;
        }

        boolean collinear = Pq1 == 0
                && Pq2 == 0
                && Qp1 == 0
                && Qp2 == 0;
        if (collinear) {
            return computeCollinearIntersection(p1, p2, q1, q2);
        }

        /**
         * At this point we know that there is a single intersection point
         * (since the lines are not collinear).
         */

        /**
         *  Check if the intersection is an endpoint. If it is, copy the endpoint as
         *  the intersection point. Copying the point rather than computing it
         *  ensures the point has the exact value, which is important for
         *  robustness. It is sufficient to simply check for an endpoint which is on
         *  the other line, since at this point we know that the inputLines must
         *  intersect.
         */
        if (Pq1 == 0 || Pq2 == 0 || Qp1 == 0 || Qp2 == 0) {
            isProper = false;

            /**
             * Check for two equal endpoints.
             * This is done explicitly rather than by the orientation tests
             * below in order to improve robustness.
             *
             * [An example where the orientation tests fail to be consistent is
             * the following (where the true intersection is at the shared endpoint
             * POINT (19.850257749638203 46.29709338043669)
             *
             * LINESTRING ( 19.850257749638203 46.29709338043669, 20.31970698357233 46.76654261437082 )
             * and
             * LINESTRING ( -48.51001596420236 -22.063180333403878, 19.850257749638203 46.29709338043669 )
             *
             * which used to produce the INCORRECT result: (20.31970698357233, 46.76654261437082, NaN)
             *
             */
            if (p1.equals2D(q1)
                    || p1.equals2D(q2)) {
                intPt[0] = p1;
            }
            else if (p2.equals2D(q1)
                    || p2.equals2D(q2)) {
                intPt[0] = p2;
            }

            /**
             * Now check to see if any endpoint lies on the interior of the other segment.
             */
            else if (Pq1 == 0) {
                intPt[0] = new Coordinate(q1);
            }
            else if (Pq2 == 0) {
                intPt[0] = new Coordinate(q2);
            }
            else if (Qp1 == 0) {
                intPt[0] = new Coordinate(p1);
            }
            else if (Qp2 == 0) {
                intPt[0] = new Coordinate(p2);
            }
        }
        else {
            isProper = true;
            intPt[0] = intersection(p1, p2, q1, q2);
        }
        return POINT_INTERSECTION;
    }



    /**
     * This method computes the actual value of the intersection point.
     * To obtain the maximum precision from the intersection calculation,
     * the coordinates are normalized by subtracting the minimum
     * ordinate values (in absolute value).  This has the effect of
     * removing common significant digits from the calculation to
     * maintain more bits of precision.
     */
    private Coordinate intersection(
            Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
    {
        Coordinate intPt = intersectionWithNormalization(p1, p2, q1, q2);

            /*
            // TESTING ONLY
            Coordinate intPtDD = CGAlgorithmsDD.intersection(p1, p2, q1, q2);
            double dist = intPt.distance(intPtDD);
            System.out.println(intPt + " - " + intPtDD + " dist = " + dist);
            //intPt = intPtDD;
            */

        /**
         * Due to rounding it can happen that the computed intersection is
         * outside the envelopes of the input segments.  Clearly this
         * is inconsistent.
         * This code checks this condition and forces a more reasonable answer
         *
         * MD - May 4 2005 - This is still a problem.  Here is a failure case:
         *
         * LINESTRING (2089426.5233462777 1180182.3877339689, 2085646.6891757075 1195618.7333999649)
         * LINESTRING (1889281.8148903656 1997547.0560044837, 2259977.3672235999 483675.17050843034)
         * int point = (2097408.2633752143,1144595.8008114607)
         *
         * MD - Dec 14 2006 - This does not seem to be a failure case any longer
         */
        if (! isInSegmentEnvelopes(intPt)) {
            //      System.out.println("Intersection outside segment envelopes: " + intPt);

            // compute a safer result
            // copy the coordinate, since it may be rounded later
            intPt = new Coordinate(nearestEndpoint(p1, p2, q1, q2));
            //    intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);

            //      System.out.println("Segments: " + this);
            //      System.out.println("Snapped to " + intPt);
            //      checkDD(p1, p2, q1, q2, intPt);
        }
        if (precisionModel != null) {
            precisionModel.makePrecise(intPt);
        }
        return intPt;
    }


    /**
     * Finds the endpoint of the segments P and Q which
     * is closest to the other segment.
     * This is a reasonable surrogate for the true
     * intersection points in ill-conditioned cases
     * (e.g. where two segments are nearly coincident,
     * or where the endpoint of one segment lies almost on the other segment).
     * <p>
     * This replaces the older CentralEndpoint heuristic,
     * which chose the wrong endpoint in some cases
     * where the segments had very distinct slopes
     * and one endpoint lay almost on the other segment.
     *
     * @param p1 an endpoint of segment P
     * @param p2 an endpoint of segment P
     * @param q1 an endpoint of segment Q
     * @param q2 an endpoint of segment Q
     * @return the nearest endpoint to the other segment
     */
    private Coordinate nearestEndpoint(Coordinate p1, Coordinate p2,
                                       Coordinate q1, Coordinate q2)
    {
        Coordinate nearestPt = p1;
        double minDist = Distance.pointToSegment(p1, q1, q2);

        double dist = Distance.pointToSegment(p2, q1, q2);
        if (dist < minDist) {
            minDist = dist;
            nearestPt = p2;
        }
        dist = Distance.pointToSegment(q1, p1, p2);
        if (dist < minDist) {
            minDist = dist;
            nearestPt = q1;
        }
        dist = Distance.pointToSegment(q2, p1, p2);
        if (dist < minDist) {
            minDist = dist;
            nearestPt = q2;
        }
        return nearestPt;
    }

    /**
     * Tests whether a point lies in the envelopes of both input segments.
     * A correctly computed intersection point should return <code>true</code>
     * for this test.
     * Since this test is for debugging purposes only, no attempt is
     * made to optimize the envelope test.
     *
     * @return <code>true</code> if the input point lies within both input segment envelopes
     */
    private boolean isInSegmentEnvelopes(Coordinate intPt)
    {
        Envelope env0 = new Envelope(inputLines[0][0], inputLines[0][1]);
        Envelope env1 = new Envelope(inputLines[1][0], inputLines[1][1]);
        return env0.contains(intPt) && env1.contains(intPt);
    }

    private Coordinate intersectionWithNormalization(
            Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
    {
        Coordinate n1 = new Coordinate(p1);
        Coordinate n2 = new Coordinate(p2);
        Coordinate n3 = new Coordinate(q1);
        Coordinate n4 = new Coordinate(q2);
        Coordinate normPt = new Coordinate();
        normalizeToEnvCentre(n1, n2, n3, n4, normPt);

        Coordinate intPt = safeHCoordinateIntersection(n1, n2, n3, n4);

        intPt.x += normPt.x;
        intPt.y += normPt.y;

        intPt.setZ((CoordinateUtils.interpolate(p1, p2, intPt)+CoordinateUtils.interpolate(q1, q2, intPt))/2);

        return intPt;
    }

    /**
     * Normalize the supplied coordinates to
     * so that the midpoint of their intersection envelope
     * lies at the origin.
     *
     * @param n00
     * @param n01
     * @param n10
     * @param n11
     * @param normPt
     */
    private void normalizeToEnvCentre(
            Coordinate n00,
            Coordinate n01,
            Coordinate n10,
            Coordinate n11,
            Coordinate normPt)
    {
        double minX0 = n00.x < n01.x ? n00.x : n01.x;
        double minY0 = n00.y < n01.y ? n00.y : n01.y;
        double maxX0 = n00.x > n01.x ? n00.x : n01.x;
        double maxY0 = n00.y > n01.y ? n00.y : n01.y;

        double minX1 = n10.x < n11.x ? n10.x : n11.x;
        double minY1 = n10.y < n11.y ? n10.y : n11.y;
        double maxX1 = n10.x > n11.x ? n10.x : n11.x;
        double maxY1 = n10.y > n11.y ? n10.y : n11.y;

        double intMinX = minX0 > minX1 ? minX0 : minX1;
        double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
        double intMinY = minY0 > minY1 ? minY0 : minY1;
        double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

        double intMidX = (intMinX + intMaxX) / 2.0;
        double intMidY = (intMinY + intMaxY) / 2.0;
        normPt.x = intMidX;
        normPt.y = intMidY;

            /*
            // equilavalent code using more modular but slower method
            Envelope env0 = new Envelope(n00, n01);
            Envelope env1 = new Envelope(n10, n11);
            Envelope intEnv = env0.intersection(env1);
            Coordinate intMidPt = intEnv.centre();

            normPt.x = intMidPt.x;
            normPt.y = intMidPt.y;
            */

        n00.x -= normPt.x;    n00.y -= normPt.y;
        n01.x -= normPt.x;    n01.y -= normPt.y;
        n10.x -= normPt.x;    n10.y -= normPt.y;
        n11.x -= normPt.x;    n11.y -= normPt.y;
    }

    /**
     * Computes a segment intersection using homogeneous coordinates.
     * Round-off error can cause the raw computation to fail,
     * (usually due to the segments being approximately parallel).
     * If this happens, a reasonable approximation is computed instead.
     *
     * @param p1 a segment endpoint
     * @param p2 a segment endpoint
     * @param q1 a segment endpoint
     * @param q2 a segment endpoint
     * @return the computed intersection point
     */
    private Coordinate safeHCoordinateIntersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
    {
        Coordinate intPt = Intersection.intersection(p1, p2, q1, q2);
            //    	System.out.println("Not calculable: " + this);
            // compute an approximate result
            //      intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);
        if(intPt == null) {
            intPt = nearestEndpoint(p1, p2, q1, q2);
        }
            //     System.out.println("Snapped to " + intPt);
        return intPt;
    }

    private int computeCollinearIntersection(Coordinate p1, Coordinate p2,
                                             Coordinate q1, Coordinate q2) {
        boolean p1q1p2 = Envelope.intersects(p1, p2, q1);
        boolean p1q2p2 = Envelope.intersects(p1, p2, q2);
        boolean q1p1q2 = Envelope.intersects(q1, q2, p1);
        boolean q1p2q2 = Envelope.intersects(q1, q2, p2);

        if (p1q1p2 && p1q2p2) {
            intPt[0] = q1;
            intPt[1] = q2;
            return COLLINEAR_INTERSECTION;
        }
        if (q1p1q2 && q1p2q2) {
            intPt[0] = p1;
            intPt[1] = p2;
            return COLLINEAR_INTERSECTION;
        }
        if (p1q1p2 && q1p1q2) {
            intPt[0] = q1;
            intPt[1] = p1;
            return q1.equals(p1) && !p1q2p2 && !q1p2q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
        }
        if (p1q1p2 && q1p2q2) {
            intPt[0] = q1;
            intPt[1] = p2;
            return q1.equals(p2) && !p1q2p2 && !q1p1q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
        }
        if (p1q2p2 && q1p1q2) {
            intPt[0] = q2;
            intPt[1] = p1;
            return q2.equals(p1) && !p1q1p2 && !q1p2q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
        }
        if (p1q2p2 && q1p2q2) {
            intPt[0] = q2;
            intPt[1] = p2;
            return q2.equals(p2) && !p1q1p2 && !q1p1q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
        }
        return NO_INTERSECTION;
    }
}