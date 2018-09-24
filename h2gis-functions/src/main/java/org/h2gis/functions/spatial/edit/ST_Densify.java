/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Densifies a geometry using the given distance tolerance.
 *
 * @see org.locationtech.jts.densify.Densifier
 * @author Erwan Bocher
 */
public class ST_Densify extends DeterministicScalarFunction {

    public ST_Densify() {
        addProperty(PROP_REMARKS, "Densifies a geometry using the given distance tolerance");
    }

    @Override
    public String getJavaStaticMethod() {
        return "densify";
    }

    /**
     * Densify a geometry using the given distance tolerance.
     *
     * @param geometry  Geometry
     * @param tolerance Distance tolerance
     * @return Densified geometry
     */
    public static Geometry densify(Geometry geometry, double tolerance) {
        if(geometry == null){
            return null;
        }
        return Densifier.densify(geometry, tolerance);
    }
}
