package org.h2gis.drivers.gpx.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
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
 * @author Antonin
 */
public abstract class AbstractGpxParserRte extends AbstractGpxParser {

    // Indicates if we are in a point or not
    private boolean point;
    // Reference to the default contentHandler of the class GpxParserDefault
    private AbstractGpxParserDefault parent;
    // A list which will contain the coordinates of the route
    private List<Coordinate> rteList;

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
        setRteID(parent.getRteID());
        setRteptID(parent.getRteptID());
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
        if (localName.compareToIgnoreCase(GPXTags.RTEPT) == 0) {
            point = true;
            setRteptID(getRteptID() + 1);
            setCurrentPoint(new RoutePoint());
            try {
                ((RoutePoint) getCurrentPoint()).rteptInit(attributes, getGeometryReader());
                Coordinate coordinate;
                coordinate = new Coordinate(Double.parseDouble(attributes.getValue(GPXTags.LON)), Double.parseDouble(attributes.getValue(GPXTags.LAT)), 0);
                rteList.add(coordinate);
            } catch (GPXException ex) {
                throw new SAXException("Problem while parsing.", ex);
            }
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

        if (getCurrentElement().compareToIgnoreCase(GPXTags.RTE) == 0) {

            parent.setRteptID(getRteptID());
            Coordinate[] rteArray = new Coordinate[rteList.size()];
            rteArray = rteList.toArray(rteArray);
            // If there are more than one routepoint, we can set a geometry to the route
            if (rteList.size() > 1) {
                Geometry geometry;
                GeometryFactory gf = new GeometryFactory();
                geometry = gf.createLineString(rteArray);
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
                throw new SAXException("Cannot import the route line :  " + getRteID(), ex);
            }
            getReader().setContentHandler(parent);

        } else if (getCurrentElement().compareToIgnoreCase(GPXTags.RTEPT) == 0) {

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
                throw new SAXException("Cannot import the route points : " + getRteptID(), ex);
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