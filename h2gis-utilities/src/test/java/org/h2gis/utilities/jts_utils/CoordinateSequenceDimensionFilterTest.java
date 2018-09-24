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

import org.locationtech.jts.io.WKTReader;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Adam Gouge
 */
public class CoordinateSequenceDimensionFilterTest {

    public WKTReader wKTReader = new WKTReader();

    @Test
    public void testDimensionSequence() throws Exception {
        CoordinateSequenceDimensionFilter cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("POINT(0 0)").apply(cd);
        assertTrue(cd.getDimension() == 2);
        assertTrue(cd.is2D());
        assertFalse(cd.isMixed());
        cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("LINESTRING(0 0, 1 0)").apply(cd);
        assertTrue(cd.getDimension() == 2);
        assertTrue(cd.is2D());
        assertFalse(cd.isMixed());
        cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("LINESTRING(0 0, 1 0 0)").apply(cd);
        assertTrue(cd.getDimension() == 3);
        assertFalse(cd.is2D());
        assertTrue(cd.isMixed());
        cd = new CoordinateSequenceDimensionFilter();
        wKTReader.read("LINESTRING(0 0 0, 1 0 0)").apply(cd);
        assertTrue(cd.getDimension() == 3);
        assertFalse(cd.is2D());
        assertFalse(cd.isMixed());
    }
}
