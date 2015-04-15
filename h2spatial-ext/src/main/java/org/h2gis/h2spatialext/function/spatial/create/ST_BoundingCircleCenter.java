package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Compute the minimum bounding circle center of a geometry
 * @author Nicolas Fortin
 */
public class ST_BoundingCircleCenter extends DeterministicScalarFunction {

    /**
     * Constructor
     */
    public ST_BoundingCircleCenter() {
        addProperty(PROP_REMARKS, "Compute the minimum bounding circle center of a geometry");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getCircumCenter";
    }

    /**
     * Compute the minimum bounding circle center of a geometry
     * @param geometry Any geometry
     * @return Minimum bounding circle center point
     */
    public static Point getCircumCenter(Geometry geometry) {
        if(geometry == null) {
            return null;
        }
        return geometry.getFactory().createPoint(new MinimumBoundingCircle(geometry).getCentre());
    }
}
