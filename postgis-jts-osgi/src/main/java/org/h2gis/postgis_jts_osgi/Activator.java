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
package org.h2gis.postgis_jts_osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

import java.util.Hashtable;

public class Activator implements BundleActivator
{
	public void start(BundleContext bundleContext) throws Exception
	{
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "PostgreSQL");
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, org.postgresql.Driver.getVersion());
		props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, org.postgresql.Driver.class.getName());
		serviceRegistration = bundleContext.registerService(DataSourceFactory.class.getName(), new DataSourceFactoryImpl(), props);
	}

	public void stop(BundleContext bundleContext) throws Exception
	{
		if (serviceRegistration != null)
			serviceRegistration.unregister();
	}

	private ServiceRegistration serviceRegistration;
}
