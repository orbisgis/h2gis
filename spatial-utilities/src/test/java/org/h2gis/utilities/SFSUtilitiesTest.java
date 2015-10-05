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
package org.h2gis.utilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNull;

/**
 * Test SFSUtilities
 *
 * @author Nicolas Fortin
 */
public class SFSUtilitiesTest {

    private static Connection connection;
    private static Statement st;

    @BeforeClass
    public static void init() throws Exception {
        String dataBaseLocation = new File("target/SFSUtilitiesTest").getAbsolutePath();
        String databasePath = "jdbc:h2:"+dataBaseLocation;
        File dbFile = new File(dataBaseLocation+".mv.db");
        Class.forName("org.h2.Driver");
        if(dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(databasePath, "sa", "");
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterClass
    public static void dispose() throws Exception {
        connection.close();
    }

    @Test
    public void testGeometryTypeConvert() throws ParseException {
        WKTReader wktReader = new WKTReader();
        assertEquals(GeometryTypeCodes.POINT, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("POINT(1 1)")));
        assertEquals(GeometryTypeCodes.LINESTRING, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("LINESTRING(1 1, 2 2)")));
        assertEquals(GeometryTypeCodes.POLYGON, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOINT, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("MULTIPOINT((1 1))")));
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("MULTILINESTRING((1 1, 2 2))")));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(wktReader.read("GEOMETRYCOLLECTION(POINT(1 1))")));
    }

    @Test
    public void testCoordinateTransform() {
        // With rotation2
        RasterMetaData rasterMetaData = new RasterMetaData(1000, 1000, 100, 100, 1, 1, 0.5,
                0.5, 27572, 0);
        assertEquals(new Coordinate(1051, 1051), rasterMetaData.getPixelCoordinate(34, 34));
        Assert.assertArrayEquals(new int[]{33, 33},
                rasterMetaData.getPixelFromCoordinate(new Coordinate(1050, 1050)));
    }

    @Test
    public void testStMetaData() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEST");
        st.execute("CREATE TABLE TEST(ID SERIAL, RAST RASTER)");
        st.execute("INSERT INTO TEST(RAST) VALUES (ST_MAKEEMPTYRASTER(256, 512, 47.65318, -2.74131, 0.0001, -0.0001, " +
                "0, 0, 4326))");
        st.execute("INSERT INTO TEST(RAST) VALUES (NULL)");
        ResultSet rs = st.executeQuery("SELECT ST_METADATA(RAST) RASTMETA FROM TEST ORDER BY ID");
        try {
            assertTrue(rs.next());
            RasterMetaData metaData = RasterMetaData.fetchRasterMetaData(rs, "rastmeta");
            assertNotNull(metaData);
            assertEquals(256, metaData.getWidth());
            assertEquals(512, metaData.getHeight());
            assertEquals(47.65318, metaData.getUpperLeftX(), 1e-5);
            assertEquals(-2.74131, metaData.getUpperLeftY(), 1e-5);
            assertEquals(0.0001, metaData.getScaleX(), 1e-4);
            assertEquals(-0.0001, metaData.getScaleY(), 1e-4);
            assertEquals(0.0, metaData.getSkewX(), 1e-12);
            assertEquals(0.0, metaData.getSkewY(), 1e-12);
            assertEquals(0, metaData.getNumBands());
            assertTrue(rs.next());
            metaData = RasterMetaData.fetchRasterMetaData(rs, "rastmeta");
            assertNull(metaData);
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
    }
}
