/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.utilities.jts_utils;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.WKTReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Adam Gouge
 */
public class CoordinateSequenceDimensionFilterTest {

    public WKTReader wKTReader = new WKTReader();

    @Test
    public void testDimensionSequence() throws Exception {
        CoordinateSequenceDimensionFilter cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("POINT(0 0)").apply(cd);
        assertEquals(2, cd.getDimension());
        assertTrue(cd.is2D());
        assertFalse(cd.isMixed());
        cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("LINESTRING(0 0, 1 0)").apply(cd);
        assertEquals(2, cd.getDimension());
        assertTrue(cd.is2D());
        assertFalse(cd.isMixed());
        cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("LINESTRING(0 0, 1 0 0)").apply(cd);
        assertEquals(3, cd.getDimension());
        assertFalse(cd.is2D());
        assertTrue(cd.isMixed());
        cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("LINESTRING(0 0 0, 1 0 0)").apply(cd);
        assertEquals(3, cd.getDimension());
        assertFalse(cd.is2D());
        assertFalse(cd.isMixed());
    }
}
