/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.gpx.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
     * @param parent
     */
    public void setParent(AbstractGpxParserDefault parent) {
        this.parent = parent;
    }
}
