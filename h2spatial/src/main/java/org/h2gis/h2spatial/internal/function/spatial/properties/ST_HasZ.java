package org.h2gis.h2spatial.internal.function.spatial.properties;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.GeometryMetaData;

import java.io.IOException;

/**
 * Check if the Geometry contain a Z value. Used in constraints.
 * @author Nicolas Fortin
 */
public class ST_HasZ extends DeterministicScalarFunction {

    public ST_HasZ() {
        addProperty(PROP_REMARKS, "Return true if the Geometry contain a Z value");
    }

    @Override
    public String getJavaStaticMethod() {
        return "hasZ";
    }

    /**
     * Check if the Geometry contain a Z value.
     * @param geometry Geometry WKB or null.
     * @return true if the Geometry contain a Z value, false otherwise. Null if geometry is null.
     * @throws IOException If Geometry MetaData is not well formed.
     */
    public static Boolean hasZ(byte[] geometry) throws IOException {
        if(geometry == null) {
            return null;
        }
        return GeometryMetaData.getMetaDataFromWKB(geometry).hasZ;
    }
}
