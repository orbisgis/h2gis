package org.h2gis.functions.spatial.generalize;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.PolygonHullSimplifier;

public class ST_SimplifyPolygonHull extends DeterministicScalarFunction {

    public ST_SimplifyPolygonHull() {
        addProperty(PROP_REMARKS, "Computes a simplified topology-preserving outer or inner hull of a polygonal geometry.\n" +
                "The Vertex Number Fraction specifies the desired result vertex count as a fraction of " +
                "the number of input vertices.  " +
                "\nThe value 1 produces the original geometry.  Smaller values produce simpler hulls.  " +
                "\nThe value 0 produces the maximum outer or minimum inner hull." +
                "\nSet isOuter is to false to compute the inner hull. ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Computes a simplified topology-preserving outer or inner hull of a polygonal geometry.
     * @param geometry the polygonal geometry to process
     * @param vertexNumFraction the fraction of number of input vertices in result
     * @return the hull geometry
     */
    public static Geometry execute(Geometry geometry, float vertexNumFraction) {
            return execute(geometry, vertexNumFraction, true);
    }

    /**
     * Computes a simplified topology-preserving outer or inner hull of a polygonal geometry.
     * @param geometry the polygonal geometry to process
      * @param isOuter indicates whether to compute an outer or inner hull
     * @param vertexNumFraction the fraction of number of input vertices in result
     * @return the hull geometry
     */
    public static Geometry execute(Geometry geometry, float vertexNumFraction, boolean isOuter) {
        if(geometry==null){
            return null;
        }
        return PolygonHullSimplifier.hull(geometry, isOuter, vertexNumFraction);
    }
}
