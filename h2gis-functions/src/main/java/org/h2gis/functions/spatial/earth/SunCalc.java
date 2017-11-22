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

package org.h2gis.functions.spatial.earth;

import org.locationtech.jts.geom.Coordinate;
import java.util.Date;

/**
 * This class is a partial Java port of SunCalc. A BSD-licensed JavaScript library for
 * calculating sun position, sunlight phases (times for sunrise, sunset, dusk,
 * etc.), moon position and lunar phase for the given location and time, created
 * by Vladimir Agafonkin (@mourner) as a part of the SunCalc.net project.
 *
 * @author Erwan Bocher
 */
public class SunCalc {    
    
    /* Constants */
    private final static double rad = Math.PI / 180;
    private final static double dayMs = 1000 * 60 * 60 * 24;
    private final static double J1970 = 2440588;
    private final static double J2000 = 2451545;
    private final static double M0 = rad * 357.5291;
    private final static double M1 = rad * 0.98560028;
    private final static double J0 = 0.0009;
    private final static double J1 = 0.0053;
    private final static double J2 = -0.0069;
    private final static double C1 = rad * 1.9148;
    private final static double C2 = rad * 0.0200;
    private final static double C3 = rad * 0.0003;
    private final static double P = rad * 102.9372;// perihelion of the Earth
    // obliquity of the Earth
    //The mean obliquity of the ecliptic is calculated by a formula of Laskar (1986), 
    //given in Jean Meeus: "Astronomical Algorithms", p. 135. 
    private final static double e = rad * 23.4397;
    
    private final static double th0 = rad * 280.16;
    private final static double th1 = rad * 360.9856235;   
   

    private SunCalc() {       
    }
  
    private static double dateToJulianDate(Date date) {
        return date.getTime() / dayMs - 0.5 + J1970;
    }
    
    private static Date julianDateToDate(double j) {
        return new Date(Math.round((j + 0.5 - J1970) * dayMs));
    }
    
    // general sun calculations
    private static long getJulianCycle(double J, double lw) {
        return Math.round(J - J2000 - J0 - lw / (2 * Math.PI));
    }

    private static double getSolarMeanAnomaly(double Js) {
        return M0 + M1 * (Js - J2000);
    }

    private static double getEquationOfCenter(double M) {
        return C1 * Math.sin(M) + C2 * Math.sin(2 * M) + C3 * Math.sin(3 * M);
    }

    private static double getEclipticLongitude(double M, double C) {
        return M + P + C + Math.PI;
    }

    private static double getSunDeclination(double Ls) {
        return Math.asin(Math.sin(Ls) * Math.sin(e));
    }

    // calculations for sun times
    private static double getApproxTransit(double Ht, double lw, double n) {
        return J2000 + J0 + (Ht + lw) / (2 * Math.PI) + n;
    }

    private static double getSolarTransit(double Js, double M, double Ls) {
        return Js + (J1 * Math.sin(M)) + (J2 * Math.sin(2 * Ls));
    }

    private static double getHourAngle(double h, double phi, double d) {
        return Math.acos((Math.sin(h) - Math.sin(phi) * Math.sin(d))
                / (Math.cos(phi) * Math.cos(d)));
    }

    // calculations for sun position
    private static double getRightAscension(double Ls) {
        return Math.atan2(Math.sin(Ls) * Math.cos(e), Math.cos(Ls));
    }

    private static double getSiderealTime(double J, double lw) {
        return th0 + th1 * (J - J2000) - lw;
    }
    
    /**
     * Sun azimuth in radians (direction along the horizon, measured from north to east)
     * e.g. 0 is north
     * @param H
     * @param phi
     * @param d
     * @return 
     */
    private static double getAzimuth(double H, double phi, double d) {
        return Math.atan2(Math.sin(H),
                Math.cos(H) * Math.sin(phi) - Math.tan(d) * Math.cos(phi))+Math.PI;
    }
    
    /**
     * Sun altitude above the horizon in radians.
     * e.g. 0 at the horizon and PI/2 at the zenith 
     * @param H
     * @param phi
     * @param d
     * @return 
     */
    private static double getAltitude(double H, double phi, double d) {
        return Math.asin(Math.sin(phi) * Math.sin(d) + Math.cos(phi)
                * Math.cos(d) * Math.cos(H));
    }

    /**
     * Returns the sun position as a coordinate with the following properties
     *
     * x: sun azimuth in radians (direction along the horizon, measured from south to
     * west), e.g. 0 is south and Math.PI * 3/4 is northwest.
     * y: sun altitude above the horizon in radians, e.g. 0 at the
     * horizon and PI/2 at the zenith (straight over your head). 
     * 
     *
     * @param date
     * @param lat
     * @param lng
     * @return
     */
    public static Coordinate getPosition(Date date, double lat,
            double lng) {
        if (isGeographic(lat, lng)) {
            double lw = rad * -lng;
            double phi = rad * lat;
            double J = dateToJulianDate(date);
            double M = getSolarMeanAnomaly(J);
            double C = getEquationOfCenter(M);
            double Ls = getEclipticLongitude(M, C);
            double d = getSunDeclination(Ls);
            double a = getRightAscension(Ls);
            double th = getSiderealTime(J, lw);
            double H = th - a;
            return new Coordinate(getAzimuth(H, phi, d),getAltitude(H, phi, d));
        } else {
            throw new IllegalArgumentException("The coordinate of the point must in latitude and longitude.");
        }
    }
    
    
    /**
     * Test if the point has valid latitude and longitude coordinates.
     * @param latitude
     * @param longitude
     * @return 
     */
    public static boolean isGeographic(double latitude,
            double longitude) {
        return latitude > -90 && latitude < 90
                && longitude > -180 && longitude < 180;
    }
}
