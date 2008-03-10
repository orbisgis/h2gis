package org.h2spatial.manual;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

public class DatatypeGeometry {
	
	private static Connection con;
	private static Statement st;
	
	
	public static void main(String args[]) throws ClassNotFoundException,
			SQLException, ParseException {
		
		createdb();
				
		ResultSet rs = st.executeQuery("SELECT * from TEST;");
				
		ResultSetMetaData rsmd2 = rs.getMetaData();
		WKBReader wkbReader = new WKBReader();
		WKTReader wKTReader = new WKTReader();
		byte valObj[] = (byte[]) null;
		Geometry geom = null;
		int srid_tmp = 0;
		for (; rs.next();) {
			String columnName = rsmd2.getColumnName(2);
			String columnTypeName = rsmd2.getColumnTypeName(2);
			int columnType = rsmd2.getColumnType(2);
			if (columnTypeName.equals("GEOMETRY")) {
				valObj = rs.getBytes(2);
				System.out.println(valObj.getClass().getName());
				geom = wkbReader.read(valObj);
				System.out.println(geom);
			}
		}
	}


	
	
	
	public static void createdb(){
		
		try {
			Class.forName("org.h2.Driver");
		
		 con = DriverManager.getConnection("jdbc:h2:/tmp/erwan/db2",
				"sa", "");
		String data = "MULTILINESTRING ((185372.453125 2427922.5, 185390.609375 2427946.75, 185404.84375 2427963, 185423.359375 2427983.75, 185440.4375 2427999.25, 185459.3125 2428016.5, 185463.578125 2428018.25))";
		Geometry geom = (new WKTReader()).read(data);
		geom.setSRID(4326);
		WKBWriter writer = new WKBWriter(3, 2);
		byte wkb[] = writer.write(geom);
		st = con.createStatement();
		st.execute("CREATE DOMAIN GEOMETRY AS BLOB ");
		
		st.execute("CREATE TABLE TEST (gid INT , the_geom GEOMETRY)");
		PreparedStatement prep = con
				.prepareStatement("INSERT INTO TEST (gid, the_geom) VALUES(?, ?)");
		prep.setInt(1, 1);
		prep.setBytes(2, wkb);
		prep.executeUpdate();
		
		
		System.out.println("Fin");
		
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}