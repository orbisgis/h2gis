/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.convert;

import org.h2.value.ValueGeometry;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Convert Well Known Binary into a LINESTRING.
 * @author Nicolas Fortin
 */
public class ST_LineFromWKB extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_LineFromWKB() {
        addProperty(PROP_REMARKS, "Convert Well Known Binary into a LINESTRING.\n If an SRID is not specified, it defaults to 0.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toLineString";
    }

    /**
     * Convert WKT into a LinearRing
     * @param bytes Byte array
     * @return LineString instance of null if bytes null
     */
    public static Geometry toLineString(byte[] bytes) throws SQLException, IOException {
        return toLineString(bytes, 0);
    }


    /**
     * Convert WKT into a LinearRing
     * @param bytes Byte array
     * @param srid SRID
     * @return LineString instance of null if bytes null
     */
    public static Geometry toLineString(byte[] bytes, int srid) throws SQLException, IOException {
        if (bytes == null) {
            return null;
        }
        ValueGeometry valueGeometry = ValueGeometry.get(bytes);
        if (valueGeometry.getTypeAndDimensionSystem() != GeometryTypeCodes.LINESTRING) {
            throw new SQLException("Provided WKB is not a POINT.");
        }
        Geometry geometry = valueGeometry.getGeometry();
        geometry.setSRID(srid);
        return geometry;
    }
}
