/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.kml;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.DriverManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, false, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, deleteFiles, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection,tableReference,fileName,progress);
        KMLWriterDriver kMLWriter = new KMLWriterDriver(connection, fileName, options, deleteFiles);
        try {
            kMLWriter.write(tableReference, progress);
            return new String[]{fileName.getAbsolutePath()};
        }catch (SQLException | IOException ex){
            throw new SQLException(ex);
        }
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String encoding, ProgressVisitor progress
    ) throws SQLException, IOException {
       return exportTable(connection, tableReference, fileName, encoding, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, ProgressVisitor progress
    ) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, boolean deleteTables, ProgressVisitor progress
    ) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }
}
