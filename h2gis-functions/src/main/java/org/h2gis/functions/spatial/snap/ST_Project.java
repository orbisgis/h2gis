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

package org.h2gis.functions.spatial.snap;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.sql.SQLException;

/**
 * Function to project a point
 * @author Erwan Bocher, CNRS
 */
public class ST_Project extends DeterministicScalarFunction {


    public ST_Project(){
        addProperty(PROP_REMARKS, "Returns a point projected from a point along a geodesic using a given distance and azimuth (bearing) " +
                "when the input geometry has a SRID equal to 4326. Otherwise project the point in cartesian plan.");
    }


    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }


    /**
     * Project a point using a given distance and azimuth
     * @param geometry {@link Geometry}
     * @param distance is given in meters
     * @param azimuth is given in radians and measured clockwise from true north.
     * @return a project point
     */
    public static Geometry execute(Geometry geometry, double distance, double azimuth) throws SQLException {
        if(geometry==null){
            return null;
        }
        if(geometry instanceof Point) {
            if(geometry.getSRID()==4326) {
                Coordinate coord = geometry.getCoordinate().copy();
                GeodesicData res = Geodesic.WGS84.Direct(coord.getX(), coord.getY(), Math.toDegrees(azimuth), distance);
                coord.setX(res.lon2);
                coord.setY(res.lat2);
                Point geom = geometry.getFactory().createPoint(coord);
                geom.setSRID(4326);
                return geom;
            }
            else{
                Coordinate coord = geometry.getCoordinate().copy();
                double angle = Angle.normalizePositive(azimuth);
                coord.setX(coord.getX() + distance * Math.cos(angle));
                coord.setY(coord.getY() + distance * Math.sin(angle));
                Geometry geom =  geometry.getFactory().createPoint(coord);
                geom.setSRID(geometry.getSRID());
                return geom;
            }
        }
        throw new SQLException("The input geometry to project must be a point");
    }
}
