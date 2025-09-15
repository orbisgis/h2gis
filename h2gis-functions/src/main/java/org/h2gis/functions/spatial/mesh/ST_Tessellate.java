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
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

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
        return delaunayData.getTrianglesAsMultiPolygon();
    }

    public static MultiPolygon tessellate(Geometry geometry) throws IllegalArgumentException {
        if(geometry == null) {
            return null;
        }
        if(geometry.isEmpty()){
            return geometry.getFactory().createMultiPolygon();
        }
        if(geometry instanceof Polygon) {
            return tessellatePolygon((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            int size =geometry.getNumGeometries();
            ArrayList<Polygon> polygons = new ArrayList<Polygon>(size * 2);
            for(int idPoly = 0; idPoly < size; idPoly++) {
                Polygon pol = (Polygon) geometry.getGeometryN(idPoly);
                if(!pol.isEmpty()) {
                    MultiPolygon triangles = tessellatePolygon(pol);
                    int sub_size = triangles.getNumGeometries();
                    polygons.ensureCapacity(sub_size);
                    for (int idTri = 0; idTri < sub_size; idTri++) {
                        polygons.add((Polygon) triangles.getGeometryN(idTri));
                    }
                }
            }
            return geometry.getFactory().createMultiPolygon(polygons.toArray(new Polygon[0]));
        } else {
            throw new IllegalArgumentException("ST_Tessellate accept only Polygon and MultiPolygon types not instance" +
                    " of "+geometry.getClass().getSimpleName());
        }
    }
}
