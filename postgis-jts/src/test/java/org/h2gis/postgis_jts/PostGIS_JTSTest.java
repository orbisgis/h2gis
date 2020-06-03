/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.postgis_jts;

import org.h2gis.postgis_jts.Driver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for POSTGIS_JTS wrapper
 * @author Erwan Bocher
 */
public class PostGIS_JTSTest {
    private static Connection connection;
    private static final String DB_NAME = "PostGIS_JTS";

    @BeforeAll
    public static void tearUp() throws Exception {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql_h2://149.202.221.161:5432/?";
        Properties props = new Properties();
        props.setProperty("user", "");
        props.setProperty("password", "");
        boolean tobeConnected = !props.get("user").toString().isEmpty() && !props.get("password").toString().isEmpty();
        System.setProperty("test.postgis",
                Boolean.toString(tobeConnected));
        if(tobeConnected) {
            Driver driver = new Driver();
            connection = driver.connect(url, props);
        }
    }
    @AfterAll
    public static void tearDown() throws Exception {
        if(connection!=null) {
            connection.close();
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "test.postgis", matches = "true")
    public void testCreatePostGIS() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE; CREATE SCHEMA MYSCHEMA; DROP TABLE IF EXISTS MYSCHEMA.GEOMTABLE_INDEX; CREATE TABLE MYSCHEMA.GEOMTABLE_INDEX (THE_GEOM GEOMETRY);");
        st.execute("INSERT INTO MYSCHEMA.GEOMTABLE_INDEX VALUES ('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))'),('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))' )");
        ResultSetWrapper rs = (ResultSetWrapper) st.executeQuery("select * from MYSCHEMA.GEOMTABLE_INDEX");
        assertTrue(rs.next());
        Geometry geom = (Geometry) rs.getObject(1);
        assertEquals(1,geom.getNumGeometries());
        assertTrue(geom.getArea()>0);
        assertTrue(rs.next());
        geom = (Geometry) rs.getObject(1);
        assertEquals(1,geom.getNumGeometries());
        assertTrue(geom.getArea()>0);
    }

    @Test
    @EnabledIfSystemProperty(named = "test.postgis", matches = "true")
    public void testEstimateExtentMethod() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE; CREATE SCHEMA MYSCHEMA; DROP TABLE IF EXISTS MYSCHEMA.GEOMTABLE; CREATE TABLE MYSCHEMA.GEOMTABLE(THE_GEOM GEOMETRY);");
        st.execute("INSERT INTO MYSCHEMA.GEOMTABLE VALUES ('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))'),('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))' )");
        st.execute("ANALYZE MYSCHEMA.GEOMTABLE");
        StringBuilder query = new StringBuilder("SELECT  ST_EstimatedExtent(");
            query.append("'").append("myschema").append("',");
        query.append("'").append("geomtable").append("','").append("the_geom").append("') :: geometry");
        try (ResultSetWrapper rs = (ResultSetWrapper) connection.createStatement().executeQuery(query.toString())) {
            if (rs.next()) {
                Geometry geom = ((Geometry) rs.getObject(1));
                assertTrue(geom.getArea()>0);
            }
        }
    }
}
