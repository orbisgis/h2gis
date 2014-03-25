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
package org.h2gis.h2spatial;

import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_GeomFromText;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GeometryTypeCodes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

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
        URL sqlURL = SpatialFunctionTest.class.getResource("ogc_conformance_test3.sql");
        URL sqlURL2 = SpatialFunctionTest.class.getResource("spatial_index_test_data.sql");
        Statement st = connection.createStatement();
        st.execute("RUNSCRIPT FROM '"+sqlURL+"'");
        st.execute("RUNSCRIPT FROM '"+sqlURL2+"'");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    private static void assertGeometryEquals(String expectedWKT, byte[] valueWKB) {
        assertTrue(Arrays.equals(ValueGeometry.get(expectedWKT).getBytes(), valueWKB));
    }

    @Test
    public void test_ST_EnvelopeIntersects() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_EnvelopesIntersect(road_segments.centerline, divided_routes.centerlines) " +
                "FROM road_segments, divided_routes WHERE road_segments.fid = 102 AND divided_routes.name = 'Route 75'");
        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
        rs.close();
    }

    @Test
    public void test_ST_UnionAggregate() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Union(ST_Accum(footprint))) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals(16,rs.getDouble(1),1e-8);
        rs.close();
    }

    @Test
    public void test_ST_UnionSimple() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Union('POLYGON((0 0,10 0,10 10,0 10,0 0))'))");
        assertTrue(rs.next());
        assertEquals(100,rs.getDouble(1),0);
        rs.close();
        rs = st.executeQuery("SELECT ST_Area(ST_Union('MULTIPOLYGON(((0 0,5 0,5 5,0 5,0 0)),((5 5,10 5,10 10,5 10,5 5)))'))");
        assertTrue(rs.next());
        assertEquals(50,rs.getDouble(1),0);
        rs.close();
    }

    @Test
    public void test_ST_UnionAggregateAlone() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Union('MULTIPOLYGON (((1 4, 1 8, 5 5, 1 4)), ((3 8, 2 5, 5 5, 3 8)))')");
        assertTrue(rs.next());
        assertEquals("POLYGON ((1 4, 1 8, 2.6 6.8, 3 8, 5 5, 1 4))",rs.getString(1));
        rs.close();
    }

    @Test
    public void test_ST_AccumArea() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Accum(footprint)) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals(16,rs.getDouble(1),1e-8);
        rs.close();
    }

    @Test
    public void test_ST_Accum() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Accum(footprint) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals("GEOMETRYCOLLECTION (POLYGON ((50 31, 54 31, 54 29, 50 29, 50 31)), POLYGON ((66 34, 62 34, 62 32, 66 32, 66 34)))",rs.getString(1));
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
        st.execute("DROP TABLE IF EXISTS input_table;" +
                "CREATE TABLE input_table(geom Geometry);" +
                "INSERT INTO input_table VALUES ('POINT(1 2)'),('LINESTRING(0 0, 1 1 2)')," +
                "('LINESTRING (1 1 1, 2 1 2, 2 2 3, 1 2 4, 1 1 5)'),('MULTIPOLYGON (((0 0, 1 1, 0 1, 0 0)))');");
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
    public void test_ST_GeometryN() throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_GeometryN('MULTIPOLYGON(((0 0, 3 -1, 1.5 2, 0 0)), " +
                "((1 2, 4 2, 4 6, 1 6, 1 2)))', 1);");
        assertTrue(rs.next());
        assertGeometryEquals("POLYGON((0 0, 3 -1, 1.5 2, 0 0))", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('MULTILINESTRING((1 1, 1 6, 2 2, -1 2), " +
                "(1 2, 4 2, 4 6))', 2);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2, 4 2, 4 6)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('MULTIPOINT((0 0), (1 6), (2 2), (1 2))', 2);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(1 6)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN('GEOMETRYCOLLECTION(" +
                "MULTIPOINT((4 4), (1 1), (1 0), (0 3)), " +
                "LINESTRING(2 6, 6 2), " +
                "POINT(4 4), " +
                "POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))', 3);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(4 4)", rs.getBytes(1));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT ST_GeometryN(" +
                "ST_GeometryN('GEOMETRYCOLLECTION(" +
                "MULTIPOINT((4 4), (1 1), (1 0), (0 3))," +
                "LINESTRING(2 6, 6 2))', 1), 4);");
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
            assertEquals("POINT (1 1)",rs.getString(1));
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
            assertEquals(27572,rs.getInt(1));
        } finally {
            rs.close();
        }
    }
}
