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

package org.h2gis.functions.osgi;


import org.h2gis.api.DriverFunction;
import org.h2gis.api.Function;
import org.h2gis.functions.io.csv.CSVDriverFunction;
import org.h2gis.functions.io.dbf.DBFDriverFunction;
import org.h2gis.functions.io.geojson.GeoJsonDriverFunction;
import org.h2gis.functions.io.gpx.GPXDriverFunction;
import org.h2gis.functions.io.kml.KMLDriverFunction;
import org.h2gis.functions.io.osm.OSMDriverFunction;
import org.h2gis.functions.io.shp.SHPDriverFunction;
import org.h2gis.functions.io.tsv.TSVDriverFunction;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import javax.sql.DataSource;
import org.h2gis.functions.factory.H2GISFunctions;

/**
 * Publish H2GIS service on OSGi. Track for DataSource service, register spatial features into it.
 * @author Nicolas Fortin
 */
public class Activator implements BundleActivator {
    private ServiceTracker<DataSource,FunctionTracker> databaseTracker;

    @Override
    public void start(BundleContext bc) {
        for(Function function : H2GISFunctions.getBuiltInsFunctions()) {
            bc.registerService(Function.class, function, null);
            if(function instanceof DriverFunction) {
                bc.registerService(DriverFunction.class, (DriverFunction) function, null);
            }
        }

        //Driver registering
        bc.registerService(DriverFunction.class, new DBFDriverFunction(), null);
        bc.registerService(DriverFunction.class, new SHPDriverFunction(), null);
        bc.registerService(DriverFunction.class, new GPXDriverFunction(), null);
        bc.registerService(DriverFunction.class, new GeoJsonDriverFunction(), null);
        bc.registerService(DriverFunction.class, new OSMDriverFunction(), null);
        bc.registerService(DriverFunction.class, new KMLDriverFunction(), null);
        bc.registerService(DriverFunction.class, new CSVDriverFunction(), null);
        bc.registerService(DriverFunction.class, new TSVDriverFunction(), null);

        DataSourceTracker dataSourceTracker = new DataSourceTracker(bc);
        databaseTracker = new ServiceTracker<>(bc,DataSource.class,dataSourceTracker);
        databaseTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) {
        databaseTracker.close();
    }
}
