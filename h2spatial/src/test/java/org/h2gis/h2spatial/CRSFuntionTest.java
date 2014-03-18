/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatial;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.Connection;
import java.sql.Statement;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;

import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.SpatialResultSet;

/**
 *
 * @author Erwan Bocher
 */
public class CRSFuntionTest {

    private static Connection connection;
    private static final String DB_NAME = "CRSFuntionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SFSUtilities.wrapConnection(SpatialH2UT.createSpatialDataBase(DB_NAME));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void test_ST_Transform27572To4326() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE init as SELECT ST_GeomFromText('POINT(584173.736059813 2594514.82833411)', 27572) as the_geom;");
        WKTReader wKTReader = new WKTReader();
        Geometry targetGeom = wKTReader.read("POINT(2.114551393 50.345609791)");
        SpatialResultSet srs = st.executeQuery("SELECT ST_TRANSFORM(the_geom, 4326) from init;").unwrap(SpatialResultSet.class);
        assertTrue(srs.next());
        assertTrue(srs.getGeometry(1).equalsExact(targetGeom, 0.0001));
        st.execute("DROP TABLE IF EXISTS init;");
    }

    @Test
    public void testST_Transform4326to2154() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE init AS SELECT ST_GeomFromText('POINT(2.114551393 50.345609791)', 4326) as the_geom;");
        WKTReader wKTReader = new WKTReader();
        Geometry targetGeom = wKTReader.read("POINT(636890.74032145 7027895.26344997)");
        SpatialResultSet srs = st.executeQuery("SELECT ST_TRANSFORM(the_geom, 2154) from init;").unwrap(SpatialResultSet.class);
        assertTrue(srs.next());
        assertTrue(srs.getGeometry(1).equalsExact(targetGeom, 0.01));
        st.execute("DROP TABLE IF EXISTS init;");
    }

    @Test
    public void testST_Transform27572to3857() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE init AS SELECT ST_GeomFromText('POINT(282331 2273699.7)', 27572) as the_geom;");
        WKTReader wKTReader = new WKTReader();
        Geometry targetGeom = wKTReader.read("POINT(-208496.537435372 6005369.87702729)");
        SpatialResultSet srs = st.executeQuery("SELECT ST_TRANSFORM(the_geom, 3857) from init;").unwrap(SpatialResultSet.class);
        assertTrue(srs.next());
        assertTrue(srs.getGeometry(1).equalsExact(targetGeom, 0.01));
        st.execute("DROP TABLE IF EXISTS init;");
    }
    
    @Test
    public void testST_Transform27572to2154WithoutNadgrid() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE init AS SELECT ST_GeomFromText('POINT(282331 2273699.7)', 27572) as the_geom;");
        WKTReader wKTReader = new WKTReader();
        Geometry targetGeom = wKTReader.read("POINT(332602.961893497 6709788.26447893)");
        SpatialResultSet srs = st.executeQuery("SELECT ST_TRANSFORM(the_geom, 2154) from init;").unwrap(SpatialResultSet.class);
        assertTrue(srs.next());
        assertTrue(srs.getGeometry(1).equalsExact(targetGeom, 0.01));
        st.execute("DROP TABLE IF EXISTS init;");
    }
    
    @Test
    public void testST_Transform27572to2154WithNadgrid() throws Exception {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE init AS SELECT ST_GeomFromText('POINT(565767.906 2669005.730)', 320002120) as the_geom;");
        WKTReader wKTReader = new WKTReader();
        Geometry targetGeom = wKTReader.read("POINT(619119.4605 7102502.9796)");
        SpatialResultSet srs = st.executeQuery("SELECT ST_TRANSFORM(the_geom, 310024140) from init;").unwrap(SpatialResultSet.class);
        assertTrue(srs.next());
        assertTrue(srs.getGeometry(1).equalsExact(targetGeom, 0.01));
        st.execute("DROP TABLE IF EXISTS init;");
    }
}
