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

package org.h2gis.functions.io.shp;

import org.apache.commons.io.FileUtils;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.utilities.*;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Fortin
 */
public class SHPEngineTest {
    private static Connection connection;
    private static final String DB_NAME = "SHPTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void readSHPMetaTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS SHPTABLE");
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'shptable');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'SHPTABLE'")) {
            assertTrue(rs.next());
            assertEquals(H2TableIndex.PK_COLUMN_NAME,rs.getString("COLUMN_NAME"));
            assertEquals("BIGINT",rs.getString("DATA_TYPE"));
            assertTrue(rs.next());
            assertEquals("THE_GEOM",rs.getString("COLUMN_NAME"));
            assertEquals("GEOMETRY",rs.getString("DATA_TYPE"));
            assertTrue(rs.next());
            assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
            assertEquals("CHARACTER VARYING",rs.getString("DATA_TYPE"));
            assertEquals(254,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
            assertTrue(rs.next());
            assertEquals("GID",rs.getString("COLUMN_NAME"));
            assertEquals("BIGINT",rs.getString("DATA_TYPE"));
            assertEquals(64,rs.getInt("NUMERIC_PRECISION"));
            assertTrue(rs.next());
            assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
            assertEquals("DOUBLE PRECISION",rs.getString("DATA_TYPE"));
            assertEquals(20,rs.getInt("DECLARED_NUMERIC_PRECISION"));
        }
        st.execute("drop table shptable");
    }

    @Test
    public void readSHPDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT * FROM shptable")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("gid"));
            assertEquals("river",rs.getString("type_axe"));
            assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))",rs.getObject("the_geom").toString());
        }
        st.execute("drop table shptable");
    }

    @Test
    public void readPartialSHPDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPtable');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT TYPE_AXE, GID, LENGTH FROM SHPTABLE;")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("gid"));
            assertEquals("river",rs.getString("type_axe"));
            assertEquals(9.492402903934545,rs.getDouble("length"), 1e-12);
        }
        st.execute("drop table shptable");
    }

    @Test
    public void testRowIdHiddenColumn() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath() + "', 'SHPTABLE');");
        // Check random access using hidden column _rowid_
        ResultSet rs = st.executeQuery("SELECT _rowid_ FROM shptable");
        try {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("_rowid_"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("_rowid_"));
            assertTrue(rs.next());
            assertEquals(3, rs.getInt("_rowid_"));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT * FROM shptable where _rowid_ = 1");
        try {
            assertTrue(rs.next());
            assertEquals(1,rs.getInt("gid"));
            assertEquals("river", rs.getString("type_axe"));
            assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))", rs.getObject("the_geom").toString());
        } finally {
            rs.close();
        }

        st.execute("drop table shptable");
    }

    @Test
    public void persistenceTest() throws Exception {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM shptable");
        assertTrue(rs.next());
        assertEquals(382, rs.getInt(1));
        connection.close();
        Thread.sleep(50);
        connection = H2GISDBFactory.openSpatialDataBase(DB_NAME);
        st = connection.createStatement();
        rs = st.executeQuery("SELECT COUNT(*) FROM shptable");
        assertTrue(rs.next());
        assertEquals(382, rs.getInt(1));
        st.execute("drop table shptable");
    }

    @Test
    public void readSHPDataTest2() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT the_geom FROM shptable")) {
            double sumLength = 0;
            while(rs.next()) {
                sumLength+=((Geometry)rs.getObject("the_geom")).getLength();
            }
            assertEquals(28469.778049948833, sumLength, 1e-12);
        }
        st.execute("drop table shptable");
    }

    @Test
    public void testReopenMovedShp() throws Exception {
        // Copy file in target
        File src = new File(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        File srcDbf = new File(SHPEngineTest.class.getResource("waternetwork.dbf").getPath());
        File srcShx = new File(SHPEngineTest.class.getResource("waternetwork.shx").getPath());
        File dst = new File("target/waternetwork_dst.shp");
        dst.delete();
        File dstDbf = new File("target/waternetwork_dst.dbf");
        dstDbf.delete();
        File dstShx = new File("target/waternetwork_dst.shx");
        dstShx.delete();
        FileUtils.copyFile(src, dst);
        FileUtils.copyFile(srcDbf, dstDbf);
        FileUtils.copyFile(srcShx, dstShx);
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('" + dst + "', 'SHPTABLE');");
        st.execute("SHUTDOWN");
        // Close database
        connection.close();
        try {
            // Wait a while
            Thread.sleep(1000);
            // Remove temp file
            assertTrue(dst.delete());
            assertTrue(dstDbf.delete());
            assertTrue(dstShx.delete());
            // Reopen it
        } finally {
            connection = H2GISDBFactory.openSpatialDataBase(DB_NAME);
            st = connection.createStatement();
        }
        ResultSet rs = st.executeQuery("SELECT SUM(ST_LENGTH(the_geom)) sumlen FROM shptable");
        try {
            assertTrue(rs.next());
            // The new table should be empty
            assertEquals(0,rs.getDouble("sumlen"),1e-12);
        } finally {
            rs.close();
        }
        // Close again the database
        connection.close();
        try {
            // Wait a while
            Thread.sleep(1000);
            // Reopen it
        } finally {
            connection = H2GISDBFactory.openSpatialDataBase(DB_NAME);
            st = connection.createStatement();
        }
        rs = st.executeQuery("SELECT SUM(ST_LENGTH(the_geom)) sumlen FROM shptable");
        try {
            assertTrue(rs.next());
            // The new table should be empty
            assertEquals(0,rs.getDouble("sumlen"),1e-12);
        } finally {
            rs.close();
        }
        st.execute("drop table if exists shptable");
    }

    @Test
    public void readSHPConstraintTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        assertEquals(GeometryTypeCodes.MULTILINESTRING, GeometryTableUtilities.getMetaData(connection, TableLocation.parse("SHPTABLE", DBTypes.H2GIS), "THE_GEOM").geometryTypeCode);
        st.execute("drop table shptable");
    }

    @Test
    public void readReadPRJWithoutEPSGTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork_without_epsg.shp").getPath()+"', 'SHPTABLE');");
        GeometryMetaData geomMeta = GeometryTableUtilities.getMetaData(connection, TableLocation.parse("SHPTABLE", DBTypes.H2GIS),"THE_GEOM");
        assertEquals(GeometryTypeCodes.MULTILINESTRING, geomMeta.geometryTypeCode);
        assertTrue(geomMeta.getSRID()==0);
        st.execute("drop table shptable");
    }

    @Test
    public void testAddIndexOnTableLink() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS shptable");
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'shptable');");
        String explainWithoutIndex;
        //Spatial index
        ResultSet rs = st.executeQuery("EXPLAIN SELECT * FROM SHPTABLE WHERE THE_GEOM && ST_BUFFER('POINT(183541 2426015)', 15)");
        try{
            assertTrue(rs.next());
            explainWithoutIndex = rs.getString(1);
        } finally {
            rs.close();
        }
        // Query plan test with index
        st.execute("CREATE INDEX ON shptable USING RTREE (the_geom)");
        rs = st.executeQuery("EXPLAIN SELECT * FROM SHPTABLE WHERE THE_GEOM && ST_BUFFER('POINT(183541 2426015)', 15)");
        try{
            assertTrue(rs.next());
            assertNotEquals(explainWithoutIndex, rs.getString(1));
        } finally {
            rs.close();
        }
        // Execute query using index
        rs = st.executeQuery("SELECT PK FROM SHPTABLE WHERE THE_GEOM && ST_BUFFER('POINT(183541 2426015)', 15) ORDER BY PK");
        try{
            assertTrue(rs.next());
            assertEquals(128, rs.getLong(1));
            assertTrue(rs.next());
            assertEquals(326, rs.getLong(1));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
        // Check if the index exists
        assertTrue(hasIndex(connection, TableLocation.parse("SHPTABLE",DBTypes.H2GIS), "the_geom"));
        st.execute("DROP TABLE IF EXISTS shptable");

        // Check if the index exists
        assertFalse(hasIndex(connection, TableLocation.parse("SHPTABLE",DBTypes.H2GIS), "the_geom"));


        //Alphanumeric index
        st.execute("DROP TABLE IF EXISTS shptable");
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'shptable');");

        rs = st.executeQuery("EXPLAIN SELECT * FROM SHPTABLE WHERE gid = 201");
        try{
            assertTrue(rs.next());
            explainWithoutIndex = rs.getString(1);
        } finally {
            rs.close();
        }

        assertFalse(JDBCUtilities.isIndexed(connection, "SHPTABLE", "GID"));
        // Query plan test with index
        st.execute("CREATE INDEX ON shptable(gid)");
        assertTrue(JDBCUtilities.isIndexed(connection, "SHPTABLE", "GID"));

        rs = st.executeQuery("EXPLAIN SELECT * FROM SHPTABLE WHERE gid = 201");
        try{
            assertTrue(rs.next());
            assertNotEquals(explainWithoutIndex, rs.getString(1));
        } finally {
            rs.close();
        }
        // Execute query using index
        rs = st.executeQuery("SELECT PK FROM SHPTABLE WHERE gid = 201 ORDER BY PK");
        try{
            assertTrue(rs.next());
            assertEquals(201, rs.getLong(1));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
        // Check if the index is here
        assertTrue(hasIndex(connection, TableLocation.parse("SHPTABLE",DBTypes.H2GIS), "GID"));

        st.execute("DROP TABLE IF EXISTS shptable");
        // Check if the index has been removed
        assertFalse(hasIndex(connection, TableLocation.parse("SHPTABLE",DBTypes.H2GIS), "GID"));

    }


    /**
     * Check if the column is indexed or not.
     * Cannot check if the index is spatial or not
     * @param connection database connection
     * @param tableLocation input table name
     * @param geometryColumnName  geometry column
     * @return true is the column is indexed
     */
    private static boolean hasIndex(Connection connection, TableLocation tableLocation, String geometryColumnName) throws SQLException {
        String schema = tableLocation.getSchema();
        String tableName = tableLocation.getTable();
        String fieldName = TableLocation.capsIdentifier(geometryColumnName, DBTypes.H2GIS);

        String query  = String.format("SELECT I.INDEX_TYPE_NAME, I.INDEX_CLASS FROM INFORMATION_SCHEMA.INDEXES AS I , " +
                        "(SELECT COLUMN_NAME, TABLE_NAME, TABLE_SCHEMA  FROM " +
                        "INFORMATION_SCHEMA.INDEX_COLUMNS WHERE TABLE_SCHEMA='%s' and TABLE_NAME='%s' AND COLUMN_NAME='%s') AS C " +
                        "WHERE I.TABLE_SCHEMA=C.TABLE_SCHEMA AND I.TABLE_NAME=C.TABLE_NAME and C.COLUMN_NAME='%s'"
                ,schema.isEmpty()?"PUBLIC":schema,tableName, fieldName, fieldName);
        try (ResultSet rs = connection.createStatement().executeQuery(query)) {
            if (rs.next()) {
                return  rs.getString("INDEX_TYPE_NAME").contains("INDEX");
            }
        }
        return false;
    }

    /**
     * Check the call of special case {@link H2TableIndex} with null at first and last
     */
    @Test
    public void readSHPOrderDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT * FROM shptable order by PK limit 8")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(3, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(4, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(5, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(6, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(7, rs.getInt("gid"));
            assertTrue(rs.next());
            assertEquals(8, rs.getInt("gid"));
        }
        st.execute("drop table shptable");
    }

    /**
     * Check the call of special case {@link H2TableIndex} with null at last part only.
     */
    @Test
    public void readSHPFilteredOrderDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        //
        ResultSet rs = st.executeQuery("EXPLAIN SELECT * FROM shptable where PK >=4 order by PK limit 5");
        assertTrue(rs.next());
        assertTrue(rs.getString(1).contains("PUBLIC.\"SHPTABLE.PK_INDEX_1") && rs.getString(1).contains("\": PK >= CAST(4 AS BIGINT)"));
        rs.close();
        // Query declared Table columns
        rs = st.executeQuery("SELECT * FROM shptable where PK >=4 order by PK limit 5");
        assertTrue(rs.next());
        assertEquals(4, rs.getInt("gid"));
        assertTrue(rs.next());
        assertEquals(5, rs.getInt("gid"));
        assertTrue(rs.next());
        assertEquals(6, rs.getInt("gid"));
        assertTrue(rs.next());
        assertEquals(7, rs.getInt("gid"));
        assertTrue(rs.next());
        assertEquals(8, rs.getInt("gid"));
        rs.close();
        st.execute("drop table shptable");
    }

    /**
     * About #806
     * Fix were a pk Index of linked table return a superior cost than spatial index.
     */
    @Test
    public void linkedShpSpatialIndexFlatQueryTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        st.execute("CREATE SPATIAL INDEX SPATIALINDEX ON PUBLIC.SHPTABLE(THE_GEOM);\n");
        try (ResultSet rs = st.executeQuery("EXPLAIN SELECT * FROM " +
                "SHPTABLE ORDER BY PK LIMIT 51;")) {
            assertTrue(rs.next());
            assertTrue(rs.getString(1).contains("PK_INDEX"), "Expected contains PK_INDEX but result is " + rs.getString(1));
        }
    }

    @Test
    public void exportImportEmptyGeometry() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS GEOTABLE");
        stat.execute("create table GEOTABLE(idarea int primary key, the_geom GEOMETRY(POINT))");
        stat.execute("insert into GEOTABLE values(1, 'POINT (0 0)'::GEOMETRY),(2, 'POINT EMPTY'::GEOMETRY);");
        // Create a shape file using table area
        stat.execute("CALL SHPWrite('target/points_withempty.shp', 'GEOTABLE', true)");
        stat.execute("CALL SHPRead('target/points_withempty.shp', 'IMPORT_GEOTABLE')");
        ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_GEOTABLE ORDER BY idarea;");
        res.next();
        assertGeometryEquals("POINT(0 0)", res.getObject(1));
        res.next();
        assertNull(res.getObject(1));
        res.close();
    }
  
    @Test
    public void readSHPWithCPGFileTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("urock_buildings.shp").getPath()+"', 'SHPtable');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT * FROM SHPTABLE where objektiden= '71193131-5b61-4d65-a661-fefb173bfd86';")) {
            assertTrue(rs.next());
            assertEquals("71193131-5b61-4d65-a661-fefb173bfd86", rs.getString("objektiden"));
            assertEquals("Bostad;Småhus friliggande",rs.getString("andamal1"));
        }
        st.execute("drop table shptable");
    }

    @Test
    public void readSHPWithCPGFordeTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL SHPREAD( '"+SHPEngineTest.class.getResource("urock_buildings.shp").getPath()+"', 'SHPtable','windows-1252');");
        try ( // Query declared Table columns
              ResultSet rs = st.executeQuery("SELECT * FROM SHPTABLE where objektiden= '3110e3c4-638c-485e-b5fe-a827ebafd071';")) {
            assertTrue(rs.next());
            assertEquals("3110e3c4-638c-485e-b5fe-a827ebafd071", rs.getString("objektiden"));
            assertEquals("Industri;Tillverkning",rs.getString("andamal1"));
        }
        st.execute("drop table shptable");
    }
}
