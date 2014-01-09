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
package org.h2gis.drivers.kml;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class KMLExporterTest {

    private static Connection connection;
    private static final String DB_NAME = "KMLExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new KMLWrite(), "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void exportKMLPoints() throws SQLException {
        Statement stat = connection.createStatement();
        File kmlFile = new File("target/kml_points.kml");
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, 'POINT (47.58 2.19)', true)");
        stat.execute("insert into KML_POINTS values(2, 'POINT (47.59 1.06)', false)");
        // Create a shape file using table area
        stat.execute("CALL KMLWrite('target/kml_points.kml', 'KML_POINTS')");
        // Read this shape file to check values
        assertTrue(kmlFile.exists());
        stat.close();
    }
    
    @Test
    public void exportKMLLineString() throws SQLException {
        Statement stat = connection.createStatement();
        File kmlFile = new File("target/kml_lineString.kml");
        stat.execute("DROP TABLE IF EXISTS KML_LINESTRING");
        stat.execute("create table KML_LINESTRING(id int primary key, the_geom LINESTRING)");
        stat.execute("insert into KML_LINESTRING values(1, 'LINESTRING (47.58 2.19, 46.58 1.19)')");
        stat.execute("insert into KML_LINESTRING values(2, 'LINESTRING (47.59 1.06, 46.58 1.19)')");
        // Create a shape file using table area
        stat.execute("CALL KMLWrite('target/kml_lineString.kml', 'KML_LINESTRING')");
        // Read this shape file to check values
        assertTrue(kmlFile.exists());

        stat.close();
    }
    
    @Test
    public void exportKMZPoints() throws SQLException {
        Statement stat = connection.createStatement();
        File kmzFile = new File("target/kml_points.kmz");
        stat.execute("DROP TABLE IF EXISTS KML_POINTS");
        stat.execute("create table KML_POINTS(id int primary key, the_geom POINT, response boolean)");
        stat.execute("insert into KML_POINTS values(1, 'POINT (47.58 2.19)', true)");
        stat.execute("insert into KML_POINTS values(2, 'POINT (47.59 1.06)', false)");
        // Create a shape file using table area
        stat.execute("CALL KMLWrite('target/kml_points.kmz', 'KML_POINTS')");
        // Read this shape file to check values
        assertTrue(kmzFile.exists());
        stat.close();
    }
}
