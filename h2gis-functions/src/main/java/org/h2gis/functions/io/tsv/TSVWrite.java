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

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Write a Tab-separated values file
 * @author Erwan Bocher
 */
public class TSVWrite extends AbstractFunction implements ScalarFunction {

    public TSVWrite() {
        addProperty(PROP_REMARKS, "Write a Tab-separated values file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "writeTSV";
    }

    /**
     * Export a table into a Tab-separated values file
     *
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws SQLException
     * @throws IOException
     */
    public static void writeTSV(Connection connection, String fileName, String tableReference) throws SQLException, IOException {
        writeTSV(connection, fileName, tableReference, null);
    }
    
    /**
     * Export a table into a Tab-separated values file
     *
     * @param connection
     * @param fileName
     * @param tableReference
     * @param encoding
     * @throws SQLException
     * @throws IOException
     */
    public static void writeTSV(Connection connection, String fileName, String tableReference, String encoding) throws SQLException, IOException {       
        TSVDriverFunction tSVDriverFunction = new TSVDriverFunction();
        tSVDriverFunction.exportTable(connection, tableReference, URIUtilities.fileFromString(fileName), new EmptyProgressVisitor(), encoding);
    }

}
