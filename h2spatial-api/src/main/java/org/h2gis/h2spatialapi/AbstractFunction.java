package org.h2gis.h2spatialapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Function that handle properties in a map
 * @author Nicolas Fortin
 */
public abstract class AbstractFunction implements Function {
    private Map<String,Object> properties = new HashMap<String,Object>();

    @Override
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * @param propertyName Property identifier
     * @param value New property value
     */
    public void addProperty(String propertyName, Object value) {
        properties.put(propertyName,value);
    }

    /**
     * @param propertyName Property identifier
     * @return True if the property is removed
     */
    public boolean removeProperty(String propertyName) {
        return properties.remove(propertyName)!=null;
    }
}
