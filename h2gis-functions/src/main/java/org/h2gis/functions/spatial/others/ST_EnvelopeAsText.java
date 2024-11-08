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

package org.h2gis.functions.spatial.others;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * Text representation of the geometry envelope
 * @author E. Bocher, CNRS
 */
public class ST_EnvelopeAsText extends AbstractFunction implements ScalarFunction {

    public ST_EnvelopeAsText() {
        addProperty(PROP_REMARKS, "Return a string representation of the Geometry envelope :\n" +
                " west, south, east, north -> minX, minY, maxX, max");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Return a string representation of the Geometry envelope
     * west, south, east, north  :  minX, minY, maxX, maxY
     *
     *
     * @param geom input geometry
     * @return a text representation of the geometry
     */
    public static String execute(Geometry geom) {
        if (geom == null) {
            return null;
        }
        Envelope env = geom.getEnvelopeInternal();
        return env.getMinX() + "," + env.getMinY() + "," + env.getMaxX() + "," + env.getMaxY();
    }
}
