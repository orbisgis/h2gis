package org.h2gis.functions;

import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.shp.SHPEngineTest;
import org.h2gis.utilities.JDBCUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class RegressionTest {

    private static Connection connection;
    private static final String DB_NAME = "RegressionTest";

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

    @Test
    public void testH2GIS_SPATIALCALL() throws SQLException {
        Statement stat = connection.createStatement();
        stat.execute("CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR \"org.h2gis.functions.factory.H2GISFunctions.load\"");
        stat.execute("CREATE ALIAS IF NOT EXISTS H2GIS_UNLOAD FOR \"org.h2gis.functions.factory.H2GISFunctions.unRegisterH2GISFunctions\"");
        stat.execute("CALL H2GIS_SPATIAL();");
        stat.execute("SELECT 1");
        stat.execute("CALL SHPRead("+ StringUtils.quoteStringSQL(SHPEngineTest.class.getResource("waternetwork.shp").getPath())+", 'water', true)");
        stat.execute("CALL H2GIS_SPATIAL();");
    }
}
