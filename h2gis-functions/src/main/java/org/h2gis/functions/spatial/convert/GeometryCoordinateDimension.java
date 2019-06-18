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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * This class is used to force the coordinate dimension of a geometry
 * 
 * @author Erwan Bocher
 */
public class GeometryCoordinateDimension {
    
     static final GeometryFactory gf = new GeometryFactory();
    
    /**
     * Force the dimension of the geometry and update correctly the coordinate 
     * dimension
     * @param geom the input geometry
     * @param dimension supported dimension is 2, 3
     * if the dimension is set to 3 the z measure are set to 0
     * @return 
     */
     public static Geometry force(Geometry geom, int dimension) {
        if (geom instanceof Point) {
            return gf.createPoint(convertSequence(geom.getCoordinates(),dimension));
        } else if (geom instanceof LineString) {
            return gf.createLineString(convertSequence(geom.getCoordinates(),dimension));
        } else if (geom instanceof Polygon) {
            return GeometryCoordinateDimension.convert((Polygon) geom,dimension);
        } else if (geom instanceof MultiPoint) {
            return gf.createMultiPoint(convertSequence(geom.getCoordinates(),dimension));
        } else if (geom instanceof MultiLineString) {
            return GeometryCoordinateDimension.convert((MultiLineString) geom,dimension);
        } else if (geom instanceof MultiPolygon) {
            return GeometryCoordinateDimension.convert((MultiPolygon) geom,dimension);
        } else if (geom instanceof GeometryCollection) {
            return GeometryCoordinateDimension.convert((GeometryCollection)geom,dimension);
        }
        return geom;
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
            geometries[i]=force(gc.getGeometryN(i),dimension);
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
        LinearRing shell = GeometryCoordinateDimension.convert(polygon.getExteriorRing(),dimension);
        int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = GeometryCoordinateDimension.convert(polygon.getInteriorRingN(i),dimension);
        }
        return gf.createPolygon(shell, holes);
    }

    /**
     * Force the dimension of the LineString and update correctly the coordinate 
     * dimension
     * @param lineString
     * @param dimension
     * @return 
     */
    public static LinearRing convert(LineString lineString,int dimension) {
        return gf.createLinearRing(convertSequence(lineString.getCoordinates(),dimension));
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate 
     * dimension
     * @param linearRing
     * @param dimension
     * @return 
     */
    public static LinearRing convert(LinearRing linearRing,int dimension) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinates(),dimension));
    }

    /**
     * Create a new CoordinateArraySequence and update its dimension
     *
     * @param cs a coordinate array
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(Coordinate[] cs,int dimension) {        
        for (int i = 0; i < cs.length; i++) { 
            Coordinate coord = cs[i];
            switch (dimension) {
                case 2:
                    coord.z = Double.NaN;
                    cs[i]=coord;
                    break;
                case 3: {
                    coord = new Coordinate(coord);
                    double z = coord.z;
                    if (Double.isNaN(z)) {
                        coord.z = 0;
                        cs[i]=coord;
                    }
                    break;
                }                
                default:
                    break;
            }
        }        
        return new CoordinateArraySequence(cs, dimension);        
    }
}
