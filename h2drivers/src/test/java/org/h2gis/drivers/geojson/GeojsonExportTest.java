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
        assertTrue(res.getString(1).equals("{\"type\":\"Polygon\",\"coordinates\":[[1.0,2.0],[2.0,3.0]]}"));
        res.close();
        stat.close();
    }
}
