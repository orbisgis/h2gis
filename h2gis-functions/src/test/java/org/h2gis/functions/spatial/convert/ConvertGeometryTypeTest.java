/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.convert;

import org.h2gis.functions.factory.H2GISSimpleDBFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Erwan Bocher, CNRS
 */
public class ConvertGeometryTypeTest {


    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "ConvertGeometryTypeTest";

    @BeforeAll
    static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISSimpleDBFactory.createSpatialDataBase(DB_NAME, true);
    }

    @BeforeEach
    void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @Test
    public void ST_Multi1() throws SQLException {
        ResultSet res = st.executeQuery("SELECT ST_MULTI('POINT(0 0)'::GEOMETRY) as mpoint," +
                "ST_MULTI(null) as geomnull, ST_MULTI('LINESTRING (130 180, 260 170)'::GEOMETRY) as mline," +
                "ST_MULTI('MULTIPOLYGON (((114 186, 180 186, 180 120, 114 120, 114 186))," +
                "  ((220 170, 250 170, 250 120, 220 120, 220 170)))'::GEOMETRY) as mpolygon,"+
                "ST_MULTI( 'GEOMETRYCOLLECTION (LINESTRING (30 80, 225 86), POINT (136 124))') as gc;");
        res.next();
        assertTrue(res.getObject(1) instanceof MultiPoint);
        assertNull(res.getObject(2));
        assertTrue(res.getObject(3) instanceof MultiLineString);
        assertTrue(res.getObject(4) instanceof MultiPolygon);
        assertTrue(res.getObject(5) instanceof GeometryCollection);
    }
}
