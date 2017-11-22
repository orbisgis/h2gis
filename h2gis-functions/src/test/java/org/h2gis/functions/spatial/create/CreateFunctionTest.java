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

package org.h2gis.functions.spatial.create;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.h2.jdbc.JdbcSQLException;
import org.h2.value.ValueGeometry;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * @author Nicolas Fortin
 */
public class CreateFunctionTest {
    private static Connection connection;
    private Statement st;
    private static final GeometryFactory FACTORY = new GeometryFactory();
    private static final WKTReader WKT_READER = new WKTReader(FACTORY);

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(CreateFunctionTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void test_ST_BoundingCircle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle('POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((366.4800710247679 257.5, 363.82882265008465 230.58142351196977, "
                + "355.97696351423673 204.69731282226294, 343.22623616044143 180.84237978870843, "
                + "326.06664389021483 159.93335610978517, 305.1576202112916 142.77376383955857, "
                + "281.3026871777371 130.02303648576327, 255.41857648803023 122.17117734991535, "
                + "228.5 119.51992897523209, 201.58142351196977 122.17117734991535, "
                + "175.69731282226294 130.02303648576327, 151.84237978870846 142.77376383955857, "
                + "130.93335610978517 159.93335610978517, 113.77376383955855 180.84237978870843, "
                + "101.02303648576327 204.697312822263, 93.17117734991535 230.58142351196986, "
                + "90.51992897523209 257.5000000000001, 93.17117734991538 284.41857648803034, "
                + "101.02303648576334 310.3026871777372, 113.77376383955868 334.1576202112917, "
                + "130.9333561097853 355.066643890215, 151.84237978870863 372.22623616044154, "
                + "175.6973128222632 384.97696351423684, 201.5814235119701 392.8288226500847, "
                + "228.50000000000034 395.4800710247679, 255.4185764880306 392.8288226500846, "
                + "281.3026871777374 384.97696351423656, 305.15762021129194 372.2262361604412, "
                + "326.0666438902152 355.06664389021455, 343.2262361604417 334.15762021129115, "
                + "355.9769635142369 310.3026871777366, 363.82882265008476 284.41857648802966, "
                + "366.4800710247679 257.5))")));
        rs.close();
    }

    @Test
    public void test_ST_BoundingCircle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle('LINESTRING (140 200, 170 150)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((184.1547594742265 175, 183.59455894601797 "
                + "169.3121885858704, 181.9354855535274 163.8429565746244, "
                + "179.24129655680906 158.8024834852735, 175.6155281280883 154.3844718719117, "
                + "171.1975165147265 150.75870344319094, 166.1570434253756 148.0645144464726, "
                + "160.6878114141296 146.40544105398203, 155 145.8452405257735, "
                + "149.3121885858704 146.40544105398203, 143.8429565746244 148.0645144464726, "
                + "138.80248348527354 150.75870344319094, 134.3844718719117 154.3844718719117,"
                + " 130.75870344319094 158.8024834852735, 128.0645144464726 163.8429565746244, "
                + "126.40544105398202 169.3121885858704, 125.8452405257735 175.00000000000003, "
                + "126.40544105398203 180.68781141412964, 128.06451444647263 186.15704342537566,"
                + " 130.75870344319097 191.19751651472652, 134.38447187191173 195.61552812808833,"
                + " 138.80248348527357 199.2412965568091, 143.84295657462442 201.9354855535274, "
                + "149.31218858587044 203.594558946018, 155.00000000000009 204.1547594742265, "
                + "160.6878114141297 203.59455894601797, 166.1570434253757 201.93548555352737, "
                + "171.19751651472654 199.241296556809, 175.61552812808839 195.61552812808824, "
                + "179.24129655680912 191.19751651472637, 181.93548555352743 186.15704342537552, "
                + "183.594558946018 180.6878114141295, 184.1547594742265 175))")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumBoundingCircle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumBoundingCircle('POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((366.4800710247679 257.5, 363.82882265008465 230.58142351196977, "
                + "355.97696351423673 204.69731282226294, 343.22623616044143 180.84237978870843, "
                + "326.06664389021483 159.93335610978517, 305.1576202112916 142.77376383955857, "
                + "281.3026871777371 130.02303648576327, 255.41857648803023 122.17117734991535, "
                + "228.5 119.51992897523209, 201.58142351196977 122.17117734991535, "
                + "175.69731282226294 130.02303648576327, 151.84237978870846 142.77376383955857, "
                + "130.93335610978517 159.93335610978517, 113.77376383955855 180.84237978870843, "
                + "101.02303648576327 204.697312822263, 93.17117734991535 230.58142351196986, "
                + "90.51992897523209 257.5000000000001, 93.17117734991538 284.41857648803034, "
                + "101.02303648576334 310.3026871777372, 113.77376383955868 334.1576202112917, "
                + "130.9333561097853 355.066643890215, 151.84237978870863 372.22623616044154, "
                + "175.6973128222632 384.97696351423684, 201.5814235119701 392.8288226500847, "
                + "228.50000000000034 395.4800710247679, 255.4185764880306 392.8288226500846, "
                + "281.3026871777374 384.97696351423656, 305.15762021129194 372.2262361604412, "
                + "326.0666438902152 355.06664389021455, 343.2262361604417 334.15762021129115, "
                + "355.9769635142369 310.3026871777366, 363.82882265008476 284.41857648802966, "
                + "366.4800710247679 257.5))")));
        rs.close();
    }

    @Test
    public void test_ST_Expand1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 10, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))")));
        rs.close();
    }

    @Test
    public void test_ST_Expand2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 5, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))")));
        rs.close();
    }

    @Test
    public void test_ST_Expand3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 5, -10);");
        rs.next();
        assertEquals(ValueGeometry.get("LINESTRING (95 150, 105 150)").getGeometry(), rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'::GEOMETRY, 5, -10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (90 150, 110 150)")));
        rs.close();
    }
    
    @Test
    public void test_ST_Expand5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 10);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))")));
        rs.close();
    }

    @Test
    public void test_ST_ExtrudeLineString() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('LINESTRING (0 0, 1 0)'::GEOMETRY, 10);");
        rs.next();
        //Test if the wall is created
        assertGeometryEquals("MULTIPOLYGON(((0 0 0, 0 0 10, 1 0 10, 1 0 0, 0 0 0)))", ValueGeometry.getFromGeometry(((Geometry) rs.getObject(1)).getGeometryN(1)).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'::GEOMETRY, 10);");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertGeometryEquals("POLYGON ((0 0 0, 0 1 0, 1 1 0, 1 0 0, 0 0 0))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(0)).getBytes());

        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), "
                + "((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), "
                + "((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0))))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(1)).getBytes());

        //Test the roof
        assertGeometryEquals("POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(2)).getBytes());

        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWithHole() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10),"
                + " (1 3, 3 3, 3 1, 1 1, 1 3))'::GEOMETRY, 10);");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertGeometryEquals("POLYGON ((0 10 0, 10 10 0, 10 0 0, 0 0 0, 0 10 0), (1 3 0, 1 1 0, 3 1 0, 3 3 0, 1 3 0))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(0)).getBytes());

        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON (((0 10 0, 0 10 10, 10 10 10, 10 10 0, 0 10 0)), ((10 10 0, 10 10 10, 10 0 10, 10 0 0, 10 10 0)), ((10 0 0, 10 0 10, 0 0 10, 0 0 0, 10 0 0)), ((0 0 0, 0 0 10, 0 10 10, 0 10 0, 0 0 0)), ((1 3 0, 1 3 10, 1 1 10, 1 1 0, 1 3 0)), ((1 1 0, 1 1 10, 3 1 10, 3 1 0, 1 1 0)), ((3 1 0, 3 1 10, 3 3 10, 3 3 0, 3 1 0)), ((3 3 0, 3 3 10, 1 3 10, 1 3 0, 3 3 0)))",
                ValueGeometry.getFromGeometry(outputGeom.getGeometryN(1)).getBytes());

        //Test the roof
        assertGeometryEquals("POLYGON ((0 10 10, 0 0 10, 10 0 10, 10 10 10, 0 10 10), (1 3 10, 3 3 10, 3 1 10, 1 1 10, 1 3 10))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(2)).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWalls() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'::GEOMETRY, 10, 1);");
        rs.next();
        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), ((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), ((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonRoof() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'::GEOMETRY, 10, 2);");
        rs.next();
        //Test the roof
        assertGeometryEquals("POLYGON((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MakePoint() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakePoint(1.4, -3.7), "
                + "ST_MakePoint(1.4, -3.7, 6.2);");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("POINT(1.4 -3.7)"), rs.getObject(1));
        assertEquals(WKT_READER.read("POINT(1.4 -3.7 6.2)"), rs.getObject(2));
        assertFalse(rs.next());
        rs.close();
    }
    
     @Test
    public void test_ST_Point() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Point(1.4, -3.7), "
                + "ST_Point(1.4, -3.7, 6.2);");
        assertTrue(rs.next());
        assertEquals(WKT_READER.read("POINT(1.4 -3.7)"), rs.getObject(1));
        assertEquals(WKT_READER.read("POINT(1.4 -3.7 6.2)"), rs.getObject(2));
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void test_ST_MakeEllipse() throws Exception {
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_MakeEllipse(ST_MakePoint(0, 0), 6, 4),"
                + "ST_MakeEllipse(ST_MakePoint(-1, 4), 2, 4),"
                + "ST_MakeEllipse(ST_MakePoint(4, -5), 4, 4),"
                + "ST_Buffer(ST_MakePoint(4, -5), 2);");
        assertTrue(rs.next());
        Polygon ellipse1 = (Polygon) rs.getObject(1);
        final Envelope ellipse1EnvelopeInternal = ellipse1.getEnvelopeInternal();
        assertEquals(101, ellipse1.getCoordinates().length);
        assertTrue(ellipse1EnvelopeInternal.centre().equals2D(new Coordinate(0, 0)));
        assertEquals(6, ellipse1EnvelopeInternal.getWidth(), 0);
        assertEquals(4, ellipse1EnvelopeInternal.getHeight(), 0);
        Polygon ellipse2 = (Polygon) rs.getObject(2);
        final Envelope ellipse2EnvelopeInternal = ellipse2.getEnvelopeInternal();
        assertEquals(101, ellipse2.getCoordinates().length);
        assertTrue(ellipse2EnvelopeInternal.centre().equals2D(new Coordinate(-1, 4)));
        assertEquals(2, ellipse2EnvelopeInternal.getWidth(), 0);
        assertEquals(4, ellipse2EnvelopeInternal.getHeight(), 0);
        Polygon circle = (Polygon) rs.getObject(3);
        final Envelope circleEnvelopeInternal = circle.getEnvelopeInternal();
        assertEquals(101, circle.getCoordinates().length);
        assertTrue(circleEnvelopeInternal.centre().equals2D(new Coordinate(4, -5)));
        assertEquals(4, circleEnvelopeInternal.getWidth(), 0);
        assertEquals(4, circleEnvelopeInternal.getHeight(), 0);
        Polygon bufferCircle = (Polygon) rs.getObject(4);
        // This test shows that the only difference between a circle
        // constructed using ST_MakeEllipse and a circle contructed using
        // ST_Buffer is the number of line segments in the approximation.
        // ST_MakeEllipse is more fine-grained (100 segments rather than 32).
        final Envelope bufferCircleEnvelopeInternal = bufferCircle.getEnvelopeInternal();
        assertEquals(33, bufferCircle.getCoordinates().length);
        assertTrue(bufferCircleEnvelopeInternal.centre().equals2D(circleEnvelopeInternal.centre()));
        assertEquals(circleEnvelopeInternal.getWidth(), bufferCircleEnvelopeInternal.getWidth(), 0);
        assertEquals(circleEnvelopeInternal.getHeight(), bufferCircleEnvelopeInternal.getHeight(), 0);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void test_ST_MakeLine() throws Exception {
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_MakeLine('POINT(1 2 3)'::Geometry, 'POINT(4 5 6)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'POINT(4 5)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'MULTIPOINT(4 5, 12 9)'::Geometry), "
                + "ST_MakeLine('MULTIPOINT(1 2, 17 6)'::Geometry, 'MULTIPOINT(4 5, 7 9, 18 -1)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'POINT(4 5)'::Geometry, 'POINT(7 8)'::Geometry);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2 3, 4 5 6)", rs.getBytes(1));
        assertGeometryEquals("LINESTRING(1 2, 4 5)", rs.getBytes(2));
        assertGeometryEquals("LINESTRING(1 2, 4 5, 12 9)", rs.getBytes(3));
        assertGeometryEquals("LINESTRING(1 2, 17 6, 4 5, 7 9, 18 -1)", rs.getBytes(4));
        assertGeometryEquals("LINESTRING(1 2, 4 5, 7 8)", rs.getBytes(5));
        assertFalse(rs.next());
        rs = st.executeQuery("SELECT "
                + "ST_MakeLine('MULTIPOINT(1 2, 3 4)'::Geometry);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2, 3 4)", rs.getBytes(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Point);"
                + "INSERT INTO input_table VALUES"
                + "('POINT(1 2)'::Geometry),"
                + "('POINT(3 4)'::Geometry),"
                + "('POINT(5 6)'::Geometry),"
                + "('POINT(7 8)'::Geometry),"
                + "('POINT(9 10)'::Geometry);");
        rs = st.executeQuery("SELECT ST_MakeLine(ST_Accum(point)) FROM input_table;");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(1 2, 3 4, 5 6, 7 8, 9 10)", rs.getBytes(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(point Geometry);"
                + "INSERT INTO input_table VALUES"
                + "('POINT(5 5)'::Geometry),"
                + "('MULTIPOINT(1 2, 7 9, 18 -4)'::Geometry),"
                + "('POINT(3 4)'::Geometry),"
                + "('POINT(99 3)'::Geometry);");
        rs = st.executeQuery("SELECT ST_MakeLine(ST_Accum(point)) FROM input_table;");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(5 5, 1 2, 7 9, 18 -4, 3 4, 99 3)", rs.getBytes(1));
        assertFalse(rs.next());
        st.execute("DROP TABLE IF EXISTS input_table;"
                + "CREATE TABLE input_table(multi_point MultiPoint);"
                + "INSERT INTO input_table VALUES"
                + "('MULTIPOINT(5 5, 1 2, 3 4, 99 3)'::Geometry), "
                + "('MULTIPOINT(-5 12, 11 22, 34 41, 65 124)'::Geometry),"
                + "('MULTIPOINT(1 12, 5 -21, 9 41, 32 124)'::Geometry);");
        rs = st.executeQuery("SELECT ST_MakeLine(ST_Accum(multi_point)) FROM input_table;");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRING(5 5, 1 2, 3 4, 99 3, "
                + "-5 12, 11 22, 34 41, 65 124, "
                + "1 12, 5 -21, 9 41, 32 124)", rs.getBytes(1));
        assertFalse(rs.next());
        rs.close();
        st.execute("DROP TABLE input_table;");
    }

    @Test
    public void test_ST_MakeLineNull() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeLine(null, 'POINT(4 5 6)')");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine('POINT(4 5 6)', null)");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine(null, null)");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine(null)");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
    }

    @Test
    public void test_ST_MakeEnvelope() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(
                WKT_READER.read("POLYGON((0 0, 1 0 0, 1 1 , 0 1, 0 0))")));
        rs.close();
    }

    @Test
    public void test_ST_MakeEnvelopeSRID() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1, 4326);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equalsExact(
                WKT_READER.read("POLYGON((0 0, 1 0 0, 1 1 , 0 1, 0 0))")));
        assertTrue(((Geometry) rs.getObject(1)).getSRID() == 4326);
        rs.close();
    }

    @Test
    public void test_ST_MakeGrid() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void testST_MakeGridFromGeometry() throws Exception {
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    /**
     * Test to create a regular square grid from a subquery
     *
     * @throws Exception
     */
    @Test
    public void testST_MakeGridFromSubquery1() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0 ))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select the_geom from input_table), 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    /**
     * Test to create a regular square grid from a complex subquery
     *
     * @throws Exception
     */
    @Test
    public void testST_MakeGridFromSubquery2() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 1 0, 1 1, 0 0 ))'));"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((1 1, 2 2, 1 2, 1 1 ))'));");
        try {
            st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select the_geom from input_table), 1, 1);");
        } catch (Exception e) {
            assertTrue(true);
        }
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid((select st_union(st_accum(the_geom)) from input_table), 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGridPoints() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegridpoints('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(0.5 0.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1.5 0.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(0.5 1.5)")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POINT(1.5 1.5)")));
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGrid2() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 2 0, 3 2, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 6);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((2 0, 3 0, 3 1, 2 1, 2 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((2 1, 3 1, 3 2, 2 2, 2 1))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGrid3() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 1.4 0, 1 0.5, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 0.5, 0.5);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 3);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 0.5 0, 0.5 0.5, 0 0.5, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0.5 0, 1 0, 1 0.5, 0.5 0.5, 0.5 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 1.5 0, 1.5 0.5, 1 0.5, 1 0))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGrid4() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry);"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON ((0 0, 2 0, 1 1, 0 0))'));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 0.5);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0, 1 0, 1 0.5, 0 0.5, 0 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((1 0, 2 0, 2 0.5, 1 0.5, 1 0))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON((0 0.5, 1 0.5, 1 1, 0 1, 0 0.5))")));
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((1 0.5, 2 0.5, 2 1, 1 1, 1 0.5))")));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakePolygon1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250, 100 250)'::GEOMETRY );");
        rs.next();
        assertGeometryEquals("POLYGON ((100 250, 100 350, 200 350, 200 250, 100 250))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MakePolygon2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250, 100 250)'::GEOMETRY, "
                + "'LINESTRING(120 320, 150 320, 150 300, 120 300, 120 320)'::GEOMETRY );");
        rs.next();
        assertGeometryEquals("POLYGON ((100 250, 100 350, 200 350, 200 250, 100 250), \n"
                + "  (120 320, 150 320, 150 300, 120 300, 120 320))", rs.getBytes(1));
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_MakePolygon3() throws Throwable {
        try {
            st.execute("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250)'::GEOMETRY, "
                    + "'LINESTRING(120 320, 150 320, 150 300, 120 300, 120 320)'::GEOMETRY );");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_MakePolygon4() throws Throwable {
        try {
            st.execute("SELECT ST_MakePolygon('POINT (100 250)'::GEOMETRY );");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ST_MakePolygon5() throws Throwable {
        try {
            st.execute("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250)'::GEOMETRY);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    @Test
    public void test_ST_OctogonalEnvelope1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('POLYGON ((170 350, 95 214, 220 120, 210 210, 159 205, 170 240, 170 350))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((95 214, 95 275, 170 350, 220 300, 220 120, 189 120, 95 214))")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('LINESTRING (50 210, 140 290, 120 120, 210 110)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((50 190, 50 210, 130 290, 140 290, 210 220, 210 110, 130 110, 50 190))")));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('MULTIPOINT ((230 220), (193 205))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((193 205, 208 220, 230 220, 215 205, 193 205))")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('MULTIPOINT ((230 220), (193 205))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("LINESTRING (230 220, 193 205)")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('POLYGON ((150 290, 110 210, 280 130, 280 250, 235 221, 150 290))'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((279.99999999999693 129.99999999999395, "
                + "326.23229461756006 228.24362606231597, 156.23229461756213 308.24362606231944, "
                + "109.99999999999888 209.99999999999753, 279.99999999999693 129.99999999999395))")));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'::GEOMETRY);");
        rs.next();
        assertTrue(((Geometry) rs.getObject(1)).equals(WKT_READER.read("POLYGON ((125.65411764705883 347.6564705882353, "
                + "8.571764705882353 252.52705882352942, "
                + "152.91764705882352 74.87058823529412, 270 170, 125.65411764705883 347.6564705882353))")));
        rs.close();
    }


    @Test
    public void test_ST_RingBuffer1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POINT(10 10)'::GEOMETRY, 10, 3);");
        rs.next();
        assertGeometryBarelyEquals("MULTIPOLYGON (((20 10, 19.807852804032304 8.049096779838717, "
                + "19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803978, "
                + "17.071067811865476 2.9289321881345254, 15.555702330196024 1.6853038769745474, "
                + "13.826834323650898 0.7612046748871322, 11.950903220161283 0.1921471959676957, "
                + "10 0, 8.049096779838719 0.1921471959676957, 6.173165676349103 0.7612046748871322, "
                + "4.44429766980398 1.6853038769745474, 2.9289321881345254 2.9289321881345245, "
                + "1.6853038769745474 4.444297669803978, 0.7612046748871322 6.173165676349106, "
                + "0.1921471959676939 8.049096779838722, 0 10.000000000000007, 0.1921471959676975 11.950903220161292, "
                + "0.7612046748871375 13.826834323650909, 1.6853038769745545 15.555702330196034, "
                + "2.928932188134537 17.071067811865486, 4.444297669803992 18.314696123025463, "
                + "6.173165676349122 19.238795325112875, 8.04909677983874 19.807852804032308, 10.000000000000025 20, "
                + "11.950903220161308 19.8078528040323, 13.826834323650925 19.238795325112857, "
                + "15.555702330196048 18.314696123025435, 17.071067811865497 17.07106781186545, "
                + "18.31469612302547 15.555702330195993, 19.238795325112882 13.826834323650862, 19.80785280403231 "
                + "11.950903220161244, 20 10)), ((30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 "
                + "2.346331352698204, 26.629392246050905 -1.1114046603920436, 24.14213562373095 -4.142135623730949, "
                + "21.111404660392047 -6.629392246050905, 17.653668647301796 -8.477590650225736, 13.901806440322567 "
                + "-9.615705608064609, 10.000000000000002 -10, 6.098193559677436 -9.615705608064609, 2.346331352698206 "
                + "-8.477590650225736, -1.11140466039204 -6.629392246050905, -4.142135623730949 -4.142135623730951, "
                + "-6.629392246050905 -1.1114046603920436, -8.477590650225736 2.3463313526982112, "
                + "-9.615705608064612 6.098193559677446, -10 10.000000000000016, "
                + "-9.615705608064605 13.901806440322584, -8.477590650225725 17.653668647301817, -6.629392246050891 21.11140466039207, -4.142135623730926 24.142135623730972, -1.1114046603920151 26.629392246050926, 2.3463313526982423 28.47759065022575, 6.098193559677479 29.615705608064616, 10.00000000000005 30, 13.901806440322618 29.615705608064598, 17.65366864730185 28.477590650225714, 21.111404660392097 26.62939224605087, 24.142135623730997 24.1421356237309, 26.629392246050944 21.111404660391987, 28.477590650225764 17.653668647301725, 29.615705608064623 13.901806440322488, 30 10), (20 10, 19.80785280403231 11.950903220161244, 19.238795325112882 13.826834323650862, 18.31469612302547 15.555702330195993, 17.071067811865497 17.07106781186545, 15.555702330196048 18.314696123025435, 13.826834323650925 19.238795325112857, 11.950903220161308 19.8078528040323, 10.000000000000025 20, 8.04909677983874 19.807852804032308, 6.173165676349122 19.238795325112875, 4.444297669803992 18.314696123025463, 2.928932188134537 17.071067811865486, 1.6853038769745545 15.555702330196034, 0.7612046748871375 13.826834323650909, 0.1921471959676975 11.950903220161292, 0 10.000000000000007, 0.1921471959676939 8.049096779838722, 0.7612046748871322 6.173165676349106, 1.6853038769745474 4.444297669803978, 2.9289321881345254 2.9289321881345245, 4.44429766980398 1.6853038769745474, 6.173165676349103 0.7612046748871322, 8.049096779838719 0.1921471959676957, 10 0, 11.950903220161283 0.1921471959676957, 13.826834323650898 0.7612046748871322, 15.555702330196024 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803978, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10)), ((40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.944088369076354 -6.667106990588067, 31.213203435596427 -11.213203435596423, 26.667106990588067 -14.944088369076358, 21.480502970952696 -17.716385975338603, 15.85270966048385 -19.423558412096913, 10.000000000000002 -20, 4.147290339516154 -19.423558412096913, -1.480502970952692 -17.716385975338603, -6.66710699058806 -14.944088369076361, -11.213203435596423 -11.213203435596427, -14.944088369076361 -6.667106990588067, -17.716385975338607 -1.4805029709526831, -19.423558412096916 4.147290339516168, -20 10.000000000000023, -19.423558412096906 15.852709660483876, -17.71638597533859 21.480502970952728, -14.944088369076333 26.667106990588103, -11.213203435596391 31.213203435596462, -6.667106990588021 34.94408836907638, -1.480502970952637 37.716385975338625, 4.147290339516219 39.423558412096924, 10.000000000000075 40, 15.852709660483928 39.423558412096895, 21.480502970952774 37.71638597533857, 26.667106990588145 34.944088369076304, 31.213203435596498 31.213203435596355, 34.94408836907642 26.66710699058798, 37.716385975338646 21.48050297095259, 39.42355841209694 15.85270966048373, 40 10), (30 10, 29.615705608064623 13.901806440322488, 28.477590650225764 17.653668647301725, 26.629392246050944 21.111404660391987, 24.142135623730997 24.1421356237309, 21.111404660392097 26.62939224605087, 17.65366864730185 28.477590650225714, 13.901806440322618 29.615705608064598, 10.00000000000005 30, 6.098193559677479 29.615705608064616, 2.3463313526982423 28.47759065022575, -1.1114046603920151 26.629392246050926, -4.142135623730926 24.142135623730972, -6.629392246050891 21.11140466039207, -8.477590650225725 17.653668647301817, -9.615705608064605 13.901806440322584, -10 10.000000000000016, -9.615705608064612 6.098193559677446, -8.477590650225736 2.3463313526982112, -6.629392246050905 -1.1114046603920436, -4.142135623730949 -4.142135623730951, -1.11140466039204 -6.629392246050905, 2.346331352698206 -8.477590650225736, 6.098193559677436 -9.615705608064609, 10.000000000000002 -10, 13.901806440322567 -9.615705608064609, 17.653668647301796 -8.477590650225736, 21.111404660392047 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920436, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10)))"
                , rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBuffer2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3);");
        rs.next();
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803979, 17.071067811865476 2.9289321881345254, 15.55570233019602 1.6853038769745474, 13.826834323650894 0.7612046748871304, 11.950903220161276 0.1921471959676939, 10 0, -10 0, -11.950903220161287 0.1921471959676975, -13.826834323650903 0.7612046748871357, -15.555702330196022 1.6853038769745474, -17.071067811865476 2.9289321881345254, -18.314696123025456 4.44429766980398, -19.238795325112868 6.173165676349104, -19.807852804032304 8.049096779838717, -20 10.000000000000002, -19.807852804032304 11.950903220161287, -19.238795325112868 13.8268343236509, -18.314696123025453 15.555702330196022, -17.071067811865476 17.071067811865476, -15.55570233019602 18.314696123025453, -13.826834323650893 19.238795325112868, -11.950903220161276 19.807852804032308, -10 20, 10 20)), ((10 30, 13.901806440322567 29.61570560806461, 17.653668647301796 28.477590650225736, 21.111404660392047 26.629392246050905, 24.14213562373095 24.14213562373095, 26.629392246050905 21.111404660392044, 28.477590650225736 17.653668647301796, 29.61570560806461 13.901806440322565, 30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 2.346331352698204, 26.629392246050905 -1.1114046603920418, 24.14213562373095 -4.142135623730949, 21.11140466039204 -6.629392246050905, 17.65366864730179 -8.47759065022574, 13.901806440322552 -9.615705608064612, 10 -10, -10 -10, -13.901806440322574 -9.615705608064605, -17.653668647301807 -8.477590650225729, -21.111404660392044 -6.629392246050905, -24.142135623730955 -4.142135623730949, -26.62939224605091 -1.11140466039204, -28.477590650225736 2.346331352698207, -29.61570560806461 6.098193559677433, -30 10.000000000000002, -29.61570560806461 13.901806440322572, -28.477590650225736 17.6536686473018, -26.629392246050905 21.111404660392044, -24.14213562373095 24.14213562373095, -21.11140466039204 26.629392246050905, -17.653668647301785 28.47759065022574, -13.90180644032255 29.615705608064612, -10 30, 10 30), (10 20, -10 20, -11.950903220161276 19.807852804032308, -13.826834323650893 19.238795325112868, -15.55570233019602 18.314696123025453, -17.071067811865476 17.071067811865476, -18.314696123025453 15.555702330196022, -19.238795325112868 13.8268343236509, -19.807852804032304 11.950903220161287, -20 10.000000000000002, -19.807852804032304 8.049096779838717, -19.238795325112868 6.173165676349104, -18.314696123025456 4.44429766980398, -17.071067811865476 2.9289321881345254, -15.555702330196022 1.6853038769745474, -13.826834323650903 0.7612046748871357, -11.950903220161287 0.1921471959676975, -10 0, 10 0, 11.950903220161276 0.1921471959676939, 13.826834323650894 0.7612046748871304, 15.55570233019602 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803979, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10, 19.807852804032304 11.950903220161283, 19.238795325112868 13.826834323650898, 18.314696123025453 15.555702330196022, 17.071067811865476 17.071067811865476, 15.555702330196024 18.314696123025453, 13.826834323650898 19.238795325112868, 11.950903220161283 19.807852804032304, 10 20)), ((10 40, 15.85270966048385 39.42355841209691, 21.480502970952696 37.7163859753386, 26.667106990588067 34.944088369076354, 31.213203435596427 31.213203435596423, 34.944088369076354 26.667106990588067, 37.7163859753386 21.480502970952692, 39.42355841209691 15.852709660483846, 40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.94408836907636 -6.6671069905880636, 31.213203435596427 -11.213203435596423, 26.667106990588064 -14.944088369076361, 21.48050297095268 -17.71638597533861, 15.85270966048383 -19.423558412096916, 10 -20, -10 -20, -15.85270966048386 -19.42355841209691, -21.48050297095271 -17.716385975338596, -26.667106990588067 -14.944088369076358, -31.21320343559643 -11.213203435596423, -34.94408836907637 -6.66710699058806, -37.71638597533861 -1.4805029709526902, -39.42355841209691 4.147290339516149, -40 10.000000000000004, -39.42355841209691 15.852709660483859, -37.7163859753386 21.4805029709527, -34.94408836907636 26.667106990588067, -31.213203435596423 31.213203435596427, -26.66710699058806 34.94408836907636, -21.480502970952678 37.71638597533861, -15.852709660483827 39.42355841209692, -10 40, 10 40), (10 30, -10 30, -13.90180644032255 29.615705608064612, -17.653668647301785 28.47759065022574, -21.11140466039204 26.629392246050905, -24.14213562373095 24.14213562373095, -26.629392246050905 21.111404660392044, -28.477590650225736 17.6536686473018, -29.61570560806461 13.901806440322572, -30 10.000000000000002, -29.61570560806461 6.098193559677433, -28.477590650225736 2.346331352698207, -26.62939224605091 -1.11140466039204, -24.142135623730955 -4.142135623730949, -21.111404660392044 -6.629392246050905, -17.653668647301807 -8.477590650225729, -13.901806440322574 -9.615705608064605, -10 -10, 10 -10, 13.901806440322552 -9.615705608064612, 17.65366864730179 -8.47759065022574, 21.11140466039204 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920418, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10, 29.61570560806461 13.901806440322565, 28.477590650225736 17.653668647301796, 26.629392246050905 21.111404660392044, 24.14213562373095 24.14213562373095, 21.111404660392047 26.629392246050905, "
                + "17.653668647301796 28.477590650225736, 13.901806440322567 29.61570560806461, 10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferEndCapSQUARE() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POINT(2 2)', 2, 2, 'endcap=square');");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON(((4 4, 4 0, 0 0, 0 4, 4 4)), ((6 6, 6 -2, -2 -2, -2 6, 6 6), (4 4, 0 4, 0 0, 4 0, 4 4)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferEndCapROUND() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'endcap=ROUND');");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803979, 17.071067811865476 2.9289321881345254, 15.55570233019602 1.6853038769745474, 13.826834323650894 0.7612046748871304, 11.950903220161276 0.1921471959676939, 10 0, -10 0, -11.950903220161287 0.1921471959676975, -13.826834323650903 0.7612046748871357, -15.555702330196022 1.6853038769745474, -17.071067811865476 2.9289321881345254, -18.314696123025456 4.44429766980398, -19.238795325112868 6.173165676349104, -19.807852804032304 8.049096779838717, -20 10.000000000000002, -19.807852804032304 11.950903220161287, -19.238795325112868 13.8268343236509, -18.314696123025453 15.555702330196022, -17.071067811865476 17.071067811865476, -15.55570233019602 18.314696123025453, -13.826834323650893 19.238795325112868, -11.950903220161276 19.807852804032308, -10 20, 10 20)), ((10 30, 13.901806440322567 29.61570560806461, 17.653668647301796 28.477590650225736, 21.111404660392047 26.629392246050905, 24.14213562373095 24.14213562373095, 26.629392246050905 21.111404660392044, 28.477590650225736 17.653668647301796, 29.61570560806461 13.901806440322565, 30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 2.346331352698204, 26.629392246050905 -1.1114046603920418, 24.14213562373095 -4.142135623730949, 21.11140466039204 -6.629392246050905, 17.65366864730179 -8.47759065022574, 13.901806440322552 -9.615705608064612, 10 -10, -10 -10, -13.901806440322574 -9.615705608064605, -17.653668647301807 -8.477590650225729, -21.111404660392044 -6.629392246050905, -24.142135623730955 -4.142135623730949, -26.62939224605091 -1.11140466039204, -28.477590650225736 2.346331352698207, -29.61570560806461 6.098193559677433, -30 10.000000000000002, -29.61570560806461 13.901806440322572, -28.477590650225736 17.6536686473018, -26.629392246050905 21.111404660392044, -24.14213562373095 24.14213562373095, -21.11140466039204 26.629392246050905, -17.653668647301785 28.47759065022574, -13.90180644032255 29.615705608064612, -10 30, 10 30), (10 20, -10 20, -11.950903220161276 19.807852804032308, -13.826834323650893 19.238795325112868, -15.55570233019602 18.314696123025453, -17.071067811865476 17.071067811865476, -18.314696123025453 15.555702330196022, -19.238795325112868 13.8268343236509, -19.807852804032304 11.950903220161287, -20 10.000000000000002, -19.807852804032304 8.049096779838717, -19.238795325112868 6.173165676349104, -18.314696123025456 4.44429766980398, -17.071067811865476 2.9289321881345254, -15.555702330196022 1.6853038769745474, -13.826834323650903 0.7612046748871357, -11.950903220161287 0.1921471959676975, -10 0, 10 0, 11.950903220161276 0.1921471959676939, 13.826834323650894 0.7612046748871304, 15.55570233019602 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803979, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10, 19.807852804032304 11.950903220161283, 19.238795325112868 13.826834323650898, 18.314696123025453 15.555702330196022, 17.071067811865476 17.071067811865476, 15.555702330196024 18.314696123025453, 13.826834323650898 19.238795325112868, 11.950903220161283 19.807852804032304, 10 20)), ((10 40, 15.85270966048385 39.42355841209691, 21.480502970952696 37.7163859753386, 26.667106990588067 34.944088369076354, 31.213203435596427 31.213203435596423, 34.944088369076354 26.667106990588067, 37.7163859753386 21.480502970952692, 39.42355841209691 15.852709660483846, 40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.94408836907636 -6.6671069905880636, 31.213203435596427 -11.213203435596423, 26.667106990588064 -14.944088369076361, 21.48050297095268 -17.71638597533861, 15.85270966048383 -19.423558412096916, 10 -20, -10 -20, -15.85270966048386 -19.42355841209691, -21.48050297095271 -17.716385975338596, -26.667106990588067 -14.944088369076358, -31.21320343559643 -11.213203435596423, -34.94408836907637 -6.66710699058806, -37.71638597533861 -1.4805029709526902, -39.42355841209691 4.147290339516149, -40 10.000000000000004, -39.42355841209691 15.852709660483859, -37.7163859753386 21.4805029709527, -34.94408836907636 26.667106990588067, -31.213203435596423 31.213203435596427, -26.66710699058806 34.94408836907636, -21.480502970952678 37.71638597533861, -15.852709660483827 39.42355841209692, -10 40, 10 40), (10 30, -10 30, -13.90180644032255 29.615705608064612, -17.653668647301785 28.47759065022574, -21.11140466039204 26.629392246050905, -24.14213562373095 24.14213562373095, -26.629392246050905 21.111404660392044, -28.477590650225736 17.6536686473018, -29.61570560806461 13.901806440322572, -30 10.000000000000002, -29.61570560806461 6.098193559677433, -28.477590650225736 2.346331352698207, -26.62939224605091 -1.11140466039204, -24.142135623730955 -4.142135623730949, -21.111404660392044 -6.629392246050905, -17.653668647301807 -8.477590650225729, -13.901806440322574 -9.615705608064605, -10 -10, 10 -10, 13.901806440322552 -9.615705608064612, 17.65366864730179 -8.47759065022574, 21.11140466039204 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920418, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10, 29.61570560806461 13.901806440322565, 28.477590650225736 17.653668647301796, 26.629392246050905 21.111404660392044, 24.14213562373095 24.14213562373095, 21.111404660392047 26.629392246050905, "
                + "17.653668647301796 28.477590650225736, 13.901806440322567 29.61570560806461, 10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test(expected = SQLException.class)
    public void test_ST_RingBufferEndCapBUTT() throws Exception {
        st.execute("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'endcap=BUTT');");
    }


    @Test
    public void test_ST_RingBufferNoHole() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POINT(2 2)', 2, 2, 'endcap=square', false);");
        assertTrue(rs.next());
        assertGeometryEquals("MULTIPOLYGON(((4 4, 4 0, 0 0, 0 4, 4 4)), ((6 6, 6 -2, -2 -2, -2 6, 6 6)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferComplex() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'endcap=ROUND quad_segs=4');");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 20, 13.826834323650898 19.238795325112868, 17.071067811865476 17.071067811865476, 19.238795325112868 13.826834323650898, 20 10, 19.238795325112868 6.173165676349102, 17.071067811865476 2.9289321881345254, 13.826834323650898 0.7612046748871322, 10 0, -10 0, -13.826834323650903 0.7612046748871357, -17.071067811865476 2.9289321881345254, -19.238795325112868 6.173165676349104, -20 10.000000000000002, -19.238795325112868 13.8268343236509, -17.071067811865476 17.071067811865476, -13.826834323650896 19.238795325112868, -10 20, 10 20)),"
                + "  ((10 30, 17.653668647301796 28.477590650225736, 24.14213562373095 24.14213562373095, 28.477590650225736 17.653668647301796, 30 10, 28.477590650225736 2.346331352698204, 24.14213562373095 -4.142135623730949, 17.653668647301796 -8.477590650225736, 10 -10, -10 -10, -17.653668647301807 -8.477590650225729, -24.142135623730955 -4.142135623730949, -28.477590650225736 2.346331352698207, -30 10.000000000000002, -28.477590650225736 17.6536686473018, -24.14213562373095 24.14213562373095, -17.653668647301792 28.477590650225736, -10 30, 10 30),"
                + "  (10 20, -10 20, -13.826834323650896 19.238795325112868, -17.071067811865476 17.071067811865476, -19.238795325112868 13.8268343236509, -20 10.000000000000002, -19.238795325112868 6.173165676349104, -17.071067811865476 2.9289321881345254, -13.826834323650903 0.7612046748871357, -10 0, 10 0, 13.826834323650898 0.7612046748871322, 17.071067811865476 2.9289321881345254, 19.238795325112868 6.173165676349102, 20 10, 19.238795325112868 13.826834323650898, 17.071067811865476 17.071067811865476, 13.826834323650898 19.238795325112868, 10 20)),"
                + "  ((10 40, 21.480502970952696 37.7163859753386, 31.213203435596427 31.213203435596423, 37.7163859753386 21.480502970952692, 40 10, 37.7163859753386 -1.4805029709526938, 31.213203435596427 -11.213203435596423, 21.480502970952696 -17.716385975338603, 10 -20, -10 -20, -21.48050297095271 -17.716385975338596, -31.21320343559643 -11.213203435596423, -37.71638597533861 -1.4805029709526902, -40 10.000000000000004, -37.7163859753386 21.4805029709527, -31.213203435596423 31.213203435596427, -21.480502970952692 37.7163859753386, -10 40, 10 40),"
                + "  (10 30, -10 30, -17.653668647301792 28.477590650225736, -24.14213562373095 24.14213562373095, -28.477590650225736 17.6536686473018, -30 10.000000000000002, -28.477590650225736 2.346331352698207, -24.142135623730955 -4.142135623730949, -17.653668647301807 -8.477590650225729, -10 -10, 10 -10, 17.653668647301796 -8.477590650225736, 24.14213562373095 -4.142135623730949, 28.477590650225736 2.346331352698204, 30 10, 28.477590650225736 17.653668647301796, 24.14213562373095 24.14213562373095, 17.653668647301796 28.477590650225736, 10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferComplex2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'endcap=ROUND');");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803979, 17.071067811865476 2.9289321881345254, 15.55570233019602 1.6853038769745474, 13.826834323650894 0.7612046748871304, 11.950903220161276 0.1921471959676939, 10 0, -10 0, -11.950903220161287 0.1921471959676975, -13.826834323650903 0.7612046748871357, -15.555702330196022 1.6853038769745474, -17.071067811865476 2.9289321881345254, -18.314696123025456 4.44429766980398, -19.238795325112868 6.173165676349104, -19.807852804032304 8.049096779838717, -20 10.000000000000002, -19.807852804032304 11.950903220161287, -19.238795325112868 13.8268343236509, -18.314696123025453 15.555702330196022, -17.071067811865476 17.071067811865476, -15.55570233019602 18.314696123025453, -13.826834323650893 19.238795325112868, -11.950903220161276 19.807852804032308, -10 20, 10 20)),"
                + "  ((10 30, 13.901806440322567 29.61570560806461, 17.653668647301796 28.477590650225736, 21.111404660392047 26.629392246050905, 24.14213562373095 24.14213562373095, 26.629392246050905 21.111404660392044, 28.477590650225736 17.653668647301796, 29.61570560806461 13.901806440322565, 30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 2.346331352698204, 26.629392246050905 -1.1114046603920418, 24.14213562373095 -4.142135623730949, 21.11140466039204 -6.629392246050905, 17.65366864730179 -8.47759065022574, 13.901806440322552 -9.615705608064612, 10 -10, -10 -10, -13.901806440322574 -9.615705608064605, -17.653668647301807 -8.477590650225729, -21.111404660392044 -6.629392246050905, -24.142135623730955 -4.142135623730949, -26.62939224605091 -1.11140466039204, -28.477590650225736 2.346331352698207, -29.61570560806461 6.098193559677433, -30 10.000000000000002, -29.61570560806461 13.901806440322572, -28.477590650225736 17.6536686473018, -26.629392246050905 21.111404660392044, -24.14213562373095 24.14213562373095, -21.11140466039204 26.629392246050905, -17.653668647301785 28.47759065022574, -13.90180644032255 29.615705608064612, -10 30, 10 30),"
                + "  (10 20, -10 20, -11.950903220161276 19.807852804032308, -13.826834323650893 19.238795325112868, -15.55570233019602 18.314696123025453, -17.071067811865476 17.071067811865476, -18.314696123025453 15.555702330196022, -19.238795325112868 13.8268343236509, -19.807852804032304 11.950903220161287, -20 10.000000000000002, -19.807852804032304 8.049096779838717, -19.238795325112868 6.173165676349104, -18.314696123025456 4.44429766980398, -17.071067811865476 2.9289321881345254, -15.555702330196022 1.6853038769745474, -13.826834323650903 0.7612046748871357, -11.950903220161287 0.1921471959676975, -10 0, 10 0, 11.950903220161276 0.1921471959676939, 13.826834323650894 0.7612046748871304, 15.55570233019602 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803979, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10, 19.807852804032304 11.950903220161283, 19.238795325112868 13.826834323650898, 18.314696123025453 15.555702330196022, 17.071067811865476 17.071067811865476, 15.555702330196024 18.314696123025453, 13.826834323650898 19.238795325112868, 11.950903220161283 19.807852804032304, 10 20)),"
                + "  ((10 40, 15.85270966048385 39.42355841209691, 21.480502970952696 37.7163859753386, 26.667106990588067 34.944088369076354, 31.213203435596427 31.213203435596423, 34.944088369076354 26.667106990588067, 37.7163859753386 21.480502970952692, 39.42355841209691 15.852709660483846, 40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.94408836907636 -6.6671069905880636, 31.213203435596427 -11.213203435596423, 26.667106990588064 -14.944088369076361, 21.48050297095268 -17.71638597533861, 15.85270966048383 -19.423558412096916, 10 -20, -10 -20, -15.85270966048386 -19.42355841209691, -21.48050297095271 -17.716385975338596, -26.667106990588067 -14.944088369076358, -31.21320343559643 -11.213203435596423, -34.94408836907637 -6.66710699058806, -37.71638597533861 -1.4805029709526902, -39.42355841209691 4.147290339516149, -40 10.000000000000004, -39.42355841209691 15.852709660483859, -37.7163859753386 21.4805029709527, -34.94408836907636 26.667106990588067, -31.213203435596423 31.213203435596427, -26.66710699058806 34.94408836907636, -21.480502970952678 37.71638597533861, -15.852709660483827 39.42355841209692, -10 40, 10 40),"
                + "  (10 30, -10 30, -13.90180644032255 29.615705608064612, -17.653668647301785 28.47759065022574, -21.11140466039204 26.629392246050905, -24.14213562373095 24.14213562373095, -26.629392246050905 21.111404660392044, -28.477590650225736 17.6536686473018, -29.61570560806461 13.901806440322572, -30 10.000000000000002, -29.61570560806461 6.098193559677433, -28.477590650225736 2.346331352698207, -26.62939224605091 -1.11140466039204, -24.142135623730955 -4.142135623730949, -21.111404660392044 -6.629392246050905, -17.653668647301807 -8.477590650225729, -13.901806440322574 -9.615705608064605, -10 -10, 10 -10, 13.901806440322552 -9.615705608064612, 17.65366864730179 -8.47759065022574, 21.11140466039204 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920418, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10, 29.61570560806461 13.901806440322565, 28.477590650225736 17.653668647301796, 26.629392246050905 21.111404660392044, 24.14213562373095 24.14213562373095, 21.111404660392047 26.629392246050905, 17.653668647301796 28.477590650225736, 13.901806440322567 29.61570560806461, 10 30)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferComplex3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, -10, 3,'endcap=ROUND');");
        assertTrue(rs.next());
        assertTrue(rs.getString(1).equalsIgnoreCase("MULTIPOLYGON EMPTY"));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferComplex4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))'::GEOMETRY, 1, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((9 20, 9.01921471959677 20.195090322016128, 9.076120467488714 20.38268343236509, 9.168530387697455 20.5555702330196, 9.292893218813452 20.707106781186546, 9.444429766980399 20.831469612302545, 9.61731656763491 20.923879532511286, 9.804909677983872 20.980785280403232, 10 21, 20 21, 20.195090322016128 20.980785280403232, 20.38268343236509 20.923879532511286, 20.5555702330196 20.831469612302545, 20.707106781186546 20.707106781186546, 20.831469612302545 20.5555702330196, 20.923879532511286 20.38268343236509, 20.980785280403232 20.195090322016128, 21 20, 21 10, 20.980785280403232 9.804909677983872, 20.923879532511286 9.61731656763491, 20.831469612302545 9.444429766980399, 20.707106781186546 9.292893218813452, 20.5555702330196 9.168530387697455, 20.38268343236509 9.076120467488714, 20.195090322016128 9.01921471959677, 20 9, 10 9, 9.804909677983872 9.01921471959677, 9.61731656763491 9.076120467488714, 9.444429766980399 9.168530387697455, 9.292893218813452 9.292893218813452, 9.168530387697455 9.444429766980399, 9.076120467488714 9.61731656763491, 9.01921471959677 9.804909677983872, 9 10, 9 20), (10 20, 10 10, 20 10, 20 20, 10 20)), ((8 20, 8.03842943919354 20.390180644032256, 8.152240934977426 20.76536686473018, 8.33706077539491 21.111140466039203, 8.585786437626904 21.414213562373096, 8.888859533960796 21.66293922460509, 9.23463313526982 21.847759065022572, 9.609819355967744 21.96157056080646, 10 22, 20 22, 20.390180644032256 21.96157056080646, 20.76536686473018 21.847759065022572, 21.111140466039206 21.66293922460509, 21.414213562373096 21.414213562373096, 21.66293922460509 21.111140466039203, 21.847759065022572 20.76536686473018, 21.96157056080646 20.390180644032256, 22 20, 22 10, 21.96157056080646 9.609819355967744, 21.847759065022572 9.23463313526982, 21.66293922460509 8.888859533960796, 21.414213562373096 8.585786437626904, 21.111140466039206 8.33706077539491, 20.76536686473018 8.152240934977426, 20.390180644032256 8.03842943919354, 20 8, 10 8, 9.609819355967742 8.03842943919354, 9.234633135269819 8.152240934977428, 8.888859533960796 8.33706077539491, 8.585786437626904 8.585786437626904, 8.337060775394908 8.888859533960796, 8.152240934977426 9.23463313526982, 8.03842943919354 9.609819355967744, 8 10, 8 20), (9 20, 9 10, 9.01921471959677 9.804909677983872, 9.076120467488714 9.61731656763491, 9.168530387697455 9.444429766980399, 9.292893218813452 9.292893218813452, 9.444429766980399 9.168530387697455, 9.61731656763491 9.076120467488714, 9.804909677983872 9.01921471959677, 10 9, 20 9, 20.195090322016128 9.01921471959677, 20.38268343236509 9.076120467488714, 20.5555702330196 9.168530387697455, 20.707106781186546 9.292893218813452, 20.831469612302545 9.444429766980399, 20.923879532511286 9.61731656763491, 20.980785280403232 9.804909677983872, 21 10, 21 20, 20.980785280403232 20.195090322016128, 20.923879532511286 20.38268343236509, 20.831469612302545 20.5555702330196, 20.707106781186546 20.707106781186546, 20.5555702330196 20.831469612302545, 20.38268343236509 20.923879532511286, 20.195090322016128 20.980785280403232, 20 21, 10 21, 9.804909677983872 20.980785280403232, 9.61731656763491 20.923879532511286, 9.444429766980399 20.831469612302545, 9.292893218813452 20.707106781186546, 9.168530387697455 20.5555702330196, 9.076120467488714 20.38268343236509, 9.01921471959677 20.195090322016128, 9 20)), ((7 20, 7.057644158790309 20.585270966048387, 7.22836140246614 21.14805029709527, 7.5055911630923635 21.666710699058807, 7.878679656440358 22.121320343559642, 8.333289300941194 22.494408836907635, 8.851949702904731 22.771638597533858, 9.414729033951616 22.94235584120969, 10 23, 20 23, 20.585270966048384 22.94235584120969, 21.14805029709527 22.771638597533858, 21.666710699058807 22.494408836907635, 22.121320343559642 22.121320343559642, 22.494408836907635 21.666710699058807, 22.771638597533858 21.14805029709527, 22.94235584120969 20.585270966048384, 23 20, 23 10, 22.94235584120969 9.414729033951616, 22.771638597533858 8.851949702904731, 22.494408836907635 8.333289300941193, 22.121320343559642 7.878679656440358, 21.666710699058807 7.505591163092364, 21.14805029709527 7.22836140246614, 20.585270966048384 7.057644158790309, 20 7, 10 7, 9.414729033951614 7.057644158790309, 8.85194970290473 7.22836140246614, 8.333289300941193 7.505591163092364, 7.878679656440357 7.878679656440358, 7.5055911630923635 8.333289300941194, 7.22836140246614 8.851949702904731, 7.057644158790309 9.414729033951614, 7 10, 7 20), (8 20, 8 10, 8.03842943919354 9.609819355967744, 8.152240934977426 9.23463313526982, 8.337060775394908 8.888859533960796, 8.585786437626904 8.585786437626904, 8.888859533960796 8.33706077539491, 9.234633135269819 8.152240934977428, 9.609819355967742 8.03842943919354, 10 8, 20 8, 20.390180644032256 8.03842943919354, 20.76536686473018 8.152240934977426, 21.111140466039206 8.33706077539491, 21.414213562373096 8.585786437626904, 21.66293922460509 8.888859533960796, 21.847759065022572 9.23463313526982, 21.96157056080646 9.609819355967744, 22 10, 22 20, 21.96157056080646 20.390180644032256, 21.847759065022572 20.76536686473018, 21.66293922460509 21.111140466039203, 21.414213562373096 21.414213562373096, 21.111140466039206 21.66293922460509, 20.76536686473018 21.847759065022572, 20.390180644032256 21.96157056080646, 20 22, 10 22, 9.609819355967744 21.96157056080646, 9.23463313526982 21.847759065022572, 8.888859533960796 21.66293922460509, 8.585786437626904 21.414213562373096, 8.33706077539491 21.111140466039203, 8.152240934977426 20.76536686473018, 8.03842943919354 20.390180644032256, 8 20)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferComplex5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))'::GEOMETRY, -1, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 19, 10 20, 11 20, 20 20, 20 10, 10 10, 10 19), (11 19, 11 11, 19 11, 19 19, 11 19)), ((11 19, 19 19, 19 11, 11 11, 11 19), (12 18, 12 12, 18 12, 18 18, 12 18)), ((12 18, 18 18, 18 12, 12 12, 12 18), (13 17, 13 13, 17 13, 17 17, 13 17)))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_RingBufferComplex6() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_RingBuffer('POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20),"
                + "  (12.1 18.1, 14.1 18.1, 14.1 16, 12.1 16, 12.1 18.1),"
                + "  (15.6 14, 18 14, 18 11.5, 15.6 11.5, 15.6 14))'::GEOMETRY, -1, 3);");
        assertTrue(rs.next());
        assertGeometryBarelyEquals("MULTIPOLYGON (((10 19, 10 20, 11 20, 20 20, 20 10, 10 10, 10 19), (11 19, 11 11, 19 11, 19 19, 11 19)), ((11 19, 19 19, 19 11, 11 11, 11 19), (12 18, 12 12, 18 12, 18 18, 12 18)), ((12 18, 18 18, 18 12, 12 12, 12 18), (13 17, 13 13, 17 13, 17 17, 13 17)))", rs.getObject(1));
        rs.close();
    }

}
