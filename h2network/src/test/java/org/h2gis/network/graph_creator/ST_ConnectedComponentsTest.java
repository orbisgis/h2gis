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
 * or contact directly: info_at_orbisgis.org
 */

package org.h2gis.network.graph_creator;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GraphConstants;
import org.junit.*;

import java.sql.*;

import static org.h2gis.utilities.GraphConstants.*;
import static org.junit.Assert.*;

/**
 * @author Adam Gouge
 */
public class ST_ConnectedComponentsTest {

    private static Connection connection;
    private Statement st;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ConnectedComponentsTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ConnectedComponents(), "");
        GraphCreatorTest.registerCormenGraph(connection);
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void DO() throws Exception {
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + NODE_COMP_SUFFIX);
        st.execute("DROP TABLE IF EXISTS CORMEN_EDGES_ALL" + EDGE_COMP_SUFFIX);

        // SELECT * FROM ST_GraphAnalysis('CORMEN_EDGES_ALL', 'directed - edge_orientation')
        checkBoolean(compute(DO));

        checkNodes(st.executeQuery("SELECT * FROM CORMEN_EDGES_ALL" + NODE_COMP_SUFFIX),
                new int[]{1, 1, 1, 1, 1});
    }


    private ResultSet compute(String orientation) throws SQLException {
        return st.executeQuery("SELECT ST_ConnectedComponents('CORMEN_EDGES_ALL', " + orientation + ")");
    }

    private void checkBoolean(ResultSet rs) throws SQLException {
        try{
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
    }

    private void checkNodes(ResultSet nodeComponents,
                            int[] components) throws SQLException {
        try {
            while (nodeComponents.next()) {
                final int nodeID = nodeComponents.getInt(GraphConstants.NODE_ID);
                assertEquals(components[nodeID - 1], nodeComponents.getInt(CONNECTED_COMPONENT));
            }
        } finally {
            nodeComponents.close();
        }
    }
}
