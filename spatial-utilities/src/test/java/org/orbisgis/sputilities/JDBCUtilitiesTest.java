package org.orbisgis.sputilities;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
        assertTrue(JDBCUtilities.isTemporaryTable(connection, TableLocation.parse("temptable1")));
        assertFalse(JDBCUtilities.isTemporaryTable(connection, TableLocation.parse("PERSTable")));
        connection.createStatement().execute("DROP TABLE TEMPTABLE1,perstable");
    }
}
