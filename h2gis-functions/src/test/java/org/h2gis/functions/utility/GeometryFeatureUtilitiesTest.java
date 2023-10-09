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
package org.h2gis.functions.utility;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.jts_utils.GeometryFeatureUtils;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Erwan Bocher, CNRS 2023
 */
public class GeometryFeatureUtilitiesTest {

    private static Connection connection;
    private Statement st;
    private static final String DB_NAME = "UtilityTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
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
    public void test_asJsonList1() throws Exception {
        st.execute("DROP TABLE IF EXISTS AREA");
        st.execute("create table area(idarea int primary key, the_geom GEOMETRY(POINT))");
        st.execute("insert into area values(1, 'POINT (-10 109)')");
        st.execute("insert into area values(2, 'POINT (-10 12)')");
        st.execute("insert into area values(3, null)");

        ArrayList<LinkedHashMap> features = GeometryFeatureUtils.toList(connection, "SELECT * FROM area", 9);
        assertEquals(3, features.size());
        LinkedHashMap<String, Object> feature = features.get(1);
        assertNotNull(feature);
        LinkedHashMap geometry = (LinkedHashMap) feature.get("geometry");
        assertNotNull(geometry);
        assertEquals("Point", geometry.get("type"));
        ArrayList coords = (ArrayList) geometry.get("coordinates");
        assertEquals(-10.0,coords.get(0));
        assertEquals(12.0,coords.get(1));
        feature = features.get(2);
        assertNotNull(feature);
        geometry = (LinkedHashMap) feature.get("geometry");
        assertNull(geometry);
    }

    @Test
    public void test_asJsonList2() throws Exception {
        st.execute("DROP TABLE IF EXISTS AREA");
        st.execute("create table area(idarea int primary key, the_geom GEOMETRY(POINT))");
        st.execute("insert into area values(1, 'POINT (-10 109)')");
        st.execute("insert into area values(2, 'POINT (-10 12)')");
        st.execute("insert into area values(3, null)");

        ArrayList<LinkedHashMap> features = GeometryFeatureUtils.toList(connection, "SELECT idarea FROM area");
        assertEquals(3, features.size());
        LinkedHashMap<String, Object> feature = features.get(1);
        assertNotNull(feature);
        Object value =  feature.get("IDAREA");
        assertNotNull(value);
        assertEquals(2, value);
        feature = features.get(2);
        assertNotNull(feature);
        value = feature.get("IDAREA");
        assertNotNull(value);
        assertEquals(3, value);
    }
}
