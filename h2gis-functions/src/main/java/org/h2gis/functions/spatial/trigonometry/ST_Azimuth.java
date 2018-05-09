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

package org.h2gis.functions.spatial.trigonometry;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.h2gis.api.DeterministicScalarFunction;


/**
 * Returns the azimuth of the segment defined by the given Point geometries.
 * Return value is in radians.
 */
public class ST_Azimuth extends DeterministicScalarFunction{


    public ST_Azimuth(){
        addProperty(PROP_REMARKS, "Returns the azimuth of the segment defined by the given Point geometries, \n" +
                "or Null if the two points are coincident. Return value is in radians. \n" +
                " Angle is computed clockwise from the north equals to 0.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "azimuth";
    }

    /**
     * This code compute the angle in radian as postgis does.
     * @author :  Jose Martinez-Llario from JASPA. JAva SPAtial for SQL
     * @param pointA
     * @param pointB
     * @return
     */
    public static Double azimuth(Geometry pointA, Geometry pointB){
        if(pointA == null||pointB == null){
            return null;
        }
        if ((pointA instanceof Point) && (pointB instanceof Point)) {
            Double angle ;
            double x0 = ((Point) pointA).getX();
            double y0 = ((Point) pointA).getY();
            double x1 = ((Point) pointB).getX();
            double y1 = ((Point) pointB).getY();

            if (x0 == x1) {
                if (y0 < y1) {
                    angle = 0.0;
                } else if (y0 > y1) {
                    angle = Math.PI;
                } else {
                    angle = null;
                }
            } else

            if (y0 == y1) {
                if (x0 < x1) {
                    angle = Math.PI / 2;
                } else if (x0 > x1) {
                    angle = Math.PI + (Math.PI / 2);
                } else {
                    angle = null;
                }
            } else

            if (x0 < x1) {
                if (y0 < y1) {
                    angle = Math.atan(Math.abs(x0 - x1) / Math.abs(y0 - y1));
                } else { /* ( y0 > y1 ) - equality case handled above */
                    angle = Math.atan(Math.abs(y0 - y1) / Math.abs(x0 - x1)) + (Math.PI / 2);
                }
            }

            else { /* ( x0 > x1 ) - equality case handled above */
                if (y0 > y1) {
                    angle = Math.atan(Math.abs(x0 - x1) / Math.abs(y0 - y1)) + Math.PI;
                } else { /* ( y0 < y1 ) - equality case handled above */
                    angle = Math.atan(Math.abs(y0 - y1) / Math.abs(x0 - x1)) + (Math.PI + (Math.PI / 2));
                }
            }
            return angle;
        }
        return null;
    }
}
