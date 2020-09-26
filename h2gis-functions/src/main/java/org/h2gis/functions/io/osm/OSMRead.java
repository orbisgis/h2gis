/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.osm;

import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueNull;
import org.h2.value.ValueVarchar;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL Function to copy OSM File data into a set of tables.
 *
 * @author Erwan Bocher
 */
public class OSMRead extends AbstractFunction implements ScalarFunction {

    public OSMRead() {
        addProperty(PROP_REMARKS, "Read a OSM file and copy the content in the specified tables.\n"
                + "The user can set a prefix name for all OSM tables and specify if the existing OSM\n"
                + " tables must be dropped."
                + "\n OSMRead(..."
                + "\n Supported arguments :"
                + "\n path of the file"
                + "\n path of the file, table name"
                + "\n path of the file, true for delete the table with the same file name"
                + "\n path of the file, table name, encoding chartset"
                + "\n path of the file, table name, encoding chartset, true to delete the table name");
    }

    @Override
    public String getJavaStaticMethod() {
        return "importTable";
    }

    /**
     *
     * @param connection
     * @param fileName
     * @param tableReference
     * @param option true to delete the existing tables or set a chartset
     * encoding
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public static void importTable(Connection connection, String fileName, String tableReference, Value option) throws FileNotFoundException, SQLException, IOException {
        String encoding = null;
        boolean deleteTable = false;
        if (option instanceof ValueBoolean) {
            deleteTable = option.getBoolean();
        } else if (option instanceof ValueVarchar) {
            encoding = option.getString();
        } else if (!(option instanceof ValueNull)) {
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        importTable(connection, fileName, tableReference, encoding, deleteTable);
    }

    /**
     *
     * @param connection
     * @param fileName
     * @param option
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public static void importTable(Connection connection, String fileName, Value option) throws FileNotFoundException, SQLException, IOException {
        String tableReference = null;
        boolean deleteTable = false;
        if (option instanceof ValueBoolean) {
           deleteTable = option.getBoolean();
            final String name = URIUtilities.fileFromString(fileName).getName();
            tableReference = name.substring(0, name.lastIndexOf(".")).replace(".", "_").toUpperCase();
            if (!tableReference.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                throw new SQLException("The file name contains unsupported characters");
            }
        } else if (option instanceof ValueVarchar) {
            tableReference = option.getString();
        } else if (!(option instanceof ValueNull)) {
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        importTable(connection, fileName, tableReference, null, deleteTable);
    }

    /**
     *
     * @param connection
     * @param fileName
     * @param tableReference
     * @param encoding
     * @param deleteTables
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public static void importTable(Connection connection, String fileName, String tableReference, String encoding, boolean deleteTables) throws FileNotFoundException, SQLException, IOException {
        OSMDriverFunction osmdf = new OSMDriverFunction();
        osmdf.importFile(connection, tableReference, URIUtilities.fileFromString(fileName), encoding, deleteTables, new EmptyProgressVisitor());
    }

    /**
     *
     * @param connection
     * @param fileName
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public static void importTable(Connection connection, String fileName) throws FileNotFoundException, SQLException, IOException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        String tableName = name.substring(0, name.lastIndexOf(".")).toUpperCase().replace(".", "_");
        if (tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            importTable(connection, fileName, tableName, null, false);
        } else {
            throw new SQLException("The file name contains unsupported characters");
        }
    }

}
