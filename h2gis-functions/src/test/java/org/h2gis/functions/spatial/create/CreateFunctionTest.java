/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.create;

import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.value.ValueGeometry;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.GeographyUtilities;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.TableLocation;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryBarelyEquals;
import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS, 2023
 */
public class CreateFunctionTest {
    private static Connection connection;
    private Statement st;
    private static final GeometryFactory FACTORY = new GeometryFactory();

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(CreateFunctionTest.class.getSimpleName());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void test_ST_BoundingCircle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle('SRID=4326;POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("SRID=4326;POLYGON ((366.4800710247679 257.5, 363.82882265008465 230.58142351196977, "
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
                + "366.4800710247679 257.5))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_BoundingCircle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_BoundingCircle('SRID=3426;LINESTRING (140 200, 170 150)'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("SRID=3426;POLYGON ((184.1547594742265 175, 183.59455894601797 "
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
                + "183.594558946018 180.6878114141295, 184.1547594742265 175))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MinimumBoundingCircle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumBoundingCircle('SRID=3426;POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("SRID=3426;POLYGON ((366.4800710247679 257.5, 363.82882265008465 230.58142351196977, "
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
                + "366.4800710247679 257.5))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('SRID=4326;POINT (100 150)'::GEOMETRY, 10, 10);");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 5, 10);");
        rs.next();
        assertGeometryEquals("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 5, -10);");
        rs.next();
        assertGeometryEquals("LINESTRING (95 150, 105 150)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'::GEOMETRY, 5, -10);");
        rs.next();
        assertGeometryEquals("LINESTRING (90 150, 110 150)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_Expand5() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Expand('POINT (100 150)'::GEOMETRY, 10);");
        rs.next();
        assertGeometryEquals("POLYGON ((90 140, 90 160, 110 160, 110 140, 90 140))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_ExtrudeLineString() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('LINESTRINGZ (0 0 0, 1 0 0)'::GEOMETRY, 10);");
        rs.next();
        //Test if the wall is created
        assertGeometryEquals("MULTIPOLYGONZ(((0 0 0, 0 0 10, 1 0 10, 1 0 0, 0 0 0)))", ValueGeometry.getFromGeometry(((Geometry) rs.getObject(1)).getGeometryN(1)).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygon() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGONZ((0 0 0, 1 0 0, 1 1 0, 0 1 0, 0 0 0))'::GEOMETRY, 10);");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertGeometryEquals("POLYGON Z((0 0 0, 0 1 0, 1 1 0, 1 0 0, 0 0 0))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(0)).getBytes());

        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGON Z (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), ((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), ((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0)))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(1)).getBytes());

        //Test the roof
        assertGeometryEquals("POLYGON Z((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(2)).getBytes());

        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWithHole() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGONZ ((0 10 0, 10 10 0, 10 0 0, 0 0 0, 0 10 0),"
                + " (1 3 0, 3 3 0, 3 1 0, 1 1 0, 1 3 0))'::GEOMETRY, 10);");
        rs.next();
        Geometry outputGeom = (Geometry) rs.getObject(1);
        //Test the floor
        assertGeometryEquals("POLYGON Z((0 10 0, 10 10 0, 10 0 0, 0 0 0, 0 10 0), (1 3 0, 1 1 0, 3 1 0, 3 3 0, 1 3 0))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(0)).getBytes());

        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGONZ (((0 10 0, 0 10 10, 10 10 10, 10 10 0, 0 10 0)), ((10 10 0, 10 10 10, 10 0 10, 10 0 0, 10 10 0)), ((10 0 0, 10 0 10, 0 0 10, 0 0 0, 10 0 0)), ((0 0 0, 0 0 10, 0 10 10, 0 10 0, 0 0 0)), ((1 3 0, 1 3 10, 1 1 10, 1 1 0, 1 3 0)), ((1 1 0, 1 1 10, 3 1 10, 3 1 0, 1 1 0)), ((3 1 0, 3 1 10, 3 3 10, 3 3 0, 3 1 0)), ((3 3 0, 3 3 10, 1 3 10, 1 3 0, 3 3 0)))",
                ValueGeometry.getFromGeometry(outputGeom.getGeometryN(1)).getBytes());

        //Test the roof
        assertGeometryEquals("POLYGONZ ((0 10 10, 0 0 10, 10 0 10, 10 10 10, 0 10 10), (1 3 10, 3 3 10, 3 1 10, 1 1 10, 1 3 10))", ValueGeometry.getFromGeometry(outputGeom.getGeometryN(2)).getBytes());
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonWalls() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGONZ((0 0 0, 1 0 0, 1 1 0, 0 1 0, 0 0 0))'::GEOMETRY, 10, 1);");
        rs.next();
        //Test if the walls are created
        assertGeometryEquals("MULTIPOLYGONz (((0 0 0, 0 0 10, 0 1 10, 0 1 0, 0 0 0)), ((0 1 0, 0 1 10, 1 1 10, 1 1 0, 0 1 0)), ((1 1 0, 1 1 10, 1 0 10, 1 0 0, 1 1 0)), ((1 0 0, 1 0 10, 0 0 10, 0 0 0, 1 0 0)))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_ExtrudePolygonRoof() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Extrude('POLYGONZ((0 0 0, 1 0 0, 1 1 0, 0 1 0, 0 0 0))'::GEOMETRY, 10, 2);");
        rs.next();
        //Test the roof
        assertGeometryEquals("POLYGONZ((0 0 10, 1 0 10, 1 1 10, 0 1 10, 0 0 10))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_MakePoint() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakePoint(1.4, -3.7), "
                + "ST_MakePoint(1.4, -3.7, 6.2);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(1.4 -3.7)", rs.getObject(1));
        assertGeometryEquals("POINTZ(1.4 -3.7 6.2)", rs.getObject(2));
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void test_ST_Point() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_Point(1.4, -3.7), "
                + "ST_Point(1.4, -3.7, 6.2);");
        assertTrue(rs.next());
        assertGeometryEquals("POINT(1.4 -3.7)", rs.getObject(1));
        assertGeometryEquals("POINTZ(1.4 -3.7 6.2)", rs.getObject(2));
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void test_ST_MakeEllipse() throws Exception {
        ResultSet rs = st.executeQuery("SELECT "
                + "ST_SETSRID(ST_MakeEllipse(ST_MakePoint(0, 0), 6, 4), 4326),"
                + "ST_MakeEllipse(ST_MakePoint(-1, 4), 2, 4),"
                + "ST_MakeEllipse(ST_MakePoint(4, -5), 4, 4),"
                + "ST_Buffer(ST_MakePoint(4, -5), 2);");
        assertTrue(rs.next());
        Polygon ellipse1 = (Polygon) rs.getObject(1);
        assertEquals(4326, ellipse1.getSRID());
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
                + "ST_MakeLine('POINTZ(1 2 3)'::Geometry, 'POINTZ(4 5 6)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'POINT(4 5)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'MULTIPOINT(4 5, 12 9)'::Geometry), "
                + "ST_MakeLine('MULTIPOINT(1 2, 17 6)'::Geometry, 'MULTIPOINT(4 5, 7 9, 18 -1)'::Geometry), "
                + "ST_MakeLine('POINT(1 2)'::Geometry, 'POINT(4 5)'::Geometry, 'POINT(7 8)'::Geometry);");
        assertTrue(rs.next());
        assertGeometryEquals("LINESTRINGZ(1 2 3, 4 5 6)", rs.getBytes(1));
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
                + "CREATE TABLE input_table(point GEOMETRY(Point));"
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
                + "CREATE TABLE input_table(multi_point GEOMETRY(MultiPoint));"
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
        ResultSet rs = st.executeQuery("SELECT ST_MakeLine(null, 'POINTZ(4 5 6)')");
        try {
            assertTrue(rs.next());
            assertNull(rs.getObject(1));
        } finally {
            rs.close();
        }
        rs = st.executeQuery("SELECT ST_MakeLine('POINTZ(4 5 6)', null)");
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
        assertGeometryEquals("SRID=0;POLYGON((0 0, 1 0, 1 1 , 0 1, 0 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeEnvelopeSRID() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeEnvelope(0,0, 1, 1, 4326);");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON((0 0, 1 0 , 1 1 , 0 1, 0 0))", rs.getObject(1));
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
        assertEquals(1111, rs.getMetaData().getColumnType(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void testST_MakeGridFromGeometry() throws Exception {
        st.execute("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 1);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    @Test
    public void testST_MakeGridFromGeometryLatLon1() throws Exception {
        Envelope env = new Envelope(0.0, 0.008983152841195214, 0.0, 0.008983152841195214);
        Envelope outPutEnv = GeographyUtilities.createEnvelope(new Coordinate(0.0, 0.0), 1000, 1000);
        assertEquals(env, outPutEnv);
        Geometry geom = FACTORY.toGeometry(outPutEnv);
        st.execute(String.format("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('srid=%s;%s'::GEOMETRY, 1000, 1000);", "4326", geom.toString()));
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 1);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((0 0, 0 0.008983152841195214, 0.0089831529516059 0.008983152841195214, 0.0089831529516059 0, 0 0))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    @Test
    public void testST_MakeGridFromGeometryLatLon2() throws Exception {
        Envelope env = new Envelope(0.0, 0.0008983152841195214, 0.0, 0.0008983152841195214);
        Envelope outPutEnv = GeographyUtilities.createEnvelope(new Coordinate(0.0, 0.0), 100, 100);
        assertEquals(env, outPutEnv);
        Geometry geom = FACTORY.toGeometry(outPutEnv);
        st.execute(String.format("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('srid=%s;%s'::GEOMETRY, 1000, 1000);", "4326", geom.toString()));
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 1);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((0 0, 0 0.008983152841195214, 0.00898315284229932 0.008983152841195214, 0.00898315284229932 0, 0 0))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    @Test
    public void testST_MakeGridFromGeometryLatLon3() throws Exception {
        Envelope env = new Envelope(0.0, 0.008983152841195214, 0.0, 0.008983152841195214);
        Envelope outPutEnv = GeographyUtilities.createEnvelope(new Coordinate(0.0, 0.0), 1000, 1000);
        assertEquals(env, outPutEnv);
        Geometry geom = FACTORY.toGeometry(outPutEnv);
        st.execute(String.format("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('srid=%s;%s'::GEOMETRY, 100, 100);", "4326", geom.toString()));
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 100);
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    /**
     * Test to create a regular square grid from a subquery
     *
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
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    /**
     * Test to create a regular square grid from a complex subquery
     *
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
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void test_ST_MakeGridSRID() throws Exception {
        st.execute("DROP TABLE IF EXISTS input_table,grid;"
                + "CREATE TABLE input_table(the_geom Geometry(POLYGON, 2154));"
                + "INSERT INTO input_table VALUES"
                + "(ST_GeomFromText('POLYGON((0 0, 2 0, 2 2, 0 0))', 2154));");
        st.execute("CREATE TABLE grid AS SELECT * FROM st_makegrid('input_table', 1, 1);");
        assertEquals(2154, GeometryTableUtilities.getSRID(connection, TableLocation.parse("GRID")));
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        assertEquals(1111, rs.getMetaData().getColumnType(1));
        rs.next();
        assertGeometryEquals("SRID=2154;POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("SRID=2154;POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("SRID=2154;POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("SRID=2154;POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
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
        assertGeometryEquals("POINT(0.5 0.5)", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POINT(1.5 0.5)", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POINT(0.5 1.5)", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POINT(1.5 1.5)", rs.getObject(1));
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
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((2 0, 3 0, 3 1, 2 1, 2 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((2 1, 3 1, 3 2, 2 2, 2 1))", rs.getObject(1));
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
        assertGeometryEquals("POLYGON((0 0, 0.5 0, 0.5 0.5, 0 0.5, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0.5 0, 1 0, 1 0.5, 0.5 0.5, 0.5 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 1.5 0, 1.5 0.5, 1 0.5, 1 0))", rs.getObject(1));
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
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 0.5, 0 0.5, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 0.5, 1 0.5, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 0.5, 1 0.5, 1 1, 0 1, 0 0.5))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON ((1 0.5, 2 0.5, 2 1, 1 1, 1 0.5))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE input_table, grid;");
    }

    @Test
    public void testST_MakeGridColumnsRows() throws Exception {
        st.execute("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 2, 2, true);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 4);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 0, 2 0, 2 1, 1 1, 1 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((0 1, 1 1, 1 2, 0 2, 0 1))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    @Test
    public void testST_MakeGridColumnsRows2() throws Exception {
        st.execute("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 1, 2, true);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 2);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("POLYGON ((0 0, 0 1, 2 1, 2 0, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON ((0 1, 0 2, 2 2, 2 1, 0 1)) ", rs.getObject(1));
        rs.next();
        rs.close();
        st.execute("DROP TABLE grid;");
    }

    @Test
    public void testST_MakeGridColumnsRows3() throws Exception {
        st.execute("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM st_makegrid('POLYGON((0 0, 2 0, 2 2, 0 0 ))'::GEOMETRY, 2, 1, true);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 2);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("POLYGON ((0 0, 0 2, 1 2, 1 0, 0 0))", rs.getObject(1));
        rs.next();
        assertGeometryEquals("POLYGON ((1 0, 1 2, 2 2, 2 0, 1 0))", rs.getObject(1));
        rs.next();
        rs.close();
        st.execute("DROP TABLE grid;");
    }


    @Test
    public void testST_MakeGridFromFunctionLatLon() throws Exception {
        st.execute("drop table if exists grid; CREATE TABLE grid AS SELECT * FROM " +
                "st_makegrid(ST_MAKEENVELOPE(0.0, 0.0, 0.008983152841195214, 0.008983152841195214, 4326), 1000, 1000);");
        ResultSet rs = st.executeQuery("select count(*) from grid;");
        rs.next();
        assertEquals(rs.getInt(1), 1);
        rs.close();
        rs = st.executeQuery("select * from grid;");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((0 0, 0 0.008983152841195214, 0.0089831529516059 0.008983152841195214, 0.0089831529516059 0, 0 0))", rs.getObject(1));
        rs.close();
        st.execute("DROP TABLE grid;");
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

    @Test
    public void test_ST_MakePolygon3() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            try {
                st.execute("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250)'::GEOMETRY, "
                        + "'LINESTRING(120 320, 150 320, 150 300, 120 300, 120 320)'::GEOMETRY );");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_MakePolygon4() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            try {
                st.execute("SELECT ST_MakePolygon('POINT (100 250)'::GEOMETRY );");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }


    @Test
    public void test_ST_MakePolygon5() {
        assertThrows(JdbcSQLNonTransientException.class, () -> {
            try {
                st.execute("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250)'::GEOMETRY);");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void test_ST_MakePolygon6() throws Exception {

        st.execute("DROP TABLE IF EXISTS LINES; CREATE TABLE lines (the_geom geometry); "
                + "insert into lines values ('LINESTRING(150 280, 190 280, 190 260, 150 260, 150 280)'), ('LINESTRING(120 320, 150 320, 150 300, 120 300, 120 320)');");
        ResultSet rs = st.executeQuery("SELECT ST_MakePolygon('LINESTRING (100 250, 100 350, 200 350, 200 250, 100 250)'::GEOMETRY, "
                + "st_accum(lines.the_geom) ) from lines ;");
        rs.next();
        assertGeometryEquals("POLYGON ((100 250, 100 350, 200 350, 200 250, 100 250), "
                + "(120 300, 150 300, 150 320, 120 320, 120 300), (150 260, 190 260, 190 280, 150 280, 150 260))", rs.getBytes(1));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((95 140, 95 160, 105 160, 105 140, 95 140))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('POLYGON ((170 350, 95 214, 220 120, 210 210, 159 205, 170 240, 170 350))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((95 214, 95 275, 170 350, 220 300, 220 120, 189 120, 95 214))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('LINESTRING (50 210, 140 290, 120 120, 210 110)'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((50 190, 50 210, 130 290, 140 290, 210 220, 210 110, 130 110, 50 190))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_OctogonalEnvelope4() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_OctogonalEnvelope('MULTIPOINT ((230 220), (193 205))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((193 205, 208 220, 230 220, 215 205, 193 205))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('MULTIPOINT ((230 220), (193 205))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("LINESTRING (230 220, 193 205)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('POLYGON ((150 290, 110 210, 280 130, 280 250, 235 221, 150 290))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("POLYGON ((279.99999999999693 129.99999999999395, "
                + "326.23229461756006 228.24362606231597, 156.23229461756213 308.24362606231944, "
                + "109.99999999999888 209.99999999999753, 279.99999999999693 129.99999999999395))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MinimumRectangle3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MinimumRectangle('LINESTRING (60 290, 67 300, 140 330, 136 319, 127 314, "
                + "116 307, 110 299, 103 289, 100 140, 110 142, 270 170)'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("POLYGON ((125.65411764705883 347.6564705882353, "
                + "8.571764705882353 252.52705882352942, "
                + "152.91764705882352 74.87058823529412, 270 170, 125.65411764705883 347.6564705882353))", rs.getObject(1));
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
    public void test_ST_RingBuffer3() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_SETSRID(ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3), 4326);");
        rs.next();
        assertGeometryBarelyEquals("SRID=4326;MULTIPOLYGON (((10 20, 11.950903220161283 19.807852804032304, 13.826834323650898 19.238795325112868, 15.555702330196024 18.314696123025453, 17.071067811865476 17.071067811865476, 18.314696123025453 15.555702330196022, 19.238795325112868 13.826834323650898, 19.807852804032304 11.950903220161283, 20 10, 19.807852804032304 8.049096779838717, 19.238795325112868 6.173165676349102, 18.314696123025453 4.444297669803979, 17.071067811865476 2.9289321881345254, 15.55570233019602 1.6853038769745474, 13.826834323650894 0.7612046748871304, 11.950903220161276 0.1921471959676939, 10 0, -10 0, -11.950903220161287 0.1921471959676975, -13.826834323650903 0.7612046748871357, -15.555702330196022 1.6853038769745474, -17.071067811865476 2.9289321881345254, -18.314696123025456 4.44429766980398, -19.238795325112868 6.173165676349104, -19.807852804032304 8.049096779838717, -20 10.000000000000002, -19.807852804032304 11.950903220161287, -19.238795325112868 13.8268343236509, -18.314696123025453 15.555702330196022, -17.071067811865476 17.071067811865476, -15.55570233019602 18.314696123025453, -13.826834323650893 19.238795325112868, -11.950903220161276 19.807852804032308, -10 20, 10 20)), ((10 30, 13.901806440322567 29.61570560806461, 17.653668647301796 28.477590650225736, 21.111404660392047 26.629392246050905, 24.14213562373095 24.14213562373095, 26.629392246050905 21.111404660392044, 28.477590650225736 17.653668647301796, 29.61570560806461 13.901806440322565, 30 10, 29.61570560806461 6.098193559677435, 28.477590650225736 2.346331352698204, 26.629392246050905 -1.1114046603920418, 24.14213562373095 -4.142135623730949, 21.11140466039204 -6.629392246050905, 17.65366864730179 -8.47759065022574, 13.901806440322552 -9.615705608064612, 10 -10, -10 -10, -13.901806440322574 -9.615705608064605, -17.653668647301807 -8.477590650225729, -21.111404660392044 -6.629392246050905, -24.142135623730955 -4.142135623730949, -26.62939224605091 -1.11140466039204, -28.477590650225736 2.346331352698207, -29.61570560806461 6.098193559677433, -30 10.000000000000002, -29.61570560806461 13.901806440322572, -28.477590650225736 17.6536686473018, -26.629392246050905 21.111404660392044, -24.14213562373095 24.14213562373095, -21.11140466039204 26.629392246050905, -17.653668647301785 28.47759065022574, -13.90180644032255 29.615705608064612, -10 30, 10 30), (10 20, -10 20, -11.950903220161276 19.807852804032308, -13.826834323650893 19.238795325112868, -15.55570233019602 18.314696123025453, -17.071067811865476 17.071067811865476, -18.314696123025453 15.555702330196022, -19.238795325112868 13.8268343236509, -19.807852804032304 11.950903220161287, -20 10.000000000000002, -19.807852804032304 8.049096779838717, -19.238795325112868 6.173165676349104, -18.314696123025456 4.44429766980398, -17.071067811865476 2.9289321881345254, -15.555702330196022 1.6853038769745474, -13.826834323650903 0.7612046748871357, -11.950903220161287 0.1921471959676975, -10 0, 10 0, 11.950903220161276 0.1921471959676939, 13.826834323650894 0.7612046748871304, 15.55570233019602 1.6853038769745474, 17.071067811865476 2.9289321881345254, 18.314696123025453 4.444297669803979, 19.238795325112868 6.173165676349102, 19.807852804032304 8.049096779838717, 20 10, 19.807852804032304 11.950903220161283, 19.238795325112868 13.826834323650898, 18.314696123025453 15.555702330196022, 17.071067811865476 17.071067811865476, 15.555702330196024 18.314696123025453, 13.826834323650898 19.238795325112868, 11.950903220161283 19.807852804032304, 10 20)), ((10 40, 15.85270966048385 39.42355841209691, 21.480502970952696 37.7163859753386, 26.667106990588067 34.944088369076354, 31.213203435596427 31.213203435596423, 34.944088369076354 26.667106990588067, 37.7163859753386 21.480502970952692, 39.42355841209691 15.852709660483846, 40 10, 39.42355841209691 4.147290339516153, 37.7163859753386 -1.4805029709526938, 34.94408836907636 -6.6671069905880636, 31.213203435596427 -11.213203435596423, 26.667106990588064 -14.944088369076361, 21.48050297095268 -17.71638597533861, 15.85270966048383 -19.423558412096916, 10 -20, -10 -20, -15.85270966048386 -19.42355841209691, -21.48050297095271 -17.716385975338596, -26.667106990588067 -14.944088369076358, -31.21320343559643 -11.213203435596423, -34.94408836907637 -6.66710699058806, -37.71638597533861 -1.4805029709526902, -39.42355841209691 4.147290339516149, -40 10.000000000000004, -39.42355841209691 15.852709660483859, -37.7163859753386 21.4805029709527, -34.94408836907636 26.667106990588067, -31.213203435596423 31.213203435596427, -26.66710699058806 34.94408836907636, -21.480502970952678 37.71638597533861, -15.852709660483827 39.42355841209692, -10 40, 10 40), (10 30, -10 30, -13.90180644032255 29.615705608064612, -17.653668647301785 28.47759065022574, -21.11140466039204 26.629392246050905, -24.14213562373095 24.14213562373095, -26.629392246050905 21.111404660392044, -28.477590650225736 17.6536686473018, -29.61570560806461 13.901806440322572, -30 10.000000000000002, -29.61570560806461 6.098193559677433, -28.477590650225736 2.346331352698207, -26.62939224605091 -1.11140466039204, -24.142135623730955 -4.142135623730949, -21.111404660392044 -6.629392246050905, -17.653668647301807 -8.477590650225729, -13.901806440322574 -9.615705608064605, -10 -10, 10 -10, 13.901806440322552 -9.615705608064612, 17.65366864730179 -8.47759065022574, 21.11140466039204 -6.629392246050905, 24.14213562373095 -4.142135623730949, 26.629392246050905 -1.1114046603920418, 28.477590650225736 2.346331352698204, 29.61570560806461 6.098193559677435, 30 10, 29.61570560806461 13.901806440322565, 28.477590650225736 17.653668647301796, 26.629392246050905 21.111404660392044, 24.14213562373095 24.14213562373095, 21.111404660392047 26.629392246050905, "
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

    @Test
    public void test_ST_RingBufferEndCapBUTT() {
        assertThrows(SQLException.class, () ->
                st.execute("SELECT ST_RingBuffer('LINESTRING (-10 10, 10 10)'::GEOMETRY, 10, 3,'endcap=BUTT');"));
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
        assertTrue(rs.getString(1).equalsIgnoreCase("MULTIPOLYGON Z (EMPTY, EMPTY, EMPTY)"));
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

    @Test
    public void test_ST_MaximumInscribedCircle1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaximumInscribedCircle('SRID=4326;POLYGON ((190 390, 100 210, 267 125, 360 280, 190 390))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("SRID=4326;POLYGON ((132.45582285446451 251.93603515625, 134.2878401128131 270.53681853330306, 139.71348849202758 288.4227842366155, 148.5242633676334 304.90658463477405, 160.3815716023006 319.3547565226994, 174.82974349022592 331.21206475736653, 191.31354388838446 340.0228396329724, 209.19950959169688 345.4484880121869, 227.80029296875 347.2805052705355, 246.40107634580306 345.4484880121869, 264.2870420491155 340.0228396329724, 280.77084244727405 331.2120647573666, 295.2190143351994 319.3547565226994, 307.07632256986653 304.9065846347741, 315.8870974454724 288.42278423661554, 321.3127458246869 270.5368185333031, 323.1447630830355 251.93603515625, 321.3127458246869 233.33525177919694, 315.8870974454724 215.44928607588452, 307.07632256986653 198.96548567772592, 295.2190143351994 184.51731378980062, 280.7708424472741 172.66000555513344, 264.2870420491155 163.84923067952758, 246.4010763458031 158.4235823003131, 227.80029296875 156.59156504196451, 209.19950959169694 158.4235823003131, 191.31354388838452 163.84923067952758, 174.82974349022595 172.6600055551334, 160.38157160230062 184.5173137898006, 148.5242633676334 198.96548567772592, 139.71348849202758 215.4492860758845, 134.2878401128131 233.33525177919688, 132.45582285446451 251.93603515625))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MaximumInscribedCircle2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MaximumInscribedCircle('MULTILINESTRING ((79.5 364, 183.5 280),  (70 220, 166 333), (130.5 242.5, 193.5 243))'::GEOMETRY);");
        rs.next();
        assertGeometryEquals("POLYGON ((25.791150048757956 303.25, 26.746291662048968 312.9477155440392, 29.575010945372796 322.2727533182625, 34.16860180303537 330.86675735054797, 40.35053511449216 338.3994648855078, 47.883242649452015 344.58139819696464, 56.47724668173744 349.1749890546272, 65.80228445596077 352.003708337951, 75.5 352.95884995124203, 85.19771554403921 352.003708337951, 94.52275331826255 349.1749890546272, 103.11675735054797 344.58139819696464, 110.64946488550783 338.3994648855078, 116.83139819696461 330.86675735054797, 121.42498905462719 322.2727533182626, 124.25370833795102 312.9477155440392, 125.20884995124204 303.25, 124.25370833795102 293.5522844559608, 121.42498905462719 284.2272466817375, 116.83139819696461 275.63324264945203, 110.64946488550783 268.1005351144922, 103.11675735054799 261.91860180303536, 94.52275331826253 257.3250109453728, 85.19771554403921 254.49629166204898, 75.5 253.54115004875797, 65.80228445596079 254.49629166204898, 56.47724668173747 257.3250109453728, 47.88324264945203 261.91860180303536, 40.350535114492175 268.1005351144922, 34.16860180303538 275.63324264945203, 29.575010945372803 284.2272466817375, 26.746291662048968 293.5522844559608, 25.791150048757956 303.25))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeArcLine1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeArcLine('POINT (0 0)'::GEOMETRY,  10, 0, PI()/2 );");
        rs.next();
        assertGeometryBarelyEquals("LINESTRING (5 0, 4.999370638369375 0.0793298191740396, 4.997482711915925 0.1586396674903383, 4.99433669591504 0.2379095791187115, 4.989933382359422 0.3171195982828225, 4.984273879759712 0.3962497842839423, 4.977359612865423 0.4752802165209133, 4.96919232230627 0.5541909995050551, 4.959774064153977 0.6329622678687463, 4.949107209404663 0.7115741913664257, 4.937194443381971 0.7900069798667495, 4.92403876506104 0.8682408883346516, 4.909643486313533 0.946256221802051, 4.894012231073893 1.0240333403259534, 4.877148934427035 1.1015526639327031, 4.859057841617708 1.178794677547136, 4.839743506981781 1.255739935905396, 4.81921079279971 1.3323690684501748, 4.7974648680724865 1.4086627842071484, 4.774511207220369 1.4846018766413742, 4.7503555887047275 1.5601672284924355, 4.725004093573342 1.635339816587108, 4.698463103929543 1.7101007166283435, 4.670739301325534 1.784431107959359, 4.641839665080363 1.858312278301638, 4.611771470522907 1.9317256284656434, 4.580542287160348 2.004652677033069, 4.548159976772592 2.0770750650094323, 4.514632691433106 2.148974560445858, 4.47996887145668 2.2203330630288707, 4.444177243274617 2.291132608637052, 4.40726681723791 2.3613553738634137, 4.369246885348924 2.4309836805023437, 4.330127018922193 2.5, 4.289917066174885 2.5683869578670313, 4.248627149747572 2.6361273380525123, 4.206267664155906 2.703204087277988, 4.1628492731738564 2.7696003193305514, 4.118382907149164 2.8352993193138536, 4.072879760251679 2.900284547855991, 4.026351287655293 2.9645396452732022, 3.9788092026541606 3.0280484356883335, 3.9302654737139378 3.0907949311030256, 3.880732321458784 3.1527633354226126, 3.83022221559489 3.2139380484326963, 3.7787478717712912 3.2743036697264256, 3.726322248378774 3.333845002581458, 3.672958543287666 3.392547057785661, 3.6186701905253504 3.45039505741056, 3.5634708568943148 3.507374438531606, 3.507374438531606 3.5634708568943148, 3.45039505741056 3.618670190525351, 3.392547057785661 3.672958543287667, 3.333845002581458 3.7263222483787732, 3.274303669726425 3.7787478717712912, 3.2139380484326967 3.83022221559489, 3.1527633354226126 3.880732321458784, 3.0907949311030265 3.9302654737139378, 3.028048435688333 3.9788092026541606, 2.9645396452732027 4.0263512876552925, 2.900284547855991 4.072879760251679, 2.8352993193138536 4.118382907149163, 2.7696003193305514 4.162849273173857, 2.7032040872779883 4.206267664155906, 2.636127338052512 4.248627149747572, 2.568386957867032 4.289917066174885, 2.4999999999999996 4.330127018922194, 2.4309836805023433 4.369246885348924, 2.3613553738634137 4.40726681723791, 2.2911326086370525 4.444177243274617, 2.2203330630288702 4.47996887145668, 2.148974560445858 4.514632691433106, 2.0770750650094323 4.548159976772592, 2.004652677033069 4.580542287160348, 1.9317256284656428 4.611771470522907, 1.8583122783016375 4.641839665080363, 1.7844311079593593 4.670739301325534, 1.7101007166283442 4.698463103929542, 1.6353398165871078 4.725004093573342, 1.5601672284924355 4.7503555887047275, 1.4846018766413747 4.774511207220369, 1.4086627842071482 4.7974648680724865, 1.3323690684501748 4.81921079279971, 1.255739935905396 4.839743506981781, 1.1787946775471365 4.859057841617708, 1.1015526639327027 4.877148934427035, 1.0240333403259534 4.894012231073893, 0.946256221802051 4.909643486313533, 0.8682408883346521 4.92403876506104, 0.7900069798667491 4.937194443381972, 0.7115741913664255 4.949107209404663, 0.6329622678687464 4.959774064153977, 0.5541909995050555 4.96919232230627, 0.475280216520913 4.977359612865423, 0.3962497842839422 4.984273879759712, 0.3171195982828228 4.989933382359422, 0.2379095791187109 4.99433669591504, " +
                "0.158639667490338 4.997482711915925, 0.0793298191740397 4.999370638369375, 0.0000000000000003 5)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeArcLine2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeArcLine('POINT (0 0)'::GEOMETRY,  10, PI()/3, PI()/3 );");
        rs.next();
        assertGeometryBarelyEquals("LINESTRING (2.5000000000000004 4.330127018922193, 2.4540579801178413 4.356328664164349, " +
                "2.4078413810475325 4.382042889305753, 2.3613553738634137 4.40726681723791, 2.314605159783313 4.431997625710608, 2.2675959695865884 4.456232547647694, 2.220333063028871 4.47996887145668, 2.172821728253562 4.503203941332127, 2.1250672812001516 4.525935157552812, 2.0770750650094323 4.548159976772592, 2.0288504494256694 4.569875912304979, 1.9803988301957844 4.59108053440137, 1.9317256284656439 4.611771470522907, 1.8828362901734947 4.631946405605932, 1.8337362854406323 4.651603082321017, 1.7844311079593593 4.670739301325534, 1.7349262743783123 4.689352921509725, 1.685227323685209 4.707441860236278, 1.635339816587109 4.725004093573342, 1.585269334888235 4.742037656520982, 1.5350214808654394 4.75854064323104, 1.4846018766413747 4.774511207220369, 1.4340161635554516 4.789947561577445, 1.3832700015326362 4.804847979162286, 1.3323690684501757 4.81921079279971, 1.281319059502314 4.833034395465866, 1.2301256865630668 4.846317240468038, 1.1787946775471365 4.859057841617708, 1.1273317757690227 4.871254773396837, 1.0757427393004189 4.882906671117361, 1.0240333403259543 4.894012231073893, 0.9722093644973553 4.904570210689582, 0.9202766102861029 4.914579428655143, 0.8682408883346521 4.92403876506104, 0.8161080208062972 4.932947161522778, 0.7638838407337356 4.941303621299331, 0.7115741913664266 4.949107209404663, 0.659184925516803 4.956357052712345, 0.6067219049054098 4.963052340053241, 0.5541909995050555 4.96919232230627, 0.5015980868840255 4.974776312482229, 0.448949051548459 4.9798036858006505, 0.3962497842839433 4.984273879759711, 0.3435061814964062 4.9881863941991735, 0.2907241445523806 4.991540791356341, 0.237909579118712 4.99433669591504, 0.1850683945017911 4.996573795047615, 0.1322065029863672 4.998251838449931, 0.0793298191740408 4.999370638369375, 0.0264442593214887 4.999930069625868, -0.026444259321488 4.999930069625868, -0.079329819174039 4.999370638369375, -0.1322065029863655 4.9982518384499315, -0.1850683945017893 4.996573795047615, -0.2379095791187103 4.99433669591504, -0.2907241445523778 4.991540791356341, -0.3435061814964034 4.988186394199174, -0.3962497842839416 4.984273879759712, -0.4489490515484574 4.9798036858006505, -0.5015980868840239 4.974776312482229, -0.5541909995050549 4.96919232230627, -0.6067219049054092 4.963052340053241, -0.6591849255168013 4.956357052712345, -0.7115741913664251 4.949107209404664, -0.7638838407337338 4.941303621299331, -0.8161080208062955 4.932947161522778, -0.8682408883346504 4.924038765061041, -0.9202766102861002 4.914579428655144, -0.9722093644973526 4.904570210689582, -1.0240333403259525 4.894012231073893, -1.0757427393004173 4.882906671117362, -1.1273317757690209 4.871254773396837, -1.1787946775471358 4.859057841617708, -1.2301256865630663 4.846317240468039, -1.2813190595023125 4.833034395465866, -1.332369068450174 4.81921079279971, -1.3832700015326347 4.804847979162286, -1.4340161635554498 4.789947561577445, -1.4846018766413729 4.77451120722037, -1.5350214808654368 4.75854064323104, -1.5852693348882334 4.742037656520983, -1.6353398165871071 4.725004093573343, -1.6852273236852076 4.707441860236278, -1.7349262743783118 4.689352921509725, -1.7844311079593589 4.670739301325534, -1.8337362854406305 4.651603082321018, -1.882836290173493 4.631946405605932, -1.9317256284656423 4.611771470522907, -1.980398830195783 4.591080534401371, -2.0288504494256676 4.56987591230498, -2.077075065009431 4.548159976772593, -2.1250672812001485 4.525935157552813, -2.172821728253559 4.503203941332129, -2.2203330630288685 4.4799688714566805, -2.267595969586586 4.456232547647695, -2.3146051597833104 4.4319976257106095, -2.361355373863411 4.407266817237911, -2.407841381047531 4.382042889305754, " +
                "-2.45405798011784 4.35632866416435, -2.499999999999999 4.330127018922194)", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeArcPolygon1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeArcPolygon('POINT (0 0)'::GEOMETRY,  10, PI()/3, PI()/3 );");
        rs.next();
        assertGeometryBarelyEquals("POLYGON ((0 0, 2.5000000000000004 4.330127018922193, 2.4540579801178413 4.356328664164349, 2.4078413810475325 4.382042889305753, 2.3613553738634137 4.40726681723791, 2.314605159783313 4.431997625710608, 2.2675959695865884 4.456232547647694, 2.220333063028871 4.47996887145668, 2.172821728253562 4.503203941332127, 2.1250672812001516 4.525935157552812, 2.0770750650094323 4.548159976772592, 2.0288504494256694 4.569875912304979, 1.9803988301957844 4.59108053440137, 1.9317256284656439 4.611771470522907, 1.8828362901734947 4.631946405605932, 1.8337362854406323 4.651603082321017, 1.7844311079593593 4.670739301325534, 1.7349262743783123 4.689352921509725, 1.685227323685209 4.707441860236278, 1.635339816587109 4.725004093573342, 1.585269334888235 4.742037656520982, 1.5350214808654394 4.75854064323104, 1.4846018766413747 4.774511207220369, 1.4340161635554516 4.789947561577445, 1.3832700015326362 4.804847979162286, 1.3323690684501757 4.81921079279971, 1.281319059502314 4.833034395465866, 1.2301256865630668 4.846317240468038, 1.1787946775471365 4.859057841617708, 1.1273317757690227 4.871254773396837, 1.0757427393004189 4.882906671117361, 1.0240333403259543 4.894012231073893, 0.9722093644973553 4.904570210689582, 0.9202766102861029 4.914579428655143, 0.8682408883346521 4.92403876506104, 0.8161080208062972 4.932947161522778, 0.7638838407337356 4.941303621299331, 0.7115741913664266 4.949107209404663, 0.659184925516803 4.956357052712345, 0.6067219049054098 4.963052340053241, 0.5541909995050555 4.96919232230627, 0.5015980868840255 4.974776312482229, 0.448949051548459 4.9798036858006505, 0.3962497842839433 4.984273879759711, 0.3435061814964062 4.9881863941991735, 0.2907241445523806 4.991540791356341, 0.237909579118712 4.99433669591504, 0.1850683945017911 4.996573795047615, 0.1322065029863672 4.998251838449931, 0.0793298191740408 4.999370638369375, 0.0264442593214887 4.999930069625868, -0.026444259321488 4.999930069625868, -0.079329819174039 4.999370638369375, -0.1322065029863655 4.9982518384499315, -0.1850683945017893 4.996573795047615, -0.2379095791187103 4.99433669591504, -0.2907241445523778 4.991540791356341, -0.3435061814964034 4.988186394199174, -0.3962497842839416 4.984273879759712, -0.4489490515484574 4.9798036858006505, -0.5015980868840239 4.974776312482229, -0.5541909995050549 4.96919232230627, -0.6067219049054092 4.963052340053241, -0.6591849255168013 4.956357052712345, -0.7115741913664251 4.949107209404664, -0.7638838407337338 4.941303621299331, -0.8161080208062955 4.932947161522778, -0.8682408883346504 4.924038765061041, -0.9202766102861002 4.914579428655144, -0.9722093644973526 4.904570210689582, -1.0240333403259525 4.894012231073893, -1.0757427393004173 4.882906671117362, -1.1273317757690209 4.871254773396837, -1.1787946775471358 4.859057841617708, -1.2301256865630663 4.846317240468039, -1.2813190595023125 4.833034395465866, -1.332369068450174 4.81921079279971, -1.3832700015326347 4.804847979162286, -1.4340161635554498 4.789947561577445, -1.4846018766413729 4.77451120722037, -1.5350214808654368 4.75854064323104, -1.5852693348882334 4.742037656520983, -1.6353398165871071 4.725004093573343, -1.6852273236852076 4.707441860236278, -1.7349262743783118 4.689352921509725, -1.7844311079593589 4.670739301325534, -1.8337362854406305 4.651603082321018, -1.882836290173493 4.631946405605932, -1.9317256284656423 4.611771470522907, -1.980398830195783 4.591080534401371, -2.0288504494256676 4.56987591230498, -2.077075065009431 4.548159976772593, -2.1250672812001485 4.525935157552813, -2.172821728253559 4.503203941332129, -2.2203330630288685 4.4799688714566805, -2.267595969586586 4.456232547647695, -2.3146051597833104 4.4319976257106095, -2.361355373863411 4.407266817237911, -2.407841381047531 4.382042889305754, " +
                "-2.45405798011784 4.35632866416435, -2.499999999999999 4.330127018922194, 0 0))", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MakeArcPolygon2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT ST_MakeArcPolygon('POINT (0 0)'::GEOMETRY,  10, 0, PI()/2 );");
        rs.next();
        assertGeometryBarelyEquals("POLYGON ((0 0, 5 0, 4.999370638369375 0.0793298191740396, " +
                "4.997482711915925 0.1586396674903383, 4.99433669591504 0.2379095791187115, 4.989933382359422 0.3171195982828225, " +
                "4.984273879759712 0.3962497842839423, 4.977359612865423 0.4752802165209133, 4.96919232230627 0.5541909995050551, " +
                "4.959774064153977 0.6329622678687463, 4.949107209404663 0.7115741913664257, 4.937194443381971 0.7900069798667495, " +
                "4.92403876506104 0.8682408883346516, 4.909643486313533 0.946256221802051, 4.894012231073893 1.0240333403259534, " +
                "4.877148934427035 1.1015526639327031, 4.859057841617708 1.178794677547136, 4.839743506981781 1.255739935905396, " +
                "4.81921079279971 1.3323690684501748, 4.7974648680724865 1.4086627842071484, 4.774511207220369 1.4846018766413742, " +
                "4.7503555887047275 1.5601672284924355, 4.725004093573342 1.635339816587108, 4.698463103929543 1.7101007166283435, " +
                "4.670739301325534 1.784431107959359, 4.641839665080363 1.858312278301638, 4.611771470522907 1.9317256284656434, 4.580542287160348 2.004652677033069, 4.548159976772592 2.0770750650094323, " +
                "4.514632691433106 2.148974560445858, 4.47996887145668 2.2203330630288707, 4.444177243274617 2.291132608637052, 4.40726681723791 2.3613553738634137, 4.369246885348924 2.4309836805023437, 4.330127018922193 2.5, 4.289917066174885 2.5683869578670313, 4.248627149747572 2.6361273380525123, " +
                "4.206267664155906 2.703204087277988, 4.1628492731738564 2.7696003193305514, 4.118382907149164 2.8352993193138536, 4.072879760251679 2.900284547855991, 4.026351287655293 2.9645396452732022, 3.9788092026541606 3.0280484356883335, 3.9302654737139378 3.0907949311030256, 3.880732321458784 3.1527633354226126, 3.83022221559489 3.2139380484326963, 3.7787478717712912 3.2743036697264256, 3.726322248378774 3.333845002581458, 3.672958543287666 3.392547057785661, 3.6186701905253504 3.45039505741056, 3.5634708568943148 3.507374438531606, " +
                "3.507374438531606 3.5634708568943148, 3.45039505741056 3.618670190525351, 3.392547057785661 3.672958543287667, 3.333845002581458 3.7263222483787732, 3.274303669726425 3.7787478717712912, 3.2139380484326967 3.83022221559489, 3.1527633354226126 3.880732321458784, 3.0907949311030265 3.9302654737139378, 3.028048435688333 3.9788092026541606, 2.9645396452732027 4.0263512876552925, 2.900284547855991 4.072879760251679, 2.8352993193138536 4.118382907149163, 2.7696003193305514 4.162849273173857, 2.7032040872779883 4.206267664155906, 2.636127338052512 4.248627149747572, 2.568386957867032 4.289917066174885, 2.4999999999999996 4.330127018922194, 2.4309836805023433 4.369246885348924, 2.3613553738634137 4.40726681723791, 2.2911326086370525 4.444177243274617, 2.2203330630288702 4.47996887145668, 2.148974560445858 4.514632691433106, 2.0770750650094323 4.548159976772592, 2.004652677033069 4.580542287160348, 1.9317256284656428 4.611771470522907, 1.8583122783016375 4.641839665080363, 1.7844311079593593 4.670739301325534, 1.7101007166283442 4.698463103929542, 1.6353398165871078 4.725004093573342, 1.5601672284924355 4.7503555887047275, 1.4846018766413747 4.774511207220369, 1.4086627842071482 4.7974648680724865, 1.3323690684501748 4.81921079279971, 1.255739935905396 4.839743506981781, 1.1787946775471365 4.859057841617708, " +
                "1.1015526639327027 4.877148934427035, 1.0240333403259534 4.894012231073893, 0.946256221802051 4.909643486313533, 0.8682408883346521 4.92403876506104, 0.7900069798667491 4.937194443381972, 0.7115741913664255 4.949107209404663, 0.6329622678687464 4.959774064153977, 0.5541909995050555 4.96919232230627, " +
                "0.475280216520913 4.977359612865423, 0.3962497842839422 4.984273879759712, 0.3171195982828228 4.989933382359422, 0.2379095791187109 4.99433669591504, 0.158639667490338 4.997482711915925, 0.0793298191740397 4.999370638369375, 0.0000000000000003 5, 0 0))\n", rs.getObject(1));
        rs.close();
    }

    @Test
    public void test_ST_MinimumBoundingRadius1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT center, radius FROM ST_MinimumBoundingRadius('POLYGON((26426 65078,26531 65242,26075 65136,26096 65427,26426 65078))'::GEOMETRY);");
        rs.next();
        assertGeometryBarelyEquals("POINT(26284.8418027133 65267.1145090825)", rs.getObject(1));
        assertEquals(247.436045591407D, rs.getDouble(2), 10-9);
        rs.close();
    }

    @Test
    public void test_ST_MinimumBoundingRadiusTable() throws Exception {
        st.execute("DROP TABLE IF EXISTS tmp_geoms;"
        +"CREATE TABLE tmp_geoms AS SELECT ST_BUFFER(ST_MAKEPOINT(X, X*10), 10) FROM GENERATE_SERIES(1, 3);");
        ResultSet rs = st.executeQuery(" select * from ST_MinimumBoundingRadius('tmp_geoms') order by id");
        while (rs.next()){
            assertTrue(rs.getInt("id")>=1);
            assertNotNull(rs.getObject("center"));
            assertTrue(rs.getDouble("radius")>0);
        }
        rs.close();
        st.execute("DROP TABLE tmp_geoms;");
    }

    @Test
    public void test_ST_MinimumBoundingRadiusTableSelect() throws Exception {
        st.execute("DROP TABLE IF EXISTS tmp_geoms;"
                +"CREATE TABLE tmp_geoms AS SELECT ST_BUFFER(ST_MAKEPOINT(X, X*10), 10) as the_geom, X as id FROM GENERATE_SERIES(1, 3);");
        ResultSet rs = st.executeQuery(" select center, radius, id, the_geom from ST_MinimumBoundingRadius('(SELECT the_geom FROM tmp_geoms where ID > 1)') order by id");
        assertEquals(4, rs.getMetaData().getColumnCount());
        while (rs.next()){
            assertNotNull(rs.getObject("center"));
            assertTrue(rs.getInt("id")<3);
            assertNotNull(rs.getObject("the_geom"));
            assertTrue(rs.getDouble("radius")>0);
        }
        rs.close();
        st.execute("DROP TABLE tmp_geoms;");
    }
}
