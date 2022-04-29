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

package org.h2gis.functions.spatial.convert;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Sylvain Palominos
 * @author Erwan Bocher
 * 
 */
public class GeometryConvertTest {
    private static final GeometryFactory FACTORY = new GeometryFactory();

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(GeometryConvertTest.class.getSimpleName());
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
    public void AsEWKB1() throws SQLException {
        ResultSet rs = st.executeQuery("SELECT RAWTOHEX(ST_AsEWKB(ST_GeomFromText('POLYGON((0 0,0 1,1 1,1 0,0 0))',4326)));");
        rs.next();
        assertEquals("0020000003000010e600000001000000050000000000000000000000000000000000000000000000003ff00000000000003ff00000000000003ff00000000000003ff0000000000000000000000000000000000000000000000000000000000000",  rs.getString(1));
    }

    @Test
    public void AsEWKB2() throws SQLException {
        ResultSet rs = st.executeQuery("SELECT RAWTOHEX(ST_AsEWKB(ST_GeomFromText('POINT M(1 2 3)',4326)));");
        rs.next();
        assertEquals("0060000001000010e63ff000000000000040000000000000004008000000000000",  rs.getString(1));
    }

    @Test
    public void AsEWKB3() throws SQLException {
        ResultSet rs = st.executeQuery("SELECT RAWTOHEX(ST_AsEWKB(ST_GeomFromText('POINT Z (1 2 3)',4326)));");
        rs.next();
        assertEquals("00a0000001000010e63ff000000000000040000000000000004008000000000000",  rs.getString(1));
    }

    @Test
    public void AsEWKB4() throws SQLException {
        ResultSet rs = st.executeQuery("SELECT RAWTOHEX(ST_AsEWKB(ST_GeomFromText('POINT ZM (1 2 3 4)',4326)));");
        rs.next();
        assertEquals("00e0000001000010e63ff0000000000000400000000000000040080000000000004010000000000000",  rs.getString(1));
    }
    
}
