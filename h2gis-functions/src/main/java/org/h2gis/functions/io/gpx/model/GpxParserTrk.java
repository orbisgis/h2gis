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
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Specific parser for tracks. It will be call each time a {@code <trk>} markup is
 * found. It is for the 1.1 version
 *
 * @author Erwan Bocher and Antonin Piasco
 */
public final class GpxParserTrk extends AbstractGpxParserTrk {

    /**
     * Create a new specific parser. It has in memory the default parser, the
     * contentBuffer, the elementNames, the currentLine and the trkID.
     *
     * @param reader The XMLReader used in the default class
     * @param parent The parser used in the default class
     */
    public GpxParserTrk(XMLReader reader, AbstractGpxParserDefault parent) {
        super.initialise(reader, parent);
    }

    /**
     * Fires whenever an XML start markup is encountered. It creates a new
     * trackSegment when a {@code <trkseg>} markup is encountered. It creates a new
     * trackPoint when a {@code <trkpt>} markup is encountered. It saves informations
     * about {@code <link>} in currentPoint or currentLine.
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
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase(GPXTags.LINK)) {
            if (isPoint()) {
                getCurrentPoint().setLink(attributes);
            } else {
                getCurrentLine().setLink(attributes);
            }
        }
    }

    /**
     * Fires whenever an XML end markup is encountered. It catches attributes of
     * trackPoints, trackSegments or routes and saves them in corresponding
     * values[].
     *
     * @param uri URI of the local element
     * @param localName Name of the local element (without prefix)
     * @param qName qName of the local element (with prefix)
     * @throws SAXException Any SAX exception, possibly wrapping another
     * exception
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if ((getCurrentElement().equalsIgnoreCase(GPXTags.TEXT)) && (isPoint())) {
            getCurrentPoint().setLinkText(getContentBuffer());
        } else if (getCurrentElement().equalsIgnoreCase(GPXTags.TEXT)) {
            getCurrentLine().setLinkText(getContentBuffer());
        }
    }
}
