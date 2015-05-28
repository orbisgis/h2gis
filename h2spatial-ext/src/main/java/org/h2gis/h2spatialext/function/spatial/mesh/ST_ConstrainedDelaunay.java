/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.mesh;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
                + "If the input geometry does not contain any lines, a delaunay triangulation will be computed.\n");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createCDT";
    }

    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry
     * @return a set of polygons (triangles)
     * @throws SQLException
     */
    public static GeometryCollection createCDT(Geometry geometry) throws SQLException {
        return createCDT(geometry, 0);
    }
    
    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry
     * @param flag
     * @return a set of polygons (triangles)
     * @throws SQLException
     */
    public static GeometryCollection createCDT(Geometry geometry, int flag) throws SQLException {
        if (geometry != null) {
            DelaunayData delaunayData = new DelaunayData();
            delaunayData.put(geometry, DelaunayData.MODE.CONSTRAINED);
            delaunayData.triangulate();
            if (flag == 0) {
                return delaunayData.getTriangles();
            } else if (flag == 1) {
                return delaunayData.getTrianglesSides();
            } else {
                throw new SQLException("Only flag 0 or 1 is supported.");
            }
        }
        return null;
    }
}
