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

package org.h2gis.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

/**
 * Convert JDBC URL into JDBC Connection properties.
 * Currently work with H2 and PostGreSQL DataSourceFactory
 * @author Nicolas Fortin
 */
public class JDBCUrlParser {
    private JDBCUrlParser() {}
    private static final String URL_STARTS = "jdbc:";
    /** If the property value of DataSourceFactory#OSGI_JDBC_DRIVER_NAME ends with SPATIAL_DATASOURCE_ENDSWITH
     *  then it is the wrapped spatial version of a DataSourceFactory  */
    public static final String SPATIAL_DATASOURCE_ENDSWITH = "_spatial";

    /**
     * Convert JDBC URL into JDBC Connection properties
     * @param jdbcUrl JDBC connection path
     * @return Properties to be used in OSGi DataSourceFactory that does not handle DataSourceFactory#JDBC_URL
     * @throws IllegalArgumentException Argument is not a valid JDBC connection URL or cannot be parsed by this class
     */
    public static Properties parse(String jdbcUrl) throws IllegalArgumentException {
        if(!jdbcUrl.startsWith(URL_STARTS)) {
            throw new IllegalArgumentException("JDBC Url must start with "+URL_STARTS);
        }
        String driverAndURI = jdbcUrl.substring(URL_STARTS.length());    
        Properties properties = new Properties();   
        URI uri = URI.create(driverAndURI.substring(driverAndURI.indexOf(':')+1));
        if(uri.getHost()!=null) {
            properties.setProperty("serverName",uri.getHost());
        }
        // Read DataBase name/path and options
        String path = uri.getPath();
        if(path!=null) {
            String[] paths = path.split(";");
            if(uri.getHost()!=null && paths[0].startsWith("/")) {
                paths[0] = paths[0].substring(1);
            }
            properties.setProperty("databaseName",paths[0]);
            for(int id=1;id<paths.length;id++) {
                String[] option = paths[id].split("=");
                if(option.length==2) {
                    properties.setProperty(option[0],option[1]);
                }
            }
        }
        String query = uri.getQuery();
        if(query!=null) {
            try {
                for(Map.Entry<String,String> entry : URIUtilities.getQueryKeyValuePairs(uri).entrySet()) {
                    properties.setProperty(entry.getKey(),entry.getValue());
                }
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalArgumentException("JDBC Url encoding error",ex);

            }
        }
        if(uri.getPort()!=-1) {
            properties.setProperty("portNumber",String.valueOf(uri.getPort()));
        }
        if(uri.getScheme()!=null && !"file".equalsIgnoreCase(uri.getScheme())) {
            properties.setProperty("networkProtocol", uri.getScheme());
        }
        return properties;
    }

}
