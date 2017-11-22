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
import org.h2gis.api.DeterministicScalarFunction;
import java.sql.SQLException;

/**
 * Convert a WKT String into a MULTILINESTRING.
 * @author Nicolas Fortin
 */
public class ST_MLineFromText extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_MLineFromText() {
        addProperty(PROP_REMARKS, "Convert a WKT String into a MULTILINESTRING.\n If an SRID is not specified, it defaults to 0.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGeometry";
    }
    
    /**
     * @param wKT WellKnown text value
     * @return Geometry
     * @throws java.sql.SQLException Invalid argument or the geometry type is wrong.
     */
    public static Geometry toGeometry(String wKT) throws SQLException {
        return toGeometry(wKT, 0);
    }
   

    /**
     * @param wKT WellKnown text value
     * @param srid Valid SRID
     * @return Geometry
     * @throws java.sql.SQLException Invalid argument or the geometry type is wrong.
     */
    public static Geometry toGeometry(String wKT, int srid) throws SQLException {
        Geometry geometry = ST_GeomFromText.toGeometry(wKT,srid);
        if(!geometry.getGeometryType().equalsIgnoreCase("MULTILINESTRING")) {
            throw new SQLException("The provided WKT Geometry is not a MULTILINESTRING");
        }
        return geometry;
    }
}
