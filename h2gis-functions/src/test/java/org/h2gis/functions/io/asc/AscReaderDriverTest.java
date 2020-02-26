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

package org.h2gis.functions.io.asc;

import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.SFSUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.unitTest.GeometryAsserts;

import static org.junit.jupiter.api.Assertions.*;

public class AscReaderDriverTest {

    private Connection connection;

    @BeforeEach
    public void tearUp() throws Exception {
        connection = SFSUtilities.wrapConnection(H2GISDBFactory.createSpatialDataBase(AscReaderDriverTest.class.getSimpleName(), true, ""));
    }

    @AfterEach
    public void tearDown() throws Exception {
        if(connection != null) {
            connection.close();
        }
    }

    @Test
    public void testReadPrecip() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        try(InputStream inputStream = AscReaderDriverTest.class.getResourceAsStream("precip30min.asc")) {
            reader.read(connection, inputStream, new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        }

        // Check database content

        // Check first read cell
        Statement st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-179.74,-80.18), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-172.604,-89.867), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }

        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-177.438, -84.077), 4326))")) {
            assertTrue(rs.next());
            assertNull(rs.getObject("Z"));
        }
    }



    @Test
    public void testReadPrecipCenter() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        try(InputStream inputStream = AscReaderDriverTest.class.getResourceAsStream("precip30min_center.asc")) {
            reader.read(connection, inputStream, new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        }

        // Check database content

        // Check first read cell
        Statement st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-180.1454, -80.303), 4326))")) {
            assertTrue(rs.next());
            assertEquals(234, rs.getInt("Z"));
        }

        // Check last read cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-173.213, -89.771), 4326))")) {
            assertTrue(rs.next());
            assertEquals(114, rs.getInt("Z"));
        }

        // Check nodata cell
        st = connection.createStatement();
        try(ResultSet rs = st.executeQuery("SELECT * FROM PRECIP30MIN WHERE ST_INTERSECTS(THE_GEOM, ST_SETSRID(ST_MAKEPOINT(-177.3831, -84.5793), 4326))")) {
            assertTrue(rs.next());
            assertNull(rs.getObject("Z"));
        }
    }
    @Test
    public void testReadPrecipPoint() throws IOException, SQLException {
        AscReaderDriver reader = new AscReaderDriver();
        reader.setAs3DPoint(true);
        try(InputStream inputStream = AscReaderDriverTest.class.getResourceAsStream("precip30min.asc")) {
            reader.read(connection, inputStream, new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        }

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

        try(InputStream inputStream = AscReaderDriverTest.class.getResourceAsStream("precip30min.asc")) {
            reader.read(connection, inputStream, new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        }

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

        try(InputStream inputStream = AscReaderDriverTest.class.getResourceAsStream("precip30min.asc")) {
            reader.read(connection, inputStream, new EmptyProgressVisitor(), "PRECIP30MIN", 4326);
        }

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
        st.execute(String.format("CALL ASCREAD('%s', 'PRECIP30MIN', '%s'" +
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
        st.execute(String.format("CALL ASCREAD('%s')",AscReaderDriverTest.class.getResource("precip30min.asc").getFile()));
        try(ResultSet rs = st.executeQuery("SELECT the_geom  FROM PRECIP30MIN limit 1")) {
            assertTrue(rs.next());
            GeometryAsserts.assertGeometryEquals("SRID=3857;POINT Z (-179.75 -80.25 234)", rs.getObject("THE_GEOM"));
        }
    }
}