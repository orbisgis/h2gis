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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.SFSUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class SpatialFunctionTest {
    private static Connection connection;
    private static final String DB_NAME = "SpatialFunctionTest";
    private static GeometryFactory FACTORY;
    private static WKTReader WKT_READER;
    public static final double TOLERANCE = 10E-10;
    private static final String POLYGON2D =
            "'POLYGON ((181 124, 87 162, 76 256, 166 315, 286 325, 373 255, " +
                    "387 213, 377 159, 351 121, 298 101, 234 56, 181 124), " +
                    "(165 244, 227 219, 234 300, 168 288, 165 244), " +
                    "(244 130, 305 135, 324 186, 306 210, 272 206, 206 174, 244 130))'";
    private static final String UNIT_SQUARE =
            "'POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))'";
    private static final String MULTIPOLYGON2D = "'MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))'";
    private static final String LINESTRING2D = "'LINESTRING (1 1, 2 1, 2 2, 1 2, 1 1)'";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, false);
        CreateSpatialExtension.initSpatialExtension(connection);
        FACTORY = new GeometryFactory();
        WKT_READER = new WKTReader();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception {
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
    public void test_ST_ExplodeEmptyGeometryCollection() throws Exception {
        Statement st = connection.createStatement();
        st.execute("create table test(the_geom GEOMETRY, value Integer);" +
                "insert into test VALUES (ST_GeomFromText('MULTILINESTRING EMPTY'),108)," +
                " (ST_GeomFromText('MULTIPOINT EMPTY'),109)," +
                " (ST_GeomFromText('MULTIPOLYGON EMPTY'),110)," +
                " (ST_GeomFromText('GEOMETRYCOLLECTION EMPTY'),111);");
        ResultSet rs = st.executeQuery("SELECT the_geom::Geometry , value FROM ST_Explode('test') ORDER BY value");
        assertTrue(rs.next());
        assertEquals(108, rs.getInt(2));
        assertEquals("LINESTRING EMPTY", ((Geometry) rs.getObject(1)).toText());
        assertTrue(rs.next());
        assertEquals(109, rs.getInt(2));
        assertNull(rs.getObject(1));    // POINT EMPTY does not exists (not supported in WKB)
        assertTrue(rs.next());
        assertEquals(110, rs.getInt(2));
        assertEquals("POLYGON EMPTY", ((Geometry) rs.getObject(1)).toText());
        assertTrue(rs.next());
        assertEquals(111, rs.getInt(2));
        assertNull(rs.getObject(1));
        rs.close();
        st.execute("drop table test");
    }


    @Test
    public void test_ST_Extent() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);" +
                "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
        ResultSet rs = st.executeQuery("select ST_Extent(the_geom) tableEnv from ptClouds;");
        assertTrue(rs.next());
        Object resultObj = rs.getObject("tableEnv");
        assertTrue(resultObj instanceof Geometry);
        Envelope result = ((Geometry) resultObj).getEnvelopeInternal();
        Envelope expected = new Envelope(-5, 99, -21, 124);
        assertEquals(expected.getMinX(), result.getMinX(), 1e-12);
        assertEquals(expected.getMaxX(), result.getMaxX(), 1e-12);
        assertEquals(expected.getMinY(), result.getMinY(), 1e-12);
        assertEquals(expected.getMaxY(), result.getMaxY(), 1e-12);
        assertFalse(rs.next());
        st.execute("drop table ptClouds");
        st.close();
    }

    @Test
    public void test_TableEnvelope() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);" +
                "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154))," +
                "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
        Envelope result = SFSUtilities.getTableEnvelope(connection, SFSUtilities.splitCatalogSchemaTableName("ptClouds"), "");
        Envelope expected = new Envelope(-5, 99, -21, 124);
        assertEquals(expected.getMinX(), result.getMinX(), 1e-12);
        assertEquals(expected.getMaxX(), result.getMaxX(), 1e-12);
        assertEquals(expected.getMinY(), result.getMinY(), 1e-12);
        assertEquals(expected.getMaxY(), result.getMaxY(), 1e-12);
        st.execute("drop table ptClouds");
    }

    @Test
    public void testAggregateProgression() {

    }

    @Test
    public void test_ST_IsRectangle() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(the_geom Polygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)); " +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_IsRectangle(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_IsValid() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(the_geom Polygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)); " +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 10 -5, 0 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_IsValid(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_Covers() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(smallc Polygon, bigc Polygon);" +
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
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_DWithin() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geomA Polygon, geomB Polygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1), " +
                "ST_PolyFromText('POLYGON ((12 0, 14 0, 14 6, 12 6, 12 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_DWithin(geomA, geomB, 2.0)," +
                "ST_DWithin(geomA, geomB, 1.0)," +
                "ST_DWithin(geomA, geomB, -1.0)," +
                "ST_DWithin(geomA, geomB, 3.0)," +
                "ST_DWithin(geomA, geomA, -1.0)," +
                "ST_DWithin(geomA, geomA, 0.0)," +
                "ST_DWithin(geomA, geomA, 5000.0) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertEquals(false, rs.getBoolean(2));
        assertEquals(false, rs.getBoolean(3));
        assertEquals(true, rs.getBoolean(4));
        assertEquals(false, rs.getBoolean(5));
        assertEquals(true, rs.getBoolean(6));
        assertEquals(true, rs.getBoolean(7));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_XYZMinMax() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(line Linestring);" +
                "INSERT INTO input_table VALUES(" +
                "ST_LineFromText('LINESTRING(1 2 3, 4 5 6)', 101));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_XMin(line), ST_XMax(line), " +
                "ST_YMin(line), ST_YMax(line)," +
                "ST_ZMin(line), ST_ZMax(line)" +
                " FROM input_table;");
        assertTrue(rs.next());
        assertEquals(1.0, rs.getDouble(1), 0.0);
        assertEquals(4.0, rs.getDouble(2), 0.0);
        assertEquals(2.0, rs.getDouble(3), 0.0);
        assertEquals(5.0, rs.getDouble(4), 0.0);
        assertEquals(3.0, rs.getDouble(5), 0.0);
        assertEquals(6.0, rs.getDouble(6), 0.0);
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_Rotate() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('LINESTRING(1 3, 1 1, 2 1)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Rotate(geom, pi())," +
                "ST_Rotate(geom, pi() / 3), " +
                "ST_Rotate(geom, pi()/2, 1.0, 1.0), " +
                "ST_Rotate(geom, -pi()/2, ST_GeomFromText('POINT(2 1)')) " +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((LineString) rs.getObject(1)).equalsExact(
                FACTORY.createLineString(
                        new Coordinate[]{new Coordinate(2, 1),
                                new Coordinate(2, 3),
                                new Coordinate(1, 3)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(2)).equalsExact(
                FACTORY.createLineString(
                        new Coordinate[]{
                                new Coordinate(
                                        (1 - 3.0 / 2) * Math.cos(Math.PI / 3) - (3 - 2) * Math.sin(Math.PI / 3) + 3.0 / 2,
                                        (1 - 3.0 / 2) * Math.sin(Math.PI / 3) + (3 - 2) * Math.cos(Math.PI / 3) + 2),
                                new Coordinate(
                                        (1 - 3.0 / 2) * Math.cos(Math.PI / 3) - (1 - 2) * Math.sin(Math.PI / 3) + 3.0 / 2,
                                        (1 - 3.0 / 2) * Math.sin(Math.PI / 3) + (1 - 2) * Math.cos(Math.PI / 3) + 2),
                                new Coordinate(
                                        (2 - 3.0 / 2) * Math.cos(Math.PI / 3) - (1 - 2) * Math.sin(Math.PI / 3) + 3.0 / 2,
                                        (2 - 3.0 / 2) * Math.sin(Math.PI / 3) + (1 - 2) * Math.cos(Math.PI / 3) + 2)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(3)).equalsExact(
                FACTORY.createLineString(
                        new Coordinate[]{new Coordinate(-1, 1),
                                new Coordinate(1, 1),
                                new Coordinate(1, 2)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(4)).equalsExact(
                FACTORY.createLineString(
                        new Coordinate[]{new Coordinate(4, 2),
                                new Coordinate(2, 2),
                                new Coordinate(2, 1)}),
                TOLERANCE));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_Scale() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(twoDLine Geometry, threeDLine Geometry);" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('LINESTRING(1 2, 4 5)')," +
                "ST_GeomFromText('LINESTRING(1 2 3, 4 5 6)'));");
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_Scale(twoDLine, 0.5, 0.75), ST_Scale(threeDLine, 0.5, 0.75), " +
                "ST_Scale(twoDLine, 0.5, 0.75, 1.2), ST_Scale(threeDLine, 0.5, 0.75, 1.2), " +
                "ST_Scale(twoDLine, 0.0, -1.0, 2.0), ST_Scale(threeDLine, 0.0, -1.0, 2.0) " +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((LineString) rs.getObject(1)).equalsExact(
                FACTORY.createLineString(new Coordinate[]{
                        new Coordinate(0.5, 1.5),
                        new Coordinate(2, 3.75)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(2)).equalsExact(
                FACTORY.createLineString(new Coordinate[]{
                        new Coordinate(0.5, 1.5, 3),
                        new Coordinate(2, 3.75, 6)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(3)).equalsExact(
                FACTORY.createLineString(new Coordinate[]{
                        new Coordinate(0.5, 1.5),
                        new Coordinate(2, 3.75)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(4)).equalsExact(
                FACTORY.createLineString(new Coordinate[]{
                        new Coordinate(0.5, 1.5, 3.6),
                        new Coordinate(2, 3.75, 7.2)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(5)).equalsExact(
                FACTORY.createLineString(new Coordinate[]{
                        new Coordinate(0, -2),
                        new Coordinate(0, -5)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(6)).equalsExact(
                FACTORY.createLineString(new Coordinate[]{
                        new Coordinate(0, -2, 6),
                        new Coordinate(0, -5, 12)}),
                TOLERANCE));
    }

    @Test
    public void test_ST_3DLength() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES" +
                "(ST_GeomFromText('LINESTRING(1 4, 15 7, 16 17)',2249))," +
                "(ST_GeomFromText('LINESTRING(1 4 3, 15 7 9, 16 17 22)',2249))," +
                "(ST_GeomFromText('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22)," +
                "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249))," +
                "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249))," +
                "(ST_GeomFromText('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))',2249))," +
                "(ST_GeomFromText('MULTIPOLYGON(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0)," +
                "(-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))',2249))," +
                "(ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22)," +
                "POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))',2249));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_3DLength(geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(Math.sqrt(205) + Math.sqrt(101), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(241) + Math.sqrt(270), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(241) + Math.sqrt(270) + 3 + Math.sqrt(2), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(6, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(2) + 2 * Math.sqrt(5) + Math.sqrt(10), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(16 + 2 * Math.sqrt(13), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(241) + Math.sqrt(270) + Math.sqrt(2) + 2 * Math.sqrt(5) +
                Math.sqrt(10), rs.getDouble(1), 0.0);
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_3DLengthEqualsST_LengthFor2DGeometry() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES" +
                "(ST_GeomFromText('LINESTRING(1 4, 15 7, 16 17)',2249))," +
                "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_Length(geom), ST_3DLength(geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(rs.getDouble(1), rs.getDouble(2), 0.0);
        assertTrue(rs.next());
        assertEquals(rs.getDouble(1), rs.getDouble(2), 0.0);
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_CoordDim() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('POINT(1 2)',1));" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('LINESTRING(0 0, 1 1 2)',1));" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('LINESTRING (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)',1));" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))',1));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_CoordDim(geom)" +
                        " FROM input_table;");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_CompactnessRatio() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES" +
                "(ST_Buffer(ST_GeomFromText('POINT(1 2)'), 10))," +
                "(ST_GeomFromText(" + POLYGON2D + "))," +
                "(ST_GeomFromText(" + UNIT_SQUARE + "))," +
                "(ST_GeomFromText(" + MULTIPOLYGON2D + "))," +
                "(ST_GeomFromText('POINT(1 2)'))," +
                "(ST_GeomFromText(" + LINESTRING2D + "))," +
                "(ST_GeomFromText('POLYGON((0 0 0, 3 0 0, 3 2 0, 0 2 1, 0 0 0))'));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_CompactnessRatio(geom) FROM input_table;");
        assertTrue(rs.next());
        // This _is_ a circle.
        assertEquals(1, rs.getDouble(1), 0.01);
        assertTrue(rs.next());
        assertEquals(0.5127681416229469, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(Math.PI) / 2, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        // For the next three values, the SQL value is NULL but the double
        // value is 0.0.
        assertEquals(0.0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0.0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0.0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        // For a 3D geometry, the 2D perimeter and area are used (projection).
        assertEquals(Math.sqrt(6 * Math.PI) / 5, rs.getDouble(1), 0.000000000000001);
        assertFalse(rs.next());
    }

    @Test
    public void test_ST_MakePoint() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_MakePoint(1.4, -3.7), " +
                "ST_MakePoint(1.4, -3.7, 6.2);");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("POINT(1.4 -3.7)"), rs.getObject(1));
        assertEquals(WKT_READER.read("POINT(1.4 -3.7 6.2)"), rs.getObject(2));
        assertFalse(rs.next());
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_MakeEllipse() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_MakeEllipse(ST_MakePoint(0, 0), 6, 4)," +
                "ST_MakeEllipse(ST_MakePoint(-1, 4), 2, 4)," +
                "ST_MakeEllipse(ST_MakePoint(4, -5), 4, 4)," +
                "ST_Buffer(ST_MakePoint(4, -5), 2);");
        assertTrue(rs.next());
        Polygon ellipse1 = (Polygon) rs.getObject(1);
        final Envelope ellipse1EnvelopeInternal = ellipse1.getEnvelopeInternal();
        assertEquals(101, ellipse1.getCoordinates().length);
        assertTrue(ellipse1EnvelopeInternal.centre().equals2D(new Coordinate(0, 0)));
        assertEquals(6, ellipse1EnvelopeInternal.getWidth(), 0);
        assertEquals(4, ellipse1EnvelopeInternal.getHeight(), 0);
        Polygon ellipse2 = (Polygon) rs.getObject(2);
        final Envelope ellipse2EnvelopeInternal = ellipse2.getEnvelopeInternal();
        assertEquals(101, ellipse2.getCoordinates().length);
        assertTrue(ellipse2EnvelopeInternal.centre().equals2D(new Coordinate(-1, 4)));
        assertEquals(2, ellipse2EnvelopeInternal.getWidth(), 0);
        assertEquals(4, ellipse2EnvelopeInternal.getHeight(), 0);
        Polygon circle = (Polygon) rs.getObject(3);
        final Envelope circleEnvelopeInternal = circle.getEnvelopeInternal();
        assertEquals(101, circle.getCoordinates().length);
        assertTrue(circleEnvelopeInternal.centre().equals2D(new Coordinate(4, -5)));
        assertEquals(4, circleEnvelopeInternal.getWidth(), 0);
        assertEquals(4, circleEnvelopeInternal.getHeight(), 0);
        Polygon bufferCircle = (Polygon) rs.getObject(4);
        // This test shows that the only difference between a circle
        // constructed using ST_MakeEllipse and a circle contructed using
        // ST_Buffer is the number of line segments in the approximation.
        // ST_MakeEllipse is more fine-grained (100 segments rather than 32).
        final Envelope bufferCircleEnvelopeInternal = bufferCircle.getEnvelopeInternal();
        assertEquals(33, bufferCircle.getCoordinates().length);
        assertTrue(bufferCircleEnvelopeInternal.centre().equals2D(circleEnvelopeInternal.centre()));
        assertEquals(circleEnvelopeInternal.getWidth(), bufferCircleEnvelopeInternal.getWidth(), 0);
        assertEquals(circleEnvelopeInternal.getHeight(), bufferCircleEnvelopeInternal.getHeight(), 0);
        assertFalse(rs.next());
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_PointsToLine() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(multi_point MultiPoint, point Point);" +
                "INSERT INTO input_table VALUES(" +
                "ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154), " +
                "ST_PointFromText('POINT(5 5)',2154));" +
                "INSERT INTO input_table VALUES(" +
                "ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154)," +
                "ST_PointFromText('POINT(1 2)',2154));" +
                "INSERT INTO input_table VALUES(" +
                "ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154)," +
                "ST_PointFromText('POINT(3 4)',2154));" +
                "INSERT INTO input_table(point) VALUES(" +
                "ST_PointFromText('POINT(99 3)',2154));");
        ResultSet rs = st.executeQuery("SELECT ST_PointsToLine(point), " +
                "ST_PointsToLine(multi_point) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("LINESTRING(5 5, 1 2, 3 4, 99 3)"), rs.getObject(1));
        assertEquals(WKT_READER.read("LINESTRING(5 5, 1 2, 3 4, 99 3," +
                "-5 12, 11 22, 34 41, 65 124," +
                "1 12, 5 -21, 9 41, 32 124)"), rs.getObject(2));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ToMultiPoint() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(empty_multi_point MultiPoint," +
                "multi_point MultiPoint, point Point, " +
                "line LineString, " +
                "polygon Polygon, multi_polygon MultiPolygon);" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('MULTIPOINT EMPTY',2154)," +
                "ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154)," +
                "ST_PointFromText('POINT(5 5)',2154)," +
                "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154)," +
                "ST_MPolyFromText('MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26)," +
                "(52 18,66 23,73 9,48 6,52 18))," +
                "((59 18,67 18,67 13,59 13,59 18)))',2154));");
        ResultSet rs = st.executeQuery("SELECT ST_ToMultiPoint(empty_multi_point), " +
                "ST_ToMultiPoint(multi_point), " +
                "ST_ToMultiPoint(point), " +
                "ST_ToMultiPoint(line), " +
                "ST_ToMultiPoint(polygon), " +
                "ST_ToMultiPoint(multi_polygon) " +
                "FROM input_table;");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("MULTIPOINT EMPTY"), rs.getObject(1));
        assertEquals(WKT_READER.read("MULTIPOINT(5 5, 1 2, 3 4, 99 3)"), rs.getObject(2));
        assertEquals(WKT_READER.read("MULTIPOINT(5 5)"), rs.getObject(3));
        assertEquals(WKT_READER.read("MULTIPOINT(5 5, 1 2, 3 4, 99 3)"), rs.getObject(4));
        assertEquals(WKT_READER.read("MULTIPOINT(0 0, 10 0, 10 5, 0 5, 0 0)"), rs.getObject(5));
        assertEquals(WKT_READER.read("MULTIPOINT(28 26,28 0,84 0,84 42,28 26," +
                                "52 18,66 23,73 9,48 6,52 18," +
                                "59 18,67 18,67 13,59 13,59 18)"), rs.getObject(6));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ToMultiLine() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(" +
                "point Point," +
                "empty_line_string LineString," +
                "line LineString, " +
                "polygon Polygon, " +
                "polygon_with_holes Polygon, " +
                "multi_polygon MultiPolygon," +
                "collection Geometry);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PointFromText('POINT(2 4)',2154)," +
                "ST_GeomFromText('LINESTRING EMPTY',2154)," +
                "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0)," +
                "(1 1, 2 1, 2 4, 1 4, 1 1))',2154)," +
                "ST_MPolyFromText('MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26)," +
                "(52 18,66 23,73 9,48 6,52 18))," +
                "((59 18,67 18,67 13,59 13,59 18)))',2154)," +
                "ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22)," +
                "POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))'));");
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_ToMultiLine(point), " +
                "ST_ToMultiLine(empty_line_string), " +
                "ST_ToMultiLine(line), " +
                "ST_ToMultiLine(polygon), " +
                "ST_ToMultiLine(polygon_with_holes), " +
                "ST_ToMultiLine(multi_polygon), " +
                "ST_ToMultiLine(collection)" +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((MultiLineString) rs.getObject(1)).isEmpty());
        assertTrue(((MultiLineString) rs.getObject(2)).isEmpty());
        assertTrue(WKT_READER.read("MULTILINESTRING((5 5, 1 2, 3 4, 99 3))").equalsExact(
                (MultiLineString) rs.getObject(3), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0, 10 5, 0 5, 0 0))").equalsExact(
                (MultiLineString) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0, 10 5, 0 5, 0 0)," +
                "(1 1, 2 1, 2 4, 1 4, 1 1))").equalsExact(
                (MultiLineString) rs.getObject(5), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((28 26,28 0,84 0,84 42,28 26)," +
                "(52 18,66 23,73 9,48 6,52 18)," +
                "(59 18,67 18,67 13,59 13,59 18))").equalsExact(
                (MultiLineString) rs.getObject(6), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((1 4 3, 15 7 9, 16 17 22)," +
                "(1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))").equalsExact(
                (MultiLineString) rs.getObject(7), TOLERANCE));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Holes() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(empty_line_string LineString," +
                "line LineString, " +
                "polygon Polygon, " +
                "polygon_with_holes Polygon, " +
                "collection Geometry);" +
                "INSERT INTO input_table VALUES(" +
                "ST_GeomFromText('LINESTRING EMPTY',2154)," +
                "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0)," +
                "(1 1, 2 1, 2 4, 1 4, 1 1))',2154)," +
                "ST_GeomFromText('GEOMETRYCOLLECTION(POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0)," +
                "(1 1, 2 1, 2 4, 1 4, 1 1))," +
                "POLYGON ((11 6, 14 6, 14 9, 11 9, 11 6)," +
                "(12 7, 14 7, 14 8, 12 8, 12 7)))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Holes(empty_line_string), " +
                "ST_Holes(line), " +
                "ST_Holes(polygon), " +
                "ST_Holes(polygon_with_holes), " +
                "ST_Holes(collection)" +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((GeometryCollection) rs.getObject(1)).isEmpty());
        assertTrue(((GeometryCollection) rs.getObject(2)).isEmpty());
        assertTrue(((GeometryCollection) rs.getObject(3)).isEmpty());
        assertTrue(WKT_READER.read("GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1)))")
                .equalsExact((GeometryCollection) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1))," +
                "POLYGON((12 7, 14 7, 14 8, 12 8, 12 7)))")
                .equalsExact((GeometryCollection) rs.getObject(5), TOLERANCE));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ToMultiSegments() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(" +
                "point Point," +
                "empty_line_string LineString," +
                "line LineString, " +
                "multi_line MultiLineString, " +
                "polygon Polygon, " +
                "polygon_with_holes Polygon, " +
                "collection Geometry);" +
                "INSERT INTO input_table VALUES(" +
                "ST_PointFromText('POINT(5 5)', 2154)," +
                "ST_GeomFromText('LINESTRING EMPTY',2154)," +
                "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154)," +
                "ST_GeomFromText('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22)," +
                "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154)," +
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0)," +
                "(1 1, 2 1, 2 4, 1 4, 1 1)," +
                "(7 1, 8 1, 8 3, 7 3, 7 1))',2154)," +
                "ST_GeomFromText('GEOMETRYCOLLECTION(POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0)," +
                "(1 1, 2 1, 2 4, 1 4, 1 1)," +
                "(7 1, 8 1, 8 3, 7 3, 7 1))," +
                "POINT(2 3)," +
                "LINESTRING (8 7, 9 5, 11 3))'));");
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_ToMultiSegments(point), " +
                "ST_ToMultiSegments(empty_line_string), " +
                "ST_ToMultiSegments(line), " +
                "ST_ToMultiSegments(multi_line), " +
                "ST_ToMultiSegments(polygon), " +
                "ST_ToMultiSegments(polygon_with_holes), " +
                "ST_ToMultiSegments(collection)" +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((MultiLineString) rs.getObject(1)).isEmpty());
        assertTrue(((MultiLineString) rs.getObject(2)).isEmpty());
        assertTrue(WKT_READER.read("MULTILINESTRING((5 5, 1 2), (1 2, 3 4), (3 4, 99 3))")
                .equalsExact((MultiLineString) rs.getObject(3), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((1 4 3, 15 7 9), (15 7 9, 16 17 22)," +
                "(0 0 0, 1 0 0), (1 0 0, 1 2 0), (1 2 0, 0 2 1))")
                .equalsExact((MultiLineString) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0))")
                .equalsExact((MultiLineString) rs.getObject(5), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0)," +
                "(1 1, 2 1), (2 1, 2 4), (2 4, 1 4), (1 4, 1 1)," +
                "(7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1))")
                .equalsExact((MultiLineString) rs.getObject(6), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0)," +
                "(1 1, 2 1), (2 1, 2 4), (2 4, 1 4), (1 4, 1 1)," +
                "(7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1)," +
                "(8 7, 9 5), (9 5, 11 3))")
                .equalsExact((MultiLineString) rs.getObject(7), TOLERANCE));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_FurthestCoordinate() throws Exception {
        Statement st = connection.createStatement();
        //            5
        //
        //       +---------+
        //       |         |
        //       |         |
        //           234
        //       |         |
        //       |         |
        //       1---------+
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(point Point);" +
                "INSERT INTO input_table VALUES" +
                "(ST_GeomFromText('POINT(0 0)'))," +
                "(ST_GeomFromText('POINT(4 2.5)'))," +
                "(ST_GeomFromText('POINT(5 2.5)'))," +
                "(ST_GeomFromText('POINT(6 2.5)'))," +
                "(ST_GeomFromText('POINT(5 7)'));");
        ResultSet rs = st.executeQuery("SELECT ST_FurthestCoordinate(point, " +
                "ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')) " +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(10 5)")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((10 0), (10 5))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((0 0), (10 0), (10 5), (0 5))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((0 0), (0 5))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((0 0), (10 0))")));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ClosestCoordinate() throws Exception {
        Statement st = connection.createStatement();
        //            5
        //
        //       +---------+
        //       |         |
        //       |         |
        //           234
        //       |         |
        //       |         |
        //       1---------+
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(point Point);" +
                "INSERT INTO input_table VALUES" +
                "(ST_GeomFromText('POINT(0 0)'))," +
                "(ST_GeomFromText('POINT(4 2.5)'))," +
                "(ST_GeomFromText('POINT(5 2.5)'))," +
                "(ST_GeomFromText('POINT(6 2.5)'))," +
                "(ST_GeomFromText('POINT(5 7)'));");
        ResultSet rs = st.executeQuery("SELECT ST_ClosestCoordinate(point, " +
                "ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')) " +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(0 0)")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((0 0), (0 5))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((0 0), (10 0), (10 5), (0 5))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((10 0), (10 5))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((0 5), (10 5))")));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_LocateAlong() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES" +
                "(ST_GeomFromText('LINESTRING(100 300, 400 300, 400 100)'))," +
                "(ST_GeomFromText('POLYGON((100 100, 400 100, 400 300, 100 300, 100 100)," +
                "(150 130, 200 130, 200 220, 150 130))'))," +
                "(ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(100 300, 400 300, 400 100), " +
                "POLYGON((100 100, 400 100, 400 300, 100 300, 100 100)))'));");
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_LocateAlong(geom, 0.5, 10)," +
                "ST_LocateAlong(geom, 0.5, -10)," +
                "ST_LocateAlong(geom, 0.3, 10)," +
                "ST_LocateAlong(geom, 0.0, 10)," +
                "ST_LocateAlong(geom, 1.0, 10)," +
                "ST_LocateAlong(geom, 2.0, 10)," +
                "ST_LocateAlong(geom, -1.0, 10) " +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 310), (410 200))")));
        assertTrue(((MultiPoint) rs.getObject(2)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 290), (390 200))")));
        assertTrue(((MultiPoint) rs.getObject(3)).
                equalsTopo(WKT_READER.read("MULTIPOINT((190 310), (410 240))")));
        assertTrue(((MultiPoint) rs.getObject(4)).
                equalsTopo(WKT_READER.read("MULTIPOINT((100 310), (410 300))")));
        assertTrue(((MultiPoint) rs.getObject(5)).
                equalsTopo(WKT_READER.read("MULTIPOINT((400 310), (410 100))")));
        assertTrue(((MultiPoint) rs.getObject(6)).
                equalsTopo(WKT_READER.read("MULTIPOINT((700 310), (410 -100))")));
        assertTrue(((MultiPoint) rs.getObject(7)).
                equalsTopo(WKT_READER.read("MULTIPOINT((-200 310), (410 500))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 110), (250 290), (110 200), (390 200))")));
        assertTrue(((MultiPoint) rs.getObject(2)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 90), (250 310), (90 200), (410 200))")));
        assertTrue(((MultiPoint) rs.getObject(3)).
                equalsTopo(WKT_READER.read("MULTIPOINT((190 110), (310 290), (110 240), (390 160))")));
        assertTrue(((MultiPoint) rs.getObject(4)).
                equalsTopo(WKT_READER.read("MULTIPOINT((100 110), (390 100), (400 290), (110 300))")));
        assertTrue(((MultiPoint) rs.getObject(5)).
                equalsTopo(WKT_READER.read("MULTIPOINT((400 110), (390 300), (100 290), (110 100))")));
        assertTrue(((MultiPoint) rs.getObject(6)).
                equalsTopo(WKT_READER.read("MULTIPOINT((700 110), (390 500), (-200 290), (110 -100))")));
        assertTrue(((MultiPoint) rs.getObject(7)).
                equalsTopo(WKT_READER.read("MULTIPOINT((-200 110), (390 -100), (700 290), (110 500))")));
        assertTrue(rs.next());
        assertTrue(((MultiPoint) rs.getObject(1)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 110), (250 290), (110 200), (390 200), " +
                        "(250 310), (410 200))")));
        assertTrue(((MultiPoint) rs.getObject(2)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 90), (250 310), (90 200), (410 200), " +
                        "(250 290), (390 200))")));
        assertTrue(((MultiPoint) rs.getObject(3)).
                equalsTopo(WKT_READER.read("MULTIPOINT((190 110), (310 290), (110 240), (390 160), " +
                        "(190 310), (410 240))")));
        assertTrue(((MultiPoint) rs.getObject(4)).
                equalsTopo(WKT_READER.read("MULTIPOINT((100 110), (390 100), (400 290), (110 300), " +
                        "(100 310), (410 300))")));
        assertTrue(((MultiPoint) rs.getObject(5)).
                equalsTopo(WKT_READER.read("MULTIPOINT((400 110), (390 300), (100 290), (110 100), " +
                        "(400 310), (410 100))")));
        assertTrue(((MultiPoint) rs.getObject(6)).
                equalsTopo(WKT_READER.read("MULTIPOINT((700 110), (390 500), (-200 290), (110 -100), " +
                        "(700 310), (410 -100))")));
        assertTrue(((MultiPoint) rs.getObject(7)).
                equalsTopo(WKT_READER.read("MULTIPOINT((-200 110), (390 -100), (700 290), (110 500), " +
                        "(-200 310), (410 500))")));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ClosestPoint() throws Exception {
        //            5
        //
        //       +---------+
        //       |         |
        //       |         |
        //           234
        //       |         |
        //       |         |
        //       1---------+
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(point Point);" +
                "INSERT INTO input_table VALUES" +
                "(ST_GeomFromText('POINT(0 0)'))," +
                "(ST_GeomFromText('POINT(4 2.5)'))," +
                "(ST_GeomFromText('POINT(5 2.5)'))," +
                "(ST_GeomFromText('POINT(6 2.5)'))," +
                "(ST_GeomFromText('POINT(5 7)'));");
        ResultSet rs = st.executeQuery("SELECT " +
                // All points except (5 7) are contained inside the polygon.
                "ST_ClosestPoint(" +
                "    ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')," +
                "    point)," +
                // Only (0 0) intersects the linestring.
                "ST_ClosestPoint(" +
                "    ST_GeomFromText('LINESTRING(0 0, 10 0, 10 5, 0 5, 0 0)')," +
                "    point)," +
                // The closest point on a point to another geometry is always
                // the point itself.
                "ST_Equals(" +
                "    ST_ClosestPoint(" +
                "        point," +
                "        ST_GeomFromText('LINESTRING(0 0, 10 0, 10 5, 0 5, 0 0)'))," +
                "    point)" +
                "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(0 0)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(0 0)")));
        assertEquals(true, rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(4 2.5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(4 0)")));
        assertEquals(true, rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(5 2.5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(5 0)")));
        assertEquals(true, rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(6 2.5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(6 0)")));
        assertEquals(true, rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(5 5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(5 5)")));
        assertEquals(true, rs.getBoolean(3));
        assertFalse(rs.next());
        // In this example, there are infinitely many closest points, but
        // ST_ClosestPoint returns the first one it finds. (The polygon listed
        // as the second parameter remains the same, but its coordinates are
        // listed in a different order).
        //
        //       +---------+
        //       |         |  |
        //       |         |  |\
        //       |         |  | \
        //       |         |   \_\
        //       +---------+     \\
        //
        rs = st.executeQuery("SELECT " +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')," +
                "    ST_GeomFromText('POLYGON((13 2, 15 0, 13 4, 13 2))'))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')," +
                "    ST_GeomFromText('POLYGON((13 4, 13 2, 15 0, 13 4))'));");
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(10 2)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(10 4)")));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ClosestPoint2() throws Exception {
        // This unit test shows that the point returned by ST_ClosestPoint
        // depends on the orientations of geometries A and B. If they have the
        // same orientation, the point returned is the first point found in A.
        // If they have opposite orientation, the point returned is the point
        // of A closest to the first point found in B.
        //
        //       + +
        //      a| |b
        //       + +
        //
        Statement st = connection.createStatement();
        final String a = "'LINESTRING(0 0, 0 1))'";
        final String aReversed = "'LINESTRING(0 1, 0 0))'";
        final String b = "'LINESTRING(1 0, 1 1))'";
        final String bReversed = "'LINESTRING(1 1, 1 0))'";
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + a + ")," +
                "    ST_GeomFromText(" + b + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + a + ")," +
                "    ST_GeomFromText(" + bReversed + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + aReversed + ")," +
                "    ST_GeomFromText(" + b + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + aReversed + ")," +
                "    ST_GeomFromText(" + bReversed + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + b + ")," +
                "    ST_GeomFromText(" + a + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + bReversed + ")," +
                "    ST_GeomFromText(" + a + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + b + ")," +
                "    ST_GeomFromText(" + aReversed + "))," +
                "ST_ClosestPoint(" +
                "    ST_GeomFromText(" + bReversed + ")," +
                "    ST_GeomFromText(" + aReversed + "));");
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(0 0)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(0 1)")));
        assertTrue(((Point) rs.getObject(3)).
                equalsTopo(WKT_READER.read("POINT(0 0)")));
        assertTrue(((Point) rs.getObject(4)).
                equalsTopo(WKT_READER.read("POINT(0 1)")));
        assertTrue(((Point) rs.getObject(5)).
                equalsTopo(WKT_READER.read("POINT(1 0)")));
        assertTrue(((Point) rs.getObject(6)).
                equalsTopo(WKT_READER.read("POINT(1 0)")));
        assertTrue(((Point) rs.getObject(7)).
                equalsTopo(WKT_READER.read("POINT(1 1)")));
        assertTrue(((Point) rs.getObject(8)).
                equalsTopo(WKT_READER.read("POINT(1 1)")));
        assertFalse(rs.next());
        rs.close();
        st.close();
    }
    
    @Test
    public void test_ST_DelaunayWithPoints1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom MultiPoint);"
                + "INSERT INTO input_table VALUES(" +
                "'MULTIPOINT ((0 0 1), (10 0 1), (10 10 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom) as the_geom FROM input_table;");    
        rs.next();
        assertEquals((Geometry)rs.getObject(1), WKT_READER.read("MULTIPOLYGON(((0 0, 10 0, 10 10, 0 0)))"));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
      
    @Test
    public void test_ST_DelaunayWithPoints2() throws Exception{
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(the_geom MultiPoint);" +
                "INSERT INTO input_table VALUES(" +
                "'MULTIPOINT ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom) as the_geom FROM input_table;");    
        rs.next();
        assertEquals((Geometry)rs.getObject(1), WKT_READER.read("MULTIPOLYGON (((0 0, 10 0, 5 5, 0 0)), ((10 0, 5 5, 10 10, 10 0)))"));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_DelaunayWithLines() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES("
                + "'MULTILINESTRING ((1.1 8 1, 8 8 1), (2 3.1 1, 8 5.1 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom) as the_geom FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals( WKT_READER.read("MULTIPOLYGON ( ((1.1 8, 2 3.1, 8 5.1, 1.1 8)),"
                + " ((1.1 8, 8 5.1, 8 8, 1.1 8)))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }    
    
    @Test
    public void test_ST_DelaunayAsMultiPolygon() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES("
                + "'POLYGON ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom, 0) as the_geom FROM input_table;");
        rs.next();
         assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON (((1.1 9, 1.1 3, 5 8, 1.1 9)), \n"
                 + "   ((1.1 9, 5 8, 8.8 9.9, 1.1 9)), \n"
                + "   ((8.8 9.9, 5 8, 9.5 6.4, 8.8 9.9)), \n"
                + "   ((5.1 1.1, 9.5 6.4, 5 8, 5.1 1.1)), \n"
                + "   ((5.1 1.1, 5 8, 1.1 3, 5.1 1.1)))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }    
    
    @Test
    public void test_ST_DelaunayAsMultiLineString() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES("
                + "'POLYGON ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom, 1) as the_geom FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTILINESTRING ((1.1 9, 1.1 3, 5 8, 1.1 9), \n"
                + "  (1.1 9, 5 8, 8.8 9.9, 1.1 9), \n"
                + "  (8.8 9.9, 5 8, 9.5 6.4, 8.8 9.9), \n"
                + "  (5.1 1.1, 9.5 6.4, 5 8, 5.1 1.1), \n"
                + "  (5.1 1.1, 5 8, 1.1 3, 5.1 1.1))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    
    @Test
    public void test_ST_ConstrainedDelaunayWithPolygon() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES("
                + "'POLYGON ((1.9 8, 2.1 2.2, 7.1 2.2, 4.9 3.5, 7.5 8.1, 3.2 6, 1.9 8))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay(the_geom) as the_geom FROM input_table;");
        rs.next();
        assertEquals((Geometry) rs.getObject(1), WKT_READER.read("MULTIPOLYGON (((1.9 8, 2.1 2.2, 3.2 6, 1.9 8)), \n"
                + "  ((2.1 2.2, 3.2 6, 4.9 3.5, 2.1 2.2)), \n"
                + "  ((2.1 2.2, 4.9 3.5, 7.1 2.2, 2.1 2.2)), \n"
                + "  ((7.1 2.2, 4.9 3.5, 7.5 8.1, 7.1 2.2)), \n"
                + "  ((4.9 3.5, 3.2 6, 7.5 8.1, 4.9 3.5)), \n"
                + "  ((3.2 6, 1.9 8, 7.5 8.1, 3.2 6)))"));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_ConstrainedDelaunayWithLines() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES("
                + "'MULTILINESTRING ((2 7, 6 7),  (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay(the_geom, 1) as the_geom FROM input_table;");
        rs.next();
        assertEquals((Geometry) rs.getObject(1), WKT_READER.read("MULTILINESTRING ((2 7, 3.2 4.6), \n"
                + "  (3.2 4.6, 4.1 5), \n"
                + "  (4.1 5, 4.1818181818181825 7), \n"
                + "  (3.2 4.6, 4.1818181818181825 7), \n"
                + "  (2 7, 4.1818181818181825 7), \n"
                + "  (4.1818181818181825 7, 5 9), \n"
                + "  (2 7, 5 9), \n"
                + "  (3.2 4.6, 6 5), \n"
                + "  (4.1 5, 6 5), \n"
                + "  (4.1818181818181825 7, 6 5), \n"
                + "  (6 5, 6 7), \n"
                + "  (4.1818181818181825 7, 6 7), \n"
                + "  (5 9, 6 7))"));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_ConstrainedDelaunayWithLines2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES("
                + "'MULTILINESTRING ((2 7, 6 7),  (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay(the_geom, 0) as the_geom FROM input_table;");
        rs.next();
        assertEquals((Geometry) rs.getObject(1), WKT_READER.read("MULTIPOLYGON (((3.2 4.6, 4.1 5, 4.1818181818181825 7, 3.2 4.6)), "
                + "((2 7, 3.2 4.6, 4.1818181818181825 7, 2 7)), ((4.1818181818181825 7, 2 7, 5 9, 4.1818181818181825 7)), "
                + "((3.2 4.6, 4.1 5, 6 5, 3.2 4.6)), ((4.1 5, 4.1818181818181825 7, 6 5, 4.1 5)), "
                + "((6 5, 4.1818181818181825 7, 6 7, 6 5)), ((4.1818181818181825 7, 5 9, 6 7, 4.1818181818181825 7)))"));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_MakeGrid() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }
    
    @Test
    public void testST_MakeGridFromGeometry() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 1);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE grid;");
        st.close();
    }

    /**
     * Test to create a regular square grid from a subquery
     *
     * @throws Exception
     */
    @Test
    public void testST_MakeGridFromSubquery1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0 ))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select the_geom from input_table), 1, 1);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }

    /**
     * Test to create a regular square grid from a complex subquery
     *
     * @throws Exception
     */
    @Test
    public void testST_MakeGridFromSubquery2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 0 ))'));"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((1 1, 2 2, 1 2, 1 1 ))'));");
        try {
            st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select the_geom from input_table), 1, 1);");
        } catch (Exception e) {
            assertTrue(true);
        }
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select st_union(st_accum(the_geom)) from input_table), 1, 1);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }
    
    
    @Test
    public void test_ST_MakeGridPoints() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegridpoints('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(0.5 0.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1.5 0.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(0.5 1.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1.5 1.5)")));        
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }
    
    @Test
    public void test_ST_MakeGrid2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 2 0, 3 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 6);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((2 0, 3 0, 3 1, 2 1, 2 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((2 1, 3 1, 3 2, 2 2, 2 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }
    
    @Test
    public void test_ST_MakeGrid3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 1.4 0, 1 0.5, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 0.5, 0.5);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 3);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 0.5 0, 0.5 0.5, 0 0.5, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0.5 0, 1 0, 1 0.5, 0.5 0.5, 0.5 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 1.5 0, 1.5 0.5, 1 0.5, 1 0))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }
    
    @Test
    public void test_ST_MakeGrid4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 2 0, 1 1, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 0.5);");
        ResultSet rs = st.executeQuery("select count(*)  from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select *  from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 0.5, 0 0.5, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 0.5, 1 0.5, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0.5, 1 0.5, 1 1, 0 1, 0 0.5))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((1 0.5, 2 0.5, 2 1, 1 1, 1 0.5))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
        st.close();
    }
    
    @Test
    public void test_ST_TriangleAspect1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect(the_geom) FROM input_table;");
        rs.next();
        assertTrue(rs.getDouble(1)==0);
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_TriangleAspect2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 1, 10 0 0, 0 10 1, 0 0 1))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect(the_geom) FROM input_table;");
        rs.next();
        assertTrue(rs.getDouble(1)==90);
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test(expected = SQLException.class)  
    public void test_ST_TriangleAspect3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 , 10 0 0, 0 10 1, 0 0 1))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect(the_geom) FROM input_table;");        
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_TriangleSlope1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleSlope(the_geom) FROM input_table;");
        rs.next();
        assertTrue(rs.getDouble(1)==0);
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_TriangleSlope2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 10, 10 0 1, 5 5 10, 0 0 10))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleSlope(the_geom) FROM input_table;");
        rs.next();
        assertTrue((rs.getDouble(1)-127.27)<10E-2);
        rs.close();
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_TriangleDirection1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).isEmpty());
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_TriangleDirection2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0 0, 4 0 0, 2 3 9, 0 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("LINESTRING(2 1 3, 2 0 0)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_BoundingCircle1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'));");
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("POLYGON ((366.4800710247679 257.5, 363.82882265008465 230.58142351196977, "
                + "355.97696351423673 204.69731282226294, 343.22623616044143 180.84237978870843, "
                + "326.06664389021483 159.93335610978517, 305.1576202112916 142.77376383955857, "
                + "281.3026871777371 130.02303648576327, 255.41857648803023 122.17117734991535, "
                + "228.5 119.51992897523209, 201.58142351196977 122.17117734991535, "
                + "175.69731282226294 130.02303648576327, 151.84237978870846 142.77376383955857, "
                + "130.93335610978517 159.93335610978517, 113.77376383955855 180.84237978870843, "
                + "101.02303648576327 204.697312822263, 93.17117734991535 230.58142351196986, "
                + "90.51992897523209 257.5000000000001, 93.17117734991538 284.41857648803034, "
                + "101.02303648576334 310.3026871777372, 113.77376383955868 334.1576202112917, "
                + "130.9333561097853 355.066643890215, 151.84237978870863 372.22623616044154, "
                + "175.6973128222632 384.97696351423684, 201.5814235119701 392.8288226500847, "
                + "228.50000000000034 395.4800710247679, 255.4185764880306 392.8288226500846, "
                + "281.3026871777374 384.97696351423656, 305.15762021129194 372.2262361604412, "
                + "326.0666438902152 355.06664389021455, 343.2262361604417 334.15762021129115, "
                + "355.9769635142369 310.3026871777366, 363.82882265008476 284.41857648802966, "
                + "366.4800710247679 257.5))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_BoundingCircle2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (140 200, 170 150)'));");
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("POLYGON ((184.1547594742265 175, 183.59455894601797 "
                + "169.3121885858704, 181.9354855535274 163.8429565746244, "
                + "179.24129655680906 158.8024834852735, 175.6155281280883 154.3844718719117, "
                + "171.1975165147265 150.75870344319094, 166.1570434253756 148.0645144464726, "
                + "160.6878114141296 146.40544105398203, 155 145.8452405257735, "
                + "149.3121885858704 146.40544105398203, 143.8429565746244 148.0645144464726, "
                + "138.80248348527354 150.75870344319094, 134.3844718719117 154.3844718719117,"
                + " 130.75870344319094 158.8024834852735, 128.0645144464726 163.8429565746244, "
                + "126.40544105398202 169.3121885858704, 125.8452405257735 175.00000000000003, "
                + "126.40544105398203 180.68781141412964, 128.06451444647263 186.15704342537566,"
                + " 130.75870344319097 191.19751651472652, 134.38447187191173 195.61552812808833,"
                + " 138.80248348527357 199.2412965568091, 143.84295657462442 201.9354855535274, "
                + "149.31218858587044 203.594558946018, 155.00000000000009 204.1547594742265, "
                + "160.6878114141297 203.59455894601797, 166.1570434253757 201.93548555352737, "
                + "171.19751651472654 199.241296556809, 175.61552812808839 195.61552812808824, "
                + "179.24129655680912 191.19751651472637, 181.93548555352743 186.15704342537552, "
                + "183.594558946018 180.6878114141295, 184.1547594742265 175))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_Densify1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (140 200, 170 150)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Densify(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("LINESTRING (140 200, 145 191.66666666666666, "
                + "150 183.33333333333334, 155 175, 160 166.66666666666669, 165 158.33333333333334, 170 150)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_Densify2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((100 150, 150 150, 150 100, 100 100, 100 150))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Densify(the_geom, 50) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("POLYGON ((100 150, 125 150, 150 150, "
                + "150 125, 150 100, 125 100, 100 100, 100 125, 100 150))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_Densify3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT (100 150)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Densify(the_geom, 50) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("POINT (100 150)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
    
    @Test
    public void test_ST_Expand1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT (100 150)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Expand(the_geom, 10, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry)rs.getObject(1)).equals(WKT_READER.read("POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
}
