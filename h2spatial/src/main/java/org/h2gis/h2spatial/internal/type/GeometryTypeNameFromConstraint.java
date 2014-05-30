package org.h2gis.h2spatial.internal.type;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.SFSUtilities;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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
     * @return Geometry type
     */
    public static String getGeometryTypeNameFromConstraint(String constraint, int numericPrecision) {
        int geometryTypeCode = GeometryTypeFromConstraint.geometryTypeFromConstraint(constraint, numericPrecision);
        return SFSUtilities.getGeometryTypeNameFromCode(geometryTypeCode);
    }
}
