/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.gpx.model;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * GpxPreparser class just reads the document. It says what type of elements
 * (waypoints, routes ,routepoints, tracks, track segments, trackpoints) the
 * document contains. It also counts each type of elements.
 *
 * @author Antonin Piasco
 * @author Erwan Bocher
 */
public final class GpxPreparser extends DefaultHandler {

    private int totalWpt;
    private int totalRte;
    private int totalRtept;
    private int totalTrk;
    private int totalTrkseg;
    private int totalTrkpt;
    private String version;

    /**
     * Initializing of the pre-parser. 
     * All values are set to zero.
     */
    public GpxPreparser() {
        version = null;
        totalWpt = 0;
        totalRte = 0;
        totalRtept = 0;
        totalTrk = 0;
        totalTrkseg = 0;
        totalTrkpt = 0;
    }

    /**
     * Reads the document and pre-parses it. The other method is called
     * automatically when a start markup is found.
     *
     * @param inputFile the file to read
     * @return a boolean if the parser ends successfully or not
     */
    public boolean read(File inputFile, String encoding) throws SAXException, IOException {
        boolean success = false;
        try (FileInputStream fs = new FileInputStream(inputFile)) {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setErrorHandler(this);
            parser.setContentHandler(this);
            InputSource is = new InputSource(fs);
            if(encoding!=null && !encoding.isEmpty()) {
                is.setEncoding(encoding);
            }
            parser.parse(is);
            success = true;
        }
        return success;
    }

    /**
     * Fires whenever an XML start markup is encountered. It indicates which
     * version of gpx file is to be parsed. It counts the waypoints, routes,
     * routepoints, tracks, track segments and trackpoints.
     *
     * @param uri URI of the local element
     * @param localName Name of the local element (without prefix)
     * @param qName qName of the local element (with prefix)
     * @param attributes Attributes of the local element (contained in the
     * markup)
     * @throws SAXException Any SAX exception, possibly wrapping another
     * exception
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.compareToIgnoreCase(GPXTags.GPX) == 0) {
            version = attributes.getValue(GPXTags.VERSION);
        } else if (localName.compareToIgnoreCase(GPXTags.RTE) == 0) {
            totalRte++;
        } else if (localName.compareToIgnoreCase(GPXTags.TRK) == 0) {
            totalTrk++;
        } else if (localName.compareToIgnoreCase(GPXTags.TRKSEG) == 0) {
            totalTrkseg++;
        } else if (localName.compareToIgnoreCase(GPXTags.WPT) == 0) {
            totalWpt++;
        } else if (localName.compareToIgnoreCase(GPXTags.RTEPT) == 0) {
            totalRtept++;
        } else if (localName.compareToIgnoreCase(GPXTags.TRKPT) == 0) {
            totalTrkpt++;
        }
    }

    /**
     * Gives the version of the gpx file.
     *
     * @return version value
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gives the total number of waypoints contained in the GPX file.
     *
     * @return the total number of waypoints
     */
    public int getTotalWpt() {
        return totalWpt;
    }

    /**
     * Gives the total number of routes contained in the GPX file.
     *
     * @return the total number of routes
     */
    public int getTotalRte() {
        return totalRte;
    }

    /**
     * Gives the total number of routepoints contained in the GPX file.
     *
     * @return the total number of routepoints
     */
    public int getTotalRtept() {
        return totalRtept;
    }

    /**
     * Gives the total number of tracks contained in the GPX file.
     *
     * @return the total number of tracks
     */
    public int getTotalTrk() {
        return totalTrk;
    }

    /**
     * Gives the total number of track segments contained in the GPX file.
     *
     * @return the total number of track segments
     */
    public int getTotalTrkseg() {
        return totalTrkseg;
    }

    /**
     * Gives the total number of trackpoints contained in the GPX file.
     *
     * @return the total number of trackpoints
     */
    public int getTotalTrkpt() {
        return totalTrkpt;
    }
}
