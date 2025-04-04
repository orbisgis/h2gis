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

package org.h2gis.functions.spatial.earth;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.sql.Timestamp;

/**
 * Compute the sun position and return a new coordinate with x = azimuth and y = altitude
 * 
 * @author Erwan Bocher
 */
public class ST_SunPosition extends DeterministicScalarFunction{

    
    public ST_SunPosition(){
        addProperty(PROP_REMARKS, "Return the sun position (horizontal coordinate system) as a Point where : \n"
                + "x = sun azimuth in radians (direction along the horizon, measured from north to\n"
                + "east).\n"        
                + "y = sun altitude above the horizon in radians, e.g. 0 at the\n"
                + "horizon and PI/2 at the zenith.\n");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "sunPosition";
    }
    
    /**
     * Return the current sun position 
     * @param point sum location
     * @return Geometry
     */
    public static Geometry sunPosition(Geometry point){
        return sunPosition(point, new Timestamp(0));
    }
    
    /**
     * Return the sun position for a given date
     * 
     * @param point location of the point
     * @param date date
     * @return sum position as a point
     */
    public static Geometry sunPosition(Geometry point, Timestamp date) throws IllegalArgumentException{
        if(point == null){
            return null;
        }
        if (point instanceof Point) {
            Coordinate coord = point.getCoordinate();
            return point.getFactory().createPoint( SunCalc.getPosition(date, coord.y, coord.x));
        } else {
            throw new IllegalArgumentException("The sun position is computed according a point location.");
        }
    }
}
