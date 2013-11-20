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
        if (localName.compareToIgnoreCase(GPXTags.LINK) == 0) {
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
        if ((getCurrentElement().compareToIgnoreCase("text") == 0) && (isPoint())) {
            getCurrentPoint().setLinkText(getContentBuffer());
        } else if (getCurrentElement().compareToIgnoreCase("text") == 0) {
            getCurrentLine().setLinkText(getContentBuffer());
        }
    }
}