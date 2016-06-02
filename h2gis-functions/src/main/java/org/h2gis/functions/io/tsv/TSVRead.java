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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

/**
 * Read a Tab-separated values file
 * @author Erwan Bocher
 */
public class TSVRead  extends AbstractFunction implements ScalarFunction{

    public TSVRead() {
        addProperty(PROP_REMARKS, "Read a Tab-separated values file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readTSV";
    }

    /**
     * Copy data from TSV File into a new table in specified connection.
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void readTSV(Connection connection, String fileName, String tableReference) throws SQLException, FileNotFoundException, IOException {
        File file = URIUtilities.fileFromString(fileName);
        if (FileUtil.isFileImportable(file, "tsv")) {
            TSVDriverFunction tsvDriver = new TSVDriverFunction();
            tsvDriver.importFile(connection, tableReference, file, new EmptyProgressVisitor());
        }
    }

    /**
     * Copy data from TSV File into a new table in specified connection.
     * @param connection
     * @param fileName
     * @throws IOException
     * @throws SQLException 
     */
    public static void readTSV(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtilities.fileFromString(fileName).getName();
        readTSV(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }
}
