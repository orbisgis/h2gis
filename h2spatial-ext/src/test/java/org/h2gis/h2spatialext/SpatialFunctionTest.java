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
package org.h2gis.h2spatialext;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_CoordDim;

import static org.junit.Assert.*;

/**
 * @author Nicolas Fortin
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class SpatialFunctionTest {

    private static Connection connection;
    private static final String DB_NAME = "SpatialFunctionTest";
    private static GeometryFactory FACTORY;
    private static WKTReader WKT_READER;
    public static final double TOLERANCE = 10E-10;
    private static final String POLYGON2D =
            "'POLYGON ((181 124, 87 162, 76 256, 166 315, 286 325, 373 255, "
            + "387 213, 377 159, 351 121, 298 101, 234 56, 181 124), "
            + "(165 244, 227 219, 234 300, 168 288, 165 244), "
            + "(244 130, 305 135, 324 186, 306 210, 272 206, 206 174, 244 130))'";
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

    private static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) {
        assertTrue(Arrays.equals(ValueGeometry.get(expectedWKT).getBytes(), valueWKB));
    }

    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary MULTIPOLYGON);"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('forests') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeEmptyGeometryCollection() throws Exception {
        Statement st = connection.createStatement();
        st.execute("create table test(the_geom GEOMETRY, value Integer);"
                + "insert into test VALUES (ST_GeomFromText('MULTILINESTRING EMPTY'),108),"
                + " (ST_GeomFromText('MULTIPOINT EMPTY'),109),"
                + " (ST_GeomFromText('MULTIPOLYGON EMPTY'),110),"
                + " (ST_GeomFromText('GEOMETRYCOLLECTION EMPTY'),111);");
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
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);"
                + "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
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
        Envelope result = SFSUtilities.getTableEnvelope(connection, TableLocation.parse("PTCLOUDS"), "");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom Polygon);"
                + "INSERT INTO input_table VALUES("
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)); "
                + "INSERT INTO input_table VALUES("
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))', 1));");
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
                "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 6 -2, 0 0))', 1));");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(smallc Polygon, bigc Polygon);"
                + "INSERT INTO input_table VALUES("
                + "ST_Buffer(ST_GeomFromText('POINT(1 2)'), 10),"
                + "ST_Buffer(ST_GeomFromText('POINT(1 2)'), 20));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_Covers(smallc, smallc),"
                + "ST_Covers(smallc, bigc),"
                + "ST_Covers(bigc, smallc),"
                + "ST_Covers(bigc, ST_ExteriorRing(bigc)),"
                + "ST_Contains(bigc, ST_ExteriorRing(bigc)) FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geomA Polygon, geomB Polygon);"
                + "INSERT INTO input_table VALUES("
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1), "
                + "ST_PolyFromText('POLYGON ((12 0, 14 0, 14 6, 12 6, 12 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_DWithin(geomA, geomB, 2.0),"
                + "ST_DWithin(geomA, geomB, 1.0),"
                + "ST_DWithin(geomA, geomB, -1.0),"
                + "ST_DWithin(geomA, geomB, 3.0),"
                + "ST_DWithin(geomA, geomA, -1.0),"
                + "ST_DWithin(geomA, geomA, 0.0),"
                + "ST_DWithin(geomA, geomA, 5000.0) FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(line Linestring);"
                + "INSERT INTO input_table VALUES("
                + "ST_LineFromText('LINESTRING(1 2 3, 4 5 6)', 101));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_XMin(line), ST_XMax(line), "
                + "ST_YMin(line), ST_YMax(line),"
                + "ST_ZMin(line), ST_ZMax(line)"
                + " FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING(1 3, 1 1, 2 1)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Rotate(geom, pi()),"
                + "ST_Rotate(geom, pi() / 3), "
                + "ST_Rotate(geom, pi()/2, 1.0, 1.0), "
                + "ST_Rotate(geom, -pi()/2, ST_GeomFromText('POINT(2 1)')) "
                + "FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(twoDLine Geometry, threeDLine Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING(1 2, 4 5)'),"
                + "ST_GeomFromText('LINESTRING(1 2 3, 4 5 6)'));");
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_Scale(twoDLine, 0.5, 0.75), ST_Scale(threeDLine, 0.5, 0.75), "
                + "ST_Scale(twoDLine, 0.5, 0.75, 1.2), ST_Scale(threeDLine, 0.5, 0.75, 1.2), "
                + "ST_Scale(twoDLine, 0.0, -1.0, 2.0), ST_Scale(threeDLine, 0.0, -1.0, 2.0) "
                + "FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(1 4, 15 7, 16 17)',2249)),"
                + "(ST_GeomFromText('LINESTRING(1 4 3, 15 7 9, 16 17 22)',2249)),"
                + "(ST_GeomFromText('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))',2249)),"
                + "(ST_GeomFromText('MULTIPOLYGON(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0),"
                + "(-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))',2249)),"
                + "(ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22),"
                + "POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))',2249));");
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
        assertEquals(Math.sqrt(241) + Math.sqrt(270) + Math.sqrt(2) + 2 * Math.sqrt(5)
                + Math.sqrt(10), rs.getDouble(1), 0.0);
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_3DLengthEqualsST_LengthFor2DGeometry() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(1 4, 15 7, 16 17)',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249));");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('POINT(1 2)',1));"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING(0 0, 1 1 2)',1));"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)',1));"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))',1));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_CoordDim(geom)"
                + " FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_Buffer(ST_GeomFromText('POINT(1 2)'), 10)),"
                + "(ST_GeomFromText(" + POLYGON2D + ")),"
                + "(ST_GeomFromText(" + UNIT_SQUARE + ")),"
                + "(ST_GeomFromText(" + MULTIPOLYGON2D + ")),"
                + "(ST_GeomFromText('POINT(1 2)')),"
                + "(ST_GeomFromText(" + LINESTRING2D + ")),"
                + "(ST_GeomFromText('POLYGON((0 0 0, 3 0 0, 3 2 0, 0 2 1, 0 0 0))'));");
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
        ResultSet rs = st.executeQuery("SELECT ST_MakePoint(1.4, -3.7), "
                + "ST_MakePoint(1.4, -3.7, 6.2);");
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
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_MakeEllipse(ST_MakePoint(0, 0), 6, 4),"
                + "ST_MakeEllipse(ST_MakePoint(-1, 4), 2, 4),"
                + "ST_MakeEllipse(ST_MakePoint(4, -5), 4, 4),"
                + "ST_Buffer(ST_MakePoint(4, -5), 2);");
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
    public void test_ST_MakeLine() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_MakeLine('POINT(1 2 3)'::Geometry, 'POINT(4 5 6)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'POINT(4 5)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'MULTIPOINT(4 5, 12 9)'::Geometry), "
                + "ST_MakeLine('MULTIPOINT(1 2, 17 6)'::Geometry, 'MULTIPOINT(4 5, 7 9, 18 -1)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'POINT(4 5)'::Geometry, 'POINT(7 8)'::Geometry);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2 3, 4 5 6)", rs.getBytes(1));
        assertGeometryEquals("LINESTRING(1 2, 4 5)", rs.getBytes(2));
        assertGeometryEquals("LINESTRING(1 2, 4 5, 12 9)", rs.getBytes(3));
        assertGeometryEquals("LINESTRING(1 2, 17 6, 4 5, 7 9, 18 -1)", rs.getBytes(4));
        assertGeometryEquals("LINESTRING(1 2, 4 5, 7 8)", rs.getBytes(5));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT "
                + "ST_MakeLine('MULTIPOINT(1 2, 3 4)'::Geometry);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2, 3 4)", rs.getBytes(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Point);"
                + "INSERT INTO input_table VALUES"
                + "('POINT(1 2)'::Geometry),"
                + "('POINT(3 4)'::Geometry),"
                + "('POINT(5 6)'::Geometry),"
                + "('POINT(7 8)'::Geometry),"
                + "('POINT(9 10)'::Geometry);");
        rs = st.executeQuery("SELECT ST_MakeLine(ST_Accum(point)) FROM input_table;");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2, 3 4, 5 6, 7 8, 9 10)", rs.getBytes(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Geometry);"
                + "INSERT INTO input_table VALUES"
                + "('POINT(5 5)'::Geometry),"
                + "('MULTIPOINT(1 2, 7 9, 18 -4)'::Geometry),"
                + "('POINT(3 4)'::Geometry),"
                + "('POINT(99 3)'::Geometry);");
        rs = st.executeQuery("SELECT ST_MakeLine(ST_Accum(point)) FROM input_table;");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(5 5, 1 2, 7 9, 18 -4, 3 4, 99 3)", rs.getBytes(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(multi_point MultiPoint);"
                + "INSERT INTO input_table VALUES"
                + "('MULTIPOINT(5 5, 1 2, 3 4, 99 3)'::Geometry), "
                + "('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)'::Geometry),"
                + "('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)'::Geometry);");
        rs = st.executeQuery("SELECT ST_MakeLine(ST_Accum(multi_point)) FROM input_table;");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(5 5, 1 2, 3 4, 99 3, "
                + "-5 12, 11 22, 34 41, 65 124, "
                + "1 12, 5 -21, 9 41, 32 124)", rs.getBytes(1));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ToMultiPoint() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(empty_multi_point MultiPoint,"
                + "multi_point MultiPoint, point Point, "
                + "line LineString, "
                + "polygon Polygon, multi_polygon MultiPolygon);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('MULTIPOINT EMPTY',2154),"
                + "ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154),"
                + "ST_PointFromText('POINT(5 5)',2154),"
                + "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154),"
                + "ST_MPolyFromText('MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26),"
                + "(52 18,66 23,73 9,48 6,52 18)),"
                + "((59 18,67 18,67 13,59 13,59 18)))',2154));");
        ResultSet rs = st.executeQuery("SELECT ST_ToMultiPoint(empty_multi_point), "
                + "ST_ToMultiPoint(multi_point), "
                + "ST_ToMultiPoint(point), "
                + "ST_ToMultiPoint(line), "
                + "ST_ToMultiPoint(polygon), "
                + "ST_ToMultiPoint(multi_polygon) "
                + "FROM input_table;");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("MULTIPOINT EMPTY"), rs.getObject(1));
        assertEquals(WKT_READER.read("MULTIPOINT(5 5, 1 2, 3 4, 99 3)"), rs.getObject(2));
        assertEquals(WKT_READER.read("MULTIPOINT(5 5)"), rs.getObject(3));
        assertEquals(WKT_READER.read("MULTIPOINT(5 5, 1 2, 3 4, 99 3)"), rs.getObject(4));
        assertEquals(WKT_READER.read("MULTIPOINT(0 0, 10 0, 10 5, 0 5, 0 0)"), rs.getObject(5));
        assertEquals(WKT_READER.read("MULTIPOINT(28 26,28 0,84 0,84 42,28 26,"
                + "52 18,66 23,73 9,48 6,52 18,"
                + "59 18,67 18,67 13,59 13,59 18)"), rs.getObject(6));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ToMultiLine() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table("
                + "point Point,"
                + "empty_line_string LineString,"
                + "line LineString, "
                + "polygon Polygon, "
                + "polygon_with_holes Polygon, "
                + "multi_polygon MultiPolygon,"
                + "collection Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_PointFromText('POINT(2 4)',2154),"
                + "ST_GeomFromText('LINESTRING EMPTY',2154),"
                + "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1))',2154),"
                + "ST_MPolyFromText('MULTIPOLYGON(((28 26,28 0,84 0,84 42,28 26),"
                + "(52 18,66 23,73 9,48 6,52 18)),"
                + "((59 18,67 18,67 13,59 13,59 18)))',2154),"
                + "ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22),"
                + "POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))'));");
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_ToMultiLine(point), "
                + "ST_ToMultiLine(empty_line_string), "
                + "ST_ToMultiLine(line), "
                + "ST_ToMultiLine(polygon), "
                + "ST_ToMultiLine(polygon_with_holes), "
                + "ST_ToMultiLine(multi_polygon), "
                + "ST_ToMultiLine(collection)"
                + "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((MultiLineString) rs.getObject(1)).isEmpty());
        assertTrue(((MultiLineString) rs.getObject(2)).isEmpty());
        assertTrue(WKT_READER.read("MULTILINESTRING((5 5, 1 2, 3 4, 99 3))").equalsExact(
                (MultiLineString) rs.getObject(3), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0, 10 5, 0 5, 0 0))").equalsExact(
                (MultiLineString) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1))").equalsExact(
                (MultiLineString) rs.getObject(5), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((28 26,28 0,84 0,84 42,28 26),"
                + "(52 18,66 23,73 9,48 6,52 18),"
                + "(59 18,67 18,67 13,59 13,59 18))").equalsExact(
                (MultiLineString) rs.getObject(6), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),"
                + "(1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))").equalsExact(
                (MultiLineString) rs.getObject(7), TOLERANCE));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Holes() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(empty_line_string LineString,"
                + "line LineString, "
                + "polygon Polygon, "
                + "polygon_with_holes Polygon, "
                + "collection Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING EMPTY',2154),"
                + "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1))',2154),"
                + "ST_GeomFromText('GEOMETRYCOLLECTION(POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1)),"
                + "POLYGON ((11 6, 14 6, 14 9, 11 9, 11 6),"
                + "(12 7, 14 7, 14 8, 12 8, 12 7)))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Holes(empty_line_string), "
                + "ST_Holes(line), "
                + "ST_Holes(polygon), "
                + "ST_Holes(polygon_with_holes), "
                + "ST_Holes(collection)"
                + "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((GeometryCollection) rs.getObject(1)).isEmpty());
        assertTrue(((GeometryCollection) rs.getObject(2)).isEmpty());
        assertTrue(((GeometryCollection) rs.getObject(3)).isEmpty());
        assertTrue(WKT_READER.read("GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1)))")
                .equalsExact((GeometryCollection) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1)),"
                + "POLYGON((12 7, 14 7, 14 8, 12 8, 12 7)))")
                .equalsExact((GeometryCollection) rs.getObject(5), TOLERANCE));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ToMultiSegments() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table("
                + "point Point,"
                + "empty_line_string LineString,"
                + "line LineString, "
                + "multi_line MultiLineString, "
                + "polygon Polygon, "
                + "polygon_with_holes Polygon, "
                + "collection Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_PointFromText('POINT(5 5)', 2154),"
                + "ST_GeomFromText('LINESTRING EMPTY',2154),"
                + "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154),"
                + "ST_GeomFromText('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1),"
                + "(7 1, 8 1, 8 3, 7 3, 7 1))',2154),"
                + "ST_GeomFromText('GEOMETRYCOLLECTION(POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1),"
                + "(7 1, 8 1, 8 3, 7 3, 7 1)),"
                + "POINT(2 3),"
                + "LINESTRING (8 7, 9 5, 11 3))'));");
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_ToMultiSegments(point), "
                + "ST_ToMultiSegments(empty_line_string), "
                + "ST_ToMultiSegments(line), "
                + "ST_ToMultiSegments(multi_line), "
                + "ST_ToMultiSegments(polygon), "
                + "ST_ToMultiSegments(polygon_with_holes), "
                + "ST_ToMultiSegments(collection)"
                + "FROM input_table;");
        assertTrue(rs.next());
        assertTrue(((MultiLineString) rs.getObject(1)).isEmpty());
        assertTrue(((MultiLineString) rs.getObject(2)).isEmpty());
        assertTrue(WKT_READER.read("MULTILINESTRING((5 5, 1 2), (1 2, 3 4), (3 4, 99 3))")
                .equalsExact((MultiLineString) rs.getObject(3), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((1 4 3, 15 7 9), (15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0), (1 0 0, 1 2 0), (1 2 0, 0 2 1))")
                .equalsExact((MultiLineString) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0))")
                .equalsExact((MultiLineString) rs.getObject(5), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0),"
                + "(1 1, 2 1), (2 1, 2 4), (2 4, 1 4), (1 4, 1 1),"
                + "(7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1))")
                .equalsExact((MultiLineString) rs.getObject(6), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0),"
                + "(1 1, 2 1), (2 1, 2 4), (2 4, 1 4), (1 4, 1 1),"
                + "(7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1),"
                + "(8 7, 9 5), (9 5, 11 3))")
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Point);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(0 0)')),"
                + "(ST_GeomFromText('POINT(4 2.5)')),"
                + "(ST_GeomFromText('POINT(5 2.5)')),"
                + "(ST_GeomFromText('POINT(6 2.5)')),"
                + "(ST_GeomFromText('POINT(5 7)'));");
        ResultSet rs = st.executeQuery("SELECT ST_FurthestCoordinate(point, "
                + "ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')) "
                + "FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Point);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(0 0)')),"
                + "(ST_GeomFromText('POINT(4 2.5)')),"
                + "(ST_GeomFromText('POINT(5 2.5)')),"
                + "(ST_GeomFromText('POINT(6 2.5)')),"
                + "(ST_GeomFromText('POINT(5 7)'));");
        ResultSet rs = st.executeQuery("SELECT ST_ClosestCoordinate(point, "
                + "ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))')) "
                + "FROM input_table;");
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(100 300, 400 300, 400 100)')),"
                + "(ST_GeomFromText('POLYGON((100 100, 400 100, 400 300, 100 300, 100 100),"
                + "(150 130, 200 130, 200 220, 150 130))')),"
                + "(ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(100 300, 400 300, 400 100), "
                + "POLYGON((100 100, 400 100, 400 300, 100 300, 100 100)))'));");
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_LocateAlong(geom, 0.5, 10),"
                + "ST_LocateAlong(geom, 0.5, -10),"
                + "ST_LocateAlong(geom, 0.3, 10),"
                + "ST_LocateAlong(geom, 0.0, 10),"
                + "ST_LocateAlong(geom, 1.0, 10),"
                + "ST_LocateAlong(geom, 2.0, 10),"
                + "ST_LocateAlong(geom, -1.0, 10) "
                + "FROM input_table;");
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
                equalsTopo(WKT_READER.read("MULTIPOINT((250 110), (250 290), (110 200), (390 200), "
                + "(250 310), (410 200))")));
        assertTrue(((MultiPoint) rs.getObject(2)).
                equalsTopo(WKT_READER.read("MULTIPOINT((250 90), (250 310), (90 200), (410 200), "
                + "(250 290), (390 200))")));
        assertTrue(((MultiPoint) rs.getObject(3)).
                equalsTopo(WKT_READER.read("MULTIPOINT((190 110), (310 290), (110 240), (390 160), "
                + "(190 310), (410 240))")));
        assertTrue(((MultiPoint) rs.getObject(4)).
                equalsTopo(WKT_READER.read("MULTIPOINT((100 110), (390 100), (400 290), (110 300), "
                + "(100 310), (410 300))")));
        assertTrue(((MultiPoint) rs.getObject(5)).
                equalsTopo(WKT_READER.read("MULTIPOINT((400 110), (390 300), (100 290), (110 100), "
                + "(400 310), (410 100))")));
        assertTrue(((MultiPoint) rs.getObject(6)).
                equalsTopo(WKT_READER.read("MULTIPOINT((700 110), (390 500), (-200 290), (110 -100), "
                + "(700 310), (410 -100))")));
        assertTrue(((MultiPoint) rs.getObject(7)).
                equalsTopo(WKT_READER.read("MULTIPOINT((-200 110), (390 -100), (700 290), (110 500), "
                + "(-200 310), (410 500))")));
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Point);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(0 0)')),"
                + "(ST_GeomFromText('POINT(4 2.5)')),"
                + "(ST_GeomFromText('POINT(5 2.5)')),"
                + "(ST_GeomFromText('POINT(6 2.5)')),"
                + "(ST_GeomFromText('POINT(5 7)'));");
        ResultSet rs = st.executeQuery("SELECT "
                + // All points except (5 7) are contained inside the polygon.
                "ST_ClosestPoint("
                + "    ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'),"
                + "    point),"
                + // Only (0 0) intersects the linestring.
                "ST_ClosestPoint("
                + "    ST_GeomFromText('LINESTRING(0 0, 10 0, 10 5, 0 5, 0 0)'),"
                + "    point),"
                + // The closest point on a point to another geometry is always
                // the point itself.
                "ST_Equals("
                + "    ST_ClosestPoint("
                + "        point,"
                + "        ST_GeomFromText('LINESTRING(0 0, 10 0, 10 5, 0 5, 0 0)')),"
                + "    point)"
                + "FROM input_table;");
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
        rs = st.executeQuery("SELECT "
                + "ST_ClosestPoint("
                + "    ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'),"
                + "    ST_GeomFromText('POLYGON((13 2, 15 0, 13 4, 13 2))')),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'),"
                + "    ST_GeomFromText('POLYGON((13 4, 13 2, 15 0, 13 4))'));");
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
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + a + "),"
                + "    ST_GeomFromText(" + b + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + a + "),"
                + "    ST_GeomFromText(" + bReversed + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + aReversed + "),"
                + "    ST_GeomFromText(" + b + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + aReversed + "),"
                + "    ST_GeomFromText(" + bReversed + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + b + "),"
                + "    ST_GeomFromText(" + a + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + bReversed + "),"
                + "    ST_GeomFromText(" + a + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + b + "),"
                + "    ST_GeomFromText(" + aReversed + ")),"
                + "ST_ClosestPoint("
                + "    ST_GeomFromText(" + bReversed + "),"
                + "    ST_GeomFromText(" + aReversed + "));");
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
                + "INSERT INTO input_table VALUES("
                + "'MULTIPOINT ((0 0 1), (10 0 1), (10 10 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom) as the_geom FROM input_table;");
        rs.next();
        assertEquals((Geometry) rs.getObject(1), WKT_READER.read("MULTIPOLYGON(((0 0, 10 0, 10 10, 0 0)))"));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_DelaunayWithPoints2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom MultiPoint);"
                + "INSERT INTO input_table VALUES("
                + "'MULTIPOINT ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY); ");
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(the_geom) as the_geom FROM input_table;");
        rs.next();
        assertEquals((Geometry) rs.getObject(1), WKT_READER.read("MULTIPOLYGON (((0 0, 10 0, 5 5, 0 0)), ((10 0, 5 5, 10 10, 10 0)))"));
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON ( ((1.1 8, 2 3.1, 8 5.1, 1.1 8)),"
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
        assertTrue(rs.getDouble(1) == 0);
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
        assertTrue(rs.getDouble(1) == 90);
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
        assertTrue(rs.getDouble(1) == 0);
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
        assertTrue((rs.getDouble(1) - 127.27) < 10E-2);
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
        assertTrue(((Geometry) rs.getObject(1)).isEmpty());
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(2 1 3, 2 0 0)")));
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((366.4800710247679 257.5, 363.82882265008465 230.58142351196977, "
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((184.1547594742265 175, 183.59455894601797 "
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (140 200, 145 191.66666666666666, "
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((100 150, 125 150, 150 150, "
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT (100 150)")));
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
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Expand2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT (100 150)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Expand(the_geom, 5, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Expand3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT (100 150)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Expand(the_geom, 5, -10) FROM input_table;");
        rs.next();
        assertEquals(ValueGeometry.get("LINESTRING (95 150, 105 150)").getGeometry(), rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Expand4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Expand(the_geom, 5, -10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (90 150, 110 150)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'));");
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((170 350, 95 214, 220 120, 210 210, 159 205, 170 240, 170 350))'));");
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 214, 95 275, 170 350, 220 300, 220 120, 189 120, 95 214))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (50 210, 140 290, 120 120, 210 110)'));");
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((50 190, 50 210, 130 290, 140 290, 210 220, 210 110, 130 110, 50 190))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT ((230 220), (193 205))'));");
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((193 205, 208 220, 230 220, 215 205, 193 205))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MinimumRectangle1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT ((230 220), (193 205))'));");
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (230 220, 193 205)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MinimumRectangle2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((150 290, 110 210, 280 130, 280 250, 235 221, 150 290))'));");
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((279.99999999999693 129.99999999999395, "
                + "326.23229461756006 228.24362606231597, 156.23229461756213 308.24362606231944, "
                + "109.99999999999888 209.99999999999753, 279.99999999999693 129.99999999999395))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MinimumRectangle3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'));");
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((125.65411764705883 347.6564705882353, "
                + "8.571764705882353 252.52705882352942, "
                + "152.91764705882352 74.87058823529412, 270 170, 125.65411764705883 347.6564705882353))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPoints1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (60 290, 67 300, 67 300, 140 330, 136 319,136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPoints2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom GEOMETRY);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('GEOMETRYCOLLECTION (LINESTRING (60 290, 67 300, 67 300, 140 330, 136 319, 127 314, 127 314, 116 307, 110 299, 103 289, 100 140, 110 142, 270 170), \n"
                + "  POLYGON ((210 320, 160 240, 220 230, 246 254, 220 260, 240 280, 280 320, 270 350, 270 350, 210 320)))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints(the_geom) FROM input_table;");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getGeometryN(0).equals(WKT_READER.read("LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, 116 307, 110 299, 103 289, 100 140, 110 142, 270 170)")));
        assertTrue(geom.getGeometryN(1).equals(WKT_READER.read("POLYGON ((210 320, 160 240, 220 230, 246 254, 220 260, 240 280, 280 320, 270 350, 210 320))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ExtrudeLineString() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (0 0, 1 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Extrude(the_geom, 10) FROM input_table;");
        rs.next();
        //Test if the wall is created
        Coordinate[] wallCoords = ((Geometry) rs.getObject(1)).getGeometryN(1).getCoordinates();
        assertTrue(wallCoords.length == 5);
        Geometry wallTarget = WKT_READER.read("MULTIPOLYGON(((0 0 0, 0 0 10, 1 0 10, 1 0 0, 0 0 0)))");
        assertTrue(CoordinateArrays.equals(wallCoords, wallTarget.getCoordinates()));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ExtrudePolygon() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Extrude(the_geom, 10) FROM input_table;");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertTrue(outputGeom.getGeometryN(0).equalsTopo(WKT_READER.read("POLYGON(( 0 0 0, 1 0 0, 1 1 0, 0 1 0, 0 0 0))")));

        Geometry walls = outputGeom.getGeometryN(1);
        //Test if the walls are created
        assertTrue(walls.getCoordinates().length == 20);
        Geometry wallTarget = WKT_READER.read("MULTIPOLYGON (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), "
                + "((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), "
                + "((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0))))");
        assertTrue(CoordinateArrays.equals(walls.getCoordinates(), wallTarget.getCoordinates()));

        //Test the roof
        assertTrue(outputGeom.getGeometryN(2).equalsTopo(WKT_READER.read("POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))")));

        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWithHole() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10), \n"
                + "  (1 3, 3 3, 3 1, 1 1, 1 3))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Extrude(the_geom, 10) FROM input_table;");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertTrue(outputGeom.getGeometryN(0).equals(WKT_READER.read("POLYGON ((0 10 0, 10 10 0, 10 0 0, 0 0 0, 0 10 0), \n"
                + "  (1 3 0, 3 3 0, 3 1 0, 1 1 0, 1 3 0))")));

        Geometry walls = outputGeom.getGeometryN(1);
        //Test if the walls are created
        assertTrue(walls.getCoordinates().length == 40);
        Geometry wallTarget = WKT_READER.read("MULTIPOLYGON (((0 10, 0 10, 10 10, 10 10, 0 10)), "
                + "((10 10, 10 10, 10 0, 10 0, 10 10)), ((10 0, 10 0, 0 0, 0 0, 10 0)), "
                + "((0 0, 0 0, 0 10, 0 10, 0 0)), ((1 3, 1 3, 1 1, 1 1, 1 3)), "
                + "((1 1, 1 1, 3 1, 3 1, 1 1)), ((3 1, 3 1, 3 3, 3 3, 3 1)), ((3 3, 3 3, 1 3, 1 3, 3 3)))");
        assertTrue(CoordinateArrays.equals(walls.getCoordinates(), wallTarget.getCoordinates()));

        //Test the roof
        assertTrue(outputGeom.getGeometryN(2).equalsExact(WKT_READER.read("POLYGON ((0 10 10, 0 0 10, 10 0 10, 10 10 10, 0 10 10),"
                + " (1 3 10, 3 3 10, 3 1 10, 1 1 10, 1 3 10)))")));

        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWalls() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Extrude(the_geom, 10, 1) FROM input_table;");
        rs.next();
        Geometry walls = (Geometry) rs.getObject(1);

        //Test if the walls are created
        assertTrue(walls.getCoordinates().length == 20);
        Geometry wallTarget = WKT_READER.read("MULTIPOLYGON (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), "
                + "((0 1 0, 0 1 10, 1 1 0, 1 1 10, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), "
                + "((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0))))");
        assertTrue(CoordinateArrays.equals(walls.getCoordinates(), wallTarget.getCoordinates()));

        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ExtrudePolygonRoof() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Extrude(the_geom, 10, 2) FROM input_table;");
        rs.next();
        Geometry roof = (Geometry) rs.getObject(1);
        //Test the roof
        assertTrue(roof.equalsExact(WKT_READER.read("POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MakeEnvelope() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(
                WKT_READER.read("POLYGON((0 0, 1 0 0, 1 1 , 0 1, 0 0))")));
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_MakeEnvelopeSRID() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1, 4326);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(
                WKT_READER.read("POLYGON((0 0, 1 0 0, 1 1 , 0 1, 0 0))")));
        assertTrue(((Geometry) rs.getObject(1)).getSRID() == 4326);
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_InterpolateLineWithoutZ() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 8, 1 8 , 3 8)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(0 8, 1 8 , 3 8)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_InterpolateLine1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 0 0, 5 0 , 10 0 10)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(0 0 0, 5 0 5, 10 0 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_InterpolateLine2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(0 0 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)) == null);
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_InterpolateLine3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTILINESTRING((0 0 0, 5 0 , 10 0 10),(0 0 0, 50 0, 100 0 100))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTILINESTRING((0 0 0, 5 0 , 10 0 10),(0 0 0, 50 0 50, 100 0 100))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddPoint1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(0 0 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint(the_geom, 'POINT(1 1)'::GEOMETRY) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)) == null);
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddPoint2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT((0 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint(the_geom, 'POINT(1 1)'::GEOMETRY) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((0 0 0), (1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddPoint3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8)'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint(the_geom, 'POINT(1.5 4 )'::GEOMETRY, 4) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(0 8, 1 8 , 1.5 8, 3 8,  8  8, 10 8, 20 8)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddPoint4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8)'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint(the_geom, 'POINT(1.5 4 )'::GEOMETRY) FROM input_table;");
        rs.next();
        //The geometry is not modified
        assertTrue(((Geometry) rs.getObject(1)) == null);
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddPoint5() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((118 134, 118 278, 266 278, 266 134, 118 134 ))'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint(the_geom, 'POINT(196 278 )'::GEOMETRY, 4) FROM input_table;");
        rs.next();
        //The geometry is not modified
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((118 134, 118 278,196 278, 266 278, 266 134, 118 134 ))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemovePoint1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(1 1)'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint(the_geom, 'POINT(1 1)'::GEOMETRY, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1 1)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemovePoint2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT ((5 5), (10 10))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint(the_geom, 'POINT(10 10)'::GEOMETRY) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((5 5)))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemovePoint3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT ((5 5), (10 10), (100 1000))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint(the_geom, 'POINT(10 10)'::GEOMETRY, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((100 1000)))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemovePoint4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((150 250, 220 250, 220 170, 150 170, 150 250))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint(the_geom, 'POINT (230 250)'::GEOMETRY, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((150 250, 220 170, 150 170, 150 250))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemovePoint5() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (100 200, 153 255, 169 175, 200 240, 250 190, 264 236, 304 236, 320 240, 340 250, 345 265, 354 295)'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint(the_geom, 'POINT (230 250)'::GEOMETRY, 100) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (100 200, 340 250, 345 265, 354 295)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 8, 1 8 , 3 8,  8  8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'POINT(1.5 4 )'::GEOMETRY, 4) FROM input_table;");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 2);
        assertTrue(geom.getGeometryN(0).equals(WKT_READER.read("LINESTRING(0 8, 1 8 , 1.5 8)")));
        assertTrue(geom.getGeometryN(1).equals(WKT_READER.read("LINESTRING(1.5 8 , 3 8,  8  8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 0, 100 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING(50 -50, 50 50)'::GEOMETRY) FROM input_table;");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 2);
        assertTrue(geom.equals(WKT_READER.read("MULTILINESTRING((0 0, 50 0), (50 0 , 100 0))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(50 0, 100 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING(50 50, 100 50)'::GEOMETRY) FROM input_table;");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 1);
        assertTrue(geom.equals(WKT_READER.read("LINESTRING(50 0, 100 0)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING (5 0, 5 10)'::GEOMETRY) FROM input_table;");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 2);
        Polygon pol1 = (Polygon) WKT_READER.read("POLYGON (( 0 0, 5 0, 5 10 , 0 10, 0 0))");
        Polygon pol2 = (Polygon) WKT_READER.read("POLYGON ((5 0, 10 0 , 10 10, 5 10, 5 0))");
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry pol = geom.getGeometryN(i);
            if (!pol.getEnvelopeInternal().equals(pol1.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol2.getEnvelopeInternal())) {
                fail();
            }
        }
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split5() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING (5 1, 5 8)'::GEOMETRY) FROM input_table;");
        rs.next();
        assertNull((Geometry) rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split6() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING (5 1, 5 12)'::GEOMETRY) FROM input_table;");
        rs.next();
        assertNull((Geometry) rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split7() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0), (2 2, 7 2, 7 7, 2 7, 2 2))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING (5 0, 5 10)'::GEOMETRY) FROM input_table;");
        rs.next();
        Geometry pols = (Geometry) rs.getObject(1);
        assertTrue(pols.getNumGeometries() == 2);
        Polygon pol1 = (Polygon) WKT_READER.read("POLYGON (( 0 0, 5 0, 5 2 ,2 2, 2 7, 5 7,  5 10, 0 10, 0 0))");
        Polygon pol2 = (Polygon) WKT_READER.read("POLYGON ((5 0, 5 2, 7 2, 7 7 , 5 7, 5 10, 10 10, 10 0, 5 0))");
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            if (!pol.getEnvelopeInternal().equals(pol1.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol2.getEnvelopeInternal())) {
                fail();
            }
        }
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Split8() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON (( 0 0 1, 10 0 5, 10 10 8 , 0 10 12, 0 0 12))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Split(the_geom, 'LINESTRING (5 0, 5 10)'::GEOMETRY) FROM input_table;");
        rs.next();
        Geometry pols = (Geometry) rs.getObject(1);
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            assertTrue(ST_CoordDim.getCoordinateDimension(
                    ValueGeometry.getFromGeometry(pol).getBytesNoCopy()) == 3);
        }
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Translate1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT(-71.01 42.37)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Translate(the_geom, 1, 0) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(-70.01 42.37)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Translate2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(-71.01 42.37,-71.11 42.38)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Translate(the_geom, 1, 0.5) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(-70.01 42.87,-70.11 42.88)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Reverse1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (105 353, 150 180, 300 280)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (300 280, 150 180, 105 353)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Reverse2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((190 300, 380 430, 430 270, 313 117, 300 110, 140 180, 190 300))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Reverse3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTILINESTRING ((10 260, 150 290, 186 406, 286 286), \n"
                + "  (120 120, 130 125, 142 129, 360 160, 357 170, 380 340))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTILINESTRING ((380 340, 357 170, 360 160, 142 129, 130 125, 120 120), \n"
                + "  (286 286, 186 406, 150 290, 10 260))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Reverse3DLine1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (105 353 10, 150 180, 300 280 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (300 280 0, 150 180, 105 353 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Reverse3DLine2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (105 353 0, 150 180, 300 280 10)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (105 353 0, 150 180, 300 280 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Reverse3DLine3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine(the_geom) FROM input_table;");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemoveHoles1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveHoles(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_RemoveHoles2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((100 370, 335 370, 335 135, 100 135, 100 370), \n"
                + "  (140 340, 120 280, 133 277, 170 280, 181 281, 220 340, 214 336, 180 340, 185 342, 155 342, 140 340), \n"
                + "  (255 293, 210 230, 260 220, 292 265, 255 293))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveHoles(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((100 370, 335 370, 335 135, 100 135, 100 370))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_UpdateZ1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT (190 300)'));");
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT (190 300 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_UpdateZ2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300), (10 11 2))'));");
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 10), (10 11 10))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_UpdateZ3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300), (10 11 2))'));");
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ(the_geom, 10, 3) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 10), (10 11 2))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_UpdateZ4() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300 1), (10 11))'));");
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ(the_geom, 10, 2) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 10), (10 11))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_UpdateZ5() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT ST_UpdateZ('POINT (190 300 10)'::GEOMETRY, 10, 9999);");
            rs.next();
        } catch (SQLException ex) {
            assertTrue(true);
        } finally {
            if (rs != null) {
                rs.close();
            }
            st.close();
        }
    }

    @Test
    public void test_ST_AddZ1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300 1), (10 11))'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 11), (10 11))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddZ2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300), (10 11))'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_AddZ3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300 10), (10 11 5))'));");
        ResultSet rs = st.executeQuery("SELECT ST_AddZ(the_geom, -10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 0), (10 11 -5))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MultiplyZ1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300 1), (10 11))'));");
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 10), (10 11))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MultiplyZ2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300), (10 11))'));");
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_MultiplyZ3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300 100), (10 11 50))'));");
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ(the_geom, 0.1) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 1), (10 11 0.5))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_PrecisionReducer1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300 100), (10 11 50))'));");
        ResultSet rs = st.executeQuery("SELECT ST_PrecisionReducer(the_geom, 0.1) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 100), (10 11 50))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_PrecisionReducer2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190.005 300 100), (10.534 11 50))'));");
        ResultSet rs = st.executeQuery("SELECT ST_PrecisionReducer(the_geom, 1) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 100), (10.5 11 50))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_PrecisionReducer3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190.005 300 100), (10.534 11 50))'));");
        ResultSet rs = st.executeQuery("SELECT ST_PrecisionReducer(the_geom, 4) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190.005 300 100), (10.534 11 50))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Simplify1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300), (10 11 50))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Simplify(the_geom, 4) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11 50))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Simplify2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (250 250, 280 290, 300 230, 340 300, 360 260, 440 310, 470 360, 604 286)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Simplify(the_geom, 40) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (250 250, 280 290, 300 230, 470 360, 604 286)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Simplify3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((250 250, 248 240, 250 180, 290 150, 332 165, 350 190, 330 200, 340 220, 360 260, 360 300, 330 310, 319 310, 300 310, 280 280, 256 284, 250 250))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Simplify(the_geom, 40) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((250 250, 360 300, 332 165, 250 180, 250 250))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_SimplifyPreserveTopology1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTIPOINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTIPOINT( (190 300), (10 11 50))'));");
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPreserveTopology(the_geom, 4) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11 50))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_SimplifyPreserveTopology2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (250 250, 280 290, 300 230, 340 300, 360 260, 440 310, 470 360, 604 286)'));");
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPreserveTopology(the_geom, 40) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (250 250, 280 290, 300 230, 470 360, 604 286)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_SimplifyPreserveTopology3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((250 250, 248 240, 250 180, 290 150, 332 165, 350 190, 330 200, 340 220, 360 260, 360 300, 330 310, 319 310, 300 310, 280 280, 256 284, 250 250))'));");
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPreserveTopology(the_geom, 40) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((250 250, 360 300, 332 165, 250 180, 250 250))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (250 250, 280 290)'));");
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateExtremities(the_geom, 40, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (250 250 40, 280 290 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 0, 5 0 , 10 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateExtremities(the_geom, 0, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(0 0 0, 5 0 5, 10 0 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities3() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT ST_ZUpdateExtremities('POINT (190 300 10)'::GEOMETRY, 10, 9999);");
            rs.next();
        } catch (SQLException ex) {
            assertTrue(true);
        } finally {
            if (rs != null) {
                rs.close();
            }
            st.close();
        }
    }

    @Test
    public void test_ST_Normalize1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(0 0, 5 0 , 10 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateExtremities(the_geom, 0, 10) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(0 0 0, 5 0 5, 10 0 10)")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Normalize2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((170 180, 310 180, 308 190, 310 206, 340 320, 135 333, 140 260, 170 180))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Normalize(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((135 333, 340 320, 310 206, 308 190, 310 180, 170 180, 140 260, 135 333))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Polygonize1() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTILINESTRING ((130 190, 80 370, 290 380), \n"
                + "  (290 380, 270 270, 130 190))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Polygonize(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON ( ((130 190, 80 370, 290 380, 270 270, 130 190)))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Polygonize2() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTILINESTRING ((50 240, 62 250, 199 425, 250 240), \n"
                + "  (50 340, 170 250, 300 370))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Polygonize(the_geom) FROM input_table;");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }

    @Test
    public void test_ST_Polygonize3() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom MULTILINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('MULTILINESTRING ((50 240, 62 250, 199 425, 250 240), \n"
                + "  (50 340, 170 250, 300 370))'));");
        ResultSet rs = st.executeQuery("SELECT ST_Polygonize(st_union(the_geom)) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON( ((231.5744116672191 306.8379184620484, 170 250, 101.95319531953196 301.03510351035106, 199 425, 231.5744116672191 306.8379184620484)))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
        st.close();
    }
}
