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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Main class to parse the GPX file
 *
 * @author Erwan Bocher
 */
public abstract class AbstractGpxParserDefault extends AbstractGpxParser {

    //Suffix table names
    String WAYPOINT = "_waypoint";
    String ROUTE = "_route";
    String ROUTEPOINT = "_routepoint";
    String TRACK = "_track";
    String TRACKSEGMENT = "_tracksegment";
    String TRACKPOINT = "_trackpoint";
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
     * @return
     */
    abstract String getCopyright();

    /**
     * Return the table identifier in the best fit depending on database type
     * @param requestedTable Catalog and schema used
     * @param tableName Table without quotes
     * @param isH2 True if H2, false if PostGRE
     * @return Fin table identifier
     */
    private static String caseIdentifier(TableLocation requestedTable, String tableName, boolean isH2) {
        return new TableLocation(requestedTable.getCatalog(), requestedTable.getSchema(),
                isH2 ? tableName.toUpperCase() : tableName.toLowerCase()).toString();
    }
    /**
     * Reads the document and parses it. The other methods are called
     * automatically when corresponding markup is found.
     *
     * @param inputFile a File representing the gpx file to read
     * @param tableName the table used to create all tables
     * @param connection the connection to the database
     * @return a boolean value if the parser ends successfully or not
     * @throws SQLException if the creation of the tables failed
     */
    public boolean read(File inputFile, String tableName, Connection connection) throws SQLException {
        // Initialisation
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        boolean success = false;
        TableLocation requestedTable = TableLocation.parse(tableName);
        String table = requestedTable.getTable();

        clear();
        // We create the tables to store all gpx data in the database
        String wptTableName = caseIdentifier(requestedTable, table + WAYPOINT, isH2);
        String routeTableName = caseIdentifier(requestedTable, table + ROUTE, isH2);
        String routePointsTableName = caseIdentifier(requestedTable, table + ROUTEPOINT, isH2);
        String trackTableName = caseIdentifier(requestedTable, table + TRACK, isH2);
        String trackSegmentsTableName = caseIdentifier(requestedTable, table + TRACKSEGMENT, isH2);
        String trackPointsTableName = caseIdentifier(requestedTable, table + TRACKPOINT, isH2);

        setWptPreparedStmt(GPXTablesFactory.createWayPointsTable(connection, wptTableName));
        setRtePreparedStmt(GPXTablesFactory.createRouteTable(connection, routeTableName));
        setRteptPreparedStmt(GPXTablesFactory.createRoutePointsTable(connection, routePointsTableName));
        setTrkPreparedStmt(GPXTablesFactory.createTrackTable(connection, trackTableName));
        setTrkSegmentsPreparedStmt(GPXTablesFactory.createTrackSegmentsTable(connection, trackSegmentsTableName));
        setTrkPointsPreparedStmt(GPXTablesFactory.createTrackPointsTable(connection, trackPointsTableName));

        // Initialisation of the contentHandler by default
        try {
            setReader(XMLReaderFactory.createXMLReader());
            getReader().setErrorHandler(this);
            getReader().setContentHandler(this);
            getReader().parse(new InputSource(new FileInputStream(inputFile)));
            success = true;
        } catch (SAXException ex) {
            throw new SQLException(ex);
        } catch (IOException ex) {
            throw new SQLException("Cannot parse the file " + inputFile.getAbsolutePath(), ex);
        } finally {
            // When the reading ends, close() method has to be called
            getWptPreparedStmt().close();
            getRtePreparedStmt().close();
            getRteptPreparedStmt().close();
            getTrkPreparedStmt().close();
            getTrkSegmentsPreparedStmt().close();
            getTrkSegmentsPreparedStmt().close();
            //We drop the table when no gpx data are imported.
            tablesCleaner(connection, new String[]{wptTableName, routeTableName, routePointsTableName,
                trackTableName, trackSegmentsTableName, trackPointsTableName});
        }

        return success;
    }

    /**
     * This method is used to delete tables that contain any rows.
     */
    public void tablesCleaner(Connection connection, String[] tableNames) throws SQLException {
        Statement statement = connection.createStatement();
        for (String tableName : tableNames) {
            ResultSet res = statement.executeQuery("SELECT count(id) FROM " + tableName + ";");
            res.next();
            if (res.getInt(1) == 0) {
                statement.execute("DROP TABLE IF EXISTS " + tableName + ";");
            }
            res.close();
        }
        statement.close();
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
     * @throws SAXException Any SAX exception, possibly wrapping another
     * exception
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.compareToIgnoreCase(GPXTags.GPX) == 0) {
            version = attributes.getValue(GPXTags.VERSION);
            creator = attributes.getValue(GPXTags.CREATOR);
        } else if (localName.compareToIgnoreCase(GPXTags.BOUNDS) == 0) {
            minLat = new Double(attributes.getValue(GPXTags.MINLAT)).doubleValue();
            maxLat = new Double(attributes.getValue(GPXTags.MAXLAT)).doubleValue();
            minLon = new Double(attributes.getValue(GPXTags.MINLON)).doubleValue();
            maxLon = new Double(attributes.getValue(GPXTags.MAXLON)).doubleValue();
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
     * @return
     */
    public String getTime() {
        return time;
    }

    /**
     * Gives the version number of the GPX document.
     *
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gives the name or URL of the software that created the GPX document.
     *
     * @return
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Gives the minimum longitude given by <bounds> element.
     *
     * @return
     */
    public double getMinLon() {
        return minLon;
    }

    /**
     * Gives the maximum longitude given by <bounds> element.
     *
     * @return
     */
    public double getMaxLon() {
        return maxLon;
    }

    /**
     * Gives the minimum latitude given by <bounds> element.
     *
     * @return
     */
    public double getMinLat() {
        return minLat;
    }

    /**
     * Gives the maximum latitude given by <bounds> element.
     *
     * @return
     */
    public double getMaxLat() {
        return maxLat;
    }

    /**
     * Gives a description of the contents of the GPX file.
     *
     * @return
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Gives keywords associated with the file. Search engines or databases can
     * use this information to classify the data.
     *
     * @return
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Gives URLs associated with the location described in the file.
     *
     * @return
     */
    public String getFullLink() {
        return "Link : " + link + "\nText of hyperlink : " + linkText;
    }

    /**
     * Gives the name of person or organization who created the GPX file. Also
     * gives an email address if exist. Also gives a link to Web site or other
     * external information about person if exist.
     *
     * @return
     */
    public String getFullAuthor() {
        return "Author : " + authorName + "\nEmail : " + email + "\nLink : " + authorLink + "\nText : " + authorLinkText;
    }

    /**
     * Set the link related to the author of the ducument.
     *
     * @param authorLink
     */
    public void setAuthorLink(String authorLink) {
        this.authorLink = authorLink;
    }

    /**
     * Set the description of the link related to the author.
     *
     * @param authorLinkText
     */
    public void setAuthorLinkText(String authorLinkText) {
        this.authorLinkText = authorLinkText;
    }

    /**
     * Set the name of the author of the document.
     *
     * @param authorName
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Set the email of the author of the document.
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Set the link related to the document.
     *
     * @param link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Set the description of hte document link.
     *
     * @param linkText
     */
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    /**
     * Gives the name of the document.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the document.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gives the parser used to parse waypoint.
     *
     * @return
     */
    public AbstractGpxParserWpt getWptParser() {
        return wptParser;
    }

    /**
     * Set the parser used to parse waypoint.
     *
     * @param wptParser
     */
    public void setWptParser(AbstractGpxParserWpt wptParser) {
        this.wptParser = wptParser;
    }

    /**
     * Set the parser used to parse routes.
     *
     * @param rteParser
     */
    public void setRteParser(AbstractGpxParserRte rteParser) {
        this.rteParser = rteParser;
    }

    /**
     * Gives the parser used to parse routes.
     *
     * @return
     */
    public AbstractGpxParserRte getRteParser() {
        return rteParser;
    }

    /**
     * Set the parser used to parse the track
     *
     * @param trkParser
     */
    public void setTrkParser(AbstractGpxParserTrk trkParser) {
        this.trkParser = trkParser;
    }

    /**
     * Givers the parser used to parse the track
     *
     * @return
     */
    public AbstractGpxParserTrk getTrkParser() {
        return trkParser;
    }
}
