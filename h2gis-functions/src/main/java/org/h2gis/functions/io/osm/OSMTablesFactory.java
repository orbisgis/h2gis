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

package org.h2gis.functions.io.osm;

import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to create the tables to import osm data
 * 
 * An OSM file is stored in 10 tables. 
 * 
 * (1) table_prefix + _node :  table that contains all nodes,
 * (2) table_prefix + _node_tag : table that contains a list of tags (key, value) for each node,
 * (3) table_prefix + _way : table that contains all ways with their geometries,
 * (4) table_prefix + _way_tag : table that contains a list of tags (key, value) for each way,
 * (5) table_prefix + _way_node : table that contains the list of nodes used to represent a way,
 * (6) table_prefix + _relation: table that contains all relations,
 * (7) table_prefix + _relation_tag : table that contains a list of tags (key, value) for each relation,
 * (8) table_prefix + _node_member : table that stores all nodes that are referenced into a relation,
 * (9) table_prefix + _way_member : table that stores all ways that are referenced into a relation,
 * (10) table_prefix + _relation_member : table that stores all relations that are referenced into a relation.
 * 
 * @author Erwan Bocher
 */
public class OSMTablesFactory {

    
    //Suffix table names
    public static final String NODE = "_node";
    public static final String WAY = "_way";
    public static final String NODE_TAG = "_node_tag";
    public static final String WAY_TAG = "_way_tag";
    public static final String WAY_NODE = "_way_node";
    public static final String RELATION = "_relation";
    public static final String RELATION_TAG = "_relation_tag";
    public static final String NODE_MEMBER = "_node_member";
    public static final String WAY_MEMBER = "_way_member";
    public static final String RELATION_MEMBER = "_relation_member";    
   

    private OSMTablesFactory() {

    }    
    
    
    /**
     * Create the nodes table that will be used to import OSM nodes 
     * Example :
     * {@code
     * <node id="298884269" lat="54.0901746" lon="12.2482632" user="SvenHRO"
     * uid="46882" visible="true" version="1" changeset="676636"
     * timestamp="2008-09-21T21:37:45Z"/>
     * }
     *
     * @param connection database
     * @param nodeTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createNodeTable(Connection connection, String nodeTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(nodeTableName);
            sb.append("(ID_NODE BIGINT PRIMARY KEY,  THE_GEOM ");
            sb.append("GEOMETRY(POINT, 4326)");
            sb.append(",ELE DOUBLE PRECISION,"
                    + "USER_NAME VARCHAR,"
                    + "UID BIGINT,"
                    + "VISIBLE BOOLEAN,"
                    + "VERSION INTEGER,"
                    + "CHANGESET INTEGER,"
                    + "LAST_UPDATE TIMESTAMP,"
                    + "NAME VARCHAR);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + nodeTableName + " VALUES (?,?,?,?,?,?,?,?,?,?);");
    }
    

    /**
     * Create a table to store the node tags.
     *
     * @param connection database
     * @param nodeTagTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createNodeTagTable(Connection connection, String nodeTagTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(nodeTagTableName);
            sb.append("(ID_NODE BIGINT, TAG_KEY VARCHAR,TAG_VALUE VARCHAR); ");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + nodeTagTableName + " VALUES ( ?, ?,?);");
    }

    /**
     * Create the ways table that will be used to import OSM ways 
     * Example :
     * {@code
     * <way id="26659127" user="Masch" uid="55988" visible="true" version="5"
     * changeset="4142606" timestamp="2010-03-16T11:47:08Z">
     * }
     *
     * @param connection database
     * @param wayTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createWayTable(Connection connection, String wayTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(wayTableName);
            sb.append("(ID_WAY BIGINT PRIMARY KEY, USER_NAME VARCHAR, UID BIGINT, VISIBLE BOOLEAN, VERSION INTEGER, CHANGESET INTEGER, LAST_UPDATE TIMESTAMP, NAME VARCHAR);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + wayTableName + " VALUES (?,?,?,?,?,?,?,?);");
    }

    /**
     * Create a table to store the way tags.
     *
     * @param connection database
     * @param wayTagTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createWayTagTable(Connection connection, String wayTagTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(wayTagTableName);
            sb.append("(ID_WAY BIGINT, TAG_KEY VARCHAR,TAG_VALUE VARCHAR);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + wayTagTableName + " VALUES ( ?, ?,?);");
    }

    /**
     * Create a table to store the list of nodes for each way.
     *
     * @param connection database
     * @param wayNodeTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createWayNodeTable(Connection connection, String wayNodeTableName) throws SQLException{
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(wayNodeTableName);
            sb.append("(ID_WAY BIGINT, ID_NODE BIGINT, NODE_ORDER INT);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + wayNodeTableName + " VALUES ( ?, ?,?);");
    }

    /**
     * Create the relation table.
     *
     * @param connection database
     * @param relationTable table name
     * @return PreparedStatement
     */
    public static PreparedStatement createRelationTable(Connection connection, String relationTable) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(relationTable);
            sb.append("(ID_RELATION BIGINT PRIMARY KEY,"
                    + "USER_NAME VARCHAR,"
                    + "UID BIGINT,"
                    + "VISIBLE BOOLEAN,"
                    + "VERSION INTEGER,"
                    + "CHANGESET INTEGER,"
                    + "LAST_UPDATE TIMESTAMP);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + relationTable + " VALUES ( ?,?,?,?,?,?,?);");
    }

    /**
     * Create the relation tags table
     *
     * @param connection database
     * @param relationTagTable table name
     * @return PreparedStatement
     */
    public static PreparedStatement createRelationTagTable(Connection connection, String relationTagTable) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(relationTagTable);
            sb.append("(ID_RELATION BIGINT, TAG_KEY VARCHAR,TAG_VALUE VARCHAR);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + relationTagTable + " VALUES ( ?, ?,?);");
    }

    /**
     * Create the node members table
     *
     * @param connection database
     * @param nodeMemberTable table name
     * @return PreparedStatement
     */
    public static PreparedStatement createNodeMemberTable(Connection connection, String nodeMemberTable) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(nodeMemberTable);
            sb.append("(ID_RELATION BIGINT,ID_NODE BIGINT, ROLE VARCHAR, NODE_ORDER INT);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + nodeMemberTable + " VALUES ( ?,?,?,?);");
    }

    /**
     * Create a table to store all way members.
     *
     * @param connection database
     * @param wayMemberTable table name
     * @return PreparedStatement
     */
    public static PreparedStatement createWayMemberTable(Connection connection, String wayMemberTable) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(wayMemberTable);
            sb.append("(ID_RELATION BIGINT, ID_WAY BIGINT, ROLE VARCHAR, WAY_ORDER INT);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + wayMemberTable + " VALUES ( ?,?,?,?);");
    }

    /**
     * Store all relation members
     *
     * @param connection database
     * @param relationMemberTable table name
     * @return PreparedStatement
     */
    public static PreparedStatement createRelationMemberTable(Connection connection, String relationMemberTable) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(relationMemberTable);
            sb.append("(ID_RELATION BIGINT, ID_SUB_RELATION BIGINT, ROLE VARCHAR, RELATION_ORDER INT);");
            stmt.execute(sb.toString());
        }
        return connection.prepareStatement("INSERT INTO " + relationMemberTable + " VALUES ( ?,?,?,?);");
    }
    
    
    /**
     * Drop the existing OSM tables used to store the imported OSM data 
     *
     * @param connection database
     * @param tablePrefix table prefix
     */
    public static void dropOSMTables(Connection connection, String tablePrefix) throws SQLException {
        final DBTypes dbType = DBUtils.getDBType(connection);
        TableLocation requestedTable = TableLocation.parse(tablePrefix, dbType);
        String osmTableName = requestedTable.getTable();        
        String[] omsTables = new String[]{NODE, NODE_TAG, WAY, WAY_NODE, WAY_TAG, RELATION, RELATION_TAG, NODE_MEMBER, WAY_MEMBER, RELATION_MEMBER};
        StringBuilder sb =  new StringBuilder("drop table if exists ");     
        String omsTableSuffix = omsTables[0];
        String osmTable = TableUtilities.caseIdentifier(requestedTable, osmTableName + omsTableSuffix, dbType);
        sb.append(osmTable);
        for (int i = 1; i < omsTables.length; i++) {
            omsTableSuffix = omsTables[i];
            osmTable = TableUtilities.caseIdentifier(requestedTable, osmTableName + omsTableSuffix, dbType);
            sb.append(",").append(osmTable);
        }        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sb.toString());
        }
    }
}
