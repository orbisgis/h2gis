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
package org.h2gis.utilities.dbtypes;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database utilities class.
 *
 * @author Erwan Bocher (CNRS 2021)
 * @author Sylvain Palominos (UBS Chaire GEOTERA 2021)
 */
public class DBUtils {
    public static DBTypes getDBType(String url) {
        return getDBType(URI.create(url));
    }

    public static DBTypes getDBType(URI uri) {
        String scheme = uri.getScheme();
        if(scheme.equals("jdbc")) {
            String ssp = uri.getSchemeSpecificPart();
            scheme = ssp.substring(0, ssp.indexOf(":"));
        }
        if(scheme.startsWith("jdbc:")) {
            scheme = scheme.substring(5);
        }
        return Constants.schemeDBTypeMap.get(scheme.toLowerCase());
    }

    public static DBTypes getDBType(Connection connection) {
        return Constants.driverDBTypeMap.get(connection.getClass().getName());
    }
}
