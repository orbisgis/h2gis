/**
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

package org.h2gis.functions.io.shp;

import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueNull;
import org.h2.value.ValueVarchar;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.URIUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL Function to copy Shape File data into a Table.
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS
 */
public class SHPRead  extends AbstractFunction implements ScalarFunction {
    public SHPRead() {
        addProperty(PROP_REMARKS, "Read a shape file and copy the content in the specified table."+
                "\n SHPRead(..."+
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

    /**
     * Copy data from Shape File into a new table in specified connection.
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param option Could be string file encoding charset or boolean value to delete the existing table
     * @param fileName File path of the SHP file or URI
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void importTable(Connection connection, String fileName, String tableReference, Value option) throws IOException, SQLException {
        String encoding =null;
        boolean deleteTable =  false;
        if(option instanceof ValueBoolean){
            deleteTable = option.getBoolean();
        }else if (option instanceof ValueVarchar){
            encoding = option.getString();
        }else if (!(option instanceof ValueNull)){
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        importTable(connection, fileName, tableReference, encoding, deleteTable);
    }

    /**
     * Copy data from Shape File into a new table in specified connection.
     * @param connection Active connection
     * @param tableReference [[catalog.]schema.]table reference
     * @param forceEncoding Use this encoding instead of DBF file header encoding property.
     * @param fileName File path of the SHP file or URI
     * @param deleteTables delete existing tables
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void importTable(Connection connection, String fileName, String tableReference,String forceEncoding, boolean deleteTables) throws IOException, SQLException {
        File file = URIUtilities.fileFromString(fileName);
        SHPDriverFunction shpDriverFunction = new SHPDriverFunction();
        shpDriverFunction.importFile(connection, tableReference,
                file,  forceEncoding,deleteTables, new EmptyProgressVisitor());
    }


    /**
     * Copy data from Shape File into a new table in specified connection.
     * @param connection Active connection
     * @param fileName File path of the SHP file or URI
     * @param option [[catalog.]schema.]table reference
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void importTable(Connection connection, String fileName, Value option) throws IOException, SQLException {
        String tableReference =null;
        boolean deleteTable =  false;
        if(option instanceof ValueBoolean){
            deleteTable = option.getBoolean();
            String name = URIUtilities.fileFromString(fileName).getName();
            String tableName = name.substring(0, name.lastIndexOf(".")).replace(".", "_").toUpperCase();
            if (tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                importTable(connection, fileName, tableName, null, deleteTable);
            } else {
                throw new SQLException("The file name contains unsupported characters");
            }
        }else if (option instanceof ValueVarchar){
            tableReference = option.getString();
            importTable(connection, fileName, tableReference, null, false);
        }else if (!(option instanceof ValueNull)){
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
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
    public static void importTable(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        String tableName = name.substring(0, name.lastIndexOf(".")).toUpperCase();
        if (tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            importTable(connection, fileName, tableName, null, false);
        } else {
            throw new SQLException("The file name contains unsupported characters");
        }
    }
}
