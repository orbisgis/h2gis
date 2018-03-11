package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.math.Vector2D;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class VisibilityAlgorithmTest {

    @Test
    public void InterpolateTest1() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(14, 6), new Coordinate(14,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        VisibilityAlgorithm.Limit limit = new VisibilityAlgorithm.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }

    @Test
    public void InterpolateTest2() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(15, 6), new Coordinate(14,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        VisibilityAlgorithm.Limit limit = new VisibilityAlgorithm.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }


    @Test
    public void InterpolateTest3() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(14, 6), new Coordinate(15,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        VisibilityAlgorithm.Limit limit = new VisibilityAlgorithm.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }


    @Test
    public void InterpolateTest4() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(5, 12), new Coordinate(12,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        VisibilityAlgorithm.Limit limit = new VisibilityAlgorithm.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
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

        VisibilityAlgorithm.Limit limit = new VisibilityAlgorithm.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(4, limit.interpolate(0), 1e-6);
    }

    @Test
    public void testIsoVist1() {
        VisibilityAlgorithm c = new VisibilityAlgorithm(50, new Coordinate(0, 0));

        c.addSegment(new Coordinate(1, 2), new Coordinate(1, 4));
        c.addSegment(new Coordinate(3, 1), new Coordinate(-1, 5));

        Set<VisibilityAlgorithm.Limit> limits = c.getLimits();

        assertEquals(4, limits.size());
    }

    @Test
    public void testIsoVist2() {
        VisibilityAlgorithm c = new VisibilityAlgorithm(50, new Coordinate(0, 0));

        c.addSegment(new Coordinate(1, 3), new Coordinate(1, 5));
        c.addSegment(new Coordinate(1, 5), new Coordinate(3, 5));

        List<LineSegment> limits = new ArrayList<>();
        for(VisibilityAlgorithm.Limit limit : c.getLimits()) {
            limits.add(limit.createSegment(new Vector2D()));
        }
        assertEquals(2, limits.size());

        assertEquals(limits.get(0).toString(),0 ,limits.get(0).p0.distance(new Coordinate(3,5)), 1e-6);
        assertEquals(limits.get(0).toString(),0 ,limits.get(0).p1.distance(new Coordinate(1 , 5)), 1e-6);

        assertEquals(limits.get(1).toString(), 0 ,limits.get(1).p0.distance(new Coordinate(1.0, 3.0)), 1e-6);
        assertEquals(limits.get(1).toString(),0 ,limits.get(1).p1.distance(new Coordinate(1, 5)), 1e-6);

    }

    @Test
    public void testIsoVist3() {
        VisibilityAlgorithm c = new VisibilityAlgorithm(50, new Coordinate(0, 0));

        c.addSegment(new Coordinate(1, 3), new Coordinate(1, 5));
        c.addSegment(new Coordinate(1, 5), new Coordinate(3, 5));
        c.addSegment(new Coordinate(0, 3), new Coordinate(4, 6));

        List<LineSegment> limits = new ArrayList<>();
        for(VisibilityAlgorithm.Limit limit : c.getLimits()) {
            limits.add(limit.createSegment(new Vector2D()));
        }

        assertEquals(7, limits.size());

        assertEquals(limits.get(0).toString(),0 ,limits.get(0).p0.distance(new Coordinate(4,6)), 1e-6);
        assertEquals(limits.get(0).toString(),0 ,limits.get(0).p1.distance(new Coordinate(2.666666666666668 , 5)), 1e-6);

        assertEquals(limits.get(1).toString(), 0 ,limits.get(1).p0.distance(new Coordinate(3.0, 5.0)), 1e-6);
        assertEquals(limits.get(1).toString(),0 ,limits.get(1).p1.distance(new Coordinate(2.666666666666668, 5)), 1e-6);

        assertEquals(limits.get(2).toString(), 0 ,limits.get(2).p0.distance(new Coordinate(2.666666666666668, 5.0)), 1e-6);
        assertEquals(limits.get(2).toString(),0 ,limits.get(2).p1.distance(new Coordinate(1, 3.75)), 1e-6);

        assertEquals(limits.get(3).toString(), 0 ,limits.get(3).p0.distance(new Coordinate(2.666666666666668, 5.0)), 1e-6);
        assertEquals(limits.get(3).toString(),0 ,limits.get(3).p1.distance(new Coordinate(1, 5)), 1e-6);

        assertEquals(limits.get(4).toString(), 0 ,limits.get(4).p0.distance(new Coordinate(1, 3.0)), 1e-6);
        assertEquals(limits.get(4).toString(),0 ,limits.get(4).p1.distance(new Coordinate(1, 3.75)), 1e-6);

        assertEquals(limits.get(5).toString(), 0 ,limits.get(5).p0.distance(new Coordinate(1, 3.75)), 1e-6);
        assertEquals(limits.get(5).toString(),0 ,limits.get(5).p1.distance(new Coordinate(1, 5)), 1e-6);

        assertEquals(limits.get(6).toString(), 0 ,limits.get(6).p0.distance(new Coordinate(1, 3.75)), 1e-6);
        assertEquals(limits.get(6).toString(),0 ,limits.get(6).p1.distance(new Coordinate(0, 3)), 1e-6);

    }

    @Test
    public void testIsoVistCrossPi() {
        VisibilityAlgorithm c = new VisibilityAlgorithm(50, new Coordinate(0, 0));

        c.addSegment(new Coordinate(-2, -1), new Coordinate(3, 4));

        List<LineSegment> limits = new ArrayList<>();
        for(VisibilityAlgorithm.Limit limit : c.getLimits()) {
            limits.add(limit.createSegment(new Vector2D()));
        }

        assertEquals(2, limits.size());

        assertEquals(limits.get(0).toString(),0 ,limits.get(0).p0.distance(new Coordinate(-1,0)), 1e-6);
        assertEquals(limits.get(0).toString(),0 ,limits.get(0).p1.distance(new Coordinate(-2 , -1)), 1e-6);

        assertEquals(limits.get(1).toString(), 0 ,limits.get(1).p0.distance(new Coordinate(3, 4)), 1e-6);
        assertEquals(limits.get(1).toString(),0 ,limits.get(1).p1.distance(new Coordinate(-1, 0)), 1e-6);

    }
}