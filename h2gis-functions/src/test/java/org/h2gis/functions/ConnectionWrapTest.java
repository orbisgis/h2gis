/**
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

package org.h2gis.functions;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SpatialResultSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.utilities.JDBCUtilities;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test spatial wrapper of Connection
 * @author Nicolas Fortin
 */
public class ConnectionWrapTest {
    private static Connection connection;
    private static final String DB_NAME = "ConnectionWrapTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = JDBCUtilities.wrapConnection(H2GISDBFactory.createSpatialDataBase(DB_NAME));
    }
    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGeometryCast() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        stat.execute("insert into area values(1, 'POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))')");
        stat.execute("insert into area values(2, 'POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))')");
        try (SpatialResultSet rs = stat.executeQuery("select idarea, the_geom  from area").unwrap(SpatialResultSet.class)) {
            assertTrue(rs.next());
            assertEquals("POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))", rs.getGeometry("the_geom").toText());
            assertEquals("POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))", rs.getGeometry(2).toText());
            assertEquals("POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))", rs.getGeometry().toText());
        }
        stat.execute("DROP TABLE AREA");
    }

    @Test
    public void testGeometryUpdate() throws Exception {
        WKTReader wktReader = new WKTReader();
        Statement stat = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_UPDATABLE);
        stat.execute("DROP TABLE IF EXISTS AREA");
        stat.execute("create table area(idarea int primary key, the_geom GEOMETRY(POLYGON))");
        SpatialResultSet rs = stat.executeQuery("select * from area").unwrap(SpatialResultSet.class);
        rs.moveToInsertRow();
        rs.updateInt(1, 1);
        rs.updateGeometry(2, wktReader.read("POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))"));
        rs.insertRow();
        rs.moveToInsertRow();
        rs.updateInt(1, 2);
        rs.updateGeometry("the_geom", wktReader.read("POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))"));
        rs.insertRow();
        rs.close();
        rs = connection.createStatement().executeQuery("select * from area").unwrap(SpatialResultSet.class);
        assertTrue(rs.next());
        assertEquals("POLYGON ((-10 109, 90 109, 90 9, -10 9, -10 109))", rs.getGeometry("the_geom").toText());
        assertTrue(rs.next());
        assertEquals("POLYGON ((90 109, 190 109, 190 9, 90 9, 90 109))", rs.getGeometry("the_geom").toText());
        assertFalse(rs.next());
        stat.execute("DROP TABLE AREA");
    }
}
