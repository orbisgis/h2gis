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

package org.h2gis.functions.spatial.predicates;

import org.h2.util.geometry.GeometryUtils;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueNull;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;

/**
 * ST_DWithin returns true if the geometries are within the specified distance of one another.
 *
 * @author Erwan Bocher, CNRS
 */
public class ST_DWithin extends DeterministicScalarFunction {

    private static final DistanceIndexCache CACHE = new DistanceIndexCache();

    public ST_DWithin() {
        addProperty(PROP_REMARKS, "Returns true if the geometries are within " +
                "the specified distance of one another.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "isWithinDistance";
    }

    /**
     * Returns true if the geometries are within the specified distance of one another.
     *
     * @param a    Geometry A
     * @param b    Geometry B
     * @param distance Distance
     * @return True if the geometries are within the specified distance of one another
     */
    public static Boolean isWithinDistance(Value a, Value b, Double distance) throws SQLException {
        if (a == ValueNull.INSTANCE || b == ValueNull.INSTANCE || distance == null) {
            return null;
        }
        ValueGeometry geomA = a.convertToGeometry(null);
        ValueGeometry geomB = b.convertToGeometry(null);
        double[] envA = geomA.getEnvelopeNoCopy();
        double[] envB = geomB.getEnvelopeNoCopy();
        if (envA == null || envB == null) {
            return false;
        }
        if (geomA.getSRID() != geomB.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        double d = distance;
        // Reject on the envelopes, without building any JTS geometry
        if (d < 0 || computeEnvelopeDistanceSquared(envA, envB) > d * d) {
            return false;
        }
        //Use the distance index cache
        DistanceIndexCache.DistanceIndex index = CACHE.get(geomA);
        if (index != null) {
            return index.isWithinDistance(geomB.getGeometry(), d);
        }
        index = CACHE.get(geomB);
        if (index != null) {
            return index.isWithinDistance(geomA.getGeometry(), d);
        }
        return geomA.getGeometry().isWithinDistance(geomB.getGeometry(), d);
    }

    /**
     * Compute the squared distance between two envelopes, without decoding the geometries.
     * two geometries are never closer than their envelopes, so if this distance is larger
     * than the requested one, the geometries can be rejected immediately.
     * The squared value is compared to {@code distance * distance} to avoid a square root.
     *
     * @param envA H2 envelope (minX, maxX, minY, maxY)
     * @param envB H2 envelope (minX, maxX, minY, maxY)
     * @return the squared distance between the two envelopes, 0 if they intersect
     */
    static double computeEnvelopeDistanceSquared(double[] envA, double[] envB) {
        double dx = Math.max(0, Math.max(envA[GeometryUtils.MIN_X] - envB[GeometryUtils.MAX_X],
                envB[GeometryUtils.MIN_X] - envA[GeometryUtils.MAX_X]));
        double dy = Math.max(0, Math.max(envA[GeometryUtils.MIN_Y] - envB[GeometryUtils.MAX_Y],
                envB[GeometryUtils.MIN_Y] - envA[GeometryUtils.MAX_Y]));
        return dx * dx + dy * dy;
    }
}