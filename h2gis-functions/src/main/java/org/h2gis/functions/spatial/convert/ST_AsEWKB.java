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

import org.h2.util.geometry.JTSUtils;
import org.h2.value.Value;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

import java.sql.SQLException;

/**
 * Convert a Geometry value into an Extended Well-Known Binary.
 * @author Nicolas Fortin
 */
public class ST_AsEWKB extends DeterministicScalarFunction {
    /**
     * Default constructor
     */
    public ST_AsEWKB() {
        addProperty(PROP_REMARKS, "Convert a geometry into EWKB representation.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "asEWKB";
    }

    /**
     * Convert a Geometry value into an Extended Well-Known Binary.
     * @param value Geometry instance
     * @return The bytes representation
     */
    public static byte[] asEWKB(Value value) throws SQLException {
        switch (value.getValueType()) {
            case Value.NULL:
            case Value.GEOMETRY:
                return value.getBytes();
            default:
                throw new SQLException("ST_AsEWKB only supports geometry value");
        }
    }
}
