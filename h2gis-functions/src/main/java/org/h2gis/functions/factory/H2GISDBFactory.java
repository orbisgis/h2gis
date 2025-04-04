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

package org.h2gis.functions.factory;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Used to create quickly a database.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher
 * @author Sylvain Palominos
 */
public class H2GISDBFactory {

    public static final String H2_PARAMETERS = ";DB_CLOSE_ON_EXIT=FALSE";

    public static final String JDBC_URL = "url";
    public static final String JDBC_USER = "user";
    public static final String JDBC_PASSWORD = "password";
    public static final String START_URL = "jdbc:h2:";
    public static final String JDBC_DATABASE_NAME = "databaseName";
    public static final String JDBC_NETWORK_PROTOCOL = "networkProtocol";
    public static final String JDBC_PORT_NUMBER = "portNumber";
    public static final String JDBC_SERVER_NAME = "serverName";

    public static final String DEFAULT_USER = "sa";
    public static final String DEFAULT_PASSWORD = "sa";

    private H2GISDBFactory() {
        // utility
    }

    /**
     * Open the connection to an existing database
     * @param dbName Database name
     * @return Active connection
     * @throws SQLException Exception
     */
    public static Connection openSpatialDataBase(String dbName) throws SQLException {
        String dbFilePath = getDataBasePath(dbName);       
        String databasePath = "jdbc:h2:"+ dbFilePath + H2_PARAMETERS;
        org.h2.Driver.load();
        // Keep a connection alive to not close the DataBase on each unit test
       return DriverManager.getConnection(databasePath,
                DEFAULT_USER, DEFAULT_PASSWORD);
    }
    /**
     * Create a spatial database
     * @param dbName filename
     * @return Connection
     * @throws java.sql.SQLException SQL Exception
     */
    public static Connection createSpatialDataBase(String dbName)throws SQLException {
        return createSpatialDataBase(dbName,true);
    }

    /**
     * Return the path of the file database
     * @param dbName database path
     * @return TODO : invalid path to be moved in test
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
     * @throws SQLException SQL issue
     */
    public static DataSource createDataSource(String dbName ,boolean initSpatial) throws SQLException {
        return createDataSource(dbName, initSpatial, H2_PARAMETERS);
    }

     /**
     * Create a database, init spatial funcyion and return a DataSource
     * @param properties for the opening of the DataBase.
     * @return a DataSource
     * @throws SQLException  SQL issue
     */
    public static DataSource createDataSource(Properties properties) throws SQLException {
        return createDataSource(properties, true);
    }

    /**
     * Create a database and return a DataSource
     * @param properties for the opening of the DataBase.
     * @param initSpatial true to load the spatial functions
     * @return a DataSource
     * @throws SQLException  SQL issue
     */
    public static DataSource createDataSource(Properties properties, boolean initSpatial) throws SQLException {
        // Create H2 memory DataSource
        JdbcDataSource dataSource = new JdbcDataSource();
        setupH2DataSource(dataSource, properties);
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
         * @param dbName Database name
         * @param initSpatial true to load the spatial functions
         * @param h2Parameters Additional h2 parameters
         * @return DataSource instance
         * @throws SQLException SQL issue
         */
    public static DataSource createDataSource(String dbName ,boolean initSpatial, String h2Parameters) throws SQLException {
        // Create H2 memory DataSource
        Properties properties = new Properties();
        String databasePath = initDBFile(dbName, h2Parameters);
        properties.setProperty(JDBC_URL, databasePath);
        properties.setProperty(JDBC_USER, DEFAULT_USER);
        properties.setProperty(JDBC_PASSWORD, DEFAULT_PASSWORD);
        JdbcDataSource dataSource = new JdbcDataSource();
        setupH2DataSource(dataSource, properties);
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
     * @param dbName Database name
     * @param h2_PARAMETERS User defined h2 parameters
     * @return The path of the database
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
     * @throws java.sql.SQLException SQL Exception
     */
    public static Connection createSpatialDataBase(String dbName,boolean initSpatial, String h2Parameters ) throws SQLException {
        String databasePath = initDBFile(dbName, h2Parameters);
        org.h2.Driver.load();
        // Keep a connection alive to not close the DataBase on each unit test
        Connection connection = DriverManager.getConnection(databasePath,
                DEFAULT_USER, DEFAULT_PASSWORD);
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
     * @throws java.sql.SQLException Database issue
     */
    public static Connection createSpatialDataBase(String dbName, boolean initSpatial )throws SQLException {
        return createSpatialDataBase(dbName, initSpatial, H2_PARAMETERS);
    }

    /**
     * Function greatly inspired by H2 org.h2.util.OsgiDataSourceFactory#setupH2DataSource(JdbcDataSource, Properties)
     * @param dataSource Datasource to set up.
     * @param props Properties to apply.
     */
    private static void setupH2DataSource(JdbcDataSource dataSource, Properties props) {
        if (props.containsKey(JDBC_USER)) {
            dataSource.setUser(props.getProperty(JDBC_USER));
        }
        if (props.containsKey(JDBC_PASSWORD)) {
            dataSource.setPassword(props.getProperty(JDBC_PASSWORD));
        }
        if (props.containsKey(JDBC_URL)) {
            dataSource.setURL(props.getProperty(JDBC_URL));
        } else {
            StringBuilder connectionUrl = new StringBuilder();
            connectionUrl.append(START_URL);
            // Set network protocol (tcp/ssl) or DB type (mem/file)
            String protocol = "";
            if (props.containsKey(JDBC_NETWORK_PROTOCOL)) {
                protocol = props.getProperty(JDBC_NETWORK_PROTOCOL);
                connectionUrl.append(protocol).append(":");
            }
            // Host name and/or port
            if (props.containsKey(JDBC_SERVER_NAME)) {
                connectionUrl.append("//").append(props.getProperty(JDBC_SERVER_NAME));
                if (props.containsKey(JDBC_PORT_NUMBER)) {
                    connectionUrl.append(":").append(props.getProperty(JDBC_PORT_NUMBER));
                }
                connectionUrl.append("/");
            } else if (props.containsKey(
                    JDBC_PORT_NUMBER)) {
                // Assume local host if only port was set
                connectionUrl
                        .append("//localhost:")
                        .append(props.getProperty(JDBC_PORT_NUMBER))
                        .append("/");
            } else if (protocol.equals("tcp") || protocol.equals("ssl")) {
                // Assume local host if network protocol is set, but no host or
                // port is set
                connectionUrl.append("//localhost/");
            }

            // DB path and name
            if (props.containsKey(JDBC_DATABASE_NAME)) {
                connectionUrl.append(props.getProperty(JDBC_DATABASE_NAME));
            }
            dataSource.setURL(connectionUrl.toString());
        }
    }
}
