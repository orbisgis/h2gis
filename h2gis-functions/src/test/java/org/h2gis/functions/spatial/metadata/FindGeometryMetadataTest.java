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
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.spatial.geometry.DummySpatialFunction;
import org.h2gis.functions.spatial.properties.ST_SRID;
import org.h2gis.utilities.JDBCUtilities;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FindGeometryMetadataTest {

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(FindGeometryMetadataTest.class.getSimpleName(), false);
        H2GISFunctions.registerFunction(connection.createStatement(), new FindGeometryMetadata(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_SRID(), "");
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
    public void testFindGeometryMetadata() throws SQLException {
        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (the_geom GEOMETRY); ");
        //st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (the_geom GEOMETRY(POINT, 2154)); ");
        ResultSet res = st.executeQuery(
                "SELECT  TABLE_CATALOG f_table_catalog, "
                        + " TABLE_SCHEMA f_table_schema, "
                        + " TABLE_NAME f_table_name, "
                        + " COLUMN_NAME f_geometry_column, "
                        + "1 storage_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[1]:: int as geometry_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[2]:: int as coord_dimension, "
                        + "GEOMETRY_SRID:: int as srid, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[3] as type "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY';");

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(0, res.getInt("GEOMETRY_TYPE"));
        assertEquals(2, res.getInt("COORD_DIMENSION"));
        assertEquals(0, res.getInt("SRID"));
        assertEquals("GEOMETRY", res.getString("TYPE"));

        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (the_geom GEOMETRY(POINTZM, 0));" +
                "INSERT INTO geotable values ('SRID=0;POINTZ(1 1 0 5)') ");
        res = st.executeQuery(
                "SELECT  TABLE_CATALOG f_table_catalog, "
                        + " TABLE_SCHEMA f_table_schema, "
                        + " TABLE_NAME f_table_name, "
                        + " COLUMN_NAME f_geometry_column, "
                        + "1 storage_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[1]:: int as geometry_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[2]:: int as coord_dimension, "
                        + "GEOMETRY_SRID:: int as srid, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[3] as type "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY';");

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(3001, res.getInt("GEOMETRY_TYPE"));
        assertEquals(4, res.getInt("COORD_DIMENSION"));
        assertEquals(0, res.getInt("SRID"));
        assertEquals("POINTZM", res.getString("TYPE"));

        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (the_geom GEOMETRY(POINTZ, 2154));" +
                "INSERT INTO geotable values ('SRID=2154;POINTZ(1 1 0)') ");

        res = st.executeQuery(
                "SELECT  TABLE_CATALOG f_table_catalog, "
                        + " TABLE_SCHEMA f_table_schema, "
                        + " TABLE_NAME f_table_name, "
                        + " COLUMN_NAME f_geometry_column, "
                        + "1 storage_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[1]:: int as geometry_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[2]:: int as coord_dimension, "
                        + "GEOMETRY_SRID:: int as srid, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[3] as type "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY';");

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(1001, res.getInt("GEOMETRY_TYPE"));
        assertEquals(3, res.getInt("COORD_DIMENSION"));
        assertEquals(2154, res.getInt("SRID"));
        assertEquals("POINTZ", res.getString("TYPE"));
    }

    @Test
    public void testFindGeometryMetadataAlterTable() throws SQLException {
        st.execute("drop table if exists geo_point;\n" +
                "CREATE TABLE geo_point (the_geom GEOMETRY);\n" +
                "ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZ, 4326);\n" +
                "INSERT INTO GEO_POINT values ('SRID=4326;POINTZ(1 1 0)');");
        ResultSet res = st.executeQuery(
                "SELECT  TABLE_CATALOG f_table_catalog, "
                        + " TABLE_SCHEMA f_table_schema, "
                        + " TABLE_NAME f_table_name, "
                        + " COLUMN_NAME f_geometry_column, "
                        + "1 storage_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[1]:: int as geometry_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[2]:: int as coord_dimension, "
                        + "GEOMETRY_SRID:: int as srid, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[3] as type "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY';");

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEO_POINT", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(1, res.getInt("GEOMETRY_TYPE"));
        assertEquals(3, res.getInt("COORD_DIMENSION"));
        assertEquals(4326, res.getInt("SRID"));
        assertEquals("POINTZ", res.getString("TYPE"));
    }

    @Test
    public void testFindGeometryMetadataAlterTableEmpty() throws SQLException {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY);");
        st.execute(" ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZ, 4326);");
        ResultSet res = st.executeQuery(
                "SELECT  TABLE_CATALOG f_table_catalog, "
                        + " TABLE_SCHEMA f_table_schema, "
                        + " TABLE_NAME f_table_name, "
                        + " COLUMN_NAME f_geometry_column, "
                        + "1 storage_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[1]:: int as geometry_type, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[2]:: int as coord_dimension, "
                        + "GEOMETRY_SRID:: int as srid, "
                        + "FindGeometryMetadata(DATA_TYPE,GEOMETRY_TYPE)[3] as type "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY';");

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEO_POINT", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(1, res.getInt("GEOMETRY_TYPE"));
        assertEquals(3, res.getInt("COORD_DIMENSION"));
        assertEquals(4326, res.getInt("SRID"));
        assertEquals("POINTZ", res.getString("TYPE"));
    }
}