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
 * to 3 and update the z value
 * 
 * @author Erwan Bocher
 */
public class UpdateGeometryZ {
    
     static final GeometryFactory gf = new GeometryFactory();
    
    /**
     * Force the dimension of the geometry to 3, update correctly the coordinate
     * dimension and change the z value
     * @param geom the input geometry
     * @param z the value to update
     * @return 
     */
     public static Geometry force(Geometry geom, double z) {
        Geometry g = geom;
        if (geom instanceof Point) {
            g = gf.createPoint(convertSequence(geom.getCoordinates(),z));
        } else if (geom instanceof LineString) {
            g = gf.createLineString(convertSequence(geom.getCoordinates(),z));
        } else if (geom instanceof Polygon) {
            g = UpdateGeometryZ.convert((Polygon) geom,z);
        } else if (geom instanceof MultiPoint) {
            g = gf.createMultiPoint(convertSequence(geom.getCoordinates(),z));
        } else if (geom instanceof MultiLineString) {
            g = UpdateGeometryZ.convert((MultiLineString) geom,z);
        } else if (geom instanceof MultiPolygon) {
            g = UpdateGeometryZ.convert((MultiPolygon) geom,z);
        } else if (geom instanceof GeometryCollection) {
            g = UpdateGeometryZ.convert((GeometryCollection)geom,z);
        }
        g.setSRID(geom.getSRID());
        return g;
    }

     /**
      * Force the dimension of the GeometryCollection and update correctly the coordinate 
      * dimension
      * @param gc
      * @param z
      * @return 
      */
    public static GeometryCollection convert(GeometryCollection gc, double z) {
        int nb = gc.getNumGeometries();
        final Geometry[] geometries = new Geometry[nb];        
        for (int i = 0; i < nb; i++) {            
            geometries[i]=force(gc.getGeometryN(i),z);
        }
        return gf.createGeometryCollection(geometries);        
    }

    /**
     * Force the dimension of the MultiPolygon and update correctly the coordinate 
     * dimension
     * @param multiPolygon
     * @param z
     * @return 
     */
    public static MultiPolygon convert(MultiPolygon multiPolygon,double z) {
        int nb = multiPolygon.getNumGeometries();
        final Polygon[] pl = new Polygon[nb];
        for (int i = 0; i < nb; i++) {
            pl[i] = UpdateGeometryZ.convert((Polygon) multiPolygon.getGeometryN(i),z);
        }
        return gf.createMultiPolygon(pl);
    }

     /**
     * Force the dimension of the MultiLineString and update correctly the coordinate 
     * dimension
     * @param multiLineString
     * @param z
     * @return 
     */
    public static MultiLineString convert(MultiLineString multiLineString, double z) {
        int nb = multiLineString.getNumGeometries();
        final LineString[] ls = new LineString[nb];
        for (int i = 0; i < nb; i++) {
            ls[i] = UpdateGeometryZ.convert((LineString) multiLineString.getGeometryN(i),z);
        }
        return gf.createMultiLineString(ls);
    }

    /**
     * Force the dimension of the Polygon and update correctly the coordinate 
     * dimension
     * @param polygon
     * @param z
     * @return 
     */
    public static Polygon convert(Polygon polygon, double z) {
        LinearRing shell = gf.createLinearRing(convertSequence(polygon.getExteriorRing().getCoordinates(),z));
        int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] =  gf.createLinearRing(convertSequence(polygon.getInteriorRingN(i).getCoordinates(),z));
        }
        return gf.createPolygon(shell, holes);
    }   
    
    

    /**
     * Force the dimension of the LineString and update correctly the coordinate 
     * dimension
     * @param lineString
     * @param z
     * @return 
     */
    public static LineString convert(LineString lineString,double z) {
        return gf.createLineString(convertSequence(lineString.getCoordinates(),z));
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate 
     * dimension
     * @param linearRing
     * @param z
     * @return 
     */
    public static LinearRing convert(LinearRing linearRing,double z) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinates(),z));
    }

    /**
     * Create a new CoordinateArraySequence, update its dimension and z value
     * If z value is equal to NaN set default to 0
     * @param cs a coordinate array
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(Coordinate[] cs,double z) {
        for (int i = 0; i < cs.length; i++) {
            Coordinate coord = cs[i];
            coord = new Coordinate(coord);
            double currentZ  = coord.z;
            if (Double.isNaN(currentZ)) {
                coord.z = Double.isNaN(z)?0:z;
                cs[i]=coord;
            }
        }
        return new CoordinateArraySequence(cs, 3);
    }
}
