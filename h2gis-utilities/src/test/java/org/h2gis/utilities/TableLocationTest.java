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

import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.Assert.*;
import static org.locationtech.jts.util.Assert.shouldNeverReachHere;

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
                "\"mytable\"",
                "\"mytable\"",
                "mytable");
        check("myschema.mytable", null,
                "", "myschema", "mytable",
                "\"myschema\".\"mytable\"",
                "\"myschema\".\"mytable\"",
                "myschema.mytable");
        check("mydb.myschema.mytable", null,
                "mydb", "myschema", "mytable",
                "\"mydb\".\"myschema\".\"mytable\"",
                "\"mydb\".\"myschema\".\"mytable\"",
                "mydb.myschema.mytable");
        check(TableLocation.parse("mydb.myschema.mytable").toString(), null,
                "mydb", "myschema", "mytable",
                "\"mydb\".\"myschema\".\"mytable\"",
                "\"mydb\".\"myschema\".\"mytable\"",
                "mydb.myschema.mytable");
    }

    @Test
    public void testSplitCatalogSchemaTableNameWithQuotes() {
        check("`mytable`", null,
                "", "", "mytable",
                "\"mytable\"",
                "\"mytable\"",
                "mytable");
        check("`myschema`.`mytable`", null,
                "", "myschema", "mytable",
                "\"myschema\".\"mytable\"",
                "\"myschema\".\"mytable\"",
                "myschema.mytable");
        check("`mydb`.`myschema`.`mytable`", null,
                "mydb", "myschema", "mytable",
                "\"mydb\".\"myschema\".\"mytable\"",
                "\"mydb\".\"myschema\".\"mytable\"",
                "mydb.myschema.mytable");
        check("`mydb`.`myschema`.`mytable.hello`", null,
                "mydb", "myschema", "mytable.hello",
                "\"mydb\".\"myschema\".\"mytable.hello\"",
                "\"mydb\".\"myschema\".\"mytable.hello\"",
                "mydb.myschema.\"mytable.hello\"");
        check("`mydb`.`my schema`.`my table`", null,
                "mydb", "my schema", "my table",
                "\"mydb\".\"my schema\".\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"",
                "mydb.\"my schema\".\"my table\"");
        check(TableLocation.parse("`mydb`.`my schema`.`my table`").toString(), null,
                "mydb", "my schema", "my table",
                "\"mydb\".\"my schema\".\"my table\"",
                "\"mydb\".\"my schema\".\"my table\"",
                "mydb.\"my schema\".\"my table\"");
        check("public.MYTABLE", null,
                "", "public", "MYTABLE",
                "\"public\".\"MYTABLE\"",
                "\"public\".MYTABLE",
                "public.\"MYTABLE\"");
    }

    @Test
    public void testTableLocationDataBaseType() {
        check("MyTable", true,
                "", "", "MYTABLE",
                "\"MYTABLE\"",
                "MYTABLE",
                "\"MYTABLE\"");
        check("\"MyTable\"", true,
                "", "", "MyTable",
                "\"MyTable\"",
                "\"MyTable\"",
                "\"MyTable\"");
        check("\"MyTable\"", false,
                "", "", "MyTable",
                "\"MyTable\"",
                "\"MyTable\"",
                "\"MyTable\"");
    }

    private void check(String input, Boolean isH2, String catalog, String schema, String table,
                       String toString, String toStringTrue, String toStringFalse) {
        TableLocation location = isH2 == null ? TableLocation.parse(input) : TableLocation.parse(input, isH2);
        assertEquals(catalog,location.getCatalog());
        assertEquals(schema,location.getSchema());
        assertEquals(table, location.getTable());
        assertEquals(toString, location.toString());
        assertEquals(toStringTrue, location.toString(true));
        assertEquals(toStringFalse, location.toString(false));
    }

    @Test
    public void testEquality() {
        assertEquals(new TableLocation("", "PUBLIC", "MYTABLE"), new TableLocation("MYTABLE"));
        assertEquals(new TableLocation("DATABASE", "PUBLIC", "MYTABLE"), TableLocation.parse("PUBLIC.MYTABLE"));
        assertEquals(new TableLocation("", "PUBLIC", "MYTABLE"), TableLocation.parse("DATABASE.PUBLIC.MYTABLE"));
        assertNotSame(TableLocation.parse("MYSCHEMA.MYTABLE"), TableLocation.parse("MYTABLE"));
        assertNotSame(TableLocation.parse("MYCATALOG.MYSCHEMA.MYTABLE"), TableLocation.parse("CATALOG2.MYSCHEMA.MYTABLE"));
        assertNotSame(TableLocation.parse("MYSCHEMA.MYTABLE"), TableLocation.parse("PUBLIC.MYTABLE"));
    }

    @Test
    public void testNumber() {
        assertEquals("\"2015MyTable\"", new TableLocation("2015MyTable").toString());
        assertEquals("\"2015MYTABLE\"", new TableLocation("2015MYTABLE").toString(true));
        assertEquals("\"2015mytable\"", new TableLocation("2015mytable").toString(false));
        assertEquals("MY2015TABLE", new TableLocation("MY2015TABLE").toString(true));
        assertEquals("my2015table", new TableLocation("my2015table").toString(false));
    }

    @Test
    public void testDefaultSchema(){
        TableLocation tableLocation = new TableLocation("tata");
        assertEquals("dflt", tableLocation.getSchema("dflt"));
        TableLocation tableLocation2 = new TableLocation("schema", "tata");
        assertNotEquals(tableLocation, tableLocation2);
        tableLocation.setDefaultSchema("schema");
        assertEquals(tableLocation, tableLocation2);
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
        assertEquals("\"JDBCUTILITIESTEST\".\"PUBLIC\".\"TATA\"", tableLocation.toString());
    }

    @Test
    public void testDefaultCatalog(){
        TableLocation tableLocation = new TableLocation("tata");
        assertEquals("dflt", tableLocation.getCatalog("dflt"));
    }

    @Test
    public void testCapsIdentifier(){
        assertEquals("IDENTIFIER", TableLocation.capsIdentifier("identifier", true));
        assertEquals("identifier", TableLocation.capsIdentifier("identifier", false));
        assertEquals("identifier", TableLocation.capsIdentifier("identifier", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoTableCstr(){
        new TableLocation("catalog", "schema", null);
        shouldNeverReachHere();
    }

    @Test
    public void testHashCode(){
        assertEquals(-811763674, new TableLocation("catalog", "schema", "table").hashCode());
    }
}
