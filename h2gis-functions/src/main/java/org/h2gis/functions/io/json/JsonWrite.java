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
package org.h2gis.functions.io.json;

import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueNull;
import org.h2.value.ValueVarchar;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL function to write a table to a JSON file.
 * 
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (Lab-STICC UBS, Chaire GEOTERA, 2020)
 */
public class JsonWrite extends AbstractFunction implements ScalarFunction {

    public JsonWrite() {
        addProperty(PROP_REMARKS, "Export a table to a JSON file." +
                "\nJsonWrite(..." +
                "\n Supported arguments :" +
                "\n path of the file, table name" +
                "\n path of the file, table name, true to delete the file if exists" +
                "\n path of the file, table name, encoding chartset" +
                "\n path of the file, table name, encoding chartset, true to delete the file if exists");
    }

    @Override
    public String getJavaStaticMethod() {
        return "exportTable";
    }

    /**
     * @param connection     Connection to the database.
     * @param fileName       Name of the destination file.
     * @param tableReference Name of the table to export or select query.
     *                       Note : The select query must be enclosed in parenthesis
     * @param encoding       Encoding of the destination file.
     * @param deleteFile     True if the destination files should be deleted, false otherwise.
     * @throws SQLException Exception thrown when an SQL error occurs.
     * @throws IOException  Exception when a file writing error occurs.
     */
    public static void exportTable(Connection connection, String fileName, String tableReference, String encoding,
                                   boolean deleteFile) throws IOException, SQLException {
        JsonDriverFunction jsonDriver = new JsonDriverFunction();
        jsonDriver.exportTable(connection, tableReference, URIUtilities.fileFromString(fileName), encoding, deleteFile,
                new EmptyProgressVisitor());
    }

    /**
     * Write the JSON file.
     *
     * @param connection     Connection to the database.
     * @param fileName       Name of the destination file.
     * @param tableReference Name of the table to export or select query.
     *                       Note : The select query must be enclosed in parenthesis
     * @throws SQLException Exception thrown when an SQL error occurs.
     * @throws IOException  Exception when a file writing error occurs.
     */
    public static void exportTable(Connection connection, String fileName, String tableReference)
            throws SQLException, IOException {
        exportTable(connection, fileName, tableReference, null, false);
    }

    /**
     * Read a table and write it into a json file.
     *
     * @param connection     Connection to the database.
     * @param fileName       Name of the destination file.
     * @param tableReference Name of the table to export or select query.
     *                       Note : The select query must be enclosed in parenthesis
     * @param option         String file encoding charset or boolean value to delete the existing file.
     * @throws SQLException Exception thrown when an SQL error occurs.
     * @throws IOException  Exception when a file writing error occurs.
     */
    public static void exportTable(Connection connection, String fileName, String tableReference, Value option)
            throws SQLException, IOException {
        String encoding = null;
        boolean deleteFiles = false;
        if (option instanceof ValueBoolean) {
            deleteFiles = option.getBoolean();
        } else if (option instanceof ValueVarchar) {
            encoding = option.getString();
        } else if (!(option instanceof ValueNull)) {
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        exportTable(connection, fileName, tableReference, encoding, deleteFiles);
    }
}