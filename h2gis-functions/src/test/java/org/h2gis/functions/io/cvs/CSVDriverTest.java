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

package org.h2gis.functions.io.cvs;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.csv.CSVDriverFunction;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class CSVDriverTest {    
    
    private static Connection connection;
    private static final String DB_NAME = "CSVImportExportTest";
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
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
        // Export in target with special chars
        File csvFile = new File("target/area éxport.csv");
        DriverFunction exp = new CSVDriverFunction();
        exp.exportTable(connection, "AREA", csvFile,new EmptyProgressVisitor());
        stat.execute("DROP TABLE IF EXISTS mycsv");
        exp.importFile(connection, "MYCSV", csvFile, new EmptyProgressVisitor());
        try (ResultSet rs = stat.executeQuery("select SUM(idarea::int) from mycsv")) {
            assertTrue(rs.next());
            assertEquals(3,rs.getDouble(1),1e-2);
        }
    }   
    
    @Test
    public void testDriverOptions() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        // Export in target with special chars
        File csvFile = new File("target/csv_options.csv");
        CSVDriverFunction exp = new CSVDriverFunction();
        exp.exportTable(connection, "AREA", csvFile,new EmptyProgressVisitor(), "fieldSeparator=| fieldDelimiter=,");
        stat.execute("DROP TABLE IF EXISTS mycsv");
        exp.importFile(connection, "MYCSV", csvFile, new EmptyProgressVisitor(), "fieldSeparator=| fieldDelimiter=,");
        try (ResultSet rs = stat.executeQuery("select SUM(idarea::int) from mycsv")) {
            assertTrue(rs.next());
            assertEquals(3,rs.getDouble(1),1e-2);
        }
    }

    @Test
    public void testDriverDeleteTable() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA, MYCSV");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        stat.execute("CREATE TABLE MYCSV(the_geom GEOMETRY, idarea int primary key)");
        // Export in target with special chars
        File csvFile = new File("target/area éxport.csv");
        DriverFunction exp = new CSVDriverFunction();
        exp.exportTable(connection, "AREA", csvFile,new EmptyProgressVisitor());
        exp.importFile(connection, "MYCSV", csvFile, new EmptyProgressVisitor(), true);
        ResultSet rs = stat.executeQuery("select SUM(idarea::int) from mycsv");
        try {
            assertTrue(rs.next());
            assertEquals(3,rs.getDouble(1),1e-2);
        } finally {
            rs.close();
        }
    }
    
     @Test
    public void testDriverQueryOptions() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(the_geom GEOMETRY, idarea int primary key)");
        stat.execute("insert into area values('POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))', 1)");
        stat.execute("insert into area values('POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))', 2)");
        // Export in target with special chars
        File csvFile = new File("target/csv_options.csv");
        CSVDriverFunction exp = new CSVDriverFunction();
        exp.exportTable(connection, "(SELECT * FROM AREA)", csvFile,new EmptyProgressVisitor(), "fieldSeparator=| fieldDelimiter=,");
        stat.execute("DROP TABLE IF EXISTS mycsv");
        exp.importFile(connection, "MYCSV", csvFile, new EmptyProgressVisitor(), "fieldSeparator=| fieldDelimiter=,");
        try (ResultSet rs = stat.executeQuery("select SUM(idarea::int) from mycsv")) {
            assertTrue(rs.next());
            assertEquals(3,rs.getDouble(1),1e-2);
        }
    }
}
