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
package org.h2gis.drivers.gpx.model;

import com.vividsolutions.jts.geom.Coordinate;
import org.xml.sax.Attributes;

/**
 * This class is used to convert a waypoint to a coordinate
 * @author Erwan Bocher
 */
public class GPXCoordinate {
    
    /**
     * General method to create a coordinate from a gpx point. 
     *
     * @param attributes Attributes of the point. Here it is latitude and
     * longitude
     * @throws NumberFormatException
     * @return a coordinate
     */
    public static Coordinate createCoordinate(Attributes attributes) throws NumberFormatException {
        // Associate a latitude and a longitude to the point
        double lat;
        double lon;
        try {
            lat = Double.parseDouble(attributes.getValue(GPXTags.LAT));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot parse the latitude value");
        }
        try {
            lon = Double.parseDouble(attributes.getValue(GPXTags.LON));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot parse the longitude value");
        }
        String eleValue = attributes.getValue(GPXTags.ELE);
        double ele = Double.NaN;
        if (eleValue != null) {
            try {
                ele = Double.parseDouble(eleValue);

            } catch (NumberFormatException e) {
                throw new NumberFormatException("Cannot parse the elevation value");
            }
        }        
        return new Coordinate(lon, lat, ele);
    }
}
