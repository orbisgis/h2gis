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

package org.h2gis.functions.io.dbf;

import org.h2.util.StringUtils;
import org.h2gis.functions.io.dbf.internal.DBFDriver;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.functions.io.shp.SHPEngineTest;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class DBFImportExportTest {
    private static Connection connection;
    private static final String DB_NAME = "DBFImportExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new DBFRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new DBFWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportTableTestGeomEnd() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, value DOUBLE, descr CHAR(50))");
        stat.execute("insert into area values(1, 4.9406564584124654, 'main area')");
        stat.execute("insert into area values(2, 2.2250738585072009, 'second area')");
        // Create a dbf file using table area
        stat.execute("CALL DBFWrite('target/area_export.dbf', 'AREA')");
        // Read this dbf file to check values
        assertTrue(dbfFile.exists());
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(dbfFile);
        assertEquals(3, dbfDriver.getFieldCount());
        assertEquals(2, dbfDriver.getRowCount());
        Object[] row = dbfDriver.getRow(0);
        assertEquals(1, row[0]);
        assertEquals(4.9406564584124654, (Double) row[1], 1e-12);
        assertEquals("main area", row[2]);
        row = dbfDriver.getRow(1);
        assertEquals(2, row[0]);
        assertEquals(2.2250738585072009, (Double) row[1], 1e-12);
        assertEquals("second area", row[2]);
    }

    @Test
    public void importTableTestGeomEnd() throws SQLException, IOException {
        Statement st = connection.createStatement();
        final String path = SHPEngineTest.class.getResource("waternetwork.dbf").getPath();
        DriverFunction driver = new DBFDriverFunction();
        st.execute("DROP TABLE IF EXISTS waternetwork");
        driver.importFile(connection, "WATERNETWORK", new File(path), new EmptyProgressVisitor());
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'WATERNETWORK'");
        assertTrue(rs.next());
        assertEquals(H2TableIndex.PK_COLUMN_NAME,rs.getString("COLUMN_NAME"));
        assertEquals("INTEGER", rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
        assertEquals("VARCHAR", rs.getString("TYPE_NAME"));
        assertEquals(254, rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        assertTrue(rs.next());
        assertEquals("GID",rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT", rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
        assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
        rs.close();
        // Check content
        rs = st.executeQuery("SELECT * FROM WATERNETWORK");
        assertTrue(rs.next());
        assertEquals("river",rs.getString("type_axe"));
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

    /**
     * Read a DBF where the encoding is missing in header.
     * @throws SQLException
     */
    @Test
    public void readDBFRussianEncodingTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists sotchi");
        st.execute("CALL DBFREAD("+ StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("sotchi.dbf").getPath())+", 'SOTCHI', 'cp1251');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM sotchi");
        // Check if fields name are OK
        ResultSetMetaData meta = rs.getMetaData();
        assertEquals("B_ДНА",meta.getColumnName(5));
        assertEquals("ИМЕНА_УЧАС",meta.getColumnName(8));
        assertEquals("ДЛИНА_КАНА",meta.getColumnName(9));
        assertEquals("ДЛИНА_КАН_",meta.getColumnName(10));
        assertEquals("ИМЯ_МУООС",meta.getColumnName(11));
        assertTrue(rs.next());
        assertEquals("ВП-2", rs.getString("NAMESHEME"));
        assertEquals("Дубовский канал",rs.getString("NAME10000"));
        assertTrue(rs.next());
        assertEquals("ВП-2-кр1-2", rs.getString("NAMESHEME"));
        assertTrue(rs.next());
        assertEquals("ВП-1", rs.getString("NAMESHEME"));
        assertTrue(rs.next());
        assertEquals("ВП-2-кр1-4", rs.getString("NAMESHEME"));
        assertTrue(rs.next());
        assertEquals("ВП-2-кр1-4-8", rs.getString("NAMESHEME"));
        assertFalse(rs.next());
        rs.close();
        st.execute("drop table sotchi");
    }

    @Test
    public void testPkDuplicate() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA, AREA2");
        stat.execute("create table area("+H2TableIndex.PK_COLUMN_NAME+" serial, value DOUBLE, descr CHAR(50))");
        stat.execute("insert into area values(null, 4.9406564584124654, 'main area')");
        stat.execute("insert into area values(null, 2.2250738585072009, 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('"+dbfFile.getPath()+"', 'AREA')");
        // Read this shape file to check values
        stat.execute("CALL DBFRead('"+dbfFile.getPath()+"', 'AREA2')");
        ResultSet rs = stat.executeQuery("SELECT * FROM AREA2");
        assertEquals(H2TableIndex.PK_COLUMN_NAME+"2", rs.getMetaData().getColumnName(1));
        assertEquals(H2TableIndex.PK_COLUMN_NAME, rs.getMetaData().getColumnName(2));
    }

    @Test
    public void testWriteDecimal() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA, AREA2");
        stat.execute("create table area(id integer, value DECIMAL(13,3), descr CHAR(50))");
        double v1 = 40656458.41;
        double v2 = 25073858.50;
        stat.execute("insert into area values(1, "+v1+", 'main area')");
        stat.execute("insert into area values(2, "+v2+", 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('"+dbfFile.getPath()+"', 'AREA')");
        // Read this shape file to check values
        stat.execute("CALL DBFRead('"+dbfFile.getPath()+"', 'AREA2')");
        ResultSet rs = stat.executeQuery("SELECT value FROM AREA2 order by id");
        assertTrue(rs.next());
        assertEquals(v1, rs.getDouble(1), 1e-12);
        assertTrue(rs.next());
        assertEquals(v2, rs.getDouble(1), 1e-12);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void testWriteReal() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA, AREA2");
        stat.execute("create table area(id integer, value REAL, descr CHAR(50))");
        double v1 = 406.56;
        double v2 = 250.73;
        stat.execute("insert into area values(1, "+v1+", 'main area')");
        stat.execute("insert into area values(2, "+v2+", 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('"+dbfFile.getPath()+"', 'AREA')");
        // Read this shape file to check values
        stat.execute("CALL DBFRead('"+dbfFile.getPath()+"', 'AREA2')");
        ResultSet rs = stat.executeQuery("SELECT value FROM AREA2 order by id");
        assertTrue(rs.next());
        assertEquals(v1, rs.getDouble(1), 1e-2);
        assertTrue(rs.next());
        assertEquals(v2, rs.getDouble(1), 1e-2);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void testWriteReadEmptyTable1() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_EMPTY");
        stat.execute("DROP TABLE IF EXISTS TABLE_EMPTY_READ");
        stat.execute("create table TABLE_EMPTY(id INTEGER)");
        stat.execute("CALL DBFWrite('target/empty.dbf', 'TABLE_EMPTY');");
        stat.execute("CALL DBFRead('target/empty.dbf', 'TABLE_EMPTY_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_EMPTY_READ;");
        ResultSetMetaData rsmd = res.getMetaData();
        assertTrue(rsmd.getColumnCount()==2);
        assertTrue(!res.next());
        stat.close();
    }
    
    @Test
    public void testWriteReadEmptyTable2() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS TABLE_EMPTY");
        stat.execute("DROP TABLE IF EXISTS TABLE_EMPTY_READ");
        stat.execute("create table TABLE_EMPTY()");
        stat.execute("CALL DBFWrite('target/empty.dbf', 'TABLE_EMPTY');");
        stat.execute("CALL DBFRead('target/empty.dbf', 'TABLE_EMPTY_READ');");
        ResultSet res = stat.executeQuery("SELECT * FROM TABLE_EMPTY_READ;");
        ResultSetMetaData rsmd = res.getMetaData();
        assertTrue(rsmd.getColumnCount()==0);
        assertTrue(!res.next());
        stat.close();
    }
}
