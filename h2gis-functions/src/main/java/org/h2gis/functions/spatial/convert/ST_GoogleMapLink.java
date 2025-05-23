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


package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 *
 * @author Erwan Bocher
 */
public class ST_GoogleMapLink extends DeterministicScalarFunction{
    
    public ST_GoogleMapLink() {
        addProperty(PROP_REMARKS, "Generate a Google Map link URL based on the center of the bounding box of the input geometry.\n"
                + "Optional arguments :\n"
                + " (1) specify the layer type  m (normal  map) , k (satellite), h (hybrid), p (terrain).\n"
                + " (2) set a zoom level between 1 and 19.\n"
                + " Default values are m and 19.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "generateGMLink";
    }

    /**
     * Generate a Google Map link URL based on the center of the bounding box of the input geometry
     * 
     * @param geom input geometry
     * @return google url location
     */
    public static String generateGMLink(Geometry geom) {
        return generateGMLink(geom, "m", 19);
    }
    
    /**
     * Generate a Google Map link URL based on the center of the bounding box of the input geometry
     * and set the layer type
     * 
     * @param geom input geometry
     * @param layerType layer type
     * @return google map url location
     */
    public static String generateGMLink(Geometry geom, String layerType) {
        return generateGMLink(geom, layerType, 19);
    }

    /**
     * Generate a Google Map link URL based on the center of the bounding box of the input geometry.
     * Set the layer type and the zoom level.
     * 
     * @param geom input geometry
     * @param layerType layer type
     * @param zoom zoom level
     * @return google map url location
     */
    public static String generateGMLink(Geometry geom, String layerType, int zoom) {
        if (geom == null) {
            return null;
        }
        try {
            LayerType layer = LayerType.valueOf(layerType.toLowerCase());
            Coordinate centre = geom.getEnvelopeInternal().centre();
            StringBuilder sb = new StringBuilder("https://maps.google.com/maps?ll=");
            sb.append(centre.y);
            sb.append(",");
            sb.append(centre.x);
            sb.append("&z=");
            sb.append(zoom);
            sb.append("&t=");
            sb.append(layer.name());
            return sb.toString();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Layer type supported are m (normal  map) , k (satellite), h (hybrid), p (terrain)", ex);
        }
    }
    
    /**
     * List of supported layers for Google Map
     */
    public enum LayerType {
        m,//– normal  map
        k,//– satellite
        h,//– hybrid
        p//– terrain
    }

}
