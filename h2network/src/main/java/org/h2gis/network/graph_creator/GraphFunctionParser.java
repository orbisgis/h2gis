package org.h2gis.network.graph_creator;

/**
 * Created by adam on 3/4/14.
 */
public class GraphFunctionParser {

    private String weightColumn;
    private String globalOrientation;
    private String edgeOrientation;

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

    protected boolean isDirectedString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().contains(DIRECTED)
                && !isUndirectedString(s)) {
            return true;
        }
        return false;
    }

    protected boolean isReversedString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().contains(REVERSED)) {
            return true;
        }
        return false;
    }

    protected boolean isUndirectedString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().contains(UNDIRECTED)) {
            return true;
        }
        return false;
    }

    protected boolean isOrientationString(String s) {
       return isDirectedString(s) || isReversedString(s) || isUndirectedString(s);
    }

    protected boolean isWeightString(String s) {
        if (s == null) {
            return false;
        }
        return !isOrientationString(s);
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

    public void parseWeightAndOrientation(String arg1, String arg2) {
        if ((arg1 == null && arg2 == null)
                || (isWeightString(arg1) && arg2 == null)
                || (arg1 == null && isWeightString(arg2))) {
            // Disable default orientations (D and WD).
            throw new IllegalArgumentException("You must specify the orientation.");
        }
        if (isWeightString(arg1) && isWeightString(arg2)) {
            throw new IllegalArgumentException("Cannot specify the weight column twice.");
        }
        if (isOrientationString(arg1) && isOrientationString(arg2)) {
            throw new IllegalArgumentException("Cannot specify the orientation twice.");
        }
        if (isWeightString(arg1) || isOrientationString(arg2)) {
            setWeightAndOrientation(arg1, arg2);
        }
        if (isOrientationString(arg1) || isWeightString(arg2)) {
            setWeightAndOrientation(arg2, arg1);
        }
    }

    private void setWeightAndOrientation(String weight, String orient) {
        weightColumn = parseWeight(weight);
        globalOrientation = parseGlobalOrientation(orient);
        if (globalOrientation != null) {
            if (!globalOrientation.equals(UNDIRECTED)) {
                edgeOrientation = parseEdgeOrientation(orient);
            }
        }
    }

    public String getWeightColumn() {
        return weightColumn;
    }

    public String getGlobalOrientation() {
        return globalOrientation;
    }

    public String getEdgeOrientation() {
        return edgeOrientation;
    }
}
