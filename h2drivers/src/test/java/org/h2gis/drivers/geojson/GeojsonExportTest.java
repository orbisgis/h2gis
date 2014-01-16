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
package org.h2gis.drivers.geojson;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Erwan Bocher
 */
public class GeojsonExportTest {

    private static Connection connection;
    private static final String DB_NAME = "GeojsonExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_AsGeoJson(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new GeojsonWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGeojsonPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POINTS");
        stat.execute("create table POINTS(idarea int primary key, the_geom POINT)");
        stat.execute("insert into POINTS values(1, 'POINT(1 2)')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from POINTS;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonLineString() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS LINES");
        stat.execute("create table LINES(idarea int primary key, the_geom LINESTRING)");
        stat.execute("insert into LINES values(1, 'LINESTRING(1 2, 2 3)')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from LINES;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0],[2.0,3.0]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonPolygon() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POLYGONS");
        stat.execute("create table POLYGONS(idarea int primary key, the_geom POLYGON)");
        stat.execute("insert into POLYGONS values(1, 'POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from POLYGONS;");
        res.next();
        assertTrue(res.getString(1).equals("{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[0.0,0.0],[2.0,0.0],[2.0,2.0],[0.0,2.0],[0.0,0.0]]]}"));
        res.close();
        stat.close();
    }

    @Test
    public void testGeojsonPolygonWithHole() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POLYGONS");
        stat.execute("create table POLYGONS(idarea int primary key, the_geom POLYGON)");
        stat.execute("insert into POLYGONS values(1, 'POLYGON ((101 345, 300 345, 300 100, 101 100, 101 345), \n"
                + "  (130 300, 190 300, 190 220, 130 220, 130 300), \n"
                + "  (220 200, 255 200, 255 138, 220 138, 220 200))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from POLYGONS;");
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
        stat.execute("DROP TABLE IF EXISTS MULTIPOINTS");
        stat.execute("create table MULTIPOINTS(idarea int primary key, the_geom MULTIPOINT)");
        stat.execute("insert into MULTIPOINTS values(1, 'MULTIPOINT ((190 320), (180 160), (394 276))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from MULTIPOINTS;");
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
        stat.execute("DROP TABLE IF EXISTS MULTILINESTRINGS");
        stat.execute("create table MULTILINESTRINGS(idarea int primary key, the_geom MULTILINESTRING)");
        stat.execute("insert into MULTILINESTRINGS values(1, 'MULTILINESTRING ((80 240, 174 356, 280 250), \n"
                + "  (110 140, 170 240, 280 360))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from MULTILINESTRINGS;");
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
        stat.execute("DROP TABLE IF EXISTS MULTIPOLYGONS");
        stat.execute("create table MULTIPOLYGONS(idarea int primary key, the_geom MULTIPOLYGON)");
        stat.execute("insert into MULTIPOLYGONS values(1, 'MULTIPOLYGON (((120 370, 180 370, 180 290, 120 290, 120 370)), \n"
                + "  ((162 245, 234 245, 234 175, 162 175, 162 245)), \n"
                + "  ((210 390, 235 390, 235 308, 210 308, 210 390)))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from MULTIPOLYGONS;");
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
        stat.execute("DROP TABLE IF EXISTS GEOMETRYCOLLECTIONS");
        stat.execute("create table GEOMETRYCOLLECTIONS(idarea int primary key, the_geom GEOMETRY)");
        stat.execute("insert into GEOMETRYCOLLECTIONS values(1, 'GEOMETRYCOLLECTION ("
                + "POLYGON ((100 360, 140 360, 140 320, 100 320, 100 360)), \n"
                + "  POINT (130 290), \n"
                + "  LINESTRING (190 360, 190 280))')");
        ResultSet res = stat.executeQuery("SELECT ST_AsGeoJson(the_geom) from GEOMETRYCOLLECTIONS;");
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
    public void testWriteGeojsonPointWithProperties() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POINTS");
        stat.execute("create table POINTS(idarea int primary key, the_geom POINT, orbisgis boolean)");
        stat.execute("insert into POINTS values(1, 'POINT(1 2)', true)");
        stat.execute("insert into POINTS values(2, 'POINT(10 200)', false)");
        stat.execute("CALL GeoJsonWrite('/tmp/points_properties.geojson', 'POINTS');");
        stat.close();
    }

    @Test
    public void testWriteGeojsonPoint() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POINTS");
        stat.execute("create table POINTS(the_geom POINT)");
        stat.execute("insert into POINTS values( 'POINT(1 2)')");
        stat.execute("insert into POINTS values( 'POINT(10 200)')");
        stat.execute("CALL GeoJsonWrite('/tmp/points.geojson', 'POINTS');");
        stat.close();
    }
}
