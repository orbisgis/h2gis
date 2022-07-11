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
package org.h2gis.functions.spatial.clean;


import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.unitTest.GeometryAsserts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

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
        GeometryAsserts.assertGeometryEquals("POINTZ(4.2 2.3 5.6)", result);

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

    @Test
    public void makeValidPolygonTest() throws SQLException {
        //Flat polygon test
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(5848, 49987),
                new Coordinate(5848, 49986),
                new Coordinate(5848, 49984),
                new Coordinate(5848, 49987)
        };
        CoordinateSequence points = new CoordinateArraySequence(coords);
        LinearRing shell = new LinearRing(points, geometryFactory);
        Polygon polygon = new Polygon(shell, null, geometryFactory);

        Geometry result = ST_MakeValid.validGeom(polygon, true);
        assertTrue(result.isEmpty());

        result = ST_MakeValid.validGeom(polygon, false);
        GeometryAsserts.assertGeometryEquals("MULTILINESTRING ((5848 49986,5848 49987), (5848 49986,5848 49984))", result);
    }
}
