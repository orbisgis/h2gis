/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.drivers.shp;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.FileUtils;
import org.h2.util.StringUtils;
import org.h2gis.drivers.DriverManager;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
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
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DriverManager(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void readSHPMetaTest() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CALL FILE_TABLE("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'SHPTABLE');");
        // Query declared Table columns
        ResultSet rs = st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = 'SHPTABLE'");
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
        st.execute("CALL FILE_TABLE('"+SHPEngineTest.class.getResource("waternetwork.shp").getPath()+"', 'SHPTABLE');");
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
        connection = SpatialH2UT.openSpatialDataBase(DB_NAME);
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
            connection = SpatialH2UT.openSpatialDataBase(DB_NAME);
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
            connection = SpatialH2UT.openSpatialDataBase(DB_NAME);
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
}
