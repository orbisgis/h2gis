package org.h2gis.h2spatialext.osgi;

import org.h2gis.h2spatialapi.Function;
import org.h2gis.h2spatialext.CreateSpatialExtension;
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
                }
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
