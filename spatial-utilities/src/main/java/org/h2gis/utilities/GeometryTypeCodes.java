/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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

package org.h2gis.utilities;

/**
 * Geometry type codes as defined in OGC SFS 1.2.1
 * @author Nicolas Fortin
 */
public interface GeometryTypeCodes {
    public static final int GEOMETRY = 0;
    public static final int POINT = 1;
    public static final int LINESTRING = 2;
    public static final int POLYGON = 3;
    public static final int MULTIPOINT = 4;
    public static final int MULTILINESTRING = 5;
    public static final int MULTIPOLYGON = 6;
    public static final int GEOMCOLLECTION = 7;
    public static final int CURVE = 13;
    public static final int SURFACE = 14;
    public static final int POLYHEDRALSURFACE = 15;
    public static final int GEOMETRYZ = 1000;
    public static final int POINTZ = POINT + 1000;
    public static final int LINESTRINGZ = LINESTRING + 1000;
    public static final int POLYGONZ = POLYGON + 1000;
    public static final int MULTIPOINTZ = MULTIPOINT + 1000;
    public static final int MULTILINESTRINGZ = MULTILINESTRING + 1000;
    public static final int MULTIPOLYGONZ = MULTIPOLYGON + 1000;
    public static final int GEOMCOLLECTIONZ = GEOMCOLLECTION + 1000;
    public static final int CURVEZ = CURVE + 1000;
    public static final int SURFACEZ = SURFACE + 1000;
    public static final int POLYHEDRALSURFACEZ = POLYHEDRALSURFACE + 1000;
    public static final int GEOMETRYM = 2000;
    public static final int POINTM = POINT + 2000;
    public static final int LINESTRINGM = LINESTRING + 2000;
    public static final int POLYGONM = POLYGON + 2000;
    public static final int MULTIPOINTM = MULTIPOINT + 2000;
    public static final int MULTILINESTRINGM = MULTILINESTRING + 2000;
    public static final int MULTIPOLYGONM = MULTIPOLYGON + 2000;
    public static final int GEOMCOLLECTIONM = GEOMCOLLECTION + 2000;
    public static final int CURVEM = CURVE + 2000;
    public static final int SURFACEM = SURFACE + 2000;
    public static final int POLYHEDRALSURFACEM = POLYHEDRALSURFACE + 2000;
    public static final int GEOMETRYZM = 3000;
    public static final int POINTZM = POINT + 3000;
    public static final int LINESTRINGZM = LINESTRING + 3000;
}
