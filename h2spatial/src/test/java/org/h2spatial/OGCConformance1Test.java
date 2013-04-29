/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2spatial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * OGC Conformance test 1 does not require DataBase spatial capability.
 * @author Nicolas Fortin
 */
public class OGCConformance1Test {
    private static final String DB_FILE_PATH = "target/test-resources/dbH2_OGC_Conf1";
    private static final File DB_FILE = new File(DB_FILE_PATH+".h2.db");
    private static final String DATABASE_PATH = "jdbc:h2:"+DB_FILE_PATH;
    private static Connection connection;

    @BeforeClass
    public static void tearUp() throws Exception {
        Class.forName("org.h2.Driver");
        if(DB_FILE.exists()) {
            DB_FILE.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(DATABASE_PATH,
                "sa", "");
        // Set up test data
        URL sqlURL = OGCConformance1Test.class.getResource("ogc_conformance_test.sql");
        Statement st = connection.createStatement();
        st.execute("RUNSCRIPT FROM '"+sqlURL+"'");
    }

    /**
     *  For this test, we will check to see that all of the feature tables are
     *  represented by entries in the GEOMETRY_COLUMNS table/view.
     *  @throws Exception
     */
    @Test
    public void N1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT f_table_name FROM geometry_columns;");
        Set<String> tablesWithGeometry = new HashSet<String>(11);
        while(rs.next()) {
            tablesWithGeometry.add(rs.getString("f_table_name"));
        }
        assertTrue(tablesWithGeometry.contains("lakes"));
        assertTrue(tablesWithGeometry.contains("road_segments"));
        assertTrue(tablesWithGeometry.contains("divided_routes"));
        assertTrue(tablesWithGeometry.contains("buildings"));
        assertTrue(tablesWithGeometry.contains("forests"));
        assertTrue(tablesWithGeometry.contains("bridges"));
        assertTrue(tablesWithGeometry.contains("named_places"));
        assertTrue(tablesWithGeometry.contains("streams"));
        assertTrue(tablesWithGeometry.contains("ponds"));
        assertTrue(tablesWithGeometry.contains("map_neatlines"));
    }

    /**
     *  For this test, we will check to see that all of the feature tables are
     *  represented by entries in the GEOMETRY_COLUMNS table/view.
     *  @throws Exception
     */
    @Test
    public void N2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT g_table_name FROM geometry_columns;");
        Set<String> tablesWithGeometry = new HashSet<String>(11);
        while(rs.next()) {
            tablesWithGeometry.add(rs.getString("g_table_name"));
        }
        assertTrue(tablesWithGeometry.contains("lake_geom"));
        assertTrue(tablesWithGeometry.contains("road_segment_geom"));
        assertTrue(tablesWithGeometry.contains("divided_route_geom"));
        assertTrue(tablesWithGeometry.contains("forest_geom"));
        assertTrue(tablesWithGeometry.contains("bridge_geom"));
        assertTrue(tablesWithGeometry.contains("stream_geom"));
        assertTrue(tablesWithGeometry.contains("building_pt_geom"));
        assertTrue(tablesWithGeometry.contains("building_area_geom"));
        assertTrue(tablesWithGeometry.contains("pond_geom"));
        assertTrue(tablesWithGeometry.contains("named_place_geom"));
        assertTrue(tablesWithGeometry.contains("map_neatline_geom"));
    }
    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    /*

-- Conformance Item N1

SELECT f_table_name

FROM geometry_columns;

-- Conformance Item N2

SELECT g_table_name FROM geometry_columns;

-- Conformance Item N3

SELECT storage_type

FROM geometry_columns

WHERE f_table_name = 'streams';

-- Conformance Item N4

SELECT geometry_type

FROM geometry_columns

WHERE f_table_name = 'streams';

-- Conformance Item N5

SELECT coord_dimension

FROM geometry_columns

WHERE f_table_name = 'streams';

-- Conformance Item N6

SELECT max_ppr

FROM geometry_columns

WHERE f_table_name = 'streams';

-- Conformance Item N7

SELECT srid

FROM geometry_columns

WHERE f_table_name = 'streams';

-- Conformance Item N8

SELECT srtext

FROM SPATIAL_REF_SYS

WHERE SRID = 101;

     */
}
