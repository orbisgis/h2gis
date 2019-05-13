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

package org.h2gis.functions.spatial.type;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert H2 constraint string into a OGC geometry type index.
 *
 * Since H21.4.198, {@link GeometryTypeFromColumnType} should be used.
 *
 * @author Nicolas Fortin
 */
@Deprecated
public class GeometryTypeFromConstraint extends DeterministicScalarFunction {
    private static final Pattern TYPE_CODE_PATTERN = Pattern.compile(
            "ST_GeometryTypeCode\\s*\\(\\s*((([\"`][^\"`]+[\"`])|(\\w+)))\\s*\\)\\s*=\\s*(\\d)+", Pattern.CASE_INSENSITIVE);
    private static final int CODE_GROUP_ID = 5;

    /**
     * Default constructor
     */
    public GeometryTypeFromConstraint() {
        addProperty(PROP_REMARKS, "Convert H2 constraint string into a OGC geometry type index.");
        addProperty(PROP_NAME, "_GeometryTypeFromConstraint");
    }

    @Override
    public String getJavaStaticMethod() {
        return "geometryTypeFromConstraint";
    }

    /**
     * Convert H2 constraint string into a OGC geometry type index.
     * @param constraint SQL Constraint ex: ST_GeometryTypeCode(the_geom) = 5
     * @param numericPrecision This parameter is available if the user give domain
     * @return Geometry type code {@link org.h2gis.utilities.GeometryTypeCodes}
     */
    public static int geometryTypeFromConstraint(String constraint, int numericPrecision) {
        if(constraint.isEmpty() && numericPrecision > GeometryTypeCodes.GEOMETRYZM) {
            return GeometryTypeCodes.GEOMETRY;
        }
        // Use Domain given parameters
        if(numericPrecision <= GeometryTypeCodes.GEOMETRYZM) {
            return numericPrecision;
        }
        // Use user defined constraint. Does not work with VIEW TABLE
        Matcher matcher = TYPE_CODE_PATTERN.matcher(constraint);
        if(matcher.find()) {
            return Integer.valueOf(matcher.group(CODE_GROUP_ID));
        } else {
            return GeometryTypeCodes.GEOMETRY;
        }
    }
}
