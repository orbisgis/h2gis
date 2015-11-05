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

import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

/**
 * A function to read a Arc/Info ASCII or GRASS ASCII Grid file.
 * 
 * @author Erwan Bocher
 */
public class ST_AsciiGridRead extends AbstractFunction implements ScalarFunction{

    
    public ST_AsciiGridRead(){
        addProperty(PROP_REMARKS, "Import a Arc/Info ASCII or GRASS ASCII Grid file image into a new table");
    }
    
    
    @Override
    public String getJavaStaticMethod() {
        return "asciiGridRead";
    }
    
    /**
     * Copy data from Arc/Info ASCII or GRASS ASCII Grid file into a new table
     * in specified connection.
     *
     * @param connection
     * @param fileName
     * @return
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public static GeoRaster asciiGridRead(Connection connection, String fileName) throws IOException, SQLException {
        File rasterFile = URIUtility.fileFromString(fileName);
        if (rasterFile.exists()) {
            AsciiGridReader asciiGridReader = AsciiGridReader.fetch(connection, rasterFile);

            AsciiGridRaster metadataAscii = asciiGridReader.getAsciiGridRaster();

            RasterUtils.RasterBandMetaData rbmd = new RasterUtils.RasterBandMetaData(metadataAscii.getNoData(), RasterUtils.PixelType.PT_64BF, true, 0);

            RasterUtils.RasterMetaData rmd = new RasterUtils.RasterMetaData(RasterUtils.LAST_WKB_VERSION, 1,
                    metadataAscii.getCellSizeX(), metadataAscii.getCellSizeY(),
                    metadataAscii.getXllCellCoordinate(), metadataAscii.getYllCellCoordinate(),
                    0, 0, asciiGridReader.getSrid(),
                    metadataAscii.getNCols(), metadataAscii.getNRows(),
                    new RasterUtils.RasterBandMetaData[]{rbmd});
            return GeoRasterRenderedImage
                    .create(asciiGridReader.getImage(), rmd);
        } else {
            throw new IllegalArgumentException("The file " + fileName + " doesn't exist.");
        }
    }
    
}
