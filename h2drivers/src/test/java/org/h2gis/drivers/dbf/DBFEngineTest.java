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

package org.h2gis.drivers.dbf;

import org.apache.commons.io.FileUtils;
import org.h2.util.StringUtils;
import org.h2gis.drivers.DriverManager;
import org.h2gis.drivers.shp.SHPEngineTest;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class  DBFEngineTest {
    private static Connection connection;
    private static final String DB_NAME = "DBFEngineTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DriverManager(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFRead(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void readDBFMetaTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'DBFTABLE');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'DBFTABLE'");
        assertTrue(rs.next());
        assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
        assertEquals("CHAR",rs.getString("TYPE_NAME"));
        assertEquals(254,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        assertTrue(rs.next());
        assertEquals("GID",rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT",rs.getString("TYPE_NAME"));
        assertEquals(18,rs.getInt("NUMERIC_PRECISION"));
        assertTrue(rs.next());
        assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
        assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
        assertEquals(20,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        rs.close();
        st.execute("drop table dbftable");
    }

    @Test
    public void readDBFDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists dbftable");
        st.execute("CALL FILE_TABLE("+StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.dbf").getPath())+", 'DBFTABLE');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM dbftable");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("gid"));
        assertEquals("river",rs.getString("type_axe"));
        rs.close();
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
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM dbftable");
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
        rs.close();
        st.execute("drop table dbftable");
    }

    @Test
    public void testReopenMovedDbf() throws Exception {
        // Copy file in target
        File srcDbf = new File(SHPEngineTest.class.getResource("waternetwork.dbf").getPath());
        File dstDbf = File.createTempFile("waternetwork",".dbf");
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
            connection = SpatialH2UT.openSpatialDataBase(DB_NAME);
            st = connection.createStatement();
        }
        ResultSet rs = st.executeQuery("SELECT COUNT(*) cpt FROM dbftable");
        try {
            assertTrue(rs.next());
            // The new table should be empty
            assertEquals(0,rs.getInt("cpt"));
        } finally {
            rs.close();
        }
        st.execute("drop table if exists dbftable");
    }

    /**
     * Read a DBF where the encoding is missing in header.
     * @throws SQLException
     */
    @Test
    public void readDBFRussianEncodingTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists sotchi");
        st.execute("CALL DBFREAD("+StringUtils.quoteStringSQL(DBFEngineTest.class.getResource("sotchi.dbf").getPath())+", 'SOTCHI', 'cp1251');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM sotchi");
        // Check if fields name are OK
        ResultSetMetaData meta = rs.getMetaData();
        assertEquals("B_ДНА",meta.getColumnName(4));
        assertEquals("ИМЕНА_УЧАС",meta.getColumnName(7));
        assertEquals("ДЛИНА_КАНА",meta.getColumnName(8));
        assertEquals("ДЛИНА_КАН_",meta.getColumnName(9));
        assertEquals("ИМЯ_МУООС",meta.getColumnName(10));
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
        ResultSet rs = st.executeQuery("SELECT * FROM SOTCHI_GOODHEADER");
        // Check if fields name are OK
        ResultSetMetaData meta = rs.getMetaData();
        assertEquals("B_ДНА",meta.getColumnName(4));
        assertEquals("ИМЕНА_УЧАС",meta.getColumnName(7));
        assertEquals("ДЛИНА_КАНА",meta.getColumnName(8));
        assertEquals("ДЛИНА_КАН_",meta.getColumnName(9));
        assertEquals("ИМЯ_МУООС",meta.getColumnName(10));
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
        st.execute("drop table SOTCHI_GOODHEADER");
    }
}
