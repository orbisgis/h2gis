package org.h2gis.h2spatialext.function.spatial.mesh;

import com.vividsolutions.jts.geom.TopologyException;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests of package spatial mesh
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class MeshFunctionTest {
    private static Connection connection;
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(MeshFunctionTest.class.getSimpleName(), false);
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
    public void testVoronoiPoints() throws Exception {

    }

    @Test
    public void test_ST_DelaunayNullValue() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay(null);");
        rs.next();
        assertTrue(rs.getObject(1)==null);
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithPoints1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTIPOINT ((0 0 1), (10 0 1), (10 10 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((0 0 1, 10 0 1, 10 10 1, 0 0 1)))",rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithPoints2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTIPOINT ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((0 0 1, 10 0 1, 5 5 1, 0 0 1)), ((5 5 1, 10 0 1, 10 10 1, 5 5 1)))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithCollection() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('GEOMETRYCOLLECTION (POLYGON ((150 380 1, 110 230 1, 180 190 1, 230 300 1, 320 280 1, 320 380 1, 150 380 1)),"
                + "  LINESTRING (70 330 1, 280 220 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((70 330 1, 110 230 1, 150 380 1, 70 330 1)), ((150 380 1, 110 230 1, 230 300 1, 150 380 1)), ((150 380 1, 230 300 1, 320 380 1, 150 380 1)), ((320 380 1, 230 300 1, 320 280 1, 320 380 1)), ((180 190 1, 280 220 1, 230 300 1, 180 190 1)), ((180 190 1, 230 300 1, 110 230 1, 180 190 1)), ((230 300 1, 280 220 1, 320 280 1, 230 300 1)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayWithLines() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('MULTILINESTRING ((1.1 8 1, 8 8 1), (2 3.1 1, 8 5.1 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((1.1 8 1, 2 3.1 1, 8 5.1 1, 1.1 8 1)), ((1.1 8 1, 8 5.1 1, 8 8 1, 1.1 8 1)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayAsMultiPolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('POLYGON ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY, 0);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((1.1 9 1, 1.1 3 1, 5 8 1, 1.1 9 1)), ((1.1 9 1, 5 8 1, 8.8 9.9 1, 1.1 9 1)), ((8.8 9.9 1, 5 8 1, 9.5 6.4 1, 8.8 9.9 1)), ((5.1 1.1 1, 9.5 6.4 1, 5 8 1, 5.1 1.1 1)), ((5.1 1.1 1, 5 8 1, 1.1 3 1, 5.1 1.1 1)))",  rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_DelaunayAsMultiLineString() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Delaunay('POLYGON ((1.1 9 1, 1.1 3 1, 5.1 1.1 1, 9.5 6.4 1, 8.8 9.9 1, 5 8 1, 1.1 9 1))'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((1.1 9 1, 8.8 9.9 1), (1.1 3 1, 1.1 9 1), (1.1 3 1, 5.1 1.1 1), (5.1 1.1 1, 9.5 6.4 1), (8.8 9.9 1, 9.5 6.4 1), (5 8 1, 9.5 6.4 1), (5 8 1, 8.8 9.9 1), (1.1 9 1, 5 8 1), (1.1 3 1, 5 8 1), (5 8 1, 5.1 1.1 1))",rs.getBytes(1));
        rs.close();
    }


    @Test
    public void test_ST_ConstrainedDelaunayNullValue() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay(null);");
        rs.next();
        assertTrue(rs.getObject(1)==null);
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithPoints() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTIPOINT ((0 0 1), (10 0 1), (10 10 1), (5 5 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON(((0 0 1, 10 0 1, 5 5 1, 0 0 1)),  ((10 0 1, 5 5 1, 10 10 1, 10 0 1)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithPolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('POLYGON ((1.9 8, 2.1 2.2, 7.1 2.2, 4.9 3.5, 7.5 8.1, 3.2 6, 1.9 8))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((1.9 8 0, 2.1 2.2 0, 3.2 6 0, 1.9 8 0)),"
                + " ((2.1 2.2 0, 3.2 6 0, 4.9 3.5 0, 2.1 2.2 0)),"
                + " ((2.1 2.2 0, 4.9 3.5 0, 7.1 2.2 0, 2.1 2.2 0)),"
                + " ((7.1 2.2 0, 4.9 3.5 0, 7.5 8.1 0, 7.1 2.2 0)),"
                + " ((4.9 3.5 0, 3.2 6 0, 7.5 8.1 0, 4.9 3.5 0)),"
                + " ((3.2 6 0, 1.9 8 0, 7.5 8.1 0, 3.2 6 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTILINESTRING ((2 7, 6 7), (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((2 7 0, 3.2 4.6 0), (3.2 4.6 0, 4.1 5 0), (4.1 5 0, 4.1818181818181825 7 0), "
                + "(3.2 4.6 0, 4.1818181818181825 7 0), (2 7 0, 4.1818181818181825 7 0), (4.1818181818181825 7 0, 5 9 0), "
                + "(2 7 0, 5 9 0), (3.2 4.6 0, 6 5 0), (4.1 5 0, 6 5 0), (4.1818181818181825 7 0, 6 5 0), (6 5 0, 6 7 0), "
                + "(4.1818181818181825 7 0, 6 7 0), (5 9 0, 6 7 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('MULTILINESTRING ((2 7, 6 7), (3.2 4.6, 5 9),(4.1 5, 6 5))'::GEOMETRY, 0);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((3.2 4.6 0, 4.1 5 0, 4.1818181818181825 7 0, 3.2 4.6 0)), "
                + "((2 7 0, 3.2 4.6 0, 4.1818181818181825 7 0, 2 7 0)), ((4.1818181818181825 7 0, 2 7 0, 5 9 0, 4.1818181818181825 7 0)), "
                + "((3.2 4.6 0, 4.1 5 0, 6 5 0, 3.2 4.6 0)), ((4.1 5 0, 4.1818181818181825 7 0, 6 5 0, 4.1 5 0)), "
                + "((6 5 0, 4.1818181818181825 7 0, 6 7 0, 6 5 0)), ((4.1818181818181825 7 0, 5 9 0, 6 7 0, 4.1818181818181825 7 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('LINESTRING (0 0, 10 0, 10 10, 0 10, 0 0)'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 0, 0 10 0), (0 0 0, 10 0 0), (0 10 0, 10 0 0), (10 0 0, 10 10 0), (0 10 0, 10 10 0))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithLines4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('LINESTRING (0 0 1, 10 0 1, 10 10 1, 0 10 1, 0 0 1)'::GEOMETRY, 1);");
        rs.next();
        assertGeometryEquals("MULTILINESTRING ((0 0 1, 0 10 1), (0 0 1, 10 0 1), (0 10 1, 10 0 1), (10 0 1, 10 10 1), (0 10 1, 10 10 1))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithCollection() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('GEOMETRYCOLLECTION (POLYGON ((150 380, 110 230, 180 190, 230 300, 320 280, 320 380, 150 380)), \n"
                + "  LINESTRING (70 330, 280 220))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((70 330 0, 110 230 0, 128.4958217270195 299.3593314763231 0, 70 330 0)), ((128.4958217270195 299.3593314763231 0, 70 330 0, 150 380 0, 128.4958217270195 299.3593314763231 0)), ((110 230 0, 210.2447552447552 256.53846153846155 0, 180 190 0, 110 230 0)), ((110 230 0, 210.2447552447552 256.53846153846155 0, 128.4958217270195 299.3593314763231 0, 110 230 0)), ((128.4958217270195 299.3593314763231 0, 230 300 0, 210.2447552447552 256.53846153846155 0, 128.4958217270195 299.3593314763231 0)), ((128.4958217270195 299.3593314763231 0, 230 300 0, 150 380 0, 128.4958217270195 299.3593314763231 0)), ((180 190 0, 210.2447552447552 256.53846153846155 0, 280 220 0, 180 190 0)), ((210.2447552447552 256.53846153846155 0, 230 300 0, 280 220 0, 210.2447552447552 256.53846153846155 0)), ((280 220 0, 230 300 0, 320 280 0, 280 220 0)), ((230 300 0, 320 380 0, 320 280 0, 230 300 0)), ((230 300 0, 320 380 0, 150 380 0, 230 300 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ConstrainedDelaunayWithCollection1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_ConstrainedDelaunay('GEOMETRYCOLLECTION(POINT (0 0 1), POINT (10 0 1), POINT (10 10 1), POINT (5 5 1))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("MULTIPOLYGON (((0 0 1, 10 0 1, 5 5 1, 0 0 1)), ((10 0 1, 5 5 1, 10 10 1, 10 0 1)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_VORONOI() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_MakePoint(A.X + (COS(B.X)), B.X - (SIN(A.X)), ROUND(LOG10(1 + A.X *" +
                " (5 * B.X)),2)) THE_GEOM from SYSTEM_RANGE(0,50) A,SYSTEM_RANGE(30,50) B;\n" +
                "drop table if exists voro;\n" +
                "create table voro as select ST_VORONOI(st_delaunay(st_accum(the_geom)), 1, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom))) the_geom from PTS;");
        ResultSet rs = st.executeQuery("select ST_NUMGEOMETRIES(the_geom) cpt,st_length(the_geom) lngth," +
                "st_numpoints(the_geom) numpts  from voro;");
        assertTrue(rs.next());
        assertEquals(3211, rs.getInt(1));
        assertEquals(2249.430, rs.getDouble(2), 1e-3);
        assertEquals(6425, rs.getInt(3));
        rs.close();
    }

    @Test
    public void test_ST_VORONOIPTS() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_MakePoint(A.X + (COS(B.X)), B.X - (SIN(A.X)), ROUND(LOG10(1 + A.X *" +
                " (5 * B.X)),2)) THE_GEOM from SYSTEM_RANGE(0,50) A,SYSTEM_RANGE(30,50) B;\n" +
                "drop table if exists voro;\n" +
                "create table voro as select ST_VORONOI(st_accum(the_geom), 2, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom))) the_geom from PTS;");
        ResultSet rs = st.executeQuery("select ST_NUMGEOMETRIES(the_geom) cpt,st_length(the_geom) lngth," +
                "st_numpoints(the_geom) numpts  from voro;");
        assertTrue(rs.next());
        assertEquals(1071, rs.getInt(1));
        assertEquals(8941.49, rs.getDouble(2), 1e-2);
        assertEquals(7465, rs.getInt(3));
        rs.close();
    }


    @Test
    /**
     * Construction of voronoi with three coplanar points make three polygons.
     */
    public void test_ST_VORONOIJTSInvalid() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_GeomFromText('MULTIPOINT(0 0, 1 0, 2 0)') the_geom;\n" +
                "drop table if exists voro;");
        ResultSet rs = st.executeQuery("select ST_NUMGEOMETRIES(ST_VORONOI(st_accum(the_geom), 2, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom)))) num from PTS;");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt("num"));
        rs.close();
    }


    @Test
    /**
     * Construction of Voronoi without input triangles is not possible.
     */
    public void test_ST_VORONOIInvalid() throws Exception {
        st.execute("drop table if exists pts;\n" +
                "create table pts as select ST_GeomFromText('MULTIPOINT(0 0 0, 1 0 0, 2 0 0)') the_geom;\n" +
                "drop table if exists voro;");
        ResultSet rs = st.executeQuery("select ST_VORONOI(ST_DELAUNAY(st_accum(the_geom)), 2, " +
                "ST_ENVELOPE(ST_ACCUM(the_geom))) the_geom from PTS;");
        assertTrue(rs.next());
        assertEquals("MULTIPOLYGON EMPTY", rs.getString("the_geom"));
        rs.close();
    }
}
