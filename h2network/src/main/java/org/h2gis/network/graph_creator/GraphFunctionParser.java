/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.network.graph_creator;

import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A helper class for parsing String arguments to h2network graph functions.
 *
 * @author Adam Gouge
 */
public class GraphFunctionParser {

    private String weightColumn;
    private Orientation globalOrientation;
    private String edgeOrientation;

    public static final String SEPARATOR = "-";
    public static final String DIRECTED = "directed";
    public static final String REVERSED = "reversed";
    public static final String UNDIRECTED = "undirected";

    public enum Orientation {
        DIRECTED, REVERSED, UNDIRECTED
    }

    public static final String EDGE_ORIENTATION_COLUMN = "edge_orientation_column";
    public static final String POSSIBLE_ORIENTATIONS =
            "'" + DIRECTED + " - " + EDGE_ORIENTATION_COLUMN + "' "
                    + "| '" + REVERSED + " - " + EDGE_ORIENTATION_COLUMN + "' "
                    + "| '" + UNDIRECTED + "'";
    public static final String ORIENTATION_ERROR =
            "Bad orientation format. Enter " + POSSIBLE_ORIENTATIONS + ".";

    /**
     * Recovers the weight column name from a string.
     *
     * @param v String
     *
     * @return the weight column name
     */
    protected String parseWeight(String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }

    /**
     * Recovers the global orientation from a string.
     *
     *
     * @param v String
     * @return The global orientation
     */
    protected static Orientation parseGlobalOrientation(String v) {
        if (v == null) {
            return null;
        }
        if (isDirectedString(v)) {
            return Orientation.DIRECTED;
        } else if (isReversedString(v)) {
            return Orientation.REVERSED;
        } else if (isUndirectedString(v)) {
            return Orientation.UNDIRECTED;
        } else {
            throw new IllegalArgumentException(ORIENTATION_ERROR);
        }
    }

    private static boolean isDirectedString(String s) {
        if (s == null) {
            return false;
        }
        return s.toLowerCase().contains(DIRECTED)
                && !isUndirectedString(s);
    }

    private static boolean isReversedString(String s) {
        if (s == null) {
            return false;
        }
        return s.toLowerCase().contains(REVERSED);
    }

    private static boolean isUndirectedString(String s) {
        if (s == null) {
            return false;
        }
        return s.toLowerCase().contains(UNDIRECTED);
    }

    private boolean isOrientationString(String s) {
       return isDirectedString(s) || isReversedString(s) || isUndirectedString(s);
    }

    private boolean isWeightString(String s) {
        if (s == null) {
            return false;
        }
        return !isOrientationString(s);
    }

    /**
     * Recovers the edge orientation from a string.
     *
     * @param v String
     * @return The edge orientation
     */
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

    /**
     * Parse the weight and orientation(s) from two strings, given in arbitrary
     * order.
     *
     * @param arg1 Weight or orientation
     * @param arg2 Weight or orientation
     */
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
            if (!globalOrientation.equals(Orientation.UNDIRECTED)) {
                edgeOrientation = parseEdgeOrientation(orient);
            }
        }
    }

    /**
     * Get the weight column name.
     *
     * @return weight column name
     */
    public String getWeightColumn() {
        return weightColumn;
    }

    /**
     * Get the global orientation string.
     *
     * @return global orientation string
     */
    public Orientation getGlobalOrientation() {
        return globalOrientation;
    }

    /**
     * Get the edge orientation column name.
     *
     * @return edge orientation column name
     */
    public String getEdgeOrientation() {
        return edgeOrientation;
    }

    /**
     * Returns true if the given string contains a comma.
     * @param s String
     * @return true if the given string contains a comma
     */
    protected static boolean isDestinationsString(String s) {
        return s.substring(0, 1).matches("[0-9]");
    }

    /**
     * Returns an array of destination ids from a comma-separated list of
     * destinations.
     *
     * @param s Comma-separated list of destinations
     *
     * @return An array of destination ids
     */
    public static int[] parseDestinationsString(String s) {
        if (!s.contains(",")) {
            return new int[]{Integer.valueOf(s.trim())};
        }
        String[] array = s.split(",");
        int[] destinations = new int[array.length];
        for (int i = 0; i < destinations.length; i++) {
            final String stringWithNoWhiteSpaces = array[i].replaceAll("\\s", "");
            if (stringWithNoWhiteSpaces.isEmpty()) {
                throw new IllegalArgumentException("Empty destination. Too many commas?");
            }
            destinations[i] = Integer.valueOf(stringWithNoWhiteSpaces);
        }
        return destinations;
    }

    /**
     * Convert an input table String to a TableLocation
     *
     * @param connection Connection
     * @param inputTable Input table
     * @return corresponding TableLocation
     * @throws SQLException
     */
    public static TableLocation parseInputTable(Connection connection,
                                                String inputTable) throws SQLException {
       return TableLocation.parse(inputTable, JDBCUtilities.isH2DataBase(connection.getMetaData()));
    }

    /**
     * Suffix a TableLocation
     *
     * @param inputTable Input table
     * @param suffix     Suffix
     * @return suffixed TableLocation
     */
    public static TableLocation suffixTableLocation(TableLocation inputTable,
                                                    String suffix) {
        return new TableLocation(inputTable.getCatalog(), inputTable.getSchema(),
                inputTable.getTable() + suffix);
    }
}
