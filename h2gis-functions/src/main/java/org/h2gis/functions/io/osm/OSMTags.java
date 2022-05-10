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

package org.h2gis.functions.io.osm;

/**
 * Commons elments used by the OSM format
 *
 * @author Erwan Bocher
 */
public class OSMTags {

    static public String USER = "user", UID = "uid", VISIBLE = "visible", VERSION = "version", CHANGE_SET = "changeset",
            TIMESTAMP = "timestamp";

    // Fields count of the nodes table
    public static final int NODEFIELDCOUNT = 8;

    // Fields count of the ways table
    public static final int WAYFIELDCOUNT = 8;

}
