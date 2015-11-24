/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 * <p/>
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 * <p/>
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.raster.cache;

import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.Utils;
import org.h2gis.utilities.JDBCUtilities;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Image stored in temporary table
 * @author Nicolas Fortin
 */
public class JDBCBuffer implements StoredImage {
    private String tempTableName;
    private static int globalCpt = 0;
    private Connection connection;
    private ResultSet rs = null;
    private RenderedImage storedImage = null;
    private boolean dropIntermediate;

    public JDBCBuffer(RenderedImage image, Connection connection, RasterUtils.RasterMetaData metaData) throws
            SQLException, IOException {
        globalCpt++;
        dropIntermediate = Utils.getProperty("h2gis.dropTableCache", true);
        this.connection = connection;
        tempTableName = findUniqueName();
        Statement st = connection.createStatement();
        try {
            String tmp = "TEMPORARY";
            if(!dropIntermediate) {
                tmp = "";
            }
            PreparedStatement pst = connection.prepareStatement(
                    "CREATE "+tmp+" TABLE " + tempTableName + "(the_raster raster) as select ?::raster;");
            try {
                InputStream inputStream = GeoRasterRenderedImage.create(image, metaData).asWKBRaster();
                try {
                    pst.setBinaryStream(1, inputStream);
                    pst.execute();
                } finally {
                    inputStream.close();
                }
            } finally {
                pst.close();
            }
        } finally {
            st.close();
        }
    }

    @Override
    public RenderedImage getImage() throws SQLException {
        if(storedImage == null) {
            Statement st = connection.createStatement();
            rs = st.executeQuery("SELECT THE_RASTER FROM "+tempTableName);
            try {
                rs.next();
                storedImage = (RenderedImage)rs.getObject(1);
            } finally {
                rs.close();
            }
        }
        return storedImage;
    }

    private String findUniqueName() throws SQLException {
        int cpt = globalCpt;
        String testName = "TMP_FLOWACCUM_"+ cpt;
        while(JDBCUtilities.tableExists(connection, testName)) {
            testName = "TMP_FLOWACCUM_"+ ++cpt;
        }
        return testName;
    }

    @Override
    public void free() throws SQLException {
        if(rs != null) {
            rs.close();
        }
        Statement st = connection.createStatement();
        try {
            if(dropIntermediate) {
                st.execute("DROP TABLE IF EXISTS " + tempTableName);
            }
        } finally {
            st.close();
        }
    }
}