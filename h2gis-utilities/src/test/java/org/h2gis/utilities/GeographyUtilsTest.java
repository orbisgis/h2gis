/*
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
package org.h2gis.utilities;

import org.h2gis.utilities.jts_utils.GeographyUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Unit test of URI utilities
 *
 * @author Erwan Bocher
 */
public class GeographyUtilsTest {

    @Test
    public void expandEnvelopeByMeters1() throws Exception {
        Envelope env = new Envelope(0.0, 2.0, 1.0, 2.0);
        assertEquals(env, GeographyUtils.expandEnvelopeByMeters(env, 0));
    }

    @Test
    public void expandEnvelopeByMeters2() throws Exception {
        Envelope env = new Envelope(0.0, 2.0, 1.0, 2.0);
        Envelope expandedEnv = GeographyUtils.expandEnvelopeByMeters(env, 1000);
        assertEquals(1000, GeographyUtils.getHaversineDistanceInMeters(new Coordinate(env.getMinX(), env.getMinY()),
                new Coordinate(expandedEnv.getMinX(), env.getMinY())), 1, "Expanded to 1000");
    }

    @Test
    public void expandEnvelopeByMeters3() throws Exception {
        Envelope env = new Envelope(22220.0, 2.0, 1.0, 2.0);
        assertThrows(IllegalArgumentException.class, () -> {
            assertEquals(env, GeographyUtils.expandEnvelopeByMeters(env, 1000));
        });
    }

    @Test
    public void expandEnvelopeByMeters4() throws Exception {
        Envelope env = new Envelope(0.0, 2.0, 1.0, 2.0);
        assertThrows(IllegalArgumentException.class, () -> {
            assertEquals(env, GeographyUtils.expandEnvelopeByMeters(env, -1));
        });
    }

    @Test
    public void expandEnvelopeByMeters5() throws Exception {
        Envelope env = new Envelope(0.0, .0, 0.0, 0.0);
        Envelope expandedEnv = GeographyUtils.expandEnvelopeByMeters(env, 200);
        assertEquals(200, GeographyUtils.getHaversineDistanceInMeters(new Coordinate(env.getMinX(), env.getMinY()),
                new Coordinate(expandedEnv.getMinX(), env.getMinY())), 1, "Expanded to 200");
    }
    
    @Test
    //https://andrew.hedges.name/experiments/haversine/
    public void haversineDistanceInMeters1() throws Exception {
        Coordinate coorda = new Coordinate(-77.037852, 38.898556);
        Coordinate coordb = new Coordinate(-77.043934, 38.897147);
        assertEquals(549, GeographyUtils.getHaversineDistanceInMeters(coorda, coordb), 1, "HaversineDistance");
    }

    /**
     * Util to display the envelope as geometry
     *
     * @param env
     */
    public void displayAsGeometry(Envelope env) {
        GeometryFactory gf = new GeometryFactory();
        System.out.println(gf.toGeometry(env));
    }
    
    @Test
    public void computeLatitudeDistance1() throws Exception {
        assertEquals(0, GeographyUtils.computeLatitudeDistance(0));
    }
    
    @Test
    public void computeLatitudeDistance2() throws Exception {
        assertTrue((1- GeographyUtils.computeLatitudeDistance(111.045))>0.01);
    }
    
    @Test
    public void computeLongitudeDistance1() throws Exception {
        assertEquals(0, GeographyUtils.computeLongitudeDistance(0, 0));
    }
    
    @Test
    public void computeLongitudeDistance2() throws Exception {
        assertTrue((1- GeographyUtils.computeLongitudeDistance(87.87018,3 ))>0.01);
    }

}
