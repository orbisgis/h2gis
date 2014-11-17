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
package org.h2gis.drivers.osm;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parse an OSM file and store the elements into a database. The database model
 * contains 10 tables.
 *
 *
 * @author Erwan Bocher
 */
public class OSMParser extends DefaultHandler {

    //Suffix table names
    private final String NODE = "_node";
    private final String WAY = "_way";
    private final String NODE_TAG = "_node_tag";
    private final String WAY_TAG = "_way_tag";
    private final String WAY_NODE = "_way_node";
    private final String RELATION = "_relation";
    private final String RELATION_TAG = "_relation_tag";
    private final String NODE_MEMBER = "_node_member";
    private final String WAY_MEMBER = "_way_member";
    private final String RELATION_MEMBER = "_relation_member";
    private PreparedStatement nodePreparedStmt;
    private PreparedStatement nodeTagPreparedStmt;
    private PreparedStatement wayPreparedStmt;
    private PreparedStatement wayTagPreparedStmt;
    private PreparedStatement relationPreparedStmt;
    private PreparedStatement relationTagPreparedStmt;
    private PreparedStatement nodeMemberPreparedStmt;
    private PreparedStatement wayMemberPreparedStmt;
    private PreparedStatement relationMemberPreparedStmt;
    private int idMemberOrder = 1;
    private TAG_LOCATION tagLocation;
    private final GeometryFactory gf = new GeometryFactory();
    private NodeOSMElement nodeOSMElement;
    private WayOSMElement wayOSMElement;
    private PreparedStatement wayNodePreparedStmt;
    private OSMElement relationOSMElement;
    private PreparedStatement updateGeometryWayPreparedStmt;

    public OSMParser() {

    }

    /**
     * Read the OSM file and create its corresponding tables.
     *
     * @param inputFile
     * @param tableName
     * @param connection
     * @param progress
     * @return
     * @throws SQLException
     */
    public boolean read(Connection connection, String tableName, File inputFile, ProgressVisitor progress) throws SQLException {
        // Initialisation
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        boolean success = false;
        TableLocation requestedTable = TableLocation.parse(tableName, isH2);
        String osmTableName = requestedTable.getTable();
        checkOSMTables(connection, isH2, requestedTable, osmTableName);
        createOSMDatabaseModel(connection, isH2, requestedTable, osmTableName);

        FileInputStream fs = null;
        try {
            fs = new FileInputStream(inputFile);
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setErrorHandler(this);
            parser.setContentHandler(this);
            parser.parse(new InputSource(fs));         
            success = true;
        } catch (SAXException ex) {
            throw new SQLException(ex);
        } catch (IOException ex) {
            throw new SQLException("Cannot parse the file " + inputFile.getAbsolutePath(), ex);
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException ex) {
                throw new SQLException("Cannot close the file " + inputFile.getAbsolutePath(), ex);
            }
            // When the reading ends, close() method has to be called
            if (nodePreparedStmt != null) {
                nodePreparedStmt.close();
            }
            if (nodeTagPreparedStmt != null) {
                nodeTagPreparedStmt.close();
            }
            if (wayPreparedStmt != null) {
                wayPreparedStmt.close();
            }
            if (wayTagPreparedStmt != null) {
                wayTagPreparedStmt.close();
            }
            if (wayNodePreparedStmt != null) {
                wayNodePreparedStmt.close();
            }
            if (relationPreparedStmt != null) {
                relationPreparedStmt.close();
            }
            if (relationTagPreparedStmt != null) {
                relationTagPreparedStmt.close();
            }
            if (nodeMemberPreparedStmt != null) {
                nodeMemberPreparedStmt.close();
            }
            if (wayMemberPreparedStmt != null) {
                wayMemberPreparedStmt.close();
            }
            if (relationMemberPreparedStmt != null) {
                relationMemberPreparedStmt.close();
            }
            if (updateGeometryWayPreparedStmt != null) {
                updateGeometryWayPreparedStmt.close();
            }
        }

        return success;
    }

    /**
     * Check if one table already exists
     *
     * @param connection
     * @param isH2
     * @param requestedTable
     * @param osmTableName
     * @throws SQLException
     */
    private void checkOSMTables(Connection connection, boolean isH2, TableLocation requestedTable, String osmTableName) throws SQLException {
        String[] omsTables = new String[]{NODE, NODE_TAG, WAY, WAY_NODE, WAY_TAG, RELATION, RELATION_TAG, NODE_MEMBER, WAY_MEMBER, RELATION_MEMBER};
        for (String omsTableSuffix : omsTables) {
            String osmTable = caseIdentifier(requestedTable, osmTableName + omsTableSuffix, isH2);
            if (JDBCUtilities.tableExists(connection, osmTable)) {
                throw new SQLException("The table " + osmTable + " already exists.");
            }
        }
    }

    /**
     * Return the table identifier in the best fit depending on database type
     *
     * @param requestedTable Catalog and schema used
     * @param tableName Table without quotes
     * @param isH2 True if H2, false if PostGRES
     * @return Find table identifier
     */
    private static String caseIdentifier(TableLocation requestedTable, String tableName, boolean isH2) {
        return new TableLocation(requestedTable.getCatalog(), requestedTable.getSchema(),
                TableLocation.parse(tableName, isH2).getTable()).toString();
    }

    /**
     * Create the OMS data model to store the content of the file
     *
     * @param connection
     * @param isH2
     * @param requestedTable
     * @param osmTableName
     * @throws SQLException
     */
    private void createOSMDatabaseModel(Connection connection, boolean isH2, TableLocation requestedTable, String osmTableName) throws SQLException {
        //Create the NODE table
        String nodeTableName = caseIdentifier(requestedTable, osmTableName + NODE, isH2);
        nodePreparedStmt = OSMTablesFactory.createNodeTable(connection, nodeTableName);
        String nodeTagTableName = caseIdentifier(requestedTable, osmTableName + NODE_TAG, isH2);
        nodeTagPreparedStmt = OSMTablesFactory.createNodeTagTable(connection, nodeTagTableName, nodeTableName);
        String wayTableName = caseIdentifier(requestedTable, osmTableName + WAY, isH2);
        wayPreparedStmt = OSMTablesFactory.createWayTable(connection, wayTableName);
        String wayTagTableName = caseIdentifier(requestedTable, osmTableName + WAY_TAG, isH2);
        wayTagPreparedStmt = OSMTablesFactory.createWayTagTable(connection, wayTagTableName, wayTableName);
        String wayNodeTableName = caseIdentifier(requestedTable, osmTableName + WAY_NODE, isH2);
        wayNodePreparedStmt = OSMTablesFactory.createWayNodeTable(connection, wayNodeTableName, nodeTableName, wayTableName);
        String relationTableName = caseIdentifier(requestedTable, osmTableName + RELATION, isH2);
        relationPreparedStmt = OSMTablesFactory.createRelationTable(connection, relationTableName);
        String relationTagTableName = caseIdentifier(requestedTable, osmTableName + RELATION_TAG, isH2);
        relationTagPreparedStmt = OSMTablesFactory.createRelationTagTable(connection, relationTableName, relationTagTableName);
        String nodeMemberTableName = caseIdentifier(requestedTable, osmTableName + NODE_MEMBER, isH2);
        nodeMemberPreparedStmt = OSMTablesFactory.createNodeMemberTable(connection, nodeMemberTableName, relationTableName, nodeTableName);
        String wayMemberTableName = caseIdentifier(requestedTable, osmTableName + WAY_MEMBER, isH2);
        wayMemberPreparedStmt = OSMTablesFactory.createWayMemberTable(connection, wayMemberTableName, relationTableName, wayTableName);
        String relationMemberTableName = caseIdentifier(requestedTable, osmTableName + RELATION_MEMBER, isH2);
        relationMemberPreparedStmt = OSMTablesFactory.createRelationMemberTable(connection, relationMemberTableName, relationTableName);
        updateGeometryWayPreparedStmt = OSMTablesFactory.updateGeometryWayTable(connection, wayTableName, wayNodeTableName, nodeTableName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.compareToIgnoreCase("osm") == 0) {
        } else if (localName.compareToIgnoreCase("node") == 0) {
            nodeOSMElement = new NodeOSMElement();
            setCommonsAttributes(nodeOSMElement, attributes);
            nodeOSMElement.createPoint(gf, attributes.getValue("lon"), attributes.getValue("lat"));
            tagLocation = TAG_LOCATION.NODE;
        } else if (localName.compareToIgnoreCase("way") == 0) {
            wayOSMElement = new WayOSMElement();
            setCommonsAttributes(wayOSMElement, attributes);
            tagLocation = TAG_LOCATION.WAY;
        } else if (localName.compareToIgnoreCase("tag") == 0) {
            String key = attributes.getValue("k");
            String value = attributes.getValue("v");
            if (tagLocation == TAG_LOCATION.NODE) {
                nodeOSMElement.addTag(key, value);
            } else if (tagLocation == TAG_LOCATION.WAY) {
                wayOSMElement.addTag(key, value);
            } else if (tagLocation == TAG_LOCATION.RELATION) {
                relationOSMElement.addTag(key, value);
            }
        } else if (localName.compareToIgnoreCase("nd") == 0) {
            wayOSMElement.addRef(attributes.getValue("ref"));
        } else if (localName.compareToIgnoreCase("relation") == 0) {
            relationOSMElement = new OSMElement();
            setCommonsAttributes(relationOSMElement, attributes);
            tagLocation = TAG_LOCATION.RELATION;
        } else if (localName.compareToIgnoreCase("member") == 0) {
            if (attributes.getValue("type").equalsIgnoreCase("node")) {
                try {
                    nodeMemberPreparedStmt.setObject(1, relationOSMElement.getID());
                    nodeMemberPreparedStmt.setObject(2, Long.valueOf(attributes.getValue("ref")));
                    nodeMemberPreparedStmt.setObject(3, attributes.getValue("role"));
                    nodeMemberPreparedStmt.setObject(4, idMemberOrder);
                    nodeMemberPreparedStmt.addBatch();
                } catch (SQLException ex) {
                    throw new SAXException("Cannot insert the node member for the relation :  " + relationOSMElement.getID(), ex);
                }
            } else if (attributes.getValue("type").equalsIgnoreCase("way")) {
                try {
                    wayMemberPreparedStmt.setObject(1, relationOSMElement.getID());
                    wayMemberPreparedStmt.setObject(2, Long.valueOf(attributes.getValue("ref")));
                    wayMemberPreparedStmt.setObject(3, attributes.getValue("role"));
                    wayMemberPreparedStmt.setObject(4, idMemberOrder);
                    wayMemberPreparedStmt.addBatch();
                } catch (SQLException ex) {
                    throw new SAXException("Cannot insert the way member for the relation :  " + relationOSMElement.getID(), ex);
                }
            } else if (attributes.getValue("type").equalsIgnoreCase("relation")) {
                try {
                    relationMemberPreparedStmt.setObject(1, relationOSMElement.getID());
                    relationMemberPreparedStmt.setObject(2, Long.valueOf(attributes.getValue("ref")));
                    relationMemberPreparedStmt.setObject(3, attributes.getValue("role"));
                    relationMemberPreparedStmt.setObject(4, idMemberOrder);
                    relationMemberPreparedStmt.addBatch();
                } catch (SQLException ex) {
                    throw new SAXException("Cannot insert the relation member for the relation :  " + relationOSMElement.getID(), ex);
                }
            }

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.compareToIgnoreCase("node") == 0) {
            tagLocation = TAG_LOCATION.OTHER;
            try {
                nodePreparedStmt.setObject(1, nodeOSMElement.getID());
                nodePreparedStmt.setObject(2, nodeOSMElement.getPoint());
                nodePreparedStmt.setObject(3, nodeOSMElement.getUser());
                nodePreparedStmt.setObject(4, nodeOSMElement.getUID());
                nodePreparedStmt.setObject(5, nodeOSMElement.getVisible());
                nodePreparedStmt.setObject(6, nodeOSMElement.getVersion());
                nodePreparedStmt.setObject(7, nodeOSMElement.getChangeSet());
                nodePreparedStmt.setObject(8, nodeOSMElement.getTimeStamp());
                nodePreparedStmt.execute();
                HashMap<String, String> tags = nodeOSMElement.getTags();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    nodeTagPreparedStmt.setObject(1, nodeOSMElement.getID());
                    nodeTagPreparedStmt.setObject(2, entry.getKey());
                    nodeTagPreparedStmt.setObject(3, entry.getValue());
                    nodeTagPreparedStmt.addBatch();
                }
                nodeTagPreparedStmt.executeBatch();

            } catch (SQLException ex) {
                throw new SAXException("Cannot insert the node  :  " + nodeOSMElement.getID(), ex);
            }
        } else if (localName.compareToIgnoreCase("way") == 0) {
            tagLocation = TAG_LOCATION.OTHER;
            try {
                wayPreparedStmt.setObject(1, wayOSMElement.getID());
                wayPreparedStmt.setObject(2, null);
                wayPreparedStmt.setObject(3, wayOSMElement.getUser());
                wayPreparedStmt.setObject(4, wayOSMElement.getUID());
                wayPreparedStmt.setObject(5, wayOSMElement.getVisible());
                wayPreparedStmt.setObject(6, wayOSMElement.getVersion());
                wayPreparedStmt.setObject(7, wayOSMElement.getChangeSet());
                wayPreparedStmt.setObject(8, wayOSMElement.getTimeStamp());
                wayPreparedStmt.execute();

                HashMap<String, String> tags = wayOSMElement.getTags();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    wayTagPreparedStmt.setObject(1, wayOSMElement.getID());
                    wayTagPreparedStmt.setObject(2, entry.getKey());
                    wayTagPreparedStmt.setObject(3, entry.getValue());
                    wayTagPreparedStmt.addBatch();
                }
                wayTagPreparedStmt.executeBatch();

                HashMap<Integer, Long> nodesRef = wayOSMElement.getNodesRef();
                for (Map.Entry<Integer, Long> entry : nodesRef.entrySet()) {
                    Integer order = entry.getKey();
                    Long ref = entry.getValue();
                    wayNodePreparedStmt.setObject(1, wayOSMElement.getID());
                    wayNodePreparedStmt.setObject(2, ref);
                    wayNodePreparedStmt.setObject(3, order);
                    wayNodePreparedStmt.addBatch();
                }
                wayNodePreparedStmt.executeBatch();
                
                //Update way geometries
                updateGeometryWayPreparedStmt.setObject(1, wayOSMElement.getID());
                updateGeometryWayPreparedStmt.setObject(2, wayOSMElement.getID());
                updateGeometryWayPreparedStmt.execute();
                

            } catch (SQLException ex) {
                throw new SAXException("Cannot insert the way  :  " + wayOSMElement.getID(), ex);
            }
        } else if (localName.compareToIgnoreCase("relation") == 0) {
            tagLocation = TAG_LOCATION.OTHER;
            try {
                relationPreparedStmt.setObject(1, relationOSMElement.getID());
                relationPreparedStmt.setObject(2, relationOSMElement.getUser());
                relationPreparedStmt.setObject(3, relationOSMElement.getUID());
                relationPreparedStmt.setObject(4, relationOSMElement.getVisible());
                relationPreparedStmt.setObject(5, relationOSMElement.getVersion());
                relationPreparedStmt.setObject(6, relationOSMElement.getChangeSet());
                relationPreparedStmt.setObject(7, relationOSMElement.getTimeStamp());
                relationPreparedStmt.execute();

                HashMap<String, String> tags = relationOSMElement.getTags();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    relationTagPreparedStmt.setObject(1, relationOSMElement.getID());
                    relationTagPreparedStmt.setObject(2, entry.getKey());
                    relationTagPreparedStmt.setObject(3, entry.getValue());
                    relationTagPreparedStmt.addBatch();
                }
                relationTagPreparedStmt.executeBatch();

                idMemberOrder = 0;

            } catch (SQLException ex) {
                throw new SAXException("Cannot insert the relation  :  " + relationOSMElement.getID(), ex);
            }
            try {
                nodeMemberPreparedStmt.executeBatch();
                wayMemberPreparedStmt.executeBatch();
                relationMemberPreparedStmt.executeBatch();
            } catch (BatchUpdateException ex) {
                //Do not catch the BatchUpdateException because the OSM file does not guarentee the
                //integrity of the data
                //eg : node ref cannot be stored in the current file. 
            } catch (SQLException ex) {
                throw new SAXException("Cannot insert the relation member :  " + relationOSMElement.getID(), ex);
            }

        } else if (localName.compareToIgnoreCase("member") == 0) {
            idMemberOrder++;
        }
    }

    /**
     *
     * @param osmElement
     * @param attributes
     * @throws ParseException
     */
    private void setCommonsAttributes(OSMElement osmElement, Attributes attributes) throws SAXException {
        osmElement.setId(attributes.getValue("id"));
        osmElement.setUser(attributes.getValue("user"));
        osmElement.setUid(attributes.getValue("uid"));
        osmElement.setVisible(attributes.getValue("visible"));
        osmElement.setVersion(attributes.getValue("version"));
        osmElement.setChangeset(attributes.getValue("changeset"));
        osmElement.setTimestamp(attributes.getValue("timestamp"));
    }

}
