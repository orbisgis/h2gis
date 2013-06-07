/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
package org.h2spatial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Nicolas Fortin
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

    @Test
    public void test_ST_EnvelopeIntersects() throws Exception  {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT ST_EnvelopesIntersect(road_segments.centerline, divided_routes.centerlines) " +
                    "FROM road_segments, divided_routes WHERE road_segments.fid = 102 AND divided_routes.name = 'Route 75'");
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
    }

    @Test
    public void test_ST_UnionAggregate() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_Area(ST_Union(ST_Accum(footprint))) FROM buildings GROUP BY SUBSTRING(address,4)");
        assertTrue(rs.next());
        assertEquals(16,rs.getDouble(1),1e-8);
    }

    @Test
    public void test_ST_Explode() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(centerlines) FROM ST_Explode('divided_routes','centerlines') WHERE name = 'Route 75' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("LINESTRING (16 0, 16 23, 16 48)", rs.getString(1));
    }

    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('forests') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
    }

    @Test
    public void test_ST_ExplodeEmptyGeometryCollection() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("create table test(the_geom GEOMETRY, value Integer)");
        st.execute("insert into test VALUES (ST_GeomFromText('MULTILINESTRING EMPTY'),108)");
        st.execute("insert into test VALUES (ST_GeomFromText('MULTIPOINT EMPTY'),109)");
        st.execute("insert into test VALUES (ST_GeomFromText('MULTIPOLYGON EMPTY'),110)");
        st.execute("insert into test VALUES (ST_GeomFromText('GEOMETRYCOLLECTION EMPTY'),111)");
        ResultSet rs = st.executeQuery("SELECT the_geom , value FROM ST_Explode('test') ORDER BY value");
        assertTrue(rs.next());
        assertEquals(108,rs.getInt(2));
        assertEquals("LINESTRING EMPTY", rs.getString(1));
        assertTrue(rs.next());
        assertEquals(109,rs.getInt(2));
        assertEquals("POINT EMPTY", rs.getString(1));
        assertTrue(rs.next());
        assertEquals(110,rs.getInt(2));
        assertEquals("POLYGON EMPTY", rs.getString(1));
        assertTrue(rs.next());
        assertEquals(111,rs.getInt(2));
        assertEquals(null, rs.getObject(1));
        rs.close();
        st.execute("drop table test");
    }
}
