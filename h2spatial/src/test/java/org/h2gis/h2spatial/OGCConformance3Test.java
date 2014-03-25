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

import org.h2.value.ValueGeometry;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Final OGC Conformance test with spatial capabilities.
 * @author Nicolas Fortin
 */
public class OGCConformance3Test {
    private static Connection connection;
    private static final String DB_NAME = "OGCConformance3Test";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        // Set up test data
        URL sqlURL = OGCConformance1Test.class.getResource("ogc_conformance_test3.sql");
        Statement st = connection.createStatement();
        // Unit test will create own spatial ref table
        st.execute("RUNSCRIPT FROM '"+sqlURL+"'");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    /**
     *  For this test, we will check to see that all of the feature tables are
     *  represented by entries in the GEOMETRY_COLUMNS table/view.
     *  @throws Exception
     */
    @Test
    public void T1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT f_table_name FROM geometry_columns;");
        Set<String> tablesWithGeometry = new HashSet<String>(11);
        while(rs.next()) {
            tablesWithGeometry.add(rs.getString("f_table_name").toLowerCase());
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
     * For this test, we will check to see that the correct geometry column
     * for the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void T2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT f_geometry_column FROM geometry_columns WHERE f_table_name = 'STREAMS';");
        assertTrue(rs.next());
        assertEquals("centerline",rs.getString("f_geometry_column").toLowerCase());
    }

    /**
     * For this test, we will check to see that the correct coordinate dimension for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void T3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT coord_dimension FROM geometry_columns WHERE f_table_name = 'STREAMS';");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of srid for
     * the streams table is represented in the GEOMETRY_COLUMNS table/view.
     * @throws Exception
     */
    @Test
    public void T4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT srid FROM geometry_columns WHERE f_table_name = 'STREAMS';");
        assertTrue(rs.next());
        assertEquals(101, rs.getInt(1));
    }

    /**
     * For this test, we will check to see that the correct value of srtext is
     * represented in the SPATIAL_REF_SYS table/view.
     * @throws Exception
     */
    @Test
    public void T5() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT srtext FROM SPATIAL_REF_SYS WHERE SRID = 101;");
        assertTrue(rs.next());
        assertEquals("PROJCS[\"UTM_ZONE_14N\", GEOGCS[\"World Geodetic System\n\n72\",DATUM[\"WGS_72\", " +
                "ELLIPSOID[\"NWL_10D\", 6378135,\n\n298.26]],PRIMEM[\"Greenwich\",\n\n0],UNIT[\"Meter\",1.0]]," +
                "PROJECTION[\"Transverse_Mercator\"],\n\nPARAMETER[\"False_Easting\", 500000.0]," +
                "PARAMETER[\"False_Northing\",\n\n0.0],PARAMETER[\"Central_Meridian\", -99.0],PARAMETER[\"Scale_Factor\"" +
                ",\n\n0.9996],PARAMETER[\"Latitude_of_origin\", 0.0],UNIT[\"Meter\", 1.0]]", rs.getString(1));
    }

    /**
     * For this test, we will determine the dimension of Blue Lake.
     * @throws Exception
     */
    @Test
    public void T6() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Dimension(shore) FROM lakes WHERE name = 'Blue Lake'");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
    }

    /**
     * For this test, we will determine  the type of Route 75.
     * @throws Exception
     */
    @Test
    public void T7() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeometryType(centerlines) FROM divided_routes WHERE name = 'Route 75';");
        assertTrue(rs.next());
        assertEquals("MULTILINESTRING", rs.getString(1).toUpperCase());
    }

    /**
     * For this test, we will determine the WKT representation of Goose Island.
     * @throws Exception
     */
    @Test
    public void T8() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM named_places WHERE name = 'Goose Island';");
        assertTrue(rs.next());
        assertEquals("POLYGON ((67 13, 67 18, 59 18, 59 13, 67 13))", rs.getString(1));
    }

    /**
     * For this test, we will determine the WKB representation of Goose Island. We will test by
     * applying AsText to the result of PolyFromText to the result of AsBinary.
     * @throws Exception
     */
    @Test
    public void T9() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_PolyFromWKB(ST_AsBinary(boundary),101)) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals("POLYGON ((67 13, 67 18, 59 18, 59 13, 67 13))", rs.getString(1));
    }

    /**
     * For this test, we will determine the SRID of Goose Island.
     * @throws Exception
     */
    @Test
    public void T10() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_SRID(boundary) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals(101, rs.getInt(1));
    }

    /**
     * For this test, we will determine whether the geometry of a segment of Route 5 is empty.
     * @throws Exception
     */
    @Test
    public void T11() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_IsEmpty(centerline) FROM road_segments WHERE name = 'Route 5' AND aliases = 'Main Street'");
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
    }

    /**
     * For this test, we will determine whether the geometry of a segment of Blue Lake is simple.
     * @throws Exception
     */
    @Test
    public void T12() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_IsSimple(shore) FROM lakes WHERE name = 'Blue Lake'");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the boundary of Goose Island.
     * @throws Exception
     */
    @Test
    public void T13() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Boundary(boundary,101)) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        // Differs from OGC, in JTS all LineString that start and end with the same coordinate create a LinearRing not a LineString.
        // Real OGC expected result "LINESTRING (67 13, 67 18, 59 18, 59 13, 67 13)"
        assertEquals(ValueGeometry.get("LINEARRING (67 13, 67 18, 59 18, 59 13, 67 13)"), ValueGeometry.get(rs.getString(1)));
    }

    /**
     * For this test, we will determine the envelope of Goose Island.
     * @throws Exception
     */
    @Test
    public void T14() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Envelope(boundary,101)) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 13, 59 18, 67 18, 67 13, 59 13))", rs.getString(1));
    }

    /**
     * For this test we will determine the X coordinate of Cam Bridge.
     * @throws Exception
     */
    @Test
    public void T15() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_X(position) FROM bridges WHERE name = 'Cam Bridge'");
        assertTrue(rs.next());
        assertEquals(44.0, rs.getDouble(1),1e-12);
    }

    /**
     * For this test we will determine the Y coordinate of Cam Bridge.
     * @throws Exception
     */
    @Test
    public void T16() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Y(position) FROM bridges WHERE name = 'Cam Bridge'");
        assertTrue(rs.next());
        assertEquals(31.0, rs.getDouble(1),1e-12);
    }


    /**
     * For this test, we will determine the start point of road segment 102.
     * @throws Exception
     */
    @Test
    public void T17() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_StartPoint(centerline)) FROM road_segments WHERE fid = 102");
        assertTrue(rs.next());
        assertEquals("POINT (0 18)", rs.getString(1));
    }

    /**
     * For this test, we will determine the end point of road segment 102.
     * @throws Exception
     */
    @Test
    public void T18() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_EndPoint(centerline)) FROM road_segments WHERE fid = 102");
        assertTrue(rs.next());
        assertEquals("POINT (44 31)", rs.getString(1));
    }

    /**
     * For this test, we will determine the boundary close state of Goose Island.
     * @throws Exception
     */
    @Test
    public void T19() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_IsClosed(ST_LineFromWKB(ST_AsBinary(ST_Boundary(boundary)),ST_SRID(boundary))) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the boundary close and simple state of Goose Island.
     * @throws Exception
     */
    @Test
    public void T20() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_IsRing(ST_LineFromWKB(ST_AsBinary(ST_Boundary(boundary)),ST_SRID(boundary))) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the length of road segment 106.
     * @throws Exception
     */
    @Test
    public void T21() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Length(centerline) FROM road_segments WHERE fid = 106");
        assertTrue(rs.next());
        assertEquals(26, rs.getDouble(1),1e-12);
    }

    /**
     * For this test, we will determine the number of points in road segment 102.
     * @throws Exception
     */
    @Test
    public void T22() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_NumPoints(centerline) FROM road_segments WHERE fid = 102");
        assertTrue(rs.next());
        assertEquals(5, rs.getInt(1));
    }

    /**
     * For this test, we will determine the 1st point in road segment 102.
     * @throws Exception
     */
    @Test
    public void T23() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_PointN(centerline, 1)) FROM road_segments WHERE fid = 102");
        assertTrue(rs.next());
        assertEquals("POINT (0 18)", rs.getString(1));
    }

    /**
     * For this test, we will determine the centroid of Goose Island.
     * @throws Exception
     */
    @Test
    public void T24() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Centroid(boundary)) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        // Real OGC unit test value "POINT (53 15.5)"
        // OGC quote "No specific algorithm is specified for the Centroid function; answers may vary with implementation."
        assertEquals("POINT (63 15.5)", rs.getString(1));
    }

    /**
     * For this test, we will determine a point on Goose Island.
     * For this test we will have to uses the Contains function (which we don't test until later).
     * @throws Exception
     */
    @Test
    public void T25() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Contains(boundary, ST_PointOnSurface(boundary)) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the area of Goose Island.
     * @throws Exception
     */
    @Test
    public void T26() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(boundary) FROM named_places WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals(40.0, rs.getDouble(1),1e-12);
    }

    /**
     * For this test, we will determine the exterior ring of Blue Lake.
     * @throws Exception
     */
    @Test
    public void T27() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_ExteriorRing(shore)) FROM lakes WHERE name = 'Blue Lake'");
        assertTrue(rs.next());
        // Differs from OGC, in JTS all LineString that start and end with the same coordinate create a LinearRing not a LineString.
        // Real OGC expected result "LINESTRING (52 18, 66 23, 73 9, 48 6, 52 18)"
        assertEquals(ValueGeometry.get("LINEARRING (52 18, 66 23, 73 9, 48 6, 52 18)"), ValueGeometry.get(rs.getString(1)));
    }

    /**
     * For this test, we will determine the number of interior rings of Blue Lake.
     * @throws Exception
     */
    @Test
    public void T28() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_NumInteriorRing(shore) FROM lakes WHERE name = 'Blue Lake'");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
    }

    /**
     * For this test, we will determine the first interior ring of Blue Lake.
     * @throws Exception
     */
    @Test
    public void T29() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_InteriorRingN(shore, 1)) FROM lakes WHERE name = 'Blue Lake'");
        assertTrue(rs.next());
        // Differs from OGC, in JTS all LineString that start and end with the same coordinate create a LinearRing not a LineString.
        // Real OGC expected result "LINESTRING (59 18, 67 18, 67 13, 59 13, 59 18)"
        assertEquals(ValueGeometry.get("LINEARRING (59 18, 67 18, 67 13, 59 13, 59 18)"), ValueGeometry.get(rs.getString(1)));
    }

    /**
     * For this test, we will determine the number of geometries in Route 75.
     * @throws Exception
     */
    @Test
    public void T30() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_NumGeometries(centerlines) FROM divided_routes WHERE name = 'Route 75'");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
    }

    /**
     * For this test, we will determine the second geometry in Route 75.
     * @throws Exception
     */
    @Test
    public void T31() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_GeometryN(centerlines, 2)) FROM divided_routes WHERE name = 'Route 75'");
        assertTrue(rs.next());
        assertEquals("LINESTRING (16 0, 16 23, 16 48)", rs.getString(1));
    }

    /**
     * For this test, we will determine if the geometry of Route 75 is closed.
     * @throws Exception
     */
    @Test
    public void T32() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_IsClosed(centerlines) FROM divided_routes WHERE name = 'Route 75'");
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the length of Route 75.
     * @throws Exception
     */
    @Test
    public void T33() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Length(centerlines) FROM divided_routes WHERE name = 'Route 75'");
        assertTrue(rs.next());
        assertEquals(96.0, rs.getDouble(1),1e-12);
    }

    /**
     * For this test, we will determine the centroid of the ponds.
     * @throws Exception
     */
    @Test
    public void T34() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Centroid(shores)) FROM ponds WHERE fid = 120");
        assertTrue(rs.next());
        assertEquals("POINT (25 42)", rs.getString(1));
    }

    /**
     * For this test, we will determine a point on the ponds.
     * @throws Exception
     */
    @Test
    public void T35() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Contains(shores, ST_PointOnSurface(shores)) FROM ponds WHERE fid = 120");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the area of the ponds.
     * @throws Exception
     */
    @Test
    public void T36() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(shores) FROM ponds WHERE fid = 120");
        assertTrue(rs.next());
        assertEquals(8.0, rs.getDouble(1),1e-12);
    }

    /**
     * For this test, we will determine if the geometry of Goose Island is equal to the same geometry as
     * constructed from it's WKT representation.
     * @throws Exception
     */
    @Test
    public void T37() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Equals(boundary, " +
                "ST_PolyFromText('POLYGON ((67 13, 67 18, 59 18, 59 13, 67 13))',1)) FROM named_places" +
                " WHERE name = 'Goose Island'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of Goose Island is equal to the same geometry as
     * constructed from it's WKT representation.
     * @throws Exception
     */
    @Test
    public void T38() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Disjoint(centerlines, boundary) FROM divided_routes, named_places" +
                " WHERE divided_routes.name = 'Route 75' AND named_places.name = 'Ashton'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of Cam Stream touches the geometry of Blue Lake.
     * @throws Exception
     */
    @Test
    public void T39() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Touches(centerline, shore) FROM streams, lakes " +
                "WHERE streams.name = 'Cam Stream' AND lakes.name = 'Blue Lake'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of the house at 215 Main Street is within Ashton.
     * @throws Exception
     */
    @Test
    public void T40() throws Exception {
        Statement st = connection.createStatement();
        // Fix OGC original request inversion of footprint and boundary
        ResultSet rs = st.executeQuery("SELECT ST_Within(footprint,boundary) FROM named_places, buildings " +
                "WHERE named_places.name = 'Ashton' AND buildings.address = '215 Main Street'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of Green Forest overlaps the geometry of Ashton.
     * @throws Exception
     */
    @Test
    public void T41() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Overlaps(forests.boundary, named_places.boundary) " +
                "FROM forests, named_places WHERE forests.name = 'Green Forest' AND named_places.name = 'Ashton'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of road segment 101 crosses the geometry of Route 75.
     * @throws Exception
     */
    @Test
    public void T42() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Crosses(road_segments.centerline, divided_routes.centerlines) " +
                "FROM road_segments, divided_routes WHERE road_segments.fid = 102 AND divided_routes.name = 'Route 75'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of road segment 101 intersects the
     * geometry of Route 75.
     * @throws Exception
     */
    @Test
    public void T43() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Intersects(road_segments.centerline, divided_routes.centerlines) " +
                "FROM road_segments, divided_routes WHERE road_segments.fid = 102 AND divided_routes.name = 'Route 75'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of Green Forest
     * contains the geometry of Ashton.
     * @throws Exception
     */
    @Test
    public void T44() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Contains(forests.boundary, named_places.boundary) " +
                "FROM forests, named_places WHERE forests.name = 'Green Forest' AND named_places.name = 'Ashton'");
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine if the geometry of Green Forest
     * relates to the geometry of Ashton using the pattern "TTTTTTTTT".
     * @throws Exception
     */
    @Test
    public void T45() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Relate(forests.boundary, named_places.boundary, 'TTTTTTTTT') " +
                "FROM forests, named_places WHERE forests.name = 'Green Forest' AND named_places.name = 'Ashton'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }

    /**
     * For this test, we will determine the distance between Cam Bridge and Ashton.
     * @throws Exception
     */
    @Test
    public void T46() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Distance(position, boundary) FROM bridges, named_places " +
                "WHERE bridges.name = 'Cam Bridge' AND named_places.name = 'Ashton'");
        assertTrue(rs.next());
        assertEquals(12.0, rs.getDouble(1),1e-12);
    }

    /**
     * For this test, we will determine the intersection between Cam Stream and Blue Lake.
     * @throws Exception
     */
    @Test
    public void T47() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Intersection(centerline, shore)) " +
                "FROM streams, lakes WHERE streams.name = 'Cam Stream'");
        assertTrue(rs.next());
        assertEquals("POINT (52 18)", rs.getString(1));
    }

    /**
     * For this test, we will determine the difference between Ashton and Green Forest.
     * @throws Exception
     */
    @Test
    public void T48() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Difference(named_places.boundary, forests.boundary)) " +
                "FROM named_places, forests WHERE named_places.name = 'Ashton'");
        assertTrue(rs.next());
        // OGC original: POLYGON ((56 34, 62 48, 84 48, 84 42, 56 34))
        // Here the polygon is the same but with a different points order
        assertEquals("POLYGON ((62 48, 84 48, 84 42, 56 34, 62 48))", rs.getString(1));
    }

    /**
     *  For this test, we will determine the union of Blue Lake and Goose Island.
     * @throws Exception
     */
    @Test
    public void T49() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_Union(shore, boundary)) FROM lakes, named_places " +
                "WHERE lakes.name = 'Blue Lake' AND named_places.name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals("POLYGON ((52 18, 66 23, 73 9, 48 6, 52 18))", rs.getString(1));
    }

    /**
     * For this test, we will determine the symmetric difference of Blue Lake and Goose Island.
     * @throws Exception
     */
    @Test
    public void T50() throws Exception {
        Statement st = connection.createStatement();
        // Test script uses 'Ashton' as the place name where it means
        // to use 'Goose Island'.
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_SymDifference(shore, boundary)) FROM lakes, named_places " +
                "WHERE lakes.name = 'Blue Lake' AND named_places.name = 'Goose Island'");
        assertTrue(rs.next());
        assertEquals("POLYGON ((52 18, 66 23, 73 9, 48 6, 52 18))", rs.getString(1));
    }

    /**
     *  For this test, we will make a 15 m buffer about Cam Bridge.
     * @throws Exception
     */
    @Test
    public void T51() throws Exception {
        Statement st = connection.createStatement();
        // OGC Original
        // SELECT count(*) FROM buildings, bridges WHERE Contains(Buffer(bridges.position, 15.0), buildings.footprint) = 1;
        // Function return Boolean value, then it does not require any comparison
        ResultSet rs = st.executeQuery("SELECT count(*) FROM buildings, bridges " +
                "WHERE ST_Contains(ST_Buffer(bridges.position, 15.0), buildings.footprint)");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
    }

    /**
     * For this test, we will determine the convex hull of Blue Lake.
     * @throws Exception
     */
    @Test
    public void T52() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(ST_ConvexHull(shore)) FROM lakes WHERE lakes.name = 'Blue Lake'");
        assertTrue(rs.next());
        // OGC original: POLYGON ((52 18, 66 23, 73 9, 48 6, 52 18))
        // Here the polygon is the same but with a different points order
        assertEquals("POLYGON ((48 6, 52 18, 66 23, 73 9, 48 6))", rs.getString(1));
    }

}
