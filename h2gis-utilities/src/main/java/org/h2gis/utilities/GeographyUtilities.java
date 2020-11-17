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
package org.h2gis.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.cts.util.UTMUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Some utilities for geographic data
 * 
 * @author Erwan Bocher
 */
public class GeographyUtilities {
    
     /**
     * The approximate radius of earth as defined for WGS84 
     * see <a href="http://en.wikipedia.org/wiki/World_Geodetic_System"></a>. 
     */
    public static final double RADIUS_OF_EARTH_IN_METERS = 6378137.0;
    
    
    /** 
    * This method is used to create a JTS envelope by a given point coordinates in degrees
    * and spatial increments in meters.
    * 
    * @param point Coordinates of a point used to build an envelope.
    * @param dx Spatial horizontal increment expressed in meters
    * @param dy Spatial vertical increment expressed in meters.
    * @return an envelope
    */
    public static Envelope createEnvelope(Coordinate point, double dx, double dy) {
        if (point.isNull() or dx==0 or dy==0) {
            return null;
        }
        if (dx<0 or dy<0) {
            throw new IllegalArgumentException("Create operation does not accept negative value");
        }

        double pLat = point.getY();
        double pLon = point.getX();
     
        //Check if the envelope has latitude, longitude coordinates        
        if (!UTMUtils.isValidLatitude((float) pLat)) {
            throw new IllegalArgumentException("Invalid latitude");
        }
        if (!UTMUtils.isValidLatitude((float) pLon)) {
            throw new IllegalArgumentException("Invalid longitude");
        }

        double dLat = computeLatitudeDistance(dy);
        double dLon = computeLongitudeDistance(dx, pLat);
    
        return new Envelope(Math.max(-180, pLon), Math.min(180, pLon + dLon), 
                Math.max(-90, pLat), Math.min(90, pLat + dLat));

    }   
    
    
    /** 
    * This method is used to create a JTS envelope by a given point coordinates in degrees
    * and spatial increments in meters.
    * 
    * @param point Coordinates of a point used to build an envelope in degrees.
    * @param dx Spatial horizontal increment expressed in meters.
    * @param dy Spatial vertical increment expressed in meters.
    * @param quadrant One of the four quadrants delimiting the plane 
    *        (counter-clockwise and starting from the upper right)
    * @return an envelope
    */
    public static Envelope createEnvelopeByQuadrant(Coordinate point, double dx, double dy, int quadrant) {
        if (point.isNull() or dx==0 or dy==0) {
            return null;
        }
        if (dx<0 or dy<0) {
            throw new IllegalArgumentException("Create operation does not accept negative value");
        }
        if (quandrant.isNull()) {
            quadrant = 1;
        }
        if (quadrant<1 or quadrant>4) {
            throw new IllegalArgumentException("Quadrant argument must range in [1,4] interval");
        }
    
        double pLat = point.getY();
        double pLon = point.getX();
     
        //Check if the envelope has latitude, longitude coordinates        
        if (!UTMUtils.isValidLatitude((float) pLat)) {
            throw new IllegalArgumentException("Invalid latitude");
        }
        if (!UTMUtils.isValidLatitude((float) pLon)) {
            throw new IllegalArgumentException("Invalid longitude");
        }

        double dLat = computeLatitudeDistance(dy);
        double dLon = computeLongitudeDistance(dx, pLat);
    
        switch(quadrant) {
            case 1: 
                return new Envelope(Math.max(-180, pLon), Math.min(180, pLon + dLon), 
                        Math.max(-90, pLat), Math.min(90, pLat + dLat));
                break;
            case 2:
                return new Envelope(Math.max(-180, pLon - dLon), Math.min(180, pLon), 
                        Math.max(-90, pLat), Math.min(90, pLat + dLat));
                break;
            case 3:
                return new Envelope(Math.max(-180, pLon - dLon), Math.min(180, pLon), 
                        Math.max(-90, pLat - dLat), Math.min(90, pLat));
                break;
            case 4:
                return new Envelope(Math.max(-180, pLon), Math.min(180, pLon + dLon), 
                        Math.max(-90, pLat - dLat), Math.min(90, pLat));
                break;
        }
    }   
    
    
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
    
    /**
     * Return a SRID code from latitude and longitude coordinates
     *
     * @param connection to the database
     * @param latitude
     * @param longitude
     * @return a SRID code
     * @throws SQLException
     */
    public static int getSRID(Connection connection, float latitude, float longitude)
            throws SQLException {
        int srid = -1;
        PreparedStatement ps = connection.prepareStatement("select SRID from PUBLIC.SPATIAL_REF_SYS where PROJ4TEXT = ?");
        ps.setString(1, UTMUtils.getProj(latitude, longitude));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                srid = rs.getInt(1);
            }
        } finally {
            ps.close();
        }
        return srid;
    }
    
}
