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
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.distance.GeometryLocation;

/**
 * Common utilities used by the edit functions
 * 
 * @author Erwan Bocher
 */
public class EditUtilities {
    
    /**
     * Gets the coordinate of a Geometry that is the nearest of a given Point,
     * with a distance tolerance.
     *
     * @param g
     * @param p
     * @param tolerance
     * @return
     */
    public static GeometryLocation getVertexToSnap(Geometry g, Point p, double tolerance) {
        DistanceOp distanceOp = new DistanceOp(g, p);
        GeometryLocation snapedPoint = distanceOp.nearestLocations()[0];
        if (tolerance == 0 || snapedPoint.getCoordinate().distance(p.getCoordinate()) <= tolerance) {
            return snapedPoint;
        }
        return null;

    }
}
