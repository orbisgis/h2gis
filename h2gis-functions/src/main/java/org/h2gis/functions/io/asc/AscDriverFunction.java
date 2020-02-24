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

package org.h2gis.functions.io.asc;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.utility.PRJUtil;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * GeoJSON driver to import a GeoJSON file and export a spatial table in a
 * GeoJSON 1.0 file.
 * 
 * @author Nicolas Fortin (Université Gustave Eiffel 2020)
 */
public class AscDriverFunction implements DriverFunction {

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"asc"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("asc")) {
            return "ESRI ASCII Raster format";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension != null && extension.equalsIgnoreCase("asc");
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException{

    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress, String encoding) throws SQLException, IOException{

    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        AscReaderDriver ascReaderDriver = new AscReaderDriver();
        importFile(connection, tableReference, fileName, progress, ascReaderDriver);
    }

    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress, AscReaderDriver ascReaderDriver)
            throws SQLException, IOException {
        int srid = 0;
        String filePath = fileName.getAbsolutePath();
        final int dotIndex = filePath.lastIndexOf('.');
        final String fileNamePrefix = filePath.substring(0, dotIndex);
        File prjFile = new File(fileNamePrefix+".prj");
        if(prjFile.exists()) {
            srid = PRJUtil.getSRID(prjFile);
        }
        try(FileInputStream fos = new FileInputStream(fileName)) {
            ascReaderDriver.read(connection, fos, progress, tableReference, srid);
        }
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                           String options) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                           boolean deleteTables) throws SQLException, IOException {

        if(deleteTables) {
            final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
            TableLocation requestedTable = TableLocation.parse(tableReference, isH2);
            Statement stmt = connection.createStatement();
            stmt.execute("DROP TABLE IF EXISTS " + requestedTable);
            stmt.close();
        }

        importFile(connection, tableReference, fileName, progress);
    }
}
