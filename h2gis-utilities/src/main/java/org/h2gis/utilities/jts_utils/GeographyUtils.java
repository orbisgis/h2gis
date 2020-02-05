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
package org.h2gis.utilities.jts_utils;

import org.cts.util.UTMUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Some utilities for geographic data
 * 
 * @author Erwan Bocher
 */
public class GeographyUtils {
    
     /**
     * The approximate radius of earth as defined for WGS84 
     * see <a href="http://en.wikipedia.org/wiki/World_Geodetic_System"></a>. 
     */
    public static final double RADIUS_OF_EARTH_IN_METERS = 6378137.0;
         
    
    /** 
     * This method is used to expand a JTS envelope by a given distance in degrees
     * in all directions.
     * 
     * see : https://www.mkompf.com/gps/distcalc.html
     * @param envelope to expand
     * @param distance expressed in degrees 
     * @return a new envelope
     */
    public static Envelope expandEnvelopeByMeters(Envelope envelope, double distance) {
        if (envelope.isNull()) {
            return null;
        }
        if(distance==0){
            return envelope;
        }
        else if(distance<0){
            throw new IllegalArgumentException("Extend operation does not accept negative value");
        }
        
        double minLon = envelope.getMinX();
        double maxLon = envelope.getMaxX();
        double maxLat = envelope.getMaxY();
        double minLat = envelope.getMinY();
        
        //Check if the envelope has latitude, longitude co-ordinates        
        if (!UTMUtils.isValidLatitude((float) minLat)){
            throw new IllegalArgumentException("Invalid min latitude");
        }
        if (!UTMUtils.isValidLatitude((float) maxLat)){
            throw new IllegalArgumentException("Invalid max latitude");
        }
        if (!UTMUtils.isValidLongitude((float) minLon)){
            throw new IllegalArgumentException("Invalid min longitude");
        }
        if (!UTMUtils.isValidLongitude((float) maxLon)){
            throw new IllegalArgumentException("Invalid max longitude");
        }
        double verticalExpansion = computeLatitudeDistance(distance);
        double horizontalExpansion = computeLongitudeDistance(distance, Math.max(Math.abs(minLat), Math.abs(maxLat)));
        
        return new Envelope(Math.max(-180, minLon - horizontalExpansion), Math.min(180, maxLon + horizontalExpansion), 
                Math.max(-90, minLat - verticalExpansion), Math.min(90, maxLat + verticalExpansion));
    }   
    
    
     /**
     * Computes the amount of degrees of latitude for a given distance in meters.
     *
     * @param meters distance in meters
     * @return latitude degrees
     */
    public static double computeLatitudeDistance(double meters) {
        return (meters * 360) / (2 * Math.PI * RADIUS_OF_EARTH_IN_METERS);
    }

    /**
     * Computes the amount of degrees of longitude for a given distance in meters.
     *
     * @param meters   distance in meters
     * @param latitude the latitude at which the calculation should be performed
     * @return longitude degrees
     */
    public static double computeLongitudeDistance(double meters, double latitude) {
        return (meters * 360) / (2 * Math.PI * RADIUS_OF_EARTH_IN_METERS * Math.cos(Math.toRadians(latitude)));
    }
    
    /**
     * Calculate the spherical distance between two coordinates in meters using the
     * Haversine formula. See https://fr.wikipedia.org/wiki/Formule_de_haversine
     * This calculation is done using the approximate earth radius
     *
     * @param coordA
     * @param coordB
     * @return distance in meters
     */
    public static double getHaversineDistanceInMeters(Coordinate coordA, Coordinate coordB) {
        double dLat = Math.toRadians(coordB.getY() - coordA.getY());
        double dLon = Math.toRadians(coordB.getX() - coordA.getX());
        double a
                = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(coordA.getY())) * Math.cos(Math.toRadians(coordB.getY()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = RADIUS_OF_EARTH_IN_METERS * c;
        return d;
    }   
    
}
