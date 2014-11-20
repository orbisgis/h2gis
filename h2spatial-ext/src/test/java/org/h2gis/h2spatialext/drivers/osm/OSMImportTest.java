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
package org.h2gis.h2spatialext.drivers.osm;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.util.StringUtils;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
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
public class OSMImportTest {

    private static Connection connection;
    private static final String DB_NAME = "OSMImportTest";
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, false);
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
    public void importOSMFile() throws SQLException {
        st.execute("DROP TABLE IF EXISTS OSM_TAG, OSM_NODE, OSM_NODE_TAG, OSM_WAY,OSM_WAY_TAG, OSM_WAY_NODE, OSM_RELATION, OSM_RELATION_TAG, OSM_NODE_MEMBER, OSM_WAY_MEMBER, OSM_RELATION_MEMBER;");
        st.execute("CALL OSMRead(" + StringUtils.quoteStringSQL("/home/ebocher/Téléchargements/nantes_france.osm") + ", 'OSM');");
        ResultSet rs = st.executeQuery("SELECT count(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME LIKE 'OSM%'");
        rs.next();
        assertTrue(rs.getInt(1) == 11);
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(ID_NODE) FROM OSM_NODE");
        rs.next();
        assertTrue(rs.getInt(1) == 3243);
        rs.close();
        // Check content
        
        //NODE
        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=462020579");
        assertTrue(rs.next());
        assertEquals("POINT (-2.1213541 47.6347657)", rs.getString("the_geom"));
        rs.close();        
        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=3003052969");
        assertTrue(rs.next());
        assertEquals("POINT (-2.121123 47.635276)", rs.getString("the_geom"));
        rs.close();
        
        //WAY
        rs = st.executeQuery("SELECT * FROM OSM_WAY WHERE ID_WAY=296521584");
        assertTrue(rs.next());
        assertEquals("LINESTRING (-2.1240567 47.6359494, -2.1243442 47.6359518, -2.1246188 47.6359542)", rs.getString("the_geom"));
        rs.close();
        
        rs = st.executeQuery("SELECT count(ID_RELATION) FROM OSM_RELATION");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        rs.close();
    }
    
    //@Test Disable because of internet connection is not always active
    public void downloadOSMFile() throws SQLException, IOException {
        File file = File.createTempFile("osm_"+ System.currentTimeMillis(), ".osm");    
        file.delete();
        st.execute("CALL ST_OSMDownloader('POLYGON ((-2.12679 47.63418, -2.12679 47.63753, -2.11823 47.63753, -2.11823 47.63418, -2.12679 47.63418))'::GEOMETRY, '"+ file.getPath()+"')");
        assertTrue(new File(file.getPath()).exists());
    }
}
