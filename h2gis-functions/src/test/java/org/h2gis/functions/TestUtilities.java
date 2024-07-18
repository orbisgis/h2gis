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
package org.h2gis.functions;

import org.h2gis.utilities.JDBCUtilities;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class TestUtilities {

    /**
     * A basic utilities to print the column names and values
     * @param res data to print
     * @throws SQLException
     */
    public static void printValues(ResultSet res) throws SQLException {
        List<String> columns = JDBCUtilities.getColumnNames(res.getMetaData());
        for(String column:columns){
            System.out.println("Column : "+ column + " -  Value : "+ res.getString(column));
        }
    }

    /**
     * A basic utilities to print the column informations
     * @param res data to print
     * @throws SQLException
     */
    public static void printColumns(ResultSet res) throws SQLException {
        ResultSetMetaData metadata = res.getMetaData();
        int cols = metadata.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            System.out.println("Column : " + metadata.getColumnName(i) +
                    "\n Label : " + metadata.getColumnLabel(i) +
                    "\n Type : " + metadata.getColumnType(i) +
                    "\n Type name : " + metadata.getColumnTypeName(i) +
                    "\n Precision : " + metadata.getPrecision(i) +
                    "\n Scale : " + metadata.getScale(i) +
                    "\n Display size : " + metadata.getColumnDisplaySize(i) +
                    "\n Class name : " + metadata.getColumnClassName(i) +
                    "\n Table name : " + metadata.getTableName(i) +
                    "\n Schema name : " + metadata.getSchemaName(i));
        }
    }

}
