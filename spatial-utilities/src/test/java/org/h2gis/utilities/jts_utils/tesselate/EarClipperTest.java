/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.utilities.jts_utils.tesselate;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Nicolas Fortin
 */
public class EarClipperTest {
    private WKTReader wktReader = new WKTReader();

    @Test
    public void testSimplePolygon() throws Exception {
        Polygon simplePoly = (Polygon)wktReader.read("POLYGON ((247718 2575233, 246822 2577397, 248613 2579169, 251206 2578031," +
                " 251579 2575681, 249564 2574188, 247718 2575233))");
        EarClipper earClipper = new EarClipper(simplePoly);
        MultiPolygon geometry = earClipper.getResult(true);

        // Check min quality
        assertEquals(0.65, getMinQuality(geometry), 0.01);
    }

    private static double getMinQuality(MultiPolygon multiPolygon) {
        double minQuality = 1;
        for(int idgeom = 0; idgeom < multiPolygon.getNumGeometries(); idgeom++) {
            Coordinate[] coords = multiPolygon.getGeometryN(idgeom).getCoordinates();
            Triangle tri = new Triangle(coords[0], coords[1], coords[2]);
            minQuality = Math.min(EdgeFlipper.evaluateQuality(tri), minQuality);
        }
        return minQuality;
    }

    @Test
    public void testPolygonWithHoles() throws Exception {
        String geom = "POLYGON ((252331 2573553, 249426 2574162, 247598 2577778, 249731 2576823, 249629 2578875, " +
                "250543 2579363, 250604 2580582, 249304 2580256, 250239 2581252, 252798 2580886, 251620 2580317," +
                " 253570 2580703, 255277 2579241, 254180 2579017, 256678 2577575, 255521 2577189, 256699 2576539," +
                " 255378 2576133, 254749 2572903, 253652 2575828, 252412 2575259, 252331 2573553), (250686 2574792," +
                " 249406 2575848, 251051 2576783, 251539 2575747, 250686 2574792), (250909 2579180, 250868 2580764," +
                " 251803 2579586, 250909 2579180), (253448 2576295, 252494 2578692, 255053 2577778, 255074 2576803," +
                " 254424 2576092, 253448 2576295))";
        Polygon inputPoly = (Polygon)wktReader.read(geom);
        EarClipper earClipper = new EarClipper(inputPoly);
        MultiPolygon geometry = earClipper.getResult(true);

        // Check min quality
        assertEquals(0.31, getMinQuality(geometry), 0.01);
    }
}
