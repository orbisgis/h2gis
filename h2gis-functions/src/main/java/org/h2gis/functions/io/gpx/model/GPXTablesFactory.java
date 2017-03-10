/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.gpx.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;

/**
 * A factory to create the tables that are used to import GPX data
 *
 * @author Erwan Bocher
 */
public class GPXTablesFactory {
    
     //Suffix table names
    public static final String WAYPOINT = "_waypoint";
    public static final String ROUTE = "_route";
    public static final String ROUTEPOINT = "_routepoint";
    public static final String TRACK = "_track";
    public static final String TRACKSEGMENT = "_tracksegment";
    public static final String TRACKPOINT = "_trackpoint";

    private GPXTablesFactory() {
    }

    /**
     * Create the waypoints table that will be used to import GPX data
     *
     * @param connection
     * @param wayPointsTableName
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayPointsTable(Connection connection, String wayPointsTableName, boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayPointsTableName).append(" (");
        if (isH2) {
            sb.append("the_geom POINT CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
             sb.append("the_geom GEOMETRY(POINT, 4326),");
        }
        sb.append(" id INT,");
        sb.append(GPXTags.LAT.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.LON.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.ELE.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.TIME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.MAGVAR.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.GEOIDHEIGHT.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append("description").append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SYM.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.FIX.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SAT.toLowerCase()).append(" INT,");
        sb.append(GPXTags.HDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.VDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.PDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.AGEOFDGPSDATA.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.DGPSID.toLowerCase()).append(" INT,");
        sb.append(GPXTags.EXTENSIONS.toLowerCase()).append(" BOOLEAN);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the waypoints table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(wayPointsTableName).append(" VALUES ( ?");
        for (int i = 1; i < GpxMetadata.WPTFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());
    }

    /**
     * Create the route table that will be used to import GPX data
     *
     * @param connection
     * @param routeTableName
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRouteTable(Connection connection, String routeTableName, boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(routeTableName).append(" (");
        if (isH2) {
            sb.append("the_geom LINESTRING CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("the_geom GEOMETRY(LINESTRING, 4326),");
        }
        sb.append(" id INT,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append("description").append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.NUMBER.toLowerCase()).append(" INT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.EXTENSIONS.toLowerCase()).append(" TEXT);");
        stmt.execute(sb.toString());
        stmt.close();

        //We return the preparedstatement of the route table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(routeTableName).append(" VALUES ( ?");
        for (int i = 1; i < GpxMetadata.RTEFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());
    }

    /**
     * Createthe route points table to store the route waypoints
     *
     * @param connection
     * @param routePointsTable
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRoutePointsTable(Connection connection, String routePointsTable,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(routePointsTable).append(" (");
        if (isH2) {
            sb.append("the_geom POINT CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("the_geom GEOMETRY(POINT, 4326),");
        }
        sb.append(" id INT,");
        sb.append(GPXTags.LAT.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.LON.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.ELE.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.TIME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.MAGVAR.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.GEOIDHEIGHT.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append("description").append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SYM.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.FIX.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SAT.toLowerCase()).append(" INT,");
        sb.append(GPXTags.HDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.VDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.PDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.AGEOFDGPSDATA.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.DGPSID.toLowerCase()).append(" INT,");
        sb.append(GPXTags.EXTENSIONS.toLowerCase()).append(" BOOLEAN,");
        sb.append("route_id").append(" INT);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the waypoints table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(routePointsTable).append(" VALUES ( ?");
        for (int i = 1; i < GpxMetadata.RTEPTFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());

    }

    /**
     * Creat the track table
     *
     * @param connection
     * @param trackTableName
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackTable(Connection connection, String trackTableName,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackTableName).append(" (");
        if (isH2) {
            sb.append("the_geom MULTILINESTRING CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("the_geom GEOMETRY(MULTILINESTRING, 4326),");
        }
        sb.append(" id INT,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append("description").append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.NUMBER.toLowerCase()).append(" INT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.EXTENSIONS.toLowerCase()).append(" TEXT);");
        stmt.execute(sb.toString());
        stmt.close();

        //We return the preparedstatement of the route table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(trackTableName).append(" VALUES ( ?");
        for (int i = 1; i < GpxMetadata.RTEFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());
    }

    /**
     * Create the track segments table to store the segments of a track
     *
     * @param connection
     * @param trackSegementsTableName
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackSegmentsTable(Connection connection, String trackSegementsTableName,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackSegementsTableName).append(" (");
        if (isH2) {
            sb.append("the_geom LINESTRING CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("the_geom GEOMETRY(LINESTRING, 4326),");
        }
        sb.append(" id INT,");
        sb.append(GPXTags.EXTENSIONS).append(" TEXT,");
        sb.append("id_track INT);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the waypoints table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(trackSegementsTableName).append(" VALUES ( ?");
        for (int i = 1; i < GpxMetadata.TRKSEGFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());

    }

    /**
     * Create the track points table to store the track waypoints
     *
     * @param connection
     * @param trackPointsTableName
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackPointsTable(Connection connection, String trackPointsTableName,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackPointsTableName).append(" (");
        if(isH2){
        sb.append("the_geom POINT CHECK ST_SRID(THE_GEOM) = 4326,");
        }
        else{
            sb.append("the_geom GEOMETRY(POINT, 4326),");
        }
        sb.append(" id INT,");
        sb.append(GPXTags.LAT.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.LON.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.ELE.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.TIME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.MAGVAR.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.GEOIDHEIGHT.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append("description").append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SYM.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.FIX.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SAT.toLowerCase()).append(" INT,");
        sb.append(GPXTags.HDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.VDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.PDOP.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.AGEOFDGPSDATA.toLowerCase()).append(" FLOAT8,");
        sb.append(GPXTags.DGPSID.toLowerCase()).append(" INT,");
        sb.append(GPXTags.EXTENSIONS.toLowerCase()).append(" BOOLEAN,");
        sb.append("track_segment_id").append(" INT);");
        stmt.execute(sb.toString());
        stmt.close();
        //We return the preparedstatement of the waypoints table
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(trackPointsTableName).append(" VALUES ( ?");
        for (int i = 1; i < GpxMetadata.RTEPTFIELDCOUNT; i++) {
            insert.append(",?");
        }
        insert.append(");");
        return connection.prepareStatement(insert.toString());
    }
    
    
    /**
     * Drop the existing GPX tables used to store the imported OSM GPX 
     *
     * @param connection
     * @param isH2
     * @param tablePrefix
     * @throws SQLException
     */
    public static void dropOSMTables(Connection connection, boolean isH2, String tablePrefix) throws SQLException {
        TableLocation requestedTable = TableLocation.parse(tablePrefix, isH2);
        String gpxTableName = requestedTable.getTable();        
        String[] gpxTables = new String[]{WAYPOINT,ROUTE,ROUTEPOINT, TRACK, TRACKPOINT, TRACKSEGMENT};
        StringBuilder sb =  new StringBuilder("drop table if exists ");     
        String gpxTableSuffix = gpxTables[0];
        String gpxTable = TableUtilities.caseIdentifier(requestedTable, gpxTableName + gpxTableSuffix, isH2);           
        sb.append(gpxTable);
        for (int i = 1; i < gpxTables.length; i++) {
            gpxTableSuffix = gpxTables[i];
            gpxTable = TableUtilities.caseIdentifier(requestedTable, gpxTableName + gpxTableSuffix, isH2);
            sb.append(",").append(gpxTable);
        }        
        Statement stmt = connection.createStatement();
        stmt.execute(sb.toString());
        stmt.close();
    }
}
