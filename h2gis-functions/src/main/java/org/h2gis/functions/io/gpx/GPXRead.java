/**
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

package org.h2gis.functions.io.gpx;

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
 * SQL Function to copy GPX File data into a Table.
 *
 * @author Erwan Bocher
 */
public class GPXRead extends AbstractFunction implements ScalarFunction {

    public GPXRead() {
        addProperty(PROP_REMARKS, "Read a GPX file and copy the content in the specified tables."
                + "\nThe user can set a prefix name for all GPX tables and specify if the existing GPX\n"
                + " tables must be dropped."+
                "\n GPXRead(..."+
                "\n Supported arguments :" +
                "\n path of the file" +
                "\n path of the file, table name"+
                "\n path of the file, table name, true to delete the table name"+
                "\n path of the file, table name, encoding chartset"+
                "\n path of the file, table name, encoding chartset, true to delete the table name");
    }

    @Override
    public String getJavaStaticMethod() {
        return "importTable";
    }



    public static void importTable(Connection connection, String fileName, Value option) throws SQLException, IOException {
        String tableReference =null;
        boolean deleteTable =  false;
        if(option instanceof ValueBoolean){
            deleteTable = option.getBoolean();
            final String name = URIUtilities.fileFromString(fileName).getName();
            tableReference = name.substring(0, name.lastIndexOf(".")).replace(".", "_").toUpperCase();
            if (!tableReference.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                throw new SQLException("The file name contains unsupported characters");
            }
        }else if (option instanceof ValueVarchar){
            tableReference = option.getString();
        }else if (!(option instanceof ValueNull)){
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        importTable( connection,  fileName,  tableReference,null,  deleteTable);
    }

    public static void importTable(Connection connection, String fileName, String tableReference, Value option) throws SQLException, IOException {
        String encoding =null;
        boolean deleteTable =  false;
        if(option instanceof ValueBoolean){
            deleteTable = option.getBoolean();
        }else if (option instanceof ValueVarchar){
            encoding = option.getString();
        }else if (!(option instanceof ValueNull)){
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        importTable(connection,  fileName,  tableReference,encoding,  deleteTable);
    }

    /**
     * Copy data from GPX File into a new table in specified connection.
     *
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path of the SHP file
     * @param encoding  charset encoding
     * @param deleteTables  true to delete the existing tables
     */
    public static void importTable(Connection connection, String fileName, String tableReference, String encoding, boolean deleteTables) throws IOException, SQLException {
        GPXDriverFunction gpxdf = new GPXDriverFunction();
        gpxdf.importFile(connection, tableReference, URIUtilities.fileFromString(fileName), encoding, deleteTables, new EmptyProgressVisitor());
    }



    /**
     * Copy data from GPX File into a new table in specified connection.
     *
     *
     * @param connection database connection
     * @param fileName input file
     */
    public static void importTable(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        String tableName = name.substring(0, name.lastIndexOf(".")).replace(".", "_").toUpperCase();
        if (tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            importTable( connection,  fileName,  tableName, null,  false);
        } else {
            throw new SQLException("The file name contains unsupported characters");
        }
    }
}
