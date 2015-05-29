/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
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

import static org.h2gis.network.graph_creator.ST_ConnectedComponents.NULL_CONNECTED_COMPONENT_NUMBER;
import static org.h2gis.network.graph_creator.ST_GraphAnalysisTest.LINE_GRAPH_TABLE;
import static org.h2gis.network.graph_creator.ST_GraphAnalysisTest.createLineGraphTable;
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
        //         1         2        5,7
        //    1-------->2-------->3<------->4        12<-      11
        //    |        /|         |         ^        /   |      ^
        //    |    3 /  |         |         |       /    /18    |
        //   9|    /   4|        6|        8|13     \___/       |17
        //    |   /     |         |         |                   |
        //    |<-/ 10   v  11,12  v    14   v            15,16  |
        //    5-------->6<------->7<--------8         9<------>10
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
        checkStronglyConnectedComponentEdges(getDOROEdgeMap());
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
        checkStronglyConnectedComponentEdges(getDOROEdgeMap());
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

    @Test
    public void testLineGraph() throws Exception {
        final int n = 200;
        final String name = LINE_GRAPH_TABLE + n;
        createLineGraphTable(connection, n);
        checkBoolean(st.executeQuery("SELECT ST_ConnectedComponents('" + name + "', " + U + ")"));
        assertEquals(getOneElementPartition(n),
                getVertexPartition(st.executeQuery("SELECT * FROM " + name + NODE_COMP_SUFFIX)));
        assertEquals(getOneElementPartition(n - 1),
                getEdgePartition(st.executeQuery("SELECT * FROM " + name + EDGE_COMP_SUFFIX)));
    }

    private Set<Set<Integer>> getOneElementPartition(int n) {
        Set<Set<Integer>> p = new HashSet<Set<Integer>>();
        Set<Integer> component = new HashSet<Integer>();
        for (int i = 0; i < n; i++) {
            component.add(i + 1);
        }
        p.add(component);
        return p;
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

    private void checkStronglyConnectedComponentEdges(Map<Integer, Set<Integer>> expectedEdgeMap)
            throws SQLException {
        final Map<Integer, Set<Integer>> actualEdgeMap =
                getCCMap(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX), GraphConstants.EDGE_ID);
        // Check edges in no strongly connected component.
        assertEquals(expectedEdgeMap.get(NULL_CONNECTED_COMPONENT_NUMBER),
                actualEdgeMap.get(NULL_CONNECTED_COMPONENT_NUMBER));
        // Check edge partition.
        assertEquals(getPartition(expectedEdgeMap), getPartition(actualEdgeMap));
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

    private Map<Integer, Set<Integer>> getDOROEdgeMap() {
        Map<Integer, Set<Integer>> p = new HashMap<Integer, Set<Integer>>();
        // We'll actually ignore all component numbers except for
        // NULL_CONNECTED_COMPONENT_NUMBER.
        p.put(1, getIntSet(1, 3, 9));
        p.put(2, getIntSet(1, 3, 9));
        p.put(3, getIntSet(5, 7, 8, 13));
        p.put(4, getIntSet(11, 12));
        p.put(5, getIntSet(15, 16));
        p.put(6, getIntSet(18));
        p.put(NULL_CONNECTED_COMPONENT_NUMBER, getIntSet(2, 4, 6, 10, 14, 17));
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
        return getPartition(getCCMap(nodeComponents, GraphConstants.NODE_ID));
    }

    private Set<Set<Integer>> getEdgePartition(ResultSet edgeComponents) throws SQLException {
        return getPartition(getCCMap(edgeComponents, GraphConstants.EDGE_ID));
    }

    private Set<Set<Integer>> getPartition(Map<Integer, Set<Integer>> map) {
        Set<Set<Integer>> p = new HashSet<Set<Integer>>();
        for (Set<Integer> cc : map.values()) {
            p.add(cc);
        }
        return p;
    }

    private Map<Integer, Set<Integer>> getCCMap(ResultSet components, String id) throws SQLException {
        Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
        try {
            while (components.next()) {
                final int ccID = components.getInt(CONNECTED_COMPONENT);
                if (map.get(ccID) == null) {
                    map.put(ccID, new HashSet<Integer>());
                }
                map.get(ccID).add(components.getInt(id));
            }
            return map;
        } finally {
            components.close();
        }
    }
}
