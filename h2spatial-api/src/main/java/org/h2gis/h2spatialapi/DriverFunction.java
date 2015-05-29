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
package org.h2gis.h2spatialapi;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This function can import/export a file into/from a table. Connection may be on a remote H2/Postgre database
 * @author Nicolas Fortin
 */
public interface DriverFunction {

    /**
     * A linked table is created instantly but work only if the DataBase is local.
     * A copy will transfer Data from the File to the remote/local database and the database content will not be synced with the file.
     */
    enum IMPORT_DRIVER_TYPE { LINK, COPY };

    /**
     * @return The driver type. A LINK Driver mean the usage of a TableEngine on H2 DataBase.
     */
    IMPORT_DRIVER_TYPE getImportDriverType();

    /**
     * Get the file extensions that can be loaded by this driver
     * @return file extension ex: ["shp","ply"]
     */
    String[] getImportFormats();

    /**
     * Get the file extensions that can be saved by this driver
     * @return file extension ex: ["shp","ply"]
     */
    String[] getExportFormats();

    /**
     * @param format Format given through getImportFormats and/or getExportFormats
     * @return The description of this format under the default Locale.An empty string if the description is not available.
     */
    String getFormatDescription(String format);

    /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to write, if exists it may be replaced
     * @throws SQLException Table read error
     * @throws IOException File write error
     */
    void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException;

    /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException;
}
