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
package org.h2gis.drivers.kml;

/**
 * Specifies how altitude components in the <coordinates> element are
 * interpreted. Possible values are
 *
 * - clampToGround - (default) Indicates to ignore an altitude specification
 * (for example, in the <coordinates> tag).
 *
 * - relativeToGround - Sets the altitude of the element relative to the actual
 * ground elevation of a particular location. For example, if the ground
 * elevation of a location is exactly at sea level and the altitude for a point
 * is set to 9 meters, then the elevation for the icon of a point placemark
 * elevation is 9 meters with this mode. However, if the same coordinate is set
 * over a location where the ground elevation is 10 meters above sea level, then
 * the elevation of the coordinate is 19 meters. A typical use of this mode is
 * for placing telephone poles or a ski lift.
 *
 * - absolute - Sets the altitude of the coordinate relative to sea level,
 * regardless of the actual elevation of the terrain beneath the element. For
 * example, if you set the altitude of a coordinate to 10 meters with an
 * absolute altitude mode, the icon of a point placemark will appear to be at
 * ground level if the terrain beneath is also 10 meters above sea level. If the
 * terrain is 3 meters above sea level, the placemark will appear elevated above
 * the terrain by 7 meters. A typical use of this mode is for aircraft
 * placement.
 *
 *
 * @author Erwan Bocher
 */
public final class AltitudeMode {

    public static final int KML_CLAMPTOGROUND = 1;
    public static final int KML_RELATIVETOGROUND = 2;
    public static final int KML_ABSOLUTE = 4;
    public static final int GX_CLAMPTOSEAFLOOR = 8;
    public static final int GX_RELATIVETOSEAFLOOR = 16;
    public static final int NONE = 0;

    /**
     * Default constructor
     */
    private AltitudeMode() {
    }

    /**
     * Generate a string value corresponding to the altitude mode.
     * 
     *
     * @param altitudeMode
     * @param sb
     */
    public static void append(int altitudeMode, StringBuilder sb) {
        switch (altitudeMode) {
            case KML_CLAMPTOGROUND:
                sb.append("<kml:altitudeMode>clampToGround</kml:altitudeMode>");
                return;
            case KML_RELATIVETOGROUND:
                sb.append("<kml:altitudeMode>relativeToGround</kml:altitudeMode>");
                return;
            case KML_ABSOLUTE:
                sb.append("<kml:altitudeMode>absolute</kml:altitudeMode>");
                return;
            case GX_CLAMPTOSEAFLOOR:
                sb.append("<gx:altitudeMode>clampToSeaFloor</gx:altitudeMode>");
                return;
            case GX_RELATIVETOSEAFLOOR:
                sb.append("<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>");
                return;
            case NONE:
                return;
            default:
                throw new IllegalArgumentException("Supported altitude modes are: \n"
                        + " For KML profils: CLAMPTOGROUND = 1; RELATIVETOGROUND = 2; ABSOLUTE = 4;\n"
                        + "For GX profils: CLAMPTOSEAFLOOR = 8; RELATIVETOSEAFLOOR = 16; \n"
                        + " No altitude: NONE = 0");
        }
    }
}
