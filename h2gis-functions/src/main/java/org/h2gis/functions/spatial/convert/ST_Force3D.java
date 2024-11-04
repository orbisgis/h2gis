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

package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Forces a Geometry into 3D mode. z-coordinates set to 0 when z value is NaN, other z-coordinates are left untouched.
 *
 * @author Erwan Bocher
 */
public class ST_Force3D extends DeterministicScalarFunction {

    static final GeometryFactory gf = new GeometryFactory();

    public ST_Force3D() {
        addProperty(PROP_REMARKS, "Forces the geometries into XYZ mode.\n "
                + "If a geometry has no Z component, then a Z value is tacked on. \n" +
                "Default z value is set to zero");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force3D";
    }

    /**
     * Converts a XY geometry to XYZ. If a geometry has no Z component, then a 0
     * Z coordinate is tacked on.
     *
     * @param geom {@link Geometry}
     * @return 3D {@link Geometry}
     */
    public static Geometry force3D(Geometry geom) {
        if (geom == null) {
            return null;
        }
        return force(geom, 0);
    }
    /**
     * Converts a XY geometry to XYZ. If a geometry has no Z component, then a 0
     * Z coordinate is tacked on.
     *
     * @param geom {@link Geometry}
     * @param zValue z value
     * @return Z {@link Geometry}
     */
    public static Geometry force3D(Geometry geom, double zValue) {
        if (geom == null) {
            return null;
        }
        return force(geom, zValue);
    }

    /**
     * Force the dimension of the geometry and update correctly the coordinate
     * dimension
     * @param geom the input geometry
     * @param zValue to update
     * @return Z {@link Geometry}
     */
    public static Geometry force(Geometry geom, double zValue) {
        Geometry g = geom;
        if (geom instanceof Point) {
            g = gf.createPoint(convertSequence(((Point) geom).getCoordinateSequence(), zValue));
            g.setSRID(geom.getSRID());
        } else if (geom instanceof LineString) {
            g = gf.createLineString(convertSequence(((LineString) geom).getCoordinateSequence(), zValue));
            g.setSRID(geom.getSRID());
        } else if (geom instanceof Polygon) {
            g = convert((Polygon) geom, zValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPoint) {
            g = convert((MultiPoint) geom,zValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiLineString) {
            g = convert((MultiLineString) geom,zValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPolygon) {
            g = convert((MultiPolygon) geom,zValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof GeometryCollection) {
            g = convert((GeometryCollection)geom,zValue);
            g.setSRID(geom.getSRID());
        }
        return g;
    }

    /**
     * Force the dimension of the MultiPoint and update correctly the coordinate
     * dimension
     *
     * @param mp {@link MultiPoint}
     * @param zValue Z value
     * @return Z {@link Geometry}
     */
    public static MultiPoint convert(MultiPoint mp, double zValue) {
        int nb = mp.getNumGeometries();
        final Point[] geometries = new Point[nb];
        for (int i = 0; i < nb; i++) {
            geometries[i] = (Point) force(mp.getGeometryN(i), zValue);
        }
        return gf.createMultiPoint(geometries);
    }

    /**
     * Force the dimension of the GeometryCollection and update correctly the coordinate
     * dimension
     * @param gc {@link GeometryCollection}
     * @param zValue Z value
     * @return Z {@link GeometryCollection}
     */
    public static GeometryCollection convert(GeometryCollection gc, double zValue) {
        int nb = gc.getNumGeometries();
        final Geometry[] geometries = new Geometry[nb];
        for (int i = 0; i < nb; i++) {
            geometries[i]=force(gc.getGeometryN(i),zValue);
        }
        return gf.createGeometryCollection(geometries);
    }

    /**
     * Force the dimension of the MultiPolygon and update correctly the coordinate
     * dimension
     * @param multiPolygon {@link MultiPolygon}
     * @param zValue z value
     * @return Z {@link MultiPolygon}
     */
    public static MultiPolygon convert(MultiPolygon multiPolygon,double zValue) {
        int nb = multiPolygon.getNumGeometries();
        final Polygon[] pl = new Polygon[nb];
        for (int i = 0; i < nb; i++) {
            pl[i] = convert((Polygon) multiPolygon.getGeometryN(i),zValue);
        }
        return gf.createMultiPolygon(pl);
    }

    /**
     * Force the dimension of the MultiLineString and update correctly the coordinate
     * dimension
     * @param multiLineString {@link MultiLineString}
     * @param zValue Z value
     * @return Z {@link MultiLineString}
     */
    public static MultiLineString convert(MultiLineString multiLineString, double zValue) {
        int nb = multiLineString.getNumGeometries();
        final LineString[] ls = new LineString[nb];
        for (int i = 0; i < nb; i++) {
            ls[i] = convert((LineString) multiLineString.getGeometryN(i),zValue);
        }
        return gf.createMultiLineString(ls);
    }

    /**
     * Force the dimension of the Polygon and update correctly the coordinate
     * dimension
     * @param polygon {@link Polygon}
     * @param zValue Z value
     * @return Z {@link Polygon}
     */
    public static Polygon convert(Polygon polygon, double zValue) {
        LinearRing shell = gf.createLinearRing(convertSequence(polygon.getExteriorRing().getCoordinateSequence(),zValue));
        int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = gf.createLinearRing(convertSequence(polygon.getInteriorRingN(i).getCoordinateSequence(), zValue));
        }
        return  gf.createPolygon(shell, holes);
    }



    /**
     * Force the dimension of the LineString and update correctly the coordinate
     * dimension
     * @param lineString {@link LineString}
     * @param zValue z value
     * @return LineString
     */
    public static LineString convert(LineString lineString,double zValue) {
        return gf.createLineString(convertSequence(lineString.getCoordinateSequence(),zValue));
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate
     * dimension
     * @param linearRing linearRing
     * @param zValue z value
     * @return LinearRing
     */
    public static LinearRing convert(LinearRing linearRing,double zValue) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinateSequence(),zValue));
    }

    /**
     * Create a new CoordinateArraySequence
     * update its dimension and set a new z value
     *
     * @param cs a coordinate array
     * @param zValue the value to update
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(CoordinateSequence cs, double zValue) {
        Coordinate[] coords = new Coordinate[cs.size()];
        for (int i = 0; i < cs.size(); i++) {
            Coordinate coord = cs.getCoordinate(i);
            coord = new Coordinate(coord);
            double z = coord.z;
            if (Double.isNaN(z)) {
                coord.setZ(zValue);
            }
            coords[i] = coord;
        }
        return new CoordinateArraySequence(coords, 3);
    }
}
