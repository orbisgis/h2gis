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
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.math.Vector3D;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.TriMarkers;

/**
 * This function is used to compute the aspect of a triangle. Aspect represents
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
     * @param geometry Polygon triangle
     * @return aspect in degree
     * @throws IllegalArgumentException  ST_TriangleAspect accept only triangles
     */
    public static Double computeAspect(Geometry geometry) throws IllegalArgumentException {
        if (geometry == null) {
            return null;
        }
        Vector3D vector = TriMarkers.getSteepestVector(TriMarkers.getNormalVector(TINFeatureFactory.createTriangle(geometry)), TINFeatureFactory.EPSILON);
        if (vector.length() < TINFeatureFactory.EPSILON) {
            return 0d;
        } else {
            Vector2D v = new Vector2D(vector.getX(), vector.getY());
            return measureFromNorth(Math.toDegrees(v.angle()));
        }
    }

    /**
     * Transforms an angle measured in degrees counterclockwise from the x-axis
     * (mathematicians) to an angle measured in degrees clockwise from the
     * y-axis (geographers).
     *
     * @param angle Mathematician's angle
     * @return Geographer's angle
     */
    public static double measureFromNorth(double angle) {
        return (450 - angle) % 360;
    }
}
