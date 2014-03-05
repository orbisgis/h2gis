/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VDijkstra;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ST_ShortestPathLength
 *
 * @author Adam Gouge
 */
public class ST_ShortestPathLength extends AbstractFunction implements ScalarFunction {

    private Connection connection;

    private GraphFunctionParser parser = new GraphFunctionParser();

    private String inputTable;
    private String weightColumn;
    private String globalOrientation;
    private String edgeOrientation;

    public static final int SOURCE_INDEX = 1;
    public static final int DESTINATION_INDEX = 2;
    public static final int DISTANCE_INDEX = 3;

    public ST_ShortestPathLength() {
        addProperty(PROP_REMARKS, "ST_ShortestPathLength ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getShortestPathLength";
    }

    /**
     * Unweighted Directed One-to-One
     *
     * @param connection  Connection
     * @param inputTable  Input table name
     * @param source      Source vertex ID
     * @param destination Destination vertex ID
     * @return Source-Destination distance table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  int source,
                                                  int destination) throws SQLException {
        return getShortestPathLength(connection, inputTable, source, destination, null, null);
    }

    /**
     * Weighted Directed One-to-One
     *
     * @param connection  Connection
     * @param inputTable  Input table name
     * @param source      Source vertex ID
     * @param destination Destination vertex ID
     * @return Source-Destination distance table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  int source,
                                                  int destination,
                                                  String weightColumn) throws SQLException {
        return getShortestPathLength(connection, inputTable, source, destination, weightColumn, null);
    }

    /**
     * Weighted Directed One-to-One
     *
     * @param connection   Connection
     * @param inputTable   Input table name
     * @param source       Source vertex ID
     * @param destination  Destination vertex ID
     * @param weightColumn Weight column name
     * @return Source-Destination distance table
     * @throws SQLException
     */
    public static ResultSet getShortestPathLength(Connection connection,
                                                  String inputTable,
                                                  int source,
                                                  int destination,
                                                  String weightColumn,
                                                  String globalOrientationString) throws SQLException {
        ST_ShortestPathLength function = new ST_ShortestPathLength();
        function.connection = connection;
        function.inputTable = inputTable;
        function.weightColumn = function.parser.parseWeight(weightColumn);
        function.globalOrientation = function.parser.parseGlobalOrientation(globalOrientationString);
        function.edgeOrientation = function.parser.parseEdgeOrientation(globalOrientationString);

        SimpleResultSet output = new SimpleResultSet();
        output.addColumn("SOURCE", Types.INTEGER, 10, 0);
        output.addColumn("DESTINATION", Types.INTEGER, 10, 0);
        output.addColumn("DISTANCE", Types.DOUBLE, 10, 0);

        KeyedGraph<VDijkstra, Edge> graph =
                new GraphCreator<VDijkstra, Edge>(connection,
                        function.inputTable,
                        function.weightColumn,
                        function.globalOrientation,
                        function.edgeOrientation,
                        VDijkstra.class,
                        Edge.class).prepareGraph();

        Dijkstra<VDijkstra, Edge> dijkstra = new Dijkstra<VDijkstra, Edge>(graph);
        final double distance = dijkstra.oneToOne(graph.getVertex(source), graph.getVertex(destination));

        output.addRow(source, destination, distance);
        return output;
    }
}
