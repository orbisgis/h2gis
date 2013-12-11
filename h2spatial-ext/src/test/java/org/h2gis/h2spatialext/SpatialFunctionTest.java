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
                "ST_MakeEllipse(ST_MakePoint(4, -5), 4, 4);");
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
}
