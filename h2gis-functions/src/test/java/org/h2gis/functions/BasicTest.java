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

package org.h2gis.functions;

import org.h2gis.functions.factory.H2GISFunctions;
import org.locationtech.jts.geom.GeometryFactory;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.h2.jdbc.JdbcSQLException;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.h2gis.utilities.trigger.UpdateTrigger;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * 
 * @author Erwan Bocher
 */
public class BasicTest {
        private static Connection connection;

        @BeforeClass
        public static void tearUp() throws Exception {
            // Keep a connection alive to not close the DataBase on each unit test
            connection = H2GISDBFactory.createSpatialDataBase("BasicTest");
        }
        @AfterClass
        public static void tearDown() throws Exception {
            connection.close();
        }
        @Test
        public void testPoints3D() throws Exception {

                WKTReader wktReader = new WKTReader();

                Geometry geom = wktReader.read("POINT(0 1 3)");

                Coordinate coord = geom.getCoordinates()[0];

                assertTrue(3 == coord.z);

        }

        @Test
        public void testUpdateTrigger() throws SQLException {
                Statement st = connection.createStatement();
               try {
                   st.execute("drop trigger if exists updatetrigger");
                   st.execute("DROP TABLE IF EXISTS test");
                   st.execute("create table test as select 1, 'POINT(1 2)'::geometry");
                   st.execute("create trigger updatetrigger AFTER INSERT, UPDATE, DELETE ON test CALL \""+UpdateTrigger.class.getName()+"\"");
                   st.execute("insert into test values(2, 'POINT(5 5)') , (3, 'POINT(1 1)')");
                   ResultSet rs = st.executeQuery("select * from "+new TableLocation(UpdateTrigger.TRIGGER_SCHEMA, UpdateTrigger.NOTIFICATION_TABLE));
                   try {
                       assertTrue(rs.next());
                       assertEquals(1,rs.getInt(2));
                       assertFalse(rs.next());
                   } finally {
                       rs.close();
                   }
               } finally {
                   st.execute("drop trigger if exists updatetrigger");
                   st.execute("DROP TABLE IF EXISTS test");
               }
        }

        /**
         * Test if H2 recognize the Geometry class used by h2gis
         */
        @Test
        public void testSameClass() {
            GeometryFactory geometryFactory = new GeometryFactory();
            Geometry geometry = geometryFactory.createPoint(new Coordinate(0,0));
            assertEquals("H2 does not use the same JTS ! Expected:\n" + Geometry.class.getName() + "\n but got:\n"
                    + DataType.getTypeClassName(DataType.getTypeFromClass(geometry.getClass())) + "\n", Value.GEOMETRY,
                    DataType.getTypeFromClass(geometry.getClass()));
        }

        @Test
        public void testGeometryType() throws Exception {
            final Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS GEOMTABLE;");
            stat.execute("CREATE TABLE GEOMTABLE (gid INTEGER AUTO_INCREMENT PRIMARY KEY, the_geom GEOMETRY);");
        }

        @Test
        public void testWriteRead3DGeometryWithNaNZ() throws ClassNotFoundException, SQLException, ParseException {

                final Statement stat = connection.createStatement();

                stat.execute("DROP TABLE IF EXISTS POINT3D");

                stat.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
                stat.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12)', 27582))");

                ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();
                Geometry geom;
                boolean hasGeometryColumn = false;
                while(rs.next()) {
                        String columnTypeName = rsmd2.getColumnTypeName(2);
                        if (columnTypeName.equalsIgnoreCase(H2GISFunctions.GEOMETRY_BASE_TYPE)) {
                                geom = (Geometry)rs.getObject("the_geom");
                                Coordinate coord = geom.getCoordinates()[0];
                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);
                                assertTrue(Double.isNaN(coord.z));
                                hasGeometryColumn = true;
                        }

                }
                assertTrue(hasGeometryColumn);
                stat.close();
        }

        @Test
        public void testST_Area() throws Exception {
            final Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery("select ST_Area(ST_GeomFromText('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))', 27572)) as area from dummy");
            assertTrue(rs.next());
            assertEquals(100.0,rs.getDouble("area"),1e-12);
            stat.close();

        }

    @Test
    public void testSFSUtilities() throws Exception {
        final Statement stat = connection.createStatement();
        String catalog = connection.getCatalog();
        stat.execute("drop schema if exists blah");
        stat.execute("create schema blah");
        stat.execute("create table blah.testSFSUtilities(id integer, the_geom point)");
        List<String> geomFields = SFSUtilities.getGeometryFields(connection, new TableLocation(catalog, "blah", "testSFSUtilities"));
        assertEquals(1, geomFields.size());
        assertEquals("THE_GEOM", geomFields.get(0));
    }

    @Test
    public void testSFSUtilitiesFirstGeometryFieldName1() throws Exception {
        final Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POINT3D");
        stat.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        stat.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12)', 27582))");
        ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
        String geomField = SFSUtilities.getFirstGeometryFieldName(rs);
        assertEquals("THE_GEOM", geomField);
    }
    
    @Test(expected = SQLException.class)
    public void testSFSUtilitiesFirstGeometryFieldName2() throws Throwable {
        try{
        final Statement stat = connection.createStatement();
        stat.execute("DROP TABLE IF EXISTS POINT3D");
        stat.execute("CREATE TABLE POINT3D (gid int )");
        stat.execute("INSERT INTO POINT3D (gid) VALUES(1)");
        ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
        SFSUtilities.getFirstGeometryFieldName(rs);
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
}
