/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2spatial.osgi;

import org.h2.engine.Constants;
import org.h2.util.OsgiDataSourceFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Publish H2Spatial service on OSGi
 * @author Nicolas Fortin
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Driver driver = Driver.loadSpatial();
        Dictionary<String,String> properties = new Hashtable<String, String>();
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, Driver.class.getName());
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "H2Spatial");
        properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, Constants.getVersion());
        bundleContext.registerService(DataSourceFactory.class, new OsgiDataSourceFactory(driver), properties);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Driver.unloadSpatial();
    }
}
