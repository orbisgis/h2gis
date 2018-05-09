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

package org.h2gis.functions.spatial.split;

import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * LineIntersector is used to split an input geometry (LineString or MultiLineString) by
 * a set of geometries. 
 * @author Erwan Bocher
 */
public class ST_LineIntersector extends  DeterministicScalarFunction{   
    
    
    
    private static final RobustLineIntersector ROBUST_INTERSECTOR = new RobustLineIntersector();
    
    public ST_LineIntersector() {
        addProperty(PROP_REMARKS, "Split an input geometry by another geometry. \n"
                + "This function uses a more robust intersection algorithm than the ST_Split function.\n"
                + "It computes the intersections between the line segments of the input geometries."
                + "A collection of LineString is returned.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "lineIntersector";
    }
    
    /**
     * Split a lineal geometry by a another geometry
     * @param inputLines
     * @param clipper
     * @return 
     */
    public static Geometry lineIntersector(Geometry inputLines, Geometry clipper) throws IllegalArgumentException {
        if(inputLines == null||clipper == null){
            return null;
        }
        if(inputLines.getDimension()==1){
        MCIndexNoder mCIndexNoder = new MCIndexNoder();
        mCIndexNoder.setSegmentIntersector(new IntersectionAdder(ROBUST_INTERSECTOR));        
        mCIndexNoder.computeNodes(getSegments(inputLines, clipper));
        Collection nodedSubstring = mCIndexNoder.getNodedSubstrings();
        GeometryFactory gf = inputLines.getFactory();
        ArrayList<LineString> linestrings = new ArrayList<LineString>(nodedSubstring.size());
        for (Iterator it = nodedSubstring.iterator(); it.hasNext();) {
            SegmentString segment = (SegmentString) it.next();
            //We keep only the segments of the input geometry
            if((Integer)segment.getData()==0){
                Coordinate[] cc = segment.getCoordinates();
                cc = CoordinateArrays.atLeastNCoordinatesOrNothing(2, cc);
                if (cc.length > 1) {
                    linestrings.add(gf.createLineString(cc));
                }
            }
        }
        if (linestrings.isEmpty()) {
            return inputLines;
        } else {
            return gf.createMultiLineString(linestrings.toArray(new LineString[linestrings.size()]));
        }}
        throw new IllegalArgumentException("Split a " + inputLines.getGeometryType() + " by a " + clipper.getGeometryType() + " is not supported.");
    }
    
    /***
     * Convert the input geometries as a list of segments and mark them with a flag
     * to identify input and output geometries.
     * @param inputLines
     * @param clipper
     * @return 
     */
    public static ArrayList<SegmentString> getSegments(Geometry inputLines, Geometry clipper) {
        ArrayList<SegmentString> segments = new ArrayList<SegmentString>();        
        addGeometryToSegments(inputLines, 0, segments);        
        addGeometryToSegments(clipper, 1, segments);
        return segments;
    }

    /**
     * Convert the a geometry as a list of segments and mark it with a flag
     * @param geometry
     * @param flag
     * @param segments 
     */
    public static void addGeometryToSegments(Geometry geometry, int flag, ArrayList<SegmentString> segments) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry component = geometry.getGeometryN(i);
            if (component instanceof Polygon) {
                add((Polygon) component, flag, segments);
            } else if (component instanceof LineString) {
                add((LineString) component, flag, segments);
            }
        }
    }

     /**
     * Convert a polygon as a list of segments and mark it with a flag
     * @param poly
     * @param flag
     * @param segments 
     */
    private static void add(Polygon poly, int flag, ArrayList<SegmentString> segments) {
        add(poly.getExteriorRing(), flag, segments);
        for (int j = 0; j < poly.getNumInteriorRing(); j++) {
            add(poly.getInteriorRingN(j), flag, segments);
        }
    }

    /**
     * Convert a linestring as a list of segments and mark it with a flag
     * @param line
     * @param flag
     * @param segments 
     */
    private static void add(LineString line, int flag, ArrayList<SegmentString> segments) {
        SegmentString ss = new NodedSegmentString(line.getCoordinates(),
                flag);
        segments.add(ss);
    }
}
