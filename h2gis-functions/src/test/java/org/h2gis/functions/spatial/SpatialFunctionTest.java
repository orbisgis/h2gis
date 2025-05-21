/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial;


import org.h2.jdbc.JdbcSQLDataException;
import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.value.ValueGeometry;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.spatial.affine_transformations.ST_Translate;
import org.h2gis.utilities.TableLocation;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

import java.sql.*;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import org.h2gis.utilities.GeometryTableUtilities;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Fortin
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class SpatialFunctionTest {

    private static Connection connection;
    private Statement st;
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

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(SpatialFunctionTest.class.getSimpleName());
        FACTORY = new GeometryFactory();
        WKT_READER = new WKTReader();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }


    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception {
        st.execute("DROP TABLE forest IF EXISTS;" +
                "CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('forests') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeEmptyGeometryCollection() throws Exception {
        st.execute("DROP TABLE TEST IF EXISTS;  create table test(the_geom GEOMETRY, val Integer);"
                + "insert into test VALUES (ST_GeomFromText('MULTILINESTRING EMPTY'),108),"
                + " (ST_GeomFromText('MULTIPOINT EMPTY'),109),"
                + " (ST_GeomFromText('MULTIPOLYGON EMPTY'),110),"
                + " (ST_GeomFromText('GEOMETRYCOLLECTION EMPTY'),111);");
        ResultSet rs = st.executeQuery("SELECT the_geom::Geometry, val FROM ST_Explode('test') ORDER BY val");
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
    public void test_ST_ExplodeWithQuery1() throws Exception {
        st.execute("DROP TABLE forests IF EXISTS;  CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('(select * from forests limit 1)') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeWithQuery3() throws Exception {
        st.execute("DROP TABLE forests IF EXISTS; CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('(select * from forests limit 1)') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeWithQuery4() throws Exception {
        st.execute("DROP TABLE forests IF EXISTS;  CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON), \"LIMIT\" INTEGER);"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101), 666);");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('(select \"LIMIT\", boundary from forests limit 1)') WHERE explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeWithQuery2() throws Exception {
        st.execute("DROP TABLE forests IF EXISTS;  CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('(select * from forests)') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeWithBadQuery() throws Throwable {
        assertThrows(SQLException.class, ()-> {
            try {
                st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                        + " boundary MULTIPOLYGON);"
                        + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                        + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
                st.execute("SELECT ST_AsText(boundary) FROM ST_Explode('select ') WHERE name = 'Green Forest' and explod_id=2");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            } finally {
                st.execute("drop table forests");
            }
        });
    }

    @Test
    public void test_ST_ExplodeWithBadQuery2() throws Throwable {
        assertThrows(SQLException.class, ()-> {
            try {
                st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                        + " boundary MULTIPOLYGON);"
                        + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                        + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
                st.execute("SELECT ST_AsText(boundary) FROM ST_Explode('select *') WHERE name = 'Green Forest' and explod_id=2");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            } finally {
                st.execute("drop table forests");
            }
        });
    }

    public void test_ST_ExplodeFieldName() throws Exception {
        st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary MULTIPOLYGON);"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('forests', 'boundary') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeWithoutFieldName() throws Throwable {
        assertThrows(SQLException.class, ()-> {
            try {
                st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                        + " boundary MULTIPOLYGON);"
                        + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                        + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101));");
                st.execute("SELECT ST_AsText(boundary) FROM ST_Explode('forests', 'the_geom') WHERE name = 'Green Forest' and explod_id=2");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            } finally {
                st.execute("drop table forests");
            }
        });
    }

    @Test
    public void test_ST_ExplodeWithSRID() throws Exception {
        st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON, 4326));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 4326));");
        st.execute("Drop table if exists exploded_forests; CREATE TABLE exploded_forests as SELECT * FROM ST_Explode('forests')");
        ResultSet rs = st.executeQuery("SELECT * FROM exploded_forests");
        assertTrue(rs.next());
        assertEquals(4326, GeometryTableUtilities.getSRID(connection, TableLocation.parse("exploded_forests")));
        st.execute("drop table forests");
    }

    @Test
    public void test_ReservervedKeyWordTable() throws Exception {
        // Test with reserved keyword named table
        st.execute("DROP TABLE IF EXISTS \"NATURAL\"");
        st.execute("CREATE TABLE \"NATURAL\" (geom GEOMETRY)");
        st.execute("INSERT INTO \"NATURAL\" VALUES (ST_SETSRID('POINT(1 1)', 4326))");
        try(ResultSet rs = st.executeQuery("select srid from geometry_columns where f_table_name = 'NATURAL'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt(1));
        }
    }

    @Test
    public void test_ST_Extent() throws Exception {
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom GEOMETRY(MultiPoint));"
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
    public void test_ST_Extent2() throws Exception {
        ResultSet rs = st.executeQuery("select ST_Extent('SRID=4326;MULTIPOLYGON (((28 0, 28 42, 84 42, 84 0, 28 0)), ((59 59, 74 59, 74 37, 59 37, 59 59)), ((36 52, 49 52, 49 29, 36 29, 36 52)))'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;POLYGON ((28 0, 28 59, 84 59, 84 0, 28 0))", rs.getObject(1));
    }

    @Test
    public void test_NULL_ST_Extent() throws Exception {
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom GEOMETRY(MultiPoint));");
        ResultSet rs = st.executeQuery("select ST_Extent(the_geom) tableEnv from ptClouds;");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
        assertFalse(rs.next());
        st.execute("drop table ptClouds");
    }

    @Test
    public void test_Empty_ST_Extent() throws Exception {
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom GEOMETRY(MultiPoint));"
                + "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT EMPTY',2154)),"
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
    public void test_TableEnvelope() throws Exception {
        st.execute("drop table if exists ptClouds");
        st.execute("create table ptClouds(id INTEGER PRIMARY KEY AUTO_INCREMENT, the_geom GEOMETRY(MultiPoint));"
                + "insert into ptClouds(the_geom) VALUES (ST_MPointFromText('MULTIPOINT(5 5, 1 2, 3 4, 99 3)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)',2154)),"
                + "(ST_MPointFromText('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)',2154));");
        Envelope result = GeometryTableUtilities.getEnvelope(connection, TableLocation.parse("PTCLOUDS"), "THE_GEOM").getEnvelopeInternal();
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
                + "CREATE TABLE input_table(the_geom GEOMETRY(Polygon));"
                + "INSERT INTO input_table VALUES("
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)); "
                + "INSERT INTO input_table VALUES("
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 7, 0 5, 0 0))', 1));");
        ResultSet rs = st.executeQuery("SELECT ST_IsRectangle(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_IsValid() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(the_geom GEOMETRY(Polygon));"
                + "INSERT INTO input_table VALUES"
                + "(ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))', 1)), "
                + "(ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 6 -2, 0 0))', 1)),"
                + "(NULL);");
        ResultSet rs = st.executeQuery("SELECT ST_IsValid(the_geom) FROM input_table;");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_Covers2() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(smallc GEOMETRY(POLYGON), bigc GEOMETRY(POLYGON));" +
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
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.getBoolean(2));
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.getBoolean(4));
        assertFalse(rs.getBoolean(5));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_Covers() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(smallc GEOMETRY(Polygon), bigc GEOMETRY(Polygon));"
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
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.getBoolean(2));
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.getBoolean(4));
        assertFalse(rs.getBoolean(5));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_DWithin() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geomA GEOMETRY(Polygon), geomB GEOMETRY(Polygon));"
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
        assertTrue(rs.getBoolean(1));
        assertFalse(rs.getBoolean(2));
        assertFalse(rs.getBoolean(3));
        assertTrue(rs.getBoolean(4));
        assertFalse(rs.getBoolean(5));
        assertTrue(rs.getBoolean(6));
        assertTrue(rs.getBoolean(7));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_XYZMinMax() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(line GEOMETRY(Linestring Z));"
                + "INSERT INTO input_table VALUES("
                + "ST_LineFromText('LINESTRINGZ(1 2 3, 4 5 6)', 101));");
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
                + "CREATE TABLE input_table(geom Geometry(GEOMETRY, 4326));"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING(1 3, 1 1, 2 1)', 4326));");
        ResultSet rs = st.executeQuery("SELECT ST_Rotate(geom, pi()),"
                + "ST_Rotate(geom, pi() / 3), "
                + "ST_Rotate(geom, pi()/2, 1.0, 1.0), "
                + "ST_Rotate(geom, -pi()/2, ST_GeomFromText('POINT(2 1)')) "
                + "FROM input_table;");
        assertTrue(rs.next());
        LineString geom = (LineString) rs.getObject(1);
        assertEquals(4326, geom.getSRID());
        assertTrue(geom.equalsExact(
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
                + "CREATE TABLE input_table(twoDLine Geometry(GEOMETRY, 4326), threeDLine Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING(1 2, 4 5)', 4326),"
                + "ST_GeomFromText('LINESTRINGZ(1 2 3, 4 5 6)'));");
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_Scale(twoDLine, 0.5, 0.75), ST_Scale(threeDLine, 0.5, 0.75), "
                + "ST_Scale(threeDLine, 0.5, 0.75, 1.2), ST_Scale(threeDLine, 0.0, -1.0, 2.0) "
                + "FROM input_table;");
        assertTrue(rs.next());
        LineString geom = (LineString) rs.getObject(1);
        assertEquals(4326, geom.getSRID());
        assertTrue(geom.equalsExact(
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
                        new Coordinate(0.5, 1.5, 3.6),
                        new Coordinate(2, 3.75, 7.2)}),
                TOLERANCE));
        assertTrue(((LineString) rs.getObject(4)).equalsExact(
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
                + "(ST_GeomFromText('LINESTRINGZ(1 4 3, 15 7 9, 16 17 22)',2249)),"
                + "(ST_GeomFromText('MULTILINESTRINGZ((1 4 3, 15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249)),"
                + "(ST_GeomFromText('POLYGONZ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))',2249)),"
                + "(ST_GeomFromText('MULTIPOLYGONZ(((0 0 0, 3 2 0, 3 2 2, 0 0 2, 0 0 0),"
                + "(-1 1 0, -1 3 0, -1 3 4, -1 1 4, -1 1 0)))',2249)),"
                + "(ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRINGZ(1 4 3, 15 7 9, 16 17 22),"
                + "POLYGONZ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))',2249));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_3DLength(geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(Math.sqrt(205) + Math.sqrt(101), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(241) + Math.sqrt(270), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(241) + Math.sqrt(270) + 3 + Math.sqrt(2), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_3DLength2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DLength('MULTIPOLYGON (((898458.2 6245894.6, 898493.4 6245894.5, 898492.3 6245888.4, 898458.7 6245888.5, 898458.2 6245894.6)))')");
        rs.next();
        assertEquals(0, rs.getDouble(1),0);
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
                + "ST_GeomFromText('LINESTRING(0 0, 1 1)',1));"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('LINESTRING Z (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)',1));"
                + "INSERT INTO input_table VALUES("
                + "ST_GeomFromText('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))',1));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_CoordDim(geom)"
                        + " FROM input_table;");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
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
                + "(ST_GeomFromText('POLYGONZ((0 0 0, 3 0 0, 3 2 0, 0 2 1, 0 0 0))'));");
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
    public void test_ST_ToMultiPoint() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(empty_multi_point GEOMETRY(MultiPoint),"
                + "multi_point GEOMETRY(MultiPoint), point GEOMETRY(Point), "
                + "line GEOMETRY(LineString), "
                + "polygon GEOMETRY(Polygon), multi_polygon GEOMETRY(MultiPolygon));"
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
        assertGeometryEquals("SRID=2154;MULTIPOINT(28 26,28 0,84 0,84 42,28 26,"
                + "52 18,66 23,73 9,48 6,52 18,"
                + "59 18,67 18,67 13,59 13,59 18)", rs.getObject(6));

        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_ToMultiLine() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table("
                + "point GEOMETRY(Point),"
                + "empty_line_string GEOMETRY(LineString),"
                + "line GEOMETRY(LineString), "
                + "polygon GEOMETRY(Polygon), "
                + "polygon_with_holes GEOMETRY(Polygon), "
                + "multi_polygon GEOMETRY(MultiPolygon),"
                + "collection Geometry, geomsrid Geometry(Linestring, 2154));"
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
                + "ST_GeomFromText('GEOMETRYCOLLECTIONZ(LINESTRINGZ(1 4 3, 15 7 9, 16 17 22),"
                + "POLYGONZ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))'),"
                + "ST_GEOMFROMTEXT('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154));");
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_ToMultiLine(point), "
                + "ST_ToMultiLine(empty_line_string), "
                + "ST_ToMultiLine(line), "
                + "ST_ToMultiLine(polygon), "
                + "ST_ToMultiLine(polygon_with_holes), "
                + "ST_ToMultiLine(multi_polygon), "
                + "ST_ToMultiLine(collection),"
                + "ST_ToMultiLine(geomsrid)"
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
        assertGeometryEquals("SRID=2154;MULTILINESTRING((5 5, 1 2, 3 4, 99 3))", rs.getObject(8));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_Holes() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(empty_line_string GEOMETRY(LineString),"
                + "line GEOMETRY(LineString), "
                + "polygon GEOMETRY(Polygon), "
                + "polygon_with_holes GEOMETRY(Polygon), "
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
                + "ST_Holes(collection),"
                + "ST_Holes(st_setsrid(collection, 4326))"
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
        assertGeometryEquals("SRID=4326;GEOMETRYCOLLECTION(POLYGON((1 1, 2 1, 2 4, 1 4, 1 1)),"
                + "POLYGON((12 7, 14 7, 14 8, 12 8, 12 7)))", rs.getObject(6));
        assertFalse(rs.next());
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_ToMultiSegments() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table("
                + "point GEOMETRY(Point),"
                + "empty_line_string GEOMETRY(LineString),"
                + "line GEOMETRY(LineString), "
                + "multi_line GEOMETRY(MultiLineString z), "
                + "polygon GEOMETRY(Polygon), "
                + "polygon_with_holes GEOMETRY(Polygon), "
                + "collection Geometry);"
                + "INSERT INTO input_table VALUES("
                + "ST_PointFromText('POINT(5 5)', 2154),"
                + "ST_GeomFromText('LINESTRING EMPTY',2154),"
                + "ST_LineFromText('LINESTRING(5 5, 1 2, 3 4, 99 3)',2154),"
                + "ST_GeomFromText('MULTILINESTRINGZ((1 4 3, 15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0))',2154),"
                + "ST_PolyFromText('POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1),"
                + "(7 1, 8 1, 8 3, 7 3, 7 1))',2154),"
                + "ST_GeomFromText('GEOMETRYCOLLECTION(POLYGON ((0 0, 10 0, 10 5, 0 5, 0 0),"
                + "(1 1, 2 1, 2 4, 1 4, 1 1),"
                + "(7 1, 8 1, 8 3, 7 3, 7 1)),"
                + "POINT(2 3),"
                + "LINESTRING (8 7, 9 5, 11 3))', 2154));");
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
        assertTrue(WKT_READER.read("MULTILINESTRINGZ((1 4 3, 15 7 9), (15 7 9, 16 17 22),"
                        + "(0 0 0, 1 0 0), (1 0 0, 1 2 0), (1 2 0, 0 2 1))")
                .equalsExact((MultiLineString) rs.getObject(4), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0))")
                .equalsExact((MultiLineString) rs.getObject(5), TOLERANCE));
        assertTrue(WKT_READER.read("MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0),"
                        + "(1 1, 2 1), (2 1, 2 4), (2 4, 1 4), (1 4, 1 1),"
                        + "(7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1))")
                .equalsExact((MultiLineString) rs.getObject(6), TOLERANCE));
        assertGeometryEquals("SRID=2154;MULTILINESTRING((0 0, 10 0), (10 0, 10 5), (10 5, 0 5), (0 5, 0 0),"
                + "(1 1, 2 1), (2 1, 2 4), (2 4, 1 4), (1 4, 1 1),"
                + "(7 1, 8 1), (8 1, 8 3), (8 3, 7 3), (7 3, 7 1),"
                + "(8 7, 9 5), (9 5, 11 3))", rs.getObject(7));
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
                + "CREATE TABLE input_table(point GEOMETRY(Point));"
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
                + "CREATE TABLE input_table(point GEOMETRY(Point));"
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
                + "CREATE TABLE input_table(point GEOMETRY(Point));"
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
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(4 2.5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(4 0)")));
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(5 2.5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(5 0)")));
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(6 2.5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(6 0)")));
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.next());
        assertTrue(((Point) rs.getObject(1)).
                equalsTopo(WKT_READER.read("POINT(5 5)")));
        assertTrue(((Point) rs.getObject(2)).
                equalsTopo(WKT_READER.read("POINT(5 5)")));
        assertTrue(rs.getBoolean(3));
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
    public void test_ST_RemoveRepeatedPoints3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))'::GEOMETRY);");
        rs.next();
        Geometry geom = (Geometry) rs.getObject(1);
        assertTrue(geom.equals(WKT_READER.read("MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPoints4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('LINESTRING (1 1, 2 2, 0 2, 1 1 )'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (1 1, 2 2, 0 2)")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPoints5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('SRID=2154;LINESTRING (60 290, 67 300, 67 300, 140 330, 136 319,136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=2154;LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314,  116 307, 110 299, 103 289, 100 140, 110 142, 270 170)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPointsTolerance() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('LINESTRING (0 0, 2 0, 10 0, 100 0)'::GEOMETRY, 3);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (0 0,  10 0, 100 0)")));
        rs.close();
    }



    @Test
    public void test_ST_RemoveRepeatedPointsTolerance1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('POLYGON ((0 0, 2 0, 10 10, 100 10, 0 0))'::GEOMETRY, 3);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((0 0,  10 10, 100 10, 0 0))")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPointsTolerance2() throws Throwable {
        assertThrows(SQLException.class, ()-> {
            try {
                st.executeQuery("SELECT ST_RemoveRepeatedPoints('LINESTRING (0 0, 2 0)'::GEOMETRY, 3);");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_RemoveRepeatedPointsTolerance3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('POINT (0 0)'::GEOMETRY, 3);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT (0 0)")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPointsTolerance4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('LINESTRING (0 0, 2 0, 10 0, 100 0, 1 1)'::GEOMETRY, 3);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (0 0,  10 0, 100 0)")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPointsTolerance5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('MULTIPOLYGON (((373849.6 6743072.4, 373848.8 6743092.4, 373852.8 6743092.5, 373853.6 6743072.5, 373851 6743072.5, 373849.6 6743072.4)))'::GEOMETRY, 2);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON (((373849.6 6743072.4, 373848.8 6743092.4, 373852.8 6743092.5, 373853.6 6743072.5, 373851 6743072.5, 373849.6 6743072.4)))")));
        rs.close();
    }

    @Test
    public void test_ST_RemoveRepeatedPointsTolerance6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveRepeatedPoints('MULTIPOLYGON (((373849.6 6743072.4, 373848.8 6743092.4, 373852.8 6743092.5, 373853.6 6743072.5, 373851.95 6743072.5, 373851 6743072.5, 373849.6 6743072.4)))'::GEOMETRY, 2);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOLYGON (((373849.6 6743072.4, 373848.8 6743092.4, 373852.8 6743092.5, 373853.6 6743072.5, 373851 6743072.5, 373849.6 6743072.4)))")));
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
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('LINESTRING Z(0 0 0, 5 0 0, 10 0 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z(0 0 0, 5 0 5, 10 0 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('POINTZ(0 0 0)'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getObject(1) == null);
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('MULTILINESTRING Z((0 0 0, 5 0 0, 10 0 10),(0 0 0, 50 0 0, 100 0 100))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING Z((0 0 0, 5 0 5, 10 0 10),(0 0 0, 50 0 50, 100 0 100))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('SRID=4326;MULTILINESTRING Z((0 0 0, 5 0 0, 10 0 10),(0 0 0, 50 0 0, 100 0 100))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;MULTILINESTRING Z((0 0 0, 5 0 5, 10 0 10),(0 0 0, 50 0 50, 100 0 100))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InterpolateLine5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Interpolate3DLine('SRID=4326;LINESTRING Z(0 0 0, 5 0 0, 10 0 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;LINESTRING Z(0 0 0, 5 0 5, 10 0 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint1() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.executeQuery("SELECT ST_InsertPoint('POINT(0 0 0)'::GEOMETRY, 'POINT(1 1)'::GEOMETRY);");
        });
    }

    @Test
    public void test_ST_InsertPoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('MULTIPOINT((0 0))'::GEOMETRY, 'POINT(1 1)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOINT((0 0), (1 1))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(1.5 4 )'::GEOMETRY, 4);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 8, 1 8 , 1.5 8, 3 8, 8 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(1.5 4 )'::GEOMETRY);");
        rs.next();
        //The geometry is not modified
        assertGeometryEquals("LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('POLYGON ((118 134, 118 278, 266 278, 266 134, 118 134 ))'::GEOMETRY, 'POINT(196 278 )'::GEOMETRY, 4);");
        rs.next();
        //The geometry is not modified
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((118 134, 118 278,196 278, 266 278, 266 134, 118 134 ))")));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1), (2 2, 4 2, 4 4, 2 4, 2 2))'::GEOMETRY, "
                + "'POINT(3 3 )'::GEOMETRY, 2);");
        rs.next();
        //The geometry is not modified
        assertGeometryEquals("POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1), (2 2, 3 2, 4 2, 4 4, 2 4, 2 2))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                + "(2 2, 4 2, 4 4, 2 4, 2 2))'::geometry,'POINT(3 3)'::geometry);");
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                + "(2 2, 4 2, 4 4, 2 4, 2 2))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('SRID=4326;POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                + "(2 2, 4 2, 4 4, 2 4, 2 2))'::geometry,'SRID=4326;POINT(3 3)'::geometry);");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                + "(2 2, 4 2, 4 4, 2 4, 2 2))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_InsertPoint9() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_InsertPoint('SRID=4326;POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                    + "(2 2, 4 2, 4 4, 2 4, 2 2))'::geometry,'SRID=2154;POINT(3 3)'::geometry);");
        });
    }

    @Test
    public void test_ST_InsertPoint10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_InsertPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, ST_POINTN('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 1));");
        rs.next();
        assertNull(rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint1() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.executeQuery("SELECT ST_AddPoint('MULTIPOINT((0 0))'::GEOMETRY, 'POINT(1 1)'::GEOMETRY);");
        });
    }

    @Test
    public void test_ST_AddPoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(0 8)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8, 0 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(0 8)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING (0 8, 1 8, 3 8, 0 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint4() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINT(0 8)'::GEOMETRY, 8);");
        });
    }

    @Test
    public void test_ST_AddPoint5() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_AddPoint('SRID=4326;POLYGON((1 1, 1 5, 5 5, 5 1, 1 1), "
                    + "(2 2, 4 2, 4 4, 2 4, 2 2))'::geometry,'SRID=2154;POINT(3 3)'::geometry);");
        });
    }

    @Test
    public void test_ST_AddPoint6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINTZ(0 8 1)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING (0 8, 1 8, 3 8, 0 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRINGZ(0 8 0, 1 8 0 , 3 8 0, 8 8 0, 10 8 0, 20 8 0)'::GEOMETRY, 'POINTZ(0 8 1)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING Z (0 8 0, 1 8 0, 3 8 0, 0 8 1, 10 8 0, 20 8 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRINGZ(0 8 0, 1 8 0 , 3 8 0, 8 8 0, 10 8 0, 20 8 0)'::GEOMETRY, 'POINT(0 8)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING Z (0 8 0, 1 8 0, 3 8 0, 0 8 0, 10 8 0, 20 8 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRINGM(0 8 0, 1 8 0 , 3 8 0, 8 8 0, 10 8 0, 20 8 0)'::GEOMETRY, 'POINT(0 8)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING M (0 8 0, 1 8 0, 3 8 0, 0 8 0, 10 8 0, 20 8 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_AddPoint10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddPoint('LINESTRING(0 8, 1 8 , 3 8, 8 8, 10 8, 20 8)'::GEOMETRY, 'POINTM(0 8 1)'::GEOMETRY, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING (0 8, 1 8, 3 8, 0 8, 10 8, 20 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('POINT(1 1)'::GEOMETRY, ST_Buffer('POINT(1 1)'::GEOMETRY, 10));");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('MULTIPOINT ((5 5), (10 10))'::GEOMETRY, ST_Buffer('POINT(10 10)'::GEOMETRY, 0.01));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((5 5)))")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('MULTIPOINT ((5 5), (10 10), (100 1000))'::GEOMETRY, "
                + "ST_Buffer('POINT(10 10)'::GEOMETRY, 10));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("MULTIPOINT((100 1000)))")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('POLYGON ((150 250, 220 250, 220 170, 150 170, 150 250))'::GEOMETRY, "
                + "ST_Buffer('POINT (230 250)'::GEOMETRY, 12));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((150 250, 220 170, 150 170, 150 250))")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('LINESTRING (100 200, 153 255, 169 175, 200 240, 250 190, "
                + "264 236, 304 236, 320 240, 340 250, 345 265, 354 295)'::GEOMETRY, ST_Buffer('POINT (230 250)'::GEOMETRY, 100));");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (100 200, 340 250, 345 265, 354 295)")));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('LINESTRING (0 0, 10 0)'::GEOMETRY, "
                + "ST_Buffer('POINT (5 0)'::GEOMETRY, 10));");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('POINT(1 1)'::GEOMETRY, "
                + "ST_Buffer('POINT(100 100)'::GEOMETRY, 10));");
        rs.next();
        assertGeometryEquals("POINT(1 1)",rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('LINESTRING(0 3, 1 1, 3 3, 5 2, 5 4, 6 5, 7 6, 7 7, 6 8)'::GEOMETRY, "
                + "ST_Buffer('POINT (3 4)'::GEOMETRY, 3));");
        rs.next();
        assertGeometryEquals("LINESTRING(0 3, 1 1, 6 5, 7 6, 7 7, 6 8)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('POLYGON((1 1, 1 6, 5 6, 5 1, 1 1), \n" +
                " (3 4, 3 5, 4 5, 4 4, 3 4)," +
                " (2 3, 3 3, 3 2, 2 2, 2 3))'::GEOMETRY, "
                + "ST_Buffer('POINT (6 7)'::GEOMETRY, 4.5));");
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 1 6, 5 1, 1 1), (2 3, 3 3, 3 2, 2 2, 2 3))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint11() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemovePoints('SRID=2154;MULTIPOINT ((5 5), (10 10))'::GEOMETRY, ST_Buffer('SRID=2154;POINT(10 10)'::GEOMETRY, 0.01));");
        rs.next();
        assertGeometryEquals("SRID=2154;MULTIPOINT((5 5))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RemovePoint12() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_RemovePoints('SRID=27572;MULTIPOINT ((5 5), (10 10))'::GEOMETRY, ST_Buffer('SRID=2154;POINT(10 10)'::GEOMETRY, 0.01));");
        });
    }

    @Test
    public void test_ST_Translate1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Translate('SRID=4326;POINT(-71.01 42.37)'::GEOMETRY, 1, 0);");
        rs.next();
        assertGeometryEquals("SRID=4326;POINT(-70.01 42.37)", rs.getBytes(1));
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
        ResultSet rs = st.executeQuery("SELECT ST_Translate('POINTZ(0 0 0)'::GEOMETRY, 5, 12, 3);");
        rs.next();
        assertGeometryEquals("POINTZ(5 12 3)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Translate4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Translate('POINTZ(1 2 3)'::GEOMETRY, 10, 20, 30);");
        rs.next();
        assertGeometryEquals("POINTZ(11 22 33)", rs.getBytes(1));
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
                "ST_Translate('LINESTRINGZ(0 0 0, 1 0 0)', 1, 2), " +
                "ST_Translate('LINESTRINGZ(0 0 0, 1 0 0)', 1, 2, 3);");
        rs.next();
        assertGeometryEquals("LINESTRING(1 2, 2 2)", rs.getBytes(1));
        assertGeometryEquals("LINESTRING(1 2, 2 2)", rs.getBytes(2));
        assertGeometryEquals("LINESTRINGZ(1 2 0, 2 2 0)", rs.getBytes(3));
        assertGeometryEquals("LINESTRINGZ(1 2 3, 2 2 3)", rs.getBytes(4));
        rs.close();
    }

    @Test
    public void test_ST_TranslateMixedDimensionXY() throws Throwable {
        assertThrows(JdbcSQLDataException.class, ()-> {
            try {
                st.executeQuery("SELECT " +
                        "ST_Translate('LINESTRING(0 0, 1 0 0)', 1, 2);");
            } catch (JdbcSQLException e) {
                final Throwable originalCause = e.getCause();
                assertEquals(ST_Translate.MIXED_DIM_ERROR,
                        originalCause.getMessage());
                throw originalCause;
            }
        });
    }

    @Test
    public void test_ST_TranslateMixedDimensionXYZ() throws Throwable {
        assertThrows(JdbcSQLDataException.class, ()-> {
            try {
                st.executeQuery("SELECT " +
                        "ST_Translate('LINESTRING(0 0, 1 0 0)', 1, 2, 3);");
            } catch (JdbcSQLException e) {
                final Throwable originalCause = e.getCause();
                assertEquals(ST_Translate.MIXED_DIM_ERROR,
                        originalCause.getMessage());
                throw originalCause;
            }
        });
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
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRINGZ (105 353 10, 150 180 0, 300 280 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z(300 280 0, 150 180 0,105 353 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRING (300 280, 150 180,105 353 )'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (105 353, 150 180, 300 280)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRING (105 353, 150 180, 300 280)'::GEOMETRY, 'desc');");
        rs.next();
        assertGeometryEquals("LINESTRING (105 353, 150 180, 300 280)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Reverse3DLine4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('LINESTRINGZ (105 353 0, 150 180 0, 300 280 10)'::GEOMETRY, 'desc');");
        rs.next();
        assertGeometryEquals("LINESTRING Z (300 280 10, 150 180 0,105 353 0 )", rs.getBytes(1));
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
    public void test_ST_Reverse3DLine6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Reverse3DLine('SRID=2154;LINESTRINGZ (105 353 10, 150 180 0, 300 280 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=2154;LINESTRING Z(300 280 0, 150 180 0,105 353 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveHoles1() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom GEOMETRY(POLYGON));"
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
                + "CREATE TABLE input_table(the_geom GEOMETRY(POLYGON));"
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
    public void test_ST_RemoveHoles3() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom GEOMETRY(POLYGON, 2154));"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))', 2154));");
        ResultSet rs = st.executeQuery("SELECT ST_RemoveHoles(the_geom) FROM input_table;");
        rs.next();
        assertGeometryEquals("SRID=2154;POLYGON ((190 300, 140 180, 300 110, 313 117, 430 270, 380 430, 190 300))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table;");
    }


    @Test
    public void test_ST_UpdateZ1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('POINTZ (190 300 0)'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("POINT Z (190 300 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINTZ( (190 300 0), (10 11 2))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 10), (10 11 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINT Z( (190 300 0), (10 11 0))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 10), (10 11 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINTZM( (190 300 0 12), (10 11 0 15))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT ZM ((10 11 10 15), (190 300 10 12))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_UpdateZ('MULTIPOINTM( (190 300 0), (10 11 0))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT ZM ((10 11 10 0), (190 300 10 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_UpdateZ6() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_UPDATEZ(ST_buffer('POINT(0 0)'::GEOMETRY, 10), 120);");
        assertTrue(rs.next());
        assertEquals(120, ((Geometry)rs.getObject(1)).getCoordinates()[0].z);
    }

    @Test
    public void test_ST_FORCE3D1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_FORCE3D('POINTZ (190 300 0)'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("POINT Z (190 300 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_FORCE3D2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_FORCE3D('MULTIPOINTZ( (190 300 0), (10 11 2))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 0), (10 11 2))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_FORCE3D3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_FORCE3D('MULTIPOINT Z( (190 300 0), (10 11 0))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 0), (10 11 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_FORCE3D6() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_FORCE3D(ST_buffer('POINT(0 0)'::GEOMETRY, 10), 120);");
        assertTrue(rs.next());
        assertEquals(120, ((Geometry)rs.getObject(1)).getCoordinates()[0].z);
    }

    @Test
    public void test_ST_AddZ1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_AddZ('MULTIPOINT Z( (190 300 1), (10 11 0))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 11), (10 11 10))", rs.getBytes(1));
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
        ResultSet rs = st.executeQuery("SELECT ST_AddZ('MULTIPOINT Z( (190 300 10), (10 11 5))'::GEOMETRY, -10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 0), (10 11 -5))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MultiplyZ1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ('MULTIPOINT Z( (190 300 1), (10 11 0))'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("MULTIPOINT Z( (190 300 10), (10 11 0))", rs.getBytes(1));
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
        ResultSet rs = st.executeQuery("SELECT ST_MultiplyZ('MULTIPOINTZ( (190 300 100), (10 11 50))'::GEOMETRY, 0.1);");
        rs.next();
        assertGeometryEquals("MULTIPOINTZ( (190 300 10), (10 11 5))", rs.getBytes(1));
        rs.close();
    }


    @Test
    public void test_ST_ZUpdateExtremities1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('LINESTRINGZ (250 250 0, 280 290 0)'::GEOMETRY, 40, 10);");
        rs.next();
        assertGeometryEquals("LINESTRINGZ (250 250 40, 280 290 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('LINESTRINGZ(0 0 0, 5 0 0, 10 0 0)'::GEOMETRY, 0, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRINGZ(0 0 0, 5 0 5, 10 0 10)")));
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
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('MULTILINESTRINGZ((0 0 12, 5 0 200 , 10 0 20),(0 0 1, 5 0 0, 10 0 2))'::GEOMETRY, 0, 10);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING Z((0 0 0, 5 0 5, 10 0 10),(0 0 0, 5 0 5, 10 0 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('MULTILINESTRINGZ((0 0 12, 5 0 200 , 10 0 20),(0 0 1, 5 0 0, 10 0 2))'::GEOMETRY, 0, 10, true);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING Z((0 0 0, 5 0 5, 10 0 10),(0 0 0, 5 0 5, 10 0 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ZUpdateExtremities6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ZUpdateLineExtremities('MULTILINESTRINGZ((0 0 12, 5 0 200 , 10 0 20),(0 0 1, 5 0 0, 10 0 2))'::GEOMETRY, 0, 10, false);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING Z((0 0 0, 5 0 200, 10 0 10),(0 0 0, 5 0 0, 10 0 10))", rs.getBytes(1));
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

    @Test
    public void test_ST_IsValidReason6() throws Throwable {
        assertThrows(JdbcSQLNonTransientException.class, ()-> {
            try {
                st.execute("SELECT ST_IsvalidReason('LINESTRING (80 240, 330 330, 280 240, 190 360)'::GEOMETRY, 199);");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_IsValidRDetail1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON ((280 330, 120 200, 360 190, 352 197, 170 290, 280 330))'::GEOMETRY);");
        rs.next();
        Object[] results = (Object []) rs.getArray(1).getArray();
        assertNotNull(results);
        assertFalse(Boolean.valueOf(results[0].toString()));
        assertEquals( "Self-intersection", results[1]);
        assertGeometryEquals("POINT(207.3066943435392 270.9366891541256)", ValueGeometry.get(results[2].toString()).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_IsValidRDetail2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON ((210 440, 134 235, 145 233, 310 200, 340 360, 210 440))'::GEOMETRY);");
        rs.next();
        Object[] results = (Object []) rs.getArray(1).getArray();
        assertNotNull(results);
        assertTrue(Boolean.valueOf(results[0].toString()));
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
        Object[] results = (Object []) rs.getArray(1).getArray();
        assertNotNull(results);
        assertTrue(Boolean.valueOf(results[0].toString()));
        assertEquals("Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }

    @Test
    public void test_ST_IsValidRDetail5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON ((100 340, 300 340, 300 180, 100 180, 100 340),"
                + "  (120 320, 160 320, 160 290, 120 290, 120 320))'::GEOMETRY, 0);");
        rs.next();
        Object[] results = (Object []) rs.getArray(1).getArray();
        assertNotNull(results);
        assertTrue(Boolean.valueOf(results[0].toString()));
        assertEquals("Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }

    @Test
    public void test_ST_IsValidRDetail6() throws Throwable {
        assertThrows(JdbcSQLNonTransientException.class, ()-> {
            try {
                st.execute("SELECT ST_IsvalidDetail('LINESTRING (80 240, 330 330, 280 240, 190 360)'::GEOMETRY, 199);");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }


    @Test
    public void test_ST_IsValidRDetail8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2,3 0))'::geometry,0);");
        rs.next();
        Object[] results = (Object []) rs.getArray(1).getArray();
        assertNotNull(results);
        assertFalse(Boolean.valueOf(results[0].toString()));
        assertEquals("Ring Self-intersection", results[1]);
        assertGeometryEquals("POINT(3 0)", ValueGeometry.get(results[2].toString()).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_IsValidRDetail9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_IsvalidDetail('POLYGON((3 0, 0 3, 6 3, 3 0, 4 2, 2 2,3 0))'::geometry,1)");
        rs.next();
        Object[] results = (Object []) rs.getArray(1).getArray();
        assertNotNull(results);
        assertTrue(Boolean.valueOf(results[0].toString()));
        assertEquals("Valid Geometry", results[1]);
        assertNull(results[2]);
        rs.close();
    }

    @Test
    public void test_ST_Force4D1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('LINESTRING (-10 10, 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING ZM(-10 10 0 0, 10 10 0 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force4D2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('LINESTRINGZ (-10 10 10 , 10 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING ZM(-10 10 10 0, 10 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force4D3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('LINESTRINGM (-10 10 10 , 10 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRINGZM(-10 10 0 10, 10 10 0 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force4D4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('POINTZ (-10 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINTZM(-10 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force4D5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('POINTZ (-10 10 10)'::GEOMETRY, 12, 12);");
        rs.next();
        assertGeometryEquals("POINTZM(-10 10 10 12)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force4D6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('POINTM (-10 10 10)'::GEOMETRY, 120, 12);");
        rs.next();
        assertGeometryEquals("POINTZM(-10 10 120 10)", rs.getBytes(1));
        rs.close();
    }
    @Test
    public void test_ST_Force4D7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force4D('POINT (-10 10)'::GEOMETRY, 12, 12);");
        rs.next();
        assertGeometryEquals("POINTZM(-10 10 12 12)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3DM1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3DM('LINESTRING (-10 10, 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING M(-10 10 0, 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_T_Force3DM2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3DM('LINESTRINGZ (-10 10 10 , 10 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING M(-10 10 0, 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_T_Force3DM3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT  ST_Force3DM('LINESTRINGM (-10 10 10 , 10 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRINGM(-10 10 10, 10 10 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3DM4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3DM('POINTZM (-10 10 12 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINT M(-10 10 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3DM5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3DM('LINESTRING (-10 10, 10 10)'::GEOMETRY, 15);");
        rs.next();
        assertGeometryEquals("LINESTRING M(-10 10 15, 10 10 15)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3DM6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3DM('POINTZ (-10 10 12)'::GEOMETRY,10);");
        rs.next();
        assertGeometryEquals("POINT M(-10 10 10)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3DM7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3DM('POINTZM (-10 10 12 10)'::GEOMETRY, 15);");
        rs.next();
        assertGeometryEquals("POINT M(-10 10 10)", rs.getBytes(1));
        rs.close();
    }


    @Test
    public void test_ST_Force3D1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRING (-10 10, 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z(-10 10 0, 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRING (-10 10, 10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z (-10 10 0, 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('POINT (-10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINTZ (-10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRINGM (-10 10 5, 10 10 5)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z (-10 10 0, 10 10 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRINGZ (-10 10 5, 10 10 5)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z (-10 10 5, 10 10 5)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force3D6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force3D('LINESTRINGZM (-10 10 5 2, 10 10 5 2)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z (-10 10 5, 10 10 5)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force2D1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('LINESTRINGZ (-10 10 2, 10 10 3)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (-10 10, 10 10)", rs.getBytes(1));
        rs.close();
        st.close();
    }

    @Test
    public void test_ST_Force2D2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('POINTZ (-10 10 2)'::GEOMETRY);");
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
    public void test_ST_Force2D4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('MULTILINESTRING((268394.46568222373 6745339.923834022, 268396.99851297046 6745339.229782337))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING((268394.46568222373 6745339.923834022, 268396.99851297046 6745339.229782337))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force2D5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('MULTILINESTRING ((268790.26367179473 6745239.766363457, 269133.30707034504 6745363.971731897, 269180.1863793153 6745381.28101521), (269347.25949076854 6745377.418053095, 269411.29051399784 6745352.142649189, 269442.8347345542 6745314.683887278, 269572.9546443491 6745235.823335887))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((268790.26367179473 6745239.766363457, 269133.30707034504 6745363.971731897, 269180.1863793153 6745381.28101521), (269347.25949076854 6745377.418053095, 269411.29051399784 6745352.142649189, 269442.8347345542 6745314.683887278, 269572.9546443491 6745235.823335887))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Force2D6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Force2D('SRID=4326;POINT (-10 10)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;POINT (-10 10)", rs.getBytes(1));
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
    public void test_ST_CollectionExtract1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('LINESTRING(-20 5, 20 20)', 2);");
        rs.next();
        assertGeometryEquals("LINESTRING(-20 5, 20 20)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('LINESTRING(-20 5, 20 20)', 1);");
        rs.next();
        assertEquals("GEOMETRYCOLLECTION EMPTY", ((Geometry) rs.getObject(1)).toText());
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('GEOMETRYCOLLECTION (POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320)),"
                + "  LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170))', 2);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((290 340, 270 230), (120 200, 230 170))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('GEOMETRYCOLLECTION (POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320)),"
                + "  LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170))', 3);");
        rs.next();
        assertGeometryEquals("POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320))", rs.getObject(1));
        rs.close();
    }


    @Test
    public void test_ST_CollectionExtract5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('GEOMETRYCOLLECTION (LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170),"
                + "  POINT (110 360),"
                + "  POINT (145 322),"
                + "  POINT (190 340),"
                + "  POINT (200 360))', 1);");
        rs.next();
        assertGeometryEquals("MULTIPOINT ((110 360), (145 322), (190 340), (200 360))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('GEOMETRYCOLLECTION (MULTILINESTRING ((181729.16666666666 2402624, 181715 2402658, 181648 2402753.714285714), (181648 2402796, 181655 2402803, 181699.625 2402824)))', 2);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((181729.16666666666 2402624, 181715 2402658, 181648 2402753.714285714), (181648 2402796, 181655 2402803, 181699.625 2402824))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('SRID=4326;GEOMETRYCOLLECTION (POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320)),"
                + "  LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170))', 3);");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('GEOMETRYCOLLECTION (LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170),"
                + "  POINT (110 360),"
                + "  POINT (145 322),"
                + "  POINT (190 340),"
                + "  POINT (200 360))', 1, 1);");
        rs.next();
        assertGeometryEquals("MULTIPOINT ((110 360), (145 322), (190 340), (200 360)) ", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CollectionExtract9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_CollectionExtract('GEOMETRYCOLLECTION (POLYGON ((140 320, 140 308, 140 287, 140 273, 140 252, 146 243, 156 241, 166 241, 176 241, 186 241, 196 241, 204 247, 212 254, 222 263, 228 271, 230 281, 214 295, 140 320)),"
                + "  LINESTRING (290 340, 270 230),"
                + "  LINESTRING (120 200, 230 170)," +
                "POINT(0 0))', 1, 3);");
        rs.next();
        assertGeometryEquals("GEOMETRYCOLLECTION (POINT (0 0), POLYGON ((140 252, 140 273, 140 287, 140 308, 140 320, 214 295, 230 281, 228 271, 222 263, 212 254, 204 247, 196 241, 186 241, 176 241, 166 241, 156 241, 146 243, 140 252)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_BoundingCircleCenter() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircleCenter(ST_EXPAND('SRID=4326;POINT(0 0)',5,5)) the_geom");
        rs.next();
        assertGeometryEquals("SRID=4326;POINT(0 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void testEmpty_ST_BoundingCircleCenter() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircleCenter('MULTIPOLYGON EMPTY') the_geom");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void testNull_ST_Buffer() throws Exception{
        ResultSet rs = st.executeQuery("SELECT ST_Buffer(null, 0.005, 'join=mitre')");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }


    @Test
    public void test_ST_PointOnSurfaceSRID() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_PointOnSurface('SRID=4326;POINT(0 0)'::GEOMETRY) the_geom");
        rs.next();
        assertGeometryEquals("SRID=4326;POINT(0 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionSRID1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Union('SRID=4326;MULTIPOLYGON (((230 350, 460 350, 460 200, 230 200, 230 350)),((460 350, 810 350, 810 200, 460 200, 460 350)))'::GEOMETRY) the_geom");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((230 200, 230 350, 460 350, 810 350, 810 200, 460 200, 230 200))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionSRID2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Union('SRID=4326;POLYGON ((230 350, 460 350, 460 200, 230 200, 230 350))'::geometry,'SRID=4326;POLYGON((460 350, 810 350, 810 200, 460 200, 460 350))'::GEOMETRY) the_geom");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((230 200, 230 350, 460 350, 810 350, 810 200, 460 200, 230 200))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionSRID3() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_Union('SRID=4327;POLYGON ((230 350, 460 350, 460 200, 230 200, 230 350))'::geometry,'SRID=4326;POLYGON((460 350, 810 350, 810 200, 460 200, 460 350))'::GEOMETRY) the_geom");
        });
    }

    @Test
    public void test_ST_UnionSRID4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Union('SRID=4326;POLYGON ((230 350, 460 350, 460 200, 230 200, 230 350))'::geometry,'LINESTRING EMPTY'::GEOMETRY) the_geom");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((230 200, 230 350, 460 350, 460 200, 230 200))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionSRID5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Union('MULTIPOINT ((1 0), EMPTY, EMPTY, (2 2))'::GEOMETRY) the_geom");
        rs.next();
        assertGeometryEquals("MULTIPOINT ((1 0), (2 2))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionSRID6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Union(st_accum(the_geom)::GEOMETRY) the_geom from" +
                "(select 'SRID=4326; POINT(0 0)'::GEOMETRY as the_geom union select 'POINT EMPTY'::GEOMETRY as the_geom) as foo ");
        rs.next();
        assertGeometryEquals("SRID=4326;POINT (0 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionSRID7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Union(NULL)");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_EstimatedExtent1() throws Exception {
        st.execute("DROP TABLE  forests IF EXISTS;" +
                "CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " boundary GEOMETRY(MULTIPOLYGON, 4326));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 4326));");
        st.execute("Drop table if exists exploded_forests; CREATE TABLE exploded_forests as SELECT * FROM ST_Explode('forests')");
        ResultSet rs = st.executeQuery("SELECT  ST_EstimatedExtent('exploded_forests')");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;POLYGON ((28 0, 28 42, 84 42, 84 0, 28 0))", rs.getObject(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_EstimatedExtent2() throws Exception {
        st.execute("DROP TABLE forests IF EXISTS;" +
                "CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64),"
                + " the_geom GEOMETRY(MULTIPOLYGON, 4326));"
                + "INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 4326));");
        ResultSet rs = st.executeQuery("SELECT  ST_EstimatedExtent('forests', 'THE_GEOM')");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;POLYGON ((28 0, 28 42, 84 42, 84 0, 28 0))", rs.getObject(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_IntersectsEmptyGeometry() throws Exception {
        ResultSet rs = st.executeQuery("Select st_intersects('POINT EMPTY'::GEOMETRY, 'POINT EMPTY'::GEOMETRY) as op ");
        rs.next();
        assertFalse(rs.getBoolean(1));
        rs.close();
    }

    @Test
    public void test_ST_IntersectsPrecision() throws Exception {
        ResultSet rs = st.executeQuery("Select st_intersects('POLYGON ((414188.5999999999 6422867.1, 414193.7 6422866.5, 414205.1 6422859.4, 414223.7 6422846.8, 414229.6 6422843.2, 414235.2 6422835.4, 414224.7 6422837.9, 414219.4 6422842.1, 414210.9 6422849, 414199.2 6422857.6, 414191.1 6422863.4, 414188.5999999999 6422867.1))'::GEOMETRY, 'LINESTRING (414187.2 6422831.6, 414179 6422836.1, 414182.2 6422841.8, 414176.7 6422844, 414184.5 6422859.5, 414188.6 6422867.1)'::GEOMETRY) as op ");
        rs.next();
        assertTrue(rs.getBoolean(1));
        rs.close();
    }

    @Test
    public void test_ST_SRIDEmptyGeometry() throws Exception {
        ResultSet rs = st.executeQuery("Select st_srid('POINT EMPTY'::GEOMETRY) as the_geom ");
        rs.next();
        assertEquals(0,  rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_CoveredBy() throws Exception {
        //From PostGIS test
        ResultSet rs = st.executeQuery("SELECT ST_CoveredBy(smallc,smallc) As smallinsmall,\n" +
                "  ST_CoveredBy(smallc, bigc) As smallcoveredbybig,\n" +
                "  ST_CoveredBy(ST_ExteriorRing(bigc), bigc) As exteriorcoveredbybig,\n" +
                "  ST_Within(ST_ExteriorRing(bigc),bigc) As exeriorwithinbig\n" +
                "FROM (SELECT ST_Buffer(ST_GeomFromText('POINT(1 2)'), 10) As smallc,\n" +
                "  ST_Buffer(ST_GeomFromText('POINT(1 2)'), 20) As bigc) As foo;");
        rs.next();
        assertTrue(rs.getBoolean(1));
        assertTrue(rs.getBoolean(2));
        assertTrue(rs.getBoolean(3));
        assertFalse(rs.getBoolean(4));
        rs.close();
    }
}
