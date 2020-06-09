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
import org.locationtech.jts.geom.Geometry;

/**
 * Return the type of geometry : ST_POINT, ST_LINESTRING, ST_POLYGON...
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS (2020)
 */
public class ST_GeometryType extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_GeometryType() {
        addProperty(PROP_REMARKS, "Return the geometry type of the ST_Geometry value\n "
                + " for a user-defined type defined in SQL/MM specification. SQL-MM 3: 5.1.4");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getGeometryType";
    }

    /**
     * @param geometry Geometry instance
     * @return Geometry type for a user-defined type defined in SQL/MM specification. SQL-MM 3: 5.1.4
     */
    public static String getGeometryType(Geometry geometry) {
        if(geometry==null) {
            return null;
        }
        return GeometryMetaData.getMetaData(geometry).getGeometryType();
    }
}
