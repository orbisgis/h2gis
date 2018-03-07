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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.math.Vector2D;
import java.util.List;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;

/**
 * This function will be used to compute the Sky View Factor based on geometries
 * @author Erwan Bocher
 */
public class ST_Svf extends DeterministicScalarFunction{

    //target step length m
    private static int TARGET_STEP_LENGTH = 10;

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
            Coordinate startCoordinate = pt.getCoordinate();
            double startZ = Double.isNaN(startCoordinate.z)?0:startCoordinate.z;
            double sumArea = 2*Math.PI; 
            double angleToRadians = Math.toRadians(angle);
            //Compute the  SVF for each ray according an angle            
            for (int i = 0; i < 360; i+=angle) {             
                //To limit the number of geometries in the query with create a progressive ray  
                Vector2D vStart = new Vector2D(startCoordinate);
                Vector2D v = Vector2D.create(0, 1);
                v.rotate(Math.toRadians(i));
                // Normalize the vector (length=1), we keep only the direction
                v.normalize();
                int stepCount = (int) Math.round(distance / TARGET_STEP_LENGTH);
                double stepLength = distance / stepCount;
                // This is the translation vector
                v = v.multiply(stepLength);
                double max = 0;
                for (int j = 0; j < stepCount; j++) {
                    LineSegment stepLine = new LineSegment(vStart.add(v.multiply(j)).toCoordinate(), vStart.add(v.multiply(j + 1)).toCoordinate());
                    LineString rayStep = stepLine.toGeometry(factory);
                    List<LineString> interEnv = sTRtree.query(rayStep.getEnvelopeInternal());
                    if (!interEnv.isEmpty()) {
                        for (LineString lineGeoms : interEnv) {
                            if (lineGeoms.intersects(rayStep)) {
                                Point ptsIntersect = (Point) lineGeoms.intersection(rayStep);
                                double coordWithZ = CoordinateUtils.interpolate(lineGeoms.getCoordinateN(0), lineGeoms.getCoordinateN(1), ptsIntersect.getCoordinate());
                                double distancePoint = ptsIntersect.distance(pt);
                                double ratio = (coordWithZ - startZ) / distancePoint;
                                if (ratio > max) {
                                    max = ratio;
                                }
                            }
                        }
                    }

                }
                sumArea -= Math.atan(max) * Math.sin(angleToRadians);
            }
            svf = sumArea / (2 * Math.PI);
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
        for (int j = 0; j < coords.length - 1; j++) {
            Coordinate startCoord = coords[j];
            Coordinate endCoord = coords[j + 1];
            if (!(Double.isNaN(startCoord.z) || Double.isNaN(endCoord.z))) {
                LineString lineString = factory.createLineString(
                        new Coordinate[]{startCoord, endCoord});
                strtree.insert(lineString.getEnvelopeInternal(), lineString);
            }
        }
    }

}
