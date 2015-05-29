/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

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
}
