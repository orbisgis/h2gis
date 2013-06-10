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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
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
}
