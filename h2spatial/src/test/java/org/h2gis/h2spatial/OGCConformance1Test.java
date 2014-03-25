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

package org.h2gis.h2spatial;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.sql.Connection;
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
    private static Connection connection;
    private static final String DB_NAME = "OGCConformance1Test";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, false);
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
     * For this test, we will check to see that all of the geometry tables are
     * represented by entries in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
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

    /**
     * For this test, we will check to see that the correct storage type for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void N3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT storage_type FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct geometry type for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void N4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT geometry_type FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct coordinate dimension for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void N5() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT coord_dimension FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of max_ppr
     * for the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void N6() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT max_ppr FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of srid for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void N7() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT srid FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(101, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of srtext is
     * represented in the SPATIAL_REF_SYS table/view.
     * @throws Exception
     */
    @Test
    public void N8() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT srtext FROM SPATIAL_REF_SYS WHERE SRID = 101;");
        assertTrue(rs.next());
        assertEquals("PROJCS[\"UTM_ZONE_14N\", GEOGCS[\"World Geodetic System\n\n72\",DATUM[\"WGS_72\", " +
                "ELLIPSOID[\"NWL_10D\", 6378135,\n\n298.26]],PRIMEM[\"Greenwich\",\n\n0],UNIT[\"Meter\",1.0]]," +
                "PROJECTION[\"Transverse_Mercator\"],\n\nPARAMETER[\"False_Easting\", 500000.0]," +
                "PARAMETER[\"False_Northing\",\n\n0.0],PARAMETER[\"Central_Meridian\", -99.0],PARAMETER[\"Scale_Factor\"" +
                ",\n\n0.9996],PARAMETER[\"Latitude_of_origin\", 0.0],UNIT[\"Meter\", 1.0]]", rs.getString(1));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }
}
