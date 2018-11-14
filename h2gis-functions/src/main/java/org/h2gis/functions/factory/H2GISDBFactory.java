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

package org.h2gis.functions.factory;

import org.h2.util.OsgiDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Used to create quickly a database on unit tests.
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class H2GISDBFactory {

    public static final String H2_PARAMETERS = ";LOCK_MODE=0;LOG=0;DB_CLOSE_DELAY=5";

    private H2GISDBFactory() {
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
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static Connection createSpatialDataBase(String dbName)throws SQLException, ClassNotFoundException {
        return createSpatialDataBase(dbName,true);
    }

    /**
     * Return the path of the file database
     * @param dbName
     * @return 
     */
    private static String getDataBasePath(String dbName) {
        if(dbName.startsWith("file://")) {
            return new File(URI.create(dbName)).getAbsolutePath();
        } else {
            return new File("target/test-resources/dbH2" + dbName).getAbsolutePath();
        }
    }

    /**
     * Create a database and return a DataSource
     * @param dbName DataBase name, or path URI
     * @param initSpatial True to enable basic spatial capabilities
     * @return DataSource
     * @throws SQLException
     */
    public static DataSource createDataSource(String dbName ,boolean initSpatial) throws SQLException {
        return createDataSource(dbName, initSpatial, H2_PARAMETERS);
    }

    /**
     * Create a database and return a DataSource
     * @param dbName
     * @param initSpatial
     * @param h2Parameters
     * @return
     * @throws SQLException
     */
    public static DataSource createDataSource(String dbName ,boolean initSpatial, String h2Parameters) throws SQLException {
        // Create H2 memory DataSource
        org.h2.Driver driver = org.h2.Driver.load();
        OsgiDataSourceFactory dataSourceFactory = new OsgiDataSourceFactory(driver);
        Properties properties = new Properties();
        String databasePath = initDBFile(dbName, h2Parameters);
        properties.setProperty(DataSourceFactory.JDBC_URL, databasePath);
        properties.setProperty(DataSourceFactory.JDBC_USER, "sa");
        properties.setProperty(DataSourceFactory.JDBC_PASSWORD, "sa");
        DataSource dataSource = dataSourceFactory.createDataSource(properties);
        // Init spatial ext
        if(initSpatial) {
            try (Connection connection = dataSource.getConnection()) {
                H2GISFunctions.load(connection);
            }
        }
        return dataSource;
    }
    
    /**
     * 
     * @param dbName
     * @param h2_PARAMETERS
     * @return 
     */
    private static String initDBFile( String dbName, String h2_PARAMETERS ) {
        String dbFilePath = getDataBasePath(dbName);
        File dbFile = new File(dbFilePath +".mv.db");
        String databasePath = "jdbc:h2:"+ dbFilePath + h2_PARAMETERS;
        if(dbFile.exists()) {
            dbFile.delete();
        }
        
        dbFile = new File(dbFilePath +".mv.db");
        if(dbFile.exists()) {
            dbFile.delete();
        }
        return databasePath;
    }

    /**
     * Create a spatial database
     * @param dbName filename
     * @param initSpatial If true add spatial features to the database
     * @param h2Parameters Additional h2 parameters
     * @return Connection
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static Connection createSpatialDataBase(String dbName,boolean initSpatial, String h2Parameters )throws SQLException, ClassNotFoundException {
        String databasePath = initDBFile(dbName, h2Parameters);
        org.h2.Driver.load();
        // Keep a connection alive to not close the DataBase on each unit test
        Connection connection = DriverManager.getConnection(databasePath,
                "sa", "sa");
        // Init spatial ext
        if(initSpatial) {
            H2GISFunctions.load(connection);
        }
        return connection;
    }
    
    /**
     * Create a spatial database and register all H2GIS functions
     * @param dbName filename
     * @param initSpatial If true add spatial features to the database
     * @return Connection
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static Connection createSpatialDataBase(String dbName, boolean initSpatial )throws SQLException, ClassNotFoundException {
        return createSpatialDataBase(dbName, initSpatial, H2_PARAMETERS);
    }
}
