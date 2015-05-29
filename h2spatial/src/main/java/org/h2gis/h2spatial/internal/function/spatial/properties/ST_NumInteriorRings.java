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
package org.h2gis.h2spatial.internal.function.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Return the number of holes in a geometry
 * @author Nicolas Fortin
 */
public class ST_NumInteriorRings extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_NumInteriorRings() {
        addProperty(PROP_REMARKS, "Return the number of holes in a geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getHoles";
    }

    /**
     * Return the number of holes in a geometry
     * @param g Geometry instance
     * @return Number of hole or null if geometry is null
     */
    public static Integer getHoles(Geometry g) {
        if(g == null) {
            return null;
        }
        int holes = 0;
        if (g instanceof GeometryCollection) {
            int geomCount = g.getNumGeometries();
            for (int i = 0; i < geomCount; i++) {
                holes += getHoles(g.getGeometryN(i));
            }
        } else if (g instanceof Polygon) {
            holes = ((Polygon) g).getNumInteriorRing();
        }
        return holes;
    }
}
