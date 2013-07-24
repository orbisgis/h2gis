package org.orbisgis.sputilities.wrapper;

import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Nicolas Fortin
 */
public class DataSourceFactoryWrapper implements DataSourceFactory {
    private DataSourceFactory factory;

    public DataSourceFactoryWrapper(DataSourceFactory factory) {
        this.factory = factory;
    }

    @Override
    public DataSource createDataSource(Properties properties) throws SQLException {
        return factory.createDataSource(properties);
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties properties) throws SQLException {
        return factory.createConnectionPoolDataSource(properties);
    }

    @Override
    public XADataSource createXADataSource(Properties properties) throws SQLException {
        return factory.createXADataSource(properties);
    }

    @Override
    public Driver createDriver(Properties properties) throws SQLException {
        return factory.createDriver(properties);
    }
}
