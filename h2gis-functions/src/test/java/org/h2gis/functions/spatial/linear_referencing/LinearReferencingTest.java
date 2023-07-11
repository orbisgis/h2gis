/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.linear_referencing;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *  @author  Erwan Bocher, CNRS (2023)
 */
public class LinearReferencingTest {

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        connection = H2GISDBFactory.createSpatialDataBase(LinearReferencingTest.class.getSimpleName());
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
    public void test_ST_LineSubstring1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineSubstring('LINESTRING(0 0, 10 0)'::GEOMETRY, 0, 0.5)");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(0 0, 5 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_LineSubstring2() throws Exception {
        assertThrows(SQLException.class, () -> {
            st.executeQuery("SELECT ST_LineSubstring('LINESTRING(0 0, 10 0)'::GEOMETRY, 1, 10)");
        });
    }

    @Test
    public void test_ST_LineSubstring3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineSubstring('MULTILINESTRING((0 0, 10 0), (10 10, 10 20))'::GEOMETRY, 0, 0.5)");
        assertTrue(rs.next());
        assertGeometryEquals("MULTILINESTRING ((0 0, 5 0), (10 10, 10 15))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_LineSubstring4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineSubstring('LINESTRINGZ(0 0 0, 10 0 10)'::GEOMETRY, 0, 0.5)");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING Z (0 0 0, 5 0 5)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_LineInterpolatePoint1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineInterpolatePoint('LINESTRING(0 0, 10 0)'::GEOMETRY, 0.5)");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(5 0)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_LineInterpolatePoint2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineInterpolatePoint('MULTILINESTRING((0 0, 10 0), (10 10, 10 20))'::GEOMETRY,0.5)");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOINT ((5 0), (10 15))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_LineInterpolatePoint3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_LineInterpolatePoint('LINESTRINGZ(0 0 0, 10 0 10)'::GEOMETRY, 0.5)");
        assertTrue(rs.next());
        assertGeometryEquals("POINT Z (5 0 5)", rs.getObject(1));
        rs.close();
    }

}
