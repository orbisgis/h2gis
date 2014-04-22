/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
package org.h2gis.network.graph_creator;

import org.h2.tools.SimpleResultSet;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueString;
import org.h2gis.h2spatialapi.ScalarFunction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.h2gis.h2spatial.TableFunctionUtil.isColumnListConnection;
import static org.h2gis.utilities.GraphConstants.DESTINATION;
import static org.h2gis.utilities.GraphConstants.DISTANCE;

/**
 * @author Adam Gogue
 */
public class ST_Accessibility extends GraphFunction implements ScalarFunction {

    @Override
    public String getJavaStaticMethod() {
        return "getAccessibility";
    }

    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             Value arg3) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        if (arg3 instanceof ValueInt) {
            int source = arg3.getInt();
            return oneToAll(connection, inputTable, orientation, null, source);
        } else if (arg3 instanceof ValueString) {
            String table = arg3.getString();
            return manyToMany(connection, inputTable, orientation, null, table);
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg3);
        }
    }

    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             Value arg3,
                                             Value arg4) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        if (arg3 instanceof ValueInt) {
            int source = arg3.getInt();
            if (arg4 instanceof ValueInt) {
                int destination = arg4.getInt();
                return oneToOne(connection, inputTable, orientation, null, source, destination);
            } else if (arg4 instanceof ValueString) {
                String destinationString = arg4.getString();
                return oneToSeveral(connection, inputTable, orientation, null, source, destinationString);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg4);
            }
        } else if (arg3 instanceof ValueString) {
            String weight = arg3.getString();
            if (arg4 instanceof ValueInt) {
                int source = arg4.getInt();
                return oneToAll(connection, inputTable, orientation, weight, source);
            } else if (arg4 instanceof ValueString) {
                String table = arg4.getString();
                return manyToMany(connection, inputTable, orientation, weight, table);
            } else {
                throw new IllegalArgumentException(ARG_ERROR + arg4);
            }
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg3);
        }
    }

    public static ResultSet getAccessibility(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String weight,
                                             int source,
                                             Value arg5) throws SQLException {
        if (isColumnListConnection(connection)) {
            return prepareResultSet();
        }
        if (arg5 instanceof ValueInt) {
            int destination = arg5.getInt();
            return oneToOne(connection, inputTable, orientation, weight, source, destination);
        } else if (arg5 instanceof ValueString) {
            String destinationString = arg5.getString();
            return oneToSeveral(connection, inputTable, orientation, weight, source, destinationString);
        } else {
            throw new IllegalArgumentException(ARG_ERROR + arg5);
        }
    }

    private static SimpleResultSet prepareResultSet() {
        SimpleResultSet output = new SimpleResultSet();
        output.addColumn(DESTINATION, Types.INTEGER, 10, 0);
        output.addColumn(DISTANCE, Types.DOUBLE, 10, 0);
        return output;
    }
}
