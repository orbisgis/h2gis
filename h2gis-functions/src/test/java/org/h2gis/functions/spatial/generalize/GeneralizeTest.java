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

package org.h2gis.functions.spatial.generalize;

import org.h2.jdbc.JdbcSQLDataException;
import org.h2.jdbc.JdbcSQLException;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.io.gpx.GPXImportTest;
import org.h2gis.functions.spatial.geometry.DummySpatialFunction;
import org.h2gis.unitTest.GeometryAsserts;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Erwan Bocher
 */
public class GeneralizeTest {
    
    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(GeneralizeTest.class.getSimpleName());
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
    public void testST_SimplifyPolygonHull1() throws SQLException{
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPolygonHull('POLYGON ((131 158, 136 163, 161 165, 173 156, 179 148, 169 140, 186 144, 190 137, 185 131, 174 128, 174 124, 166 119, 158 121, 158 115, 165 107, 161 97, 166 88, 166 79, 158 57, 145 57, 112 53, 111 47, 93 43, 90 48, 88 40, 80 39, 68 32, 51 33, 40 31, 39 34, 49 38, 34 38, 25 34, 28 39, 36 40, 44 46, 24 41, 17 41, 14 46, 19 50, 33 54, 21 55, 13 52, 11 57, 22 60, 34 59, 41 68, 75 72, 62 77, 56 70, 46 72, 31 69, 46 76, 52 82, 47 84, 56 90, 66 90, 64 94, 56 91, 33 97, 36 100, 23 100, 22 107, 29 106, 31 112, 46 116, 36 118, 28 131, 53 132, 59 127, 62 131, 76 130, 80 135, 89 137, 87 143, 73 145, 80 150, 88 150, 85 157, 99 162, 116 158, 115 165, 123 165, 122 170, 134 164, 131 158))'::GEOMETRY,  0.3)");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((11 57, 56 91, 33 97, 23 100, 22 107, 28 131, 80 135, 73 145, 85 157, 99 162, 122 170, 161 165, 173 156, 186 144, 190 137, 185 131, 174 124, 166 119, 166 79, 158 57, 68 32, 40 31, 25 34, 17 41, 14 46, 11 57))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void testST_SimplifyPolygonHull2() throws SQLException{
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPolygonHull('POLYGON ((131 158, 136 163, 161 165, 173 156, 179 148, 169 140, 186 144, 190 137, 185 131, 174 128, 174 124, 166 119, 158 121, 158 115, 165 107, 161 97, 166 88, 166 79, 158 57, 145 57, 112 53, 111 47, 93 43, 90 48, 88 40, 80 39, 68 32, 51 33, 40 31, 39 34, 49 38, 34 38, 25 34, 28 39, 36 40, 44 46, 24 41, 17 41, 14 46, 19 50, 33 54, 21 55, 13 52, 11 57, 22 60, 34 59, 41 68, 75 72, 62 77, 56 70, 46 72, 31 69, 46 76, 52 82, 47 84, 56 90, 66 90, 64 94, 56 91, 33 97, 36 100, 23 100, 22 107, 29 106, 31 112, 46 116, 36 118, 28 131, 53 132, 59 127, 62 131, 76 130, 80 135, 89 137, 87 143, 73 145, 80 150, 88 150, 85 157, 99 162, 116 158, 115 165, 123 165, 122 170, 134 164, 131 158))'::GEOMETRY,  0.3, false)");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON ((28 131, 59 127, 76 130, 89 137, 99 162, 116 158, 131 158, 161 165, 179 148, 169 140, 190 137, 158 121, 158 115, 161 97, 158 57, 145 57, 112 53, 90 48, 68 32, 33 54, 41 68, 75 72, 64 94, 36 100, 46 116, 28 131))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void testST_SimplifyPolygonHull3() throws SQLException{
        ResultSet rs = st.executeQuery("SELECT ST_SimplifyPolygonHull('SRID=4326;POLYGON ((131 158, 136 163, 161 165, 173 156, 179 148, 169 140, 186 144, 190 137, 185 131, 174 128, 174 124, 166 119, 158 121, 158 115, 165 107, 161 97, 166 88, 166 79, 158 57, 145 57, 112 53, 111 47, 93 43, 90 48, 88 40, 80 39, 68 32, 51 33, 40 31, 39 34, 49 38, 34 38, 25 34, 28 39, 36 40, 44 46, 24 41, 17 41, 14 46, 19 50, 33 54, 21 55, 13 52, 11 57, 22 60, 34 59, 41 68, 75 72, 62 77, 56 70, 46 72, 31 69, 46 76, 52 82, 47 84, 56 90, 66 90, 64 94, 56 91, 33 97, 36 100, 23 100, 22 107, 29 106, 31 112, 46 116, 36 118, 28 131, 53 132, 59 127, 62 131, 76 130, 80 135, 89 137, 87 143, 73 145, 80 150, 88 150, 85 157, 99 162, 116 158, 115 165, 123 165, 122 170, 134 164, 131 158))'::GEOMETRY,  0.3, false)");
        assertTrue(rs.next());
        assertGeometryEquals("SRID=4326;POLYGON ((28 131, 59 127, 76 130, 89 137, 99 162, 116 158, 131 158, 161 165, 179 148, 169 140, 190 137, 158 121, 158 115, 161 97, 158 57, 145 57, 112 53, 90 48, 68 32, 33 54, 41 68, 75 72, 64 94, 36 100, 46 116, 28 131))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void testST_SimplifyPolygonHull4(){
        assertThrows(SQLException.class, ()-> {
            try {
                st.execute("SELECT ST_SimplifyPolygonHull('LINESTRING (5 125, 38 51, 39 52.4, 39.6 53.2, 41.4 54.4, 118 115)'::GEOMETRY,  0.3, false)");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
}
