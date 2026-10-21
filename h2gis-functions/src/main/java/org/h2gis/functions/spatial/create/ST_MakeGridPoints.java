/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.create;

import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.h2gis.functions.spatial.create.ST_MakeGrid.buildGrid;
import static org.h2gis.functions.spatial.create.ST_MakeGrid.toAngle;

/**
 * Create a regular grid of points based on a table or a geometry envelope.
 * Each point is the center of the cell computed by ST_MakeGrid with the same arguments.
 *
 * @author Erwan Bocher
 */
public class ST_MakeGridPoints extends AbstractFunction implements ScalarFunction {

    public ST_MakeGridPoints() {
        addProperty(PROP_REMARKS, "Calculate a regular grid of points located at the center of each cell.\n"
                + "ST_MakeGridPoints(geometry|tableName, deltaX, deltaY)\n"
                + "ST_MakeGridPoints(geometry|tableName, deltaX, deltaY, upperOrder|angle)\n"
                + "ST_MakeGridPoints(geometry|tableName, deltaX, deltaY, upperOrder, isColumnsRows|angle)\n"
                + "ST_MakeGridPoints(geometry|tableName, deltaX, deltaY, upperOrder, isColumnsRows, angle)\n"
                + "The first argument is either a geometry or a table name.\n"
                + "deltaX and deltaY are the cell size in a cartesian plane, or the number of columns and rows "
                + "when isColumnsRows is true.\n"
                + "upperOrder set to true starts the cells from the upper left corner.\n"
                + "angle is the rotation of the grid in radian.\n"
                + "Note: the geometry could be expressed using a subquery as (SELECT the_geom from myTable)");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGridPoints";
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains the center of all cells as a set of points
     */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY) throws SQLException {
        return buildGrid(connection, value, deltaX, deltaY, false, false, 0, true);
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @param valueAngleOrder a boolean to start from the upper left corner, or the rotation angle in radian
     * @return a resultset that contains the center of all cells as a set of points
     */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY, Value valueAngleOrder) throws SQLException {
        if (valueAngleOrder instanceof ValueBoolean) {
            return buildGrid(connection, value, deltaX, deltaY, valueAngleOrder.getBoolean(), false, 0, true);
        }
        return buildGrid(connection, value, deltaX, deltaY, false, false, toAngle(valueAngleOrder), true);
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size, or the number of columns
     * @param deltaY the Y cell size, or the number of rows
     * @param upperOrder start the cell from the upper left corner
     * @param valueAngleColumnsRows a boolean to indicate that deltaX and deltaY refer to the number
     *                              of columns and rows, or the rotation angle in radian
     * @return a resultset that contains the center of all cells as a set of points
     */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY, boolean upperOrder, Value valueAngleColumnsRows) throws SQLException {
        if (valueAngleColumnsRows instanceof ValueBoolean) {
            return buildGrid(connection, value, deltaX, deltaY, upperOrder, valueAngleColumnsRows.getBoolean(), 0, true);
        }
        return buildGrid(connection, value, deltaX, deltaY, upperOrder, false, toAngle(valueAngleColumnsRows), true);
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size, or the number of columns
     * @param deltaY the Y cell size, or the number of rows
     * @param upperOrder start the cell from the upper left corner
     * @param isColumnsRowsMeasure deltaX and deltaY refer to the number of columns and rows
     * @param angle the rotation in radian
     * @return a resultset that contains the center of all cells as a set of points
     */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY, boolean upperOrder, boolean isColumnsRowsMeasure, Value angle) throws SQLException {
        return buildGrid(connection, value, deltaX, deltaY, upperOrder, isColumnsRowsMeasure, toAngle(angle), true);
    }
}