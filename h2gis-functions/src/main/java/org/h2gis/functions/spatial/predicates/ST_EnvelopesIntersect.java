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

package org.h2gis.functions.spatial.predicates;

import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Return true if the envelope of Geometry A intersects the envelope of
 * Geometry B.
 *
 * @author Nicolas Fortin
 */
public class ST_EnvelopesIntersect extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_EnvelopesIntersect() {
        addProperty(PROP_REMARKS, "Return true if the envelope of Geometry A intersects the envelope of Geometry B");
    }

    @Override
    public String getJavaStaticMethod() {
        return "intersects";
    }

    /**
     * @param surface Surface Geometry.
     * @param testGeometry Geometry instance
     * @return true if the envelope of Geometry A intersects the envelope of
     * Geometry B
     */
    public static Boolean intersects(Geometry surface,Geometry testGeometry) {
        if(surface==null && testGeometry==null) {
            return null;
        }
        return !(testGeometry == null || surface == null) && surface.getEnvelopeInternal().intersects(testGeometry.getEnvelopeInternal());
    }
}
