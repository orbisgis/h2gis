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

package org.h2gis.functions.spatial.geometry;

import org.h2.jdbc.JdbcSQLDataException;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.unitTest.GeometryAsserts;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Erwan Bocher
 */
public class GeometryFormsTest {
    
    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(GeometryFormsTest.class.getSimpleName());
        H2GISFunctions.registerFunction(connection.createStatement(), new DummySpatialFunction(), "");
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
    public void testOGCWKTForms() throws SQLException{     
        checkFormAndResult("POINT (0 0)", "POINT (0 0)");
        checkFormAndResult("LINESTRING (0 0, 1 1)", "LINESTRING (0 0, 1 1)");    
        checkFormAndResult("POLYGON ((140 250, 176 250, 176 214, 140 214, 140 250))", "POLYGON ((140 250, 176 250, 176 214, 140 214, 140 250))"); 
        
        checkFormAndResult("MULTIPOINT ((160 220), (180 200))", "MULTIPOINT ((160 220), (180 200))");
        checkFormAndResult("MULTILINESTRING ((125 195, 190 220), (176 176, 160 230))", "MULTILINESTRING ((125 195, 190 220), (176 176, 160 230))");
        checkFormAndResult("MULTIPOLYGON (((150 250, 210 250, 210 210, 150 210, 150 250)), ((220 220, 240 220, 240 180, 220 180, 220 220)))", "MULTIPOLYGON (((150 250, 210 250, 210 210, 150 210, 150 250)), ((220 220, 240 220, 240 180, 220 180, 220 220)))");
        checkFormAndResult("GEOMETRYCOLLECTION (LINESTRING (125 195, 190 220), LINESTRING (176 176, 160 230), POINT (200 200))", "GEOMETRYCOLLECTION (LINESTRING (125 195, 190 220), LINESTRING (176 176, 160 230), POINT (200 200))");
    }
    
    @Test
    public void testJTSWKTForms() throws SQLException{     
        checkFormAndResult("POINT (0 0 3)", "POINT Z (0 0 3)");    
        checkFormAndResult("POINT (0 0 3 4)", "POINT ZM (0 0 3 4)");
        checkFormAndResult("LINESTRING (0 0 1, 1 1 1)", "LINESTRING Z (0 0 1, 1 1 1)"); 
        checkFormAndResult("LINESTRING (0 0 1 2, 1 1 1 2)", "LINESTRING ZM (0 0 1 2, 1 1 1 2)"); 
        checkFormAndResult("POLYGON ((140 250 1, 176 250 1, 176 214 1, 140 214 1, 140 250 1))", "POLYGON Z((140 250 1, 176 250 1, 176 214 1, 140 214 1, 140 250 1))"); 
        checkFormAndResult("POLYGON ((140 250 1 1, 176 250 1 1, 176 214 1 1, 140 214 1 1, 140 250 1 1))", "POLYGON ZM((140 250 1 1, 176 250 1 1, 176 214 1 1, 140 214 1 1, 140 250 1 1))"); 
   }
    
    @Test
    public void testEWKTForms() throws SQLException{   
        checkFormAndResult("POINT Z(0 0 3)", "POINT Z (0 0 3)");    
        checkFormAndResult("POINT ZM(0 0 3 4)", "POINT ZM (0 0 3 4)");
        checkFormAndResult("LINESTRING Z(0 0 1, 1 1 1)", "LINESTRING Z (0 0 1, 1 1 1)"); 
        checkFormAndResult("LINESTRING ZM(0 0 1 2, 1 1 1 2)", "LINESTRING ZM (0 0 1 2, 1 1 1 2)"); 
        checkFormAndResult("POLYGON Z((140 250 1, 176 250 1, 176 214 1, 140 214 1, 140 250 1))", "POLYGON Z((140 250 1, 176 250 1, 176 214 1, 140 214 1, 140 250 1))"); 
        checkFormAndResult("POLYGON ZM((140 250 1 1, 176 250 1 1, 176 214 1 1, 140 214 1 1, 140 250 1 1))", "POLYGON ZM((140 250 1 1, 176 250 1 1, 176 214 1 1, 140 214 1 1, 140 250 1 1))"); 
   }


    private void checkFormAndResult(String actual, String expected) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT '" + actual + "'::GEOMETRY");
        rs.next();
        GeometryAsserts.assertGeometryEquals(expected, rs.getObject(1));
    }
    
    @Test
    public void testDummySpatialFunction() throws SQLException{
        ResultSet rs = st.executeQuery("SELECT DummySpatialFunction('POINT (0 0 3)'::GEOMETRY)");
        rs.next();
        GeometryAsserts.assertGeometryEquals("POINT Z(0 0 3)", rs.getObject(1));
        
        rs = st.executeQuery("SELECT DummySpatialFunction('POINT (0 0 3)'::GEOMETRY)");
        rs.next();
        GeometryAsserts.assertGeometryEquals("POINT (0 0 3)", rs.getObject(1));
    }
    
    
    @Test
    public void testDummySpatialFunctionZNaN() throws SQLException{
        ResultSet rs = st.executeQuery("SELECT DummySpatialFunction('POINT (0 0 3)'::GEOMETRY, true)");
        rs.next();
        GeometryAsserts.assertGeometryEquals("POINT(0 0)", rs.getObject(1));
    }
    
    @Test
    public void testDummySpatialFunctionOneZNaNInvalid() throws Exception {
        assertThrows(JdbcSQLDataException.class, () -> {
            ResultSet rs = st.executeQuery("SELECT DummySpatialFunction('LINESTRING (160 220 0, 180 200 0)'::GEOMETRY, false)");
            try {
                assertTrue(rs.next());
                assertNotNull(rs.getObject(1));
            } finally {
                rs.close();
            }
        });
    }
}
