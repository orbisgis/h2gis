/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * Default parser. This class parses GPX 1.1 files and saves them in a .gdms
 * file. It set a contentHandler by default which is able to save general
 * informations about the document. To save specific informations (waypoints,
 * routes and tracks) it will call specific classes.
 *
 * @author Antonin, Erwan Bocher
 */
public class GpxParser extends AbstractGpxParserDefault {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpxParser.class);
    // Indicator to know if we are in <author> element
    private boolean author;
    // Informations about the copyright :
    // Year of copyright.
    private String year;
    // Link to external file containing license text.
    private String license;
    // Copyright holder (TopoSoft, Inc.)
    private String copyrighter;

    /**
     * Create a new GPX parser and specify what kind of data must be parsed in
     * the GPX file
     *
     * @param wpt
     * @param rte
     * @param trk
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
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        super.startElement(uri, localName, qName, attributes);        
        if (localName.compareToIgnoreCase(GPXTags.LINK) == 0) {
            if (author) {
                setAuthorLink(attributes.getValue(GPXTags.HREF));
            } else if (!isSpecificElement()) {
                setLink(attributes.getValue(GPXTags.HREF));
            }

        } else if (localName.compareToIgnoreCase(GPXTags.EMAIL) == 0) {
            setEmail(attributes.getValue(GPXTags.ID) + "@" + attributes.getValue(GPXTags.DOMAIN));

        } else if (localName.compareToIgnoreCase(GPXTags.COPYRIGHT) == 0) {

            copyrighter = attributes.getValue(GPXTags.AUTHOR);

        } else if (localName.compareToIgnoreCase(GPXTags.AUTHOR) == 0) {

            author = true;

        } else if (localName.compareToIgnoreCase(GPXTags.WPT) == 0) {
            setSpecificElement(true);
            if (isWpt()) {
                setWptID(getWptID() + 1);
            }
            // Initialisation of a waypoint
            try {
                setCurrentPoint(new WayPoint());
                ((WayPoint) getCurrentPoint()).wptInit(attributes, getGeometryReader());
                // ContentHandler changing
                setWptParser(new GpxParserWpt(getReader(), this));
                getReader().setContentHandler(getWptParser());
            } catch (GPXException ex) {
                LOGGER.error("Problem while parsing", ex);
            }
        } else if (localName.compareToIgnoreCase(GPXTags.RTE) == 0) {
        } else if (localName.compareToIgnoreCase(GPXTags.TRK) == 0) {
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
