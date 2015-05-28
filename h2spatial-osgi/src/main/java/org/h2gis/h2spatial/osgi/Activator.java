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

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.Function;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import javax.sql.DataSource;

/**
 * Publish H2Spatial service on OSGi. Track for DataSource service, register spatial features into it.
 * @author Nicolas Fortin
 */
public class Activator implements BundleActivator {
    private ServiceTracker<DataSource,FunctionTracker> databaseTracker;
    //private PermanentFunctionClassLoader classLoader;

    @Override
    public void start(BundleContext bc) throws Exception {
        for(Function function : CreateSpatialExtension.getBuiltInsFunctions()) {
            bc.registerService(Function.class,
                    function,
                    null);
            if(function instanceof DriverFunction) {
                bc.registerService(DriverFunction.class,
                        (DriverFunction) function,
                        null);
            }
        }
        DataSourceTracker dataSourceTracker = new DataSourceTracker(bc);
        databaseTracker = new ServiceTracker<DataSource, FunctionTracker>(bc,DataSource.class,dataSourceTracker);
        databaseTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        databaseTracker.close();
    }
}
