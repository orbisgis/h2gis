/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able
 * to manipulate and create vector and raster spatial information. OrbisGIS
 * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OrbisGIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
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