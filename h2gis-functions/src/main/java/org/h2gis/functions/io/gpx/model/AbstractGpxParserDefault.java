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

import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.h2gis.utilities.FileUtilities;

/**
 * Main class to parse the GPX file
 *
 * @author Erwan Bocher
 */
public abstract class AbstractGpxParserDefault extends AbstractGpxParser {

    private final Connection connection;
    private final File fileName;
    private final String encoding;
    private final boolean deleteTable;
    // Specific parsers
    private AbstractGpxParserWpt wptParser;
    private AbstractGpxParserRte rteParser;
    private AbstractGpxParserTrk trkParser;
    // General informations about the document to read
    // The <bounds> element has attributes which specify minimum and maximum latitude and longitude.
    private double minLat, maxLat, minLon, maxLon;
    // Name or URL of the software that created ther GPX document.
    private String creator;
    // The version number of the GPX document.
    private String version;
    // The name of the GPX file.
    private String name;
    // A description of the contents of the GPX file.
    private String desc;
    // URLs associated with the location described in the file.
    private String link;
    // Text of hyperlink.
    private String linkText;
    // The creation date of the file.
    private String time;
    // Name of the person or organization who created the GPX file.
    private String authorName;
    // Email address of the person or organization who created the GPX file.
    private String email;
    // Link to Web site or other external information about person.
    private String authorLink;
    //Text of author's hyperlink.
    private String authorLinkText;
    // Keywords associated with the file.
    private String keywords;
    // The max size of the StringStack
    public static final int STRINGSTACK_SIZE = 50;

    public AbstractGpxParserDefault(Connection connection, File fileName, String encoding, boolean deleteTable) {
        super();
        this.connection=connection;
        this.fileName=fileName;
        this.encoding=encoding;
        this.deleteTable=deleteTable;
    }

    /**
     * Initialisation of all the indicators used to read the document.
     */
    public void clear() {
        setElementNames(new StringStack(STRINGSTACK_SIZE));
        setContentBuffer(new StringBuilder());
        setSpecificElement(false);
        minLat = 0;
        maxLat = 0;
        minLon = 0;
        maxLon = 0;
        creator = null;
        version = null;
        name = null;
        desc = null;
        link = null;
        linkText = null;
        time = null;
        authorName = null;
        email = null;
        authorLink = null;
        authorLinkText = null;
        keywords = null;
    }

    /**
     * Gives copyright and license information governing use of the file.
     *
     * @return copyright value
     */
    abstract String getCopyright();
    
    
    

    /**
     * Reads the document and parses it.The other methods are called
     * automatically when corresponding markup is found.
     *
     * @param tableName the table used to create all tables
     * @param progress Progress visitor following the execution.
     * @return a boolean value if the parser ends successfully or not
     * @throws SQLException or FileNotFoundException if the creation of the tables failed
     */
    public String[] read(String tableName, ProgressVisitor progress) throws SQLException, FileNotFoundException {
        if (FileUtilities.isFileImportable(fileName, "gpx")) {
            // Initialisation
            final DBTypes dbType = DBUtils.getDBType(connection);
            ArrayList<String> tableNames = new ArrayList<>();
            TableLocation requestedTable = TableLocation.parse(tableName, dbType);
            if (deleteTable) {
                GPXTablesFactory.dropOSMTables(connection, requestedTable);
            }
            if (fileName.length() == 0) {
                String outputEmptyTable = requestedTable.toString();
                JDBCUtilities.createEmptyTable(connection, outputEmptyTable);
                tableNames.add(outputEmptyTable);
            }
            else {
                String table = requestedTable.getTable();
                clear();
                GpxPreparser gpxPreparser = new GpxPreparser();
                try {
                    gpxPreparser.read(fileName, encoding);
                } catch (SAXException ex) {
                    throw new SQLException(ex);
                } catch (IOException ex) {
                    throw new SQLException(ex);
                }
                // We create the tables to store all gpx data in the database
                if (gpxPreparser.getTotalWpt() > 0) {
                    String wptTableName = TableUtilities.caseIdentifier(requestedTable, table + GPXTablesFactory.WAYPOINT, dbType);
                    if (JDBCUtilities.tableExists(connection, TableLocation.parse(wptTableName, dbType))) {
                        throw new SQLException("The table " + wptTableName + " already exists.");
                    }
                    setWptPreparedStmt(GPXTablesFactory.createWayPointsTable(connection, wptTableName));
                    tableNames.add(wptTableName);
                }
                if (gpxPreparser.getTotalRte() > 0 && gpxPreparser.getTotalRtept() > 0) {
                    String routeTableName = TableUtilities.caseIdentifier(requestedTable, table + GPXTablesFactory.ROUTE, dbType);
                    if (JDBCUtilities.tableExists(connection, TableLocation.parse(routeTableName, dbType))) {
                        throw new SQLException("The table " + routeTableName + " already exists.");
                    }
                    String routePointsTableName = TableUtilities.caseIdentifier(requestedTable, table + GPXTablesFactory.ROUTEPOINT, dbType);
                    if (JDBCUtilities.tableExists(connection, TableLocation.parse(routePointsTableName, dbType))) {
                        throw new SQLException("The table " + routePointsTableName + " already exists.");
                    }

                    setRtePreparedStmt(GPXTablesFactory.createRouteTable(connection, routeTableName));
                    setRteptPreparedStmt(GPXTablesFactory.createRoutePointsTable(connection, routePointsTableName));

                    tableNames.add(routeTableName);
                    tableNames.add(routePointsTableName);

                }

                if (gpxPreparser.getTotalTrk() > 0 && gpxPreparser.getTotalTrkseg() > 0
                        && gpxPreparser.getTotalTrkpt() > 0) {
                    String trackTableName = TableUtilities.caseIdentifier(requestedTable, table + GPXTablesFactory.TRACK, dbType);
                    if (JDBCUtilities.tableExists(connection, TableLocation.parse(trackTableName, dbType))) {
                        throw new SQLException("The table " + trackTableName + " already exists.");
                    }

                    String trackSegmentsTableName = TableUtilities.caseIdentifier(requestedTable, table + GPXTablesFactory.TRACKSEGMENT, dbType);
                    if (JDBCUtilities.tableExists(connection, TableLocation.parse(trackSegmentsTableName, dbType))) {
                        throw new SQLException("The table " + trackSegmentsTableName + " already exists.");
                    }

                    String trackPointsTableName = TableUtilities.caseIdentifier(requestedTable, table + GPXTablesFactory.TRACKPOINT, dbType);
                    if (JDBCUtilities.tableExists(connection, TableLocation.parse(trackPointsTableName, dbType))) {
                        throw new SQLException("The table " + trackPointsTableName + " already exists.");
                    }

                    setTrkPreparedStmt(GPXTablesFactory.createTrackTable(connection, trackTableName));
                    setTrkSegmentsPreparedStmt(GPXTablesFactory.createTrackSegmentsTable(connection, trackSegmentsTableName));
                    setTrkPointsPreparedStmt(GPXTablesFactory.createTrackPointsTable(connection, trackPointsTableName));

                    tableNames.add(trackTableName);
                    tableNames.add(trackSegmentsTableName);
                    tableNames.add(trackPointsTableName);

                }

                // Initialisation of the contentHandler by default
                try {
                    setReader(XMLReaderFactory.createXMLReader());
                    getReader().setErrorHandler(this);
                    getReader().setContentHandler(this);
                    getReader().parse(new InputSource(new FileInputStream(fileName)));
                    return tableNames.toArray(new String[0]);
                } catch (SAXException ex) {
                    throw new SQLException(ex);
                } catch (IOException ex) {
                    throw new SQLException("Cannot parse the file " + fileName.getAbsolutePath(), ex);
                } finally {
                    // When the reading ends, close() method has to be called
                    if (getWptPreparedStmt() != null) {
                        getWptPreparedStmt().close();
                    }
                    if (getRteptPreparedStmt() != null) {
                        getRtePreparedStmt().close();
                        getRteptPreparedStmt().close();
                    }
                    if (getTrkPointsPreparedStmt() != null) {
                        getTrkPreparedStmt().close();
                        getTrkSegmentsPreparedStmt().close();
                        getTrkPointsPreparedStmt().close();
                    }
                }
            }
        }
        return null;
    }    

    /**
     * Fires whenever an XML start markup is encountered. It takes general
     * information about the document. It change the ContentHandler to parse
     * specific informations when {@code <wpt>}, {@code <rte>} or {@code <trk>} markup are found.
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
        if (localName.compareToIgnoreCase(GPXTags.GPX) == 0) {
            version = attributes.getValue(GPXTags.VERSION);
            creator = attributes.getValue(GPXTags.CREATOR);
        } else if (localName.compareToIgnoreCase(GPXTags.BOUNDS) == 0) {
            minLat = Double.parseDouble(attributes.getValue(GPXTags.MINLAT));
            maxLat = Double.parseDouble(attributes.getValue(GPXTags.MAXLAT));
            minLon = Double.parseDouble(attributes.getValue(GPXTags.MINLON));
            maxLon = Double.parseDouble(attributes.getValue(GPXTags.MAXLON));
        }
        // Clear content buffer

        getContentBuffer()
                .delete(0, getContentBuffer().length());
        // Store name of current element in stack
        getElementNames()
                .push(qName);
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
        // currentElement represents the last string encountered in the document
        setCurrentElement(getElementNames().pop());

        if (getCurrentElement().equalsIgnoreCase(GPXTags.WPT)) {
            setSpecificElement(false);
        } else if (getCurrentElement().equalsIgnoreCase(GPXTags.RTE)) {
            setSpecificElement(false);
        } else if (getCurrentElement().equalsIgnoreCase(GPXTags.TRK)) {
            setSpecificElement(false);
        } else if ((getCurrentElement().equalsIgnoreCase(GPXTags.TIME)) && (!isSpecificElement())) {
            time = getContentBuffer().toString();
        } else if ((getCurrentElement().equalsIgnoreCase(GPXTags.DESC)) && (!isSpecificElement())) {
            desc = getContentBuffer().toString();
        } else if (localName.equalsIgnoreCase(GPXTags.KEYWORDS)) {
            keywords = getContentBuffer().toString();
        }
    }

    /**
     * Gives the date when this file is created.
     *
     * @return time value
     */
    public String getTime() {
        return time;
    }

    /**
     * Gives the version number of the GPX document.
     *
     * @return gpx version of the document
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gives the name or URL of the software that created the GPX document.
     *
     * @return gpx creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Gives the minimum longitude given by {@code <bounds>} element.
     *
     * @return the minimum longitude
     */
    public double getMinLon() {
        return minLon;
    }

    /**
     * Gives the maximum longitude given by {@code <bounds>} element.
     *
     * @return the maximum longitude
     */
    public double getMaxLon() {
        return maxLon;
    }

    /**
     * Gives the minimum latitude given by {@code <bounds>} element.
     *
     * @return the minimum latitude
     */
    public double getMinLat() {
        return minLat;
    }

    /**
     * Gives the maximum latitude given by {@code <bounds>} element.
     *
     * @return the maximum latitude
     */
    public double getMaxLat() {
        return maxLat;
    }

    /**
     * Gives a description of the contents of the GPX file.
     *
     * @return the description value
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Gives keywords associated with the file. Search engines or databases can
     * use this information to classify the data.
     *
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Gives URLs associated with the location described in the file.
     *
     * @return urls
     */
    public String getFullLink() {
        return "Link : " + link + "\nText of hyperlink : " + linkText;
    }

    /**
     * Gives the name of person or organization who created the GPX file. Also
     * gives an email address if exist. Also gives a link to Web site or other
     * external information about person if exist.
     *
     * @return author
     */
    public String getFullAuthor() {
        return "Author : " + authorName + "\nEmail : " + email + "\nLink : " + authorLink + "\nText : " + authorLinkText;
    }

    /**
     * Set the link related to the author of the document.
     *
     * @param authorLink the link related to the author
     */
    public void setAuthorLink(String authorLink) {
        this.authorLink = authorLink;
    }

    /**
     * Set the description of the link related to the author.
     *
     * @param authorLinkText the description of the link related to the author
     */
    public void setAuthorLinkText(String authorLinkText) {
        this.authorLinkText = authorLinkText;
    }

    /**
     * Set the name of the author of the document.
     *
     * @param authorName the name of the author
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Set the email of the author of the document.
     *
     * @param email the email of the author
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Set the link related to the document.
     *
     * @param link link related to the document
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Set the description of the document link.
     *
     * @param linkText the description of the document link
     */
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    /**
     * Gives the name of the document.
     *
     * @return the name of the document
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the document.
     *
     * @param name the name of the document
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gives the parser used to parse waypoint.
     *
     * @return the wpt parse
     */
    public AbstractGpxParserWpt getWptParser() {
        return wptParser;
    }

    /**
     * Set the parser used to parse waypoint.
     *
     * @param wptParser set the wptParser
     */
    public void setWptParser(AbstractGpxParserWpt wptParser) {
        this.wptParser = wptParser;
    }

    /**
     * Set the parser used to parse routes.
     *
     * @param rteParser set the RTE parser
     */
    public void setRteParser(AbstractGpxParserRte rteParser) {
        this.rteParser = rteParser;
    }

    /**
     * Gives the parser used to parse routes.
     *
     * @return the Rte parser
     */
    public AbstractGpxParserRte getRteParser() {
        return rteParser;
    }

    /**
     * Set the parser used to parse the track
     *
     * @param trkParser set the trk parser
     */
    public void setTrkParser(AbstractGpxParserTrk trkParser) {
        this.trkParser = trkParser;
    }

    /**
     * Givers the parser used to parse the track
     *
     * @return the Trk parser
     */
    public AbstractGpxParserTrk getTrkParser() {
        return trkParser;
    }
}
