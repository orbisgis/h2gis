package org.h2spatial;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @author bocher
 * 
 * This class can be used to build a sql script for H2 spatial functions.
 * 
 * 
 */

public class SQLCodegenerator {

	static Method[] methods = GeoSpatialFunctions.class.getDeclaredMethods();

	public static void main(String[] args) {

		// Build the spatial database with the functions

		// 1. Connect to the database
		try {

			Class.forName("org.h2.Driver");

			Connection con = DriverManager.getConnection(
					"jdbc:h2:/c:/Temp/h2/erwan1", "sa", "");
			Statement st = con.createStatement();

			// 2. Add the functions

			addSpatialFunctions(st);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// to use drop or create
		// stat.execute("DROP ALIAS GeoVersion");

	}

	/*
	 * Create java code to add function copy paste into
	 * GeoSpatialFunctionsAddRemove to upload it
	 */

	public static void addSpatialFunctions(Statement st) throws SQLException {

		ResultSet result = st
				.executeQuery("SELECT * FROM INFORMATION_SCHEMA.FUNCTION_ALIASES WHERE ALIAS_NAME='GEOVERSION';");

		// we need to test if the geospatial functions exist

		if (result.next() == false) {

			// This method is used to limite the number of blob files.
			// The data access is increased.

			st.executeUpdate("SET LOG 0");
			st.executeUpdate("SET MAX_LENGTH_INPLACE_LOB 200000");

			for (int k = 0; k < GeoSpatialFunctions.class.getDeclaredMethods().length; k++) {

				String function = "CREATE ALIAS ";

				String functionName = methods[k].getName();
				String functionClass = methods[k].getDeclaringClass().getName();

				String complete = (function + functionName + " FOR " + "\""
						+ functionClass + "." + functionName + "\"");

				st.execute(complete);
			}

		} else {
			/**
			 * @todo add message the functions exist.
			 * 
			 */
		}
	}

	/*
	 * Create java code to drop function copy paste into
	 * GeoSpatialFunctionsAddRemove to upload it
	 */

	public static void dropGenerator(Statement st) throws SQLException {
		String[] statDrop = new String[GeoSpatialFunctions.class
				.getDeclaredMethods().length];

		for (int k = 0; k < GeoSpatialFunctions.class.getDeclaredMethods().length; k++) {
			String function = "DROP ALIAS ";
			String functionName = methods[k].getName();
			st.execute(function + functionName);
		}
	}
}