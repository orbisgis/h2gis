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
package org.h2gis.functions.spatial.metadata;

import org.h2.util.StringUtils;
import org.h2gis.functions.TestUtilities;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.shp.SHPEngineTest;
import org.h2gis.postgis_jts_osgi.DataSourceFactoryImpl;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.h2.jdbc.JdbcSQLException;
import org.h2gis.utilities.GeometryMetaData;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.Tuple;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import org.h2gis.functions.spatial.crs.UpdateGeometrySRID;
import org.h2gis.utilities.JDBCUtilities;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryTableUtilsTest {

    private static Connection connection;
    private static Connection conPost;
    private Statement st;
    private static final Logger log = LoggerFactory.getLogger(GeometryTableUtilsTest.class);

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(GeometryTableUtilsTest.class.getSimpleName());
        connection.createStatement().execute("DROP TABLE IF EXISTS NOGEOM");
        connection.createStatement().execute("CREATE TABLE NOGEOM (id INT, str VARCHAR(100))");
        connection.createStatement().execute("INSERT INTO NOGEOM VALUES (25, 'twenty five')");
        connection.createStatement().execute("INSERT INTO NOGEOM VALUES (6, 'six')");

        connection.createStatement().execute("DROP TABLE IF EXISTS POINTTABLE");
        connection.createStatement().execute("CREATE TABLE POINTTABLE (geom GEOMETRY)");
        connection.createStatement().execute("INSERT INTO POINTTABLE VALUES ('POINT(1 1)')");

        connection.createStatement().execute("DROP TABLE IF EXISTS GEOMTABLE");
        connection.createStatement().execute("CREATE TABLE GEOMTABLE (geom GEOMETRY, pt GEOMETRY(POINTZM), linestr GEOMETRY(LINESTRING), "
                + "plgn GEOMETRY(POLYGON), multipt GEOMETRY(MULTIPOINT), multilinestr GEOMETRY(MULTILINESTRING), multiplgn GEOMETRY(MULTIPOLYGON), "
                + "geomcollection GEOMETRY(GEOMETRYCOLLECTION))");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('POINT(1 1)', 'POINT(1 1 0 0)',"
                + " 'LINESTRING(1 1, 2 2)', 'POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 'MULTIPOINT((1 1))',"
                + " 'MULTILINESTRING((1 1, 2 2))', 'MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))',"
                + " 'GEOMETRYCOLLECTION(POINT(1 1))')");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('LINESTRING(1 1, 2 2)', 'POINT(2 2 0 0)',"
                + " 'LINESTRING(2 2, 1 1)', 'POLYGON((1 1, 1 3, 3 3, 3 1, 1 1))', 'MULTIPOINT((3 3))',"
                + " 'MULTILINESTRING((1 1, 3 3))', 'MULTIPOLYGON(((1 1, 1 3, 3 3, 3 1, 1 1)))',"
                + " 'GEOMETRYCOLLECTION(POINT(3 3))')");

        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();

        DataSource ds = dataSourceFactory.createDataSource(props);
        conPost = ds.getConnection();
        if (conPost == null) {
            System.setProperty("postgresql", "false");
        } else {
            System.setProperty("postgresql", "true");
        }
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
    public void testGeometryMetadataFunctions() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY)");
        st.execute("DROP VIEW IF EXISTS geo_cols; CREATE VIEW geo_cols AS "
                + "SELECT  f_table_catalog, "
                + " f_table_schema, "
                + " f_table_name, "
                + " f_geometry_column, "
                + "1 storage_type, "
                + "t[1] as geometry_type, "
                + "t[2] as coord_dimension, "
                + "t[3] as srid, "
                + "t[4] as type "
                + "FROM (SELECT TABLE_CATALOG f_table_catalog, "
                + "TABLE_SCHEMA f_table_schema, "
                + "TABLE_NAME f_table_name, "
                + "COLUMN_NAME f_geometry_column, "
                + "1 storage_type, FindGeometryMetadata(TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME, "
                + " COLUMN_NAME, COLUMN_TYPE) as t FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TYPE_NAME = 'GEOMETRY'); ");
        ResultSet rs = st.executeQuery("SELECT * FROM geo_cols");
        rs.next();
        assertEquals("DBH2GEOMETRYTABLEUTILSTEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_POINT", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(0, rs.getInt("geometry_type"));
        assertEquals(2, rs.getInt("coord_dimension"));
        assertEquals(0, rs.getInt("srid"));
        assertEquals("GEOMETRY", rs.getString("type"));

        //Alter the geometry
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINT Z, 4326)");
        rs = st.executeQuery("SELECT * FROM geo_cols");
        rs.next();
        assertEquals("DBH2GEOMETRYTABLEUTILSTEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_POINT", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(1001, rs.getInt("geometry_type"));
        assertEquals(3, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("POINTZ", rs.getString("type"));

        st.execute("drop table if exists geo_linestring; CREATE TABLE geo_linestring (the_geom GEOMETRY(LINESTRINGZM, 4326))");
        rs = st.executeQuery("SELECT * FROM geo_cols where f_table_name= 'GEO_LINESTRING' ");
        rs.next();
        assertEquals("DBH2GEOMETRYTABLEUTILSTEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_LINESTRING", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(3002, rs.getInt("geometry_type"));
        assertEquals(4, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("LINESTRINGZM", rs.getString("type"));
        st.execute("DROP VIEW IF EXISTS geo_cols");
    }

    @Test
    public void testGeometryMetadataUtils() throws Exception {
        TableLocation location=TableLocation.parse("GEO_POINT",DBTypes.H2);
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY)");
        GeometryMetaData geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("GEOMETRY", geomMetadata.geometryType);
        assertEquals("GEOMETRY", geomMetadata.sfs_geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINT Z, 4326)");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZM)");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
    }

    @Test
    public void testGeometryMetadataUtils2() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY, geom GEOMETRY(POINT Z,4326))");
        LinkedHashMap<String, GeometryMetaData> geomMetadatas = GeometryTableUtilities.getMetaData(connection, TableLocation.parse("GEO_POINT", DBTypes.H2));
        Set<Map.Entry<String, GeometryMetaData>> elements = geomMetadatas.entrySet();
        Iterator<Map.Entry<String, GeometryMetaData>> iterator = elements.iterator();
        Map.Entry<String, GeometryMetaData> geomMetWithField = iterator.next();
        assertEquals("THE_GEOM", geomMetWithField.getKey());
        GeometryMetaData geomMetadata = geomMetWithField.getValue();
        assertEquals("GEOMETRY", geomMetadata.geometryType);
        assertEquals("GEOMETRY", geomMetadata.sfs_geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geomMetWithField = iterator.next();
        assertEquals("GEOM", geomMetWithField.getKey());
        geomMetadata = geomMetWithField.getValue();
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZM)");
        geomMetadatas = GeometryTableUtilities.getMetaData(connection, TableLocation.parse("GEO_POINT", DBTypes.H2));
        elements = geomMetadatas.entrySet();
        geomMetWithField = elements.iterator().next();
        assertEquals("THE_GEOM", geomMetWithField.getKey());
        geomMetadata = geomMetWithField.getValue();
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
    }

    @Test
    public void testFirstGeometryFieldName1() throws Exception {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        st.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12)', 27582))");
        ResultSet rs = st.executeQuery("SELECT * from POINT3D;");
        Tuple<String, Integer> geomField = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(rs);
        assertEquals("THE_GEOM", geomField.first());
        assertEquals(2, geomField.second());
    }

    @Test
    public void testFirstGeometryFieldName2() throws Throwable {
        assertThrows(SQLException.class, () -> {
            try {
                st.execute("DROP TABLE IF EXISTS POINT3D");
                st.execute("CREATE TABLE POINT3D (gid int )");
                st.execute("INSERT INTO POINT3D (gid) VALUES(1)");
                ResultSet rs = st.executeQuery("SELECT * from POINT3D;");
                GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(rs);
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testHasGeometryField() throws SQLException {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM POINT3D");
        assertTrue(GeometryTableUtilities.hasGeometryColumn(rs));
        assertTrue(GeometryTableUtilities.hasGeometryColumn(connection, TableLocation.parse("POINT3D",DBTypes.H2GIS)));
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int)");
        rs = connection.createStatement().executeQuery("SELECT * FROM POINT3D");
        assertFalse(GeometryTableUtilities.hasGeometryColumn(rs));
        st.execute("DROP SCHEMA IF EXISTS ORBISGIS CASCADE");
        st.execute("CREATE SCHEMA ORBISGIS;");
        st.execute("CREATE TABLE ORBISGIS.POINT3D (gid int , the_geom GEOMETRY)");
        assertTrue(GeometryTableUtilities.hasGeometryColumn(connection, TableLocation.parse("ORBISGIS.POINT3D",DBTypes.H2GIS)));
    }

    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testHasGeometryFieldPostGIS() throws SQLException {
        Statement stat = conPost.createStatement();
        stat.execute("DROP TABLE IF EXISTS POINT3D");
        stat.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        ResultSet rs = conPost.createStatement().executeQuery("SELECT * FROM POINT3D");
        assertTrue(GeometryTableUtilities.hasGeometryColumn(rs));
        assertTrue(GeometryTableUtilities.hasGeometryColumn(conPost, TableLocation.parse("point3d",DBTypes.POSTGIS)));
        stat.execute("DROP TABLE IF EXISTS POINT3D");
        stat.execute("CREATE TABLE POINT3D (gid int)");
        rs = conPost.createStatement().executeQuery("SELECT * FROM POINT3D");
        assertFalse(GeometryTableUtilities.hasGeometryColumn(rs));
        stat.execute("DROP SCHEMA IF EXISTS ORBISGIS CASCADE");
        stat.execute("CREATE SCHEMA ORBISGIS;");
        stat.execute("CREATE TABLE ORBISGIS.POINT3D (gid int , the_geom GEOMETRY)");
        assertTrue(GeometryTableUtilities.hasGeometryColumn(conPost, TableLocation.parse("orbisgis.point3d",DBTypes.POSTGIS)));
        stat.execute("DROP SCHEMA IF EXISTS ORBISGIS CASCADE");        
    }

    @Test
    public void testResultSetEnvelope1() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0), GeometryTableUtilities.getEnvelope(rs).getEnvelopeInternal());
    }

    @Test
    public void testResultSetEnvelope2() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertThrows(SQLException.class, ()
                -> GeometryTableUtilities.getEnvelope(rs));
    }

    @Test
    public void testResultSetEnvelope3() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0), GeometryTableUtilities.getEnvelope(rs, "GEOM").getEnvelopeInternal());
        rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0), GeometryTableUtilities.getEnvelope(rs, "MULTILINESTR").getEnvelopeInternal());
    }

    @Test
    public void testResultSetEnvelope4() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertThrows(SQLException.class, ()
                -> GeometryTableUtilities.getEnvelope(rs, "GEOM"));
    }

    @Test
    public void testGeometryFields1() throws SQLException {
        ArrayList geomColumns = new ArrayList();
        geomColumns.add("GEOM");
        geomColumns.add(("PT"));
        geomColumns.add("LINESTR");
        geomColumns.add("PLGN");
        geomColumns.add("MULTIPT");
        geomColumns.add("MULTILINESTR");
        geomColumns.add("MULTIPLGN");
        geomColumns.add("GEOMCOLLECTION");
        LinkedHashMap<String, Integer> geomFieldNameIndex = GeometryTableUtilities.getGeometryColumnNamesAndIndexes(connection, TableLocation.parse("GEOMTABLE", DBTypes.H2));
        assertEquals(8, geomFieldNameIndex.size());
        assertNotNull(geomFieldNameIndex.keySet().stream()
                .filter(columName -> geomColumns.contains(columName))
                .findAny()
                .orElse(null));
    }

    @Test
    public void testGeometryFields3() throws SQLException {
        ArrayList geomColumns = new ArrayList();
        geomColumns.add("GEOM");
        geomColumns.add(("PT"));
        geomColumns.add("LINESTR");
        geomColumns.add("PLGN");
        geomColumns.add("MULTIPT");
        geomColumns.add("MULTILINESTR");
        geomColumns.add("MULTIPLGN");
        geomColumns.add("GEOMCOLLECTION");
        List<String> geomFieldNameIndex = GeometryTableUtilities.getGeometryColumnNames(connection, TableLocation.parse("GEOMTABLE", DBTypes.H2));
        assertEquals(8, geomFieldNameIndex.size());
        assertNotNull(geomFieldNameIndex.stream()
                .filter(columName -> geomColumns.contains(columName))
                .findAny()
                .orElse(null));
    }

    @Test
    public void testGeometryFields4() throws SQLException {
        ArrayList geomColumns = new ArrayList();
        geomColumns.add("GEOM");
        geomColumns.add(("PT"));
        geomColumns.add("MULTIPLGN");
        ResultSet rs = st.executeQuery("SELECT GEOM, PT,  MULTIPLGN FROM GEOMTABLE");
        List<String> geomFieldNameIndex = GeometryTableUtilities.getGeometryColumnNames(rs.getMetaData());
        assertEquals(3, geomFieldNameIndex.size());
        assertNotNull(geomFieldNameIndex.stream()
                .filter(columName -> geomColumns.contains(columName))
                .findAny()
                .orElse(null));
    }

    @Test
    public void testGetSRID() throws SQLException {
        assertEquals(0, GeometryTableUtilities.getSRID(connection, TableLocation.parse("GEOMTABLE")));
        assertEquals(0, GeometryTableUtilities.getSRID(connection, TableLocation.parse("NOGEOM")));
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY(POINTZ, 4326))");
        assertEquals(4326, GeometryTableUtilities.getSRID(connection, TableLocation.parse("POINT3D")));

    }

    /**
     * Check constraint pass
     *
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns3() throws SQLException {
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (GEOMETRY, 4326))");
        st.execute("insert into T_SRID VALUES(ST_GEOMFROMTEXT('POINT (2 47)',4326))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt("srid"));
            assertFalse(rs.next());
        }
        assertEquals(4326, GeometryTableUtilities.getSRID(connection, TableLocation.parse("T_SRID")));
    }

    /**
     * Check constraint pass
     *
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY(GEOMETRY, 27572))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(27572, rs.getInt("srid"));
            assertFalse(rs.next());
        }
    }

    /**
     * Check constraint pass
     *
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns2() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (GEOMETRY, 27572))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(27572, rs.getInt("srid"));
            assertFalse(rs.next());
        }
    }

    /**
     * Check constraint pass
     *
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns4() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (POINT, 4326))");
        st.execute("insert into T_SRID VALUES(ST_GEOMFROMTEXT('POINT (2 47)',4326))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt("srid"));
            assertFalse(rs.next());
        }
        assertEquals(4326, GeometryTableUtilities.getSRID(connection, TableLocation.parse("T_SRID")));
    }

    /**
     * Check constraint pass
     *
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns5() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (MULTIPOLYGON, 4326))");
        st.execute("insert into T_SRID VALUES(ST_GEOMFROMTEXT('MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 4326))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt("srid"));
            assertFalse(rs.next());
        }
        assertEquals(4326, GeometryTableUtilities.getSRID(connection, TableLocation.parse("T_SRID")));
    }

    @Test
    public void testSFSUtilities() throws Exception {
        String catalog = connection.getCatalog();
        st.execute("drop schema if exists blah");
        st.execute("create schema blah");
        st.execute("create table blah.testSFSUtilities(id integer, the_geom GEOMETRY(point))");
        LinkedHashMap<String, Integer> geomFields = GeometryTableUtilities.getGeometryColumnNamesAndIndexes(connection, TableLocation.parse("blah.testSFSUtilities", DBTypes.H2GIS));
        assertEquals(1, geomFields.size());
        Map.Entry<String, Integer> entry = geomFields.entrySet().iterator().next();
        assertEquals("THE_GEOM", entry.getKey());
        assertEquals(2, entry.getValue());
    }

    @Test
    public void testTableEnvelope() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE", DBTypes.H2GIS);
        assertThrows(SQLException.class, ()
                -> GeometryTableUtilities.getEnvelope(connection, tableLocation, ""));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "GEOM").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "PT").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "LINESTR").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "PLGN").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "MULTIPT").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "MULTILINESTR").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "MULTIPLGN").getEnvelopeInternal());
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtilities.getEnvelope(connection, tableLocation, "GEOMCOLLECTION").getEnvelopeInternal());
    }

    @Test
    public void testBadTableEnvelope() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("NOGEOM", DBTypes.H2GIS);
        assertThrows(SQLException.class, ()
                -> GeometryTableUtilities.getEnvelope(connection, tableLocation, ""));
    }

    @Test
    public void testEstimatedExtentWithoutIndex() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE",DBTypes.H2GIS);
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtilities.getEstimatedExtent(connection, tableLocation, "GEOM").getEnvelopeInternal());
    }

    @Test
    public void testEstimatedExtentWithIndex() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS GEOMTABLE_INDEX; CREATE TABLE GEOMTABLE_INDEX (THE_GEOM GEOMETRY);");
        st.execute("INSERT INTO GEOMTABLE_INDEX VALUES ('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))'),('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))' )");
        st.execute("CREATE SPATIAL INDEX ON GEOMTABLE_INDEX(THE_GEOM)");
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE_INDEX", DBTypes.H2GIS);
        assertEquals(new Envelope(150.0, 240.0, 250.0, 360.0),
                GeometryTableUtilities.getEstimatedExtent(connection, tableLocation, "THE_GEOM").getEnvelopeInternal());
    }

    @Test
    public void testGeometryType() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE", DBTypes.H2);
        assertEquals(GeometryTypeCodes.GEOMETRY,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "GEOM").geometryTypeCode);
        assertEquals(GeometryTypeCodes.POINTZM,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "PT").geometryTypeCode);
        assertEquals(GeometryTypeCodes.LINESTRING,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "LINESTR").geometryTypeCode);
        assertEquals(GeometryTypeCodes.POLYGON,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "PLGN").geometryTypeCode);
        assertEquals(GeometryTypeCodes.MULTIPOINT,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "MULTIPT").geometryTypeCode);
        assertEquals(GeometryTypeCodes.MULTILINESTRING,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "MULTILINESTR").geometryTypeCode);
        assertEquals(GeometryTypeCodes.MULTIPOLYGON,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "MULTIPLGN").geometryTypeCode);
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION,
                GeometryTableUtilities.getMetaData(connection, tableLocation, "GEOMCOLLECTION").geometryTypeCode);
    }

    @Test
    public void testGeometryTypeNoGeomTableEmptyField() throws SQLException {
        assertThrows(SQLException.class, ()
                -> GeometryTableUtilities.getMetaData(connection, TableLocation.parse("NOGEOM"), ""));
    }

    @Test
    public void testGeometryTypeNoGeomTable() throws SQLException {
        assertNull(GeometryTableUtilities.getMetaData(connection, TableLocation.parse("NOGEOM", DBTypes.H2), "id"));
    }

    @Test
    public void testGeometryTypeNotValidField() throws SQLException {
        assertNull(GeometryTableUtilities.getMetaData(connection, TableLocation.parse("NOGEOM", DBTypes.H2), "notAField"));
    }

    @Test
    public void testPrepareInformationSchemaStatement() throws SQLException {
        PreparedStatement ps = GeometryTableUtilities.prepareInformationSchemaStatement(connection, "cat", "sch", "tab",
                "INFORMATION_SCHEMA.TABLE_CONSTRAINTS", "limit 1", "TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME");
        assertEquals(ps.toString().substring(ps.toString().indexOf(": ") + 2), "SELECT * from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where UPPER(TABLE_CATALOG) "
                + "= ? AND UPPER(TABLE_SCHEMA) = ? AND UPPER(TABLE_NAME) = ? limit 1 {1: 'CAT', 2: 'SCH', 3: 'TAB'}");
    }

    @Test
    public void testPrepareInformationSchemaStatement2() throws SQLException {
        PreparedStatement ps = GeometryTableUtilities.prepareInformationSchemaStatement(connection, "cat", "sch", "tab",
                "geometry_columns", "limit 1");
        assertEquals(ps.toString().substring(ps.toString().indexOf(": ") + 2), "SELECT * from geometry_columns where UPPER(f_table_catalog) "
                + "= ? AND UPPER(f_table_schema) = ? AND UPPER(f_table_name) = ? limit 1 {1: 'CAT', 2: 'SCH', 3: 'TAB'}");
    }

    @Test
    public void testGetMetadataFromResulset() throws SQLException {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY(POINTZ, 4326))");
        st.execute("insert into POINT3D VALUES(1, 'SRID=4326;POINTZ(0 0 0)')");
        Tuple<String, GeometryMetaData> metadata = GeometryTableUtilities.getFirstColumnMetaData(st.executeQuery("select * from POINT3D"));
        assertEquals("THE_GEOM", metadata.first());
        GeometryMetaData geomMet = metadata.second();
        assertEquals(0, geomMet.SRID);
        assertEquals(2, geomMet.dimension);
        assertFalse(geomMet.hasM);
        assertFalse(geomMet.hasZ);
        assertEquals("GEOMETRY", geomMet.geometryType);
        assertEquals(GeometryTypeCodes.GEOMETRY, geomMet.geometryTypeCode);
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        st.execute("insert into POINT3D VALUES(1, 'SRID=4326;POINTZ(0 0 0)')");
        metadata = GeometryTableUtilities.getFirstColumnMetaData(st.executeQuery("select * from POINT3D"));
        assertEquals("THE_GEOM", metadata.first());
        geomMet = metadata.second();
        assertEquals(0, geomMet.SRID);
        assertEquals(2, geomMet.dimension);
        assertFalse(geomMet.hasM);
        assertFalse(geomMet.hasZ);
        assertEquals("GEOMETRY", geomMet.geometryType);
        assertEquals(GeometryTypeCodes.GEOMETRY, geomMet.geometryTypeCode);
    }

    @Test
    public void testGetMetadatasFromResulset() throws SQLException {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , geom GEOMETRY(POINTZ, 4326), the_geom GEOMETRY)");
        st.execute("insert into POINT3D VALUES(1, 'SRID=4326;POINTZ(0 0 0)', 'SRID=4326;POINTZ(0 0 0)')");
        LinkedHashMap<String, GeometryMetaData> metadata = GeometryTableUtilities.getMetaData(st.executeQuery("select * from POINT3D"));
        Set<Map.Entry<String, GeometryMetaData>> entries = metadata.entrySet();
        assertEquals(2, metadata.size());
        Iterator<Map.Entry<String, GeometryMetaData>> iterator = entries.iterator();
        Map.Entry<String, GeometryMetaData> entry = iterator.next();
        assertEquals("GEOM", entry.getKey());
        GeometryMetaData geomMet = entry.getValue();
        assertEquals(0, geomMet.SRID);
        assertEquals(2, geomMet.dimension);
        assertFalse(geomMet.hasM);
        assertFalse(geomMet.hasZ);
        assertEquals("GEOMETRY", geomMet.geometryType);
        assertEquals(GeometryTypeCodes.GEOMETRY, geomMet.geometryTypeCode);
        entry = iterator.next();
        assertEquals("THE_GEOM", entry.getKey());
        geomMet = entry.getValue();
        assertEquals(0, geomMet.SRID);
        assertEquals(2, geomMet.dimension);
        assertFalse(geomMet.hasM);
        assertFalse(geomMet.hasZ);
        assertEquals("GEOMETRY", geomMet.geometryType);
        assertEquals(GeometryTypeCodes.GEOMETRY, geomMet.geometryTypeCode);
    }

    @Test
    public void testGeometryMetadataSQL() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY)");
        TableLocation tableLocation = TableLocation.parse("GEO_POINT", DBTypes.H2GIS);
        GeometryMetaData geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals("GEOMETRY", geomMetadata.getSQL());
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINT Z, 4326)");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZM)");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZM)", geomMetadata.getSQL());
    }

    @Test
    public void testAlterSRID() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINT))");
        st.execute("insert into geo_point VALUES('POINT(0 0)')");
        TableLocation tableLocation = TableLocation.parse("GEO_POINT", DBTypes.H2GIS);
        GeometryMetaData geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals(0, geomMetadata.getSRID());
        GeometryTableUtilities.alterSRID(connection, tableLocation, "THE_GEOM", 4326);
        geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals("GEOMETRY(POINT,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINTZ))");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals(0, geomMetadata.getSRID());
        GeometryTableUtilities.alterSRID(connection, tableLocation, "THE_GEOM", 4326);
        geomMetadata = GeometryTableUtilities.getMetaData(connection,tableLocation, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINTZ, 2154))");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals(2154, geomMetadata.getSRID());
        GeometryTableUtilities.alterSRID(connection, tableLocation, "THE_GEOM", 4326);
        geomMetadata = GeometryTableUtilities.getMetaData(connection, tableLocation, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());
    }

    @Test
    public void testUpdateSRIDFunction() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINT))");
        st.execute("insert into geo_point VALUES('SRID=0;POINT(0 0)')");
        TableLocation location=TableLocation.parse("GEO_POINT", DBTypes.H2);
        GeometryMetaData geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals(0, geomMetadata.getSRID());
        st.execute("SELECT UpdateGeometrySRID('GEO_POINT','THE_GEOM',4326);");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("GEOMETRY(POINT,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());
        ResultSet res = st.executeQuery("select * from geo_point");
        res.next();
        assertEquals(4326, ((Geometry) res.getObject(1)).getSRID());
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINTZ))");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals(0, geomMetadata.getSRID());
        st.execute("SELECT UpdateGeometrySRID('GEO_POINT','THE_GEOM',4326);");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINTZ, 2154))");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals(2154, geomMetadata.getSRID());
        st.execute("SELECT UpdateGeometrySRID('GEO_POINT','THE_GEOM',4326);");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());
        st.execute("SELECT UpdateGeometrySRID('GEO_POINT','the_geom',4326);");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID());  
        st.execute("SELECT UpdateGeometrySRID('geo_point','the_geom',4326);");
        geomMetadata = GeometryTableUtilities.getMetaData(connection, location, "THE_GEOM");
        assertEquals("GEOMETRY(POINTZ,4326)", geomMetadata.getSQL());
        assertEquals(4326, geomMetadata.getSRID()); 
    }
    
    @Test
    public void testUpdateSRIDFunctionResponse() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY(POINT))");
        st.execute("insert into geo_point VALUES('SRID=0;POINT(0 0)')");
        assertTrue(UpdateGeometrySRID.changeSRID(connection, "GEO_POINT", "THE_GEOM",4326));
        assertFalse(UpdateGeometrySRID.changeSRID(connection, "GEO_POINT", "THE_GEOM",4326));
        assertTrue(UpdateGeometrySRID.changeSRID(connection, "GEO_POINT", "THE_GEOM",0));
    }
    
    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testEstimatedExtentPostGIS() throws SQLException {
        Statement statement = conPost.createStatement();
        statement.execute("DROP TABLE IF EXISTS PUBLIC.GEOMTABLE; CREATE TABLE PUBLIC.GEOMTABLE (THE_GEOM GEOMETRY(GEOMETRY, 4326));");
        statement.execute("INSERT INTO PUBLIC.GEOMTABLE VALUES (ST_GeomFromText('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))', 4326)),(ST_GeomFromText('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))', 4326) )");
        statement.execute("ANALYZE PUBLIC.GEOMTABLE");
        TableLocation tableLocation = TableLocation.parse("geomtable", DBTypes.POSTGIS);
        Geometry geom = GeometryTableUtilities.getEstimatedExtent(conPost, tableLocation, "the_geom");
        assertNotNull(geom);
        assertEquals(4326, geom.getSRID());        
        statement.execute("DROP TABLE IF EXISTS PUBLIC.GEOMTABLE;");
    }

    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testEstimatedExtentSchemaPostGIS() throws SQLException {
        Statement statement = conPost.createStatement();
        statement.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE; CREATE SCHEMA MYSCHEMA; DROP TABLE IF EXISTS MYSCHEMA.GEOMTABLE; CREATE TABLE MYSCHEMA.GEOMTABLE (THE_GEOM GEOMETRY(GEOMETRY, 4326));");
        statement.execute("INSERT INTO MYSCHEMA.GEOMTABLE VALUES (ST_GeomFromText('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))', 4326)),(ST_GeomFromText('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))', 4326) )");
        statement.execute("ANALYZE MYSCHEMA.GEOMTABLE");
        TableLocation tableLocation = TableLocation.parse("myschema.geomtable", DBTypes.POSTGIS);
        Geometry geom = GeometryTableUtilities.getEstimatedExtent(conPost, tableLocation, "the_geom");
        assertNotNull(geom);
        assertEquals(4326, geom.getSRID());
        statement.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE;");
    }

    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testEnvelopeSchemaPostGIS() throws SQLException {
        Statement statement = conPost.createStatement();
        statement.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE; CREATE SCHEMA MYSCHEMA; DROP TABLE IF EXISTS MYSCHEMA.GEOMTABLE; CREATE TABLE MYSCHEMA.GEOMTABLE (THE_GEOM GEOMETRY(GEOMETRY, 4326));");
        statement.execute("INSERT INTO MYSCHEMA.GEOMTABLE VALUES (ST_GeomFromText('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))', 4326)),(ST_GeomFromText('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))', 4326) )");
        TableLocation tableLocation = TableLocation.parse("myschema.geomtable", DBTypes.POSTGIS);
        Geometry geom = GeometryTableUtilities.getEnvelope(conPost, tableLocation, "the_geom");
        assertNotNull(geom);
        assertTrue(geom.getArea() > 0);
        assertEquals(4326, geom.getSRID());
        statement.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE;");
    }

    @Test
    public void testGetMetadataFromGeometry() throws SQLException {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY(POINTZ, 4326))");
        st.execute("insert into POINT3D VALUES(1, 'SRID=4326;POINTZ(0 0 0)')");
        ResultSet resultSet = st.executeQuery("SELECT THE_GEOM FROM POINT3D");
        resultSet.next();
        GeometryMetaData geomMet = GeometryMetaData.getMetaData((Geometry) resultSet.getObject(1));
        assertEquals(4326, geomMet.SRID);
        assertEquals(3, geomMet.dimension);
        assertFalse(geomMet.hasM);
        assertTrue(geomMet.hasZ);
        assertEquals("POINTZ", geomMet.geometryType);
        assertEquals(GeometryTypeCodes.POINTZ, geomMet.geometryTypeCode);
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        st.execute("insert into POINT3D VALUES(1, 'SRID=4326;POINTM(0 0 0)')");
        resultSet = st.executeQuery("SELECT THE_GEOM FROM POINT3D");
        resultSet.next();
        geomMet = GeometryMetaData.getMetaData((Geometry) resultSet.getObject(1));
        assertEquals(4326, geomMet.SRID);
        assertEquals(3, geomMet.dimension);
        assertTrue(geomMet.hasM);
        assertFalse(geomMet.hasZ);
        assertEquals("POINTM", geomMet.geometryType);
        assertEquals(GeometryTypeCodes.POINTM, geomMet.geometryTypeCode);
    }

    @Test
    public void testCreateDDL() throws SQLException {
        TableLocation location = TableLocation.parse("PERSTABLE", DBTypes.H2);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable");
        assertEquals("CREATE TABLE PERSTABLE", JDBCUtilities.createTableDDL(connection,location));
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY, type int, name varchar, city varchar(12), "
                + "temperature double precision, location GEOMETRY(POINTZ, 4326), wind CHARACTER VARYING(64))");
        String ddl = JDBCUtilities.createTableDDL(connection, location);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute(ddl);
        assertEquals("CREATE TABLE PERSTABLE (ID INTEGER,THE_GEOM GEOMETRY,TYPE INTEGER,NAME VARCHAR,CITY VARCHAR(12),TEMPERATURE DOUBLE PRECISION,LOCATION GEOMETRY(POINTZ,4326),WIND VARCHAR(64))",
                ddl);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY(POINTZ, 4326))");
        assertEquals("CREATE TABLE PERSTABLE (ID INTEGER,THE_GEOM GEOMETRY(POINTZ,4326))",
                JDBCUtilities.createTableDDL(connection, location));
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY(POINTZ, 0))");
        assertEquals("CREATE TABLE PERSTABLE (ID INTEGER,THE_GEOM GEOMETRY(POINTZ,0))",
                JDBCUtilities.createTableDDL(connection, location));
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY(GEOMETRY, 0))");
        assertEquals("CREATE TABLE PERSTABLE (ID INTEGER,THE_GEOM GEOMETRY)",
                JDBCUtilities.createTableDDL(connection, location));
        assertEquals("CREATE TABLE MYTABLE (THE_GEOM GEOMETRY)",
                JDBCUtilities.createTableDDL(st.executeQuery("SELECT the_geom from PERSTABLE"), "MYTABLE"));
    }

    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testCreateDDLPostGIS() throws SQLException {
        Statement stat = conPost.createStatement();
        stat.execute("DROP TABLE IF EXISTS perstable");
        stat.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY, type int, name varchar, city varchar(12), "
                + "temperature double precision, location GEOMETRY(POINTZ, 4326), wind CHARACTER VARYING(64))");
        String ddl = JDBCUtilities.createTableDDL(conPost, TableLocation.parse("perstable", DBTypes.POSTGIS));
        stat.execute("DROP TABLE IF EXISTS perstable");
        stat.execute(ddl);
        assertEquals("CREATE TABLE perstable (id int4,the_geom GEOMETRY,type int4,name varchar,city varchar(12),temperature DOUBLE PRECISION,location GEOMETRY(POINTZ,4326),wind varchar(64))",
                ddl);
        stat.execute("DROP TABLE IF EXISTS perstable");
        stat.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY(POINTZ, 4326))");
        ddl = JDBCUtilities.createTableDDL(conPost, TableLocation.parse("perstable", DBTypes.POSTGIS));
        assertEquals("CREATE TABLE perstable (id int4,the_geom GEOMETRY(POINTZ,4326))", ddl);
        stat.execute("DROP TABLE IF EXISTS perstable");
        stat.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY(POINTZ, 0))");
        assertEquals("CREATE TABLE perstable (id int4,the_geom GEOMETRY(POINTZ,0))",
                JDBCUtilities.createTableDDL(conPost, TableLocation.parse("perstable", DBTypes.POSTGIS)));
        stat.execute("DROP TABLE IF EXISTS perstable");
        stat.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY(GEOMETRY, 0))");
        assertEquals("CREATE TABLE perstable (id int4,the_geom GEOMETRY)",
                JDBCUtilities.createTableDDL(conPost, TableLocation.parse("perstable", DBTypes.POSTGIS)));
        assertEquals("CREATE TABLE mytable (the_geom GEOMETRY)",
                JDBCUtilities.createTableDDL(stat.executeQuery("SELECT the_geom from PERSTABLE"), "mytable"));
    }

    @Test
    public void testCreateDDLSourceTarget() throws SQLException {
        TableLocation location = TableLocation.parse("PERSTABLE", DBTypes.H2);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY, type int, name varchar, city varchar(12), "
                + "temperature double precision, location GEOMETRY(POINTZ, 4326), wind CHARACTER VARYING(64))");
        String ddl = JDBCUtilities.createTableDDL(connection, location, TableLocation.parse("orbisgis",DBTypes.H2));
        assertEquals("CREATE TABLE ORBISGIS (ID INTEGER,THE_GEOM GEOMETRY,TYPE INTEGER,NAME VARCHAR,CITY VARCHAR(12),TEMPERATURE DOUBLE PRECISION,LOCATION GEOMETRY(POINTZ,4326),WIND VARCHAR(64))",
                ddl);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, the_geom GEOMETRY, type int, name varchar, city varchar(12), "
                + "temperature double precision, location GEOMETRY(POINTZ, 4326), wind CHARACTER VARYING(64))");
        ddl = JDBCUtilities.createTableDDL(connection, location, TableLocation.parse("\"OrbisGIS\"",DBTypes.H2));
        assertEquals("CREATE TABLE \"OrbisGIS\" (ID INTEGER,THE_GEOM GEOMETRY,TYPE INTEGER,NAME VARCHAR,CITY VARCHAR(12),TEMPERATURE DOUBLE PRECISION,LOCATION GEOMETRY(POINTZ,4326),WIND VARCHAR(64))",
                ddl);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, name varchar(26))");       
        ddl = JDBCUtilities.createTableDDL(connection, location, TableLocation.parse("\"OrbisGIS\"",DBTypes.H2));
        assertEquals("CREATE TABLE \"OrbisGIS\" (ID INTEGER,NAME VARCHAR(26))",
                ddl);
        st.execute("DROP TABLE IF EXISTS perstable");
        st.execute("CREATE TABLE perstable (id INTEGER PRIMARY KEY, name varchar)");       
        ddl = JDBCUtilities.createTableDDL(connection,location, TableLocation.parse("\"OrbisGIS\"",DBTypes.H2));
        assertEquals("CREATE TABLE \"OrbisGIS\" (ID INTEGER,NAME VARCHAR)",
                ddl);
    }   
  

    @Test
    public void testGetEnvelopeFromGeometryFields() throws SQLException, ParseException {
        String sqlData = "DROP TABLE IF EXISTS public.building_indicators;\n"
                + "CREATE TABLE public.building_indicators (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.building_indicators\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";

        st.execute(sqlData);
        TableLocation location = TableLocation.parse("public.building_indicators", DBTypes.H2);
        Geometry env = GeometryTableUtilities.getEnvelope(connection, location, new String[]{"the_geom"});
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576555.1334797409 5384506.127270323, 576555.1334797409 5384648.519150911, 576646.7754414822 5384648.519150911, 576646.7754414822 5384506.127270323, 576555.1334797409 5384506.127270323))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, location, new String[]{"st_buffer(the_geom,0)"});
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, location, "st_buffer(the_geom,0)", "the_geom");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        assertThrows(SQLException.class, () -> {
            try {
               GeometryTableUtilities.getEnvelope(connection, location, new String[]{"st_buffer(orbisgis,0)"});
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
        
        assertThrows(SQLException.class, () -> {
            try {
                GeometryTableUtilities.getEnvelope(connection, location, "st_buffer(the_geom,0)", "st_setsrid(the_geom, 2154)");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
    
    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testGetEnvelopeFromGeometryFieldsPostGIS() throws SQLException, ParseException {
        String sqlData = "DROP TABLE IF EXISTS public.buildings;\n"
                + "CREATE TABLE public.buildings (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.buildings\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";
        Statement stat = conPost.createStatement();
        stat.execute(sqlData);
        TableLocation location = TableLocation.parse("public.buildings", DBTypes.POSTGIS);
        Geometry env = GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"the_geom"});
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576555.1334797409 5384506.127270323, 576555.1334797409 5384648.519150911, 576646.7754414822 5384648.519150911, 576646.7754414822 5384506.127270323, 576555.1334797409 5384506.127270323))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"st_buffer(the_geom,0)"});
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(conPost, location, "st_buffer(the_geom,0)", "the_geom");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        assertThrows(SQLException.class, () -> {
            try {
               GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"st_buffer(orbisgis,0)"});
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
        
        assertThrows(SQLException.class, () -> {
            try {
                GeometryTableUtilities.getEnvelope(conPost, location, "st_buffer(the_geom,0)", "st_setsrid(the_geom, 2154)");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
    
    
    @Test
    public void testGetEnvelopeFromGeometryFieldsWithFilter() throws SQLException, ParseException {
        String sqlData = "DROP TABLE IF EXISTS public.building_indicators;\n"
                + "CREATE TABLE public.building_indicators (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.building_indicators\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";

        st.execute(sqlData);
        TableLocation location = TableLocation.parse("public.building_indicators", DBTypes.H2);
        Geometry env = GeometryTableUtilities.getEnvelope(connection, location, new String[]{"the_geom"}, "WHERE id_build=1164");
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576569.6799396832 5384553.281278896, 576569.6799396832 5384560.202691146, 576584.3405324102 5384560.202691146, 576584.3405324102 5384553.281278896, 576569.6799396832 5384553.281278896))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, location, new String[]{"st_buffer(the_geom,0)"}, "WHERE id_build=1164");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, location, new String[]{"st_buffer(the_geom,0)", "the_geom"}, "WHERE id_build=1164");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        assertThrows(SQLException.class, () -> {
            try {
               GeometryTableUtilities.getEnvelope(connection, location, new String[]{"st_buffer(the_geom,0)"}, "select");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
        
        assertThrows(SQLException.class, () -> {
            try {
                GeometryTableUtilities.getEnvelope(connection, location, new String[]{"st_buffer(the_geom,0)", "st_setsrid(the_geom, 2154)"}, "");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
    
    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")    
    public void testGetEnvelopeFromGeometryFieldsWithFilterPostGIS() throws SQLException, ParseException {
        String sqlData = "DROP TABLE IF EXISTS public.buildings;\n"
                + "CREATE TABLE public.buildings (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.buildings\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";

        Statement stat = conPost.createStatement();
        stat.execute(sqlData);
        TableLocation location = TableLocation.parse("public.buildings", DBTypes.POSTGIS);
        Geometry env = GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"the_geom"}, "WHERE id_build=1164");
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576569.6799396832 5384553.281278896, 576569.6799396832 5384560.202691146, 576584.3405324102 5384560.202691146, 576584.3405324102 5384553.281278896, 576569.6799396832 5384553.281278896))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(), env.getEnvelopeInternal());

        env = GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"st_buffer(the_geom,0)"}, "WHERE id_build=1164");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"st_buffer(the_geom,0)", "the_geom"}, "WHERE id_build=1164");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        assertThrows(SQLException.class, () -> {
            try {
               GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"st_buffer(the_geom,0)"}, "select");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
        
        assertThrows(SQLException.class, () -> {
            try {
                GeometryTableUtilities.getEnvelope(conPost, location, new String[]{"st_buffer(the_geom,0)", "st_setsrid(the_geom, 2154)"}, "");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
    
    @Test
    public void testGetEnvelopeFromGeometryFieldsSubQuery() throws SQLException, ParseException {
        String sqlData = "DROP TABLE IF EXISTS public.building_indicators;\n"
                + "CREATE TABLE public.building_indicators (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.building_indicators\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";

        st.execute(sqlData);
        Geometry env = GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"the_geom"});
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576569.6799396832 5384553.281278896, 576569.6799396832 5384560.202691146, 576584.3405324102 5384560.202691146, 576584.3405324102 5384553.281278896, 576569.6799396832 5384553.281278896))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)"});
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)", "the_geom"});
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        assertThrows(SQLException.class, () -> {
            try {
               GeometryTableUtilities.getEnvelope(connection, "SELECT", new String[]{"st_buffer(the_geom,0)"});
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
        
        assertThrows(SQLException.class, () -> {
            try {
                GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)", "st_setsrid(the_geom, 2154)"});
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
    
    @Test
    public void testGetEnvelopeFromGeometryFieldsSubQueryFilter() throws SQLException, ParseException {
        String sqlData = "DROP TABLE IF EXISTS public.building_indicators;\n"
                + "CREATE TABLE public.building_indicators (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.building_indicators\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";

        st.execute(sqlData);
        Geometry env = GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"the_geom"}, "limit 1");
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576569.6799396832 5384553.281278896, 576569.6799396832 5384560.202691146, 576584.3405324102 5384560.202691146, 576584.3405324102 5384553.281278896, 576569.6799396832 5384553.281278896))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)"},"limit 1");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(connection, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)", "the_geom"}, "limit 1");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
    }
    
    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")   
    public void testGetEnvelopeFromGeometryFieldsSubQueryFilterPostGIS() throws SQLException, ParseException {
        Statement stat = conPost.createStatement();
        String sqlData = "DROP TABLE IF EXISTS public.building_indicators;\n"
                + "CREATE TABLE public.building_indicators (\n"
                + "	the_geom geometry(POLYGON, 32630),\n"
                + "	id_build int4\n"
                + "); "
                + "INSERT INTO public.building_indicators\n"
                + "VALUES('SRID=32630;POLYGON ((576569.8855746118 5384560.202691146, 576569.6799396832 5384553.6405112585, 576584.1349142857 5384553.281278896, 576584.3405324102 5384559.843459481, 576569.8855746118 5384560.202691146))', 1164),"
                + "('SRID=32630;POLYGON ((576613.4515820902 5384506.430423924, 576623.851014461 5384506.127270323, 576624.030867693 5384514.579094129, 576613.7066755823 5384514.772095093, 576613.4515820902 5384506.430423924))', 1165),"
                + "('SRID=32630;POLYGON ((576615.749017773 5384624.174842924, 576638.3786205315 5384640.147609505, 576632.6616354721 5384648.519150911, 576609.8565085419 5384632.443959317, 576615.749017773 5384624.174842924))', 1166), "
                + "('SRID=32630;POLYGON ((576635.6545708041 5384554.315989608, 576638.6060492478 5384554.133821507, 576638.5595789884 5384552.132020645, 576646.5234286813 5384552.018102771, 576646.7754414822 5384560.582087368, 576635.786417653 5384560.877169167, 576635.6545708041 5384554.315989608))', 1167), "
                + "('SRID=32630;POLYGON ((576555.1334797409 5384549.885025587, 576558.5272249203 5384549.708830485, 576558.5752354743 5384551.599474974, 576564.9174325457 5384551.4633860495, 576565.0988820936 5384559.804052239, 576555.3644671134 5384560.005180355, 576555.2370617936 5384553.110537442, 576555.1334797409 5384549.885025587))', 1168),"
                + "('SRID=32630;POLYGON ((576584.2490658457 5384555.728703618, 576588.6010553383 5384555.565564131, 576588.5919809027 5384556.232496579, 576593.4894335563 5384556.076782604, 576593.6731186393 5384562.082785434, 576584.3794601331 5384562.401036767, 576584.3405324102 5384559.843459481, 576584.2490658457 5384555.728703618))', 1169);";

        stat.execute(sqlData);
        Geometry env = GeometryTableUtilities.getEnvelope(conPost, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"the_geom"}, "limit 1");
        WKTReader reader = new WKTReader();
        Geometry expectedGeom = reader.read("POLYGON ((576569.6799396832 5384553.281278896, 576569.6799396832 5384560.202691146, 576584.3405324102 5384560.202691146, 576584.3405324102 5384553.281278896, 576569.6799396832 5384553.281278896))");
        expectedGeom.setSRID(32630);
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(conPost, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)"},"limit 1");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
        
        env = GeometryTableUtilities.getEnvelope(conPost, "SELECT * FROM public.building_indicators WHERE id_build=1164", new String[]{"st_buffer(the_geom,0)", "the_geom"}, "limit 1");
        assertEquals(expectedGeom.getSRID(), env.getSRID());
        assertEquals(expectedGeom.getEnvelopeInternal(),env.getEnvelopeInternal());
    }
    
    @Test
    public void testAuthorityAndSRID() throws SQLException, ParseException {
        assertNull(GeometryTableUtilities.getAuthorityAndSRID(connection, 0));
        String[] authSrid = GeometryTableUtilities.getAuthorityAndSRID(connection, 4326);
        assertEquals(authSrid[0], "EPSG");
        assertEquals(authSrid[1], "4326");
        authSrid = GeometryTableUtilities.getAuthorityAndSRID(connection, 2154);
        assertEquals(authSrid[0], "EPSG");
        assertEquals(authSrid[1], "2154");
        assertNull(GeometryTableUtilities.getAuthorityAndSRID(connection, -9999));
    }

    @Test
    public void testIsSpatialIndexed() throws Exception {
        TableLocation tableLocation = TableLocation.parse("GEO_POINT", DBTypes.H2GIS);
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (id int, the_geom GEOMETRY)");
        st.execute("INSERT INTO geo_point VALUES(1, 'POINT(1 2)')");
        st.execute("create spatial index geotable_sp_index on geo_point(the_geom)");
        assertTrue(GeometryTableUtilities.isSpatialIndexed(connection, tableLocation, "the_geom"));
        st.execute("drop index geotable_sp_index ");
        assertFalse(GeometryTableUtilities.isSpatialIndexed(connection, tableLocation, "the_geom"));
    }

    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testPostGISIsSpatialIndexed() throws Exception {
        TableLocation tableLocation = TableLocation.parse("geo_point", DBTypes.POSTGIS);
        Statement stat = conPost.createStatement();
        stat.execute("drop table if exists geo_point; CREATE TABLE geo_point (id int, the_geom GEOMETRY)");
        stat.execute("INSERT INTO geo_point VALUES(1, 'POINT(1 2)')");
        stat.execute("create index geotable_sp_index on geo_point  USING GIST (the_geom);");
        assertTrue(GeometryTableUtilities.isSpatialIndexed(conPost, tableLocation, "the_geom"));
        stat.execute("drop index geotable_sp_index ");
        assertFalse(GeometryTableUtilities.isSpatialIndexed(conPost,tableLocation, "the_geom"));
    }

    @Test
    public void checkIndexes() throws SQLException {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (id int, the_geom GEOMETRY)");
        st.execute("INSERT INTO geo_point VALUES(1, 'POINT(1 2)')");
        st.execute("create spatial index geotable_sp_index on geo_point(the_geom)");
        String query  = String.format("SELECT I.*,C.* FROM INFORMATION_SCHEMA.INDEXES AS I , " +
                "(SELECT COLUMN_NAME, TABLE_NAME, TABLE_SCHEMA  FROM " +
                "INFORMATION_SCHEMA.INDEXES WHERE TABLE_SCHEMA='%s' and TABLE_NAME='%s' AND COLUMN_NAME='%s') AS C " +
                "WHERE I.TABLE_SCHEMA=C.TABLE_SCHEMA AND I.TABLE_NAME=C.TABLE_NAME and C.COLUMN_NAME='%s'", "PUBLIC", "GEO_POINT", "THE_GEOM","THE_GEOM");
        ResultSet rs = st.executeQuery(query);
        rs.next();
        TestUtilities.printValues(rs);
        st.execute("DROP TABLE IF EXISTS shptable");
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'shptable');");
        System.out.println("The table exists "+ JDBCUtilities.tableExists(connection, TableLocation.parse("SHPTABLE")));
        st.execute("create spatial index on shptable(the_geom)");
        query  = String.format("SELECT I.*,C.* FROM INFORMATION_SCHEMA.INDEXES AS I , " +
                "(SELECT COLUMN_NAME, TABLE_NAME, TABLE_SCHEMA  FROM " +
                "INFORMATION_SCHEMA.INDEXES WHERE TABLE_SCHEMA='%s' and TABLE_NAME='%s' AND COLUMN_NAME='%s') AS C " +
                "WHERE I.TABLE_SCHEMA=C.TABLE_SCHEMA AND I.TABLE_NAME=C.TABLE_NAME and C.COLUMN_NAME='%s'", "PUBLIC", "SHPTABLE", "THE_GEOM","THE_GEOM");
        rs = st.executeQuery(query);
        rs.next();
        TestUtilities.printValues(rs);
    }
    
    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void testGetSRIDSameTableNames() throws SQLException {
        Statement statement = conPost.createStatement();
        statement.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE; CREATE SCHEMA MYSCHEMA; "
                + "DROP TABLE IF EXISTS MYSCHEMA.GEOMTABLE; "
                + "CREATE TABLE MYSCHEMA.GEOMTABLE (THE_GEOM GEOMETRY(GEOMETRY, 4326));");
        TableLocation tableLocation = TableLocation.parse("myschema.geomtable");        
        assertEquals(4326, GeometryTableUtilities.getSRID(conPost, tableLocation));

        statement.execute("DROP TABLE IF EXISTS GEOMTABLE; "
                + "CREATE TABLE GEOMTABLE (THE_GEOM GEOMETRY(GEOMETRY, 2154));");
        tableLocation = TableLocation.parse("geomtable");
        assertEquals(2154, GeometryTableUtilities.getSRID(conPost, tableLocation));

        statement.execute("DROP SCHEMA IF EXISTS MYSCHEMA CASCADE;");
    }
}
