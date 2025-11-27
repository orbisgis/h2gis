/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.generalize;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.PolygonHullSimplifier;

/**
 * Computes a simplified topology-preserving outer or inner hull of a polygonal geometry
 * @author Erwan Bocher, CNRS
 */
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
