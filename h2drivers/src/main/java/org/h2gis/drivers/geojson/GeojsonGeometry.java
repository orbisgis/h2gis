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
}
