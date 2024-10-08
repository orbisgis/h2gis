/*
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
package org.h2gis.functions.io.json;

import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Erwan Bocher
 */
public class JsonImportExportTest {
    
     private static Connection connection;
    private static final String DB_NAME = "JsonExportTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new JsonWrite(), "");
        
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }
    
    @Test
    public void testWriteJson() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes INTEGER ARRAY[4])");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', ARRAY[10000, 20000, 30000, 10000])");
             stat.execute("CALL JSONWrite('target/result.json', 'TABLE_POINT', true);");
             String result = new String( Files.readAllBytes(Paths.get("target/result.json")));
             assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",result);
         }
    }
    
    @Test
    public void testWriteResultSetJson() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes  INTEGER ARRAY[4])");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)',ARRAY[10000, 20000, 30000, 10000])");
             ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINT");
             JsonWriteDriver jsonDriver = new JsonWriteDriver(connection);
             jsonDriver.write(new EmptyProgressVisitor(), rs, new File("target/result.json"), true);
             String result = new String( Files.readAllBytes(Paths.get("target/result.json")));
             assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",result);
         }
    }

    @Test
    public void testWriteResultSetJsonGZ() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
            stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes  INTEGER ARRAY[4])");
            stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', ARRAY[10000, 20000, 30000, 10000])");
            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINT");
            JsonWriteDriver jsonDriver = new JsonWriteDriver(connection);
            jsonDriver.write(new EmptyProgressVisitor(), rs, new File("target/result.gz"), true);
            File outpuFile = new File("target/result.gz");
            assertTrue(outpuFile.exists());
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(outpuFile));
            InputStreamReader reader = new InputStreamReader(gzis);
            BufferedReader in = new BufferedReader(reader);
            assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",in.readLine());
            gzis.close();
        }
    }
    
    @Test
    public void testWriteQueryJson() throws Exception {
         try (Statement stat = connection.createStatement()) {
             stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
             stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT), codes  INTEGER ARRAY[4])");
             stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', ARRAY[10000, 20000, 30000, 10000]),(2, 'POINT(12 200)', ARRAY[10000, 20000, 30000, 10000])");
             stat.execute("CALL JSONWrite('target/result.json', '(SELECT * FROM TABLE_POINT WHERE idarea=1)', true);");
             String result = new String( Files.readAllBytes(Paths.get("target/result.json")));
             assertEquals("{\"IDAREA\":1,\"THE_GEOM\":\"POINT (1 2)\",\"CODES\":[10000,20000,30000,10000]}",result);
         }
    }
    
    @Test
    public void testWriteBadEncoding() {
        assertThrows(SQLException.class, () -> {
            try (Statement stat = connection.createStatement()) {
                stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
                stat.execute("create table TABLE_POINT(idarea int primary key, the_geom POINT, codes  INTEGER ARRAY[4])");
                stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)', ARRAY[10000, 20000, 30000, 10000]),(2, 'POINT(12 200)', ARRAY[10000, 20000, 30000, 10000])");
                stat.execute("CALL JSONWrite('target/result.json', '(SELECT * FROM TABLE_POINT WHERE idarea=1)', 'CP52', true);");
            }
        });
    }
    
    @Test
    public void testSelectWriteReadJsonLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL JsonWrite('target/lines.json', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', true);");
            String result = new String( Files.readAllBytes(Paths.get("target/lines.json")));
            assertEquals("{\"THE_GEOM\":\"LINESTRING (1 10, 20 15)\",\"ID\":2}",result);
        }
    }
    
    @Test
    public void testSelectWrite() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL JsonWrite('target/lines.json', '(SELECT ST_GEOMFROMTEXT(''LINESTRING(1 10, 20 15)'', 4326) as the_geom)', true);");
            String result = new String( Files.readAllBytes(Paths.get("target/lines.json")));
            assertEquals("{\"THE_GEOM\":\"SRID=4326;LINESTRING (1 10, 20 15)\"}",result);
        }
    }
    
}
