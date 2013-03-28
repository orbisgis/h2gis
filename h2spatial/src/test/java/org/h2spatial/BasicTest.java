/**
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
package org.h2spatial;

import org.junit.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import static org.junit.Assert.*;
/**
 * 
 * @author Erwan Bocher
 */
public class BasicTest {

        String DATABASEPATH = "jdbc:h2:src/test/resources/backup/dbH2";

        @Test
        public void testPoints3D() throws Exception {

                WKTReader wktReader = new WKTReader();

                Geometry geom = wktReader.read("POINT(0 1 3)");

                Coordinate coord = geom.getCoordinates()[0];

                assertTrue(3 == coord.z);

        }

        @Test
        public void testWriteRead2DGeometry() throws ClassNotFoundException,
                SQLException, ParseException {

                Class.forName("org.h2.Driver");
                Connection con = DriverManager.getConnection(DATABASEPATH,
                        "sa", "");

                final Statement stat = con.createStatement();
                SQLCodegenerator.addSpatialFunctions(stat);

                stat.execute("DROP TABLE IF EXISTS POINT2D");

                stat.execute("CREATE TABLE POINT2D (gid int , the_geom blob)");
                stat.execute("INSERT INTO POINT2D (gid, the_geom) VALUES(1, GeomFromText('POINT(0 12)', 27582))");


                ResultSet rs = stat.executeQuery("SELECT * from POINT2D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();
                WKBReader wkbReader = new WKBReader();
                byte valObj[] = (byte[]) null;
                Geometry geom = null;

                for (; rs.next();) {


                        String columnTypeName = rsmd2.getColumnTypeName(2);

                        if (columnTypeName.equals("BLOB")) {
                                valObj = rs.getBytes(2);
                                geom = wkbReader.read(valObj);
                                Coordinate coord = geom.getCoordinates()[0];

                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);

                        }

                }


                stat.close();

                con.close();

        }

        @Test
        public void testWriteRead3DGeometry() throws ClassNotFoundException,
                SQLException, ParseException {

                Class.forName("org.h2.Driver");
                Connection con = DriverManager.getConnection(DATABASEPATH,
                        "sa", "");

                final Statement stat = con.createStatement();
                SQLCodegenerator.addSpatialFunctions(stat);

                stat.execute("DROP TABLE IF EXISTS POINT3D");

                stat.execute("CREATE TABLE POINT3D (gid int , the_geom blob)");
                stat.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, GeomFromText('POINT(0 12 3)', 27582))");



                ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();
                WKBReader wkbReader = new WKBReader();
                byte valObj[] = (byte[]) null;
                Geometry geom = null;

                for (; rs.next();) {


                        String columnTypeName = rsmd2.getColumnTypeName(2);

                        if (columnTypeName.equals("BLOB")) {
                                valObj = rs.getBytes(2);
                                geom = wkbReader.read(valObj);
                                Coordinate coord = geom.getCoordinates()[0];

                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);
                                assertTrue(coord.z == 3);

                        }

                }

                stat.close();

                con.close();

        }

        @Test
        public void testWriteRead3DGeometryWithNaNZ() throws ClassNotFoundException, SQLException, ParseException {

                Class.forName("org.h2.Driver");
                Connection con = DriverManager.getConnection(DATABASEPATH,
                        "sa", "");

                final Statement stat = con.createStatement();
                SQLCodegenerator.addSpatialFunctions(stat);

                stat.execute("DROP TABLE IF EXISTS POINT3D");

                stat.execute("CREATE TABLE POINT3D (gid int , the_geom blob)");
                stat.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, GeomFromText('POINT(0 12)', 27582))");

                ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();
                WKBReader wkbReader = new WKBReader();
                byte valObj[] = (byte[]) null;
                Geometry geom = null;

                for (; rs.next();) {


                        String columnTypeName = rsmd2.getColumnTypeName(2);

                        if (columnTypeName.equals("BLOB")) {
                                valObj = rs.getBytes(2);
                                geom = wkbReader.read(valObj);
                                Coordinate coord = geom.getCoordinates()[0];

                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);
                                assertTrue(Double.isNaN(coord.z));

                        }

                }
                stat.close();
                con.close();

        }
}
