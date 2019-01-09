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

package org.h2gis.functions.io.geojson;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL function to read a GeoJSON file an creates the corresponding spatial
 * table.
 *
 * @author Erwan Bocher
 */
public class GeoJsonRead extends AbstractFunction implements ScalarFunction {

    public GeoJsonRead() {
        addProperty(PROP_REMARKS, "Import a GeoJSON 1.0 file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readGeoJson";
    }
    
    /**
     * 
     * @param connection
     * @param fileName
     * @throws IOException
     * @throws SQLException 
     */
    public static void readGeoJson(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        String tableName = name.substring(0, name.lastIndexOf(".")).toUpperCase();
        if (tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            readGeoJson(connection, fileName, tableName);
        } else {
            throw new SQLException("The file name contains unsupported characters");
        }
    }

    /**
     * Read the GeoJSON file.
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws IOException
     * @throws SQLException 
     */
    public static void readGeoJson(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        GeoJsonDriverFunction gjdf = new GeoJsonDriverFunction();
        gjdf.importFile(connection, tableReference, URIUtilities.fileFromString(fileName), new EmptyProgressVisitor());
    }
}
