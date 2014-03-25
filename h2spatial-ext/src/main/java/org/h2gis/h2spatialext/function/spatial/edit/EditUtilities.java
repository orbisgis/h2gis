/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
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
