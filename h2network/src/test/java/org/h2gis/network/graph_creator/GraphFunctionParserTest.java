/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_orbisgis.org
 */
package org.h2gis.network.graph_creator;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the parsing methods of {@link org.h2gis.network.graph_creator.GraphFunctionParser}.
 *
 * @author Adam Gouge
 */
public class GraphFunctionParserTest {

    private static GraphFunctionParser parser;

    @BeforeClass
    public static void setUp() {
        parser = new GraphFunctionParser();
    }

    private void checkOrientation(String input, String global, String local) {
        assertEquals(global, parser.parseGlobalOrientation(input));
        assertEquals(local, parser.parseEdgeOrientation(input));
    }

    @Test
    public void testNonNullOrientations() {
        checkOrientation("directed - edge_orientation", GraphFunctionParser.DIRECTED, "edge_orientation");
        checkOrientation("directed- edge_orientation", GraphFunctionParser.DIRECTED, "edge_orientation");
        checkOrientation("directed-edge_orientation", GraphFunctionParser.DIRECTED, "edge_orientation");
        checkOrientation(" directed-edge_orientation   ", GraphFunctionParser.DIRECTED, "edge_orientation");
        checkOrientation("diRecTEd - name", GraphFunctionParser.DIRECTED, "name");
        checkOrientation("rEVersEd - rname", GraphFunctionParser.REVERSED, "rname");
        checkOrientation("UnDiRecTEd - q w98 er2", GraphFunctionParser.UNDIRECTED, "q w98 er2");
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
}
