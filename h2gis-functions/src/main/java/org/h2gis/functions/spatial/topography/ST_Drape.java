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
package org.h2gis.functions.spatial.topography;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.TriMarkers;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.linemerge.LineMerger;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * This function could be used to drape a geometry to a set of triangles
 * @author Erwan Bocher
 */
public class ST_Drape extends DeterministicScalarFunction{

    
    public ST_Drape(){
        addProperty(PROP_REMARKS, "This function drapes an input geometry to a set of triangles.\n"
                + "Notes : The supported input geometry types are POINT, MULTIPOINT, LINESTRING, MULTILINESTRING, POLYGON and MULTIPOLYGON \n"
                + "In case of 1 or 2 dimension, the input geometry is intersected with the triangles to perform a full draping.\n"
                + "If a point lies on two triangles the z value of the first triangle is kept.\n"
                + "A zero value is set to the z ordinate when the point is outside a triangle.\n" 
                + "Input triangles must be passed using a POLYGON Z form.");
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
        if(geomToDrape.getSRID()!=triangles.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        
        //Check if triangles are triangles and create a quadtree to perform spatial queries
        int nb = triangles.getNumGeometries();
        STRtree sTRtree = new STRtree();
        for (int i = 0; i < nb; i++) {
            Geometry geom = triangles.getGeometryN(i);
            sTRtree.insert(geom.getEnvelopeInternal(), TINFeatureFactory.createTriangle(geom));
        }
      
        if (geomToDrape instanceof Point) {
            return drapePoint(geomToDrape, triangles, sTRtree);
        } else if (geomToDrape instanceof MultiPoint) {
            return drapePoints(geomToDrape, triangles,sTRtree);
        } else if (geomToDrape instanceof MultiLineString) {
            return drapeMultiLineString((MultiLineString) geomToDrape, triangles, sTRtree);
        } else if (geomToDrape instanceof MultiPolygon) {
            return drapeMultiPolygon((MultiPolygon) geomToDrape, triangles, sTRtree);
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
     * Drape a multipoint geometry to a set of triangles
     * @param pts
     * @param triangles
     * @param sTRtree
     * @return
     */
    public static Geometry drapePoints(Geometry pts, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = pts.getFactory();
        int nbPts = pts.getNumGeometries();
        Point[] points = new Point[nbPts];
        for (int i = 0; i < nbPts; i++) {
            points[i] = factory.createPoint(updateCoordinates(((Point)pts.getGeometryN(i)).getCoordinateSequence(), sTRtree));
        }
        return factory.createMultiPoint(points);
    }

    /**
     * Drape a point geometry to a set of triangles
     * @param pts
     * @param triangles
     * @param sTRtree
     * @return
     */
    public static Geometry drapePoint(Geometry pts, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = pts.getFactory();
        return factory.createPoint(updateCoordinates(((Point)pts).getCoordinateSequence(), sTRtree));
    }
    
    
    /**
     * Drape a multilinestring to a set of triangles
     * @param polygons
     * @param triangles
     * @param sTRtree
     * @return 
     */
    public static Geometry drapeMultiPolygon(MultiPolygon polygons, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = polygons.getFactory();         
        //Split the triangles in lines to perform all intersections
        Geometry triangleLines = LinearComponentExtracter.getGeometry(triangles, true);
        int nbPolygons = polygons.getNumGeometries();
        Polygon[] polygonsDiff = new Polygon[nbPolygons];
        for (int i = 0; i < nbPolygons; i++) {
            polygonsDiff[i] = processPolygon((Polygon) polygons.getGeometryN(i), triangleLines, factory,sTRtree);
        }
        return factory.createMultiPolygon(polygonsDiff);
    }
    
    /**
     * Drape a multilinestring to a set of triangles
     * @param lines
     * @param triangles
     * @param sTRtree
     * @return 
     */
    public static Geometry drapeMultiLineString(MultiLineString lines, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = lines.getFactory();         
        //Split the triangles in lines to perform all intersections
        Geometry triangleLines = LinearComponentExtracter.getGeometry(triangles, true);
        int nbLines = lines.getNumGeometries();
        LineString[] lineStrings = new LineString[nbLines];
        for (int i = 0; i < nbLines; i++) {
            LineString lineDiff = (LineString) lineMerge(lines.getGeometryN(i).difference(triangleLines), factory);
            lineStrings[i] = factory.createLineString(updateCoordinates(lineDiff.getCoordinateSequence(), sTRtree));
        }
        return factory.createMultiLineString(lineStrings);
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
        LineString diffExt = (LineString) lineMerge(line.difference(triangleLines), factory);
        return factory.createLineString(updateCoordinates(diffExt.getCoordinateSequence(), sTRtree));
    }


    /**
     * Drape a polygon on a set of triangles
     * @param p
     * @param triangles
     * @param sTRtree
     * @return 
     */
    public static Polygon drapePolygon(Polygon p, Geometry triangles, STRtree sTRtree) {
        GeometryFactory factory = p.getFactory();
        //Split the triangles in lines to perform all intersections
        Geometry triangleLines = LinearComponentExtracter.getGeometry(triangles, true);        
        Polygon splittedP = processPolygon(p, triangleLines, factory,sTRtree);
        return splittedP;
    }
    
    /**
     * Cut the lines of the polygon with the triangles
     * @param p
     * @param triangleLines
     * @param factory
     * @return 
     */
    private static Polygon processPolygon(Polygon p, Geometry triangleLines, GeometryFactory factory,STRtree sTRtree) {
        Geometry diffExt = p.getExteriorRing().difference(triangleLines);
        final int nbOfHoles = p.getNumInteriorRing();
        final LinearRing[] holes = new LinearRing[nbOfHoles];
        for (int i = 0; i < nbOfHoles; i++) {
            LinearRing hole = factory.createLinearRing(lineMerge(p.getInteriorRingN(i).difference(triangleLines), factory).getCoordinates());
            holes[i] = factory.createLinearRing(updateCoordinates(hole.getCoordinateSequence(),sTRtree));
        }
        LinearRing shell = factory.createLinearRing(lineMerge(diffExt, factory).getCoordinates());
        return factory.createPolygon(factory.createLinearRing(updateCoordinates(shell.getCoordinateSequence(),sTRtree)), holes);

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
     * Update the coordinates and compute the z values
     * @param cs
     * @param indexedTriangles
     * @return
     */
    private static CoordinateArraySequence updateCoordinates(CoordinateSequence cs, STRtree indexedTriangles) {
        int updateDim = cs.getDimension();
        if(cs.getDimension()==2){
            updateDim =3;
        }
        Coordinate[] coords = cs.toCoordinateArray();
        for (int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
            coord = new Coordinate(coord);
            List<Triangle> result = indexedTriangles.query(new Envelope(coord));
            if (!result.isEmpty()) {
                double z = 0;
                for (Triangle triangle : result) {
                    if (TriMarkers.intersects(coord, triangle)) {
                        z = triangle.interpolateZ(coord);
                        break;
                    }
                }
                coord.z = z;
                coords[i]=coord;
            }
            else{
                coord.z = 0;
                coords[i]=coord;
            }
        }
        return new CoordinateArraySequence(coords, updateDim);
    }
}
