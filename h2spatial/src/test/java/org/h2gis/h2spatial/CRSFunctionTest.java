/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatial;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.SFSUtilities;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.spatialut.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Erwan Bocher
 */
public class CRSFunctionTest {

    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "CRSFunctionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SFSUtilities.wrapConnection(SpatialH2UT.createSpatialDataBase(DB_NAME));
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
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
    public void test_ST_Transform27572To4326() throws Exception {
        checkProjectedGeom("POINT(584173.736059813 2594514.82833411)", 27572, 4326,
                "POINT(2.114551398096724 50.34560979151726)");
    }

    @Test
    public void testST_Transform4326to2154() throws Exception {
        checkProjectedGeom("POINT(2.114551393 50.345609791)", 4326, 2154,
                "POINT(636890.7403226076 7027895.263553156)");
    }

    @Test
    public void test_ST_Transform27572to3857() throws Exception {
        checkProjectedGeom("POINT(282331 2273699.7)", 27572, 3857,
                "POINT(-208496.53743537163 6005369.877027287)");
    }

    @Test
    public void testST_Transform27572to2154WithoutNadgrid() throws Exception {
        checkProjectedGeom("POINT(282331 2273699.7)", 27572, 2154,
                "POINT(332602.9618934966 6709788.264478932)");
    }

    @Test
    public void testST_Transform27572to2154WithNadgrid() throws Exception {
        checkProjectedGeom("POINT(565767.906 2669005.730)", 320002120, 310024140,
                "POINT(619119.4605077105 7102502.97947694)");
    }

    @Test
    public void testST_TransformAsIdentity() throws Exception {
        checkProjectedGeom("POINT(565767.906 2669005.730)", 2154, 2154,
                "POINT(565767.906 2669005.730)");
    }

    private void checkProjectedGeom(String inputGeom, int inProj, int outProj, String expectedGeom) throws SQLException {
        ResultSet srs = st.executeQuery("SELECT ST_TRANSFORM(" +
                "ST_GeomFromText('" + inputGeom + "', " + inProj + "), " + outProj + ");");
        try {
            assertTrue(srs.next());
            assertGeometryEquals(expectedGeom, outProj, srs.getObject(1));
            assertFalse(srs.next());
        } finally {
            srs.close();
        }
    }
}
