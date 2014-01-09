/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.kml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;

/**
 * A driver to export spatial table to kml 2.2 file.
 *
 * @author Erwan Bocher
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
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
        ProgressVisitor copyProgress = progress.subProcess(recordCount);
        KMLWriter kMLWriter = new KMLWriter(connection, tableReference, fileName);
        kMLWriter.write(copyProgress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
