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

package org.h2gis.functions.spatial.distance;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;

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
    public static Point closestPoint(Geometry geomA, Geometry geomB) throws SQLException {
        if (geomA == null || geomB == null) {
            return null;
        }
        if(geomA.isEmpty()||geomB.isEmpty()){
            return null;
        }
        if(geomA.getSRID()!=geomB.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        // Return the closest point on geomA. (We would have used index
        // 1 to return the closest point on geomB.)
        return geomA.getFactory().createPoint(DistanceOp.nearestPoints(geomA, geomB)[0]);
    }
}
