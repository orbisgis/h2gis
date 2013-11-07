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
package org.h2gis.h2spatialext;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.h2gis.utilities.SFSUtilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Nicolas Fortin
 */
public class SpatialFunctionTest {
    private static Connection connection;
    private static final String DB_NAME = "SpatialFunctionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME,false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64)," +
                " boundary MULTIPOLYGON);" +
                "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0," +
                "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('forests') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeEmptyGeometryCollection() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("create table test(the_geom GEOMETRY, value Integer);" +
                "insert into test VALUES (ST_GeomFromText('MULTILINESTRING EMPTY'),108)," +
                " (ST_GeomFromText('MULTIPOINT EMPTY'),109)," +
                " (ST_GeomFromText('MULTIPOLYGON EMPTY'),110)," +
                " (ST_GeomFromText('GEOMETRYCOLLECTION EMPTY'),111);");
        ResultSet rs = st.executeQuery("SELECT the_geom::Geometry , value FROM ST_Explode('test') ORDER BY value");
        assertTrue(rs.next());
        assertEquals(108,rs.getInt(2));
        assertEquals("LINESTRING EMPTY", ((Geometry)rs.getObject(1)).toText());
        assertTrue(rs.next());
        assertEquals(109,rs.getInt(2));
        assertNull(rs.getObject(1));    // POINT EMPTY does not exists (not supported in WKB)
        assertTrue(rs.next());
        assertEquals(110,rs.getInt(2));
        assertEquals("POLYGON EMPTY", ((Geometry)rs.getObject(1)).toText());
        assertTrue(rs.next());
        assertEquals(111,rs.getInt(2));
        assertNull(rs.getObject(1));
        rs.close();
        st.execute("drop table test");
    }


    @Test
    public void test_ST_Extent() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);" +
        "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
        ResultSet rs = st.executeQuery("select ST_Extent(the_geom) tableEnv from ptClouds;");
        assertTrue(rs.next());
        Object resultObj = rs.getObject("tableEnv");
        assertTrue(resultObj instanceof Envelope);
        Envelope result = (Envelope)resultObj;
        Envelope expected = new Envelope(-5, 99, -21, 124);
        assertEquals(expected.getMinX(),result.getMinX(),1e-12);
        assertEquals(expected.getMaxX(),result.getMaxX(),1e-12);
        assertEquals(expected.getMinY(),result.getMinY(),1e-12);
        assertEquals(expected.getMaxY(),result.getMaxY(),1e-12);
        assertFalse(rs.next());
        st.execute("drop table ptClouds");
        st.close();
    }

    @Test
    public void test_TableEnvelope() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);" +
                "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
        Envelope result = SFSUtilities.getTableEnvelope(connection, SFSUtilities.splitCatalogSchemaTableName("ptClouds"), "");
        Envelope expected = new Envelope(-5, 99, -21, 124);
        assertEquals(expected.getMinX(),result.getMinX(),1e-12);
        assertEquals(expected.getMaxX(),result.getMaxX(),1e-12);
        assertEquals(expected.getMinY(),result.getMinY(),1e-12);
        assertEquals(expected.getMaxY(),result.getMaxY(),1e-12);
        st.execute("drop table ptClouds");
    }

    @Test
    public void testAggregateProgression() {

    }

    @Test
    public void test_ST_IsRectangle() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE input_table(the_geom Polygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)); " +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_IsRectangle(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
    }

    @Test
    public void test_ST_IsValid() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE input_table(the_geom Polygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)); " +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 10 -5, 0 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_IsValid(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
    }

    @Test
    public void test_ST_Covers() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE input_table(smallc Polygon, bigc Polygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_Buffer(ST_GeomFromText('POINT(1 2)'), 10)," +
                "ST_Buffer(ST_GeomFromText('POINT(1 2)'), 20));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_Covers(smallc, smallc)," +
                "ST_Covers(smallc, bigc)," +
                "ST_Covers(bigc, smallc)," +
                "ST_Covers(bigc, ST_ExteriorRing(bigc))," +
                "ST_Contains(bigc, ST_ExteriorRing(bigc)) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertEquals(false, rs.getBoolean(2));
        assertEquals(true, rs.getBoolean(3));
        assertEquals(true, rs.getBoolean(4));
        assertEquals(false, rs.getBoolean(5));
    }
}
