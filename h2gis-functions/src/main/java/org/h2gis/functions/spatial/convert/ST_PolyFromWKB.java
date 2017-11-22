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

package org.h2gis.functions.spatial.convert;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.jts_utils.GeometryMetaData;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Convert Well Known Binary into Geometry then check that it is a Polygon
 * @author Nicolas Fortin
 */
public class ST_PolyFromWKB extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_PolyFromWKB() {
        addProperty(PROP_REMARKS, "Convert Well Known Binary into Geometry then check that it is a POLYGON."
                + "\n If an SRID is not specified, it defaults to 0.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toPolygon";
    }
    
    /**
     * @param bytes WKB
     * @return Geometry instance of null if bytes are null
     * @throws SQLException Wkb parse exception
     */
    public static Geometry toPolygon(byte[] bytes) throws SQLException, IOException {
        return toPolygon(bytes, 0);
    }

    /**
     * @param bytes WKB
     * @param srid SRID
     * @return Geometry instance of null if bytes are null
     * @throws SQLException Wkb parse exception
     */
    public static Geometry toPolygon(byte[] bytes, int srid) throws SQLException, IOException {
        if(bytes==null) {
            return null;
        }
        WKBReader wkbReader = new WKBReader();
        try {
            if(GeometryMetaData.getMetaDataFromWKB(bytes).geometryType != GeometryTypeCodes.POLYGON) {
                throw new SQLException("Provided WKB is not a Polygon.");
            }
            Geometry geometry = wkbReader.read(bytes);
            geometry.setSRID(srid);
            return geometry;
        } catch (ParseException ex) {
            throw new SQLException("ParseException while evaluating ST_PolyFromWKB",ex);
        }
    }
}
