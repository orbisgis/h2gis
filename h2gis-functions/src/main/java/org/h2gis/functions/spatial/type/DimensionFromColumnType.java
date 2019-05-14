/*
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

package org.h2gis.functions.spatial.type;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.properties.ColumnSRID;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check column type for Z constraint. Has M is not supported yet by JTS Topology Suite WKTReader.
 *
 * @author Erwan Bocher (CNRS)
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class DimensionFromColumnType extends DeterministicScalarFunction {
    private static final Pattern PATTERN = Pattern.compile(
            "\"?ST_COORDDIM\\s*\"?\\(([^)]+)\\)\\s*([<|>|!]?=|<>|>|<)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Check column type for Z constraint. Has M is not supported yet by JTS Topology Suite WKTReader.
     */
    public DimensionFromColumnType() {
        addProperty(PROP_REMARKS, "Check column type for Z constraint.");
        addProperty(PROP_NAME, "_DimensionFromColumnType");
    }

    @Override
    public String getJavaStaticMethod() {
        return "dimensionFromConnectionColumnType";
    }

    /**
     * Public for Unit test
     * @param constraint Constraint value ex: ST_COORDIM(the_geom) = 3
     * @param columnName Column name ex:the_geom
     * @return The dimension constraint [2-3]
     */
    public static int dimensionFromColumnType(String constraint, String columnName) {
        Matcher matcher = PATTERN.matcher(constraint);
        if(matcher.find()) {
            String extractedColumnName = matcher.group(1).replace("\"","").replace("`", "");
            if(extractedColumnName.equalsIgnoreCase(columnName)) {
                int constraint_value = Integer.valueOf(matcher.group(3));
                String sign = matcher.group(2);
                if("<>".equals(sign) || "!=".equals(sign)) {
                    constraint_value = constraint_value == 3 ? 2 : 3;
                }
                if("<".equals(sign)) {
                    constraint_value = 2;
                }
                if(">".equals(sign)) {
                    constraint_value = constraint_value == 2 ? 3 : 2;
                }
                return constraint_value;
            }
        }
        return 2;
    }

    /**
     * @param connection Active connection
     * @param catalogName Table db
     * @param schemaName Table schema
     * @param tableName Table name
     * @param columnName Column name
     * @param constraint Column constraint
     * @return The dimension constraint [2-3]
     */
    public static int dimensionFromConnectionColumnType(Connection connection, String catalogName, String schemaName, String tableName, String columnName,String constraint) {
        try {
            Statement st = connection.createStatement();
            // Merge column constraint and table constraint
            constraint+= ColumnSRID.fetchConstraint(connection, catalogName, schemaName, tableName);
            return dimensionFromColumnType(constraint, columnName);
        } catch (SQLException ex) {
            return 2;
        }
    }
}
