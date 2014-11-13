/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.h2gis.drivers.osm;

import java.util.HashMap;

/**
 * A class to manage the way element properties.
 * 
 * @author Erwan Bocher
 */
public class WayOSMElement extends OSMElement{
    private final HashMap<Integer, Long> nodesRef;
    private int order = 1;
    
    public WayOSMElement(){
        super();
        nodesRef = new HashMap<Integer, Long>();
    }
   
    /**
     * Add in a list the ref of the node used to describe the way.
     * @param ref 
     */
    public void addRef(String ref){
        nodesRef.put(order++, Long.valueOf(ref));
    }

    /**
     * Return the list of nodes 
     * @return 
     */
    public HashMap<Integer, Long> getNodesRef() {
        return nodesRef;
    }
    
    
}
