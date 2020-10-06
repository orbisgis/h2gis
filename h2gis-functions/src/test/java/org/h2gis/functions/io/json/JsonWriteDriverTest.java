/*
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

import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class dedicated to the {@link JsonWriteDriver} class.
 *
 * @author Sylvain PALOMINOS (Lab-STICC UBS, Chaire GEOTERA, 2020)
 */
public class JsonWriteDriverTest {

    /**  Test database connection. */
    private static Connection connection;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(
                JsonWriteDriverTest.class.getSimpleName() + "_" + UUID.randomUUID().toString());
        Statement stat = connection.createStatement();
        stat.execute("CREATE TABLE TABLE_POINT(idarea INT PRIMARY KEY, the_geom GEOMETRY(POINT), codes INTEGER ARRAY[4])");
        stat.execute("INSERT INTO TABLE_POINT VALUES(1, 'POINT(1 2)', ARRAY[10000, 20000, 30000, 10000])");
    }

    @AfterAll
    public static void afterAll() throws Exception {
        connection.close();
    }

    /**
     * Test the {@link JsonWriteDriver#write(ProgressVisitor, String, File, boolean, String)} method.
     */
    @Test
    void testWrite1() {
        JsonWriteDriver writer = new JsonWriteDriver(connection);
        //Write query
        String path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        File f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "(SELECT idarea FROM table_point)", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(12, f.length());

        //Write table
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //Write zip
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".zip");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(287, f.length());

        //Write gz
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".gz");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(79, f.length());

        //No Progress
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //No query
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        final File f1 = new File(path + ".json");
        assertThrows(SQLException.class, () ->
                writer.write(new EmptyProgressVisitor(), "", f1, true, "UTF-8"));

        //No query
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        final File f2 = new File(path + ".json");
        assertThrows(SQLException.class, () ->
                writer.write(new EmptyProgressVisitor(), "", f2, true, "UTF-8"));

        //No parenthesis
        assertThrows(SQLException.class, () ->
            writer.write(new EmptyProgressVisitor(), "SELECT idarea FROM table_point", f2, true, "UTF-8"));

        //Rewrite
        try {
            writer.write(new EmptyProgressVisitor(), "(SELECT idarea FROM table_point)", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(12, f.length());

        //No rewrite
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-8");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //UTF-16 encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-16");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(142, f.length());

        //UTF-32 encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "UTF-32");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(284, f.length());

        //No encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, "");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //No encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), "table_point", f, true, null);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //No connection
        JsonWriteDriver writer2 = new JsonWriteDriver(null);
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        File f3 = new File(path + ".json");
        assertThrows(NullPointerException.class, () ->
                writer2.write(new EmptyProgressVisitor(), "table_point", f3, true, "UTF-8"));
    }

    /**
     * Test the {@link JsonWriteDriver#write(ProgressVisitor, ResultSet, File, boolean)} and
     * {@link JsonWriteDriver#write(ProgressVisitor, ResultSet, File, boolean, String)}  method.
     */
    @Test
    void testWrite2() {
        ResultSet rs = null;
        try {
            rs = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                    .executeQuery("SELECT * FROM table_point");
        }
        catch (SQLException e) {
            fail(e);
        }
        JsonWriteDriver writer = new JsonWriteDriver(connection);

        //Write query
        String path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        File f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //Write zip
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".zip");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(287, f.length());

        //Write gz
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".gz");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(79, f.length());

        //No Progress
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(null, rs, f, true);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //No ResultSet
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        final File f1 = new File(path + ".json");
        assertThrows(SQLException.class, () ->
                writer.write(new EmptyProgressVisitor(), null, f1, true));

        //Rewrite
        ResultSet litteRs = null;
        try {
            litteRs = connection.createStatement().executeQuery("SELECT idarea FROM table_point");
        } catch (SQLException e) {
            fail(e);
        }
        try {
            writer.write(new EmptyProgressVisitor(), litteRs, f, true);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(12, f.length());

        //No rewrite
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, false);
        } catch (SQLException | IOException e) {
            fail(e);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //UTF-16 encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true, "UTF-16");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(142, f.length());

        //UTF-32 encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true, "UTF-32");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(284, f.length());

        //No encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true, "");
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        //No encoding
        path = "./target/" + JsonWriteDriverTest.class.getSimpleName() +
                "_" + UUID.randomUUID().toString();
        f = new File(path + ".json");
        try {
            writer.write(new EmptyProgressVisitor(), rs, f, true, null);
        } catch (SQLException | IOException throwables) {
            fail(throwables);
        }
        assertTrue(f.exists());
        assertEquals(71, f.length());

        try {
            rs.close();
            litteRs.close();
        } catch (SQLException e) {
            fail(e);
        }
    }
}
