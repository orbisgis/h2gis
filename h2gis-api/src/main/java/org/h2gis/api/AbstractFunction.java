/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of the Function interface which is able to handle properties into a map.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public abstract class AbstractFunction implements Function {
    private Map<String, Object> properties = new HashMap<>();

    @Override
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Add a property to the map.
     *
     * @param propertyName Property identifier.
     * @param value        New property value.
     */
    public void addProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
    }

    /**
     * Remove a property from the map.
     *
     * @param propertyName Property identifier.
     * @return True if the property is removed.
     */
    public boolean removeProperty(String propertyName) {
        return properties.remove(propertyName) != null;
    }
}
