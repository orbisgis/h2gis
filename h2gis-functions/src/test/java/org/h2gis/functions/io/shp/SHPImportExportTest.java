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
package org.h2gis.functions.io.shp;

import org.h2.util.StringUtils;
import org.h2.value.ValueGeometry;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.dbf.DBFRead;
import org.h2gis.functions.io.dbf.DBFWrite;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.functions.io.shp.internal.SHPDriver;
import org.h2gis.postgis_jts_osgi.DataSourceFactoryImpl;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2gis.unitTest.GeometryAsserts;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;

import static org.junit.jupiter.api.Assertions.*;
import org.locationtech.jts.io.WKTReader;

/**
 * Test copy data from SHP to database
 *
 * @author Nicolas Fortin
 */
public class SHPImportExportTest {

    private static Connection connection;
    private static final String DB_NAME = "SHPImportTest";

    private static final Logger log = LoggerFactory.getLogger(SHPImportExportTest.class);

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

    @Test
    public void exportTableTestGeomEnd() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/area_export.shp");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        stat.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
        stat.execute("insert into area values(2, 'POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export.shp', 'AREA', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(shpFile);
        shpDriver.setGeometryFieldIndex(1);
        assertEquals(2, shpDriver.getFieldCount());
        assertEquals(2, shpDriver.getRowCount());
        assertEquals(1, shpDriver.getField(0, 0).getInt());
        // The driver can not create POLYGON
        assertGeometryEquals("MULTIPOLYGON (((-10 109, 90 109, 90 9, -10 9, -10 109)))", (ValueGeometry) shpDriver.getField(0, 1));
        assertEquals(2, shpDriver.getField(1, 0).getInt());
        assertGeometryEquals("MULTIPOLYGON (((90 109, 190 109, 190 9, 90 9, 90 109)))", (ValueGeometry) shpDriver.getField(1, 1));
    }

    @Test
    public void exportTableTestGeomDeb() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/area_export1.shp");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY(POLYGON), idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export1.shp', 'AREA', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(shpFile);
        shpDriver.setGeometryFieldIndex(0);
        assertEquals(2, shpDriver.getFieldCount());
        assertEquals(2, shpDriver.getRowCount());
        assertEquals(1, shpDriver.getField(0, 1).getInt());
        // The driver can not create POLYGON
        assertGeometryEquals("MULTIPOLYGON (((-10 109, 90 109, 90 9, -10 9, -10 109)))", (ValueGeometry) shpDriver.getField(0, 0));
        assertEquals(2, shpDriver.getField(1, 1).getInt());
        assertGeometryEquals("MULTIPOLYGON (((90 109, 190 109, 190 9, 90 9, 90 109)))", (ValueGeometry) shpDriver.getField(1, 0));
    }

    @Test
    public void copySHPTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK");
        final String path = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        st.execute("CALL SHPRead(" + path + ", 'WATERNETWORK');");
        checkSHPReadResult(st);
    }

    @Test
    public void copySHPTestAutomaticTableName() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK");
        final String path = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        // No table name is specified:
        st.execute("CALL SHPRead(" + path + ", 'waternetwork');");
        checkSHPReadResult(st);
    }

    private void checkSHPReadResult(Statement st) throws SQLException {
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'WATERNETWORK'");
        assertTrue(rs.next());
        assertEquals(H2TableIndex.PK_COLUMN_NAME, rs.getString("COLUMN_NAME"));
        assertEquals("INTEGER", rs.getString("DATA_TYPE"));
        assertTrue(rs.next());
        assertEquals("THE_GEOM", rs.getString("COLUMN_NAME"));
        assertEquals("GEOMETRY", rs.getString("DATA_TYPE"));
        assertTrue(rs.next());
        assertEquals("TYPE_AXE", rs.getString("COLUMN_NAME"));
        assertEquals("CHARACTER VARYING", rs.getString("DATA_TYPE"));
        assertEquals(254, rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        assertTrue(rs.next());
        assertEquals("GID", rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT", rs.getString("DATA_TYPE"));
        assertTrue(rs.next());
        assertEquals("LENGTH", rs.getString("COLUMN_NAME"));
        assertEquals("DOUBLE PRECISION", rs.getString("DATA_TYPE"));
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(H2TableIndex.PK_COLUMN_NAME));
        assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))", rs.getString("the_geom"));
        assertEquals("river", rs.getString("type_axe"));
        assertEquals(9.492402903934545, rs.getDouble("length"), 1e-12);
        assertEquals(1, rs.getInt("GID"));
        assertTrue(rs.next());
        assertEquals("ditch", rs.getString("type_axe"));
        assertEquals(261.62989135452983, rs.getDouble("length"), 1e-12);
        assertEquals(2, rs.getInt("GID"));
        rs.close();
        // Computation
        rs = st.executeQuery("SELECT SUM(length) sumlen FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals(28469.778049948833, rs.getDouble(1), 1e-12);
        rs.close();
        st.execute("drop table WATERNETWORK");
    }

    @Test
    public void exportTableWithoutConstraint() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/area_export2.shp");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export2.shp', 'AREA', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(shpFile);
        shpDriver.setGeometryFieldIndex(0);
        assertEquals(2, shpDriver.getFieldCount());
        assertEquals(2, shpDriver.getRowCount());
        assertEquals(1, shpDriver.getField(0, 1).getInt());
        // The driver can not create POLYGON
        assertGeometryEquals("MULTIPOLYGON (((-10 109, 90 109, 90 9, -10 9, -10 109)))", (ValueGeometry) shpDriver.getField(0, 0));
        assertEquals(2, shpDriver.getField(1, 1).getInt());
        assertGeometryEquals("MULTIPOLYGON (((90 109, 190 109, 190 9, 90 9, 90 109)))", (ValueGeometry) shpDriver.getField(1, 0));
    }

    @Test
    public void exportTableWithoutConstraintException() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS AREA");
            stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
            stat.execute("insert into area values('POINT (-10 109)', 1)");
            stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
            // Create a shape file using table area
            stat.execute("CALL SHPWrite('target/area_export_ex.shp', 'AREA')");
        });
    }

    @Test
    public void testDriverManager() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        // Export in target with special chars
        File shpFile = new File("target/area éxport.shp");
        DriverFunction exp = new SHPDriverFunction();
        exp.exportTable(connection, "AREA", shpFile, true,new EmptyProgressVisitor());
        stat.execute("DROP TABLE IF EXISTS myshp");
        DriverFunction manager = new DriverManager();
        manager.importFile(connection, "MYSHP", shpFile, new EmptyProgressVisitor());
        ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom)) from myshp");
        try {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        } finally {
            rs.close();
        }
    }

    @Test
    public void exportTableWithNullGeom() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS AREA");
            stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
            stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
            stat.execute("insert into area values(NULL, 2)");
            // Create a shape file using table area
            stat.execute("CALL SHPWrite('target/area_export3.shp', 'AREA')");
        });
    }

    @Test
    public void exportTableLineString() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/line_export.shp");
        stat.execute("DROP TABLE IF EXISTS LINE");
        stat.execute("create table LINE(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
        stat.execute("insert into LINE values(1, 'LINESTRING (-10 109, 90 109, 90 9, -10 9)')");
        stat.execute("insert into LINE values(2, 'LINESTRING (90 109, 190 109, 190 9, 90 9)')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/line_export.shp', 'LINE', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(shpFile);
        shpDriver.setGeometryFieldIndex(1);
        assertEquals(2, shpDriver.getFieldCount());
        assertEquals(2, shpDriver.getRowCount());
        assertEquals(1, shpDriver.getField(0, 0).getInt());
        // The driver can not create POLYGON
        assertGeometryEquals("MULTILINESTRING ((-10 109, 90 109, 90 9, -10 9))", (ValueGeometry) shpDriver.getField(0, 1));
        assertEquals(2, shpDriver.getField(1, 0).getInt());
        assertGeometryEquals("MULTILINESTRING ((90 109, 190 109, 190 9, 90 9))", (ValueGeometry) shpDriver.getField(1, 1));
    }

    @Test
    public void readSHPURITest() throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK");
        final String path = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").toURI().toString());
        st.execute("CALL SHPRead(" + path + ", 'WATERNETWORK');");
        checkSHPReadResult(st);
    }

    @Test
    public void readSHPIntoTwoTablesWithSameSridTest() throws Exception {
        doReadSHPIntoTwoTables("SRIDTABLE1", "SRIDTABLE2", "sridtable1.shp", "sridtable1.shp");
    }

    @Test
    public void readSHPIntoTwoTablesWithDifferentSridTest() throws Exception {
        doReadSHPIntoTwoTables("SRIDTABLE1", "SRIDTABLE2", "sridtable1.shp", "sridtable2.shp");
    }

    private void doReadSHPIntoTwoTables(String table1, String table2, String shpFile1, String shpFile2) throws Exception {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS " + table1);
        st.execute("DROP TABLE IF EXISTS " + table2);
        final String path1 = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource(shpFile1).toURI().toString());
        final String path2 = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource(shpFile2).toURI().toString());
        st.execute("CALL SHPRead(" + path1 + ", '" + table1 + "');");
        st.execute("CALL SHPRead(" + path2 + ", '" + table2 + "');");
        ResultSet rs = st.executeQuery("SELECT count(*) FROM " + table1);
        assertTrue(rs.next());
        assertEquals(382, rs.getInt(1));
        rs.close();
        rs = st.executeQuery("SELECT count(*) FROM " + table2);
        assertTrue(rs.next());
        assertEquals(382, rs.getInt(1));
        rs.close();
        st.execute("drop table " + table1);
        st.execute("drop table " + table2);
    }

    @Test
    public void testReservedKeyWord() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key, \"NATURAL\" boolean)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1, True)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2, False)");
        // Export in target with special chars
        File shpFile = new File("target/test_export4.shp");
        DriverFunction exp = new SHPDriverFunction();
        exp.exportTable(connection, "AREA", shpFile, true,new EmptyProgressVisitor());
        stat.execute("DROP TABLE IF EXISTS myshp");
        SHPDriverFunction driverFunction = new SHPDriverFunction();
        driverFunction.importFile(connection, "MYSHP", shpFile, new EmptyProgressVisitor());
        ResultSet rs = stat.executeQuery("select * from myshp");
        try {
            assertEquals(4, rs.findColumn("NATURAL"));
        } finally {
            rs.close();
        }
    }

    @Test
    public void exportTableTestZ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/area_export5.shp");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON Z))");
        stat.execute("insert into area values(1, 'POLYGON ((-10 109 5, 90 109 5, 90 9 5, -10 9 5, -10 109 5))')");
        stat.execute("insert into area values(2, 'POLYGON ((90 109 3, 190 109 3, 190 9 3, 90 9 3, 90 109 3))')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export5.shp', 'AREA', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        SHPDriver shpDriver = new SHPDriver();
        shpDriver.initDriverFromFile(shpFile);
        shpDriver.setGeometryFieldIndex(1);
        assertEquals(2, shpDriver.getFieldCount());
        assertEquals(2, shpDriver.getRowCount());
        assertEquals(1, shpDriver.getField(0, 0).getInt());
        // The driver can not create POLYGON
        WKTWriter toText = new WKTWriter(3);
        assertEquals("MULTIPOLYGON Z(((-10 109 5, 90 109 5, 90 9 5, -10 9 5, -10 109 5)))", toText.write(((ValueGeometry)shpDriver.getField(0, 1)).getGeometry()));
        assertEquals(2, shpDriver.getField(1, 0).getInt());
        assertEquals("MULTIPOLYGON Z(((90 109 3, 190 109 3, 190 9 3, 90 9 3, 90 109 3)))", toText.write(((ValueGeometry) shpDriver.getField(1, 1)).getGeometry()));
    }

    @Test
    public void exportImportPolygonZ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/area_export6.shp");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON Z))");
        stat.execute("insert into area values(1, 'POLYGON ((-10 109 5, 90 109 5, 90 9 5, -10 9 5, -10 109 5))')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export6.shp', 'AREA', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_AREA;");
        stat.execute("CALL SHPRead('target/area_export6.shp', 'IMPORT_AREA')");
        ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_AREA;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        Coordinate[] coords = geom.getCoordinates();
        int count = 0;
        for (Coordinate coord : coords) {
            if (coord.z == 5) {
                count++;
            }
        }
        assertEquals(count, coords.length);
        res.close();

    }

    @Test
    public void exportImportPointZ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/punctual_export.shp");
        stat.execute("DROP TABLE IF EXISTS PUNCTUAL");
        stat.execute("create table punctual(idarea int primary key, the_geom GEOMETRY(POINT Z))");
        stat.execute("insert into punctual values(1, 'POINT(-10 109 5)')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/punctual_export.shp', 'PUNCTUAL', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_PUNCTUAL;");
        stat.execute("CALL SHPRead('target/punctual_export.shp', 'IMPORT_PUNCTUAL')");
        ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_PUNCTUAL;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        Coordinate coord = geom.getCoordinate();
        assertEquals(coord.z, 5, 10E-1);
        res.close();
    }

    @Test
    public void exportImportLineStringZ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/lineal_export.shp");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12 6 0)')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/lineal_export.shp', 'LINEAL', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
        stat.execute("CALL SHPRead('target/lineal_export.shp', 'IMPORT_LINEAL')");
        ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_LINEAL;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        Coordinate[] coords = geom.getCoordinates();
        assertEquals(coords[0].z, 5, 10E-1);
        //Since the 'NaN' DOUBLE values for Z coordinates is invalid in a shapefile, it is converted to '0.0'.
        assertEquals(coords[1].z, 0, 10E-1);
        res.close();
    }

    @Test
    public void exportImportOptions() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/lineal_export.shp");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12 6 0)')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/lineal_export.shp', 'LINEAL', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        stat.execute("CALL SHPRead('target/lineal_export.shp', 'IMPORT_LINEAL', true)");
        ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_LINEAL;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        Coordinate[] coords = geom.getCoordinates();
        assertEquals(coords[0].z, 5, 10E-1);
        //Since the 'NaN' DOUBLE values for Z coordinates is invalid in a shapefile, it is converted to '0.0'.
        assertEquals(coords[1].z, 0, 10E-1);
        res.close();
    }

    @Test
    public void exportTableWithBadExtensionName() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS AREA");
            stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
            stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");

            // Create a shape file using table area
            stat.execute("CALL SHPWrite('target/area_export.blabla', 'AREA')");
        });
    }

    @Test
    public void exportTableWithBadNullExtension() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS AREA");
            stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
            stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");

            // Create a shape file using table area
            stat.execute("CALL SHPWrite('target/area_export.', 'AREA')");
        });
    }

    @Test
    public void importFileNoExist() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("CALL SHPRead('target/blabla.shp', 'BLABLA')");
        });
    }

    @Test
    public void importFileWithBadExtension() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            File file = new File("target/area_export.blabla");
            file.delete();
            file.createNewFile();
            stat.execute("CALL SHPRead('target/area_export.blabla', 'BLABLA')");
            file.delete();
        });
    }

    @Test
    public void exportTableWithNoPRJ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");

        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export7.shp', 'AREA', true)");

        assertTrue(!new File("target/area_export7.prj").exists());
    }

    @Test
    public void exportTableWithPRJ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY(POLYGON, 4326), idarea int primary key)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 4326), 1)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export8.shp', 'AREA',true)");
        assertTrue(new File("target/area_export8.prj").exists());
    }

    @Test
    public void exportImportTableWithOGCPRJ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, AREA_READ");
        stat.execute("create table area(the_geom GEOMETRY(POLYGON, 4326), idarea int primary key)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 4326), 1)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export9.shp', 'AREA', true)");
        stat.execute("CALL SHPRead('target/area_export9.shp', 'AREA_READ')");
        ResultSet res = stat.executeQuery("SELECT ST_SRID(THE_GEOM) FROM AREA_READ;");
        res.next();
        assertTrue(res.getInt(1) == 4326);
        res.close();
    }

    @Test
    public void exportUnknownhSRIDPRJ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY(POLYGON, 9999), idarea int primary key)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 9999), 1)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export10.shp', 'AREA', true)");
    }

    @Test
    public void readEmptyPRJ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, AREA_READ");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))'), 1)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export11.shp', 'AREA', true)");
        new File("target/area_export.prj").createNewFile();
        stat.execute("CALL SHPRead('target/area_export11.shp', 'AREA_READ')");
        ResultSet res = stat.executeQuery("SELECT ST_SRID(THE_GEOM) FROM AREA_READ;");
        res.next();
        assertTrue(res.getInt(1) == 0);
        res.close();
    }

    @Test
    public void exportTableGeometryCollection() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS GEOM_COLL");
            stat.execute("create table GEOM_COLL(idarea int primary key, the_geom GEOMETRY)");
            stat.execute("insert into GEOM_COLL values(1, 'GEOMETRYCOLLECTION (LINESTRING (184 375, 97 245), POLYGON ((180 270, 220 270, 220 230, 180 230, 180 270)))')");
            // Create a shape file using table area
            stat.execute("CALL SHPWrite('target/geomcoll_export.shp', 'GEOM_COLL', true)");
        });
    }

    @Test
    public void geomTableToDBF() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, AREA_READ");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key, type varchar)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))'), 1, 'breton')");
        stat.execute("CALL DBFWrite('target/area_export12.dbf', 'AREA', true)");
        stat.execute("CALL DBFRead('target/area_export12.dbf', 'AREA_READ')");
        ResultSet res = stat.executeQuery("SELECT * FROM AREA_READ;");
        res.next();
        assertTrue(res.getInt(2) == 1);
        assertTrue(res.getString(3).equals("breton"));
        res.close();
    }

    @Test
    public void twoGeomTableToDBF() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, AREA_READ");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key, geom GEOMETRY, type varchar)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))'), 1,"
                + "ST_GEOMFROMTEXT('POINT (-10 109)'), 'breton')");
        stat.execute("CALL DBFWrite('target/area_export13.dbf', 'AREA', true)");
        stat.execute("CALL DBFRead('target/area_export13.dbf', 'AREA_READ')");
        ResultSet res = stat.executeQuery("SELECT * FROM AREA_READ;");
        res.next();
        assertTrue(res.getInt(2) == 1);
        assertTrue(res.getString(3).equals("breton"));
        res.close();
    }

    @Test
    public void writeReadTableWithTwoGeometries() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, AREA_READ");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key, geom GEOMETRY)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))'), 1,ST_GEOMFROMTEXT('POINT (-10 109)'))");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export14.shp', 'AREA', true)");
        new File("target/area_export.prj").createNewFile();
        stat.execute("CALL SHPRead('target/area_export14.shp', 'AREA_READ')");
        ResultSet res = stat.executeQuery("SELECT * FROM AREA_READ;");
        res.next();
        assertTrue(res.getInt(3) == 1);
        res.close();
    }

    @Test
    public void read_similar_paths() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK");
        final String path = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        st.execute("CALL SHPRead(" + path + ", 'WATERNETWORK');");
        st.execute("CALL SHPWrite('target/test_river.shp', 'WATERNETWORK', true)");
        st.execute("CALL SHPWrite('target/river.shp', 'WATERNETWORK', true)");
        st.execute("CALL SHPRead('target/test_river.shp', 'RIVER');");

        // Check content
        ResultSet rs = st.executeQuery("SELECT * FROM RIVER");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(H2TableIndex.PK_COLUMN_NAME));
        assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))", rs.getString("the_geom"));
        assertEquals("river", rs.getString("type_axe"));
        assertEquals(9.492402903934545, rs.getDouble("length"), 1e-12);
        assertEquals(1, rs.getInt("GID"));
        assertTrue(rs.next());
        assertEquals("ditch", rs.getString("type_axe"));
        assertEquals(261.62989135452983, rs.getDouble("length"), 1e-12);
        assertEquals(2, rs.getInt("GID"));
        rs.close();
    }

    @Test
    public void exportAndReadFileWithOGCPRJ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, AREA_READ");
        stat.execute("create table area(the_geom GEOMETRY(POLYGON, 4326), idarea int primary key)");
        stat.execute("insert into area values(ST_GEOMFROMTEXT('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 4326), 1)");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export_srid.shp', 'AREA', true)");
        stat.execute("CALL FILE_TABLE('target/area_export_srid.shp', 'AREA_SRID');");
        ResultSet res = stat.executeQuery("SELECT ST_SRID(THE_GEOM) FROM AREA_SRID;");
        res.next();
        assertTrue(res.getInt(1) == 4326);
        res.close();
    }

    @Test
    public void exportSelect() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK,RIVER");
        final String path = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        st.execute("CALL SHPRead(" + path + ", 'WATERNETWORK');");
        st.execute("CALL SHPWrite('target/test_river.shp', '(select * from WATERNETWORK)', true)");
        st.execute("CALL SHPRead('target/test_river.shp', 'RIVER');");
        // Check content
        ResultSet rs = st.executeQuery("SELECT * FROM RIVER");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(H2TableIndex.PK_COLUMN_NAME));
        assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))", rs.getString("the_geom"));
        assertEquals("river", rs.getString("type_axe"));
        assertEquals(9.492402903934545, rs.getDouble("length"), 1e-12);
        assertEquals(1, rs.getInt("GID"));
        assertTrue(rs.next());
        assertEquals("ditch", rs.getString("type_axe"));
        assertEquals(261.62989135452983, rs.getDouble("length"), 1e-12);
        assertEquals(2, rs.getInt("GID"));
        rs.close();
    }

    @Test
    public void exportSelectLimit() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK,RIVER");
        final String path = StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        st.execute("CALL SHPRead(" + path + ", 'WATERNETWORK');");
        st.execute("CALL SHPWrite('target/test_river.shp', '(select * from WATERNETWORK limit 1)', true)");
        st.execute("CALL SHPRead('target/test_river.shp', 'RIVER');");
        // Check content
        ResultSet rs = st.executeQuery("SELECT count(*) FROM RIVER");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
    }

    @Test
    public void exportImportCharacters() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/area_export_characters.shp");
        stat.execute("DROP TABLE IF EXISTS AREA, table_characters");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key, cover varchar)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1, 'Forêt')");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))',2, 'Zone arborée')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/area_export_characters.shp', 'AREA', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());

        stat.execute("CALL SHPRead('target/area_export_characters.shp', 'table_characters')");
        // Check content
        ResultSet rs = stat.executeQuery("SELECT * FROM table_characters");
        assertTrue(rs.next());
        assertEquals("Forêt", rs.getString("cover"));
        assertTrue(rs.next());
        assertEquals("Zone arborée", rs.getString("cover"));
        rs.close();
    }

    @Test
    public void testSelectWriteReadSHPLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS,TABLE_LINESTRINGS_READ");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL SHPWrite('target/lines.shp', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', true);");
            stat.execute("CALL SHPRead('target/lines.shp', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            GeometryAsserts.assertGeometryEquals("MULTILINESTRING ((1 10, 20 15))",  (Geometry)res.getObject("THE_GEOM"));
            assertEquals(2, res.getInt("ID"));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testSelectWriteRead() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL SHPWrite('target/lines.shp', '(SELECT ST_GEOMFROMTEXT(''LINESTRING(1 10, 20 15)'', 4326) as the_geom)', true);");
            stat.execute("CALL SHPRead('target/lines.shp', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            Geometry geom = (Geometry) res.getObject("THE_GEOM");
            assertEquals(4326, geom.getSRID());
            assertGeometryEquals("SRID=4326;MULTILINESTRING ((1 10, 20 15))", geom);
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testSelectWriteRead2() throws Exception {
        try (Statement stat = connection.createStatement()) {
            new File("target/points.shp").delete();
            stat.execute(" DROP TABLE IF EXISTS orbisgis;"
                    + "CREATE TABLE orbisgis (id int, the_geom geometry(point, 4326));"
                    + "INSERT INTO orbisgis VALUES (1, 'SRID=4326;POINT(10 10)'::GEOMETRY), (2, 'SRID=4326;POINT(1 1)'::GEOMETRY); ");
            stat.execute("CALL SHPWrite('target/points.shp', '(SELECT st_buffer(the_geom, 10) as the_geom, id from orbisgis where id=1)', true);");
            stat.execute("CALL SHPRead('target/points.shp', 'TABLE_POINTS_READ', true);");
            WKTReader wKTReader = new WKTReader();
            Geometry geomOutPut = wKTReader.read("POINT(10 10)").buffer(10);
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            Geometry geom = (Geometry) res.getObject("THE_GEOM");
            assertEquals(4326, geom.getSRID());            
            assertGeometryBarelyEquals(geomOutPut.toString(), geom.getGeometryN(0));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.execute("CALL SHPRead('target/points.shp', true);");
            res = stat.executeQuery("SELECT * FROM POINTS where id=1;");
            res.next();
            geom = (Geometry) res.getObject("THE_GEOM");
            assertEquals(4326, geom.getSRID());
            assertGeometryBarelyEquals(geomOutPut.toString(), geom.getGeometryN(0));
            res.close();
            stat.execute("DROP TABLE IF EXISTS POINTS");
        }
    }
    
    
    @Test
    public void testWriteReadNullValues() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute(" DROP TABLE IF EXISTS orbisgis;"
                    + "CREATE TABLE orbisgis (the_geom geometry(point, 4326), id int, name varchar, version REAL, age FLOAT, distance DOUBLE PRECISION );"
                    + "INSERT INTO orbisgis VALUES ('SRID=4326;POINT(10 10)'::GEOMETRY, null, null, null, null, null); ");
            
            stat.execute("CALL SHPWrite('target/orbisgis_null.shp','orbisgis', true);");
            stat.execute("CALL SHPRead('target/orbisgis_null.shp', 'TABLE_ORBISGIS');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_ORBISGIS;");
            res.next();
            //PK field added by the driver
            assertEquals(1,res.getObject(1));
            //Geometry field
            Geometry geom = (Geometry) res.getObject(2);
            assertEquals(4326, geom.getSRID());
            GeometryAsserts.assertGeometryEquals("SRID=4326;POINT(10 10)", geom);
            assertNull(res.getObject(3));
            assertNull(res.getObject(4));
            assertNull( res.getObject(5));
            assertNull( res.getObject(6));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_ORBISGIS");
        }
    }

    @Test
    public void exportImportPointPostGIS(TestInfo testInfo) throws SQLException, IOException {
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
            Statement stat = con.createStatement();
            File shpFile = new File("target/punctual_export_postgis.shp");
            Files.deleteIfExists(shpFile.toPath());
            stat.execute("DROP TABLE IF EXISTS PUNCTUAL");
            stat.execute("create table punctual(idarea int primary key, the_geom GEOMETRY(POINTZ, 4326))");
            stat.execute("insert into punctual values(1, ST_GEOMFROMTEXT('POINT(-10 109 5)',4326))");
            // Create a shape file using table area
            SHPDriverFunction driver = new SHPDriverFunction();
            driver.exportTable(con, "punctual", shpFile,true, new EmptyProgressVisitor());
            // Read this shape file to check values
            assertTrue(shpFile.exists());
            stat.execute("DROP TABLE IF EXISTS IMPORT_PUNCTUAL;");
            driver.importFile(con, "IMPORT_PUNCTUAL", shpFile, true, new EmptyProgressVisitor());
            ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_PUNCTUAL;");
            res.next();
            Geometry geom = (Geometry) res.getObject(1);
            assertEquals(4326, geom.getSRID());
            Coordinate coord = geom.getCoordinate();
            assertEquals(coord.z, 5, 10E-1);
            stat.execute("DROP TABLE IF EXISTS IMPORT_PUNCTUAL;");
            res.close();
        }
    }

    @Test
    public void testSelectWriteReadPostGIS(TestInfo testInfo) throws Exception {
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
            try (Statement stat = con.createStatement()) {
                stat.execute(" DROP TABLE IF EXISTS orbisgis;"
                        + "CREATE TABLE orbisgis (id int, the_geom geometry(point, 4326));"
                        + "INSERT INTO orbisgis VALUES (1, ST_GEOMFROMTEXT('POINT(10 10)',4326)), (2, ST_GEOMFROMTEXT('POINT(1 1)',4326)); ");
                SHPDriverFunction shpDriverFunction = new SHPDriverFunction();
                shpDriverFunction.exportTable(con, "(SELECT st_buffer(the_geom, 10) as the_geom from orbisgis)", new File("target/points.shp"), null, true, new EmptyProgressVisitor());
                shpDriverFunction.importFile(con, "TABLE_POINTS_READ", new File("target/points.shp"), null, true, new EmptyProgressVisitor());
                ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
                res.next();
                Geometry geom = (Geometry) res.getObject("THE_GEOM");
                assertEquals(4326, geom.getSRID());
                GeometryAsserts.assertGeometryEquals("SRID=4326;MULTIPOLYGON (((0 9.999999999999968, 0.1921471959676886 11.950903220161248, 0.761204674887118 13.826834323650864, 1.685303876974526 15.55570233019599, 2.928932188134495 17.071067811865447, 4.444297669803942 18.314696123025428, 6.173165676349064 19.238795325112854, 8.049096779838678 19.807852804032297, 9.999999999999963 20, 11.950903220161248 19.80785280403231, 13.826834323650868 19.238795325112882, 15.555702330195995 18.31469612302547, 17.071067811865454 17.071067811865497, 18.31469612302544 15.555702330196045, 19.23879532511286 13.826834323650921, 19.8078528040323 11.950903220161305, 20 10, 19.807852804032308 8.049096779838719, 19.238795325112868 6.173165676349106, 18.314696123025456 4.444297669803983, 17.071067811865483 2.9289321881345307, 15.55570233019603 1.6853038769745528, 13.826834323650909 0.7612046748871375, 11.950903220161296 0.19214719596769747, 10.000000000000016 0, 8.049096779838735 0.19214719596769214, 6.173165676349122 0.7612046748871251, 4.444297669803995 1.6853038769745368, 2.9289321881345405 2.9289321881345085, 1.6853038769745616 4.444297669803957, 0.7612046748871428 6.173165676349077, 0.19214719596770102 8.049096779838688, 0 9.999999999999968)))", geom);
                res.close();
                stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            }
        }
    }
    
    @Test
    public void exportImportNotSensitive() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File shpFile = new File("target/punctual_export.shp");
        stat.execute("DROP TABLE IF EXISTS PUNCTUAL");
        stat.execute("create table punctual(idarea int primary key, the_geom GEOMETRY(POINT Z))");
        stat.execute("insert into punctual values(1, 'POINT(-10 109 5)')");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/punctual_export.shp', 'punctual', true)");
        // Read this shape file to check values
        assertTrue(shpFile.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_PUNCTUAL;");
        stat.execute("CALL SHPRead('target/punctual_export.shp', 'IMPORT_PUNCTUAL')");
        ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_PUNCTUAL;");
        res.next();
        Geometry geom = (Geometry) res.getObject(1);
        Coordinate coord = geom.getCoordinate();
        assertEquals(coord.z, 5, 10E-1);
        res.close();
    }
}
