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
        TableLocation location = TableLocation.parse("mytable");
        assertEquals("",location.getCatalog());
        assertEquals("",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"mytable\"", location.toString());
        assertEquals("\"mytable\"", location.toString(true));
        assertEquals("mytable", location.toString(false));
        location = TableLocation.parse("myschema.mytable");
        assertEquals("",location.getCatalog());
        assertEquals("myschema",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"myschema\".\"mytable\"", location.toString());
        assertEquals("\"myschema\".\"mytable\"", location.toString(true));
        assertEquals("myschema.mytable", location.toString(false));
        location = TableLocation.parse("mydb.myschema.mytable");
        assertEquals("mydb",location.getCatalog());
        assertEquals("myschema",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"mydb\".\"myschema\".\"mytable\"", location.toString());
        assertEquals("\"mydb\".\"myschema\".\"mytable\"", location.toString(true));
        assertEquals("mydb.myschema.mytable", location.toString(false));
        location = TableLocation.parse(location.toString());
        assertEquals("mydb",location.getCatalog());
        assertEquals("myschema",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"mydb\".\"myschema\".\"mytable\"", location.toString());
        assertEquals("\"mydb\".\"myschema\".\"mytable\"", location.toString(true));
        assertEquals("mydb.myschema.mytable", location.toString(false));
    }

    @Test
    public void testSplitCatalogSchemaTableNameWithQuotes() {
        TableLocation location = TableLocation.parse("`mytable`");
        assertEquals("",location.getCatalog());
        assertEquals("",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"mytable\"", location.toString());
        assertEquals("\"mytable\"", location.toString(true));
        assertEquals("mytable", location.toString(false));
        location = TableLocation.parse("`myschema`.`mytable`");
        assertEquals("",location.getCatalog());
        assertEquals("myschema",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"myschema\".\"mytable\"", location.toString());
        assertEquals("\"myschema\".\"mytable\"", location.toString(true));
        assertEquals("myschema.mytable", location.toString(false));
        location = TableLocation.parse("`mydb`.`myschema`.`mytable`");
        assertEquals("mydb",location.getCatalog());
        assertEquals("myschema",location.getSchema());
        assertEquals("mytable",location.getTable());
        assertEquals("\"mydb\".\"myschema\".\"mytable\"", location.toString());
        assertEquals("\"mydb\".\"myschema\".\"mytable\"", location.toString(true));
        assertEquals("mydb.myschema.mytable", location.toString(false));
        location = TableLocation.parse("`mydb`.`myschema`.`mytable.hello`");
        assertEquals("mydb",location.getCatalog());
        assertEquals("myschema",location.getSchema());
        assertEquals("mytable.hello",location.getTable());
        assertEquals("\"mydb\".\"myschema\".\"mytable.hello\"", location.toString());
        assertEquals("\"mydb\".\"myschema\".\"mytable.hello\"", location.toString(true));
        assertEquals("mydb.myschema.\"mytable.hello\"", location.toString(false));
        location = TableLocation.parse("`mydb`.`my schema`.`my table`");
        assertEquals("mydb",location.getCatalog());
        assertEquals("my schema",location.getSchema());
        assertEquals("my table",location.getTable());
        assertEquals("\"mydb\".\"my schema\".\"my table\"", location.toString());
        assertEquals("\"mydb\".\"my schema\".\"my table\"", location.toString(true));
        assertEquals("mydb.\"my schema\".\"my table\"", location.toString(false));
        location = TableLocation.parse(location.toString());
        assertEquals("mydb",location.getCatalog());
        assertEquals("my schema",location.getSchema());
        assertEquals("my table",location.getTable());
        assertEquals("\"mydb\".\"my schema\".\"my table\"", location.toString());
        assertEquals("\"mydb\".\"my schema\".\"my table\"", location.toString(true));
        assertEquals("mydb.\"my schema\".\"my table\"", location.toString(false));
        location = TableLocation.parse("public.MYTABLE");
        assertEquals("",location.getCatalog());
        assertEquals("public",location.getSchema());
        assertEquals("MYTABLE",location.getTable());
        assertEquals("\"public\".\"MYTABLE\"", location.toString());
        assertEquals("\"public\".MYTABLE", location.toString(true));
        assertEquals("public.\"MYTABLE\"", location.toString(false));
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
}
