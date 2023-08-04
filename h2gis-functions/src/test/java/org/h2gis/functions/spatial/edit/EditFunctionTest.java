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
package org.h2gis.functions.spatial.edit;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.spatial.processing.ProcessingFunctionTest;
import org.junit.jupiter.api.*;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Erwan Bocher, CNRS, 2023
 */
public class EditFunctionTest {

    private static Connection connection;
    private Statement st;
    private static WKTReader WKT_READER;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(EditFunctionTest.class.getSimpleName());
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
    public void testST_ForcePolygonCW1() throws Exception {
        String geom = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        String resultGeom = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        ResultSet rs = st.executeQuery("SELECT ST_ForcePolygonCW('" +geom+ "'::GEOMETRY);");
        assertTrue(rs.next());
        assertEquals(resultGeom,  rs.getString(1));
        rs.close();
    }

    @Test
    public void testST_ForcePolygonCW2() throws Exception {
        String geom = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        String resultGeom = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        ResultSet rs = st.executeQuery("SELECT ST_ForcePolygonCW('" +geom+ "'::GEOMETRY);");
        assertTrue(rs.next());
        assertEquals(resultGeom,  rs.getString(1));
        rs.close();
    }

    @Test
    public void testST_ForcePolygonCW3() throws Exception {
        String geom = "POLYGON ((90 270, 90 200, 330 200, 330 270, 90 270)," +
                "  (160 250, 225 250, 225 218, 160 218, 160 250))";
        String resultGeom = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270), (160 250, 160 218, 225 218, 225 250, 160 250))";
        ResultSet rs = st.executeQuery("SELECT ST_ForcePolygonCW('" +geom+ "'::GEOMETRY);");
        assertTrue(rs.next());
        assertEquals(resultGeom,  rs.getString(1));
        rs.close();
    }

    @Test
    public void testST_ForcePolygonCW4() throws Exception {
        String geom = "GEOMETRYCOLLECTION (POLYGON ((90 270, 90 200, 330 200, 330 270, 90 270), \n" +
                "  (160 250, 225 250, 225 218, 160 218, 160 250)), \n" +
                "  LINESTRING (70 190, 84 178, 95 167, 104 160, 115 151, 124 145, 132 139, 130 140), \n" +
                "  POINT (180 350), \n" +
                "  POINT (270 320))";
        String resultGeom = "GEOMETRYCOLLECTION (POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270), (160 250, 160 218, 225 218, 225 250, 160 250)), LINESTRING (70 190, 84 178, 95 167, 104 160, 115 151, 124 145, 132 139, 130 140), POINT (180 350), POINT (270 320))";
        ResultSet rs = st.executeQuery("SELECT ST_ForcePolygonCW('" +geom+ "'::GEOMETRY);");
        assertTrue(rs.next());
        assertEquals(resultGeom,  rs.getString(1));
        rs.close();
    }

    @Test
    public void testST_ForcePolygonCCW1() throws Exception {
        String geom = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        String resultGeom = "POLYGON ((90 270, 90 200, 330 200, 330 270, 90 270))";
        ResultSet rs = st.executeQuery("SELECT ST_ForcePolygonCCW('" +geom+ "'::GEOMETRY);");
        assertTrue(rs.next());
        assertEquals(resultGeom,  rs.getString(1));
        rs.close();
    }

    @Test
    public void testST_ForcePolygonCCW2() throws Exception {
        String geom = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0)," +
                "  (2 2, 4 2, 4 4, 2 4, 2 2))";
        String resultGeom = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 2 4, 4 4, 4 2, 2 2))";
        ResultSet rs = st.executeQuery("SELECT ST_ForcePolygonCCW('" +geom+ "'::GEOMETRY);");
        assertTrue(rs.next());
        assertEquals(resultGeom,  rs.getString(1));
        rs.close();
    }
}
