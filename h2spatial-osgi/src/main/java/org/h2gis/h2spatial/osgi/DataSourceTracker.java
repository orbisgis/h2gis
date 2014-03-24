/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatial.osgi;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatialapi.Function;
import org.h2gis.utilities.JDBCUrlParser;
import org.h2gis.utilities.JDBCUtilities;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

/**
 * When a new data source is registered this tracker add spatial features to the linked database.
 * @author Nicolas Fortin
 */
public class DataSourceTracker implements ServiceTrackerCustomizer<DataSource,FunctionTracker> {
    private BundleContext bundleContext;
    /**
     * Constructor
     * @param bundleContext BundleContext instance
     */
    public DataSourceTracker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public FunctionTracker addingService(ServiceReference<DataSource> dataSourceServiceReference) {
        DataSource dataSource = bundleContext.getService(dataSourceServiceReference);
        try {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData meta = connection.getMetaData();
                // If not H2 or in client mode, does not register H2 spatial functions.
                Properties properties = JDBCUrlParser.parse(meta.getURL());
                if(!JDBCUtilities.H2_DRIVER_NAME.equals(meta.getDriverName())
                        || "tcp".equalsIgnoreCase(properties.getProperty(DataSourceFactory.JDBC_NETWORK_PROTOCOL))) {
                    return null;
                }
                // Register built-ins functions
                for(Function function : CreateSpatialExtension.getBuiltInsFunctions()) {
                    CreateSpatialExtension.registerFunction(connection.createStatement(),function,"",false);
                }
                CreateSpatialExtension.registerGeometryType(connection);
                CreateSpatialExtension.registerSpatialTables(connection);
            } finally {
                connection.close();
            }
        } catch (SQLException ex) {
            System.err.print(ex.toString());
        }
        try {
            FunctionTracker functionTracker = new FunctionTracker(dataSource,bundleContext);
            functionTracker.open();
            return functionTracker;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void modifiedService(ServiceReference<DataSource> dataSourceServiceReference, FunctionTracker functionTracker) {

    }

    @Override
    public void removedService(ServiceReference<DataSource> dataSourceServiceReference, FunctionTracker functionTracker) {
        if(functionTracker!=null) {
            functionTracker.close();
        }
    }
}
