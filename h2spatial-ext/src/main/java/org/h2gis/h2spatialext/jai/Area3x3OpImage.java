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
package org.h2gis.h2spatialext.jai;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFactory;
import javax.media.jai.RasterFormatTag;
import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 3x3 computing helper. Do the computation in double scale, in order to avoid code redundancy.
 * TODO Thread worker pool in order to use all cores
 * @author Nicolas Fortin
 * @author Erwan Bocher
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
    // For composite image source. This information help to decompose bands into multiple sources
    private final int[] sourcesBandsIndex;
    // in {@link NEIGHBORS_INDEX} the index of center.
    public static final int SRC_INDEX = 4;
    public Area3x3OpImage(RenderedImage source, BorderExtender extender, Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(source, layout, config, true, extender, 1, 1, 1, 1);
        sourcesBandsIndex = new int[] {0};
    }

    public Area3x3OpImage(Collection<RenderedImage> sources, BorderExtender extender, Map config, ImageLayout layout) {
        // Require 1 neighbors around the source pixel
        super(mergeSources(sources), imageLayoutForMultipleSource(sources, layout), config, true, extender, 1, 1, 1, 1);
        sourcesBandsIndex = new int[sources.size()];
        int bandId = 0;
        int idSource = 0;
        for(RenderedImage source : sources) {
            sourcesBandsIndex[idSource++] = bandId;
            bandId += source.getSampleModel().getNumBands();
        }
    }

    public static ImageLayout imageLayoutForMultipleSource(Collection<RenderedImage> vectSource, ImageLayout layout) {
        if(vectSource.isEmpty()) {
            throw new IllegalArgumentException("AreaOpImage without sources");
        }
        RenderedImage refImage = vectSource.iterator().next();
        SampleModel sampleModel;
        if(layout == null) {
            sampleModel = refImage.getSampleModel();
            layout = new ImageLayout(refImage);
        } else {
            sampleModel = layout.getSampleModel(refImage);
        }

        int numBands = refImage.getSampleModel().getNumBands();

        SampleModel csm = RasterFactory
                .createComponentSampleModel(sampleModel, sampleModel.getDataType(), layout.getTileWidth(refImage),
                        layout.getTileHeight(refImage), numBands);

        layout.setSampleModel(csm);
        return layout;
    }

    public static RenderedImage mergeSources(Collection<RenderedImage> sources) {
        // Before merging sources, all data type must be the same, without loosing precision
        int upperByteSize = 0;
        int upperTypeFormat = -1;
        for (RenderedImage im : sources) {
            int imDataType = im.getSampleModel().getDataType();
            int imTypeSize = DataBuffer.getDataTypeSize(imDataType);
            if(imTypeSize > upperByteSize) {
                upperTypeFormat = imDataType;
                upperByteSize = imTypeSize;
            }
        }

        ParameterBlockJAI pbjai = new ParameterBlockJAI("bandmerge");
        int srcIndex = 0;
        for (RenderedImage im : sources) {
            if(im.getSampleModel().getDataType() == upperTypeFormat) {
                pbjai.setSource(im, srcIndex++);
            } else {
                // Scale up format
                ParameterBlock pbConvert = new ParameterBlock();
                pbConvert.addSource(im);
                pbConvert.add(upperTypeFormat);
                pbjai.setSource(JAI.create("format", pbConvert), srcIndex++);
            }
        }
        RenderingHints renderingHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout(sources.iterator().next()));
        RenderedImage merged = JAI.create("bandmerge", pbjai, renderingHints);
        return merged;
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        // Retrieve format tags.
        RasterFormatTag[] formatTags = getFormatTags();
        Rectangle srcRect = mapDestRect(destRect, 0);
        Raster source = sources[0];
        RasterAccessor sourceAccessor = new RasterAccessor(source, srcRect, formatTags[0],
                getSourceImage(0).getColorModel());
        // last tag id is for destination
        RasterAccessor dst = new RasterAccessor(dest, destRect, formatTags[sources.length], getColorModel());
        SrcDataStruct srcDataStruct = dataStructFromRasterAccessor(sourceAccessor);
        switch (dst.getDataType()) {
            case DataBuffer.TYPE_FLOAT:
                processingFloatDest(srcDataStruct, dst);
                break;
            case DataBuffer.TYPE_INT:
                processingIntDest(srcDataStruct, dst);
                break;
            case DataBuffer.TYPE_BYTE:
                processingByteDest(srcDataStruct, dst);
                break;
            case DataBuffer.TYPE_SHORT:
                processingShortDest(srcDataStruct, dst);
                break;
            case DataBuffer.TYPE_DOUBLE:
                processingDoubleDest(srcDataStruct, dst);
                break;
        }
    }

    protected static SrcDataStruct dataStructFromRasterAccessor(RasterAccessor rasterAccessor) {
        switch (rasterAccessor.getDataType())  {
            case DataBuffer.TYPE_FLOAT:
                return new SrcDataStructFloat(rasterAccessor);
            case DataBuffer.TYPE_INT:
                return new SrcDataStructInt(rasterAccessor);
            case DataBuffer.TYPE_BYTE:
                return new SrcDataStructByte(rasterAccessor);
            case DataBuffer.TYPE_SHORT:
                return new SrcDataStructShort(rasterAccessor);
            default:
                return new SrcDataStructDouble(rasterAccessor);
        }
    }

    protected void processingDoubleDest(SrcDataStruct srcDataStruct, RasterAccessor dst) {

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final double destDataArrays[][] = dst.getDoubleDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final double dstData[] = destDataArrays[idBand];
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    double[][] neighborsValues = getNeighborsValues(srcDataStruct, i, j, idBand);
                    // Compute in sub method
                    dstData[dstPixelOffset] = computeCell(idBand, neighborsValues);
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

    private interface SrcDataStruct {
        /**
         * Get neighbors value around cell i,j
         * @param band source band
         * @param i source column
         * @param j source row
         * @return neighbors values
         */
        double[] getNeighborsValues(int band, int i, int j);
    }

    private static class SrcDataStructDouble implements SrcDataStruct {

        public SrcDataStructDouble(RasterAccessor rasterAccess) {
            srcDataArrays = rasterAccess.getDoubleDataArrays();
            srcBandOffsets = rasterAccess.getBandOffsets();
            srcPixelStride = rasterAccess.getPixelStride();
            srcScanlineStride = rasterAccess.getScanlineStride();
            rightPixelOffset = srcPixelStride * 2;
            bottomScanlineOffset = srcScanlineStride * 2;
        }

        final double srcDataArrays[][];
        final int srcBandOffsets[];
        final int srcPixelStride;
        final int srcScanlineStride;
        final int rightPixelOffset;
        final int bottomScanlineOffset;

        @Override
        public double[] getNeighborsValues(int band, int i, int j) {
            double[] srcData = srcDataArrays[band];
            int srcPixelOffset = srcBandOffsets[band] + j * srcScanlineStride + i * srcPixelStride;
            return new double[]{ srcData[srcPixelOffset], // top left
                    srcData[srcPixelOffset + srcPixelStride], // top
                    srcData[srcPixelOffset + rightPixelOffset], // top right
                    srcData[srcPixelOffset + srcScanlineStride], // left
                    srcData[srcPixelOffset + srcScanlineStride + srcPixelStride], //center
                    srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                    srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                    srcData[srcPixelOffset + bottomScanlineOffset + srcPixelStride], // bottom
                    srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
            };
        }
    }


    protected void processingShortDest(SrcDataStruct srcDataStruct, RasterAccessor dst) {

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final short destDataArrays[][] = dst.getShortDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final short dstData[] = destDataArrays[idBand];
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    double[][] neighborsValues = getNeighborsValues(srcDataStruct, i, j, idBand);
                    // Compute in sub method
                    double value = computeCell(idBand, neighborsValues);
                    dstData[dstPixelOffset] = (short)Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
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

    private static class SrcDataStructShort implements SrcDataStruct{

        public SrcDataStructShort(RasterAccessor rasterAccess) {
            srcDataArrays = rasterAccess.getShortDataArrays();
            srcBandOffsets = rasterAccess.getBandOffsets();
            srcPixelStride = rasterAccess.getPixelStride();
            srcScanlineStride = rasterAccess.getScanlineStride();
            rightPixelOffset = srcPixelStride * 2;
            bottomScanlineOffset = srcScanlineStride * 2;
        }

        final short srcDataArrays[][];
        final int srcBandOffsets[];
        final int srcPixelStride;
        final int srcScanlineStride;
        final int rightPixelOffset;
        final int bottomScanlineOffset;


        @Override
        public double[] getNeighborsValues(int band, int i, int j) {
            short[] srcData = srcDataArrays[band];
            int srcPixelOffset = srcBandOffsets[band] + j * srcScanlineStride + i * srcPixelStride;
            return new double[]{ srcData[srcPixelOffset], // top left
                    srcData[srcPixelOffset + srcPixelStride], // top
                    srcData[srcPixelOffset + rightPixelOffset], // top right
                    srcData[srcPixelOffset + srcScanlineStride], // left
                    srcData[srcPixelOffset + srcScanlineStride + srcPixelStride], //center
                    srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                    srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                    srcData[srcPixelOffset + bottomScanlineOffset + srcPixelStride], // bottom
                    srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
            };
        }
    }

    private double[][] getNeighborsValues(SrcDataStruct srcDataStruct,int i, int j, int idBand) {
        double[][] neighborsValues = new double[sourcesBandsIndex.length][];
        for(int idSrc=0; idSrc < neighborsValues.length; idSrc++) {
            int sourceBandIndex = sourcesBandsIndex[idSrc] + idBand;
            neighborsValues[idSrc] = srcDataStruct.getNeighborsValues(sourceBandIndex, i, j);
        }
        return neighborsValues;
    }

    protected void processingByteDest(SrcDataStruct srcDataStruct, RasterAccessor dst) {

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final byte destDataArrays[][] = dst.getByteDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final byte dstData[] = destDataArrays[idBand];
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    double[][] neighborsValues = getNeighborsValues(srcDataStruct, i, j, idBand);
                    // Compute in sub method
                    double value = computeCell(idBand, neighborsValues);
                    dstData[dstPixelOffset] =  (byte)Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, value));
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

    private static class SrcDataStructByte implements SrcDataStruct {

        public SrcDataStructByte(RasterAccessor rasterAccess) {
            srcDataArrays = rasterAccess.getByteDataArrays();
            srcBandOffsets = rasterAccess.getBandOffsets();
            srcPixelStride = rasterAccess.getPixelStride();
            srcScanlineStride = rasterAccess.getScanlineStride();
            bottomScanlineOffset = srcScanlineStride * 2;
            rightMostOffset = srcPixelStride * 2;
        }

        final byte srcDataArrays[][];
        final int srcBandOffsets[];
        final int srcPixelStride;
        final int srcScanlineStride;
        final int bottomScanlineOffset;
        final int rightMostOffset;


        @Override
        public double[] getNeighborsValues(int band, int i, int j) {
            byte[] srcData = srcDataArrays[band];
            int srcPixelOffset = srcBandOffsets[band] + j * srcScanlineStride + i * srcPixelStride;
            return new double[]{ srcData[srcPixelOffset] & 0xff, // top left
                    srcData[srcPixelOffset + srcPixelStride] & 0xff, // top
                    srcData[srcPixelOffset + rightMostOffset] & 0xff, // top right
                    srcData[srcPixelOffset + srcScanlineStride] & 0xff, // left
                    srcData[srcPixelOffset + srcScanlineStride + srcPixelStride] & 0xff, //center
                    srcData[srcPixelOffset + srcScanlineStride + rightMostOffset] & 0xff, // right
                    srcData[srcPixelOffset + bottomScanlineOffset] & 0xff, // bottom left
                    srcData[srcPixelOffset + bottomScanlineOffset + srcPixelStride] & 0xff, // bottom
                    srcData[srcPixelOffset + bottomScanlineOffset + rightMostOffset] & 0xff // bottom right
            };
        }
    }

    protected void processingFloatDest(SrcDataStruct srcDataStruct, RasterAccessor dst) {

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final float destDataArrays[][] = dst.getFloatDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final float dstData[] = destDataArrays[idBand];
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    double[][] neighborsValues = getNeighborsValues(srcDataStruct, i, j, idBand);
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

    private static class SrcDataStructInt implements SrcDataStruct{

        public SrcDataStructInt(RasterAccessor rasterAccess) {
            srcDataArrays = rasterAccess.getIntDataArrays();
            srcBandOffsets = rasterAccess.getBandOffsets();
            srcPixelStride = rasterAccess.getPixelStride();
            srcScanlineStride = rasterAccess.getScanlineStride();
            rightPixelOffset = rasterAccess.getNumBands() * 2;
            bottomScanlineOffset = srcScanlineStride * 2;
        }

        final int srcDataArrays[][];
        final int srcBandOffsets[];
        final int srcPixelStride;
        final int srcScanlineStride;
        final int rightPixelOffset;
        final int bottomScanlineOffset;


        @Override
        public double[] getNeighborsValues(int band, int i, int j) {
            int[] srcData = srcDataArrays[band];
            int srcPixelOffset = srcBandOffsets[band] + j * srcScanlineStride + i * srcPixelStride;
            return new double[]{ srcData[srcPixelOffset], // top left
                    srcData[srcPixelOffset + srcPixelStride], // top
                    srcData[srcPixelOffset + rightPixelOffset], // top right
                    srcData[srcPixelOffset + srcScanlineStride], // left
                    srcData[srcPixelOffset + srcScanlineStride + srcPixelStride], //center
                    srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                    srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                    srcData[srcPixelOffset + bottomScanlineOffset + srcPixelStride], // bottom
                    srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
            };
        }
    }
    protected void processingIntDest(SrcDataStruct srcDataStruct, RasterAccessor dst) {

        final int destWidth = dst.getWidth();
        final int destHeight = dst.getHeight();
        final int destNumBands = dst.getNumBands();

        final int destDataArrays[][] = dst.getIntDataArrays();
        final int destBandOffsets[] = dst.getBandOffsets();
        final int destPixelStride = dst.getPixelStride();
        final int dstScanlineStride = dst.getScanlineStride();

        for(int idBand = 0; idBand < destNumBands; idBand++) {
            final int dstData[] = destDataArrays[idBand];
            int dstScanlineOffset = destBandOffsets[idBand];
            // Init
            for (int j = 0; j < destHeight; j++) {
                int dstPixelOffset = dstScanlineOffset;
                for (int i = 0; i < destWidth; i++) {
                    double[][] neighborsValues = getNeighborsValues(srcDataStruct, i, j, idBand);
                    // Compute in sub method
                    dstData[dstPixelOffset] = (int) computeCell(idBand, neighborsValues);
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

    private static class SrcDataStructFloat implements SrcDataStruct {

        public SrcDataStructFloat(RasterAccessor rasterAccess) {
            srcDataArrays = rasterAccess.getFloatDataArrays();
            srcBandOffsets = rasterAccess.getBandOffsets();
            srcPixelStride = rasterAccess.getPixelStride();
            srcScanlineStride = rasterAccess.getScanlineStride();
            rightPixelOffset = srcPixelStride * 2;
            bottomScanlineOffset = srcScanlineStride * 2;
        }

        final float srcDataArrays[][];
        final int srcBandOffsets[];
        final int srcPixelStride;
        final int srcScanlineStride;
        final int rightPixelOffset;
        final int bottomScanlineOffset;


        @Override
        public double[] getNeighborsValues(int band, int i, int j) {
            float[] srcData = srcDataArrays[band];
            int srcPixelOffset = srcBandOffsets[band] + j * srcScanlineStride + i * srcPixelStride;
            return new double[]{ srcData[srcPixelOffset], // top left
                    srcData[srcPixelOffset + srcPixelStride], // top
                    srcData[srcPixelOffset + rightPixelOffset], // top right
                    srcData[srcPixelOffset + srcScanlineStride], // left
                    srcData[srcPixelOffset + srcScanlineStride + srcPixelStride], //center
                    srcData[srcPixelOffset + srcScanlineStride + rightPixelOffset], // right
                    srcData[srcPixelOffset + bottomScanlineOffset], // bottom left
                    srcData[srcPixelOffset + bottomScanlineOffset + srcPixelStride], // bottom
                    srcData[srcPixelOffset + bottomScanlineOffset + rightPixelOffset] // bottom right
            };
        }
    }

    protected abstract double computeCell(int band, final double[][] neighborsValues);
}
