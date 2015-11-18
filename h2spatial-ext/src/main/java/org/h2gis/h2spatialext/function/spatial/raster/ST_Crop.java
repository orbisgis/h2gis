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

package org.h2gis.h2spatialext.function.spatial.raster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.awt.Rectangle;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.RasterUtils.RasterMetaData;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Crops the pixel values of a raster to the envelope of the geometry.
 *
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class ST_Crop extends DeterministicScalarFunction{

    public ST_Crop() {
        addProperty(PROP_REMARKS, "Crops the pixel values of a raster to the envelope of the geometry.");
    }

    
    @Override
    public String getJavaStaticMethod() {
        return "crop";
    }
    
    public static GeoRaster crop(GeoRaster geoRaster, Geometry geom) throws IOException{
        if(geom == null){
            throw new IllegalArgumentException("A geometry must be specified");
        }
        
        Envelope inputCropEnv = geom.getEnvelopeInternal();
   
        if ((inputCropEnv.getWidth() == 0) || (inputCropEnv.getHeight() == 0)) {
            throw new IllegalArgumentException("The envelope of the input geometry must be greater than zero");
        }        
        if(geoRaster==null){
            return null;
        }

        RasterMetaData metaData = geoRaster.getMetaData();
        
      
        // Compute pixel envelope source
        // As raster can be transformed, all corners are retrieved
        int[] p0 = metaData.getPixelFromCoordinate(new Coordinate(inputCropEnv.getMinX(), inputCropEnv.getMinY()));
        int[] p1 = metaData.getPixelFromCoordinate(new Coordinate(inputCropEnv.getMaxX(), inputCropEnv.getMinY()));
        int[] p2 = metaData.getPixelFromCoordinate(new Coordinate(inputCropEnv.getMaxX(), inputCropEnv.getMaxY()));
        int[] p3 = metaData.getPixelFromCoordinate(new Coordinate(inputCropEnv.getMinX(), inputCropEnv.getMaxY()));
        int minX = Math.max(0, Math.min(Math.min(Math.min(p0[0], p1[0]), p2[0]), p3[0]));
        int maxX = Math.min(metaData.width, Math.max(Math.max(Math.max(p0[0], p1[0]), p2[0]), p3[0]));
        int minY = Math.max(0, Math.min(Math.min(Math.min(p0[1], p1[1]), p2[1]), p3[1]));
        int maxY = Math.min(metaData.height, Math.max(Math.max(Math.max(p0[1], p1[1]), p2[1]), p3[1]));
        Rectangle envPixSource = new Rectangle(minX, minY, maxX - minX, maxY - minY);

        int newWidth = maxX - minX;
        int newHeight =maxY - minY;
        
        if (!(envPixSource.width > 0 && envPixSource.height > 0)) {
            return null;
        }        
        Coordinate upCorner = metaData.getPixelCoordinate(minX, minY);
        
        RasterMetaData outputMetadata = new RasterUtils.RasterMetaData(RasterUtils.LAST_WKB_VERSION, metaData.numBands, metaData.scaleX, metaData
                .scaleY, upCorner.x, upCorner.y, metaData.skewX, metaData.skewY, metaData.srid, newWidth, newHeight, metaData.bands);

        
        return GeoRasterRenderedImage.create(cropOp(geoRaster, minX, minY, newWidth, newHeight), outputMetadata);

    }   
    
    
    
    /**
     * Crops the pixel values of a rendered image to a specified rectangle.
     *
     * @param geoRaster The input GeoRaster.
     * @param x The x origin for each band.
     * @param y The y origin for each band.
     * @param width The width for each band.
     * @param height The height for each band.
     * @return
     */
    public static RenderedOp cropOp(GeoRaster geoRaster, double x, double y, int width, int height) {

        if ((width == 0) || (height == 0)) {
            throw new IllegalArgumentException("The width and height value cannot be equal to zero.");
        }
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(geoRaster);
        pb.add((float) x);
        pb.add((float) y);
        pb.add((float) width);
        pb.add((float) height);   
        
        return JAI.create("crop", pb);
    }
    
}
