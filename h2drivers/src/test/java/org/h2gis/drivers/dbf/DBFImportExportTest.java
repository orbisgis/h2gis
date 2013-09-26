/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
package org.h2gis.drivers.dbf;

import org.h2gis.drivers.dbf.internal.DBFDriver;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class DBFImportExportTest {
    private static Connection connection;
    private static final String DB_NAME = "SHPImportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFRead(), "");
        CreateSpatialExtension.registerFunction(connection.createStatement(), new DBFWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportTableTestGeomEnd() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File dbfFile = new File("target/area_export.dbf");
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, value DOUBLE, descr CHAR(50))");
        stat.execute("insert into area values(1, 4.9406564584124654, 'main area')");
        stat.execute("insert into area values(2, 2.2250738585072009, 'second area')");
        // Create a shape file using table area
        stat.execute("CALL DBFWrite('target/area_export.dbf', 'AREA')");
        // Read this shape file to check values
        assertTrue(dbfFile.exists());
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(dbfFile);
        assertEquals(3, dbfDriver.getFieldCount());
        assertEquals(2, dbfDriver.getRowCount());
        Object[] row = dbfDriver.getRow(0);
        assertEquals(1, row[0]);
        assertEquals(4.9406564584124654, (Double) row[1], 1e-12);
        assertEquals("main area", row[2]);
        row = dbfDriver.getRow(1);
        assertEquals(2, row[0]);
        assertEquals(2.2250738585072009, (Double) row[1], 1e-12);
        assertEquals("second area", row[2]);
    }
}
