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


package org.h2gis.functions.io.tsv;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class TSVDriverTest {

    private static Connection connection;
    private static final String DB_NAME = "TSVImportExportTest";
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new TSVRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new TSVWrite(), "");
        
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }
    
    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }
    
    @Test
    public void testDriverManager() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        File tsvFile = new File("target/area éxport.tsv");
        DriverFunction exp = new TSVDriverFunction();
        exp.exportTable(connection, "AREA", tsvFile, new EmptyProgressVisitor());
        stat.execute("DROP TABLE IF EXISTS mytsv");
        exp.importFile(connection, "MYTSV", tsvFile, new EmptyProgressVisitor());
        ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from mytsv");
        try {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        } finally {
            rs.close();
        }
    }
    
    @Test
    public void testWriteRead() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File tsvFile = new File("target/mytsv_export.tsv");
        stat.execute("DROP TABLE IF EXISTS myTSV");
        stat.execute("create table myTSV(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into myTSV values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into myTSV values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        stat.execute("CALL TSVWrite('target/mytsv_export.tsv', 'myTSV')");
        assertTrue(tsvFile.exists());
        stat.execute("CALL TSVRead('target/mytsv_export.tsv', 'TSV_IMPORT');");
        ResultSet rs = stat.executeQuery("select SUM(ST_AREA(the_geom::GEOMETRY)) from TSV_IMPORT");
        try {
            assertTrue(rs.next());
            assertEquals(20000, rs.getDouble(1), 1e-6);
        } finally {
            rs.close();
        }

    }
    
    @Test
    public void testWriteReadEmptyTable() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS empty_table");
        stat.execute("create table empty_table()");
        stat.execute("CALL TSVWrite('target/empty_table.tsv', 'empty_table');");
        stat.execute("CALL TSVRead('target/empty_table.tsv', 'empty_table_read');");
        ResultSet res = stat.executeQuery("SELECT * FROM empty_table_read;");
        ResultSetMetaData rsmd = res.getMetaData();
        assertTrue(rsmd.getColumnCount()==0);
        assertTrue(!res.next());
        stat.close();
    }
    
}
