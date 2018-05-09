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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.h2gis.api.DeterministicScalarFunction;
import java.util.ArrayList;

/**
 * Tessellate a set of Polygon with adaptive triangles.
 * @author Nicolas Fortin
 */
public class ST_Tessellate extends DeterministicScalarFunction {

    public ST_Tessellate() {
        addProperty(PROP_REMARKS, "Return the tessellation of a (multi)polygon surface with adaptive triangles\n" +
                "Ex:\n" +
                "```sql\n" +
                "SELECT ST_TESSELLATE('POLYGON ((-6 -2, -8 2, 0 8, -8 -7, -10 -1, -6 -2))') the_geom" +
                "```");
    }

    @Override
    public String getJavaStaticMethod() {
        return "tessellate";
    }

    private static MultiPolygon tessellatePolygon(Polygon polygon) {
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.put(polygon, DelaunayData.MODE.TESSELLATION);
        // Do triangulation
        delaunayData.triangulate();
        return delaunayData.getTriangles();
    }

    public static MultiPolygon tessellate(Geometry geometry) throws IllegalArgumentException {
        if(geometry == null) {
            return null;
        }
        if(geometry instanceof Polygon) {
            return tessellatePolygon((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            ArrayList<Polygon> polygons = new ArrayList<Polygon>(geometry.getNumGeometries() * 2);
            for(int idPoly = 0; idPoly < geometry.getNumGeometries(); idPoly++) {
                MultiPolygon triangles = tessellatePolygon((Polygon)geometry.getGeometryN(idPoly));
                polygons.ensureCapacity(triangles.getNumGeometries());
                for(int idTri=0; idTri < triangles.getNumGeometries(); idTri++) {
                    polygons.add((Polygon)triangles.getGeometryN(idTri));
                }
            }
            return geometry.getFactory().createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        } else {
            throw new IllegalArgumentException("ST_Tessellate accept only Polygon and MultiPolygon types not instance" +
                    " of "+geometry.getClass().getSimpleName());
        }
    }
}
