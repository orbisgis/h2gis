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

package org.h2gis.ext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.api.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Registers the SQL functions contained in h2spatial-functions and h2-network.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class H2GISExtensionFactory {  
   

     private static final Logger LOGGER = LoggerFactory.getLogger(H2GISExtensionFactory.class);
     
    /**
     * Init H2 DataBase with all H2GIS functions
     *
     * @param connection Active connection
     * @throws SQLException
     */
    public static void initH2GISExtension(Connection connection) throws SQLException {
        org.h2gis.functions.factory.H2GISFunctionsFactory.initH2GISFunctions(connection);
        // Register project's functions
        registerFunctions(connection);
    }
    
    
    /**
     * @return instance of all built-ins functions
     * @throws java.sql.SQLException
     */
    public static Function[] getBuiltInsFunctions() throws SQLException {
        return new Function[] {
            //H2-Network functions
                
               };
    }

    /**
     * Register built-in functions
     *
     * @param connection Active connection
     * @throws SQLException Error while creating statement
     */
    public static void registerFunctions(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for (Function function : getBuiltInsFunctions()) {
            try {
                org.h2gis.functions.factory.H2GISFunctionsFactory.registerFunction(st, function, "");
            } catch (SQLException ex) {
                // Catch to register other functions
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        }
    }
}
