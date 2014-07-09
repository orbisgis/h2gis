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
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Adam Gouge
 */
public class ST_ShortestPathTreeTest {

    private static Connection connection;
    private Statement st;
    private static final double TOLERANCE = 0.0;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String W = "'weight'";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ShortestPathTreeTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ShortestPath(), "");
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

    private ResultSet oneToAll(String table, String orientation, int source) throws SQLException {
        return oneToAll(table, orientation, null, source, Double.POSITIVE_INFINITY);
    }

    private ResultSet oneToAll(String table, String orientation, int source, double radius) throws SQLException {
        return oneToAll(table, orientation, null, source, radius);
    }

    private ResultSet oneToAll(String table, String orientation, String weight, int source) throws SQLException {
        return oneToAll(table, orientation, weight, source, Double.POSITIVE_INFINITY);
    }

    private ResultSet oneToAll(String table, String orientation, String weight,
                               int source, double radius) throws SQLException {
        return st.executeQuery(
                "SELECT * FROM ST_ShortestPathTree('" + table + "', "
                        + orientation
                        + ((weight != null) ? ", " + weight : "") + ", "
                        + source
                        + ((radius < Double.POSITIVE_INFINITY) ? ", " + radius  : "") + ")");
    }
}
