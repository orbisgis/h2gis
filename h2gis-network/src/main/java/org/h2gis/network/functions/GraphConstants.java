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
package org.h2gis.network.functions;

/**
 * Column names used by graph functions.
 *
 * @author Adam Gouge
 */
public interface GraphConstants {
    String EDGE_ID = "EDGE_ID";
    String START_NODE = "START_NODE";
    String END_NODE = "END_NODE";
    String NODE_ID = "NODE_ID";
    String THE_GEOM = "THE_GEOM";
    String SOURCE  = "SOURCE";
    String DESTINATION  = "DESTINATION";
    String CLOSEST_DEST  = "CLOSEST_DEST";
    String DISTANCE  = "DISTANCE";
    String BETWEENNESS  = "BETWEENNESS";
    String CLOSENESS  = "CLOSENESS";
    String NODE_CENT_SUFFIX = "_NODE_CENT";
    String EDGE_CENT_SUFFIX = "_EDGE_CENT";
    String CONNECTED_COMPONENT = "CONNECTED_COMPONENT";
    String NODE_COMP_SUFFIX = "_NODE_CC";
    String EDGE_COMP_SUFFIX = "_EDGE_CC";
    String PATH_ID = "PATH_ID";
    String PATH_EDGE_ID = "PATH_EDGE_ID";
    String TREE_ID = "TREE_ID";
    String WEIGHT = "WEIGHT";
}
