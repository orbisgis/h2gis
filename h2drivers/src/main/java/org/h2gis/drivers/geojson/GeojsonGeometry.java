/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.geojson;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Convert a JTS geometry to a Geojson 1.0 representation.
 *
 * A geometry is a GeoJSON object where the type member's value is one of the
 * ollowing strings: "Point", "MultiPoint", "LineString", "MultiLineString",
 * "Polygon", "MultiPolygon", or "GeometryCollection".
 *
 * A GeoJSON geometry object of any type other than "GeometryCollection" must
 * have a member with the name "coordinates". The value of the coordinates
 * member is always an array. The structure for the elements in this array is
 * determined by the type of geometry.
 *
 * @author Erwan Bocher
 */
public class GeojsonGeometry {

    private GeojsonGeometry() {
    }

    /**
     * Transform a JTS geometry to a geojson representation
     *
     * @param geom
     * @param sb
     */
    public static void toGeojsonGeometry(Geometry geom, StringBuilder sb) {
        if (geom instanceof Point) {
            toGeojsonPoint(null, sb);
        } else if (geom instanceof LineString) {
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
     * Syntax :
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     * @param point
     * @param sb
     */
    public static void toGeojsonPoint(Point point, StringBuilder sb) {
        Coordinate coord = point.getCoordinate();
        sb.append("{{\"type\":\"Point\",\"coordinates\":[");
        sb.append(coord.x).append(",").append(coord.y);
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(coord.z);
        }
        sb.append("]}");
    }

    /**
     * Coordinates of LineString are an array of positions
     *
     * Syntax :
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param lineString
     * @param sb
     */
    public static void toGeojsonLineString(LineString lineString, StringBuilder sb) {
    }

    /**
     * Convert a jts array of coordinates to a geojson coordinates
     * representation
     *
     * Syntax :
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
     * Convert a JTS coordinate to a geojson representation
     *
     * Only x, y and z values are supported
     *
     * Syntax :
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
}
