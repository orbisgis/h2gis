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


import org.apache.commons.lang3.StringUtils;
import org.h2gis.utilities.URIUtilities;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to extract overpass data into a file
 * @author E Bocher, CNRS
 */
public class OverpassTool {

    /** Overpass server endpoint as defined by WSDL2 definition */
    private String endPoint = "https://overpass-api.de/api/interpreter?data=";

    private String proxyHost;
    private int proxyPort;

    public OverpassTool() {
    }

    /**
     * Prepare the connection to the overpass endpoint
     *
     * @param overpassQuery overpass query
     * @return HttpURLConnection
     */
    public HttpURLConnection prepareConnection(String overpassQuery) throws Exception {
        Matcher timeoutMatcher = Pattern.compile("\\[timeout:(\\d+)\\]").matcher(overpassQuery);
        int timeout;
        if (timeoutMatcher.find()) {
            timeout = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(timeoutMatcher.group(1)));
        } else {
            timeout = (int) TimeUnit.MINUTES.toMillis(3);
        }
        URL queryUrl = new URL(getEndpoint() + URLEncoder.encode(overpassQuery, StandardCharsets.UTF_8));
        HttpURLConnection connection;
        if (getProxyHost() != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            connection = (HttpURLConnection) queryUrl.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) queryUrl.openConnection();
        }
        connection.setRequestProperty("User-Agent", "H2GIS_" + System.currentTimeMillis());
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    /**
     * Download the result of the query in a file
     *
     * @param overpassQuery the overpass QL
     * @return a stream connection
     * @throws Exception if the server cannot execute the query
     */
    public InputStream downloadAsStream(String overpassQuery) throws Exception {
        if (overpassQuery == null || overpassQuery.isEmpty()) {
            throw new IllegalArgumentException("The overpass query cannot be null or empty");
        }
        HttpURLConnection connection = prepareConnection("[out:json][timeout:25];\n" + overpassQuery + "\nout geom;");
        connection.connect();
        switch (connection.getResponseCode()) {
            case 400:
                throw new IOException("Error : Cannot execute the Overpass query " + connection.getURL());
            case 509:
                throw new IOException("Error: You have downloaded too much data. Please try again later");
            default:
                return connection.getInputStream();
        }

    }

    /**
     * Download the result of the query into a file
     * @param overpassQuery overpass QL
     * @param outputFile the path of the file to save the data
     * @param deleteFile true to delete the file if exists
     * @throws Exception if the server cannot execute the query
     */
    public void downloadFile(String overpassQuery, String outputFile, boolean deleteFile) throws Exception {
        if (overpassQuery == null || overpassQuery.isEmpty()) {
            throw new IllegalArgumentException("The overpass query cannot be null or empty");
        }
        String cleanQuery = StringUtils.deleteWhitespace(overpassQuery);
        File file = URIUtilities.fileFromString(outputFile);
        checkOutputFile(cleanQuery, file);

        if (file.exists()) {
            if (deleteFile) {
                file.delete();
            } else {
                throw new FileNotFoundException("The following file already exists:\n" + outputFile);
            }
        }
        HttpURLConnection connection = prepareConnection(overpassQuery);
        connection.connect();
        switch (connection.getResponseCode()) {
            case 400:
                throw new IOException("Error : Cannot execute the Overpass query " + connection.getURL());
            case 509:
                throw new IOException("Error: You have downloaded too much data. Please try again later");
            default:
                try (InputStream in = connection.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                    byte[] data = new byte[4096];
                    while (true) {
                        int numBytes = in.read(data);
                        if (numBytes == -1) {
                            break;
                        }
                        out.write(data, 0, numBytes);
                    }
                }
                break;
        }
    }

    /**
     * Check if the file extension is supported and the same as specified in the query
     * @param overpassQuery query
     * @param outputFile output file
     */
    public void checkOutputFile(String overpassQuery, File outputFile) {
        if (outputFile.getName().toLowerCase().endsWith(".csv") && !StringUtils.containsIgnoreCase(overpassQuery, "out:csv")) {
            throw new IllegalArgumentException("The file extension is not compatible with the one specified in the request. Please use csv");
        } else if (outputFile.getName().toLowerCase().endsWith(".json") && !StringUtils.containsIgnoreCase(overpassQuery, "out:json")) {
            throw new IllegalArgumentException("The file extension is not compatible with the one specified in the request. Please use json");
        } else if (outputFile.getName().toLowerCase().endsWith(".osm") && StringUtils.containsAnyIgnoreCase(overpassQuery, "out:json", "out:csv")) {
            throw new IllegalArgumentException("The file extension is not compatible with the one specified in the request");
        }
    }

    /**
     * Overpass endpoint
     * @return endpoint
     */
    public String getEndpoint() {
        return endPoint;
    }

    /**
     * Set new endpoint
     * @param endPoint endpoint
     */
    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    /**
     * Set a port to the proxy
     * @return the proxy value
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Set a port to the proxy
     * @param proxyPort proxy port value
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Get the proxy host
     * @return the proxy host
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Set a new proxy host
     * @param proxyHost proxy host value
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
}
