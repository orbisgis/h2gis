package org.h2gis.functions;

import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.shp.SHPEngineTest;
import org.h2gis.functions.io.utility.IOMethods;
import org.h2gis.utilities.JDBCUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class RegressionTest {

    private static Connection connection;
    private static final String DB_NAME = RegressionTest.class.getSimpleName()+"_RegressionTest";

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
        Connection linkedDBConnection =   DriverManager.getConnection("jdbc:h2:mem:linked_db", "sa", "sa");
        Statement statementLinked = linkedDBConnection.createStatement();
        statementLinked.execute("DROP TABLE IF EXISTS table_to_link;" );
        statementLinked.execute("CREATE TABLE table_to_link  ( the_geom GEOMETRY, the_geom2  GEOMETRY(POINT Z)," +
                "ID INTEGER,  TEMPERATURE DOUBLE PRECISION,  LANDCOVER  VARCHAR)");
        statementLinked.execute("INSERT INTO table_to_link VALUES ('POINT(0 0)', 'POINT(1 1 0)', 1, 2.3, 'Simple points')"+
        ",('POINT(0 1 2)', 'POINT(10 11 12)', 2, 0.568, '3D point')");
        Statement stat = connection.createStatement();
        stat.execute("CREATE LINKED TABLE LINKED_TABLE('org.h2.Driver', 'jdbc:h2:mem:linked_db', 'sa', 'sa', 'TABLE_TO_LINK') FETCH_SIZE 100 ");
        ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM LINKED_TABLE");
        res.next();
        assertEquals(2, res.getInt(1));
        res.close();
        linkedDBConnection.close();
    }
}
