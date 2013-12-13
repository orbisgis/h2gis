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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.math.Vector2D;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
 * This function is used to computed the aspect of a triangle Aspect represents
 * the main slope direction angle compared to the north direction.
 *
 * @author Erwan Bocher
 */
public class ST_TriangleAspect extends DeterministicScalarFunction {

    public ST_TriangleAspect() {
        addProperty(PROP_REMARKS, "Compute the aspect of steepest downhill slope for a triangle\n. "
                + "The aspect value is expressed in degrees compared to the north direction.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeAspect"; 
    }
    
    /**
     * Compute the aspect in degree. The geometry must be a triangle.
     * @param geometry
     * @return
     * @throws DelaunayError 
     */
    public static double computeAspect(Geometry geometry) throws DelaunayError{
            DTriangle dTriangle = TINFeatureFactory.createDTriangle(geometry);
            DPoint steepestVector = dTriangle.getSteepestVector();
            if (steepestVector.equals(new DPoint(0, 0, 0))) {
                return 0d;
            } else {
                Vector2D v = new Vector2D(steepestVector.getX(), steepestVector.getY());
                return (Math.toDegrees(v.angle() + (Math.PI / 2)));
            }
    }
}
