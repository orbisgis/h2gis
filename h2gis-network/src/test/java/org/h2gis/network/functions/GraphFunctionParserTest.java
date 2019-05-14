/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.network.functions;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the parsing methods of {@link org.h2gis.network.graph_creator.GraphFunctionParser}.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class GraphFunctionParserTest {

    private static GraphFunctionParser parser;
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws Exception {
        parser = new GraphFunctionParser();
        connection = H2GISDBFactory.createSpatialDataBase(GraphFunctionParserTest.class.getSimpleName());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    private void checkOrientation(String input, GraphFunctionParser.Orientation global, String local) {
        assertEquals(global, parser.parseGlobalOrientation(input));
        assertEquals(local, parser.parseEdgeOrientation(input));
    }

    @Test
    public void testNonNullOrientations() {
        checkOrientation("directed - edge_orientation", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation("directed- edge_orientation", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation("directed-edge_orientation", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation(" directed-edge_orientation   ", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation("diRecTEd - name", GraphFunctionParser.Orientation.DIRECTED, "name");
        checkOrientation("rEVersEd - rname", GraphFunctionParser.Orientation.REVERSED, "rname");
        checkOrientation("UnDiRecTEd - q w98 er2", GraphFunctionParser.Orientation.UNDIRECTED, "q w98 er2");
    }

    @Test
    public void testNullOrientation() {
        assertNull(GraphFunctionParser.parseGlobalOrientation(null));
        assertNull(parser.parseEdgeOrientation(null));
    }

    @Test
    public void testGlobalOrientationSpellingError() {
        assertThrows(IllegalArgumentException.class, () -> GraphFunctionParser.parseGlobalOrientation("undirrected"));
    }

    @Test
    public void testMissingEdgeOrientationError() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseEdgeOrientation("directed"));
    }

    @Test
    public void testEdgeOrientationUndirectedGraphError() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseEdgeOrientation("undirected"));
    }

    @Test
    public void testNonNullWeights() {
        assertEquals("weight", parser.parseWeight("weight"));
        assertEquals("weight", parser.parseWeight("  weight     "));
        assertEquals("weight_column", parser.parseWeight("weight_column"));
        assertEquals("weight column", parser.parseWeight("weight column"));
    }

    @Test
    public void testNullWeight() {
        assertNull(parser.parseWeight(null));
    }

    @Test
    public void testWeightOrientationParser() {
        // DO
        GraphFunctionParser p = new GraphFunctionParser();
        p.parseWeightAndOrientation("directed - edge_orientation", null);
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation(null, "directed - edge_orientation");
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        // RO
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("reversed - edge_orientation", null);
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation(null, "reversed - edge_orientation");
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        // U
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("undirected", null);
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.UNDIRECTED, null);
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation(null, "undirected");
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.UNDIRECTED, null);
        // WDO
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("weight", "directed - edge_orientation");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("directed - edge_orientation", "weight");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        // WRO
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("weight", "reversed - edge_orientation");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("reversed - edge_orientation", "weight");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        // WU
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("weight", "undirected");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.UNDIRECTED, null);
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("undirected", "weight");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.UNDIRECTED, null);
    }

    private void checkWeightAndOrientation(GraphFunctionParser p, String weight,
                                           GraphFunctionParser.Orientation global, String local) {
        assertEquals(weight, p.getWeightColumn());
        assertEquals(global, p.getGlobalOrientation());
        assertEquals(local, p.getEdgeOrientation());
    }

    @Test
    public void testDFail() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseWeightAndOrientation(null, null));
    }

    @Test
    public void testWDFail1() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseWeightAndOrientation("weight", null));
    }

    @Test
    public void testWDFail2() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseWeightAndOrientation(null, "weight"));
    }

    @Test
    public void testDoubleWeight() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseWeightAndOrientation("weight", "distance"));
    }

    @Test
    public void testDoubleOrientation() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseWeightAndOrientation("undirected", "undirected"));
    }

    @Test
    public void testDestinationsString() {
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, GraphFunctionParser.parseDestinationsString("1, 2, 3, 4, 5"));
        assertArrayEquals(new int[]{2343, 637, 1, 345}, GraphFunctionParser.parseDestinationsString("2343   ,    637,1, 345"));
        assertArrayEquals(new int[]{1, 2}, GraphFunctionParser.parseDestinationsString("1, 2,,,,,,,,"));
    }

    @Test
    public void testDestinationsStringFail() {
        assertThrows(IllegalArgumentException.class, () -> GraphFunctionParser.parseDestinationsString("1, 2,,3"));
    }

    @Test
    public void testParseInputTable() throws SQLException {
        final TableLocation roads_edges = TableUtilities.parseInputTable(connection, "ROADS_EDGES");
        assertEquals("", roads_edges.getCatalog());
        assertEquals("", roads_edges.getSchema());
        assertEquals("ROADS_EDGES", roads_edges.getTable());
    }

    @Test
    public void testSuffixTableLocation() throws SQLException {
        final TableLocation roads_edges = TableUtilities.parseInputTable(connection, "ROADS_EDGES");
        final TableLocation suffixed = TableUtilities.suffixTableLocation(roads_edges, "_SUFFIX");
        assertEquals("", suffixed.getCatalog());
        assertEquals("", suffixed.getSchema());
        assertEquals("ROADS_EDGES_SUFFIX", suffixed.getTable());
    }
}
