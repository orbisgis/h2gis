/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.api;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This function can import/export a file into/from a table. Connection may be
 * on a remote H2/Postgres database. The file can be linked to the database or
 * copied into the database.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public interface DriverFunction {


    /**
     * A linked table is created instantly but work only if the DataBase is
     * local. A copy will transfer Data from the File to the remote/local
     * database and the database content will not be synced with the file.
     */
    enum IMPORT_DRIVER_TYPE {
        LINK, COPY
    }

    /**
     * Return the driver import type.
     *
     * @return The driver type. A LINK Driver mean the usage of a TableEngine on
     * H2 DataBase.
     */
    IMPORT_DRIVER_TYPE getImportDriverType();

    /**
     * Return the file extensions that can be loaded by this driver
     *
     * @return File extension ex: ["shp","ply"].
     */
    String[] getImportFormats();

    /**
     * Return the file extensions that can be saved by this driver
     *
     * @return File extension ex: ["shp","ply"].
     */
    String[] getExportFormats();

    /**
     * Return the description of the specified format.
     *
     * @param format Format given through getImportFormats and/or
     * getExportFormats
     * @return The description of this format under the default Locale. An empty
     * string if the description is not available.
     */
    String getFormatDescription(String format);

    /**
     * Returns true if the file extension is from a spatial file, false
     * otherwise or if the driver does not recognize it.
     *
     * @param extension Extension to check.
     * @return True if the extension is a spatial one, false otherwise.
     */
    boolean isSpatialFormat(String extension);

    /**
     * Export the specified table from the specified connection into the
     * specified file.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to write, if exists it may be replaced.
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table read error.
     * @throws IOException File write error.
     * @return the path of the file(s) used to store the table
     */
    String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException;

    /**
     * Export the specified table from the specified connection into the
     * specified file.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to write, if exists it may be replaced.
     * @param deleteFiles True to delete the files if exist
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table read error.
     * @throws IOException File write error.
     * @return the path of the file(s) used to store the table
     */
    String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress)
            throws SQLException, IOException;

    /**
     * Export the specified table from the specified connection into the
     * specified file.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to write, if exists it may be replaced.
     * @param options Options to use for the export like encoding, separator ...
     * The options are different from a format to another.
     * @param deleteFiles True to delete the files if exist
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table read error.
     * @throws IOException File write error.     *
     * @return the path of the file(s) used to store the table
     */
    String[] exportTable(Connection connection, String tableReference, File fileName,
            String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException;

    /**
     * Export the specified table from the specified connection into the
     * specified file.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to write, if exists it may be replaced.
     * @param progress Progress visitor following the execution.
     * @param options Options to use for the export like encoding, separator ...
     * The options are different from a format to another.
     * @throws SQLException Table read error.
     * @throws IOException File write error.     *
     * @return the path of the file(s) used to store the table
     */
    String[] exportTable(Connection connection, String tableReference, File fileName,
            String options, ProgressVisitor progress) throws SQLException, IOException;

    /**
     * Import the specified file into the specified table in the specified
     * connection.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to read.
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table write error.
     * @throws IOException File read error.
     * @return The name of table formatted according the database rules
     * if the the user set a subquery e.g : "(SELECT * FROM H2GIS LIMIT 1)" it will return the query
     */
    String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException;

    /**
     * Import the specified file into the specified table in the specified
     * connection.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to read.
     * @param options Options to use for the export like encoding, separator ...
     * The options are different from a format to another.
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table write error.
     * @throws IOException File read error.
     * @return The name of table formatted according the database rules
     * if the the user set a subquery e.g : "(SELECT * FROM H2GIS LIMIT 1)" it will return the query
     */
    String[] importFile(Connection connection, String tableReference, File fileName, String options, ProgressVisitor progress)
            throws SQLException, IOException;

    /**
     * Import the specified file into the specified table in the specified
     * connection.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to read.
     * @param deleteTables True if the existing table used for the import should
     * be deleted, false otherwise.
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table write error.
     * @throws IOException File read error.
     * @return The name of table formatted according the database rules
     * if the the user set a subquery e.g : "(SELECT * FROM H2GIS LIMIT 1)" it will return the query
     */
    String[] importFile(Connection connection, String tableReference, File fileName, boolean deleteTables, ProgressVisitor progress
    ) throws SQLException, IOException;

    /**
     * Import the specified file into the specified table in the specified
     * connection.
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference.
     * @param fileName File path to read.
     * @param options Options to use for the export like encoding, separator ...
     * The options are different from a format to another.
     * @param deleteTables True if the existing table used for the import should
     * be deleted, false otherwise.
     * @param progress Progress visitor following the execution.
     * @throws SQLException Table write error.
     * @throws IOException File read error.
     * @return The name of table formatted according the database rules
     * if the the user set a subquery e.g : "(SELECT * FROM H2GIS LIMIT 1)" it will return the query
     */
    String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress
    ) throws SQLException, IOException;

}
