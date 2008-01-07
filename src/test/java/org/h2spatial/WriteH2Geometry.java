package org.h2spatial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

public class WriteH2Geometry {
	public static void main(String args[]) throws ClassNotFoundException,
			SQLException, ParseException {
		Class.forName("org.h2.Driver");
		Connection con = DriverManager.getConnection("jdbc:h2:/tmp/h2/test7",
				"sa", "");
		String data = "MULTILINESTRING ((185372.453125 2427922.5, 185390.609375 2427946.75, 185404.84375 2427963, 185423.359375 2427983.75, 185440.4375 2427999.25, 185459.3125 2428016.5, 185463.578125 2428018.25))";
		Geometry geom = (new WKTReader()).read(data);
		geom.setSRID(4326);
		WKBWriter writer = new WKBWriter(3, 2);
		byte wkb[] = writer.write(geom);
		Statement st = con.createStatement();
		st.execute("CREATE TABLE polygon1 (gid int , the_geom blob)");
		PreparedStatement prep = con
				.prepareStatement("INSERT INTO polygon1 (gid, the_geom) VALUES(?, ?)");
		prep.setInt(1, 1);
		prep.setBytes(2, wkb);
		prep.executeUpdate();
		System.out.println("Fin");
	}
}