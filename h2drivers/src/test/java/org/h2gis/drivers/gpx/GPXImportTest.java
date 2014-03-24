/*
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
package org.h2gis.drivers.gpx;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.util.StringUtils;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class GPXImportTest {

    private static Connection connection;
    private static final String DB_NAME = "GPXImportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new GPXRead(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void importGPXWaypoints() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS GPXDATA_WAYPOINT,gpxdata_waypoint");
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
        rs.close();
        st.execute("drop table GPXDATA_WAYPOINT");
    }

    @Test
    public void importGPXRoute() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS GPXDATA_ROUTE, GPXDATA_ROUTEPOINT;");
        st.execute("CALL GPXRead(" + StringUtils.quoteStringSQL(GPXImportTest.class.getResource("route.gpx").getPath()) + ", 'gpxdata');");
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
        st.execute("drop table GPXDATA_ROUTE,  GPXDATA_ROUTEPOINT;");
    }

    @Test
    public void importGPXTrack() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS GPXDATA_TRACK, GPXDATA_TRACKSEGMENT,GPXDATA_TRACKPOINT,gpxdata_route;");
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
        st.execute("DROP TABLE GPXDATA_TRACK, GPXDATA_TRACKSEGMENT,GPXDATA_TRACKPOINT;");
    }
    
    
    @Test
    public void importGPXWaypointsFileName() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WAYPOINT_WAYPOINT");
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
        st.execute("drop table WAYPOINT_WAYPOINT");
    }
}
