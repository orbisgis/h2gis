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

package org.h2gis.functions.io.fgb;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.functions.io.file_table.FileTableReader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Reads a FlatGeobuf file as a table, without importing it:
 *
 * <pre>
 * SELECT * FROM FGBTable('/data/building.fgb');
 * SELECT AVG(HEIGHT) FROM FGBTable('/data/building.fgb', 'HEIGHT');
 * </pre>
 *
 * @author Erwan Bocher, CNRS
 */
public class FGBTable extends AbstractFunction implements ScalarFunction {

    public FGBTable() {
        addProperty(PROP_REMARKS, "Read a FlatGeobuf file as a table, without importing it.\n"
                + "SELECT * FROM FGBTable('/data/file.fgb');\n"
                + "An optional second argument lists the columns to read:\n"
                + "SELECT * FROM FGBTable('/data/file.fgb', 'THE_GEOM, HEIGHT');");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readTable";
    }

    /**
     * @param connection database connection
     * @param fileName path of the file
     * @return the features of the file, the geometry in the THE_GEOM column
     */
    public static ResultSet readTable(Connection connection, String fileName) throws SQLException {
        return readTable(connection, fileName, null);
    }

    /**
     * @param connection database connection
     * @param fileName path of the file
     * @param columns names of the columns to read, separated by commas, null or empty for all
     * @return the columns of the features, in the order of the list
     */
    public static ResultSet readTable(Connection connection, String fileName, String columns) throws SQLException {
        return FileTableReader.readTable(connection, new SequentialFGBEngine(), fileName, "fgb", columns, null);
    }

    /**
     * The rows are read in order: no need to keep the address of each row.
     */
    private static final class SequentialFGBEngine extends FGBEngine {
        @Override
        protected FGBDriver createDriver(File filePath, List<String> args) throws IOException {
            FGBDriver driver = super.createDriver(filePath, args);
            driver.setCacheRowAddress(false);
            return driver;
        }
    }
}