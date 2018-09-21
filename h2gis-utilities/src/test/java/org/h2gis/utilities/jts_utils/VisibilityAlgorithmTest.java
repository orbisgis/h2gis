package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.math.Vector2D;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class VisibilityAlgorithmTest {

    @Test
    public void testIsoVistEmpty() {
        VisibilityAlgorithm c = new VisibilityAlgorithm(50);

        Polygon poly = c.getIsoVist(new Coordinate(0, 0));

        assertEquals(100, poly.getNumPoints());
    }

}