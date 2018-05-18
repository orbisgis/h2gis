/*
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

import org.h2.tools.SimpleResultSet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit test of TableUtilities
 *
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class TableUtilitiesTest {

    /** Test database connection. */
    private static Connection connection;

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

        connection.createStatement().execute("DROP TABLE IF EXISTS TATA");
        connection.createStatement().execute("CREATE TABLE TATA (id INT, str VARCHAR(100))");
        connection.createStatement().execute("INSERT INTO TATA VALUES (25, 'twenty five')");
        connection.createStatement().execute("INSERT INTO TATA VALUES (6, 'six')");
    }

    @Test
    public void copyFieldTest() throws Exception {
        SimpleResultSet rs = new SimpleResultSet();
        // Feed with fields
        TableUtilities.copyFields(connection, rs, TableLocation.parse("TATA",
                JDBCUtilities.isH2DataBase(connection.getMetaData())));
        assertEquals(2, rs.getColumnCount());
        assertEquals(1, rs.findColumn("id"));
        assertEquals(2, rs.findColumn("str"));
    }

    @Test
    public void columnListConnectionTest() throws Exception {
        assertFalse(TableUtilities.isColumnListConnection(connection));
    }

    @Test
    public void columnParseInputTableTest() throws Exception {
        assertEquals("TATA", TableUtilities.parseInputTable(connection, "TATA")
                .toString(JDBCUtilities.isH2DataBase(connection.getMetaData())));
    }

    @Test
    public void suffixTableLocationTest() throws Exception {
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        TableLocation tableLocation = TableLocation.parse("TATA", isH2);
        assertEquals("TATA_SUFF", TableUtilities.suffixTableLocation(tableLocation, "_SUFF").toString(isH2));
    }

    @Test
    public void caseIdentifierTest() throws Exception {
        TableLocation tableLocation = TableLocation.parse("TATA", true);
        assertEquals("\"TATA\"", TableUtilities.caseIdentifier(tableLocation, "TATA", true));
        assertEquals("\"tata\"", TableUtilities.caseIdentifier(tableLocation, "TATA", false));
    }
}
