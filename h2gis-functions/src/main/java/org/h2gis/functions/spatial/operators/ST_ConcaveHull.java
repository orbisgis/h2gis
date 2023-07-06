package org.h2gis.functions.spatial.operators;

import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueNumeric;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.geom.Geometry;

import java.sql.SQLException;

/**
 * Compute the concave geometry that encloses the vertices of the input geometry
 */
public class ST_ConcaveHull extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_ConcaveHull() {
        addProperty(PROP_REMARKS, "Computes the concave geometry that encloses the vertices of the input geometry.\n" +
                "Set allow_holes to true to allow hole in the concave geometry.\n" +
                "Set a ratio value between 0 and 1 to increase the hull concaveness.\n"+
                "Values between 0.3 and 0.1 produce reasonable results ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Compute the concave geometry that encloses the vertices of the input geometry
     * @param geometry input geometry
     * @return the concave geometry
     */
    public static Geometry execute(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        ConcaveHull concaveHull = new ConcaveHull(geometry);
        return concaveHull.getHull();
    }

    /**
     * Compute the concave geometry that encloses the vertices of the input geometry
     * @param geometry input geometry
     * @param param set true to allow hole or a double value to increase the hull concaveness
     * @return the concave geometry
     */
    public static Geometry execute(Geometry geometry, Value param) throws SQLException {
        if (geometry == null) {
            return null;
        }
        ConcaveHull concaveHull = new ConcaveHull(geometry);
        if(param instanceof ValueBoolean){
            concaveHull.setHolesAllowed(param.getBoolean());
        } else if (param instanceof ValueNumeric) {
            concaveHull.setMaximumEdgeLengthRatio(param.getDouble());
        }
        else{
            throw new SQLException("Parameter type not allowed.");
        }
        return concaveHull.getHull();
    }

    /**
     * Compute the concave geometry that encloses the vertices of the input geometry
     * @param geometry input geometry
     * @param ratio concaveness ratio
     * @param allow_holes true to allow hole
     * @return the concave geometry
     */
    public static Geometry execute(Geometry geometry, double ratio, boolean allow_holes) {
        if (geometry == null) {
            return null;
        }
        ConcaveHull concaveHull = new ConcaveHull(geometry);
        concaveHull.setMaximumEdgeLengthRatio(ratio);
        concaveHull.setHolesAllowed(allow_holes);
        return concaveHull.getHull();
    }
}
