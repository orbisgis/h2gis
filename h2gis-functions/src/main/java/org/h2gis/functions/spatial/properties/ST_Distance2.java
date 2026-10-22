/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.properties;

import org.h2.value.ValueGeometry;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.predicates.DistanceIndexCache;

/**
 * ST_Distance returns the 2D distance between two geometries.
 *
 * <p>When the same large geometry is given on successive calls (a constant
 * argument, or the outer row of a nested loop join), the distance is computed
 * with an index on this geometry, shared with ST_DWithin.</p>
 */
public class ST_Distance2 extends DeterministicScalarFunction {

    private static final DistanceIndexCache CACHE = DistanceIndexCache.INSTANCE;

    public ST_Distance2() {
        addProperty(PROP_REMARKS, "Returns the 2-dimensional minimum Cartesian distance between two geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "distance";
    }

    /**
     * @param geomA Geometry A
     * @param geomB Geometry B
     * @return the 2D distance between the two geometries
     */
    public static Double distance(ValueGeometry geomA, ValueGeometry geomB) {
        if (geomA == null || geomB == null) {
            return null;
        }
        if (geomA.getEnvelopeNoCopy() == null || geomB.getEnvelopeNoCopy() == null) {
            return geomA.getGeometry().distance(geomB.getGeometry());
        }
        DistanceIndexCache.DistanceIndex index = CACHE.get(geomA);
        if (index != null) {
            return index.distance(geomB.getGeometry());
        }
        index = CACHE.get(geomB);
        if (index != null) {
            return index.distance(geomA.getGeometry());
        }
        return geomA.getGeometry().distance(geomB.getGeometry());
    }
}