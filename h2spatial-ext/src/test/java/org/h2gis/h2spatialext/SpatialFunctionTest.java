/**
* h2spatial is a library that brings spatial support to the H2 Java database.
*
* h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
* SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
* or contact directly: info_at_ orbisgis.org
*/
package org.h2gis.h2spatialext;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import org.h2.jdbc.JdbcSQLException;
import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_CoordDim;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.function.spatial.affine_transformations.ST_Translate;
import org.h2gis.h2spatialext.function.spatial.topography.ST_TriangleAspect;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.*;

/**
* @author Nicolas Fortin
* @author Adam Gouge
* @author Erwan Bocher
*/
public class SpatialFunctionTest {

    private static Connection connection;
    private Statement st;
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

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }


    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception {
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
        assertNull(rs.getObject(1)); // POINT EMPTY does not exists (not supported in WKB)
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
    }

    @Test
    public void test_NULL_ST_Extent() throws Exception {
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);");
        ResultSet rs = st.executeQuery("select ST_Extent(the_geom) tableEnv from ptClouds;");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
        assertFalse(rs.next());
        st.execute("drop table ptClouds");
    }

    @Test
    public void test_TableEnvelope() throws Exception {
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom MultiPoint);"
                + "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
        Envelope result = SFSUtilities.getTableEnvelope(connection, TableLocation.parse("PTCLOUDS"), "");
        Envelope expected = new Envelope(-5, 99, -21, 124);
        assertEquals(expected.getMinX(), result.getMinX(), 1e-12);
        assertEquals(expected.getMaxX(), result.getMaxX(), 1e-12);
        assertEquals(expected.getMinY(), result.getMinY(), 1e-12);
        assertEquals(expected.getMaxY(), result.getMaxY(), 1e-12);
        st.execute("drop table ptClouds");
    }

    @Test
    public void test_ST_IsRectangle() throws Exception {
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
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom Polygon);"
                + "INSERT INTO input_table VALUES"
                + "(ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)), "
                + "(ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 6 -2, 0 0))', 1)),"
                + "(NULL);");
        ResultSet rs = st.executeQuery("SELECT ST_IsValid(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(true, rs.getBoolean(1));
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
        assertTrue(rs.next());
        assertEquals(false, rs.getBoolean(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_Covers2() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(smallc POLYGON, bigc POLYGON);" +
                "INSERT INTO input_table VALUES(" +
                "'POLYGON((1 1, 5 1, 5 4, 1 4, 1 1))'::Geometry," +
                "'POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'::Geometry);");
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
    public void test_ST_Covers() throws Exception {
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
    public void test_ST_3DLength2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DLength('MULTIPOLYGON (((898458.2 6245894.6, 898493.4 6245894.5, 898492.3 6245888.4, 898458.7 6245888.5, 898458.2 6245894.6)))')");
        rs.next();        
        assertEquals(81.11, rs.getDouble(1),0.01);
        rs.close();
    }

    @Test
    public void test_ST_3DLengthEqualsST_LengthFor2DGeometry() throws Exception {
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
        ResultSet rs = st.executeQuery("SELECT ST_MakePoint(1.4, -3.7), "
                + "ST_MakePoint(1.4, -3.7, 6.2);");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("POINT(1.4 -3.7)"), rs.getObject(1));
        assertEquals(WKT_READER.read("POINT(1.4 -3.7 6.2)"), rs.getObject(2));
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void test_ST_MakeEllipse() throws Exception {
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
    }

    @Test
    public void test_ST_MakeLine() throws Exception {
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
    }
    @Test
    public void test_ST_MakeLineNull() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeLine(null, 'POINT(4 5 6)')");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine('POINT(4 5 6)', null)");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine(null, null)");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine(null)");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_ToMultiPoint() throws Exception {
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
    }

    @Test
    public void test_ST_ToMultiLine() throws Exception {
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
    }

    @Test
    public void test_ST_Holes() throws Exception {
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
    }

    @Test
    public void test_ST_ToMultiSegments() throws Exception {
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
    }

    @Test
    public void test_ST_FurthestCoordinate() throws Exception {
        // 5
        //
        // +---------+
        // | |
        // | |
        // 234
        // | |
        // | |
        // 1---------+
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
    }

    @Test
    public void test_ST_ClosestCoordinate() throws Exception {
        // 5
        //
        // +---------+
        // | |
        // | |
        // 234
        // | |
        // | |
        // 1---------+
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
    }

    @Test
    public void test_ST_LocateAlong() throws Exception {
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
    }
    
     @Test
    public void test_ST_LocateAlong2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LocateAlong('LINESTRING ( 76 194, 181 175 )'::GEOMETRY, 1.1, 0);");
        rs.next();
        assertEquals("MULTIPOINT ((191.5 173.1))",
                rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_ClosestPoint() throws Exception {
        // 5
        //
        // +---------+
        // | |
        // | |
        // 234
        // | |
        // | |
        // 1---------+
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
                + " ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'),"
                + " point),"
                + // Only (0 0) intersects the linestring.
                "ST_ClosestPoint("
                + " ST_GeomFromText('LINESTRING(0 0, 10 0, 10 5, 0 5, 0 0)'),"
                + " point),"
                + // The closest point on a point to another geometry is always
                // the point itself.
                "ST_Equals("
                + " ST_ClosestPoint("
                + " point,"
                + " ST_GeomFromText('LINESTRING(0 0, 10 0, 10 5, 0 5, 0 0)')),"
                + " point)"
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
        // +---------+
        // | | |
        // | | |\
        // | | | \
        // | | \_\
        // +---------+ \\
        //
        rs = st.executeQuery("SELECT "
                + "ST_ClosestPoint("
                + " ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'),"
                + " ST_GeomFromText('POLYGON((13 2, 15 0, 13 4, 13 2))')),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText('POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))'),"
                + " ST_GeomFromText('POLYGON((13 4, 13 2, 15 0, 13 4))'));");
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(10 2)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(10 4)")));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_ClosestPoint2() throws Exception {
        // This unit test shows that the point returned by ST_ClosestPoint
        // depends on the orientations of geometries A and B. If they have the
        // same orientation, the point returned is the first point found in A.
        // If they have opposite orientation, the point returned is the point
        // of A closest to the first point found in B.
        //
        // + +
        // a| |b
        // + +
        //
        final String a = "'LINESTRING(0 0, 0 1))'";
        final String aReversed = "'LINESTRING(0 1, 0 0))'";
        final String b = "'LINESTRING(1 0, 1 1))'";
        final String bReversed = "'LINESTRING(1 1, 1 0))'";
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + a + "),"
                + " ST_GeomFromText(" + b + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + a + "),"
                + " ST_GeomFromText(" + bReversed + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + aReversed + "),"
                + " ST_GeomFromText(" + b + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + aReversed + "),"
                + " ST_GeomFromText(" + bReversed + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + b + "),"
                + " ST_GeomFromText(" + a + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + bReversed + "),"
                + " ST_GeomFromText(" + a + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + b + "),"
                + " ST_GeomFromText(" + aReversed + ")),"
                + "ST_ClosestPoint("
                + " ST_GeomFromText(" + bReversed + "),"
                + " ST_GeomFromText(" + aReversed + "));");
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
    }    
   
    
    @Test
    public void test_ST_DelaunayNullValue() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(null);");
        rs.next();
        assertTrue(rs.getObject(1)==null);
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithPoints1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTIPOINT ((0 0 1), (10 0 1), (10 10 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((0 0 1, 10 0 1, 10 10 1, 0 0 1)))",rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithPoints2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTIPOINT ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON(((0 0 1, 10 0 1, 5 5 1, 0 0 1)),  ((10 0 1, 5 5 1, 10 10 1, 10 0 1)))",  rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_DelaunayWithCollection() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('GEOMETRYCOLLECTION (POLYGON ((150 380 1, 110 230 1, 180 190 1, 230 300 1, 320 280 1, 320 380 1, 150 380 1)),"
                + "  LINESTRING (70 330 1, 280 220 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((70 330 1, 110 230 1, 150 380 1, 70 330 1)), ((110 230 1, 230 300 1, 180 190 1, 110 230 1)), ((110 230 1, 230 300 1, 150 380 1, 110 230 1)), ((180 190 1, 230 300 1, 280 220 1, 180 190 1)), ((280 220 1, 230 300 1, 320 280 1, 280 220 1)), ((230 300 1, 320 380 1, 320 280 1, 230 300 1)), ((230 300 1, 320 380 1, 150 380 1, 230 300 1))) ", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithLines() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTILINESTRING ((1.1 8 1, 8 8 1), (2 3.1 1, 8 5.1 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((1.1 8 1, 2 3.1 1, 8 5.1 1, 1.1 8 1)), ((8 5.1 1, 1.1 8 1, 8 8 1, 8 5.1 1)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayAsMultiPolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('POLYGON ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY, 0);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON(((1.1 3 1, 5 8 1, 1.1 9 1, 1.1 3 1)),   ((1.1 3 1, 5 8 1, 5.1 1.1 1, 1.1 3 1)),  ((5 8 1, 9.5 6.4 1, 5.1 1.1 1, 5 8 1)),  ((5 8 1, 1.1 9 1, 8.8 9.9 1, 5 8 1)),  ((5 8 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1)))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayAsMultiLineString() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('POLYGON ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING((1.1 3 1, 1.1 9 1), (1.1 3 1, 5 8 1),  (1.1 9 1, 5 8 1), (1.1 3 1, 5.1 1.1 1),  (5 8 1, 5.1 1.1 1), (5 8 1, 9.5 6.4 1),  (5 8 1, 8.8 9.9 1), (1.1 9 1, 8.8 9.9 1),(5.1 1.1 1, 9.5 6.4 1),  (8.8 9.9 1, 9.5 6.4 1))",rs.getBytes(1));
        rs.close();
    }
    
    
    @Test
    public void test_ST_ConstrainedDelaunayNullValue() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay(null);");
        rs.next();
        assertTrue(rs.getObject(1)==null);
        rs.close();
    }
    
    @Test
    public void test_ST_ConstrainedDelaunayWithPoints() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTIPOINT ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON(((0 0 1, 10 0 1, 5 5 1, 0 0 1)),  ((10 0 1, 5 5 1, 10 10 1, 10 0 1)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithPolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('POLYGON ((1.9 8, 2.1 2.2, 7.1 2.2, 4.9 3.5, 7.5 8.1, 3.2 6, 1.9 8))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((1.9 8 0, 2.1 2.2 0, 3.2 6 0, 1.9 8 0)),"
                + " ((2.1 2.2 0, 3.2 6 0, 4.9 3.5 0, 2.1 2.2 0)),"
                + " ((2.1 2.2 0, 4.9 3.5 0, 7.1 2.2 0, 2.1 2.2 0)),"
                + " ((7.1 2.2 0, 4.9 3.5 0, 7.5 8.1 0, 7.1 2.2 0)),"
                + " ((4.9 3.5 0, 3.2 6 0, 7.5 8.1 0, 4.9 3.5 0)),"
                + " ((3.2 6 0, 1.9 8 0, 7.5 8.1 0, 3.2 6 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTILINESTRING ((2 7, 6 7), (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((2 7 0, 3.2 4.6 0), (3.2 4.6 0, 4.1 5 0), (4.1 5 0, 4.1818181818181825 7 0), "
                + "(3.2 4.6 0, 4.1818181818181825 7 0), (2 7 0, 4.1818181818181825 7 0), (4.1818181818181825 7 0, 5 9 0), "
                + "(2 7 0, 5 9 0), (3.2 4.6 0, 6 5 0), (4.1 5 0, 6 5 0), (4.1818181818181825 7 0, 6 5 0), (6 5 0, 6 7 0), "
                + "(4.1818181818181825 7 0, 6 7 0), (5 9 0, 6 7 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTILINESTRING ((2 7, 6 7), (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY, 0);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((3.2 4.6 0, 4.1 5 0, 4.1818181818181825 7 0, 3.2 4.6 0)), "
                + "((2 7 0, 3.2 4.6 0, 4.1818181818181825 7 0, 2 7 0)), ((4.1818181818181825 7 0, 2 7 0, 5 9 0, 4.1818181818181825 7 0)), "
                + "((3.2 4.6 0, 4.1 5 0, 6 5 0, 3.2 4.6 0)), ((4.1 5 0, 4.1818181818181825 7 0, 6 5 0, 4.1 5 0)), "
                + "((6 5 0, 4.1818181818181825 7 0, 6 7 0, 6 5 0)), ((4.1818181818181825 7 0, 5 9 0, 6 7 0, 4.1818181818181825 7 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ConstrainedDelaunayWithLines3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('LINESTRING (0 0, 10 0, 10 10, 0 10, 0 0)'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 0, 0 10 0), (0 0 0, 10 0 0), (0 10 0, 10 0 0), (10 0 0, 10 10 0), (0 10 0, 10 10 0))", rs.getBytes(1));
        rs.close();                
    }
    
     @Test
    public void test_ST_ConstrainedDelaunayWithLines4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('LINESTRING (0 0 1, 10 0 1, 10 10 1, 0 10 1, 0 0 1)'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 1, 0 10 1), (0 0 1, 10 0 1), (0 10 1, 10 0 1), (10 0 1, 10 10 1), (0 10 1, 10 10 1))", rs.getBytes(1));
        rs.close();                
    }    
    
    @Test
    public void test_ST_ConstrainedDelaunayWithCollection() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('GEOMETRYCOLLECTION (POLYGON ((150 380, 110 230, 180 190, 230 300, 320 280, 320 380, 150 380)), \n"
                + "  LINESTRING (70 330, 280 220))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((70 330 0, 110 230 0, 128.4958217270195 299.3593314763231 0, 70 330 0)), ((128.4958217270195 299.3593314763231 0, 70 330 0, 150 380 0, 128.4958217270195 299.3593314763231 0)), ((110 230 0, 210.2447552447552 256.53846153846155 0, 180 190 0, 110 230 0)), ((110 230 0, 210.2447552447552 256.53846153846155 0, 128.4958217270195 299.3593314763231 0, 110 230 0)), ((128.4958217270195 299.3593314763231 0, 230 300 0, 210.2447552447552 256.53846153846155 0, 128.4958217270195 299.3593314763231 0)), ((128.4958217270195 299.3593314763231 0, 230 300 0, 150 380 0, 128.4958217270195 299.3593314763231 0)), ((180 190 0, 210.2447552447552 256.53846153846155 0, 280 220 0, 180 190 0)), ((210.2447552447552 256.53846153846155 0, 230 300 0, 280 220 0, 210.2447552447552 256.53846153846155 0)), ((280 220 0, 230 300 0, 320 280 0, 280 220 0)), ((230 300 0, 320 380 0, 320 280 0, 230 300 0)), ((230 300 0, 320 380 0, 150 380 0, 230 300 0)))", rs.getBytes(1));
        rs.close();
    }
    
     @Test
    public void test_ST_ConstrainedDelaunayWithCollection1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('GEOMETRYCOLLECTION(POINT (0 0 1), POINT (10 0 1), POINT (10 10 1), POINT (5 5 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((0 0 1, 10 0 1, 5 5 1, 0 0 1)), ((10 0 1, 5 5 1, 10 10 1, 10 0 1)))", rs.getBytes(1));
        rs.close();
    }
    

    @Test
    public void test_ST_MakeGrid() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
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
    }

    @Test
    public void testST_MakeGridFromGeometry() throws Exception {
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
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
    }

    /**
* Test to create a regular square grid from a subquery
*
* @throws Exception
*/
    @Test
    public void testST_MakeGridFromSubquery1() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0 ))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select the_geom from input_table), 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
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
    }

    /**
* Test to create a regular square grid from a complex subquery
*
* @throws Exception
*/
    @Test
    public void testST_MakeGridFromSubquery2() throws Exception {
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
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
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
    }

    @Test
    public void test_ST_MakeGridPoints() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegridpoints('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(0.5 0.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1.5 0.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(0.5 1.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1.5 1.5)")));
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGrid2() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 2 0, 3 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 6);
        rs.close();
        rs = st.executeQuery("select * from grid;");
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
    }

    @Test
    public void test_ST_MakeGrid3() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 1.4 0, 1 0.5, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 0.5, 0.5);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 3);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 0.5 0, 0.5 0.5, 0 0.5, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0.5 0, 1 0, 1 0.5, 0.5 0.5, 0.5 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 1.5 0, 1.5 0.5, 1 0.5, 1 0))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGrid4() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 2 0, 1 1, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 0.5);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
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
        ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getDouble(1) == 0);
        rs.close();
    }

    @Test
    public void test_ST_TriangleAspect2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect('POLYGON ((0 0 1, 10 0 0, 0 10 1, 0 0 1))'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getDouble(1) == 90);
        rs.close();
    }

    @Test(expected = SQLException.class)
    public void test_ST_TriangleAspect3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect('POLYGON ((0 0 , 10 0 0, 0 10 1, 0 0 1))'::GEOMETRY);");
        rs.close();
    }

    @Test
    public void testMeasureFromNorth() throws Exception {
        assertEquals(180., ST_TriangleAspect.measureFromNorth(-450.), 0.);
        assertEquals(180., ST_TriangleAspect.measureFromNorth(-90.), 0.);
        assertEquals(90., ST_TriangleAspect.measureFromNorth(0.), 0.);
        assertEquals(0., ST_TriangleAspect.measureFromNorth(90.), 0.);
        assertEquals(270., ST_TriangleAspect.measureFromNorth(180.), 0.);
        assertEquals(180., ST_TriangleAspect.measureFromNorth(270.), 0.);
        assertEquals(90., ST_TriangleAspect.measureFromNorth(360.), 0.);
        assertEquals(0., ST_TriangleAspect.measureFromNorth(450.), 0.);
        assertEquals(0., ST_TriangleAspect.measureFromNorth(810.), 0.);
    }

    @Test
    public void test_ST_TriangleAspect() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT " +
                        "ST_TriangleAspect('POLYGON((0 0 0, 3 0 0, 0 3 0, 0 0 0))')," +
                        "ST_TriangleAspect('POLYGON((0 0 1, 3 0 0, 0 3 1, 0 0 1))')," +
                        "ST_TriangleAspect('POLYGON((0 0 1, 3 0 1, 0 3 0, 0 0 1))')," +
                        "ST_TriangleAspect('POLYGON((0 0 1, 3 0 0, 3 3 1, 0 0 1))');");
        assertTrue(rs.next());
        assertTrue(rs.getDouble(1) == 0);
        assertTrue(rs.getDouble(2) == 90);
        assertTrue(rs.getDouble(3) == 0);
        assertTrue(rs.getDouble(4) == 135);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void test_ST_TriangleSlope1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleSlope('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getDouble(1) == 0);
        rs.close();
    }

    @Test
    public void test_ST_TriangleSlope2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleSlope('POLYGON ((0 0 10, 10 0 1, 5 5 10, 0 0 10))'::GEOMETRY);");
        rs.next();
        assertTrue((rs.getDouble(1) - 127.27) < 10E-2);
        rs.close();
    }

    @Test
    public void test_ST_TriangleDirection1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON ((0 0 0, 2 0 0, 1 1 0, 0 0 0))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).isEmpty());
        rs.close();
    }

    @Test
    public void test_ST_TriangleDirection2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON ((0 0 0, 4 0 0, 2 3 9, 0 0 0))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING(2 1 3, 2 0 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_BoundingCircle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle('POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'::GEOMETRY);");
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
    }

    @Test
    public void test_ST_BoundingCircle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle('LINESTRING (140 200, 170 150)'::GEOMETRY);");
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
    }

    @Test
    public void test_ST_Densify1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Densify('LINESTRING (140 200, 170 150)'::GEOMETRY, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (140 200, 145 191.66666666666666, "
                + "150 183.33333333333334, 155 175, 160 166.66666666666669, 165 158.33333333333334, 170 150)")));
        rs.close();
    }

    @Test
    public void test_ST_Densify2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Densify('POLYGON ((100 150, 150 150, 150 100, 100 100, 100 150))'::GEOMETRY, 50);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((100 150, 125 150, 150 150, "
                + "150 125, 150 100, 125 100, 100 100, 100 125, 100 150))")));
        rs.close();
    }

    @Test
    public void test_ST_Densify3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Densify('POINT (100 150)'::GEOMETRY, 50);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT (100 150)")));
        rs.close();
    }

    @Test
    public void test_ST_Expand1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 10, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))")));
        rs.close();
    }

    @Test
    public void test_ST_Expand2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 5, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))")));
        rs.close();
    }

    @Test
    public void test_ST_Expand3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 5, -10);");
        rs.next();
        assertEquals(ValueGeometry.get("LINESTRING (95 150, 105 150)").getGeometry(), rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'::GEOMETRY, 5, -10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (90 150, 110 150)")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('POLYGON ((170 350, 95 214, 220 120, 210 210, 159 205, 170 240, 170 350))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 214, 95 275, 170 350, 220 300, 220 120, 189 120, 95 214))")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('LINESTRING (50 210, 140 290, 120 120, 210 110)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((50 190, 50 210, 130 290, 140 290, 210 220, 210 110, 130 110, 50 190))")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('MULTIPOINT ((230 220), (193 205))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((193 205, 208 220, 230 220, 215 205, 193 205))")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('MULTIPOINT ((230 220), (193 205))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (230 220, 193 205)")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('POLYGON ((150 290, 110 210, 280 130, 280 250, 235 221, 150 290))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((279.99999999999693 129.99999999999395, "
                + "326.23229461756006 228.24362606231597, 156.23229461756213 308.24362606231944, "
                + "109.99999999999888 209.99999999999753, 279.99999999999693 129.99999999999395))")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((125.65411764705883 347.6564705882353, "
                + "8.571764705882353 252.52705882352942, "
                + "152.91764705882352 74.87058823529412, 270 170, 125.65411764705883 347.6564705882353))")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPoints1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('LINESTRING (60 290, 67 300, 67 300, 140 330, 136 319,136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPoints2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('GEOMETRYCOLLECTION (LINESTRING (60 290, 67 300, 67 300, 140 330, 136 319, 127 314, 127 314, 116 307, 110 299, 103 289, 100 140, 110 142, 270 170), \n"
                + " POLYGON ((210 320, 160 240, 220 230, 246 254, 220 260, 240 280, 280 320, 270 350, 270 350, 210 320)))'::GEOMETRY);");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getGeometryN(0).equals(WKT_READER.read("LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, 116 307, 110 299, 103 289, 100 140, 110 142, 270 170)")));
        assertTrue(geom.getGeometryN(1).equals(WKT_READER.read("POLYGON ((210 320, 160 240, 220 230, 246 254, 220 260, 240 280, 280 320, 270 350, 210 320))")));
        rs.close();
    }

    @Test
    public void test_ST_ExtrudeLineString() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('LINESTRING (0 0, 1 0)'::GEOMETRY, 10);");
        rs.next();
        //Test if the wall is created
        assertGeometryEquals("MULTIPOLYGON(((0 0 0, 0 0 10, 1 0 10, 1 0 0, 0 0 0)))", 
                ValueGeometry.getFromGeometry(((Geometry) rs.getObject(1)).getGeometryN(1)).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'::GEOMETRY, 10);");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertGeometryEquals("POLYGON ((0 0 0, 0 1 0, 1 1 0, 1 0 0, 0 0 0))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(0)).getBytes());

        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), "
                + "((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), "
                + "((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0))))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(1)).getBytes());

        //Test the roof
        assertGeometryEquals("POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(2)).getBytes());

        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWithHole() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10),"
                + " (1 3, 3 3, 3 1, 1 1, 1 3))'::GEOMETRY, 10);");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertGeometryEquals("POLYGON ((0 10 0, 10 10 0, 10 0 0, 0 0 0, 0 10 0), (1 3 0, 1 1 0, 3 1 0, 3 3 0, 1 3 0))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(0)).getBytes());
          
        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON (((0 10 0, 0 10 10, 10 10 10, 10 10 0, 0 10 0)), ((10 10 0, 10 10 10, 10 0 10, 10 0 0, 10 10 0)), ((10 0 0, 10 0 10, 0 0 10, 0 0 0, 10 0 0)), ((0 0 0, 0 0 10, 0 10 10, 0 10 0, 0 0 0)), ((1 3 0, 1 3 10, 1 1 10, 1 1 0, 1 3 0)), ((1 1 0, 1 1 10, 3 1 10, 3 1 0, 1 1 0)), ((3 1 0, 3 1 10, 3 3 10, 3 3 0, 3 1 0)), ((3 3 0, 3 3 10, 1 3 10, 1 3 0, 3 3 0)))",
                ValueGeometry.getFromGeometry(outputGeom.getGeometryN(1)).getBytes());

        //Test the roof        
        assertGeometryEquals("POLYGON ((0 10 10, 0 0 10, 10 0 10, 10 10 10, 0 10 10), (1 3 10, 3 3 10, 3 1 10, 1 1 10, 1 3 10))",
                ValueGeometry.getFromGeometry(outputGeom.getGeometryN(2)).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWalls() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'::GEOMETRY, 10, 1);");
        rs.next();
        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), ((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), ((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0)))", rs.getBytes(1));        
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonRoof() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'::GEOMETRY, 10, 2);");
        rs.next();
        //Test the roof
        assertGeometryEquals("POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeEnvelope() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(
                WKT_READER.read("POLYGON((0 0, 1 0 0, 1 1 , 0 1, 0 0))")));
        rs.close();
    }

    @Test
    public void test_ST_MakeEnvelopeSRID() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1, 4326);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(
                WKT_READER.read("POLYGON((0 0, 1 0 0, 1 1 , 0 1, 0 0))")));
        assertTrue(((Geometry) rs.getObject(1)).getSRID() == 4326);
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLineWithoutZ() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('LINESTRING(0 8, 1 8 , 3 8)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 8, 1 8 , 3 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('LINESTRING(0 0 0, 5 0 , 10 0 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 0 0, 5 0 5, 10 0 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('POINT(0 0 0)'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getObject(1) == null);
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('MULTILINESTRING((0 0 0, 5 0 , 10 0 10),(0 0 0, 50 0, 100 0 100))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING((0 0 0, 5 0 5, 10 0 10),(0 0 0, 50 0 50, 100 0 100))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('POINT(0 0 0)'::GEOMETRY, 'POINT(1 1)'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getObject(1) == null);
        rs.close();
    }

    @Test
    public void test_ST_AddPoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('MULTIPOINT((0 0 0))'::GEOMETRY, 'POINT(1 1)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOINT((0 0 0), (1 1))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(1.5 4 )'::GEOMETRY, 4);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 8, 1 8 , 1.5 8, 3 8, 8 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(1.5 4 )'::GEOMETRY);");
        rs.next();
        //The geometry is not modified
        assertGeometryEquals("LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('POLYGON ((118 134, 118 278, 266 278, 266 134, 118 134 ))'::GEOMETRY, 'POINT(196 278 )'::GEOMETRY, 4);");
        rs.next();
        //The geometry is not modified
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((118 134, 118 278,196 278, 266 278, 266 134, 118 134 ))")));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1), (2 2, 4 2, 4 4, 2 4, 2 2))'::GEOMETRY, "
                + "'POINT(3 3 )'::GEOMETRY, 2);");
        rs.next();
        //The geometry is not modified
        assertGeometryEquals("POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1), (2 2, 3 2, 4 2, 4 4, 2 4, 2 2))", rs.getBytes(1));
        rs.close();
    }    
    
    @Test
    public void test_ST_AddPoint7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                + "(2 2, 4 2, 4 4, 2 4, 2 2))'::geometry,'POINT(3 3)'::geometry);");
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                + "(2 2, 4 2, 4 4, 2 4, 2 2))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('POINT(1 1)'::GEOMETRY, ST_Buffer('POINT(1 1)'::GEOMETRY, 10));");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('MULTIPOINT ((5 5), (10 10))'::GEOMETRY, ST_Buffer('POINT(10 10)'::GEOMETRY, 0.01));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((5 5)))")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('MULTIPOINT ((5 5), (10 10), (100 1000))'::GEOMETRY, "
                + "ST_Buffer('POINT(10 10)'::GEOMETRY, 10));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((100 1000)))")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('POLYGON ((150 250, 220 250, 220 170, 150 170, 150 250))'::GEOMETRY, "
                + "ST_Buffer('POINT (230 250)'::GEOMETRY, 12));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((150 250, 220 170, 150 170, 150 250))")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('LINESTRING (100 200, 153 255, 169 175, 200 240, 250 190, "
                + "264 236, 304 236, 320 240, 340 250, 345 265, 354 295)'::GEOMETRY, ST_Buffer('POINT (230 250)'::GEOMETRY, 100));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (100 200, 340 250, 345 265, 354 295)")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('LINESTRING (0 0, 10 0)'::GEOMETRY, "
                + "ST_Buffer('POINT (5 0)'::GEOMETRY, 10));");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('POINT(1 1)'::GEOMETRY, "
                + "ST_Buffer('POINT(100 100)'::GEOMETRY, 10));");
        rs.next();
        assertGeometryEquals("POINT(1 1)",rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('LINESTRING(0 3, 1 1, 3 3, 5 2, 5 4, 6 5, 7 6, 7 7, 6 8)'::GEOMETRY, "
                + "ST_Buffer('POINT (3 4)'::GEOMETRY, 3));");
        rs.next();
        assertGeometryEquals("LINESTRING(0 3, 1 1, 6 5, 7 6, 7 7, 6 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoint('POLYGON((1 1, 1 6, 5 6, 5 1, 1 1), \n" +
" (3 4, 3 5, 4 5, 4 4, 3 4)," +
" (2 3, 3 3, 3 2, 2 2, 2 3))'::GEOMETRY, "
                + "ST_Buffer('POINT (6 7)'::GEOMETRY, 4.5));");
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 1 6, 5 1, 1 1), (2 3, 3 3, 3 2, 2 2, 2 3))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Split1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)'::GEOMETRY, 'POINT(1.5 4 )'::GEOMETRY, 4);");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 2);
        assertTrue(geom.getGeometryN(0).equals(WKT_READER.read("LINESTRING(0 8, 1 8 , 1.5 8)")));
        assertTrue(geom.getGeometryN(1).equals(WKT_READER.read("LINESTRING(1.5 8 , 3 8, 8 8, 10 8, 20 8, 25 8, 30 8, 50 8, 100 8)")));
        rs.close();
    }

    @Test
    public void test_ST_Split2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('LINESTRING(0 0, 100 0)'::GEOMETRY, 'LINESTRING(50 -50, 50 50)'::GEOMETRY);");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 2);
        assertTrue(geom.equals(WKT_READER.read("MULTILINESTRING((0 0, 50 0), (50 0 , 100 0))")));
        rs.close();
    }

    @Test
    public void test_ST_Split3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('LINESTRING(50 0, 100 0)'::GEOMETRY, 'LINESTRING(50 50, 100 50)'::GEOMETRY);");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.getNumGeometries() == 1);
        assertTrue(geom.equals(WKT_READER.read("LINESTRING(50 0, 100 0)")));
        rs.close();
    }

    @Test
    public void test_ST_Split4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))'::GEOMETRY, 'LINESTRING (5 0, 5 10)'::GEOMETRY);");
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
    }

    @Test
    public void test_ST_Split5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))'::GEOMETRY, 'LINESTRING (5 1, 5 8)'::GEOMETRY);");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Split6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0))'::GEOMETRY, 'LINESTRING (5 1, 5 12)'::GEOMETRY);");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Split7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('POLYGON (( 0 0, 10 0, 10 10 , 0 10, 0 0), (2 2, 7 2, 7 7, 2 7, 2 2))'::GEOMETRY, 'LINESTRING (5 0, 5 10)'::GEOMETRY);");
        rs.next();
        Geometry pols = (Geometry) rs.getObject(1);
        assertTrue(pols.getNumGeometries() == 2);
        Polygon pol1 = (Polygon) WKT_READER.read("POLYGON (( 0 0, 5 0, 5 2 ,2 2, 2 7, 5 7, 5 10, 0 10, 0 0))");
        Polygon pol2 = (Polygon) WKT_READER.read("POLYGON ((5 0, 5 2, 7 2, 7 7 , 5 7, 5 10, 10 10, 10 0, 5 0))");
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            if (!pol.getEnvelopeInternal().equals(pol1.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol2.getEnvelopeInternal())) {
                fail();
            }
        }
        rs.close();
    }

    @Test
    public void test_ST_Split8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('POLYGON (( 0 0 1, 10 0 5, 10 10 8 , 0 10 12, 0 0 12))'::GEOMETRY, 'LINESTRING (5 0, 5 10)'::GEOMETRY);");
        rs.next();
        Geometry pols = (Geometry) rs.getObject(1);
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            assertTrue(ST_CoordDim.getCoordinateDimension(
                    ValueGeometry.getFromGeometry(pol).getBytesNoCopy()) == 3);
        }
        rs.close();
    }
    
    @Test
    public void test_ST_Split9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('MULTIPOLYGON ((( 0 0 1, 10 0 5, 10 10 8 , 0 10 12, 0 0 12)))'::GEOMETRY, 'LINESTRING (5 0, 5 10)'::GEOMETRY);");
        rs.next();
        Geometry pols = (Geometry) rs.getObject(1);
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            assertTrue(ST_CoordDim.getCoordinateDimension(
                    ValueGeometry.getFromGeometry(pol).getBytesNoCopy()) == 3);
        }
        rs.close();
    }
    
    @Test
    public void test_ST_Split10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Split('MULTIPOLYGON (((50 200, 150 200, 150 100, 50 100, 50 200)),"
                + "  ((50 50, 150 50, 150 0, 50 0, 50 50)))'::GEOMETRY, 'LINESTRING (100 250, 100 -10)'::GEOMETRY);");
        rs.next();
        Geometry pols = (Geometry) rs.getObject(1);
        assertTrue(pols.getNumGeometries() == 4);
        Polygon pol1 = (Polygon) WKT_READER.read("POLYGON ((50 50, 100 50, 100 0, 50 0, 50 50))");
        Polygon pol2 = (Polygon) WKT_READER.read("POLYGON ((100 50, 150 50, 150 0, 100 0, 100 50))");
        Polygon pol3 = (Polygon) WKT_READER.read("POLYGON ((50 200, 100 200, 100 100, 50 100, 50 200))");
        Polygon pol4 = (Polygon) WKT_READER.read("POLYGON ((100 200, 150 200, 150 100, 100 100, 100 200))");
        for (int i = 0; i < pols.getNumGeometries(); i++) {
            Geometry pol = pols.getGeometryN(i);
            if (!pol.getEnvelopeInternal().equals(pol1.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol2.getEnvelopeInternal())
                    &&!pol.getEnvelopeInternal().equals(pol3.getEnvelopeInternal())
                    && !pol.getEnvelopeInternal().equals(pol4.getEnvelopeInternal())) {
                fail();
            }
        }
        rs.close();
    }
    

    @Test
    public void test_ST_Translate1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Translate('POINT(-71.01 42.37)'::GEOMETRY, 1, 0);");
        rs.next();
        assertGeometryEquals("POINT(-70.01 42.37)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Translate2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Translate('LINESTRING(-71.01 42.37,-71.11 42.38)'::GEOMETRY, 1, 0.5);");
        rs.next();
        assertGeometryEquals("LINESTRING(-70.01 42.87,-70.11 42.88)", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Translate3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Translate('POINT(0 0 0)'::GEOMETRY, 5, 12, 3);");
        rs.next();
        assertGeometryEquals("POINT(5 12 3)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Translate4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Translate('POINT(1 2 3)'::GEOMETRY, 10, 20, 30);");
        rs.next();
        assertGeometryEquals("POINT(11 22 33)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_TranslateNull() throws Exception {
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_Translate(NULL, 1, 2), " +
                "ST_Translate(NULL, 1, 2, 3);");
        rs.next();
        assertNull(rs.getBytes(1));
        assertNull(rs.getBytes(2));
        rs.close();
    }

    @Test
    public void test_ST_TranslateSameDimension() throws Exception {
        ResultSet rs = st.executeQuery("SELECT " +
                "ST_Translate('LINESTRING(0 0, 1 0)', 1, 2), " +
                "ST_Translate('LINESTRING(0 0, 1 0)', 1, 2, 3), " +
                "ST_Translate('LINESTRING(0 0 0, 1 0 0)', 1, 2), " +
                "ST_Translate('LINESTRING(0 0 0, 1 0 0)', 1, 2, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING(1 2, 2 2)", rs.getBytes(1));
        assertGeometryEquals("LINESTRING(1 2, 2 2)", rs.getBytes(2));
        assertGeometryEquals("LINESTRING(1 2 0, 2 2 0)", rs.getBytes(3));
        assertGeometryEquals("LINESTRING(1 2 3, 2 2 3)", rs.getBytes(4));
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_TranslateMixedDimensionXY() throws Throwable {
        try {
            st.executeQuery("SELECT " +
                    "ST_Translate('LINESTRING(0 0, 1 0 0)', 1, 2);");
        } catch (JdbcSQLException e) {
            final Throwable originalCause = e.getOriginalCause();
            assertEquals(ST_Translate.MIXED_DIM_ERROR,
                    originalCause.getMessage());
            throw originalCause;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_TranslateMixedDimensionXYZ() throws Throwable {
        try {
            st.executeQuery("SELECT " +
                    "ST_Translate('LINESTRING(0 0, 1 0 0)', 1, 2, 3);");
        } catch (JdbcSQLException e) {
            final Throwable originalCause = e.getOriginalCause();
            assertEquals(ST_Translate.MIXED_DIM_ERROR,
                    originalCause.getMessage());
            throw originalCause;
        }
    }

    @Test
    public void test_ST_ReverseNULL() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse(NULL);");
        rs.next();
        assertGeometryEquals(null, rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ReverseMultiPoint() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse('MULTIPOINT((4 4), (1 1), (1 0), (0 3))');");
        rs.next();
        assertGeometryEquals("MULTIPOINT((0 3), (1 0), (1 1), (4 4))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse('LINESTRING (105 353, 150 180, 300 280)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (300 280, 150 180, 105 353)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((190 300, 380 430, 430 270, 313 117, 300 110, 140 180, 190 300))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse('MULTILINESTRING ((10 260, 150 290, 186 406, 286 286), "
                + " (120 120, 130 125, 142 129, 360 160, 357 170, 380 340))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((380 340, 357 170, 360 160, 142 129, 130 125, 120 120), \n"
                + " (286 286, 186 406, 150 290, 10 260))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine1() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (105 353 10, 150 180, 300 280 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRING (105 353 10, 150 180, 300 280 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (300 280 0, 150 180,105 353 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRING (300 280 10, 150 180,105 353 0 )'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (105 353 0, 150 180, 300 280 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine3() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom LINESTRING);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING (105 353 10, 150 180, 300 280 0)'));");
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRING (105 353 10, 150 180, 300 280 0)'::GEOMETRY, 'desc');");
        rs.next();
        assertGeometryEquals("LINESTRING (105 353 10, 150 180, 300 280 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRING (105 353 0, 150 180, 300 280 10)'::GEOMETRY, 'desc');");
        rs.next();
        assertGeometryEquals("LINESTRING (300 280 10, 150 180,105 353 0 )", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))'::GEOMETRY);");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveHoles1() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveHoles(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_RemoveHoles2() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POLYGON);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((100 370, 335 370, 335 135, 100 135, 100 370), \n"
                + " (140 340, 120 280, 133 277, 170 280, 181 281, 220 340, 214 336, 180 340, 185 342, 155 342, 140 340), \n"
                + " (255 293, 210 230, 260 220, 292 265, 255 293))'));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveHoles(the_geom) FROM input_table;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((100 370, 335 370, 335 135, 100 135, 100 370))")));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_UpdateZ1() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom POINT);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POINT (190 300)'));");
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ(the_geom, 10) FROM input_table;");
        rs.next();
        assertGeometryEquals("POINT (190 300 10)", rs.getBytes(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_UpdateZ2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINT( (190 300), (10 11 2))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 10), (10 11 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINT( (190 300), (10 11 2))'::GEOMETRY, 10, 3);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 10), (10 11 2))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINT( (190 300 1), (10 11))'::GEOMETRY, 10, 2);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 10), (10 11))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ5() throws SQLException {
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
        ResultSet rs = st.executeQuery("SELECT ST_AddZ('MULTIPOINT( (190 300 1), (10 11))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 11), (10 11))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddZ2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddZ('MULTIPOINT( (190 300), (10 11))'::GEOMETRY, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11))")));
        rs.close();
    }

    @Test
    public void test_ST_AddZ3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddZ('MULTIPOINT( (190 300 10), (10 11 5))'::GEOMETRY, -10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 0), (10 11 -5))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MultiplyZ1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ('MULTIPOINT( (190 300 1), (10 11))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 10), (10 11))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MultiplyZ2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ('MULTIPOINT( (190 300), (10 11))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300), (10 11))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MultiplyZ3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ('MULTIPOINT( (190 300 100), (10 11 50))'::GEOMETRY, 0.1);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 10), (10 11 5))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_PrecisionReducer1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_PrecisionReducer('MULTIPOINT( (190 300 100), (10 11 50))'::GEOMETRY, 0.1);");
        rs.next();
        assertGeometryEquals("MULTIPOINT( (190 300 100), (10 11 50))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_PrecisionReducer2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_PrecisionReducer('MULTIPOINT( (190.005 300 100), (10.534 11 50))'::GEOMETRY, 1);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300 100), (10.5 11 50))")));
        rs.close();
    }

    @Test
    public void test_ST_PrecisionReducer3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_PrecisionReducer('MULTIPOINT( (190.005 300 100), (10.534 11 50))'::GEOMETRY, 4);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190.005 300 100), (10.534 11 50))")));
        rs.close();
    }

    @Test
    public void test_ST_Simplify1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Simplify('MULTIPOINT( (190 300), (10 11 50))'::GEOMETRY, 4);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11 50))")));
        rs.close();
    }

    @Test
    public void test_ST_Simplify2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Simplify('LINESTRING (250 250, 280 290, 300 230, 340 300, 360 260, 440 310, 470 360, 604 286)'::GEOMETRY, 40);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (250 250, 280 290, 300 230, 470 360, 604 286)")));
        rs.close();
    }

    @Test
    public void test_ST_Simplify3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Simplify('POLYGON ((250 250, 248 240, 250 180, 290 150, 332 165, 350 190, 330 200, 340 220, 360 260, 360 300, 330 310, 319 310, 300 310, 280 280, 256 284, 250 250))'::GEOMETRY, 40);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((250 250, 360 300, 332 165, 250 180, 250 250))")));
        rs.close();
    }

    @Test
    public void test_ST_SimplifyPreserveTopology1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPreserveTopology('MULTIPOINT( (190 300), (10 11 50))'::GEOMETRY, 4);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT( (190 300), (10 11 50))")));
        rs.close();
    }

    @Test
    public void test_ST_SimplifyPreserveTopology2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPreserveTopology('LINESTRING (250 250, 280 290, 300 230, 340 300, 360 260, 440 310, 470 360, 604 286)'::GEOMETRY, 40);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (250 250, 280 290, 300 230, 470 360, 604 286)")));
        rs.close();
    }

    @Test
    public void test_ST_SimplifyPreserveTopology3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPreserveTopology('POLYGON ((250 250, 248 240, 250 180, 290 150, 332 165, 350 190, 330 200, 340 220, 360 260, 360 300, 330 310, 319 310, 300 310, 280 280, 256 284, 250 250))'::GEOMETRY, 40);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((250 250, 360 300, 332 165, 250 180, 250 250))")));
        rs.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('LINESTRING (250 250, 280 290)'::GEOMETRY, 40, 10);");
        rs.next();
        assertGeometryEquals("LINESTRING (250 250 40, 280 290 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('LINESTRING(0 0, 5 0 , 10 0)'::GEOMETRY, 0, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING(0 0 0, 5 0 5, 10 0 10)")));
        rs.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities3() throws SQLException {
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('POINT (190 300 10)'::GEOMETRY, 10, 9999);");
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
    public void test_ST_ZUpdateExtremities4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('MULTILINESTRING((0 0 12, 5 0 200 , 10 0 20),(0 0 1, 5 0 , 10 0 2))'::GEOMETRY, 0, 10);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING((0 0 0, 5 0 5, 10 0 10),(0 0 0, 5 0 5, 10 0 10))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ZUpdateExtremities5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('MULTILINESTRING((0 0 12, 5 0 200 , 10 0 20),(0 0 1, 5 0 , 10 0 2))'::GEOMETRY, 0, 10, true);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING((0 0 0, 5 0 5, 10 0 10),(0 0 0, 5 0 5, 10 0 10))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ZUpdateExtremities6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('MULTILINESTRING((0 0 12, 5 0 200 , 10 0 20),(0 0 1, 5 0 , 10 0 2))'::GEOMETRY, 0, 10, false);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING((0 0 0, 5 0 200, 10 0 10),(0 0 0, 5 0 , 10 0 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Normalize() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Normalize('POLYGON ((170 180, 310 180, 308 190, 310 206, 340 320, 135 333, 140 260, 170 180))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((135 333, 340 320, 310 206, 308 190, 310 180, 170 180, 140 260, 135 333))")));
        rs.close();
    }

    @Test
    public void test_ST_Polygonize1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Polygonize('MULTILINESTRING ((130 190, 80 370, 290 380), \n"
                + " (290 380, 270 270, 130 190))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON ( ((130 190, 80 370, 290 380, 270 270, 130 190)))")));
        rs.close();
    }

    @Test
    public void test_ST_Polygonize2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Polygonize('MULTILINESTRING ((50 240, 62 250, 199 425, 250 240), \n"
                + " (50 340, 170 250, 300 370))'::GEOMETRY);");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Polygonize3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Polygonize(st_union('MULTILINESTRING ((50 240, 62 250, 199 425, 250 240), \n"
                + " (50 340, 170 250, 300 370))'::GEOMETRY));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON( ((231.5744116672191 306.8379184620484, 170 250, 101.95319531953196 301.03510351035106, 199 425, 231.5744116672191 306.8379184620484)))")));
        rs.close();
    }

    @Test
    public void test_ST_RingBuffer1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POINT(10 10)'::GEOMETRY, 10, 3);");
        rs.next();
        assertGeometryBarelyEquals("MULTIPOLYGON (((20 10, 19.807852804032304 8.049096779838717, "
                        + "19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803978, "
                        + "17.071067811865476 2.9289321881345254, 15.555702330196024 1.6853038769745474, "
                        + "13.826834323650898 0.7612046748871322, 11.950903220161283 0.1921471959676957, "
                        + "10 0, 8.049096779838719 0.1921471959676957, 6.173165676349103 0.7612046748871322, "
                        + "4.44429766980398 1.6853038769745474, 2.9289321881345254 2.9289321881345245, "
                        + "1.6853038769745474 4.444297669803978, 0.7612046748871322 6.173165676349106, "
                        + "0.1921471959676939 8.049096779838722, 0 10.000000000000007, 0.1921471959676975 11.950903220161292, "
                        + "0.7612046748871375 13.826834323650909, 1.6853038769745545 15.555702330196034, "
                        + "2.928932188134537 17.071067811865486, 4.444297669803992 18.314696123025463, "
                        + "6.173165676349122 19.238795325112875, 8.04909677983874 19.807852804032308, 10.000000000000025 20, "
                        + "11.950903220161308 19.8078528040323, 13.826834323650925 19.238795325112857, "
                        + "15.555702330196048 18.314696123025435, 17.071067811865497 17.07106781186545, "
                        + "18.31469612302547 15.555702330195993, 19.238795325112882 13.826834323650862, 19.80785280403231 "
                        + "11.950903220161244, 20 10)), ((30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 "
                        + "2.346331352698204, 26.629392246050905 -1.1114046603920436, 24.14213562373095 -4.142135623730949, "
                        + "21.111404660392047 -6.629392246050905, 17.653668647301796 -8.477590650225736, 13.901806440322567 "
                        + "-9.615705608064609, 10.000000000000002 -10, 6.098193559677436 -9.615705608064609, 2.346331352698206 "
                        + "-8.477590650225736, -1.11140466039204 -6.629392246050905, -4.142135623730949 -4.142135623730951, "
                        + "-6.629392246050905 -1.1114046603920436, -8.477590650225736 2.3463313526982112, "
                        + "-9.615705608064612 6.098193559677446, -10 10.000000000000016, "
                        + "-9.615705608064605 13.901806440322584, -8.477590650225725 17.653668647301817, -6.629392246050891 21.11140466039207, -4.142135623730926 24.142135623730972, -1.1114046603920151 26.629392246050926, 2.3463313526982423 28.47759065022575, 6.098193559677479 29.615705608064616, 10.00000000000005 30, 13.901806440322618 29.615705608064598, 17.65366864730185 28.477590650225714, 21.111404660392097 26.62939224605087, 24.142135623730997 24.1421356237309, 26.629392246050944 21.111404660391987, 28.477590650225764 17.653668647301725, 29.615705608064623 13.901806440322488, 30 10), (20 10, 19.80785280403231 11.950903220161244, 19.238795325112882 13.826834323650862, 18.31469612302547 15.555702330195993, 17.071067811865497 17.07106781186545, 15.555702330196048 18.314696123025435, 13.826834323650925 19.238795325112857, 11.950903220161308 19.8078528040323, 10.000000000000025 20, 8.04909677983874 19.807852804032308, 6.173165676349122 19.238795325112875, 4.444297669803992 18.314696123025463, 2.928932188134537 17.071067811865486, 1.6853038769745545 15.555702330196034, 0.7612046748871375 13.826834323650909, 0.1921471959676975 11.950903220161292, 0 10.000000000000007, 0.1921471959676939 8.049096779838722, 0.7612046748871322 6.173165676349106, 1.6853038769745474 4.444297669803978, 2.9289321881345254 2.9289321881345245, 4.44429766980398 1.6853038769745474, 6.173165676349103 0.7612046748871322, 8.049096779838719 0.1921471959676957, 10 0, 11.950903220161283 0.1921471959676957, 13.826834323650898 0.7612046748871322, 15.555702330196024 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803978, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10)), ((40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.944088369076354 -6.667106990588067, 31.213203435596427 -11.213203435596423, 26.667106990588067 -14.944088369076358, 21.480502970952696 -17.716385975338603, 15.85270966048385 -19.423558412096913, 10.000000000000002 -20, 4.147290339516154 -19.423558412096913, -1.480502970952692 -17.716385975338603, -6.66710699058806 -14.944088369076361, -11.213203435596423 -11.213203435596427, -14.944088369076361 -6.667106990588067, -17.716385975338607 -1.4805029709526831, -19.423558412096916 4.147290339516168, -20 10.000000000000023, -19.423558412096906 15.852709660483876, -17.71638597533859 21.480502970952728, -14.944088369076333 26.667106990588103, -11.213203435596391 31.213203435596462, -6.667106990588021 34.94408836907638, -1.480502970952637 37.716385975338625, 4.147290339516219 39.423558412096924, 10.000000000000075 40, 15.852709660483928 39.423558412096895, 21.480502970952774 37.71638597533857, 26.667106990588145 34.944088369076304, 31.213203435596498 31.213203435596355, 34.94408836907642 26.66710699058798, 37.716385975338646 21.48050297095259, 39.42355841209694 15.85270966048373, 40 10), (30 10, 29.615705608064623 13.901806440322488, 28.477590650225764 17.653668647301725, 26.629392246050944 21.111404660391987, 24.142135623730997 24.1421356237309, 21.111404660392097 26.62939224605087, 17.65366864730185 28.477590650225714, 13.901806440322618 29.615705608064598, 10.00000000000005 30, 6.098193559677479 29.615705608064616, 2.3463313526982423 28.47759065022575, -1.1114046603920151 26.629392246050926, -4.142135623730926 24.142135623730972, -6.629392246050891 21.11140466039207, -8.477590650225725 17.653668647301817, -9.615705608064605 13.901806440322584, -10 10.000000000000016, -9.615705608064612 6.098193559677446, -8.477590650225736 2.3463313526982112, -6.629392246050905 -1.1114046603920436, -4.142135623730949 -4.142135623730951, -1.11140466039204 -6.629392246050905, 2.346331352698206 -8.477590650225736, 6.098193559677436 -9.615705608064609, 10.000000000000002 -10, 13.901806440322567 -9.615705608064609, 17.653668647301796 -8.477590650225736, 21.111404660392047 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920436, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10)))"
        , rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBuffer2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3);");
        rs.next();
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803979, 17.071067811865476 2.9289321881345254, 15.55570233019602 1.6853038769745474, 13.826834323650894 0.7612046748871304, 11.950903220161276 0.1921471959676939, 10 0, -10 0, -11.950903220161287 0.1921471959676975, -13.826834323650903 0.7612046748871357, -15.555702330196022 1.6853038769745474, -17.071067811865476 2.9289321881345254, -18.314696123025456 4.44429766980398, -19.238795325112868 6.173165676349104, -19.807852804032304 8.049096779838717, -20 10.000000000000002, -19.807852804032304 11.950903220161287, -19.238795325112868 13.8268343236509, -18.314696123025453 15.555702330196022, -17.071067811865476 17.071067811865476, -15.55570233019602 18.314696123025453, -13.826834323650893 19.238795325112868, -11.950903220161276 19.807852804032308, -10 20, 10 20)), ((10 30, 13.901806440322567 29.61570560806461, 17.653668647301796 28.477590650225736, 21.111404660392047 26.629392246050905, 24.14213562373095 24.14213562373095, 26.629392246050905 21.111404660392044, 28.477590650225736 17.653668647301796, 29.61570560806461 13.901806440322565, 30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 2.346331352698204, 26.629392246050905 -1.1114046603920418, 24.14213562373095 -4.142135623730949, 21.11140466039204 -6.629392246050905, 17.65366864730179 -8.47759065022574, 13.901806440322552 -9.615705608064612, 10 -10, -10 -10, -13.901806440322574 -9.615705608064605, -17.653668647301807 -8.477590650225729, -21.111404660392044 -6.629392246050905, -24.142135623730955 -4.142135623730949, -26.62939224605091 -1.11140466039204, -28.477590650225736 2.346331352698207, -29.61570560806461 6.098193559677433, -30 10.000000000000002, -29.61570560806461 13.901806440322572, -28.477590650225736 17.6536686473018, -26.629392246050905 21.111404660392044, -24.14213562373095 24.14213562373095, -21.11140466039204 26.629392246050905, -17.653668647301785 28.47759065022574, -13.90180644032255 29.615705608064612, -10 30, 10 30), (10 20, -10 20, -11.950903220161276 19.807852804032308, -13.826834323650893 19.238795325112868, -15.55570233019602 18.314696123025453, -17.071067811865476 17.071067811865476, -18.314696123025453 15.555702330196022, -19.238795325112868 13.8268343236509, -19.807852804032304 11.950903220161287, -20 10.000000000000002, -19.807852804032304 8.049096779838717, -19.238795325112868 6.173165676349104, -18.314696123025456 4.44429766980398, -17.071067811865476 2.9289321881345254, -15.555702330196022 1.6853038769745474, -13.826834323650903 0.7612046748871357, -11.950903220161287 0.1921471959676975, -10 0, 10 0, 11.950903220161276 0.1921471959676939, 13.826834323650894 0.7612046748871304, 15.55570233019602 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803979, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10, 19.807852804032304 11.950903220161283, 19.238795325112868 13.826834323650898, 18.314696123025453 15.555702330196022, 17.071067811865476 17.071067811865476, 15.555702330196024 18.314696123025453, 13.826834323650898 19.238795325112868, 11.950903220161283 19.807852804032304, 10 20)), ((10 40, 15.85270966048385 39.42355841209691, 21.480502970952696 37.7163859753386, 26.667106990588067 34.944088369076354, 31.213203435596427 31.213203435596423, 34.944088369076354 26.667106990588067, 37.7163859753386 21.480502970952692, 39.42355841209691 15.852709660483846, 40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.94408836907636 -6.6671069905880636, 31.213203435596427 -11.213203435596423, 26.667106990588064 -14.944088369076361, 21.48050297095268 -17.71638597533861, 15.85270966048383 -19.423558412096916, 10 -20, -10 -20, -15.85270966048386 -19.42355841209691, -21.48050297095271 -17.716385975338596, -26.667106990588067 -14.944088369076358, -31.21320343559643 -11.213203435596423, -34.94408836907637 -6.66710699058806, -37.71638597533861 -1.4805029709526902, -39.42355841209691 4.147290339516149, -40 10.000000000000004, -39.42355841209691 15.852709660483859, -37.7163859753386 21.4805029709527, -34.94408836907636 26.667106990588067, -31.213203435596423 31.213203435596427, -26.66710699058806 34.94408836907636, -21.480502970952678 37.71638597533861, -15.852709660483827 39.42355841209692, -10 40, 10 40), (10 30, -10 30, -13.90180644032255 29.615705608064612, -17.653668647301785 28.47759065022574, -21.11140466039204 26.629392246050905, -24.14213562373095 24.14213562373095, -26.629392246050905 21.111404660392044, -28.477590650225736 17.6536686473018, -29.61570560806461 13.901806440322572, -30 10.000000000000002, -29.61570560806461 6.098193559677433, -28.477590650225736 2.346331352698207, -26.62939224605091 -1.11140466039204, -24.142135623730955 -4.142135623730949, -21.111404660392044 -6.629392246050905, -17.653668647301807 -8.477590650225729, -13.901806440322574 -9.615705608064605, -10 -10, 10 -10, 13.901806440322552 -9.615705608064612, 17.65366864730179 -8.47759065022574, 21.11140466039204 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920418, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10, 29.61570560806461 13.901806440322565, 28.477590650225736 17.653668647301796, 26.629392246050905 21.111404660392044, 24.14213562373095 24.14213562373095, 21.111404660392047 26.629392246050905, "
                + "17.653668647301796 28.477590650225736, 13.901806440322567 29.61570560806461, 10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferEndCapSQUARE() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POINT(2 2)', 2, 2, 'square');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON(((4 4, 4 0, 0 0, 0 4, 4 4)), ((6 6, 6 -2, -2 -2, -2 6, 6 6), (4 4, 0 4, 0 0, 4 0, 4 4)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferEndCapROUND() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'ROUND');");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803979, 17.071067811865476 2.9289321881345254, 15.55570233019602 1.6853038769745474, 13.826834323650894 0.7612046748871304, 11.950903220161276 0.1921471959676939, 10 0, -10 0, -11.950903220161287 0.1921471959676975, -13.826834323650903 0.7612046748871357, -15.555702330196022 1.6853038769745474, -17.071067811865476 2.9289321881345254, -18.314696123025456 4.44429766980398, -19.238795325112868 6.173165676349104, -19.807852804032304 8.049096779838717, -20 10.000000000000002, -19.807852804032304 11.950903220161287, -19.238795325112868 13.8268343236509, -18.314696123025453 15.555702330196022, -17.071067811865476 17.071067811865476, -15.55570233019602 18.314696123025453, -13.826834323650893 19.238795325112868, -11.950903220161276 19.807852804032308, -10 20, 10 20)), ((10 30, 13.901806440322567 29.61570560806461, 17.653668647301796 28.477590650225736, 21.111404660392047 26.629392246050905, 24.14213562373095 24.14213562373095, 26.629392246050905 21.111404660392044, 28.477590650225736 17.653668647301796, 29.61570560806461 13.901806440322565, 30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 2.346331352698204, 26.629392246050905 -1.1114046603920418, 24.14213562373095 -4.142135623730949, 21.11140466039204 -6.629392246050905, 17.65366864730179 -8.47759065022574, 13.901806440322552 -9.615705608064612, 10 -10, -10 -10, -13.901806440322574 -9.615705608064605, -17.653668647301807 -8.477590650225729, -21.111404660392044 -6.629392246050905, -24.142135623730955 -4.142135623730949, -26.62939224605091 -1.11140466039204, -28.477590650225736 2.346331352698207, -29.61570560806461 6.098193559677433, -30 10.000000000000002, -29.61570560806461 13.901806440322572, -28.477590650225736 17.6536686473018, -26.629392246050905 21.111404660392044, -24.14213562373095 24.14213562373095, -21.11140466039204 26.629392246050905, -17.653668647301785 28.47759065022574, -13.90180644032255 29.615705608064612, -10 30, 10 30), (10 20, -10 20, -11.950903220161276 19.807852804032308, -13.826834323650893 19.238795325112868, -15.55570233019602 18.314696123025453, -17.071067811865476 17.071067811865476, -18.314696123025453 15.555702330196022, -19.238795325112868 13.8268343236509, -19.807852804032304 11.950903220161287, -20 10.000000000000002, -19.807852804032304 8.049096779838717, -19.238795325112868 6.173165676349104, -18.314696123025456 4.44429766980398, -17.071067811865476 2.9289321881345254, -15.555702330196022 1.6853038769745474, -13.826834323650903 0.7612046748871357, -11.950903220161287 0.1921471959676975, -10 0, 10 0, 11.950903220161276 0.1921471959676939, 13.826834323650894 0.7612046748871304, 15.55570233019602 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803979, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10, 19.807852804032304 11.950903220161283, 19.238795325112868 13.826834323650898, 18.314696123025453 15.555702330196022, 17.071067811865476 17.071067811865476, 15.555702330196024 18.314696123025453, 13.826834323650898 19.238795325112868, 11.950903220161283 19.807852804032304, 10 20)), ((10 40, 15.85270966048385 39.42355841209691, 21.480502970952696 37.7163859753386, 26.667106990588067 34.944088369076354, 31.213203435596427 31.213203435596423, 34.944088369076354 26.667106990588067, 37.7163859753386 21.480502970952692, 39.42355841209691 15.852709660483846, 40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.94408836907636 -6.6671069905880636, 31.213203435596427 -11.213203435596423, 26.667106990588064 -14.944088369076361, 21.48050297095268 -17.71638597533861, 15.85270966048383 -19.423558412096916, 10 -20, -10 -20, -15.85270966048386 -19.42355841209691, -21.48050297095271 -17.716385975338596, -26.667106990588067 -14.944088369076358, -31.21320343559643 -11.213203435596423, -34.94408836907637 -6.66710699058806, -37.71638597533861 -1.4805029709526902, -39.42355841209691 4.147290339516149, -40 10.000000000000004, -39.42355841209691 15.852709660483859, -37.7163859753386 21.4805029709527, -34.94408836907636 26.667106990588067, -31.213203435596423 31.213203435596427, -26.66710699058806 34.94408836907636, -21.480502970952678 37.71638597533861, -15.852709660483827 39.42355841209692, -10 40, 10 40), (10 30, -10 30, -13.90180644032255 29.615705608064612, -17.653668647301785 28.47759065022574, -21.11140466039204 26.629392246050905, -24.14213562373095 24.14213562373095, -26.629392246050905 21.111404660392044, -28.477590650225736 17.6536686473018, -29.61570560806461 13.901806440322572, -30 10.000000000000002, -29.61570560806461 6.098193559677433, -28.477590650225736 2.346331352698207, -26.62939224605091 -1.11140466039204, -24.142135623730955 -4.142135623730949, -21.111404660392044 -6.629392246050905, -17.653668647301807 -8.477590650225729, -13.901806440322574 -9.615705608064605, -10 -10, 10 -10, 13.901806440322552 -9.615705608064612, 17.65366864730179 -8.47759065022574, 21.11140466039204 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920418, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10, 29.61570560806461 13.901806440322565, 28.477590650225736 17.653668647301796, 26.629392246050905 21.111404660392044, 24.14213562373095 24.14213562373095, 21.111404660392047 26.629392246050905, "
                        + "17.653668647301796 28.477590650225736, 13.901806440322567 29.61570560806461, 10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test(expected = SQLException.class)
    public void test_ST_RingBufferEndCapBUTT() throws Exception {
        st.execute("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'BUTT');");
    }

    @Test
    public void test_ST_RingBufferNoHole() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POINT(2 2)', 2, 2, 'square', false);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON(((4 4, 4 0, 0 0, 0 4, 4 4)), ((6 6, 6 -2, -2 -2, -2 6, 6 6)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MinimumDiameter1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumDiameter('LINESTRING (50 240, 62 250, 199 425, 250 240)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (128.69067451174988 337.7031864743203, 250 240)")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumDiameter2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumDiameter('POLYGON ((360 380, 230 150, 370 100, 510 100, 517 110, 650 390, 430 220, 360 380))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (282.3538681948424 242.62607449856733, 517 110)")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumDiameter3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumDiameter('POINT (395 278)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(WKT_READER.read("LINESTRING (395 278, 395 278)")));
        rs.close();
    }

    @Test
    public void test_ST_Azimuth1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT degrees(ST_Azimuth(ST_MakePoint(0, 0), ST_MakePoint(0, 10)) ) as degAz;");
        rs.next();
        assertEquals(rs.getDouble(1), 0, 0.00001);
        rs.close();
    }

    @Test
    public void test_ST_Azimuth2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT degrees(ST_Azimuth(ST_MakePoint(0, 0), ST_MakePoint(10, 0)) ) as degAz;");
        rs.next();
        assertEquals(rs.getDouble(1), 90, 0.00001);
        rs.close();
    }

    @Test
    public void test_ST_Azimuth3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT degrees(ST_Azimuth(ST_MakePoint(0, 0), ST_MakePoint(0, -10)) ) as degAz;");
        rs.next();
        assertEquals(rs.getDouble(1), 180, 0.00001);
        rs.close();
    }

    @Test
    public void test_ST_Azimuth4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT degrees(ST_Azimuth(ST_MakePoint(0, 0), ST_MakePoint(0, 0)) ) as degAz;");
        rs.next();
        assertEquals(rs.getDouble(1), 0, 0.00001);
        rs.close();
    }

    @Test
    public void test_ST_Snap1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Snap('LINESTRING(1 2, 2 4, 4 4, 5 2)'::GEOMETRY, "
                + "'LINESTRING(5 2, 2 1, 1 2)'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("LINESTRING(1 2, 2 4, 4 4, 5 2)",rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Snap2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Snap('LINESTRING(1 2, 2 4, 4 4, 5 2)'::GEOMETRY, "
                + "'LINESTRING(5 2, 2 1, 1 2)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING(1 2, 1 2, 2 1, 5 2, 5 2)",rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Snap3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Snap('POLYGON((3 3, 1 2, 0 2, 0 1, -2 1, -1 7, 3 6, 4 8,7 8, 6 6, 9 6, 8 1, 8 1, 3 3))'::GEOMETRY,"
                +"'POLYGON((1 1, 1 7, 7 7, 7 1, 1 1))'::GEOMETRY, 2);");
        rs.next();
        assertGeometryEquals("POLYGON((3 3, 1 1, 1 1, 1 1, -2 1, -1 7, 1 7, 3 6, 4 8, 7 7, 7 7, 9 6, 7 1, 7 1, 3 3))"
                ,rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_IsValidReason1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidReason('POLYGON ((280 330, 120 200, 360 190, 352 197, 170 290, 280 330))'::GEOMETRY);");
        rs.next();
        assertNotNull(rs.getString(1));
        assertEquals( "Self-intersection at or near point (207.3066943435392, 270.9366891541256, NaN)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_IsValidReason2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidReason('POLYGON ((0 350, 200 350, 200 250, 0 250, 0 350))'::GEOMETRY);");
        rs.next();
        assertNotNull(rs.getString(1));
        assertEquals("Valid Geometry", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_IsValidReason3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidReason('LINESTRING (130 340, 280 210, 120 210, 320 350)'::GEOMETRY, 1);");
        rs.next();
        assertNotNull(rs.getString(1));
        assertEquals( "Valid Geometry", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_IsValidReason4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidReason('LINESTRING (130 340, 280 210, 120 210, 320 350)'::GEOMETRY, 0);");
        rs.next();
        assertNotNull(rs.getString(1));
        assertEquals( "Valid Geometry", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_IsValidReason5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidReason(null);");
        rs.next();
        assertNotNull(rs.getString(1));
        assertEquals( "Null Geometry", rs.getString(1));
        rs.close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_ST_IsValidReason6() throws Throwable {
        try {
            st.execute("SELECT ST_IsvalidReason('LINESTRING (80 240, 330 330, 280 240, 190 360)'::GEOMETRY, 199);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test
    public void test_ST_IsValidRDetail1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON ((280 330, 120 200, 360 190, 352 197, 170 290, 280 330))'::GEOMETRY);");
        rs.next();
        Object[] results = (Object [])rs.getObject(1);
        assertNotNull(results);
        assertFalse((Boolean)results[0]);
        assertEquals( "Self-intersection", results[1]);
        assertGeometryEquals("POINT(207.3066943435392 270.9366891541256)", ValueGeometry.getFromGeometry(results[2]).getBytes());
        rs.close();
    }
    
     @Test
    public void test_ST_IsValidRDetail2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON ((210 440, 134 235, 145 233, 310 200, 340 360, 210 440))'::GEOMETRY);");
        rs.next();
        Object[] results = (Object [])rs.getObject(1);
        assertNotNull(results);
        assertTrue((Boolean)results[0]);
        assertEquals( "Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }
     
    @Test
    public void test_ST_IsValidRDetail3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail(null);");
        rs.next();       
        assertNull(rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_IsValidRDetail4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('LINESTRING (80 240, 330 330, 280 240, 190 360)'::GEOMETRY, 1);");
        rs.next();
        Object[] results = (Object[]) rs.getObject(1);
        assertNotNull(results);
        assertTrue((Boolean) results[0]);
        assertEquals("Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }
    
    @Test
    public void test_ST_IsValidRDetail5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON ((100 340, 300 340, 300 180, 100 180, 100 340),"
                + "  (120 320, 160 320, 160 290, 120 290, 120 320))'::GEOMETRY, 0);");
        rs.next();
        Object[] results = (Object[]) rs.getObject(1);
        assertNotNull(results);
        assertTrue((Boolean) results[0]);
        assertEquals("Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_ST_IsValidRDetail6() throws Throwable {
        try {
            st.execute("SELECT ST_IsvalidDetail('LINESTRING (80 240, 330 330, 280 240, 190 360)'::GEOMETRY, 199);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
    
    @Test
    public void test_ST_IsValidRDetail8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2,3 0))'::geometry,0);");
        rs.next();
        Object[] results = (Object[]) rs.getObject(1);
        assertNotNull(results);
        assertFalse((Boolean) results[0]);
        assertEquals("Ring Self-intersection", results[1]);
        assertGeometryEquals("POINT(3 0)", ValueGeometry.getFromGeometry(results[2]).getBytes());
        rs.close();
    }
    
    @Test
    public void test_ST_IsValidRDetail9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2,3 0))'::geometry,1)");
        rs.next();
        Object[] results = (Object[]) rs.getObject(1);
        assertNotNull(results);
        assertTrue((Boolean) results[0]);
        assertEquals("Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }

    @Test
    public void test_ST_MakePolygon1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250, 100 250)'::GEOMETRY );");
        rs.next();
        assertGeometryEquals("POLYGON ((100 250, 100 350, 200 350, 200 250, 100 250))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MakePolygon2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250, 100 250)'::GEOMETRY, "
                + "'LINESTRING(120 320, 150 320, 150 300, 120 300, 120 320)'::GEOMETRY );");
        rs.next();
        assertGeometryEquals("POLYGON ((100 250, 100 350, 200 350, 200 250, 100 250), \n"
                + "  (120 320, 150 320, 150 300, 120 300, 120 320))", rs.getBytes(1));
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_MakePolygon3() throws Throwable {
        try {
            st.execute("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250)'::GEOMETRY, "
                    + "'LINESTRING(120 320, 150 320, 150 300, 120 300, 120 320)'::GEOMETRY );");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_MakePolygon4() throws Throwable {
        try {
            st.execute("SELECT ST_MakePolygon('POINT (100 250)'::GEOMETRY );");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_MakePolygon5() throws Throwable {
        try {
            st.execute("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250)'::GEOMETRY);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
    @Test
    public void test_ST_Force3D1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRING (-10 10, 10 10 3)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (-10 10 0, 10 10 3)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRING (-10 10, 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (-10 10 0, 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('POINT (-10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINT (-10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force2D1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('LINESTRING (-10 10 2, 10 10 3)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (-10 10, 10 10)", rs.getBytes(1));
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_Force2D2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('POINT (-10 10 2)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINT (-10 10)", rs.getBytes(1));
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_Force2D3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('POINT (-10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINT (-10 10)", rs.getBytes(1));
        rs.close();
    }   
    
    
    @Test
    public void test_ST_LineIntersector1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineIntersector('LINESTRING (0 0 0, 10 0 0)'::GEOMETRY, 'LINESTRING (5 5 0, 5 -5 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 0, 5 0), (5 0, 10 0 0))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_LineIntersector2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineIntersector('LINESTRING (0 0 0, 10 0 0)'::GEOMETRY, "
                + "'MULTILINESTRING ((5 5, 5 -5),(1.3 4.3, 1.3 -2.7, 3.1 -2.5, 3.1 2.5))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 0, 1.3 0), (1.3 0, 3.1 0), (3.1 0, 5 0), (5 0, 10 0 0))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_LineIntersector3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineIntersector('LINESTRING (0 0 0, 10 0 0)'::GEOMETRY, "
                + "'MULTIPOLYGON (((0.9 2.3, 4.2 2.3, 4.2 -1.8, 0.9 -1.8, 0.9 2.3)),((6 2, 8.5 2, 8.5 -1.6, 6 -1.6, 6 2)))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 0, 0.9 0), (0.9 0, 4.2 0), (4.2 0, 6 0), (6 0, 8.5 0), (8.5 0, 10 0 0)) ", rs.getBytes(1));
        rs.close();
    }  
    
    @Test(expected = IllegalArgumentException.class)
    public void test_ST_LineIntersector4()  throws Throwable {
        try {
            st.execute("SELECT ST_LineIntersector( 'MULTIPOLYGON (((0.9 2.3, 4.2 2.3, 4.2 -1.8, 0.9 -1.8, 0.9 2.3)),((6 2, 8.5 2, 8.5 -1.6, 6 -1.6, 6 2)))'::GEOMETRY,"
                + "'LINESTRING (0 0 0, 10 0 0)'::GEOMETRY);");       
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
    @Test
    public void test_ST_LineIntersector5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineIntersector('LINESTRING (452437 6838440, 452577 6837738, 452888 6837917)'::GEOMETRY, "
                + "'LINESTRING (452400 6837700, 452577 6837738)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((452437 6838440, 452577 6837738), (452577 6837738, 452888 6837917))", rs.getBytes(1));
        rs.close();
    } 
    
    @Test
    public void test_ST_LineIntersector6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineIntersector('LINESTRING ( 267 299, 293.9773755656108 139.93665158371041, 362 243 )'::GEOMETRY, "
                + "'LINESTRING ( 123 201, 333 126 )'::GEOMETRY);");
        rs.next();
        assertEquals("MULTILINESTRING ((267 299, 293.9773755656108 139.93665158371041), (293.9773755656108 139.93665158371041, 362 243))", rs.getString(1));
        rs.close();
    }
    
    
    @Test
    public void test_ST_OffSetCurve1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('POINT (10 10)'::GEOMETRY, 10);");
        rs.next();
        assertEquals("LINESTRING (20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803978, 17.071067811865476 2.9289321881345254, 15.555702330196024 1.6853038769745474, 13.826834323650898 0.7612046748871322, 11.950903220161283 0.1921471959676957, 10 0, 8.049096779838719 0.1921471959676957, 6.173165676349103 0.7612046748871322, 4.44429766980398 1.6853038769745474, 2.9289321881345254 2.9289321881345245, 1.6853038769745474 4.444297669803978, 0.7612046748871322 6.173165676349106, 0.1921471959676939 8.049096779838722, 0 10.000000000000007, 0.1921471959676975 11.950903220161292, 0.7612046748871375 13.826834323650909, 1.6853038769745545 15.555702330196034, 2.928932188134537 17.071067811865486, 4.444297669803992 18.314696123025463, 6.173165676349122 19.238795325112875, 8.04909677983874 19.807852804032308, 10.000000000000025 20, 11.950903220161308 19.8078528040323, 13.826834323650925 19.238795325112857, 15.555702330196048 18.314696123025435, 17.071067811865497 17.07106781186545, 18.31469612302547 15.555702330195993, 19.238795325112882 13.826834323650862, 19.80785280403231 11.950903220161244, 20 10)",
                rs.getString(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OffSetCurve2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('LINESTRING (0 10, 10 10)'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("LINESTRING (0 20, 10 20)",
                rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OffSetCurve3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('LINESTRING (0 10, 10 10)'::GEOMETRY, -10);");
        rs.next();
        assertGeometryEquals("LINESTRING (0 0, 10 0)",
                rs.getBytes(1));
        rs.close();
    }
    
    
    @Test
    public void test_ST_OffSetCurve4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('MULTILINESTRING ((0 10, 10 10),(10 20, 20 20))'::GEOMETRY, -10);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0, 10 0), (10 10, 20 10))",
                rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OffSetCurve5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('POLYGON ((5 10, 10 10, 10 5, 5 5, 5 10))'::GEOMETRY, 10);");
        rs.next();
        assertEquals("LINESTRING (-5 10, -4.807852804032304 11.950903220161287, -4.238795325112868 13.8268343236509, -3.3146961230254526 15.555702330196022, -2.0710678118654746 17.071067811865476, -0.55570233019602 18.314696123025453, 1.173165676349103 19.238795325112868, 3.049096779838718 19.807852804032304, 5 20, 10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 20 5, 19.807852804032304 3.0490967798387176, 19.238795325112868 1.173165676349102, 18.314696123025453 -0.5557023301960218, 17.071067811865476 -2.0710678118654746, 15.555702330196024 -3.3146961230254526, 13.826834323650898 -4.238795325112868, 11.950903220161283 -4.807852804032304, 10 -5, 5 -5, 3.049096779838713 -4.8078528040323025, 1.1731656763490967 -4.238795325112864, -0.5557023301960218 -3.3146961230254526, -2.0710678118654773 -2.0710678118654746, -3.3146961230254544 -0.55570233019602, -4.238795325112868 1.1731656763491034, -4.807852804032304 3.0490967798387163, -5 5, -5 10)",
                rs.getString(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OffSetCurve6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('POLYGON ((0 15, 15 15, 15 0, 0 0, 0 15), (5 10, 10 10, 10 5, 5 5, 5 10))'::GEOMETRY, 1);");
        rs.next();
        assertEquals("MULTILINESTRING ((-1 15, -0.9807852804032304 15.195090322016128, -0.9238795325112867 15.38268343236509, -0.8314696123025453 15.555570233019601, -0.7071067811865475 15.707106781186548, -0.555570233019602 15.831469612302545, -0.3826834323650897 15.923879532511286, -0.1950903220161282 15.98078528040323, 0 16, 15 16, 15.195090322016128 15.98078528040323, 15.38268343236509 15.923879532511286, 15.555570233019603 15.831469612302545, 15.707106781186548 15.707106781186548, 15.831469612302545 15.555570233019601, 15.923879532511286 15.38268343236509, 15.98078528040323 15.195090322016128, 16 15, 16 0, 15.98078528040323 -0.1950903220161282, 15.923879532511286 -0.3826834323650898, 15.831469612302545 -0.5555702330196022, 15.707106781186548 -0.7071067811865475, 15.555570233019603 -0.8314696123025452, 15.38268343236509 -0.9238795325112867, 15.195090322016128 -0.9807852804032304, 15 -1, 0 -1, -0.1950903220161287 -0.9807852804032303, -0.3826834323650903 -0.9238795325112865, -0.5555702330196022 -0.8314696123025452, -0.7071067811865477 -0.7071067811865475, -0.8314696123025455 -0.555570233019602, -0.9238795325112868 -0.3826834323650897, -0.9807852804032304 -0.1950903220161284, -1 0, -1 15), (6 9, 9 9, 9 6, 6 6, 6 9))",
                rs.getString(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OffSetCurve7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('POLYGON ((0 15, 15 15, 15 0, 0 0, 0 15), (5 10, 10 10, 10 5, 5 5, 5 10))'::GEOMETRY, 10);");
        rs.next();
        assertEquals("LINESTRING (-10 15, -9.807852804032304 16.950903220161287, -9.238795325112868 18.8268343236509, -8.314696123025453 20.55570233019602, -7.071067811865475 22.071067811865476, -5.55570233019602 23.314696123025453, -3.826834323650897 24.238795325112868, -1.950903220161282 24.807852804032304, 0 25, 15 25, 16.950903220161283 24.807852804032304, 18.8268343236509 24.238795325112868, 20.555702330196024 23.314696123025453, 22.071067811865476 22.071067811865476, 23.314696123025453 20.55570233019602, 24.238795325112868 18.8268343236509, 24.807852804032304 16.950903220161283, 25 15, 25 0, 24.807852804032304 -1.9509032201612824, 24.238795325112868 -3.826834323650898, 23.314696123025453 -5.555702330196022, 22.071067811865476 -7.071067811865475, 20.555702330196024 -8.314696123025453, 18.8268343236509 -9.238795325112868, 16.950903220161283 -9.807852804032304, 15 -10, 0 -10, -1.9509032201612866 -9.807852804032303, -3.8268343236509033 -9.238795325112864, -5.555702330196022 -8.314696123025453, -7.071067811865477 -7.071067811865475, -8.314696123025454 -5.55570233019602, -9.238795325112868 -3.8268343236508966, -9.807852804032304 -1.9509032201612837, -10 0, -10 15)",
                rs.getString(1));
        rs.close();
    }
    
     @Test
    public void test_ST_OffSetCurve8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OffSetCurve('GEOMETRYCOLLECTION (POLYGON ((0 15, 15 15, 15 0, 0 0, 0 15), (5 10, 10 10, 10 5, 5 5, 5 10)),LINESTRING (0 20, 10 20))'::GEOMETRY, 10);");
        rs.next();
        assertEquals("MULTILINESTRING ((-10 15, -9.807852804032304 16.950903220161287, -9.238795325112868 18.8268343236509, -8.314696123025453 20.55570233019602, -7.071067811865475 22.071067811865476, -5.55570233019602 23.314696123025453, -3.826834323650897 24.238795325112868, -1.950903220161282 24.807852804032304, 0 25, 15 25, 16.950903220161283 24.807852804032304, 18.8268343236509 24.238795325112868, 20.555702330196024 23.314696123025453, 22.071067811865476 22.071067811865476, 23.314696123025453 20.55570233019602, 24.238795325112868 18.8268343236509, 24.807852804032304 16.950903220161283, 25 15, 25 0, 24.807852804032304 -1.9509032201612824, 24.238795325112868 -3.826834323650898, 23.314696123025453 -5.555702330196022, 22.071067811865476 -7.071067811865475, 20.555702330196024 -8.314696123025453, 18.8268343236509 -9.238795325112868, 16.950903220161283 -9.807852804032304, 15 -10, 0 -10, -1.9509032201612866 -9.807852804032303, -3.8268343236509033 -9.238795325112864, -5.555702330196022 -8.314696123025453, -7.071067811865477 -7.071067811865475, -8.314696123025454 -5.55570233019602, -9.238795325112868 -3.8268343236508966, -9.807852804032304 -1.9509032201612837, -10 0, -10 15), (0 30, 10 30))",
                rs.getString(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ProjectPoint1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ProjectPoint('POINT(5 5)', 'LINESTRING (0 0, 10 0)');");
        rs.next();
        assertGeometryEquals("POINT(5 0)", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ProjectPoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ProjectPoint('POINT(0 5)', 'LINESTRING (0 0, 10 0)');");
        rs.next();
        assertGeometryEquals("POINT(0 0)", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ProjectPoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ProjectPoint('POINT(-20 5)', 'LINESTRING (0 0, 10 0)');");
        rs.next();
        assertGeometryEquals("POINT(0 0)", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_ProjectPoint4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ProjectPoint('LINESTRING(-20 5, 20 20)', 'LINESTRING (0 0, 10 0)');");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_CollectExtract1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectExtract('LINESTRING(-20 5, 20 20)', 2);");
        rs.next();
        assertGeometryEquals("LINESTRING(-20 5, 20 20)", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_CollectExtract2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectExtract('LINESTRING(-20 5, 20 20)', 1);");
        rs.next();
        assertEquals("GEOMETRYCOLLECTION EMPTY", ((Geometry) rs.getObject(1)).toText());
        rs.close();
    }
    
    @Test
    public void test_ST_CollectExtract3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectExtract('GEOMETRYCOLLECTION (POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320)),"
                + "  LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170))', 2);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((290 340, 270 230), (120 200, 230 170))", rs.getObject(1));
        rs.close();
    }
    
    @Test
    public void test_ST_CollectExtract4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectExtract('GEOMETRYCOLLECTION (POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320)),"
                + "  LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170))', 3);");
        rs.next();
        assertGeometryEquals("POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320))", rs.getObject(1));
        rs.close();
    }
    
    
    @Test
    public void test_ST_CollectExtract5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectExtract('GEOMETRYCOLLECTION (LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170),"
                + "  POINT (110 360),"
                + "  POINT (145 322),"
                + "  POINT (190 340),"
                + "  POINT (200 360))', 1);");
        rs.next();
        assertGeometryEquals("MULTIPOINT ((110 360), (145 322), (190 340), (200 360))", rs.getObject(1));
        rs.close();
    }
    
}
