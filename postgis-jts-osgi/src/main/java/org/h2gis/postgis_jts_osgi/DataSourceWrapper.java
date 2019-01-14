/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.postgis_jts_osgi;

import org.h2gis.postgis_jts.ConnectionWrapper;
import org.h2gis.postgis_jts.JtsGeometry;
import org.postgis.PGbox2d;
import org.postgis.PGbox3d;
import org.postgresql.PGConnection;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Configure Postgre connection to use PostGIS.
 * @author Nicolas Fortin
 */
public class DataSourceWrapper implements DataSource {
    private DataSource pgDataSource;

    /**
     * Constructor.
     * @param pgDataSource Instance of Postgre datasource
     */
    public DataSourceWrapper(DataSource pgDataSource) {
        this.pgDataSource = pgDataSource;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        pgDataSource.setLoginTimeout(seconds);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return configureConnection(pgDataSource.getConnection());
    }

    private Connection configureConnection(Connection connection) throws SQLException {
        if(connection instanceof PGConnection) {
            ((PGConnection) connection).addDataType("geometry", JtsGeometry.class);
            ((PGConnection) connection).addDataType("box3d", PGbox3d.class);
            ((PGConnection) connection).addDataType("box2d", PGbox2d.class);
        }
        return new ConnectionWrapper(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return configureConnection(pgDataSource.getConnection(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return pgDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        pgDataSource.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return pgDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return pgDataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Unsupported operation");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
