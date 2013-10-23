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

package org.h2gis.h2spatial;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GeometryTypeCodes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/**
 * Test constraints on geometry type.
 * @author Nicolas Fortin
 */
public class GeometryTypeConstraintTest {
    private static Connection connection;


    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("GeometryTypeConstraintTest");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    /**
     * LineString into Geometry column
     * @throws Exception
     */
    @Test
    public void LineStringInGeometry() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table test IF EXISTS");
        st.execute("create table test (the_geom GEOMETRY)");
        st.execute("insert into test values (ST_LineFromText('LINESTRING( 0 18, 10 21, 16 23, 28 26, 44 31 )' ,101))");
        ResultSet rs = st.executeQuery("SELECT count(*) FROM test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
    }

    /**
     * LineString into LineString column
     * @throws Exception
     */
    @Test
    public void LineStringInLineString() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table test IF EXISTS");
        st.execute("create table test (the_geom LINESTRING)");
        st.execute("insert into test values (ST_LineFromText('LINESTRING( 0 18, 10 21, 16 23, 28 26, 44 31 )' ,101))");
        ResultSet rs = st.executeQuery("SELECT count(*) FROM test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
    }

    /**
     * LineString into Point column
     * @throws Exception
     */
    @Test(expected = SQLException.class)
    public void LineStringInPoint() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table test IF EXISTS");
        st.execute("create table test (the_geom POINT)");
        st.execute("insert into test values (ST_LineFromText('LINESTRING( 0 18, 10 21, 16 23, 28 26, 44 31 )' ,101))");
    }

    /**
     * LineString into LineString column
     * @throws Exception
     */
    @Test
    public void testGeometryColumnsView() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table T_GEOMETRY IF EXISTS");
        st.execute("create table T_GEOMETRY (the_geom GEOMETRY)");
        st.execute("drop table T_POINT IF EXISTS");
        st.execute("create table T_POINT (the_geom POINT)");
        st.execute("drop table T_LINE IF EXISTS");
        st.execute("create table T_LINE (the_geom LINESTRING)");
        st.execute("drop table T_POLYGON IF EXISTS");
        st.execute("create table T_POLYGON (the_geom POLYGON)");
        st.execute("drop table T_MPOINT IF EXISTS");
        st.execute("create table T_MPOINT (the_geom MULTIPOINT)");
        st.execute("drop table T_MLINE IF EXISTS");
        st.execute("create table T_MLINE (the_geom MULTILINESTRING)");
        st.execute("drop table T_MPOLYGON IF EXISTS");
        st.execute("create table T_MPOLYGON (the_geom MULTIPOLYGON)");

        ResultSet rs = st.executeQuery("select * from GEOMETRY_COLUMNS where F_TABLE_NAME in ('T_GEOMETRY','T_POINT','T_LINE','T_POLYGON','T_MGEOMETRY','T_MPOINT','T_MLINE','T_MPOLYGON') ORDER BY F_TABLE_NAME");
        assertTrue(rs.next());
        assertEquals("T_GEOMETRY",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.GEOMETRY,rs.getInt("geometry_type"));
        assertTrue(rs.next());
        assertEquals("T_LINE",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.LINESTRING,rs.getInt("geometry_type"));
        assertTrue(rs.next());
        assertEquals("T_MLINE",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.MULTILINESTRING,rs.getInt("geometry_type"));
        assertTrue(rs.next());
        assertEquals("T_MPOINT",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.MULTIPOINT,rs.getInt("geometry_type"));
        assertTrue(rs.next());
        assertEquals("T_MPOLYGON",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON,rs.getInt("geometry_type"));
        assertTrue(rs.next());
        assertEquals("T_POINT",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.POINT,rs.getInt("geometry_type"));
        assertTrue(rs.next());
        assertEquals("T_POLYGON",rs.getString("F_TABLE_NAME"));
        assertEquals(GeometryTypeCodes.POLYGON,rs.getInt("geometry_type"));
        assertFalse(rs.next());
        st.execute("drop table T_GEOMETRY, T_POINT,  T_LINE, T_POLYGON");
        st.execute("drop table T_MPOINT,  T_MLINE, T_MPOLYGON");
    }

    /**
     * LineString into LineString column
     * @throws Exception
     */
    @Test
    public void testGeometryColumnsViewUtility() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table T_GEOMETRY IF EXISTS");
        st.execute("create table T_GEOMETRY (the_geom GEOMETRY)");
        st.execute("drop table T_POINT IF EXISTS");
        st.execute("create table T_POINT (the_geom POINT)");
        st.execute("drop table T_LINE IF EXISTS");
        st.execute("create table T_LINE (the_geom LINESTRING)");
        st.execute("drop table T_POLYGON IF EXISTS");
        st.execute("create table T_POLYGON (the_geom POLYGON)");
        st.execute("drop table T_MPOINT IF EXISTS");
        st.execute("create table T_MPOINT (the_geom MULTIPOINT)");
        st.execute("drop table T_MLINE IF EXISTS");
        st.execute("create table T_MLINE (the_geom MULTILINESTRING)");
        st.execute("drop table T_MPOLYGON IF EXISTS");
        st.execute("create table T_MPOLYGON (the_geom MULTIPOLYGON)");

        assertEquals(GeometryTypeCodes.GEOMETRY,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_GEOMETRY"),""));
        assertEquals(GeometryTypeCodes.LINESTRING,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_LINE"),""));
        assertEquals(GeometryTypeCodes.POLYGON,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_POLYGON"),""));
        assertEquals(GeometryTypeCodes.POINT,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_POINT"),""));
        assertEquals(GeometryTypeCodes.MULTILINESTRING,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_MLINE"),""));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_MPOLYGON"),""));
        assertEquals(GeometryTypeCodes.MULTIPOINT,
                SFSUtilities.getGeometryType(connection, TableLocation.parse("T_MPOINT"),""));

        st.execute("drop table T_GEOMETRY, T_POINT,  T_LINE, T_POLYGON");
        st.execute("drop table T_MPOINT,  T_MLINE, T_MPOLYGON");
    }
}
