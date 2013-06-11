package org.h2gis.h2spatialext;

import org.h2gis.h2spatialext.function.spatial.table.ST_Explode;
import org.h2gis.h2spatialapi.Function;
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
                bc.registerService(Function.class,
                        new ST_Explode(),
                        null);
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
