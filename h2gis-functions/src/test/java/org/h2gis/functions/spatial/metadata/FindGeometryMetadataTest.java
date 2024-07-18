/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.metadata;


import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.spatial.properties.ST_SRID;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

//TODO : This test class is mixing test on the 'FindGeometryMetadata' function but also other data in the table 'INFORMATION_SCHEMA.COLUMNS'
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
        ResultSet res = getFullMetaResultSet("GEOTABLE");
        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(0, res.getInt("GEOMETRY_TYPE"));
        assertEquals(2, res.getInt("COORD_DIMENSION"));
        assertEquals(0, res.getObject("SRID"));
        assertEquals("GEOMETRY", res.getString("TYPE"));

        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (the_geom GEOMETRY(POINTZM, 0));" +
                "INSERT INTO geotable values ('SRID=0;POINTZM(1 1 0 5)') ");

        res = getFullMetaResultSet("GEOTABLE");
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

        res = getFullMetaResultSet("GEOTABLE");
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
    public void testFindGeometryMetadataCaseSensitive() throws SQLException {
        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (\"the_geom\" GEOMETRY); ");

        ResultSet res = getFullMetaResultSet("GEOTABLE");
        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("the_geom", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(0, res.getInt("GEOMETRY_TYPE"));
        assertEquals(2, res.getInt("COORD_DIMENSION"));
        assertEquals(0, res.getObject("SRID"));
        assertEquals("GEOMETRY", res.getString("TYPE"));

        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (\"the_geom\" GEOMETRY(POINTZM, 0));" +
                "INSERT INTO geotable values ('SRID=0;POINTZM(1 1 0 5)') ");

        res = getFullMetaResultSet("GEOTABLE");
        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("the_geom", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(3001, res.getInt("GEOMETRY_TYPE"));
        assertEquals(4, res.getInt("COORD_DIMENSION"));
        assertEquals(0, res.getInt("SRID"));
        assertEquals("POINTZM", res.getString("TYPE"));

        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (\"the_geom\" GEOMETRY(POINTZ, 2154));" +
                "INSERT INTO geotable values ('SRID=2154;POINTZ(1 1 0)') ");

        res = getFullMetaResultSet("GEOTABLE");
        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEOTABLE", res.getString("TABLE_NAME"));
        assertEquals("the_geom", res.getString("COLUMN_NAME"));
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
        ResultSet res = getFullMetaResultSet();

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEO_POINT", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(1001, res.getInt("GEOMETRY_TYPE"));
        assertEquals(3, res.getInt("COORD_DIMENSION"));
        assertEquals(4326, res.getInt("SRID"));
        assertEquals("POINTZ", res.getString("TYPE"));
    }

    @Test
    public void testFindGeometryMetadataAlterTableEmpty() throws SQLException {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY);");
        st.execute(" ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZ, 4326);");
        ResultSet res = getFullMetaResultSet();

        assertTrue(res.next());
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", res.getString("TABLE_CATALOG"));
        assertEquals("PUBLIC", res.getString("TABLE_SCHEMA"));
        assertEquals("GEO_POINT", res.getString("TABLE_NAME"));
        assertEquals("THE_GEOM", res.getString("COLUMN_NAME"));
        assertEquals(1, res.getInt("STORAGE_TYPE"));
        assertEquals(1001, res.getInt("GEOMETRY_TYPE"));
        assertEquals(3, res.getInt("COORD_DIMENSION"));
        assertEquals(4326, res.getInt("SRID"));
        assertEquals("POINTZ", res.getString("TYPE"));
    }

    @Test
    public void testCheckSRID() throws SQLException {
        st.execute("DROP TABLE IF EXISTS geotable; CREATE TABLE geotable (the_geom GEOMETRY); ");
        ResultSet res = st.executeQuery(
                "SELECT   "
                        + "GEOMETRY_SRID as srid "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY' and TABLE_NAME='GEOTABLE';");
        assertTrue(res.next());
        assertNull(res.getObject("SRID"));

        res = st.executeQuery(
                "SELECT   "
                        + "CASE WHEN GEOMETRY_SRID IS NULL THEN 0 ELSE GEOMETRY_SRID END as srid "
                        + " FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE DATA_TYPE = 'GEOMETRY' and TABLE_NAME='GEOTABLE';");
        assertTrue(res.next());
        assertEquals(0, res.getObject("SRID"));
    }

    /**
     * Return a {@link ResultSet} with all the data about the tabel 'INFORMATION_SCHEMA.COLUMNS' filtered by the given
     * table name. If the name is null, the results are not filtered.
     *
     * @param tableName Name of the table used to filter the results.
     * @return A {@link ResultSet} with all the data about the tabel 'INFORMATION_SCHEMA.COLUMNS'.
     * @throws SQLException Exception thrown on SQL error while executing the query.
     */
    private ResultSet getFullMetaResultSet(String tableName) throws SQLException {
        return st.executeQuery("SELECT  TABLE_CATALOG f_table_catalog, "
                + " TABLE_SCHEMA f_table_schema, "
                + " TABLE_NAME f_table_name, "
                + " COLUMN_NAME f_geometry_column, "
                + "1 storage_type, "
                + "CAST(FindGeometryMetadata(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME, DATA_TYPE, GEOMETRY_TYPE,GEOMETRY_SRID)[1] AS INTEGER) as geometry_type, "
                + "CAST(FindGeometryMetadata(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME, DATA_TYPE, GEOMETRY_TYPE,GEOMETRY_SRID)[2] AS INTEGER) as coord_dimension, "
                + "CASE WHEN GEOMETRY_SRID IS NULL THEN 0 ELSE GEOMETRY_SRID END as srid, "
                + "FindGeometryMetadata(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME, DATA_TYPE, GEOMETRY_TYPE,GEOMETRY_SRID)[4] as type "
                + " FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE DATA_TYPE = 'GEOMETRY' " +
                (tableName == null ? "" : "and TABLE_NAME='"+tableName+"';"));
    }

    /**
     * Return a {@link ResultSet} with all the data about the tabel 'INFORMATION_SCHEMA.COLUMNS'.
     *
     * @return A {@link ResultSet} with all the data about the tabel 'INFORMATION_SCHEMA.COLUMNS'.
     * @throws SQLException Exception thrown on SQL error while executing the query.
     */
    private ResultSet getFullMetaResultSet() throws SQLException {
        return getFullMetaResultSet(null);
    }
}