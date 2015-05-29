/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatial.osgi;

import org.h2gis.utilities.JDBCUtilities;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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
                if(!JDBCUtilities.H2_DRIVER_NAME.equals(meta.getDriverName())
                        || (meta.getURL() != null && meta.getURL().toLowerCase().startsWith("jdbc:h2:tcp://"))) {
                    return null;
                }
                // Check if the database has been properly initialised by the DataSource service provider
                if(!JDBCUtilities.tableExists(connection, "PUBLIC.GEOMETRY_COLUMNS")) {
                    return null;
                }
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
