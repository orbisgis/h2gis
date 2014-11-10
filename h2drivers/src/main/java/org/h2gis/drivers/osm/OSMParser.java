/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.h2gis.drivers.osm;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Erwan Bocher
 */
public class OSMParser extends DefaultHandler{
    
    
    //Suffix table names
    String NODE = "_node";
    String WAY = "_way";
    String NODE_TAG = "_node_tag";
    String WAY_TAG = "_way_tag";
    String WAY_NODE = "_way_node";
    String TAG = "_tag";
    String RELATION = "_relation";
    String RELATION_TAG = "_relation_tag";
    String NODE_MEMBER = "_node_member";
    String WAY_MEMBER = "_way_member";
    private PreparedStatement nodePreparedStmt;
    private PreparedStatement nodeTagPreparedStmt;
    private PreparedStatement wayPreparedStmt;
    private PreparedStatement wayTagPreparedStmt;
    private PreparedStatement tagPreparedStmt;
    private PreparedStatement relationPreparedStmt;
    private PreparedStatement relationTagPreparedStmt;
    private PreparedStatement nodeMemberPreparedStmt;
    private PreparedStatement wayMemberPreparedStmt;
    private  long nodeId =1;
    private  long wayId =1;
    private long relationId=1;
    private long tagId=0;
    private TAG_LOCATION tagLocation;
    private GeometryFactory gf = new GeometryFactory();
    private NodeOSMElement nodeOSMElement;
    private WayOSMElement wayOSMElement;
    private PreparedStatement wayNodePreparedStmt;
    
    public OSMParser(){
        
    }
    
    /**
     * 
     * @param inputFile
     * @param tableName
     * @param connection
     * @return
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws SAXException
     * @throws IOException 
     */
    public boolean read(File inputFile, String tableName, Connection connection) throws SQLException, FileNotFoundException, SAXException, IOException {
        // Initialisation
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        boolean success = false;
        TableLocation requestedTable = TableLocation.parse(tableName, isH2);
        String osmTableName = requestedTable.getTable();        
        checkOSMTables(connection, isH2, requestedTable, osmTableName); 
        createOSMDatabaseModel(connection,  isH2,  requestedTable,osmTableName);
        
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
            // When the reading ends, close() method has to be called
            if (nodePreparedStmt!= null) {
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
            if (relationTagPreparedStmt!= null) {
                relationTagPreparedStmt.close();
            }
            if (nodeMemberPreparedStmt != null) {
                nodeMemberPreparedStmt.close();
            }
            if (wayMemberPreparedStmt != null) {
                wayMemberPreparedStmt.close();
            }
            if (tagPreparedStmt != null) {
                tagPreparedStmt.close();
            }
        }        
        System.out.println("Node : "+ nodeId + " Way : "+ wayId + " Relation : "+ relationId + " Nombre de tag "+ tagId);
        
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
        String[] omsTables = new String[]{NODE, NODE_TAG, WAY,WAY_NODE, WAY_TAG, TAG, RELATION, RELATION_TAG, NODE_MEMBER, WAY_MEMBER};
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
     * Create the OMS datamodel to store the content of the file
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
        String tagTableName = caseIdentifier(requestedTable, osmTableName + TAG, isH2);
        tagPreparedStmt = OSMTablesFactory.createTagTable(connection, tagTableName);
        String nodeTagTableName = caseIdentifier(requestedTable, osmTableName + NODE_TAG, isH2);
        nodeTagPreparedStmt = OSMTablesFactory.createNodeTagTable(connection, nodeTagTableName, nodeTableName, tagTableName);
        String wayTableName = caseIdentifier(requestedTable, osmTableName + WAY, isH2);
        wayPreparedStmt = OSMTablesFactory.createWayTable(connection,wayTableName );
        String wayTagTableName = caseIdentifier(requestedTable, osmTableName + WAY_TAG, isH2);
        wayTagPreparedStmt = OSMTablesFactory.createWayTagTable(connection, wayTagTableName, wayTableName, tagTableName);
        String wayNodeTableName = caseIdentifier(requestedTable, osmTableName + WAY_NODE, isH2);
        wayNodePreparedStmt = OSMTablesFactory.createWayNodeTable(connection, wayNodeTableName, nodeTableName, wayTableName);       
        String relationTableName = caseIdentifier(requestedTable, osmTableName + RELATION, isH2);
        relationPreparedStmt = OSMTablesFactory.createRelationTable(connection, relationTableName);
        String relationTagTableName = caseIdentifier(requestedTable, osmTableName + RELATION_TAG, isH2);
        relationTagPreparedStmt = OSMTablesFactory.createRelationTagTable(connection, relationTableName, relationTagTableName, tagTableName);
        String nodeMemberTableName = caseIdentifier(requestedTable, osmTableName + NODE_MEMBER, isH2);
        nodeMemberPreparedStmt = OSMTablesFactory.createNodeMemberTable(connection, nodeMemberTableName, relationTableName, nodeTableName);
        String wayMemberTableName = caseIdentifier(requestedTable, osmTableName + WAY_MEMBER, isH2);
        wayMemberPreparedStmt =  OSMTablesFactory.createWayMemberTable(connection, wayMemberTableName, relationTableName, wayTableName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        if(localName.compareToIgnoreCase("osm")==0){
            String version = attributes.getValue("version");
            System.out.println("OSM "+ version);
        }
        else if(localName.compareToIgnoreCase("node")==0){
            nodeOSMElement = new NodeOSMElement();            
            setCommonsAttributes(nodeOSMElement, attributes);
            nodeOSMElement.createPoint(gf, attributes.getValue("lon"), attributes.getValue("lat"));
            tagLocation = TAG_LOCATION.NODE;
            //System.out.println(nodeId+ " Node "+ nodeOSMElement.getID()+ " : "+ nodeOSMElement.getPoint());
        }
        else if(localName.compareToIgnoreCase("way")==0){
            wayOSMElement = new WayOSMElement();
            setCommonsAttributes(wayOSMElement, attributes);
            tagLocation = TAG_LOCATION.WAY;
            
        }        
        else if(localName.compareToIgnoreCase("tag")==0){
            String key = attributes.getValue("k");
            String value = attributes.getValue("v");
            try {
                tagPreparedStmt.setString(1, key);
                tagPreparedStmt.setString(2, value);
                tagPreparedStmt.setString(3, key);
                tagPreparedStmt.setString(4, value);
                tagPreparedStmt.execute();                
            } catch (SQLException ex) {
                throw new SAXException("Cannot set the tag value :  " + key + "=" + value, ex);
            }            
            if (tagLocation == TAG_LOCATION.NODE) {
                nodeOSMElement.addTag(key, value);
            } else if (tagLocation == TAG_LOCATION.WAY) {
                wayOSMElement.addTag(key, value);
            } else if (tagLocation == TAG_LOCATION.RELATION) {
                //System.out.println(" Relation tag  : "+ key + " ,  "+ value);
            }       
        }
        else if(localName.compareToIgnoreCase("nd")==0){
             wayOSMElement.addRef(attributes.getValue("ref"));
        }        
        else if(localName.compareToIgnoreCase("relation")==0){
            tagLocation = TAG_LOCATION.RELATION;
            String id = attributes.getValue("id");
            //System.out.println(" Relation id  : "+ id);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.compareToIgnoreCase("node") == 0) {
            nodeId++;
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
            
        }
        else if(localName.compareToIgnoreCase("way")==0){
            wayId++;
            tagLocation = TAG_LOCATION.OTHER;
            try{
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
                
             } catch (SQLException ex) {
                throw new SAXException("Cannot insert the way  :  " + wayOSMElement.getID(), ex);
            }
        }
        else if(localName.compareToIgnoreCase("relation")==0){
            relationId++;
            tagLocation = TAG_LOCATION.OTHER;
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
