/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.h2gis.api.DeterministicScalarFunction;

/**
 * For geometry type returns minimum distance in meters between two geometries
 * @author Michael MATUR
 */
public class ST_DistanceSphere extends DeterministicScalarFunction {

    private static final double EARTH_RADIUS_METER = 6370986;

    /**
     * Default constructor
     */
    public ST_DistanceSphere() {
        addProperty(PROP_REMARKS, "Returns minimum distance in meters between two lon/lat points. Uses a spherical earth and radius derived from the spheroid defined by the SRID");
    }

    @Override
    public String getJavaStaticMethod() {
        return "distanceSphere";
    }

    /**
     * @param a Geometry instance or null
     * @param b Geometry instance or null
     * @return minimum distance in meters between two geometries
     */
    public static Double distanceSphere(Geometry a,Geometry b) {
        if(a==null || b==null) {
            return null;
        }
        double aX = 0;
        double aY = 0;
	    double bX = 0;
	    double bY = 0;
        if (a instanceof Point) {
            aX = ((Point) a).getX();
            aY = ((Point) a).getY();
        } else if (a instanceof LineString || a instanceof Polygon) {
	        aX = a.getCentroid().getX();
	        aY = a.getCentroid().getY();
        }

        if (b instanceof Point) {
            bX = ((Point) b).getX();
            bY = ((Point) b).getY();
        } else if (b instanceof LineString || b instanceof Polygon) {
            bX = b.getCentroid().getX();
            bY = b.getCentroid().getY();
        }

        Double dY = Math.toRadians(bY - aY);
        Double dX = Math.toRadians(bX - aX);
        Double raY = Math.toRadians(aY);
        Double rbY = Math.toRadians(bY);

	    return EARTH_RADIUS_METER * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(dY / 2), 2) +
		    Math.pow(Math.sin(dX / 2), 2) * Math.cos(raY) * Math.cos(rbY)));
    }
}
