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
package org.h2gis.utilities;

import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cts.util.UTMUtils;

/**
 * Generic utilities function to retrieve spatial metadata trough SFS
 * specification. Compatible with H2 and PostGIS.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class SFSUtilities {

      

    /**
     * In order to be able to use {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get
     * {@link SpatialResultSet} and {@link SpatialResultSetMetaData} this method
     * wrap the provided dataSource.
     *
     * @param dataSource H2 or PostGIS DataSource
     *
     * @return Wrapped DataSource, with spatial methods
     */
    public static DataSource wrapSpatialDataSource(DataSource dataSource) {
        try {
            if (dataSource.isWrapperFor(DataSourceWrapper.class)) {
                return dataSource;
            } else {
                return new DataSourceWrapper(dataSource);
            }
        } catch (SQLException ex) {
            return new DataSourceWrapper(dataSource);
        }
    }

    /**
     * Use this only if DataSource is not available. In order to be able to use
     * {@link ResultSet#unwrap(Class)} and
     * {@link java.sql.ResultSetMetaData#unwrap(Class)} to get
     * {@link SpatialResultSet} and {@link SpatialResultSetMetaData} this method
     * wrap the provided connection.
     *
     * @param connection H2 or PostGIS Connection
     *
     * @return Wrapped DataSource, with spatial methods
     */
    public static Connection wrapConnection(Connection connection) {
        try {
            if (connection.isWrapperFor(ConnectionWrapper.class)) {
                return connection;
            } else {
                return new ConnectionWrapper(connection);
            }
        } catch (SQLException ex) {
            return new ConnectionWrapper(connection);
        }
    }

    /**
     * Return a SRID code from latitude and longitude coordinates
     *
     * @param connection to the database
     * @param latitude
     * @param longitude
     * @return a SRID code
     * @throws SQLException
     */
    public static int getSRID(Connection connection, float latitude, float longitude)
            throws SQLException {
        int srid = -1;
        PreparedStatement ps = connection.prepareStatement("select SRID from PUBLIC.SPATIAL_REF_SYS where PROJ4TEXT = ?");
        ps.setString(1, UTMUtils.getProj(latitude, longitude));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                srid = rs.getInt(1);
            }
        } finally {
            ps.close();
        }
        return srid;
    }
}
