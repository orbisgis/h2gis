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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ebocher
 */
public class OSMParser extends DefaultHandler{
    
    
    //Suffix table names
    String NODE = "_node";
    String WAY = "_way";
    String NODE_TAG = "_node_tag";
    String WAY_TAG = "_way_tag";
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
    
    public OSMParser(){
        
    }
    
    
    public boolean read(File inputFile, String tableName, Connection connection) throws SQLException {
        // Initialisation
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        boolean success = false;
        TableLocation requestedTable = TableLocation.parse(tableName, isH2);
        String osmTableName = requestedTable.getTable();        
        checkOSMTables(connection, isH2, requestedTable, osmTableName); 
        createOSMDatabaseModel( connection,  isH2,  requestedTable,osmTableName);
        
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
        String[] omsTables = new String[]{NODE, NODE_TAG, WAY, WAY_TAG, TAG, RELATION, RELATION_TAG, NODE_MEMBER, WAY_MEMBER};
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
       nodePreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + NODE, isH2));
       nodeTagPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + NODE_TAG, isH2));
       wayPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + WAY, isH2));
       wayTagPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + WAY_TAG, isH2));
       tagPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + TAG, isH2));
       relationPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + RELATION, isH2));
       relationTagPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + RELATION_TAG, isH2));
       nodeMemberPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + NODE_MEMBER, isH2));
       wayMemberPreparedStmt =  OSMTablesFactory.createNodeTable(connection, caseIdentifier(requestedTable, osmTableName + WAY_MEMBER, isH2));
    }
    
}
