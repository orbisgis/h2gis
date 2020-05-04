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

package org.h2gis.functions.spatial.properties;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryMetaData;

import java.io.IOException;

/**
 * Retrieve the SRID from an EWKB encoded geometry.
 * @author Nicolas Fortin
 */
public class ST_SRID extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_SRID() {
        addProperty(PROP_REMARKS, "Retrieve the SRID from an EWKB encoded geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getSRID";
    }

    /**
     * @param geometry Geometry instance or null
     * @return SRID value or 0 if input geometry does not have one.
     * @throws IOException
     */
    public static Integer getSRID(byte[] geometry) throws IOException {
        if(geometry==null) {
            return 0;
        }
        return GeometryMetaData.getMetaData(geometry).SRID;
    }
}
