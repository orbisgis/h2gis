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
import org.locationtech.jts.triangulate.polygon.PolygonTriangulator;

/**
 * Tessellate a set of Polygon with adaptive triangles.
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class ST_Tesselate extends DeterministicScalarFunction {

    public ST_Tesselate() {
        addProperty(PROP_REMARKS, "Return the tessellation of a (multi)polygon surface with adaptive triangles\n" +
                "Ex:\n" +
                "```sql\n" +
                "SELECT ST_TESSELATE('POLYGON ((-6 -2, -8 2, 0 8, -8 -7, -10 -1, -6 -2))') the_geom" +
                "```");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    public static Geometry execute(Geometry geometry) throws IllegalArgumentException {
        if(geometry == null) {
            return null;
        }
        if(geometry.isEmpty()){
            return geometry.getFactory().createMultiPolygon();
        }
        return PolygonTriangulator.triangulate(geometry);
    }
}
