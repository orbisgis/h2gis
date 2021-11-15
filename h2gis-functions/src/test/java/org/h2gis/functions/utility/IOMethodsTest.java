/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.utility;

import java.io.File;
import java.io.IOException;

import org.h2gis.api.DriverFunction;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.TableLocation;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.io.dbf.DBFRead;
import org.h2gis.functions.io.dbf.DBFWrite;
import org.h2gis.functions.io.shp.SHPRead;
import org.h2gis.functions.io.shp.SHPWrite;
import org.h2gis.functions.io.utility.IOMethods;
import org.h2gis.postgis_jts_osgi.DataSourceFactoryImpl;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.locationtech.jts.geom.Geometry;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Erwan Bocher, CNRS, 2020
 */
public class IOMethodsTest {

    private static Connection connection;
    private Statement st;
    private static final String DB_NAME = "UtilityTest";

    private static final Logger log = LoggerFactory.getLogger(IOMethodsTest.class);

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new SHPRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new SHPWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new DBFWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new DBFRead(), "");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void test_importExportFile() throws Exception {
        File shpFile = new File("target/area_export.shp");
        st.execute("DROP TABLE IF EXISTS AREA");
        st.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        st.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
        // Create a shape file using table area
        IOMethods ioMethods = new IOMethods();
        ioMethods.exportToFile(connection, "AREA", "target/area_export.shp", null, true);
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        String[] tableNames = ioMethods.importFile(connection, shpFile.getAbsolutePath(), "test_table", null, true);
        assertEquals("TEST_TABLE", tableNames[0]);
        ResultSet res = st.executeQuery("SELECT * FROM test_table");
        assertTrue(res.next());
        assertEquals(1, res.getInt(1));
        assertGeometryEquals("MULTIPOLYGON (((-10 9, -10 109, 90 109, 90 9, -10 9)))", (Geometry) res.getObject(2));
        res.close();
    }

    @Test
    public void test_importExportBadFile() throws Exception {
        IOMethods ioMethods = new IOMethods();
        assertThrows(SQLException.class, () -> {
            ioMethods.importFile(connection, "", "test_table", null, true);
        });
        assertThrows(SQLException.class, () -> {
            ioMethods.importFile(connection, "target/area_export.shp", "", null, true);
        });
        assertThrows(SQLException.class, () -> {
            ioMethods.exportToFile(connection, "", "target/area_export.shp", null, true);
        });
        assertThrows(SQLException.class, () -> {
            ioMethods.exportToFile(null, "", "target/area_export.shp", null, true);
        });
        assertThrows(SQLException.class, () -> {
            ioMethods.exportToFile(connection, "table_name", "", null, true);
        });
    }

    @Test
    public void test_importExportFilePOSTGIS(TestInfo testInfo) throws SQLException, IOException {
        IOMethods ioMethods = new IOMethods();
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            File shpFile = new File("target/area_export.shp");
            st.execute("DROP TABLE IF EXISTS AREA");
            st.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            st.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
            // Create a shape file using table area
            st.execute("CALL SHPWrite('target/area_export.shp', 'AREA', true)");
            // Read this shape file to check values
            assertTrue(shpFile.exists());
            String[] tableName = ioMethods.importFile(con, shpFile.getAbsolutePath(), "test_table", null, true);
            assertEquals("test_table", tableName[0]);
            ResultSet res = con.createStatement().executeQuery("SELECT * FROM test_table");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("MULTIPOLYGON (((-10 9, -10 109, 90 109, 90 9, -10 9)))", (Geometry) res.getObject(2));
            res.close();
            ioMethods.exportToFile(con, "test_table", "target/area_export.shp", null, true);
            // Read this shape file to check values
            assertTrue(shpFile.exists());
            ioMethods.importFile(connection, shpFile.getAbsolutePath(), "test_table", null, true);
            res = st.executeQuery("SELECT * FROM test_table");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("MULTIPOLYGON (((-10 9, -10 109, 90 109, 90 9, -10 9)))", (Geometry) res.getObject(2));
            res.close();
        }
    }

    @Test
    public void testExportPOSTGISTableToH2GIS(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        st.execute("DROP TABLE IF EXISTS AREA");
        st.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        st.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            IOMethods.exportToDataBase(connection, "area", con, "area_postgis", -1, 2);
            ResultSet res = con.createStatement().executeQuery("SELECT * FROM area_postgis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            res.close();
        }
    }

    @Test
    public void testExportH2GISTableToPOSTGIS(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            postgisST.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");

            IOMethods.exportToDataBase(con, "area", connection, "area_h2gis", -1, 2);
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            res.close();
        }
    }

    @Test
    public void testExportPostgisTableToH2GISwithSRID(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON, 4326), point GEOMETRY(POINT, 0))");
            postgisST.execute("insert into area values(1, 'SRID=4326;POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 'SRID=0;POINT (-10 109)')");

            IOMethods.exportToDataBase(con, "area", connection, "area_h2gis", -1, 2);
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("SRID=4326;POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            assertGeometryEquals("POINT (-10 109)", (Geometry) res.getObject(3));
            res.close();
            assertEquals(4326, GeometryTableUtilities.getSRID(connection, TableLocation.parse("area_h2gis")));
        }
    }

    @Test
    public void testExportPostgisTableToH2GISwithSRIDMixed(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(GEOMETRY), point GEOMETRY(GEOMETRY))");
            postgisST.execute("insert into area values(1, 'SRID=4326;POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 'SRID=0;POINT (-10 109)')");
            postgisST.execute("insert into area values(2, 'SRID=0;POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 'SRID=4326;POINT (-10 109)')");

            IOMethods.exportToDataBase(con, "area", connection, "area_h2gis", -1, 2);
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("SRID=4326;POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            assertGeometryEquals("POINT (-10 109)", (Geometry) res.getObject(3));
            assertTrue(res.next());
            assertEquals(2, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            assertGeometryEquals("SRID=4326;POINT (-10 109)", (Geometry) res.getObject(3));
            res.close();
        }
    }
    
    @Test
    public void testExportPOSTGISQueryToH2GIS(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            postgisST.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
            postgisST.execute("insert into area values(2, 'POLYGON ((-10 200, 90 109, 90 9, -10 20, -10 200))')");

            IOMethods.exportToDataBase(con, "(SELECT * FROM area where idarea=2)", connection, "area_h2gis", -1, 2);
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(2, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 20, -10 200, 90 109, 90 9, -10 20))", (Geometry) res.getObject(2));
            res.close();
        }
    }

    @Test
    public void testExportH2GISTableToPOSTGISInsert(TestInfo testInfo) throws SQLException, IOException {
        st.execute("DROP TABLE IF EXISTS area_h2gis");
        st.execute("create table area_h2gis(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        st.execute("insert into area_h2gis values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            postgisST.execute("insert into area values(2, 'POLYGON ((-20 109, 90 109, 90 9, -10 9, -20 109))')");

            IOMethods.exportToDataBase(con, "area", connection, "area_h2gis", 1, 2);
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            assertTrue(res.next());
            assertEquals(2, res.getInt(1));
            assertGeometryEquals("POLYGON ((-20 109, 90 109, 90 9, -10 9, -20 109))", (Geometry) res.getObject(2));
            res.close();
        }
    }

    @Test
    public void testExportH2GISTableToPOSTGIS2(TestInfo testInfo) throws Exception {
        st.execute("DROP TABLE IF EXISTS area_h2gis");
        st.execute("create table area_h2gis(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            AtomicReference<Connection> value = new AtomicReference<>();
            value.set(con);
            assertThrows(SQLException.class, () -> {
                IOMethods.exportToDataBase(value.get(), "area", connection, "area_h2gis", 0, 2);
            });
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS area_postgis");
            assertThrows(SQLException.class, () -> {
                IOMethods.exportToDataBase(value.get(), "area_postgis", connection, "area_h2gis", -1, 2);
            });
        }
    }

    @Test
    public void test_linkedFile() throws Exception {
        IOMethods ioMethods = new IOMethods();
        File shpFile = new File("target/area_export.shp");
        st.execute("DROP TABLE IF EXISTS AREA");
        st.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        st.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
        // Create a shape file using table area
        ioMethods.exportToFile(connection, "AREA", "target/area_export.shp", null, true);
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        IOMethods.linkedFile(connection, shpFile.getAbsolutePath(), "test_table", true);
        ResultSet res = st.executeQuery("SELECT * FROM test_table");
        assertTrue(res.next());
        assertEquals(1, res.getInt(1));
        assertGeometryEquals("MULTIPOLYGON (((-10 9, -10 109, 90 109, 90 9, -10 9)))", (Geometry) res.getObject(2));
        res.close();
    }

    @Test
    public void test_linkedFileUnsupported(TestInfo testInfo) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            AtomicReference<Connection> value = new AtomicReference<>();
            value.set(con);
            assertThrows(SQLException.class, () -> {
                IOMethods.linkedFile(value.get(), "target/myfile.shp", "test_table", true);
            });
        }
    }

    @Test
    public void testLinkedTable(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            postgisST.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
            Map<String, String> map = new HashMap<>();
            props.forEach((key, value) -> map.put(key.toString(), value.toString()));
            IOMethods.linkedTable(connection, map, "area", "area_h2gis", true );
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            res.close();
        }
    }
    
    @Test
    public void testLinkedTableQuery(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            postgisST.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
            Map<String, String> map = new HashMap<>();
            props.forEach((key, value) -> map.put(key.toString(), value.toString()));
            IOMethods.linkedTable(connection, map, "(select * from area where idarea=1)", "area_h2gis", true );
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertGeometryEquals("POLYGON ((-10 9, -10 109, 90 109, 90 9, -10 9))", (Geometry) res.getObject(2));
            res.close();
        }
    }

    @Test
    public void testRemoveAddDriver() {
        IOMethods ioMethods = new IOMethods();
        assertTrue(ioMethods.getAllExportDriverSupportedExtensions().contains("shp"));
        DriverFunction df = ioMethods.getExportDriverFromFile(new File("test.shp"));
        assertNotNull(df);
        ioMethods.removeDriver(df);
        assertFalse(ioMethods.getAllExportDriverSupportedExtensions().contains("shp"));
    }
    
    @Test
    public void testExportPOSTGISQueryEmptyResultToH2GIS(TestInfo testInfo) throws SQLException, IOException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            Statement postgisST = con.createStatement();
            postgisST.execute("DROP TABLE IF EXISTS AREA");
            postgisST.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            postgisST.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
            postgisST.execute("insert into area values(2, 'POLYGON ((-10 200, 90 109, 90 9, -10 20, -10 200))')");
            IOMethods.exportToDataBase(con, "(SELECT * FROM area where idarea=3)", connection, "area_h2gis", -1, 2);
            ResultSet res = st.executeQuery("SELECT * FROM area_h2gis");
            assertFalse(res.next());
            res.close();
        }
    }
}
