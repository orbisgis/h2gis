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

package org.h2gis.functions.spatial.ogc;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Second unit test group, use advanced SQL features like foreign key constraint.
 * But no Spatial features.
 * @author Nicolas Fortin
 */
public class OGCConformance2Test {
    private static final String DB_NAME = "OGCConformance2Test";
    private static Connection connection;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        // Set up test data
        Statement st = connection.createStatement();
        //Remove view to not be in conflict with this script that does not remove any existing table
        st.execute("drop view if exists geometry_columns;");
        st.execute("drop table if exists spatial_ref_sys;");
        OGCConformance1Test.executeScript(connection, "ogc_conformance_test2.sql");
    }


    /**
     *  For this test, we will check to see that all of the feature tables are
     *  represented by entries in the GEOMETRY_COLUMNS table/view.
     */
    @Test
    public void B1() throws Exception {
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
     */
    @Test
    public void B2() throws Exception {
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
     */
    @Test
    public void B3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT storage_type FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct geometry type for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     */
    @Test
    public void B4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT geometry_type FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct coordinate dimension for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     */
    @Test
    public void B5() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT coord_dimension FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of srid for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     */
    @Test
    public void B6() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT srid FROM geometry_columns WHERE f_table_name = 'streams';");
        assertTrue(rs.next());
        assertEquals(101, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of srtext is
     * represented in the SPATIAL_REF_SYS table/view.
     */
    @Test
    public void B7() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT srtext FROM SPATIAL_REF_SYS WHERE SRID = 101;");
        assertTrue(rs.next());
        OGCConformance1Test.osIndepentendAssertEquals("PROJCS[\"UTM_ZONE_14N\", GEOGCS[\"World Geodetic System\n\n72\",DATUM[\"WGS_72\", " +
                "ELLIPSOID[\"NWL_10D\", 6378135,\n\n298.26]],PRIMEM[\"Greenwich\",\n\n0],UNIT[\"Meter\",1.0]]," +
                "PROJECTION[\"Transverse_Mercator\"],\n\nPARAMETER[\"False_Easting\", 500000.0]," +
                "PARAMETER[\"False_Northing\",\n\n0.0],PARAMETER[\"Central_Meridian\", -99.0],PARAMETER[\"Scale_Factor\"" +
                ",\n\n0.9996],PARAMETER[\"Latitude_of_origin\", 0.0],UNIT[\"Meter\", 1.0]]", rs.getString(1));
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }
}
