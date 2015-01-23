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
import java.sql.Array;
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
public class SpatialFunctionTest2 {
    
    private static Connection connection;
    private Statement st;
    private static final String DB_NAME = "SpatialFunctionTest2";
    
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
    public void test_ST_SunPosition1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SunPosition('POINT (-1.5490144 47.2488601)'::GEOMETRY, '2015-1-22 10:00:00');");
        assertTrue(rs.next());
        Geometry point = (Geometry)rs.getObject(1);
        System.err.println("Sun azimut :"+ Math.toDegrees(point.getCoordinate().x) + " altitude : "+ Math.toDegrees(point.getCoordinate().y));
        //résultats tirés de http://www.sunearthtools.com/dp/tools/pos_sun.php     
        //Assert.assertEquals(point.getCoordinate().y, 0.07382742, 0.01); //4,23 degrés
        //Assert.assertEquals(point.getCoordinate().x, 0.84002696898487, 0.01);
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10)'::GEOMETRY,2 , "
                + "radians(90),radians(45));");
         assertTrue(rs.next());
         assertGeometryEquals("POLYGON ((10 5, 10 10, 8 10, 8 5, 10 5))", rs.getBytes(1));
         rs.close();        
    }
    
    @Test
    public void test_ST_Shadow1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('LINESTRING (10 5, 10 10)'::GEOMETRY,2 , "
                + "radians(270),radians(45));");
         assertTrue(rs.next());
         assertGeometryEquals("POLYGON ((10 5, 10 10, 12 10, 12 5, 10 5))", rs.getBytes(1));
         rs.close();        
    }
    
    @Test
    public void test_ST_Shadow2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_GeometryShadow('POINT (10 5)'::GEOMETRY,2 , "
                + "radians(270),radians(45));");
         assertTrue(rs.next());
         assertGeometryEquals("LINESTRING (10 5, 12 5)", rs.getBytes(1));
         rs.close();        
    }
    
    
    @Test
    public void test_ST_Shadow3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('POLYGON ((-1.5493799591210962 47.248973095582315, -1.5488521492139842 47.24913253815842, -1.5486102363398913 47.24877516686715, -1.5491380462470032 47.24861572429104, -1.5493799591210962 47.248973095582315))'::GEOMETRY,10 , "
                + "ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00'));");
        assertTrue(rs.next());
        System.out.println(rs.getString(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('POLYGON ((100 300, 250 300, 250 150, 100 150, 100 300),"
                + "  (150 250, 200 250, 200 200, 150 200, 150 250))'::GEOMETRY,10 , "
                + "ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00'));");
        assertTrue(rs.next());
        System.out.println(rs.getString(1));
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('MULTILINESTRING ((100 300, 250 300, 250 150, 100 150, 100 300),"
                + "  (150 250, 200 250, 200 200, 150 200, 150 250))'::GEOMETRY,1 , "
                + "ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00'));");
        assertTrue(rs.next());
        System.out.println(rs.getString(1));
        rs.close();
    }
   
}
