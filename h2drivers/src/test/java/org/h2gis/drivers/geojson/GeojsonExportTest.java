/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.geojson;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import static org.junit.Assert.assertTrue;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class GeojsonExportTest {

    private WKTReader wKTReader = new WKTReader();

    @Test
    public void testGeojsonPoint() throws Exception {
        Geometry geom = wKTReader.read("POINT(1 2)");
        StringBuilder sb = new StringBuilder();
        GeojsonGeometry.toGeojsonPoint((Point)geom, sb);
        //assertTrue(sb.toString().equals("{{"type":"Point","coordinates":[1.0,2.0]}"));        
    }
}
