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

package org.h2gis.functions.io.gpx.model;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Default parser. This class parses GPX 1.1 files and saves them in a
 * file. It set a contentHandler by default which is able to save general
 * information about the document. To save specific information (waypoints,
 * routes and tracks) it will call specific classes.
 *
 * @author  Erwan Bocher and Antonin Piasco
 */
public class GpxParser extends AbstractGpxParserDefault {

    // Indicator to know if we are in <author> element
    private boolean author;
    // Informations about the copyright :
    // Year of copyright.
    private String year;
    // Link to external file containing license text.
    private String license;
    // Copyright holder (TopoSoft, Inc.)
    private String copyrighter;
    //Waypoint, route and track id
    private int idWpt = 1;
    private int idRte = 1;
    private int trkID = 1;

    /**
     * Create a new GPX parser and specify what kind of data must be parsed in
     * the GPX file
     */
    public GpxParser() {
    }

    /**
     * Fires whenever an XML start markup is encountered. It takes general
     * information about the document. It change the ContentHandler to parse
     * specific informations when <wpt>, <rte> or <trk> markup are found.
     *
     * @param uri URI of the local element
     * @param localName Name of the local element (without prefix)
     * @param qName qName of the local element (with prefix)
     * @param attributes Attributes of the local element (contained in the
     * markup)
     * @throws SAXException
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase(GPXTags.LINK)) {
            if (author) {
                setAuthorLink(attributes.getValue(GPXTags.HREF));
            } else if (!isSpecificElement()) {
                setLink(attributes.getValue(GPXTags.HREF));
            }
        } else if (localName.equalsIgnoreCase(GPXTags.EMAIL)) {
            setEmail(attributes.getValue(GPXTags.ID) + "@" + attributes.getValue(GPXTags.DOMAIN));
        } else if (localName.equalsIgnoreCase(GPXTags.COPYRIGHT)) {
            copyrighter = attributes.getValue(GPXTags.AUTHOR);
        } else if (localName.equalsIgnoreCase(GPXTags.AUTHOR)) {
            author = true;
        } else if (localName.equalsIgnoreCase(GPXTags.WPT)) {
            setSpecificElement(true);
            // Initialisation of a waypoint
            try {
                GPXPoint currentPoint = new GPXPoint(GpxMetadata.WPTFIELDCOUNT);
                Coordinate coordinate = GPXCoordinate.createCoordinate(attributes);
                Point geom = getGeometryFactory().createPoint(coordinate);
                geom.setSRID(4326);
                currentPoint.setValue(GpxMetadata.THE_GEOM, geom);
                currentPoint.setValue(GpxMetadata.PTLAT, coordinate.y);
                currentPoint.setValue(GpxMetadata.PTLON, coordinate.x);
                currentPoint.setValue(GpxMetadata.PTELE, coordinate.z);
                //Set the identifier
                currentPoint.setValue(GpxMetadata.PTID, idWpt++);
                setCurrentPoint(currentPoint);
                // ContentHandler changing
                setWptParser(new GpxParserWpt(getReader(), this));
                getReader().setContentHandler(getWptParser());
            } catch (NumberFormatException ex) {
                throw new SAXException(ex);
            }
        } else if (localName.equalsIgnoreCase(GPXTags.RTE)) {
            setSpecificElement(true);
            // Initialisation of a route
            GPXLine route = new GPXLine(GpxMetadata.RTEFIELDCOUNT);
            route.setValue(GpxMetadata.LINEID, idRte++);
            setCurrentLine(route);
            // ContentHandler changing
            setRteParser(new GpxParserRte(getReader(), this));
            getReader().setContentHandler(getRteParser());
        } else if (localName.equalsIgnoreCase(GPXTags.TRK)) {
            setSpecificElement(true);
            // Initialisation of a track
            GPXLine track = new GPXLine(GpxMetadata.TRKFIELDCOUNT);
            track.setValue(GpxMetadata.LINEID, trkID++);
            setCurrentLine(track);
            // ContentHandler changing
            setTrkParser(new GpxParserTrk(getReader(), this));
            getReader().setContentHandler(getTrkParser());
        }
    }

    /**
     * Fires whenever an XML end markup is encountered. It catches attributes of
     * the different elements and saves them in corresponding values[].
     *
     * @param uri URI of the local element
     * @param localName Name of the local element (without prefix)
     * @param qName qName of the local element (with prefix)
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
    }

    /**
     * Initialisation of all the indicators used to read the document.
     */
    @Override
    public void clear() {
        super.clear();
        author = false;
        year = null;
        license = null;
        copyrighter = null;
    }

    /**
     * Gives copyright and license information governing use of the file.
     *
     * @return
     */
    @Override
    public String getCopyright() {
        return "Copyright :\n\t" + copyrighter + "\n\t" + year + "\n\t" + license;
    }
}
