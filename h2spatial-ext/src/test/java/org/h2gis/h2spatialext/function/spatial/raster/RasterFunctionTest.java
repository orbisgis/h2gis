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

import com.sun.media.jai.opimage.FilteredSubsampleOpImage;
import com.vividsolutions.jts.geom.Coordinate;
import org.h2.jdbc.JdbcSQLException;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.Utils;
import org.h2.util.imageio.WKBRasterReader;
import org.h2.util.imageio.WKBRasterReaderSpi;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.media.jai.DataBufferFloat;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Erwan Bocher
 */
public class RasterFunctionTest {

    private static Connection connection;
    private Statement st;

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(RasterFunctionTest.class.getSimpleName(), false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }
    
    @Test
    public void testST_BAND1() throws Exception {
        
        BufferedImage image = new BufferedImage(5, 5, BufferedImage
                .TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int red = 0;
                int green = 0;
                int blue = 255;
                raster.setPixel(x, y, new int[]{red, green, blue});
            }
        }
        
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        // Create table with test image
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) " +
                "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(image
                , 1, -1, 0, 0, 0, 0, 27572, 0)
                .asWKBRaster());
        ps.execute();
        ps.close();        
        ResultSet rs = st.executeQuery("select st_band(the_raster, 3) from test;");
        assertTrue(rs.next());
        Blob blob = rs.getBlob(1);
        RasterUtils.RasterMetaData metaData = RasterUtils.RasterMetaData
                .fetchMetaData(blob.getBinaryStream());
        assertEquals(5, metaData.width);
        assertEquals(5, metaData.height);
        assertEquals(1, metaData.numBands);
        
        ImageInputStream inputStream = ImageIO.createImageInputStream(blob);
        assertTrue(inputStream != null);
        // Fetch WKB Raster Image reader
        Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
        assertTrue(readers.hasNext());
        ImageReader wkbReader = readers.next();
        // Feed WKB Raster Reader with blob data
        wkbReader.setInput(inputStream);
        // Retrieve data as a BufferedImage
        BufferedImage imageRes = wkbReader.read(wkbReader.getMinIndex());
        int pixelsSource[] = new int[]{255, 255, 255, 255, 255,
            255, 255, 255, 255, 255,
            255, 255, 255, 255, 255,
            255, 255, 255, 255, 255,
            255, 255, 255, 255, 255};
        int pixelsDest[] = imageRes.getData().getPixels(0, 0, imageRes.getWidth(),
                imageRes.getHeight(),(int[])null);
        Assert.assertArrayEquals(pixelsSource, pixelsDest);            
        rs.close();
    }

    private static double dist(int x, int y, int targetX, int targetY) {
        return Math.sqrt(Math.pow(targetX - x,2) + Math.pow(targetY - y,2));
    }


    private static BufferedImage getTestImage(final int width,final int
            height, int... bands) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage
                .TYPE_INT_RGB);
        final double maxDist = Math.sqrt(width*width+height*height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = (int)(255 * dist(x,y,0,0) / maxDist);
                int green = (int)(255 * dist(x,y,width,height / 2) / maxDist);
                int blue = (int)(255 * dist(x,y,0,height) / maxDist);
                int[] bandSrc = new int[]{red, green, blue};
                int[] bandDst = new int[3];
                int j = 0;
                for(int i : bands) {
                    bandDst[j++] = bandSrc[i];
                }
                image.setRGB(x, y, (bandDst[0] << 16) | (bandDst[1] << 8) | bandDst[2]);
            }
        }
        return image;
    }

    private static BufferedImage getImageRegion(Blob blob, Rectangle
            rectangle) throws IOException {
        ImageInputStream inputStream = ImageIO.createImageInputStream(blob);
        // Fetch WKB Raster Image reader
        Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
        ImageReader wkbReader = readers.next();
        // Feed WKB Raster Reader with blob data
        wkbReader.setInput(inputStream);
        // Retrieve data as a BufferedImage
        ImageReadParam param = wkbReader.getDefaultReadParam();
        param.setSourceRegion(rectangle);
        return wkbReader.read(wkbReader.getMinIndex(), param);
    }

    public static void writePlainPGM(RenderedImage image, File path) throws IOException {
        Raster data = image.getData();
        BufferedWriter writer = null;
        try {
            final int width = image.getWidth();
            final int height = image.getHeight();
            writer = new BufferedWriter(new FileWriter(path));
            // write header
            writer.write("P2\n");
            // Write width height
            writer.write(String.valueOf(image.getWidth()));
            writer.write(" ");
            writer.write(String.valueOf(image.getHeight()));
            writer.write("\n");
            // Write max value
            int[] pixelValues = data.getPixels(0, 0, width, height, (int[]) null);
            int maxValue = Integer.MIN_VALUE;
            for (int pixelValue : pixelValues) {
                maxValue = Math.max(pixelValue, maxValue);
            }
            String maxVal = String.valueOf(maxValue);
            writer.write(maxVal);
            writer.write("\n");
            final String format = "%0"+maxVal.length()+"d";
            // Write pixels values
            int lineCarCount = 0;
            for(int y = 0; y < height; y++) {
                for(int x=0; x < width; x++) {
                    String val = String.format(format ,pixelValues[y*width + x])+ " ";
                    if(val.length() + lineCarCount > 70 || (x == 0 && y > 0)) {
                        writer.write("\n");
                        lineCarCount = val.length();
                    } else {
                        lineCarCount += val.length();
                    }
                    writer.write(val);
                }
            }
        } finally {
            if(writer != null) {
                writer.close();
            }
        }

    }

    /**
     * Compare image buffer. Does not take account of ColorModel.
     * @param expectedImage
     * @param imageB
     */
    public static void assertImageBufferEquals(RenderedImage expectedImage, RenderedImage imageB) {
        int[] pixelsExpected = expectedImage.getData().getPixels(0,0,expectedImage.getWidth(),
                expectedImage.getHeight(),(int[])null);
        int[] pixelsSource = imageB.getData().getPixels(0, 0, imageB.getWidth(), imageB.getHeight(), (int[]) null);
        assertArrayEquals(pixelsExpected, pixelsSource);
    }

    /**
     * Compare the rendered result of the two provided image.
     * @param expectedImage
     * @param imageB
     */
    public static void assertImageEquals(RenderedImage expectedImage, RenderedImage imageB) {
        BufferedImage expectedImageDest = new BufferedImage(expectedImage.getWidth(),expectedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        BufferedImage destB = new BufferedImage(imageB.getWidth(),imageB.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = expectedImageDest.createGraphics();
        g.drawRenderedImage(expectedImage, new AffineTransform());
        g.dispose();
        g = destB.createGraphics();
        g.drawRenderedImage(imageB, new AffineTransform());
        g.dispose();
        assertImageBufferEquals(expectedImageDest, destB);
    }

    @Test
    public void testPlanarImage() throws Exception {
        PlanarImage input = JAI.create("fileload", RasterFunctionTest.class.getResource("calibration.png").getPath());
        GeoRasterRenderedImage geoRaster = GeoRasterRenderedImage.create(input, 1, -1, 0, 0, 0, 0, 0, 0);
        ImageInputStream is = new MemoryCacheImageInputStream(geoRaster.asWKBRaster());
        WKBRasterReader reader = new WKBRasterReader(new WKBRasterReaderSpi());
        reader.setInput(is);
        RenderedImage img = reader.read(reader.getMinIndex());
        //JAI.create("filestore",img,"/tmp/rastertest/calibration.png","PNG");
        assertImageEquals(input, img);
    }

    @Test
    public void testST_BANDMultiBand() throws Exception{
        Statement stat = connection.createStatement();
        // Declare custom function for rescaling image
        stat.execute("drop table if exists test");
        stat.execute("create table test(id identity, data raster)");
        // Create table with test image
        PreparedStatement st = connection.prepareStatement("INSERT INTO TEST(data) " +
                "values(?)");
        BufferedImage srcImage = getTestImage(10, 10, 0, 1, 2);
        st.setBinaryStream(1,
                GeoRasterRenderedImage.create(srcImage, 1, -1, 0, 0, 0, 0, 27572, 0).asWKBRaster
                        ());
        st.execute();
        // Call ST_BAND
        ResultSet rs = stat.executeQuery("SELECT ST_BAND(DATA, 3,2,1) rast from test");
        assertTrue(rs.next());
        RasterUtils.RasterMetaData metaData = RasterUtils.RasterMetaData
                .fetchMetaData(rs.getBinaryStream(1));
        assertEquals(3, metaData.numBands);
        // Get image portion
        BufferedImage image = getImageRegion(rs.getBlob(1), new Rectangle(0, 0, 10, 10));
        // Produce expected image
        RenderedImage expectedImage = getTestImage(10, 10, 2, 1, 0);
        // Compare pixels
        //JAI.create("filestore",image,"/tmp/rastertest/image.png","PNG");
        //JAI.create("filestore", expectedImage, "/tmp/rastertest/expectedImage.png", "PNG");
        assertImageEquals(expectedImage, image);
        rs.close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testST_BAND2() throws Exception, Throwable {

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int red = 0;
                int green = 0;
                int blue = 255;
                raster.setPixel(x, y, new int[]{red, green, blue});
            }
        }

        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        // Create table with test image
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(image, 1, -1, 0, 0, 0, 0, 27572, 0)
                .asWKBRaster());
        ps.execute();
        ps.close();
        try {
            st.execute("select st_band(the_raster, -1) from test;");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testST_BAND3() throws Exception, Throwable {

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int red = 0;
                int green = 0;
                int blue = 255;
                raster.setPixel(x, y, new int[]{red, green, blue});
            }
        }

        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        // Create table with test image
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(image, 1, -1, 0, 0, 0, 0, 27572, 0)
                .asWKBRaster());
        ps.execute();
        ps.close();
        try {
            st.execute("select st_band(the_raster, 0) from test;");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
    
     @Test(expected = IllegalArgumentException.class)
    public void testST_BAND4() throws Exception, Throwable {

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int red = 0;
                int green = 0;
                int blue = 255;
                raster.setPixel(x, y, new int[]{red, green, blue});
            }
        }

        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        // Create table with test image
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(image, 1, -1, 0, 0, 0, 0, 27572, 0)
                .asWKBRaster());
        ps.execute();
        ps.close();
        try {
            st.execute("select st_band(the_raster, 5, 6, 7) from test;");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
     @Test(expected = IllegalArgumentException.class)
    public void testST_BAND5() throws Exception, Throwable {

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int red = 0;
                int green = 0;
                int blue = 255;
                raster.setPixel(x, y, new int[]{red, green, blue});
            }
        }

        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        // Create table with test image
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(image, 1, -1, 0, 0, 0, 0, 27572, 0)
                .asWKBRaster());
        ps.execute();
        ps.close();
        try {
            st.execute("select st_band(the_raster, 1, 2, -1) from test;");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

    private static RenderedImage imageFromArray(float[] array, int width, int height) {
        DataBufferFloat dbuffer = new DataBufferFloat(array, width * height);
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point());
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    private static RenderedImage imageFromArray(int[] array, int width, int height) {
        DataBufferInt dbuffer = new DataBufferInt(array, width * height);
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_INT, width, height, 1);
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point());
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    private static RenderedImage imageFromArray(byte[] array, int width, int height) {
        DataBufferByte dbuffer = new DataBufferByte(array, width * height);
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 1);
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point());
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    private static RenderedImage imageFromArray(short[] array, int width, int height) {
        DataBufferShort dbuffer = new DataBufferShort(array, width * height);
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_SHORT, width, height, 1);
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point());
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    private static RenderedImage imageFromArray(double[] array, int width, int height) {
        DataBufferDouble dbuffer = new DataBufferDouble(array, width * height);
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_DOUBLE, width, height, 1);
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dbuffer, new Point());
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    @Test
    public void testST_D8SlopeFloat() throws SQLException, IOException {
        int width = 100;
        int height = 100;
        final double noData = -1;
        final float pixelSize = 100;
        final float slope = 0.1f;
        float[] imageData = new float[width * height];
        for(int y =0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                imageData[y * width + x] = 50 + x * pixelSize * slope;
            }
        }
        // Create image from int array
        RenderedImage im = imageFromArray(imageData, width, height);
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, height, 0, 0, 27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8SLOPE
        ResultSet rs = st.executeQuery("SELECT ST_D8Slope(the_raster, 'PERCENT') the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        Raster rasterSlope = wkbRasterImage.getData();

        for(int y =0; y < rasterSlope.getHeight(); y++) {
            for(int x = 0; x < rasterSlope.getWidth(); x++) {
                double value = rasterSlope.getSampleDouble(x, y, 0);
                if(!Double.isNaN(value) && value != noData) {
                    assertTrue(Double.compare(value, 0) != 0);
                    assertEquals(slope, value / 100, 1e-8);
                }
            }
        }
        rs.close();
    }



    @Test
    public void testST_D8SlopeInt() throws SQLException, IOException {
        int width = 100;
        int height = 100;
        final double noData = -1;
        final float pixelSize = 100;
        final float slope = 0.1f;
        int[] imageData = new int[width * height];
        for(int y =0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                imageData[y * width + x] = (int)(50 + x * pixelSize * slope);
            }
        }
        // Create image from int array
        RenderedImage im = imageFromArray(imageData, width, height);
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, height, 0, 0, 27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8SLOPE
        ResultSet rs = st.executeQuery("SELECT ST_D8Slope(the_raster, 'PERCENT') the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        Raster rasterSlope = wkbRasterImage.getData();

        for(int y =0; y < rasterSlope.getHeight(); y++) {
            for(int x = 0; x < rasterSlope.getWidth(); x++) {
                double value = rasterSlope.getSampleDouble(x, y, 0);
                if(!Double.isNaN(value) && value != noData) {
                    assertTrue(Double.compare(value, 0) != 0);
                    assertEquals(slope, value / 100, 1e-8);
                }
            }
        }
        rs.close();
    }



    @Test
    public void testST_D8SlopeByte() throws SQLException, IOException {
        int width = 10;
        int height = 10;
        final double noData = 0;
        final float pixelSize = 10;
        final float slope = 0.1f;
        byte[] imageData = new byte[width * height];
        for(int y =0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                imageData[y * width + x] = (byte)(Byte.MIN_VALUE + 1 + x * pixelSize * slope);
            }
        }
        // Create image from int array
        RenderedImage im = imageFromArray(imageData, width, height);
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, height, 0, 0, 27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8SLOPE
        ResultSet rs = st.executeQuery("SELECT ST_D8Slope(the_raster, 'PERCENT') the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        Raster rasterSlope = wkbRasterImage.getData();

        for(int y =0; y < rasterSlope.getHeight(); y++) {
            for(int x = 0; x < rasterSlope.getWidth(); x++) {
                double value = rasterSlope.getSampleDouble(x, y, 0);
                if(!Double.isNaN(value) && value != noData) {
                    assertTrue(Double.compare(value, 0) != 0);
                    assertEquals(slope, value / 100, 1e-8);
                }
            }
        }
        rs.close();
    }


    @Test
    public void testST_D8SlopeShort() throws SQLException, IOException {
        int width = 100;
        int height = 100;
        final double noData = -1;
        final float pixelSize = 100;
        final float slope = 0.1f;
        short[] imageData = new short[width * height];
        for(int y =0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                imageData[y * width + x] = (short)(50 + x * pixelSize * slope);
            }
        }
        // Create image from int array
        RenderedImage im = imageFromArray(imageData, width, height);
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, height, 0, 0, 27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8SLOPE
        ResultSet rs = st.executeQuery("SELECT ST_D8Slope(the_raster, 'PERCENT') the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        Raster rasterSlope = wkbRasterImage.getData();

        for(int y =0; y < rasterSlope.getHeight(); y++) {
            for(int x = 0; x < rasterSlope.getWidth(); x++) {
                double value = rasterSlope.getSampleDouble(x, y, 0);
                if(!Double.isNaN(value) && value != noData) {
                    assertTrue(Double.compare(value, 0) != 0);
                    assertEquals(slope, value / 100, 1e-8);
                }
            }
        }
        rs.close();
    }


    @Test
    public void testST_D8SlopeDouble() throws SQLException, IOException {
        int width = 100;
        int height = 100;
        final double noData = -1;
        final float pixelSize = 100;
        final float slope = 0.1f;
        double[] imageData = new double[width * height];
        for(int y =0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                imageData[y * width + x] = (short)(50 + x * pixelSize * slope);
            }
        }
        // Create image from int array
        RenderedImage im = imageFromArray(imageData, width, height);
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, height, 0, 0, 27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8SLOPE
        ResultSet rs = st.executeQuery("SELECT ST_D8Slope(the_raster, 'PERCENT') the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        Raster rasterSlope = wkbRasterImage.getData();

        for(int y =0; y < rasterSlope.getHeight(); y++) {
            for(int x = 0; x < rasterSlope.getWidth(); x++) {
                double value = rasterSlope.getSampleDouble(x, y, 0);
                if(!Double.isNaN(value) && value != noData) {
                    assertTrue(Double.compare(value, 0) != 0);
                    assertEquals(slope, value / 100, 1e-8);
                }
            }
        }
        rs.close();
    }

    private static RenderedImage readImage(URL url) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(url.getFile(), "r");
        ImageInputStream iis = ImageIO.createImageInputStream(randomAccessFile);
        try {
            Iterator<ImageReader> itReaders = ImageIO.getImageReaders(iis);
            ImageReader imageReader = itReaders.next();
            imageReader.setInput(iis);
            return imageReader.readAsRenderedImage(imageReader.getMinIndex(), imageReader.getDefaultReadParam());
        } finally {
            iis.close();
        }
    }


    private static void writeImage(URL url,RenderedImage im, String format) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(url.getFile(), "rw");
        ImageOutputStream ios = ImageIO.createImageOutputStream(randomAccessFile);
        try {
            Iterator<ImageWriter> itWriters = ImageIO.getImageWritersByFormatName(format);
            ImageWriter imageWriter = itWriters.next();
            imageWriter.setOutput(ios);
            imageWriter.write(new IIOImage(im,null,null));
        } finally {
            ios.close();
        }
    }

    @Test
    public void testST_D8FlowDirection() throws SQLException, IOException {
        double pixelSize = 15;
        double noData = -2;
        // Read unit test image
        RenderedImage im = readImage(RasterFunctionTest.class.getResource("dem1.pgm"));
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, pixelSize * im.getHeight(),
                0, 0,  27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8FlowDirection
        ResultSet rs = st.executeQuery("SELECT ST_D8FlowDirection(the_raster) the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        //writePlainPGM(wkbRasterImage, new File("target/expect.pgm"));
        RenderedImage expectedImage = readImage(RasterFunctionTest.class.getResource("dem1_expected.pgm"));
        assertImageBufferEquals(expectedImage, wkbRasterImage);
    }

    @Test
    public void testST_D8FlowDirectionSink() throws SQLException, IOException {
        double pixelSize = 15;
        double noData = -2;
        // Read unit test image
        RenderedImage im = readImage(RasterFunctionTest.class.getResource("dem2.pgm"));
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, pixelSize * im.getHeight(),
                0, 0,  27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8FlowDirection
        ResultSet rs = st.executeQuery("SELECT ST_D8FlowDirection(the_raster) the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        //writePlainPGM(wkbRasterImage, new File("target/expect.pgm"));
        RenderedImage expectedImage = readImage(RasterFunctionTest.class.getResource("dem2_expected.pgm"));
        assertImageBufferEquals(expectedImage, wkbRasterImage);
    }

    @Test
    public void testST_D8FlowDirectionDouble() throws SQLException, IOException {

        int width = 15;
        int height = 15;
        final double noData = -1;
        final float pixelSize = 100;
        double[] imageData = new double[width * height];
        Coordinate mountPos = new Coordinate(width / 2 + 0.5, height / 2 + 0.5);
        for(int y =0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                imageData[y * width + x] = (1 / new Coordinate(x, y).distance(mountPos)) * width;
            }
        }
        // Create image from int array
        RenderedImage im = imageFromArray(imageData, width, height);
        // Store into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) " + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, height, 0, 0, 27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8FlowDirection
        ResultSet rs = st.executeQuery("SELECT ST_D8FlowDirection(the_raster) the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        RenderedImage expectedImage = readImage(RasterFunctionTest.class.getResource("dem3_expected.pgm"));
        assertImageBufferEquals(expectedImage, wkbRasterImage);
    }



    private void testST_D8FlowAccumulation() throws SQLException, IOException {
        double pixelSize = 15;
        double noData = 0;
        // Read unit test image
        RenderedImage im = readImage(RasterFunctionTest.class.getResource("flowDir1.pgm"));
        // Store direction into H2 DB
        st.execute("drop table if exists test");
        st.execute("create table test(id identity, the_raster raster)");
        PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST(the_raster) "
                + "values(?)");
        ps.setBinaryStream(1, GeoRasterRenderedImage.create(im, pixelSize, -pixelSize, 0, pixelSize * im.getHeight(),
                0, 0,  27572, noData)
                .asWKBRaster());
        ps.execute();
        ps.close();

        // Call ST_D8FlowDirection
        ResultSet rs = st.executeQuery("SELECT ST_D8FlowAccumulation(the_raster) the_raster from test");
        assertTrue(rs.next());
        RenderedImage wkbRasterImage = (RenderedImage)rs.getObject(1);
        // Check values
        //writePlainPGM(wkbRasterImage, new File("target/expect.pgm"));
        RenderedImage expectedImage = readImage(RasterFunctionTest.class.getResource("flowDir1_expected.pgm"));
        assertImageBufferEquals(expectedImage, wkbRasterImage);
    }

    @Test
    public void testST_D8FlowAccumulationCached() throws SQLException, IOException {
        System.setProperty(CreateSpatialExtension.RASTER_PROCESSING_IN_MEMORY_KEY, String.valueOf(false));
        testST_D8FlowAccumulation();
    }

    @Test
    public void testST_D8FlowAccumulationMemory() throws SQLException, IOException {
        System.setProperty(CreateSpatialExtension.RASTER_PROCESSING_IN_MEMORY_KEY, String.valueOf(true));
        testST_D8FlowAccumulation();
    }
}
