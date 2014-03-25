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
package org.h2gis.drivers.gpx.model;

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

    private GPXTablesFactory() {
    }

    /**
     * Create the waypoints table that will be used to import GPX data
     *
     * @param connection
     * @param wayPointsTableName
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayPointsTable(Connection connection, String wayPointsTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayPointsTableName);
        sb.append(" (the_geom POINT,id INT,");
        sb.append(GPXTags.LAT.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.LON.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.ELE.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.TIME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.MAGVAR.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.GEOIDHEIGHT.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.DESC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SYM.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.FIX.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SAT.toLowerCase()).append(" INT,");
        sb.append(GPXTags.HDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.VDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.PDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.AGEOFDGPSDATA.toLowerCase()).append(" DOUBLE,");
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
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRouteTable(Connection connection, String routeTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(routeTableName);
        sb.append(" (the_geom LineString,id INT,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.DESC.toLowerCase()).append(" TEXT,");
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
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRoutePointsTable(Connection connection, String routePointsTable) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(routePointsTable);
        sb.append(" (the_geom POINT,id INT, ");
        sb.append(GPXTags.LAT.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.LON.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.ELE.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.TIME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.MAGVAR.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.GEOIDHEIGHT.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.DESC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SYM.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.FIX.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SAT.toLowerCase()).append(" INT,");
        sb.append(GPXTags.HDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.VDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.PDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.AGEOFDGPSDATA.toLowerCase()).append(" DOUBLE,");
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
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackTable(Connection connection, String trackTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackTableName);
        sb.append(" (the_geom MultiLineString,id INT,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.DESC.toLowerCase()).append(" TEXT,");
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
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackSegmentsTable(Connection connection, String trackSegementsTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackSegementsTableName);
        sb.append(" (the_geom LINESTRING,id INT,");
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
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackPointsTable(Connection connection, String trackPointsTableName) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackPointsTableName);
        sb.append(" (the_geom POINT,id INT, ");
        sb.append(GPXTags.LAT.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.LON.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.ELE.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.TIME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.MAGVAR.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.GEOIDHEIGHT.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.NAME.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.CMT.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.DESC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SRC.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREF.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.HREFTITLE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SYM.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.TYPE.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.FIX.toLowerCase()).append(" TEXT,");
        sb.append(GPXTags.SAT.toLowerCase()).append(" INT,");
        sb.append(GPXTags.HDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.VDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.PDOP.toLowerCase()).append(" DOUBLE,");
        sb.append(GPXTags.AGEOFDGPSDATA.toLowerCase()).append(" DOUBLE,");
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
}
