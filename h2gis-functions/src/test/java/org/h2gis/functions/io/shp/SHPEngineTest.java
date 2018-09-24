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

package org.h2gis.functions.io.shp;

import org.locationtech.jts.geom.Geometry;
import org.apache.commons.io.FileUtils;
import org.h2.util.StringUtils;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class SHPEngineTest {
    private static Connection connection;
    private static final String DB_NAME = "SHPTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new DriverManager(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void readSHPMetaTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'shptable');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'SHPTABLE'");
        assertTrue(rs.next());
        assertEquals(H2TableIndex.PK_COLUMN_NAME,rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT",rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("THE_GEOM",rs.getString("COLUMN_NAME"));
        assertEquals("GEOMETRY",rs.getString("TYPE_NAME"));
        assertTrue(rs.next());
        assertEquals("TYPE_AXE",rs.getString("COLUMN_NAME"));
        assertEquals("CHAR",rs.getString("TYPE_NAME"));
        assertEquals(254,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        assertTrue(rs.next());
        assertEquals("GID",rs.getString("COLUMN_NAME"));
        assertEquals("BIGINT",rs.getString("TYPE_NAME"));
        assertEquals(18,rs.getInt("NUMERIC_PRECISION"));
        assertTrue(rs.next());
        assertEquals("LENGTH",rs.getString("COLUMN_NAME"));
        assertEquals("DOUBLE",rs.getString("TYPE_NAME"));
        assertEquals(20,rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
        rs.close();
        st.execute("drop table shptable");
    }

    @Test
    public void readSHPDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM shptable");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("gid"));
        assertEquals("river",rs.getString("type_axe"));
        assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))",rs.getObject("the_geom").toString());
        rs.close();
        st.execute("drop table shptable");
    }

    @Test
    public void readPartialSHPDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPtable');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT TYPE_AXE, GID, LENGTH FROM SHPTABLE;");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("gid"));
        assertEquals("river",rs.getString("type_axe"));
        assertEquals(9.492402903934545,rs.getDouble("length"), 1e-12);
        rs.close();
        st.execute("drop table shptable");
    }

    @Test
    public void testRowIdHiddenColumn() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
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
            assertEquals(1, rs.getInt("gid"));
            assertEquals("river",rs.getString("type_axe"));
            assertEquals("MULTILINESTRING ((183299.71875 2425074.75, 183304.828125 2425066.75))",rs.getObject("the_geom").toString());
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
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT the_geom FROM shptable");
        double sumLength = 0;
        while(rs.next()) {
            sumLength+=((Geometry)rs.getObject("the_geom")).getLength();
        }
        assertEquals(28469.778049948833, sumLength, 1e-12);
        rs.close();
        st.execute("drop table shptable");
    }

    @Test
    public void testReopenMovedShp() throws Exception {
        // Copy file in target
        File src = new File(SHPEngineTest.class.getResource("waternetwork.shp").getPath());
        File srcDbf = new File(SHPEngineTest.class.getResource("waternetwork.dbf").getPath());
        File srcShx = new File(SHPEngineTest.class.getResource("waternetwork.shx").getPath());
        File tmpFile = File.createTempFile("waternetwork","");
        File dst = new File(tmpFile + ".shp");
        File dstDbf = new File(tmpFile + ".dbf");
        File dstShx = new File(tmpFile + ".shx");
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
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryType(connection, TableLocation.parse("SHPTABLE"), ""));
        st.execute("drop table shptable");
    }

    @Test
    public void testAddIndexOnTableLink() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS shptable");
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'shptable');");
        String explainWithoutIndex;
        ResultSet rs = st.executeQuery("EXPLAIN SELECT * FROM SHPTABLE WHERE THE_GEOM && ST_BUFFER('POINT(183541 2426015)', 15)");
        try{
            assertTrue(rs.next());
            explainWithoutIndex = rs.getString(1);
        } finally {
            rs.close();
        }
        // Query plan test with index
        st.execute("CREATE SPATIAL INDEX ON shptable(the_geom)");
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
        // Check if the index is here
        rs = st.executeQuery("select * from INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = 'SHPTABLE' and COLUMN_NAME='THE_GEOM'");
        try {
            assertTrue(rs.next());
            assertEquals("org.h2.index.SpatialTreeIndex", rs.getString("INDEX_CLASS"));
        } finally {
            rs.close();
        }
        st.execute("DROP TABLE IF EXISTS shptable");
        // Check if the index has been removed
        rs = st.executeQuery("select * from INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = 'SHPTABLE' and COLUMN_NAME='THE_GEOM'");
        try {
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
    }

    /**
     * Check the call of special case {@link H2TableIndex#find(org.h2.engine.Session, org.h2.result.SearchRow, org.h2.result.SearchRow)} with null at first and last
     * @throws SQLException
     */
    @Test
    public void readSHPOrderDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM shptable order by PK limit 8");
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
        rs.close();
        st.execute("drop table shptable");
    }

    /**
     * Check the call of special case {@link H2TableIndex#find(org.h2.engine.Session, org.h2.result.SearchRow, org.h2.result.SearchRow)} with null at last part only.
     * @throws SQLException
     */
    @Test
    public void readSHPFilteredOrderDataTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table if exists shptable");
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
        //
        ResultSet rs = st.executeQuery("EXPLAIN SELECT * FROM shptable where PK >=4 order by PK limit 5");
        assertTrue(rs.next());
        Assert.assertThat(rs.getString(1), CoreMatchers.containsString("PUBLIC.\"SHPTABLE.PK_INDEX_1\": PK >= 4"));
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
     * @throws SQLException
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
            assertTrue( "Expected contains PK_INDEX but result is " + rs.getString(1) , rs.getString(1).contains("PK_INDEX"));
        }
    }
}
