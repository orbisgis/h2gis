package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Adam Gouge
 */
public class CoordinateUtilsTest {

    @Test
    public void testContainsCoordsOfMixedDimension() {
        assertFalse(CoordinateUtils.containsCoordsOfMixedDimension(new Coordinate[]{}));
        assertFalse(CoordinateUtils.containsCoordsOfMixedDimension(
                new Coordinate[]{new Coordinate(0.0, 1.0)}));
        assertFalse(CoordinateUtils.containsCoordsOfMixedDimension(
                new Coordinate[]{new Coordinate(0.0, 1.0, 2.0)}));
        assertTrue(CoordinateUtils.containsCoordsOfMixedDimension(
                new Coordinate[]{
                        new Coordinate(0.0, 1.0),
                        new Coordinate(0.0, 1.0, 2.0)}
        ));
        assertFalse(CoordinateUtils.containsCoordsOfMixedDimension(
                new Coordinate[]{
                        new Coordinate(0.0, 1.0),
                        new Coordinate(0.0, 1.0)}
        ));
        assertFalse(CoordinateUtils.containsCoordsOfMixedDimension(
                new Coordinate[]{
                        new Coordinate(0.0, 1.0, 2.0),
                        new Coordinate(0.0, 1.0, 2.0)}
        ));
    }
}
