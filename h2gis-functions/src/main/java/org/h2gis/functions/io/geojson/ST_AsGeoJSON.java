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

package org.h2gis.functions.io.geojson;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Transform a JTS geometry to a GeoJSON geometry representation.
 *
 * @author Erwan Bocher
 */
public class ST_AsGeoJSON extends DeterministicScalarFunction {

    public ST_AsGeoJSON() {
        addProperty(PROP_REMARKS, "Return the geometry as a Geometry Javascript Object Notation (GeoJSON 1.0) element.\n"
                + "2D and 3D Geometries are both supported.\n"
                + "GeoJSON only supports SFS 1.1 geometry types (POINT, LINESTRING, POLYGON and COLLECTION).");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGeojson";
    }

    /**
     * Convert the geometry to a GeoJSON representation.
     *
     * @param geom
     * @return
     */
    public static String toGeojson(Geometry geom) {
        StringBuilder sb = new StringBuilder();
        toGeojsonGeometry(geom, sb);
        return sb.toString();
    }
    
    
    /**
     * Transform a JTS geometry to a GeoJSON representation.
     *
     * @param geom
     * @param sb
     */
    public static void toGeojsonGeometry(Geometry geom, StringBuilder sb) {
        if (geom instanceof Point) {
            toGeojsonPoint((Point) geom, sb);
        } else if (geom instanceof LineString) {
            toGeojsonLineString((LineString) geom, sb);
        } else if (geom instanceof Polygon) {
            toGeojsonPolygon((Polygon) geom, sb);
        } else if (geom instanceof MultiPoint) {
            toGeojsonMultiPoint((MultiPoint) geom, sb);
        } else if (geom instanceof MultiLineString) {
            toGeojsonMultiLineString((MultiLineString) geom, sb);
        } else if (geom instanceof MultiPolygon) {
            toGeojsonMultiPolygon((MultiPolygon) geom, sb);
        } else {
            toGeojsonGeometryCollection((GeometryCollection) geom, sb);
        }
    }

    /**
     * For type "Point", the "coordinates" member must be a single position.
     *
     * A position is the fundamental geometry construct. The "coordinates"
     * member of a geometry object is composed of one position (in the case of a
     * Point geometry), an array of positions (LineString or MultiPoint
     * geometries), an array of arrays of positions (Polygons,
     * MultiLineStrings), or a multidimensional array of positions
     * (MultiPolygon).
     *
     * A position is represented by an array of numbers. There must be at least
     * two elements, and may be more. The order of elements must follow x, y, z
     * order (easting, northing, altitude for coordinates in a projected
     * coordinate reference system, or longitude, latitude, altitude for
     * coordinates in a geographic coordinate reference system). Any number of
     * additional elements are allowed -- interpretation and meaning of
     * additional elements is beyond the scope of this specification.
     *
     * Syntax:
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     * @param point
     * @param sb
     */
    public static void toGeojsonPoint(Point point, StringBuilder sb) {
        Coordinate coord = point.getCoordinate();
        sb.append("{\"type\":\"Point\",\"coordinates\":[");
        sb.append(coord.x).append(",").append(coord.y);
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(coord.z);
        }
        sb.append("]}");
    }

    /**
     * Coordinates of a MultiPoint are an array of positions.
     *
     * Syntax:
     *
     * { "type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param multiPoint
     * @param sb
     */
    public static void toGeojsonMultiPoint(MultiPoint multiPoint, StringBuilder sb) {
        sb.append("{\"type\":\"MultiPoint\",\"coordinates\":");
        toGeojsonCoordinates(multiPoint.getCoordinates(), sb);
        sb.append("}");
    }

    /**
     * Coordinates of LineString are an array of positions.
     *
     * Syntax:
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param lineString
     * @param sb
     */
    public static void toGeojsonLineString(LineString lineString, StringBuilder sb) {
        sb.append("{\"type\":\"LineString\",\"coordinates\":");
        toGeojsonCoordinates(lineString.getCoordinates(), sb);
        sb.append("}");
    }

    /**
     * Coordinates of a MultiLineString are an array of LineString coordinate
     * arrays.
     *
     * Syntax:
     *
     * { "type": "MultiLineString", "coordinates": [ [ [100.0, 0.0], [101.0,
     * 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }
     *
     * @param multiLineString
     * @param sb
     */
    public static void toGeojsonMultiLineString(MultiLineString multiLineString, StringBuilder sb) {
        sb.append("{\"type\":\"MultiLineString\",\"coordinates\":[");
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            toGeojsonCoordinates(multiLineString.getGeometryN(i).getCoordinates(), sb);
            if (i < multiLineString.getNumGeometries() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
    }

    /**
     * Coordinates of a Polygon are an array of LinearRing coordinate arrays.
     * The first element in the array represents the exterior ring. Any
     * subsequent elements represent interior rings (or holes).
     *
     * Syntax:
     *
     * No holes:
     *
     * { "type": "Polygon", "coordinates": [ [ [100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }
     *
     * With holes:
     *
     * { "type": "Polygon", "coordinates": [ [ [100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ], [ [100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ] ] }
     *
     *
     * @param polygon
     * @param sb
     */
    public static void toGeojsonPolygon(Polygon polygon, StringBuilder sb) {
        sb.append("{\"type\":\"Polygon\",\"coordinates\":[");
        //Process exterior ring
        toGeojsonCoordinates(polygon.getExteriorRing().getCoordinates(), sb);
        //Process interior rings
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            sb.append(",");
            toGeojsonCoordinates(polygon.getInteriorRingN(i).getCoordinates(), sb);
        }
        sb.append("]}");
    }

    /**
     * Coordinates of a MultiPolygon are an array of Polygon coordinate arrays.
     *
     * Syntax:
     *
     * { "type": "MultiPolygon", "coordinates": [ [[[102.0, 2.0], [103.0, 2.0],
     * [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], [[[100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], [[100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }
     *
     * @param multiPolygon
     * @param sb
     */
    public static void toGeojsonMultiPolygon(MultiPolygon multiPolygon, StringBuilder sb) {
        sb.append("{\"type\":\"MultiPolygon\",\"coordinates\":[");

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon p = (Polygon) multiPolygon.getGeometryN(i);
            sb.append("[");
            //Process exterior ring
            toGeojsonCoordinates(p.getExteriorRing().getCoordinates(), sb);
            //Process interior rings
            for (int j = 0; j < p.getNumInteriorRing(); j++) {
                sb.append(",");
                toGeojsonCoordinates(p.getInteriorRingN(j).getCoordinates(), sb);
            }
            sb.append("]");
            if (i < multiPolygon.getNumGeometries() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
    }

    /**
     * A GeoJSON object with type "GeometryCollection" is a geometry object
     * which represents a collection of geometry objects.
     *
     * A geometry collection must have a member with the name "geometries". The
     * value corresponding to "geometries"is an array. Each element in this
     * array is a GeoJSON geometry object.
     *
     * Syntax:
     *
     * { "type": "GeometryCollection", "geometries": [ { "type": "Point",
     * "coordinates": [100.0, 0.0] }, { "type": "LineString", "coordinates": [
     * [101.0, 0.0], [102.0, 1.0] ] } ] }
     *
     * @param geometryCollection
     * @param sb
     */
    public static void toGeojsonGeometryCollection(GeometryCollection geometryCollection, StringBuilder sb) {
        sb.append("{\"type\":\"GeometryCollection\",\"geometries\":[");
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry geom = geometryCollection.getGeometryN(i);
            if (geom instanceof Point) {
                toGeojsonPoint((Point) geom, sb);
            } else if (geom instanceof LineString) {
                toGeojsonLineString((LineString) geom, sb);
            } else if (geom instanceof Polygon) {
                toGeojsonPolygon((Polygon) geom, sb);
            }
            if (i < geometryCollection.getNumGeometries() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
    }

    /**
     * Convert a jts array of coordinates to a GeoJSON coordinates
     * representation.
     *
     * Syntax:
     *
     * [[X1,Y1],[X2,Y2]]
     *
     * @param coords
     * @param sb
     */
    public static void toGeojsonCoordinates(Coordinate[] coords, StringBuilder sb) {
        sb.append("[");
        for (int i = 0; i < coords.length; i++) {
            toGeojsonCoordinate(coords[i], sb);
            if (i < coords.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
    }

    /**
     * Convert a JTS coordinate to a GeoJSON representation.
     *
     * Only x, y and z values are supported.
     *
     * Syntax:
     *
     * [X,Y] or [X,Y,Z]
     *
     * @param coord
     * @param sb
     */
    public static void toGeojsonCoordinate(Coordinate coord, StringBuilder sb) {
        sb.append("[");
        sb.append(coord.x).append(",").append(coord.y);
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(coord.z);
        }
        sb.append("]");
    }

    /**
     * Convert a JTS Envelope to a GeoJSON representation.
     *
     * @param e The envelope
     *
     * @return The envelope encoded as GeoJSON
     */
    public String toGeoJsonEnvelope(Envelope e) {
        return new StringBuffer().append("[").append(e.getMinX()).append(",")
                .append(e.getMinY()).append(",").append(e.getMaxX()).append(",")
                .append(e.getMaxY()).append("]").toString();
    }
}
