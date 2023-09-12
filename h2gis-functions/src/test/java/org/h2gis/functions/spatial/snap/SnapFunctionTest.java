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

package org.h2gis.functions.spatial.snap;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;

/**
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS, 2023
 */
public class SnapFunctionTest {
    private static Connection connection;
    private Statement st;
    private static final GeometryFactory FACTORY = new GeometryFactory();

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(SnapFunctionTest.class.getSimpleName());
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
    public void test_ST_Project1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Project('SRID=4326;POINT(0 0)'::GEOMETRY, 100000, radians(45.0));");
        rs.next();
        assertGeometryBarelyEquals("POINT(0.635231029125537 0.639472334729198)", rs.getObject(1), 10-8);
        rs.close();
    }

    @Test
    public void test_ST_Project2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Project('SRID=4326;POINT(0 0)'::GEOMETRY, 100000, radians(405.0));");
        rs.next();
        assertGeometryBarelyEquals("POINT(0.635231029125537 0.639472334729198)", rs.getObject(1), 10-8);
        rs.close();
    }
    @Test
    public void test_ST_Project3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Project('SRID=4326;POINTZ(0 0 10)'::GEOMETRY, 100000, radians(405.0));");
        rs.next();
        assertGeometryBarelyEquals("POINTZ(0.635231029125537 0.639472334729198 10)", rs.getObject(1), 10-8);
        rs.close();
    }

    @Test
    public void test_ST_Project4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Project('POINT(0 0)'::GEOMETRY, 100000, radians(45.0));");
        rs.next();
        System.out.println(rs.getObject(1));
        assertGeometryBarelyEquals("POINT (70710.67811865476 70710.67811865475)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Project5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Project('POINT(0 0)'::GEOMETRY, 100000, radians(405.0));");
        rs.next();
        System.out.println(rs.getObject(1));
        assertGeometryBarelyEquals("POINT (70710.67811865476 70710.67811865475)", rs.getObject(1));
        rs.close();
    }
}
