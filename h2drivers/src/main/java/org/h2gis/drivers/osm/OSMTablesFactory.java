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
import org.h2gis.drivers.gpx.model.GPXTags;
import org.h2gis.drivers.gpx.model.GpxMetadata;

/**
 * Class to create the tables to import osm data
 * 
 * @author Erwan Bocher
 */
public class OSMTablesFactory {
 
    private OSMTablesFactory(){
        
    }
    
    /**
     * Create the nodes table that will be used to import OSM nodes
     * Example : 
     * <node id="298884269" lat="54.0901746" lon="12.2482632" user="SvenHRO" uid="46882" visible="true" version="1" changeset="676636" timestamp="2008-09-21T21:37:45Z"/>
     * @param connection
     * @param nodesTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createNodesTable(Connection connection, String nodesTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(nodesTableName);
        sb.append(" (the_geom POINT,id LONG,");
        sb.append(OSMTags.USER.toLowerCase()).append(" TEXT,");
        sb.append(OSMTags.UID.toLowerCase()).append(" LONG,");
        sb.append(OSMTags.VISIBLE.toLowerCase()).append(" BOOLEAN,");
        sb.append(OSMTags.VERSION.toLowerCase()).append(" INT,");
        sb.append(OSMTags.CHANGE_SET.toLowerCase()).append(" LONG,");      
        sb.append(OSMTags.TIMESTAMP.toLowerCase()).append(" TIMESTAMP);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the waypoints table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(nodesTableName).append(" VALUES ( ?");
        for (int i = 1; i < OSMTags.NODEFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());
    }
    
    /**
     * Create the ways table that will be used to import OSM ways
     * Example : 
     *  <way id="26659127" user="Masch" uid="55988" visible="true" version="5" changeset="4142606" timestamp="2010-03-16T11:47:08Z">
     * @param connection
     * @param waysTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWaysTable(Connection connection, String waysTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(waysTableName);
        sb.append(" (the_geom LINESTRING,id LONG,");
        sb.append(OSMTags.USER.toLowerCase()).append(" TEXT,");
        sb.append(OSMTags.UID.toLowerCase()).append(" LONG,");
        sb.append(OSMTags.VISIBLE.toLowerCase()).append(" BOOLEAN,");
        sb.append(OSMTags.VERSION.toLowerCase()).append(" INT,");
        sb.append(OSMTags.CHANGE_SET.toLowerCase()).append(" LONG,");      
        sb.append(OSMTags.TIMESTAMP.toLowerCase()).append(" TIMESTAMP);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the waypoints table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(waysTableName).append(" VALUES ( ?");
        for (int i = 1; i < OSMTags.WAYFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());
    }
}
