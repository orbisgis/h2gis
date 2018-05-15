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

package org.h2gis.functions.io.geojson;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.sql.*;

import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 * @author Hai Trung Pham
 */
public class GeojsonImportExportTest {

    private static Connection connection;
    private static final String DB_NAME = "GeojsonExportTest";
    private static final WKTReader WKTREADER = new WKTReader();

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_AsGeoJSON(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new GeoJsonWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new GeoJsonRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_GeomFromGeoJSON(), "");
        
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGeojsonPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
        stat.execute("create table TABLE_POINT(idarea int primary key, the_geom POINT)");
        stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POINT;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonLineString() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_LINE");
        stat.execute("create table TABLE_LINE(idarea int primary key, the_geom LINESTRING)");
        stat.execute("insert into TABLE_LINE values(1, 'LINESTRING(1 2, 2 3)')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_LINE;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0],[2.0,3.0]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonPolygon() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
        stat.execute("create table TABLE_POLYGON(idarea int primary key, the_geom POLYGON)");
        stat.execute("insert into TABLE_POLYGON values(1, 'POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POLYGON;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[0.0,0.0],[2.0,0.0],[2.0,2.0],[0.0,2.0],[0.0,0.0]]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonPolygonWithHole() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
        stat.execute("create table TABLE_POLYGON(idarea int primary key, the_geom POLYGON)");
        stat.execute("insert into TABLE_POLYGON values(1, 'POLYGON ((101 345, 300 345, 300 100, 101 100, 101 345), \n"
                + "  (130 300, 190 300, 190 220, 130 220, 130 300), \n"
                + "  (220 200, 255 200, 255 138, 220 138, 220 200))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POLYGON;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"Polygon\",\"coordinates\":["
                + "[[101.0,345.0],[300.0,345.0],[300.0,100.0],[101.0,100.0],[101.0,345.0]],"
                + "[[130.0,300.0],[190.0,300.0],[190.0,220.0],[130.0,220.0],[130.0,300.0]],"
                + "[[220.0,200.0],[255.0,200.0],[255.0,138.0],[220.0,138.0],[220.0,200.0]]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonMultiPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINT");
        stat.execute("create table TABLE_MULTIPOINT(idarea int primary key, the_geom MULTIPOINT)");
        stat.execute("insert into TABLE_MULTIPOINT values(1, 'MULTIPOINT ((190 320), (180 160), (394 276))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_MULTIPOINT;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"MultiPoint\",\"coordinates\":["
                + "[190.0,320.0],"
                + "[180.0,160.0],"
                + "[394.0,276.0]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonMultiLineString() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRING");
        stat.execute("create table TABLE_MULTILINESTRING(idarea int primary key, the_geom MULTILINESTRING)");
        stat.execute("insert into TABLE_MULTILINESTRING values(1, 'MULTILINESTRING ((80 240, 174 356, 280 250), \n"
                + "  (110 140, 170 240, 280 360))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_MULTILINESTRING;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"MultiLineString\",\"coordinates\":["
                + "[[80.0,240.0],[174.0,356.0],[280.0,250.0]],"
                + "[[110.0,140.0],[170.0,240.0],[280.0,360.0]]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonMultiPolygon() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON");
        stat.execute("create table TABLE_MULTIPOLYGON(idarea int primary key, the_geom MULTIPOLYGON)");
        stat.execute("insert into TABLE_MULTIPOLYGON values(1, 'MULTIPOLYGON (((120 370, 180 370, 180 290, 120 290, 120 370)), \n"
                + "  ((162 245, 234 245, 234 175, 162 175, 162 245)), \n"
                + "  ((210 390, 235 390, 235 308, 210 308, 210 390)))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_MULTIPOLYGON;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"MultiPolygon\",\"coordinates\":["
                + "[[[120.0,370.0],[180.0,370.0],[180.0,290.0],[120.0,290.0],[120.0,370.0]]],"
                + "[[[162.0,245.0],[234.0,245.0],[234.0,175.0],[162.0,175.0],[162.0,245.0]]],"
                + "[[[210.0,390.0],[235.0,390.0],[235.0,308.0],[210.0,308.0],[210.0,390.0]]]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonGeometryCollection() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_GEOMETRYCOLLECTION");
        stat.execute("create table TABLE_GEOMETRYCOLLECTION(idarea int primary key, the_geom GEOMETRY)");
        stat.execute("insert into TABLE_GEOMETRYCOLLECTION values(1, 'GEOMETRYCOLLECTION ("
                + "POLYGON ((100 360, 140 360, 140 320, 100 320, 100 360)), \n"
                + "  POINT (130 290), \n"
                + "  LINESTRING (190 360, 190 280))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_GEOMETRYCOLLECTION;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"GeometryCollection\",\"geometries\":["
                + "{\"type\":\"Polygon\",\"coordinates\":["
                + "[[100.0,360.0],[140.0,360.0],[140.0,320.0],[100.0,320.0],[100.0,360.0]]"
                + "]},"
                + "{\"type\":\"Point\",\"coordinates\":[130.0,290.0]},"
                + "{\"type\":\"LineString\",\"coordinates\":[[190.0,360.0],[190.0,280.0]]}]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.execute("create table TABLE_POINTS(the_geom POINT)");
        stat.execute("insert into TABLE_POINTS values( 'POINT(1 2)')");
        stat.execute("insert into TABLE_POINTS values( 'POINT(10 200)')");
        stat.execute("CALL GeoJsonWrite('target/points.geojson', 'TABLE_POINTS');");
        stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(1 2)")));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonPointProperties() throws Exception {
        Statement stat = connection.createStatement();
        try {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(the_geom POINT, id INT, climat VARCHAR)");
            stat.execute("insert into TABLE_POINTS values( 'POINT(1 2)', 1, 'bad')");
            stat.execute("insert into TABLE_POINTS values( 'POINT(10 200)', 2, 'good')");
            stat.execute("CALL GeoJsonWrite('target/points_properties.geojson', 'TABLE_POINTS');");
            stat.execute("CALL GeoJsonRead('target/points_properties.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(1 2)")));
            assertTrue((res.getInt(2) == 1));
            assertTrue((res.getString(3).equals("bad")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
            assertTrue((res.getInt(2) == 2));
            assertTrue((res.getString(3).equals("good")));
            res.close();
        } finally {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.close();
        }
    }

    @Test
    public void testWriteReadGeojsonLinestring() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS");
        stat.execute("create table TABLE_LINESTRINGS(the_geom LINESTRING)");
        stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)')");
        stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)')");
        stat.execute("CALL GeoJsonWrite('target/lines.geojson', 'TABLE_LINESTRINGS');");
        stat.execute("CALL GeoJsonRead('target/lines.geojson', 'TABLE_LINESTRINGS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 2, 5 3, 10 19)")));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 10, 20 15)")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonMultiLinestring() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS");
        stat.execute("create table TABLE_MULTILINESTRINGS(the_geom MULTILINESTRING)");
        stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((90 220, 260 320, 280 200), \n"
                + "  (150 140, 210 190, 210 220))')");
        stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((126 324, 280 300), \n"
                + "  (140 190, 320 220))')");
        stat.execute("CALL GeoJsonWrite('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS');");
        stat.execute("CALL GeoJsonRead('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTILINESTRINGS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((90 220, 260 320, 280 200), \n"
                + "  (150 140, 210 190, 210 220))")));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((126 324, 280 300), \n"
                + "  (140 190, 320 220))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonMultiPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS");
        stat.execute("create table TABLE_MULTIPOINTS(the_geom MULTIPOINT)");
        stat.execute("insert into TABLE_MULTIPOINTS values( 'MULTIPOINT ((140 260), (246 284))')");
        stat.execute("insert into TABLE_MULTIPOINTS values( 'MULTIPOINT ((150 290), (180 170), (266 275))')");
        stat.execute("CALL GeoJsonWrite('target/multipoints.geojson', 'TABLE_MULTIPOINTS');");
        stat.execute("CALL GeoJsonRead('target/multipoints.geojson', 'TABLE_MULTIPOINTS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOINTS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((150 290), (180 170), (266 275))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonPolygon() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
        stat.execute("create table TABLE_POLYGON(the_geom POLYGON)");
        stat.execute("insert into TABLE_POLYGON values( 'POLYGON ((110 320, 220 320, 220 200, 110 200, 110 320))')");
        stat.execute("CALL GeoJsonWrite('target/polygon.geojson', 'TABLE_POLYGON');");
        stat.execute("CALL GeoJsonRead('target/polygon.geojson', 'TABLE_POLYGON_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POLYGON_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((110 320, 220 320, 220 200, 110 200, 110 320))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonPolygonWithHoles() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
        stat.execute("create table TABLE_POLYGON(the_geom POLYGON)");
        stat.execute("insert into TABLE_POLYGON values( 'POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300), \n"
                + "  (120 280, 170 280, 170 220, 120 220, 120 280), \n"
                + "  (191 195, 250 195, 250 140, 191 140, 191 195))')");
        stat.execute("CALL GeoJsonWrite('target/polygonholes.geojson', 'TABLE_POLYGON');");
        stat.execute("CALL GeoJsonRead('target/polygonholes.geojson', 'TABLE_POLYGON_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POLYGON_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300), \n"
                + "  (120 280, 170 280, 170 220, 120 220, 120 280), \n"
                + "  (191 195, 250 195, 250 140, 191 140, 191 195))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonMultiPolygon() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON");
        stat.execute("create table TABLE_MULTIPOLYGON(the_geom MULTIPOLYGON)");
        stat.execute("insert into TABLE_MULTIPOLYGON values( 'MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)), \n"
                + "  ((151 235, 236 235, 236 176, 151 176, 151 235)), \n"
                + "  ((200 350, 245 350, 245 278, 200 278, 200 350)))')");
        stat.execute("CALL GeoJsonWrite('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON');");
        stat.execute("CALL GeoJsonRead('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOLYGON_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)), \n"
                + "  ((151 235, 236 235, 236 176, 151 176, 151 235)), \n"
                + "  ((200 350, 245 350, 245 278, 200 278, 200 350)))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonGeometryCollection() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_GEOMETRYCOLLECTION");
        stat.execute("create table TABLE_GEOMETRYCOLLECTION(the_geom GEOMETRY)");
        stat.execute("insert into TABLE_GEOMETRYCOLLECTION values( 'GEOMETRYCOLLECTION (POLYGON ((80 320, 110 320, 110 280, 80 280, 80 320)), \n"
                + "  LINESTRING (70 190, 77 200, 150 240), \n"
                + "  POINT (160 300))')");
        stat.execute("CALL GeoJsonWrite('target/geometrycollection.geojson', 'TABLE_GEOMETRYCOLLECTION');");
        stat.execute("CALL GeoJsonRead('target/geometrycollection.geojson', 'TABLE_GEOMETRYCOLLECTION_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_GEOMETRYCOLLECTION_READ;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        assertTrue(geom.getNumGeometries()==3);
        assertTrue(geom.getGeometryN(0).equals(WKTREADER.read("POLYGON ((80 320, 110 320, 110 280, 80 280, 80 320))")));
        assertTrue(geom.getGeometryN(1).equals(WKTREADER.read("LINESTRING (70 190, 77 200, 150 240)")));
        assertTrue(geom.getGeometryN(2).equals(WKTREADER.read("POINT (160 300)")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_GEOMETRYCOLLECTION_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonSingleMultiPolygon() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON");
        stat.execute("create table TABLE_MULTIPOLYGON(the_geom MULTIPOLYGON)");
        stat.execute("insert into TABLE_MULTIPOLYGON values( 'MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)))')");
        stat.execute("CALL GeoJsonWrite('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON');");
        stat.execute("CALL GeoJsonRead('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOLYGON_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonSingleMultiPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS");
        stat.execute("create table TABLE_MULTIPOINTS(the_geom MULTIPOINT)");
        stat.execute("insert into TABLE_MULTIPOINTS values( 'MULTIPOINT ((140 260))')");
        stat.execute("CALL GeoJsonWrite('target/multipoints.geojson', 'TABLE_MULTIPOINTS');");
        stat.execute("CALL GeoJsonRead('target/multipoints.geojson', 'TABLE_MULTIPOINTS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOINTS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonSingleMultiLinestring() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS");
        stat.execute("create table TABLE_MULTILINESTRINGS(the_geom MULTILINESTRING)");
        stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((90 220, 260 320, 280 200))')");
        stat.execute("CALL GeoJsonWrite('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS');");
        stat.execute("CALL GeoJsonRead('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTILINESTRINGS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((90 220, 260 320, 280 200))")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonCRS() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_CRS");
        stat.execute("create table TABLE_POINTS_CRS(the_geom POINT CHECK ST_SRID(THE_GEOM)=4326, id INT, climat VARCHAR)");
        stat.execute("insert into TABLE_POINTS_CRS values( ST_GEOMFROMTEXT('POINT(1 2)', 4326), 1, 'bad')");
        stat.execute("insert into TABLE_POINTS_CRS values( ST_GEOMFROMTEXT('POINT(10 200)',4326), 2, 'good')");
        stat.execute("CALL GeoJsonWrite('target/points_crs_properties.geojson', 'TABLE_POINTS_CRS');");
        stat.execute("CALL GeoJsonRead('target/points_crs_properties.geojson', 'TABLE_POINTS_CRS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_CRS_READ;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        assertTrue(geom.equals(WKTREADER.read("POINT(1 2)")));
        assertTrue((geom.getSRID() == 4326));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_CRS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadBadSRID() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
        stat.execute("create table TABLE_POINTS(the_geom POINT CHECK ST_SRID(THE_GEOM)=9999, id INT, climat VARCHAR)");
        stat.execute("insert into TABLE_POINTS values( ST_GEOMFROMTEXT('POINT(1 2)', 9999), 1, 'bad')");
        stat.execute("insert into TABLE_POINTS values( ST_GEOMFROMTEXT('POINT(10 200)',9999), 2, 'good')");
        stat.execute("CALL GeoJsonWrite('target/points_properties.geojson', 'TABLE_POINTS');");
        stat.execute("CALL GeoJsonRead('target/points_properties.geojson', 'TABLE_POINTS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        assertTrue(geom.equals(WKTREADER.read("POINT(1 2)")));
        assertTrue((geom.getSRID() == 0));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.close();
    }

    @Test
    public void testWriteReadGeojsonMixedGeometries() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MIXED");
        stat.execute("create table TABLE_MIXED(the_geom GEOMETRY)");
        stat.execute("insert into TABLE_MIXED values( 'MULTIPOINT ((140 260), (246 284))')");
        stat.execute("insert into TABLE_MIXED values( 'LINESTRING (150 290, 180 170, 266 275)')");
        stat.execute("CALL GeoJsonWrite('target/mixedgeom.geojson', 'TABLE_MIXED');");
        stat.execute("CALL GeoJsonRead('target/mixedgeom.geojson', 'TABLE_MIXED_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MIXED_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING (150 290, 180 170, 266 275)")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MIXED_READ");
        stat.close();
    }
    
    @Test
    public void testReadGeoJSON1() throws Exception {
        Statement stat = connection.createStatement();        
        ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('{\"type\":\"Point\",\"coordinates\":[10,1]}')");
        res.next();
        assertTrue(res.getString(1).equals("POINT (10 1)"));
        stat.close();
    }
    
    @Test
    public void testReadGeoJSON2() throws Exception {
        Statement stat = connection.createStatement();        
        ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('{\"type\":\"LineString\",\"coordinates\":[[1,1],[10,10]]}')");
        res.next();
        assertTrue(res.getString(1).equals("LINESTRING (1 1, 10 10)"));
        stat.close();
    }
    
    @Test
    public void testReadGeoJSON3() throws Exception {
        Statement stat = connection.createStatement();        
        ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('{ \"type\": \"MultiPoint\", \"coordinates\": [ [100, 0], [101, 1] ]}')");
        res.next();
        assertTrue(res.getString(1).equals("MULTIPOINT ((100 0), (101 1))"));
        stat.close();
    }
    
    @Test
    public void testWriteReadNullGeojsonPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
        stat.execute("create table TABLE_POINTS(the_geom POINT, id int)");
        stat.execute("insert into TABLE_POINTS values( null, 1)");
        stat.execute("insert into TABLE_POINTS values( 'POINT(10 200)', 2)");
        stat.execute("CALL GeoJsonWrite('target/null_point.geojson', 'TABLE_POINTS');");
        stat.execute("CALL GeoJsonRead('target/null_point.geojson', 'TABLE_POINTS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
        res.next();
        assertNull(res.getObject(1));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.close();
    }
    
    
    @Test
    public void testWriteReadlGeojsonComplex() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_COMPLEX, TABLE_COMPLEX_READ");
        stat.execute("create table TABLE_COMPLEX(the_geom geometry, gid long)");
        stat.execute("insert into TABLE_COMPLEX values( null, 1463655908000)");
        stat.execute("insert into TABLE_COMPLEX values( 'POINT(10 200)', 1)");
        stat.execute("insert into TABLE_COMPLEX values( 'LINESTRING(15 20, 0 0)',  NULL)");
        stat.execute("CALL GeoJsonWrite('target/complex.geojson', 'TABLE_COMPLEX');");
        stat.execute("CALL GeoJsonRead('target/complex.geojson', 'TABLE_COMPLEX_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_COMPLEX_READ;");
        res.next();
        assertNull(res.getObject(1));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(15 20, 0 0)")));
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.close();
    }
    
    
    @Test
    public void testReadComplexFile() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_COMPLEX_READ");
        stat.execute("CALL GeoJsonRead("+ StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("complex.geojson").getPath()) + ", 'TABLE_COMPLEX_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_COMPLEX_READ;");
        res.next();
        assertNull(res.getObject(1));
        assertTrue(res.getString(7).equals("#C5E805"));
        assertNull(res.getObject(31));
        assertNull(res.getObject(32));
        res.next();
        assertEquals(10.2d, ((Geometry) res.getObject(1)).getCoordinate().z, 0);
        assertEquals(0.87657195d, res.getDouble(31), 0);
        assertEquals(234.16d, res.getDouble(32), 0);
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.close();
    }

    @Test
    public void testReadProperties() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_PROPERTIES_READ;");
        stat.execute("CALL GeoJsonRead("+ StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("data.geojson").getPath()) + ", 'TABLE_PROPERTIES_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_PROPERTIES_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((7.49587624983838 48.5342070572556, 7.49575955525988 48.5342516702309, 7.49564286068138 48.5342070572556, 7.49564286068138 48.534117831187, 7.49575955525988 48.5340732180938, 7.49587624983838 48.534117831187, 7.49587624983838 48.5342070572556))")));
        assertEquals(-105576, res.getDouble(2), 0);
        assertEquals(275386, res.getDouble(3), 0);
        assertEquals(56.848998452816424, res.getDouble(4), 0);
        assertEquals(55.87291487481895, res.getDouble(5), 0);
        assertEquals(0.0, res.getDouble(6), 0);
        assertTrue(res.getString(7).equals("null"));
        assertEquals(2, res.getDouble(8), 0);
        assertTrue(res.getString(9).equals("2017-01-19T18:29:26+01:00"));
        assertTrue(res.getBigDecimal(10).toString().equals("1484846966000"));
        assertTrue(res.getString(11).equals("2017-01-19T18:29:27+01:00"));
        assertTrue(res.getBigDecimal(12).toString().equals("1484846967000"));
        assertTrue(res.getString(13).equals("{}"));
        Object[] tinyArray = {(Integer) 13, "string", "{}"};
        Object[] expectedResult = {(Integer) 49, (Double) 40.0, "{}", "string", tinyArray};
        Object[] result = (Object[]) res.getObject(14);
        assertArrayEquals(expectedResult, result);
        expectedResult = new Object[]{58, 47, 58, 57, 58, 49, 58, 51, 58, 58, 49, 57, 58, 58, 49, 58, 57, 56, 57, 58, 59, 58, 57, 58, 49, 47, 48, 57, 48, 58, 57, 57, 51, 56, 52, 57, 51, 57, 49, 58, 55, 58, 50, 48, 48, 52, 56, 57, 48, 58, 52, 48, 53, 50, 57, 54, 57, 47, 58, 57, 54, 54, 53, 56, 57, 55, 58, 58, 57, 58, 57, 57};
        result = (Object[]) res.getObject(15);
        assertArrayEquals(expectedResult, result);
        res.next();
        res.close();
        stat.close();
    }

    @Test
    public void testWriteReadProperties() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_PROPERTIES;");
        stat.execute("CALL GeoJsonRead("+ StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("data.geojson").getPath()) + ", 'TABLE_PROPERTIES');");
        stat.execute("CALL GeoJsonWrite('target/properties_read.geojson','TABLE_PROPERTIES')");
        stat.execute("DROP TABLE IF EXISTS TABLE_PROPERTIES_READ;");
        stat.execute("CALL GeoJsonRead('target/properties_read.geojson', 'TABLE_PROPERTIES_READ')");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_PROPERTIES_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((7.49587624983838 48.5342070572556, 7.49575955525988 48.5342516702309, 7.49564286068138 48.5342070572556, 7.49564286068138 48.534117831187, 7.49575955525988 48.5340732180938, 7.49587624983838 48.534117831187, 7.49587624983838 48.5342070572556))")));
        assertEquals(-105576, res.getDouble(2), 0);
        assertEquals(275386, res.getDouble(3), 0);
        assertEquals(56.848998452816424, res.getDouble(4), 0);
        assertEquals(55.87291487481895, res.getDouble(5), 0);
        assertEquals(0.0, res.getDouble(6), 0);
        assertTrue(res.getString(7).equals("null"));
        assertEquals(2, res.getDouble(8), 0);
        assertTrue(res.getString(9).equals("2017-01-19T18:29:26+01:00"));
        assertTrue(res.getBigDecimal(10).toString().equals("1484846966000"));
        assertTrue(res.getString(11).equals("2017-01-19T18:29:27+01:00"));
        assertTrue(res.getBigDecimal(12).toString().equals("1484846967000"));
        assertTrue(res.getString(13).equals("{}"));
        Object[] tinyArray = {(Integer) 13, "string", "{}"};
        Object[] expectedResult = {(Integer) 49, (Double) 40.0, "{}", "string", tinyArray};
        Object[] result = (Object[]) res.getObject(14);
        assertArrayEquals(expectedResult, result);
        expectedResult = new Object[]{58, 47, 58, 57, 58, 49, 58, 51, 58, 58, 49, 57, 58, 58, 49, 58, 57, 56, 57, 58, 59, 58, 57, 58, 49, 47, 48, 57, 48, 58, 57, 57, 51, 56, 52, 57, 51, 57, 49, 58, 55, 58, 50, 48, 48, 52, 56, 57, 48, 58, 52, 48, 53, 50, 57, 54, 57, 47, 58, 57, 54, 54, 53, 56, 57, 55, 58, 58, 57, 58, 57, 57};
        result = (Object[]) res.getObject(15);
        assertArrayEquals(expectedResult, result);
        res.next();
        res.close();
        stat.close();
    }

    @Test
    public void testWriteReadNullField() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_NULL;");
        stat.execute("CALL GeoJsonRead("+ StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("null.geojson").getPath()) + ", 'TABLE_NULL');");
        stat.execute("CALL GeoJsonWrite('target/null_read.geojson','TABLE_NULL')");
        stat.execute("DROP TABLE IF EXISTS TABLE_NULL_READ");
        stat.execute("CALL GeoJsonRead('target/null_read.geojson', 'TABLE_NULL_READ')");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_NULL_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)) == null);
        assertTrue(res.getString(2).equals("string"));
        assertTrue(res.getString(3).equals("{}"));
        Object[] tinyArray1 = {13, "string", "{}", "null"};
        Object[] expectedResult1 = {49, 40.0, "{}", "string", tinyArray1};
        Object[] result1 = (Object[]) res.getObject(4);
        assertArrayEquals(expectedResult1, result1);
        res.next();
        assertTrue(((Geometry) res.getObject(1)) == null);
        assertEquals(true, res.getBoolean(2));
        assertTrue(res.getString(3).equals("{}"));
        Object[] expectedResult2 = {1,2};
        Object[] result2 = (Object[]) res.getObject(4);
        assertArrayEquals(expectedResult2, result2);
        res.next();
        assertTrue(((Geometry) res.getObject(1)) == null);
        assertEquals(2, res.getInt(2), 0);
        assertTrue(res.getString(3).equals("{}"));
        Object[] expectedResult3 = {1,2};
        Object[] result3 = (Object[]) res.getObject(4);
        assertArrayEquals(expectedResult3, result3);
        res.next();
        assertTrue(((Geometry) res.getObject(1)) == null);
        assertTrue(res.getString(2).equals("{}"));
        assertTrue(res.getString(3).equals("{}"));
        Object[] expectedResult4 = {1,2};
        Object[] result4 = (Object[]) res.getObject(4);
        assertArrayEquals(expectedResult4, result4);
        res.next();
        assertTrue(((Geometry) res.getObject(1)) == null);
        assertTrue(res.getString(2).equals("(5, 6, 6)"));
        assertTrue(res.getString(3).equals("{}"));
        Object[] expectedResult5 = {1,2};
        Object[] result5 = (Object[]) res.getObject(4);
        assertArrayEquals(expectedResult5, result5);
        res.next();
        assertTrue(((Geometry) res.getObject(1)) == null);
        assertTrue(res.getString(2).equals("null"));
        assertTrue(res.getString(3).equals("{}"));
        Object[] expectedResult6 = {1,2};
        Object[] result6 = (Object[]) res.getObject(4);
        assertArrayEquals(expectedResult6, result6);
        res.close();
        stat.close();
    }

    @Test
    public void testReadAdditionalProps() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_ADDITIONALPROPS_READ;");
        stat.execute("CALL GeoJsonRead("+ StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("additionalProps.geojson").getPath()) + ", 'TABLE_ADDITIONALPROPS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_ADDITIONALPROPS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT (100.0 0.0)")));
        assertEquals(105576, res.getDouble(2), 0);
        res.next();
        res.close();
        stat.close();
    }
    
    @Test
    public void testWriteReadEmptyTable() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
        stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        stat.execute("create table TABLE_POINTS(the_geom POINT)");
        stat.execute("CALL GeoJsonWrite('target/points.geojson', 'TABLE_POINTS');");
        stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
        ResultSetMetaData rsmd = res.getMetaData();
        assertTrue(rsmd.getColumnCount()==0);
        assertTrue(!res.next());
        stat.close();
    }
    
    @Test
    public void testWriteReadGeojsonProperties() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_MIXED_PROPS");
        stat.execute("create table TABLE_MIXED_PROPS(the_geom GEOMETRY, decimal_field DECIMAL(4, 2), numeric_field NUMERIC(4,2), real_field REAL)");
        stat.execute("insert into TABLE_MIXED_PROPS values( 'MULTIPOINT ((140 260), (246 284))', 12.12, 14.23, 23)");
        stat.execute("CALL GeoJsonWrite('target/mixedgeomprops.geojson', 'TABLE_MIXED_PROPS');");
        stat.execute("CALL GeoJsonRead('target/mixedgeomprops.geojson', 'TABLE_MIXED_PROPS_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MIXED_PROPS_READ;");
        res.next();
        assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
        assertEquals(12.12, res.getDouble(2), 0);
        assertEquals(14.23, res.getDouble(3), 0);
        assertEquals(23, res.getDouble(4), 0);
        res.close();
        stat.execute("DROP TABLE IF EXISTS TABLE_MIXED_PROPS_READ");
        stat.close();
    }

}
