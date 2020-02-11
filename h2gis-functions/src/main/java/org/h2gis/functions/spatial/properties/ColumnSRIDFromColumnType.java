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

package org.h2gis.functions.spatial.properties;

import org.h2.util.StringUtils;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get the column SRID from column type and data.
 *
 * @author Erwan Bocher (CNRS)
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class ColumnSRIDFromColumnType extends AbstractFunction implements ScalarFunction {
    private static final String SRID_FUNC = ST_SRID.class.getSimpleName();
    private static final Pattern SRID_CONSTRAINT_PATTERN = Pattern.compile("\"?ST_SRID\\s*\"?\\(([^)]+)\\)\\s*([<|>|!]?=|<>|>|<)\\s*(\\d+)|^\\s*GEOMETRY\\s*\\([\\w|\\s]+,\\s*(\\d*)\\)", Pattern.CASE_INSENSITIVE);

    public ColumnSRIDFromColumnType() {
        addProperty(PROP_REMARKS, "Get the column SRID from column type and data.");
        addProperty(PROP_NAME, "_ColumnSRIDFromColumnType");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getSRID";
    }

    /**
     *
     * @param constraint Constraint expression ex:"ST_SRID(the_geom) = 27572"
     * @return The SRID or 0 if no constraint are found or constraint on other column
     */
    public static int getSRIDFromColumnType(String constraint, String columnName) {
        int srid = 0;
        Matcher matcher = SRID_CONSTRAINT_PATTERN.matcher(constraint);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                String extractedColumnName = matcher.group(1).replace("\"", "").replace("`", "");
                if (extractedColumnName.equalsIgnoreCase(columnName)) {
                    srid = Integer.valueOf(matcher.group(3));
                }
            }
            else{
                srid = Integer.valueOf(matcher.group(4));
            }
        }
        return srid;
    }

    /**
     * Read table constraints from database metadata.
     * @param connection Active connection
     * @param catalogName Catalog name or empty string
     * @param schemaName Schema name or empty string
     * @param tableName table name
     * @return Found table constraints
     * @throws SQLException
     */
    public static String fetchConstraint(Connection connection, String catalogName, String schemaName, String tableName) throws SQLException {
        // Merge column constraint and table constraint
        PreparedStatement pst = SFSUtilities.prepareInformationSchemaStatement(connection, catalogName, schemaName,
                tableName, "INFORMATION_SCHEMA.TABLE_CONSTRAINTS", "", "TABLE_CATALOG", "TABLE_SCHEMA","TABLE_NAME");
        try (ResultSet rsConstraint = pst.executeQuery()) {
            StringBuilder constraint = new StringBuilder();
            while (rsConstraint.next()) {
                String tableConstr = rsConstraint.getString("SQL");
                if(tableConstr != null) {
                    constraint.append(tableConstr);
                }
            }
            return constraint.toString();
        } finally {
            pst.close();
        }
    }


    /**
     * @param connection Active connection
     * @param tableName Target table name
     * @param columnName Spatial field name
     * @param constraint Column constraint
     * @return The column SRID from constraints and data.
     */
    public static int getSRID(Connection connection, String catalogName, String schemaName, String tableName, String columnName,String constraint) {
        try {
            Statement st = connection.createStatement();
            constraint += fetchConstraint(connection, catalogName, schemaName, tableName);
            int srid = getSRIDFromColumnType(constraint, columnName);
            if(srid > 0) {
                return srid;
            }
            try ( // Fetch the first geometry to find a stored SRID
                    ResultSet rs = st.executeQuery(String.format("select ST_SRID(%s) from %s LIMIT 1;",
                            StringUtils.quoteJavaString(columnName.toUpperCase()),new TableLocation(catalogName, schemaName, tableName)))) {
                if(rs.next()) {
                    srid = rs.getInt(1);
                    if(srid > 0) {
                        return srid;
                    }
                }
            }
            // Unable to find a valid SRID
            return 0;
        } catch (SQLException ex) {
            return 0;
        }
    }
}
