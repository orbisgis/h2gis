package org.h2gis.h2spatialext.osgi;

import org.h2gis.drivers.csv.CSVDriverFunction;
import org.h2gis.drivers.dbf.DBFDriverFunction;
import org.h2gis.drivers.geojson.GeoJsonDriverFunction;
import org.h2gis.drivers.gpx.GPXDriverFunction;
import org.h2gis.drivers.kml.KMLDriverFunction;
import org.h2gis.drivers.shp.SHPDriverFunction;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.Function;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.h2gis.drivers.osm.OSMDriverFunction;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Registers services provided by this plugin bundle.
 */
public class Activator implements BundleActivator {
        /**
         * Starting bundle, register services.
         * @param bc
         * @throws Exception
         */
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
                bc.registerService(DriverFunction.class, new DBFDriverFunction(), null);
                bc.registerService(DriverFunction.class, new SHPDriverFunction(), null);
                bc.registerService(DriverFunction.class, new GPXDriverFunction(), null);
                bc.registerService(DriverFunction.class, new GeoJsonDriverFunction(), null);
                bc.registerService(DriverFunction.class, new OSMDriverFunction(), null);
                bc.registerService(DriverFunction.class, new KMLDriverFunction(), null);
                bc.registerService(DriverFunction.class, new CSVDriverFunction(), null);
                
        }

        /**
         * Called before the bundle is unloaded.
         * @param bc
         * @throws Exception
         */
        @Override
        public void stop(BundleContext bc) throws Exception {

        }
}
