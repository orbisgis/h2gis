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
 * Forces a Geometry into 3DM mode by returning a copy with
 * If a geometry has no M component, then a 0 M coordinate is tacked on.
 * Z value is removed
 *
 * @author Erwan Bocher, CNRS, 2020
 */
public class ST_Force3DM extends DeterministicScalarFunction {

    static final GeometryFactory gf = new GeometryFactory();

    public ST_Force3DM() {
        addProperty(PROP_REMARKS, "Forces the geometries into XYM mode.\n "
                + "If a geometry has no M component, then a M value is tacked on." +
                " Z value is removed. Default M value is z to zero.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force3DM";
    }

    /**
     * Converts a XY, XYZ geometry to XYM.
     *
     * @param geom {@link Geometry}
     * @return Geometry
     */
    public static Geometry force3DM(Geometry geom) {
        if (geom == null) {
            return null;
        }
        return forceXYM(geom,0 );
    }

    /**
     * Converts a XY, XYZ geometry to XYM.
     *
     * @param geom {@link Geometry}
     * @param mValue m value
     * @return M Geometry
     */
    public static Geometry force3DM(Geometry geom, double mValue) {
        if (geom == null) {
            return null;
        }
        return forceXYM(geom, mValue);
    }


    /**
     * Force the dimension of the geometry and update correctly the coordinate
     * dimension
     * @param geom the input geometry
     * @param mValue m value
     * @return M Geometry
     */
    public static Geometry forceXYM(Geometry geom, double mValue) {
        int dimension =2;
        Geometry g = geom;
        if (geom instanceof Point) {
            CoordinateSequence cs = ((Point) geom).getCoordinateSequence();
            if(cs.getDimension()!=dimension|| cs.getMeasures()!=1) {
                g = gf.createPoint(convertSequence(cs,mValue));
                g.setSRID(geom.getSRID());
            }
        } else if (geom instanceof LineString) {
            CoordinateSequence cs = ((LineString) geom).getCoordinateSequence();
            if(cs.getDimension()!=dimension || cs.getMeasures()!=1) {
                g = gf.createLineString(convertSequence(cs,mValue));
                g.setSRID(geom.getSRID());
            }
        } else if (geom instanceof Polygon) {
            g = convert((Polygon) geom,  mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPoint) {
            g = convert((MultiPoint) geom,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiLineString) {
            g = convert((MultiLineString) geom,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPolygon) {
            g = convert((MultiPolygon) geom,mValue);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof GeometryCollection) {
            g = convert((GeometryCollection)geom,mValue);
            g.setSRID(geom.getSRID());
        }
        return g;
    }

    /**
     * Force the dimension of the GeometryCollection and update correctly the coordinate
     * dimension
     * @param gc {@link GeometryCollection}
     * @param mValue m value
     * @return GeometryCollection
     */
    public static GeometryCollection convert(GeometryCollection gc, double mValue) {
        int nb = gc.getNumGeometries();
        final Geometry[] geometries = new Geometry[nb];
        for (int i = 0; i < nb; i++) {
            geometries[i]=forceXYM(gc.getGeometryN(i), mValue);
        }
        return gf.createGeometryCollection(geometries);
    }

    /**
     * Force the dimension of the MultiPolygon and update correctly the coordinate
     * dimension
     * @param multiPolygon {@link MultiPolygon}
     * @param mValue m value
     * @return M {@link MultiPolygon}
     */
    public static MultiPolygon convert(MultiPolygon multiPolygon,double mValue) {
        int nb = multiPolygon.getNumGeometries();
        final Polygon[] pl = new Polygon[nb];
        for (int i = 0; i < nb; i++) {
            pl[i] = convert((Polygon) multiPolygon.getGeometryN(i),mValue);
        }
        return gf.createMultiPolygon(pl);
    }

    /**
     * Force the dimension of the MultiLineString and update correctly the coordinate
     * dimension
     * @param multiLineString {@link MultiLineString}
     * @param mValue m value
     * @return M {@link MultiLineString}
     */
    public static MultiLineString convert(MultiLineString multiLineString, double mValue) {
        int nb = multiLineString.getNumGeometries();
        final LineString[] ls = new LineString[nb];
        for (int i = 0; i < nb; i++) {
            ls[i] = convert((LineString) multiLineString.getGeometryN(i),mValue);
        }
        return gf.createMultiLineString(ls);
    }

    /**
     * Force the dimension of the Polygon and update correctly the coordinate
     * dimension
     * @param polygon {@link Polygon}
     * @param mValue M value
     * @return M {@link Polygon}
     */
    public static Polygon convert(Polygon polygon, double mValue) {
        CoordinateSequence cs = polygon.getExteriorRing().getCoordinateSequence();
            LinearRing shell = gf.createLinearRing(convertSequence(cs, mValue));
            int nbOfHoles = polygon.getNumInteriorRing();
            final LinearRing[] holes = new LinearRing[nbOfHoles];
            for (int i = 0; i < nbOfHoles; i++) {
                    holes[i] = gf.createLinearRing(
                            convertSequence(polygon.getInteriorRingN(i).getCoordinateSequence(), mValue));
            }
            Polygon p = gf.createPolygon(shell, holes);
            p.setSRID(polygon.getSRID());
            return p;
    }



    /**
     * Force the dimension of the LineString and update correctly the coordinate
     * dimension
     * @param lineString {@link LineString}
     * @param mValue M value
     * @return M {@link LineString}
     */
    public static LineString convert(LineString lineString,double mValue) {
        return gf.createLineString(convertSequence(lineString.getCoordinateSequence(),mValue));
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate
     * dimension
     * @param linearRing {@link LinearRing}
     * @param mValue M value
     * @return M {@link LinearRing}
     */
    public static LinearRing convert(LinearRing linearRing,double mValue) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinateSequence(),mValue));
    }

    /**
     * Create a new CoordinateArraySequence and update its dimension
     *
     * @param cs a coordinate array
     * @param mValue to set
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(CoordinateSequence cs, double mValue) {
        boolean hasM= cs.getMeasures() == 1;
        CoordinateXYM[] coordsXYM = new CoordinateXYM[cs.size()];
        for (int i = 0; i < cs.size(); i++) {
            Coordinate coordTmp = cs.getCoordinate(i);
            CoordinateXYM coord = new CoordinateXYM(coordTmp);
            if(hasM){
                coord.setM(coordTmp.getM());
            }
            else{
                coord.setM(mValue);
            }
            coordsXYM[i]=coord;
        }
        return new CoordinateArraySequence(coordsXYM, 2, 1);
    }
}
