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

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2gis.h2spatial.ut.SpatialH2UT;
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
        ResultSet rs = st.executeQuery("SELECT ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00');");
        assertTrue(rs.next());
        Array data = rs.getArray(1);
        Object[] valueArray = (Object[]) data.getArray();
        //résultats tirés de http://www.sunearthtools.com/dp/tools/pos_sun.php     
        Assert.assertEquals(((Double)valueArray[0]), 0.07382742, 0.01); //4,23 degrés
        Assert.assertEquals(((Double)valueArray[1]), 0.84002696898487, 0.01);
        rs.close();
    }
    
    @Test
    public void test_ST_Shadow() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('LINESTRING (100 350, 200 350, 200 250, 100 250, 100 350)'::GEOMETRY,10 , "
                + "ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00'));");
         assertTrue(rs.next());
         System.out.println(rs.getString(1));
         rs.close();        
    }
    
    @Test
    public void test_ST_Shadow1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('LINESTRING (120 270, 270 220)'::GEOMETRY,10 , "
                + "ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00'));");
         assertTrue(rs.next());
         System.out.println(rs.getString(1));
         rs.close();        
    }
    
     @Test
    public void test_ST_Shadow2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('LINESTRING (87 225, 120 270, 270 220, 225 139, 87 225)'::GEOMETRY,10 , "
                + "ST_SunPosition('POINT (-1.5485036 47.2484747)'::GEOMETRY, '2014-12-20 16:40:00'));");
         assertTrue(rs.next());
         System.out.println(rs.getString(1));
         rs.close();        
    }
    
    @Test
    public void test_ST_Shadow3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Shadow('POLYGON ((100 300, 250 300, 250 150, 100 150, 100 300))'::GEOMETRY,10 , "
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
