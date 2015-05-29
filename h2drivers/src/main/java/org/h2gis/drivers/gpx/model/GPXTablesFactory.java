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
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createWayPointsTable(Connection connection, String wayPointsTableName, boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(wayPointsTableName);
        if (isH2) {
            sb.append(" (the_geom POINT CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
             sb.append("GEOMETRY(POINT, 4326)");
        }
        sb.append(" id INT,");
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
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRouteTable(Connection connection, String routeTableName, boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(routeTableName);
        if (isH2) {
            sb.append(" (the_geom LINESTRING CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("GEOMETRY(LINESTRING, 4326)");
        }
        sb.append(" id INT,");
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
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createRoutePointsTable(Connection connection, String routePointsTable,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(routePointsTable);
        if (isH2) {
            sb.append(" (the_geom POINT CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("GEOMETRY(POINT, 4326)");
        }
        sb.append(" id INT,");
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
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackTable(Connection connection, String trackTableName,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackTableName);
        if (isH2) {
            sb.append(" (the_geom MULTILINESTRING CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("GEOMETRY(MULTILINESTRING, 4326)");
        }
        sb.append(" id INT,");
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
     * @param isH2 set true if it's an H2 database
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createTrackSegmentsTable(Connection connection, String trackSegementsTableName,boolean isH2) throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(trackSegementsTableName);
        if (isH2) {
            sb.append(" (the_geom LINESTRING CHECK ST_SRID(THE_GEOM) = 4326,");
        } else {
            sb.append("GEOMETRY(LINESTRING, 4326)");
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
        sb.append(trackPointsTableName);
        if(isH2){
        sb.append(" (the_geom POINT CHECK ST_SRID(THE_GEOM) = 4326,");
        }
        else{
            sb.append("GEOMETRY(POINT, 4326)");
        }
        sb.append(" id INT,");
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
