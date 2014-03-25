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
 * or contact directly: info_at_orbisgis.org
 */

package org.h2gis.network.graph_creator;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests the parsing methods of {@link org.h2gis.network.graph_creator.GraphFunctionParser}.
 *
 * @author Adam Gouge
 */
public class GraphFunctionParserTest {

    private GraphFunctionParser parser;

    @Before
    public void setUp() {
        parser = new GraphFunctionParser();
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
        assertEquals(null, parser.parseGlobalOrientation(null));
        assertEquals(null, parser.parseEdgeOrientation(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGlobalOrientationSpellingError() {
        parser.parseGlobalOrientation("undirrected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingEdgeOrientationError() {
        parser.parseEdgeOrientation("directed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeOrientationUndirectedGraphError() {
        parser.parseEdgeOrientation("undirected");
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
        assertEquals(null, parser.parseWeight(null));
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

    @Test(expected = IllegalArgumentException.class)
    public void testDFail() {
        parser.parseWeightAndOrientation(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWDFail1() {
        parser.parseWeightAndOrientation("weight", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWDFail2() {
        parser.parseWeightAndOrientation(null, "weight");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleWeight() {
        parser.parseWeightAndOrientation("weight", "distance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleOrientation() {
        parser.parseWeightAndOrientation("undirected", "undirected");
    }

    @Test
    public void testDestinationsString() {
        assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5},
                parser.parseDestinationsString("1, 2, 3, 4, 5")));
        assertTrue(Arrays.equals(new int[]{2343, 637, 1, 345},
                parser.parseDestinationsString("2343   ,    637,1, 345")));
        assertTrue(Arrays.equals(new int[]{1, 2},
                parser.parseDestinationsString("1, 2,,,,,,,,")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestinationsStringFail() {
        assertTrue(Arrays.equals(new int[]{1, 2, 3},
                parser.parseDestinationsString("1, 2,,3")));
    }
}
