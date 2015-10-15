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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.h2.jdbc.JdbcSQLException;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
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
        
        BufferedImage image = new BufferedImage(10, 10, BufferedImage
                .TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
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
        assertEquals(10, metaData.width);
        assertEquals(10, metaData.height);
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
        int[] iArray = new int[1];        
        imageRes.getRaster().getPixel(5, 5, iArray);        
        assertEquals(0, iArray[1]);        
        rs.close();
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void testST_BAND2() throws Exception, Throwable {

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
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

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
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

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
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

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
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
