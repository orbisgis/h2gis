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
import java.util.Map;
import javax.media.jai.JAI;
import org.h2.api.GeoRaster;
import org.h2.util.RasterUtils;
import org.h2gis.drivers.utility.FileUtil;
import org.h2gis.drivers.utility.PRJUtil;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

/**
 *
 * @author Erwan Bocher
 */
public class ST_WorldFileImageWrite extends AbstractFunction implements ScalarFunction{

    public ST_WorldFileImageWrite(){
        addProperty(PROP_REMARKS, "Export a raster to a world file image.\n"
                + "Supported formats are : \n"
                + "- png with pgw, pngw,\n"
                + "- bmp with bpw or bmpw,\n"
                + "- gif with gfw or gifw,\n"
                + "- jpeg with jpw, jgw, jpgw or jpegw,\n"
                + "- jpg with jpw, jgw, jpgw or jpegw,\n"
                + "- tif with tfw or tifw,\n"
                + "- tiff with tfw or tiffw.\n"
                + "and wld for all listed formats");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "worldFileImageWriter";
    }
    
    /**
     * 
     * @param connection
     * @param geoRaster
     * @param fileName 
     * @throws java.sql.SQLException 
     * @throws java.io.IOException 
     */
    public static void worldFileImageWriter(Connection connection, String fileName, GeoRaster geoRaster) throws SQLException, IOException {
        if (geoRaster != null) {
            File inputFile = URIUtility.fileFromString(fileName);

            String filePath = inputFile.getPath();
            final int dotIndex = filePath.lastIndexOf('.');
            String fileNameExtension = filePath.substring(dotIndex + 1).toLowerCase();
            String filePathWithoutExtension = filePath.substring(0, dotIndex + 1);
            
            String[] worldFileExtensions = WorldFileImageReader.worldFileExtensions.get(fileNameExtension);

            if(worldFileExtensions==null){
                 throw new IOException("Cannot support this format : " + inputFile.getAbsolutePath());
            }

            JAI.create("filestore", geoRaster, filePath, fileNameExtension);

            RasterUtils.RasterMetaData met = geoRaster.getMetaData();

            
            WorldFileImageWriter.writeWorldFile(met, new File(filePathWithoutExtension + worldFileExtensions[0]));

            PRJUtil.writePRJ(connection, met.srid, new File(filePathWithoutExtension + "prj"));

        } else {
            throw new SQLException("The raster object cannot be null.");
        }
    }
}
