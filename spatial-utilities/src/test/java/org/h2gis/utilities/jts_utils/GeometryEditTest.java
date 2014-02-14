/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.utilities.jts_utils;

import org.junit.Test;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.GeometryLocation;
import java.util.List;
import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GeometryEditTest {

    public WKTReader wKTReader = new WKTReader();

       

    /**
     * Test cut a polygon
     *
     * @throws Exception
     */
    @Test
    public void testCutPolygon() throws Exception {
        Polygon polygon = (Polygon) wKTReader.read("POLYGON (( 0 0 ,10 0, 10 10, 0 10, 0 0 ))");
        Polygon cutter = (Polygon) wKTReader.read("POLYGON (( 2 2  ,7 2, 7 7, 2 7, 2 2))");
        //Test cut a polygon inside
        List<Polygon> result = GeometryEdit.cutPolygonWithPolygon(polygon, cutter);
        assertEquals(result.get(0).getNumInteriorRing(), 1);
        assertEquals(result.get(0).getInteriorRingN(0).getEnvelopeInternal(), cutter.getEnvelopeInternal());

        //Test cut a polygon outside
        cutter = (Polygon) wKTReader.read("POLYGON (( 2 -1.8153735632183903, 7.177873563218391 -1.8153735632183903, 7.177873563218391 7, 2 7, 2 -1.8153735632183903 ))");
        result = GeometryEdit.cutPolygonWithPolygon(polygon, cutter);
        assertEquals(result.get(0), wKTReader.read("POLYGON (( 2 0, 0 0, 0 10, 10 10, 10 0, 7.177873563218391 0, 7.177873563218391 7, 2 7, 2 0 ))"));
    }
    
}
