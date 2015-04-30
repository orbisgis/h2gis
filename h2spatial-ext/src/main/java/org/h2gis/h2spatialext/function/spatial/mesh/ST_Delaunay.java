/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatialext.function.spatial.mesh;

import java.sql.SQLException;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
