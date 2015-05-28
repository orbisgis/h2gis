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
import org.h2gis.utilities.SFSUtilities;


/**
 * Parse the constraint and return the Geometry type name.
 * @author Nicolas Fortin
 */
public class GeometryTypeNameFromConstraint extends DeterministicScalarFunction {

    public GeometryTypeNameFromConstraint() {
        addProperty(PROP_REMARKS, "Parse the constraint and return the Geometry type name");
        addProperty(PROP_NAME, "_GeometryTypeNameFromConstraint");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getGeometryTypeNameFromConstraint";
    }

    /**
     * Parse the constraint and return the Geometry type name.
     * @param constraint Constraint on geometry type
     * @param numericPrecision of the geometry type
     * @return Geometry type
     */
    public static String getGeometryTypeNameFromConstraint(String constraint, int numericPrecision) {
        int geometryTypeCode = GeometryTypeFromConstraint.geometryTypeFromConstraint(constraint, numericPrecision);
        return SFSUtilities.getGeometryTypeNameFromCode(geometryTypeCode);
    }
}
