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
                + "If a geometry has no M component, then a 0 M coordinate is tacked on." +
                " Z value is removed");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force3DM";
    }

    /**
     * Converts a XY geometry to XYZ. If a geometry has no Z component, then a 0
     * Z coordinate is tacked on.
     *
     * @param geom
     * @return
     */
    public static Geometry force3DM(Geometry geom) {
        if (geom == null) {
            return null;
        }
        return forceXYM(geom );
    }


    /**
     * Force the dimension of the geometry and update correctly the coordinate
     * dimension
     * @param geom the input geometry
     * @return
     */
    public static Geometry forceXYM(Geometry geom) {
        int dimension =2;
        Geometry g = geom;
        if (geom instanceof Point) {
            CoordinateSequence cs = ((Point) geom).getCoordinateSequence();
            if(cs.getDimension()!=dimension|| cs.getMeasures()!=1) {
                g = gf.createPoint(convertSequence(cs));
                g.setSRID(geom.getSRID());
            }
        } else if (geom instanceof LineString) {
            CoordinateSequence cs = ((LineString) geom).getCoordinateSequence();
            if(cs.getDimension()!=dimension || cs.getMeasures()!=1) {
                g = gf.createLineString(convertSequence(cs));
                g.setSRID(geom.getSRID());
            }
        } else if (geom instanceof Polygon) {
            g = GeometryCoordinateDimension.convert((Polygon) geom, dimension);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPoint) {
            g = GeometryCoordinateDimension.convert((MultiPoint) geom,dimension);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiLineString) {
            g = GeometryCoordinateDimension.convert((MultiLineString) geom,dimension);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof MultiPolygon) {
            g = GeometryCoordinateDimension.convert((MultiPolygon) geom,dimension);
            g.setSRID(geom.getSRID());
        } else if (geom instanceof GeometryCollection) {
            g = GeometryCoordinateDimension.convert((GeometryCollection)geom,dimension);
            g.setSRID(geom.getSRID());
        }
        return g;
    }

    /**
     * Force the dimension of the GeometryCollection and update correctly the coordinate
     * dimension
     * @param gc
     * @param dimension
     * @return
     */
    public static GeometryCollection convert(GeometryCollection gc, int dimension) {
        int nb = gc.getNumGeometries();
        final Geometry[] geometries = new Geometry[nb];
        for (int i = 0; i < nb; i++) {
            geometries[i]=forceXYM(gc.getGeometryN(i));
        }
        return gf.createGeometryCollection(geometries);
    }

    /**
     * Force the dimension of the MultiPolygon and update correctly the coordinate
     * dimension
     * @param multiPolygon
     * @param dimension
     * @return
     */
    public static MultiPolygon convert(MultiPolygon multiPolygon,int dimension) {
        int nb = multiPolygon.getNumGeometries();
        final Polygon[] pl = new Polygon[nb];
        for (int i = 0; i < nb; i++) {
            pl[i] = GeometryCoordinateDimension.convert((Polygon) multiPolygon.getGeometryN(i),dimension);
        }
        return gf.createMultiPolygon(pl);
    }

    /**
     * Force the dimension of the MultiLineString and update correctly the coordinate
     * dimension
     * @param multiLineString
     * @param dimension
     * @return
     */
    public static MultiLineString convert(MultiLineString multiLineString, int dimension) {
        int nb = multiLineString.getNumGeometries();
        final LineString[] ls = new LineString[nb];
        for (int i = 0; i < nb; i++) {
            ls[i] = GeometryCoordinateDimension.convert((LineString) multiLineString.getGeometryN(i),dimension);
        }
        return gf.createMultiLineString(ls);
    }

    /**
     * Force the dimension of the Polygon and update correctly the coordinate
     * dimension
     * @param polygon
     * @param dimension
     * @return
     */
    public static Polygon convert(Polygon polygon, int dimension) {
        CoordinateSequence cs = polygon.getExteriorRing().getCoordinateSequence();
        if(cs.getDimension()!=dimension|| cs.getMeasures()==1) {
            LinearRing shell = gf.createLinearRing(convertSequence(cs));
            int nbOfHoles = polygon.getNumInteriorRing();
            final LinearRing[] holes = new LinearRing[nbOfHoles];
            for (int i = 0; i < nbOfHoles; i++) {
                CoordinateSequence csHole = polygon.getInteriorRingN(i).getCoordinateSequence();
                if(csHole.getDimension()!=dimension|| cs.getMeasures()==1) {
                    holes[i] = gf.createLinearRing(convertSequence(csHole));
                }
                else {
                    holes[i] = gf.createLinearRing(polygon.getInteriorRingN(i).getCoordinates());
                }
            }
            Polygon p = gf.createPolygon(shell, holes);
            p.setSRID(polygon.getSRID());
            return p;
        }
        return polygon;
    }



    /**
     * Force the dimension of the LineString and update correctly the coordinate
     * dimension
     * @param lineString
     * @param dimension
     * @return
     */
    public static LineString convert(LineString lineString,int dimension) {
        CoordinateSequence cs = lineString.getCoordinateSequence();
        if(cs.getDimension()!=dimension|| cs.getMeasures()==1) {
            return gf.createLineString(convertSequence(cs));
        }
        return lineString;
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate
     * dimension
     * @param linearRing
     * @param dimension
     * @return
     */
    public static LinearRing convert(LinearRing linearRing,int dimension) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinateSequence()));
    }

    /**
     * Create a new CoordinateArraySequence and update its dimension
     *
     * @param cs a coordinate array
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(CoordinateSequence cs) {
        boolean hasM=false;
        if(cs.getMeasures()==1){
            hasM =true;
        }
        CoordinateXYM[] coordsXYM = new CoordinateXYM[cs.size()];
        for (int i = 0; i < cs.size(); i++) {
            Coordinate coordTmp = cs.getCoordinate(i);
            CoordinateXYM coord = new CoordinateXYM(coordTmp);
            if(hasM){
                coord.setM(coordTmp.getM());
            }else {
                double z = coord.z;
                if (Double.isNaN(z)) {
                    coord.z = 0;
                }
            }
            coordsXYM[i]=coord;
        }
        return new CoordinateArraySequence(coordsXYM, 2, 1);
    }

    /**
     * Create a new CoordinateArraySequence with XYZM
     *
     * @param cs a coordinate array
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertXYZMSequence(CoordinateSequence cs,int dimension) {
        boolean hasM=false;
        if(cs.getMeasures()==1){
            hasM =true;
        }
        CoordinateXYZM[] coordsXYZM = new CoordinateXYZM[cs.size()];
        for (int i = 0; i < cs.size(); i++) {
            Coordinate coordTmp = cs.getCoordinate(i);
            CoordinateXYZM coord = new CoordinateXYZM(coordTmp);
            if(hasM){
                coord.setM(coordTmp.getM());
                coord.setZ(0);
            }else {
                double z = coord.z;
                if (Double.isNaN(z)) {
                    coord.z = 0;
                }
            }
            coordsXYZM[i]=coord;
        }
        return new CoordinateArraySequence(coordsXYZM, dimension);
    }
}
