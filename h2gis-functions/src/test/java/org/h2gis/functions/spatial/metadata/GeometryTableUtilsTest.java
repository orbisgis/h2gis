/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.metadata;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class GeometryTableUtilsTest {

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(GeometryTableUtilsTest.class.getSimpleName());
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
    public void testGeometryMetadataFunctions() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY)");
        st.execute("DROP VIEW IF EXISTS geo_cols; CREATE VIEW geo_cols AS "
                + "SELECT  f_table_catalog, "
                + " f_table_schema, "
                + " f_table_name, "
                + " f_geometry_column, "
                + "1 storage_type, "
                + "t[1] as geometry_type, "
                + "t[2] as coord_dimension, "
                + "t[3] as srid, "
                + "t[4] as type "
                + "FROM (SELECT TABLE_CATALOG f_table_catalog, "
                + "TABLE_SCHEMA f_table_schema, "
                + "TABLE_NAME f_table_name, "
                + "COLUMN_NAME f_geometry_column, "
                + "1 storage_type, FindGeometryMetadata(TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME, "
                + " COLUMN_NAME, COLUMN_TYPE) as t FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TYPE_NAME = 'GEOMETRY'); ");
        ResultSet rs = st.executeQuery("SELECT * FROM geo_cols");
        rs.next();
        assertEquals("DBH2GEOMETRYTABLEUTILSTEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_POINT", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(0, rs.getInt("geometry_type"));
        assertEquals(2, rs.getInt("coord_dimension"));
        assertEquals(0, rs.getInt("srid"));
        assertEquals("GEOMETRY", rs.getString("type"));

        //Alter the geometry
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINT Z, 4326)");
        rs = st.executeQuery("SELECT * FROM geo_cols");
        rs.next();
        assertEquals("DBH2GEOMETRYTABLEUTILSTEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_POINT", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(1001, rs.getInt("geometry_type"));
        assertEquals(3, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("POINTZ", rs.getString("type"));

        st.execute("drop table if exists geo_linestring; CREATE TABLE geo_linestring (the_geom GEOMETRY(LINESTRINGZM, 4326))");
        rs = st.executeQuery("SELECT * FROM geo_cols where f_table_name= 'GEO_LINESTRING' ");
        rs.next();
        assertEquals("DBH2GEOMETRYTABLEUTILSTEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_LINESTRING", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(3002, rs.getInt("geometry_type"));
        assertEquals(4, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("LINESTRINGZM", rs.getString("type"));
    }
   
}
