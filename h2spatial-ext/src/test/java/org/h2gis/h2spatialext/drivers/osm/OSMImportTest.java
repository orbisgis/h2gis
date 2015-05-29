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
package org.h2gis.h2spatialext.drivers.osm;

import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import static org.junit.Assert.assertFalse;
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
    public void importBz2OSMFile() throws SQLException {
        st.execute("DROP TABLE IF EXISTS OSM_TAG, OSM_NODE, OSM_NODE_TAG, OSM_WAY,OSM_WAY_TAG, OSM_WAY_NODE, OSM_RELATION, OSM_RELATION_TAG, OSM_NODE_MEMBER, OSM_WAY_MEMBER, OSM_RELATION_MEMBER;");
        st.execute("CALL OSMRead(" + StringUtils.quoteStringSQL(OSMImportTest.class.getResource("saint_jean.osm.bz2").getPath()) + ", 'OSM');");
        ResultSet rs = st.executeQuery("SELECT count(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME LIKE 'OSM%'");
        rs.next();
        assertTrue(rs.getInt(1) == 11);
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(ID_NODE) FROM OSM_NODE");
        rs.next();
        assertEquals(3243, rs.getInt(1));
        rs.close();
        // Check content

        //NODE
        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=462020579");
        assertTrue(rs.next());
        assertEquals("POINT (-2.1213541 47.6347657)", rs.getString("the_geom"));
        rs.close();

        rs = st.executeQuery("SELECT * FROM OSM_NODE WHERE ID_NODE=670177172");
        assertTrue(rs.next());
        // NODE Z extraction
        assertEquals(91.9,rs.getDouble("ELE"),0.1);
        assertFalse(rs.wasNull());
        assertEquals(4326,((Point)rs.getObject("THE_GEOM")).getSRID());
        // Node SRID extraction
        rs.close();

        // Geometry columns SRID information
        rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME='OSM_NODE'");
        assertTrue(rs.next());
        assertEquals(4326, rs.getInt("SRID"));

        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=3003052969");
        assertTrue(rs.next());
        assertEquals("POINT (-2.121123 47.635276)", rs.getString("the_geom"));
        rs.close();

        rs = st.executeQuery("SELECT count(ID_RELATION) FROM OSM_RELATION");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        rs.close();
    }

    @Test
    public void importGzipOSMFile() throws SQLException {
        st.execute("DROP TABLE IF EXISTS OSM_TAG, OSM_NODE, OSM_NODE_TAG, OSM_WAY,OSM_WAY_TAG, OSM_WAY_NODE, OSM_RELATION, OSM_RELATION_TAG, OSM_NODE_MEMBER, OSM_WAY_MEMBER, OSM_RELATION_MEMBER;");
        st.execute("CALL OSMRead(" + StringUtils.quoteStringSQL(OSMImportTest.class.getResource("saint_jean.osm.gz").getPath()) + ", 'OSM');");
        ResultSet rs = st.executeQuery("SELECT count(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME LIKE 'OSM%'");
        rs.next();
        assertTrue(rs.getInt(1) == 11);
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(ID_NODE) FROM OSM_NODE");
        rs.next();
        assertEquals(3243, rs.getInt(1));
        rs.close();
        // Check content

        //NODE
        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=462020579");
        assertTrue(rs.next());
        assertEquals("POINT (-2.1213541 47.6347657)", rs.getString("the_geom"));
        rs.close();

        rs = st.executeQuery("SELECT * FROM OSM_NODE WHERE ID_NODE=670177172");
        assertTrue(rs.next());
        // NODE Z extraction
        assertEquals(91.9,rs.getDouble("ELE"),0.1);
        assertFalse(rs.wasNull());
        assertEquals(4326,((Point)rs.getObject("THE_GEOM")).getSRID());
        // Node SRID extraction
        rs.close();

        // Geometry columns SRID information
        rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME='OSM_NODE'");
        assertTrue(rs.next());
        assertEquals(4326, rs.getInt("SRID"));

        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=3003052969");
        assertTrue(rs.next());
        assertEquals("POINT (-2.121123 47.635276)", rs.getString("the_geom"));
        rs.close();

        rs = st.executeQuery("SELECT count(ID_RELATION) FROM OSM_RELATION");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        rs.close();
    }
    
    @Test
    public void importOSMFile() throws SQLException {
        st.execute("DROP TABLE IF EXISTS OSM_TAG, OSM_NODE, OSM_NODE_TAG, OSM_WAY,OSM_WAY_TAG, OSM_WAY_NODE, OSM_RELATION, OSM_RELATION_TAG, OSM_NODE_MEMBER, OSM_WAY_MEMBER, OSM_RELATION_MEMBER;");
        st.execute("CALL OSMRead(" + StringUtils.quoteStringSQL(OSMImportTest.class.getResource("saint_jean.osm").getPath()) + ", 'OSM');");
        ResultSet rs = st.executeQuery("SELECT count(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME LIKE 'OSM%'");
        rs.next();
        assertTrue(rs.getInt(1) == 11);
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(ID_NODE) FROM OSM_NODE");
        rs.next();
        assertEquals(3243, rs.getInt(1));
        rs.close();
        // Check content
        
        //NODE
        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=462020579");
        assertTrue(rs.next());
        assertEquals("POINT (-2.1213541 47.6347657)", rs.getString("the_geom"));
        rs.close();

        rs = st.executeQuery("SELECT * FROM OSM_NODE WHERE ID_NODE=670177172");
        assertTrue(rs.next());
        // NODE Z extraction
        assertEquals(91.9,rs.getDouble("ELE"),0.1);
        assertFalse(rs.wasNull());
        assertEquals(4326,((Point)rs.getObject("THE_GEOM")).getSRID());
        // Node SRID extraction
        rs.close();

        // Geometry columns SRID information
        rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME='OSM_NODE'");
        assertTrue(rs.next());
        assertEquals(4326, rs.getInt("SRID"));

        rs = st.executeQuery("SELECT THE_GEOM FROM OSM_NODE WHERE ID_NODE=3003052969");
        assertTrue(rs.next());
        assertEquals("POINT (-2.121123 47.635276)", rs.getString("the_geom"));
        rs.close();
        
        rs = st.executeQuery("SELECT count(ID_RELATION) FROM OSM_RELATION");
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
        rs.close();
    }
    
    //Disable this @Test to avoid internet connection error
    public void downloadOSMFile() throws SQLException, IOException {
        if(IsNetworkAvailable()){
        File file = File.createTempFile("osm_"+ System.currentTimeMillis(), ".osm");    
        file.delete();
        st.execute("CALL ST_OSMDownloader('POLYGON ((-2.12679 47.63418, -2.12679 47.63753, -2.11823 47.63753, -2.11823 47.63418, -2.12679 47.63418))'::GEOMETRY, '"+ file.getPath()+"')");
        assertTrue(new File(file.getPath()).exists());
        }
    }
    
    //Disable this @Test to avoid internet connection error
    public void downloadOSMFile2() throws SQLException, IOException {
        if(IsNetworkAvailable()){
        File file = File.createTempFile("osm2_"+ System.currentTimeMillis(), ".osm");    
        file.delete();
        st.execute("CALL ST_OSMDownloader('POLYGON ((-2.130192869203905 47.633867888575935, -2.1318522937536533 47.640236490902, -2.1233757737562904 47.64032618952631, -2.1196532808473956 47.63960860053182, -2.1203708698418815 47.63377818995163, -2.130192869203905 47.633867888575935))'::GEOMETRY, '"+ file.getPath()+"')");
        assertTrue(new File(file.getPath()).exists());
        }
    }
    
    //Disable this @Test to avoid internet connection error
    public void downloadOSMFileAndImport() throws SQLException, IOException {
        if(IsNetworkAvailable()){
        File file = File.createTempFile("osm3_"+ System.currentTimeMillis(), ".osm");    
        file.delete();
        st.execute("CALL ST_OSMDownloader('POLYGON ((-2.12679 47.63418, -2.12679 47.63753, -2.11823 47.63753, -2.11823 47.63418, -2.12679 47.63418))'::GEOMETRY, '"+ file.getPath()+"')");
        assertTrue(new File(file.getPath()).exists());
        st.execute("DROP TABLE IF EXISTS OSM_TAG, OSM_NODE, OSM_NODE_TAG, OSM_WAY,OSM_WAY_TAG, OSM_WAY_NODE, OSM_RELATION, OSM_RELATION_TAG, OSM_NODE_MEMBER, OSM_WAY_MEMBER, OSM_RELATION_MEMBER;");
        st.execute("CALL OSMRead(" + StringUtils.quoteStringSQL(file.getPath()) + ", 'OSM');");
        ResultSet rs = st.executeQuery("SELECT count(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME LIKE 'OSM%'");
        rs.next();
        assertTrue(rs.getInt(1) == 11);
        rs.close();
        // Check number
        rs = st.executeQuery("SELECT count(ID_NODE) FROM OSM_NODE");
        rs.next();
        assertEquals(3245, rs.getInt(1));
        rs.close();
        }
    }
    
    /**
     * A method to test if the internet network is active.
     *
     * @return
     */
    public static boolean IsNetworkAvailable() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }
}
