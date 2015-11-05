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
package org.h2gis.drivers.asciiGrid;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.drivers.utility.FileUtil;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 * A driver to import and export Ascii grid image
 * 
 * @author Erwan Bocher
 */
public class AsciiGridDriverFunction implements DriverFunction{

    
    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"asc", "arx"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"asc", "arx"};
    }

    @Override
    public String getFormatDescription(String format) {
    if (format.equalsIgnoreCase("asc")) {
            return "Arc/Info ASCII Grid format.";
        } 
        else if (format.equalsIgnoreCase("arx")) {
            return "GRASS ASCII Grid format.";
        }        
        else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        if (extension.equalsIgnoreCase("asc")) {
            return true;
        } else {
            return extension.equalsIgnoreCase("arx");
        }
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        if (fileName.exists()) {
            AsciiGridReader asciiGridReader =  AsciiGridReader.fetch(connection, fileName);
            asciiGridReader.read(tableReference, connection, progress);
        } else {
            throw new IllegalArgumentException("The file " + fileName + " doesn't exist.");
        }
    }
    
}
