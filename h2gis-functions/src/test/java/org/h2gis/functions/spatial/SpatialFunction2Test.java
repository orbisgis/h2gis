/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial;


import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Erwan Bocher
 */
public class SpatialFunction2Test {

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(SpatialFunction2Test.class.getSimpleName());
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
    public void test_ST_SunPosition() throws Exception {
        //Test based on http://www.esrl.noaa.gov/gmd/grad/solcalc/
        ResultSet rs = st.executeQuery("SELECT ST_SunPosition('POINT (139.74 35.65)'::GEOMETRY, '2015-1-25 21:49:26+01:00');");
        assertTrue(rs.next());
        Geometry point = (Geometry) rs.getObject(1);
        assertEquals(104.99439384398441, Math.toDegrees(point.getCoordinate().x), 10E-1);
        assertEquals(-11.642433433992824, Math.toDegrees(point.getCoordinate().y), 10E-1);
        rs.close();
    }

    @Test
    public void test_ST_Shadow() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10)'::GEOMETRY, "
                + "radians(90),radians(45), 2);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGONZ ((10 5 0, 10 10 0, 8 10 0, 8 5 0, 10 5 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10)'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGONZ ((10 5 0, 10 10 0, 12 10 0, 12 5 0, 10 5 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POINTZ (10 5 0)'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRINGZ (10 5 0, 12 5 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10, 12 10, 12 11.5)'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGONZ (((10 5 0, 10 10 0, 12 10 0, 12 5 0, 10 5 0)),"
                + "  ((12 10 0, 12 11.5 0, 14 11.5 0, 14 10 0, 12 10 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10, 10 5, 8 5, 8 10, 10 10))'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGONZ ((10 5 0, 10 10 0, 12 10 0, 12 5 0, 10 5 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON Z((10 10 1, 10 5 0, 8 5 10, 8 10 0, 10 10 0))'::GEOMETRY, "
                + "radians(225),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGONZ ((8 10 0, 9.414213562373096 11.414213562373096 0, 11.414213562373096 11.414213562373096 0, 11.414213562373096 6.414213562373096 0, 10 5 0, 10 6.414213562373096 0.14142135623730961, 10 10 0, 9.414213562373096 10 0, 8 10 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10, 10 5, 8 5, 8 10, 10 10),"
                + "  (8.5 8, 9.5 8, 9.5 6, 8.5 6, 8.5 8))'::GEOMETRY, "
                + "radians(270),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGONZ (((10 5 0, 10 10 0, 10.5 10 0, 10.5 5 0, 10 5 0)), ((8.5 6 0, 8.5 8 0, 9 8 0, 9 6 0, 8.5 6 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((6 12, 6 9, 8 9, 8 8.1, 10.9 8.1, 10.9 9, 10 9, 10 11, 9 11, 9 9, 8.5 9, 8.5 12, 6 12), (6.7 11.1, 7.6 11.1, 7.6 10.3, 6.7 10.3, 6.7 11.1))'::GEOMETRY, "
                + "radians(270),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGONZ (((10.9 8.1 0, 10.9 9 0, 11.4 9 0, 11.4 8.1 0, 10.9 8.1 0)), ((10 9 0, 10 11 0, 10.5 11 0, 10.5 9 0, 10 9 0)), ((8.5 9 0, 8.5 12 0, 9 12 0, 9 11 0, 9 9 0, 8.5 9 0)), ((6.7 10.3 0, 6.7 11.1 0, 7.2 11.1 0, 7.2 10.3 0, 6.7 10.3 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((6 12, 6 9, 8 9, 8 8.1, 10.9 8.1, 10.9 9, 10 9, 10 11, 9 11, 9 9, 8.5 9, 8.5 12, 6 12), (6.7 11.1, 7.6 11.1, 7.6 10.3, 6.7 10.3, 6.7 11.1))'::GEOMETRY, "
                + "radians(315),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGONZ (((8 8.1 0, 8.353553390593273 8.1 0, 10.9 8.1 0, 10.9 8.646446609406727 0, 10.9 9 0, 11.253553390593273 8.646446609406727 0, 11.253553390593273 7.746446609406726 0, 8.353553390593273 7.746446609406726 0, 8 8.1 0)), ((10 9 0, 10 10.646446609406727 0, 10 11 0, 10.353553390593273 10.646446609406727 0, 10.353553390593273 9 0, 10 9 0)), ((8.5 9 0, 8.5 11.646446609406727 0, 8.5 12 0, 8.853553390593273 11.646446609406727 0, 8.853553390593273 9 0, 8.5 9 0)), ((6 9 0, 6.353553390593274 9 0, 8 9 0, 8 8.646446609406727 0, 6.353553390593274 8.646446609406727 0, 6 9 0)), ((6.7 10.3 0, 6.7 11.1 0, 7.6 11.1 0, 7.6 10.746446609406727 0, 7.053553390593274 10.746446609406727 0, 7.053553390593274 10.3 0, 6.7 10.3 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((354610.6 6689345.5, 354626.2 6689342.7, 354627.2 6689342.2, 354639.5 6689334.4, 354640.3 6689333, 354640.6 6689331.4, 354640.2 6689330, 354639.6 6689328.6, 354634 6689322.2, 354633.1 6689321.1, 354628 6689314.4, 354626.7 6689313.5, 354625.4 6689313.4, 354622.4 6689313.7, 354621 6689314.4, 354608.7 6689341.9, 354610.6 6689345.5), (354624.2 6689335.3, 354623.8 6689333.7, 354622.1 6689329, 354625.1 6689322.1, 354628.7 6689326.5, 354632.4 6689331, 354624.2 6689335.3))'::GEOMETRY, "
                + "radians(270),radians(45), 2);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGONZ (((354610.6 6689345.5 0, 354612.6 6689345.5 0, 354628.2 6689342.7 0, 354629.2 6689342.2 0, 354641.5 6689334.4 0, 354642.3 6689333 0, 354642.6 6689331.4 0, 354642.2 6689330 0, 354641.6 6689328.6 0, 354636 6689322.2 0, 354635.1 6689321.1 0, 354630 6689314.4 0, 354628.7 6689313.5 0, 354627.4 6689313.4 0, 354625.4 6689313.4 0, 354626.7 6689313.5 0, 354628 6689314.4 0, 354633.1 6689321.1 0, 354634 6689322.2 0, 354639.6 6689328.6 0, 354640.2 6689330 0, 354640.6 6689331.4 0, 354640.3 6689333 0, 354639.5 6689334.4 0, 354627.2 6689342.2 0, 354626.2 6689342.7 0, 354610.6 6689345.5 0)), ((354622.1 6689329 0, 354623.8 6689333.7 0, 354624.2 6689335.3 0, 354625.96819407004 6689334.37277628 0, 354625.8 6689333.7 0, 354624.1 6689329 0, 354626.40599369083 6689323.696214511 0, 354625.1 6689322.1 0, 354622.1 6689329 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((354610.6 6689345.5, 354626.2 6689342.7, 354627.2 6689342.2, 354639.5 6689334.4, 354640.3 6689333, 354640.6 6689331.4, 354640.2 6689330, 354639.6 6689328.6, 354634 6689322.2, 354633.1 6689321.1, 354628 6689314.4, 354626.7 6689313.5, 354625.4 6689313.4, 354622.4 6689313.7, 354621 6689314.4, 354608.7 6689341.9, 354610.6 6689345.5), (354624.2 6689335.3, 354623.8 6689333.7, 354622.1 6689329, 354625.1 6689322.1, 354628.7 6689326.5, 354632.4 6689331, 354624.2 6689335.3))'::GEOMETRY, "
                + "radians(270),radians(45), 2, false);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON Z (((354610.6 6689345.5 0, 354626.2 6689342.7 0, 354628.2 6689342.7 0, 354612.6 6689345.5 0, 354610.6 6689345.5 0)), ((354626.2 6689342.7 0, 354627.2 6689342.2 0, 354629.2 6689342.2 0, 354628.2 6689342.7 0, 354626.2 6689342.7 0)), ((354627.2 6689342.2 0, 354639.5 6689334.4 0, 354641.5 6689334.4 0, 354629.2 6689342.2 0, 354627.2 6689342.2 0)), ((354639.5 6689334.4 0, 354640.3 6689333 0, 354642.3 6689333 0, 354641.5 6689334.4 0, 354639.5 6689334.4 0)), ((354640.3 6689333 0, 354640.6 6689331.4 0, 354642.6 6689331.4 0, 354642.3 6689333 0, 354640.3 6689333 0)), ((354640.6 6689331.4 0, 354640.2 6689330 0, 354642.2 6689330 0, 354642.6 6689331.4 0, 354640.6 6689331.4 0)), ((354640.2 6689330 0, 354639.6 6689328.6 0, 354641.6 6689328.6 0, 354642.2 6689330 0, 354640.2 6689330 0)), ((354639.6 6689328.6 0, 354634 6689322.2 0, 354636 6689322.2 0, 354641.6 6689328.6 0, 354639.6 6689328.6 0)), ((354634 6689322.2 0, 354633.1 6689321.1 0, 354635.1 6689321.1 0, 354636 6689322.2 0, 354634 6689322.2 0)), ((354633.1 6689321.1 0, 354628 6689314.4 0, 354630 6689314.4 0, 354635.1 6689321.1 0, 354633.1 6689321.1 0)), ((354628 6689314.4 0, 354626.7 6689313.5 0, 354628.7 6689313.5 0, 354630 6689314.4 0, 354628 6689314.4 0)), ((354626.7 6689313.5 0, 354625.4 6689313.4 0, 354627.4 6689313.4 0, 354628.7 6689313.5 0, 354626.7 6689313.5 0)), ((354625.4 6689313.4 0, 354622.4 6689313.7 0, 354624.4 6689313.7 0, 354627.4 6689313.4 0, 354625.4 6689313.4 0)), ((354622.4 6689313.7 0, 354621 6689314.4 0, 354623 6689314.4 0, 354624.4 6689313.7 0, 354622.4 6689313.7 0)), ((354621 6689314.4 0, 354608.7 6689341.9 0, 354610.7 6689341.9 0, 354623 6689314.4 0, 354621 6689314.4 0)), ((354608.7 6689341.9 0, 354610.6 6689345.5 0, 354612.6 6689345.5 0, 354610.7 6689341.9 0, 354608.7 6689341.9 0)), ((354624.2 6689335.3 0, 354623.8 6689333.7 0, 354625.8 6689333.7 0, 354626.2 6689335.3 0, 354624.2 6689335.3 0)), ((354623.8 6689333.7 0, 354622.1 6689329 0, 354624.1 6689329 0, 354625.8 6689333.7 0, 354623.8 6689333.7 0)), ((354622.1 6689329 0, 354625.1 6689322.1 0, 354627.1 6689322.1 0, 354624.1 6689329 0, 354622.1 6689329 0)), ((354625.1 6689322.1 0, 354628.7 6689326.5 0, 354630.7 6689326.5 0, 354627.1 6689322.1 0, 354625.1 6689322.1 0)), ((354628.7 6689326.5 0, 354632.4 6689331 0, 354634.4 6689331 0, 354630.7 6689326.5 0, 354628.7 6689326.5 0)), ((354632.4 6689331 0, 354624.2 6689335.3 0, 354626.2 6689335.3 0, 354634.4 6689331 0, 354632.4 6689331 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_FlipCoordinates1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_FlipCoordinates('POINT(1 2)');");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(2 1)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_FlipCoordinates2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_FlipCoordinates('LINESTRINGZ (10 5 0, 12 5 0)');");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING Z (5 10 0, 5 12 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MaxDistance1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'LINESTRING ( 2 0, 0 2 )'::geometry)");
        rs.next();
        assertEquals(2, rs.getDouble(1), 0);
        rs.close();
    }

    @Test
    public void test_ST_MaxDistance2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'LINESTRING ( 2 2, 2 2 )'::geometry)");
        rs.next();
        assertEquals(2.8284, rs.getDouble(1), 0.001);
        rs.close();
    }

    @Test
    public void test_ST_MaxDistance3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('LINESTRING ( 0 0, 2 2, 10 0 )'::geometry, 'LINESTRING ( 0 0, 2 2, 10 0 )'::geometry)");
        rs.next();
        assertEquals(10, rs.getDouble(1), 0);
        rs.close();
    }


    @Test
    public void test_ST_MaxDistance4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'POINT(0 10)'::geometry)");
        rs.next();
        assertEquals(10, rs.getDouble(1), 0);
        rs.close();
    }

    @Test
    public void test_ST_MaxDistance5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'POINT(0 10)'::geometry)");
        rs.next();
        assertEquals(10, rs.getDouble(1), 0);
        rs.close();
    }

    @Test
    public void test_ST_MaxDistance6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance(ST_BUFFER('POINT(0 0)'::geometry, 10), ST_BUFFER('POINT(0 0)'::geometry, 10))");
        rs.next();
        assertEquals(20, rs.getDouble(1), 0);
        rs.close();
    }

    @Test
    public void test_ST_LongestLine1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('POINT(0 0)'::geometry, 'POINT(0 0)'::geometry)");
        rs.next();
        assertNull(rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_LongestLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('MULTIPOINT((0 0),(0 0))'::geometry, 'MULTIPOINT((0 0),(0 0))'::geometry)");
        rs.next();
        assertNull(rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_LongestLine3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('POINT(0 0)'::geometry, 'LINESTRING(0 0,5 20, 10 0)'::geometry)");
        rs.next();
        assertGeometryEquals("LINESTRING (0 0, 5 20)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_LongestLine4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('POLYGON ((50 400, 50 350, 50 300, 70 290, 80 280, 90 260, 99 254, 109 253, 120 252, 130 252, 140 255, 150 259, 159 264, 168 270, 172 280, 174 290, 175 300, 173 310, 167 320, 158 325, 148 325, 138 323, 129 316, 124 306, 114 312, 108 321, 105 332, 106 342, 112 351, 121 357, 132 359, 143 359, 153 359, 163 360, 173 359, 183 357, 193 355, 203 355, 216 355, 227 355, 238 357, 247 362, 256 369, 264 378, 269 387, 275 396, 273 406, 267 414, 259 421, 250 426, 240 426, 230 426, 220 425, 210 420, 199 418, 189 419, 179 419, 169 419, 158 419, 148 419, 138 419, 126 419, 116 419, 101 419, 83 419, 70 421, 60 427, 52 433, 44 441, 40 440, 50 400))'::geometry, "
                + "'POLYGON ((310 230, 250 160, 280 130, 310 160, 330 130, 327 119, 327 109, 327 99, 329 89, 332 79, 332 69, 342 67, 349 75, 356 83, 420 70, 390 170, 310 230))'::geometry)");
        rs.next();
        assertGeometryEquals("LINESTRING (40 440, 420 70)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_LongestLine5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('MULTIPOLYGON (((50 400, 50 350, 50 300, 70 290, 80 280, 90 260, 99 254, 109 253, 120 252, 130 252, 140 255, 150 259, 159 264, 168 270, 172 280, 174 290, 175 300, 173 310, 167 320, 158 325, 148 325, 138 323, 129 316, 124 306, 114 312, 108 321, 105 332, 106 342, 112 351, 121 357, 132 359, 143 359, 153 359, 163 360, 173 359, 183 357, 193 355, 203 355, 216 355, 227 355, 238 357, 247 362, 256 369, 264 378, 269 387, 275 396, 273 406, 267 414, 259 421, 250 426, 240 426, 230 426, 220 425, 210 420, 199 418, 189 419, 179 419, 169 419, 158 419, 148 419, 138 419, 126 419, 116 419, 101 419, 83 419, 70 421, 60 427, 52 433, 44 441, 40 440, 50 400),(80 400, 100 400, 100 390, 80 390, 80 400),(177 395, 200 395, 200 380, 177 380, 177 395), (74 344, 80 344, 80 310, 74 310, 74 344)))'::geometry, "
                + "'POLYGON ((310 230, 250 160, 280 130, 310 160, 330 130, 327 119, 327 109, 327 99, 329 89, 332 79, 332 69, 342 67, 349 75, 356 83, 420 70, 390 170, 310 230),(310 190, 330 190, 330 180, 310 180, 310 190), (350 140, 364 140, 364 116, 350 116, 350 140))'::geometry)");
        rs.next();
        assertGeometryEquals("LINESTRING (40 440, 420 70)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_LongestLine6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('POLYGON ((320 430, 250 370, 220 260, 266 214, 296 164, 380 160, 390 200, 510 110, 510 160, 506 170, 470 200, 450 210, 453 220, 460 240, 469 248, 500 270, 460 320, 480 340, 500 380, 480 430, 460 450, 430 460, 420 460, 325 472, 320 430))'::geometry, "
                + "'POLYGON ((320 430, 250 370, 220 260, 266 214, 296 164, 380 160, 390 200, 510 110, 510 160, 506 170, 470 200, 450 210, 453 220, 460 240, 469 248, 500 270, 460 320, 480 340, 500 380, 480 430, 460 450, 430 460, 420 460, 325 472, 320 430))'::geometry)");
        rs.next();
        assertGeometryEquals("LINESTRING (325 472, 510 110)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Perimeter1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Perimeter('POLYGON ((130 390, 280 390, 280 210, 130 210, 130 390))'::GEOMETRY)");
        rs.next();
        assertEquals(660, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_Perimeter2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Perimeter('POLYGON ((130 390, 280 390, 280 210, 130 210, 130 390),"
                + "  (160 360, 214 360, 214 324, 160 324, 160 360),"
                + "  (205 275, 230 275, 230 250, 205 250, 205 275))'::GEOMETRY)");
        rs.next();
        assertEquals(660, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON ((1 1, 3 1, 3 2, 1 2, 1 1))'::GEOMETRY)");
        rs.next();
        assertEquals(2, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGONZ((1 1 0, 3 1 0, 3 2 0, 1 2 0, 1 1 0))'::GEOMETRY)");
        rs.next();
        assertEquals(2, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGONZ((1 1 1, 3 1 1, 3 2 1, 1 2 1, 1 1 1))'::GEOMETRY)");
        rs.next();
        assertEquals(2, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGONZ ((0 0 0, 10 0 0, 0 10 0,0 0 0))'::GEOMETRY)");
        rs.next();
        assertEquals(50, rs.getDouble(1), 1e-1);
        rs.close();
    }

    @Test
    public void test_ST_3DArea5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGONZ ((0 0 0, 10 0 10, 0 10 0,0 0 0))'::GEOMETRY)");
        rs.next();
        assertEquals(70.711, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGONZ ((1 1 0, 2 1 1, 3 1 0, 3 2 0, 2 2 1, 1 2 0, 1 1 0))'::GEOMETRY)");
        rs.next();
        assertEquals(2.828, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON ((1 1, 2 1, 3 1, 3 2, 2 2, 1 2, 1 1),"
                + "  (1.2 1.8, 1.7 1.8, 1.7 1.5, 1.2 1.5, 1.2 1.8),"
                + "  (2.2 1.5, 2.8 1.5, 2.8 1.2, 2.2 1.2, 2.2 1.5))'::GEOMETRY)");
        rs.next();
        assertEquals(1.67, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGONZ ((1 1 0, 2 1 1, 3 1 0, 3 2 0, 2 2 1, 1 2 0, 1 1 0),"
                + "  (1.2 1.8 0, 1.7 1.8 0, 1.7 1.5 0, 1.2 1.5 0, 1.2 1.8 0),"
                + "  (2.2 1.5 0, 2.8 1.5 0, 2.8 1.2 0, 2.2 1.2 0, 2.2 1.5 0))'::GEOMETRY)");
        rs.next();
        assertEquals(3.256, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('MULTIPOLYGONZ(((0 0 0, 10 0 10, 0 10 0,0 0 0)),  ((0 0 0, 10 0 10, 0 10 0,0 0 0)))');");
        rs.next();
        assertEquals(141.422, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('GEOMETRYCOLLECTIONZ("
                + "MULTIPOINT Z((4 4 0), (1 1 0), (1 0 0), (0 3 0)),"
                + "LINESTRING Z(2 1 0, 1 3 0, 5 2 0),"
                + "POLYGON Z((0 0 0, 10 0 10, 0 10 0,0 0 0)))');");
        rs.next();
        assertEquals(70.711, rs.getDouble(1), 1e-3);
        rs.close();
    }

    @Test
    public void test_ST_3DArea11() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('GEOMETRYCOLLECTION("
                + "MULTIPOINT((4 4), (1 1), (1 0), (0 3)),"
                + "LINESTRING(2 1, 1 3, 5 2))');");
        rs.next();
        assertEquals(0.d, rs.getDouble(1), 1e-1);
        rs.close();
    }

    @Test
    public void test_ST_3DPerimeter() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('LINESTRING(1 4, 15 7, 16 17)',2249)),"
                + "(ST_GeomFromText('LINESTRING Z(1 4 3, 15 7 9, 16 17 22)',2249)),"
                + "(ST_GeomFromText('MULTILINESTRING Z((1 4 3, 15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249)),"
                + "(ST_GeomFromText('POLYGON Z((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))',2249)),"
                + "(ST_GeomFromText('POLYGON Z((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1),"
                + "  (1.235 1.83 0, 1.825 1.83 0, 1.825 1.605 0, 1.235 1.605 0, 1.235 1.83 0),"
                + "  (2.235 1.63 0, 2.635 1.63 0, 2.635 1.155 0, 2.235 1.155 0, 2.235 1.63 0))',2249)),"
                + "(ST_GeomFromText('GEOMETRYCOLLECTIONZ(LINESTRINGZ(1 4 3, 15 7 9, 16 17 22),"
                + "POLYGONZ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))',2249));");
        ResultSet rs = st.executeQuery(
                "SELECT ST_3DPerimeter(geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(6, rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(2) + 2 * Math.sqrt(5) + Math.sqrt(10), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(2) + 2 * Math.sqrt(5) + Math.sqrt(10), rs.getDouble(1), 0.0);
        assertTrue(rs.next());
        assertEquals(Math.sqrt(2) + 2 * Math.sqrt(5) + Math.sqrt(10), rs.getDouble(1), 0.0);
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_GeomFromGML() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_GeomFromGML('"
                        + "<gml:LineString srsName=\"EPSG:4269\">"
                        + "<gml:coordinates>"
                        + "-71.16028,42.258729 -71.160837,42.259112 -71.161143,42.25932"
                        + "</gml:coordinates>"
                        + "</gml:LineString>');");
        rs.next();
        assertGeometryEquals("SRID=4269;LINESTRING (-71.16028 42.258729, -71.160837 42.259112, -71.161143 42.25932)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_OSMMapLink1() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_OSMMapLink('POINT(-2.070365 47.643713)'::GEOMETRY);");
        rs.next();
        assertEquals("http://www.openstreetmap.org/?minlon=-2.070365&minlat=47.643713&maxlon=-2.070365&maxlat=47.643713", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_OSMMapLink2() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_OSMMapLink('POINT(-2.070365 47.643713)'::GEOMETRY, true);");
        rs.next();
        assertEquals("http://www.openstreetmap.org/?minlon=-2.070365&minlat=47.643713&maxlon=-2.070365&maxlat=47.643713&mlat=47.643713&mlon=-2.070365", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_OSMMapLink3() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_OSMMapLink('POLYGON ((-2.0709347546515247 47.644338296511584, -2.070931209637288 47.64401570021603, -2.070280699524818 47.64397847756654, -2.070241704368212 47.6441858608994, -2.0703338747383713 47.64435070406142, -2.0709347546515247 47.644338296511584))'::GEOMETRY);");
        rs.next();
        assertEquals("http://www.openstreetmap.org/?minlon=-2.0709347546515247&minlat=47.64397847756654&maxlon=-2.070241704368212&maxlat=47.64435070406142", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_OSMMapLink4() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_OSMMapLink('POLYGON ((-2.0709347546515247 47.644338296511584, -2.070931209637288 47.64401570021603, -2.070280699524818 47.64397847756654, -2.070241704368212 47.6441858608994, -2.0703338747383713 47.64435070406142, -2.0709347546515247 47.644338296511584))'::GEOMETRY, true);");
        rs.next();
        assertEquals("http://www.openstreetmap.org/?minlon=-2.0709347546515247&minlat=47.64397847756654&maxlon=-2.070241704368212&maxlat=47.64435070406142&mlat=47.64416459081398&mlon=-2.0705882295098683", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_GoogleMapLink1() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_GoogleMapLink('POINT(-2.070365 47.643713)'::GEOMETRY);");
        rs.next();
        assertEquals("https://maps.google.com/maps?ll=47.643713,-2.070365&z=19&t=m", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_GoogleMapLink2() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_GoogleMapLink('POINT(-2.070365 47.643713)'::GEOMETRY, 'p');");
        rs.next();
        assertEquals("https://maps.google.com/maps?ll=47.643713,-2.070365&z=19&t=p", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_GoogleMapLink3() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            try {
                st.execute("SELECT ST_GoogleMapLink('POINT(-2.070365 47.643713)'::GEOMETRY, 'dsp');");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_GoogleMapLink4() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_GoogleMapLink('POINT(-2.070365 47.643713)'::GEOMETRY, 'p', 8);");
        rs.next();
        assertEquals("https://maps.google.com/maps?ll=47.643713,-2.070365&z=8&t=p", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_AsGML1() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_GeomFromGML(ST_ASGML('POINT(-2.070365 47.643713)'::GEOMETRY));");
        rs.next();
        assertGeometryEquals("POINT (-2.070365 47.643713)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_NPoints1() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NPoints('POINT(-2.070365 47.643713)'::GEOMETRY);");
        rs.next();
        assertEquals(1, rs.getInt(1));
        rs.close();
    }

    @Test
    public void test_ST_NPoints2() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NPoints('MULTIPOINT((4 4), (1 1), (1 0), (0 3))'::GEOMETRY);");
        rs.next();
        assertEquals(4, rs.getInt(1));
        rs.close();
    }

    @Test
    public void test_ST_NPoints3() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NPoints('POLYGONZ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))'::GEOMETRY);");
        rs.next();
        assertEquals(5, rs.getInt(1));
        rs.close();
    }

    @Test
    public void test_ST_NumInteriorRings1() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NumInteriorRings('POLYGONZ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))'::GEOMETRY);");
        rs.next();
        assertEquals(0, rs.getInt(1));
        rs.close();
    }

    @Test
    public void test_ST_NumInteriorRings2() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NumInteriorRings(null);");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_NumInteriorRings3() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NumInteriorRings('POLYGON ((95 371, 310 371, 310 230, 95 230, 95 371),  (120 350, 185 350, 185 301, 120 301, 120 350))'::geometry);");
        rs.next();
        assertEquals(1, rs.getInt(1));
        rs.close();
    }

    @Test
    public void test_ST_NumInteriorRings4() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NumInteriorRings('GEOMETRYCOLLECTION (POLYGON ((95 371, 310 371, 310 230, 95 230, 95 371),"
                        + "  (120 350, 185 350, 185 301, 120 301, 120 350)),"
                        + "  LINESTRING (112 165, 200 140))'::geometry);");
        rs.next();
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_NumInteriorRings5() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NumInteriorRings('MULTIPOLYGON (((95 371, 310 371, 310 230, 95 230, 95 371),"
                        + "  (120 350, 185 350, 185 301, 120 301, 120 350)),"
                        + "  ((150 190, 210 190, 210 160, 150 160, 150 190)))'::geometry);");
        rs.next();
        assertEquals(1, rs.getInt(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveDuplicatedCoordinates1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveDuplicatedCoordinates('MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOINT((4 4), (1 1), (1 0), (0 3))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveDuplicatedCoordinates2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveDuplicatedCoordinates('MULTIPOINT((4 4), (1 1), (1 0), (1 1), (4 4), (0 3), (4 4))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOINT((4 4), (1 1), (1 0), (0 3))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveDuplicatedCoordinates3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveDuplicatedCoordinates('LINESTRING(4 4, 1 1, 1 1)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING(4 4,  1 1)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveDuplicatedCoordinates4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveDuplicatedCoordinates('POLYGON((4 4, 1 1, 1 1, 0 0, 4 4))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON((4 4, 1 1, 0 0, 4 4))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_RemoveDuplicatedCoordinates5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RemoveDuplicatedCoordinates('SRID=4326;MULTIPOINT((4 4), (1 1), (1 0), (0 3), (4 4))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;MULTIPOINT((4 4), (1 1), (1 0), (0 3))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('SRID=4326;POINT(0 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;POINT (0 0)", rs.getString(1));
        rs.close();
    }


    @Test
    public void test_ST_MakeValid4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POINTZ(0 1 2)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).getCoordinate().z == 2);
        rs.close();
    }

    @Test
    public void test_ST_MakeValid5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('LINESTRING(0 0, 10 0, 20 0, 20 0, 30 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 0, 10 0, 20 0, 20 0, 30 0)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('LINESTRING(0 0, 10 0, 20 0, 20 0, 30 0)'::GEOMETRY, true,false);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 0, 10 0, 20 0, 30 0)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('LINESTRING(0 0, 10 0, 20 0, 20 0, 30 0)'::GEOMETRY, true,false);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 0, 10 0, 20 0, 30 0)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('LINESTRING Z(0 0 1, 10 0 2, 20 0 1, 20 0 1, 30 0 0)'::GEOMETRY, true,false);");
        rs.next();
        assertGeometryEquals("LINESTRING Z(0 0 1, 10 0 2, 20 0 1, 30 0 0)", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POLYGON (( 322 354, 322 348, 325 351, 328 351, 331 348, 331 354, 328 351, 325 351, 322 354 ))'::GEOMETRY, false);");
        rs.next();
        assertGeometryEquals("GEOMETRYCOLLECTION (LINESTRING (325 351, 328 351), POLYGON ((322 348, 322 354, 325 351, 322 348)), POLYGON ((328 351, 331 354, 331 348, 328 351)))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POLYGON (( 322 354, 322 348, 322 354, 322 348, 325 351, 328 351, 331 348, 331 354, 328 351, 325 351, 322 354 ))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((322 348, 322 354, 325 351, 322 348)), ((328 351, 331 354, 331 348, 328 351)))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid11() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POLYGON (( 322 354, 322 348, 322 354, 322 348, 325 351, 328 351, 331 348, 331 354, 328 351, 325 351, 322 354 ))'::GEOMETRY, true);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((322 348, 322 354, 325 351, 322 348)), ((328 351, 331 354, 331 348, 328 351)))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid12() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), ((10 0, 20 0, 20 10, 10 10, 10 0)))'::GEOMETRY, false);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((0 0, 0 10, 10 10, 20 10, 20 0, 10 0, 0 0)))", rs.getString(1));
        rs.close();
    }


    @Test
    public void test_ST_MakeValid13() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POLYGON Z ((353851 7684917 0, 353851 7684918 136.1, 353853 7684918 0, 353852 7684918 135.6, 353851 7684917 0))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON Z((353851 7684917 0, 353851 7684918 136.1, 353852 7684918 135.6, 353851 7684917 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeValid14() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('SRID=4326;LINESTRING(0 0, 10 0, 20 0, 20 0, 30 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;LINESTRING(0 0, 10 0, 20 0, 20 0, 30 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_DistanceSpherePointToPointEpsg4326() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(0 0)'::GEOMETRY, 'POINT(-118 38)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(12421874.764417205, rs.getDouble(1), 1e-8);
    }

    @Test
    public void test_ST_DistanceSpherePointToPointEpsg4008() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere(ST_SetSRID('POINT(0 0)'::GEOMETRY, 4008), ST_SetSRID('POINT(-118 38)'::GEOMETRY, 4008))");
        assertTrue(rs.next());
        assertEquals(12421855.452633386, rs.getDouble(1), 1e-8);
    }

    @Test
    public void test_ST_DistanceSpherePointToPointEpsg2375() throws Exception {
        Statement st = connection.createStatement();
        assertThrows(SQLException.class, () -> {
            ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere(ST_SetSRID('POINT(0 0)'::GEOMETRY, 2375), ST_SetSRID('POINT(-118 38)'::GEOMETRY, 2375))");
            assertTrue(rs.next());
        });
    }

    @Test
    public void test_ST_DistanceSpherePointToLineString() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(0 0)'::GEOMETRY, 'LINESTRING (10 5, 10 10)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(1241932.5985221416, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSphereLineStringToPoint() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('LINESTRING (10 5, 10 10)'::GEOMETRY, 'POINT(0 0)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(1241932.5985221416, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSphereLineStringToLineString() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('LINESTRING (10 5, 10 10)'::GEOMETRY, 'LINESTRING(0 0, 1 1)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(1093701.742472634, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointToPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(0 0)'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(157249.5977685051, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToPoint() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY, 'POINT(0 0)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(157249.5977685051, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointInPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(1 1)'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSphereLineStringToPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('LINESTRING (100 50, 50 50)'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(5763657.991914633, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToLineString() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY, 'LINESTRING (100 50, 50 50)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(5763657.991914633, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToLineStringDistanceZero() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY, 'LINESTRING (1 10, 10 10)'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON ((130 390, 280 390, 280 210, 130 210, 130 390))'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(8496739.27764427, rs.getDouble(1), 1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointToMultiPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT (130 210)'::GEOMETRY, 'MULTIPOLYGON (((50 400, 50 350, 50 300, 70 290, 80 280, 90 260, 99 254, 109 253, 120 252, 130 252, 140 255, 150 259, 159 264, 168 270, 172 280, 174 290, 175 300, 173 310, 167 320, 158 325, 148 325, 138 323, 129 316, 124 306, 114 312, 108 321, 105 332, 106 342, 112 351, 121 357, 132 359, 143 359, 153 359, 163 360, 173 359, 183 357, 193 355, 203 355, 216 355, 227 355, 238 357, 247 362, 256 369, 264 378, 269 387, 275 396, 273 406, 267 414, 259 421, 250 426, 240 426, 230 426, 220 425, 210 420, 199 418, 189 419, 179 419, 169 419, 158 419, 148 419, 138 419, 126 419, 116 419, 101 419, 83 419, 70 421, 60 427, 52 433, 44 441, 40 440, 50 400),(80 400, 100 400, 100 390, 80 390, 80 400),(177 395, 200 395, 200 380, 177 380, 177 395), (74 344, 80 344, 80 310, 74 310, 74 344)))'::GEOMETRY)");
        assertTrue(rs.next());
        assertEquals(1074360.2834168628, rs.getDouble(1), 1e-8);
    }

    @Test
    public void test_ST_Node1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Node('MULTILINESTRING ((100 300, 200 200), (100 200, 200 300))'::GEOMETRY)");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((100 300, 150 250), (150 250, 200 200), (100 200, 150 250),(150 250, 200 300))", rs.getObject(1));
    }

    @Test
    public void test_ST_Node2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Node('MULTIPOLYGON (((100 200, 200 200, 200 100, 100 100, 100 200)), ((151 225, 300 225, 300 70, 151 70, 151 225)))'::GEOMETRY)");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((100 200, 151 200), (151 200, 200 200, 200 100, 151 100), (151 100, 100 100, 100 200), (151 225, 300 225, 300 70, 151 70, 151 100), (151 100, 151 200), (151 200, 151 225))", rs.getObject(1));
    }

    @Test
    public void test_ST_Node3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Node('SRID=4326;MULTILINESTRING ((100 300, 200 200), (100 200, 200 300))'::GEOMETRY)");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;MULTILINESTRING ((100 300, 150 250), (150 250, 200 200), (100 200, 150 250),(150 250, 200 300))", rs.getObject(1));
    }

    @Test
    public void test_ST_Node4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Node('MULTILINESTRING ((5 4, 7 4), (6 4, 8 4))'::GEOMETRY)");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((5 4, 6 4), (6 4, 7 4), (7 4, 8 4))", rs.getObject(1));
    }

    @Test
    public void test_ST_SVF1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf(null, null, 1,0)");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
    }

    @Test
    public void test_ST_SVF2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINTZ(0 0 0)'::GEOMETRY, 'POLYGONZ ((10 -1 10, 20 -1 10, 20 20 10, 10 20 10, 10 -1 10))'::GEOMETRY, 50, 8) as result");
        assertTrue(rs.next());
        double dTheta = Math.toRadians(45);
        double sinGamma0 = Math.sin(Math.atan(10 / Math.sqrt(200)));
        double sinGamma1 = Math.sin(Math.atan(1));
        double svfTest = 1 - (dTheta * (sinGamma0 * sinGamma0 + sinGamma1 * sinGamma1)) / (2 * Math.PI);
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINTZ(0 0 0)'::GEOMETRY, ST_FORCE3D(ST_buffer('POINT(0 0)'::GEOMETRY, 10, 120), 12), 50, 8) as result");
        assertTrue(rs.next());
        double svfTest = 0.4098;
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINTZ(0 0 0)'::GEOMETRY, 'MULTILINESTRINGZ((-10 -1000 12, -10 1000 12), (10 -1000 12, 10 1000 12))'::GEOMETRY, 100, 120) as result");
        assertTrue(rs.next());
        double svfTest = 0.6402;
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF5() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINTZ(0 0 0)'::GEOMETRY, 'MULTIPOLYGONZ(((10 -5 2, 10 5 2, 15 5 2, 15 -5 2, 10 -5 2)), ((15 -5 20, 15 5 20, 20 5 20, 20 -5 20, 15 -5 20)))'::GEOMETRY, 100, 8) as result");
        assertTrue(rs.next());
        double dTheta = Math.toRadians(45);
        double sinGamma = Math.sin(Math.atan2(20, 15));
        double svfTest = 1 - (dTheta * sinGamma * sinGamma) / (2 * Math.PI);
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF6() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINTZ(0 0 0)'::GEOMETRY, 'MULTILINESTRINGZ ((-10 -1000 12, -10 1000 12), (10 -1000 12, 10 1000 12),   (330 -185 10, 325 190 10))'::GEOMETRY, 100, 120) as result");
        assertTrue(rs.next());
        double svfTest = 0.6402;
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF7() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINTZ(0 0 0)'::GEOMETRY, 'MULTILINESTRING ( (330 -185, 325 190))'::GEOMETRY, 100, 120) as result");
        assertTrue(rs.next());
        assertEquals(1, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF8() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINT EMPTY'::GEOMETRY, 'MULTILINESTRING ( (330 -185, 325 190))'::GEOMETRY, 100, 120) as result");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
    }

    @Test
    public void test_ST_SVF9() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT ST_svf('SRID=2154;POINTZ(0 0 0)'::GEOMETRY, " +
                        "ST_FORCE3D(ST_buffer('SRID=2154;POINT(0 0)'::GEOMETRY, 10, 120), 12), 50, 8) as result");
        assertTrue(rs.next());
        double svfTest = 0.4098;
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }

    @Test
    public void test_ST_SVF10() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_svf('SRID=27572;POINTZ(0 0 0)'::GEOMETRY, " +
                    "ST_FORCE3D(ST_buffer('SRID=2154;POINT(0 0)'::GEOMETRY, 10, 120), 12), 50, 8) as result");
        });
    }

    @Test
    public void test_ST_ShortestLine1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT st_length(ST_ShortestLine('POINT(0 0)'::GEOMETRY, 'LINESTRING(0 0, 10 10)'::GEOMETRY)) as result");
        assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.1);
    }

    @Test
    public void test_ST_ShortestLine2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ShortestLine('POINT(10 0 )'::GEOMETRY, 'LINESTRING(5 5, 10 10)'::GEOMETRY) as result");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (10 0, 5 5)", rs.getObject(1));
    }

    @Test
    public void test_ST_ShortestLine3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ShortestLine('POINT(10 0 )'::GEOMETRY, 'LINESTRING(1 10, 5 5, 10 10)'::GEOMETRY) as result");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (10 0, 5 5)", rs.getObject(1));
    }

    @Test
    public void test_ST_ShortestLine4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ShortestLine('POLYGON ((0 4, 8 4, 8 0, 0 0, 0 4))'::GEOMETRY, 'POLYGON ((2 10.1, 9 10.1, 9 4.9, 2 4.9, 2 10.1))'::GEOMETRY) as result");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (2 4, 2 4.9)", rs.getObject(1));
    }

    @Test
    public void test_ST_GENERATEPOINTS1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeneratePoints('SRID=4326;POLYGON ((0 4, 8 4, 8 0, 0 0, 0 4))'::GEOMETRY,10) as result");
        assertTrue(rs.next());
        Geometry geom = (Geometry) rs.getObject(1);
        assertEquals(4326, geom.getSRID());
        assertEquals(10, geom.getNumPoints());
    }

    @Test
    public void test_ST_GENERATEPOINTS2() throws Exception {
        assertThrows(SQLException.class, () -> {
            try {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT ST_GeneratePoints('LINESTRING (0 4, 8 4, 8 0, 0 0, 0 4)'::GEOMETRY,10) as result");
                assertTrue(rs.next());
                assertNull(rs.getObject(1));
            } catch (JdbcSQLException e) {
                throw e.getCause();
            } finally {
            }
        });
    }

    @Test
    public void test_ST_GENERATEPOINTS3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeneratePoints('POLYGON EMPTY'::GEOMETRY,10) as result");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
    }

    @Test
    public void test_ST_GENERATEPOINTSINGRID1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeneratePointsInGrid('POLYGON ((300 300, 299 95, 600 100, 600 300, 450 180, 300 300))'::GEOMETRY,10, 15) as result");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOINT ((299.5 100), (299.5 115), (299.5 130), (299.5 145), (299.5 160), (299.5 175), (299.5 190), (299.5 205), (299.5 220), (299.5 235), (299.5 250), (299.5 265), (299.5 280), (299.5 295), (309.5 100), (309.5 115), (309.5 130), (309.5 145), (309.5 160), (309.5 175), (309.5 190), (309.5 205), (309.5 220), (309.5 235), (309.5 250), (309.5 265), (309.5 280), (309.5 295), (319.5 100), (319.5 115), (319.5 130), (319.5 145), (319.5 160), (319.5 175), (319.5 190), (319.5 205), (319.5 220), (319.5 235), (319.5 250), (319.5 265), (319.5 280), (319.5 295), (329.5 100), (329.5 115), (329.5 130), (329.5 145), (329.5 160), (329.5 175), (329.5 190), (329.5 205), (329.5 220), (329.5 235), (329.5 250), (329.5 265), (329.5 280), (329.5 295), (339.5 100), (339.5 115), (339.5 130), (339.5 145), (339.5 160), (339.5 175), (339.5 190), (339.5 205), (339.5 220), (339.5 235), (339.5 250), (339.5 265), (339.5 280), (339.5 295), (349.5 100), (349.5 115), (349.5 130), (349.5 145), (349.5 160), (349.5 175), (349.5 190), (349.5 205), (349.5 220), (349.5 235), (349.5 250), (349.5 265), (349.5 280), (349.5 295), (359.5 100), (359.5 115), (359.5 130), (359.5 145), (359.5 160), (359.5 175), (359.5 190), (359.5 205), (359.5 220), (359.5 235), (359.5 250), (359.5 265), (359.5 280), (359.5 295), (369.5 100), (369.5 115), (369.5 130), (369.5 145), (369.5 160), (369.5 175), (369.5 190), (369.5 205), (369.5 220), (369.5 235), (369.5 250), (369.5 265), (369.5 280), (369.5 295), (379.5 100), (379.5 115), (379.5 130), (379.5 145), (379.5 160), (379.5 175), (379.5 190), (379.5 205), (379.5 220), (379.5 235), (379.5 250), (379.5 265), (379.5 280), (379.5 295), (389.5 100), (389.5 115), (389.5 130), (389.5 145), (389.5 160), (389.5 175), (389.5 190), (389.5 205), (389.5 220), (389.5 235), (389.5 250), (389.5 265), (389.5 280), (389.5 295), (399.5 100), (399.5 115), (399.5 130), (399.5 145), (399.5 160), (399.5 175), (399.5 190), (399.5 205), (399.5 220), (399.5 235), (399.5 250), (399.5 265), (399.5 280), (399.5 295), (409.5 100), (409.5 115), (409.5 130), (409.5 145), (409.5 160), (409.5 175), (409.5 190), (409.5 205), (409.5 220), (409.5 235), (409.5 250), (409.5 265), (409.5 280), (409.5 295), (419.5 100), (419.5 115), (419.5 130), (419.5 145), (419.5 160), (419.5 175), (419.5 190), (419.5 205), (419.5 220), (419.5 235), (419.5 250), (419.5 265), (419.5 280), (419.5 295), (429.5 100), (429.5 115), (429.5 130), (429.5 145), (429.5 160), (429.5 175), (429.5 190), (429.5 205), (429.5 220), (429.5 235), (429.5 250), (429.5 265), (429.5 280), (429.5 295), (439.5 100), (439.5 115), (439.5 130), (439.5 145), (439.5 160), (439.5 175), (439.5 190), (439.5 205), (439.5 220), (439.5 235), (439.5 250), (439.5 265), (439.5 280), (439.5 295), (449.5 100), (449.5 115), (449.5 130), (449.5 145), (449.5 160), (449.5 175), (449.5 190), (449.5 205), (449.5 220), (449.5 235), (449.5 250), (449.5 265), (449.5 280), (449.5 295), (459.5 100), (459.5 115), (459.5 130), (459.5 145), (459.5 160), (459.5 175), (459.5 190), (459.5 205), (459.5 220), (459.5 235), (459.5 250), (459.5 265), (459.5 280), (459.5 295), (469.5 100), (469.5 115), (469.5 130), (469.5 145), (469.5 160), (469.5 175), (469.5 190), (469.5 205), (469.5 220), (469.5 235), (469.5 250), (469.5 265), (469.5 280), (469.5 295), (479.5 100), (479.5 115), (479.5 130), (479.5 145), (479.5 160), (479.5 175), (479.5 190), (479.5 205), (479.5 220), (479.5 235), (479.5 250), (479.5 265), (479.5 280), (479.5 295), (489.5 100), (489.5 115), (489.5 130), (489.5 145), (489.5 160), (489.5 175), (489.5 190), (489.5 205), (489.5 220), (489.5 235), (489.5 250), (489.5 265), (489.5 280), (489.5 295), (499.5 100), (499.5 115), (499.5 130), (499.5 145), (499.5 160), (499.5 175), (499.5 190), (499.5 205), (499.5 220), (499.5 235), (499.5 250), (499.5 265), (499.5 280), (499.5 295), (509.5 100), (509.5 115), (509.5 130), (509.5 145), (509.5 160), (509.5 175), (509.5 190), (509.5 205), (509.5 220), (509.5 235), (509.5 250), (509.5 265), (509.5 280), (509.5 295), (519.5 100), (519.5 115), (519.5 130), (519.5 145), (519.5 160), (519.5 175), (519.5 190), (519.5 205), (519.5 220), (519.5 235), (519.5 250), (519.5 265), (519.5 280), (519.5 295), (529.5 100), (529.5 115), (529.5 130), (529.5 145), (529.5 160), (529.5 175), (529.5 190), (529.5 205), (529.5 220), (529.5 235), (529.5 250), (529.5 265), (529.5 280), (529.5 295), (539.5 100), (539.5 115), (539.5 130), (539.5 145), (539.5 160), (539.5 175), (539.5 190), (539.5 205), (539.5 220), (539.5 235), (539.5 250), (539.5 265), (539.5 280), (539.5 295), (549.5 100), (549.5 115), (549.5 130), (549.5 145), (549.5 160), (549.5 175), (549.5 190), (549.5 205), (549.5 220), (549.5 235), (549.5 250), (549.5 265), (549.5 280), (549.5 295), (559.5 100), (559.5 115), (559.5 130), (559.5 145), (559.5 160), (559.5 175), (559.5 190), (559.5 205), (559.5 220), (559.5 235), (559.5 250), (559.5 265), (559.5 280), (559.5 295), (569.5 100), (569.5 115), (569.5 130), (569.5 145), (569.5 160), (569.5 175), (569.5 190), (569.5 205), (569.5 220), (569.5 235), (569.5 250), (569.5 265), (569.5 280), (569.5 295), (579.5 100), (579.5 115), (579.5 130), (579.5 145), (579.5 160), (579.5 175), (579.5 190), (579.5 205), (579.5 220), (579.5 235), (579.5 250), (579.5 265), (579.5 280), (579.5 295), (589.5 100), (589.5 115), (589.5 130), (589.5 145), (589.5 160), (589.5 175), (589.5 190), (589.5 205), (589.5 220), (589.5 235), (589.5 250), (589.5 265), (589.5 280), (589.5 295), (599.5 100), (599.5 115), (599.5 130), (599.5 145), (599.5 160), (599.5 175), (599.5 190), (599.5 205), (599.5 220), (599.5 235), (599.5 250), (599.5 265), (599.5 280), (599.5 295))", ((Geometry) rs.getObject(1)));
    }

    @Test
    public void test_ST_GENERATEPOINTSINGRID2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeneratePointsInGrid('POLYGON ((300 300, 299 95, 600 100, 600 300, 450 180, 300 300))'::GEOMETRY,10, 15, true) as result");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOINT ((299.5 100), (299.5 115), (299.5 130), (299.5 145), (299.5 160), (299.5 175), (299.5 190), (309.5 100), (309.5 115), (309.5 130), (309.5 145), (309.5 160), (309.5 175), (309.5 190), (309.5 205), (309.5 220), (309.5 235), (309.5 250), (309.5 265), (309.5 280), (319.5 100), (319.5 115), (319.5 130), (319.5 145), (319.5 160), (319.5 175), (319.5 190), (319.5 205), (319.5 220), (319.5 235), (319.5 250), (319.5 265), (319.5 280), (329.5 100), (329.5 115), (329.5 130), (329.5 145), (329.5 160), (329.5 175), (329.5 190), (329.5 205), (329.5 220), (329.5 235), (329.5 250), (329.5 265), (339.5 100), (339.5 115), (339.5 130), (339.5 145), (339.5 160), (339.5 175), (339.5 190), (339.5 205), (339.5 220), (339.5 235), (339.5 250), (339.5 265), (349.5 100), (349.5 115), (349.5 130), (349.5 145), (349.5 160), (349.5 175), (349.5 190), (349.5 205), (349.5 220), (349.5 235), (349.5 250), (359.5 100), (359.5 115), (359.5 130), (359.5 145), (359.5 160), (359.5 175), (359.5 190), (359.5 205), (359.5 220), (359.5 235), (359.5 250), (369.5 100), (369.5 115), (369.5 130), (369.5 145), (369.5 160), (369.5 175), (369.5 190), (369.5 205), (369.5 220), (369.5 235), (379.5 100), (379.5 115), (379.5 130), (379.5 145), (379.5 160), (379.5 175), (379.5 190), (379.5 205), (379.5 220), (379.5 235), (389.5 100), (389.5 115), (389.5 130), (389.5 145), (389.5 160), (389.5 175), (389.5 190), (389.5 205), (389.5 220), (399.5 100), (399.5 115), (399.5 130), (399.5 145), (399.5 160), (399.5 175), (399.5 190), (399.5 205), (399.5 220), (409.5 100), (409.5 115), (409.5 130), (409.5 145), (409.5 160), (409.5 175), (409.5 190), (409.5 205), (419.5 100), (419.5 115), (419.5 130), (419.5 145), (419.5 160), (419.5 175), (419.5 190), (429.5 100), (429.5 115), (429.5 130), (429.5 145), (429.5 160), (429.5 175), (429.5 190), (439.5 100), (439.5 115), (439.5 130), (439.5 145), (439.5 160), (439.5 175), (449.5 100), (449.5 115), (449.5 130), (449.5 145), (449.5 160), (449.5 175), (459.5 100), (459.5 115), (459.5 130), (459.5 145), (459.5 160), (459.5 175), (469.5 100), (469.5 115), (469.5 130), (469.5 145), (469.5 160), (469.5 175), (469.5 190), (479.5 100), (479.5 115), (479.5 130), (479.5 145), (479.5 160), (479.5 175), (479.5 190), (489.5 100), (489.5 115), (489.5 130), (489.5 145), (489.5 160), (489.5 175), (489.5 190), (489.5 205), (499.5 100), (499.5 115), (499.5 130), (499.5 145), (499.5 160), (499.5 175), (499.5 190), (499.5 205), (509.5 100), (509.5 115), (509.5 130), (509.5 145), (509.5 160), (509.5 175), (509.5 190), (509.5 205), (509.5 220), (519.5 100), (519.5 115), (519.5 130), (519.5 145), (519.5 160), (519.5 175), (519.5 190), (519.5 205), (519.5 220), (519.5 235), (529.5 100), (529.5 115), (529.5 130), (529.5 145), (529.5 160), (529.5 175), (529.5 190), (529.5 205), (529.5 220), (529.5 235), (539.5 100), (539.5 115), (539.5 130), (539.5 145), (539.5 160), (539.5 175), (539.5 190), (539.5 205), (539.5 220), (539.5 235), (539.5 250), (549.5 100), (549.5 115), (549.5 130), (549.5 145), (549.5 160), (549.5 175), (549.5 190), (549.5 205), (549.5 220), (549.5 235), (549.5 250), (559.5 100), (559.5 115), (559.5 130), (559.5 145), (559.5 160), (559.5 175), (559.5 190), (559.5 205), (559.5 220), (559.5 235), (559.5 250), (559.5 265), (569.5 100), (569.5 115), (569.5 130), (569.5 145), (569.5 160), (569.5 175), (569.5 190), (569.5 205), (569.5 220), (569.5 235), (569.5 250), (569.5 265), (579.5 100), (579.5 115), (579.5 130), (579.5 145), (579.5 160), (579.5 175), (579.5 190), (579.5 205), (579.5 220), (579.5 235), (579.5 250), (579.5 265), (579.5 280), (589.5 100), (589.5 115), (589.5 130), (589.5 145), (589.5 160), (589.5 175), (589.5 190), (589.5 205), (589.5 220), (589.5 235), (589.5 250), (589.5 265), (589.5 280), (599.5 100), (599.5 115), (599.5 130), (599.5 145), (599.5 160), (599.5 175), (599.5 190), (599.5 205), (599.5 220), (599.5 235), (599.5 250), (599.5 265), (599.5 280), (599.5 295))", ((Geometry) rs.getObject(1)));
    }

    @Test
    public void test_ST_GENERATEPOINTSINGRID3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeneratePointsInGrid('POLYGON ((300 300, 299 95, 600 100, 600 300, 450 180, 300 300), (330 200, 409 200, 409 125, 330 125, 330 200), (516 194, 575 194, 575 131, 516 131, 516 194))'::GEOMETRY,10, 15, true) as result");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOINT ((299.5 100), (299.5 115), (299.5 130), (299.5 145), (299.5 160), (299.5 175), (299.5 190), (309.5 100), (309.5 115), (309.5 130), (309.5 145), (309.5 160), (309.5 175), (309.5 190), (309.5 205), (309.5 220), (309.5 235), (309.5 250), (309.5 265), (309.5 280), (319.5 100), (319.5 115), (319.5 130), (319.5 145), (319.5 160), (319.5 175), (319.5 190), (319.5 205), (319.5 220), (319.5 235), (319.5 250), (319.5 265), (319.5 280), (329.5 100), (329.5 115), (329.5 130), (329.5 145), (329.5 160), (329.5 175), (329.5 190), (329.5 205), (329.5 220), (329.5 235), (329.5 250), (329.5 265), (339.5 100), (339.5 115), (339.5 205), (339.5 220), (339.5 235), (339.5 250), (339.5 265), (349.5 100), (349.5 115), (349.5 205), (349.5 220), (349.5 235), (349.5 250), (359.5 100), (359.5 115), (359.5 205), (359.5 220), (359.5 235), (359.5 250), (369.5 100), (369.5 115), (369.5 205), (369.5 220), (369.5 235), (379.5 100), (379.5 115), (379.5 205), (379.5 220), (379.5 235), (389.5 100), (389.5 115), (389.5 205), (389.5 220), (399.5 100), (399.5 115), (399.5 205), (399.5 220), (409.5 100), (409.5 115), (409.5 130), (409.5 145), (409.5 160), (409.5 175), (409.5 190), (409.5 205), (419.5 100), (419.5 115), (419.5 130), (419.5 145), (419.5 160), (419.5 175), (419.5 190), (429.5 100), (429.5 115), (429.5 130), (429.5 145), (429.5 160), (429.5 175), (429.5 190), (439.5 100), (439.5 115), (439.5 130), (439.5 145), (439.5 160), (439.5 175), (449.5 100), (449.5 115), (449.5 130), (449.5 145), (449.5 160), (449.5 175), (459.5 100), (459.5 115), (459.5 130), (459.5 145), (459.5 160), (459.5 175), (469.5 100), (469.5 115), (469.5 130), (469.5 145), (469.5 160), (469.5 175), (469.5 190), (479.5 100), (479.5 115), (479.5 130), (479.5 145), (479.5 160), (479.5 175), (479.5 190), (489.5 100), (489.5 115), (489.5 130), (489.5 145), (489.5 160), (489.5 175), (489.5 190), (489.5 205), (499.5 100), (499.5 115), (499.5 130), (499.5 145), (499.5 160), (499.5 175), (499.5 190), (499.5 205), (509.5 100), (509.5 115), (509.5 130), (509.5 145), (509.5 160), (509.5 175), (509.5 190), (509.5 205), (509.5 220), (519.5 100), (519.5 115), (519.5 130), (519.5 205), (519.5 220), (519.5 235), (529.5 100), (529.5 115), (529.5 130), (529.5 205), (529.5 220), (529.5 235), (539.5 100), (539.5 115), (539.5 130), (539.5 205), (539.5 220), (539.5 235), (539.5 250), (549.5 100), (549.5 115), (549.5 130), (549.5 205), (549.5 220), (549.5 235), (549.5 250), (559.5 100), (559.5 115), (559.5 130), (559.5 205), (559.5 220), (559.5 235), (559.5 250), (559.5 265), (569.5 100), (569.5 115), (569.5 130), (569.5 205), (569.5 220), (569.5 235), (569.5 250), (569.5 265), (579.5 100), (579.5 115), (579.5 130), (579.5 145), (579.5 160), (579.5 175), (579.5 190), (579.5 205), (579.5 220), (579.5 235), (579.5 250), (579.5 265), (579.5 280), (589.5 100), (589.5 115), (589.5 130), (589.5 145), (589.5 160), (589.5 175), (589.5 190), (589.5 205), (589.5 220), (589.5 235), (589.5 250), (589.5 265), (589.5 280), (599.5 100), (599.5 115), (599.5 130), (599.5 145), (599.5 160), (599.5 175), (599.5 190), (599.5 205), (599.5 220), (599.5 235), (599.5 250), (599.5 265), (599.5 280), (599.5 295))", ((Geometry) rs.getObject(1)));
    }

    @Test
    public void test_ST_GENERATEPOINTSINGRID4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeneratePointsInGrid('POLYGON EMPTY'::GEOMETRY,10, 15, true) as result");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
    }

    @Test
    public void test_ST_ISOVIST1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Isovist('POINT(0 0)'::GEOMETRY, 'LINESTRING (100 0, 100 100, 0 100)'::GEOMETRY, "
                + "150);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((-150 0, -147.11779206048456 29.26354830241928, -138.581929876693 57.40251485476347, -124.7204418453818 83.33553495294032, -106.06601717798212 106.06601717798213, -83.3355349529403 124.7204418453818, -57.40251485476347 138.58192987669304, -29.263548302419224 147.1177920604846, 0 150, 0 100, 100 100, 100 0, 150 0, 147.1177920604846 -29.26354830241931, 138.58192987669298 -57.40251485476355, 124.7204418453818 -83.33553495294032, 106.06601717798208 -106.06601717798215, 83.3355349529403 -124.72044184538181, 57.402514854763496 -138.58192987669298, 29.263548302419252 -147.11779206048456, 0 -150, -29.263548302419295 -147.11779206048456, -57.40251485476355 -138.58192987669298, -83.33553495294032 -124.72044184538179, -106.06601717798215 -106.06601717798212, -124.72044184538181 -83.3355349529403, -138.58192987669304 -57.40251485476345, -147.11779206048456 -29.263548302419252, -150 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_ISOVIST2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Isovist('POINT(0 0)'::GEOMETRY, 'MULTILINESTRING ((100 0, 100 100, 0 100),  (-100 100, -100 -100, 100 -100))'::GEOMETRY, "
                + "150);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((-106.06601717798212 106.06601717798213, -83.3355349529403 124.7204418453818, -57.40251485476347 138.58192987669304, -29.263548302419224 147.1177920604846, 0 150, 0 100, 100 100, 100 0, 150 0, 147.1177920604846 -29.26354830241931, 138.58192987669298 -57.40251485476355, 124.7204418453818 -83.33553495294032, 106.06601717798213 -106.0660171779821, 100 -100, -100 -100, -100 100.00000000000003, -106.06601717798212 106.06601717798213))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_ISOVIST3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Isovist('SRID=4326;POINT(0 0)'::GEOMETRY, 'SRID=4326;LINESTRING (100 0, 100 100, 0 100)'::GEOMETRY, "
                + "150);");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;POLYGON ((-150 0, -147.11779206048456 29.26354830241928, -138.581929876693 57.40251485476347, -124.7204418453818 83.33553495294032, -106.06601717798212 106.06601717798213, -83.3355349529403 124.7204418453818, -57.40251485476347 138.58192987669304, -29.263548302419224 147.1177920604846, 0 150, 0 100, 100 100, 100 0, 150 0, 147.1177920604846 -29.26354830241931, 138.58192987669298 -57.40251485476355, 124.7204418453818 -83.33553495294032, 106.06601717798208 -106.06601717798215, 83.3355349529403 -124.72044184538181, 57.402514854763496 -138.58192987669298, 29.263548302419252 -147.11779206048456, 0 -150, -29.263548302419295 -147.11779206048456, -57.40251485476355 -138.58192987669298, -83.33553495294032 -124.72044184538179, -106.06601717798215 -106.06601717798212, -124.72044184538181 -83.3355349529403, -138.58192987669304 -57.40251485476345, -147.11779206048456 -29.263548302419252, -150 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_ISOVIST4() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_Isovist('SRID=2154;POINT(0 0)'::GEOMETRY, 'SRID=4326;LINESTRING (100 0, 100 100, 0 100)'::GEOMETRY, "
                    + "150);");
        });
    }

    @Test
    public void test_ST_VariableBuffer1() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_VariableBuffer('POINT(0 0)'::GEOMETRY, 1, 12);");
        });
    }

    @Test
    public void test_ST_VariableBuffer2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_VariableBuffer('LINESTRING (200 200, 400 200)'::GEOMETRY, 10, 20);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((190 200, 190.1921471959677 201.95090322016128, 190.76120467488713 203.8268343236509, 191.68530387697456 205.55570233019603, 192.92893218813452 207.07106781186548, 194.44429766980397 208.31469612302544, 196.1731656763491 209.23879532511287, 198.04909677983872 209.8078528040323, 199.5 209.98749217771908, 399 219.97498435543818, 400 220, 403.90180644032256 219.61570560806462, 407.6536686473018 218.47759065022575, 411.11140466039205 216.6293922460509, 414.14213562373095 214.14213562373095, 416.6293922460509 211.11140466039205, 418.47759065022575 207.6536686473018, 419.6157056080646 203.90180644032256, 420 200, 419.6157056080646 196.09819355967744, 418.47759065022575 192.3463313526982, 416.6293922460509 188.88859533960795, 414.14213562373095 185.85786437626905, 411.11140466039205 183.3706077539491, 407.6536686473018 181.52240934977425, 403.90180644032256 180.38429439193538, 400 180, 399 180.02501564456182, 199.5 190.01250782228092, 198.04909677983872 190.1921471959677, 196.1731656763491 190.76120467488713, 194.44429766980397 191.68530387697456, 192.92893218813452 192.92893218813452, 191.68530387697456 194.44429766980397, 190.76120467488713 196.1731656763491, 190.1921471959677 198.04909677983872, 190 200))", rs.getObject(1));
    }

    @Test
    public void test_ST_SUBDIVIDE1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('POLYGON ((120 330, 162 330, 207 330, 258 330, 300 330, 300 277, 300 208, 300 150, 257 150, 196 150, 120 150, 120 198, 120 256, 120 330))'::GEOMETRY, "
                + "8);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((120 150, 120 198, 120 240, 210 240, 210 150, 196 150, 120 150)), ((210 150, 210 240, 300 240, 300 208, 300 150, 257 150, 210 150)), ((120 240, 120 256, 120 330, 162 330, 207 330, 210 330, 210 240, 120 240)), ((210 240, 210 330, 258 330, 300 330, 300 277, 300 240, 210 240)))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SUBDIVIDE2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('POLYGON ((100 300, 200 300, 200 200, 100 200, 100 300), \n" +
                "  (110 290, 153.5 290, 153.5 252.5, 110 252.5, 110 290))'::GEOMETRY, "
                + "8);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((100 200, 100 250, 150 250, 150 200, 100 200)), ((100 250, 100 300, 150 300, 150 290, 110 290, 110 252.5, 150 252.5, 150 250, 100 250)), ((150 200, 150 250, 200 250, 200 200, 150 200)), ((150 250, 150 252.5, 153.5 252.5, 153.5 290, 150 290, 150 300, 200 300, 200 250, 150 250)))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SUBDIVIDE3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('POLYGON((132 10,119 23,85 35,68 29,66 28,49 42,32 56,22 64,32 110,40 119,36 150,\n" +
                "57 158,75 171,92 182,114 184,132 186,146 178,176 184,179 162,184 141,190 122,\n" +
                "190 100,185 79,186 56,186 52,178 34,168 18,147 13,132 10))'::GEOMETRY, "
                + "10);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((22 64, 29.391304347826086 98, 106 98, 106 27.58823529411765, 85 35, 68 29, 66 28, 49 42, 32 56, 22 64)), \n" +
                        "  ((29.391304347826086 98, 32 110, 40 119, 36 150, 57 158, 75 171, 92 182, 106 183.27272727272728, 106 98, 29.391304347826086 98)), \n" +
                        "  ((106 27.58823529411765, 106 54, 147.76190476190476 54, 147.76190476190476 13.18140589569161, 147 13, 132 10, 119 23, 106 27.58823529411765)), \n" +
                        "  ((106 54, 106 98, 147.76190476190476 98, 147.76190476190476 54, 106 54)), \n" +
                        "  ((106 98, 106 142, 148 142, 148 98, 106 98)), \n" +
                        "  ((106 142, 106 183.27272727272728, 114 184, 132 186, 146 178, 148 178.4, 148 142, 106 142)), \n" +
                        "  ((147.76190476190476 13.18140589569161, 147.76190476190476 54, 186 54, 186 52, 178 34, 168 18, 147.76190476190476 13.18140589569161)), \n" +
                        "  ((147.76190476190476 54, 147.76190476190476 98, 189.52380952380952 98, 185 79, 186 56, 186 54, 147.76190476190476 54)), \n" +
                        "  ((148 98, 148 142, 183.76190476190476 142, 184 141, 190 122, 190 100, 189.52380952380952 98, 148 98)), \n" +
                        "  ((148 142, 148 178.4, 176 184, 179 162, 183.76190476190476 142, 148 142)))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SUBDIVIDE4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('POLYGON ((100 300, 200 300, 200 200, 100 200, 100 300), \n" +
                "  (110 290, 153.5 290, 153.5 252.5, 110 252.5, 110 290))'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((100 200, 100 250, 150 250, 150 200, 100 200)), ((100 250, 100 300, 150 300, 150 290, 110 290, 110 252.5, 150 252.5, 150 250, 100 250)), \n" +
                        "  ((150 200, 150 250, 200 250, 200 200, 150 200)),   ((150 250, 150 252.5, 153.5 252.5, 153.5 290, 150 290, 150 300, 200 300, 200 250, 150 250)))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SUBDIVIDE5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('LINESTRING (-200 200, 200 200)'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((-200 200, 0 200), (0 200, 200 200))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SUBDIVIDE6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('GEOMETRYCOLLECTION (LINESTRING (-200 200, 200 200)," +
                "  POINT (-103 273)," +
                "  POINT (-40 254))'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals("GEOMETRYCOLLECTION (POINT (-103 273), POINT (-40 254), LINESTRING (-200 200, 0 200), LINESTRING (0 200, 200 200))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SUBDIVIDE7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_subdivide('SRID=4326;LINESTRING (-200 200, 200 200)'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;MULTILINESTRING ((-200 200, 0 200), (0 200, 200 200))",
                rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_INTERSECTION_GRIDSIZE() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Intersection('LINESTRING (0 0, 0.9 1)', 'LINESTRING(0 1, 1 0)', 1e-04);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT (0.4737 0.5263)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_DIFFERENCE_GRIDSIZE() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Difference('LINESTRING (0 0, 0.9 1)', 'LINESTRING(0 1, 1 0)', 1e-04);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((0 0, 0.4737 0.5263), (0.4737 0.5263, 0.9 1))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_SYMDIFFERENCE_GRIDSIZE() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SymDifference('LINESTRING (0 0, 0.9 1)', 'LINESTRING(0 1, 1 0)', 1e-04);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((0 0, 0.4737 0.5263), (0.4737 0.5263, 0.9 1), (0 1, 0.4737 0.5263), (0.4737 0.5263, 1 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_UNION_COVERAGE() throws Exception {
        ResultSet rs = st.executeQuery("SELECT st_union(the_geom) FROM st_makegrid('POLYGON((0 0, 100 0, 100 100, 0 0 ))'::GEOMETRY, 100, 100);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CONCAVEHULL1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConcaveHull(" +
                "'MULTIPOINT ((10 72), (53 76), (56 66), (63 58), (71 51), (81 48), (91 46), (101 45), (111 46), (121 47), (131 50), (140 55), (145 64), (144 74), (135 80), (125 83), (115 85), (105 87), (95 89), (85 91), (75 93), (65 95), (55 98), (45 102), (37 107), (29 114), (22 122), (19 132), (18 142), (21 151), (27 160), (35 167), (44 172), (54 175), (64 178), (74 180), (84 181), (94 181), (104 181), (114 181), (124 181), (134 179), (144 177), (153 173), (162 168), (171 162), (177 154), (182 145), (184 135), (139 132), (136 142), (128 149), (119 153), (109 155), (99 155), (89 155), (79 153), (69 150), (61 144), (63 134), (72 128), (82 125), (92 123), (102 121), (112 119), (122 118), (132 116), (142 113), (151 110), (161 106), (170 102), (178 96), (185 88), (189 78), (190 68), (189 58), (185 49), (179 41), (171 34), (162 29), (153 25), (143 23), (133 21), (123 19), (113 19), (102 19), (92 19), (82 19), (72 21), (62 22), (52 25), (43 29), (33 34), (25 41), (19 49), (14 58), (21 73), (31 74), (42 74), (173 134), (161 134), (150 133), (97 104), (52 117), (157 156), (94 171), (112 106), (169 73), (58 165), (149 40), (70 33), (147 157), (48 153), (140 96), (47 129), (173 55), (144 86), (159 67), (150 146), (38 136), (111 170), (124 94), (26 59), (60 41), (71 162), (41 64), (88 110), (122 34), (151 97), (157 56), (39 146), (88 33), (159 45), (47 56), (138 40), (129 165), (33 48), (106 31), (169 147), (37 122), (71 109), (163 89), (37 156), (82 170), (180 72), (29 142), (46 41), (59 155), (124 106), (157 80), (175 82), (56 50), (62 116), (113 95), (144 167))'::GEOMETRY, 0.1) ;");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((10 72, 21 73, 31 74, 42 74, 53 76, 56 66, 63 58, 71 51, 81 48, 91 46, 101 45, 111 46, 121 47, 131 50, 140 55, 145 64, 144 74, 135 80, 125 83, 115 85, 105 87, 95 89, 85 91, 75 93, 65 95, 55 98, 45 102, 37 107, 29 114, 22 122, 19 132, 18 142, 21 151, 27 160, 35 167, 44 172, 54 175, 64 178, 74 180, 84 181, 94 181, 104 181, 114 181, 124 181, 134 179, 144 177, 153 173, 162 168, 171 162, 177 154, 182 145, 184 135, 173 134, 161 134, 150 133, 139 132, 136 142, 128 149, 119 153, 109 155, 99 155, 89 155, 79 153, 69 150, 61 144, 63 134, 72 128, 82 125, 92 123, 102 121, 112 119, 122 118, 132 116, 142 113, 151 110, 161 106, 170 102, 178 96, 185 88, 189 78, 190 68, 189 58, 185 49, 179 41, 171 34, 162 29, 153 25, 143 23, 133 21, 123 19, 113 19, 102 19, 92 19, 82 19, 72 21, 62 22, 52 25, 43 29, 33 34, 25 41, 19 49, 14 58, 10 72))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_CONCAVEHULL2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConcaveHull(" +
                "'MULTIPOINT ((132 64), (114 64), (99 64), (81 64), (63 64), (57 49), (52 36), (46 20), (37 20), (26 20), (32 36), (39 55), (43 69), (50 84), (57 100), (63 118), (68 133), (74 149), (81 164), (88 180), (101 180), (112 180), (119 164), (126 149), (132 131), (139 113), (143 100), (150 84), (157 69), (163 51), (168 36), (174 20), (163 20), (150 20), (143 36), (139 49), (132 64), (99 151), (92 138), (88 124), (81 109), (74 93), (70 82), (83 82), (99 82), (112 82), (126 82), (121 96), (114 109), (110 122), (103 138), (99 151), (34 27), (43 31), (48 44), (46 58), (52 73), (63 73), (61 84), (72 71), (90 69), (101 76), (123 71), (141 62), (166 27), (150 33), (159 36), (146 44), (154 53), (152 62), (146 73), (134 76), (143 82), (141 91), (130 98), (126 104), (132 113), (128 127), (117 122), (112 133), (119 144), (108 147), (119 153), (110 171), (103 164), (92 171), (86 160), (88 142), (79 140), (72 124), (83 131), (79 118), (68 113), (63 102), (68 93), (35 45))'::GEOMETRY, 0.15, true) ;");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((26 20, 32 36, 35 45, 39 55, 43 69, 50 84, 57 100, 63 118, 68 133, 74 149, 81 164, 88 180, 101 180, 112 180, 119 164, 126 149, 132 131, 139 113, 143 100, 150 84, 157 69, 163 51, 168 36, 174 20, 163 20, 150 20, 143 36, 139 49, 132 64, 114 64, 99 64, 81 64, 63 64, 57 49, 52 36, 46 20, 37 20, 26 20), (74 93, 83 82, 99 82, 112 82, 121 96, 114 109, 110 122, 103 138, 92 138, 88 124, 81 109, 74 93))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_M1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_M('POINT(0 0)') ;");
        assertTrue(rs.next());
        assertNull(rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_M2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_M('POINTM(0 0 2)') ;");
        assertTrue(rs.next());
        assertEquals(2.0d,rs.getDouble(1));
        rs.close();
    }

    @Test
    public void test_ST_M3() throws Exception {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            try {
                st.executeQuery("SELECT ST_M('LINESTRING (-0.9 3.6, 1.7 3.3)') ;");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
}
