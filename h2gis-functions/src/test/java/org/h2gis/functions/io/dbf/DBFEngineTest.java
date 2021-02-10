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

import org.apache.commons.io.FileUtils;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.functions.io.shp.SHPEngineTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Fortin
 */
public class  DBFEngineTest {
    private static Connection connection;
    private static final String DB_NAME = "DBFEngineTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new DriverManager(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new DBFRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new DBFWrite(), "");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void readDBFMetaTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'DBFTABLE');");
        try ( // Query declared Table columns
            ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'DBFTABLE'")) {
            assertTrue(rs.next());
            assertEquals(H2TableIndex.PK_COLUMN_NAME,rs.getString("COLUMN_NAME"));
            assertEquals("BIGINT",rs.getString("TYPE_NAME"));
            assertTrue(rs.next());
            assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
            assertEquals("CHAR",rs.getString("TYPE_NAME"));
            assertEquals(254,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
            assertTrue(rs.next());
            assertEquals("GID",rs.getString("COLUMN_NAME"));
            assertEquals("BIGINT",rs.getString("TYPE_NAME"));
            assertEquals(19,rs.getInt("NUMERIC_PRECISION"));
            assertTrue(rs.next());
            assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
            assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
            assertEquals(20,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        }
        st.execute("drop table dbftable");
    }


    @Test
    public void readDBFDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'DBFTABLE');");
        try ( // Query declared Table columns
                ResultSet rs = st.executeQuery("SELECT * FROM dbftable")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("gid"));
            assertEquals("river",rs.getString("type_axe"));
        }
        st.execute("drop table dbftable");
    }

    @Test
    public void testRowIdHiddenColumn() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'DBFTABLE');");
        // Check random access using hidden column _rowid_
        PreparedStatement pst = connection.prepareStatement("SELECT * FROM dbftable where _rowid_ = ?");
        pst.setInt(1, 1);
        ResultSet rs = pst.executeQuery();
        try {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("gid"));
            assertEquals("river",rs.getString("type_axe"));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT _rowid_ FROM dbftable");
        try {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
        } finally {
            rs.close();
        }
        st.execute("drop table if exists dbftable");

    }

    @Test
    public void readDBFEncodingTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("encoding_test.dbf").getPath())+", 'DBFTABLE');");
        try ( // Query declared Table columns
                ResultSet rs = st.executeQuery("SELECT * FROM dbftable")) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("RIVERTYPE"));
            assertEquals("松柏坑溪",rs.getString("RIVERNAME"));
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("RIVERTYPE"));
            assertEquals("劍潭湖",rs.getString("RIVERNAME"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("RIVERTYPE"));
            assertEquals("竹篙水溪",rs.getString("RIVERNAME"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("RIVERTYPE"));
            assertEquals("霞苞蓮幹線",rs.getString("RIVERNAME"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("RIVERTYPE"));
            assertEquals("延潭大排水溝",rs.getString("RIVERNAME"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("RIVERTYPE"));
            assertEquals("林內圳幹線",rs.getString("RIVERNAME"));
        }
        st.execute("drop table dbftable");
    }

    @Test
    public void testReopenMovedDbf() throws Exception {
        // Copy file in target
        File srcDbf = new File(SHPEngineTest.class.getResource("waternetwork.dbf").getPath());
        File dstDbf = new File("target/waternetwork.dbf");
        if(dstDbf.exists()){
            dstDbf.delete();       
        }   
        FileUtils.copyFile(srcDbf, dstDbf);
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE('" + dstDbf + "', 'DBFTABLE');");
        st.execute("SHUTDOWN");
        // Close database
        connection.close();
        try {
            // Wait a while
            Thread.sleep(1000);
            // Remove temp file
            assertTrue(dstDbf.delete());
            // Reopen it
        } finally {
            connection = H2GISDBFactory.openSpatialDataBase(DB_NAME);
            st = connection.createStatement();
        }
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) cpt FROM dbftable")) {
            assertTrue(rs.next());
            // The new table should be empty
            assertEquals(0,rs.getInt("cpt"));
        }
        st.execute("drop table if exists dbftable");
    }




    /**
     * Read a DBF where the encoding is missing in header. Then write it with a good header. Then check the content.
     * @throws SQLException
     */
    @Test
    public void readDBFRussianWrongEncodingThenWriteThenRead() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists sotchi");
        st.execute("CALL DBFREAD("+StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("sotchi.dbf").getPath())+", 'SOTCHI', 'cp1251');");
        st.execute("CALL DBFWRITE('target/sotchi.dbf', 'SOTCHI', 'cp1251');");
        st.execute("drop table if exists sotchi");
        st.execute("CALL FILE_TABLE('target/sotchi.dbf', 'SOTCHI_GOODHEADER');");
        // Check if fields name are OK
        try (ResultSet rs = st.executeQuery("SELECT * FROM SOTCHI_GOODHEADER")) {
            // Check if fields name are OK
            ResultSetMetaData meta = rs.getMetaData();
            assertEquals("B_ДНА",meta.getColumnName(6));
            assertEquals("ИМЕНА_УЧАС",meta.getColumnName(9));
            assertEquals("ДЛИНА_КАНА",meta.getColumnName(10));
            assertEquals("ДЛИНА_КАН_",meta.getColumnName(11));
            assertEquals("ИМЯ_МУООС",meta.getColumnName(12));
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
        }
        st.execute("drop table SOTCHI_GOODHEADER");
    }

    //TODO : check why the data type change from DOUBLE PRECISION to REAL
    @Test
    public void testRestartDb() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'dbftable');");
        // Close Db
        st.execute("SHUTDOWN");
        connection = H2GISDBFactory.openSpatialDataBase(DB_NAME);
        st = connection.createStatement();
        try (ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'DBFTABLE'")) {
            assertTrue(rs.next());
            assertEquals(H2TableIndex.PK_COLUMN_NAME,rs.getString("COLUMN_NAME"));
            assertEquals("BIGINT",rs.getString("TYPE_NAME"));
            assertTrue(rs.next());
            assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
            assertEquals("CHAR",rs.getString("TYPE_NAME"));
            assertEquals(254,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
            assertTrue(rs.next());
            assertEquals("GID",rs.getString("COLUMN_NAME"));
            assertEquals("BIGINT",rs.getString("TYPE_NAME"));
            assertTrue(rs.next());
            assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
            assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
        }
        st.execute("drop table dbftable");
    }


   // @Test
    public void readDBFNullDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("null_values.dbf").getPath())+", 'DBFTABLE');");
        try ( // Query declared Table columns
                ResultSet rs = st.executeQuery("SELECT * FROM dbftable")) {
            assertTrue(rs.next());
            assertNull(rs.getObject("MAXSPEED"));
            assertTrue(rs.next());
            assertEquals(130, rs.getObject("MAXSPEED"));
            assertTrue(rs.next());
            assertEquals(130, rs.getObject("MAXSPEED"));
            assertTrue(rs.next());
            assertEquals(130, rs.getObject("MAXSPEED"));
            assertTrue(rs.next());
            assertEquals(90, rs.getObject("MAXSPEED"));
            assertTrue(rs.next());
            assertNull(rs.getObject("MAXSPEED"));
        }
        st.execute("drop table dbftable");
    }


    @Test
    public void readDBFCommaSeparatorNumericTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("comma_separator.dbf").getPath())+", 'DBFTABLE');");
        try ( // Query declared Table columns
                ResultSet rs = st.executeQuery("SELECT * FROM dbftable")) {
            assertTrue(rs.next());
            assertEquals(1.,rs.getDouble("PREC_PLANI"),1e-12);
        }
        st.execute("drop table dbftable");
    }

    @Test
    public void testPkColumn() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'DBFTABLE');");
        // Check usage of Primary Key
        ResultSet rs = st.executeQuery("EXPLAIN SELECT * FROM DBFTABLE WHERE "+H2TableIndex.PK_COLUMN_NAME+" = 5");
        try {
            assertTrue(rs.next());
            assertTrue(rs.getString(1).endsWith("\": "+H2TableIndex.PK_COLUMN_NAME+" = 5 */\nWHERE \""+
                    H2TableIndex.PK_COLUMN_NAME+"\" = 5"));
        } finally {
            rs.close();
        }
        // Check if row equality test is good
        rs = st.executeQuery("SELECT * FROM DBFTABLE WHERE "+H2TableIndex.PK_COLUMN_NAME+" = 5");
        try {
            assertTrue(rs.next());
            assertEquals(5, rs.getInt(H2TableIndex.PK_COLUMN_NAME));
            assertEquals(5, rs.getInt("GID"));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
    }
}
