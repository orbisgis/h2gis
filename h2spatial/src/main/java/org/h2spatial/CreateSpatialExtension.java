/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
package org.h2spatial;

import org.h2spatial.internal.GeoSpatialFunctions;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class can be used to build a sql script for H2 spatial functions.
 * 
 * @author Erwan Bocher
 */
public class CreateSpatialExtension {
    /** H2 base type for geometry column {@link java.sql.ResultSetMetaData#getColumnTypeName(int)} */
    public static final String GEOMETRY_BASE_TYPE = "VARBINARY";

    /**
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     */
    public static void InitSpatialExtension(Connection connection) throws SQLException {
        registerGeometryType(connection);
        addSpatialFunctions(connection);
    }

    private static void registerGeometryType(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CREATE DOMAIN IF NOT EXISTS GEOMETRY AS VARBINARY;");
    }
	/*
	 * Create java code to add function copy paste into
	 * GeoSpatialFunctionsAddRemove to upload it
	 */

	private static void addSpatialFunctions(Connection connection) throws SQLException {

		ResultSet result = connection.createStatement()
				.executeQuery("SELECT * FROM INFORMATION_SCHEMA.FUNCTION_ALIASES WHERE ALIAS_NAME='GEOVERSION';");

		// we need to test if the geospatial functions exist

		if (!result.next()) {

			// This method is used to limit the number of blob files.
			// The data access is increased.
            Statement st = connection.createStatement();
			for (Method method : GeoSpatialFunctions.class.getDeclaredMethods()) {
                // Drop alias if exists
				String functionName = method.getName();
				String functionClass = method.getDeclaringClass().getName();
                st.execute("DROP ALIAS IF EXISTS " + functionName);
                // Create alias, H2 does not support prepare statement on create alias
                st.execute("CREATE ALIAS " + functionName + " FOR \"" + functionClass + "." + functionName + "\"");
			}
		}
	}

	/*
	 * Remove spatial type and functions from the current connection.
	 */
	public static void DisposeSpatialExtension(Connection connection) throws SQLException {
		for (Method method : GeoSpatialFunctions.class.getDeclaredMethods()) {
			String functionName = method.getName();
            connection.createStatement().execute("DROP ALIAS IF EXISTS " + functionName);
		}
	}
}