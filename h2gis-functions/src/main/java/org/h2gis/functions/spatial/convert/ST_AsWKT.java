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
import org.locationtech.jts.io.WKTWriter;

/**
 * Convert a Geometry value into a Well Known Text value.
 * @author Nicolas Fortin
 */
public class ST_AsWKT extends DeterministicScalarFunction {
    /**
     * Default constructor
     */
    public ST_AsWKT() {
        addProperty(PROP_REMARKS, "Convert a geometry into WKT, a text representation of the geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "asWKT";
    }

    /**
     * Convert a Geometry value into a Well Known Text value.
     * @param geometry Geometry instance
     * @return The String representation
     */
    public static String asWKT(Geometry geometry) {
        if(geometry==null) {
            return null;
        }
        WKTWriter wktWriter = new WKTWriter(4);
        return wktWriter.write(geometry);
    }
}
