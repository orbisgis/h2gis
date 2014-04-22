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
import org.javanetworkanalyzer.analyzers.AccessibilityAnalyzer;
import org.javanetworkanalyzer.data.VAccess;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import static org.h2gis.h2spatial.TableFunctionUtil.isColumnListConnection;
import static org.h2gis.utilities.GraphConstants.CLOSEST_DEST;
import static org.h2gis.utilities.GraphConstants.DISTANCE;
import static org.h2gis.utilities.GraphConstants.SOURCE;

/**
 * @author Adam Gogue
 */
public class ST_Accessibility extends GraphFunction implements ScalarFunction {

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
        // Decide whether this is a destination string or a table string.
        if (GraphFunctionParser.isDestinationsString(arg4)) {
            final int[] dests = GraphFunctionParser.parseDestinationsString(arg4);
            return allToSeveral(connection, inputTable, orientation, weight, dests);
        } else {
            // arg4 is a destination table.
            return allToMany(connection, inputTable, orientation, weight, arg4);
        }
    }

    private static ResultSet allToSeveral(Connection connection,
                                          String inputTable,
                                          String orientation,
                                          String weight,
                                          int[] dests) throws SQLException {
        final SimpleResultSet output = prepareResultSet();
        final KeyedGraph<VAccess, Edge> graph =
                prepareGraph(connection, inputTable, orientation, weight, VAccess.class);

        Set<VAccess> destinations = new HashSet<VAccess>();
        for (int i = 0; i < dests.length; i++) {
            destinations.add(graph.getVertex(dests[i]));
        }

        new AccessibilityAnalyzer(graph, destinations).compute();

        for (VAccess v : graph.vertexSet()) {
            output.addRow(v.getID(), v.getClosestDestinationId(), v.getDistanceToClosestDestination());
        }
        return output;
    }

    private static ResultSet allToMany(Connection connection,
                                       String inputTable,
                                       String orientation,
                                       String weight,
                                       String destTable) {
        return null;
    }

    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn(SOURCE, Types.INTEGER, 10, 0);
        output.addColumn(CLOSEST_DEST, Types.INTEGER, 10, 0);
        output.addColumn(DISTANCE, Types.DOUBLE, 10, 0);
        return output;
    }
}
