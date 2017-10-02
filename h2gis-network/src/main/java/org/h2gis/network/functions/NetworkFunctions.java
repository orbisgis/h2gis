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

package org.h2gis.network.functions;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.api.Function;
import org.h2gis.functions.factory.H2GISFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to load the Network functions
 * 
 * @author Erwan Bocher
 */
public class NetworkFunctions {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkFunctions.class);

    
    /**
     * @return instance of all built-ins functions
     * @throws java.sql.SQLException
     */
    public static Function[] getBuiltInsFunctions() throws SQLException {
        return new Function[]{
            new ST_Accessibility(),
            new ST_ConnectedComponents(),
            new ST_GraphAnalysis(),
            new ST_ShortestPathLength(),
            new ST_ShortestPathTree(),
            new ST_ShortestPath()
                    
        };
    }
    
    /**
     * Init H2 DataBase with the network functions
     *
     * @param connection Active connection
     * @throws SQLException
     */
    public static void load(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for (Function function : getBuiltInsFunctions()) {
            try {
                H2GISFunctions.registerFunction(st, function, "");
            } catch (SQLException ex) {
                // Catch to register other functions
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

}
