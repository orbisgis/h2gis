/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
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
