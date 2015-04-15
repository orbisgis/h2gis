package org.h2gis.utilities.jts_utils.tesselate;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Nicolas Fortin
 */
public class EarClipperTest {

    public void testSimplePolygon() {

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
        WKTReader wktReader = new WKTReader();
        EarClipper earClipper = new EarClipper((Polygon)wktReader.read(geom));
        Geometry geometry = earClipper.getResult(true);
        // Check min quality

    }
}
