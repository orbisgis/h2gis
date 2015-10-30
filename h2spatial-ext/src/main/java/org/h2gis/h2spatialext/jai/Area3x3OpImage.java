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
    public static final Point[] NEIGHBORS_INDEX = new Point[] {
            new Point(-1, -1), // top left
            new Point(0, -1), // top
            new Point(1, -1), // top right
            new Point(-1, 0), // left
            new Point(1, 0), // right
            new Point(-1, 1), // bottom left
            new Point(0, 1), // bottom
            new Point(1, 1) // bottom right
    };
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

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final float destDataArrays[][] = dst.getFloatDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        final float srcDataArrays[][] = rasterAccess.getFloatDataArrays();
        final int srcBandOffsets[] = rasterAccess.getBandOffsets();
        final int srcPixelStride = rasterAccess.getPixelStride();
        final int srcScanlineStride = rasterAccess.getScanlineStride();

        // precalculate offsets
        final int bottomScanlineOffset = srcScanlineStride * 2;
        final int rightPixelOffset = destNumBands * 2;
        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final float dstData[] = destDataArrays[idBand];
            final float srcData[] = srcDataArrays[idBand];
            int srcScanlineOffset = srcBandOffsets[idBand];
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    final double[] neighborsValues = new double[]{srcData[srcPixelOffset], // top left
                            srcData[srcPixelOffset + destNumBands], // top
                            srcData[srcPixelOffset + rightPixelOffset], // top right
                            srcData[srcPixelOffset + srcScanlineStride], // left
                            srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                            srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                            srcData[srcPixelOffset + bottomScanlineOffset + destNumBands], // bottom
                            srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
                    };
                    int srcPos = srcPixelOffset + srcScanlineStride + destNumBands;
                    // Compute in sub method
                    dstData[dstPixelOffset] = (float) computeCell((srcPos / srcPixelStride) % srcScanlineStride,
                            (srcPos / srcPixelStride) / srcScanlineStride, idBand, srcData[srcPos], neighborsValues);
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += destPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
        // If the RasterAccessor object set up a temporary buffer for the
        // op to write to, tell the RasterAccessor to write that data
        // to the raster no that we're done with it.
        if (dst.isDataCopy()) {
            dst.clampDataArrays();
            dst.copyDataToRaster();
        }
    }

    protected abstract double computeCell(int i, int j, int band, double cellValue, final double[] neighborsValues);
}
