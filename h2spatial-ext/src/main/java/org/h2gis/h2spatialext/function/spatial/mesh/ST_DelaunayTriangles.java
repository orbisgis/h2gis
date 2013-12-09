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
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
 * Returns polygons that represent a Delaunay Triangulation from a collections
 * of points.
 * Note that the triangulation doesn't compute the intersections between lines 
 * it takes only existing coordinates.
 *
 * @author Erwan Bocher
 */
public class ST_DelaunayTriangles extends DeterministicScalarFunction {

    public static GeometryFactory gf = new GeometryFactory();

    public ST_DelaunayTriangles() {
        addProperty(PROP_REMARKS, "Returns polygons that represent a Delaunay Triangulation from a geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createDT";
    }

    /**
     * Build a delaunay triangulation based on all coordinates of the geometry
     *
     * @param geometry
     * @return a set of polygons (triangles)
     * @throws SQLException
     */
    public static Geometry createDT(Geometry geometry) throws SQLException, DelaunayError {
        ConstrainedMesh mesh = new ConstrainedMesh();
        mesh.setVerbose(true);
        List<DPoint> pointsToAdd = new ArrayList<DPoint>();
        for (Coordinate coordinate : geometry.getCoordinates()) {
            pointsToAdd.add(new DPoint(coordinate));
        }
        mesh.setPoints(pointsToAdd);
        mesh.processDelaunay();
        List<DTriangle> triangles = mesh.getTriangleList();
        Polygon[] polygons = new Polygon[triangles.size()];
        for (int i = 0; i < triangles.size(); i++) {
            DTriangle dt = triangles.get(i);
            Coordinate[] coords = new Coordinate[DTriangle.PT_NB + 1];
            coords[0] = dt.getPoint(0).getCoordinate();
            coords[1] = dt.getPoint(1).getCoordinate();
            coords[2] = dt.getPoint(2).getCoordinate();
            coords[3] = dt.getPoint(0).getCoordinate();
            CoordinateSequence cs = new CoordinateArraySequence(coords);
            LinearRing lr = new LinearRing(cs, gf);
            Polygon poly = new Polygon(lr, null, gf);
            polygons[i] = poly;
        }
        return gf.createMultiPolygon(polygons);
    }
}
