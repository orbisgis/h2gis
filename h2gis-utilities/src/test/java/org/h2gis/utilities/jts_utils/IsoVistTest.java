package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;
import org.junit.Test;

import static org.junit.Assert.*;

public class IsoVistTest {

    @Test
    public void InterpolateTest() {
        Coordinate origin = new Coordinate(10, 10);
        LineSegment segment = new LineSegment(new Coordinate(14, 6), new Coordinate(14,14));
        Coordinate projPoint = segment.closestPoint(origin);
        Vector2D v1 = new Vector2D(origin, segment.p0);
        Vector2D v2 = new Vector2D(origin, segment.p1);

        IsoVist.Limit limit = new IsoVist.Limit(v1.angle(), v1.length(), v2.angle(), v2.length());
        assertEquals(projPoint.distance(origin), limit.interpolate(new Vector2D(origin, projPoint).angle()), 1e-6);
    }

}