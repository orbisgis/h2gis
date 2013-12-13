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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.error.DelaunayError;

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
                + "Output is a COLLECTION of polygons, for flag=0 (default flag) or a MULTILINESTRING for flag=1");
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
     * @throws SQLException, DelaunayError
     */
    public static GeometryCollection createDT(Geometry geometry) throws SQLException, DelaunayError {
        return createDT(geometry, 0);
    }

    /**
     * Build a delaunay triangulation based on all coordinates of the geometry
     *
     * @param geometry
     * @param flag
     * @return Output is a COLLECTION of polygons (for flag=0) or a
     * MULTILINESTRING (for flag=1)
     * @throws SQLException, DelaunayError
     */
    public static GeometryCollection createDT(Geometry geometry, int flag) throws SQLException, DelaunayError {
        if (flag == 0) {
            return DelaunayTools.toMultiPolygon(buildDelaunay(geometry).getTriangleList());
        } else if (flag == 1) {
            return DelaunayTools.toMultiLineString(buildDelaunay(geometry).getEdges());
        } else {
            throw new SQLException("Only flag 0 or 1 is supported.");
        }
    }

    /**
     * Compute a delaunay triangulation
     *
     * @param geometry
     * @return
     * @throws DelaunayError
     */
    private static ConstrainedMesh buildDelaunay(Geometry geometry) throws DelaunayError {
        ConstrainedMesh mesh = new ConstrainedMesh();
        mesh.setVerbose(true);
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.put(geometry);
        mesh.setPoints(delaunayData.getDelaunayPoints());
        mesh.processDelaunay();
        return mesh;
    }
}
