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
package org.h2gis.drivers.gpx.model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Specific parser for routes. It will be call each time a <rte> markup is
 * found. It is for the 1.1 version
 *
 * @author Antonin
 */
public final class GpxParserRte extends AbstractGpxParserRte {

    /**
     * Create a new specific parser. It has in memory the default parser, the
     * contentBuffer, the elementNames, the currentLine and the rteID.
     *
     * @param reader The XMLReader used in the default class
     * @param parent The parser used in the default class
     */
    public GpxParserRte(XMLReader reader, GpxParser parent) {
        super.initialise(reader, parent);
    }

    /**
     * Fires whenever an XML start markup is encountered. It creates a new
     * routePoint when a <rtept> markup is encountered. It saves informations
     * about <link> in currentPoint or currentLine.
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
     * routePoints or routes and saves them in corresponding values[].
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