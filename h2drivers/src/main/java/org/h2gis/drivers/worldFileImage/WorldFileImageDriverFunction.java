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

package org.h2gis.drivers.worldFileImage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 * Read an image world file and convert it into a RASTER datatype.
 * 
 * @author Erwan Bocher
 */
public class WorldFileImageDriverFunction implements DriverFunction{

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {   
        return new String[]{"png", "bmp", "gif","jpeg","jpg", "tif", "tiff"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"png", "bmp", "gif","jpeg","jpg", "tif", "tiff"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("png")) {
            return "PNG format with pgw, pngw or wld world file.";
        } 
        else if (format.equalsIgnoreCase("bmp")) {
            return "BMP format with bpw, bmpw or wld world file.";
        } 
        else if (format.equalsIgnoreCase("gif")) {
            return "GIF format with gfw, gifw or wld world file.";
        }
        else if (format.equalsIgnoreCase("jpg")) {
            return "JPG format with jpw, jgw, jpgw or wld world file.";
        }
        else if (format.equalsIgnoreCase("jpeg")) {
            return "JPEG format with jpw, jgw, jpgw, jpegw or wld world file.";
        }
        else if (format.equalsIgnoreCase("tif")) {
            return "TIF format with tfw, tifw or wld world file.";
        }
        else if (format.equalsIgnoreCase("tiff")) {
            return "TIFF format with tfw, tiffw or wld world file.";
        }
        else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        if (extension.equalsIgnoreCase("png")) {
            return true;
        } else if (extension.equalsIgnoreCase("jpeg")) {
            return true;
        } else if (extension.equalsIgnoreCase("jpg")) {
            return true;
        } else if (extension.equalsIgnoreCase("bmp")) {
            return true;
        } else if (extension.equalsIgnoreCase("gif")) {
            return true;
        } else if (extension.equalsIgnoreCase("tif")) {
            return true;
        } else return extension.equalsIgnoreCase("tiff");
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        WorldFileImageWriter worldFileImageWriter = new WorldFileImageWriter();
        worldFileImageWriter.write(connection, tableReference, fileName, progress);                
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        if (fileName.exists()) {
            WorldFileImageReader worldFileImageReader = WorldFileImageReader.fetch(connection, fileName);
            worldFileImageReader.read(tableReference, connection, progress);
        } else {
            throw new IllegalArgumentException("The file " + fileName + " doesn't exist.");
        }
    }
    
}
