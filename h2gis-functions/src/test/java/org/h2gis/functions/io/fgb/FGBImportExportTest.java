package org.h2gis.functions.io.fgb;

import org.h2.value.ValueGeometry;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.functions.io.geojson.*;
import org.h2gis.postgis_jts.PostGISDBFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FGBImportExportTest {


    private static Connection connection;
    private static final String DB_NAME = "FGBImportExportTest";
    private static final WKTReader WKTREADER = new WKTReader();
    private static final Logger log = LoggerFactory.getLogger(FGBImportExportTest.class);

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new FGBWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new FGBRead(), "");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testWriteReadFGBPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT))");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)')");

            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS; CREATE TABLE TABLE_POINTS (the_geom GEOMETRY(POINT))" +
                    " as SELECT st_makepoint(-60 + x*random()/500.00, 30 + x*random()/500.00) as the_geom" +
                    "  FROM GENERATE_SERIES(1, 10000);");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");

           //stat.execute("CALL SHPWRITE('target/points.shp', 'TABLE_POINTS', true);");

            /*stat.execute("CALL GeoJsonRead('target/multipoints.geojson', 'TABLE_MULTIPOINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOINTS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((150 290), (180 170), (266 275))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS_READ");*/
        }
    }

    @Test
    public void testFGBEngine() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), land varchar)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 'corn')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 'grass')");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");

            File fgbFile = new File("target/points.fgb");
            FGBDriver fgbDriver = new FGBDriver();
            fgbDriver.initDriverFromFile(fgbFile);
            assertEquals(3, fgbDriver.getFieldCount());
            for (int i = 0; i <fgbDriver.getRowCount() ; i++) {
                for (int j = 0; j < fgbDriver.getFieldCount(); j++) {
                    System.out.println(fgbDriver.getField(i, j).getString());
                }
            }
        }
    }

    @Test
    public void testFGBFileTable() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), land varchar)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 'corn')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 'grass')");
            stat.execute("CALL FILE_TABLE('target/points.fgb', 'points');");
            ResultSet rs = stat.executeQuery("SELECT * FROM points");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
        }
    }

    /**
     * Use externally generated FGP and GeoJSON files from flatgeobuf repository
     */
    @Test
    public void testExternalFGPImport() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBRead('"+FGBImportExportTest.class.getResource("countries.fgb").getPath()+"', 'COUNTRIES_FGB', true);");
            stat.execute("CALL GEOJSONREAD('"+FGBImportExportTest.class.getResource("countries.geojson")+"', 'COUNTRIES_GEOJSON', true);");
        }

    }

    @Disabled
    @Test
    public void testWriteRamdomPoints() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS (THE_GEOM GEOMETRY(POINT)) as SELECT  st_makepoint(-60 + x*random()/500.00, 30 + x*random()/500.00) AS_THE_GEOM FROM GENERATE_SERIES(1, 10000)");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");
            /*stat.execute("CALL GeoJsonRead('target/multipoints.geojson', 'TABLE_MULTIPOINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOINTS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((150 290), (180 170), (266 275))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS_READ");*/
        }
    }
}
