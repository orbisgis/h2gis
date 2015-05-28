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

/**
 * This class stores field information for a standard GPX file.
 *
 * @author Erwan Bocher and Antonin Piasco
 */
public class GpxMetadata {

    // Fields count of the tables
    public static final int WPTFIELDCOUNT = 24;
    public static final int RTEFIELDCOUNT = 11;
    public static final int RTEPTFIELDCOUNT = 25;
    public static final int TRKFIELDCOUNT = 11;
    public static final int TRKSEGFIELDCOUNT = 4;
    public static final int TRKPTFIELDCOUNT = 25;
    // Constant for the geometry field
    public static final int THE_GEOM = 0;   
    
    // Constant for the points
    public static final int PTID = 1;
    public static final int PTLAT = 2;
    public static final int PTLON = 3;
    public static final int PTELE = 4;
    public static final int PTTIME = 5;
    public static final int PTMAGVAR = 6;
    public static final int PTGEOIDWEIGHT = 7;
    public static final int PTNAME = 8;
    public static final int PTCMT = 9;
    public static final int PTDESC = 10;
    public static final int PTSRC = 11;
    public static final int PTLINK = 12;
    public static final int PTLINKTEXT = 13;
    public static final int PTSYM = 14;
    public static final int PTTYPE = 15;
    public static final int PTFIX = 16;
    public static final int PTSAT = 17;
    public static final int PTHDOP = 18;
    public static final int PTVDOP = 19;
    public static final int PTPDOP = 20;
    public static final int PTAGEOFDGPSDATA = 21;
    public static final int PTDGPSID = 22;
    public static final int PTEXTENSIONS = 23;
    // Constants for the lines
    public static final int LINEID = 1;
    public static final int LINENAME = 2;
    public static final int LINECMT = 3;
    public static final int LINEDESC = 4;
    public static final int LINESRC = 5;
    public static final int LINELINK_HREF = 6;
    public static final int LINELINK_HREFTITLE = 7;
    public static final int LINENUMBER = 8;
    public static final int LINETYPE = 9;
    public static final int LINEEXTENSIONS = 10;
    
    // Constant for the foreign route ID in routepoints
    public static final int RTEPT_RTEID = 24;
    // Constants for the track segments
    public static final int TRKSEGID = 1;
    public static final int TRKSEGEXTENSIONS = 2;
    public static final int TRKSEG_TRKID = 3;
    // Constant for the foreign track segment in trackpoints
    public static final int TRKPT_TRKSEGID = 24;
   
    private GpxMetadata() {
    }
}
