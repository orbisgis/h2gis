package org.h2gis.utilities;

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
    public static final String NODE_GEOM = "THE_GEOM";
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
}
