/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
 * This function is used to computed the main slope direction on a triangle
 * 
* @author Erwan Bocher
 */
public class ST_TriangleDirection extends DeterministicScalarFunction {

    private static GeometryFactory gf = new GeometryFactory();

    public ST_TriangleDirection() {
        addProperty(PROP_REMARKS, "Compute the steepest vector director for a triangle\n"
                + "and represent it as a linestring");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeDirection";
    }

    /**
     * Compute the main slope direction
     * @param geometry
     * @return
     * @throws DelaunayError 
     */
    public static LineString computeDirection(Geometry geometry) throws DelaunayError {
        DTriangle dTriangle = TINFeatureFactory.createDTriangle(geometry);
        DPoint pointIntersection = dTriangle.getSteepestIntersectionPoint(dTriangle.getBarycenter());
        if (pointIntersection != null) {
            return gf.createLineString(new Coordinate[]{dTriangle.getBarycenter().getCoordinate(), dTriangle.getSteepestIntersectionPoint(dTriangle.getBarycenter()).getCoordinate()});
        }        
        return gf.createLineString(new Coordinate[] {});
    }
    
}
