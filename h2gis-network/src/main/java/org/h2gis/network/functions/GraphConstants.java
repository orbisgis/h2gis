/**
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
package org.h2gis.network.functions;

/**
 * Column names used by graph functions.
 *
 * @author Adam Gouge
 */
public interface GraphConstants {
    public static final String EDGE_ID = "EDGE_ID";
    public static final String START_NODE = "START_NODE";
    public static final String END_NODE = "END_NODE";
    public static final String NODE_ID = "NODE_ID";
    public static final String THE_GEOM = "THE_GEOM";
    public static final String SOURCE  = "SOURCE";
    public static final String DESTINATION  = "DESTINATION";
    public static final String CLOSEST_DEST  = "CLOSEST_DEST";
    public static final String DISTANCE  = "DISTANCE";
    public static final String BETWEENNESS  = "BETWEENNESS";
    public static final String CLOSENESS  = "CLOSENESS";
    public static final String NODE_CENT_SUFFIX = "_NODE_CENT";
    public static final String EDGE_CENT_SUFFIX = "_EDGE_CENT";
    public static final String CONNECTED_COMPONENT = "CONNECTED_COMPONENT";
    public static final String NODE_COMP_SUFFIX = "_NODE_CC";
    public static final String EDGE_COMP_SUFFIX = "_EDGE_CC";
    public static final String PATH_ID = "PATH_ID";
    public static final String PATH_EDGE_ID = "PATH_EDGE_ID";
    public static final String TREE_ID = "TREE_ID";
    public static final String WEIGHT = "WEIGHT";
}
