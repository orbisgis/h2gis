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
package org.h2gis.graalvm;

import org.h2gis.utilities.GeometryTableUtilities;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JSON returned by h2gis_get_tables_json, tested on a JVM.
 *
 * @author Erwan Bocher, CNRS
 */
public class TablesJsonTest {

    @Test
    public void testTablesJson() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:TablesJsonTest", "sa", "");
             Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE PUBLIC.SPATIAL_REF_SYS(SRID INT PRIMARY KEY, AUTH_NAME VARCHAR, "
                    + "AUTH_SRID INT, SRTEXT VARCHAR, PROJ4TEXT VARCHAR)");
            st.execute("INSERT INTO PUBLIC.SPATIAL_REF_SYS VALUES (2154, 'EPSG', 2154, '', '')");
            st.execute("CREATE TABLE ROADS(ID BIGINT PRIMARY KEY, THE_GEOM GEOMETRY(LINESTRING, 2154), G GEOMETRY)");
            st.execute("CREATE SPATIAL INDEX ON ROADS(THE_GEOM)");
            st.execute("CREATE TABLE \"quo\"\"te\"(\"na\\me\" GEOMETRY(POINT Z))");

            String json = TablesJson.toJson(GeometryTableUtilities.getTablesMetaData(connection, true));
            assertEquals("["
                    + "{\"catalog\":\"TABLESJSONTEST\",\"schema\":\"PUBLIC\",\"table\":\"ROADS\",\"tableType\":\"TABLE\","
                    + "\"fid\":\"ID\",\"fidIndex\":1,\"geometryColumns\":["
                    + "{\"name\":\"THE_GEOM\",\"index\":2,\"geometryType\":\"LINESTRING\",\"geometryTypeCode\":2,"
                    + "\"dimension\":2,\"hasZ\":false,\"hasM\":false,\"srid\":2154,\"sridDeclared\":true,"
                    + "\"authName\":\"EPSG\",\"authSrid\":2154,\"spatialIndex\":true},"
                    + "{\"name\":\"G\",\"index\":3,\"geometryType\":\"GEOMETRY\",\"geometryTypeCode\":0,"
                    + "\"dimension\":2,\"hasZ\":false,\"hasM\":false,\"srid\":0,\"sridDeclared\":false,"
                    + "\"authName\":null,\"authSrid\":0,\"spatialIndex\":false}]},"
                    + "{\"catalog\":\"TABLESJSONTEST\",\"schema\":\"PUBLIC\",\"table\":\"quo\\\"te\",\"tableType\":\"TABLE\","
                    + "\"fid\":null,\"fidIndex\":0,\"geometryColumns\":["
                    + "{\"name\":\"na\\\\me\",\"index\":1,\"geometryType\":\"POINTZ\",\"geometryTypeCode\":1001,"
                    + "\"dimension\":3,\"hasZ\":true,\"hasM\":false,\"srid\":0,\"sridDeclared\":false,"
                    + "\"authName\":null,\"authSrid\":0,\"spatialIndex\":false}]}"
                    + "]", json);

            assertEquals("[]", TablesJson.toJson(GeometryTableUtilities.getTablesMetaData(connection, "NOPE", null, false)));
        }
    }
}