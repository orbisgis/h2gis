/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.convert;

import org.h2.value.ValueGeometry;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.GeometryMetaData;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Convert Well Known Binary into a POINT.
 *
 * @author Erwan Bocher
 */
public class ST_PointFromWKB extends DeterministicScalarFunction {


    public ST_PointFromWKB() {
        addProperty(PROP_REMARKS, "Convert Well Known Binary into a POINT.\n If an SRID is not specified, it defaults to 0.");
    }

    /**
     * Convert WKT into a Point
     *
     * @param bytes Byte array
     * @return Point instance of null if bytes null
     * @throws SQLException WKB Parse error
     */
    public static Geometry toPoint(byte[] bytes) throws SQLException, IOException {
        return toPoint(bytes, 0);
    }

    /**
     * Convert WKT into a Point
     *
     * @param bytes Byte array
     * @param srid  SRID
     * @return Point instance of null if bytes null
     * @throws SQLException WKB Parse error
     */
    public static Geometry toPoint(byte[] bytes, int srid) throws SQLException, IOException {
        if (bytes == null) {
            return null;
        }
        ValueGeometry valueGeometry = ValueGeometry.get(bytes);
        if (valueGeometry.getTypeAndDimensionSystem() != GeometryTypeCodes.POINT) {
            throw new SQLException("Provided WKB is not a POINT.");
        }
        Geometry geometry = valueGeometry.getGeometry();
        geometry.setSRID(srid);
        return geometry;
    }

    @Override
    public String getJavaStaticMethod() {
        return "toPoint";
    }

}
