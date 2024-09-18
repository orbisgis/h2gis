/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.coverage;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;

public class CoverageFunctionTest {
    private static Connection connection;
    private Statement st;
    private static final GeometryFactory FACTORY = new GeometryFactory();

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(CoverageFunctionTest.class.getSimpleName());
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
    public void test_ST_CoverageUnion1() throws Exception {
        ResultSet res = st.executeQuery("WITH coverage(id, geom) AS (VALUES\n" +
                "  (1, 'POLYGON ((10 10, 10 150, 80 190, 110 150, 90 110, 40 110, 50 60, 10 10))'::geometry),\n" +
                "  (2, 'POLYGON ((120 10, 10 10, 50 60, 100 70, 120 10))'::geometry),\n" +
                "  (3, 'POLYGON ((140 80, 120 10, 100 70, 40 110, 90 110, 110 150, 140 80))'::geometry),\n" +
                "  (4, 'POLYGON ((140 190, 120 170, 140 130, 160 150, 140 190))'::geometry),\n" +
                "  (5, 'POLYGON ((180 160, 170 140, 140 130, 160 150, 140 190, 180 160))'::geometry)\n" +
                ")\n" +
                "SELECT ST_CoverageUnion(ARRAY_AGG(geom))\n" +
                "  FROM coverage;\n");
        res.next();
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 150, 80 190, 110 150, 140 80, 120 10, 10 10, 10 150), (50 60, 100 70, 40 110, 50 60)), ((120 170, 140 190, 180 160, 170 140, 140 130, 120 170)))",res.getObject(1));
        res.close();
    }
}
