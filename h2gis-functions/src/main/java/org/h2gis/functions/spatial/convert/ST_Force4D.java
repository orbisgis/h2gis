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
 * Forces a Geometry into 4D mode by returning a copy with
 *  z set to 0 (other z-measure are left untouched).
 *  M set to 0 (other z-measure are left untouched).
 *
 * @author Erwan Bocher
 */
public class ST_Force4D extends DeterministicScalarFunction {

    static final GeometryFactory gf = new GeometryFactory();

    public ST_Force4D() {
        addProperty(PROP_REMARKS, "Forces the geometries into XYZM mode.\n "
                + "If a geometry has no Z  or M measure, then a z and m values are tacked on." +
                "Default z and m value are set to zero.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force4D";
    }

    /**
     * Converts a geometry to XYZM.
     * If a geometry has no Z  or M measure, then a 0 is tacked on.
     *
     * @param geom {@link Geometry}
     * @return Geometry
     */
    public static Geometry force4D(Geometry geom) {
        if (geom == null) {
            return null;
        }
        return force4D(geom, 0,0);
    }

    /**
     * Converts a geometry to XYZM.
     * If a geometry has no Z  or M measure, then a 0 is tacked on.
     *
     * @param geom {@link Geometry}
     * @param  zValue  z value
     * @param mValue m value
     * @return Geometry
     */
    public static Geometry force4D(Geometry geom, double zValue, double mValue) {
        if (geom == null) {
            return null;
        }
        return force(geom, zValue,mValue);
    }


    /**
     * Force the dimension of the geometry and update correctly the coordinate
     * dimension
     * @param geom the input geometry
     * @param zValue to update
     * @param mValue to update
     * @return Geometry
     */
    public static Geometry force(Geometry geom, double zValue, double mValue) {
        Geometry g = geom;
        if (geom instanceof Point) {
            g = gf.createPoint(convertSequence(((Point) geom).getCoordinateSequence(), zValue,mValue));
            g.setSRID(geom.getSRID());
        } else if (geom instanceof LineString) {
            g = gf.createLineString(convertSequence(((LineString) geom).getCoordinateSequence(), zValue,mValue));
            g.setSRID(geom.getSRID());
        } else if (geom instanceof Polygon) {
            g = convert((Polygon) geom, zValue,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPoint) {
            g = convert((MultiPoint) geom,zValue,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiLineString) {
            g = convert((MultiLineString) geom,zValue,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPolygon) {
            g = convert((MultiPolygon) geom,zValue,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof GeometryCollection) {
            g = convert((GeometryCollection)geom,zValue,mValue);
            g.setSRID(geom.getSRID());
        }
        return g;
    }

    /**
     * Force the dimension of the MultiPoint and update correctly the coordinate
     * dimension
     *
     * @param mp {@link MultiPoint}
     * @param zValue z value
     * @param mValue m value
     * @return MultiPoint
     */
    public static MultiPoint convert(MultiPoint mp, double zValue,double mValue) {
        int nb = mp.getNumGeometries();
        final Point[] geometries = new Point[nb];
        for (int i = 0; i < nb; i++) {
            geometries[i] = (Point) force(mp.getGeometryN(i), zValue,mValue);
        }
        return gf.createMultiPoint(geometries);
    }

    /**
     * Force the dimension of the GeometryCollection and update correctly the coordinate
     * dimension
     * @param gc {@link GeometryCollection}
     * @param zValue z value
     * @param mValue m value
     * @return GeometryCollection
     */
    public static GeometryCollection convert(GeometryCollection gc, double zValue,double mValue) {
        int nb = gc.getNumGeometries();
        final Geometry[] geometries = new Geometry[nb];
        for (int i = 0; i < nb; i++) {
            geometries[i]=force(gc.getGeometryN(i),zValue,mValue);
        }
        return gf.createGeometryCollection(geometries);
    }

    /**
     * Force the dimension of the MultiPolygon and update correctly the coordinate
     * dimension
     * @param multiPolygon {@link MultiPolygon}
     * @param zValue z value
     * @param mValue m value
     * @return MultiPolygon
     */
    public static MultiPolygon convert(MultiPolygon multiPolygon,double zValue,double mValue) {
        int nb = multiPolygon.getNumGeometries();
        final Polygon[] pl = new Polygon[nb];
        for (int i = 0; i < nb; i++) {
            pl[i] = convert((Polygon) multiPolygon.getGeometryN(i),zValue,mValue);
        }
        return gf.createMultiPolygon(pl);
    }

    /**
     * Force the dimension of the MultiLineString and update correctly the coordinate
     * dimension
     * @param multiLineString {@link MultiLineString}
     * @param zValue z value
     * @param mValue m value
     * @return MultiLineString
     */
    public static MultiLineString convert(MultiLineString multiLineString, double zValue,double mValue) {
        int nb = multiLineString.getNumGeometries();
        final LineString[] ls = new LineString[nb];
        for (int i = 0; i < nb; i++) {
            ls[i] = convert((LineString) multiLineString.getGeometryN(i),zValue,mValue);
        }
        return gf.createMultiLineString(ls);
    }

    /**
     * Force the dimension of the Polygon and update correctly the coordinate
     * dimension
     * @param polygon {@link Polygon}
     * @param zValue z value
     * @param mValue m value
     * @return Polygon
     */
    public static Polygon convert(Polygon polygon, double zValue,double mValue) {
        LinearRing shell = gf.createLinearRing(convertSequence(polygon.getExteriorRing().getCoordinateSequence(),zValue,mValue));
        int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = gf.createLinearRing(convertSequence(polygon.getInteriorRingN(i).getCoordinateSequence(), zValue,mValue));
        }
        return  gf.createPolygon(shell, holes);
    }



    /**
     * Force the dimension of the LineString and update correctly the coordinate
     * dimension
     * @param lineString {@link LineString}
     * @param zValue z value
     * @param mValue m value
     * @return LineString
     */
    public static LineString convert(LineString lineString,double zValue,double mValue) {
        return gf.createLineString(convertSequence(lineString.getCoordinateSequence(),zValue,mValue));
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate
     * dimension
     * @param linearRing {@link LinearRing}
     * @param zValue z value
     * @param mValue m value
     * @return LinearRing
     */
    public static LinearRing convert(LinearRing linearRing,double zValue,double mValue) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinateSequence(),zValue,mValue));
    }

    /**
     * Create a new CoordinateArraySequence
     * update its dimension and set a new z value
     *
     * @param cs a coordinate array
     * @param zValue the value to update
     * @param mValue the value to update
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(CoordinateSequence cs, double zValue,double mValue) {
        boolean hasM=false;
        if(cs.getMeasures()==1){
            hasM =true;
        }
        Coordinate[] coords = new Coordinate[cs.size()];
        for (int i = 0; i < cs.size(); i++) {
            Coordinate coord = cs.getCoordinate(i);
            if(hasM){
                double mValue_ = coord.getM();
                if(!Double.isNaN(mValue_)){
                    mValue=mValue_;
                }
            }
            double z_tmp = coord.getZ();
            if (!Double.isNaN(z_tmp)) {
                zValue=z_tmp;
            }
            coords[i] = new CoordinateXYZM(coord.x,coord.y, zValue,mValue);;
        }
        return new CoordinateArraySequence(coords, 4);
    }
}
