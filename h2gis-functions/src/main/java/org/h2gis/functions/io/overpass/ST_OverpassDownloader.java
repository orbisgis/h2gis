/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.overpass;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

/**
 * A function to extract data from Overpass api and save it into a file
 * @author E. Bocher, CNRS
 */
public class ST_OverpassDownloader extends AbstractFunction implements ScalarFunction {

    public ST_OverpassDownloader() {
        addProperty(PROP_REMARKS, "Extract OSM data from Overpass api server and save the result into a file.\n"
                + "\n ST_OverpassDownloader(..."
                + "\n Supported arguments :"
                + "\n overpass query as string, path of the file to store the result"
                + "\n overpass query as string, path of the file to store the result, true to delete the file if exist"
                + "\n overpass query as string, path of the file to store the result, true to delete the file if exist, network options as 'proxyhost=? proxyport=? endpoint=?");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Extract OSM DATA
     * @param overpassQuery the overpass query
     * @param fileName the output file
     */
    public static void execute(String overpassQuery, String fileName) throws Exception {
        execute(overpassQuery, fileName, true);
    }

    /**
     * Extract OSM DATA
     * @param overpassQuery the overpass query
     * @param fileName the output file
     * @param deleteFile true to delete the file if exists
     */
    public static void execute(String overpassQuery, String fileName, boolean deleteFile) throws Exception {
        OverpassTool overpassTool = new OverpassTool();
        overpassTool.downloadFile(overpassQuery, fileName, deleteFile);
    }

    /**
     * Extract OSM DATA
     * @param overpassQuery the overpass query
     * @param fileName the output file
     * @param deleteFile true to delete the file if exists
     * @param options network options as proxyhost=? proxyport=? endpoint=?
     */
    public static void execute(String overpassQuery, String fileName, boolean deleteFile, String options) throws Exception {
        OverpassTool overpassTool = new OverpassTool();
        if (options != null) {
            String[] optionsNet = options.split("\\s+");
            for (String params : optionsNet) {
                String[] keyValue = params.split("=");
                if (keyValue[0].equalsIgnoreCase("proxyhost")) {
                    overpassTool.setProxyHost(keyValue[1]);
                } else if (keyValue[0].equalsIgnoreCase("proxyport")) {
                    overpassTool.setProxyPort(Integer.parseInt(keyValue[1]));
                } else if (keyValue[0].equalsIgnoreCase("endpoint")) {
                    overpassTool.setEndPoint(keyValue[1]);
                }
            }
        }
        overpassTool.downloadFile(overpassQuery, fileName, deleteFile);
    }
}
