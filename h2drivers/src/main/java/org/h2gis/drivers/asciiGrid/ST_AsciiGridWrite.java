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

import com.sun.media.jai.operator.ImageWriteDescriptor;
import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import org.h2.api.GeoRaster;
import org.h2.util.RasterUtils;
import org.h2gis.drivers.utility.PRJUtil;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

/**
 *
 * @author Erwan Bocher
 */
public class ST_AsciiGridWrite extends AbstractFunction implements ScalarFunction {

    public ST_AsciiGridWrite() {
        addProperty(PROP_REMARKS, "Export a raster table into a Arc/Info ASCII or GRASS ASCII Grid file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "asciiGridWrite";
    }

    /**
     *
     * @param connection
     * @param fileName
     * @param geoRaster
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static void asciiGridWrite(Connection connection, String fileName, GeoRaster geoRaster) throws IOException, SQLException {
        if (geoRaster != null) {
           
            RasterUtils.RasterMetaData metadata = geoRaster.getMetaData();
            if (geoRaster.getMetaData().numBands != 1) {
                throw new SQLException("ST_AsciiGridWrite accept only raster with one band");
            }
        
            File inputFile = URIUtility.fileFromString(fileName);

            String filePath = inputFile.getPath();
            final int dotIndex = filePath.lastIndexOf('.');
            String fileNameExtension = filePath.substring(dotIndex + 1).toLowerCase();
            String filePathWithoutExtension = filePath.substring(0, dotIndex + 1);

            boolean isGrass;
            if (fileNameExtension.equals("asc")) {
                isGrass = false;
            } else if (fileNameExtension.equals("arx")) {
                isGrass = true;
            } else {
                throw new IOException("Cannot support this format : " + inputFile.getAbsolutePath());
            }

            final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
                    "ImageWrite");
            pbjImageWrite.setParameter("Output", inputFile);
            pbjImageWrite.addSource(geoRaster);

            AsciiGridsImageMetadata gridMetadata = new AsciiGridsImageMetadata(
                    metadata.width, metadata.height, metadata.scaleX, metadata.scaleY,
                    metadata.ipX, metadata.ipY,
                    false, isGrass, metadata.bands[0].noDataValue);
            pbjImageWrite.setParameter("ImageMetadata", gridMetadata);

            final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
            final ImageWriter writer = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
            writer.dispose();

            PRJUtil.writePRJ(connection, metadata.srid, new File(filePathWithoutExtension + "prj"));

        } else {
            throw new SQLException("The raster object cannot be null.");
        }
    }
}
