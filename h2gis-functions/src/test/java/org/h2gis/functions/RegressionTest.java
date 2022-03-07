package org.h2gis.functions;

import org.h2.jdbc.JdbcSQLException;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.shp.SHPEngineTest;
import org.h2gis.functions.io.utility.IOMethods;
import org.h2gis.utilities.JDBCUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class RegressionTest {

    private static Connection connection;
    private static final String DB_NAME = RegressionTest.class.getSimpleName() + "_RegressionTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testBigGeometry() throws SQLException {
        Statement stat = connection.createStatement();
        assertDoesNotThrow(() -> {
            try {
                stat.execute("SELECT ST_ACCUM(st_makepoint(-60 + x*random()/500.00, 30 + x*random()/500.00)) FROM GENERATE_SERIES(1, 100000)");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Disabled
    @Test
    public void testBigGeometryDelaunay() throws SQLException {
        Statement stat = connection.createStatement();
        assertDoesNotThrow(() -> {
            try {
                stat.execute("SELECT ST_DELAUNAY(ST_ACCUM(st_makepoint(-60 + x*random()/500.00, 30 + x*random()/500.00, x))) FROM GENERATE_SERIES(1, 100000)");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testH2gis_spatialCall() throws SQLException {
        Statement stat = connection.createStatement();
        assertDoesNotThrow(() -> {
            stat.execute("CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR \"org.h2gis.functions.factory.H2GISFunctions.load\"");
            stat.execute("CREATE ALIAS IF NOT EXISTS H2GIS_UNLOAD FOR \"org.h2gis.functions.factory.H2GISFunctions.unRegisterH2GISFunctions\"");
            stat.execute("CALL H2GIS_SPATIAL();");
            stat.execute("SELECT 1");
            stat.execute("CALL SHPRead(" + StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath()) + ", 'water', true)");
            stat.execute("CALL H2GIS_SPATIAL();");
        });
    }

    @Test
    public void testLinkedTableGeometry() throws SQLException, ClassNotFoundException {
        Connection linkedDBConnection = DriverManager.getConnection("jdbc:h2:mem:linked_db", "sa", "sa");
        Statement statementLinked = linkedDBConnection.createStatement();
        statementLinked.execute("DROP TABLE IF EXISTS table_to_link;");
        statementLinked.execute("CREATE TABLE table_to_link  ( the_geom GEOMETRY, the_geom2  GEOMETRY(POINT Z)," +
                "ID INTEGER,  TEMPERATURE DOUBLE PRECISION,  LANDCOVER  VARCHAR)");
        statementLinked.execute("INSERT INTO table_to_link VALUES ('POINT(0 0)', 'POINTZ(1 1 0)', 1, 2.3, 'Simple points')" +
                ",('POINT(0 1)', 'POINTZ(10 11 12)', 2, 0.568, '3D point')");
        Statement stat = connection.createStatement();
        stat.execute("CREATE LINKED TABLE LINKED_TABLE('org.h2.Driver', 'jdbc:h2:mem:linked_db', 'sa', 'sa', 'TABLE_TO_LINK') FETCH_SIZE 100 ");
        ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM LINKED_TABLE");
        res.next();
        assertEquals(2, res.getInt(1));
        res.close();
        linkedDBConnection.close();
    }


    //TODO : related to //TODO : waiting for https://github.com/orbisgis/h2gis/issues/1244
    @Disabled
    @Test
    public void testJTS() throws ParseException {
        String geomA_wkt = "MULTIPOLYGON (((184661.64063 2431733.5, 184652.21875 2431759.5, 184646.03125 2431775, 184641.5 2431787.75, 184632.45313 2431812, 184620.78125 2431842, 184604.10938 2431886.25, 184602.92188 2431891, 184617.45313 2431898.25, 184662.6875 2431917.75, 184666.26563 2431915.25, 184675.79688 2431920, 184669.35938 2431943.75, 184684.125 2431946.75, 184704.84375 2431948.75, 184728.17188 2431948, 184758.89063 2431945.75, 184784.85938 2431944.25, 184844.85938 2431946.5, 184856.70313 2431947.75, 184906.40625 2431832, 184923.65625 2431795, 184939.10938 2431757, 184951.89063 2431723.25, 184968.25 2431670.5, 184969.8125 2431666.5, 184949.8125 2431668, 184890.95313 2431674.5, 184832.6875 2431680.5, 184761.32813 2431689.75, 184695.03125 2431698, 184673.17188 2431700.25, 184672.21875 2431705.25, 184670.79688 2431710, 184666.26563 2431720.75, 184661.64063 2431733.5)))";
        String geomB_wkt = "POLYGON Z((184635.3007232633 2431979.9689098285 30, 184674.17520872923 2431843.505908667 35, 184698.06594086462 2431990.7904990697 30, 184635.3007232633 2431979.9689098285 30))";
        WKTReader wktReader = new WKTReader();
        Geometry geomA = wktReader.read(geomA_wkt);
        Geometry geomB = wktReader.read(geomB_wkt);
        Geometry result = geomA.intersection(geomB);
        System.out.println(new WKTWriter(3).write(result));
    }

    @Disabled
    @Test
    public void testST_BufferBug() throws SQLException {
        Statement stat = connection.createStatement();
        assertDoesNotThrow(() -> {
            try {
                stat.execute("SELECT ST_BUFFER('LINESTRING (307095.4 6739498.8, 307113.6 6739493.1, 307172.2 6739471.6, 307246.5 6739446.4, 307283 6739433.5, 307346 6739414.1)'::GEOMETRY, 1,'endcap=flat') ;");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
}
