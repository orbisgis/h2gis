package org.h2gis.h2spatialext.jai;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

/**
 * Slope operation on raster
 * @author Nicolas Fortin
 */
public class SlopeOpImage extends AreaOpImage {
    public SlopeOpImage(RenderedImage source, BorderExtender extender, Map config, ImageLayout layout,
            KernelJAI kernel) {
        super(source, layout, config, true, extender, kernel.getLeftPadding(), kernel.getRightPadding(),
                kernel.getTopPadding(), kernel.getBottomPadding());

        if ((kernel.getWidth() != 3) ||
                (kernel.getHeight() != 3) ||
                (kernel.getXOrigin() != 1) ||
                (kernel.getYOrigin() != 1)) {
            throw new RuntimeException("Slope must be done on 3x3 kernel");
        }
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {

        // Retrieve format tags.
        RasterFormatTag[] formatTags = getFormatTags();

        Raster source = sources[0];
        Rectangle srcRect = mapDestRect(destRect, 0);

        RasterAccessor src =
                new RasterAccessor(source,srcRect,
                        formatTags[0],
                        getSourceImage(0).getColorModel());
        RasterAccessor dst =
                new RasterAccessor(dest,destRect,
                        formatTags[1], getColorModel());


        int dwidth = dst.getWidth();
        int dheight = dst.getHeight();
        int dnumBands = dst.getNumBands();

        int dstDataArrays[][] = dst.getIntDataArrays();
        int dstBandOffsets[] = dst.getBandOffsets();
        int dstPixelStride = dst.getPixelStride();
        int dstScanlineStride = dst.getScanlineStride();

        int srcDataArrays[][] = src.getIntDataArrays();
        int srcBandOffsets[] = src.getBandOffsets();
        int srcPixelStride = src.getPixelStride();
        int srcScanlineStride = src.getScanlineStride();

        // precalculate offsets
        int bottomScanlineOffset = srcScanlineStride*2;
        int rightPixelOffset = dnumBands*2;
        final int k = 0;
        int dstData[] = dstDataArrays[k];
        int srcData[] = srcDataArrays[k];
        int srcScanlineOffset = srcBandOffsets[k];
        int dstScanlineOffset = dstBandOffsets[k];
        for (int j = 0; j < dheight; j++)  {
            int srcPixelOffset = srcScanlineOffset;
            int dstPixelOffset = dstScanlineOffset;
            for (int i = 0; i < dwidth; i++)  {
                float f =
                        srcData[srcPixelOffset] +
                                srcData[srcPixelOffset + dnumBands] +
                                srcData[srcPixelOffset +
                                        rightPixelOffset] +
                                srcData[srcPixelOffset + srcScanlineStride] +
                                srcData[srcPixelOffset +
                                        srcScanlineStride +
                                        dnumBands] +
                                srcData[srcPixelOffset +
                                        srcScanlineStride +
                                        rightPixelOffset] +
                                srcData[srcPixelOffset +
                                        bottomScanlineOffset] +
                                srcData[srcPixelOffset +
                                        bottomScanlineOffset +
                                        dnumBands] +
                                srcData[srcPixelOffset +
                                        bottomScanlineOffset +
                                        rightPixelOffset];

                dstData[dstPixelOffset] = (int)f;
                srcPixelOffset += srcPixelStride;
                dstPixelOffset += dstPixelStride;
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
}
