/*
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

package org.h2gis.api;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This function can import/export a file into/from a table. Connection may be on a remote H2/Postgre database.
 * The file can be linked to the database or copied into the database.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public interface DriverFunction {

    /**
     * A linked table is created instantly but work only if the DataBase is local.
     * A copy will transfer Data from the File to the remote/local database and the database content will not be
     * synced with the file.
     */
    enum IMPORT_DRIVER_TYPE { LINK, COPY }

    /**
     * Return the driver import type.
     *
     * @return The driver type. A LINK Driver mean the usage of a TableEngine on H2 DataBase.
     */
    IMPORT_DRIVER_TYPE getImportDriverType();

    /**
     * Return the file extensions that can be loaded by this driver
     *
     * @return file extension ex: ["shp","ply"]
     */
    String[] getImportFormats();

    /**
     * Return the file extensions that can be saved by this driver
     *
     * @return file extension ex: ["shp","ply"]
     */
    String[] getExportFormats();

    /**
     * Return the description of the specified format.
     *
     * @param format Format given through getImportFormats and/or getExportFormats
     *
     * @return The description of this format under the default Locale.
     *          An empty string if the description is not available.
     */
    String getFormatDescription(String format);

    /**
     * Returns true if the file extension is from a spatial file, false otherwise or if the driver does not recognize
     * it.
     *
     * @param extension Extension to check.
     *
     * @return True if the extension is a spatial one, false otherwise.
     */
    boolean isSpatialFormat(String extension);

    /**
     * Export the specified table from the specified connection into the specified file.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to write, if exists it may be replaced
     * @param progress
     *
     * @throws SQLException Table read error
     * @throws IOException File write error
     */
    void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException;

    /**
     * Import the specified file into the specified table in the specified connection.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param progress
     *
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException;
}
