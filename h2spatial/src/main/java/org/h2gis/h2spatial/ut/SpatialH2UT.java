/**
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

package org.h2gis.h2spatial.ut;

import org.h2.util.OsgiDataSourceFactory;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Used to create quickly a database on unit tests.
 * @author Nicolas Fortin
 */
public class SpatialH2UT {

    private static final String H2_PARAMETERS = ";LOCK_MODE=0;LOG=0;DB_CLOSE_DELAY=5"; //;DEFAULT_TABLE_ENGINE=org.h2.mvstore.db.MVTableEngine;LOCK_MODE=0;LOG=0";
    //private static final String H2_PARAMETERS = ";DEFAULT_TABLE_ENGINE=org.h2.mvstore.db.MVTableEngine;LOCK_MODE=0;LOG=0";
    private SpatialH2UT() {
        // utility
    }

    /**
     * Open the connection to an existing database
     * @param dbName
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Connection openSpatialDataBase(String dbName) throws SQLException, ClassNotFoundException {
        String dbFilePath = getDataBasePath(dbName);       
        String databasePath = "jdbc:h2:"+ dbFilePath + H2_PARAMETERS;
        org.h2.Driver.load();
        // Keep a connection alive to not close the DataBase on each unit test
        return DriverManager.getConnection(databasePath,
                "sa", "sa");
    }
    /**
     * Create a spatial database
     * @param dbName filename
     * @return Connection
     * @throws Exception
     */
    public static Connection createSpatialDataBase(String dbName)throws SQLException, ClassNotFoundException {
        return createSpatialDataBase(dbName,true);
    }
    private static String getDataBasePath(String dbName) {
        return "target/test-resources/dbH2"+dbName;
    }

    /**
     * Create a database and return a DataSource
     * @param dbName
     * @param initSpatial
     * @return
     * @throws SQLException
     */
    public static DataSource createDataSource(String dbName ,boolean initSpatial) throws SQLException {
        // Create H2 memory DataSource
        org.h2.Driver driver = org.h2.Driver.load();
        OsgiDataSourceFactory dataSourceFactory = new OsgiDataSourceFactory(driver);
        Properties properties = new Properties();
        String databasePath = initDBFile(dbName);
        properties.setProperty(DataSourceFactory.JDBC_URL, databasePath);
        properties.setProperty(DataSourceFactory.JDBC_USER, "sa");
        properties.setProperty(DataSourceFactory.JDBC_PASSWORD, "sa");
        DataSource dataSource = dataSourceFactory.createDataSource(properties);
        // Init spatial ext
        if(initSpatial) {
            Connection connection = dataSource.getConnection();
            try {
                CreateSpatialExtension.initSpatialExtension(connection);
            } finally {
                connection.close();
            }
        }
        return dataSource;
    }

    private static String initDBFile( String dbName ) {
        String dbFilePath = getDataBasePath(dbName);
        File dbFile = new File(dbFilePath +".h2.db");
        String databasePath = "jdbc:h2:"+ dbFilePath + H2_PARAMETERS;
        if(dbFile.exists()) {
            dbFile.delete();
        }
        return databasePath;
    }

    /**
     * Create a spatial database
     * @param dbName filename
     * @param initSpatial If true add spatial features to the database
     * @return Connection
     * @throws Exception
     */
    public static Connection createSpatialDataBase(String dbName,boolean initSpatial)throws SQLException, ClassNotFoundException {
        String databasePath = initDBFile(dbName);
        org.h2.Driver.load();
        // Keep a connection alive to not close the DataBase on each unit test
        Connection connection = DriverManager.getConnection(databasePath,
                "sa", "sa");
        Statement st = connection.createStatement();
        //Create one row table for tests
        st.execute("CREATE TABLE dummy(id INTEGER);");
        st.execute("INSERT INTO dummy values (1)");
        // Init spatial ext
        if(initSpatial) {
            CreateSpatialExtension.initSpatialExtension(connection);
        }
        return connection;
    }
}
