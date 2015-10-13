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
package org.h2gis.drivers.raster;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.util.StringUtils;
import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class WorldImageImportExportTest {

    private Statement st;
    private static Connection connection;
    private static final String DB_NAME = "WorldFileImageImportExportTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_WorldFileImageRead(), "");
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
    public void importRasterFile1() throws SQLException, IOException {
        st.execute("select ST_WorldFileImageRead(" + StringUtils.quoteStringSQL(WorldImageImportExportTest.class.getResource("remote_sensing.png").getPath())
                + ")");
        ResultSet rs = st.executeQuery("select the_raster from remote_sensing;");
        rs.next();
        
        rs.close();

    }
    
     @Test
    public void importRasterFile2() throws SQLException, IOException {
        st.execute("select ST_WorldFileImageRead(" + StringUtils.quoteStringSQL(WorldImageImportExportTest.class.getResource("remote_sensing.png").getPath())
                + ", 'myraster')");
        ResultSet rs = st.executeQuery("select the_raster from myraster;");
        rs.next();
        
        rs.close();

    }
}
