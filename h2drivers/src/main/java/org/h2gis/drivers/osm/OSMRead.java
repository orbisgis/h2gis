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
package org.h2gis.drivers.osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

/**
 * SQL Function to copy OSM File data into a set of tables.
 *
 * @author Erwan Bocher
 */
public class OSMRead extends AbstractFunction implements ScalarFunction {

    public OSMRead() {
        addProperty(PROP_REMARKS, "Read a OSM file and copy the content in the specified tables.\n" +
                "Here a sample in order to extract buildings polygons using way nodes:\n" +
                "create index on MAP_WAY_NODE(ID_WAY,ID_NODE);\n" +
                "drop table if exists MAP_BUILDINGS,MAP_WAY_GEOM;\n" +
                "create table MAP_BUILDINGS(ID_WAY bigint primary key) as SELECT DISTINCT ID_WAY FROM MAP_WAY_TAG WT," +
                " MAP_TAG T WHERE WT.ID_TAG = T.ID_TAG AND T.TAG_KEY IN ('building');\n" +
                "create table MAP_WAY_GEOM(ID_WAY BIGINT PRIMARY KEY, THE_GEOM POLYGON) AS SELECT ID_WAY, " +
                "ST_MAKEPOLYGON(ST_MAKELINE(THE_GEOM)) THE_GEOM FROM (SELECT (SELECT ST_ACCUM(THE_GEOM) THE_GEOM FROM" +
                " (SELECT N.ID_NODE, N.THE_GEOM,WN.ID_WAY IDWAY FROM MAP_NODE N,MAP_WAY_NODE WN WHERE N.ID_NODE = WN" +
                ".ID_NODE ORDER BY WN.NODE_ORDER) WHERE  IDWAY = W.ID_WAY) THE_GEOM ,W.ID_WAY FROM MAP_WAY W," +
                "MAP_BUILDINGS B WHERE W.ID_WAY = B.ID_WAY) GEOM_TABLE WHERE ST_GEOMETRYN(THE_GEOM," +
                "1) = ST_GEOMETRYN(THE_GEOM, ST_NUMGEOMETRIES(THE_GEOM)) AND ST_NUMGEOMETRIES(THE_GEOM) > 2;");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readOSM";
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName, String tableReference) throws FileNotFoundException, SQLException {
        File file = URIUtility.fileFromString(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("The following file does not exists:\n" + fileName);
        }
        if (file.getName().toLowerCase().endsWith(".osm")) {
            OSMParser osmp = new OSMParser();
            osmp.read(connection, tableReference, file, new EmptyProgressVisitor());
        } else if (file.getName().toLowerCase().endsWith(".osm.gz")) {
            OSMParser osmp = new OSMParser();
            osmp.read(connection, tableReference, file, new EmptyProgressVisitor());
        } else if (file.getName().toLowerCase().endsWith(".osm.bz2")) {
            OSMParser osmp = new OSMParser();
            osmp.read(connection, tableReference, file, new EmptyProgressVisitor());
        } else {
            throw new SQLException("Supported formats are .osm, .osm.gz, .osm.bz2");
        }
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName) throws FileNotFoundException, SQLException {
        final String name = URIUtility.fileFromString(fileName).getName();
        readOSM(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }

}
