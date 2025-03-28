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

package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * Forces a Geometry into 2D mode by returning a copy with 
 * its z-coordinate set to NaN.
 *
 * @author Erwan Bocher
 */
public class ST_Force2D extends DeterministicScalarFunction {
    
    public ST_Force2D() {
        addProperty(PROP_REMARKS, "Forces the geometries into a \"2-dimensional mode\" \n"
                + " so that all output representations will only have the X and Y coordinates.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "force2D";
    }

    /**
     * Converts a XYZ geometry to XY.
     *
     * @param geom {@link Geometry}
     * @return Geometry in 2D
     */
    public static Geometry force2D(Geometry geom) {
        if (geom == null) {
            return null;
        }        
        return GeometryCoordinateDimension.force(geom, 2);        
    }
}
