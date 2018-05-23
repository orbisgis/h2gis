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

package org.h2gis.functions.spatial;


import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static junit.framework.Assert.assertTrue;

import org.locationtech.jts.geom.Geometry;

import org.h2.jdbc.JdbcSQLException;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Erwan Bocher
 */
public class SpatialFunction2Test {

    private static Connection connection;
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(SpatialFunction2Test.class.getSimpleName());
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
    public void test_ST_SunPosition() throws Exception {
        //Test based on http://www.esrl.noaa.gov/gmd/grad/solcalc/
        ResultSet rs = st.executeQuery("SELECT ST_SunPosition('POINT (139.74 35.65)'::GEOMETRY, '2015-1-25 21:49:26+01:00');");
        assertTrue(rs.next());
        Geometry point = (Geometry) rs.getObject(1);
        Assert.assertEquals(104.99439384398441, Math.toDegrees(point.getCoordinate().x), 10E-1);
        Assert.assertEquals(-11.642433433992824, Math.toDegrees(point.getCoordinate().y), 10E-1);
        rs.close();
    }

    @Test
    public void test_ST_Shadow() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10)'::GEOMETRY, "
                + "radians(90),radians(45), 2);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((10 5 0, 10 10 0, 8 10 0, 8 5 0, 10 5 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10)'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((10 5 0, 10 10 0, 12 10 0, 12 5 0, 10 5 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POINT (10 5)'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (10 5 0, 12 5 0)", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10, 12 10, 12 11.5)'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((10 5 0, 10 10 0, 12 10 0, 12 5 0, 10 5 0)),"
                + "  ((12 10 0, 12 11.5 0, 14 11.5 0, 14 10 0, 12 10 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10, 10 5, 8 5, 8 10, 10 10))'::GEOMETRY, "
                + "radians(270),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((10 5 0, 10 10 0, 12 10 0, 12 5 0, 10 5 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10 1, 10 5, 8 5 10, 8 10, 10 10))'::GEOMETRY, "
                + "radians(225),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((8 10 0, 9.414213562373096 11.414213562373096 0, 11.414213562373096 11.414213562373096 0, 11.414213562373096 6.414213562373096 0, 10 5 0, 10 6.414213562373096 0, 10 10 0, 9.414213562373096 10 0, 8 10 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10, 10 5, 8 5, 8 10, 10 10),"
                + "  (8.5 8, 9.5 8, 9.5 6, 8.5 6, 8.5 8))'::GEOMETRY, "
                + "radians(270),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((10 5 0, 10 10 0, 10.5 10 0, 10.5 5 0, 10 5 0)), ((8.5 6 0, 8.5 8 0, 9 8 0, 9 6 0, 8.5 6 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((6 12, 6 9, 8 9, 8 8.1, 10.9 8.1, 10.9 9, 10 9, 10 11, 9 11, 9 9, 8.5 9, 8.5 12, 6 12), (6.7 11.1, 7.6 11.1, 7.6 10.3, 6.7 10.3, 6.7 11.1))'::GEOMETRY, "
                + "radians(270),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((10.9 8.1 0, 10.9 9 0, 11.4 9 0, 11.4 8.1 0, 10.9 8.1 0)), ((10 9 0, 10 11 0, 10.5 11 0, 10.5 9 0, 10 9 0)), ((8.5 9 0, 8.5 12 0, 9 12 0, 9 11 0, 9 9 0, 8.5 9 0)), ((6.7 10.3 0, 6.7 11.1 0, 7.2 11.1 0, 7.2 10.3 0, 6.7 10.3 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((6 12, 6 9, 8 9, 8 8.1, 10.9 8.1, 10.9 9, 10 9, 10 11, 9 11, 9 9, 8.5 9, 8.5 12, 6 12), (6.7 11.1, 7.6 11.1, 7.6 10.3, 6.7 10.3, 6.7 11.1))'::GEOMETRY, "
                + "radians(315),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((8 8.1 0, 8.353553390593273 8.1 0, 10.9 8.1 0, 10.9 8.646446609406727 0, 10.9 9 0, 11.253553390593273 8.646446609406727 0, 11.253553390593273 7.746446609406726 0, 8.353553390593273 7.746446609406726 0, 8 8.1 0)), ((10 9 0, 10 10.646446609406727 0, 10 11 0, 10.353553390593273 10.646446609406727 0, 10.353553390593273 9 0, 10 9 0)), ((8.5 9 0, 8.5 11.646446609406727 0, 8.5 12 0, 8.853553390593273 11.646446609406727 0, 8.853553390593273 9 0, 8.5 9 0)), ((6 9 0, 6.353553390593274 9 0, 8 9 0, 8 8.646446609406727 0, 6.353553390593274 8.646446609406727 0, 6 9 0)), ((6.7 10.3 0, 6.7 11.1 0, 7.6 11.1 0, 7.6 10.746446609406727 0, 7.053553390593274 10.746446609406727 0, 7.053553390593274 10.3 0, 6.7 10.3 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((354610.6 6689345.5, 354626.2 6689342.7, 354627.2 6689342.2, 354639.5 6689334.4, 354640.3 6689333, 354640.6 6689331.4, 354640.2 6689330, 354639.6 6689328.6, 354634 6689322.2, 354633.1 6689321.1, 354628 6689314.4, 354626.7 6689313.5, 354625.4 6689313.4, 354622.4 6689313.7, 354621 6689314.4, 354608.7 6689341.9, 354610.6 6689345.5), (354624.2 6689335.3, 354623.8 6689333.7, 354622.1 6689329, 354625.1 6689322.1, 354628.7 6689326.5, 354632.4 6689331, 354624.2 6689335.3))'::GEOMETRY, "
                + "radians(270),radians(45), 2);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((354610.6 6689345.5 0, 354612.6 6689345.5 0, 354628.2 6689342.7 0, 354629.2 6689342.2 0, 354641.5 6689334.4 0, 354642.3 6689333 0, 354642.6 6689331.4 0, 354642.2 6689330 0, 354641.6 6689328.6 0, 354636 6689322.2 0, 354635.1 6689321.1 0, 354630 6689314.4 0, 354628.7 6689313.5 0, 354627.4 6689313.4 0, 354625.4 6689313.4 0, 354626.7 6689313.5 0, 354628 6689314.4 0, 354633.1 6689321.1 0, 354634 6689322.2 0, 354639.6 6689328.6 0, 354640.2 6689330 0, 354640.6 6689331.4 0, 354640.3 6689333 0, 354639.5 6689334.4 0, 354627.2 6689342.2 0, 354626.2 6689342.7 0, 354610.6 6689345.5 0)), ((354622.1 6689329 0, 354623.8 6689333.7 0, 354624.2 6689335.3 0, 354625.96819407004 6689334.37277628 0, 354625.8 6689333.7 0, 354624.1 6689329 0, 354626.40599369083 6689323.696214511 0, 354625.1 6689322.1 0, 354622.1 6689329 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((354610.6 6689345.5, 354626.2 6689342.7, 354627.2 6689342.2, 354639.5 6689334.4, 354640.3 6689333, 354640.6 6689331.4, 354640.2 6689330, 354639.6 6689328.6, 354634 6689322.2, 354633.1 6689321.1, 354628 6689314.4, 354626.7 6689313.5, 354625.4 6689313.4, 354622.4 6689313.7, 354621 6689314.4, 354608.7 6689341.9, 354610.6 6689345.5), (354624.2 6689335.3, 354623.8 6689333.7, 354622.1 6689329, 354625.1 6689322.1, 354628.7 6689326.5, 354632.4 6689331, 354624.2 6689335.3))'::GEOMETRY, "
                + "radians(270),radians(45), 2, false);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((354610.6 6689345.5, 354626.2 6689342.7, 354628.2 6689342.7 0, 354612.6 6689345.5 0, 354610.6 6689345.5)), ((354626.2 6689342.7, 354627.2 6689342.2, 354629.2 6689342.2 0, 354628.2 6689342.7 0, 354626.2 6689342.7)), ((354627.2 6689342.2, 354639.5 6689334.4, 354641.5 6689334.4 0, 354629.2 6689342.2 0, 354627.2 6689342.2)), ((354639.5 6689334.4, 354640.3 6689333, 354642.3 6689333 0, 354641.5 6689334.4 0, 354639.5 6689334.4)), ((354640.3 6689333, 354640.6 6689331.4, 354642.6 6689331.4 0, 354642.3 6689333 0, 354640.3 6689333)), ((354640.6 6689331.4, 354640.2 6689330, 354642.2 6689330 0, 354642.6 6689331.4 0, 354640.6 6689331.4)), ((354640.2 6689330, 354639.6 6689328.6, 354641.6 6689328.6 0, 354642.2 6689330 0, 354640.2 6689330)), ((354639.6 6689328.6, 354634 6689322.2, 354636 6689322.2 0, 354641.6 6689328.6 0, 354639.6 6689328.6)), ((354634 6689322.2, 354633.1 6689321.1, 354635.1 6689321.1 0, 354636 6689322.2 0, 354634 6689322.2)), ((354633.1 6689321.1, 354628 6689314.4, 354630 6689314.4 0, 354635.1 6689321.1 0, 354633.1 6689321.1)), ((354628 6689314.4, 354626.7 6689313.5, 354628.7 6689313.5 0, 354630 6689314.4 0, 354628 6689314.4)), ((354626.7 6689313.5, 354625.4 6689313.4, 354627.4 6689313.4 0, 354628.7 6689313.5 0, 354626.7 6689313.5)), ((354625.4 6689313.4, 354622.4 6689313.7, 354624.4 6689313.7 0, 354627.4 6689313.4 0, 354625.4 6689313.4)), ((354622.4 6689313.7, 354621 6689314.4, 354623 6689314.4 0, 354624.4 6689313.7 0, 354622.4 6689313.7)), ((354621 6689314.4, 354608.7 6689341.9, 354610.7 6689341.9 0, 354623 6689314.4 0, 354621 6689314.4)), ((354608.7 6689341.9, 354610.6 6689345.5, 354612.6 6689345.5 0, 354610.7 6689341.9 0, 354608.7 6689341.9)), ((354624.2 6689335.3, 354623.8 6689333.7, 354625.8 6689333.7 0, 354626.2 6689335.3 0, 354624.2 6689335.3)), ((354623.8 6689333.7, 354622.1 6689329, 354624.1 6689329 0, 354625.8 6689333.7 0, 354623.8 6689333.7)), ((354622.1 6689329, 354625.1 6689322.1, 354627.1 6689322.1 0, 354624.1 6689329 0, 354622.1 6689329)), ((354625.1 6689322.1, 354628.7 6689326.5, 354630.7 6689326.5 0, 354627.1 6689322.1 0, 354625.1 6689322.1)), ((354628.7 6689326.5, 354632.4 6689331, 354634.4 6689331 0, 354630.7 6689326.5 0, 354628.7 6689326.5)), ((354632.4 6689331, 354624.2 6689335.3, 354626.2 6689335.3 0, 354634.4 6689331 0, 354632.4 6689331)))", rs.getBytes(1));
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
        ResultSet rs = st.executeQuery("SELECT ST_FlipCoordinates('LINESTRING (10 5 0, 12 5 0)');");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (5 10 0, 5 12 0)", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_MaxDistance1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'LINESTRING ( 2 0, 0 2 )'::geometry)");
        rs.next();
        Assert.assertEquals(2, rs.getDouble(1), 0);
        rs.close();
    }
    
    @Test
    public void test_ST_MaxDistance2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'LINESTRING ( 2 2, 2 2 )'::geometry)");
        rs.next();
        Assert.assertEquals(2.8284, rs.getDouble(1), 0.001);
        rs.close();
    }
    
    @Test
    public void test_ST_MaxDistance3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('LINESTRING ( 0 0, 2 2, 10 0 )'::geometry, 'LINESTRING ( 0 0, 2 2, 10 0 )'::geometry)");
        rs.next();
        Assert.assertEquals(10, rs.getDouble(1), 0);
        rs.close();
    }
    
    
    @Test
    public void test_ST_MaxDistance4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'POINT(0 10)'::geometry)");
        rs.next();
        Assert.assertEquals(10, rs.getDouble(1), 0);
        rs.close();
    }
    
    @Test
    public void test_ST_MaxDistance5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance('POINT(0 0)'::geometry, 'POINT(0 10)'::geometry)");
        rs.next();
        Assert.assertEquals(10, rs.getDouble(1), 0);
        rs.close();
    }
    
    @Test
    public void test_ST_MaxDistance6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaxDistance(ST_BUFFER('POINT(0 0)'::geometry, 10), ST_BUFFER('POINT(0 0)'::geometry, 10))");
        rs.next();
        Assert.assertEquals(20, rs.getDouble(1), 0);
        rs.close();
    }
    
    @Test
    public void test_ST_LongestLine1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('POINT(0 0)'::geometry, 'POINT(0 0)'::geometry)");
        rs.next();
        Assert.assertNull(rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_LongestLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LongestLine('MULTIPOINT((0 0),(0 0))'::geometry, 'MULTIPOINT((0 0),(0 0))'::geometry)");
        rs.next();
        Assert.assertNull(rs.getBytes(1));
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
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON((1 1 0, 3 1 0, 3 2 0, 1 2 0, 1 1 0))'::GEOMETRY)");
        rs.next();
        assertEquals(2, rs.getDouble(1), 1e-3);
        rs.close();
    }
    
    @Test
    public void test_ST_3DArea3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON((1 1 1, 3 1 1, 3 2 1, 1 2 1, 1 1 1))'::GEOMETRY)");
        rs.next();
        assertEquals(2, rs.getDouble(1), 1e-3);
        rs.close();
    }
    
    @Test
    public void test_ST_3DArea4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON ((0 0 0, 10 0 0, 0 10 0,0 0 0))'::GEOMETRY)");
        rs.next();
        assertEquals(50, rs.getDouble(1), 1e-1);
        rs.close();
    }
    
    @Test
    public void test_ST_3DArea5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON ((0 0 0, 10 0 10, 0 10 0,0 0 0))'::GEOMETRY)");
        rs.next();
        assertEquals(70.711, rs.getDouble(1), 1e-3);
        rs.close();
    }
    
    @Test
    public void test_ST_3DArea6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON ((1 1 0, 2 1 1, 3 1 0, 3 2 0, 2 2 1, 1 2 0, 1 1 0))'::GEOMETRY)");
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
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('POLYGON ((1 1 0, 2 1 1, 3 1 0, 3 2 0, 2 2 1, 1 2 0, 1 1 0),"
                + "  (1.2 1.8 0, 1.7 1.8 0, 1.7 1.5 0, 1.2 1.5 0, 1.2 1.8 0),"
                + "  (2.2 1.5 0, 2.8 1.5 0, 2.8 1.2 0, 2.2 1.2 0, 2.2 1.5 0))'::GEOMETRY)");
        rs.next();
        assertEquals(3.256, rs.getDouble(1), 1e-3);
        rs.close();
    }
    
    @Test
    public void test_ST_3DArea9() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('MULTIPOLYGON(((0 0 0, 10 0 10, 0 10 0,0 0 0)),  ((0 0 0, 10 0 10, 0 10 0,0 0 0)))');");
        rs.next();
        assertEquals(141.422, rs.getDouble(1), 1e-3);
        rs.close();
    }
    
    @Test
    public void test_ST_3DArea10() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_3DArea('GEOMETRYCOLLECTION("
                + "MULTIPOINT((4 4), (1 1), (1 0), (0 3)),"
                + "LINESTRING(2 1, 1 3, 5 2),"
                + "POLYGON((0 0 0, 10 0 10, 0 10 0,0 0 0)))');");
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
                + "(ST_GeomFromText('LINESTRING(1 4 3, 15 7 9, 16 17 22)',2249)),"
                + "(ST_GeomFromText('MULTILINESTRING((1 4 3, 15 7 9, 16 17 22),"
                + "(0 0 0, 1 0 0, 1 2 0, 0 2 1))',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1, 3 1, 3 2, 1 2, 1 1))',2249)),"
                + "(ST_GeomFromText('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))',2249)),"
                + "(ST_GeomFromText('POLYGON ((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1),"
                + "  (1.235 1.83, 1.825 1.83, 1.825 1.605, 1.235 1.605, 1.235 1.83),"
                + "  (2.235 1.63, 2.635 1.63, 2.635 1.155, 2.235 1.155, 2.235 1.63))',2249)),"
                + "(ST_GeomFromText('GEOMETRYCOLLECTION(LINESTRING(1 4 3, 15 7 9, 16 17 22),"
                + "POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))',2249));");
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
        assertGeometryEquals("LINESTRING (-71.16028 42.258729, -71.160837 42.259112, -71.161143 42.25932)", rs.getString(1));
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
    
    @Test(expected = IllegalArgumentException.class)
    public void test_ST_GoogleMapLink3() throws Throwable {
        try {
            st.execute("SELECT ST_GoogleMapLink('POINT(-2.070365 47.643713)'::GEOMETRY, 'dsp');");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
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
                "SELECT ST_NPoints('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))'::GEOMETRY);");
        rs.next();
        assertEquals(5, rs.getInt(1));
        rs.close();
    }
    
    @Test
    public void test_ST_NumInteriorRings1() throws Exception {
        ResultSet rs = st.executeQuery(
                "SELECT ST_NumInteriorRings('POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1))'::GEOMETRY);");
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
    public void test_ST_MakeValid1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POINT(0 0)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POINT(0 0)", rs.getString(1));
        rs.close();
    }
        
    
    @Test
    public void test_ST_MakeValid4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POINT(0 1 2)'::GEOMETRY);");
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
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('LINESTRING(0 0 1, 10 0 2, 20 0 1, 20 0 1, 30 0)'::GEOMETRY, true,false);");
        rs.next();
        assertGeometryEquals("LINESTRING(0 0 1, 10 0 2, 20 0 1, 30 0)", rs.getString(1));
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
        ResultSet rs = st.executeQuery("SELECT ST_MakeValid('POLYGON ((353851 7684917, 353851 7684918 136.1, 353853 7684918, 353852 7684918 135.6, 353851 7684917))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((353851 7684917, 353851 7684918 136.1, 353852 7684918 135.6, 353851 7684917))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_DistanceSpherePointToPointEpsg4326() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(0 0)'::GEOMETRY, 'POINT(-118 38)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(12421874.764417205, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointToPointEpsg4008() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere(ST_SetSRID('POINT(0 0)'::GEOMETRY, 4008), ST_SetSRID('POINT(-118 38)'::GEOMETRY, 4008))");
        Assert.assertTrue(rs.next());
        assertEquals(12421855.452633386, rs.getDouble(1),1e-12);
    }

    @Test(expected = SQLException.class)
    public void test_ST_DistanceSpherePointToPointEpsg2375() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere(ST_SetSRID('POINT(0 0)'::GEOMETRY, 2375), ST_SetSRID('POINT(-118 38)'::GEOMETRY, 2375))");
        Assert.assertTrue(rs.next());
    }

    @Test
    public void test_ST_DistanceSpherePointToLineString() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(0 0)'::GEOMETRY, 'LINESTRING (10 5, 10 10)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(1241932.5985221416, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSphereLineStringToPoint() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('LINESTRING (10 5, 10 10)'::GEOMETRY, 'POINT(0 0)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(1241932.5985221416, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSphereLineStringToLineString() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('LINESTRING (10 5, 10 10)'::GEOMETRY, 'LINESTRING(0 0, 1 1)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(1093701.742472634, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointToPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(0 0)'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(157249.5977685051, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToPoint() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY, 'POINT(0 0)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(157249.5977685051, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointInPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT(1 1)'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSphereLineStringToPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('LINESTRING (100 50, 50 50)'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(5763657.991914633, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToLineString() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY, 'LINESTRING (100 50, 50 50)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(5763657.991914633, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToLineStringDistanceZero() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY, 'LINESTRING (1 10, 10 10)'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePolygonToPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POLYGON ((130 390, 280 390, 280 210, 130 210, 130 390))'::GEOMETRY, 'POLYGON((1 1,10 0,10 10,0 10,1 1),(5 5,7 5,7 7,5 7, 5 5))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(8496739.27764427, rs.getDouble(1),1e-12);
    }

    @Test
    public void test_ST_DistanceSpherePointToMultiPolygon() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_DistanceSphere('POINT (130 210)'::GEOMETRY, 'MULTIPOLYGON (((50 400, 50 350, 50 300, 70 290, 80 280, 90 260, 99 254, 109 253, 120 252, 130 252, 140 255, 150 259, 159 264, 168 270, 172 280, 174 290, 175 300, 173 310, 167 320, 158 325, 148 325, 138 323, 129 316, 124 306, 114 312, 108 321, 105 332, 106 342, 112 351, 121 357, 132 359, 143 359, 153 359, 163 360, 173 359, 183 357, 193 355, 203 355, 216 355, 227 355, 238 357, 247 362, 256 369, 264 378, 269 387, 275 396, 273 406, 267 414, 259 421, 250 426, 240 426, 230 426, 220 425, 210 420, 199 418, 189 419, 179 419, 169 419, 158 419, 148 419, 138 419, 126 419, 116 419, 101 419, 83 419, 70 421, 60 427, 52 433, 44 441, 40 440, 50 400),(80 400, 100 400, 100 390, 80 390, 80 400),(177 395, 200 395, 200 380, 177 380, 177 395), (74 344, 80 344, 80 310, 74 310, 74 344)))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertEquals(1074360.2834168628, rs.getDouble(1),1e-12);
    }
    
    @Test
    public void test_ST_Node1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Node('MULTILINESTRING ((100 300, 200 200), (100 200, 200 300))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((100 300, 150 250), (150 250, 200 200), (100 200, 150 250),(150 250, 200 300))" , rs.getObject(1));
    }
    
    @Test
    public void test_ST_Node2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Node('MULTIPOLYGON (((100 200, 200 200, 200 100, 100 100, 100 200)), ((151 225, 300 225, 300 70, 151 70, 151 225)))'::GEOMETRY)");
        Assert.assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((100 200, 151 200), (151 200, 200 200, 200 100, 151 100), (151 100, 100 100, 100 200), (151 225, 300 225, 300 70, 151 70, 151 100), (151 100, 151 200), (151 200, 151 225))" , rs.getObject(1));
    }
    
    @Test
    public void test_ST_SVF1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf(null, null, 1,0)");
        Assert.assertTrue(rs.next());
        assertEquals(-1, rs.getDouble(1), 0);
    }
    
    @Test
    public void test_ST_SVF2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINT(0 0 0)'::GEOMETRY, 'POLYGON ((10 -1 10, 20 -1 10, 20 20 10, 10 20 10, 10 -1 10))'::GEOMETRY, 50, 8) as result");
        Assert.assertTrue(rs.next());
        double dTheta = Math.toRadians(45);
        double sinGamma0 = Math.sin(Math.atan(10/Math.sqrt(200)));
        double sinGamma1 = Math.sin(Math.atan(1));
        double svfTest = 1-(dTheta*(sinGamma0*sinGamma0 + sinGamma1*sinGamma1))/(2*Math.PI);
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }
    
    @Test
    public void test_ST_SVF3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINT(0 0 0)'::GEOMETRY, ST_UPDATEZ(ST_buffer('POINT(0 0)'::GEOMETRY, 10, 120), 12), 50, 8) as result");
        Assert.assertTrue(rs.next());
        double svfTest = 0.4098;
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }
    
    @Test
    public void test_ST_SVF4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINT(0 0 0)'::GEOMETRY, 'MULTILINESTRING((-10 -1000 12, -10 1000 12), (10 -1000 12, 10 1000 12))'::GEOMETRY, 100, 120) as result");
        Assert.assertTrue(rs.next());
        double svfTest = 0.6402;
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }
    
    @Test
    public void test_ST_SVF5() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_svf('POINT(0 0 0)'::GEOMETRY, 'MULTIPOLYGON(((10 -5 2, 10 5 2, 15 5 2, 15 -5 2, 10 -5 2)), ((15 -5 20, 15 5 20, 20 5 20, 20 -5 20, 15 -5 20)))'::GEOMETRY, 100, 8) as result");
        Assert.assertTrue(rs.next());
        double dTheta = Math.toRadians(45);
        double sinGamma = Math.sin(Math.atan2(20, 15));
        double svfTest = 1 - (dTheta * sinGamma * sinGamma) / (2 * Math.PI);
        assertEquals(svfTest, rs.getDouble(1), 0.01);
    }
    
    @Test
    public void test_ST_ShortestLine1() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT st_length(ST_ShortestLine('POINT(0 0 0)'::GEOMETRY, 'LINESTRING(0 0, 10 10)'::GEOMETRY)) as result");
        Assert.assertTrue(rs.next());
        assertEquals(0, rs.getDouble(1), 0.1);
    }
    
    @Test
    public void test_ST_ShortestLine2() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ShortestLine('POINT(10 0 )'::GEOMETRY, 'LINESTRING(5 5, 10 10)'::GEOMETRY) as result");
        Assert.assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (10 0, 5 5)", rs.getObject(1));
    }
    
    @Test
    public void test_ST_ShortestLine3() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ShortestLine('POINT(10 0 )'::GEOMETRY, 'LINESTRING(1 10, 5 5, 10 10)'::GEOMETRY) as result");
        Assert.assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (10 0, 5 5)", rs.getObject(1));
    }
    
    @Test
    public void test_ST_ShortestLine4() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ShortestLine('POLYGON ((0 4, 8 4, 8 0, 0 0, 0 4))'::GEOMETRY, 'POLYGON ((2 10.1, 9 10.1, 9 4.9, 2 4.9, 2 10.1))'::GEOMETRY) as result");
        Assert.assertTrue(rs.next());
        assertGeometryEquals("LINESTRING (2 4, 2 4.9)", rs.getObject(1));
    }
}
