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
package org.h2gis.postgis_jts;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Driver that provide directly the JTS Object when calling {@link java.sql.ResultSet#getObject(int)}
 * To create linked table on H2 database to PostGIS db:
 * CREATE LINKED TABLE mytable('org.orbisgis.postgis_jts.Driver',
 * 'jdbc:postgresql_h2://serverdomain:5432/databasename', 'user', 'password', '(select * from mytable)');
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class Driver extends JtsWrapper {

    private static final String POSTGIS_PROTOCOL = "jdbc:postgres_jts:";
    private static final String POSTGIS_H2PROTOCOL = "jdbc:postgresql_h2:";

    static {
        try {
            // Try to register ourself to the DriverManager
            java.sql.DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "PostGIS H2 compatible Driver", e);
        }
    }

    /**
     * Default constructor.
     */
    public Driver() {
        super();
    }

    /**
     * Mangles the PostGIS URL to return the original PostGreSQL URL.
     *
     * @param url
     * @return Mangled PostGIS URL
     */
    public static String mangleURL(String url) throws SQLException {
        if (url.startsWith(POSTGIS_H2PROTOCOL)) {
            return POSTGIS_PROTOCOL + url.substring(POSTGIS_H2PROTOCOL.length());
        } else {
            throw new SQLException("Unknown protocol or subprotocol in url " + url);
        }
    }

    /**
     * Check whether the driver thinks he can handle the given URL.
     *
     * @see java.sql.Driver#acceptsURL
     *
     * @param url the URL of the driver.
     *
     * @return true if this driver accepts the given URL.
     */
    @Override
    public boolean acceptsURL(String url) {
        try {
            url = mangleURL(url);
        } catch (SQLException e) {
            return false;
        }
        return super.acceptsURL(url);
    }

    /**
     * Returns our own CVS version plus postgres Version.
     *
     * @return Driver version.
     */
    public static String getVersion() {
        return "H2 compatible driver, wrapping pg " + org.postgresql.Driver.getVersion();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return new ConnectionWrapper(super.connect(POSTGIS_PROTOCOL + url.substring(POSTGIS_H2PROTOCOL.length()), info));
    }
}
