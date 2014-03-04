package org.h2gis.network.graph_creator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adam on 3/4/14.
 */
public class GraphFunctionParser {

    private final Logger LOGGER = LoggerFactory.getLogger(GraphFunctionParser.class);

    public static final String SEPARATOR = "-";
    public static final String DIRECTED = "directed";
    public static final String REVERSED = "reversed";
    public static final String UNDIRECTED = "undirected";
    public static final String EDGE_ORIENTATION_COLUMN = "edge_orientation_column";
    public static final String POSSIBLE_ORIENTATIONS =
            "'" + DIRECTED + " - " + EDGE_ORIENTATION_COLUMN + "' "
                    + "| '" + REVERSED + " - " + EDGE_ORIENTATION_COLUMN + "' "
                    + "| '" + UNDIRECTED + "'";

    private String globalOrientation;
    private String edgeOrientationColumnName;

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
        String name = v.trim();
        checkForIllegalCharacters(name);
        LOGGER.info("Weights column name = '{}'.", name);
        return name;
    }

    /**
     * Makes sure the given string contains only letters, numbers and
     * underscores.
     *
     * @param v String
     */
    private void checkForIllegalCharacters(String v) {
        Matcher m = Pattern.compile("[^_0-9A-Za-z]").matcher(v);
        String illegalCharacters = "";
        while (m.find()) {
            illegalCharacters += "\"" + m.group() + "\", ";
        }
        if (!illegalCharacters.isEmpty()) {
            throw new IllegalArgumentException("Illegal character: " + illegalCharacters);
        }
    }

    /**
     * Recovers the global (and edge) orientation(s) from the given string,
     * making sure the edge orientation column exists in the given data set.
     *
     * @param v     String
     * @return True if the orientations were correctly parsed.
     * @throws IllegalArgumentException
     */
    protected boolean parseOrientation(String v) throws IllegalArgumentException {
        if (v != null) {
            if (isDirectedString(v) || isReversedString(v)) {
                if (isDirectedString(v)) {
                    globalOrientation = DIRECTED;
                } else if (isReversedString(v)) {
                    globalOrientation = REVERSED;
                }
                edgeOrientationColumnName = getEdgeOrientationColumnName(v);
                LOGGER.info("Global orientation = '{}'.", globalOrientation);
                LOGGER.info("Edge orientation column name = '{}'.", edgeOrientationColumnName);
                return true;
            } else if (isUndirectedString(v)) {
                globalOrientation = UNDIRECTED;
                if (!v.trim().equalsIgnoreCase(UNDIRECTED)) {
                    LOGGER.warn("Edge orientations are ignored for undirected graphs.");
                }
                LOGGER.info("Global orientation = '{}'.", globalOrientation);
                return true;
            } else {
                throw new IllegalArgumentException("Invalid orientation String.");
            }
        }
        return false;
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

    private String getEdgeOrientationColumnName(String v) {
        if (!v.contains(SEPARATOR)) {
            throw new IllegalArgumentException("Bad orientation format. Enter "
                    + POSSIBLE_ORIENTATIONS + ".");
        } else {
            // Extract the global and edge orientations.
            String[] s = v.split(SEPARATOR);
            if (s.length == 2) {
                // Return just the edge orientation column name and not the
                // global orientation.
                return s[1].trim();
            } else {
                throw new IllegalArgumentException(
                        "You must specify both global and edge orientations for "
                                + "directed or reversed graphs. Separate them by "
                                + "a '" + SEPARATOR + "'.");
            }
        }
    }

    /**
     * Returns the global orientation string.
     *
     * @return The global orientation string
     */
    public String getGlobalOrientation() {
        return globalOrientation;
    }

    /**
     * Returns the edge orientation string.
     *
     * @return The edge orientation string
     */
    public String getEdgeOrientationColumnName() {
        return edgeOrientationColumnName;
    }
}

