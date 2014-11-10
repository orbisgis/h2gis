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
 *
 * @author Erwan Bocher
 */
public class WayOSMElement extends OSMElement{
    private final HashMap<Integer, Long> nodesRef;
    private int order = 0;
    
    public WayOSMElement(){
        super();
        nodesRef = new HashMap<Integer, Long>();
    }
    
    public void addRef(String ref){
        nodesRef.put(order++, Long.valueOf(ref));
    }

    public HashMap<Integer, Long> getNodesRef() {
        return nodesRef;
    }
    
    
}
