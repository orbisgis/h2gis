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

import org.locationtech.jts.geom.GeometryFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.PreparedStatement;

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
     * @return get buffer
     */
    public StringBuilder getContentBuffer() {
        return contentBuffer;
    }

    /**
     * Set the contentBuffer.
     *
     * @param contentBuffer set a buffer
     */
    public void setContentBuffer(StringBuilder contentBuffer) {
        this.contentBuffer = contentBuffer;
    }

    /**
     * Gives a string representing the value of the element which is being
     * parsed.
     *
     * @return get the current element
     */
    public String getCurrentElement() {
        return currentElement;
    }

    /**
     * Set the string representing the value of the element which is being
     * parsed.
     *
     * @param currentElement set current element
     */
    public void setCurrentElement(String currentElement) {
        this.currentElement = currentElement;
    }

    /**
     * Gives the point which is being parsed.
     *
     * @return current gpx point
     */
    public GPXPoint getCurrentPoint() {
        return currentPoint;
    }

    /**
     * Set the point which will be parsed.
     *
     * @param currentPoint set current gpx point
     */
    public void setCurrentPoint(GPXPoint currentPoint) {
        this.currentPoint = currentPoint;
    }

    /**
     * Gives the XMLReader used to parse the document.
     * @return get {@link XMLReader}
     */
    public XMLReader getReader() {
        return reader;
    }

    /**
     * Set the XMLReader used to parse the document.
     *
     * @param reader set {@link XMLReader}
     */
    public void setReader(XMLReader reader) {
        this.reader = reader;
    }

    /**
     * Gives the actual StringStack elementNames
     *
     * @return the element names
     */
    public StringStack getElementNames() {
        return elementNames;
    }

    /**
     * Set the actual StringStack elementNames
     *
     * @param elementNames set element names
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
     * @param specificElement set true it's a specific element type
     */
    public void setSpecificElement(boolean specificElement) {
        this.specificElement = specificElement;
    }

    /**
     * Get the PreparedStatement of the waypoints table.
     *
     * @return the waypoints preparedstatement
     */
    public PreparedStatement getWptPreparedStmt() {
        return wptPreparedStmt;
    }

    /**
     * Set the PreparedStatement of the waypoints table.
     *
     * @param wptPreparedStmt set the waypoints preparedstatement
     */
    public void setWptPreparedStmt(PreparedStatement wptPreparedStmt) {
        this.wptPreparedStmt = wptPreparedStmt;
    }

    /**
     * Set the PreparedStatement of the route table.
     *
     * @param rtePreparedStmt set the routes preparedstatement
     */
    public void setRtePreparedStmt(PreparedStatement rtePreparedStmt) {
        this.rtePreparedStmt = rtePreparedStmt;
    }

    /**
     * Gives the preparedstatement used to store route data
     *
     * @return get the routes preparedstatement
     */
    public PreparedStatement getRtePreparedStmt() {
        return rtePreparedStmt;
    }

    /**
     * Set the PreparedStatement of the route points table.
     *
     * @param rteptPreparedStmt set the routes preparedstatement
     */
    public void setRteptPreparedStmt(PreparedStatement rteptPreparedStmt) {
        this.rteptPreparedStmt = rteptPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the route points.
     *
     * @return the routes preparedstatement
     */
    public PreparedStatement getRteptPreparedStmt() {
        return rteptPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the track.
     * @return the track preparedstatement
     */
    public PreparedStatement getTrkPreparedStmt() {
        return trkPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the track points.
     * @return the points preparedstatement
     */
    public PreparedStatement getTrkPointsPreparedStmt() {
        return trkPointsPreparedStmt;
    }

    /**
     * Gives the prepared statement used to store the track segments.
     * @return the track preparedstatement
     */
    public PreparedStatement getTrkSegmentsPreparedStmt() {
        return trkSegmentsPreparedStmt;
    }

    /**
     * Set the prepared statement used to store the track.
     * @param trkPreparedStmt  set the preparedstatement to save the tracks
     */
    public void setTrkPreparedStmt(PreparedStatement trkPreparedStmt) {
        this.trkPreparedStmt = trkPreparedStmt;
    }

    /**
     * Set the prepared statement used to store the track segments.
     * @param trkSegmentsPreparedStmt set the preparedstatement to save the lines
     */
    public void setTrkSegmentsPreparedStmt(PreparedStatement trkSegmentsPreparedStmt) {
        this.trkSegmentsPreparedStmt = trkSegmentsPreparedStmt;
    }

    /**
     * Set the prepared statement used to store the track points.
     * @param trkPointsPreparedStmt set the preparedstatement to store points
     */
    public void setTrkPointsPreparedStmt(PreparedStatement trkPointsPreparedStmt) {
        this.trkPointsPreparedStmt = trkPointsPreparedStmt;
    }

    /**
     * Gives the segment which is being parsed.
     *
     * @return current gpx line
     */
    public GPXLine getCurrentSegment() {
        return currentSegment;
    }

    /**
     * Set the segment which will be parsed.
     *
     * @param currentSegment input GPX line
     */
    public void setCurrentSegment(GPXLine currentSegment) {
        this.currentSegment = currentSegment;
    }

    /**
     * Gives a geometryFactory to construct gpx geometries
     *
     * @return current geometry factory
     */
    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    /**
     * Gives the line which is being parsed.
     *
     * @return gpx line
     */
    public GPXLine getCurrentLine() {
        return currentLine;
    }

    /**
     * Set the line which will be parsed.
     *
     * @param currentLine set the line to parse
     */
    public void setCurrentLine(GPXLine currentLine) {
        this.currentLine = currentLine;
    }
    
}
