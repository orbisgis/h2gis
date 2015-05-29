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
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Compute the minimum bounding circle of a geometry. For more information,
 * see {@link MinimumBoundingCircle}.
 *
 * @author Erwan Bocher
 */
public class ST_BoundingCircle extends DeterministicScalarFunction {

    public ST_BoundingCircle() {
        addProperty(PROP_REMARKS, "Compute the minimum bounding circle of a geometry");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeBoundingCircle";
    }

    /**
     * Computes the bounding circle
     *
     * @param geometry
     * @return
     */
    public static Geometry computeBoundingCircle(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        return new MinimumBoundingCircle(geometry).getCircle();
    }
}
