package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.math.Vector2D;
import org.junit.Test;

import static org.junit.Assert.*;

public class IsoVistTest {

    @Test
    public void InterpolateTest1() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(14, 6), new Coordinate(14,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        IsoVist.Limit limit = new IsoVist.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }

    @Test
    public void InterpolateTest2() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(15, 6), new Coordinate(14,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        IsoVist.Limit limit = new IsoVist.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }


    @Test
    public void InterpolateTest3() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(14, 6), new Coordinate(15,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        IsoVist.Limit limit = new IsoVist.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }


    @Test
    public void InterpolateTest4() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(5, 12), new Coordinate(12,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        IsoVist.Limit limit = new IsoVist.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }


    /**
     * Check with angle out of Limit angles
     */
    @Test
    public void InterpolateTestOut1() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(14, 12), new Coordinate(14,15));
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        IsoVist.Limit limit = new IsoVist.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(4, limit.interpolate(0), 1e-6);
    }
}