package org.h2gis.utilities;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class JDBCUtilitiesTest {
    private static Connection connection;

    @BeforeClass
    public static void init() throws Exception {
        String dataBaseLocation = "target/JDBCUtilitiesTest";
        String databasePath = "jdbc:h2:"+dataBaseLocation;
        File dbFile = new File(dataBaseLocation+".h2.db");
        Class.forName("org.h2.Driver");
        if(dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(databasePath,
                "sa", "");
    }
    @AfterClass
    public static void dispose() throws Exception {
        connection.close();
    }

    @Test
    public void testTemporaryTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS TEMPTABLE1,perstable");
        connection.createStatement().execute("CREATE TEMPORARY TABLE TEMPTABLE1");
        connection.createStatement().execute("CREATE TABLE perstable");
        assertTrue(JDBCUtilities.isTemporaryTable(connection, "temptable1"));
        assertFalse(JDBCUtilities.isTemporaryTable(connection, "PERSTable"));
        connection.createStatement().execute("DROP TABLE TEMPTABLE1,perstable");
    }

    @Test
    public void testRowCount() throws SQLException {
        connection.createStatement().execute("DROP SCHEMA IF EXISTS testschema");
        connection.createStatement().execute("CREATE SCHEMA testschema");
        connection.createStatement().execute("DROP TABLE IF EXISTS testschema.testRowCount");
        connection.createStatement().execute("CREATE TABLE testschema.testRowCount(id integer primary key, val double)");
        connection.createStatement().execute("INSERT INTO testschema.testRowCount VALUES (1, 0.2)");
        connection.createStatement().execute("INSERT INTO testschema.testRowCount VALUES (2, 0.2)");
        connection.createStatement().execute("INSERT INTO testschema.testRowCount VALUES (3, 0.5)");
        connection.createStatement().execute("INSERT INTO testschema.testRowCount VALUES (4, 0.6)");
        assertEquals(4, JDBCUtilities.getRowCount(connection, "TESTSCHEMA.TESTROWCOUNT"));
    }

    @Test
    public void testPrimaryKeyExtract() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS TEMPTABLE");
        connection.createStatement().execute("CREATE TABLE TEMPTABLE(id integer primary key)");
        assertEquals(1, JDBCUtilities.getIntegerPrimaryKey(connection.getMetaData(), "TEMPTABLE"));
        connection.createStatement().execute("DROP SCHEMA IF EXISTS SCHEM");
        connection.createStatement().execute("CREATE SCHEMA SCHEM");
        connection.createStatement().execute("DROP TABLE IF EXISTS SCHEM.TEMPTABLE");
        connection.createStatement().execute("CREATE TABLE SCHEM.TEMPTABLE(id integer primary key)");
        connection.createStatement().execute("DROP TABLE IF EXISTS TEMPTABLE");
        connection.createStatement().execute("CREATE TABLE TEMPTABLE(id varchar primary key)");
        assertEquals(0, JDBCUtilities.getIntegerPrimaryKey(connection.getMetaData(), "TEMPTABLE"));
        connection.createStatement().execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test
    public void testGetFieldNameFromIndex() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS TEMPTABLE");
        connection.createStatement().execute("CREATE TABLE TEMPTABLE(id integer, name varchar)");
        assertEquals("ID", JDBCUtilities.getFieldName(connection.getMetaData(), "TEMPTABLE", 1));
        assertEquals("NAME", JDBCUtilities.getFieldName(connection.getMetaData(), "TEMPTABLE", 2));
        connection.createStatement().execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test
    public void isH2() throws SQLException {
        assertTrue(JDBCUtilities.isH2DataBase(connection.getMetaData()));
    }
}
