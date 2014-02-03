package org.h2gis.h2spatial.internal.function.spatial.properties;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.GeometryMetaData;

import java.io.IOException;

/**
 * Returns the OGC SFS {@link org.h2gis.utilities.GeometryTypeCodes} of a Geometry. This function does not take account of Z nor M.
 * This function is not part of SFS; see {@link org.h2gis.h2spatial.internal.function.spatial.properties.ST_GeometryType}
 * It is used in constraints.
 * @author Nicolas Fortin
 */
public class ST_GeometryTypeCode extends DeterministicScalarFunction {
    public ST_GeometryTypeCode() {
        addProperty(PROP_REMARKS, "Returns the OGC SFS geometry type code from a Geometry");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getTypeCode";
    }

    /**
     * @param geometry Geometry WKB.
     * @return Returns the OGC SFS {@link org.h2gis.utilities.GeometryTypeCodes} of a Geometry. This function does not take account of Z nor M.
     * @throws IOException WKB is not valid.
     */
    public static Integer getTypeCode(byte[] geometry) throws IOException {
        if(geometry == null) {
            return null;
        }
        return GeometryMetaData.getMetaDataFromWKB(geometry).geometryType;
    }
}
