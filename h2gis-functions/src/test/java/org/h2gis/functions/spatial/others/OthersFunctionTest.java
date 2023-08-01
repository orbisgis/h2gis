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
package org.h2gis.functions.spatial.others;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.spatial.processing.ProcessingFunctionTest;
import org.junit.jupiter.api.*;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Erwan Bocher, CNRS, 2023
 */
public class OthersFunctionTest {

    private static Connection connection;
    private Statement st;
    private static WKTReader WKT_READER;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(ProcessingFunctionTest.class.getSimpleName());
        WKT_READER = new WKTReader();
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
    public void testST_Clip1() throws Exception {
        String geomToClip = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        String geomForClip ="POLYGON ((170 330, 260 330, 260 150, 170 150, 170 330))";
        String resultGeom = "MULTIPOLYGON (((90 200, 90 270, 170 270, 170 200, 90 200)), ((170 200, 170 270, 260 270, 260 200, 170 200)), ((260 200, 260 270, 330 270, 330 200, 260 200)))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip2() throws Exception {
        String geomToClip = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270))";
        String geomForClip ="POLYGON ((170 330, 260 330, 260 150, 170 150, 170 330))";
        String resultGeom = "MULTIPOLYGON (((90 200, 90 270, 170 270, 170 200, 90 200)), ((170 200, 170 270, 260 270, 260 200, 170 200)), ((260 200, 260 270, 330 270, 330 200, 260 200)))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip3() throws Exception {
        String geomToClip = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270)," +
                "  (192 245, 220 245, 220 220, 192 220, 192 245))";
        String geomForClip ="POLYGON ((170 330, 260 330, 260 150, 170 150, 170 330))";
        String resultGeom = "MULTIPOLYGON (((90 200, 90 270, 170 270, 170 200, 90 200)), ((170 200, 170 270, 260 270, 260 200, 170 200), (192 220, 220 220, 220 245, 192 245, 192 220)), ((260 200, 260 270, 330 270, 330 200, 260 200)))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip4() throws Exception {
        String geomToClip = "POLYGON ((90 270, 330 270, 330 200, 90 200, 90 270)," +
                "  (192 245, 220 245, 220 220, 192 220, 192 245))";
        String geomForClip ="POLYGON ((210 292, 289 292, 289 180, 210 180, 210 292)," +
                "  (216 240, 279 240, 279 230, 216 230, 216 240))";
        String resultGeom = "MULTIPOLYGON (((90 200, 90 270, 210 270, 210 245, 192 245, 192 220, 210 220, 210 200, 90 200)), ((210 200, 210 220, 220 220, 220 230, 279 230, 279 240, 220 240, 220 245, 210 245, 210 270, 289 270, 289 200, 210 200)), ((289 200, 289 270, 330 270, 330 200, 289 200)))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip5() throws Exception {
        String geomToClip = "POLYGON ((96 269, 90 221, 117 222, 119 223.25, 167 268, 141 267, 96 269)," +
                "  (109 263, 101 254, 107 247, 108.25 246.75, 122 256, 109 263)," +
                "  (111 239, 100 231, 105 228, 106.5 228, 107.5 228, 118 229, 111 239))";
        String geomForClip ="GEOMETRYCOLLECTION (POLYGON ((80 228, 88 201, 107 208, 97 226, 80 228))," +
                "  LINESTRING (140 212, 128 291)," +
                "  LINESTRING (65 248, 66.25 248, 69.25 248, 148 262, 154 281))";
        String resultGeom = "MULTIPOLYGON (((90 221, 90.71739130434783 226.7391304347826, 97 226, 99.58064516129032 221.3548387096774, 90 221)), ((90.71739130434783 226.7391304347826, 93.92329545454545 252.38636363636363, 101.26446280991736 253.69146005509643, 107 247, 108.25 246.75, 122 256, 120.0763723150358 257.035799522673, 132.8151191454396 259.3004656258559, 135.8981288981289 239.00398475398475, 119 223.25, 117 222, 99.58064516129032 221.3548387096774, 97 226, 90.71739130434783 226.7391304347826), (100 231, 105 228, 106.5 228, 107.5 228, 118 229, 111 239, 100 231)), ((93.92329545454545 252.38636363636363, 96 269, 131.5819881053526 267.4185783064288, 132.8151191454396 259.3004656258559, 120.0763723150358 257.035799522673, 109 263, 101 254, 101.26446280991736 253.69146005509643, 93.92329545454545 252.38636363636363)), ((131.5819881053526 267.4185783064288, 141 267, 149.6844262295082 267.3340163934426, 148 262, 132.8151191454396 259.3004656258559, 131.5819881053526 267.4185783064288)), ((132.8151191454396 259.3004656258559, 148 262, 149.6844262295082 267.3340163934426, 167 268, 135.8981288981289 239.00398475398475, 132.8151191454396 259.3004656258559)))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip6() throws Exception {
        String geomToClip = "MULTIPOLYGON (((106 273, 115 273, 115 263, 106 263, 106 273)), \n" +
                "  ((112.6 258.6, 120.4 258.6, 120.4 251.7, 112.6 251.7, 112.6 258.6)), \n" +
                "  ((126 271, 135 271, 135 264, 126 264, 126 271)))";
        String geomForClip ="POLYGON ((112 267, 133 267, 133 256, 112 256, 112 267))";
        String resultGeom = "MULTIPOLYGON (((106 263, 106 273, 115 273, 115 267, 112 267, 112 263, 106 263)), ((112 263, 112 267, 115 267, 115 263, 112 263)), ((112.6 251.7, 112.6 256, 120.4 256, 120.4 251.7, 112.6 251.7)), ((112.6 256, 112.6 258.6, 120.4 258.6, 120.4 256, 112.6 256)), ((126 264, 126 267, 133 267, 133 264, 126 264)), ((126 267, 126 271, 135 271, 135 264, 133 264, 133 267, 126 267)))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip7() throws Exception {
        String geomToClip = "MULTILINESTRING ((110.2 269.5, 107 249, 117 247), \n" +
                "  (117 264, 129 254), \n" +
                "  (120 250, 134 250))";
        String geomForClip ="MULTILINESTRING ((103.2 260.5, 110 257), \n" +
                "  (106 242, 115.3 252.5), \n" +
                "  (120 250, 140 250))";
        String resultGeom = "MULTILINESTRING ((107 249, 108.37901726427623 257.8343293492696), (107 249, 111.41747572815534 248.11650485436894), (108.37901726427623 257.8343293492696, 110.2 269.5), (111.41747572815534 248.11650485436894, 117 247), (117 264, 129 254), (120 250, 134 250))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip8() throws Exception {
        String geomToClip = "MULTILINESTRING ((110.2 269.5, 107 249, 117 247), \n" +
                "  (117 264, 129 254), \n" +
                "  (120 250, 134 250))";
        String geomForClip ="GEOMETRYCOLLECTION (LINESTRING (103.2 260.5, 110 257), \n" +
                "  LINESTRING (106 242, 115.3 252.5), \n" +
                "  LINESTRING (120 250, 140 250), \n" +
                "  POLYGON ((106.6 267.4, 112 267.4, 112 264, 106.6 264, 106.6 267.4)), \n" +
                "  POLYGON ((127.5 267.5, 137 267.5, 137 263, 127.5 263, 127.5 267.5)))";
        String resultGeom = "MULTILINESTRING ((110.2 269.5, 109.87219512195122 267.4), \n" +
                "  (109.87219512195122 267.4, 109.34146341463415 264), \n" +
                "  (109.34146341463415 264, 108.37901726427623 257.8343293492696), \n" +
                "  (108.37901726427623 257.8343293492696, 107 249), \n" +
                "  (107 249, 111.41747572815534 248.11650485436894), \n" +
                "  (111.41747572815534 248.11650485436894, 117 247), \n" +
                "  (117 264, 129 254), \n" +
                "  (120 250, 134 250))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip9() throws Exception {
        String geomToClip = "LINESTRING (80 250, 130 250)";
        String geomForClip ="LINESTRING (90 250, 110 250)";
        String resultGeom = "MULTILINESTRING ((80 250, 90 250), (90 250, 110 250), (110 250, 130 250))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void testST_Clip10() {
        String geomToClip = "LINESTRING (80 250, 130 250)";
        String geomForClip ="GEOMETRYCOLLECTION (POINT (90 250), \n" +
                "  POINT (95 250), \n" +
                "  POINT (0 250))";
        assertThrows(SQLException.class, () -> {
            st.execute("SELECT ST_CLIP('" +geomToClip+
                    "'::GEOMETRY, '" + geomForClip+
                    "'::GEOMETRY);");
        });
    }

    @Test
    public void testST_Clip11() throws SQLException {
        String geomToClip = "LINESTRING (80 250, 130 250)";
        String geomForClip ="GEOMETRYCOLLECTION (POINT (90 250), \n" +
                "  POINT (95 250), \n" +
                "  LINESTRING (99 250, 106 250), \n" +
                "  LINESTRING (107.6 245.75, 107.5 256.45))";
        String resultGeom = "MULTILINESTRING ((80 250, 99 250), (99 250, 106 250), (106 250, 107.56028037383177 250), (107.56028037383177 250, 130 250))";
        ResultSet rs = st.executeQuery("SELECT ST_CLIP('" +geomToClip+
                "'::GEOMETRY, '" + geomForClip+
                "'::GEOMETRY);");
        assertTrue(rs.next());
        assertGeometryEquals(resultGeom,  rs.getBytes(1));
        rs.close();
    }
}
