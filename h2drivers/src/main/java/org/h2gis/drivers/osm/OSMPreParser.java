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
package org.h2gis.drivers.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.h2gis.drivers.gpx.model.GPXTags;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * OSMPreParser class just reads the document. It says what type of elements
 * (nodes, ways and relation) the document contains. It also counts each type of
 * elements.
 *
 * @author Erwan Bocher
 */
public class OSMPreParser extends DefaultHandler {

    private int totalNode;
    private int totalWay;
    private int totalRelation;
    private String version;

    /**
     * Initializing of the pre-parser. All values are set to zero.
     */
    public OSMPreParser() {
        version = null;
        totalNode = 0;
        totalWay = 0;
        totalRelation = 0;
    }

    /**
     * Reads the document and pre-parses it. The other method is called
     * automatically when a start markup is found.
     *
     * @param inputFile the file to read
     * @return a boolean if the parser ends successfully or not
     * @throws SAXException
     * @throws IOException
     */
    public boolean read(File inputFile) throws SAXException, IOException {
        boolean success = false;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(inputFile);
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setErrorHandler(this);
            parser.setContentHandler(this);
            parser.parse(new InputSource(fs));
            success = true;
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
        return success;
    }

    /**
     * Fires whenever an XML start markup is encountered. It indicates which
     * version of osm file is to be parsed.
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
        if (localName.compareToIgnoreCase("osm") == 0) {
            version = attributes.getValue(GPXTags.VERSION);
        } else if (localName.compareToIgnoreCase("node") == 0) {
            totalNode++;
        } else if (localName.compareToIgnoreCase("way") == 0) {
            totalWay++;
        } else if (localName.compareToIgnoreCase("relation") == 0) {
            totalRelation++;
        }
    }

    public String getVersion() {
        return version;
    }

    public int getTotalNode() {
        return totalNode;
    }

    public int getTotalWay() {
        return totalWay;
    }

    public int getTotalRelation() {
        return totalRelation;
    }

}
