/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.mesh;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import java.sql.SQLException;

/**
 * Returns polygons or lines that represent a Delaunay triangulation constructed
 * from a geometry. Note that the triangulation computes
 * the intersections between lines.
 *
 * @author Erwan Bocher
 */
public class ST_ConstrainedDelaunay extends DeterministicScalarFunction {

 
    public ST_ConstrainedDelaunay() {
        addProperty(PROP_REMARKS, "Returns polygons that represent a Constrained Delaunay Triangulation from a geometry.\n"
                + "Output is a COLLECTION of polygons, for flag=0 (default flag) or a MULTILINESTRING for flag=1.\n"
                + "If the input geometry does not contain any lines or polygons, a delaunay triangulation will be computed." +
                " The minPointSpacing is the merge distance between provided points, by default this value is 1e-12\n");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createCDT";
    }

    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry {@link Geometry}
     * @return a set of polygons (triangles)
     */
    public static GeometryCollection createCDT(Geometry geometry) throws SQLException {
        return createCDT(geometry, 0);
    }
    
    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry Geometry
     * @param flag 0 = polygon, 1 = lines
     * @return a set of geometries (flag 0 = polygon, flag 1 = lines)
     */
    public static GeometryCollection createCDT(Geometry geometry, int flag) throws SQLException {
        return createCDT(geometry, flag, DelaunayData.DEFAULT_EPSILON);
    }


    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry Geometry
     * @param flag 0 = polygon, 1 = lines
     * @param minPointSpacing Will merge points if the distance is inferior between this parameter
     * @return a set of geometries (flag 0 = polygon, flag 1 = lines)
     */
    public static GeometryCollection createCDT(Geometry geometry, int flag, double minPointSpacing) throws SQLException {
        if (geometry != null) {
            DelaunayData delaunayData = new DelaunayData();
            delaunayData.setEpsilon(Math.max(0, minPointSpacing));
            delaunayData.put(OverlayNGRobust.union(geometry), DelaunayData.MODE.CONSTRAINED);
            delaunayData.triangulate();
            if (flag == 0) {
                return delaunayData.getTrianglesAsMultiPolygon();
            } else if (flag == 1) {
                return delaunayData.getTrianglesSides();
            } else {
                throw new SQLException("Only flag 0 or 1 is supported.");
            }
        }
        return null;
    }
}
