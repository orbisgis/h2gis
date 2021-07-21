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

package org.h2gis.functions.spatial.topography;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Fortin
 */
public class TopographyTest {
    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(TopographyTest.class.getSimpleName());
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

    @Test
    public void test_ST_TriangleAspect3() {
        assertThrows(SQLException.class, ()-> {
            ResultSet rs = st.executeQuery("SELECT ST_TriangleAspect('POLYGON ((0 0 , 10 0 0, 0 10 1, 0 0 1))'::GEOMETRY);");
            rs.close();
        });
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
        assertEquals(0, rs.getDouble(1), 1e-12);
        assertEquals(90, rs.getDouble(2), 1e-12);
        assertEquals(0, rs.getDouble(3), 1e-12);
        assertEquals(135, rs.getDouble(4), 1e-12);
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
        assertEquals(127.27, rs.getDouble(1), 10E-2);
        rs.close();
    }

    /**
     * 10% slope. 10m down after 100m distance.
     * @throws Exception
     */
    @Test
    public void test_ST_TriangleSlope3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleSlope('POLYGON ((0 0 100, 10 0 100, 5 100 90, 0 0 100))'::GEOMETRY);");
        rs.next();
        assertEquals(10, rs.getDouble(1), 10E-2);
        rs.close();
    }

    /**
     * 200% slope.
     * @throws Exception
     */
    @Test
    public void test_ST_TriangleSlope4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleSlope('POLYGON((0 0 -5, 4 0 -5, 2 3 1, 0 0 -5))');");
        rs.next();
        assertEquals(200, rs.getDouble(1), 10E-2);
        rs.close();
    }

    @Test
    public void test_ST_TriangleDirection1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON Z((0 0 0, 2 0 0, 1 1 0, 0 0 0))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).isEmpty());
        rs.close();
    }

    @Test
    public void test_ST_TriangleDirection2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON Z((0 0 0, 4 0 0, 2 3 9, 0 0 0))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING Z(2 1 3, 2 0 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_TriangleDirection3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON Z((0 0 100, 10 0 100, 5 100 90, 0 0 100))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("LINESTRING Z(5 33.33 96.66, 5 100 90)", rs.getObject(1), 0.01);
        rs.close();
    }


    @Test
    public void test_ST_TriangleDirection4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON Z((182966.69179438584 2428143.025232138 70, 183059.9584658498 2428116.2361122346 65, 183056.0723545388 2428151.2111140336 65, 182966.69179438584 2428143.025232138 70))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("LINESTRING Z(183027.57 2428136.82 66.67, 183057.30 2428140.13 65)", rs.getObject(1), 0.01);
        rs.close();
    }



    @Test
    public void test_ST_TriangleDirection5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_TriangleDirection('POLYGON Z((184994.93499517522 2428907.874116194" +
                " 65, 184992.64696198824 2428864.401485642 60, 185015.52729385791 2428882.7057511373 65," +
                " 184994.93499517522 2428907.874116194 65))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("LINESTRING Z(185001.03641700713 2428884.9937843247 63.33333333336688," +
                " 184993.40201293994 2428878.7474537245 61.65000000004103)", rs.getObject(1), 0.01);
        rs.close();
    }

    @Test
    public void testST_TriangleContouringWithZ() throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS TIN");
            st.execute("CREATE TABLE TIN AS SELECT 'POLYGON ((-9.19 3.7 1, 0.3 1.41 4.4, -5.7 -4.15 4, -9.19 3.7 1))'::geometry the_geom");
            ResultSet rs = st.executeQuery("select * from ST_TriangleContouring('TIN', 2,3,4,5) order by idiso");
            assertEquals(2, rs.getMetaData().getColumnCount());
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.4 3.03 2, -9.19 3.7 1, -8.02 1.09 2, -6.4 3.03 2))", rs.getObject(1));
            assertEquals(0, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.05 -0.56 3, -6.4 3.03 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.4 3.03 2, -8.02 1.09 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.05 -0.56 3, -3.61 2.35 3, -6.4 3.03 2, -6.05 -0.56 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -5.7 -4.15 4, -6.05 -0.56 3, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -0.82 1.68 4, -6.05 -0.56 3, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-0.82 1.68 4, -3.61 2.35 3, -6.05 -0.56 3, -0.82 1.68 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -6.05 -0.56 3, -5.7 -4.15 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, 0.3 1.41 4.4, -0.82 1.68 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-5.7 -4.15 4, 0.3 1.41 4.4, -1.52 0.85 4, -5.7 -4.15 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertFalse(rs.next());
        } finally {
            st.close();
        }
    }



    @Test
    public void testST_TriangleContouringWithColumns() throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS TIN");
            st.execute("CREATE TABLE TIN AS SELECT 'POLYGON ((-9.19 3.7 1, 0.3 1.41 4.4, -5.7 -4.15 4, -9.19 3.7 1))'::geometry the_geom, 1.0 as m1, 4.4 as m2, 4.0 as m3");
            ResultSet rs = st.executeQuery("select * from ST_TriangleContouring('TIN','m1','m2','m3',2,3,4,5) order by idiso");
            assertEquals(5, rs.getMetaData().getColumnCount());
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.4 3.03 2, -9.19 3.7 1, -8.02 1.09 2, -6.4 3.03 2))", rs.getObject(1));
            assertEquals(0, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.05 -0.56 3, -6.4 3.03 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.4 3.03 2, -8.02 1.09 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.05 -0.56 3, -3.61 2.35 3, -6.4 3.03 2, -6.05 -0.56 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -5.7 -4.15 4, -6.05 -0.56 3, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -0.82 1.68 4, -6.05 -0.56 3, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-0.82 1.68 4, -3.61 2.35 3, -6.05 -0.56 3, -0.82 1.68 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -6.05 -0.56 3, -5.7 -4.15 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, 0.3 1.41 4.4, -0.82 1.68 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-5.7 -4.15 4, 0.3 1.41 4.4, -1.52 0.85 4, -5.7 -4.15 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertFalse(rs.next());
        } finally {
            st.close();
        }
    }


    @Test
    public void testST_TriangleContouringWithZDoubleRange() throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS TIN");
            st.execute("CREATE TABLE TIN AS SELECT 'POLYGON ((-9.19 3.7 1, 0.3 1.41 4.4, -5.7 -4.15 4, -9.19 3.7 1))'::geometry the_geom");
            ResultSet rs = st.executeQuery("select * from ST_TriangleContouring('TIN', DOUBLERANGE(2,6,1)) order by idiso");
            assertEquals(2, rs.getMetaData().getColumnCount());
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.4 3.03 2, -9.19 3.7 1, -8.02 1.09 2, -6.4 3.03 2))", rs.getObject(1));
            assertEquals(0, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.05 -0.56 3, -6.4 3.03 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -6.4 3.03 2, -8.02 1.09 2, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.05 -0.56 3, -3.61 2.35 3, -6.4 3.03 2, -6.05 -0.56 3))", rs.getObject(1));
            assertEquals(1, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-6.86 -1.53 3, -5.7 -4.15 4, -6.05 -0.56 3, -6.86 -1.53 3))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -0.82 1.68 4, -6.05 -0.56 3, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-0.82 1.68 4, -3.61 2.35 3, -6.05 -0.56 3, -0.82 1.68 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, -6.05 -0.56 3, -5.7 -4.15 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(2, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-1.52 0.85 4, 0.3 1.41 4.4, -0.82 1.68 4, -1.52 0.85 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertTrue(rs.next());
            assertGeometryBarelyEquals("POLYGON ((-5.7 -4.15 4, 0.3 1.41 4.4, -1.52 0.85 4, -5.7 -4.15 4))", rs.getObject(1));
            assertEquals(3, rs.getInt("idiso"));
            assertFalse(rs.next());
        } finally {
            st.close();
        }
    }

    @Test
    /**
     * Check if an empty contouring does not stop the parsing of input table.
     */
    public void testContouringEmptyRow() throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS TIN");
            st.execute("CREATE TABLE TIN(pk serial, THE_GEOM GEOMETRY);");
            st.execute("INSERT INTO TIN(THE_GEOM) VALUES ('POLYGON((0 0 5, 3 0 5, 3 3 10, 0 0 10))')");
            st.execute("INSERT INTO TIN(THE_GEOM) VALUES ('POLYGON((0 0 0, 3 0 0, 3 3 3, 0 0 0))')");
            ResultSet rs = st.executeQuery("SELECT pk FROM ST_TriangleContouring('TIN', -1 ,1 , 4)");
            Set<Integer> pk = new HashSet<Integer>();
            while(rs.next()) {
                pk.add(rs.getInt("PK"));
            }
            // There is no iso with the first triangle, however there is iso with the second
            assertTrue(pk.contains(2));
        } finally {
            st.close();
        }
    }
    
    @Test
    public void testST_Drape1() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('LINESTRING (-5 5, 15 5)'::GEOMETRY, 'POLYGONZ ((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();
            assertGeometryEquals("LINESTRING Z(-5 5 0, 5 5 5, 10 5 5, 15 5 0)", rs.getObject(1));
        } finally {
            st.close();
        }
    }

    @Test
    public void testST_Drape2() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('LINESTRING (-5 5, 15 5)'::GEOMETRY, 'MULTIPOLYGON Z (((0 0 0, 10 0 0, 10 10 10, 0 0 0)), ((10 10 10, 10 0 0, 15 0 0, 10 10 0)))'::geometry)");
            rs.next();
            assertGeometryEquals("LINESTRING Z (-5 5 0, 5 5 5, 10 5 5, 12.5 5 5, 15 5 0)", rs.getObject(1));
        } finally {
            st.close();
        }
    }

    @Test
    public void testST_Drape3() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('POLYGON ((1 8, 8 8, 8 2, 1 2, 1 8))'::GEOMETRY, 'POLYGON Z ((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();
            assertGeometryEquals("POLYGON Z((1 2 0, 1 8 0, 8 8 8, 8 2 2, 2 2 2, 1 2 0))", rs.getObject(1));
        } finally {
            st.close();
        }
    }

    @Test
    public void testST_Drape4() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('POLYGON ((1 2, 1 8, 8 8, 8 2, 1 2),(3 6, 7 6, 7 4, 3 4, 3 6))'::GEOMETRY, 'POLYGON Z((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();
            assertGeometryEquals("POLYGON Z((1 2 0, 1 8 0, 8 8 8, 8 2 2, 2 2 2, 1 2 0), (3 4 0, 4 4 4, 7 4 4, 7 6 6, 6 6 6, 3 6 0, 3 4 0))", rs.getObject(1));
        } finally {
            st.close();
        }
    }
    
    @Test
    public void testST_Drape5() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('MULTIPOINT ((8 5.1 0), (5 2 0), (9 2 0), (6.1 4 0), (3 9 0), (0.3 7.2 0), (3.8 6.7 0), (11.5 7.4 0), (12.1 3 0), (0 0 0))'::GEOMETRY, 'POLYGON Z((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();            
            assertGeometryEquals("MULTIPOINT Z ((8 5.1 5.1), (5 2 2), (9 2 2), (6.1 4 4), (3 9 0), (0.3 7.2 0), (3.8 6.7 0), (11.5 7.4 0), (12.1 3 0), (0 0 0))", rs.getObject(1));
        } finally {
            st.close();
        }
    }
    
    @Test
    public void testST_Drape6() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('MULTILINESTRING ((-5 5, 15 5), (2 8, 15 8))'::GEOMETRY, 'POLYGON Z((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();
            assertGeometryEquals("MULTILINESTRING Z((-5 5 0, 5 5 5, 10 5 5, 15 5 0), (2 8 0, 8 8 8, 10 8 8, 15 8 0))", rs.getObject(1));
        } finally {
            st.close();
        }
    }
    
    @Test
    public void testST_Drape7() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('MULTILINESTRING ((-5 5, 15 5), (15 5.1, 5 8, 2 3, 12.9 3))'::GEOMETRY, 'POLYGON Z((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();
            assertGeometryEquals("MULTILINESTRING Z((-5 5 0, 5 5 5, 10 5 5, 15 5 0), (12.9 3 0, 10 3 3, 3 3 3, 2 3 0, 5 8 0, 7.325581395348837 7.325581395348837 7.325581395348837, 10 6.55 6.550000000000001, 15 5.1 0))", rs.getObject(1));
        } finally {
            st.close();
        }
    }
    
    
    @Test
    public void testST_Drape8() throws SQLException {
        Statement st = connection.createStatement();
        try {
            ResultSet rs = st.executeQuery("select st_drape('MULTIPOLYGON (((1 5, 6 5, 6 2, 1 2, 1 5)), ((9 8, 11 8, 11 5, 9 5, 9 8)))'::GEOMETRY, 'POLYGON Z((0 0 0, 10 0 0, 10 10 10, 0 0 0))'::geometry)");
            rs.next();
            assertGeometryEquals("MULTIPOLYGON Z(((1 2 0, 1 5 0, 5 5 5, 6 5 5, 6 2 2, 2 2 2, 1 2 0)), ((9 5 5, 9 8 8, 10 8 8, 11 8 0, 11 5 0, 10 5 5, 9 5 5)))", rs.getObject(1));
        } finally {
            st.close();
        }
    }
}
