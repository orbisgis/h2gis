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
package org.h2gis.drivers.gpx;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.drivers.utility.FileUtil;

/**
 * SQL Function to copy GPX File data into a Table.
 *
 * @author Erwan Bocher
 */
public class GPXRead extends AbstractFunction implements ScalarFunction {

    public GPXRead() {
        addProperty(PROP_REMARKS, "Read a GPX file and copy the content in the specified tables.");
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
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void readGPX(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        File file = URIUtility.fileFromString(fileName);
        if (FileUtil.isFileImportable(file, "gpx")) {
            GPXDriverFunction gpxdf = new GPXDriverFunction();
            gpxdf.importFile(connection, tableReference, URIUtility.fileFromString(fileName), new EmptyProgressVisitor());
        }
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
        final String name = URIUtility.fileFromString(fileName).getName();
        readGPX(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }
}
