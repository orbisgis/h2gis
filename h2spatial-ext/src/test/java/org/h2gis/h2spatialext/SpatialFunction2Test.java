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

import com.vividsolutions.jts.geom.Geometry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class SpatialFunction2Test {

    private static Connection connection;
    private Statement st;
    private static final String DB_NAME = "SpatialFunction2Test";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, false);
        CreateSpatialExtension.initSpatialExtension(connection);
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
        ResultSet rs = st.executeQuery("SELECT ST_SunPosition('POINT (139.74 35.65)'::GEOMETRY, '2015-1-25 21:49:26');");
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
        assertGeometryEquals("POLYGON ((10 10 0, 12 10 0, 12 5 0, 10 5 0, 10 10 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10 1, 10 5, 8 5 10, 8 10, 10 10))'::GEOMETRY, "
                + "radians(225),radians(45), 2 );");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((8 10 0, 9.414213562373096 11.414213562373096 0, 11.414213562373096 11.414213562373096 0, 11.414213562373096 6.414213562373096 0, 10 5 0, 10 10 0, 8 10 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_Shadow6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((10 10, 10 5, 8 5, 8 10, 10 10),"
                + "  (8.5 8, 9.5 8, 9.5 6, 8.5 6, 8.5 8))'::GEOMETRY, "
                + "radians(270),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((10 10 0, 10.5 10 0, 10.5 5 0, 10 5 0, 10 10 0)), ((9 8 0, 9 6 0, 8.5 6 0, 8.5 8 0, 9 8 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow7() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((6 12, 6 9, 8 9, 8 8.1, 10.9 8.1, 10.9 9, 10 9, 10 11, 9 11, 9 9, 8.5 9, 8.5 12, 6 12), (6.7 11.1, 7.6 11.1, 7.6 10.3, 6.7 10.3, 6.7 11.1))'::GEOMETRY, "
                + "radians(270),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((8.5 12 0, 9 12 0,9 11 0, 9 9 0, 8.5 9 0, 8.5 12 0)),"
                + "  ((10 11 0, 10.5 11 0, 10.5 9 0, 10 9 0, 10 11 0)),"
                + "  ((10.9 9 0, 11.4 9 0, 11.4 8.1 0, 10.9 8.1 0, 10.9 9 0)),"
                + "  ((7.2 11.1 0, 7.2 10.3 0, 6.7 10.3 0, 6.7 11.1 0, 7.2 11.1 0)))", rs.getBytes(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow8() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POLYGON ((6 12, 6 9, 8 9, 8 8.1, 10.9 8.1, 10.9 9, 10 9, 10 11, 9 11, 9 9, 8.5 9, 8.5 12, 6 12), (6.7 11.1, 7.6 11.1, 7.6 10.3, 6.7 10.3, 6.7 11.1))'::GEOMETRY, "
                + "radians(315),radians(45), 0.5);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON (((10 11 0, 10.353553390593273 10.646446609406727 0, 10.353553390593273 9 0, 10 9 0, 10 11 0)), ((10.9 9 0, 11.253553390593273 8.646446609406727 0, 11.253553390593273 7.746446609406726 0, 8.353553390593273 7.746446609406726 0, 8 8.1 0, 10.9 8.1 0, 10.9 9 0)), ((8 8.646446609406727 0, 6.353553390593274 8.646446609406727 0, 6 9 0, 8 9 0, 8 8.646446609406727 0)), ((8.5 12 0, 8.853553390593273 11.646446609406727 0, 8.853553390593273 9 0, 8.5 9 0, 8.5 12 0)), ((7.6 10.746446609406727 0, 7.053553390593274 10.746446609406727 0, 7.053553390593274 10.3 0, 6.7 10.3 0, 6.7 11.1 0, 7.6 11.1 0, 7.6 10.746446609406727 0)))", rs.getBytes(1));
        rs.close();
    }
}
