package org.h2gis.h2spatialext.jai;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

/**
 * 3x3 computing helper. Do the computation in double scale, in order to avoid code redundancy.
 * TODO Thread worker pool in order to use all cores
 * @author Nicolas Fortin
 */
public abstract class Area3x3OpImage extends AreaOpImage {

    public Area3x3OpImage(RenderedImage source, BorderExtender extender, Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(source, layout, config, true, extender, 1, 1, 1, 1);
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        // Retrieve format tags.
        RasterFormatTag[] formatTags = getFormatTags();

        Raster source = sources[0];
        Rectangle srcRect = mapDestRect(destRect, 0);

        RasterAccessor rasterAccess =
                new RasterAccessor(source, srcRect, formatTags[0], getSourceImage(0).getColorModel());
        RasterAccessor dst = new RasterAccessor(dest, destRect, formatTags[1], getColorModel());

        switch (source.getTransferType()) {
            case DataBuffer.TYPE_FLOAT:
                processingFloatSource(rasterAccess, dst);
                break;
        }
    }

    protected void processingFloatSource(RasterAccessor rasterAccess, RasterAccessor dst) {

        int destWidth = dst.getWidth();
        int destHeight = dst.getHeight();
        int destNumBands = dst.getNumBands();

        float destDataArrays[][] = dst.getFloatDataArrays();
        int destBandOffsets[] = dst.getBandOffsets();
        int destPixelStride = dst.getPixelStride();
        int dstScanlineStride = dst.getScanlineStride();

        float srcDataArrays[][] = rasterAccess.getFloatDataArrays();
        int srcBandOffsets[] = rasterAccess.getBandOffsets();
        int srcPixelStride = rasterAccess.getPixelStride();
        int srcScanlineStride = rasterAccess.getScanlineStride();

        // precalculate offsets
        int bottomScanlineOffset = srcScanlineStride * 2;
        int rightPixelOffset = destNumBands * 2;
        final int k = 0;
        float dstData[] = destDataArrays[k];
        float srcData[] = srcDataArrays[k];
        int srcScanlineOffset = srcBandOffsets[k];
        int dstScanlineOffset = destBandOffsets[k];
        for (int j = 0; j < destHeight; j++) {
            int srcPixelOffset = srcScanlineOffset;
            int dstPixelOffset = dstScanlineOffset;
            for (int i = 0; i < destWidth; i++) {
                final double[] neighborsValues = new double[] {
                        srcData[srcPixelOffset], // top left
                        srcData[srcPixelOffset + destNumBands], // top
                        srcData[srcPixelOffset + rightPixelOffset], // top right
                        srcData[srcPixelOffset + srcScanlineStride], // left
                        srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                        srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                        srcData[srcPixelOffset + bottomScanlineOffset + destNumBands], // bottom
                        srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
                };
                // Compute in sub method
                dstData[dstPixelOffset] = (float)computeCell((srcPixelOffset / srcPixelStride) % srcScanlineStride,
                        (srcPixelOffset / srcPixelStride) / srcScanlineStride, k, neighborsValues);
                srcPixelOffset += srcPixelStride;
                dstPixelOffset += destPixelStride;
            }
            srcScanlineOffset += srcScanlineStride;
            dstScanlineOffset += dstScanlineStride;
        }
        // If the RasterAccessor object set up a temporary buffer for the
        // op to write to, tell the RasterAccessor to write that data
        // to the raster no that we're done with it.
        if (dst.isDataCopy()) {
            dst.clampDataArrays();
            dst.copyDataToRaster();
        }
    }

    protected abstract double computeCell(int i, int j, int band, final double[] neighborsValues);
}
