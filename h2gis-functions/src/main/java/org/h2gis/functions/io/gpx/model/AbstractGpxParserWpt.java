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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstract class of the parsers dedicated to waypoints. A specific parser for
 * version 1.0 and version 1.1 will extend this class.
 *
 * @author Erwan Bocher and Antonin Piasco,
 */
public class AbstractGpxParserWpt extends AbstractGpxParser {

    // Reference to the parent of this specific parser.
    private AbstractGpxParserDefault parent;

    /**
     * Create a new specific parser. It has in memory the default parser, the
     * contentBuffer, the elementNames and the currentPoint.
     *
     * @param reader The XMLReader used in the default class
     * @param parent The parser used in the default class
     */
    public void initialise(XMLReader reader, AbstractGpxParserDefault parent) {
        setReader(reader);
        setParent(parent);
        setContentBuffer(parent.getContentBuffer());
        setWptPreparedStmt(parent.getWptPreparedStmt());
        setElementNames(parent.getElementNames());
        setCurrentPoint(parent.getCurrentPoint());
    }

    /**
     * Fires whenever an XML start markup is encountered.
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
        // Clear content buffer
        getContentBuffer().delete(0, getContentBuffer().length());
        // Store name of current element in stack
        getElementNames().push(qName);
    }

    /**
     * Fires whenever an XML end markup is encountered. It catches attributes of
     * the waypoint and saves them in corresponding values[].
     *
     * @param uri URI of the local element
     * @param localName Name of the local element (without prefix)
     * @param qName qName of the local element (with prefix)
     * @throws SAXException Any SAX exception, possibly wrapping another
     * exception
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // currentElement represents the last string encountered in the document
        setCurrentElement(getElementNames().pop());

        if (getCurrentElement().equalsIgnoreCase(GPXTags.WPT)) {
            //if </wpt> markup is found, the currentPoint is added in the table wptdbd and the default contentHandler is setted.
            try {
                PreparedStatement pStm = getWptPreparedStmt();
                int i = 1;
                Object[] values = getCurrentPoint().getValues();
                for (Object object : values) {
                    pStm.setObject(i, object);
                    i++;
                }
                pStm.execute();
            } catch (SQLException ex) {
                throw new SAXException("Cannot import the waypoint.", ex);
            }
            getReader().setContentHandler(parent);

        } else {
            getCurrentPoint().setAttribute(getCurrentElement(), getContentBuffer());
        }
    }

    /**
     * Set the parent of this specific parser.
     *
     * @param parent {@link AbstractGpxParserDefault}
     */
    public void setParent(AbstractGpxParserDefault parent) {
        this.parent = parent;
    }
}
