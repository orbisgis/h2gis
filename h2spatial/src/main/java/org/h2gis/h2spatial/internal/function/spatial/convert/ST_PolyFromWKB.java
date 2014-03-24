/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatial.internal.function.spatial.convert;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
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
        addProperty(PROP_REMARKS, "Convert Well Known Binary into Geometry then check that it is a Polygon.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toPolygon";
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
