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
package org.h2gis.drivers;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A factory to create a database
 *
 * @author Erwan Bocher
 */
public class DataBaseFactory {

    public static final String H2_PARAMETERS = ";LOCK_MODE=0;LOG=0;DB_CLOSE_DELAY=5";

    private DataBaseFactory() {
    }

    /**
     * Create a database
     *
     * @param dbName filename
     * @return Connection
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static Connection createDataBase(String dbName) throws SQLException, ClassNotFoundException {
        return createDataBase(dbName, H2_PARAMETERS);
    }

    /**
     * Create a database
     *
     * @param dbName filename
     * @param h2Parameters Additional h2 parameters
     * @return Connection
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static Connection createDataBase(String dbName, String h2Parameters) throws SQLException, ClassNotFoundException {
        String databasePath = initDBFile(dbName, h2Parameters);
        org.h2.Driver.load();
        // Keep a connection alive to not close the DataBase on each unit test
        Connection connection = java.sql.DriverManager.getConnection(databasePath,
                "sa", "sa");
        Statement st = connection.createStatement();
        //Create one row table for tests
        st.execute("CREATE TABLE dummy(id INTEGER);");
        st.execute("INSERT INTO dummy values (1)");
        return connection;
    }

    /**
     * Create the file on disk for the database
     *
     * @param dbName
     * @param h2_PARAMETERS
     * @return
     */
    private static String initDBFile(String dbName, String h2_PARAMETERS) {
        String dbFilePath = getDataBasePath(dbName);
        File dbFile = new File(dbFilePath + ".mv.db");
        String databasePath = "jdbc:h2:" + dbFilePath + h2_PARAMETERS;
        if (dbFile.exists()) {
            dbFile.delete();
        }

        dbFile = new File(dbFilePath + ".mv.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        return databasePath;
    }

    /**
     * Return a path for the file database
     *
     * @param dbName
     * @return
     */
    private static String getDataBasePath(String dbName) {
        if (dbName.startsWith("file://")) {
            return new File(URI.create(dbName)).getAbsolutePath();
        } else {
            return new File("target/test-resources/dbH2" + dbName).getAbsolutePath();
        }
    }
}
