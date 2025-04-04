/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.network.functions;

import org.h2gis.api.AbstractFunction;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper class for Graph Functions.
 *
 * @author Adam Gouge
 */
public class GraphFunction extends AbstractFunction {

    public static final String ARG_ERROR  = "Unrecognized argument: ";

    /**
     * Return a JGraphT graph from the input edges table.
     *
     * @param connection  Connection
     * @param inputTable  Input table name
     * @param orientation Orientation string
     * @param weight      Weight column name, null for unweighted graphs
     * @param vertexClass type of vertex
     * @param edgeClass type of edge
     * @return Graph
     */
    protected static KeyedGraph prepareGraph(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String weight,
                                             Class vertexClass,
                                             Class edgeClass) throws SQLException {
        GraphFunctionParser parser = new GraphFunctionParser();
        parser.parseWeightAndOrientation(orientation, weight);

        return new GraphCreator(connection,
                inputTable,
                parser.getGlobalOrientation(), parser.getEdgeOrientation(), parser.getWeightColumn(),
                vertexClass,
                edgeClass).prepareGraph();
    }

    /**
     * Log the time elapsed from startTime until now.
     *
     * @param logger    Logger
     * @param startTime Start time in milliseconds
     */
    protected static void logTime(Logger logger, long startTime) {
        logger.debug("    " + (System.currentTimeMillis() - startTime) / 1000. + " seconds");
    }
}
