/*
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
package org.h2gis.drivers.kml;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class KMLExporterTest {

    private static Connection connection;
    private static final String DB_NAME = "KMLExportTest";
    private static WKTReader WKT_READER = new WKTReader();

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new KMLWrite(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_AsKml(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportKMLPoints() throws SQLException {
        Statement stat = connection.createStatement();
        File kmlFile = new File("target/kml_points.kml");
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (2.19 47.58)', 4326), true)");
        stat.execute("insert into KML_POINTS values(2, ST_Geomfromtext('POINT (1.06 47.59)',  4326), false)");
        // Create a KML file
        stat.execute("CALL KMLWrite('target/kml_points.kml', 'KML_POINTS')");
        assertTrue(kmlFile.exists());
        stat.close();
    }

    @Test
    public void exportKMLLineString() throws SQLException {
        Statement stat = connection.createStatement();
        File kmlFile = new File("target/kml_lineString.kml");
        stat.execute("DROP TABLE IF EXISTS KML_LINESTRING");
        stat.execute("create table KML_LINESTRING(id int primary key, the_geom LINESTRING)");
        stat.execute("insert into KML_LINESTRING values(1, ST_Geomfromtext('LINESTRING (2.19 47.58,1.19 46.58)', 4326))");
        stat.execute("insert into KML_LINESTRING values(2, ST_Geomfromtext('LINESTRING (1.06 47.59,1.19 46.58)', 4326))");
        // Create a KML file
        stat.execute("CALL KMLWrite('target/kml_lineString.kml', 'KML_LINESTRING')");
        assertTrue(kmlFile.exists());
        stat.close();
    }

    @Test
    public void exportKMZPoints() throws SQLException {
        Statement stat = connection.createStatement();
        File kmzFile = new File("target/kml_points.kmz");
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (2.19 47.58)',4326), true)");
        stat.execute("insert into KML_POINTS values(2, ST_Geomfromtext('POINT (1.06 47.59)',4326), false)");
        // Create a KMZ file
        stat.execute("CALL KMLWrite('target/kml_points.kmz', 'KML_POINTS')");
        assertTrue(kmzFile.exists());
        stat.close();
    }

    @Test
    public void testCreateKMLPoint() throws Exception {
        Geometry geom = WKT_READER.read("POINT(1 2)");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom,  sb);
        assertTrue(sb.toString().equals("<Point><coordinates>1.0,2.0</coordinates></Point>"));
    }

    @Test
    public void testCreateKMLLineString() throws Exception {
        Geometry geom = WKT_READER.read("LINESTRING(1 1, 2 2, 3 3)");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertTrue(sb.toString().equals("<LineString><coordinates>1.0,1.0 2.0,2.0 3.0,3.0</coordinates></LineString>"));
    }

    @Test
    public void testCreateKMLPolygon() throws Exception {
        Geometry geom = WKT_READER.read("POLYGON ((140 370, 60 150, 220 120, 310 180, 372 355, 240 260, 140 370))");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertTrue(sb.toString().equals("<Polygon><outerBoundaryIs><LinearRing><coordinates>"
                + "140.0,370.0 60.0,150.0 220.0,120.0 310.0,180.0 372.0,355.0 240.0,260.0 140.0,370.0"
                + "</coordinates></LinearRing></outerBoundaryIs></Polygon>"));
    }

    @Test
    public void testCreateKMLPolygonWithHoles() throws Exception {
        Geometry geom = WKT_READER.read("POLYGON ((100 360, 320 360, 320 150, 100 150, 100 360), \n"
                + "  (146 326, 198 326, 198 275, 146 275, 146 326), \n"
                + "  (230 240, 270 240, 270 190, 230 190, 230 240))");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertTrue(sb.toString().equals("<Polygon><outerBoundaryIs><LinearRing><coordinates>"
                + "100.0,360.0 320.0,360.0 320.0,150.0 100.0,150.0 100.0,360.0</coordinates>"
                + "</LinearRing></outerBoundaryIs><innerBoundaryIs><LinearRing><coordinates>"
                + "146.0,326.0 198.0,326.0 198.0,275.0 146.0,275.0 146.0,326.0</coordinates>"
                + "</LinearRing></innerBoundaryIs><innerBoundaryIs><LinearRing>"
                + "<coordinates>230.0,240.0 270.0,240.0 270.0,190.0 230.0,190.0 230.0,240.0"
                + "</coordinates></LinearRing></innerBoundaryIs></Polygon>"));
    }

    @Test
    public void testCreateKMLMultiGeometry() throws Exception {
        Geometry geom = WKT_READER.read("GEOMETRYCOLLECTION (POLYGON ((100 360, 320 360, "
                + "320 150, 100 150, 100 360), \n"
                + "  (146 326, 198 326, 198 275, 146 275, 146 326), \n"
                + "  (230 240, 270 240, 270 190, 230 190, 230 240)), \n"
                + "  LINESTRING (140 420, 286 425, 383 315), \n"
                + "  POINT (79 305))");
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geom, sb);
        assertTrue(sb.toString().equals("<MultiGeometry><Polygon><outerBoundaryIs><LinearRing>"
                + "<coordinates>100.0,360.0 320.0,360.0 320.0,150.0 100.0,150.0 100.0,360.0"
                + "</coordinates></LinearRing></outerBoundaryIs><innerBoundaryIs><LinearRing>"
                + "<coordinates>146.0,326.0 198.0,326.0 198.0,275.0 146.0,275.0 146.0,326.0"
                + "</coordinates></LinearRing></innerBoundaryIs><innerBoundaryIs><LinearRing>"
                + "<coordinates>230.0,240.0 270.0,240.0 270.0,190.0 230.0,190.0 230.0,240.0"
                + "</coordinates></LinearRing></innerBoundaryIs></Polygon><LineString>"
                + "<coordinates>140.0,420.0 286.0,425.0 383.0,315.0</coordinates>"
                + "</LineString><Point><coordinates>79.0,305.0</coordinates></Point></MultiGeometry>"));
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
        }
    }

    @Test
    public void testST_AsKml() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, ST_Geomfromtext('POINT (2.19 47.58)',4326), true)");
        // Create a KMZ file
        ResultSet res = stat.executeQuery("SELECT ST_AsKml(the_geom) from KML_POINTS");
        res.next();
        assertTrue(res.getString(1).equals("<Point><coordinates>2.19,47.58</coordinates></Point>"));
        res.close();
        stat.close();
    }
}
