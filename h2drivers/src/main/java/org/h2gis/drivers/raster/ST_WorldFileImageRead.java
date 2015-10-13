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
import java.sql.SQLException;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

/**
 * A function to read a worldfile image.
 * A world file establish an image-to-world transformation that converts 
 * the image coordinates to real-world coordinates.
 * 
 * @author Erwan Bocher
 */
public class ST_WorldFileImageRead extends AbstractFunction implements ScalarFunction{

    public ST_WorldFileImageRead(){
        addProperty(PROP_REMARKS, "Import a world file image into a new table.\n"
                + "Supported formats are : \n"
                + "- png with pgw, pngw,\n"
                + "- bmp with bpw or bmpw,\n"
                + "- gif with gfw or gifw,\n"
                + "- jpeg with jpw, jgw, jpgw or jpegw,\n"
                + "- jpg with jpw, jgw, jpgw or jpegw,\n"
                + "- tif with tfw or tifw,\n"
                + "- tiff with tfw or tiffw.\n"
                + "and wld for all listed formats");
    }
    @Override
    public String getJavaStaticMethod() {
        return "worldFileImageRead";
    }
    
    /**
     * Copy data from  world image file into a new table in specified connection.
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws SQLException
     * @throws IOException 
     */
    public static void worldFileImageRead(Connection connection, String fileName, String tableReference) throws SQLException, IOException{
        WorldFileImageReader rasterWorldFileReader = new WorldFileImageReader();
        rasterWorldFileReader.read(URIUtility.fileFromString(fileName), tableReference, connection, new EmptyProgressVisitor());
    }
    
    /**
     * Copy data from world image file into a new table in specified connection.
     *
     *
     * @param connection
     * @param fileName
     * @throws IOException
     * @throws SQLException
     */
    public static void worldFileImageRead(Connection connection, String fileName) throws IOException, SQLException {
        final String name = URIUtility.fileFromString(fileName).getName();
        worldFileImageRead(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }
}
