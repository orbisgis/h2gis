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

package org.h2gis.utilities;

import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.junit.*;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * @author Nicolas Fortin
 */
public class JDBCUtilitiesTest {

    private static Connection connection;
    private static Statement st;

    @BeforeClass
    public static void init() throws Exception {
        String dataBaseLocation = new File("target/JDBCUtilitiesTest").getAbsolutePath();
        String databasePath = "jdbc:h2:"+dataBaseLocation;
        File dbFile = new File(dataBaseLocation+".mv.db");
        Class.forName("org.h2.Driver");
        if(dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(databasePath,
                "sa", "");
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterClass
    public static void dispose() throws Exception {
        connection.close();
    }

    @Test
    public void testTemporaryTable() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEMPTABLE1,perstable");
        st.execute("CREATE TEMPORARY TABLE TEMPTABLE1");
        st.execute("CREATE TABLE perstable");
        assertTrue(JDBCUtilities.isTemporaryTable(connection, "temptable1"));
        assertFalse(JDBCUtilities.isTemporaryTable(connection, "PERSTable"));
        st.execute("DROP TABLE TEMPTABLE1,perstable");
    }

    @Test
    public void testRowCount() throws SQLException {
        st.execute("DROP SCHEMA IF EXISTS testschema");
        st.execute("CREATE SCHEMA testschema");
        st.execute("DROP TABLE IF EXISTS testschema.testRowCount");
        st.execute("CREATE TABLE testschema.testRowCount(id integer primary key, val double)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (1, 0.2)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (2, 0.2)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (3, 0.5)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (4, 0.6)");
        assertEquals(4, JDBCUtilities.getRowCount(connection, "TESTSCHEMA.TESTROWCOUNT"));
    }

    @Test
    public void testPrimaryKeyExtract() throws SQLException {
        st.execute("DROP SCHEMA IF EXISTS ATEMPSCHEMA");
        st.execute("CREATE SCHEMA ATEMPSCHEMA");
        st.execute("CREATE TABLE ATEMPSCHEMA.TEMPTABLE(id integer)");
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer primary key)");
        assertEquals(1, JDBCUtilities.getIntegerPrimaryKey(connection, "TEMPTABLE"));
        st.execute("DROP SCHEMA IF EXISTS SCHEM");
        st.execute("CREATE SCHEMA SCHEM");
        st.execute("DROP TABLE IF EXISTS SCHEM.TEMPTABLE");
        st.execute("CREATE TABLE SCHEM.TEMPTABLE(id integer primary key)");
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id varchar primary key)");
        assertEquals(0, JDBCUtilities.getIntegerPrimaryKey(connection, "TEMPTABLE"));
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test(expected = SQLException.class)
    public void testPrimaryKeyExtractOnNonexistantTable() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        try {
            JDBCUtilities.getIntegerPrimaryKey(connection, "TEMPTABLE");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Table TEMPTABLE not found"));
            throw e;
        }
    }

    @Test
    public void testGetFieldNameFromIndex() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer, name varchar)");
        assertEquals("ID", JDBCUtilities.getFieldName(connection.getMetaData(), "TEMPTABLE", 1));
        assertEquals("NAME", JDBCUtilities.getFieldName(connection.getMetaData(), "TEMPTABLE", 2));
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test
    public void testTableExists() throws SQLException {
        // Don't use quotes
        st.execute("DROP TABLE IF EXISTS temptable");
        st.execute("CREATE TABLE temptable(id integer, name varchar)");
        assertTrue(JDBCUtilities.tableExists(connection, "TEMPTABLE"));
        assertFalse(JDBCUtilities.tableExists(connection, "temptable"));
        assertFalse(JDBCUtilities.tableExists(connection, "teMpTAbLE"));
        assertFalse(JDBCUtilities.tableExists(connection, "\"teMpTAbLE\""));
        st.execute("DROP TABLE IF EXISTS teMpTAbLE");
        st.execute("CREATE TABLE teMpTAbLE(id integer, name varchar)");
        assertTrue(JDBCUtilities.tableExists(connection, "TEMPTABLE"));
        assertFalse(JDBCUtilities.tableExists(connection, "temptable"));
        assertFalse(JDBCUtilities.tableExists(connection, "teMpTAbLE"));
        assertFalse(JDBCUtilities.tableExists(connection, "\"teMpTAbLE\""));
        // Use quotes
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("DROP TABLE IF EXISTS \"teMpTAbLE\"");
        st.execute("CREATE TABLE \"teMpTAbLE\"(id integer, name varchar)");
        assertTrue(JDBCUtilities.tableExists(connection, "\"teMpTAbLE\""));
        assertTrue(JDBCUtilities.tableExists(connection, "teMpTAbLE"));
        assertFalse(JDBCUtilities.tableExists(connection, "temptable"));
        assertFalse(JDBCUtilities.tableExists(connection, "TEMPTABLE"));
    }

    @Test
    public void isH2() throws SQLException {
        assertTrue(JDBCUtilities.isH2DataBase(connection.getMetaData()));
    }

    @Test
    public void testHasField() throws SQLException {
        st.execute("DROP TABLE IF EXISTS temptable");
        st.execute("CREATE TABLE temptable(id integer, name varchar)");
        assertTrue(JDBCUtilities.hasField(connection, "TEMPTABLE", "ID"));
        // The field name does not necessarily need to be capitalized.
        assertTrue(JDBCUtilities.hasField(connection, "TEMPTABLE", "id"));
        // The table name needs to be capitalized
        assertFalse(JDBCUtilities.hasField(connection, "temptable", "id"));
        assertFalse(JDBCUtilities.hasField(connection, "TEMPTABLE", "some_other_field"));
    }

    @Test
    public void testCancel() throws SQLException {
        boolean aborted = false;
        ProgressVisitor progressVisitor = new EmptyProgressVisitor();
        Statement statement = connection.createStatement();
        PropertyChangeListener listener = JDBCUtilities.attachCancelResultSet(statement, progressVisitor);
        statement.execute("CREATE ALIAS SLEEP FOR \"java.lang.Thread.sleep\"");
        CancelThread cancelThread = new CancelThread(progressVisitor);
        cancelThread.start();
        try {
            statement.execute("SELECT SLEEP(10) FROM SYSTEM_RANGE(1, 200)");
        } catch (SQLException ex) {
            aborted = true;
        } finally {
            progressVisitor.removePropertyChangeListener(listener);
        }
        statement.close();
        assertTrue(aborted);
    }

    private static class CancelThread extends Thread {
        private ProgressVisitor pm;

        private CancelThread(ProgressVisitor pm) {
            this.pm = pm;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // Ignore
            }
            pm.cancel();
        }
    }

    @Test
    public void testIsLinkedTable() throws ClassNotFoundException, SQLException {
        String dataBaseLocation = new File("target/JDBCUtilitiesTest2").getAbsolutePath();
        String databasePath = "jdbc:h2:"+dataBaseLocation;
        File dbFile = new File(dataBaseLocation+".mv.db");
        Class.forName("org.h2.Driver");
        if(dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        Connection con = DriverManager.getConnection(databasePath, "sa", "");

        Statement st = con.createStatement();
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer, name varchar)");

        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS LINKEDTABLE");
        statement.execute("CREATE LINKED TABLE LINKEDTABLE('org.h2.Driver', '"+databasePath+"', 'sa', '', 'TEMPTABLE');");
        assertTrue(JDBCUtilities.isLinkedTable(connection, "LINKEDTABLE"));
    }
    
    @Test
    public void testCreateEmptyTable() throws SQLException {
        st.execute("drop table if exists emptytable");
        JDBCUtilities.createEmptyTable(connection, "emptytable");
        ResultSet res = st.executeQuery("SELECT * FROM emptytable;");
        ResultSetMetaData rsmd = res.getMetaData();
        assertTrue(rsmd.getColumnCount()==0);
        assertTrue(!res.next());
        st.execute("DROP TABLE emptytable");
    }
}
