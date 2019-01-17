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

package org.h2gis.functions.io.gpx;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.utilities.URIUtilities;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL Function to copy GPX File data into a Table.
 *
 * @author Erwan Bocher
 */
public class GPXRead extends AbstractFunction implements ScalarFunction {

    public GPXRead() {
        addProperty(PROP_REMARKS, "Read a GPX file and copy the content in the specified tables."
                + "\nThe user can set a prefix name for all GPX tables and specify if the existing GPX\n"
                + " tables must be dropped.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readGPX";
    }
    
    
    /**
     * Copy data from GPX File into a new table in specified connection.
     *
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path of the SHP file
     * @param deleteTables  true to delete the existing tables
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void readGPX(Connection connection, String fileName, String tableReference, boolean deleteTables) throws IOException, SQLException {
        File file = URIUtilities.fileFromString(fileName);
        if (FileUtil.isFileImportable(file, "gpx")) {
            GPXDriverFunction gpxdf = new GPXDriverFunction();
            gpxdf.importFile(connection, tableReference, URIUtilities.fileFromString(fileName), new EmptyProgressVisitor(), deleteTables);
        }
    }
    

    /**
     * Copy data from GPX File into a new table in specified connection.
     *
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path of the SHP file
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void readGPX(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        readGPX(connection, fileName, tableReference, false);
    }

    /**
     * Copy data from GPX File into a new table in specified connection.
     *
     *
     * @param connection
     * @param fileName
     * @throws IOException
     * @throws SQLException
     */
    public static void readGPX(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        String tableName = name.substring(0, name.lastIndexOf(".")).toUpperCase();
        if (tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            readGPX(connection, fileName, tableName);
        } else {
            throw new SQLException("The file name contains unsupported characters");
        }
    }
}
