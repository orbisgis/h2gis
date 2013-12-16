/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.clean;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;

/**
 * This utility class provides cleaning utilities for JTS {@link Geometry}
 * objects.
 *
 * @author Erwan Bocher
 */
public final class GeometryClean {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    /**
     * Create a nice Polygon from the given Polygon. Will ensure that shells are
     * clockwise and holes are counter-clockwise.
     *
     * @param p The Polygon to make "nice".
     * @return The "nice" Polygon.
     */
    public static Polygon makeGoodShapePolygon(Polygon p) {
        LinearRing outer;
        LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
        Coordinate[] coords;

        coords = p.getExteriorRing().getCoordinates();

        if (CGAlgorithms.isCCW(coords)) {
            outer = (LinearRing) p.getExteriorRing().reverse();
        } else {
            outer = (LinearRing) p.getExteriorRing();
        }

        for (int t = 0, tt = p.getNumInteriorRing(); t < tt; t++) {
            coords = p.getInteriorRingN(t).getCoordinates();

            if (!(CGAlgorithms.isCCW(coords))) {
                holes[t] = (LinearRing) p.getInteriorRingN(t).reverse();
            } else {
                holes[t] = (LinearRing) p.getInteriorRingN(t);
            }
        }

        return FACTORY.createPolygon(outer, holes);
    }

    /**
     * Like makeGoodShapePolygon, but applied towards a multipolygon.
     *
     * @param mp The MultiPolygon to "niceify".
     * @return The "nicified" MultiPolygon.
     */
    public static MultiPolygon makeGoodShapeMultiPolygon(MultiPolygon mp) {
        MultiPolygon result;
        Polygon[] ps = new Polygon[mp.getNumGeometries()];

        //check each sub-polygon
        for (int t = 0; t < mp.getNumGeometries(); t++) {
            ps[t] = makeGoodShapePolygon((Polygon) mp.getGeometryN(t));
        }

        result = FACTORY.createMultiPolygon(ps);

        return result;
    }

    /**
     * Removes duplicated points within a geometry.
     *
     * @param geom
     * @return
     */
    public static Geometry removeDuplicatedPoints(Geometry geom) {
        if (geom.isEmpty()) {
            return geom;
        } else if (geom.getDimension() == 0) {
            return geom;
        } else if (geom instanceof LineString) {
            return removeDuplicatedPoints((LineString) geom);
        } else if (geom instanceof MultiLineString) {
            return removeDuplicatedPoints((MultiLineString) geom);
        } else if (geom instanceof Polygon) {
            return removeDuplicatedPoints((Polygon) geom);
        } else if (geom instanceof MultiPolygon) {
            return removeDuplicatedPoints((MultiPolygon) geom);
        } else if (geom instanceof GeometryCollection) {
            return removeDuplicatedPoints((GeometryCollection) geom);
        }
        return null;
    }

    /**
     * Removes duplicated points within a LineString.
     *
     * @param g
     * @return
     */
    public static LineString removeDuplicatedPoints(LineString g) {
        Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(g.getCoordinates());
        return FACTORY.createLineString(coords);
    }

    /**
     * Removes duplicated points within a linearRing.
     *
     * @param g
     * @return
     */
    public static LinearRing removeDuplicatedPoints(LinearRing g) {
        Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(g.getCoordinates());
        return FACTORY.createLinearRing(coords);
    }

    /**
     * Removes duplicated points in a MultiLineString.
     *
     * @param g
     * @return
     */
    public static MultiLineString removeDuplicatedPoints(MultiLineString g) {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            LineString line = (LineString) g.getGeometryN(i);
            lines.add(removeDuplicatedPoints(line));
        }
        return FACTORY.createMultiLineString(GeometryFactory.toLineStringArray(lines));
    }

    /**
     * Removes duplicated points within a Polygon.
     *
     * @param poly
     * @return
     */
    public static Polygon removeDuplicatedPoints(Polygon poly) {
        Coordinate[] shellCoords = CoordinateArrays.removeRepeatedPoints(poly.getExteriorRing().getCoordinates());
        LinearRing shell = FACTORY.createLinearRing(shellCoords);
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            Coordinate[] holeCoords = CoordinateArrays.removeRepeatedPoints(poly.getInteriorRingN(i).getCoordinates());
            holes.add(FACTORY.createLinearRing(holeCoords));
        }
        return FACTORY.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
    }

    /**
     * Removes duplicated points within a MultiPolygon.
     *
     * @param g
     * @return
     */
    public static MultiPolygon removeDuplicatedPoints(MultiPolygon g) {
        ArrayList<Polygon> polys = new ArrayList<Polygon>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Polygon poly = (Polygon) g.getGeometryN(i);
            polys.add(removeDuplicatedPoints(poly));
        }
        return FACTORY.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
    }

    /**
     * Removes duplicated points within a GeometryCollection
     *
     * @param g
     * @return
     */
    public static GeometryCollection removeDuplicatedPoints(GeometryCollection g) {
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Geometry geom = g.getGeometryN(i);
            geoms.add(removeDuplicatedPoints(geom));
        }
        return FACTORY.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
    }

    /**
     * Private constructor for utility class.
     */
    private GeometryClean() {
    }
}
