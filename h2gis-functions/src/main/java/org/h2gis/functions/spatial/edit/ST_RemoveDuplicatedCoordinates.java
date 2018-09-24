/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.util.ArrayList;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;

/**
 *
 * @author Erwan Bocher
 */
public class ST_RemoveDuplicatedCoordinates extends DeterministicScalarFunction {
    
    private static final GeometryFactory FACTORY = new GeometryFactory();


    public ST_RemoveDuplicatedCoordinates() {
        addProperty(PROP_REMARKS, "Returns a version of the given geometry without duplicated coordinates.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removeDuplicatedCoordinates";
    }

    /**
     * Returns a version of the given geometry with duplicated coordinates removed.
     *
     * @param geometry
     * @return
     */
    public static Geometry removeDuplicatedCoordinates(Geometry geometry) {
        return removeCoordinates(geometry);
    }

    /**
     * Removes duplicated coordinates within a geometry.
     *
     * @param geom
     * @return
     */
    public static Geometry removeCoordinates(Geometry geom) {
        if(geom ==null){
            return null;
        }
        else if (geom.isEmpty()) {
            return geom;
        } else if (geom instanceof Point){
            return geom;        }
         else if (geom instanceof MultiPoint) {
            return removeCoordinates((MultiPoint) geom);
        } else if (geom instanceof LineString) {
            return removeCoordinates((LineString) geom);
        } else if (geom instanceof MultiLineString) {
            return removeCoordinates((MultiLineString) geom);
        } else if (geom instanceof Polygon) {
            return removeCoordinates((Polygon) geom);
        } else if (geom instanceof MultiPolygon) {
            return removeCoordinates((MultiPolygon) geom);
        } else if (geom instanceof GeometryCollection) {
            return removeCoordinates((GeometryCollection) geom);
        }
        return null;
    }
    
    
    /**
     * Removes duplicated coordinates within a MultiPoint.
     *
     * @param g
     * @return
     */
    public static MultiPoint removeCoordinates(MultiPoint g) {
        Coordinate[] coords = CoordinateUtils.removeDuplicatedCoordinates(g.getCoordinates(),false);
        return FACTORY.createMultiPoint(coords);
    }

    /**
     * Removes duplicated coordinates within a LineString.
     *
     * @param g
     * @return
     */
    public static LineString removeCoordinates(LineString g) {
        Coordinate[] coords = CoordinateUtils.removeDuplicatedCoordinates(g.getCoordinates(), false);
        return FACTORY.createLineString(coords);
    }

    /**
     * Removes duplicated coordinates within a linearRing.
     *
     * @param g
     * @return
     */
    public static LinearRing removeCoordinates(LinearRing g) {
        Coordinate[] coords = CoordinateUtils.removeDuplicatedCoordinates(g.getCoordinates(),false);
        return FACTORY.createLinearRing(coords);
    }

    /**
     * Removes duplicated coordinates in a MultiLineString.
     *
     * @param g
     * @return
     */
    public static MultiLineString removeCoordinates(MultiLineString g) {
        ArrayList<LineString> lines = new ArrayList<LineString>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            LineString line = (LineString) g.getGeometryN(i);
            lines.add(removeCoordinates(line));
        }
        return FACTORY.createMultiLineString(GeometryFactory.toLineStringArray(lines));
    }

    /**
     * Removes duplicated coordinates within a Polygon.
     *
     * @param poly
     * @return
     */
    public static Polygon removeCoordinates(Polygon poly) {
        Coordinate[] shellCoords = CoordinateUtils.removeDuplicatedCoordinates(poly.getExteriorRing().getCoordinates(),true);
        LinearRing shell = FACTORY.createLinearRing(shellCoords);
        ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            Coordinate[] holeCoords = CoordinateUtils.removeDuplicatedCoordinates(poly.getInteriorRingN(i).getCoordinates(),true);
            holes.add(FACTORY.createLinearRing(holeCoords));
        }
        return FACTORY.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
    }

    /**
     * Removes duplicated coordinates within a MultiPolygon.
     *
     * @param g
     * @return
     */
    public static MultiPolygon removeCoordinates(MultiPolygon g) {
        ArrayList<Polygon> polys = new ArrayList<Polygon>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Polygon poly = (Polygon) g.getGeometryN(i);
            polys.add(removeCoordinates(poly));
        }
        return FACTORY.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
    }

    /**
     * Removes duplicated coordinates within a GeometryCollection
     *
     * @param g
     * @return
     */
    public static GeometryCollection removeCoordinates(GeometryCollection g) {
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Geometry geom = g.getGeometryN(i);
            geoms.add(removeCoordinates(geom));
        }
        return FACTORY.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
    }
}
    
