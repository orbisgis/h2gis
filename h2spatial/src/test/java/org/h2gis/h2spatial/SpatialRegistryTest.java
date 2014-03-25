/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatial;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import org.cts.parser.proj.ProjKeyParameters;
import org.h2gis.h2spatial.internal.function.spatial.crs.SpatialRefRegistry;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.h2gis.utilities.SFSUtilities;

/**
 *
 * @author ebocher
 */
public class SpatialRegistryTest {

    private static Connection connection;
    private static final String DB_NAME = "SpatialRegistryTest";
    private static SpatialRefRegistry srr;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SFSUtilities.wrapConnection(SpatialH2UT.createSpatialDataBase(DB_NAME));
        srr = new SpatialRefRegistry();
        srr.setConnection(connection);
    }

    @AfterClass
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
        assertTrue(parameters.get(ProjKeyParameters.towgs84).equals("219.315,168.975,-166.145,0.198,5.926,-2.356,-57.104"));
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
