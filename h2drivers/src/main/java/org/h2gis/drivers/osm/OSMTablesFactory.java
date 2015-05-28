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
 * (11) table_prefix + _relation_member : table that stores all relations that are referenced into a relation.
 * 
 * @author Erwan Bocher
 */
public class OSMTablesFactory {

    

    private OSMTablesFactory() {

    }
    
    
    /**
     * Create the tag table to store all key and value
     * @param connection
     * @param tagTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTagTable(Connection connection, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        // PostgreSQL and H2 will automatically create an index on TAG_KEY,TAG_VALUE when UNIQUE constraint is set
        stmt.execute("CREATE TABLE " + tagTableName + "(ID_TAG SERIAL PRIMARY KEY, TAG_KEY VARCHAR UNIQUE);");
        stmt.close();
        //We return the prepared statement of the tag table
        return connection.prepareStatement("INSERT INTO " + tagTableName + " (TAG_KEY) VALUES (?)");
    }

    /**
     * Create the nodes table that will be used to import OSM nodes 
     * Example :
     * <node id="298884269" lat="54.0901746" lon="12.2482632" user="SvenHRO"
     * uid="46882" visible="true" version="1" changeset="676636"
     * timestamp="2008-09-21T21:37:45Z"/>
     *
     * @param connection
     * @param nodeTableName
     * @param isH2
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeTable(Connection connection, String nodeTableName, boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeTableName);
        sb.append("(ID_NODE BIGINT PRIMARY KEY,  THE_GEOM ");
        if(isH2) {
            sb.append("POINT CHECK ST_SRID(THE_GEOM)=4326");
        } else {
            sb.append("GEOMETRY(POINT, 4326)");
        }
        sb.append(",ELE DOUBLE PRECISION,"
                + "USER_NAME VARCHAR,"
                + "UID BIGINT,"
                + "VISIBLE BOOLEAN,"
                + "VERSION INTEGER,"
                + "CHANGESET INTEGER,"
                + "LAST_UPDATE TIMESTAMP,"
                + "NAME VARCHAR);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + nodeTableName + " VALUES (?,?,?,?,?,?,?,?,?,?);");
    }
    

    /**
     * Create a table to store the node tags.
     *
     * @param connection
     * @param nodeTagTableName
     * @param tagTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeTagTable(Connection connection, String nodeTagTableName, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeTagTableName);
        sb.append("(ID_NODE BIGINT, ID_TAG BIGINT,TAG_VALUE VARCHAR); ");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the tag table
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(nodeTagTableName);
        insert.append("VALUES ( ?, ");
        insert.append("(SELECT ID_TAG FROM ").append(tagTableName).append(" WHERE TAG_KEY = ? LIMIT 1)");
        insert.append(", ?);");
        return connection.prepareStatement(insert.toString());
    }

    /**
     * Create the ways table that will be used to import OSM ways 
     * Example :
     * <way id="26659127" user="Masch" uid="55988" visible="true" version="5"
     * changeset="4142606" timestamp="2010-03-16T11:47:08Z">
     *
     * @param connection
     * @param wayTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayTable(Connection connection, String wayTableName, boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayTableName);
        sb.append("(ID_WAY BIGINT PRIMARY KEY, USER_NAME VARCHAR, UID BIGINT, VISIBLE BOOLEAN, VERSION INTEGER, CHANGESET INTEGER, LAST_UPDATE TIMESTAMP, NAME VARCHAR);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + wayTableName + " VALUES (?,?,?,?,?,?,?,?);");
    }

    /**
     * Create a table to store the way tags.
     *
     * @param connection
     * @param wayTagTableName
     * @param tagTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayTagTable(Connection connection, String wayTagTableName, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayTagTableName);
        sb.append("(ID_WAY BIGINT, ID_TAG BIGINT, VALUE VARCHAR);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the way tag table
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(wayTagTableName);
        insert.append("VALUES ( ?, ");
        insert.append("(SELECT ID_TAG FROM ").append(tagTableName).append(" WHERE TAG_KEY = ? LIMIT 1)");
        insert.append(", ?);");
        return connection.prepareStatement(insert.toString());
    }

    /**
     * Create a table to store the list of nodes for each way.
     *
     * @param connection
     * @param wayNodeTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayNodeTable(Connection connection, String wayNodeTableName) throws SQLException{
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayNodeTableName);
        sb.append("(ID_WAY BIGINT, ID_NODE BIGINT, NODE_ORDER INT);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + wayNodeTableName + " VALUES ( ?, ?,?);");
    }

    /**
     * Create the relation table.
     *
     * @param connection
     * @param relationTable
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRelationTable(Connection connection, String relationTable) throws SQLException {
        Statement stmt = connection.createStatement();
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
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + relationTable + " VALUES ( ?,?,?,?,?,?,?);");
    }

    /**
     * Create the relation tags table
     *
     * @param connection
     * @param tagTableName
     * @param relationTagTable
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRelationTagTable(Connection connection, String relationTagTable, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(relationTagTable);
        sb.append("(ID_RELATION BIGINT, ID_TAG BIGINT, TAG_VALUE VARCHAR);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the way tag table
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(relationTagTable);
        insert.append("VALUES ( ?, ");
        insert.append("(SELECT ID_TAG FROM ").append(tagTableName).append(" WHERE TAG_KEY = ? LIMIT 1)");
        insert.append(", ?);");
        return connection.prepareStatement(insert.toString());
    }

    /**
     * Create the node members table
     *
     * @param connection
     * @param nodeMemberTable
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeMemberTable(Connection connection, String nodeMemberTable) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeMemberTable);
        sb.append("(ID_RELATION BIGINT,ID_NODE BIGINT, ROLE VARCHAR, NODE_ORDER INT);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + nodeMemberTable + " VALUES ( ?,?,?,?);");
    }

    /**
     * Create a table to store all way members.
     *
     * @param connection
     * @param wayMemberTable
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayMemberTable(Connection connection, String wayMemberTable) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayMemberTable);
        sb.append("(ID_RELATION BIGINT, ID_WAY BIGINT, ROLE VARCHAR, WAY_ORDER INT);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + wayMemberTable + " VALUES ( ?,?,?,?);");
    }

    /**
     * Store all relation members
     *
     * @param connection
     * @param relationMemberTable
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRelationMemberTable(Connection connection, String relationMemberTable) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(relationMemberTable);
        sb.append("(ID_RELATION BIGINT, ID_SUB_RELATION BIGINT, ROLE VARCHAR, RELATION_ORDER INT);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + relationMemberTable + " VALUES ( ?,?,?,?);");
    }
}
