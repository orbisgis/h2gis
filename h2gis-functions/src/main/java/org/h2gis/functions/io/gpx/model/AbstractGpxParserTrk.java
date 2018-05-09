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
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Abstract class of the parsers dedicated to tracks. A specific parser for
 * version 1.0 and version 1.1 will extend this class.
 *
 * @author Erwan Bocher and Antonin Piasco
 */
public abstract class AbstractGpxParserTrk extends AbstractGpxParser {

// Indicates if we are in a point or not
    private boolean point;
// Indicates if we are in a segment or not
    private boolean segment;
// Reference to the default contentHandler of the class GpxParserDefault
    private AbstractGpxParserDefault parent;
// A list which will contain the coordinates of the track segment
    private List<Coordinate> trksegList;
// A list which will contain the track segments
    private List<LineString> trkList;
    //The track segment id
    private int trksegID = 1;
    //The track point id
    private int trkptID = 1;

    /**
     * Create a new specific parser. It has in memory the default parser, the
     * contentBuffer, the elementNames, the currentLine and the trkID.
     *
     * @param reader The XMLReader used in the default class
     * @param parent The parser used in the default class
     */
    public void initialise(XMLReader reader, AbstractGpxParserDefault parent) {
        setReader(reader);
        setParent(parent);
        setContentBuffer(parent.getContentBuffer());
        setTrkPreparedStmt(parent.getTrkPreparedStmt());
        setTrkSegmentsPreparedStmt(parent.getTrkSegmentsPreparedStmt());
        setTrkPointsPreparedStmt(parent.getTrkPointsPreparedStmt());
        setElementNames(parent.getElementNames());
        setCurrentLine(parent.getCurrentLine());
        setTrksegList(new ArrayList<Coordinate>());
        setTrkList(new ArrayList<LineString>());
    }

    /**
     * Fires whenever an XML start markup is encountered. It creates a new
     * trackSegment when a <trkseg> markup is encountered. It creates a new
     * trackPoint when a <trkpt> markup is encountered. It saves informations
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
        if (localName.equalsIgnoreCase(GPXTags.TRKSEG)) {
            segment = true;
            GPXLine trkSegment = new GPXLine(GpxMetadata.TRKSEGFIELDCOUNT);
            trkSegment.setValue(GpxMetadata.LINEID, trksegID++);
            trkSegment.setValue(GpxMetadata.TRKSEG_TRKID, getCurrentLine().getValues()[GpxMetadata.LINEID]);
            setCurrentSegment(trkSegment);
            trksegList.clear();
        } else if (localName.equalsIgnoreCase(GPXTags.TRKPT)) {
            point = true;
            GPXPoint trackPoint = new GPXPoint(GpxMetadata.TRKPTFIELDCOUNT);
            Coordinate coordinate = GPXCoordinate.createCoordinate(attributes);
            Point geom = getGeometryFactory().createPoint(coordinate);
            geom.setSRID(4326);
            trackPoint.setValue(GpxMetadata.THE_GEOM, geom);
            trackPoint.setValue(GpxMetadata.PTLAT, coordinate.y);
            trackPoint.setValue(GpxMetadata.PTLON, coordinate.x);
            trackPoint.setValue(GpxMetadata.PTELE, coordinate.z);
            trackPoint.setValue(GpxMetadata.PTID, trkptID++);
            trackPoint.setValue(GpxMetadata.TRKPT_TRKSEGID, trksegID);
            trksegList.add(coordinate);
            setCurrentPoint(trackPoint);
        }
        // Clear content buffer
        getContentBuffer().delete(0, getContentBuffer().length());
        // Store name of current element in stack
        getElementNames().push(qName);
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
        // currentElement represents the last string encountered in the document
        setCurrentElement(getElementNames().pop());
        if (getCurrentElement().equalsIgnoreCase(GPXTags.TRK)) {
            //parent.setTrksegID(getTrksegID());
            //parent.setTrkptID(getTrkptID());
            // Set the track geometry.
            MultiLineString geometry = getGeometryFactory().createMultiLineString(trkList.toArray(new LineString[trkList.size()]));
            geometry.setSRID(4326);
            getCurrentLine().setGeometry(geometry);
            // if </trk> markup is found, the currentLine is added in the table rtedbd and the default contentHandler is setted.
            try {
                PreparedStatement pStm = getTrkPreparedStmt();
                int i = 1;
                Object[] values = getCurrentLine().getValues();
                for (Object object : values) {
                    pStm.setObject(i, object);
                    i++;
                }
                pStm.execute();
            } catch (SQLException ex) {
                throw new SAXException("Cannot import the track line ", ex);
            }
            getReader().setContentHandler(parent);

        } else if (getCurrentElement().compareToIgnoreCase("trkseg") == 0) {
            Coordinate[] trksegArray = trksegList.toArray(new Coordinate[trksegList.size()]);
            // If there are more than one trackpoint, we can set a geometry to the track segment
            if (trksegList.size() > 1) {
                LineString geometry = getGeometryFactory().createLineString(trksegArray);
                geometry.setSRID(4326);
                getCurrentSegment().setGeometry(geometry);
                trkList.add(geometry);
            }
            // if </trkseg> markup is found, the currentSegment is added in the table trksegdbd.
            try {
                PreparedStatement pStm = getTrkSegmentsPreparedStmt();
                int i = 1;
                Object[] values = getCurrentSegment().getValues();
                for (Object object : values) {
                    pStm.setObject(i, object);
                    i++;
                }
                pStm.execute();
            } catch (SQLException ex) {
                throw new SAXException("Cannot import the track segment ", ex);
            }

        } else if (getCurrentElement().equalsIgnoreCase(GPXTags.TRKPT)) {
            // if </trkpt> markup is found, the currentPoint is added in the table trkptdbd.
            point = false;
            try {
                PreparedStatement pStm = getTrkPointsPreparedStmt();
                int i = 1;
                Object[] values = getCurrentPoint().getValues();
                for (Object object : values) {
                    pStm.setObject(i, object);
                    i++;
                }
                pStm.execute();
            } catch (SQLException ex) {
                throw new SAXException("Cannot import the track waypoints.", ex);
            }
        } else if (point) {
            getCurrentPoint().setAttribute(getCurrentElement(), getContentBuffer());
        } else if (segment) {
            getCurrentSegment().setExtensions();
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
     * Set the list corresponding to the segments' list of coordinates of the
     * actual track.
     *
     * @param trkList
     */
    public void setTrkList(List<LineString> trkList) {
        this.trkList = trkList;
    }

    /**
     * Set the list corresponding to the points' coordinates of the actual track
     * segment.
     *
     * @param trksegList
     */
    public void setTrksegList(List<Coordinate> trksegList) {
        this.trksegList = trksegList;
    }
}
