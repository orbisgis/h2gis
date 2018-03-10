package org.h2gis.utilities.jts_utils;


import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.math.Vector2D;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * This class compute an IsoVist from a coordinate and a set of segments
 */
public class IsoVist {
    // maintain the list of limits sorted by angle
    private TreeSet<Limit> limits = new TreeSet<>(new LimitComparator());
    private double maxDistance;
    private Coordinate viewPoint;
    private Vector2D viewVector;
    private double epsilon = 1e-6;

    public IsoVist(double maxDistance, Coordinate viewPoint) {
        this.maxDistance = maxDistance;
        this.viewPoint = viewPoint;
        this.viewVector = Vector2D.create(viewPoint);
    }

    public void addSegment(Coordinate p0, Coordinate p1) {
        if(p0.distance(p1) < epsilon) {
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
            // Check if segment cross Pi -Pi
            if(angle2 - angle1 > Math.PI) {
                // Split into two segments
                double distance = CGAlgorithms.distancePointLine(viewPoint, p0, p1);
                Coordinate p = viewVector.add(Vector2D.create(-distance, 0)).toCoordinate();
                addSegment(p, p0);
                addSegment(p1, p);
            } else {
                // Now check if this segment is crossing existing limits
                
            }
        }
    }

    public Polygon GetIsoVist() {
        // Fill holes using max distance

        // Construct polygon

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

    }

    public static final class LimitComparator implements Comparator<Limit> {
        @Override
        public int compare(Limit o1, Limit o2) {
            return Double.compare(o1.angleStart, o2.angleStart);
        }
    }
}
