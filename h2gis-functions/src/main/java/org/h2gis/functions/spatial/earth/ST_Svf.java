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
package org.h2gis.functions.spatial.earth;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.operation.distance.GeometryLocation;
import java.sql.SQLException;
import java.util.List;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.edit.EditUtilities;
import org.h2gis.utilities.jts_utils.CoordinateUtils;

/**
 * This function will be used to compute the Sky View Factor based on geometries
 * @author Erwan Bocher
 */
public class ST_Svf extends DeterministicScalarFunction{

    @Override
    public String getJavaStaticMethod() {
        return "computeSvf";
    }
    
    /**
     * The method to compute the Sky View Factor
     * @param pt
     * @param distance
     * @param angle
     * @param geoms
     * @return 
     */
    public static double computeSvf(Point pt, double distance, int angle, Geometry geoms){
        double svf = -1;
        if(pt ==null){
            return svf;
        }
        if(geoms == null){
            return svf;
        }
        if(distance<=0){
            throw new IllegalArgumentException("The distance value must be greater than 0");
        }
        
        if(angle <=0 || angle >360){
            throw new IllegalArgumentException("The angle value must be included between 0 and 360°");
        }

        if (geoms.getDimension() > 0) {            
            GeometryFactory factory = pt.getFactory();
            //Convert input geoms to a set of linestring
            STRtree sTRtree = new STRtree();
            int nbGeoms = geoms.getNumGeometries();
            for (int i = 0; i < nbGeoms; i++) {
                Geometry subGeom = geoms.getGeometryN(i);
                if (subGeom instanceof LineString) {
                    addSegments(subGeom.getCoordinates(), factory, sTRtree);
                } else if (subGeom instanceof Polygon) {
                    Polygon p = (Polygon) subGeom;
                    addSegments(p.getExteriorRing().getCoordinates(), factory, sTRtree);
                    int nbInterior = p.getNumInteriorRing();
                    for (int j = 0; j < nbInterior; j++) {
                        addSegments(p.getInteriorRingN(j).getCoordinates(), factory, sTRtree);
                    }
                }
            }
            
            double x = pt.getX();
            double y = pt.getY();
            double z = Double.isNaN(pt.getCoordinate().z)?0:pt.getCoordinate().z;
            double sumArea = 2*Math.PI; 
            double angleToRadians = Math.toRadians(angle);
            //Compute the  SVF for each ray according an angle            
            for (int i = 0; i < 360; i+=angle) {                
                double iRadians = Math.toRadians(i);
                Coordinate[] coordinates = new Coordinate[2];
                coordinates[0] = pt.getCoordinate();
                coordinates[1] =  new Coordinate(x + distance* Math.sin(iRadians), y+ distance*Math.cos(iRadians));
                LineString ray = factory.createLineString(coordinates);    
                double max =0;
                List<LineString> interEnv = sTRtree.query(ray.getEnvelopeInternal());                
                if (!interEnv.isEmpty()) {
                    for (LineString lineGeoms : interEnv) {
                        if (lineGeoms.intersects(ray)) {
                            Point ptsIntersect = (Point) lineGeoms.intersection(ray);
                            double coordWithZ = CoordinateUtils.interpolate(lineGeoms.getCoordinateN(0), lineGeoms.getCoordinateN(1), ptsIntersect.getCoordinate());                            
                            double distancePoint = ptsIntersect.distance(pt);
                            double ratio = (coordWithZ-z)/distancePoint;
                            if(ratio>max){
                                max = ratio;
                            }
                        }
                    }
                } 
                sumArea-=Math.atan(max)*Math.sin(angleToRadians);
            }
            svf = sumArea/(2*Math.PI);
        }
        
        return svf;
        
    }
    
    /**
     * 
     * @param coords
     * @param factory
     * @param strtree 
     */
    public static void addSegments(final Coordinate[] coords, GeometryFactory factory, STRtree strtree) {
        Coordinate[] coordsFiltered = CoordinateArrays.removeRepeatedPoints(coords);
        for (int j = 0; j < coordsFiltered.length - 1; j++) {
            Coordinate startCoord = coordsFiltered[j];
            Coordinate endCoord = coordsFiltered[j + 1];
            if (!(Double.isNaN(startCoord.z) || Double.isNaN(endCoord.z))) {
                LineString lineString = factory.createLineString(
                        new Coordinate[]{startCoord, endCoord});
                strtree.insert(lineString.getEnvelopeInternal(), lineString);
            }
        }
    }

}
