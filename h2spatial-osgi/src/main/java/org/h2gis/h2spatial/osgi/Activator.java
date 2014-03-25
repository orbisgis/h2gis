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

import org.osgi.framework.Bundle;
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
    private Bundle bundle;
    //private PermanentFunctionClassLoader classLoader;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundle = bundleContext.getBundle();
        //classLoader =new PermanentFunctionClassLoader();
        //Utils.addClassFactory(classLoader);
        DataSourceTracker dataSourceTracker = new DataSourceTracker(bundleContext);
        databaseTracker = new ServiceTracker<DataSource, FunctionTracker>(bundleContext,DataSource.class,dataSourceTracker);
        databaseTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        databaseTracker.close();
        //Utils.removeClassFactory(classLoader);
    }

    /**
     * Use this bundle class loader instead of H2 one.
     */
    /*
    private class PermanentFunctionClassLoader implements Utils.ClassFactory
    {
        @Override
        public boolean match(String name) {
            return name.startsWith(DataSourceTracker.PREFIX);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return bundle.loadClass(name.substring(DataSourceTracker.PREFIX.length()));
        }
    }
    */
}
