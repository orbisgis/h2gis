/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    assertFalse(isoVist.contains(poly.getGeometryN(0).getCentroid()));
    assertFalse(isoVist.contains(poly.getGeometryN(1).getCentroid()));
  }

  @Test
  public void testIsoVistIntersections() throws ParseException {
    WKTReader wktReader = new WKTReader();

    VisibilityAlgorithm c = new VisibilityAlgorithm(10);
    Geometry poly = wktReader.read("MULTIPOLYGON(((1 2, 3 2, 2 3, 1 2)),((2 4, 5 2, 5 5, 2 4)),((1 1 0, 4 1 0, 4 4 5, 1 1 0)))");
    c.addGeometry(poly);
    Polygon isoVist = c.getIsoVist(new Coordinate(2.5, 3), true);
    assertFalse(isoVist.contains(poly.getGeometryN(0).getCentroid()));
    assertFalse(isoVist.contains(poly.getGeometryN(1).getCentroid()));
    assertFalse(isoVist.contains(poly.getGeometryN(2).getCentroid()));

    // Put view point inside geom 2 part

    isoVist = c.getIsoVist(new Coordinate(1.8, 2.4), true);

    assertEquals(wktReader.read("POLYGON ((2.5 2.5, 2 3, 1 2, 2 2, 2.5 2.5))"), isoVist);
  }

  @Test
  public void testPrecisionIssue() throws ParseException {
    WKTReader wktReader = new WKTReader();

    VisibilityAlgorithm c = new VisibilityAlgorithm(1000);
    Geometry poly = wktReader.read("MULTIPOLYGON (((609778.8 5227863.7 0, 609745.7 5227865.9 0, 609748.5 5227906.1 0, 609781.6 5227903.9 0, 609779.9 5227879.6 0, 609782.2 5227879.5 0, 609784.7 5227914.7 0, 609826.5 5227911.7 0, 609824.4 5227882.3 0, 609820.6 5227875.9 0, 609817 5227876.1 0, 609817 5227875.8 0, 609810.2 5227876.2 0, 609809.3 5227861.4 0, 609807.7 5227861.3 0, 609807.6 5227859.4 0, 609805.7 5227859.6 0, 609805.5 5227857.1 0, 609796.5 5227857.7 0, 609796.7 5227860.1 0, 609798.4 5227860 0, 609798.5 5227862 0, 609793.6 5227862.1 0, 609793.4 5227857.9 0, 609778.4 5227858.9 0, 609778.8 5227863.7 0)), ((609820.8 5227867.3 0, 609823.7 5227866.2 0, 609813.3 5227836.7 0, 609805.1 5227816.1 0, 609800.9 5227817.8 0, 609805.4 5227829 0, 609804.7 5227829.3 0, 609816.4 5227861 0, 609820.8 5227867.3 0)), ((609799.5 5227805.3 0, 609797.3 5227806.2 0, 609799.6 5227811.7 0, 609801.9 5227810.9 0, 609799.5 5227805.3 0)), ((609796.6 5227794.9 0, 609792.2 5227797.1 0, 609793.2 5227800 0, 609797.9 5227798 0, 609796.6 5227794.9 0)), ((609776.7 5227834.2 0, 609776.7 5227834.5 0, 609791.8 5227833.4 0, 609790.3 5227812.2 0, 609775.2 5227813.2 0, 609776.4 5227829.8 0, 609771.3 5227830.2 0, 609771.5 5227832.6 0, 609770.5 5227832.8 0, 609770.6 5227834.5 0, 609776.7 5227834.2 0)), ((609758.4 5227763.4 0, 609757.2 5227746.7 0, 609748.3 5227747.1 0, 609747.1 5227732.3 0, 609730.6 5227733.4 0, 609729.5 5227718.5 0, 609727.6 5227718.5 0, 609727.4 5227712.6 0, 609700.6 5227714.3 0, 609701 5227720.4 0, 609690.3 5227721.1 0, 609677.4 5227752.1 0, 609669.2 5227775.6 0, 609670.8 5227799.2 0, 609760.7 5227791.9 0, 609760.6 5227791.4 0, 609775.1 5227790 0, 609774.1 5227776.6 0, 609770.6 5227776.9 0, 609769.5 5227762.5 0, 609758.4 5227763.4 0)), ((609763.2 5227799.5 0, 609750 5227800.6 0, 609750.8 5227810.3 0, 609764.1 5227809.2 0, 609763.2 5227799.5 0)), ((609728 5227818.5 0, 609727.8 5227815.4 0, 609717.3 5227816.1 0, 609717.4 5227819.2 0, 609707.3 5227819.9 0, 609710.2 5227868.5 0, 609737.6 5227866.5 0, 609736.8 5227854.8 0, 609747 5227853.9 0, 609744.5 5227817.4 0, 609728 5227818.5 0)), ((609697 5227854.2 0, 609690.6 5227854.6 0, 609693.7 5227897.6 0, 609699.7 5227897.2 0, 609697 5227854.2 0)), ((609694.9 5227826 0, 609682.9 5227826.8 0, 609682.5 5227822.1 0, 609680.9 5227822.2 0, 609680.3 5227817.5 0, 609674 5227817.9 0, 609674.2 5227822.4 0, 609669.9 5227822.8 0, 609670.3 5227827.6 0, 609664.3 5227828 0, 609664.7 5227835.2 0, 609660.8 5227835.6 0, 609659.1 5227841 0, 609660 5227853.6 0, 609656.8 5227853.9 0, 609657.4 5227860.4 0, 609669.4 5227859.3 0, 609669 5227852.8 0, 609696.8 5227851.1 0, 609694.9 5227826 0)), ((609686.9 5227806.6 0, 609661.8 5227808.8 0, 609662 5227811.8 0, 609687.1 5227809.7 0, 609686.9 5227806.6 0)), ((609624.3 5227691 0, 609626.5 5227684 0, 609598.1 5227674.9 0, 609573 5227752.8 0, 609561.4 5227749 0, 609534.9 5227828.8 0, 609529.4 5227827 0, 609517.9 5227862 0, 609616.7 5227895 0, 609677.1 5227709.1 0, 609647.2 5227698.7 0, 609641.1 5227701.7 0, 609640.7 5227703.7 0, 609639 5227703.1 0, 609636.4 5227704.8 0, 609634.3 5227711.2 0, 609619.4 5227706.3 0, 609624.3 5227691 0)), ((609649.3 5227811.8 0, 609643.4 5227830 0, 609654.5 5227833.8 0, 609660.7 5227815.6 0, 609649.3 5227811.8 0)))");
    c.addGeometry(poly);
    Polygon isoVist = c.getIsoVist(new Coordinate(609844.1, 5227908.1), true);

    assertFalse(isoVist.contains(wktReader.read("POINT (609783.6567353908 5227823.552135403)")));
  }
}