/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatial.internal.type;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert H2 constraint string into a OGC geometry type index.
 * @author Nicolas Fortin
 */
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
