/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatialext.function.spatial.earth;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.util.Date;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_SunDirection extends DeterministicScalarFunction{

   

    @Override
    public String getJavaStaticMethod() {
        return "sunDirection";
    }

    /**
     * 
     * @param point
     * @return 
     */
    public static Point sunDirection(Geometry point) {
        return sunDirection(point, new Date());
    }

    /**
     * 
     * @param point
     * @param date
     * @return 
     */
    public static Point sunDirection(Geometry point, Date date) {
        if (point instanceof Point) {
            double[] position = SunCalc.getPosition(date, point.getCoordinate().y, point.getCoordinate().x);
            return point.getFactory().createPoint(calculateDirection(position[0], position[1]));
        } else {
            throw new IllegalArgumentException("The sun direction is computed according a point location.");
        }
    }

    /**
     * Calculation of the sun direction
     *     
     * @param altitude
     * @param azimuth
     * @return sun coordinates
     */
    public static Coordinate calculateDirection(double altitude, double azimuth) {
        Coordinate direction = new Coordinate();
        double length = 1.0 / (Math.tan(altitude));
        direction.x = -Math.cos(azimuth) * length;
        direction.y = -Math.sin(azimuth) * length;
        return direction;
    }
    
}
