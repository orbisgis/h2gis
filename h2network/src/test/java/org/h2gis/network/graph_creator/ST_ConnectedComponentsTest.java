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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
    private static final String EDGES = "EDGES";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ConnectedComponentsTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ConnectedComponents(), "");
        registerEdges(connection);
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

    public static void registerEdges(Connection connection) throws SQLException {
        final Statement st = connection.createStatement();
        st.execute("CREATE TABLE " + EDGES + "(" +
                "EDGE_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "START_NODE INT, END_NODE INT, EDGE_ORIENTATION INT);" +
                "INSERT INTO " + EDGES + "(START_NODE, END_NODE, EDGE_ORIENTATION) VALUES "
                + "(1, 2, 1),"
                + "(2, 3, 1),"
                + "(2, 5, 1),"
                + "(2, 6, 1),"
                + "(3, 4, 1),"
                + "(3, 7, 1),"
                + "(4, 3, 1),"
                + "(4, 8, 1),"
                + "(5, 1, 1),"
                + "(5, 6, 1),"
                + "(6, 7, 1),"
                + "(7, 6, 1),"
                + "(8, 4, 1),"
                + "(8, 7, 1),"
                + "(9, 10, 1),"
                + "(10, 9, 1),"
                + "(10, 11, 1),"
                + "(12, 12, 1);");
    }

    @Test
    public void DO() throws Exception {
        dropTables();
        // SELECT ST_ConnectedComponents('" + EDGES + "', 'directed - edge_orientation')
        checkBoolean(compute(DO));
        assertEquals(getDOROVertexPartition(),
                getVertexPartition(st.executeQuery("SELECT * FROM " + EDGES + NODE_COMP_SUFFIX)));
        assertEquals(getDOROEdgePartition(),
                getEdgePartition(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX)));
    }

    @Test
    public void RO() throws Exception {
        // Note that strongly connected components are invariant under global
        // edge orientation reversal.
        dropTables();
        // SELECT ST_ConnectedComponents('" + EDGES + "', 'reversed - edge_orientation')
        checkBoolean(compute(RO));
        assertEquals(getDOROVertexPartition(),
                getVertexPartition(st.executeQuery("SELECT * FROM " + EDGES + NODE_COMP_SUFFIX)));
        assertEquals(getDOROEdgePartition(),
                getEdgePartition(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX)));
    }

    @Test
    public void U() throws Exception {
        dropTables();
        // SELECT ST_ConnectedComponents('" + EDGES + "', 'undirected')
        checkBoolean(compute(U));
        assertEquals(getUVertexPartition(),
                getVertexPartition(st.executeQuery("SELECT * FROM " + EDGES + NODE_COMP_SUFFIX)));
        assertEquals(getUEdgePartition(),
                getEdgePartition(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX)));
    }

    private void dropTables() throws SQLException {
        st.execute("DROP TABLE IF EXISTS " + EDGES + NODE_COMP_SUFFIX);
        st.execute("DROP TABLE IF EXISTS " + EDGES + EDGE_COMP_SUFFIX);
    }

    private ResultSet compute(String orientation) throws SQLException {
        return st.executeQuery("SELECT ST_ConnectedComponents('" + EDGES + "', " + orientation + ")");
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

    private Set<Set<Integer>> getDOROVertexPartition() {
        Set<Set<Integer>> p = new HashSet<Set<Integer>>();
        p.add(getIntSet(1, 2, 5));
        p.add(getIntSet(3, 4, 8));
        p.add(getIntSet(6, 7));
        p.add(getIntSet(9, 10));
        p.add(getIntSet(11));
        p.add(getIntSet(12));
        return p;
    }

    private Set<Set<Integer>> getDOROEdgePartition() {
        Set<Set<Integer>> p = new HashSet<Set<Integer>>();
        p.add(getIntSet(1, 3, 9));
        p.add(getIntSet(1, 3, 9));
        p.add(getIntSet(5, 7, 8, 13));
        p.add(getIntSet(11, 12));
        p.add(getIntSet(15, 16));
        p.add(getIntSet(18));
        p.add(getIntSet(2, 4, 6, 10, 14, 17));
        return p;
    }

    private Set<Set<Integer>> getUVertexPartition() {
        Set<Set<Integer>> p = new HashSet<Set<Integer>>();
        p.add(getIntSet(1, 2, 3, 4, 5, 6, 7, 8));
        p.add(getIntSet(9, 10, 11));
        p.add(getIntSet(12));
        return p;
    }

    private Set<Set<Integer>> getUEdgePartition() {
        Set<Set<Integer>> p = new HashSet<Set<Integer>>();
        p.add(getIntSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        p.add(getIntSet(15, 16, 17));
        p.add(getIntSet(18));
        return p;
    }

    private Set<Integer> getIntSet(Integer... ints) {
        return new HashSet<Integer>(Arrays.asList(ints));
    }

    private Set<Set<Integer>> getVertexPartition(ResultSet nodeComponents) throws SQLException {
        return getPartition(nodeComponents, GraphConstants.NODE_ID);
    }

    private Set<Set<Integer>> getEdgePartition(ResultSet edgeComponents) throws SQLException {
        return getPartition(edgeComponents, GraphConstants.EDGE_ID);
    }

    private Set<Set<Integer>> getPartition(ResultSet components,
                                           String id) throws SQLException {
        try {
            Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
            while (components.next()) {
                final int ccID = components.getInt(CONNECTED_COMPONENT);
                if (map.get(ccID) == null) {
                    map.put(ccID, new HashSet<Integer>());
                }
                map.get(ccID).add(components.getInt(id));
            }
            Set<Set<Integer>> p = new HashSet<Set<Integer>>();
            for (Set<Integer> cc : map.values()) {
                p.add(cc);
            }
            return p;
        } finally {
            components.close();
        }
    }
}
