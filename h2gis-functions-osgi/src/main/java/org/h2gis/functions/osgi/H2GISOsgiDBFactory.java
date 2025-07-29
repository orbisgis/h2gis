/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.osgi;

import org.h2.util.OsgiDataSourceFactory;
import org.h2gis.functions.factory.H2GISFunctions;
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
public class H2GISOsgiDBFactory {

    public static final String H2_PARAMETERS = ";DB_CLOSE_ON_EXIT=FALSE";

    private H2GISOsgiDBFactory() {
        // utility
    }

    /**
     * Open the connection to an existing database
     * @param dbName name of the database
     * @return a connection to the database
     */
    public static Connection openSpatialDataBase(String dbName) throws SQLException {
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
     */
    public static Connection createSpatialDataBase(String dbName)throws SQLException, ClassNotFoundException {
        return createSpatialDataBase(dbName,true);
    }

    /**
     * Return the path of the file database
     * @param dbName name of the database
     * @return a path to the database
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
     */
    public static DataSource createDataSource(String dbName ,boolean initSpatial) throws SQLException {
        return createDataSource(dbName, initSpatial, H2_PARAMETERS);
    }

     /**
     * Create a database, init spatial function and return a DataSource
     * @param properties for the opening of the DataBase.
     * @return a DataSource
     */
    public static DataSource createDataSource(Properties properties) throws SQLException {
        return createDataSource(properties, true);
    }

    /**
     * Create a database and return a DataSource
     * @param properties for the opening of the DataBase.
     * @param initSpatial true to load the spatial functions
     * @return a DataSource
     */
    public static DataSource createDataSource(Properties properties, boolean initSpatial) throws SQLException {
        // Create H2 memory DataSource
        org.h2.Driver driver = org.h2.Driver.load();
        OsgiDataSourceFactory dataSourceFactory = new OsgiDataSourceFactory(driver);
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
         * Create a database and return a DataSource
         * @param dbName database name
         * @param initSpatial true to init spatial functions
         * @param h2Parameters database parameters
         * @return a connection
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
     * @param dbName  path to the database
     * @param h2_PARAMETERS Additional h2 parameters
     * @return path to the database
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
     * @throws SQLException throws an exception if the spatial functions can not be loaded
     */
    public static Connection createSpatialDataBase(String dbName,boolean initSpatial, String h2Parameters )throws SQLException {
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
     * @throws SQLException throws an exception if the spatial functions can not be loaded
     */
    public static Connection createSpatialDataBase(String dbName, boolean initSpatial )throws SQLException {
        return createSpatialDataBase(dbName, initSpatial, H2_PARAMETERS);
    }
}
