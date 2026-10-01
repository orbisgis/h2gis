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

import org.h2gis.api.DeterministicScalarFunction;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueNull;
import org.locationtech.jts.geom.prep.PreparedGeometry;

import java.sql.SQLException;

/**
 * ST_CoveredBy returns true if no point in geometry B is outside geometry A.
 *
 * @author Erwan Bocher, CNRS
 */
public class ST_CoveredBy extends DeterministicScalarFunction {

    public ST_CoveredBy() {
        addProperty(PROP_REMARKS, "Returns true if this geomA is covered by geomB according the definitions : \n" +
                "Every point of this geometry is a point of the other geometry.\n" +
                "The DE-9IM Intersection Matrix for the two geometries matches\n" +
                " at least one of the following patterns:\n" +
                " [T*F**F***]\n" +
                " [*TF**F***]\n" +
                " [**FT*F***]\n" +
                " [**F*TF***]\n");
    }

    @Override
    public String getJavaStaticMethod() {
        return "evaluate";
    }

    private static final PreparedGeometryCache CACHE = new PreparedGeometryCache();

    /**
     * @param a first geometry
     * @param b second geometry
     * @return true if no point in geometry A is outside geometry B
     */
    public static Boolean evaluate(Value a, Value b) throws SQLException {
        if (a == ValueNull.INSTANCE || b == ValueNull.INSTANCE) {
            return null;
        }
        ValueGeometry geomA = a.convertToGeometry(null);
        ValueGeometry geomB = b.convertToGeometry(null);
        double[] envelopeA = geomA.getEnvelopeNoCopy();
        double[] envelopeB = geomB.getEnvelopeNoCopy();
        // A null envelope means an empty geometry
        if (envelopeA == null || envelopeB == null) {
            return false;
        }
        PreparedGeometryCache.checkSRID(geomA, geomB);
        if (!PreparedGeometryCache.envelopeContains(envelopeB, envelopeA)) {
            return false;
        }
        // A covered by B is B covers A
        PreparedGeometry prepared = CACHE.get(geomB);
        if (prepared != null) {
            return prepared.covers(geomA.getGeometry());
        }
        return geomA.getGeometry().coveredBy(geomB.getGeometry());
    }
}
