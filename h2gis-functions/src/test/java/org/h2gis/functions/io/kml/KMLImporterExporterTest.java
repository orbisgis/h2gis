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

package org.h2gis.functions.io.kml;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.jdbc.JdbcSQLException;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class KMLImporterExporterTest {

    private static Connection connection;
    private static final String DB_NAME = "KMLExportTest";
    private static final WKTReader WKT_READER = new WKTReader();

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new KMLWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_AsKml(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportKMLPoints() throws SQLException {
        Statement stat = connection.createStatement();
        File kmlFile = new File("target/kml_points.kml");
        try {
            stat.execute("DROP TABLE IF EXISTS KML_POINTS");
            stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
            stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (2.19 47.58)', 4326), true)");
            stat.execute("insert into KML_POINTS values(2, ST_Geomfromtext('POINT (1.06 47.59)',  4326), false)");
            // Create a KML file
            stat.execute("CALL KMLWrite('target/kml_points.kml', 'KML_POINTS')");
            assertTrue(kmlFile.exists());
        } finally {
            stat.close();
            assertTrue(kmlFile.delete());
            assertFalse(kmlFile.exists());
        }
    }

    @Test
    public void exportKMLLineString() throws SQLException {
        Statement stat = connection.createStatement();
        File kmlFile = new File("target/kml_lineString.kml");
        try {
            stat.execute("DROP TABLE IF EXISTS KML_LINESTRING");
            stat.execute("create table KML_LINESTRING(id int primary key, the_geom LINESTRING)");
            stat.execute("insert into KML_LINESTRING values(1, ST_Geomfromtext('LINESTRING (2.19 47.58,1.19 46.58)', 4326))");
            stat.execute("insert into KML_LINESTRING values(2, ST_Geomfromtext('LINESTRING (1.06 47.59,1.19 46.58)', 4326))");
            // Create a KML file
            stat.execute("CALL KMLWrite('target/kml_lineString.kml', 'KML_LINESTRING')");
            assertTrue(kmlFile.exists());
        } finally {
            stat.close();
            assertTrue(kmlFile.delete());
            assertFalse(kmlFile.exists());
        }
    }

    @Test
    public void exportKMZPoints() throws SQLException {
        Statement stat = connection.createStatement();
        File kmzFile = new File("target/kml_points.kmz");
        try {
            stat.execute("DROP TABLE IF EXISTS KML_POINTS");
            stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
            stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (2.19 47.58)',4326), true)");
            stat.execute("insert into KML_POINTS values(2, ST_Geomfromtext('POINT (1.06 47.59)',4326), false)");
            // Create a KMZ file
            stat.execute("CALL KMLWrite('target/kml_points.kmz', 'KML_POINTS')");
            assertTrue(kmzFile.exists());
        } finally {
            stat.close();
            assertTrue(kmzFile.delete());
            assertFalse(kmzFile.exists());
        }
    }

    @Test
    public void createKMLPoint() throws Exception {
        Geometry geom = WKT_READER.read("POINT(1 2)");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertEquals("<Point><coordinates>1.0,2.0</coordinates></Point>", sb.toString());
    }

    @Test
    public void createKMLLineString() throws Exception {
        Geometry geom = WKT_READER.read("LINESTRING(1 1, 2 2, 3 3)");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertEquals("<LineString><coordinates>1.0,1.0 2.0,2.0 3.0,3.0</coordinates></LineString>", sb.toString());
    }

    @Test
    public void createKMLPolygon() throws Exception {
        Geometry geom = WKT_READER.read("POLYGON ((140 370, 60 150, 220 120, 310 180, 372 355, 240 260, 140 370))");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertEquals("<Polygon><outerBoundaryIs><LinearRing><coordinates>"
                + "140.0,370.0 60.0,150.0 220.0,120.0 310.0,180.0 372.0,355.0 240.0,260.0 140.0,370.0"
                + "</coordinates></LinearRing></outerBoundaryIs></Polygon>", sb.toString());
    }

    @Test
    public void createKMLPolygonWithHoles() throws Exception {
        Geometry geom = WKT_READER.read("POLYGON ((100 360, 320 360, 320 150, 100 150, 100 360), \n"
                + "  (146 326, 198 326, 198 275, 146 275, 146 326), \n"
                + "  (230 240, 270 240, 270 190, 230 190, 230 240))");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertEquals("<Polygon><outerBoundaryIs><LinearRing><coordinates>"
                + "100.0,360.0 320.0,360.0 320.0,150.0 100.0,150.0 100.0,360.0</coordinates>"
                + "</LinearRing></outerBoundaryIs><innerBoundaryIs><LinearRing><coordinates>"
                + "146.0,326.0 198.0,326.0 198.0,275.0 146.0,275.0 146.0,326.0</coordinates>"
                + "</LinearRing></innerBoundaryIs><innerBoundaryIs><LinearRing>"
                + "<coordinates>230.0,240.0 270.0,240.0 270.0,190.0 230.0,190.0 230.0,240.0"
                + "</coordinates></LinearRing></innerBoundaryIs></Polygon>", sb.toString());
    }

    @Test
    public void createKMLMultiGeometry() throws Exception {
        Geometry geom = WKT_READER.read("GEOMETRYCOLLECTION (POLYGON ((100 360, 320 360, "
                + "320 150, 100 150, 100 360), \n"
                + "  (146 326, 198 326, 198 275, 146 275, 146 326), \n"
                + "  (230 240, 270 240, 270 190, 230 190, 230 240)), \n"
                + "  LINESTRING (140 420, 286 425, 383 315), \n"
                + "  POINT (79 305))");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertEquals("<MultiGeometry><Polygon><outerBoundaryIs><LinearRing>"
                + "<coordinates>100.0,360.0 320.0,360.0 320.0,150.0 100.0,150.0 100.0,360.0"
                + "</coordinates></LinearRing></outerBoundaryIs><innerBoundaryIs><LinearRing>"
                + "<coordinates>146.0,326.0 198.0,326.0 198.0,275.0 146.0,275.0 146.0,326.0"
                + "</coordinates></LinearRing></innerBoundaryIs><innerBoundaryIs><LinearRing>"
                + "<coordinates>230.0,240.0 270.0,240.0 270.0,190.0 230.0,190.0 230.0,240.0"
                + "</coordinates></LinearRing></innerBoundaryIs></Polygon><LineString>"
                + "<coordinates>140.0,420.0 286.0,425.0 383.0,315.0</coordinates>"
                + "</LineString><Point><coordinates>79.0,305.0</coordinates></Point></MultiGeometry>", sb.toString());
    }

    @Test
    public void exportKMZPointsBadSRID() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (47.58 2.19)',27572), true)");
        stat.execute("insert into KML_POINTS values(2, ST_Geomfromtext('POINT (47.59 1.06)',27572), false)");
        // Create a KMZ file
        try {
            stat.execute("CALL KMLWrite('target/kml_points.kmz', 'KML_POINTS')");
        } catch (SQLException ex) {
            assertTrue(true);
        } finally {
            stat.close();
            File kmzFile = new File("target/kml_points.kmz");
            assertTrue(kmzFile.delete());
            assertFalse(kmzFile.exists());
        }
    }

    @Test
    public void exportKMZPointsNoSRID() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (47.58 2.19)'), true)");
        stat.execute("insert into KML_POINTS values(2, ST_Geomfromtext('POINT (47.59 1.06)'), false)");
        // Create a KMZ file
        try {
            stat.execute("CALL KMLWrite('target/kml_points.kmz', 'KML_POINTS')");
        } catch (SQLException ex) {
            assertTrue(true);
        } finally {
            stat.close();
            File kmzFile = new File("target/kml_points.kmz");
            assertTrue(kmzFile.delete());
            assertFalse(kmzFile.exists());
        }
    }

    @Test
    public void testST_AsKml1() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (2.19 47.58)',4326), true)");
        // Create a KMZ file
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(the_geom) from KML_POINTS");
        res.next();
        assertEquals("<Point><coordinates>2.19,47.58</coordinates></Point>", res.getString(1));
        res.close();
        stat.close();
    }

    @Test
    public void testST_AsKml2() throws SQLException {
        Statement stat = connection.createStatement();
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(ST_Geomfromtext("
                + "    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100,"
                + "                -1.49 47.17 100)',4326), true, 2);");
        res.next();
        assertEquals("<LineString><extrude>1</extrude><kml:altitudeMode>relativeToGround</kml:altitudeMode><coordinates>-1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,47.19,100.0 -1.49,47.17,100.0</coordinates></LineString>", res.getString(1));
        res.close();
        stat.close();
    }

    @Test
    public void testST_AsKml3() throws SQLException {
        Statement stat = connection.createStatement();
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(ST_Geomfromtext("
                + "    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100,"
                + "                -1.49 47.17 100)',4326), true, 1);");
        res.next();
        assertEquals("<LineString><extrude>1</extrude><kml:altitudeMode>clampToGround</kml:altitudeMode><coordinates>-1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,47.19,100.0 -1.49,47.17,100.0</coordinates></LineString>", res.getString(1));
        res.close();
        stat.close();
    }

    @Test
    public void testST_AsKml4() throws SQLException {
        Statement stat = connection.createStatement();
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(ST_Geomfromtext("
                + "    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100,"
                + "                -1.49 47.17 100)',4326), true, 4);");
        res.next();
        assertEquals("<LineString><extrude>1</extrude><kml:altitudeMode>absolute</kml:altitudeMode><coordinates>-1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,47.19,100.0 -1.49,47.17,100.0</coordinates></LineString>", res.getString(1));
        res.close();
        stat.close();
    }

    @Test
    public void testST_AsKml5() throws SQLException {
        Statement stat = connection.createStatement();
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(ST_Geomfromtext("
                + "    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100,"
                + "                -1.49 47.17 100)',4326), true, 8);");
        res.next();
        assertEquals("<LineString><extrude>1</extrude><gx:altitudeMode>clampToSeaFloor</gx:altitudeMode><coordinates>-1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,47.19,100.0 -1.49,47.17,100.0</coordinates></LineString>", res.getString(1));
        res.close();
        stat.close();
    }

    @Test
    public void testST_AsKml6() throws SQLException {
        Statement stat = connection.createStatement();
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(ST_Geomfromtext("
                + "    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100,"
                + "                -1.49 47.17 100)',4326), true, 16);");
        res.next();
        assertEquals("<LineString><extrude>1</extrude><gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode><coordinates>-1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,47.19,100.0 -1.49,47.17,100.0</coordinates></LineString>", res.getString(1));
        res.close();
        stat.close();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testST_AsKml7() throws Throwable {
        Statement stat = connection.createStatement();
        try {
            stat.execute("SELECT ST_AsKml(ST_Geomfromtext("
                    + "    'LINESTRING(-1.53 47.24 100, -1.51 47.22 100, -1.50 47.19 100,"
                    + "                -1.49 47.17 100)',4326), true, 666);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        } finally {
            stat.close();
        }
    }

    @Test(expected = SQLException.class)
    public void importFileNoExist() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("CALL KMLRead('target/blabla.kml', 'BLABLA')");
    }

    @Test(expected = SQLException.class)
    public void importFileWithBadExtension() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File file = new File("target/area_export.blabla");
        file.createNewFile();
        stat.execute("CALL KMLRead('target/area_export.blabla', 'BLABLA')");
    }
}
