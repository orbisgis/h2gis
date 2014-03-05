package org.h2gis.network.graph_creator;

/**
 * Created by adam on 3/4/14.
 */
public class GraphFunctionParser {

    public static final String SEPARATOR = "-";
    public static final String DIRECTED = "directed";
    public static final String REVERSED = "reversed";
    public static final String UNDIRECTED = "undirected";

    public static final String EDGE_ORIENTATION_COLUMN = "edge_orientation_column";
    public static final String POSSIBLE_ORIENTATIONS =
            "'" + DIRECTED + " - " + EDGE_ORIENTATION_COLUMN + "' "
                    + "| '" + REVERSED + " - " + EDGE_ORIENTATION_COLUMN + "' "
                    + "| '" + UNDIRECTED + "'";
    public static final String ORIENTATION_ERROR =
            "Bad orientation format. Enter " + POSSIBLE_ORIENTATIONS + ".";

    /**
     * Recovers the weight column name from the given string.
     *
     * @param v String
     *
     * @return True if the weight column name was correctly parsed.
     */
    public static String parseWeight(String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }

    /**
     * Recovers the global (and edge) orientation(s) from the given string,
     * making sure the edge orientation column exists in the given data set.
     *
     * @param v     String
     * @return True if the orientations were correctly parsed.
     * @throws IllegalArgumentException
     */
    public static String parseGlobalOrientation(String v) throws IllegalArgumentException {
        if (v == null) {
            return null;
        }
        if (isDirectedString(v)) {
            return DIRECTED;
        } else if (isReversedString(v)) {
            return REVERSED;
        } else if (isUndirectedString(v)) {
            return UNDIRECTED;
        } else {
            throw new IllegalArgumentException(ORIENTATION_ERROR);
        }
    }

    public static boolean isDirectedString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().contains(DIRECTED)
                && !isUndirectedString(s)) {
            return true;
        }
        return false;
    }

    public static boolean isReversedString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().contains(REVERSED)) {
            return true;
        }
        return false;
    }

    public static boolean isUndirectedString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().contains(UNDIRECTED)) {
            return true;
        }
        return false;
    }

    public static boolean isOrientationString(String s) {
       return isDirectedString(s) || isReversedString(s) || isUndirectedString(s);
    }

    public static boolean isWeightString(String s) {
        if (s == null) {
            return false;
        }
        return !isOrientationString(s);
    }

    public static String parseEdgeOrientation(String v) {
        if (v == null) {
            return null;
        }
        if (!v.contains(SEPARATOR)) {
            throw new IllegalArgumentException(ORIENTATION_ERROR);
        }
        // Extract the global and edge orientations.
        String[] s = v.split(SEPARATOR);
        if (s.length == 2) {
            // Return just the edge orientation column name and not the
            // global orientation.
            return s[1].trim();
        } else {
            throw new IllegalArgumentException(ORIENTATION_ERROR);
        }
    }

    static void parseWeightAndOrientation(ST_ShortestPathLength function, String arg1, String arg2) {
        if (isWeightString(arg1)
                && isWeightString(arg2)) {
            throw new IllegalArgumentException("Cannot specify the weight column twice.");
        }
        if (isOrientationString(arg1)
                && isOrientationString(arg2)) {
            throw new IllegalArgumentException("Cannot specify the orientation twice.");
        }
        if (isWeightString(arg1)
                || isOrientationString(arg2)) {
            ST_ShortestPathLength.setWeightAndOrientation(function, arg1, arg2);
        }
        if (isWeightString(arg2)
                || isOrientationString(arg1)) {
            ST_ShortestPathLength.setWeightAndOrientation(function, arg2, arg1);
        }
    }
}

