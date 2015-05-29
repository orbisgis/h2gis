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
package org.h2gis.drivers.shp;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.URIUtility;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.drivers.utility.FileUtil;

/**
 * SQL Function to copy Shape File data into a Table.
 * @author Nicolas Fortin
 */
public class SHPRead  extends AbstractFunction implements ScalarFunction {
    public SHPRead() {
        addProperty(PROP_REMARKS, "Read a shape file and copy the content in the specified table.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readShape";
    }

    /**
     * Copy data from Shape File into a new table in specified connection.
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path of the SHP file or URI
     * @param forceEncoding Use this encoding instead of DBF file header encoding property.
     */
    public static void readShape(Connection connection, String fileName, String tableReference,String forceEncoding) throws IOException, SQLException {
        File file = URIUtility.fileFromString(fileName);
        if (FileUtil.isFileImportable(file, "shp")) {
            SHPDriverFunction shpDriverFunction = new SHPDriverFunction();
            shpDriverFunction.importFile(connection, TableLocation.parse(tableReference, true).toString(true),
                    file, new EmptyProgressVisitor(), forceEncoding);
        }
    }

    /**
     * Copy data from Shape File into a new table in specified connection.
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path of the SHP file or URI
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void readShape(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        readShape(connection, fileName, tableReference, null);
    }

    /**
     * Copy data from Shape File into a new table in specified connection.
     * The newly created table is given the same name as the filename
     * without the ".shp" extension. If such a table already exists, an
     * exception is thrown.
     *
     * @param connection Active connection
     * @param fileName   File path of the SHP file or URI
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void readShape(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtility.fileFromString(fileName).getName();
        readShape(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }
}
