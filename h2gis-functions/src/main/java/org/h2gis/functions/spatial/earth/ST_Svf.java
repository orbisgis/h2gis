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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.math.Vector2D;
import java.util.List;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;

/**
 * This function will be used to compute the Sky View Factor based on geometries
 * @author Erwan Bocher, CNRS
 * @author Jérémy Bernard, CNRS
 * @author Nicolas Fortin, IFSTTAR
 */
public class ST_Svf extends DeterministicScalarFunction{

    //target step length m
    private static int RAY_STEP_LENGTH = 10;
    
    public ST_Svf(){
        addProperty(PROP_REMARKS, "Return the Sky View Factor (SVF) for a given point.\n"
                + "pt = Point coordinates (x, y, z) - the SVF is calculated from this point\n"
                + "geoms = Geometries used as sky obstacles (z coordinates should be given and not NaN)\n"
                + "distance = Only obstacles located within this distance from pt are considered in the calculation (double - in meters)\n"
                + "rayCount = Number of ray considered for the calculation (integer - number of direction of calculation)\n"
                + "An optional argument may be passed:\n"
                + "RAY_STEP_LENGTH = 10 (default) Each ray is subdivided to make the calculation faster. This argument set\n"
                + "the length of each subdivision");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeSvf";
    }
    
    /**
     * The method to compute the Sky View Factor
     *
     * @param pt
     * @param distance
     * @param rayCount number of rays
     * @param geoms
     * @return
     */
    public static double computeSvf(Point pt, Geometry geoms, double distance, int rayCount) {
        return computeSvf(pt, geoms, distance, rayCount, RAY_STEP_LENGTH);
    }   
  
    
    /**
     * The method to compute the Sky View Factor
     * @param pt
     * @param distance
     * @param rayCount number of rays
     * @param stepRayLength length of sub ray used to limit the number of geometries when requested
     * @param geoms
     * @return 
     */
    public static double computeSvf(Point pt, Geometry geoms, double distance, int rayCount, int stepRayLength){   
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
        
        if(rayCount < 4 ){
            throw new IllegalArgumentException("The number of rays must be greater than or equal to 4");
        }
        
        if(stepRayLength<=0){
            throw new IllegalArgumentException("The ray length parameter must be greater than 0");
        }
        
        RAY_STEP_LENGTH = stepRayLength;

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
            double elementaryAngle = sumArea / rayCount;
            int stepCount = (int) Math.round(distance / RAY_STEP_LENGTH);
            double stepLength = distance / stepCount;
            //Compute the  SVF for each ray according an angle  
            for (int i = 0; i < rayCount; i+=1) {             
                //To limit the number of geometries in the query with create a progressive ray
                Vector2D vStart = new Vector2D(startCoordinate);
                double angleRad = elementaryAngle * i;
                Vector2D v = Vector2D.create(Math.cos(angleRad), Math.sin(angleRad));
                // This is the translation vector
                v = v.multiply(stepLength);
                double max = 0;
                for (int j = 0; j < stepCount; j++) {
                    LineSegment stepLine = new LineSegment(vStart.add(v.multiply(j)).toCoordinate(), vStart.add(v.multiply(j + 1)).toCoordinate());
                    LineString rayStep = stepLine.toGeometry(factory);
                    List<LineString> interEnv = sTRtree.query(rayStep.getEnvelopeInternal());
                    if (!interEnv.isEmpty()) {
                        for (LineString lineGeoms : interEnv) {
                            Coordinate[] coords = lineGeoms.getCoordinates();
                            Coordinate coordsStart = coords[0];
                            Coordinate coordsEnd = coords[1];
                            if (Math.max(coordsStart.z, coordsEnd.z) > max * j * stepLength){
                                Geometry ptsIntersect =  lineGeoms.intersection(rayStep);
                                if (ptsIntersect instanceof Point && ptsIntersect!=null) {
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
                }
                double sinTheta = Math.sin(Math.atan(max));
                sumArea -= elementaryAngle * sinTheta * sinTheta;
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
