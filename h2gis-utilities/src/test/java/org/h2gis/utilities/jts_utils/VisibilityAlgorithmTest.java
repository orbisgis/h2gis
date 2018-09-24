package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.util.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class VisibilityAlgorithmTest {

  /**
   * Test without geometries
   */
  @Test
  public void testIsoVistEmpty() {
    VisibilityAlgorithm c = new VisibilityAlgorithm(50);

    Polygon poly = c.getIsoVist(new Coordinate(0, 0), true);

    assertEquals(33, poly.getNumPoints());
  }

  /**
   * Test with geometry crossing 0 coordinates
   *
   * @throws ParseException
   */
  @Test
  public void testIsoVistCross0() throws ParseException {
    WKTReader wktReader = new WKTReader();

    VisibilityAlgorithm c = new VisibilityAlgorithm(50);

    Geometry bound = wktReader.read("POLYGON ((-100 -100, -100 100, 100 100, 100 -100, -100 -100))");
    c.addGeometry(bound);
    Geometry poly2 = wktReader.read("POLYGON ((-31 -9.8, -32 10.2, -12.1 10.5, -12 -9.6, -31 -9.8))");
    c.addGeometry(poly2);

    Polygon isoVist = c.getIsoVist(new Coordinate(0, 0), false);

    assertFalse(isoVist.contains(poly2.getCentroid()));
  }

  /**
   * Test with geometry with only positive values
   *
   * @throws ParseException
   */
  @Test
  public void testIsoVistNoCross() throws ParseException {
    WKTReader wktReader = new WKTReader();

    VisibilityAlgorithm c = new VisibilityAlgorithm(50);

    Geometry bound = wktReader.read("POLYGON ((0 0, 0 200, 200 200, 200 0, 0 0))");
    c.addGeometry(bound);
    Geometry poly2 = wktReader.read("POLYGON ((69 90.2, 68 110.2, 87.9 110.5, 88 90.4, 69 90.2))");
    c.addGeometry(poly2);

    Polygon isoVist = c.getIsoVist(new Coordinate(100, 100), false);

    assertFalse(isoVist.contains(poly2.getCentroid()));
  }

  @Test
  public void testIsoVistMultiplePoly() throws ParseException {
    WKTReader wktReader = new WKTReader();

    VisibilityAlgorithm c = new VisibilityAlgorithm(10);
    Geometry poly = wktReader.read("MULTIPOLYGON (((1 1, 1 3, 2 4, 3 3, 3 2, 2 2, 1 1)),((3 5, 4 3, 4 2, 3 1, 5 1, 5 4, 3 5)))");
    c.addGeometry(poly);
    Polygon isoVist = c.getIsoVist(new Coordinate(3.5, 2.5), true);

    System.out.println(isoVist.toText());

  }
}