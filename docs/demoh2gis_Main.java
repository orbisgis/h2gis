package org.orbisgis.demoh2gis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2gis.ext.H2GISExtension;

/**
 * Short demo of in memory spatial database.
 */
public class Main {
    public static void main (String[] args) {
        try {
            Class.forName("org.h2.Driver");
            // Open memory H2 table
            try(Connection connection = DriverManager.getConnection("jdbc:h2:mem:syntax","sa", "sa");
                Statement st = connection.createStatement()) {
                // Import spatial functions, domains and drivers
                // If you are using a file database, you have to do only that once.
                H2GISExtension.load(connection);
                // Create a table
                st.execute("CREATE TABLE ROADS (the_geom MULTILINESTRING, speed_limit INT)");
                // Add some roads
                st.execute("INSERT INTO ROADS VALUES ('MULTILINESTRING((15 5, 20 6, 25 7))', 80)");
                st.execute("INSERT INTO ROADS VALUES ('MULTILINESTRING((20 6, 21 15, 21 25))', 50)");
                // Compute the sum of roads length
                try(ResultSet rs = st.executeQuery("SELECT SUM(ST_LENGTH(the_geom)) total_length from ROADS")) {
                    if(rs.next()) {
                        System.out.println("Total length of roads: "+rs.getDouble("total_length")+" m");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
