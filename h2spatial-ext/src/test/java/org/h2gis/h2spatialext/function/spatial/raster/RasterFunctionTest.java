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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
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
        ResultSet rs = st.executeQuery("select st_band(the_raster, 2) from test;");
        assertTrue(rs.next());
        RasterUtils.RasterMetaData metaData = RasterUtils.RasterMetaData
                .fetchMetaData(rs.getBinaryStream(1));
        assertEquals(10, metaData.width);
        assertEquals(10, metaData.height);
        assertEquals(1, metaData.numBands);
        rs.close();
    }
    
}
