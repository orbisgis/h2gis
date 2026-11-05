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
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import org.h2gis.utilities.JDBCUtilities.TABLE_TYPE;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of {@link GeometryTableUtilities#getTablesMetaData} and
 * {@link GeometryTableUtilities#findSRIDFromData}.
 * <p>
 * These methods only need the H2 catalog and the native GEOMETRY type, so the
 * tests run on a plain H2 database with a small SPATIAL_REF_SYS table.
 */
public class TablesMetaDataTest {

    private static Connection connection;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the in-memory database between the tests
        connection = DriverManager.getConnection("jdbc:h2:mem:" + TablesMetaDataTest.class.getSimpleName(), "sa", "");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE PUBLIC.SPATIAL_REF_SYS(SRID INT PRIMARY KEY, AUTH_NAME VARCHAR, "
                    + "AUTH_SRID INT, SRTEXT VARCHAR, PROJ4TEXT VARCHAR)");
            st.execute("INSERT INTO PUBLIC.SPATIAL_REF_SYS VALUES (4326, 'EPSG', 4326, '', ''), "
                    + "(2154, 'EPSG', 2154, '', '')");
            st.execute("CREATE TABLE ROADS(ID BIGINT PRIMARY KEY, NAME VARCHAR, THE_GEOM GEOMETRY(LINESTRING, 2154), "
                    + "G2 GEOMETRY, G3 GEOMETRY(POLYGON Z), G4 GEOMETRY(POINT, 9999))");
            st.execute("CREATE SPATIAL INDEX ON ROADS(THE_GEOM)");
            st.execute("INSERT INTO ROADS VALUES (1, 'a', 'SRID=2154;LINESTRING(0 0, 1 1)', NULL, NULL, NULL)");
            st.execute("INSERT INTO ROADS VALUES (2, 'b', 'SRID=2154;LINESTRING(0 0, 2 2)', 'SRID=4326;POINT(1 1)', NULL, NULL)");
            st.execute("CREATE TABLE COMPOSITE(A INT, B INT, GEOM GEOMETRY(POINT, 4326), PRIMARY KEY (A, B))");
            st.execute("CREATE TABLE TEXTPK(CODE VARCHAR PRIMARY KEY, GEOM GEOMETRY(MULTIPOLYGON, 4326))");
            st.execute("CREATE TABLE NOGEOM(ID INT PRIMARY KEY, V VARCHAR)");
            st.execute("CREATE VIEW V_ROADS AS SELECT ID, THE_GEOM FROM ROADS");
            st.execute("CREATE SCHEMA \"other\"");
            st.execute("CREATE TABLE \"other\".\"lower case\"(\"id\" INT PRIMARY KEY, \"geom\" GEOMETRY(POINT ZM, 4326))");
            st.execute("INSERT INTO \"other\".\"lower case\" VALUES (1, 'SRID=4326;POINT ZM(1 2 3 4)')");
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    private static Map<String, TableMetaData> byName(List<TableMetaData> tables) {
        Map<String, TableMetaData> map = new LinkedHashMap<>();
        for (TableMetaData table : tables) {
            map.put(table.getTableLocation().getSchema() + "." + table.getTableLocation().getTable(), table);
        }
        return map;
    }

    @Test
    public void testAllTables() throws SQLException {
        Map<String, TableMetaData> tables = byName(GeometryTableUtilities.getTablesMetaData(connection));
        assertTrue(tables.containsKey("PUBLIC.NOGEOM"));
        assertTrue(tables.containsKey("PUBLIC.SPATIAL_REF_SYS"));
        assertFalse(tables.get("PUBLIC.NOGEOM").hasGeometry());
        assertEquals("ID", tables.get("PUBLIC.NOGEOM").getPrimaryKeyName());
        for (String name : tables.keySet()) {
            assertFalse(name.startsWith("INFORMATION_SCHEMA."), name);
        }
    }

    @Test
    public void testSpatialOnly() throws SQLException {
        Map<String, TableMetaData> tables = byName(GeometryTableUtilities.getTablesMetaData(connection, true));
        assertFalse(tables.containsKey("PUBLIC.NOGEOM"));
        assertFalse(tables.containsKey("PUBLIC.SPATIAL_REF_SYS"));
        for (TableMetaData table : tables.values()) {
            assertTrue(table.hasGeometry(), table.toString());
        }
    }

    @Test
    public void testGeometryColumns() throws SQLException {
        TableMetaData roads = byName(GeometryTableUtilities.getTablesMetaData(connection, true)).get("PUBLIC.ROADS");
        assertNotNull(roads);
        assertEquals(TABLE_TYPE.TABLE, roads.getTableType());
        assertEquals("ID", roads.getPrimaryKeyName());
        assertEquals(1, roads.getPrimaryKeyIndex());
        assertArrayEquals(new String[]{"THE_GEOM", "G2", "G3", "G4"},
                roads.getGeometryColumns().keySet().toArray(new String[0]));

        TableMetaData.GeometryColumn theGeom = roads.getGeometryColumns().get("THE_GEOM");
        assertEquals(3, theGeom.getIndex());
        assertEquals(GeometryTypeCodes.LINESTRING, theGeom.getMetaData().getGeometryTypeCode());
        assertEquals(2154, theGeom.getSRID());
        assertTrue(theGeom.isSRIDDeclared());
        assertEquals("EPSG", theGeom.getAuthorityName());
        assertEquals(2154, theGeom.getAuthoritySRID());
        assertTrue(theGeom.isSpatialIndexed());

        // no declared type and SRID: nothing is read from the data
        TableMetaData.GeometryColumn g2 = roads.getGeometryColumns().get("G2");
        assertEquals(GeometryTypeCodes.GEOMETRY, g2.getMetaData().getGeometryTypeCode());
        assertEquals(0, g2.getSRID());
        assertFalse(g2.isSRIDDeclared());
        assertNull(g2.getAuthorityName());
        assertFalse(g2.isSpatialIndexed());

        TableMetaData.GeometryColumn g3 = roads.getGeometryColumns().get("G3");
        assertEquals(GeometryTypeCodes.POLYGONZ, g3.getMetaData().getGeometryTypeCode());
        assertTrue(g3.getMetaData().hasZ());
        assertFalse(g3.getMetaData().hasM());
        assertEquals(3, g3.getMetaData().getDimension());

        // declared SRID unknown in SPATIAL_REF_SYS
        TableMetaData.GeometryColumn g4 = roads.getGeometryColumns().get("G4");
        assertEquals(9999, g4.getSRID());
        assertTrue(g4.isSRIDDeclared());
        assertNull(g4.getAuthorityName());
        assertEquals(0, g4.getAuthoritySRID());
    }

    @Test
    public void testPrimaryKeys() throws SQLException {
        Map<String, TableMetaData> tables = byName(GeometryTableUtilities.getTablesMetaData(connection, true));
        // a primary key of several columns or of a non integer type is not a FID
        assertNull(tables.get("PUBLIC.COMPOSITE").getPrimaryKeyName());
        assertEquals(0, tables.get("PUBLIC.COMPOSITE").getPrimaryKeyIndex());
        assertNull(tables.get("PUBLIC.TEXTPK").getPrimaryKeyName());
    }

    @Test
    public void testView() throws SQLException {
        TableMetaData view = byName(GeometryTableUtilities.getTablesMetaData(connection, true)).get("PUBLIC.V_ROADS");
        assertNotNull(view);
        assertEquals(TABLE_TYPE.VIEW, view.getTableType());
        assertNull(view.getPrimaryKeyName());
        TableMetaData.GeometryColumn theGeom = view.getGeometryColumns().get("THE_GEOM");
        assertEquals(GeometryTypeCodes.LINESTRING, theGeom.getMetaData().getGeometryTypeCode());
        assertEquals(2154, theGeom.getSRID());
        assertFalse(theGeom.isSpatialIndexed());
    }

    @Test
    public void testQuotedNames() throws SQLException {
        TableMetaData table = byName(GeometryTableUtilities.getTablesMetaData(connection, true)).get("other.lower case");
        assertNotNull(table);
        assertEquals("id", table.getPrimaryKeyName());
        TableMetaData.GeometryColumn geom = table.getGeometryColumns().get("geom");
        assertEquals(GeometryTypeCodes.POINTZM, geom.getMetaData().getGeometryTypeCode());
        assertTrue(geom.getMetaData().hasZ());
        assertTrue(geom.getMetaData().hasM());
        assertEquals(4326, geom.getSRID());
        assertEquals(4326, GeometryTableUtilities.findSRIDFromData(connection, table.getTableLocation(), "geom"));
    }

    @Test
    public void testPatterns() throws SQLException {
        Map<String, TableMetaData> other = byName(GeometryTableUtilities.getTablesMetaData(connection, "other", null, false));
        assertArrayEquals(new String[]{"other.lower case"}, other.keySet().toArray(new String[0]));

        Map<String, TableMetaData> roads = byName(GeometryTableUtilities.getTablesMetaData(connection, "PUBLIC", "%ROADS", true));
        assertArrayEquals(new String[]{"PUBLIC.ROADS", "PUBLIC.V_ROADS"}, roads.keySet().toArray(new String[0]));

        // patterns are case sensitive
        assertTrue(GeometryTableUtilities.getTablesMetaData(connection, "public", null, false).isEmpty());
        assertTrue(GeometryTableUtilities.getTablesMetaData(connection, null, "NOPE%", false).isEmpty());
    }

    @Test
    public void testFindSRIDFromData() throws SQLException {
        TableLocation roads = new TableLocation(null, "PUBLIC", "ROADS", DBTypes.H2);
        // the first non null geometry gives the SRID
        assertEquals(4326, GeometryTableUtilities.findSRIDFromData(connection, roads, "G2"));
        // only null geometries
        assertEquals(0, GeometryTableUtilities.findSRIDFromData(connection, roads, "G3"));
        assertEquals(2154, GeometryTableUtilities.findSRIDFromData(connection, roads, "THE_GEOM"));
    }

    @Test
    public void testWithoutSpatialRefSys() throws SQLException {
        try (Connection h2 = DriverManager.getConnection("jdbc:h2:mem:" + TablesMetaDataTest.class.getSimpleName() + "_nosrs", "sa", "");
             Statement st = h2.createStatement()) {
            st.execute("CREATE TABLE P(ID INT PRIMARY KEY, G GEOMETRY(POINT, 4326))");
            List<TableMetaData> tables = GeometryTableUtilities.getTablesMetaData(h2);
            assertEquals(1, tables.size());
            TableMetaData.GeometryColumn g = tables.get(0).getGeometryColumns().get("G");
            assertEquals(4326, g.getSRID());
            assertNull(g.getAuthorityName());
        }
    }
}