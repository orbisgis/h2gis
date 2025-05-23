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

package org.h2gis.functions.io.gpx.model;

import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
     * @param connection database connection
     * @param wayPointsTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createWayPointsTable(Connection connection, String wayPointsTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(wayPointsTableName).append(" (");
            sb.append("the_geom GEOMETRY(POINT, 4326),");
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
        }
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
     * @param connection database connection
     * @param routeTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createRouteTable(Connection connection, String routeTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(routeTableName).append(" (");
            sb.append("the_geom GEOMETRY(LINESTRING, 4326),");
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
        }

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
     * @param connection database
     * @param routePointsTable table name
     * @return PreparedStatement
     */
    public static PreparedStatement createRoutePointsTable(Connection connection, String routePointsTable) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(routePointsTable).append(" (");
            sb.append("the_geom GEOMETRY(POINT, 4326),");
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
        }
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
     * @param connection database
     * @param trackTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createTrackTable(Connection connection, String trackTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(trackTableName).append(" (");
            sb.append("the_geom GEOMETRY(MULTILINESTRING, 4326),");
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
        }

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
     * @param connection database
     * @param trackSegementsTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createTrackSegmentsTable(Connection connection, String trackSegementsTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(trackSegementsTableName).append(" (");
            sb.append("the_geom GEOMETRY(LINESTRING, 4326),");
            sb.append(" id INT,");
            sb.append(GPXTags.EXTENSIONS).append(" TEXT,");
            sb.append("id_track INT);");
            stmt.execute(sb.toString());
        }
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
     * @param connection database
     * @param trackPointsTableName table name
     * @return PreparedStatement
     */
    public static PreparedStatement createTrackPointsTable(Connection connection, String trackPointsTableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            StringBuilder sb = new StringBuilder("CREATE TABLE ");
            sb.append(trackPointsTableName).append(" (");
            sb.append("the_geom GEOMETRY(POINT, 4326),");
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
        }
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
     * @param connection database
     * @param tablePrefix table prefix
     */
    public static void dropOSMTables(Connection connection, TableLocation tablePrefix) throws SQLException {
        final DBTypes dbType = DBUtils.getDBType(connection);
        String gpxTableName = tablePrefix.toString();
        String[] gpxTables = new String[]{WAYPOINT,ROUTE,ROUTEPOINT, TRACK, TRACKPOINT, TRACKSEGMENT};
        StringBuilder sb =  new StringBuilder("drop table if exists ");     
        String gpxTableSuffix = gpxTables[0];
        String gpxTable = TableUtilities.caseIdentifier(tablePrefix, gpxTableName + gpxTableSuffix, dbType);
        sb.append(gpxTable);
        for (int i = 1; i < gpxTables.length; i++) {
            gpxTableSuffix = gpxTables[i];
            gpxTable = TableUtilities.caseIdentifier(tablePrefix, gpxTableName + gpxTableSuffix, dbType);
            sb.append(",").append(gpxTable);
        }        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sb.toString());
        }
    }
}
