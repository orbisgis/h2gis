package org.h2gis.functions.spatial.clean;


import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.unitTest.GeometryAsserts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class dedicated to {@link ST_MakeValid}.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class ST_MakeValidTest {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "ST_MakeValidTest";

    @BeforeAll
    static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME, true);
    }

    @BeforeEach
    void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @Test
    public void makeValidNullTest() throws SQLException {
        assertNull(ST_MakeValid.validGeom(null));
    }

    @Test
    public void makeValidPointTest() throws SQLException {
        Coordinate[] coords = new Coordinate[]{new Coordinate(4.2, 2.3)};
        Point point = new Point(new CoordinateArraySequence(coords), geometryFactory);
        Geometry result = ST_MakeValid.validGeom(point);
        GeometryAsserts.assertGeometryEquals("POINT(4.2 2.3)", result);

        coords = new Coordinate[]{new Coordinate(4.2, NaN)};
        point = new Point(new CoordinateArraySequence(coords), geometryFactory);
        result = ST_MakeValid.validGeom(point);
        assertTrue(result.isEmpty());

        coords = new Coordinate[]{new Coordinate(4.2, 2.3, 5.6)};
        point = new Point(new CoordinateArraySequence(coords), geometryFactory);
        result = ST_MakeValid.validGeom(point);
        GeometryAsserts.assertGeometryEquals("POINT(4.2 2.3 5.6)", result);

        coords = new Coordinate[]{new Coordinate(4.2, 2.3, NaN)};
        point = new Point(new CoordinateArraySequence(coords), geometryFactory);
        result = ST_MakeValid.validGeom(point);
        GeometryAsserts.assertGeometryEquals("POINT(4.2 2.3)", result);
    }

    @Test
    public void makeValidMultiPointTest() throws SQLException {
        Coordinate[] coords1 = new Coordinate[]{new Coordinate(4.2, 2.3)};
        Coordinate[] coords2 = new Coordinate[]{new Coordinate(25, 142)};
        Coordinate[] coords3 = new Coordinate[]{new Coordinate(0.8, 8.9)};
        Point[] points = new Point[]{
                new Point(new CoordinateArraySequence(coords1), geometryFactory),
                new Point(new CoordinateArraySequence(coords2), geometryFactory),
                new Point(new CoordinateArraySequence(coords3), geometryFactory)
        };
        MultiPoint multiPoint = new MultiPoint(points, geometryFactory);
        Geometry result = ST_MakeValid.validGeom(multiPoint);
        GeometryAsserts.assertGeometryEquals("MULTIPOINT((4.2 2.3),(25 142),(0.8 8.9))", result);

        coords2 = new Coordinate[]{new Coordinate(25, NaN)};
        points = new Point[]{
                new Point(new CoordinateArraySequence(coords1), geometryFactory),
                new Point(new CoordinateArraySequence(coords2), geometryFactory),
                new Point(new CoordinateArraySequence(coords3), geometryFactory)
        };
        multiPoint = new MultiPoint(points, geometryFactory);
        result = ST_MakeValid.validGeom(multiPoint);
        GeometryAsserts.assertGeometryEquals("MULTIPOINT((4.2 2.3),(0.8 8.9))", result);

        //TODO this case seems problematic
        /*coords2 = new Coordinate[]{new Coordinate(25, 142, 5.6)};
        points = new Point[]{
                new Point(new CoordinateArraySequence(coords1), geometryFactory),
                new Point(new CoordinateArraySequence(coords2), geometryFactory),
                new Point(new CoordinateArraySequence(coords3), geometryFactory)
        };
        multiPoint = new MultiPoint(points, geometryFactory);
        result = ST_MakeValid.validGeom(multiPoint);
        GeometryAsserts.assertGeometryEquals("MULTIPOINT((4.2 2.3),(25 142),(0.8 8.9))", result);*/

        coords2 = new Coordinate[]{new Coordinate(25, 142, NaN)};
        points = new Point[]{
                new Point(new CoordinateArraySequence(coords1), geometryFactory),
                new Point(new CoordinateArraySequence(coords2), geometryFactory),
                new Point(new CoordinateArraySequence(coords3), geometryFactory)
        };
        multiPoint = new MultiPoint(points, geometryFactory);
        result = ST_MakeValid.validGeom(multiPoint);
        GeometryAsserts.assertGeometryEquals("MULTIPOINT((4.2 2.3),(25 142),(0.8 8.9))", result);

        points = new Point[]{ };
        multiPoint = new MultiPoint(points, geometryFactory);
        result = ST_MakeValid.validGeom(multiPoint);
        assertTrue(result.isEmpty());
    }
}
