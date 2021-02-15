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

package org.h2gis.functions.io.geojson;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

/**
 * GeoJSON driver to import a GeoJSON file and export a spatial table in a
 * GeoJSON 1.0 file.
 * 
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class GeoJsonDriverFunction implements DriverFunction {

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"geojson", "geojson.gz"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"geojson", "geojson.gz"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("geojson")) {
            return "GeoJSON 1.0";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equals("geojson");
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException{
        return exportTable(connection,tableReference, fileName, null, false, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection,tableReference, fileName, null, deleteFiles, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String encoding, boolean deleteFiles, ProgressVisitor progress) throws SQLException {
        progress  = DriverManager.check(connection, tableReference, fileName, progress);
        GeoJsonWriteDriver geoJsonDriver = new GeoJsonWriteDriver(connection);
        try {
            geoJsonDriver.write(progress, tableReference, fileName, encoding, deleteFiles);
            return new String[]{fileName.getAbsolutePath()};
        }catch (SQLException|IOException ex){
            throw new SQLException(ex);
        }
    }


    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String encoding, ProgressVisitor progress) throws SQLException, IOException{
        return exportTable(connection,tableReference, fileName, encoding, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        DriverManager.check(connection,tableReference,fileName,progress);
        GeoJsonReaderDriver geoJsonReaderDriver = new GeoJsonReaderDriver(connection, fileName, options, deleteTables);
        String outputTable =  geoJsonReaderDriver.read(progress, tableReference);
        if(outputTable==null){
            return null;
        }
        return new String[]{outputTable};
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        return importFile(connection,  tableReference,  fileName, null, false,  progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,  String options,ProgressVisitor progress
                          ) throws SQLException, IOException {
        return importFile(connection,  tableReference,  fileName, options, false,  progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,
                           boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection,  tableReference,  fileName, null, deleteTables,  progress);
    }
}
