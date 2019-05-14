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

package org.h2gis.functions.spatial.crs;

import org.cts.parser.proj.ProjKeyParameters;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author ebocher
 */
public class SpatialRegistryTest {

    private static Connection connection;
    private static final String DB_NAME = "SpatialRegistryTest";
    private static SpatialRefRegistry srr;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SFSUtilities.wrapConnection(H2GISDBFactory.createSpatialDataBase(DB_NAME));
        srr = new SpatialRefRegistry();
        srr.setConnection(connection);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void test4326Parameters() throws Exception {
        Map<String, String> parameters = srr.getParameters("4326");
        //Expected 
        //# WGS 84
        //<4326> +proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs  <>                
        assertTrue(parameters.get(ProjKeyParameters.proj).equals("longlat"));
        assertTrue(parameters.get(ProjKeyParameters.ellps).equals("WGS84"));
        assertTrue(parameters.get(ProjKeyParameters.datum).equals("WGS84"));
    }

    @Test
    public void test2736Parameters() throws Exception {
        Map<String, String> parameters = srr.getParameters("2736");
        //Expected 
        //+proj=utm +zone=36 +south +ellps=clrk66 
        //+towgs84=219.315,168.975,-166.145,0.198,5.926,-2.356,-57.104 +units=m +no_defs '          
        assertTrue(parameters.get(ProjKeyParameters.proj).equals("utm"));
        assertTrue(parameters.get(ProjKeyParameters.zone).equals("36"));
        assertTrue(parameters.get(ProjKeyParameters.south) == null);
        assertTrue(parameters.get(ProjKeyParameters.ellps).equals("clrk66"));
        assertTrue(parameters.get(ProjKeyParameters.towgs84).equals("-80,-100,-228,0,0,0,0"));
        assertTrue(parameters.get(ProjKeyParameters.units).equals("m"));
    }

    @Test
    public void testCodes() throws Exception {
        Set<String> codes = srr.getSupportedCodes();
        assertTrue(codes.contains("4326"));
        assertTrue(codes.contains("27572"));
        assertTrue(codes.contains("2154"));
        assertTrue(codes.contains("3857"));
    }
}
