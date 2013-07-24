package org.orbisgis.sputilities.osgi;

import org.orbisgis.sputilities.JDBCUrlParser;
import org.orbisgis.sputilities.URIUtility;
import org.orbisgis.sputilities.wrapper.DataSourceFactoryWrapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

/**
 * This tracker will register a new DataSourceFactoryWrapper for each registered DataSourceFactory service.
 * @author Nicolas Fortin
 */
public class DataSourceFactoryTracker implements ServiceTrackerCustomizer<DataSourceFactory,ServiceRegistration<DataSourceFactory>> {
    private BundleContext bundleContext;

    public DataSourceFactoryTracker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ServiceRegistration<DataSourceFactory> addingService(ServiceReference<DataSourceFactory> dataSourceFactoryServiceReference) {
        DataSourceFactory dsf = bundleContext.getService(dataSourceFactoryServiceReference);
        if(!(dsf instanceof DataSourceFactoryWrapper)) {
            // Wrap only if OSGI_JDBC_DRIVER_NAME property is set
            boolean customized = false;
            // Clone properties
            Dictionary<String,Object> properties = new Hashtable<String, Object>();
            for(String propKey : dataSourceFactoryServiceReference.getPropertyKeys()) {
                Object value = dataSourceFactoryServiceReference.getProperty(propKey);
                if(DataSourceFactory.OSGI_JDBC_DRIVER_NAME.equals(propKey) && value!=null) {
                    value = value.toString()+ JDBCUrlParser.SPATIAL_DATASOURCE_ENDSWITH;
                    customized = true;
                }
                properties.put(propKey,value);
            }
            if(customized) {
                return bundleContext.registerService(DataSourceFactory.class,new DataSourceFactoryWrapper(dsf),properties);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<DataSourceFactory> dataSourceFactoryServiceReference, ServiceRegistration<DataSourceFactory> dataSourceFactoryWrapper) {
        // Do not track DataSourceFactory properties
    }

    @Override
    public void removedService(ServiceReference<DataSourceFactory> dataSourceFactoryServiceReference, ServiceRegistration<DataSourceFactory> dataSourceFactoryWrapper) {
        dataSourceFactoryWrapper.unregister();
    }
}
