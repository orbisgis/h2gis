/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.mesh;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.util.List;
import org.jdelaunay.delaunay.geometries.DEdge;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
 * Tools to convert JDelaunay objects to JTS Geometry
 *
 * @author Erwan Bocher
 */
public class DelaunayTools {

    public static GeometryFactory gf = new GeometryFactory();

    /**
     * Convert a list of triangles to a MultiPolygon
     * @param triangles
     * @return 
     */
    public static MultiPolygon toMultiPolygon(List<DTriangle> triangles) {
        Polygon[] polygons = new Polygon[triangles.size()];
        for (int i = 0; i < triangles.size(); i++) {
            polygons[i] = toPolygon(triangles.get(i));
        }
        return gf.createMultiPolygon(polygons);
    }
    
    /**
     * Convert a JDelaunay triangle to a JTS polygon
     * @param triangle
     * @return 
     */
    public static Polygon toPolygon(DTriangle triangle) {
        Coordinate[] coords = new Coordinate[DTriangle.PT_NB + 1];
        coords[0] = triangle.getPoint(0).getCoordinate();
        coords[1] = triangle.getPoint(1).getCoordinate();
        coords[2] = triangle.getPoint(2).getCoordinate();
        coords[3] = triangle.getPoint(0).getCoordinate();        
        LinearRing lr = gf.createLinearRing(coords);
        return gf.createPolygon(lr, null);
    }
    
    /**
     * Convert a list of triangles to a MultiLineString
     *
     * @param triangles
     * @return
     */
    public static MultiLineString toMultiLineString(List<DEdge> edges) {
        LineString[] lines = new LineString[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            lines[i] = toLineString(edges.get(i));
        }
        return gf.createMultiLineString(lines);
    }
    
    /**
     * Convert a JDelaunay edge to a JTS linestring
     * @param dEdge
     * @return 
     */
    public static LineString toLineString(DEdge dEdge) {
        Coordinate[] coords = new Coordinate[2];
        coords[0] = dEdge.getPointLeft().getCoordinate();
        coords[1] = dEdge.getPointRight().getCoordinate();
        return gf.createLineString(coords);
    }
}
