/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.properties;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Erwan Bocher, CNRS (2020)
 */
public class PropertiesFunctionTest {

    private static Connection connection;
    private Statement st;
    private static WKTReader WKT_READER;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(PropertiesFunctionTest.class.getSimpleName());
        WKT_READER = new WKTReader();
        WKT_READER.setIsOldJtsCoordinateSyntaxAllowed(false);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void testST_GeometryType() throws Exception {
        HashMap<String, String> geometriesToTest = new HashMap();
        geometriesToTest.put("POINT", "SRID=4326;POINT(0 0)");
        geometriesToTest.put("POINTZ", "SRID=4326;POINTZ(0 0 0)");
        geometriesToTest.put("POINTM", "SRID=4326;POINTM(0 0 0)");
        geometriesToTest.put("POINTZM", "SRID=4326;POINTZM(0 0 0 0)");
        geometriesToTest.put("LINESTRING", "SRID=4326;LINESTRING(20 10,20 20)");
        geometriesToTest.put("LINESTRINGZ", "SRID=4326;LINESTRINGZ(20 10 0,20 20 0)");
        geometriesToTest.put("LINESTRINGM", "SRID=4326;LINESTRINGM(20 10 0,20 20 0)");
        geometriesToTest.put("LINESTRINGZM", "SRID=4326;LINESTRINGZM(20 10 0 0,20 20 0 0)");
        geometriesToTest.put("MULTILINESTRING", "SRID=4326;MULTILINESTRING((0 0, 10 15), (56 50, 10 15))");
        geometriesToTest.put("MULTILINESTRINGZ", "SRID=4326;MULTILINESTRINGZ((0 0 0, 10 15 0), (56 50 0, 10 15 0))");
        geometriesToTest.put("MULTILINESTRINGM", "SRID=4326;MULTILINESTRINGM((0 0 0, 10 15 0), (56 50 0, 10 15 0))");
        geometriesToTest.put("MULTILINESTRINGZM", "SRID=4326;MULTILINESTRINGZM((0 0 0 0, 10 15 0 0), (56 50 0 0, 10 15 0 0))");

        for (Map.Entry<String, String> entry : geometriesToTest.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            ResultSet rs = st.executeQuery("SELECT ST_GeometryType('" + val + "'::GEOMETRY);");
            assertTrue(rs.next());
            assertEquals(key, rs.getString(1));
            rs.close();
        }
    }

    @Test
    public void testST_MemSize() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MEmSIZE(null)");
        res.next();
        assertNull(res.getObject(1));
        res.close();
    }

    @Test
    public void testST_MemSize1() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MemSize(ST_GeomFromText('POINT(0 0)'))");
        res.next();
        assertEquals(21l, res.getObject(1));
        res.close();
    }

    @Test
    public void testST_MemSize2() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MemSize(ST_GeomFromText('POINT(0 0)', 4326))");
        res.next();
        assertEquals(25l, res.getObject(1));
        res.close();
    }

    @Test
    public void testST_MemSize3() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MemSize(ST_GeomFromText('POINT EMPTY'))");
        res.next();
        assertEquals(21l, res.getObject(1));
        res.close();
    }

}
