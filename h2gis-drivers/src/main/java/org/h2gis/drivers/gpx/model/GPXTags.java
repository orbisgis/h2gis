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

package org.h2gis.drivers.gpx.model;

/**
 * A class to manage all GPXTags used to parse the GPX and create the tables
 *
 * @author Erwan Bocher
 */
public class GPXTags {

    static public String LINK = "link", HREF = "href", ID = "id", EMAIL = "email",
            DOMAIN = "domain", COPYRIGHT = "copyright", AUTHOR = "author", WPT = "wpt",
            RTE = "rte", TRK = "trk", NAME = "name", TEXT = "text", YEAR = "year",
            LICENSE = "license", GPX = "gpx", VERSION = "version", CREATOR = "creator",
            BOUNDS = "bounds", MINLAT = "minlat", MAXLAT = "maxlat", MINLON = "minlon",
            MAXLON = "maxlon", TIME = "time", DESC = "desc", KEYWORDS = "keywords", RTEPT = "rtept",
            LON = "lon", LAT = "lat", TRKSEG = "trkseg", TRKPT = "trkpt", CMT = "cmt", SRC = "src",
            NUMBER = "number", TYPE = "type", EXTENSIONS = "extensions",
            URL = "url",  ELE = "ele", MAGVAR = "magvar", GEOIDHEIGHT = "geoidheight",
            SYM = "sym", FIX = "fix";
    static public String SAT = "sat", HDOP = "hdop", VDOP = "vdop", PDOP = "pdop",
            AGEOFDGPSDATA = "ageofdgpsdata",
            DGPSID = "dgpsid", HREFTITLE="href_title";

    private GPXTags() {
    }
}
