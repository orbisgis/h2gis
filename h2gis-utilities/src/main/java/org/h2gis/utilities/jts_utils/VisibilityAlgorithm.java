package org.h2gis.utilities.jts_utils;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.math.Vector2D;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class compute an IsoVist from a coordinate and a set of segments
 */
public class VisibilityAlgorithm {
    private static final double PI_DIV2 = Math.PI / 2.;
    // maintain the list of limits sorted by angle
    private TreeSet<Limit> limits = new TreeSet<>(new LimitComparator());
    private double maxDistance;
    private Coordinate viewPoint;
    private Vector2D viewVector;
    private double epsilon = 1e-6;

    public VisibilityAlgorithm(double maxDistance, Coordinate viewPoint) {
        this.maxDistance = maxDistance;
        this.viewPoint = viewPoint;
        this.viewVector = Vector2D.create(viewPoint);
    }

    public void addSegment(Coordinate p0, Coordinate p1) {
        if(p0.distance(p1) < epsilon || p0.distance(viewPoint) < epsilon || p1.distance(viewPoint) < epsilon) {
            return;
        }
        // Before adding a segment into the limits
        // The segment must be cut on the following conditions
        // The segment is crossing the angle pi (we will start from -pi looking for the first limit start)
        // The segment cross other limits
        Vector2D v1 = Vector2D.create(viewPoint, p0);
        Vector2D v2 = Vector2D.create(viewPoint, p1);
        double angle1 = v1.angle();
        double angle2 = v2.angle();
        if(angle2 < angle1) {
            // Reverse points
            addSegment(p1, p0);
        } else {
            Limit insertedLimit = new Limit(angle1, v1.length(), angle2, v2.length());
            // Check if segment cross Pi -Pi
            if(angle2 - angle1 > Math.PI) {
                // Split into two segments
                double distance = insertedLimit.interpolate(Math.PI);
                addSegment(new Coordinate(viewPoint.x - distance, viewPoint.y - epsilon), p0);
                addSegment(p1, new Coordinate(viewPoint.x - distance, viewPoint.y + epsilon));
            } else {
                // Now check if this segment is crossing existing limits
                Set<Limit> limitToRemove = new HashSet<>();
                Set<Limit> limitToInsert = new HashSet<>();
                // Todo Get all limits within the bounds of start angle->end angle
                // limits.subSet(insertedLimit, new Limit(insertedLimit.angleEnd, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE))
                for (Limit limit : limits) {
                    if (insertedLimit.angleEnd < limit.angleStart) {
                        break;
                    } else {
                        Double intersectionAngle = insertedLimit.getIntersectionAngle(limit);
                        if (intersectionAngle != null && intersectionAngle > insertedLimit.angleStart && intersectionAngle < insertedLimit.angleEnd) {
                            // Cut the two limits
                            Limit otherLimitBegin = new Limit(limit.angleStart, limit.distanceStart, intersectionAngle, limit.interpolate(intersectionAngle));
                            Limit otherLimitEnd = new Limit(otherLimitBegin.angleEnd, otherLimitBegin.distanceEnd, limit.angleEnd, limit.distanceEnd);
                            Limit newLimitBegin = new Limit(insertedLimit.angleStart, insertedLimit.distanceStart, otherLimitBegin.angleEnd, otherLimitBegin.distanceEnd);
                            insertedLimit = new Limit(otherLimitBegin.angleEnd, otherLimitBegin.distanceEnd, insertedLimit.angleEnd, insertedLimit.distanceEnd);
                            limitToRemove.add(limit);
                            limitToInsert.add(otherLimitBegin);
                            limitToInsert.add(otherLimitEnd);
                            limitToInsert.add(newLimitBegin);
                        }
                    }
                }
                limits.removeAll(limitToRemove);
                limits.addAll(limitToInsert);
                limits.add(insertedLimit);
            }
        }
    }

    public SortedSet<Limit> getLimits() {
        return Collections.unmodifiableSortedSet(limits);
    }

    public Polygon GetIsoVist() {
        // Fill holes using max distance

        // Construct polygon
        return null;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void addLineString(LineString lineString) {
        int nPoint = lineString.getNumPoints();
        for(int idPoint = 0; idPoint < nPoint - 1; idPoint++) {
            addSegment(lineString.getCoordinateN(idPoint), lineString.getCoordinateN(idPoint+1));
        }
    }

    public void addGeometry(Geometry geometry) {
        if(geometry instanceof GeometryCollection) {
            int geoCount = geometry.getNumGeometries();
            for(int n = 0; n < geoCount; n++) {
                Geometry simpleGeom = geometry.getGeometryN(n);
                if(simpleGeom instanceof LineString) {
                    addLineString((LineString)simpleGeom);
                } else if(simpleGeom instanceof Polygon) {
                    Polygon poly = ((Polygon) simpleGeom);
                    addLineString(poly.getExteriorRing());
                    final int ringCount = poly.getNumInteriorRing();
                    // Keep interior ring if the viewpoint is inside the polygon
                    for(int nr = 0; nr < ringCount; nr++) {
                        addLineString(poly.getInteriorRingN(nr));
                    }
                }
            }
        }
    }

    /**
     * Define a visualisation stop (segment)
     */
    public static final class Limit {
        public final double angleStart; // angle in radians
        public final double distanceStart; // distance in local coordinate system from viewPoint to the start corner
        public final double angleEnd;
        public final double distanceEnd;

        public Limit(double angleStart, double distanceStart, double angleEnd, double distanceEnd) {
            this.angleStart = angleStart;
            this.distanceStart = distanceStart;
            this.angleEnd = angleEnd;
            this.distanceEnd = distanceEnd;
        }

        /**
         * Create a segment with the local coordinate system using viewPoint as center of the unit circle
         * @param viewPoint
         * @return
         */
        public LineSegment createSegment(Vector2D viewPoint) {
            return new LineSegment(viewPoint.add(Vector2D.create(Math.cos(angleStart),
                    Math.sin(angleStart)).multiply(distanceStart)).toCoordinate(),
                    viewPoint.add(Vector2D.create(Math.cos(angleEnd),
                            Math.sin(angleEnd)).multiply(distanceEnd)).toCoordinate());
        }

        /**
         * Interpolate distance for an angle
         * @param angle Angle in radians
         * @return Distance of a point projected to this segment
         */
        double interpolate(double angle) {
            // https://en.wikipedia.org/wiki/Solution_of_triangles#Solving_plane_triangles
            // https://en.wikipedia.org/wiki/Law_of_cosines
            // https://en.wikipedia.org/wiki/Law_of_sines
            final double phi1 = angle - angleStart;
            final double phi2 = getStartPointAngle();
            return (distanceStart * Math.sin(phi2))/(Math.sin(phi1 + phi2));
        }

        /**
         * @param other Other limit
         * @return The angle of the intersection point with other limit. May be outside of limit angles.
         */
        Double getIntersectionAngle(Limit other) {
            // TODO use simple approach (do no transform into linesegment)
            Vector2D viewPoint = new Vector2D();
            LineSegment s1 = other.createSegment(viewPoint);
            LineSegment s2 = createSegment(viewPoint);
            Coordinate i = s1.lineIntersection(s2);
            if(i != null) {
                return new Vector2D(new Coordinate(), i).angle();
            } else {
                return null;
            }
        }

        /**
         * @return start point angle (in the triangle viewPoint/p0/p1
         */
        double getStartPointAngle() {
            final double gamma = angleEnd - angleStart;
            return PI_DIV2 - gamma / 2. + - Math.atan(((distanceStart - distanceEnd)/
                    (distanceEnd + distanceStart)) * (1.0 / Math.tan(gamma / 2.)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Limit limit = (Limit) o;
            return Double.compare(limit.angleStart, angleStart) == 0 &&
                    Double.compare(limit.distanceStart, distanceStart) == 0 &&
                    Double.compare(limit.angleEnd, angleEnd) == 0 &&
                    Double.compare(limit.distanceEnd, distanceEnd) == 0;
        }

        @Override
        public int hashCode() {

            return Objects.hash(angleStart, distanceStart, angleEnd, distanceEnd);
        }
    }

    public static final class LimitComparator implements Comparator<Limit> {
        @Override
        public int compare(Limit o1, Limit o2) {
            int res = Double.compare(o1.angleStart, o2.angleStart);
            if(res != 0) {
                return res;
            }
            res = Double.compare(o1.distanceStart, o2.distanceStart);
            if(res != 0) {
                return res;
            }
            res = Double.compare(o1.angleEnd, o2.angleEnd);
            if(res != 0) {
                return res;
            }
            return Double.compare(o1.distanceEnd, o2.distanceEnd);
        }
    }
}
