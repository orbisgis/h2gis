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

package org.h2gis.h2spatialext.function.spatial.convert;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * This class is used to generate an OSM map link URL from a geometry.
 * 
 * @author Erwan Bocher
 */
public class ST_OSMMapLink extends DeterministicScalarFunction {

    public ST_OSMMapLink() {
        addProperty(PROP_REMARKS, "Generate an OSM map link URL based on the bounding box of the input geometry.\n"
                + "An optional argument could be used to place a marker on the center of the bounding box.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "generateLink";
    }

    /**
     * Create the OSM map link based on the bounding box of the geometry.
     *
     * @param geom the input geometry.
     * @return
     */
    public static String generateLink(Geometry geom) {
        return generateLink(geom, false);
    }

    /**
     * Create the OSM map link based on the bounding box of the geometry.
     *
     * @param geom the input geometry.
     * @param withMarker true to place a marker on the center of the BBox.
     * @return
     */
    public static String generateLink(Geometry geom, boolean withMarker) {
        if (geom == null) {
            return null;
        }
        Envelope env = geom.getEnvelopeInternal();
        StringBuilder sb = new StringBuilder("http://www.openstreetmap.org/?");
        sb.append("minlon=").append(env.getMinX());
        sb.append("&minlat=").append(env.getMinY());
        sb.append("&maxlon=").append(env.getMaxX());
        sb.append("&maxlat=").append(env.getMaxY());
        if (withMarker) {
            Coordinate centre = env.centre();
            sb.append("&mlat=").append(centre.y);
            sb.append("&mlon=").append(centre.x);
        }
        return sb.toString();
    }
}
