package org.h2gis.h2spatialext.function.spatial.create;

/**
 * PostGIS compatibility function alias.
 * @author Nicolas Fortin
 */
public class ST_MinimumBoundingCircle extends ST_BoundingCircle {
    public ST_MinimumBoundingCircle() {
        addProperty(PROP_REMARKS, "Compute the minimum bounding circle of a geometry. This is an alias for ST_BoundingCircle");
    }
}
