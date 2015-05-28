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

import org.h2.tools.SimpleResultSet;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.javanetworkanalyzer.analyzers.AccessibilityAnalyzer;
import org.javanetworkanalyzer.data.VAccess;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import static org.h2gis.h2spatial.TableFunctionUtil.isColumnListConnection;
import static org.h2gis.network.graph_creator.GraphFunctionParser.parseInputTable;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates, for each vertex, the closest destination among several possible
 * destinations, as well as the distance to this destination.
 *
 * @author Adam Gogue
 */
public class ST_Accessibility extends GraphFunction implements ScalarFunction {

    public static final String REMARKS =
            "`ST_Accessibility` calculates, for each vertex in a graph, the closest\n" +
            "destination among several possible destinations as well as the distance to this\n" +
            "destination. Possible signatures: \n" +
            "* `ST_Accessibility('input_edges', 'o[ - eo]', 'ds')`\n" +
            "* `ST_Accessibility('input_edges', 'o[ - eo]', 'dt')`\n" +
            "* `ST_Accessibility('input_edges', 'o[ - eo]', 'w', 'ds')`\n" +
            "* `ST_Accessibility('input_edges', 'o[ - eo]', 'w', 'dt')` \n" +
            "\n" +
            "where \n" +
            "* `input_edges` = Edges table produced by `ST_Graph` from table `input`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected).\n" +
            "  Required if global orientation is directed or reversed.\n" +
            "* `w` = Name of column containing edge weights as doubles\n" +
            "* `ds` = Comma-separated Destination string ('dest1, dest2, ...')\n" +
            "* `dt` = Destination table name (must contain column containing integer vertex\n" +
            "  ids)\n";

    /**
     * Constructor
     */
    public ST_Accessibility() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getAccessibility";
    }

    /**
     * @param connection  Connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param arg3        Destination string or destination table
     * @return Table with closest destination id and distance to closest
     * destination
     * @throws SQLException
     */
    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String arg3) throws SQLException {
        return getAccessibility(connection, inputTable, orientation, null, arg3);
    }

    /**
     * @param connection  Connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @param weight      Weight
     * @param arg4        Destination string or destination table
     * @return Table with closest destination id and distance to closest
     * destination
     * @throws SQLException
     */
    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String weight,
                                             String arg4) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        final KeyedGraph<VAccess, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight, VAccess.class, Edge.class);
        // Decide whether this is a destination string or a table string.
        if (GraphFunctionParser.isDestinationsString(arg4)) {
            return compute(graph, prepareDestSet(graph, GraphFunctionParser.parseDestinationsString(arg4)));
        } else {
            // arg4 is a destination table.
            return compute(graph, prepareDestSet(connection, graph, arg4));
        }
    }

    private static ResultSet compute(KeyedGraph<VAccess, Edge> graph,
                                     Set<VAccess> destinations) throws SQLException {
        SimpleResultSet output = prepareResultSet();
        new AccessibilityAnalyzer(graph, destinations).compute();
        for (VAccess v : graph.vertexSet()) {
            output.addRow(v.getID(), v.getClosestDestinationId(), v.getDistanceToClosestDestination());
        }
        return output;
    }

    private static Set<VAccess> prepareDestSet(KeyedGraph<VAccess, Edge> graph, int[] dests) {
        Set<VAccess> destinations = new HashSet<VAccess>();
        for (int i = 0; i < dests.length; i++) {
            destinations.add(graph.getVertex(dests[i]));
        }
        return destinations;
    }

    private static Set<VAccess> prepareDestSet(Connection connection,
                                               KeyedGraph<VAccess, Edge> graph,
                                               String destTable) throws SQLException {
        final Statement st = connection.createStatement();
        Set<VAccess> destinations = new HashSet<VAccess>();
        try {
            final TableLocation destinationTable = parseInputTable(connection, destTable);
            final ResultSet rs = st.executeQuery(
                    "SELECT " + DESTINATION + " FROM " + destinationTable);
            while (rs.next()) {
                destinations.add(graph.getVertex(rs.getInt(1)));
            }
        } finally {
            st.close();
        }
        return destinations;
    }

    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(CLOSEST_DEST, Types.INTEGER, 10, 0);
        output.addColumn(DISTANCE, Types.DOUBLE, 10, 0);
        return output;
    }
}
