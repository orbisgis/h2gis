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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Abstract class of the parsers dedicated to routes. A specific parser for
 * version 1.0 and version 1.1 will extend this class.
 *
 * @author Erwan Bocher and Antonin Piasco
 */
public abstract class AbstractGpxParserRte extends AbstractGpxParser {

    // Indicates if we are in a point or not
    private boolean point;
    // Reference to the default contentHandler of the class GpxParserDefault
    private AbstractGpxParserDefault parent;
    // A list which will contain the coordinates of the route
    private List<Coordinate> rteList;
    private int idRtPt = 1;

    /**
     * Create a new specific parser. It has in memory the default parser, the
     * contentBuffer, the elementNames, the currentLine and the rteID.
     *
     * @param reader The XMLReader used in the default class
     * @param parent The parser used in the default class
     */
    public void initialise(XMLReader reader, AbstractGpxParserDefault parent) {
        setReader(reader);
        setParent(parent);
        setContentBuffer(parent.getContentBuffer());
        setRtePreparedStmt(parent.getRtePreparedStmt());
        setRteptPreparedStmt(parent.getRteptPreparedStmt());
        setElementNames(parent.getElementNames());
        setCurrentLine(parent.getCurrentLine());
        setRteList(new ArrayList<Coordinate>());
    }

    /**
     * Fires whenever an XML start markup is encountered. It creates a new
     * routePoint when a <rtept> markup is encountered.
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
        if (localName.equalsIgnoreCase(GPXTags.RTEPT)) {
            point = true;
            GPXPoint routePoint = new GPXPoint(GpxMetadata.RTEPTFIELDCOUNT);
            try {
                Coordinate coordinate = GPXCoordinate.createCoordinate(attributes);
                Point geom = getGeometryFactory().createPoint(coordinate);
                geom.setSRID(4326);
                routePoint.setValue(GpxMetadata.THE_GEOM, geom);
                routePoint.setValue(GpxMetadata.PTLAT, coordinate.y);
                routePoint.setValue(GpxMetadata.PTLON, coordinate.x);
                routePoint.setValue(GpxMetadata.PTELE, coordinate.z);
                routePoint.setValue(GpxMetadata.PTID, idRtPt++);
                routePoint.setValue(GpxMetadata.RTEPT_RTEID, getCurrentLine().getValues()[GpxMetadata.LINEID]);
                rteList.add(coordinate);
            } catch (NumberFormatException ex) {
                throw new SAXException(ex);
            }
            setCurrentPoint(routePoint);
        }

        // Clear content buffer
        getContentBuffer().delete(0, getContentBuffer().length());

        // Store name of current element in stack
        getElementNames().push(qName);
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
        // currentElement represents the last string encountered in the document
        setCurrentElement(getElementNames().pop());
        if (getCurrentElement().equalsIgnoreCase(GPXTags.RTE)) {
            Coordinate[] rteArray = new Coordinate[rteList.size()];
            rteArray = rteList.toArray(rteArray);
            // If there are more than one routepoint, we can set a geometry to the route
            if (rteList.size() > 1) {
                LineString geometry = getGeometryFactory().createLineString(rteArray);
                geometry.setSRID(4326);
                getCurrentLine().setGeometry(geometry);
            }
            // if </rte> markup is found, the currentLine is added in the table rtedbd and the default contentHandler is setted.
            try {
                PreparedStatement pStm = getRtePreparedStmt();
                int i = 1;
                Object[] values = getCurrentLine().getValues();
                for (Object object : values) {
                    pStm.setObject(i, object);
                    i++;
                }
                pStm.execute();
            } catch (SQLException ex) {
                throw new SAXException("Cannot import the route line ", ex);
            }
            getReader().setContentHandler(parent);

        } else if (getCurrentElement().equalsIgnoreCase(GPXTags.RTEPT)) {
            // if </rtept> markup is found, the currentPoint is added in the table rteptdbd.
            point = false;
            try {
                PreparedStatement pStm = getRteptPreparedStmt();
                int i = 1;
                Object[] values = getCurrentPoint().getValues();
                for (Object object : values) {
                    pStm.setObject(i, object);
                    i++;
                }
                pStm.execute();
            } catch (SQLException ex) {
                throw new SAXException("Cannot import the route points ", ex);
            }
        } else if (point) {
            getCurrentPoint().setAttribute(getCurrentElement(), getContentBuffer());

        } else {
            getCurrentLine().setAttribute(getCurrentElement(), getContentBuffer());
        }
    }

    /**
     * *****************************
     ***** GETTERS AND SETTERS ***** *****************************
     */
    /**
     * Set the parent of this specific parser.
     *
     * @param parent
     */
    public void setParent(AbstractGpxParserDefault parent) {
        this.parent = parent;
    }

    /**
     * Indicates if we are in a point.
     *
     * @return true if we are in a point, false else
     */
    public boolean isPoint() {
        return point;
    }

    /**
     * Set the list corresponding to the points' coordinates of the actual
     * route.
     *
     * @param rteList
     */
    public void setRteList(List<Coordinate> rteList) {
        this.rteList = rteList;
    }
}
