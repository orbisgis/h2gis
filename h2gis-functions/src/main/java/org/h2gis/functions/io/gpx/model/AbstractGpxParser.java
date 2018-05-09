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

import org.locationtech.jts.geom.GeometryFactory;
import java.sql.PreparedStatement;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Abstract class of all Gpx-Parsers. It contains the more general attributes,
 * setters and getters used in parsers. It also defines the method characters
 * which is used in all other parsers.
 *
 * @author Erwan Bocher and Antonin Piasco
 */
public abstract class AbstractGpxParser extends DefaultHandler {

    //To build a geometry
    private GeometryFactory geometryFactory = new GeometryFactory();
    private XMLReader reader;
    private StringBuilder contentBuffer;
    // String with the value of the element which is being parsed
    private String currentElement;
    // Abstract point which will take values of the current point during the parsing
    private GPXPoint currentPoint;
    // This will take values of the current track segment during the parsing
    private GPXLine currentSegment;
    // Abstract line which will take values of the current line during the parsing
    private GPXLine currentLine;
    // A stack to know in which element we are
    private StringStack elementNames;
    // Variable to know if we are in an element supposed to be parser by a specific parser
    private boolean specificElement;
    //PreparedStatement to manage gpx tables
    private PreparedStatement wptPreparedStmt, rtePreparedStmt, rteptPreparedStmt,
            trkPreparedStmt, trkSegmentsPreparedStmt, trkPointsPreparedStmt;

    /**
     * Fires one or more times for each text node encountered. It saves text
     * informations in contentBuffer.
     *
     * @param ch The characters from the XML document
     * @param start The start position in the array
     * @param length The number of characters to read from the array
     * @throws SAXException Any SAX exception, possibly wrapping another
     * exception
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        contentBuffer.append(String.copyValueOf(ch, start, length));
    }

    /**
     * Gives the actual contentBuffer
     *
     * @return
     */
    public StringBuilder getContentBuffer() {
        return contentBuffer;
    }

    /**
     * Set the contentBuffer.
     *
     * @param contentBuffer
     */
    public void setContentBuffer(StringBuilder contentBuffer) {
        this.contentBuffer = contentBuffer;
    }

    /**
     * Gives a string representing the value of the element which is being
     * parsed.
     *
     * @return
     */
    public String getCurrentElement() {
        return currentElement;
    }

    /**
     * Set the string representing the value of the element which is being
     * parsed.
     *
     * @param currentElement
     */
    public void setCurrentElement(String currentElement) {
        this.currentElement = currentElement;
    }

    /**
     * Gives the point which is being parsed.
     *
     * @return
     */
    public GPXPoint getCurrentPoint() {
        return currentPoint;
    }

    /**
     * Set the point which will be parsed.
     *
     * @param currentPoint
     */
    public void setCurrentPoint(GPXPoint currentPoint) {
        this.currentPoint = currentPoint;
    }

    /**
     * Gives the XMLReader used to parse the document.
     * @return 
     */
    public XMLReader getReader() {
        return reader;
    }

    /**
     * Set the XMLReader used to parse the document.
     *
     * @param reader
     */
    public void setReader(XMLReader reader) {
        this.reader = reader;
    }

    /**
     * Gives the actual StringStack elementNames
     *
     * @return
     */
    public StringStack getElementNames() {
        return elementNames;
    }

    /**
     * Set the actual StringStack elementNames
     *
     * @param elementNames
     */
    public void setElementNames(StringStack elementNames) {
        this.elementNames = elementNames;
    }

    /**
     * Indicates if we are in a specific element (waypoint, route or track).
     *
     * @return true if we are in a specific element, false else
     */
    public boolean isSpecificElement() {
        return specificElement;
    }

    /**
     * Set the indicator to know if we are in a specific element.
     *
     * @param specificElement
     */
    public void setSpecificElement(boolean specificElement) {
        this.specificElement = specificElement;
    }

    /**
     * Get the PreparedStatement of the waypoints table.
     *
     * @return
     */
    public PreparedStatement getWptPreparedStmt() {
        return wptPreparedStmt;
    }

    /**
     * Set the PreparedStatement of the waypoints table.
     *
     * @param wptPreparedStmt
     */
    public void setWptPreparedStmt(PreparedStatement wptPreparedStmt) {
        this.wptPreparedStmt = wptPreparedStmt;
    }

    /**
     * Set the PreparedStatement of the route table.
     *
     * @param rtePreparedStmt
     */
    public void setRtePreparedStmt(PreparedStatement rtePreparedStmt) {
        this.rtePreparedStmt = rtePreparedStmt;
    }

    /**
     * Gives the preparedstatement used to store route data
     *
     * @return
     */
    public PreparedStatement getRtePreparedStmt() {
        return rtePreparedStmt;
    }

    /**
     * Set the PreparedStatement of the route points table.
     *
     * @param rteptPreparedStmt
     */
    public void setRteptPreparedStmt(PreparedStatement rteptPreparedStmt) {
        this.rteptPreparedStmt = rteptPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the route points.
     *
     * @return
     */
    public PreparedStatement getRteptPreparedStmt() {
        return rteptPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the track.
     * @return 
     */
    public PreparedStatement getTrkPreparedStmt() {
        return trkPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the track points.
     * @return 
     */
    public PreparedStatement getTrkPointsPreparedStmt() {
        return trkPointsPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the track segments.
     * @return 
     */
    public PreparedStatement getTrkSegmentsPreparedStmt() {
        return trkSegmentsPreparedStmt;
    }

    /**
     * Set the prepared statement used to store the track.
     * @param trkPreparedStmt 
     */
    public void setTrkPreparedStmt(PreparedStatement trkPreparedStmt) {
        this.trkPreparedStmt = trkPreparedStmt;
    }

    /**
     * Set the prepared statement used to store the track segments.
     * @param trkSegmentsPreparedStmt 
     */
    public void setTrkSegmentsPreparedStmt(PreparedStatement trkSegmentsPreparedStmt) {
        this.trkSegmentsPreparedStmt = trkSegmentsPreparedStmt;
    }

    /**
     * Set the prepared statement used to store the track points.
     * @param trkPointsPreparedStmt 
     */
    public void setTrkPointsPreparedStmt(PreparedStatement trkPointsPreparedStmt) {
        this.trkPointsPreparedStmt = trkPointsPreparedStmt;
    }

    /**
     * Gives the segment which is being parsed.
     *
     * @return
     */
    public GPXLine getCurrentSegment() {
        return currentSegment;
    }

    /**
     * Set the segment which will be parsed.
     *
     * @param currentSegment
     */
    public void setCurrentSegment(GPXLine currentSegment) {
        this.currentSegment = currentSegment;
    }

    /**
     * Gives a geometryFactory to construct gpx geometries
     *
     * @return
     */
    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    /**
     * Gives the line which is being parsed.
     *
     * @return
     */
    public GPXLine getCurrentLine() {
        return currentLine;
    }

    /**
     * Set the line which will be parsed.
     *
     * @param currentLine
     */
    public void setCurrentLine(GPXLine currentLine) {
        this.currentLine = currentLine;
    }
    
}
