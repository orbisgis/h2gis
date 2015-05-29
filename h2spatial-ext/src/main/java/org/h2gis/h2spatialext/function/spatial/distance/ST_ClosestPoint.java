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
package org.h2gis.h2spatialext.function.spatial.distance;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * ST_ClosestPoint returns the 2D point on geometry A that is closest to
 * geometry B.  If the closest point is not unique, it returns the first one it
 * finds. This means that the point returned depends on the order of the
 * geometry's coordinates.
 *
 * @author Adam Gouge
 */
public class ST_ClosestPoint extends DeterministicScalarFunction {

    public ST_ClosestPoint() {
        addProperty(PROP_REMARKS, "Returns the 2D point on geometry A that is " +
                "closest to geometry B.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "closestPoint";
    }

    /**
     * Returns the 2D point on geometry A that is closest to geometry B.
     *
     * @param geomA Geometry A
     * @param geomB Geometry B
     * @return The 2D point on geometry A that is closest to geometry B
     */
    public static Point closestPoint(Geometry geomA, Geometry geomB) {
        if (geomA == null || geomB == null) {
            return null;
        }
        // Return the closest point on geomA. (We would have used index
        // 1 to return the closest point on geomB.)
        return geomA.getFactory().createPoint(DistanceOp.nearestPoints(geomA, geomB)[0]);
    }
}
