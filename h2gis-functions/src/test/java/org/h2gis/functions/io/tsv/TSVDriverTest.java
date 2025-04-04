/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */


package org.h2gis.functions.io.tsv;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.postgis_jts.PostGISDBFactory;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import org.h2gis.unitTest.GeometryAsserts;

import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import org.h2.jdbc.JdbcSQLNonTransientException;

/**
 *
 * @author Erwan Bocher
 */
public class TSVDriverTest {

    private static Connection connection;
    private static final String DB_NAME = "TSVImportExportTest";
    private Statement st;

    private static final Logger log = LoggerFactory.getLogger(TSVDriverTest.class);
    private static final PostGISDBFactory dataSourceFactory = new PostGISDBFactory();

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new TSVRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new TSVWrite(), "");
        
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
    public void testDriverManager() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        File tsvFile = new File("target/area éxport.tsv");
        DriverFunction exp = new TSVDriverFunction();
        exp.exportTable(connection, "AREA", tsvFile, true, new EmptyProgressVisitor());
        stat.execute("DROP TABLE IF EXISTS mytsv");
        exp.importFile(connection, "MYTSV", tsvFile, new EmptyProgressVisitor());
        try (ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from mytsv")) {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        }
    }
    
    @Test
    public void testWriteRead() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File tsvFile = new File("target/mytsv_export.tsv");
        stat.execute("DROP TABLE IF EXISTS myTSV");
        stat.execute("create table myTSV(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into myTSV values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into myTSV values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        stat.execute("CALL TSVWrite('target/mytsv_export.tsv', 'myTSV', true)");
        assertTrue(tsvFile.exists());
        stat.execute("CALL TSVRead('target/mytsv_export.tsv', 'TSV_IMPORT');");
        try (ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from TSV_IMPORT")) {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        }
    }

    @Test
    public void testWriteReadGZ() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File tsvFile = new File("target/mytsv_export.gz");
        stat.execute("DROP TABLE IF EXISTS myTSV");
        stat.execute("create table myTSV(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into myTSV values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into myTSV values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        stat.execute("CALL TSVWrite('target/mytsv_export.gz', 'myTSV', true)");
        assertTrue(tsvFile.exists());
        stat.execute("CALL TSVRead('target/mytsv_export.gz', 'TSV_IMPORT', true);");
        try (ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from TSV_IMPORT")) {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        }
    }    
    
    
    @Test
    public void testWriteReadGZ2() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS myTSV");
        stat.execute("create table myTSV(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into myTSV values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into myTSV values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        stat.execute("CALL TSVWrite('target/mytsv_export.tsv.gz', 'myTSV', true)");
        stat.execute("CALL TSVRead('target/mytsv_export.tsv.gz', 'TSV_IMPORT', true);");
        try (ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from TSV_IMPORT")) {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        }
    }
    
    @Test
    public void testWriteReadEmptyTable() throws SQLException {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS empty_table");
            stat.execute("create table empty_table()");
            stat.execute("CALL TSVWrite('target/empty_table.tsv', 'empty_table', true);");
            stat.execute("CALL TSVRead('target/empty_table.tsv', 'empty_table_read');");
            ResultSet res = stat.executeQuery("SELECT * FROM empty_table_read;");
            ResultSetMetaData rsmd = res.getMetaData();
            assertTrue(rsmd.getColumnCount()==0);
            assertTrue(!res.next());
        }
    }
    
    @Test
    public void exportImportFileWithSpace() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            File fileOut = new File("target/lineal export.tsv");
            stat.execute("DROP TABLE IF EXISTS LINEAL");
            stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12  6)')");
            // Create a shape file using table area
            stat.execute("CALL TSVWrite('target/lineal export.tsv', 'LINEAL')");
            // Read this shape file to check values
            assertTrue(fileOut.exists());
            stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
            stat.execute("CALL TSVRead('target/lineal export.tsv')");
        });
    }
    
    @Test
    public void exportImportFileWithDot() throws SQLException, IOException {
        assertThrows(SQLException.class, () -> {
            Statement stat = connection.createStatement();
            File fileOut = new File("target/lineal.export.tsv");
            stat.execute("DROP TABLE IF EXISTS LINEAL");
            stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12  6)')");
            // Create a shape file using table area
            stat.execute("CALL TSVWrite('target/lineal.export.tsv', 'LINEAL')");
            // Read this shape file to check values
            assertTrue(fileOut.exists());
            stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
            stat.execute("CALL TSVRead('target/lineal.export.tsv')");
        });
    }
    
    
    @Test
    public void testWriteQueryRead() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File tsvFile = new File("target/mytsv_export.tsv");
        stat.execute("DROP TABLE IF EXISTS myTSV");
        stat.execute("create table myTSV(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into myTSV values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into myTSV values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        stat.execute("CALL TSVWrite('target/mytsv_export.tsv', '(SELECT * FROM myTSV)', true)");
        assertTrue(tsvFile.exists());
        stat.execute("CALL TSVRead('target/mytsv_export.tsv', 'MYTSV_IMPORT');");
        try (ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from MYTSV_IMPORT")) {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        }

    }
    
    @Test
    public void testSelectWriteReadTSVLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS, TABLE_LINESTRINGS_READ");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL TSVWrite('target/lines.tsv', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', true);");
            stat.execute("CALL TSVRead('target/lines.tsv', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            GeometryAsserts.assertGeometryEquals("LINESTRING(1 10, 20 15)", res.getString("THE_GEOM"));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testSelectWriteReadTSVLinestringPOSTGIS(TestInfo testInfo) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        Connection con= null;
        try {
            DataSource ds  = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test "+ testInfo.getDisplayName());
        }
        if(con!=null) {
            try (Statement stat = con.createStatement()) {
                stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS, TABLE_LINESTRINGS_READ");
                stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
                stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
                TSVDriverFunction tsvDriverFunction = new TSVDriverFunction();
                tsvDriverFunction.exportTable(con, "table_linestrings", new File("target/lines_postgis.tsv"),true, new EmptyProgressVisitor());
                tsvDriverFunction.importFile(con, "table_linestrings_read", new File("target/lines_postgis.tsv"), true, new EmptyProgressVisitor());
                ResultSet res = stat.executeQuery("SELECT * FROM table_linestrings_read;");
                res.next();
                GeometryAsserts.assertGeometryEquals("LINESTRING(1 10, 20 15)", res.getString("THE_GEOM"));
                res.close();
                stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
            }
        }
    }
    
    @Test
    public void testSelectWriteManyTimes() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS, TABLE_LINESTRINGS_READ");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL TSVWrite('target/lines.tsv', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', true);");
            assertThrows(JdbcSQLNonTransientException.class, () -> {
                 stat.execute("CALL TSVWrite('target/lines.tsv', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)');");
            });
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }
    

    
}
