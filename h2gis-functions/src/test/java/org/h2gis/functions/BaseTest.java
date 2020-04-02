/*
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
package org.h2gis.functions;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.orbisgis.commons.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Abstract class for test classes.
 * <p>
 * This abstract class contains method to create a test database and give access to a {@link Statement} to perform SQL
 * queries. To initiate the {@link Statement}, call the {@link BaseTest#statementInit()} or
 * {@link BaseTest#statementInit(String)} method.
 * <p>
 * <p>
 * This abstract class contains geometries of each kind :
 * ┬─ POINT              ─────────────────┬──┬─ (0 0) centered ─┬──┬─ 2D    ─┬─
 * ├─ LINESTRING         ─────────────────┤  └─ not centered   ─┘  ├─ 3D    ─┤
 * ├─ POLYGON            ─┬─ With hole ─┬─┤                        └─ Mixed ─┘
 * │                      └─ No hole   ─┘ │
 * ├─ MULTIPOINT         ─────────────────┤
 * ├─ MULTILINESTRING    ─────────────────┤
 * ├─ MULTIPOLYGON       ─┬─ With hole ─┬─┤
 * │                      └─ No hole   ─┘ │
 * └─ GEOMETRYCOLLECTION ─────────────────┘
 * To initiate the geometries, call the {@link BaseTest#geometriesInit()} method.
 *
 * @author Sylvain PALOMINOS (UBS Lab-STICC 2020)
 */
public abstract class BaseTest {

    /**
     * Default tolerance for the geometry assertion.
     */
    protected static final double TOLERANCE = 10E-10;

    /**
     * POINT (3.0 5.0)
     */
    protected static Point POINT_2D;
    /**
     * POINT Z (3.0 5.0 7.0)
     */
    protected static Point POINT_3D;
    /**
     * LINESTRING (3.0 5.0, 4.0 8.0, 6.0 9.0)
     */
    protected static LineString LINESTRING_2D;
    /**
     * LINESTRING Z (3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0)
     */
    protected static LineString LINESTRING_3D;
    /**
     * LINESTRING Z (3.0 5.0, 4.0 8.0 8.0, 6.0 9.0)
     */
    protected static LineString LINESTRING_MIXED;
    /**
     * POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0))
     */
    protected static Polygon POLYGON_2D;
    /**
     * POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *          (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *          (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0))
     */
    protected static Polygon POLYGON_HOLES_2D;
    /**
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0))
     */
    protected static Polygon POLYGON_3D;
    /**
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *            (4.0 7.0 4.0,5.0 8.0 11.0,6.0 8.0 10.0,4.0 7.0 4.0),
     *            (7.0 9.0 11.0, 6.0 7.0 8.0, 7.0 8.0 9.0, 7.0 9.0 11.0))
     */
    protected static Polygon POLYGON_HOLES_3D;
    /**
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0))
     */
    protected static Polygon POLYGON_MIXED;
    /**
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *            (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *            (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0))
     */
    protected static Polygon POLYGON_HOLES_MIXED;
    /**
     * MULTIPOINT ((3.0 5.0), (4.0 8.0), (6.0 9.0))
     */
    protected static MultiPoint MULTIPOINT_2D;
    /**
     * MULTIPOINT Z ((3.0 5.0 7.0), (4.0 8.0 8.0), (6.0 9.0 10.0))
     */
    protected static MultiPoint MULTIPOINT_3D;
    /**
     * MULTIPOINT Z ((3.0 5.0 7.0), (4.0 8.0), (6.0 9.0 10.0))
     */
    protected static MultiPoint MULTIPOINT_MIXED;
    /**
     * MULTILINESTRING ((3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                  (9.0 2.0, 1.0 2.0, 6.0 6.0),
     *                  (10.0 1.0, 9.0 2.0, 8.0 3.0))
     */
    protected static MultiLineString MULTILINESTRING_2D;
    /**
     * MULTILINESTRING Z ((3.0 5.0 7.0, 4.0 8.0 12.0, 6.0 9.0 12.0),
     *                    (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                    (10.0 1.0 1.0, 9.0 2.0 2.0, 8.0 3.0 3.0))
     */
    protected static MultiLineString MULTILINESTRING_3D;
    /**
     * MULTILINESTRING Z ((3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                    (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                    (10.0 1.0, 9.0 2.0, 8.0 3.0))
     */
    protected static MultiLineString MULTILINESTRING_MIXED;
    /**
     * MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *               ((10.0 11.0, 15.0 16.0, 2.0 12.0, 1.0 11.0)),
     *               ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_2D;
    /**
     * MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *               ((10.0 11.0, 15.0 16.0, 2.0 12.0, 10.0 11.0)),
     *               ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_HOLES_2D;
    /**
     * MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_3D;
    /**
     * MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                  (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *                  (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_HOLES_3D;
    /**
     * MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_MIXED;
    /**
     * MULTIPOLYGON Z (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                  (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                  (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_HOLES_MIXED;
    /**
     * GEOMETRYCOLLECTION (
     *                     POINT (3.0 5.0),
     *                     LINESTRING (3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                     POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                     POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                              (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                              (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *                     MULTIPOINT ((3.0 5.0), (4.0 8.0), (6.0 9.0)),
     *                     MULTILINESTRING ((3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                                      (9.0 2.0, 1.0 2.0, 6.0 6.0),
     *                                      (10.0 1.0, 9.0 2.0, 8.0 3.0)),
     *                     MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                                   ((10.0 11.0, 15.0 16.0, 2.0 12.0, 1.0 11.0)),
     *                                   ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0))),
     *                     MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                                    (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                                    (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *                                   ((10.0 11.0, 15.0 16.0, 2.0 12.0, 10.0 11.0)),
     *                                   ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     * )
     */
    protected static GeometryCollection GEOMETRY_COLLECTION_2D;
    /**
     * GEOMETRYCOLLECTION (
     *                     POINT Z (3.0 5.0 7.0),
     *                     LINESTRING Z (3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0),
     *                     POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                     POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                (4.0 7.0 4.0,5.0 8.0 11.0,6.0 8.0 10.0,4.0 7.0 4.0),
     *                                (7.0 9.0 11.0, 6.0 7.0 8.0, 7.0 8.0 9.0, 7.0 9.0 11.0)),
     *                     MULTIPOINT Z ((3.0 5.0 7.0), (4.0 8.0 8.0), (6.0 9.0 10.0)),
     *                     MULTILINESTRING Z ((3.0 5.0 7.0, 4.0 8.0 12.0, 6.0 9.0 12.0),
     *                                        (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                                        (10.0 1.0 1.0, 9.0 2.0 2.0, 8.0 3.0 3.0)),
     *                     MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                                     ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                                     ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0))),
     *                     MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                      (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *                                      (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0)),
     *                                     ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                                     ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     * )
     */
    protected static GeometryCollection GEOMETRY_COLLECTION_3D;
    /**
     * GEOMETRYCOLLECTION (
     *                     POINT (3.0 5.0),
     *                     LINESTRING Z (3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0),
     *                     POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                     POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                (4.0 7.0 4.0,5.0 8.0 11.0,6.0 8.0 10.0,4.0 7.0 4.0),
     *                                (7.0 9.0 11.0, 6.0 7.0 8.0, 7.0 8.0 9.0, 7.0 9.0 11.0)),
     *                     MULTIPOINT ((3.0 5.0), (4.0 8.0), (6.0 9.0)),
     *                     MULTILINESTRING Z ((3.0 5.0 7.0, 4.0 8.0 12.0, 6.0 9.0 12.0),
     *                                        (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                                        (10.0 1.0 1.0, 9.0 2.0 2.0, 8.0 3.0 3.0)),
     *                     MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                                   ((10.0 11.0, 15.0 16.0, 2.0 12.0, 10.0 11.0)),
     *                                   ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)),
     *                     MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                      (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *                                      (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0)),
     *                                     ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                                     ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     * )
     */
    protected static GeometryCollection GEOMETRY_COLLECTION_MIXED;

    /**
     * POINT (0.0 0.0)
     */
    protected static Point POINT_0_2D;
    /**
     * POINT Z (0.0 0.0 0.0)
     */
    protected static Point POINT_0_3D;
    /**
     * LINESTRING (1.0 1.0, 0.0 0.0, -1.0 -1.0)
     */
    protected static LineString LINESTRING_0_2D;
    /**
     * LINESTRING Z (1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0)
     */
    protected static LineString LINESTRING_0_3D;
    /**
     * LINESTRING Z (1.0 1.0, 0.0 0.0 1.0, -1.0 -1.0)
     */
    protected static LineString LINESTRING_0_MIXED;
    /**
     * POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0))
     */
    protected static Polygon POLYGON_0_2D;
    /**
     * POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *          (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0))
     */
    protected static Polygon POLYGON_HOLES_0_2D;
    /**
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0))
     */
    protected static Polygon POLYGON_0_3D;
    /**
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *            (1.0 1.0 1.0, 1.0 -1.0 0.0, -1.0 -1.0 -1.0, -1.0 1.0 0.0, 1.0 1.0 1.0))
     */
    protected static Polygon POLYGON_HOLES_0_3D;
    /**
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0, -2.0 -2.0 -2.0, -2.0 2.0, 2.0 2.0 2.0))
     */
    protected static Polygon POLYGON_0_MIXED;
    /**
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0, -2.0 -2.0 -2.0, -2.0 2.0, 2.0 2.0 2.0),
     *            (1.0 1.0 1.0, 1.0 -1.0 0.0, -1.0 -1.0 -1.0, -1.0 1.0 0.0, 1.0 1.0 1.0))
     */
    protected static Polygon POLYGON_HOLES_0_MIXED;
    /**
     * MULTIPOINT ((1.0 1.0), (0.0 0.0), (-1.0 -1.0))
     */
    protected static MultiPoint MULTIPOINT_0_2D;
    /**
     * MULTIPOINT Z ((1.0 1.0 1.0), (0.0 0.0 0.0), (-1.0 -1.0 -1.0))
     */
    protected static MultiPoint MULTIPOINT_0_3D;
    /**
     * MULTIPOINT Z ((1.0 1.0 1.0), (0.0 0.0), (-1.0 -1.0 -1.0))
     */
    protected static MultiPoint MULTIPOINT_0_MIXED;
    /**
     * MULTILINESTRING ((1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                  (1.0 0.0, 0.0 0.0, -1.0 0.0),
     *                  (-1.0 1.0, 0.0 0.0, 1.0 1.0))
     */
    protected static MultiLineString MULTILINESTRING_0_2D;
    /**
     * MULTILINESTRING Z ((1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                    (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                    (-1.0 1.0 0.0 , 0.0 0.0 1.0, 1.0 -1.0 0.0))
     */
    protected static MultiLineString MULTILINESTRING_0_3D;
    /**
     * MULTILINESTRING Z ((1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                    (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                    (-1.0 1.0, 0.0 0.0, 1.0 1.0))
     */
    protected static MultiLineString MULTILINESTRING_0_MIXED;
    /**
     * MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *               ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_0_2D;
    /**
     * MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *                (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *               ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_HOLES_0_2D;
    /**
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                 ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_0_3D;
    /**
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                  (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                 ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_HOLES_0_3D;
    /**
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                 ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_0_MIXED;
    /**
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                  (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                 ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     */
    protected static MultiPolygon MULTIPOLYGON_HOLES_0_MIXED;
    /**
     * GEOMETRYCOLLECTION (
     *                     POINT (0.0 0.0),
     *                     LINESTRING (1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                     POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                     POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *                              (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                     MULTIPOINT ((1.0 1.0), (0.0 0.0), (-1.0 -1.0)),
     *                     MULTILINESTRING ((1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                                      (1.0 0.0, 0.0 0.0, -1.0 0.0),
     *                                      (-1.0 1.0, 0.0 0.0, 1.0 1.0)),
     *                     MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                                   ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0))),
     *                     MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *                                    (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                                   ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     * )
     */
    protected static GeometryCollection GEOMETRY_COLLECTION_0_2D;
    /**
     * GEOMETRYCOLLECTION (
     *                     POINT Z (0.0 0.0 0.0),
     *                     LINESTRING Z (1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                     POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                     POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                     MULTIPOINT Z ((1.0 1.0 1.0), (0.0 0.0 0.0), (-1.0 -1.0 -1.0)),
     *                     MULTILINESTRING Z ((1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                                        (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                                        (-1.0 1.0 0.0 , 0.0 0.0 1.0, 1.0 -1.0 0.0)),
     *                     MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                                     ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0))),
     *                     MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                      (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                                     ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     * )
     */
    protected static GeometryCollection GEOMETRY_COLLECTION_0_3D;
    /**
     * GEOMETRYCOLLECTION (
     *                     POINT (0.0 0.0),
     *                     LINESTRING Z (1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                     POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                     POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                     MULTIPOINT ((1.0 1.0), (0.0 0.0), (-1.0 -1.0)),
     *                     MULTILINESTRING Z ((1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                                        (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                                        (-1.0 1.0 0.0 , 0.0 0.0 1.0, 1.0 -1.0 0.0)),
     *                     MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                                   ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0))),
     *                     MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                      (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                                     ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     * )
     */
    protected static GeometryCollection GEOMETRY_COLLECTION_0_MIXED;

    /**
     * {@link Statement} used for SQL queries.
     */
    protected Statement st;
    /**
     * Preset {@link GeometryFactory} used for JTS geometry creation.
     */
    protected static GeometryFactory FACTORY;

    /**
     * Initialisation of default test geometries.
     * Each geometries has e 2D, 3D and Mixed coordinates version.
     * Each are duplicated : one centered around (0 0), the other not.
     */
    protected static void geometriesInit() {
        FACTORY = new GeometryFactory();

        POINT_2D = createPoint2D();
        POINT_3D = createPoint3D();

        LINESTRING_2D = createLineString2D();
        LINESTRING_3D = createLineString3D();
        LINESTRING_MIXED = createLineStringMixed();

        POLYGON_2D = createPolygon2D();
        POLYGON_HOLES_2D = createPolygonHole2D();
        POLYGON_3D = createPolygon3D();
        POLYGON_HOLES_3D = createPolygonHole3D();
        POLYGON_MIXED = createPolygonMixed();
        POLYGON_HOLES_MIXED = createPolygonHoleMixed();

        MULTIPOINT_2D = createMultiPoint2D();
        MULTIPOINT_3D = createMultiPoint3D();
        MULTIPOINT_MIXED = createMultiPointMixed();

        MULTILINESTRING_2D = createMultiLineString2D();
        MULTILINESTRING_3D = createMultiLineString3D();
        MULTILINESTRING_MIXED = createMultiLineStringMixed();

        MULTIPOLYGON_2D = createMultiPolygon2D();
        MULTIPOLYGON_HOLES_2D = createMultiPolygonHole2D();
        MULTIPOLYGON_3D = createMultiPolygon3D();
        MULTIPOLYGON_HOLES_3D = createMultiPolygonHole3D();
        MULTIPOLYGON_MIXED = createMultiPolygonMixed();
        MULTIPOLYGON_HOLES_MIXED = createMultiPolygonHoleMixed();

        GEOMETRY_COLLECTION_2D = createGeometryCollection2D();
        GEOMETRY_COLLECTION_3D = createGeometryCollection3D();
        GEOMETRY_COLLECTION_MIXED = createGeometryCollectionMixed();


        POINT_0_2D = createPoint02D();
        POINT_0_3D = createPoint03D();

        LINESTRING_0_2D = createLineString02D();
        LINESTRING_0_3D = createLineString03D();
        LINESTRING_0_MIXED = createLineString0Mixed();

        POLYGON_0_2D = createPolygon02D();
        POLYGON_HOLES_0_2D = createPolygonHole02D();
        POLYGON_0_3D = createPolygon03D();
        POLYGON_HOLES_0_3D = createPolygonHole03D();
        POLYGON_0_MIXED = createPolygon0Mixed();
        POLYGON_HOLES_0_MIXED = createPolygonHole0Mixed();

        MULTIPOINT_0_2D = createMultiPoint02D();
        MULTIPOINT_0_3D = createMultiPoint03D();
        MULTIPOINT_0_MIXED = createMultiPoint0Mixed();

        MULTILINESTRING_0_2D = createMultiLineString02D();
        MULTILINESTRING_0_3D = createMultiLineString03D();
        MULTILINESTRING_0_MIXED = createMultiLineString0Mixed();

        MULTIPOLYGON_0_2D = createMultiPolygon02D();
        MULTIPOLYGON_HOLES_0_2D = createMultiPolygonHole02D();
        MULTIPOLYGON_0_3D = createMultiPolygon03D();
        MULTIPOLYGON_HOLES_0_3D = createMultiPolygonHole03D();
        MULTIPOLYGON_0_MIXED = createMultiPolygon0Mixed();
        MULTIPOLYGON_HOLES_0_MIXED = createMultiPolygonHole0Mixed();

        GEOMETRY_COLLECTION_0_2D = createGeometryCollection02D();
        GEOMETRY_COLLECTION_0_3D = createGeometryCollection03D();
        GEOMETRY_COLLECTION_0_MIXED = createGeometryCollection0Mixed();
    }

    /**
     * Creates a database in the maven /target folder with the given nullable prefix and a random UUID.
     *
     * @param prefix {@link String} prefix of the database name.
     * @throws SQLException           SQL {@link Exception} thrown during the creation of the database or the loading of the
     *                                spatial extension.
     * @throws ClassNotFoundException {@link Exception} thrown during the creation of the database, when calling the H2
     *                                driver class..
     */
    protected void statementInit(@Nullable String prefix) throws SQLException, ClassNotFoundException {
        Connection connection = H2GISDBFactory.createSpatialDataBase(
                "target/" + (prefix!=null?prefix:"") + UUID.randomUUID().toString());
        st = connection.createStatement();
    }

    /**
     * Creates a database in the maven /target folder with a random UUID.
     *
     * @throws SQLException           SQL {@link Exception} thrown during the creation of the database or the loading of the
     *                                spatial extension.
     * @throws ClassNotFoundException {@link Exception} thrown during the creation of the database, when calling the H2
     *                                driver class..
     */
    protected void statementInit() throws SQLException, ClassNotFoundException {
        statementInit(null);
    }

    /**
     * Utility method converting an array containing a multiple of 2 doubles into a 2D {@link CoordinateSequence}.
     *
     * @param doubles Array containing a multiple of 2 double values.
     * @return A 2D {@link CoordinateSequence} build from the given doubles.
     * @throws IllegalArgumentException {@link Exception} thrown when the number of double in the given array is not a
     * multiple of 2.
     */
    private static CoordinateSequence doublesTo2DCS(double... doubles) {
        //Check that the number of double is a multiple of 2
        if (doubles.length % 2 != 0) {
            throw new IllegalArgumentException("The doubles array should contains pairs of values");
        }
        //Build the Coordinate array with the pairs of double.
        Coordinate[] coordinates = new Coordinate[doubles.length / 2];
        for (int i = 0; i < doubles.length / 2; i++) {
            coordinates[i] = new Coordinate(doubles[i * 2], doubles[i * 2 + 1]);
        }
        //Return the CoordinateSequence containing the Coordinate array
        return new CoordinateArraySequence(coordinates);
    }
    /**
     * Utility method converting an array containing a multiple of 3 doubles into a 3D {@link CoordinateSequence}.
     *
     * @param doubles Array containing a multiple of 3 double values.
     * @return A 3D {@link CoordinateSequence} build from the given doubles.
     * @throws IllegalArgumentException {@link Exception} thrown when the number of double in the given array is not a
     * multiple of 3.
     */
    private static CoordinateSequence doublesTo3DCS(double... doubles) {
        //Check that the number of double is a multiple of 3
        if (doubles.length % 3 != 0) {
            throw new IllegalArgumentException("The doubles array should contains triplet of values");
        }
        //Build the Coordinate array with the pairs of double.
        Coordinate[] coordinates = new Coordinate[doubles.length / 3];
        for (int i = 0; i < doubles.length / 3; i++) {
            coordinates[i] = new Coordinate(doubles[i * 3], doubles[i * 3 + 1]);
        }
        //Return the CoordinateSequence containing the Coordinate array
        return new CoordinateArraySequence(coordinates);
    }

    /**
     * Creates a 2D {@link Point} corresponding to the WKT :
     * POINT (3.0 5.0)
     *
     * @return A 2D {@link Point}.
     */
    private static Point createPoint2D() {
        return FACTORY.createPoint(doublesTo2DCS(3.0, 5.0));
    }
    /**
     * Creates a 3D {@link Point} corresponding to the WKT :
     * POINT (3.0 5.0 7.0)
     *
     * @return A 3D {@link Point}.
     */
    private static Point createPoint3D() {
        return FACTORY.createPoint(doublesTo3DCS(3.0, 5.0, 7.0));
    }
    /**
     * Creates a 2D {@link LineString} corresponding to the WKT :
     * LINESTRING (3.0 5.0, 4.0 8.0, 6.0 9.0)
     *
     * @return A 2D {@link LineString}.
     */
    private static LineString createLineString2D() {
        return FACTORY.createLineString(doublesTo2DCS(
                3.0, 5.0,
                4.0, 8.0,
                6.0, 9.0));
    }
    /**
     * Creates a 3D {@link LineString} corresponding to the WKT :
     * LINESTRING Z (3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0)
     *
     * @return A 3D {@link LineString}.
     */
    private static LineString createLineString3D() {
        return FACTORY.createLineString(doublesTo3DCS(
                3.0, 5.0, 7.0,
                4.0, 8.0, 8.0,
                6.0, 9.0, 10.0));
    }
    /**
     * Creates a 3D {@link LineString} corresponding to the WKT :
     * LINESTRING Z (3.0 5.0, 4.0 8.0 8.0, 6.0 9.0)
     *
     * @return A 3D {@link LineString}.
     */
    private static LineString createLineStringMixed() {
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(3.0, 5.0),
                new Coordinate(4.0, 8.0, 8.0),
                new Coordinate(6.0, 9.0)};
        CoordinateSequence cs = new CoordinateArraySequence(coordinates);
        return FACTORY.createLineString(cs);
    }
    /**
     * Creates a 2D {@link Polygon} corresponding to the WKT :
     * POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0))
     *
     * @return A 2D {@link Polygon}.
     */
    private static Polygon createPolygon2D() {
        return FACTORY.createPolygon(FACTORY.createLinearRing(doublesTo2DCS(
                3.0, 5.0,
                4.0, 8.0,
                6.0, 9.0,
                10.0, 11.0,
                8.0, 7.0,
                3.0, 5.0)));
    }
    /**
     * Creates a 2D {@link Polygon} with hole corresponding to the WKT :
     * POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *          (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *          (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0))
     *
     * @return A 2D {@link Polygon}.
     */
    private static Polygon createPolygonHole2D() {
        LinearRing shell = FACTORY.createLinearRing(doublesTo2DCS(
                3.0, 5.0,
                4.0, 8.0,
                6.0, 9.0,
                10.0, 11.0,
                8.0, 7.0,
                3.0, 5.0));
        LinearRing hole1 = FACTORY.createLinearRing(doublesTo2DCS(
                4.0, 7.0,
                5.0, 8.0,
                6.0, 8.0,
                4.0, 7.0));
        LinearRing hole2 = FACTORY.createLinearRing(doublesTo2DCS(
                7.0, 9.0,
                6.0, 7.0,
                7.0, 8.0,
                7.0, 9.0));
        return FACTORY.createPolygon(shell, new LinearRing[]{hole1, hole2});
    }
    /**
     * Creates a 3D {@link Polygon} corresponding to the WKT :
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0))
     *
     * @return A 3D {@link Polygon}.
     */
    private static Polygon createPolygon3D() {
        return FACTORY.createPolygon(FACTORY.createLinearRing(doublesTo3DCS(
                3.0, 5.0, 7.0,
                4.0, 8.0, 8.0,
                6.0, 9.0, 10.0,
                10.0, 11.0, 12.0,
                8.0, 7.0, 6.0,
                3.0, 5.0, 7.0)));
    }
    /**
     * Creates a 3D {@link Polygon} with hole corresponding to the WKT :
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *            (4.0 7.0 4.0,5.0 8.0 11.0,6.0 8.0 10.0,4.0 7.0 4.0),
     *            (7.0 9.0 11.0, 6.0 7.0 8.0, 7.0 8.0 9.0, 7.0 9.0 11.0))
     *
     * @return A 3D {@link Polygon}.
     */
    private static Polygon createPolygonHole3D() {
        LinearRing shell = FACTORY.createLinearRing(doublesTo3DCS(
                3.0, 5.0, 7.0,
                4.0, 8.0, 8.0,
                6.0, 9.0, 10.0,
                10.0, 11.0, 12.0,
                8.0, 7.0, 6.0,
                3.0, 5.0, 7.0));
        LinearRing hole1 = FACTORY.createLinearRing(doublesTo3DCS(
                4.0, 7.0, 4.0,
                5.0, 8.0, 11.0,
                6.0, 8.0, 10.0,
                4.0, 7.0, 4.0));
        LinearRing hole2 = FACTORY.createLinearRing(doublesTo3DCS(
                7.0, 9.0, 11.0,
                6.0, 7.0, 8.0,
                7.0, 8.0, 9.0,
                7.0, 9.0, 11.0));
        return FACTORY.createPolygon(shell, new LinearRing[]{hole1, hole2});
    }
    /**
     * Creates a Mixed {@link Polygon} corresponding to the WKT :
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0))
     *
     * @return A 3D {@link Polygon}.
     */
    private static Polygon createPolygonMixed() {
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(3.0, 5.0, 7.0),
                new Coordinate(4.0, 8.0),
                new Coordinate(6.0, 9.0, 10.0),
                new Coordinate(10.0, 11.0),
                new Coordinate(8.0, 7.0, 6.0),
                new Coordinate(3.0, 5.0, 7.0)};
        return FACTORY.createPolygon(FACTORY.createLinearRing(coordinates));
    }
    /**
     * Creates a Mixed {@link Polygon} with hole corresponding to the WKT :
     * POLYGON Z ((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *            (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *            (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0))
     *
     * @return A Mixed {@link Polygon}.
     */
    private static Polygon createPolygonHoleMixed() {
        LinearRing shell = FACTORY.createLinearRing(new Coordinate[]{
                new Coordinate(3.0, 5.0, 7.0),
                new Coordinate(4.0, 8.0),
                new Coordinate(6.0, 9.0, 10.0),
                new Coordinate(10.0, 11.0),
                new Coordinate(8.0, 7.0, 6.0),
                new Coordinate(3.0, 5.0, 7.0)});
        LinearRing hole1 = FACTORY.createLinearRing(new Coordinate[]{
                new Coordinate(4.0, 7.0, 4.0),
                new Coordinate(5.0, 8.0),
                new Coordinate(6.0, 8.0, 10.0),
                new Coordinate(4.0, 7.0, 4.0)});
        LinearRing hole2 = FACTORY.createLinearRing(new Coordinate[]{
                new Coordinate(7.0, 9.0, 11.0),
                new Coordinate(6.0, 7.0),
                new Coordinate(7.0, 8.0),
                new Coordinate(7.0, 9.0, 11.0)});
        return FACTORY.createPolygon(shell, new LinearRing[]{hole1, hole2});
    }
    /**
     * Creates a 2D {@link MultiPoint} corresponding to the WKT :
     * MULTIPOINT ((3.0 5.0), (4.0 8.0), (6.0 9.0))
     *
     * @return A 2D {@link MultiPoint}.
     */
    private static MultiPoint createMultiPoint2D() {
        Point point1 = FACTORY.createPoint(doublesTo2DCS(3.0, 5.0));
        Point point2 = FACTORY.createPoint(doublesTo2DCS(4.0, 8.0));
        Point point3 = FACTORY.createPoint(doublesTo2DCS(6.0, 9.0));
        return FACTORY.createMultiPoint(new Point[]{point1, point2, point3});
    }
    /**
     * Creates a 3D {@link MultiPoint} corresponding to the WKT :
     * MULTIPOINT ((3.0 5.0 7.0), (4.0 8.0 8.0), (6.0 9.0 10.0))
     *
     * @return A 3D {@link MultiPoint}.
     */
    private static MultiPoint createMultiPoint3D() {
        Point point1 = FACTORY.createPoint(doublesTo3DCS(3.0, 5.0, 7.0));
        Point point2 = FACTORY.createPoint(doublesTo3DCS(4.0, 8.0, 8.0));
        Point point3 = FACTORY.createPoint(doublesTo3DCS(6.0, 9.0, 10.0));
        return FACTORY.createMultiPoint(new Point[]{point1, point2, point3});
    }
    /**
     * Creates a Mixed {@link MultiPoint} corresponding to the WKT :
     * MULTIPOINT Z ((3.0 5.0 7.0), (4.0 8.0), (6.0 9.0 10.0))
     *
     * @return A Mixed {@link MultiPoint}.
     */
    private static MultiPoint createMultiPointMixed() {
        Point point1 = FACTORY.createPoint(doublesTo3DCS(3.0, 5.0, 7.0));
        Point point2 = FACTORY.createPoint(doublesTo2DCS(4.0, 8.0));
        Point point3 = FACTORY.createPoint(doublesTo3DCS(6.0, 9.0, 10.0));
        return FACTORY.createMultiPoint(new Point[]{point1, point2, point3});
    }
    /**
     * Creates a 2D {@link MultiLineString} corresponding to the WKT :
     * MULTILINESTRING ((3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                  (9.0 2.0, 1.0 2.0, 6.0 6.0),
     *                  (10.0 1.0, 9.0 2.0, 8.0 3.0))
     *
     * @return A 2D {@link MultiLineString}.
     */
    private static MultiLineString createMultiLineString2D() {
        LineString lineString1 = FACTORY.createLineString(doublesTo2DCS(
                3.0, 5.0,
                4.0, 8.0,
                6.0, 9.0));
        LineString lineString2 = FACTORY.createLineString(doublesTo2DCS(
                9.0, 2.0,
                1.0, 2.0,
                6.0, 6.0));
        LineString lineString3 = FACTORY.createLineString(doublesTo2DCS(
                10.0, 1.0,
                9.0, 2.0,
                8.0, 3.0));
        return FACTORY.createMultiLineString(new LineString[]{lineString1, lineString2, lineString3});
    }
    /**
     * Creates a 3D {@link MultiLineString} corresponding to the WKT :
     * MULTILINESTRING Z ((3.0 5.0 7.0, 4.0 8.0 12.0, 6.0 9.0 12.0),
     *                    (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                    (10.0 1.0 1.0, 9.0 2.0 2.0, 8.0 3.0 3.0))
     *
     * @return A 3D {@link MultiLineString}.
     */
    private static MultiLineString createMultiLineString3D() {
        LineString lineString1 = FACTORY.createLineString(doublesTo3DCS(
                3.0, 5.0, 7.0,
                4.0, 8.0, 12.0,
                6.0, 9.0, 12.0));
        LineString lineString2 = FACTORY.createLineString(doublesTo3DCS(
                9.0, 2.0, 9.0,
                1.0, 2.0, 3.0,
                6.0, 6.0, 6.0));
        LineString lineString3 = FACTORY.createLineString(doublesTo3DCS(
                10.0, 1.0, 1.0,
                9.0, 2.0, 2.0,
                8.0, 3.0, 3.0));
        return FACTORY.createMultiLineString(new LineString[]{lineString1, lineString2, lineString3});
    }
    /**
     * Creates a Mixed {@link MultiLineString} corresponding to the WKT :
     * MULTILINESTRING Z ((3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                    (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                    (10.0 1.0, 9.0 2.0, 8.0 3.0))
     *
     * @return A Mixed {@link MultiLineString}.
     */
    private static MultiLineString createMultiLineStringMixed() {
        LineString lineString1 = FACTORY.createLineString(doublesTo2DCS(
                3.0, 5.0,
                4.0, 8.0,
                6.0, 9.0));
        LineString lineString2 = FACTORY.createLineString(doublesTo3DCS(
                9.0, 2.0, 9.0,
                1.0, 2.0, 3.0,
                6.0, 6.0, 6.0));
        LineString lineString3 = FACTORY.createLineString(doublesTo2DCS(
                10.0, 1.0,
                9.0, 2.0,
                8.0, 3.0));
        return FACTORY.createMultiLineString(new LineString[]{lineString1, lineString2, lineString3});
    }
    /**
     * Creates a 2D {@link MultiPolygon} corresponding to the WKT :
     * MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *               ((10.0 11.0, 15.0 16.0, 2.0 12.0, 1.0 11.0)),
     *               ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     *
     * @return A 2D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygon2D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                10.0, 11.0,
                15.0, 16.0,
                2.0, 12.0,
                1.0, 11.0));
        Polygon poly2 = FACTORY.createPolygon(doublesTo2DCS(
                0.0, 0.0,
                1.0, 0.0,
                1.0, 1.0,
                0.0, 1.0,
                0.0, 0.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygon2D(), poly1, poly2});
    }
    /**
     * Creates a 2D {@link MultiPolygon} with hole corresponding to the WKT :
     * MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *               ((10.0 11.0, 15.0 16.0, 2.0 12.0, 10.0 11.0)),
     *               ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     *
     * @return A 2D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygonHole2D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                10.0, 11.0,
                15.0, 16.0,
                2.0, 12.0,
                10.0, 11.0));
        Polygon poly2 = FACTORY.createPolygon(doublesTo2DCS(
                0.0, 0.0,
                1.0, 0.0,
                1.0, 1.0,
                0.0, 1.0,
                0.0, 0.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygonHole2D(), poly1, poly2});
    }
    /**
     * Creates a 3D {@link MultiPolygon} corresponding to the WKT :
     * MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     *
     * @return A 3D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygon3D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo3DCS(
                10.0, 11.0, 12.0,
                15.0, 16.0, 17.0,
                2.0, 12.0, 2.0,
                10.0, 11.0, 12.0));
        Polygon poly2 = FACTORY.createPolygon(doublesTo3DCS(
                0.0, 0.0, 11.0,
                1.0, 0.0, 12.0,
                1.0, 1.0, 13.0,
                0.0, 1.0, 14.0,
                0.0, 0.0, 11.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygon3D(), poly1, poly2});
    }
    /**
     * Creates a 3D {@link MultiPolygon} with hole corresponding to the WKT :
     * MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                  (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *                  (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     *
     * @return A 3D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygonHole3D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo3DCS(
                10.0, 11.0, 12.0,
                15.0, 16.0, 17.0,
                2.0, 12.0, 2.0,
                10.0, 11.0, 12.0));
        Polygon poly2 = FACTORY.createPolygon(doublesTo3DCS(
                0.0, 0.0, 11.0,
                1.0, 0.0, 12.0,
                1.0, 1.0, 13.0,
                0.0, 1.0, 14.0,
                0.0, 0.0, 11.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygonHole3D(), poly1, poly2});
    }
    /**
     * Creates a Mixed {@link MultiPolygon} corresponding to the WKT :
     * MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     *
     * @return A Mixed {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygonMixed() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo3DCS(
                10.0, 11.0, 12.0,
                15.0, 16.0, 17.0,
                2.0, 12.0, 2.0,
                10.0, 11.0, 12.0));
        Polygon poly2 = FACTORY.createPolygon(doublesTo2DCS(
                0.0, 0.0,
                1.0, 0.0,
                1.0, 1.0,
                0.0, 1.0,
                0.0, 0.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygon3D(), poly1, poly2});
    }
    /**
     * Creates a 3D {@link MultiPolygon} with hole corresponding to the WKT :
     * MULTIPOLYGON Z (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                  (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                  (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *                 ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                 ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     *
     * @return A 3D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygonHoleMixed() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                10.0, 11.0,
                15.0, 16.0,
                2.0, 12.0,
                10.0, 11.0));
        Polygon poly2 = FACTORY.createPolygon(doublesTo3DCS(
                0.0, 0.0, 11.0,
                1.0, 0.0, 12.0,
                1.0, 1.0, 13.0,
                0.0, 1.0, 14.0,
                0.0, 0.0, 11.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygonHole2D(), poly1, poly2});
    }
    /**
     * Creates a 2D {@link GeometryCollection} corresponding to the WKT :
     * GEOMETRYCOLLECTION (
     *                     POINT (3.0 5.0),
     *                     LINESTRING (3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                     POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                     POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                              (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                              (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *                     MULTIPOINT ((3.0 5.0), (4.0 8.0), (6.0 9.0)),
     *                     MULTILINESTRING ((3.0 5.0, 4.0 8.0, 6.0 9.0),
     *                                      (9.0 2.0, 1.0 2.0, 6.0 6.0),
     *                                      (10.0 1.0, 9.0 2.0, 8.0 3.0)),
     *                     MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                                   ((10.0 11.0, 15.0 16.0, 2.0 12.0, 1.0 11.0)),
     *                                   ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0))),
     *                     MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0),
     *                                    (4.0 7.0, 5.0 8.0, 6.0 8.0, 4.0 7.0),
     *                                    (7.0 9.0, 6.0 7.0, 7.0 8.0, 7.0 9.0)),
     *                                   ((10.0 11.0, 15.0 16.0, 2.0 12.0, 10.0 11.0)),
     *                                   ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)))
     * )
     *
     * @return A 2D {@link GeometryCollection}.
     */
    private static GeometryCollection createGeometryCollection2D() {
        return FACTORY.createGeometryCollection(new Geometry[]{createPoint2D(), createLineString2D(),
                createPolygon2D(), createPolygonHole2D(), createMultiPoint2D(), createMultiLineString2D(),
                createMultiPolygon2D(), createMultiPolygonHole2D()});
    }
    /**
     * Creates a 3D {@link GeometryCollection} corresponding to the WKT :
     * GEOMETRYCOLLECTION (
     *                     POINT Z (3.0 5.0 7.0),
     *                     LINESTRING Z (3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0),
     *                     POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                     POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                (4.0 7.0 4.0,5.0 8.0 11.0,6.0 8.0 10.0,4.0 7.0 4.0),
     *                                (7.0 9.0 11.0, 6.0 7.0 8.0, 7.0 8.0 9.0, 7.0 9.0 11.0)),
     *                     MULTIPOINT Z ((3.0 5.0 7.0), (4.0 8.0 8.0), (6.0 9.0 10.0)),
     *                     MULTILINESTRING Z ((3.0 5.0 7.0, 4.0 8.0 12.0, 6.0 9.0 12.0),
     *                                        (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                                        (10.0 1.0 1.0, 9.0 2.0 2.0, 8.0 3.0 3.0)),
     *                     MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0)),
     *                                     ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                                     ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0))),
     *                     MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                      (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *                                      (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0)),
     *                                     ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                                     ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     * )
     *
     * @return A 3D {@link GeometryCollection}.
     */
    private static GeometryCollection createGeometryCollection3D() {
        return FACTORY.createGeometryCollection(new Geometry[]{createPoint3D(), createLineString3D(),
                createPolygon3D(), createPolygonHole3D(), createMultiPoint3D(), createMultiLineString3D(),
                createMultiPolygon3D(), createMultiPolygonHole3D()});
    }
    /**
     * Creates a Mixed {@link GeometryCollection} corresponding to the WKT :
     * GEOMETRYCOLLECTION (
     *                     POINT (3.0 5.0),
     *                     LINESTRING Z (3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0),
     *                     POLYGON ((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                     POLYGON Z ((3.0 5.0 7.0, 4.0 8.0 8.0, 6.0 9.0 10.0, 10.0 11.0 12.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                (4.0 7.0 4.0,5.0 8.0 11.0,6.0 8.0 10.0,4.0 7.0 4.0),
     *                                (7.0 9.0 11.0, 6.0 7.0 8.0, 7.0 8.0 9.0, 7.0 9.0 11.0)),
     *                     MULTIPOINT ((3.0 5.0), (4.0 8.0), (6.0 9.0)),
     *                     MULTILINESTRING Z ((3.0 5.0 7.0, 4.0 8.0 12.0, 6.0 9.0 12.0),
     *                                        (9.0 2.0 9.0, 1.0 2.0 3.0, 6.0 6.0 6.0),
     *                                        (10.0 1.0 1.0, 9.0 2.0 2.0, 8.0 3.0 3.0)),
     *                     MULTIPOLYGON (((3.0 5.0, 4.0 8.0, 6.0 9.0, 10.0 11.0, 8.0 7.0, 3.0 5.0)),
     *                                   ((10.0 11.0, 15.0 16.0, 2.0 12.0, 10.0 11.0)),
     *                                   ((0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)),
     *                     MULTIPOLYGON Z (((3.0 5.0 7.0, 4.0 8.0, 6.0 9.0 10.0, 10.0 11.0, 8.0 7.0 6.0, 3.0 5.0 7.0),
     *                                      (4.0 7.0 4.0, 5.0 8.0, 6.0 8.0 10.0, 4.0 7.0 4.0),
     *                                      (7.0 9.0 11.0, 6.0 7.0, 7.0 8.0, 7.0 9.0 11.0)),
     *                                     ((10.0 11.0 12.0, 15.0 16.0 17.0, 2.0 12.0 2.0, 10.0 11.0 12.0)),
     *                                     ((0.0 0.0 11.0, 1.0 0.0 12.0, 1.0 1.0 13.0, 0.0 1.0 14.0, 0.0 0.0 11.0)))
     * )
     *
     * @return A Mixed {@link GeometryCollection}.
     */
    private static GeometryCollection createGeometryCollectionMixed() {
        return FACTORY.createGeometryCollection(new Geometry[]{createPoint2D(), createLineString3D(),
                createPolygon2D(), createPolygonHole3D(), createMultiPoint2D(), createMultiLineString3D(),
                createMultiPolygon2D(), createMultiPolygonHole3D()});
    }

    /**
     * Creates a (0 0) centered 2D {@link Point} corresponding to the WKT :
     * POINT (0.0 0.0)
     *
     * @return A (0 0) centered 2D {@link Point}.
     */
    private static Point createPoint02D() {
        return FACTORY.createPoint(doublesTo2DCS(0.0, 0.0));
    }
    /**
     * Creates a (0 0) centered 3D {@link Point} corresponding to the WKT :
     * POINT (0.0 0.0 0.0)
     *
     * @return A (0 0) centered 3D {@link Point}.
     */
    private static Point createPoint03D() {
        return FACTORY.createPoint(doublesTo3DCS(0.0, 0.0, 0.0));
    }
    /**
     * Creates a (0 0) centered 2D {@link LineString} corresponding to the WKT :
     * LINESTRING (1.0 1.0, 0.0 0.0, -1.0 -1.0)
     *
     * @return A (0 0) centered  2D{@link LineString}.
     */
    private static LineString createLineString02D() {
        return FACTORY.createLineString(doublesTo2DCS(
                1.0, 1.0,
                0.0, 0.0,
                -1.0, -1.0));
    }
    /**
     * Creates a (0 0) centered 3D {@link LineString} corresponding to the WKT :
     * LINESTRING (1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0)
     *
     * @return A (0 0) centered  3D{@link LineString}.
     */
    private static LineString createLineString03D() {
        return FACTORY.createLineString(doublesTo3DCS(
                1.0, 1.0, 1.0,
                0.0, 0.0, 0.0,
                -1.0, -1.0, -1.0));
    }
    /**
     * Creates a (0 0) centered Mixed {@link LineString} corresponding to the WKT :
     * LINESTRING Z (1.0 1.0, 0.0 0.0 1.0, -1.0 -1.0)
     *
     * @return A (0 0) centered Mixed {@link LineString}.
     */
    private static LineString createLineString0Mixed() {
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(1.0, 1.0),
                new Coordinate(0.0, 0.0, 1.0),
                new Coordinate(-1.0, -1.0)};
        CoordinateSequence cs = new CoordinateArraySequence(coordinates);
        return FACTORY.createLineString(cs);
    }
    /**
     * Creates a (0 0) centered 2D {@link Polygon} corresponding to the WKT :
     * POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0))
     *
     * @return A (0 0) centered 2D {@link Polygon}.
     */
    private static Polygon createPolygon02D() {
        return FACTORY.createPolygon(FACTORY.createLinearRing(doublesTo2DCS(
                2.0, 2.0,
                2.0, -2.0,
                -2.0, -2.0,
                -2.0, 2.0,
                2.0, 2.0)));
    }
    /**
     * Creates a (0 0) centered 2D {@link Polygon} with hole corresponding to the WKT :
     * POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *          (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0))
     *
     * @return A (0 0) centered 2D {@link Polygon} with hole.
     */
    private static Polygon createPolygonHole02D() {
        LinearRing shell = FACTORY.createLinearRing(doublesTo2DCS(
                2.0, 2.0,
                2.0, -2.0,
                -2.0, -2.0,
                -2.0, 2.0,
                2.0, 2.0));
        LinearRing hole1 = FACTORY.createLinearRing(doublesTo2DCS(
                1.0, 1.0,
                1.0, -1.0,
                -1.0, -1.0,
                -1.0, 1.0,
                1.0, 1.0));
        return FACTORY.createPolygon(shell, new LinearRing[]{hole1});
    }
    /**
     * Creates a (0 0) centered 3D {@link Polygon} corresponding to the WKT :
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0))
     *
     * @return A (0 0) centered 3D {@link Polygon}.
     */
    private static Polygon createPolygon03D() {
        return FACTORY.createPolygon(FACTORY.createLinearRing(doublesTo2DCS(
                2.0, 2.0, 2.0,
                2.0, -2.0, 0.0,
                -2.0, -2.0, -2.0,
                -2.0, 2.0, 0.0,
                2.0, 2.0, 2.0)));
    }
    /**
     * Creates a (0 0) centered 3D {@link Polygon} with hole corresponding to the WKT :
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *            (1.0 1.0 1.0, 1.0 -1.0 0.0, -1.0 -1.0 -1.0, -1.0 1.0 0.0, 1.0 1.0 1.0))
     *
     * @return A (0 0) centered 3D {@link Polygon} with hole.
     */
    private static Polygon createPolygonHole03D() {
        LinearRing shell = FACTORY.createLinearRing(doublesTo3DCS(
                2.0, 2.0, 2.0,
                2.0, -2.0, 0.0,
                -2.0, -2.0, -2.0,
                -2.0, 2.0, 0.0,
                2.0, 2.0, 2.0));
        LinearRing hole1 = FACTORY.createLinearRing(doublesTo3DCS(
                1.0, 1.0, 1.0,
                1.0, -1.0, 0.0,
                -1.0, -1.0, -1.0,
                -1.0, 1.0, 0.0,
                1.0, 1.0, 1.0));
        return FACTORY.createPolygon(shell, new LinearRing[]{hole1});
    }
    /**
     * Creates a (0 0) centered Mixed {@link Polygon} corresponding to the WKT :
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0, -2.0 -2.0 -2.0, -2.0 2.0, 2.0 2.0 2.0))
     *
     * @return A (0 0) centered Mixed {@link Polygon}.
     */
    private static Polygon createPolygon0Mixed() {
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(2.0, 2.0, 2.0),
                new Coordinate(2.0, -2.0),
                new Coordinate(-2.0, -2.0, -2.0),
                new Coordinate(-2.0, 2.0, 0.0),
                new Coordinate(2.0, 2.0)};
        return FACTORY.createPolygon(FACTORY.createLinearRing(coordinates));
    }
    /**
     * Creates a (0 0) centered Mixed {@link Polygon} with hole corresponding to the WKT :
     * POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0, -2.0 -2.0 -2.0, -2.0 2.0, 2.0 2.0 2.0),
     *            (1.0 1.0 1.0, 1.0 -1.0 0.0, -1.0 -1.0 -1.0, -1.0 1.0 0.0, 1.0 1.0 1.0))
     *
     * @return A (0 0) centered Mixed {@link Polygon} with hole.
     */
    private static Polygon createPolygonHole0Mixed() {
        LinearRing shell = FACTORY.createLinearRing(new Coordinate[]{
                new Coordinate(2.0, 2.0, 2.0),
                new Coordinate(2.0, -2.0),
                new Coordinate(-2.0, -2.0, -2.0),
                new Coordinate(-2.0, 2.0, 0.0),
                new Coordinate(2.0, 2.0)});
        LinearRing hole1 = FACTORY.createLinearRing(new Coordinate[]{
                new Coordinate(1.0, 1.0),
                new Coordinate(1.0, -1.0, 0.0),
                new Coordinate(-1.0, -1.0),
                new Coordinate(-1.0, 1.0, 0.0),
                new Coordinate(1.0, 1.0, 1.0)});
        return FACTORY.createPolygon(shell, new LinearRing[]{hole1});
    }
    /**
     * Creates a (0 0) centered 2D {@link MultiPoint} corresponding to the WKT :
     * MULTIPOINT ((1.0 1.0), (0.0 0.0), (-1.0 -1.0))
     *
     * @return A (0 0) centered 2D {@link MultiPoint}.
     */
    private static MultiPoint createMultiPoint02D() {
        Point point1 = FACTORY.createPoint(doublesTo2DCS(1.0, 1.0));
        Point point2 = FACTORY.createPoint(doublesTo2DCS(0.0, 0.0));
        Point point3 = FACTORY.createPoint(doublesTo2DCS(-1.0, -1.0));
        return FACTORY.createMultiPoint(new Point[]{point1, point2, point3});
    }
    /**
     * Creates a (0 0) centered 3D {@link MultiPoint} corresponding to the WKT :
     * MULTIPOINT Z ((1.0 1.0 1.0), (0.0 0.0 0.0), (-1.0 -1.0 -1.0))
     *
     * @return A (0 0) centered 3D {@link MultiPoint}.
     */
    private static MultiPoint createMultiPoint03D() {
        Point point1 = FACTORY.createPoint(doublesTo3DCS(1.0, 1.0, 1.0));
        Point point2 = FACTORY.createPoint(doublesTo3DCS(0.0, 0.0, 0.0));
        Point point3 = FACTORY.createPoint(doublesTo3DCS(-1.0, -1.0, -1.0));
        return FACTORY.createMultiPoint(new Point[]{point1, point2, point3});
    }
    /**
     * Creates a (0 0) centered Mixed {@link MultiPoint} corresponding to the WKT :
     * MULTIPOINT Z ((1.0 1.0 1.0), (0.0 0.0), (-1.0 -1.0 -1.0))
     *
     * @return A (0 0) centered Mixed {@link MultiPoint}.
     */
    private static MultiPoint createMultiPoint0Mixed() {
        Point point1 = FACTORY.createPoint(doublesTo3DCS(1.0, 1.0, 1.0));
        Point point2 = FACTORY.createPoint(doublesTo2DCS(0.0, 0.0));
        Point point3 = FACTORY.createPoint(doublesTo3DCS(-1.0, -1.0, -1.0));
        return FACTORY.createMultiPoint(new Point[]{point1, point2, point3});
    }
    /**
     * Creates a (0 0) centered 2D {@link MultiLineString} corresponding to the WKT :
     * MULTILINESTRING ((1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                  (1.0 0.0, 0.0 0.0, -1.0 0.0),
     *                  (-1.0 1.0, 0.0 0.0, 1.0 1.0))
     *
     * @return A (0 0) centered 2D {@link MultiLineString}.
     */
    private static MultiLineString createMultiLineString02D() {
        LineString lineString1 = FACTORY.createLineString(doublesTo2DCS(
                1.0, 1.0,
                0.0, 0.0,
                -1.0, -1.0));
        LineString lineString2 = FACTORY.createLineString(doublesTo2DCS(
                1.0, 0.0,
                0.0, 0.0,
                -1.0, 0.0));
        LineString lineString3 = FACTORY.createLineString(doublesTo2DCS(
                -1.0, 1.0,
                0.0, 0.0,
                1.0, 1.0));
        return FACTORY.createMultiLineString(new LineString[]{lineString1, lineString2, lineString3});
    }
    /**
     * Creates a (0 0) centered 3D {@link MultiLineString} corresponding to the WKT :
     * MULTILINESTRING Z ((1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                    (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                    (-1.0 1.0 0.0 , 0.0 0.0 1.0, 1.0 -1.0 0.0))
     *
     * @return A (0 0) centered 3D {@link MultiLineString}.
     */
    private static MultiLineString createMultiLineString03D() {
        LineString lineString1 = FACTORY.createLineString(doublesTo3DCS(
                1.0, 1.0, 1.0,
                0.0, 0.0, 0.0,
                -1.0, -1.0, -1.0));
        LineString lineString2 = FACTORY.createLineString(doublesTo3DCS(
                1.0, 0.0, 0.0,
                0.0, 0.0, 1.0,
                -1.0, 0.0, 0.0));
        LineString lineString3 = FACTORY.createLineString(doublesTo3DCS(
                -1.0, 1.0, 0.0,
                0.0, 0.0, 1.0,
                1.0, -1.0, 0.0));
        return FACTORY.createMultiLineString(new LineString[]{lineString1, lineString2, lineString3});
    }
    /**
     * Creates a (0 0) centered Mixed {@link MultiLineString} corresponding to the WKT :
     * MULTILINESTRING Z ((1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                    (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                    (-1.0 1.0, 0.0 0.0, 1.0 1.0))
     *
     * @return A (0 0) centered Mixed {@link MultiLineString}.
     */
    private static MultiLineString createMultiLineString0Mixed() {
        LineString lineString1 = FACTORY.createLineString(doublesTo2DCS(
                1.0, 1.0,
                0.0, 0.0,
                -1.0, -1.0));
        LineString lineString2 = FACTORY.createLineString(doublesTo3DCS(
                1.0, 0.0, 0.0,
                0.0, 0.0, 1.0,
                -1.0, 0.0, 0.0));
        LineString lineString3 = FACTORY.createLineString(doublesTo2DCS(
                -1.0, 1.0,
                0.0, 0.0,
                1.0, 1.0));
        return FACTORY.createMultiLineString(new LineString[]{lineString1, lineString2, lineString3});
    }
    /**
     * Creates a (0 0) centered 2D {@link MultiPolygon} corresponding to the WKT :
     * MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *               ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     *
     * @return A (0 0) centered 2D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygon02D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                3.0, 3.0,
                3.0, -3.0,
                -3.0, -3.0,
                -3.0, 3.0,
                3.0, 3.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygon2D(), poly1});
    }
    /**
     * Creates a (0 0) centered 2D {@link MultiPolygon} with hole corresponding to the WKT :
     * MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *                (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *               ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     *
     * @return A (0 0) centered 2D {@link MultiPolygon} with hole.
     */
    private static MultiPolygon createMultiPolygonHole02D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                3.0, 3.0,
                3.0, -3.0,
                -3.0, -3.0,
                -3.0, 3.0,
                3.0, 3.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygonHole2D(), poly1});
    }
    /**
     * Creates a (0 0) centered 3D {@link MultiPolygon} corresponding to the WKT :
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                 ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     *
     * @return A (0 0) centered 3D {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygon03D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo3DCS(
                3.0, 3.0, 3.0,
                3.0, -3.0, 0.0,
                -3.0, -3.0, -3.0,
                -3.0, 3.0, 0.0,
                3.0, 3.0, 3.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygon3D(), poly1});
    }
    /**
     * Creates a (0 0) centered 3D {@link MultiPolygon} with hole corresponding to the WKT :
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                 ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     *
     * @return A (0 0) centered 3D {@link MultiPolygon} with hole.
     */
    private static MultiPolygon createMultiPolygonHole03D() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo3DCS(
                3.0, 3.0, 3.0,
                3.0, -3.0, 0.0,
                -3.0, -3.0, -3.0,
                -3.0, 3.0, 0.0,
                3.0, 3.0, 3.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygonHole3D(), poly1});
    }
    /**
     * Creates a (0 0) centered Mixed {@link MultiPolygon} corresponding to the WKT :
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                 ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     *
     * @return A (0 0) centered Mixed {@link MultiPolygon}.
     */
    private static MultiPolygon createMultiPolygon0Mixed() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                3.0, 3.0, 3.0,
                3.0, -3.0, 0.0,
                -3.0, -3.0, -3.0,
                -3.0, 3.0, 0.0,
                3.0, 3.0, 3.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygon3D(), poly1});
    }
    /**
     * Creates a (0 0) centered Mixed {@link MultiPolygon} with hole corresponding to the WKT :
     * MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                 ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     *
     * @return A (0 0) centered Mixed {@link MultiPolygon} with hole.
     */
    private static MultiPolygon createMultiPolygonHole0Mixed() {
        Polygon poly1 = FACTORY.createPolygon(doublesTo2DCS(
                3.0, 3.0, 3.0,
                3.0, -3.0, 0.0,
                -3.0, -3.0, -3.0,
                -3.0, 3.0, 0.0,
                3.0, 3.0, 3.0));
        return FACTORY.createMultiPolygon(new Polygon[]{createPolygonHole2D(), poly1});
    }
    /**
     * Creates a (0 0) centered 2D {@link GeometryCollection} corresponding to the WKT :
     * GEOMETRYCOLLECTION (
     *                     POINT (0.0 0.0),
     *                     LINESTRING (1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                     POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                     POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *                              (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                     MULTIPOINT ((1.0 1.0), (0.0 0.0), (-1.0 -1.0)),
     *                     MULTILINESTRING ((1.0 1.0, 0.0 0.0, -1.0 -1.0),
     *                                      (1.0 0.0, 0.0 0.0, -1.0 0.0),
     *                                      (-1.0 1.0, 0.0 0.0, 1.0 1.0)),
     *                     MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                                   ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0))),
     *                     MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0),
     *                                    (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                                   ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0)))
     * )
     *
     * @return A (0 0) centered 2D {@link GeometryCollection}.
     */
    private static GeometryCollection createGeometryCollection02D() {
        return FACTORY.createGeometryCollection(new Geometry[]{createPoint02D(), createLineString02D(),
                createPolygon02D(), createPolygonHole02D(), createMultiPoint02D(), createMultiLineString02D(),
                createMultiPolygon02D(), createMultiPolygonHole02D()});
    }
    /**
     * Creates a (0 0) centered 3D {@link GeometryCollection} corresponding to the WKT :
     * GEOMETRYCOLLECTION (
     *                     POINT Z (0.0 0.0 0.0),
     *                     LINESTRING Z (1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                     POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                     POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                     MULTIPOINT Z ((1.0 1.0 1.0), (0.0 0.0 0.0), (-1.0 -1.0 -1.0)),
     *                     MULTILINESTRING Z ((1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                                        (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                                        (-1.0 1.0 0.0 , 0.0 0.0 1.0, 1.0 -1.0 0.0)),
     *                     MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0)),
     *                                     ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0))),
     *                     MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                      (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                                     ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     * )
     *
     * @return A (0 0) centered 3D {@link GeometryCollection}.
     */
    private static GeometryCollection createGeometryCollection03D() {
        return FACTORY.createGeometryCollection(new Geometry[]{createPoint03D(), createLineString03D(),
                createPolygon03D(), createPolygonHole03D(), createMultiPoint03D(), createMultiLineString03D(),
                createMultiPolygon03D(), createMultiPolygonHole03D()});
    }
    /**
     * Creates a (0 0) centered Mixed {@link GeometryCollection} corresponding to the WKT :
     * GEOMETRYCOLLECTION (
     *                     POINT (0.0 0.0),
     *                     LINESTRING Z (1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                     POLYGON ((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                     POLYGON Z ((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                     MULTIPOINT ((1.0 1.0), (0.0 0.0), (-1.0 -1.0)),
     *                     MULTILINESTRING Z ((1.0 1.0 1.0, 0.0 0.0 0.0, -1.0 -1.0 -1.0),
     *                                        (1.0 0.0 0.0, 0.0 0.0 1.0, -1.0 0.0 0.0),
     *                                        (-1.0 1.0 0.0 , 0.0 0.0 1.0, 1.0 -1.0 0.0)),
     *                     MULTIPOLYGON (((2.0 2.0, 2.0 -2.0, -2.0 -2.0, -2.0 2.0, 2.0 2.0)),
     *                                   ((3.0 3.0, 3.0 -3.0, -3.0 -3.0, -3.0 3.0, 3.0 3.0))),
     *                     MULTIPOLYGON Z (((2.0 2.0 2.0, 2.0 -2.0 0.0, -2.0 -2.0 -2.0, -2.0 2.0 0.0, 2.0 2.0 2.0),
     *                                      (1.0 1.0, 1.0 -1.0, -1.0 -1.0, -1.0 1.0, 1.0 1.0)),
     *                                     ((3.0 3.0 3.0, 3.0 -3.0 0.0, -3.0 -3.0 -3.0, -3.0 3.0 0.0, 3.0 3.0 3.0)))
     * )
     *
     * @return A (0 0) centered Mixed {@link GeometryCollection}.
     */
    private static GeometryCollection createGeometryCollection0Mixed() {
        return FACTORY.createGeometryCollection(new Geometry[]{createPoint02D(), createLineString03D(),
                createPolygon02D(), createPolygonHole03D(), createMultiPoint02D(), createMultiLineString03D(),
                createMultiPolygon02D(), createMultiPolygonHole03D()});
    }
}
