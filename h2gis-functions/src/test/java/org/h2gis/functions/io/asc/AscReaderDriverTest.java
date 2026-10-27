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

package org.h2gis.functions.io.asc;

import org.h2gis.api.EmptyProgressVisitor;
import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.postgis_jts.PostGISDBFactory;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2gis.unitTest.GeometryAsserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class AscReaderDriverTest {

    private Connection connection;
    private static final String DB_NAME = "ASCRead_db";

    private static final Logger log = LoggerFactory.getLogger(AscReaderDriverTest.class);
    private static final PostGISDBFactory dataSourceFactory = new PostGISDBFactory();

    @BeforeEach
    public void tearUp() throws Exception {
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if(connection != null) {
            connection.close();
        }
    }

    @Test
    public void testReadPrecip() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS PRECIP30MIN");
        AscReaderDriver reader = new AscReaderDriver();
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);

        // Check first read cell
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-179.75,-80.25), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-172.75, -89.75), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }
        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM,  ST_SETSRID(ST_MAKEPOINT(-177.25, -84.25), 4326))")) {
            assertFalse(rs.next());
        }
    }


    /**
     * Test reading two time the same asc by pushing more lines
     * asc files may be tiled, it is interesting to have a method to read all asc files into the same output table
     */
    @Test
    public void testReadPrecipTwoTimes() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS PRECIP30MIN");
        AscReaderDriver reader = new AscReaderDriver();
        reader.setDeleteTable(true);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);

        // Check number of read cells
        assertEquals(299, JDBCUtilities.getRowCount(connection, TableLocation.parse("PRECIP30MIN", DBTypes.H2GIS)));

        // Check first read cell
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-179.75,-80.25), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-172.75, -89.75), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }
        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM,  ST_SETSRID(ST_MAKEPOINT(-177.25, -84.25), 4326))")) {
            assertFalse(rs.next());
        }

        // Push the same file as a new tile
        reader.setDeleteTable(false);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);

        // Check first read cell
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-179.75,-80.25), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-172.75, -89.75), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }
        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM,  ST_SETSRID(ST_MAKEPOINT(-177.25, -84.25), 4326))")) {
            assertFalse(rs.next());
        }

        // Check number of read cells
        assertEquals(299 * 2, JDBCUtilities.getRowCount(connection, TableLocation.parse("PRECIP30MIN", DBTypes.H2GIS)));
    }

    @Test
    public void testReadPrecipDouble() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS PRECIP30MIN");
        AscReaderDriver reader = new AscReaderDriver();
        reader.setZType(2);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);

        // Check first read cell
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-179.75,-80.25), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234.0, rs.getDouble("Z"), 0.00001);
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-172.75, -89.75), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getDouble("Z"), 0.00001);
        }
        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM,  ST_SETSRID(ST_MAKEPOINT(-177.25, -84.25), 4326))")) {
            assertFalse(rs.next());
        }
    }

    @Test
    public void testReadPrecipCenterNodata() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        reader.setDeleteTable(true);
        reader.setImportNodata(true);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min_center.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);

        // Check database content
        // Check first read cell
        Statement st = connection.createStatement();

        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-180, -80.50), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-173, -90), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }

        st.execute("CALL SHPWRITE('target/grid.shp', 'PRECIP30MIN', true)");
        //st.execute("CALL SHPWRITE('target/grid_nodata.shp', '(SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM,  st_buffer(ST_SETSRID(ST_MAKEPOINT(-179.5,-80.25), 4326), 0.1)))')");

        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-177.50, -84.5), 4326))")) {
            assertTrue(rs.next());
            assertEquals(-9999, rs.getInt("Z"));
        }
    }

    @Test
    public void testReadPrecipCenter() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        reader.setDeleteTable(true);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min_center.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);

        // Check database content

        // Check first read cell
        Statement st = connection.createStatement();

        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-180, -80.50), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-173, -90), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }

        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-177.50, -84.5), 4326))")) {
            assertFalse(rs.next());
        }
    }
    @Test
    public void testReadPrecipPoint() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        reader.setAs3DPoint(true);
        reader.setDeleteTable(true);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);


        // Check database content

        // Check first read cell
        Statement st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT ST_Z(THE_GEOM) Z FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_EXPAND(ST_MAKEPOINT(-179.74,-80.18), 0.25, 0.25), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT ST_Z(THE_GEOM) Z FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_EXPAND( ST_MAKEPOINT(-172.604,-89.867), 0.25, 0.25), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }

        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT  ST_Z(THE_GEOM) Z FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_EXPAND( ST_MAKEPOINT(-177.438, -84.077), 0.25, 0.25), 4326))")) {
            assertFalse(rs.next());
        }
    }


    @Test
    public void testReadPrecipEnvelope() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        reader.setExtractEnvelope(new Envelope(-178.242, -174.775, -89.707, -85.205));
        reader.setDeleteTable(true);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        // Check database content
        // Check number of extracted cells
        Statement st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT COUNT(*) CPT FROM PRECIP30MIN")) {
            assertTrue(rs.next());
            assertEquals(90, rs.getInt("CPT"));
        }
    }

    @Test
    public void testReadPrecipDownscale() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        reader.setDownScale(5);
        reader.setDeleteTable(true);
        reader.read(connection, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        // Check database content

        // Check number of extracted cells
        Statement st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT COUNT(*) CPT FROM PRECIP30MIN")) {
            assertTrue(rs.next());
            assertEquals((15 / 5) * (20 / 5), rs.getInt("CPT"));
        }
    }


    @Test
    public void testASCRead() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s')",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));

        // Check number of extracted cells
        try(ResultSet rs = st.executeQuery("SELECT COUNT(*) CPT FROM PRECIP30MIN")) {
            assertTrue(rs.next());
            assertEquals(299, rs.getInt("CPT"));
        }

        Envelope env = new Envelope(-178.242, -174.775, -89.707, -85.205);
        GeometryFactory factory = new GeometryFactory();
        Geometry envGeom = factory.toGeometry(env);
        envGeom.setSRID(3857);
        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format(
                "CALL ASCREAD('%s', 'PRECIP30MIN', '%s'" +
                        "::GEOMETRY , 1, TRUE)",AscReaderDriverTest.class.getResource("precip30min.asc").getFile(),
                envGeom.toString()
        ));

        // Check number of extracted cells
        try(ResultSet rs = st.executeQuery("SELECT COUNT(*) CPT FROM PRECIP30MIN")) {
            assertTrue(rs.next());
            assertEquals(90, rs.getInt("CPT"));
        }
        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s', 'PRECIP30MIN', NULL, 5, TRUE)",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));

        // Check number of extracted cells
        try(ResultSet rs = st.executeQuery("SELECT COUNT(*) CPT FROM PRECIP30MIN")) {
            assertTrue(rs.next());
            assertEquals((15 / 5) * (20 / 5), rs.getInt("CPT"));
        }
    }

    @Test
    public void testASCReadPoints() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s')",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom  FROM PRECIP30MIN limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
        }
    }

    @Test
    public void testReadPrecipEnvelopePOSTGIS(TestInfo testInfo) throws IOException, SQLException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            AscReaderDriver reader = new AscReaderDriver();
            reader.setExtractEnvelope(new Envelope(-178.242, -174.775, -89.707, -85.205));
            reader.setDeleteTable(true);
            reader.read(con, new File(AscReaderDriverTest.class.getResource("precip30min.asc").getPath()), new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
            // Check database content
            // Check number of extracted cells
            Statement st = con.createStatement();
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) CPT FROM PRECIP30MIN")) {
                assertTrue(rs.next());
                assertEquals(90, rs.getInt("CPT"));
            }
        }
    }

    @Test
    public void testASCReadPointsTwoTimes() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s')",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom  FROM PRECIP30MIN limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
        }
        st.execute("DROP TABLE PRECIP30MIN_NEXT IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s','PRECIP30MIN_NEXT')",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom  FROM PRECIP30MIN_NEXT limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
        }
    }

    @Test
    public void testASCReadPointsZType() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s', 1)",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom, z  FROM PRECIP30MIN limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
            assertTrue(rs.getObject("Z") instanceof Integer);
        }

        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s', 2)",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom, z  FROM PRECIP30MIN limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
            assertTrue(rs.getObject("Z") instanceof Double);
        }

        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s', 'mydemtable', 2)",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom, z  FROM mydemtable limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
            assertTrue(rs.getObject("Z") instanceof Double);
        }

        st.execute("DROP TABLE PRECIP30MIN IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s')",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom, z  FROM PRECIP30MIN limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
            assertTrue(rs.getObject("Z") instanceof Double);
        }

    }

    @Test
    public void testASCReadGZFile() throws IOException, SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE PRECIP30MIN_ASC IF EXISTS");
        st.execute(String.format("CALL ASCREAD('%s')",AscReaderDriverTest.class.getResource("precip30min.asc.gz").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom  FROM PRECIP30MIN_ASC limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("POINT Z (-179.75 -80.25 234)", (Geometry) rs.getObject("THE_GEOM"));
        }
        st.execute("DROP TABLE PRECIP30MIN_ASC IF EXISTS");
    }
    /**
     * ASCTable returns the cells ASCRead imports, with the same options
     */
    @Test
    public void testASCTable() throws SQLException {
        try (Statement stat = connection.createStatement()) {
            H2GISFunctions.registerFunction(stat, new ASCTable(), "");
            String path = StringUtils.quoteStringSQL(AscReaderDriverTest.class.getResource("precip30min.asc").getPath());
            String gzPath = StringUtils.quoteStringSQL(AscReaderDriverTest.class.getResource("precip30min.asc.gz").getPath());
            String envelope = "ST_MakeEnvelope(-178, -86, -173, -80)";
            String[][] cases = {
                    {"CALL ASCRead(" + path + ", 'ASC_IMPORTED', true)", path},
                    {"CALL ASCRead(" + path + ", 'ASC_IMPORTED', NULL, 1, false, true, 'UTF-8', 1)", path + ", 1"},
                    {"CALL ASCRead(" + path + ", 'ASC_IMPORTED', " + envelope + ", 2, true, true)", path + ", " + envelope + ", 2, true"},
                    {"CALL ASCRead(" + path + ", 'ASC_IMPORTED', NULL, 3, false, true)", path + ", NULL, 3, false"},
                    {"CALL ASCRead(" + gzPath + ", 'ASC_IMPORTED', true)", gzPath}};
            long[] expectedRows = {299, 299, 42, 35, 299};
            for (int i = 0; i < cases.length; i++) {
                stat.execute(cases[i][0]);
                String table = "(SELECT * FROM ASCTable(" + cases[i][1] + "))";
                ResultSet res = stat.executeQuery("SELECT (SELECT COUNT(*) FROM ASC_IMPORTED), (SELECT COUNT(*) FROM " + table + "), "
                        + "(SELECT COUNT(*) FROM (SELECT PK, THE_GEOM, Z FROM ASC_IMPORTED EXCEPT SELECT * FROM " + table + ")), "
                        + "(SELECT COUNT(*) FROM (SELECT * FROM " + table + " EXCEPT SELECT PK, THE_GEOM, Z FROM ASC_IMPORTED))");
                assertTrue(res.next());
                assertEquals(expectedRows[i], res.getLong(1), cases[i][1]);
                assertEquals(expectedRows[i], res.getLong(2), cases[i][1]);
                assertEquals(0, res.getLong(3), cases[i][1]);
                assertEquals(0, res.getLong(4), cases[i][1]);
                res.close();
            }
            // the SRID of the .prj file, an INTEGER Z
            ResultSet res = stat.executeQuery("SELECT ST_SRID(THE_GEOM), Z FROM ASCTable(" + path + ", 1) LIMIT 1");
            assertTrue(res.next());
            assertEquals(3857, res.getInt(1));
            assertInstanceOf(Integer.class, res.getObject(2));
            res.close();
            assertThrows(SQLException.class, () -> stat.executeQuery("SELECT * FROM ASCTable('target/does_not_exist.asc')"));
            assertThrows(SQLException.class, () -> stat.executeQuery("SELECT * FROM ASCTable(" + path + ", 3)"));
            assertThrows(SQLException.class, () -> stat.executeQuery("SELECT * FROM ASCTable(" + path + ", NULL, 0, false)"));
            stat.execute("DROP TABLE ASC_IMPORTED");
        }
    }
}