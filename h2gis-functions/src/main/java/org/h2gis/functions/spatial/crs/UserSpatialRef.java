package org.h2gis.functions.spatial.crs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * This class contains methods to manage custom CoordinateReferenceSystem that can be build
 * from a proj file that cannot contain any EPSG authority information
 *
 */
public class UserSpatialRef {

    public static String USER_SPATIAL_REF_SYS_TABLE = "USER_SPATIAL_REF_SYS";
    static String USER_SPATIAL_REF_SYS_SEQUENCE = "USER_SPATIAL_REF_SYS_SEQ";
    private static Connection con;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSpatialRef.class);

    /**
     * Create a temporary table available only for the local connexion to store prj information when
     * CTS is not able to detect the official SRID
     * A unique identifier is added. It starts at 1000000
     * @param con
     * @throws SQLException
     */
    public static void createUserSpatialRefTable(Connection con) throws SQLException {
        try ( // Create the table if not exists
              Statement st = con.createStatement()) {
              st.execute("CREATE SEQUENCE IF NOT EXISTS "+USER_SPATIAL_REF_SYS_SEQUENCE+ " AS INTEGER START WITH 1000000;" +
                      "CREATE LOCAL TEMPORARY TABLE IF NOT EXISTS "+USER_SPATIAL_REF_SYS_TABLE+" (SRID bigint primary key, SRTEXT VARCHAR);");
        }
    }

    /**
     * Autogenarate a new user srid and store the prj in the USER_SPATIAL_REF_SYS table
     * @param prj the input prj file
     * @return
     */
    public static int getUserSRID(String prj)  {
        if(con==null){
            LOGGER.warn("Please init the connection to the database\n,  otherwise the srid will be set to 0");
            return 0;
        }
        int generateSrid =0;
        try ( // Create the table if not exists
              Statement st = con.createStatement()) {
            PreparedStatement prep = con.prepareStatement("INSERT INTO " +USER_SPATIAL_REF_SYS_TABLE+ "(SRID, SRTEXT) VALUES (next value for "+USER_SPATIAL_REF_SYS_SEQUENCE+ ", ?)",
                    Statement.RETURN_GENERATED_KEYS);
            prep.setString(1, prj);
            prep.execute();
            ResultSet rs = prep.getGeneratedKeys();
            if(rs.next()){
                generateSrid = rs.getInt(1);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return generateSrid;
    }

    /**
     * Init a temporary table USER_SPATIAL_REF_SYS to store non standardised prj
     * @param connection
     * @throws SQLException
     */
    public static void init(Connection connection) throws SQLException {
        con = connection;
        createUserSpatialRefTable(connection);
    }
}
