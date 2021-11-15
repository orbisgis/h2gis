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

package org.h2gis.utilities;

import org.h2gis.utilities.dbtypes.DBTypes;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TableLocation
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class TableLocationTest {

    @Test
    public void testSplitCatalogSchemaTableName() {
        check("mytable", null,
                "", "", "mytable",
                "mytable",
                "\"mytable\"",
                "mytable");
        check("myschema.mytable", null,
                "", "myschema", "mytable",
                "myschema.mytable",
                "\"myschema\".\"mytable\"",
                "myschema.mytable");
        check("mydb.myschema.mytable", null,
                "mydb", "myschema", "mytable",
                "mydb.myschema.mytable",
                "\"mydb\".\"myschema\".\"mytable\"",
                "mydb.myschema.mytable");
        check(TableLocation.parse("mydb.myschema.mytable").toString(), DBTypes.POSTGRESQL,
                "mydb", "myschema", "mytable",
                "mydb.myschema.mytable",
                "\"mydb\".\"myschema\".\"mytable\"",
                "mydb.myschema.mytable");
    }

    @Test
    public void testSplitCatalogSchemaTableNameWithQuotes() {
        check("`mytable`", null,
                "", "", "\"mytable\"",
                "\"mytable\"",
                "\"mytable\"",
                "\"mytable\"");
        check("`myschema`.`mytable`", null,
                "", "\"myschema\"", "\"mytable\"",
                "\"myschema\".\"mytable\"",
                "\"myschema\".\"mytable\"",
                "\"myschema\".\"mytable\"");
        check("`mydb`.`myschema`.`mytable`", null,
                "\"mydb\"", "\"myschema\"", "\"mytable\"",
                "\"mydb\".\"myschema\".\"mytable\"",
                "\"mydb\".\"myschema\".\"mytable\"",
                "\"mydb\".\"myschema\".\"mytable\"");
        check("`mydb`.`myschema`.`mytable.hello`", null,
                "\"mydb\"", "\"myschema\"", "\"mytable.hello\"",
                "\"mydb\".\"myschema\".\"mytable.hello\"",
                "\"mydb\".\"myschema\".\"mytable.hello\"",
                "\"mydb\".\"myschema\".\"mytable.hello\"");
        check("`mydb`.`my schema`.`my table`", null,
                "\"mydb\"", "\"my schema\"", "\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"");
        check(TableLocation.parse("`mydb`.`my schema`.`my table`").toString(), null,
                "\"mydb\"", "\"my schema\"", "\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"");
        check("public.MYTABLE", null,
                "", "public", "MYTABLE",
                "public.MYTABLE",
                "\"public\".MYTABLE",
                "public.\"MYTABLE\"");
    }

    @Test
    public void testTableLocationDataBaseType() {
        check("MyTable", DBTypes.H2,
                "", "", "MYTABLE",
                "MYTABLE",
                "MYTABLE",
                "\"MYTABLE\"");
        check("\"MyTable\"", DBTypes.H2GIS,
                "", "", "\"MyTable\"",
                "\"MyTable\"",
                "\"MyTable\"",
                "\"MyTable\"");
        check("\"MyTable\"", DBTypes.POSTGRESQL,
                "", "", "\"MyTable\"",
                "\"MyTable\"",
                "\"MyTable\"",
                "\"MyTable\"");
    }

    private void check(String input, DBTypes dbTypes, String catalog, String schema, String table,
                       String toString, String toH2, String toPOSTGRESQL) {
        TableLocation location = dbTypes == null ? TableLocation.parse(input) : TableLocation.parse(input, dbTypes);
        assertEquals(catalog,location.getCatalog());
        assertEquals(schema,location.getSchema());
        assertEquals(table, location.getTable());
        assertEquals(toString, location.toString());
        assertEquals(toH2, location.toString(DBTypes.H2));
        assertEquals(toPOSTGRESQL, location.toString(DBTypes.POSTGRESQL));
    }

    @Test
    public void testEquality() {
        assertEquals(new TableLocation("", "public", "MYTABLE"), new TableLocation("MYTABLE"));
        assertEquals(new TableLocation("DATABASE", "PUBLIC", "MYTABLE"), TableLocation.parse("PUBLIC.MYTABLE"));
        assertEquals(new TableLocation("", "PUBLIC", "MYTABLE"), TableLocation.parse("DATABASE.PUBLIC.MYTABLE"));
        assertNotSame(TableLocation.parse("MYSCHEMA.MYTABLE"), TableLocation.parse("MYTABLE"));
        assertNotSame(TableLocation.parse("MYCATALOG.MYSCHEMA.MYTABLE"), TableLocation.parse("CATALOG2.MYSCHEMA.MYTABLE"));
        assertNotSame(TableLocation.parse("MYSCHEMA.MYTABLE"), TableLocation.parse("PUBLIC.MYTABLE"));
    }

    @Test
    public void testNumber() {
        assertEquals("2015MyTable", new TableLocation("2015MyTable").toString());
        assertEquals("\"2015MYTABLE\"", new TableLocation("2015MYTABLE").toString(DBTypes.H2));
        assertEquals("\"2015mytable\"", new TableLocation("2015mytable").toString(DBTypes.POSTGRESQL));
        assertEquals("MY2015TABLE", new TableLocation("MY2015TABLE").toString(DBTypes.H2));
        assertEquals("my2015table", new TableLocation("my2015table").toString(DBTypes.POSTGRESQL));
    }

    @Test
    public void testDefaultSchema(){
        TableLocation tableLocation = new TableLocation("tata");
        assertEquals("dflt", tableLocation.getSchema("dflt"));
        TableLocation tableLocation2 = new TableLocation("schema", "tata");
        assertNotEquals(tableLocation, tableLocation2);
        tableLocation.setDefaultSchema("schema");
        assertEquals(tableLocation, tableLocation2);
        assertEquals("", TableLocation.parse("test", DBTypes.H2).getSchema());
        assertEquals("public", TableLocation.parse("test", DBTypes.H2).getSchema("public"));
    }

    @Test
    public void testCstrResultSet() throws Exception {
        String dataBaseLocation = new File("target/JDBCUtilitiesTest").getAbsolutePath();
        String databasePath = "jdbc:h2:"+dataBaseLocation;
        File dbFile = new File(dataBaseLocation+".mv.db");
        Class.forName("org.h2.Driver");
        if(dbFile.exists()) {dbFile.delete();}
        // Keep a connection alive to not close the DataBase on each unit test
        Connection connection = DriverManager.getConnection(databasePath,"sa", "");
        connection.createStatement().execute("DROP TABLE IF EXISTS TATA");
        connection.createStatement().execute("CREATE TABLE TATA (id INT, str VARCHAR(100))");
        connection.createStatement().execute("INSERT INTO TATA VALUES (25, 'twenty five')");
        connection.createStatement().execute("INSERT INTO TATA VALUES (6, 'six')");

        ResultSet rs = connection.getMetaData().getTables(null, null, "TATA" , null);
        rs.next();
        TableLocation tableLocation = new TableLocation(rs);
        assertEquals("JDBCUTILITIESTEST.PUBLIC.TATA", tableLocation.toString());
    }

    @Test
    public void testDefaultCatalog(){
        TableLocation tableLocation = new TableLocation("tata");
        assertEquals("dflt", tableLocation.getCatalog("dflt"));
    }

    @Test
    public void testCapsIdentifier(){
        assertEquals("IDENTIFIER", TableLocation.capsIdentifier("identifier", DBTypes.H2GIS));
        assertEquals("identifier", TableLocation.capsIdentifier("identifier", DBTypes.POSTGRESQL));
        assertEquals("identifier", TableLocation.capsIdentifier("identifier", null));
    }

    @Test
    public void testNoTableCstr(){
        assertThrows(IllegalArgumentException.class,() ->
                new TableLocation("catalog", "schema", null));
    }

    @Test
    public void testHashCode(){
        assertEquals(-811763674, new TableLocation("catalog", "schema", "table").hashCode());
    }


    @Test
    public void testSplitTable() {
        assertArrayEquals(new String[]{"", "", "test"}, TableLocation.split("test"));
        assertArrayEquals(new String[]{"", "", "Test"}, TableLocation.split("Test"));
        assertArrayEquals(new String[]{"", "", "\"Test\""}, TableLocation.split("\"Test\""));
        assertArrayEquals(new String[]{"", "schema", "\"Test\""}, TableLocation.split("schema.\"Test\""));
        assertArrayEquals(new String[]{"", "schema", "\"Test.super\""}, TableLocation.split("schema.\"Test.super\""));
    }

    @Test
    public void testParsing(){
        assertEquals("TEST", TableLocation.parse("test", DBTypes.H2).toString());
        assertEquals("\"235TEST\"", TableLocation.parse("235test", DBTypes.H2).toString());
        assertEquals("test", TableLocation.parse("test", DBTypes.POSTGIS).toString());
        assertEquals("test", TableLocation.parse("TEST", DBTypes.POSTGIS).toString());
        assertEquals("\"217test\"", TableLocation.parse("217TEST", DBTypes.POSTGIS).toString());
        assertEquals("\"217TEST\"", TableLocation.parse("\"217TEST\"", DBTypes.POSTGIS).toString());
        assertEquals("\"create\"", TableLocation.parse("CREATE", DBTypes.POSTGIS).toString());
        assertEquals("CATALOG.SCHEMA.\"TABLE\"", TableLocation.parse("caTAlog.schEma.TAbLe", DBTypes.H2GIS).toString());
        assertEquals("catalog.schema.\"table\"", TableLocation.parse("caTAlog.schEma.TAbLe", DBTypes.POSTGIS).toString());
    }
}
