/*
 * Copyright (C) 2015 CNRS
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

package org.h2gis.h2spatialext.function.spatial.raster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
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
 * Crops the pixel values of a raster to the envelope of the geometry or a set
 * of parameters :
 *
 * The x origin for each band. 
 * The y origin for each band. 
 * The width for each band. 
 * The height for each band.
 *
 * @author Erwan Bocher
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

        RasterMetaData metadata = geoRaster.getMetaData();
        Envelope geoRasterEnv = metadata.getEnvelope();
        
        int[] origin = metadata.getPixelFromCoordinate(new Coordinate(inputCropEnv.getMinX(), inputCropEnv.getMinY()));

        int[] corner = metadata.getPixelFromCoordinate(new Coordinate(inputCropEnv.getMaxX(), inputCropEnv.getMaxY()));
        
        int x = Math.min(origin[0], corner[0]);
        x = Math.max(x, 0);
        int y = Math.min(origin[1], corner[1]);
        y = Math.max(y, 0);
        int width = Math.abs(origin[0] - corner[0]);
        width = Math.min(width, geoRaster.getWidth());
        int height = Math.abs(origin[1] - corner[1]);
        height = Math.min(height, geoRaster.getHeight());

        int maxWidth = Math.max(geoRaster.getWidth() - x, 0);
        int maxHeight = Math.max(geoRaster.getWidth() - y, 0);
        width = Math.min(maxWidth, width);
        height = Math.min(maxHeight, height);
        if ((width == 0) || (height == 0)) {
            return null;
        }
        
        Coordinate upCorner = metadata.getPixelCoordinate(x, y+height);
        
        RasterMetaData outputMetadata = new RasterUtils.RasterMetaData(RasterUtils.LAST_WKB_VERSION, metadata.numBands, metadata.scaleX, metadata
                .scaleY, upCorner.x, upCorner.y, metadata.skewX, metadata.skewY, metadata.srid, width, height, metadata.bands);

        
        return GeoRasterRenderedImage.create(cropOp(geoRaster, metadata, x, y, width, height), outputMetadata);

    }
    
    
    /**
     * Crops the pixel values of a rendered image to a specified rectangle.
     *
     * @param geoRaster The input GeoRaster.
     * @param metadata The metadata of the input GeoRaster.
     * @param x The x origin for each band.
     * @param y The y origin for each band.
     * @param width The width for each band.
     * @param height The height for each band.
     * @return
     */
    public static RenderedOp cropOp(GeoRaster geoRaster, RasterMetaData metadata, double x, double y, int width, int height) {

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
