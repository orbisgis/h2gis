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

package org.h2gis.functions.io.kml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

/**
 * SQL Function to export a spatial table to a KML file.
 *
 * @author Erwan Bocher
 */
public class KMLWrite extends AbstractFunction implements ScalarFunction {

    public KMLWrite() {
        addProperty(PROP_REMARKS, "Export a spatial table to a KML or KMZ file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "writeKML";
    }

    /**
     * This method is used to write a spatial table into a KML file
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws SQLException
     * @throws IOException 
     */
    public static void writeKML(Connection connection, String fileName, String tableReference) throws SQLException, IOException {
        KMLDriverFunction kMLDriverFunction = new KMLDriverFunction();
        kMLDriverFunction.exportTable(connection, tableReference, URIUtilities.fileFromString(fileName), new EmptyProgressVisitor());
    }
}
