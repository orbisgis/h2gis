package org.h2gis.h2spatialext;

import org.h2.value.ValueGeometry;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * @author Nicolas Fortin
 */
public class TableFunctionTest {

    private static Connection connection;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(TableFunctionTest.class.getSimpleName(), false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    public static void assertGeometryEquals(String expectedWKT, Object resultSetObject) {
        assertEquals(ValueGeometry.get(expectedWKT), ValueGeometry.getFromGeometry(resultSetObject));
    }

    public void testST_TriangleContouringWithZ() throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS TIN");
            st.execute("CREATE TABLE TIN AS SELECT 'POLYGON ((-9.1 3.7 1, 0.2 1.4 4.4, -5.7 -4.1 4, -9.1 3.7 1))' the_geom");
            ResultSet rs = st.executeQuery("select * from ST_TriangleContouring('TIN')");
        } finally {
            st.close();
        }
    }
}
