/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.crs;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;

/**
 * Function to update the SRID of a geometry column
 * 
 * @author Erwan Bocher, CNRS (2020)
 */
public class UpdateGeometrySRID  extends AbstractFunction implements ScalarFunction {

    public UpdateGeometrySRID() {
        addProperty(PROP_REMARKS, "Updates the SRID of all features in a geometry column. ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "changeSRID";
    }

    /**
     * Method to update the SRID
     * 
     * @param connection active connection to the database
     * @param table_name name of the tabe
     * @param column_name name of the geomtry column
     * @param srid value of the new srid
     * @return true if the SRID is changed
     * @throws SQLException 
     */
    public static boolean changeSRID(Connection connection, String table_name, String column_name, int srid) throws SQLException {
        return changeSRID(connection, null, null, table_name, column_name, srid);
    }

    /**
     * Method to update the SRID
     * 
     * @param connection active connection to the database
     * @param schema_name name of the schema
     * @param table_name name of the tabe
     * @param column_name name of the geomtry column
     * @param srid value of the new srid
     * @return true if the SRID is changed
     * @throws SQLException 
     */
    public static boolean changeSRID(Connection connection, String schema_name, String table_name, String column_name, int srid) throws SQLException {
        return changeSRID(connection, null, schema_name, table_name, column_name, srid);
    }

    /**
     * Method to update the SRID
     * 
     * @param connection active connection to the database
     * @param catalog_name name of the catalog
     * @param schema_name name of the schema
     * @param table_name name of the tabe
     * @param column_name name of the geomtry column
     * @param srid value of the new srid
     * @return true if the SRID is changed
     * @throws SQLException 
     */
    public static boolean changeSRID(Connection connection, String catalog_name, String schema_name, String table_name, String column_name, int srid) throws SQLException {
        TableLocation tableLocation = new TableLocation(catalog_name, schema_name, table_name, DBTypes.H2GIS);
        return GeometryTableUtilities.alterSRID(connection, tableLocation, column_name, srid);
    }

}
