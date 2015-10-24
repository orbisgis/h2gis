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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import org.h2.jdbc.JdbcSQLException;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.imageio.WKBRasterReader;
import org.h2.util.imageio.WKBRasterReaderSpi;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

    public static void assertImageEquals(RenderedImage expectedImage, RenderedImage imageB) {
        BufferedImage expectedImageDest = new BufferedImage(expectedImage.getWidth(),expectedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        BufferedImage destB = new BufferedImage(imageB.getWidth(),imageB.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = expectedImageDest.createGraphics();
        g.drawRenderedImage(expectedImage, new AffineTransform());
        g.dispose();
        g = destB.createGraphics();
        g.drawRenderedImage(imageB, new AffineTransform());
        g.dispose();
        int[] pixelsExpected = expectedImageDest.getData().getPixels(0,0,expectedImageDest.getWidth(),
                expectedImageDest.getHeight(),(int[])null);
        int[] pixelsSource = destB.getData().getPixels(0, 0, destB.getWidth(), destB.getHeight(), (int[]) null);
        assertArrayEquals(pixelsExpected, pixelsSource);
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
    
}
