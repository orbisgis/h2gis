/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities.dbtypes;

import org.h2gis.postgis_jts_osgi.DataSourceFactoryImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class dedicated to {@link DBUtils}.
 *
 * @author Erwan Bocher (CNRS 2021)
 * @author Sylvein PALOMINOS (UBS Chaire GEOTERA 2021)
 */
public class DBUtilsTest {

    private static Connection h2Conn;
    private static Connection postConn;
    private static Statement h2St;

    @BeforeAll
    public static void init() throws Exception {
        String dataBaseLocation = new File("target/DBUtilsTest").getAbsolutePath();
        String databasePath = "jdbc:h2:" + dataBaseLocation;
        File dbFile = new File(dataBaseLocation + ".mv.db");
        Class.forName("org.h2.Driver");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        h2Conn = DriverManager.getConnection(databasePath,
                "sa", "");

        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();

        DataSource ds = dataSourceFactory.createDataSource(props);
        postConn = ds.getConnection();
        if (postConn == null) {
            System.setProperty("postgresql", "false");
        } else {
            System.setProperty("postgresql", "true");
        }
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        h2St = h2Conn.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        h2St.close();
    }

    @AfterAll
    public static void dispose() throws Exception {
        h2Conn.close();
    }

    @Test
    public void getDBTypeFromConnection() throws SQLException {
        assertEquals(DBTypes.H2, DBUtils.getDBType(h2Conn));
    }

    @Test
    @DisabledIfSystemProperty(named = "postgresql", matches = "false")
    public void getDBTypeFromConnection2() throws SQLException {
        assertEquals(DBTypes.POSTGIS, DBUtils.getDBType(postConn));
    }

    @Test
    public void getDBTypeFromURILTest() {
        String str1 = "jdbc:postgresql://localhost/test";
        String str2 = "postgresql://localhost/test";

        URI uri1 = URI.create(str1);
        URI uri2 = URI.create(str2);

        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(str1));
        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(str2));
        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(uri1));
        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(uri2));

        str1 = "jdbc:h2://localhost/test";
        str2 = "h2://localhost/test";

        uri1 = URI.create(str1);
        uri2 = URI.create(str2);

        assertEquals(DBTypes.H2, DBUtils.getDBType(str1));
        assertEquals(DBTypes.H2, DBUtils.getDBType(str2));
        assertEquals(DBTypes.H2, DBUtils.getDBType(uri1));
        assertEquals(DBTypes.H2, DBUtils.getDBType(uri2));
    }
}
