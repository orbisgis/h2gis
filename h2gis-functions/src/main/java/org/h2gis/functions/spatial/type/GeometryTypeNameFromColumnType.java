/*
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

package org.h2gis.functions.spatial.type;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.SFSUtilities;


/**
 * Parse the column type and return the Geometry type name.
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class GeometryTypeNameFromColumnType extends DeterministicScalarFunction {

    public GeometryTypeNameFromColumnType() {
        addProperty(PROP_REMARKS, "Parse the column type and return the Geometry type name");
        addProperty(PROP_NAME, "_GeometryTypeNameFromColumnType");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getGeometryTypeNameFromColumnType";
    }

    /**
     * Parse the column type and return the Geometry type name.
     * @param columnType column type
     * @return Geometry type
     */
    public static String getGeometryTypeNameFromColumnType(String columnType) {
        int geometryTypeCode = GeometryTypeFromColumnType.geometryTypeFromColumnType(columnType);
        return SFSUtilities.getGeometryTypeNameFromCode(geometryTypeCode);
    }
}
