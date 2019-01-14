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

import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.ds.PGPoolingDataSource;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class DataSourceFactoryImpl implements DataSourceFactory {
    // org.postgresql.ds.jdbc23.AbstractJdbc23PoolingDataSource hold a static container of DataSource instance.
    // JDBC_DATASOURCE_NAME should be unique on each call of CreateDataSource with different parameters
    private static AtomicInteger dataSourceCount = new AtomicInteger(0);

    @Override
    public DataSource createDataSource(Properties properties) throws SQLException {
        if (properties == null) {
            properties = new Properties();
        }
        if(properties.getProperty(JDBC_DATASOURCE_NAME) == null) {
            properties.setProperty(JDBC_DATASOURCE_NAME, DataSourceFactoryImpl.class.getSimpleName() + "_" +
                    dataSourceCount.getAndAdd(1));
        }
        PGPoolingDataSource dataSource = PGPoolingDataSource.getDataSource(properties.getProperty(JDBC_DATASOURCE_NAME));
        if(dataSource == null) {
            dataSource = new PGPoolingDataSource();
            // Set dataSourceName, databaseName, user, and password
            dataSource.setDataSourceName(properties.getProperty(JDBC_DATASOURCE_NAME));
            String url = properties.getProperty(JDBC_URL);
            if(url != null) {
                dataSource.setUrl(url);
            }
            dataSource.setPortNumber(Integer.valueOf(properties.getProperty(JDBC_PORT_NUMBER, Integer.toString(dataSource.getPortNumber()))));
            dataSource.setServerName(properties.getProperty(JDBC_SERVER_NAME, dataSource.getServerName()));
            dataSource.setUser(properties.getProperty(JDBC_USER, dataSource.getUser()));
            dataSource.setPassword(properties.getProperty(JDBC_PASSWORD, dataSource.getPassword()));
            dataSource.setDatabaseName(properties.getProperty(JDBC_DATABASE_NAME, dataSource.getDatabaseName()));
        }
        return new DataSourceWrapper(dataSource);
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties properties) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public XADataSource createXADataSource(Properties properties) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Driver createDriver(Properties properties) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
