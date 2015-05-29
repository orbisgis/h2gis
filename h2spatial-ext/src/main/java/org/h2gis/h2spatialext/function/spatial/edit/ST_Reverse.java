/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Returns the geometry with vertex order reversed.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_Reverse extends DeterministicScalarFunction {

    public ST_Reverse() {
        addProperty(PROP_REMARKS, "Returns the geometry with vertex order reversed.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "reverse";
    }

    /**
     * Returns the geometry with vertex order reversed.
     *
     * @param geometry Geometry
     * @return geometry with vertex order reversed
     */
    public static Geometry reverse(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof MultiPoint) {
            return reverseMultiPoint((MultiPoint) geometry);
        }
        return geometry.reverse();
    }

    /**
     * Returns the MultiPoint with vertex order reversed. We do our own
     * implementation here because JTS does not handle it.
     *
     * @param mp MultiPoint
     * @return MultiPoint with vertex order reversed
     */
    public static Geometry reverseMultiPoint(MultiPoint mp) {
        int nPoints = mp.getNumGeometries();
        Point[] revPoints = new Point[nPoints];
        for (int i = 0; i < nPoints; i++) {
            revPoints[nPoints - 1 - i] = (Point) mp.getGeometryN(i).reverse();
        }
        return mp.getFactory().createMultiPoint(revPoints);
    }
}
