/**
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Final OGC Conformance test with spatial capabilities.
 * @author Nicolas Fortin
 */
public class OGCConformance3Test {
    private static final String DB_FILE_PATH = "target/test-resources/dbH2_OGC_Conf3";
    private static final File DB_FILE = new File(DB_FILE_PATH+".h2.db");
    private static final String DATABASE_PATH = "jdbc:h2:"+DB_FILE_PATH;
    private static Connection connection;

    @BeforeClass
    public static void tearUp() throws Exception {
        Class.forName("org.h2.Driver");
        if(DB_FILE.exists()) {
            assertTrue(DB_FILE.delete());
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(DATABASE_PATH,
                "sa", "");
        // Init spatial ext
        CreateSpatialExtension.initSpatialExtension(connection);
        // Set up test data
        URL sqlURL = OGCConformance1Test.class.getResource("ogc_conformance_test3.sql");
        Statement st = connection.createStatement();
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
        ResultSet rs = st.executeQuery("SELECT ST_Dimension(shore) FROM lakes WHERE name = 'BLUE LAKE'");
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
        ResultSet rs = st.executeQuery("SELECT ST_IsSimple(shore) FROM lakes WHERE name = 'BLUE LAKE'");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
    }


    /*

-- Conformance Item T12

SELECT IsSimple(shore) FROM lakes WHERE name = 'Blue Lake';

-- Conformance Item T13

SELECT AsText(Boundary((boundary),101)

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T14

SELECT AsText(Envelope((boundary),101)

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T15

SELECT X(position)

FROM bridges

WHERE name = ‘Cam Bridge’;

-- Conformance Item T16

SELECT Y(position)

FROM bridges

WHERE name = 'Cam Bridge';

-- Conformance Item T17

SELECT AsText(StartPoint(centerline))

FROM road_segments

WHERE fid = 102;

-- Conformance Item T18

SELECT AsText(EndPoint(centerline))

FROM road_segments

WHERE fid = 102;

-- Conformance Item T19

SELECT IsClosed(LineFromWKB(AsBinary(Boundary(boundary)),SRID(boundary)))

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T20

SELECT IsRing(LineFromWKB(AsBinary(Boundary(boundary)),SRID(boundary)))

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T21

SELECT Length(centerline)

FROM road_segments

WHERE fid = 106;

-- Conformance Item T22

SELECT NumPoints(centerline)

FROM road_segments

WHERE fid = 102;

-- Conformance Item T23

SELECT AsText(PointN(centerline, 1))

FROM road_segments

WHERE fid = 102;

-- Conformance Item T24

SELECT AsText(Centroid(boundary))

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T25

SELECT Contains(boundary, PointOnSurface(boundary))

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T26

SELECT Area(boundary)

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T27

SELECT AsText(ExteriorRing(shore))

FROM lakes

WHERE name = 'Blue Lake';

-- Conformance Item T28

SELECT NumInteriorRing(shore)

FROM lakes

WHERE name = 'Blue Lake';

-- Conformance Item T29

SELECT AsText(InteriorRingN(shore, 1))

FROM lakes

WHERE name = 'Blue Lake';

-- Conformance Item T30

SELECT NumGeometries(centerlines)

FROM divided_routes

WHERE name = 'Route 75';

-- Conformance Item T31

SELECT AsText(GeometryN(centerlines, 2))

FROM divided_routes

WHERE name = 'Route 75';

-- Conformance Item T32

SELECT IsClosed(centerlines)

FROM divided_routes

WHERE name = 'Route 75';

-- Conformance Item T33

SELECT Length(centerlines)

FROM divided_routes

WHERE name = 'Route 75';

-- Conformance Item T34

SELECT AsText(Centroid(shores))

FROM ponds

WHERE fid = 120;

-- Conformance Item T35

SELECT Contains(shores, PointOnSurface(shores))

FROM ponds

WHERE fid = 120;

-- Conformance Item T36

SELECT Area(shores)

FROM ponds

WHERE fid = 120;

-- Conformance Item T37

SELECT Equals(boundary,

PolyFromText('POLYGON( ( 67 13, 67 18, 59 18, 59 13, 67 13) )',1))

FROM named_places

WHERE name = 'Goose Island';

-- Conformance Item T38

SELECT Disjoint(centerlines, boundary)

FROM divided_routes, named_places

WHERE divided_routes.name = 'Route 75'



AND named_places.name = 'Ashton';

-- Conformance Item T39

SELECT Touches(centerline, shore)

FROM streams, lakes

WHERE streams.name = 'Cam Stream'



AND lakes.name = 'Blue Lake';

-- Conformance Item T40

SELECT Within(boundary, footprint)

FROM named_places, buildings

WHERE named_places.name = 'Ashton'



AND buildings.address = '215 Main Street';

-- Conformance Item T41

SELECT Overlaps(forests.boundary, named_places.boundary)

FROM forests, named_places

WHERE forests.name = 'Green Forest'



AND named_places.name = 'Ashton';

-- Conformance Item T42

SELECT Crosses(road_segments.centerline, divided_routes.centerlines)

FROM road_segments, divided_routes

WHERE road_segment.fid = 102



AND divided_routes.name = 'Route 75';

-- Conformance Item T43

SELECT Intersects(road_segments.centerline, divided_routes.centerlines)

FROM road_segments, divided_routes

WHERE road_segments.fid = 102



AND divided_routes.name = 'Route 75';

-- Conformance Item T44

SELECT Contains(forests.boundary, named_places.boundary)

FROM forests, named_places

WHERE forests.name = 'Green Forest'



AND named_places.name = 'Ashton';

-- Conformance Item T45

SELECT Relate(forests.boundary, named_places.boundary, 'TTTTTTTTT')

FROM forests, named_places

WHERE forests.name = 'Green Forest'



AND named_places.name = 'Ashton';

-- Conformance Item T46

SELECT Distance(position, boundary)

FROM bridges, named_places

WHERE bridges.name = 'Cam Bridge'



AND named_places.name = 'Ashton';

-- Conformance Item T47

SELECT AsText(Intersection(centerline, shore))

FROM streams, lakes

WHERE streams.name = 'Cam Stream'



AND lakes.name = 'Blue Lake';

-- Conformance Item T48

SELECT AsText(Difference(named_places.boundary, forests.boundary))

FROM named_places, forests

WHERE named_places.name = 'Ashton'



AND forests.name = 'Green Forest';

-- Conformance Item T49

SELECT AsText(Union(shore, boundary))

FROM lakes, named_places

WHERE lakes.name = 'Blue Lake'



AND named_places.name = ‘Goose Island’;

-- Conformance Item T50

SELECT AsText(SymDifference(shore, boundary))

FROM lakes, named_places

WHERE lakes.name = 'Blue Lake'



AND named_places.name = 'Ashton';

-- Conformance Item T51

SELECT count(*)

FROM buildings, bridges

WHERE Contains(Buffer(bridges.position, 15.0), buildings.footprint)

= 1;

-- Conformance Item T52

SELECT AsText(ConvexHull(shore))

FROM lakes

WHERE lakes.name = 'Blue Lake';
    */
}
