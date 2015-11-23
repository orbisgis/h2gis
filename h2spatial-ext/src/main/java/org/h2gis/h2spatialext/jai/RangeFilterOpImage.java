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

import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

/**
 * @author Nicolas Fortin
 * For returnFilterOnMatch=true:
 * Take image A, image B and interval min max. if min <= B <= max then output B else A.
 * For returnFilterOnMatch=false:
 * Take image A, image B and interval min max. if min <= B <= max then output A else B.
 * This is usefull to copy only nodata pixels to other image, or create a mask.
 * This could be done by combining some JAI Operators, but doing this in one step consume less cpu cycles.
 */
public class RangeFilterOpImage extends PointOpImage {
    protected final double[][] minMaxMatchFilter;
    protected final boolean returnFilterOnMatch;
    private static final int MIN = 0;
    private static final int MAX = 1;
    /**
     * Constructor
     * @param source0 Source that contain data to copy
     * @param filter Source that contain constants value
     * @param minMaxMatchFilter Constant to filter
     * @param layout Image layout, can be null
     * @param configuration Configuration
     */
    public RangeFilterOpImage(RenderedImage source0, RenderedImage filter, double[][] minMaxMatchFilter,
            boolean returnFilterOnMatch, ImageLayout layout, Map configuration) {
        super(source0, filter, layout, configuration, true);
        this.minMaxMatchFilter = minMaxMatchFilter;
        this.returnFilterOnMatch = returnFilterOnMatch;
        if(getNumBands() != minMaxMatchFilter.length) {
            throw new IllegalArgumentException(
                    "Input image have " + getNumBands() + " bands and constant filter have " +
                            + minMaxMatchFilter.length + " bands");
        }
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        // Retrieve format tags.
        RasterFormatTag[] formatTags = getFormatTags();

        /* For PointOpImage, srcRect = destRect. */
        RasterAccessor s1 = new RasterAccessor(sources[0], destRect,
                formatTags[0],
                getSourceImage(0).getColorModel());
        RasterAccessor s2 = new RasterAccessor(sources[1], destRect,
                formatTags[1],
                getSourceImage(1).getColorModel());
        RasterAccessor d = new RasterAccessor(dest, destRect,
                formatTags[2], getColorModel());

        int src1LineStride = s1.getScanlineStride();
        int src1PixelStride = s1.getPixelStride();
        int[] src1BandOffsets = s1.getBandOffsets();

        int src2LineStride = s2.getScanlineStride();
        int src2PixelStride = s2.getPixelStride();
        int[] src2BandOffsets = s2.getBandOffsets();

        int dstNumBands = d.getNumBands();
        int dstWidth = d.getWidth();
        int dstHeight = d.getHeight();
        int dstLineStride = d.getScanlineStride();
        int dstPixelStride = d.getPixelStride();
        int[] dstBandOffsets = d.getBandOffsets();

        switch (d.getDataType()) {

            case DataBuffer.TYPE_BYTE:
                byteLoop(dstNumBands, dstWidth, dstHeight,
                        src1LineStride, src1PixelStride,
                        src1BandOffsets, s1.getByteDataArrays(),
                        src2LineStride, src2PixelStride,
                        src2BandOffsets, s2.getByteDataArrays(),
                        dstLineStride, dstPixelStride,
                        dstBandOffsets, d.getByteDataArrays());
                break;

            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                shortLoop(dstNumBands, dstWidth, dstHeight,
                        src1LineStride, src1PixelStride,
                        src1BandOffsets, s1.getShortDataArrays(),
                        src2LineStride, src2PixelStride,
                        src2BandOffsets, s2.getShortDataArrays(),
                        dstLineStride, dstPixelStride,
                        dstBandOffsets, d.getShortDataArrays());
                break;

            case DataBuffer.TYPE_INT:
                intLoop(dstNumBands, dstWidth, dstHeight,
                        src1LineStride, src1PixelStride,
                        src1BandOffsets, s1.getIntDataArrays(),
                        src2LineStride, src2PixelStride,
                        src2BandOffsets, s2.getIntDataArrays(),
                        dstLineStride, dstPixelStride,
                        dstBandOffsets, d.getIntDataArrays());
                break;
            case DataBuffer.TYPE_FLOAT:
                floatLoop(dstNumBands, dstWidth, dstHeight, src1LineStride, src1PixelStride, src1BandOffsets,
                        s1.getFloatDataArrays(), src2LineStride, src2PixelStride, src2BandOffsets,
                        s2.getFloatDataArrays(), dstLineStride, dstPixelStride, dstBandOffsets, d.getFloatDataArrays());
                break;
            case DataBuffer.TYPE_DOUBLE:
                doubleLoop(dstNumBands, dstWidth, dstHeight, src1LineStride, src1PixelStride, src1BandOffsets,
                        s1.getDoubleDataArrays(), src2LineStride, src2PixelStride, src2BandOffsets,
                        s2.getDoubleDataArrays(), dstLineStride, dstPixelStride, dstBandOffsets,
                        d.getDoubleDataArrays());
                break;
        }

        if (d.isDataCopy()) {
            d.clampDataArrays();
            d.copyDataToRaster();
        }
    }


    private void byteLoop(int dstNumBands, int dstWidth, int dstHeight,
            int src1LineStride, int src1PixelStride,
            int[] src1BandOffsets, byte[][] src1Data,
            int src2LineStride, int src2PixelStride,
            int[] src2BandOffsets, byte[][] src2Data,
            int dstLineStride, int dstPixelStride,
            int[] dstBandOffsets, byte[][] dstData) {

        for (int b = 0; b < dstNumBands; b++) {
            final byte min = (byte)(128 + minMaxMatchFilter[b][MIN]);
            final byte max = (byte)(128 + minMaxMatchFilter[b][MIN]);
            byte[] s1 = src1Data[b];
            byte[] s2 = src2Data[b];
            byte[] d = dstData[b];
            int src1LineOffset = src1BandOffsets[b];
            int src2LineOffset = src2BandOffsets[b];
            int dstLineOffset = dstBandOffsets[b];

            for (int h = 0; h < dstHeight; h++) {
                int src1PixelOffset = src1LineOffset;
                int src2PixelOffset = src2LineOffset;
                int dstPixelOffset = dstLineOffset;
                src1LineOffset += src1LineStride;
                src2LineOffset += src2LineStride;
                dstLineOffset += dstLineStride;

                for (int w = 0; w < dstWidth; w++) {
                    final byte p = s2[src2PixelOffset];
                    if(min <= p && p <= max) {
                        // Match !
                        d[dstPixelOffset] = returnFilterOnMatch ? p : s1[src1PixelOffset];
                    } else {
                        // Not match
                        d[dstPixelOffset] = returnFilterOnMatch ? s1[src1PixelOffset] : p;
                    }
                    src1PixelOffset += src1PixelStride;
                    src2PixelOffset += src2PixelStride;
                    dstPixelOffset += dstPixelStride;
                }
            }
        }
    }

    private void shortLoop(int dstNumBands, int dstWidth, int dstHeight,
            int src1LineStride, int src1PixelStride,
            int[] src1BandOffsets, short[][] src1Data,
            int src2LineStride, int src2PixelStride,
            int[] src2BandOffsets, short[][] src2Data,
            int dstLineStride, int dstPixelStride,
            int[] dstBandOffsets, short[][] dstData) {

        for (int b = 0; b < dstNumBands; b++) {
            final short min = (short)(minMaxMatchFilter[b][MIN]);
            final short max = (short)(minMaxMatchFilter[b][MIN]);
            short[] s1 = src1Data[b];
            short[] s2 = src2Data[b];
            short[] d = dstData[b];
            int src1LineOffset = src1BandOffsets[b];
            int src2LineOffset = src2BandOffsets[b];
            int dstLineOffset = dstBandOffsets[b];

            for (int h = 0; h < dstHeight; h++) {
                int src1PixelOffset = src1LineOffset;
                int src2PixelOffset = src2LineOffset;
                int dstPixelOffset = dstLineOffset;
                src1LineOffset += src1LineStride;
                src2LineOffset += src2LineStride;
                dstLineOffset += dstLineStride;

                for (int w = 0; w < dstWidth; w++) {
                    final short p = s2[src2PixelOffset];
                    if(min <= p && p <= max) {
                        // Match !
                        d[dstPixelOffset] = returnFilterOnMatch ? p : s1[src1PixelOffset];
                    } else {
                        // Not match
                        d[dstPixelOffset] = returnFilterOnMatch ? s1[src1PixelOffset] : p;
                    }
                    src1PixelOffset += src1PixelStride;
                    src2PixelOffset += src2PixelStride;
                    dstPixelOffset += dstPixelStride;
                }
            }
        }
    }

    private void intLoop(int dstNumBands, int dstWidth, int dstHeight,
            int src1LineStride, int src1PixelStride,
            int[] src1BandOffsets, int[][] src1Data,
            int src2LineStride, int src2PixelStride,
            int[] src2BandOffsets, int[][] src2Data,
            int dstLineStride, int dstPixelStride,
            int[] dstBandOffsets, int[][] dstData) {

        for (int b = 0; b < dstNumBands; b++) {
            final int min = (int)(minMaxMatchFilter[b][MIN]);
            final int max = (int)(minMaxMatchFilter[b][MIN]);
            int[] s1 = src1Data[b];
            int[] s2 = src2Data[b];
            int[] d = dstData[b];
            int src1LineOffset = src1BandOffsets[b];
            int src2LineOffset = src2BandOffsets[b];
            int dstLineOffset = dstBandOffsets[b];

            for (int h = 0; h < dstHeight; h++) {
                int src1PixelOffset = src1LineOffset;
                int src2PixelOffset = src2LineOffset;
                int dstPixelOffset = dstLineOffset;
                src1LineOffset += src1LineStride;
                src2LineOffset += src2LineStride;
                dstLineOffset += dstLineStride;

                for (int w = 0; w < dstWidth; w++) {
                    final int p = s2[src2PixelOffset];
                    if(min <= p && p <= max) {
                        // Match !
                        d[dstPixelOffset] = returnFilterOnMatch ? p : s1[src1PixelOffset];
                    } else {
                        // Not match
                        d[dstPixelOffset] = returnFilterOnMatch ? s1[src1PixelOffset] : p;
                    }
                    src1PixelOffset += src1PixelStride;
                    src2PixelOffset += src2PixelStride;
                    dstPixelOffset += dstPixelStride;
                }
            }
        }
    }


    private void floatLoop(int dstNumBands, int dstWidth, int dstHeight,
            int src1LineStride, int src1PixelStride,
            int[] src1BandOffsets, float[][] src1Data,
            int src2LineStride, int src2PixelStride,
            int[] src2BandOffsets, float[][] src2Data,
            int dstLineStride, int dstPixelStride,
            int[] dstBandOffsets, float[][] dstData) {

        for (int b = 0; b < dstNumBands; b++) {
            final float min = (float)(minMaxMatchFilter[b][MIN]);
            final float max = (float)(minMaxMatchFilter[b][MIN]);
            float[] s1 = src1Data[b];
            float[] s2 = src2Data[b];
            float[] d = dstData[b];
            int src1LineOffset = src1BandOffsets[b];
            int src2LineOffset = src2BandOffsets[b];
            int dstLineOffset = dstBandOffsets[b];

            for (int h = 0; h < dstHeight; h++) {
                int src1PixelOffset = src1LineOffset;
                int src2PixelOffset = src2LineOffset;
                int dstPixelOffset = dstLineOffset;
                src1LineOffset += src1LineStride;
                src2LineOffset += src2LineStride;
                dstLineOffset += dstLineStride;

                for (int w = 0; w < dstWidth; w++) {
                    final float p = s2[src2PixelOffset];
                    if(min <= p && p <= max) {
                        // Match !
                        d[dstPixelOffset] = returnFilterOnMatch ? p : s1[src1PixelOffset];
                    } else {
                        // Not match
                        d[dstPixelOffset] = returnFilterOnMatch ? s1[src1PixelOffset] : p;
                    }
                    src1PixelOffset += src1PixelStride;
                    src2PixelOffset += src2PixelStride;
                    dstPixelOffset += dstPixelStride;
                }
            }
        }
    }


    private void doubleLoop(int dstNumBands, int dstWidth, int dstHeight,
            int src1LineStride, int src1PixelStride,
            int[] src1BandOffsets, double[][] src1Data,
            int src2LineStride, int src2PixelStride,
            int[] src2BandOffsets, double[][] src2Data,
            int dstLineStride, int dstPixelStride,
            int[] dstBandOffsets, double[][] dstData) {

        for (int b = 0; b < dstNumBands; b++) {
            final double min = minMaxMatchFilter[b][MIN];
            final double max = minMaxMatchFilter[b][MIN];
            double[] s1 = src1Data[b];
            double[] s2 = src2Data[b];
            double[] d = dstData[b];
            int src1LineOffset = src1BandOffsets[b];
            int src2LineOffset = src2BandOffsets[b];
            int dstLineOffset = dstBandOffsets[b];

            for (int h = 0; h < dstHeight; h++) {
                int src1PixelOffset = src1LineOffset;
                int src2PixelOffset = src2LineOffset;
                int dstPixelOffset = dstLineOffset;
                src1LineOffset += src1LineStride;
                src2LineOffset += src2LineStride;
                dstLineOffset += dstLineStride;

                for (int w = 0; w < dstWidth; w++) {
                    final double p = s2[src2PixelOffset];
                    if(min <= p && p <= max) {
                        // Match !
                        d[dstPixelOffset] = returnFilterOnMatch ? p : s1[src1PixelOffset];
                    } else {
                        // Not match
                        d[dstPixelOffset] = returnFilterOnMatch ? s1[src1PixelOffset] : p;
                    }
                    src1PixelOffset += src1PixelStride;
                    src2PixelOffset += src2PixelStride;
                    dstPixelOffset += dstPixelStride;
                }
            }
        }
    }
}
