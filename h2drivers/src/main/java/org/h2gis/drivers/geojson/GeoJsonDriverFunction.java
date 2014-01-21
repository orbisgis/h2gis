/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.drivers.geojson;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;

/**
 *
 * @author Erwan Bocher
 */
public class GeoJsonDriverFunction implements DriverFunction {

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"geojson"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"geojson"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("geojson")) {
            return "GeoJson 1.0";
        } else {
            return "";
        }
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
        ProgressVisitor copyProgress = progress.subProcess(recordCount);
        GeoJsonWriteDriver geoJsonDriver = new GeoJsonWriteDriver(connection, tableReference, fileName);
        geoJsonDriver.write(copyProgress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        GeoJsonReaderDriver geoJsonReaderDriver = new GeoJsonReaderDriver(connection, tableReference, fileName);
        geoJsonReaderDriver.read(progress);
    }
}
