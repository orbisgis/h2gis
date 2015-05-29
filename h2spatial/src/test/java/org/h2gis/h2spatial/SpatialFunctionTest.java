/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatial;

import org.h2.jdbc.JdbcSQLException;
import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_GeomFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_PointFromText;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GeometryTypeCodes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.*;

/**
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class SpatialFunctionTest {

    private static Connection connection;
    private static final String DB_NAME = "SpatialFunctionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        // Set up test data
        OGCConformance1Test.executeScript(connection, "ogc_conformance_test3.sql");
        OGCConformance1Test.executeScript(connection, "spatial_index_test_data.sql");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void test_ST_EnvelopeIntersects() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_EnvelopesIntersect(road_segments.centerline, divided_routes.centerlines) "
                + "FROM road_segments, divided_routes WHERE road_segments.fid = 102 AND divided_routes.name = 'Route 75'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionAggregate() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Union(ST_Accum(footprint))) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals(16, rs.getDouble(1), 1e-8);
        rs.close();
    }

    @Test
    public void test_ST_UnionSimple() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Union('POLYGON((0 0,10 0,10 10,0 10,0 0))'))");
        assertTrue(rs.next());
        assertEquals(100, rs.getDouble(1), 0);
        rs.close();
        rs = st.executeQuery("SELECT ST_Area(ST_Union('MULTIPOLYGON(((0 0,5 0,5 5,0 5,0 0)),((5 5,10 5,10 10,5 10,5 5)))'))");
        assertTrue(rs.next());
        assertEquals(50, rs.getDouble(1), 0);
        rs.close();
    }

    @Test
    public void test_ST_UnionAggregateAlone() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Union('MULTIPOLYGON (((1 4, 1 8, 5 5, 1 4)), ((3 8, 2 5, 5 5, 3 8)))')");
        assertTrue(rs.next());
        assertEquals("POLYGON ((1 4, 1 8, 2.6 6.8, 3 8, 5 5, 1 4))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_AccumArea() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Accum(footprint)) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals(16, rs.getDouble(1), 1e-8);
        rs.close();
    }

    @Test
    public void test_ST_Accum() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Accum(footprint) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals("MULTIPOLYGON (((50 31, 54 31, 54 29, 50 29, 50 31)), ((66 34, 62 34, 62 32, 66 32, 66 34)))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_AccumPoint() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Accum('MULTIPOINT((0 0), (1 1))'::geometry)");
        assertTrue(rs.next());
        assertEquals("MULTIPOINT ((0 0), (1 1))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_AccumLine() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Accum('GEOMETRYCOLLECTION(LINESTRING(0 0, 1 1),LINESTRING(5 5, 8 8))'::geometry)");
        assertTrue(rs.next());
        assertEquals("MULTILINESTRING ((0 0, 1 1), (5 5, 8 8))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_AccumCollection() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS TESTACCUMCOLLECT;" +
                "CREATE TABLE TESTACCUMCOLLECT AS SELECT 'MULTIPOLYGON (((50 31, 54 31, 54 29, 50 29, 50 31))," +
                " ((66 34, 62 34, 62 32, 66 32, 66 34)))'::geometry the_geom");
        ResultSet rs = st.executeQuery("SELECT ST_Accum(the_geom) FROM TESTACCUMCOLLECT");
        assertTrue(rs.next());
        assertEquals("MULTIPOLYGON (((50 31, 54 31, 54 29, 50 29, 50 31)), ((66 34, 62 34, 62 32, 66 32, 66 34)))", rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_Accum_LeftJoin() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS LEFT_TEST, RIGHT_TEST");
        st.execute("CREATE TABLE LEFT_TEST(GID serial, the_geom POINT)");
        st.execute("CREATE TABLE RIGHT_TEST(GID serial, the_geom POINT)");
        st.execute("INSERT INTO LEFT_TEST(the_geom) VALUES ('POINT(1 1)')");
        ResultSet rs = st.executeQuery("SELECT ST_Accum(r.the_geom) FROM LEFT_TEST L LEFT JOIN RIGHT_TEST R ON (L.GID = R.GID) group by l.gid");
        assertTrue(rs.next());
        assertGeometryEquals("GEOMETRYCOLLECTION EMPTY", rs.getObject(1));
        rs.close();
    }

    @Test
    public void testFunctionRemarks() throws SQLException {
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DummyFunction(), "");
        ResultSet procedures = connection.getMetaData().getProcedures(null, null, "DUMMYFUNCTION");
        assertTrue(procedures.next());
        assertEquals(DummyFunction.REMARKS, procedures.getString("REMARKS"));
        procedures.close();
        CreateSpatialExtension.unRegisterFunction(connection.createStatement(), new DummyFunction());
    }

    @Test
    public void testSetSRID() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists testSrid");
        st.execute("create table testSrid(the_geom geometry)");
        st.execute("insert into testSrid values (ST_GeomFromText('POINT( 15 25 )',27572))");
        ResultSet rs = st.executeQuery("SELECT ST_SRID(ST_SETSRID(the_geom,5321)) trans,ST_SRID(the_geom) original  FROM testSrid");
        assertTrue(rs.next());
        assertEquals(27572, rs.getInt("original"));
        assertEquals(5321, rs.getInt("trans"));
    }

    @Test
    public void test_ST_CoordDim() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES ('POINT(1 2)'),('LINESTRING(0 0, 1 1 2)'),"
                + "('LINESTRING (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)'),('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))');");
        ResultSet rs = st.executeQuery(
                "SELECT ST_CoordDim(geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        st.execute("DROP TABLE input_table;");
    }
    
    @Test
    public void test_ST_Is3D() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(geom Geometry);"
                + "INSERT INTO input_table VALUES ('POINT(1 2)'),('LINESTRING(0 0, 1 1 2)'),"
                + "('LINESTRING (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)'),('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))');");
        ResultSet rs = st.executeQuery(
                "SELECT ST_Is3D(geom) FROM input_table;");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_GeometryN() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeometryN('MULTIPOLYGON(((0 0, 3 -1, 1.5 2, 0 0)), "
                + "((1 2, 4 2, 4 6, 1 6, 1 2)))', 1);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON((0 0, 3 -1, 1.5 2, 0 0))", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2), "
                + "(1 2, 4 2, 4 6))', 2);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2, 4 2, 4 6)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('MULTIPOINT((0 0), (1 6), (2 2), (1 2))', 2);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(1 6)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('GEOMETRYCOLLECTION("
                + "MULTIPOINT((4 4), (1 1), (1 0), (0 3)), "
                + "LINESTRING(2 6, 6 2), "
                + "POINT(4 4), "
                + "POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))', 3);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(4 4)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN("
                + "ST_GeometryN('GEOMETRYCOLLECTION("
                + "MULTIPOINT((4 4), (1 1), (1 0), (0 3)),"
                + "LINESTRING(2 6, 6 2))', 1), 4);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(0 3)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('LINESTRING(1 1, 1 6, 2 2, -1 2)', 1);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 1, 1 6, 2 2, -1 2)", rs.getBytes(1));
        assertFalse(rs.next());
    }

    @Test(expected = SQLException.class)
    public void test_ST_GeometryNIndexOutOfRange() throws Exception {
        Statement st = connection.createStatement();
        st.executeQuery("SELECT ST_GeometryN('LINESTRING(1 1, 1 6, 2 2, -1 2)', 0);");
    }

    @Test
    public void test_ST_GeometryTypeCode() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('POINT(1 1)'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.POINT, rs.getInt(1));
        rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('LINESTRING(1 1, 2 2)'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.LINESTRING, rs.getInt(1));
        rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('POLYGON((1 1, 2 2, 5 3, 1 1))'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.POLYGON, rs.getInt(1));
        rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('MULTIPOINT(1 1,2 2)'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.MULTIPOINT, rs.getInt(1));
        rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('MULTILINESTRING((1 1, 2 2),(3 3, 5 4))'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.MULTILINESTRING, rs.getInt(1));
        rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('MULTIPOLYGON(((1 1, 2 2, 5 3, 1 1)),((0 0, 2 2, 5 3, 0 0)))'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, rs.getInt(1));
        rs = st.executeQuery(
                "SELECT ST_GeometryTypeCode('GEOMETRYCOLLECTION(POINT(4 6),LINESTRING(4 6,7 10))'::geometry)");
        assertTrue(rs.next());
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, rs.getInt(1));
    }

    @Test
    public void test_ST_ASWkt() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_ASWKT('POINT(1 1 1)')");
        try {
            assertTrue(rs.next());
            assertEquals("POINT (1 1)", rs.getString(1));
        } finally {
            rs.close();
        }

    }

    @Test
    public void test_ST_Envelope() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Envelope(ST_GeomFromText('LINESTRING(1 1,5 5)', 27572))");
        try {
            assertTrue(rs.next());
            assertEquals(ValueGeometry.getFromGeometry(ST_GeomFromText.toGeometry("POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1))", 27572)),
                    ValueGeometry.getFromGeometry(rs.getObject(1)));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_SRID(ST_Envelope(ST_GeomFromText('LINESTRING(1 1,5 5)', 27572)))");
        try {
            assertTrue(rs.next());
            assertEquals(27572, rs.getInt(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_Buffer1() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Buffer("
                + " ST_GeomFromText('POINT(100 90)'),"
                + " 50, 2);");
        try {
            assertTrue(rs.next());
            assertEquals("POLYGON ((150 90, 135.35533905932738 54.64466094067263, 100 40, 64.64466094067262 54.64466094067262,"
                    + " 50 90, 64.64466094067262 125.35533905932738, 99.99999999999999 140,"
                    + " 135.35533905932738 125.35533905932738, 150 90))", rs.getString(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_Buffer2() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Buffer("
                + " ST_GeomFromText("
                + "  'LINESTRING(50 50,150 150,150 50)'"
                + " ), 10, 'endcap=round join=round');");
        try {
            assertTrue(rs.next());
            assertEquals("POLYGON ((142.92893218813452 157.07106781186548, 144.44429766980397 158.31469612302544,"
                    + " 146.1731656763491 159.23879532511287, 148.04909677983872 159.8078528040323, 150 160,"
                    + " 151.95090322016128 159.8078528040323, 153.8268343236509 159.23879532511287,"
                    + " 155.55570233019603 158.31469612302544, 157.07106781186548 157.07106781186548,"
                    + " 158.31469612302544 155.55570233019603, 159.23879532511287 153.8268343236509,"
                    + " 159.8078528040323 151.95090322016128, 160 150, 160 50, 159.8078528040323 48.04909677983872,"
                    + " 159.23879532511287 46.1731656763491, 158.31469612302544 44.44429766980398, 157.07106781186548"
                    + " 42.928932188134524, 155.55570233019603 41.685303876974544, 153.8268343236509 40.76120467488713,"
                    + " 151.95090322016128 40.19214719596769, 150 40, 148.04909677983872 40.19214719596769, 146.1731656763491"
                    + " 40.76120467488713, 144.44429766980397 41.685303876974544, 142.92893218813452 42.928932188134524,"
                    + " 141.68530387697456 44.44429766980398, 140.76120467488713 46.17316567634911, 140.1921471959677"
                    + " 48.04909677983872, 140 50, 140 125.85786437626905, 57.071067811865476 42.928932188134524, 55.55570233019602"
                    + " 41.685303876974544, 53.8268343236509 40.76120467488713, 51.95090322016128 40.19214719596769, 50 40,"
                    + " 48.04909677983872 40.19214719596769, 46.1731656763491 40.76120467488713, 44.44429766980398 41.685303876974544,"
                    + " 42.928932188134524 42.928932188134524, 41.685303876974544 44.44429766980398, 40.76120467488713 46.1731656763491,"
                    + " 40.19214719596769 48.04909677983871, 40 50, 40.19214719596769 51.95090322016129, 40.76120467488713 53.8268343236509,"
                    + " 41.68530387697455 55.55570233019603, 42.928932188134524 57.071067811865476, 142.92893218813452 157.07106781186548))", rs.getString(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_Buffer3() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Buffer("
                + " ST_GeomFromText('POINT(100 90)'),"
                + " 50, 2);");
        try {
            assertTrue(rs.next());
            assertEquals("POLYGON ((150 90, 135.35533905932738 54.64466094067263, 100 40, 64.64466094067262 54.64466094067262,"
                    + " 50 90, 64.64466094067262 125.35533905932738, 99.99999999999999 140,"
                    + " 135.35533905932738 125.35533905932738, 150 90))", rs.getString(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_Buffer4() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Buffer("
                + " ST_GeomFromText('LINESTRING (100 250, 200 250, 150 350)'),"
                + " 10, 'quad_segs=2 endcap=round join=mitre');");
        try {
            assertTrue(rs.next());
            assertEquals("POLYGON ((183.81966011250108 260, 141.05572809000085 345.5278640450004, "
                    + "140.51316701949486 353.1622776601684, 145.52786404500043 358.94427190999915, "
                    + "153.16227766016837 359.48683298050514, 158.94427190999915 354.4721359549996, "
                    + "216.18033988749883 239.99999999999997, 100 240, 92.92893218813452 242.92893218813452, "
                    + "90 250, 92.92893218813452 257.0710678118655, 100 260, 183.81966011250108 260))", rs.getString(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_Buffer5() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Buffer("
                + " ST_GeomFromText('LINESTRING (100 250, 200 250, 150 350)'),"
                + " 10, 'quad_segs=2 endcap=square join=bevel');");
        try {
            assertTrue(rs.next());
            assertEquals("POLYGON ((183.81966011250108 260, 141.05572809000085 345.5278640450004,"
                    + " 136.58359213500128 354.47213595499954, 154.47213595499957 363.41640786499875, "
                    + "208.94427190999915 254.47213595499957, 200 240, 100 240, 90 240, 90 260, "
                    + "183.81966011250108 260))", rs.getString(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_Buffer6() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Buffer(ST_GeomFromText('LINESTRING (100 250, 200 250, 150 350)'),"
                + " 10, 'quad_segs=2 endcap=flat join=bevel');");
        try {
            assertTrue(rs.next());
            assertEquals("POLYGON ((183.81966011250108 260, 141.05572809000085 345.5278640450004, "
                    + "158.94427190999915 354.4721359549996, "
                    + "208.94427190999915 254.47213595499957, "
                    + "200 240, 100 240, 100 260, 183.81966011250108 260))", rs.getString(1));
        } finally {
            rs.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_Buffer7() throws Throwable {
        Statement st = connection.createStatement();
        try {
        st.execute("SELECT ST_Buffer("
                + " ST_GeomFromText('LINESTRING (100 250, 200 250, 150 350)'),"
                + " 10, 'quad_segs=2 endcap=flated');");
        }catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_Buffer8() throws Throwable {
        Statement st = connection.createStatement();
        try {
            st.execute("SELECT ST_Buffer("
                    + " ST_GeomFromText('LINESTRING (100 250, 200 250, 150 350)'),"
                    + " 10, 'quad_segments=2 endcap=flated');");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
    @Test
    public void test_ST_OrderingEquals1() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_OrderingEquals('LINESTRING(0 0, 10 10)'::GEOMETRY,"
                + "'LINESTRING(0 0, 5 5, 10 10)'::GEOMETRY);");
        rs.next();
        assertTrue(!rs.getBoolean(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OrderingEquals2() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_OrderingEquals('LINESTRING(0 0, 10 10)'::GEOMETRY,"
                + "'LINESTRING(0 0, 0 0, 10 10)'::GEOMETRY);");
        rs.next();
        assertTrue(!rs.getBoolean(1));
        rs.close();
    }
    
    @Test
    public void test_ST_OrderingEquals3() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_OrderingEquals('LINESTRING(0 0, 0 0, 10 10)'::GEOMETRY,"
                + "'LINESTRING(0 0, 0 0, 10 10)'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getBoolean(1));
        rs.close();
    }
    
     @Test
    public void test_ST_OrderingEquals4() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10)'::GEOMETRY,"
                + "'LINESTRING(0 0, 0 0, 10 10)'::GEOMETRY);");
        rs.next();
        assertTrue(!rs.getBoolean(1));
        rs.close();
    }
 
    @Test
    public void test_ST_OrderingEquals5() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_OrderingEquals('LINESTRING(0 0 1, 0 0, 10 10 3)'::GEOMETRY,"
                + "'LINESTRING(0 0 1, 0 0, 10 10 3)'::GEOMETRY);");
        rs.next();
        assertTrue(rs.getBoolean(1));
        rs.close();
    }

    @Test
    public void test_ST_PointFromTextNullWKT() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_PointFromText(NULL, 2154);");
        rs.next();
        assertEquals(null, rs.getObject(1));
        assertFalse(rs.next());
        rs.close();
    }

    @Test(expected = SQLException.class)
    public void test_ST_PointFromTextWrongType() throws Throwable {
        Statement st = connection.createStatement();
        try {
            st.executeQuery("SELECT ST_PointFromText('LINESTRING(0 0, 1 0)', 2154);");
        } catch (JdbcSQLException e) {
            final Throwable originalCause = e.getOriginalCause();
            assertTrue(e.getMessage().contains(ST_PointFromText.TYPE_ERROR + "LineString"));
            throw originalCause;
        }
    }
    
    @Test
    public void test_ST_PointFromWKB1() throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_PointFromWKB(ST_AsBinary('POINT(0 10)'::GEOMETRY))");
        rs.next();
        assertEquals("POINT (0 10)", rs.getString(1));
        assertFalse(rs.next());
        rs.close();
    }
    
    @Test(expected = SQLException.class)
    public void test_ST_PointFromWKB2() throws Throwable {
        Statement st = connection.createStatement();
        st.executeQuery("SELECT ST_PointFromWKB(ST_AsBinary('LINESTRING(0 10, 10 10)'::GEOMETRY));");
    }
}
