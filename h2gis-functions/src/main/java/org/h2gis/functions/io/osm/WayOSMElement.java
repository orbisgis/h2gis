/**
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

package org.h2gis.functions.io.osm;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to manage the way element properties.
 *
 * @author Erwan Bocher
 */
public class WayOSMElement extends OSMElement {

    private final List<Long> nodesRef = new ArrayList<Long>();

    public WayOSMElement() {
        super();
    }

    /**
     * Add in a list the ref of the node used to describe the way.
     *
     * @param ref
     */
    public void addRef(String ref) {
        if(ref!=null) {
            nodesRef.add(Long.valueOf(ref));
        }
    }

    /**
     * Return the list of nodes
     *
     * @return
     */
    public List<Long> getNodesRef() {
        return nodesRef;
    }

}
