/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
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
 * SQL function to write a spatial table to a GeoJSON file.
 *
 * @author Erwan Bocher
 */
public class GeoJsonWrite extends AbstractFunction implements ScalarFunction {

    public GeoJsonWrite() {
        addProperty(PROP_REMARKS, "Export a spatial table to a GeoJSON 1.0 file.\n "
                + "\nGeoJsonWrite(..."
                + "\n Supported arguments :"
                + "\n path of the file, table name"
                + "\n path of the file, table name, true to delete the file if exists");
    }

    @Override
    public String getJavaStaticMethod() {
        return "exportTable";
    }

    /**
     * Read a table and write it into a GEOJSON file.
     *
     * @param connection Active connection
     * @param fileName Shape file name or URI
     * @param tableReference Table name or select query Note : The select query
     * must be enclosed in parenthesis
     * @param deleteFile true to delete output file
     */
    public static void exportTable(Connection connection, String fileName, String tableReference, boolean deleteFile) throws IOException, SQLException {
        GeoJsonDriverFunction geoJsonDriver = new GeoJsonDriverFunction();
        geoJsonDriver.exportTable(connection, tableReference, URIUtilities.fileFromString(fileName),  deleteFile, new EmptyProgressVisitor());
    }

    /**
     * Write the GeoJSON file.
     *
     * @param connection database     * @param fileName input file
     * @param tableReference output table name
     */
    public static void exportTable(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        exportTable(connection, fileName, tableReference, false);
    }
}
