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

package org.h2gis.functions.io.osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.URIUtilities;

/**
 * SQL Function to copy OSM File data into a set of tables.
 *
 * @author Erwan Bocher
 */
public class OSMRead extends AbstractFunction implements ScalarFunction {

    public OSMRead() {
        addProperty(PROP_REMARKS, "Read a OSM file and copy the content in the specified tables.\n"
                + "The user can set a prefix name for all OSM tables and specify if the existing OSM\n"
                + " tables must be dropped." +
                "\nHere a sample in order to extract buildings polygons using way nodes:\n" +
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
     * @param deleteTables  true to delete the existing tables
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName, String tableReference, boolean deleteTables) throws FileNotFoundException, SQLException, IOException {
        if (deleteTables) {
            OSMTablesFactory.dropOSMTables(connection, JDBCUtilities.isH2DataBase(connection.getMetaData()), tableReference);
        }
        File file = URIUtilities.fileFromString(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("The following file does not exists:\n" + fileName);
        }
        OSMDriverFunction osmdf = new OSMDriverFunction();
        osmdf.importFile(connection, tableReference, file, new EmptyProgressVisitor(), deleteTables);
 
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName, String tableReference) throws FileNotFoundException, SQLException, IOException {
        readOSM(connection, fileName, tableReference, false);
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName) throws FileNotFoundException, SQLException, IOException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        readOSM(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }

}
