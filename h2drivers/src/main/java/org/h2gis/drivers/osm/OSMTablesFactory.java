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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to create the tables to import osm data
 * 
 * @author Erwan Bocher
 */
public class OSMTablesFactory {
 
    private OSMTablesFactory(){
        
    }
    
    /**
     * Create the nodes table that will be used to import OSM nodes Example :
     * <node id="298884269" lat="54.0901746" lon="12.2482632" user="SvenHRO"
     * uid="46882" visible="true" version="1" changeset="676636"
     * timestamp="2008-09-21T21:37:45Z"/>
     *
     * @param connection
     * @param nodeTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeTable(Connection connection, String nodeTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeTableName);
        sb.append("(ID_NODE BIGINT PRIMARY KEY,  THE_GEOM POINT,"
                + "USER_NAME VARCHAR,"
                + "UID BIGINT,"
                + "VISIBLE BOOLEAN,"
                + "VERSION INTEGER,"
                + "CHANGESET INTEGER,"
                + "TIMESTAMP TIMESTAMP);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + nodeTableName + " VALUES ( ?, ?, ?,?,?,?,?,?);");
    }

    /**
     *
     * @param connection
     * @param tagTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTagTable(Connection connection, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tagTableName);
        sb.append("(ID_TAG SERIAL, TAG_KEY VARCHAR, TAG_VALUE VARCHAR);");        
        stmt.execute(sb.toString());
        //Create index
        stmt.execute("CREATE INDEX ON "+ tagTableName+"(TAG_KEY, TAG_VALUE);");
        stmt.close();
        
        //We return the preparedstatement of the tag table
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(tagTableName);
        insert.append("(TAG_KEY, TAG_VALUE) SELECT ?, ? WHERE NOT EXISTS(SELECT * FROM ");
        insert.append(tagTableName);
        insert.append(" WHERE TAG_KEY=? AND TAG_VALUE=?);");        
        return connection.prepareStatement(insert.toString());
    }

    /**
     *
     * @param connection
     * @param nodeTagTableName
     * @param nodeTableName
     * @param tagTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeTagTable(Connection connection, String nodeTagTableName, String nodeTableName, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeTagTableName);
        sb.append("(ID_NODE BIGINT, ID_TAG BIGINT, PRIMARY KEY(ID_NODE, ID_TAG), FOREIGN KEY(ID_NODE) REFERENCES ");
        sb.append(nodeTableName);
        sb.append(", FOREIGN KEY(ID_TAG) REFERENCES ");
        sb.append(tagTableName).append(");");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the tag table
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(nodeTagTableName);
        insert.append(" VALUES ( ?, ");
        insert.append("(SELECT ID_TAG FROM ").append(tagTableName).append(" WHERE TAG_KEY = ? AND TAG_VALUE = ? LIMIT 1)");
        insert.append(");");        
        return connection.prepareStatement( insert.toString());
    }
    

    /**
     * Create the ways table that will be used to import OSM ways Example :
     * <way id="26659127" user="Masch" uid="55988" visible="true" version="5"
     * changeset="4142606" timestamp="2010-03-16T11:47:08Z">
     *
     * @param connection
     * @param wayTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayTable(Connection connection, String wayTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayTableName);
        sb.append("(ID_WAY BIGINT PRIMARY KEY,"
                + "THE_GEOM LINESTRING,"
                + "USER_NAME VARCHAR,"
                + "UID BIGINT,"
                + "VISIBLE BOOLEAN,"
                + "VERSION INTEGER,"
                + "CHANGESET INTEGER,"
                + "LAST_UPDATE TIMESTAMP);");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + wayTableName + " VALUES ( ?, ?,?,?,?,?,?,?);");
    }
    
    /**
     * 
     * @param connection
     * @param wayTagTableName
     * @param wayTableName
     * @param tagTableName
     * @return
     * @throws SQLException 
     */
     public static PreparedStatement createWayTagTable(Connection connection, String wayTagTableName, String wayTableName, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayTagTableName);
        sb.append("(ID_WAY BIGINT, ID_TAG BIGINT, PRIMARY KEY(ID_WAY, ID_TAG), FOREIGN KEY(ID_WAY) REFERENCES ");
        sb.append(wayTableName);
        sb.append(", FOREIGN KEY(ID_TAG) REFERENCES ");
        sb.append(tagTableName).append(");");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the tag table
        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(wayTagTableName);
        insert.append(" VALUES ( ?, ");
        insert.append("(SELECT ID_TAG FROM ").append(tagTableName).append(" WHERE TAG_KEY = ? AND TAG_VALUE = ? LIMIT 1)");
        insert.append(");");        
        return connection.prepareStatement( insert.toString());
    }

    /**
     *
     * @param connection
     * @param nodeWayTableName
     * @param nodeTableName
     * @param wayTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeWayTable(Connection connection, String nodeWayTableName, String nodeTableName, String wayTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeWayTableName);
        sb.append("(ID_WAY BIGINT, ID_NODE BIGINT, NODE_ORDER INT , PRIMARY KEY(ID_WAY,ID_NODE,NODE_ORDER), FOREIGN KEY(ID_NODE) REFERENCES ");
        sb.append(nodeTableName);
        sb.append(", FOREIGN KEY(ID_WAY) REFERENCES ");
        sb.append(wayTableName).append(");");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + wayTableName + " VALUES ( ?, ?,?);");
    }

    /**
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
     *
     * @param connection
     * @param relationTable
     * @param relationTagTable
     * @param tagTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRelationTagTable(Connection connection, String relationTable, String relationTagTable, String tagTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(relationTagTable);
        sb.append("(ID_RELATION BIGINT, ID_TAG BIGINT, PRIMARY KEY(ID_RELATION, ID_TAG), FOREIGN KEY(ID_RELATION) REFERENCES ");
        sb.append(relationTable);
        sb.append(",FOREIGN KEY(ID_TAG) REFERENCES ");
        sb.append(tagTableName).append(");");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + relationTable + " VALUES ( ?,?,?,?,?,?,?);");
    }

    /**
     *
     * @param connection
     * @param nodeMemberTable
     * @param relationTable
     * @param nodeTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodeMemberTable(Connection connection, String nodeMemberTable, String relationTable, String nodeTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodeMemberTable);
        sb.append("(ID_RELATION BIGINT,ID_NODE BIGINT, ROLE VARCHAR, NODE_ORDER INT, PRIMARY KEY(ID_RELATION, ID_NODE, NODE_ORDER) ,FOREIGN KEY(ID_RELATION) REFERENCES ");
        sb.append(relationTable);
        sb.append(", FOREIGN KEY(ID_NODE) REFERENCES ");
        sb.append(nodeTableName).append(");");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + nodeMemberTable + " VALUES ( ?,?,?,?);");
    }

    /**
     *
     * @param connection
     * @param wayMemberTable
     * @param relationTable
     * @param wayTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayMemberTable(Connection connection, String wayMemberTable, String relationTable, String wayTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayMemberTable);
        sb.append("(ID_RELATION BIGINT, ID_WAY BIGINT, ROLE VARCHAR, WAY_ORDER INT, PRIMARY KEY(ID_RELATION, ID_WAY, WAY_ORDER), FOREIGN KEY(ID_RELATION) REFERENCES ");
        sb.append(relationTable);
        sb.append(", FOREIGN KEY(ID_WAY) REFERENCES ");
        sb.append(wayTableName).append(");");
        stmt.execute(sb.toString());
        stmt.close();
        return connection.prepareStatement("INSERT INTO " + wayMemberTable + " VALUES ( ?,?,?,?);");
    }
}
