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


package org.h2gis.functions.io.geojson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import java.io.IOException;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_GeomFromGeoJSON extends DeterministicScalarFunction{
    private static JsonFactory jsFactory;
    private static GJGeometryReader reader;

    public ST_GeomFromGeoJSON() {
        addProperty(PROP_REMARKS, "Convert a geojson representation of a geometry to a geometry object.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "geomFromGeoJSON";
    }
    
    /**
     * Convert a geojson geometry to geometry.
     *
     * @param geojson
     * @return
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static Geometry geomFromGeoJSON(String geojson) throws IOException, SQLException {
        if (geojson == null) {
            return null;
        }
        if (jsFactory == null) {
            jsFactory = new JsonFactory();
            jsFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            jsFactory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            jsFactory.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
            reader = new GJGeometryReader(new GeometryFactory());
        }
        JsonParser jp = jsFactory.createParser(geojson);
        return reader.parseGeometry(jp);
    }
}
