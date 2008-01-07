package org.h2spatial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

public class ReadH2Geometry {
	public static void main(String args[]) throws ClassNotFoundException,
			SQLException, ParseException {
		Class.forName("org.h2.Driver");
		Connection con = DriverManager.getConnection(
				"jdbc:h2:C:/Temp/erwan/db", "sa", "");
		Statement st = con.createStatement();
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
			if (columnTypeName.equals("BLOB")) {
				valObj = rs.getBytes(2);
				System.out.println(valObj.getClass().getName());
				geom = wkbReader.read(valObj);
				System.out.println(geom);
			}
		}
	}
}