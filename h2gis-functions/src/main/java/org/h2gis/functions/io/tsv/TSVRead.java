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


package org.h2gis.functions.io.tsv;

import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueNull;
import org.h2.value.ValueVarchar;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.utilities.URIUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Read a Tab-separated values file
 * @author Erwan Bocher
 */
public class TSVRead  extends AbstractFunction implements ScalarFunction{

    public TSVRead() {
        addProperty(PROP_REMARKS, "Read a Tab-separated values file." +
                "\n TSVRead(..."+
                "\n Supported arguments :" +
                "\n path of the file" +
                "\n path of the file, table name"+
                "\n path of the file, true for delete the table with the same file name"+
                "\n path of the file, table name, true to delete the table name"+
                "\n path of the file, table name, true to delete the table name"+
                "\n path of the file, table name, encoding chartset"+
                "\n path of the file, table name, encoding chartset, true to delete the table name");
    }

    @Override
    public String getJavaStaticMethod() {
        return "importTable";
    }

    /**
     * Copy data from TSV File into a new table in specified connection.
     * @param connection
     * @param fileName
     * @param option  table name or true to delete it
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void importTable(Connection connection, String fileName, Value option) throws SQLException, FileNotFoundException, IOException {
        String tableReference =null;
        boolean deleteTable =  false;
        if(option instanceof ValueBoolean){
            deleteTable = option.getBoolean();
        }else if (option instanceof ValueVarchar){
            tableReference = option.getString();
        }else if (!(option instanceof ValueNull)){
            throw new SQLException("Supported optional parameter is boolean or varchar");
        }
        importTable( connection,  fileName,  tableReference,null,  deleteTable);
    }

    public static void importTable(Connection connection, String fileName, String tableReference, Value option) throws SQLException, FileNotFoundException, IOException {
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
     *
     * @param connection
     * @param fileName
     * @param tableReference
     * @param encoding
     * @param deleteTable
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void importTable(Connection connection, String fileName, String tableReference, String encoding, boolean deleteTable) throws SQLException, FileNotFoundException, IOException {
        TSVDriverFunction tsvDriver = new TSVDriverFunction();
        tsvDriver.importFile(connection, tableReference, URIUtilities.fileFromString(fileName),encoding,deleteTable, new EmptyProgressVisitor());
    }

    /**
     * Copy data from TSV File into a new table in specified connection.
     * @param connection
     * @param fileName
     * @throws IOException
     * @throws SQLException 
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
