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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.h2gis.drivers.utility.PRJUtil;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

/**
 * Methods to read a georeferenced image
 * 
 * @author Erwan Bocher
 */
public class WorldFileImageReader {
    
    private static Map<String, String[]> worldFileExtensions;
    
    private float scaleX = 1;

    private float scaleY = -1;

    private float skewX = 0;

    private float skewY = 0;

    private double upperLeftX = 0;

    private double upperLeftY = 0;
    
    private int srid=0;
    
    static {
		worldFileExtensions = new HashMap<String, String[]>();
		worldFileExtensions.put("tif", new String[] { "tfw", "tifw" , "wld"});
		worldFileExtensions.put("tiff", new String[] { "tfw", "tiffw" , "wld"});
		worldFileExtensions.put("jpg", new String[] { "jpw", "jgw", "jpgw",
				"jpegw","wld" });
		worldFileExtensions.put("jpeg", new String[] { "jpw", "jgw", "jpgw",
				"jpegw","wld" });
		worldFileExtensions.put("gif", new String[] { "gfw", "gifw","wld" });
		worldFileExtensions.put("bmp", new String[] { "bpw", "bmpw","wld" });
		worldFileExtensions.put("png", new String[] { "pgw", "pngw","wld" });
	}
    private String fileNameExtension;
    private String filePathWithoutExtension;
    private File worldFile;
    
    public WorldFileImageReader(){
        
    }

    /**
     * Read the georeferenced image
     * @param imageFile
     * @param tableReference
     * @param connection
     * @param progress
     * @throws SQLException
     * @throws IOException 
     */
    public void read(File imageFile, String tableReference, Connection connection, ProgressVisitor progress) throws SQLException, IOException {
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        String filePath = imageFile.getPath();        
        final int dotIndex = filePath.lastIndexOf('.');
        fileNameExtension = filePath.substring(dotIndex + 1).toLowerCase();
        filePathWithoutExtension = filePath.substring(0, dotIndex+1);
        if (isThereAnyWorldFile()) {
            readWorldFile(worldFile);
            //Check PRJ file
            if (new File(filePath + "prj").exists()) {
                srid = PRJUtil.getSRID(connection, new File(filePath + "prj"));
            }
            else if (new File(filePath + "PRJ").exists()) {
                srid = PRJUtil.getSRID(connection, new File(filePath + "PRJ"));
            }
            readImage(imageFile, tableReference, isH2, connection);
        } else {
            throw new SQLException("Cannot support this extension : " + fileNameExtension);
        }
    }
    
    /**
     * Import the image
     * 
     * @param imageFile
     * @param tableReference
     * @param isH2 
     */
    public void readImage(File imageFile, String tableReference, boolean isH2, Connection connection) throws SQLException{        
        TableLocation location = TableLocation.parse(tableReference, isH2);
        StringBuilder sb = new StringBuilder();
        //H2GIS
        if(isH2){
        sb.append("create table ").append(location.toString()).append("(id serial, the_raster raster);");
        sb.append("insert into ").append(location.toString()).append("(the_raster)").append(" values (ST_RasterFromImage(FILE_READ('");
        sb.append(imageFile.getPath()).append("'), ");
        sb.append(upperLeftX).append(",");
        sb.append(upperLeftY).append(",");
        sb.append(scaleX).append(",");
        sb.append(scaleY).append(",");
        sb.append(skewX).append(",");
        sb.append(skewY).append(",");
        sb.append(srid).append("));");
        }
        else{
        //PostGIS
        
        }        
        Statement stmt = connection.createStatement();
        stmt.execute(sb.toString());
        stmt.close();
    
    }
    
     /**
     * Check if the world file exists
     * 
     * @return @throws IOException
     */
    private boolean isThereAnyWorldFile() throws IOException {
        for (String extension : worldFileExtensions.get(fileNameExtension)) {
            if (new File(filePathWithoutExtension + extension).exists()) {
                worldFile = new File(filePathWithoutExtension  + extension);
                return true;
            } else if (new File(filePathWithoutExtension + extension.toUpperCase()).exists()) {
                worldFile = new File(filePathWithoutExtension
                        + extension.toUpperCase());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Read the world file 
     * A world file file is a plain ASCII text file consisting of six values separated by newlines. 
     * The format is: 
     * 
     * pixel X size rotation about the Y axis (usually 0.0) 
     * rotation about the X axis (usually 0.0)
     * negative pixel Y size 
     * X coordinate of upper left pixel center 
     * Y coordinate of upper left pixel center
     * 
     * @param worldFile
     * @throws IOException 
     */
    public void readWorldFile(File worldFile) throws IOException {
        final FileReader fin = new FileReader(worldFile);
        final BufferedReader in = new BufferedReader(fin);
        String lineIn = in.readLine();
        int line = 0;
        while ((in.ready() || lineIn != null) && line < 6) {
            if (lineIn != null && !"".equals(lineIn)) {
                switch (line) {
                    case 0:
                        scaleX = Float.valueOf(lineIn.trim());
                        break;
                    case 1:
                        skewX = Float.valueOf(lineIn.trim());
                        break;
                    case 2:
                        skewY = Float.parseFloat(lineIn.trim());
                        break;
                    case 3:
                        scaleY = Float.valueOf(lineIn.trim());
                        break;
                    case 4:
                        upperLeftX = Double.valueOf(lineIn.trim());
                        break;
                    case 5:
                        upperLeftY = Double.valueOf(lineIn.trim());
                        break;
                }
            }
            line++;
            lineIn = null;
            if (in.ready()) {
                lineIn = in.readLine();
            }
        }
        in.close();
    }
    
}
