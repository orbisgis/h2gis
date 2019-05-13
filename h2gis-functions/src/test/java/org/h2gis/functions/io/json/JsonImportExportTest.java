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
package org.h2gis.functions.io.json;

import java.io.File;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.api.EmptyProgressVisitor;

/**
 *
 * @author Erwan Bocher
 */
public class JsonImportExportTest {
    
     private static Connection connection;
    private static final String DB_NAME = "JsonExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new JsonWrite(), "");
        
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }
    
    @Test
    public void testWriteJson() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes  ARRAY)");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', (10000, 20000, 30000, 10000))");
             stat.execute("CALL JSONWrite('target/result.json', 'TABLE_POINT');");
             String result = new String( Files.readAllBytes(Paths.get("target/result.json")));
             Assert.assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",result);
         }
    }
    
    @Test
    public void testWriteResultSetJson() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes  ARRAY)");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', (10000, 20000, 30000, 10000))");
             ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINT");
             JsonWriteDriver jsonWriteDriver = new JsonWriteDriver(connection);
             jsonWriteDriver.write(new EmptyProgressVisitor(), rs, new File("target/result.json"));
             String result = new String( Files.readAllBytes(Paths.get("target/result.json")));
             Assert.assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",result);
         }
    }
    
    @Test
    public void testWriteQueryJson() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes  ARRAY)");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', (10000, 20000, 30000, 10000)),(2, 'POINT(12 200)', (10000, 20000, 30000, 10000))");
             stat.execute("CALL JSONWrite('target/result.json', '(SELECT * FROM TABLE_POINT WHERE idarea=1)');");
             String result = new String( Files.readAllBytes(Paths.get("target/result.json")));
             Assert.assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",result);
         }
    }
    
    @Test(expected = SQLException.class)
    public void testWriteBadEncoding() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom POINT, codes  ARRAY)");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', (10000, 20000, 30000, 10000)),(2, 'POINT(12 200)', (10000, 20000, 30000, 10000))");
             stat.execute("CALL JSONWrite('target/result.json', '(SELECT * FROM TABLE_POINT WHERE idarea=1)', 'CP52');");
         }
    }
    
}
