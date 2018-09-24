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

package org.h2gis.functions.spatial.processing;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.h2.jdbc.JdbcSQLException;
import org.h2.value.ValueGeometry;
import org.h2gis.functions.spatial.properties.ST_CoordDim;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

/**
 * @author Nicolas Fortin
 */
public class ProcessingFunctionTest {
    private static Connection connection;
    private Statement st;
    private static WKTReader WKT_READER;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(ProcessingFunctionTest.class.getSimpleName());
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
    public void testLineMerger() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('MULTILINESTRING((0 0, 10 15), (56 50, 10 15))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING((0 0, 10 15, 56 50))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('MULTILINESTRING((10 20, 10 10), (20 20,10 10)," +
                " (30 10,20 10), (30 20,20 20), (30 30,30 20), (20 10,20 20))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((10 20, 10 10, 20 20), (30 30, 30 20, 20 20), (30 10, 20 10, 20 20)) ",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('MULTILINESTRING((20 10,20 20))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING((20 10,20 20))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('LINESTRING(20 10,20 20)');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((20 10, 20 20))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('POINT(20 20)');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING EMPTY", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testLineMerger6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineMerge('POLYGON((20 20, 40 20, 40 40, 20 40, 20 20))');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING EMPTY", rs.getBytes(1));
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
        assertEquals("LINESTRING (-10 15, -9.807852804032304 16.950903220161287, -9.238795325112868 18.8268343236509, -8.314696123025453 20.55570233019602, -7.071067811865475 22.071067811865476, -5.55570233019602 23.314696123025453, -3.826834323650897 24.238795325112868, -1.950903220161282 24.807852804032304, 0 25, 15 25, 16.950903220161283 24.807852804032304, 18.8268343236509 24.238795325112868, 20.555702330196024 23.314696123025453, 22.071067811865476 22.071067811865476, 23.314696123025453 20.55570233019602, 24.238795325112868 18.8268343236509, 24.807852804032304 16.950903220161283, 25 15, 25 0, 24.807852804032304 -1.9509032201612824, 24.238795325112868 -3.826834323650898, 23.314696123025453 -5.555702330196022, 22.071067811865476 -7.071067811865475, 20.555702330196024 -8.314696123025453, 18.8268343236509 -9.238795325112868, 16.950903220161283 -9.807852804032304, 15 -10, 0 -10, -1.9509032201612866 -9.807852804032303, -3.8268343236509033 -9.238795325112864, -5.555702330196022 -8.314696123025453, -7.071067811865477 -7.071067811865475, -8.314696123025454 -5.55570233019602, -9.238795325112868 -3.8268343236508966, -9.807852804032304 -1.9509032201612837, -10 0, -10 15)", rs.getString(1));
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
    public void test_ST_RingSideBuffer1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingSideBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 10, -10 10, -10 20, 10 20, 10 10)),"
                + "  ((-10 20, -10 30, 10 30, 10 20, -10 20)),"
                + "  ((-10 30, -10 40, 10 40, 10 30, -10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingSideBuffer2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingSideBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, -10, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((-10 10, 10 10, 10 0, -10 0, -10 10)), ((10 0, 10 -10, -10 -10, -10 0, 10 0)), ((10 -10, 10 -20, -10 -20, -10 -10, 10 -10)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingSideBuffer3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingSideBuffer('POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))'::GEOMETRY, -1, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 19, 10 20, 11 20, 20 20, 20 10, 10 10, 10 19),"
                + "  (11 19, 11 11, 19 11, 19 19, 11 19)),"
                + "  ((11 19, 19 19, 19 11, 11 11, 11 19),"
                + "    (12 18, 12 12, 18 12, 18 18, 12 18)),"
                + "  ((12 18, 18 18, 18 12, 12 12, 12 18),"
                + "    (13 17, 13 13, 17 13, 17 17, 13 17)))", rs.getObject(1));
        rs.close();
    }

    @Test(expected = SQLException.class)
    public void test_ST_RingSideBuffer4()  throws Throwable {
        try {
            st.execute("SELECT ST_RingSideBuffer('MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20)),"
                    + "  ((0 29, 10 29, 10 20, 0 20, 0 29)))'::GEOMETRY, -1, 3);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = SQLException.class)
    public void test_ST_RingSideBuffer5()  throws Throwable {
        try {
            st.execute("SELECT ST_RingSideBuffer('POINT(10 20)'::GEOMETRY, -1, 3);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test
    public void test_ST_RingSideBuffer6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingSideBuffer('POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20),"
                + "  (12.1 18.1, 14.1 18.1, 14.1 16, 12.1 16, 12.1 18.1),"
                + "  (15.6 14, 18 14, 18 11.5, 15.6 11.5, 15.6 14))'::GEOMETRY, 1, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((9 20, 9.01921471959677 20.195090322016128, 9.076120467488714 20.38268343236509, 9.168530387697455 20.5555702330196, 9.292893218813452 20.707106781186546, 9.444429766980399 20.831469612302545, 9.61731656763491 20.923879532511286, 9.804909677983872 20.980785280403232, 10 21, 20 21, 20.195090322016128 20.980785280403232, 20.38268343236509 20.923879532511286, 20.5555702330196 20.831469612302545, 20.707106781186546 20.707106781186546, 20.831469612302545 20.5555702330196, 20.923879532511286 20.38268343236509, 20.980785280403232 20.195090322016128, 21 20, 21 10, 20.980785280403232 9.804909677983872, 20.923879532511286 9.61731656763491, 20.831469612302545 9.444429766980399, 20.707106781186546 9.292893218813452, 20.5555702330196 9.168530387697455, 20.38268343236509 9.076120467488714, 20.195090322016128 9.01921471959677, 20 9, 10 9, 9.804909677983872 9.01921471959677, 9.61731656763491 9.076120467488714, 9.444429766980399 9.168530387697455, 9.292893218813452 9.292893218813452, 9.168530387697455 9.444429766980399, 9.076120467488714 9.61731656763491, 9.01921471959677 9.804909677983872, 9 10, 9 20), (10 20, 10 10, 20 10, 20 20, 10 20)), ((8 20, 8.03842943919354 20.390180644032256, 8.152240934977426 20.76536686473018, 8.33706077539491 21.111140466039203, 8.585786437626904 21.414213562373096, 8.888859533960796 21.66293922460509, 9.23463313526982 21.847759065022572, 9.609819355967744 21.96157056080646, 10 22, 20 22, 20.390180644032256 21.96157056080646, 20.76536686473018 21.847759065022572, 21.111140466039206 21.66293922460509, 21.414213562373096 21.414213562373096, 21.66293922460509 21.111140466039203, 21.847759065022572 20.76536686473018, 21.96157056080646 20.390180644032256, 22 20, 22 10, 21.96157056080646 9.609819355967744, 21.847759065022572 9.23463313526982, 21.66293922460509 8.888859533960796, 21.414213562373096 8.585786437626904, 21.111140466039206 8.33706077539491, 20.76536686473018 8.152240934977426, 20.390180644032256 8.03842943919354, 20 8, 10 8, 9.609819355967742 8.03842943919354, 9.234633135269819 8.152240934977428, 8.888859533960796 8.33706077539491, 8.585786437626904 8.585786437626904, 8.337060775394908 8.888859533960796, 8.152240934977426 9.23463313526982, 8.03842943919354 9.609819355967744, 8 10, 8 20), (9 20, 9 10, 9.01921471959677 9.804909677983872, 9.076120467488714 9.61731656763491, 9.168530387697455 9.444429766980399, 9.292893218813452 9.292893218813452, 9.444429766980399 9.168530387697455, 9.61731656763491 9.076120467488714, 9.804909677983872 9.01921471959677, 10 9, 20 9, 20.195090322016128 9.01921471959677, 20.38268343236509 9.076120467488714, 20.5555702330196 9.168530387697455, 20.707106781186546 9.292893218813452, 20.831469612302545 9.444429766980399, 20.923879532511286 9.61731656763491, 20.980785280403232 9.804909677983872, 21 10, 21 20, 20.980785280403232 20.195090322016128, 20.923879532511286 20.38268343236509, 20.831469612302545 20.5555702330196, 20.707106781186546 20.707106781186546, 20.5555702330196 20.831469612302545, 20.38268343236509 20.923879532511286, 20.195090322016128 20.980785280403232, 20 21, 10 21, 9.804909677983872 20.980785280403232, 9.61731656763491 20.923879532511286, 9.444429766980399 20.831469612302545, 9.292893218813452 20.707106781186546, 9.168530387697455 20.5555702330196, 9.076120467488714 20.38268343236509, 9.01921471959677 20.195090322016128, 9 20)), ((7 20, 7.057644158790309 20.585270966048387, 7.22836140246614 21.14805029709527, 7.5055911630923635 21.666710699058807, 7.878679656440358 22.121320343559642, 8.333289300941194 22.494408836907635, 8.851949702904731 22.771638597533858, 9.414729033951616 22.94235584120969, 10 23, 20 23, 20.585270966048384 22.94235584120969, 21.14805029709527 22.771638597533858, 21.666710699058807 22.494408836907635, 22.121320343559642 22.121320343559642, 22.494408836907635 21.666710699058807, 22.771638597533858 21.14805029709527, 22.94235584120969 20.585270966048384, 23 20, 23 10, 22.94235584120969 9.414729033951616, 22.771638597533858 8.851949702904731, 22.494408836907635 8.333289300941193, 22.121320343559642 7.878679656440358, 21.666710699058807 7.505591163092364, 21.14805029709527 7.22836140246614, 20.585270966048384 7.057644158790309, 20 7, 10 7, 9.414729033951614 7.057644158790309, 8.85194970290473 7.22836140246614, 8.333289300941193 7.505591163092364, 7.878679656440357 7.878679656440358, 7.5055911630923635 8.333289300941194, 7.22836140246614 8.851949702904731, 7.057644158790309 9.414729033951614, 7 10, 7 20), (8 20, 8 10, 8.03842943919354 9.609819355967744, 8.152240934977426 9.23463313526982, 8.337060775394908 8.888859533960796, 8.585786437626904 8.585786437626904, 8.888859533960796 8.33706077539491, 9.234633135269819 8.152240934977428, 9.609819355967742 8.03842943919354, 10 8, 20 8, 20.390180644032256 8.03842943919354, 20.76536686473018 8.152240934977426, 21.111140466039206 8.33706077539491, 21.414213562373096 8.585786437626904, 21.66293922460509 8.888859533960796, 21.847759065022572 9.23463313526982, 21.96157056080646 9.609819355967744, 22 10, 22 20, 21.96157056080646 20.390180644032256, 21.847759065022572 20.76536686473018, 21.66293922460509 21.111140466039203, 21.414213562373096 21.414213562373096, 21.111140466039206 21.66293922460509, 20.76536686473018 21.847759065022572, 20.390180644032256 21.96157056080646, 20 22, 10 22, 9.609819355967744 21.96157056080646, 9.23463313526982 21.847759065022572, 8.888859533960796 21.66293922460509, 8.585786437626904 21.414213562373096, 8.33706077539491 21.111140466039203, 8.152240934977426 20.76536686473018, 8.03842943919354 20.390180644032256, 8 20)))", rs.getObject(1));
        rs.close();
    }


    @Test
    public void test_ST_SideBuffer1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SideBuffer('LINESTRING (120 150, 180 270)', 10);");
        rs.next();
        assertEquals("POLYGON ((180 270, 120 150, 111.05572809000084 154.47213595499957, 171.05572809000085 274.4721359549996, 180 270))",
                rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_SideBuffer2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SideBuffer('LINESTRING (120 150, 180 270)', -10);");
        rs.next();
        assertEquals("POLYGON ((120 150, 180 270, 188.94427190999915 265.5278640450004, 128.94427190999915 145.52786404500043, 120 150))",
                rs.getString(1));
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_SideBuffer3()  throws Throwable {
        try {
            st.execute("SELECT ST_SideBuffer('LINESTRING (120 150, 180 270)', 10, 'endcap=square');");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test
    public void test_ST_SideBuffer4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SideBuffer('LINESTRING (100 200, 150 250, 200 200)', 10, 'join=round quad_segs=2');");
        rs.next();
        assertEquals("POLYGON ((200 200, 150 250, 100 200, 92.92893218813452 207.07106781186548, 142.92893218813452 257.0710678118655, 150 260, 157.07106781186548 257.0710678118655, 207.07106781186548 207.07106781186548, 200 200))",
                rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_SideBuffer5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SideBuffer('POINT (100 200)', 10, 'join=round quad_segs=2');");
        rs.next();
        assertEquals("POLYGON ((110 200, 107.07106781186548 192.92893218813452, 100 190, 92.92893218813452 192.92893218813452, 90 200, 92.92893218813452 207.07106781186548, 100 210, 107.07106781186548 207.07106781186548, 110 200))",
                rs.getString(1));
        rs.close();
    }
    
    
    @Test
    public void test_ST_SideBuffer6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SideBuffer(null, 10);");
        rs.next();
        assertNull(rs.getObject(1));        
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
            assertTrue(ST_CoordDim.getCoordinateDimension(ValueGeometry.getFromGeometry(pol).getBytesNoCopy()) == 3);
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

}
