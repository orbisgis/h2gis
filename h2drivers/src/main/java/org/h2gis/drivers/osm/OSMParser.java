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

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.h2.api.ErrorCode;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Parse an OSM file and store the elements into a database. The database model
 * contains 10 tables.
 *
 *
 * @author Erwan Bocher
 */
public class OSMParser extends DefaultHandler {

    //Suffix table names
    private static final String TAG = "_tag";
    private static final String NODE = "_node";
    private static final String WAY = "_way";
    private static final String NODE_TAG = "_node_tag";
    private static final String WAY_TAG = "_way_tag";
    private static final String WAY_NODE = "_way_node";
    private static final String RELATION = "_relation";
    private static final String RELATION_TAG = "_relation_tag";
    private static final String NODE_MEMBER = "_node_member";
    private static final String WAY_MEMBER = "_way_member";
    private static final String RELATION_MEMBER = "_relation_member";
    // Set the same batch size as OSMOSIS
    // 1000 lines is 73 kb for node insert
    private static final int BATCH_SIZE = 1000;
    private PreparedStatement nodePreparedStmt;
    private PreparedStatement nodeTagPreparedStmt;
    private PreparedStatement wayPreparedStmt;
    private PreparedStatement wayTagPreparedStmt;
    private PreparedStatement relationPreparedStmt;
    private PreparedStatement relationTagPreparedStmt;
    private PreparedStatement nodeMemberPreparedStmt;
    private PreparedStatement wayMemberPreparedStmt;
    private PreparedStatement relationMemberPreparedStmt;
    private PreparedStatement wayNodePreparedStmt;
    private int nodePreparedStmtBatchSize = 0;
    private int nodeTagPreparedStmtBatchSize = 0;
    private int wayPreparedStmtBatchSize = 0;
    private int wayTagPreparedStmtBatchSize = 0;
    private int relationPreparedStmtBatchSize = 0;
    private int relationTagPreparedStmtBatchSize = 0;
    private int nodeMemberPreparedStmtBatchSize = 0;
    private int wayMemberPreparedStmtBatchSize = 0;
    private int relationMemberPreparedStmtBatchSize = 0;
    private int wayNodePreparedStmtBatchSize = 0;
    private Set<String> insertedTagsKeys = new HashSet<String>();
    private int idMemberOrder = 1;
    private TAG_LOCATION tagLocation;
    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
    private NodeOSMElement nodeOSMElement;
    private WayOSMElement wayOSMElement;
    private OSMElement relationOSMElement;
    private ProgressVisitor progress = new EmptyProgressVisitor();
    private FileChannel fc;
    private long fileSize = 0;
    private long readFileSizeEachNode = 1;
    private long nodeCountProgress = 0;
    private PreparedStatement tagPreparedStmt;
    // For progression information return
    private static final int AVERAGE_NODE_SIZE = 500;
    private static String TAG_DUPLICATE_EXCEPTION = String.valueOf(ErrorCode.DUPLICATE_KEY_1);

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
        this.progress = progress.subProcess(100);
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
            this.fc = fs.getChannel();
            this.fileSize = fc.size();
            // Given the file size and an average node file size.
            // Skip how many nodes in order to update progression at a step of 1%
            readFileSizeEachNode = Math.max(1, (this.fileSize / AVERAGE_NODE_SIZE) / 100);
            nodeCountProgress = 0;
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setErrorHandler(this);
            parser.setContentHandler(this);
            if(inputFile.getName().endsWith(".osm")) {
                parser.parse(new InputSource(fs));
            } else if(inputFile.getName().endsWith(".osm.gz")) {
                parser.parse(new InputSource(new GZIPInputStream(fs)));
            } else if(inputFile.getName().endsWith(".osm.bz2")) {
                parser.parse(new InputSource(new BZip2CompressorInputStream(fs)));
            }
            else{
                throw new SQLException("Supported formats are .osm, .osm.gz, .osm.bz2");
            }
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
            if(tagPreparedStmt!=null){
                tagPreparedStmt.close();
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
        String[] omsTables = new String[]{TAG, NODE, NODE_TAG, WAY, WAY_NODE, WAY_TAG, RELATION, RELATION_TAG, NODE_MEMBER, WAY_MEMBER, RELATION_MEMBER};
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
        String tagTableName = caseIdentifier(requestedTable, osmTableName + TAG, isH2);
        tagPreparedStmt =  OSMTablesFactory.createTagTable(connection, tagTableName);
        String nodeTableName = caseIdentifier(requestedTable, osmTableName + NODE, isH2);
        nodePreparedStmt = OSMTablesFactory.createNodeTable(connection, nodeTableName, isH2);
        String nodeTagTableName = caseIdentifier(requestedTable, osmTableName + NODE_TAG, isH2);
        nodeTagPreparedStmt = OSMTablesFactory.createNodeTagTable(connection, nodeTagTableName, tagTableName);
        String wayTableName = caseIdentifier(requestedTable, osmTableName + WAY, isH2);
        wayPreparedStmt = OSMTablesFactory.createWayTable(connection, wayTableName, isH2);
        String wayTagTableName = caseIdentifier(requestedTable, osmTableName + WAY_TAG, isH2);
        wayTagPreparedStmt = OSMTablesFactory.createWayTagTable(connection, wayTagTableName, tagTableName);
        String wayNodeTableName = caseIdentifier(requestedTable, osmTableName + WAY_NODE, isH2);
        wayNodePreparedStmt = OSMTablesFactory.createWayNodeTable(connection, wayNodeTableName);
        String relationTableName = caseIdentifier(requestedTable, osmTableName + RELATION, isH2);
        relationPreparedStmt = OSMTablesFactory.createRelationTable(connection, relationTableName);
        String relationTagTableName = caseIdentifier(requestedTable, osmTableName + RELATION_TAG, isH2);
        relationTagPreparedStmt = OSMTablesFactory.createRelationTagTable(connection, relationTagTableName, tagTableName);
        String nodeMemberTableName = caseIdentifier(requestedTable, osmTableName + NODE_MEMBER, isH2);
        nodeMemberPreparedStmt = OSMTablesFactory.createNodeMemberTable(connection, nodeMemberTableName);
        String wayMemberTableName = caseIdentifier(requestedTable, osmTableName + WAY_MEMBER, isH2);
        wayMemberPreparedStmt = OSMTablesFactory.createWayMemberTable(connection, wayMemberTableName);
        String relationMemberTableName = caseIdentifier(requestedTable, osmTableName + RELATION_MEMBER, isH2);
        relationMemberPreparedStmt = OSMTablesFactory.createRelationMemberTable(connection, relationMemberTableName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String type = attributes.getValue("type");
        if(progress.isCanceled()) {
            throw new SAXException("Canceled by user");
        }
        if (localName.compareToIgnoreCase("node") == 0) {
            nodeOSMElement = new NodeOSMElement(Double.valueOf(attributes.getValue("lat")),Double.valueOf(attributes.getValue("lon")));
            setCommonsAttributes(nodeOSMElement, attributes);
            tagLocation = TAG_LOCATION.NODE;
        } else if (localName.compareToIgnoreCase("way") == 0) {
            wayOSMElement = new WayOSMElement();
            setCommonsAttributes(wayOSMElement, attributes);
            tagLocation = TAG_LOCATION.WAY;
        } else if (localName.compareToIgnoreCase("tag") == 0) {
            String key = attributes.getValue("k");
            String value = attributes.getValue("v");
            boolean insertTag = true;
            switch (tagLocation) {
                case NODE:
                    insertTag = nodeOSMElement.addTag(key, value);
                    break;
                case WAY:
                    insertTag = wayOSMElement.addTag(key, value);
                    break;
                case RELATION:
                    insertTag = relationOSMElement.addTag(key, value);
                    break;
            }
            try{
                if(insertTag && !insertedTagsKeys.contains(key)) {
                    tagPreparedStmt.setObject(1, key);
                    tagPreparedStmt.execute();
                    insertedTagsKeys.add(key);
                }
            } catch (SQLException ex) {
                    if(ex.getErrorCode() != ErrorCode.DUPLICATE_KEY_1 && !TAG_DUPLICATE_EXCEPTION.equals(ex.getSQLState())) {
                        throw new SAXException("Cannot insert the tag :  {" + key + " , " + value + "}", ex);
                    }
            }
        } else if (localName.compareToIgnoreCase("nd") == 0) {
            wayOSMElement.addRef(attributes.getValue("ref"));
        } else if (localName.compareToIgnoreCase("relation") == 0) {
            relationOSMElement = new OSMElement();
            setCommonsAttributes(relationOSMElement, attributes);
            tagLocation = TAG_LOCATION.RELATION;
        } else if (localName.compareToIgnoreCase("member") == 0) {
            if (type.equalsIgnoreCase("node")) {
                try {
                    nodeMemberPreparedStmt.setObject(1, relationOSMElement.getID());
                    nodeMemberPreparedStmt.setObject(2, Long.valueOf(attributes.getValue("ref")));
                    nodeMemberPreparedStmt.setObject(3, attributes.getValue("role"));
                    nodeMemberPreparedStmt.setObject(4, idMemberOrder);
                    nodeMemberPreparedStmt.addBatch();
                    nodeMemberPreparedStmtBatchSize++;
                } catch (SQLException ex) {
                    throw new SAXException("Cannot insert the node member for the relation :  " + relationOSMElement.getID(), ex);
                }
            } else if (type.equalsIgnoreCase("way")) {
                try {
                    wayMemberPreparedStmt.setObject(1, relationOSMElement.getID());
                    wayMemberPreparedStmt.setObject(2, Long.valueOf(attributes.getValue("ref")));
                    wayMemberPreparedStmt.setObject(3, attributes.getValue("role"));
                    wayMemberPreparedStmt.setObject(4, idMemberOrder);
                    wayMemberPreparedStmt.addBatch();
                    wayMemberPreparedStmtBatchSize++;
                } catch (SQLException ex) {
                    throw new SAXException("Cannot insert the way member for the relation :  " + relationOSMElement.getID(), ex);
                }
            } else if (type.equalsIgnoreCase("relation")) {
                try {
                    relationMemberPreparedStmt.setObject(1, relationOSMElement.getID());
                    relationMemberPreparedStmt.setObject(2, Long.valueOf(attributes.getValue("ref")));
                    relationMemberPreparedStmt.setObject(3, attributes.getValue("role"));
                    relationMemberPreparedStmt.setObject(4, idMemberOrder);
                    relationMemberPreparedStmt.addBatch();
                    relationMemberPreparedStmtBatchSize++;
                } catch (SQLException ex) {
                    throw new SAXException("Cannot insert the relation member for the relation :  " + relationOSMElement.getID(), ex);
                }
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Execute remaining batch
        try {
            nodePreparedStmtBatchSize = insertBatch(nodePreparedStmt, nodePreparedStmtBatchSize, 1);
            nodeTagPreparedStmtBatchSize = insertBatch(nodeTagPreparedStmt, nodeTagPreparedStmtBatchSize, 1);
            wayPreparedStmtBatchSize = insertBatch(wayPreparedStmt, wayPreparedStmtBatchSize, 1);
            wayTagPreparedStmtBatchSize = insertBatch(wayTagPreparedStmt, wayTagPreparedStmtBatchSize, 1);
            relationPreparedStmtBatchSize = insertBatch(relationPreparedStmt, relationPreparedStmtBatchSize, 1);
            relationTagPreparedStmtBatchSize = insertBatch(relationTagPreparedStmt, relationTagPreparedStmtBatchSize, 1);
            nodeMemberPreparedStmtBatchSize = insertBatch(nodeMemberPreparedStmt,nodeMemberPreparedStmtBatchSize, 1);
            wayMemberPreparedStmtBatchSize = insertBatch(wayMemberPreparedStmt, wayMemberPreparedStmtBatchSize, 1);
            relationMemberPreparedStmtBatchSize = insertBatch(relationMemberPreparedStmt, relationMemberPreparedStmtBatchSize, 1);
            wayNodePreparedStmtBatchSize = insertBatch(wayNodePreparedStmt, wayNodePreparedStmtBatchSize, 1);
        } catch (SQLException ex) {
            throw new SAXException("Could not insert sql batch", ex);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.compareToIgnoreCase("node") == 0) {
            tagLocation = TAG_LOCATION.OTHER;
            try {
                nodePreparedStmt.setObject(1, nodeOSMElement.getID());
                nodePreparedStmt.setObject(2, nodeOSMElement.getPoint(gf));
                nodePreparedStmt.setObject(3, nodeOSMElement.getElevation());
                nodePreparedStmt.setObject(4, nodeOSMElement.getUser());
                nodePreparedStmt.setObject(5, nodeOSMElement.getUID());
                nodePreparedStmt.setObject(6, nodeOSMElement.getVisible());
                nodePreparedStmt.setObject(7, nodeOSMElement.getVersion());
                nodePreparedStmt.setObject(8, nodeOSMElement.getChangeSet());
                nodePreparedStmt.setObject(9, nodeOSMElement.getTimeStamp(), Types.DATE);
                nodePreparedStmt.setString(10, nodeOSMElement.getName());
                nodePreparedStmt.addBatch();
                nodePreparedStmtBatchSize++;
                HashMap<String, String> tags = nodeOSMElement.getTags();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    nodeTagPreparedStmt.setObject(1, nodeOSMElement.getID());
                    nodeTagPreparedStmt.setObject(2, entry.getKey());
                    nodeTagPreparedStmt.setObject(3, entry.getValue());
                    nodeTagPreparedStmt.addBatch();
                    nodeTagPreparedStmtBatchSize++;
                }
            } catch (SQLException ex) {
                throw new SAXException("Cannot insert the node  :  " + nodeOSMElement.getID(), ex);
            }
        } else if (localName.compareToIgnoreCase("way") == 0) {
            tagLocation = TAG_LOCATION.OTHER;
            try {
                wayPreparedStmt.setObject(1, wayOSMElement.getID());
                wayPreparedStmt.setObject(2, wayOSMElement.getUser());
                wayPreparedStmt.setObject(3, wayOSMElement.getUID());
                wayPreparedStmt.setObject(4, wayOSMElement.getVisible());
                wayPreparedStmt.setObject(5, wayOSMElement.getVersion());
                wayPreparedStmt.setObject(6, wayOSMElement.getChangeSet());
                wayPreparedStmt.setTimestamp(7, wayOSMElement.getTimeStamp());
                wayPreparedStmt.setString(8, wayOSMElement.getName());
                wayPreparedStmt.addBatch();
                wayPreparedStmtBatchSize++;
                HashMap<String, String> tags = wayOSMElement.getTags();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    wayTagPreparedStmt.setObject(1, wayOSMElement.getID());
                    wayTagPreparedStmt.setObject(2, entry.getKey());
                    wayTagPreparedStmt.setObject(3, entry.getValue());
                    wayTagPreparedStmt.addBatch();
                    wayTagPreparedStmtBatchSize++;
                }
                int order = 1;
                for (long ref :  wayOSMElement.getNodesRef()) {
                    wayNodePreparedStmt.setObject(1, wayOSMElement.getID());
                    wayNodePreparedStmt.setObject(2, ref);
                    wayNodePreparedStmt.setObject(3, order++);
                    wayNodePreparedStmt.addBatch();
                    wayNodePreparedStmtBatchSize++;
                }
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
                relationPreparedStmt.setTimestamp(7, relationOSMElement.getTimeStamp());
                relationPreparedStmt.addBatch();
                relationPreparedStmtBatchSize++;
                HashMap<String, String> tags = relationOSMElement.getTags();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    relationTagPreparedStmt.setObject(1, relationOSMElement.getID());
                    relationTagPreparedStmt.setObject(2, entry.getKey());
                    relationTagPreparedStmt.setObject(3, entry.getValue());
                    relationTagPreparedStmt.addBatch();
                    relationTagPreparedStmtBatchSize++;
                }
                idMemberOrder = 0;
            } catch (SQLException ex) {
                throw new SAXException("Cannot insert the relation  :  " + relationOSMElement.getID(), ex);
            }
        } else if (localName.compareToIgnoreCase("member") == 0) {
            idMemberOrder++;
        }
        try {
            insertBatch();
        } catch (SQLException ex) {
            throw new SAXException("Could not insert sql batch", ex);
        }
        if(nodeCountProgress++ % readFileSizeEachNode == 0) {
            // Update Progress
            try {
                progress.setStep((int) (((double) fc.position() / fileSize) * 100));
            } catch (IOException ex) {
                // Ignore
            }
        }
    }

    private void insertBatch() throws SQLException {
        nodePreparedStmtBatchSize = insertBatch(nodePreparedStmt, nodePreparedStmtBatchSize);
        nodeTagPreparedStmtBatchSize = insertBatch(nodeTagPreparedStmt, nodeTagPreparedStmtBatchSize);
        wayPreparedStmtBatchSize = insertBatch(wayPreparedStmt, wayPreparedStmtBatchSize);
        wayTagPreparedStmtBatchSize = insertBatch(wayTagPreparedStmt, wayTagPreparedStmtBatchSize);
        relationPreparedStmtBatchSize = insertBatch(relationPreparedStmt, relationPreparedStmtBatchSize);
        relationTagPreparedStmtBatchSize = insertBatch(relationTagPreparedStmt, relationTagPreparedStmtBatchSize);
        nodeMemberPreparedStmtBatchSize = insertBatch(nodeMemberPreparedStmt,nodeMemberPreparedStmtBatchSize);
        wayMemberPreparedStmtBatchSize = insertBatch(wayMemberPreparedStmt, wayMemberPreparedStmtBatchSize);
        relationMemberPreparedStmtBatchSize = insertBatch(relationMemberPreparedStmt, relationMemberPreparedStmtBatchSize);
        wayNodePreparedStmtBatchSize = insertBatch(wayNodePreparedStmt, wayNodePreparedStmtBatchSize);
    }
    private int insertBatch(PreparedStatement st, int batchSize, int maxBatchSize) throws SQLException {
        if(batchSize >= maxBatchSize) {
            st.executeBatch();
            return 0;
        } else {
            return batchSize;
        }
    }

    private int insertBatch(PreparedStatement st, int batchSize) throws SQLException {
        return insertBatch(st, batchSize, BATCH_SIZE);
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
