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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;

/**
 * A driver to export spatial table to kml 2.2 file.
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class KMLDriverFunction implements DriverFunction {

    @Override
    public String[] getImportFormats() {
        return new String[0];
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"kml", "kmz"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("kml")) {
            return "KML 2.2";
        } else if (format.equalsIgnoreCase("kmz")) {
            return "KMZ 2.2";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equalsIgnoreCase("kml") || extension.equalsIgnoreCase("kmz");
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        KMLWriterDriver kMLWriter = new KMLWriterDriver(connection, tableReference, fileName);
        kMLWriter.write(progress);
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                            String options) throws SQLException, IOException {
        exportTable(connection, tableReference, fileName, progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
       throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                           String options) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                           boolean deleteTables) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }
}
