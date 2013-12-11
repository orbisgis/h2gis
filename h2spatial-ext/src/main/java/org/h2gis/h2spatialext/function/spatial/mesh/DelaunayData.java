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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DEdge;
import org.jdelaunay.delaunay.geometries.DPoint;

/**
 * This class is used to collect all data used to computed a mesh based on a
 * delaunay triangulation
 *
 * @author Erwan Bocher
 */
public class DelaunayData {

    List<DPoint> delaunayPoints = null;
    ArrayList<DEdge> delaunayEdges = null;

    /**
     * Create a meshData structure to collect points and edges that will be used
     * by the DelaunayTriangulation
     */
    public DelaunayData() {
        this.delaunayPoints = new ArrayList<DPoint>();
        this.delaunayEdges = new ArrayList<DEdge>();
    }

    /**
     * Put a geometry into the data array. Set true to populate the list of
     * points and edges, needed for the ContrainedDelaunayTriangulation Set
     * false to populate only the list of points Note the z value is forced to O
     * when it's equal to NaN
     *
     * @param geom
     * @param isConstrained
     * @throws DelaunayError
     */
    public void put(Geometry geom, boolean isConstrained) throws DelaunayError {
        if (isConstrained) {
            if (geom instanceof Point) {
                addPoint((Point) geom);
            } else if (geom instanceof MultiPoint) {
                addMultiPoint((MultiPoint) geom);
            } else if (geom instanceof GeometryCollection) {
                addGeometryCollection((GeometryCollection) geom);
            } else {
                addGeometry(geom);
            }
        } else {
            addCoordinates(geom);
        }
    }

    /**
     * Put a geometry into the data array of points. If you want to build a
     * constrained delaunay triangulation used the method
     * {@code put(Geometry geom, boolean isConstrained)} Note the z value is
     * forced to O when it's equal to NaN
     *
     * @param geom
     * @throws DelaunayError
     */
    public void put(Geometry geom) throws DelaunayError {
        put(geom, false);
    }

    /**
     * We add a point to the list of points used by the triangulation
     *
     * @param geom
     * @throws DelaunayError
     */
    private void addPoint(Point geom) throws DelaunayError {
        Coordinate pt = geom.getCoordinate();
        double z = Double.isNaN(pt.z) ? 0 : pt.z;
        delaunayPoints.add(new DPoint(pt.x, pt.y, z));
    }

    /**
     * Add all points of a multiPoint to the list of points used by the
     * triangulation.
     *
     * @param pts
     * @throws DelaunayError
     */
    private void addMultiPoint(MultiPoint multiPoint) throws DelaunayError {
        for (Coordinate coordinate : multiPoint.getCoordinates()) {
            delaunayPoints.add(new DPoint(
                    coordinate.x,
                    coordinate.y,
                    Double.isNaN(coordinate.z) ? 0 : coordinate.z));
        }
    }

    /**
     * Add a geometry to the list of points and edges used by the triangulation.
     * This method is used for Polygon and Lines
     *
     * @param geom
     * @throws DelaunayError
     */
    private void addGeometry(Geometry geom) throws DelaunayError {
        if (geom.isValid()) {
            Coordinate[] coords = geom.getCoordinates();
            Coordinate c1 = coords[0];
            c1.z = Double.isNaN(c1.z) ? 0 : c1.z;
            Coordinate c2;
            for (int k = 1; k < coords.length; k++) {
                c2 = coords[k];
                c2.z = Double.isNaN(c2.z) ? 0 : c2.z;
                delaunayEdges.add(new DEdge(new DPoint(c1), new DPoint(c2)));
                c1 = c2;
            }
        }
    }

    /**
     * Add a GeometryCollection to the list of points and edges used by the
     * triangulation.
     *
     * @param geomcol
     * @throws DelaunayError
     */
    private void addGeometryCollection(GeometryCollection geomcol) throws DelaunayError {
        int num = geomcol.getNumGeometries();
        for (int i = 0; i < num; i++) {
            addGeometry(geomcol.getGeometryN(i));
        }
    }

    /**
     * Add all coordinates of the geometry to the list of points
     *
     * @param geom
     * @throws DelaunayError
     */
    private void addCoordinates(Geometry geom) throws DelaunayError {
        for (Coordinate coordinate : geom.getCoordinates()) {
            delaunayPoints.add(new DPoint(coordinate));
        }
    }

    /**
     * Gives the collection of edges
     * @return 
     */
    public ArrayList<DEdge> getDelaunayEdges() {
        return delaunayEdges;
    }

    /**
     * Gives the collection of points
     * @return 
     */
    public List<DPoint> getDelaunayPoints() {
        return delaunayPoints;
    }
}
