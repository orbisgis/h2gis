/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
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
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates, for each vertex, the (distance to the) closest destination among
 * several possible destinations.
 *
 * @author Adam Gogue
 */
public class ST_Accessibility extends GraphFunction implements ScalarFunction {

    public static final String REMARKS =
            "ST_Accessibility calculates, for each vertex in a graph, the (distance to " +
            "the) closest destination among several possible destinations. " +
            "<p>Possible signatures: " +
            "<ol> " +
            "<li><code> ST_Accessibility('input_edges', 'o[ - eo]', 'ds') </code></li> " +
            "<li><code> ST_Accessibility('input_edges', 'o[ - eo]', 'dt') </code></li> " +
            "<li><code> ST_Accessibility('input_edges', 'o[ - eo]', 'w', 'ds') </code></li> " +
            "<li><code> ST_Accessibility('input_edges', 'o[ - eo]', 'w', 'dt') </code></li> " +
            "</ol> " +
            "where " +
            "<ul> " +
            "<li><code>input_edges</code> = Edges table produced by <code>ST_Graph</code> from table <code>input</code></li> " +
            "<li><code>o</code> = Global orientation (directed, reversed or undirected)</li> " +
            "<li><code>eo</code> = Edge orientation (1 = directed, -1 = reversed, 0 = " +
            "undirected). Required if global orientation is directed or reversed.</li> " +
            "<li><code>w</code> = Name of column containing edge weights as doubles</li> " +
            "<li><code>ds</code> = Comma-separated Destination string ('dest1, dest2, ...')</li> " +
            "<li><code>dt</code> = Destination table name (must contain column " +
            DESTINATION + "containing integer vertex ids)</li> " +
            "</ul> ";

    public ST_Accessibility() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "getAccessibility";
    }

    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String arg3) throws SQLException {
        return getAccessibility(connection, inputTable, orientation, null, arg3);
    }

    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String weight,
                                             String arg4) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        final KeyedGraph<VAccess, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight, VAccess.class);
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
            final ResultSet rs = st.executeQuery(
                    "SELECT * FROM " + TableLocation.parse(destTable).getTable());
            while (rs.next()) {
                destinations.add(graph.getVertex(rs.getInt(DESTINATION)));
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
