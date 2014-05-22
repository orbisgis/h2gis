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

import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.javanetworkanalyzer.data.VUCent;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.h2gis.network.graph_creator.GraphFunctionParser.Orientation.UNDIRECTED;
import static org.h2gis.network.graph_creator.GraphFunctionParser.parseGlobalOrientation;
import static org.h2gis.utilities.GraphConstants.*;

/**
 * Calculates the connected components (for undirected graphs) or strongly
 * connected components (for directed graphs) of a graph.
 *
 * @author Adam Gouge
 */
public class ST_ConnectedComponents  extends GraphFunction implements ScalarFunction {

    private static Connection connection;
    private TableLocation tableName;
    private TableLocation nodesName;
    private TableLocation edgesName;
    protected static final int BATCH_SIZE = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_ConnectedComponents.class);
    public static final String REMARKS =
            "`ST_ConnectedComponents` the connected components (for undirected graphs) or\n" +
            "strongly connected components (for directed graphs) of a graph.  It produces\n" +
            "two tables (nodes and edges) containing a node or edge id and a connected\n" +
            "component id. Signature: \n" +
            "* `ST_ConnectedComponents('input_edges', 'o[ - eo]')`\n" +
            "\n" +
            "where \n" +
            "* `input_edges` = Edges table produced by `ST_Graph` from table `input`\n" +
            "* `o` = Global orientation (directed, reversed or undirected)\n" +
            "* `eo` = Edge orientation (1 = directed, -1 = reversed, 0 = undirected).\n";

    /**
     * Constructor
     */
    public ST_ConnectedComponents() {
        this(null, null);
    }

    /**
     * Constructor
     *
     * @param connection Connection
     * @param inputTable Input table
     */
    public ST_ConnectedComponents(Connection connection,
                                  String inputTable) {
        addProperty(PROP_REMARKS, REMARKS);
        if (connection != null) {
            this.connection = connection;
        }
        if (inputTable != null) {
            this.tableName = TableLocation.parse(inputTable);
            this.nodesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + NODE_COMP_SUFFIX);
            this.edgesName = new TableLocation(tableName.getCatalog(), tableName.getSchema(),
                    tableName.getTable() + EDGE_COMP_SUFFIX);
        }
    }

    @Override
    public String getJavaStaticMethod() {
        return "getConnectedComponents";
    }

    /**
     * Calculate the node and edge connected component tables.
     *
     * @param connection  Connection
     * @param inputTable  Edges table produced by ST_Graph
     * @param orientation Orientation string
     * @return True if the calculation was successful
     * @throws SQLException
     */
    public static boolean getConnectedComponents(Connection connection,
                                                   String inputTable,
                                                   String orientation) throws SQLException {
        ST_ConnectedComponents f = new ST_ConnectedComponents(connection, inputTable);
        try {
            createTables(f);
            final KeyedGraph graph = prepareGraph(connection, inputTable, orientation, null,
                    VUCent.class, Edge.class);
            if (parseGlobalOrientation(orientation).equals(UNDIRECTED)) {
                storeConnectedComponents(new ConnectivityInspector<VUCent, Edge>(
                        (UndirectedGraph<VUCent, Edge>) graph).connectedSets());
            } else {
                storeConnectedComponents(new StrongConnectivityInspector<VUCent, Edge>(
                        (DirectedGraph<VUCent, Edge>) graph).stronglyConnectedSets());
            }
        } catch (SQLException e) {
            LOGGER.error("Problem creating connected component tables.");
            final Statement statement = connection.createStatement();
            try {
                statement.execute("DROP TABLE IF EXISTS " + f.nodesName);
                statement.execute("DROP TABLE IF EXISTS " + f.edgesName);
            } finally {
                statement.close();
            }
            return false;
        }
        return true;
    }

    private static void storeConnectedComponents(List<Set<VUCent>> componentsList) {
    }

    private static void createTables(ST_ConnectedComponents f) throws SQLException {
        final Statement st = connection.createStatement();
        try {
            st.execute("CREATE TABLE " + f.nodesName + "(" +
                    NODE_ID + " INTEGER PRIMARY KEY, " +
                    CONNECTED_COMPONENT + " DOUBLE);");
            st.execute("CREATE TABLE " + f.edgesName + "(" +
                    EDGE_ID + " INTEGER PRIMARY KEY, " +
                    CONNECTED_COMPONENT + " DOUBLE);");
        } finally {
            st.close();
        }
    }
}
