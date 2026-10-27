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

package org.h2gis.functions.io.shp;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.io.file_table.FileTableReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Reads a shapefile as a table, without importing it:
 *
 * <pre>
 * SELECT * FROM SHPTable('/data/building.shp');
 * SELECT AVG(HEIGHT) FROM SHPTable('/data/building.shp', 'HEIGHT');
 * SELECT * FROM SHPTable('/data/building.shp', '', 'CP1252');
 * </pre>
 *
 * @author Erwan Bocher, CNRS
 */
public class SHPTable extends AbstractFunction implements ScalarFunction {

    public SHPTable() {
        addProperty(PROP_REMARKS, "Read a shapefile as a table, without importing it.\n"
                + "SELECT * FROM SHPTable('/data/building.shp');\n"
                + "An optional second argument lists the columns to read:\n"
                + "SELECT * FROM SHPTable('/data/building.shp', 'THE_GEOM, HEIGHT');\n"
                + "An optional third argument gives the encoding of the file, when it is not given by the file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readTable";
    }

    /**
     * @param connection database connection
     * @param fileName path of the file
     * @return the rows of the file
     */
    public static ResultSet readTable(Connection connection, String fileName) throws SQLException {
        return readTable(connection, fileName, null, null);
    }

    /**
     * @param connection database connection
     * @param fileName path of the file
     * @param columns names of the columns to read, separated by commas, null or empty for all
     * @return the columns of the rows, in the order of the list
     */
    public static ResultSet readTable(Connection connection, String fileName, String columns) throws SQLException {
        return readTable(connection, fileName, columns, null);
    }

    /**
     * @param connection database connection
     * @param fileName path of the file
     * @param columns names of the columns to read, separated by commas, null or empty for all
     * @param encoding encoding of the file (e.g. UTF-8, CP1252), null to use the one of the file
     * @return the columns of the rows, in the order of the list
     */
    public static ResultSet readTable(Connection connection, String fileName, String columns, String encoding) throws SQLException {
        return FileTableReader.readTable(connection, new SHPEngine(), fileName, "shp", columns, encoding);
    }
}