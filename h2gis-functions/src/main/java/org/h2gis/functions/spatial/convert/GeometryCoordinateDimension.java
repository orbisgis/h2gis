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
 * @author Erwan Bocher, CNRS, 2020
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
         Geometry g = geom;
         if (geom instanceof Point) {
             g = gf.createPoint(convertSequence(((Point) geom).getCoordinateSequence(), dimension));
             g.setSRID(geom.getSRID());
         } else if (geom instanceof LineString) {
             g = gf.createLineString(convertSequence(((LineString) geom).getCoordinateSequence(), dimension));
             g.setSRID(geom.getSRID());
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
        LinearRing shell = gf.createLinearRing(convertSequence(polygon.getExteriorRing().getCoordinateSequence(),dimension));
        int nbOfHoles = polygon.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = gf.createLinearRing(convertSequence(polygon.getInteriorRingN(i).getCoordinateSequence(), dimension));
        }
        return  gf.createPolygon(shell, holes);
    }
    
    

    /**
     * Force the dimension of the LineString and update correctly the coordinate 
     * dimension
     * @param lineString
     * @param dimension
     * @return 
     */
    public static LineString convert(LineString lineString,int dimension) {
        return gf.createLineString(convertSequence(lineString.getCoordinateSequence(),dimension));
    }

    /**
     * Force the dimension of the LinearRing and update correctly the coordinate 
     * dimension
     * @param linearRing
     * @param dimension
     * @return 
     */
    public static LinearRing convert(LinearRing linearRing,int dimension) {
        return gf.createLinearRing(convertSequence(linearRing.getCoordinateSequence(),dimension));
    }

    /**
     * Create a new CoordinateArraySequence and update its dimension
     *
     * @param cs a coordinate array
     * @return a new CoordinateArraySequence
     */
    private static CoordinateArraySequence convertSequence(CoordinateSequence cs,int dimension) {
        if(dimension==4){
            return convertXYZMSequence(cs, dimension);
        }
        Coordinate[] coords = new Coordinate[cs.size()];
        for (int i = 0; i < cs.size(); i++) {
            Coordinate coord = cs.getCoordinate(i);
            switch (dimension) {
                case 2:
                    coord.z = Double.NaN;
                    coords[i]=coord;
                    break;
                case 3: {
                    coord = new Coordinate(coord);
                    double z = coord.z;
                    if (Double.isNaN(z)) {
                        coord.z = 0;
                    }
                    coords[i]=coord;
                    break;
                }
                default:
                    break;
            }
        }        
        return new CoordinateArraySequence(coords, dimension);
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
