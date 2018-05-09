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


package org.h2gis.functions.spatial.convert;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

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
     * @param geom
     * @return 
     */
    public static String generateGMLink(Geometry geom) {
        return generateGMLink(geom, "m", 19);
    }
    
    /**
     * Generate a Google Map link URL based on the center of the bounding box of the input geometry
     * and set the layer type
     * 
     * @param geom
     * @param layerType
     * @return 
     */
    public static String generateGMLink(Geometry geom, String layerType) {
        return generateGMLink(geom, layerType, 19);
    }

    /**
     * Generate a Google Map link URL based on the center of the bounding box of the input geometry.
     * Set the layer type and the zoom level.
     * 
     * @param geom
     * @param layerType
     * @param zoom
     * @return 
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
        p;//– terrain
    }

}
