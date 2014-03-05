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
    protected String parseWeight(String v) {
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
    protected String parseGlobalOrientation(String v) throws IllegalArgumentException {
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

    private boolean isDirectedString(String s) {
        if (s.toLowerCase().contains(DIRECTED)
                && !isUndirectedString(s)) {
            return true;
        }
        return false;
    }

    private boolean isReversedString(String s) {
        if (s.toLowerCase().contains(REVERSED)) {
            return true;
        }
        return false;
    }

    private boolean isUndirectedString(String s) {
        if (s.toLowerCase().contains(UNDIRECTED)) {
            return true;
        }
        return false;
    }

    protected String parseEdgeOrientation(String v) {
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
}

