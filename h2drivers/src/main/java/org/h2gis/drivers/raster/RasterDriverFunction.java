/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.drivers.raster;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 * Read a georaster world file and convert it into a RASTER datatype.
 * 
 * @author Erwan Bocher
 */
public class RasterDriverFunction implements DriverFunction{

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {   
        return new String[]{"png"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[0];
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("png")) {
            return "PNG file with its WorldFile pgw";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equalsIgnoreCase("png");
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        RasterWorldFileReader rasterWorldFileReader = new RasterWorldFileReader();
        rasterWorldFileReader.read(fileName, tableReference, connection, progress);
    }
    
}
