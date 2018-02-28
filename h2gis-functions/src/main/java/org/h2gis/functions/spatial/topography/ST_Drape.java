/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.functions.spatial.topography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.TriMarkers;

/**
 * This function could be used to drape a geometry to a set of triangles
 * @author Erwan Bocher
 */
public class ST_Drape extends DeterministicScalarFunction{

    
    public ST_Drape(){
        addProperty(PROP_REMARKS, "This function drapes an input geometry to a set of triangles.\n"
                + "Notes : The supported input geometry types are POINT, MULTIPOINT, LINESTRING and POLYGON \n"
                + "In case of linestring or polygon, the input geometry is intersected with the triangles to perform a full draping.\n"
                + "If a point lies on two triangles the z value of the first triangle is kept.");
    }
    @Override
    public String getJavaStaticMethod() {
        return "drape";
    }
    
    public static Geometry drape(Geometry geomToDrape, Geometry triangles) throws SQLException {
       if (geomToDrape == null) {
            return null;
        }
        if (triangles == null) {
            return geomToDrape;
        }
        
        //Check if triangles are triangles and create a quadtree to perform spatial queries
        int nb = triangles.getNumGeometries();
        STRtree sTRtree = new STRtree();
        for (int i = 0; i < nb; i++) {
            Geometry geom = triangles.getGeometryN(i);
            sTRtree.insert(geom.getEnvelopeInternal(), TINFeatureFactory.createTriangle(geom));
        }        
        
        if(geomToDrape.getDimension()==0){            
            return  drapePoints(geomToDrape, triangles, sTRtree);
        }
        else if (geomToDrape instanceof Polygon) {
            return drapePolygon((Polygon) geomToDrape, triangles, sTRtree);
        } else if (geomToDrape instanceof LineString) {
            return drapeLineString((LineString) geomToDrape, triangles, sTRtree);
        } else {
            throw new SQLException("Drape " + geomToDrape.getGeometryType() + " is not supported.");
        } 
    }
    
    /**
     * Drape a point or a multipoint geometry to a set of triangles
     * @param pts
     * @param triangles
     * @param sTRtree
     * @return 
     */
    public static Geometry drapePoints(Geometry pts, Geometry triangles, STRtree sTRtree) {
        Geometry geomDrapped = (Geometry) pts.clone();
        CoordinateSequenceFilter drapeFilter = new DrapeFilter(sTRtree);
        geomDrapped.apply(drapeFilter);
        return geomDrapped;
    }
    
    /**
     * Drape a linestring to a set of triangles
     * @param line
     * @param triangles
     * @param sTRtree
     * @return 
     */
    public static Geometry drapeLineString(LineString line, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = line.getFactory();
        //Split the triangles in lines to perform all intersections
        Geometry triangleLines = LinearComponentExtracter.getGeometry(triangles, true);
        Geometry diffExt = lineMerge(line.difference(triangleLines), factory);
        CoordinateSequenceFilter drapeFilter = new DrapeFilter(sTRtree);
        diffExt.apply(drapeFilter);
        return diffExt;
    }
    
    /**
     * Drape a polygon on a set of triangles
     * @param p
     * @param triangles
     * @param sTRtree
     * @return 
     */
    public static Geometry drapePolygon(Polygon p, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = p.getFactory();
        //Split the triangles in lines to perform all intersections
        Geometry triangleLines = LinearComponentExtracter.getGeometry(triangles, true);
        Geometry diffExt = p.getExteriorRing().difference(triangleLines);
        final int nbOfHoles = p.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            holes[i] = factory.createLinearRing(lineMerge(p.getInteriorRingN(i).difference(triangleLines), factory).getCoordinates());
        }
        Polygon splittedP = factory.createPolygon(factory.createLinearRing(lineMerge(diffExt, factory).getCoordinates()), holes);
        CoordinateSequenceFilter drapeFilter = new DrapeFilter(sTRtree);
        splittedP.apply(drapeFilter);
        return splittedP;
    }
    
    /**
     * A method to merge a geometry to a set of linestring
     * @param geom
     * @param factory
     * @return 
     */
    public static Geometry lineMerge(Geometry geom, GeometryFactory factory) {
        LineMerger merger = new LineMerger();
        merger.add(geom);
        Collection lines = merger.getMergedLineStrings();
        return factory.buildGeometry(lines);
    }

    /**
     * A filter to compute the z value of a coordinate according its location
     * on a triangle
     */
    private static class DrapeFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private final STRtree q;
       
        public DrapeFilter(STRtree q) {
           this.q=q;
        }

        @Override
        public void filter(CoordinateSequence seq, int i) {            
            Coordinate coord = seq.getCoordinate(i);
            List<Triangle> result = q.query(new Envelope(coord));
            if (!result.isEmpty()) {
                double z = Double.NaN;
                for (Triangle triangle : result) {
                    if (TriMarkers.intersects(coord, triangle)) {
                        z = triangle.interpolateZ(coord);
                        break;
                    }
                }
                seq.setOrdinate(i, 2, z );

            }
            if (i == seq.size()) {
                done = true;
            }
        }

        @Override
        public boolean isDone() {
                return done;
        }

        @Override
        public boolean isGeometryChanged() {
            return true;
        }
    }
    
}
