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

package org.h2gis.utilities;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Test TableLocation
 *
 * @author Nicolas Fortin
 */
public class TableLocationTest {

    @Test
    public void testSplitCatalogSchemaTableName() {
        check("mytable", "", "", "mytable", "\"mytable\"", "\"mytable\"", "mytable");
        check("myschema.mytable", "", "myschema", "mytable", "\"myschema\".\"mytable\"", "\"myschema\".\"mytable\"", "myschema.mytable");
        check("mydb.myschema.mytable", "mydb", "myschema", "mytable", "\"mydb\".\"myschema\".\"mytable\"", "\"mydb\".\"myschema\".\"mytable\"", "mydb.myschema.mytable");
        check(location.toString(), "mydb", "myschema", "mytable", "\"mydb\".\"myschema\".\"mytable\"", "\"mydb\".\"myschema\".\"mytable\"", "mydb.myschema.mytable");
    }

    @Test
    public void testSplitCatalogSchemaTableNameWithQuotes() {
        check("`mytable`", "", "", "mytable", "\"mytable\"", "\"mytable\"", "mytable");
        check("`myschema`.`mytable`", "", "myschema", "mytable", "\"myschema\".\"mytable\"", "\"myschema\".\"mytable\"", "myschema.mytable");
        check("`mydb`.`myschema`.`mytable`", "mydb", "myschema", "mytable", "\"mydb\".\"myschema\".\"mytable\"", "\"mydb\".\"myschema\".\"mytable\"", "mydb.myschema.mytable");
        check("`mydb`.`myschema`.`mytable.hello`", "mydb", "myschema", "mytable.hello", "\"mydb\".\"myschema\".\"mytable.hello\"", "\"mydb\".\"myschema\".\"mytable.hello\"", "mydb.myschema.\"mytable.hello\"");
        check("`mydb`.`my schema`.`my table`", "mydb", "my schema", "my table", "\"mydb\".\"my schema\".\"my table\"", "\"mydb\".\"my schema\".\"my table\"", "mydb.\"my schema\".\"my table\"");
        check(location.toString(), "mydb", "my schema", "my table", "\"mydb\".\"my schema\".\"my table\"", "\"mydb\".\"my schema\".\"my table\"", "mydb.\"my schema\".\"my table\"");
        check("public.MYTABLE", "", "public", "MYTABLE", "\"public\".\"MYTABLE\"", "\"public\".MYTABLE", "public.\"MYTABLE\"");
    }

    @Test
    public void testTableLocationDataBaseType() {
        TableLocation location = TableLocation.parse("MyTable", true);
        assertEquals("",location.getCatalog());
        assertEquals("",location.getSchema());
        assertEquals("MYTABLE", location.getTable());
        assertEquals("\"MYTABLE\"", location.toString());
        assertEquals("MYTABLE", location.toString(true));
        assertEquals("\"MYTABLE\"", location.toString(false));
        location = TableLocation.parse("\"MyTable\"", true);
        assertEquals("",location.getCatalog());
        assertEquals("",location.getSchema());
        assertEquals("MyTable", location.getTable());
        assertEquals("\"MyTable\"", location.toString());
        assertEquals("\"MyTable\"", location.toString(true));
        assertEquals("\"MyTable\"", location.toString(false));
        location = TableLocation.parse("\"MyTable\"", false);
        assertEquals("",location.getCatalog());
        assertEquals("",location.getSchema());
        assertEquals("MyTable", location.getTable());
        assertEquals("\"MyTable\"", location.toString());
        assertEquals("\"MyTable\"", location.toString(true));
        assertEquals("\"MyTable\"", location.toString(false));
    }

    private void check(String input, String catalog, String schema, String table,
                       String toString, String toStringTrue, String toStringFalse) {
        TableLocation location = TableLocation.parse(input);
        assertEquals(catalog,location.getCatalog());
        assertEquals(schema,location.getSchema());
        assertEquals(table, location.getTable());
        assertEquals(toString, location.toString());
        assertEquals(toStringTrue, location.toString(true));
        assertEquals(toStringFalse, location.toString(false));
    }
}
