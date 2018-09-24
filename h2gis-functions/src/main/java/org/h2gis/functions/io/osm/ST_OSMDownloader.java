/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.osm;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.spatial.crs.ST_Transform;
import org.h2gis.utilities.URIUtilities;

/**
 * This function is used to download data from the osm api using a bounding box.
 *
 * @author Erwan Bocher
 */
public class ST_OSMDownloader extends AbstractFunction implements ScalarFunction {

    private static final String OSM_API_URL = "http://api.openstreetmap.org/api/0.6/";

    public ST_OSMDownloader() {
        addProperty(PROP_REMARKS, "Extract an OSM XML file from the OSM api server using a the bounding box of a given geometry.\n"
                + "A path must be set to specified where the OSM file will be stored./n"
                + "Set true to delete the XML file if exists. Default behaviour is false.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "downloadData";
    }

    /**
     * 
     * @param con the database connection
     * @param area The geometry used to compute the area set to the OSM server
     * @param fileName The path to save the osm file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void downloadData(Connection con, Geometry area, String fileName) throws FileNotFoundException, IOException, SQLException {
            downloadData(con,area, fileName, false);
    }   
    
    /**
     * 
     * @param con the database connection
     * @param area The geometry used to compute the area set to the OSM server
     * @param fileName The path to save the osm file
     * @param deleteFile True to delete the file if exists
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void downloadData(Connection con,Geometry area, String fileName, boolean deleteFile) throws FileNotFoundException, IOException, SQLException {
        File file = URIUtilities.fileFromString(fileName);
        if (file.exists()) {
            if(deleteFile){
              file.delete();
            }
            else{
            throw new FileNotFoundException("The following file already exists:\n" + fileName);
            }
        }
        if (file.getName().toLowerCase().endsWith(".osm")) {
            if (area != null) {
                int srid = area.getSRID();
                if (srid!=0) {
                    downloadOSMFile(file, ST_Transform.ST_Transform(con, area, 4326).getEnvelopeInternal());
                } else {
                    downloadOSMFile(file, area.getEnvelopeInternal());
                }
            }
        } else {
            throw new SQLException("Supported format is .osm");
        }
    }
    
     

    /**
     * Download OSM file from the official server
     *
     * @param file
     * @param geometryEnvelope
     * @throws IOException
     */
    public static void downloadOSMFile(File file, Envelope geometryEnvelope) throws IOException {
        HttpURLConnection urlCon = (HttpURLConnection) createOsmUrl(geometryEnvelope).openConnection();
        urlCon.setRequestMethod("GET");
        urlCon.connect();
        switch (urlCon.getResponseCode()) {
            case 400:
                throw new IOException("Error : Cannot query the OSM API with the following bounding box");
            case 509:
                throw new IOException("Error: You have downloaded too much data. Please try again later");
            default:
                InputStream in = urlCon.getInputStream();
                OutputStream out = new FileOutputStream(file);
                try {
                    byte[] data = new byte[4096];
                    while (true) {
                        int numBytes = in.read(data);
                        if (numBytes == -1) {
                            break;
                        }
                        out.write(data, 0, numBytes);
                    }
                } finally {
                    out.close();
                    in.close();
                }       break;
        }
    }

    /**
     * Build the OSM URL based on a given envelope
     *
     * @param geometryEnvelope
     * @return
     */
    private static URL createOsmUrl(Envelope geometryEnvelope) {
        try {
            return new URL(OSM_API_URL + "map?bbox=" + geometryEnvelope.getMinX() + ","
                    + geometryEnvelope.getMinY()
                    + "," + geometryEnvelope.getMaxX() + "," + geometryEnvelope.getMaxY());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
