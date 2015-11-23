/*
 * @(#) $Header$
 *
 * Copyright (C) 2007  Forklabs Daniel Léonard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.h2gis.h2spatialext.jai;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.media.jai.ColormapOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

/**
 * Class {@code UnaryFunctionOpImage} is an {@link OpImage} implementing the
 * <em>unaryfunction</em> operation as described in
 * {@link UnaryFunctionDescriptor}.
 *
 * @author   <a href="mailto:forklabs at gmail.com?subject=ca.forklabs.media.jai.opimage.UnaryFunctionOpImage">Daniel Léonard</a>
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class UnaryFunctionOpImage extends ColormapOpImage {
// TODO : add an aspect that will cache the calculation value for any type
//---------------------------
// Instance variables
//---------------------------

   /** The unary function. */
   private UnaryFunction<Double, Double> function;


//---------------------------
// Constructors
//---------------------------

   /**
    * Constructor.
    * <p>
    * The layout of the source is used as the fall-back for the layout of the
    * destination. Any layout parameters not specified in the {@code layout}
    * argument are set to the same value as that of the source.
    * @param   source   the source image.
    * @param   function   the function to apply to the source image.
    * @param   layout   the image layout of the destination image.
    * @param   config   the configuration of the operation.
    */
   public UnaryFunctionOpImage(RenderedImage source, UnaryFunction<Double, Double> function, ImageLayout layout, Map<?, ?> config) {
      super(source, layout, config, true);
      this.setup(function);
      }


//---------------------------
// Accessors and mutators
//---------------------------

   /**
    * Changes the function to apply to the image.
    * @param   function   the new function.
    */
   protected void setUnaryFunction(UnaryFunction<Double, Double> function) {
      this.function = function;
      }

   /**
    * Gets the function to appy to the image.
    * @return   the function.
    */
   protected UnaryFunction<Double, Double> getUnaryFunction() {
      return this.function;
      }


//---------------------------
// Implemented methods from javax.media.jai.ColormapOpImage;
//---------------------------


   /**
    * Transforms the colormap.
    * @param   color_map   the color map.
    */
   @SuppressWarnings({ "boxing", "hiding" })
   @Override
   protected void transformColormap(byte[][] color_map) {
      UnaryFunction<Double, Double> function = this.getUnaryFunction();
      for(int b = 0, bands = color_map.length; b < bands; b++) {
         byte[] map = color_map[b];
         for(int e = 0, entries = map.length; e < entries; e++) {
            double value = map[e] & 0xFF;
            map[e] = (byte) (function.invoke(value) + 0.5);
            }
         }
      }


//---------------------------
// Instance methods
//---------------------------

   /**
    * Sets up this {@link OpImage}.
    * @param   function   the function to apply.
    */
   protected void setup(UnaryFunction<Double, Double> function) {
      this.setUnaryFunction(function);
      this.permitInPlaceOperation();
      this.initializeColormapOperation();
      }

   /**
    * Gets the raster format tag for the raster at the given index.
    * @param   index   the index of the image.
    * @return   the raster format tag.
    */
   protected RasterFormatTag getFormatTag(int index) {
      RasterFormatTag[] tags = this.getFormatTags();
      RasterFormatTag tag = tags[index];
      return tag;
      }

   /**
    * Gets the raster format tag for the given source raster.
    * @param   index   the index of the source image.
    * @return   the raster format tag.
    */
   protected RasterFormatTag getSourceFormatTag(int index) {
      RasterFormatTag tag = this.getFormatTag(index);
      return tag;
      }

   /**
    * Gets the raster format tag for the sink raster.
    * @return   the raster format tag.
    */
   protected RasterFormatTag getSinkFormatTag() {
   // the index of the sink image is the number of images,
   // all the previous indices are for source images
      int index = this.getNumSources();
      RasterFormatTag tag = this.getFormatTag(index);
      return tag;
      }

   /**
    * Builds a raster accessor for rasters associated with this operation.
    * @param   raster   the raster the accessor is for.
    * @param   tag   the format tag for the raster.
    * @param   bounds   the bounds.
    * @param   color_model   the color model.
    * @return   the raster accessor.
    */
   protected RasterAccessor buildRasterAccessor(Raster raster, RasterFormatTag tag, Rectangle bounds, ColorModel color_model) {
      RasterAccessor accessor = new RasterAccessor(raster, bounds, tag, color_model);
      return accessor;
      }

   /**
    * Builds a raster accessor for the sink raster.
    * @param   raster   the sink raster.
    * @param   bounds   the bounds.
    * @return   the raster accessor.
    */
   protected RasterAccessor buildSinkRasterAccessor(WritableRaster raster, Rectangle bounds) {
      RasterFormatTag tag = this.getSinkFormatTag();
      ColorModel color_model = this.getColorModel();
      RasterAccessor sink_raster_accessor = this.buildRasterAccessor(raster, tag, bounds, color_model);
      return sink_raster_accessor;
      }

   /**
    * Builds the raster accessor for the source raster at the given index.
    * @param   raster   the source raster.
    * @param   bounds   the bounds on the sink image.
    * @param   index   the index of the source image.
    * @return   the raster accessor.
    */
   protected RasterAccessor buildSourceRasterAccessor(Raster raster, Rectangle bounds, int index) {
      Rectangle source_bounds = this.mapDestRect(bounds, index);
      RasterFormatTag format_tag = this.getSourceFormatTag(index);
      PlanarImage source = this.getSourceImage(index);
      ColorModel color_model = source.getColorModel();
      RasterAccessor accessor = this.buildRasterAccessor(raster, format_tag, source_bounds, color_model);
      return accessor;
      }

   /**
    * Builds all the source raster accessors.
    * @param   rasters   the source rasters.
    * @param   bounds   the bounds.
    * @return   the raster accessors.
    */
   protected RasterAccessor[] buildSourceRasterAccessors(Raster[] rasters, Rectangle bounds) {
      int len = this.getNumSources();
      RasterAccessor[] accessors = new RasterAccessor[len];
      for (int i = 0; i < len; i++) {
         Raster raster = rasters[i];
         RasterAccessor accessor = this.buildSourceRasterAccessor(raster, bounds, i);
         accessors[i] = accessor;
         }
      return accessors;
      }

   /**
    * Builds the 3-dimensional source matrix.
    * @param   <A>   the type of pixels.
    * @param   sources   the source rasters.
    * @param   specialization   the specialization for the type of pixels.
    * @return   the 3-dimensional source matrix.
    */
   protected <A> A[][] buildAllSourceData(RasterAccessor[] sources, PixelSpecialization<A> specialization) {
      int len = sources.length;
      A[][] data = specialization.buildAllData(len);
      for (int i = 0; i < len; i++) {
         RasterAccessor source = sources[i];
         data[i] = specialization.extractData(source);
         }
      return data;
      }

   /**
    * Resets the line offsets by setting them to their corresponding band
    * offset.
    * @param   band   the current band.
    * @param   band_offsets   the band offsets.
    * @param   line_offsets   the line offsets.
    */
   protected void resetLineOffsets(int band, int[][] band_offsets, int[] line_offsets) {
      for (int i = 0, len = line_offsets.length; i < len; i++) {
         line_offsets[i] = band_offsets[i][band];
         }
      }

   /**
    * Resets the pixel offsets by setting them to their corresponding line
    * offset.
    * @param   line_offsets   the line offsets.
    * @param   pixel_offsets   the pixel offsets.
    */
   protected void resetPixelOffsets(int[] line_offsets, int[] pixel_offsets) {
      for (int i = 0, len = pixel_offsets.length; i < len; i++) {
         pixel_offsets[i] = line_offsets[i];
         }
      }

   /**
    * Increments the pixel offsets by their corresponding pixel strides.
    * @param   pixel_offsets   the pixel offsets.
    * @param   pixel_strides   the pixel strides.
    */
   protected void incrementPixelOffsets(int[] pixel_offsets, int[] pixel_strides) {
      for (int i = 0, len = pixel_offsets.length; i < len; i++) {
         pixel_offsets[i] += pixel_strides[i];
         }
      }

   /**
    * Increments the line offsets by their corresponding line strides.
    * @param   line_offsets   the line offsets.
    * @param   line_strides   the line strides.
    */
   protected void incrementLineOffsets(int[] line_offsets, int[] line_strides) {
      for (int i = 0, len = line_offsets.length; i < len; i++) {
         line_offsets[i] += line_strides[i];
         }
      }

   /**
    * Really computes the image.
    * @param   sources   the source raster accessors.
    * @param   sink   the sink raster accessor.
    * @param   specialization   the pixel specialization.
    * @param   <A>   the type of pixel (as an array of that type).
    */
   @SuppressWarnings({ "boxing", "hiding" })
   protected <A> void compute(RasterAccessor[] sources, RasterAccessor sink, PixelSpecialization<A> specialization) {
// If an image is broken into tiles, method computeRect() is called once for
// each tile, the rectangle bounds defining the tile.
//
// The band offset give the position of the first pixel (in a band), that is the
// pixel at position (0, 0) IN the tile - which can be anywhere in the image.
//
// The line stride gives the distance from a pixel on one line to the
// corresponding pixel on the next line. It can be seen as the width of the
// whole image (an NOT the width of the tile).
//
// The pixel stride gives the distance between to pixels on the same row.
//
// The formula to get to any pixel in the tile is as follow :
//
//   pixel_offset = band_offset + (y * line_stride) + (x * pixel_stride)
//
// where 'x' and 'y' are the coordinate of the pixel IN the tile space (and not
// in the image space).
//
// The image data structure as represented as a matrix. All the pixels of one
// band are laid out in row major configuration in a single array (see the
// formula above) and all the bands are put together in another array, giving
// the matrix. Thus the first dimension of the array represents the band and
// the second represents the pixel. A pixel in band 'b' at position 'p' is
// accessed like that :
//
//    data[b][p]
//
//
// Now, given that, here is how this code works. The pixels are traversed top to
// bottom, left to right (loops on 'w' and on 'h'). Finally this process is
// repeated for each band (loop on 'b').
//
// To prevent the calculation of the pixel offset at each pixel with the formula
// above, and since at any given time we are interested with the same
// corresponding pixel in all images, source and sink alike, the pixel offset
// are calculated incrementally. In any given band, the offset of the first
// pixel, (0, 0) is 'band_offset'. This value, for each image, is kept in the
// line offset memory, the beginning of each new line being :
//
//    'band_offset + (h * line_stride)'
//
// or :
//
//    'band_offset += line_stride'
//
// for each new line after the first one. Using the 'line_offset', the position
// of each pixel is calculated by incrementing a copy of that offset by the
// 'pixel_stride'.

      int images = sources.length;

   // the values of each source image is stored at its corresponding index
   // while the values of the sink image are stored at the last position, at
   // index 'images'
      int[] line_strides = new int[images + 1];
      int[] pixel_strides = new int[images + 1];
      int[][] band_offsets = new int[images + 1][];
      for (int i = 0; i < images; i++) {
         RasterAccessor source = sources[i];
         line_strides[i] = source.getScanlineStride();
         pixel_strides[i] = source.getPixelStride();
         band_offsets[i] = source.getBandOffsets();
         }
      line_strides[images] = sink.getScanlineStride();
      pixel_strides[images] = sink.getPixelStride();
      band_offsets[images] = sink.getBandOffsets();

      int[] line_offsets = new int[images + 1];
      int[] pixel_offsets = new int[images + 1];

      A[][] all_source_data = this.buildAllSourceData(sources, specialization);
      A[] sink_data = specialization.extractData(sink);
      int bands = sink.getNumBands();
      int height = sink.getHeight();
      int width = sink.getWidth();
      UnaryFunction<Double, Double> function = this.getUnaryFunction();

      for (int b = 0; b < bands; b++) {
         this.resetLineOffsets(b, band_offsets, line_offsets);
         for (int h = 0; h < height; h++) {
            this.resetPixelOffsets(line_offsets, pixel_offsets);
            for (int w = 0; w < width; w++) {
               int source_index = 0;
               double value = specialization.extractPixelValueAt(all_source_data, source_index, b, pixel_offsets[0]);

               value = function.invoke(value);

               specialization.setPixelValueAt(value, sink_data, b, pixel_offsets[images]);

               this.incrementPixelOffsets(pixel_offsets, pixel_strides);
               }
            this.incrementLineOffsets(line_offsets, line_strides);
            }
         }
      }

   /**
    * Compute the image for images with bytes as the data type.
    * @param   sources   the source images.
    * @param   sink   the sink image.
    */
   protected void computeByte(RasterAccessor[] sources, RasterAccessor sink) {
      PixelSpecialization<byte[]> specialization = UnaryFunctionOpImage.BYTE_SPECIALIZATION;
      this.compute(sources, sink, specialization);
      }

   /**
    * Compute the image for images with shorts as the data type.
    * @param   sources   the source images.
    * @param   sink   the sink image.
    */
   protected void computeShort(RasterAccessor[] sources, RasterAccessor sink) {
      PixelSpecialization<short[]> specialization = UnaryFunctionOpImage.SHORT_SPECIALIZATION;
      this.compute(sources, sink, specialization);
      }

   /**
    * Compute the image for images with unsigned shorts as the data type.
    * @param   sources   the source images.
    * @param   sink   the sink image.
    */
   protected void computeUShort(RasterAccessor[] sources, RasterAccessor sink) {
      PixelSpecialization<short[]> specialization = UnaryFunctionOpImage.U_SHORT_SPECIALIZATION;
      this.compute(sources, sink, specialization);
      }

   /**
    * Compute the image for images with integers as the data type.
    * @param   sources   the source images.
    * @param   sink   the sink image.
    */
   protected void computeInt(RasterAccessor[] sources, RasterAccessor sink) {
      PixelSpecialization<int[]> specialization = UnaryFunctionOpImage.INT_SPECIALIZATION;
      this.compute(sources, sink, specialization);
      }

   /**
    * Compute the image for images with floats as the data type.
    * @param   sources   the source images.
    * @param   sink   the sink image.
    */
   protected void computeFloat(RasterAccessor[] sources, RasterAccessor sink) {
      PixelSpecialization<float[]> specialization = UnaryFunctionOpImage.FLOAT_SPECIALIZATION;
      this.compute(sources, sink, specialization);
      }

   /**
    * Compute the image for images with doubles as the data type.
    * @param   sources   the source images.
    * @param   sink   the sink image.
    */
   protected void computeDouble(RasterAccessor[] sources, RasterAccessor sink) {
      PixelSpecialization<double[]> specialization = UnaryFunctionOpImage.DOUBLE_SPECIALIZATION;
      this.compute(sources, sink, specialization);
      }

   /**
    * Computes the image.
    * @param   sources   the source image.
    * @param   sink   the median image.
    */
   protected void compute(RasterAccessor[] sources, RasterAccessor sink) {
      int data_type = sink.getDataType();
      switch (data_type) {
         case DataBuffer.TYPE_BYTE:
            this.computeByte(sources, sink);
            break;
         case DataBuffer.TYPE_USHORT:
            this.computeUShort(sources, sink);
            break;
         case DataBuffer.TYPE_SHORT:
            this.computeShort(sources, sink);
            break;
         case DataBuffer.TYPE_INT:
            this.computeInt(sources, sink);
            break;
         case DataBuffer.TYPE_FLOAT:
            this.computeFloat(sources, sink);
            break;
         case DataBuffer.TYPE_DOUBLE:
            this.computeDouble(sources, sink);
            break;
         default:
            String message = this.getUnknownDataTypeErrorMessage(data_type);
            throw new IllegalStateException(message);
         }
      }

   /**
    * Gets the error message telling that the raster data type is unknown.
    * @param   type   the bad type.
    * @return   the formatted error message.
    */
   @SuppressWarnings("boxing")
   protected String getUnknownDataTypeErrorMessage(int type) {
      String key ="Data type";
      String message = "Messages";
      return message;
      }


//---------------------------
// Overridden methods from javax.media.jai.OpImage
//---------------------------

   /**
    * Calculates the median of the corresponding pixels of the source images
    * within a specified rectangle.
    * @param   sources   the cobbled sources.
    * @param   sink      the raster for each calculation.
    * @param   bounds   the region of interest.
    */
   @Override
   protected void computeRect(Raster[] sources, WritableRaster sink, Rectangle bounds) {
      RasterAccessor[] source_accessors = this.buildSourceRasterAccessors(sources, bounds);
      RasterAccessor sink_accessor = this.buildSinkRasterAccessor(sink, bounds);

      this.compute(source_accessors, sink_accessor);

      if (sink_accessor.needsClamping()) {
         sink_accessor.clampDataArrays();
         }
      sink_accessor.copyDataToRaster();
      }


//---------------------------
// Inner classes
//---------------------------

   /**
    * Class {@code PixelSpecialization} specializes pixel manipulation on the
    * array type.
    * <p>
    * It is impossible to use generics on primitive data types, but not on
    * <em>arrays</em> of primitive data type.
    *
    * @param   <A>   the array type (such as {@code byte[]} or {@code int[]}).
    *
    * @author   <a href="mailto:forklabs at gmail.com?subject=ca.forklabs.media.jai.operator.UnaryFunctionOpImage$PixelSpecialization">Daniel Léonard</a>
    * @version $Revision: 1.2 $
    */
   protected static abstract class PixelSpecialization<A> {

      /**
       * Builds the 3-dimensional source matrix. The first dimension represents
       * the different source images, the second dimension represents individual
       * bands inside a single source image and the last dimension contains all
       * the pixels.
       * @param   len   the size of the first dimension, the number of source
       *                images.
       * @return   the source matrix.
       */
      public abstract A[][] buildAllData(int len);

      /**
       * Extracts the 2-dimensional data matrix from the given image raster. The
       * first dimension represents the individual bands inside the image and
       * the last dimension contains all the pixels.
       * @param    accessor   the raster accessor over the image raster.
       * @return   the image matrix.
       */
      public abstract A[] extractData(RasterAccessor accessor);

      /**
       * Extracts the specified pixel, basically doing :
       * <blockquote><code>
       * double value = all_data[image][band][pixel];
       * return value;
       * </code></blockquote>
       * @param   all_data   the 3-dimensional source matrix
       * @param   image   the image position in the source matrix.
       * @param   band   the band position in the source image.
       * @param   pixel   the pixel position in the band.
       * @return   the pixel value.
       */
      public abstract double extractPixelValueAt(A[][] all_data, int image, int band, int pixel);

      /**
       * Changes the given pixel.
       * @param   value   the new pixel value.
       * @param   data   the image data.
       * @param   band   the band position in the image.
       * @param   pixel   the pixel position in the band.
       */
      public abstract void setPixelValueAt(double value, A[] data, int band, int pixel);

      }


//---------------------------
// Class variables
//---------------------------

   /** Specialization for data of type <em>byte</em>. */
   protected static PixelSpecialization<byte[]> BYTE_SPECIALIZATION = new PixelSpecialization<byte[]>() {

      @Override public byte[][][] buildAllData(int len) {
         byte[][][] all_data = new byte[len][][];
         return all_data;
         }

      @Override public byte[][] extractData(RasterAccessor raster_accessor) {
         byte[][] data = raster_accessor.getByteDataArrays();
         return data;
         }

      @Override public double extractPixelValueAt(byte[][][] all_data, int image, int band, int pixel) {
         double value = all_data[image][band][pixel] & 0xff;
         return value;
         }

      @Override public void setPixelValueAt(double value, byte[][] sink_data, int band, int pixel) {
         sink_data[band][pixel] = (byte) (value + 0.5);
         }

      };

   /** Specialization for data of type <em>short</em>. */
   protected static PixelSpecialization<short[]> SHORT_SPECIALIZATION = new PixelSpecialization<short[]>() {

      @Override public short[][][] buildAllData(int len) {
         short[][][] all_data = new short[len][][];
         return all_data;
         }

      @Override public short[][] extractData(RasterAccessor raster_accessor) {
         short[][] data = raster_accessor.getShortDataArrays();
         return data;
         }

      @Override public double extractPixelValueAt(short[][][] all_data, int image, int band, int pixel) {
         double value = all_data[image][band][pixel];
         return value;
         }

      @Override public void setPixelValueAt(double value, short[][] sink_data, int band, int pixel) {
         sink_data[band][pixel] = (short) (value + 0.5);
         }

      };

   /** Specialization for data of type <em>unsigned short</em>. */
   protected static PixelSpecialization<short[]> U_SHORT_SPECIALIZATION = new PixelSpecialization<short[]>() {

      @Override public short[][][] buildAllData(int len) {
         short[][][] all_data = new short[len][][];
         return all_data;
         }

      @Override public short[][] extractData(RasterAccessor raster_accessor) {
         short[][] data = raster_accessor.getShortDataArrays();
         return data;
         }

      @Override public double extractPixelValueAt(short[][][] all_data, int image, int band, int pixel) {
         double value = all_data[image][band][pixel] & 0xffff;
         return value;
         }

      @Override public void setPixelValueAt(double value, short[][] sink_data, int band, int pixel) {
         sink_data[band][pixel] = (short) (value + 0.5);
         }

      };

   /** Specialization for data of type <em>int</em>. */
   protected static PixelSpecialization<int[]> INT_SPECIALIZATION = new PixelSpecialization<int[]>() {

      @Override public int[][][] buildAllData(int len) {
         int[][][] all_data = new int[len][][];
         return all_data;
         }

      @Override public int[][] extractData(RasterAccessor raster_accessor) {
         int[][] data = raster_accessor.getIntDataArrays();
         return data;
         }

      @Override public double extractPixelValueAt(int[][][] all_data, int image, int band, int pixel) {
         double value = all_data[image][band][pixel];
         return value;
         }

      @Override public void setPixelValueAt(double value, int[][] sink_data, int band, int pixel) {
         sink_data[band][pixel] = (int) (value + 0.5);
         }

      };

   /** Specialization for data of type <em>float</em>. */
   protected static PixelSpecialization<float[]> FLOAT_SPECIALIZATION = new PixelSpecialization<float[]>() {

      @Override public float[][][] buildAllData(int len) {
         float[][][] all_data = new float[len][][];
         return all_data;
         }

      @Override public float[][] extractData(RasterAccessor raster_accessor) {
         float[][] data = raster_accessor.getFloatDataArrays();
         return data;
         }

      @Override public double extractPixelValueAt(float[][][] all_data, int image, int band, int pixel) {
         double value = all_data[image][band][pixel];
         return value;
         }

      @Override public void setPixelValueAt(double value, float[][] sink_data, int band, int pixel) {
         sink_data[band][pixel] = (float) value;
         }

      };

   /** Specialization for data of type <em>double</em>. */
   protected static PixelSpecialization<double[]> DOUBLE_SPECIALIZATION = new PixelSpecialization<double[]>() {

      @Override public double[][][] buildAllData(int len) {
         double[][][] all_data = new double[len][][];
         return all_data;
         }

      @Override public double[][] extractData(RasterAccessor raster_accessor) {
         double[][] data = raster_accessor.getDoubleDataArrays();
         return data;
         }

      @Override public double extractPixelValueAt(double[][][] all_data, int image, int band, int pixel) {
         double value = all_data[image][band][pixel];
         return value;
         }

      @Override public void setPixelValueAt(double value, double[][] sink_data, int band, int pixel) {
         sink_data[band][pixel] = value;
         }

      };

   }

