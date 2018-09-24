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

package org.h2gis.functions.io.gpx;

import org.locationtech.jts.geom.Geometry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.jdbc.JdbcSQLException;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class GPXImportTest {

    private static Connection connection;
    private static final String DB_NAME = "GPXImportTest";
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new GPXRead(), "");
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
    public void importGPXWaypoints() throws SQLException {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ", 'GPXDATA');");
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_WAYPOINT'");
        assertTrue(rs.next());
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_WAYPOINT");
        rs.next();
        assertTrue(rs.getInt(1) == 3);
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM GPXDATA_WAYPOINT");
        assertTrue(rs.next());
        assertEquals("POINT (-71.119277 42.438878)", rs.getString("the_geom"));
        assertEquals(4326, ((Geometry)rs.getObject("the_geom")).getSRID());
        rs.close();
    }
    
    @Test(expected = SQLException.class)
    public void importGPXWaypoints1() throws Throwable {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ", 'GPXDATA');");
        try {
            st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ", 'GPXDATA');");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test
    public void importGPXRoute() throws SQLException {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("route.gpx").getPath()) + ", 'GPXDATA');");
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_ROUTE'");
        assertTrue(rs.next());
        rs.close();
        rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_ROUTEPOINT'");
        assertTrue(rs.next());
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_ROUTE");
        rs.next();
        assertTrue(rs.getInt(1) == 1);
        rs.close();
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_ROUTEPOINT");
        rs.next();
        assertTrue(rs.getInt(1) == 5);
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM GPXDATA_ROUTE");
        assertTrue(rs.next());
        assertEquals("LINESTRING (-71.107628 42.43095, -71.109236 42.43124, -71.109942 42.43498, -71.119676 42.456592, -71.119845 42.457388)", rs.getString("the_geom"));
        rs.close();
    }
    
    @Test
    public void importGPXRouteTwice() throws SQLException {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("route.gpx").getPath()) + ", 'GPXDATA');");
        try {
            st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("route.gpx").getPath()) + ", 'GPXDATA');");
        } catch (JdbcSQLException e) {
            assertTrue(e.getOriginalCause().getMessage().equals("The table " + "\"GPXDATA_ROUTE\"" + " already exists."));
        }
    }

    @Test
    public void importGPXTrack() throws SQLException {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("track.gpx").getPath()) + ", 'GPXDATA');");
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_TRACK'");
        assertTrue(rs.next());
        rs.close();
        rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_TRACK'");
        assertTrue(rs.next());
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_TRACK");
        rs.next();
        assertTrue(rs.getInt(1) == 1);
        rs.close();
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_TRACKSEGMENT");
        rs.next();
        assertTrue(rs.getInt(1) == 2);
        rs.close();
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_TRACKPOINT");
        rs.next();
        assertTrue(rs.getInt(1) == 4);
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM GPXDATA_TRACK");
        assertTrue(rs.next());
        assertEquals("MULTILINESTRING ((-71.09622 42.210009, -71.09622 42.210031), (-71.102335 42.209129, -71.1024 42.208958))"
                , rs.getString("the_geom"));
        rs.close();
    }
    
    
    @Test
    public void importGPXWaypointsFileName() throws SQLException {
        st.execute("DROP TABLE IF EXISTS WAYPOINT_WAYPOINT, WAYPOINT_ROUTE, WAYPOINT_ROUTEPOINT,WAYPOINT_TRACK, WAYPOINT_TRACKSEGMENT, WAYPOINT_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ");");
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'WAYPOINT_WAYPOINT'");
        assertTrue(rs.next());
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(id) FROM WAYPOINT_WAYPOINT");
        rs.next();
        assertTrue(rs.getInt(1) == 3);
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM WAYPOINT_WAYPOINT");
        assertTrue(rs.next());
        assertEquals("POINT (-71.119277 42.438878)", rs.getString("the_geom"));
        rs.close();
    }
    
    @Test
    public void importGPXFiles() throws SQLException {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ", 'GPXDATA');");       
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("route.gpx").getPath()) + ", 'GPXDATA');");
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_WAYPOINT'"
                + " or TABLE_NAME = 'GPXDATA_ROUTE' or TABLE_NAME = 'GPXDATA_ROUTEPOINT'");
        assertTrue(rs.next());
        rs.close();        
        rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_TRACK'"
                + " or TABLE_NAME = 'GPXDATA_TRACKSEGMENT' or TABLE_NAME = 'GPXDATA_TRACKPOINT'");
        assertTrue(!rs.next());
        rs.close();    
    }
    
    @Test
    public void importGPXTwiceWithDelete() throws SQLException {
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT, GPXDATA_ROUTE, GPXDATA_ROUTEPOINT,GPXDATA_TRACK, GPXDATA_TRACKSEGMENT, GPXDATA_TRACKPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ", 'GPXDATA');");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("waypoint.gpx").getPath()) + ", 'GPXDATA', true);");
        
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'GPXDATA_WAYPOINT'");
        assertTrue(rs.next());
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(id) FROM GPXDATA_WAYPOINT");
        rs.next();
        assertTrue(rs.getInt(1) == 3);
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM GPXDATA_WAYPOINT");
        assertTrue(rs.next());
        assertEquals("POINT (-71.119277 42.438878)", rs.getString("the_geom"));
        assertEquals(4326, ((Geometry)rs.getObject("the_geom")).getSRID());
        rs.close();
    }
}
