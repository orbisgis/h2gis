package org.h2gis.h2spatialext.jai;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            new Point(0, 0), // center
            new Point(1, 0), // right
            new Point(-1, 1), // bottom left
            new Point(0, 1), // bottom
            new Point(1, 1) // bottom right
    };
    // in {@link NEIGHBORS_INDEX} the index of center.
    public static final int SRC_INDEX = 4;
    public Area3x3OpImage(RenderedImage source, BorderExtender extender, Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(source, layout, config, true, extender, 1, 1, 1, 1);
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        // Retrieve format tags.
        RasterFormatTag[] formatTags = getFormatTags();
        Rectangle srcRect = mapDestRect(destRect, 0);

        List<RasterAccessor> rasterAccessList = new ArrayList<RasterAccessor>(sources.length);
        int srcIndex = 0;
        for(Raster source : sources) {
                rasterAccessList.add(new RasterAccessor(source, srcRect, formatTags[srcIndex], getSourceImage(srcIndex)
                        .getColorModel()));
            srcIndex++;
        }
        RasterAccessor dst = new RasterAccessor(dest, destRect, formatTags[1], getColorModel());

        switch (sources[0].getTransferType()) {
            case DataBuffer.TYPE_FLOAT:
                processingFloatSource(rasterAccessList, dst);
                break;
        }
    }

    protected void processingFloatSource(List<RasterAccessor> rasterAccess, RasterAccessor dst) {

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final float destDataArrays[][] = dst.getFloatDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        final List<SrcDataStruct> srcDataStructs = new ArrayList<SrcDataStruct>(rasterAccess.size());
        for(RasterAccessor rasterAccessor : rasterAccess) {
            srcDataStructs.add(new SrcDataStruct(rasterAccessor));
        }

        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final float dstData[] = destDataArrays[idBand];
            double defaultValue = getBandDefaultValue(idBand);
            Arrays.fill(dstData, (float)defaultValue);
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    double[][] neighborsValues = new double[srcDataStructs.size()][];
                    for(int idSrc=0; idSrc < neighborsValues.length; idSrc++) {
                        neighborsValues[idSrc] = srcDataStructs.get(idSrc).getNeighborsValues(idBand, i, j);
                    }
                    // Compute in sub method
                    dstData[dstPixelOffset] = (float) computeCell(idBand, neighborsValues);
                    dstPixelOffset += destPixelStride;
                }
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

    private static class SrcDataStruct {

        public SrcDataStruct(RasterAccessor rasterAccess) {
            srcDataArrays = rasterAccess.getFloatDataArrays();
            srcBandOffsets = rasterAccess.getBandOffsets();
            srcPixelStride = rasterAccess.getPixelStride();
            srcScanlineStride = rasterAccess.getScanlineStride();
            destNumBands = rasterAccess.getNumBands();
            rightPixelOffset = rasterAccess.getNumBands() * 2;
            bottomScanlineOffset = srcScanlineStride * 2;
        }

        final float srcDataArrays[][];
        final int srcBandOffsets[];
        final int srcPixelStride;
        final int srcScanlineStride;
        final int rightPixelOffset;
        final int destNumBands;
        final int bottomScanlineOffset;

        double[] getNeighborsValues(int band, int i, int j) {
            float[] srcData = srcDataArrays[band];
            int srcPixelOffset = srcBandOffsets[band] + j * srcScanlineStride + i * srcPixelStride;
            return new double[]{ srcData[srcPixelOffset], // top left
                    srcData[srcPixelOffset + destNumBands], // top
                    srcData[srcPixelOffset + rightPixelOffset], // top right
                    srcData[srcPixelOffset + srcScanlineStride], // left
                    srcData[srcPixelOffset + srcScanlineStride + destNumBands], //center
                    srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                    srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                    srcData[srcPixelOffset + bottomScanlineOffset + destNumBands], // bottom
                    srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
            };
        }
    }

    protected abstract double getBandDefaultValue(int band);

    protected abstract double computeCell(int band, final double[][] neighborsValues);
}
