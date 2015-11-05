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

package org.h2gis.drivers.worldFileImage;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.imageio.RenderedImageReader;
import org.h2gis.drivers.utility.FileUtil;
import org.h2gis.drivers.utility.PRJUtil;
import org.h2gis.h2spatialapi.InputStreamProgressMonitor;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Methods to read a georeferenced image
 * 
 * @author Erwan Bocher
 */
public class WorldFileImageReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldFileImageReader.class);
    public static final Map<String, String[]> worldFileExtensions;
    
    private double scaleX = 1;

    private double scaleY = -1;

    private double skewX = 0;

    private double skewY = 0;

    private double upperLeftX = 0;

    private double upperLeftY = 0;
    
    private int srid=0;
    
    static {
		worldFileExtensions = new HashMap<String, String[]>();
		worldFileExtensions.put("tif", new String[] { "tfw", "tifw" , "wld"});
		worldFileExtensions.put("tiff", new String[] {"tfw","tiffw" , "wld"});
		worldFileExtensions.put("jpg", new String[] { "jpw", "jgw", "jpgw",
				"jpegw","wld" });
		worldFileExtensions.put("jpeg", new String[] { "jgw", "jpw", "jpgw",
				"jpegw","wld" });
		worldFileExtensions.put("gif", new String[] { "gfw", "gifw","wld" });
		worldFileExtensions.put("bmp", new String[] { "bpw", "bmpw","wld" });
		worldFileExtensions.put("png", new String[] { "pgw", "pngw","wld" });
	}
    
    private String fileNameExtension;
    private String filePathWithoutExtension;
    private File worldFile;
    private File imageFile;

    /**
     * Use {@link #fetch(Connection, File)}
     */
    private WorldFileImageReader(){
        
    }

    /**
     * Create WorldFileImageReader using world file on disk.
     * @param connection Active connection, not closed by this method
     * @param imageFile Image file path
     * @return Instance of WorldFileImageReader
     * @throws IOException
     * @throws SQLException
     */
    public static WorldFileImageReader fetch(Connection connection, File imageFile) throws IOException, SQLException {
        WorldFileImageReader worldFileImageReader = new WorldFileImageReader();
        worldFileImageReader.imageFile = imageFile;
        String filePath = imageFile.getPath();
        final int dotIndex = filePath.lastIndexOf('.');
        worldFileImageReader.fileNameExtension = filePath.substring(dotIndex + 1).toLowerCase();
        worldFileImageReader.filePathWithoutExtension = filePath.substring(0, dotIndex+1);
        if (worldFileImageReader.isThereAnyWorldFile()) {
            worldFileImageReader.readWorldFile();
        } else {
            // Use default metadata but warn the user
            // The user may want to be able to create raster metadata through sql commands in H2
            LOGGER.warn("World file is not available with this raster, default raster metadata has been set");
            worldFileImageReader.scaleX = 1;
            worldFileImageReader.scaleY = -1;
            worldFileImageReader.upperLeftX = 0;
            worldFileImageReader.upperLeftY = 0; // should be image height
            worldFileImageReader.skewX = 0;
            worldFileImageReader.skewY = 0;
        }
        Map<String, File> matchExt = FileUtil.fetchFileByIgnoreCaseExt(imageFile.getParentFile(), FileUtil
                .getBaseName(imageFile), "prj");
        worldFileImageReader.srid = PRJUtil.getSRID(connection, matchExt.get("prj"));
        return worldFileImageReader;
    }

    /**
     * Copy the georeferenced image into a table
     * @param tableReference
     * @param connection
     * @param progress
     * @throws SQLException
     * @throws IOException 
     */
    public void read(String tableReference, Connection connection, ProgressVisitor progress) throws SQLException, IOException {
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        readImage(tableReference, isH2, connection, progress);
    }
    
    /**
     * Import the image
     *
     * @param tableReference
     * @param isH2 
     * @param connection 
     * @param progressVisitor 
     * @throws java.sql.SQLException 
     */
    public void readImage(String tableReference, boolean isH2, Connection connection, ProgressVisitor progressVisitor) throws
            SQLException{
        TableLocation location = TableLocation.parse(tableReference, isH2);
        StringBuilder sb = new StringBuilder();
        sb.append("create table ").append(location.toString()).append("(id serial, the_raster raster) as ");
        sb.append("select null, ");
        boolean transferWKBRaster;
        if(isH2) {
            sb.append("?;");
            transferWKBRaster = true;
        } else {
            sb.append("ST_SetGeoReference(ST_FromGDALRaster(?,");
            sb.append(srid);
            sb.append("), ");
            sb.append(upperLeftX).append(",");
            sb.append(upperLeftY).append(",");
            sb.append(scaleX).append(",");
            sb.append(scaleY).append(",");
            sb.append(skewX).append(",");
            sb.append(skewY);
            sb.append("));");
            transferWKBRaster = false;
        }
        PreparedStatement stmt = connection.prepareStatement(sb.toString());
        try {
            if(!transferWKBRaster) {
                // Transfer the image directly
                FileInputStream fileInputStream = new FileInputStream(imageFile);
                try {
                    stmt.setBinaryStream(1, new InputStreamProgressMonitor(progressVisitor, fileInputStream,
                                    imageFile.length()), imageFile.length());
                    stmt.execute();
                } finally {
                    fileInputStream.close();
                }
            } else {
                // Convert the image to WKB Raster locally then transfer the bytes
                RandomAccessFile raf = new RandomAccessFile(imageFile, "r");
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(raf);
                try {
                    Iterator<ImageReader> readerIterator = ImageIO.getImageReaders(imageInputStream);
                    if(readerIterator == null || !readerIterator.hasNext()) {
                        throw new SQLException("Could not find image reader for "+imageFile);
                    }
                    ImageReader imageReader = readerIterator.next();
                    imageReader.setInput(imageInputStream);
                    RenderedImageReader renderedImageReader = new RenderedImageReader(imageReader);
                    GeoRaster geoRaster = GeoRasterRenderedImage
                            .create(renderedImageReader, scaleX, scaleY, upperLeftX, upperLeftY, skewX, skewY, srid,
                                    Double.NaN);
                    InputStream wkbStream = geoRaster.asWKBRaster();
                    try {
                        stmt.setBinaryStream(1, new InputStreamProgressMonitor(progressVisitor,wkbStream,geoRaster
                                .getMetaData().getTotalLength()));
                        stmt.execute();
                    } finally {
                        wkbStream.close();
                    }
                } finally {
                    stmt.close();
                    imageInputStream.close();
                }
            }
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            throw new SQLException(ex.getLocalizedMessage(), ex);
        }
        stmt.close();
    
    }
    
     /**
     * Check if the world file exists
     * 
     * @return @throws IOException
     */
    private boolean isThereAnyWorldFile() throws IOException {
        File parentFolder = imageFile.getParentFile();
        String fileNameWithoutExt = FileUtil.getBaseName(imageFile);
        Map<String, File> worldFiles = FileUtil.fetchFileByIgnoreCaseExt(parentFolder, fileNameWithoutExt,
                worldFileExtensions.get(fileNameExtension));
        if(!worldFiles.isEmpty()) {
            worldFile = worldFiles.values().iterator().next();
            return true;
        } else {
            return false;
        }
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
     * @throws IOException 
     */
    private void readWorldFile() throws IOException {
        final FileReader fin = new FileReader(worldFile);
        final BufferedReader in = new BufferedReader(fin);
        try {
            String lineIn = in.readLine();
            int line = 0;
            while ((in.ready() || lineIn != null) && line < 6) {
                if (lineIn != null && !"".equals(lineIn)) {
                    switch (line) {
                        case 0:
                            scaleX = Double.valueOf(lineIn.trim());
                            break;
                        case 1:
                            skewX = Double.valueOf(lineIn.trim());
                            break;
                        case 2:
                            skewY = Double.valueOf(lineIn.trim());
                            break;
                        case 3:
                            scaleY = Double.valueOf(lineIn.trim());
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
        } finally {
            in.close();
        }
        // ESRI and WKB Raster have a slight difference on insertion point
        upperLeftX = upperLeftX + scaleX * 0.5;
        upperLeftY = upperLeftY + scaleY * 0.5;
    }

    /**
     * @return Pixel size X
     */
    public double getScaleX() {
        return scaleX;
    }

    /**
     * @return Pixel size Y
     */
    public double getScaleY() {
        return scaleY;
    }

    /**
     * @return Pixel rotation X
     */
    public double getSkewX() {
        return skewX;
    }

    /**
     * @return Pixel rotation Y
     */
    public double getSkewY() {
        return skewY;
    }

    /**
     * @return Upper left image position
     */
    public double getUpperLeftX() {
        return upperLeftX;
    }

    /**
     * @return Upper left image position
     */
    public double getUpperLeftY() {
        return upperLeftY;
    }

    /**
     * @return Projection id
     */
    public int getSrid() {
        return srid;
    }
}
