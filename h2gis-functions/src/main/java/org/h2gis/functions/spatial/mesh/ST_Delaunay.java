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

package org.h2gis.functions.spatial.mesh;

import java.sql.SQLException;
import java.util.List;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Returns polygons that represent a Delaunay triangulation constructed from a
 * collection of points. Note that the triangulation doesn't compute the
 * intersections between lines; it takes only existing coordinates.
 *
 *
 * @author Erwan Bocher
 */
public class ST_Delaunay extends DeterministicScalarFunction {

    public ST_Delaunay() {
        addProperty(PROP_REMARKS, "Returns polygons that represent a Delaunay Triangulation from a geometry.\n"
                + "Output is a COLLECTION of polygons, for flag=0 (default flag) or a MULTILINESTRING for flag=1\n"
         );
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
    public static GeometryCollection createDT(Geometry geometry) throws SQLException {
        return createDT(geometry, 0);
    }
    
    /**
     * Build a delaunay triangulation based on all coordinates of the geometry
     *
     * @param geometry
     * @param flag  for flag=0 (default flag) or a MULTILINESTRING for flag=1
     * @return a set of polygons (triangles)
     * @throws SQLException
     */
    public static GeometryCollection createDT(Geometry geometry, int flag) throws SQLException {
        if (geometry != null) {
            DelaunayTriangulationBuilder triangulationBuilder = new DelaunayTriangulationBuilder();
            triangulationBuilder.setSites(geometry);
            if(flag == 0) {
                return getTriangles(geometry.getFactory(), triangulationBuilder);
            } else {
                return (GeometryCollection)triangulationBuilder.getEdges(geometry.getFactory());
            }
        }
        return null;
    }

    private static GeometryCollection getTriangles(GeometryFactory geomFact,
                                                   DelaunayTriangulationBuilder delaunayTriangulationBuilder) {
        QuadEdgeSubdivision subdiv = delaunayTriangulationBuilder.getSubdivision();
        List triPtsList = subdiv.getTriangleCoordinates(false);
        Polygon[] tris = new Polygon[triPtsList.size()];
        int i = 0;
        for (Object aTriPtsList : triPtsList) {
            Coordinate[] triPt = (Coordinate[]) aTriPtsList;
            tris[i++] = geomFact.createPolygon(geomFact.createLinearRing(triPt), null);
        }
        return geomFact.createMultiPolygon(tris);
    }
}
