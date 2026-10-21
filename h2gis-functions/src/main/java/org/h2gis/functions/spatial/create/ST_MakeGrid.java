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

import org.h2.value.*;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Create a regular grid based on a table or a geometry envelope. The geometry
 * envelope could be expressed using a subquery as 
 * (SELECT the_geom from myTable)
 *
 * @author Erwan Bocher
 * @author Nathan Marie (CNRS)
 */
public class ST_MakeGrid extends AbstractFunction implements ScalarFunction {

  
    public ST_MakeGrid() {
        addProperty(PROP_REMARKS, "Calculate a regular grid.\n"
                + "ST_MakeGrid(geometry|tableName, deltaX, deltaY)\n"
                + "ST_MakeGrid(geometry|tableName, deltaX, deltaY, upperOrder|angle)\n"
                + "ST_MakeGrid(geometry|tableName, deltaX, deltaY, upperOrder, isColumnsRows|angle)\n"
                + "ST_MakeGrid(geometry|tableName, deltaX, deltaY, upperOrder, isColumnsRows, angle)\n"
                + "The first argument is either a geometry or a table name.\n"
                + "deltaX and deltaY are the cell size in a cartesian plane, or the number of columns and rows "
                + "when isColumnsRows is true.\n"
                + "upperOrder set to true starts the cells from the upper left corner.\n"
                + "angle is the rotation of the grid in radian.\n"
                + "Note: the geometry could be expressed using a subquery as (SELECT the_geom from myTable)");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGrid";
    }

    /**
     * Create a regular grid using the first input argument to compute the full
     * extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains all cells as a set of polygons
     */
    public static ResultSet createGrid(Connection connection, Value value, double deltaX, double deltaY) throws SQLException {
            return buildGrid(connection, value, deltaX, deltaY, false, false, 0);
    }

    /**
     * Create a regular grid using the first input argument to compute the full
     * extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @param upperOrder start the cell from the upper left corner
     * @param isColumnsRowsMeasure deltaX and deltaY refer to the number of columns and rows
     * @param angle the rotation in radian
     * @return a resultset that contains all cells as a set of polygons
     */
    public static ResultSet createGrid(Connection connection, Value value, double deltaX, double deltaY, boolean upperOrder, boolean isColumnsRowsMeasure, Value angle) throws SQLException {
        return buildGrid(connection, value, deltaX, deltaY, upperOrder, isColumnsRowsMeasure, toAngle(angle));
    }

    /**
     * Create a regular grid using the first input argument to compute the full
     * extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @param valueAngleOrder could be the boolean to indicate the start from the upper left corner or the angle in rotation in radian
     * @return a resultset that contains all cells as a set of polygons
     */
    public static ResultSet createGrid(Connection connection, Value value, double deltaX, double deltaY, Value valueAngleOrder) throws SQLException {
        if (valueAngleOrder == null || valueAngleOrder == ValueNull.INSTANCE) {
            return buildGrid(connection, value, deltaX, deltaY, false, false, 0);
        }
        if (valueAngleOrder instanceof ValueBoolean) {
            return buildGrid(connection, value, deltaX, deltaY, valueAngleOrder.getBoolean(), false, 0);
        }
        if (DataType.isNumericType(valueAngleOrder.getValueType())) {
            return buildGrid(connection, value, deltaX, deltaY, false, false, valueAngleOrder.getDouble());
        }
        throw new SQLException("This function supports only angle or a boolean as last argument.");
    }

    /**
     * Create a regular grid using the first input argument to compute the full
     * extent.
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size, or the number of columns
     * @param deltaY the Y cell size, or the number of rows
     * @param upperOrder start the cell from the upper left corner
     * @param valueAngleColumnsRows a boolean to indicate that deltaX and deltaY refer to the number
     *                              of columns and rows, or the rotation angle in radian
     * @return a resultset that contains all cells as a set of polygons
     */
    public static ResultSet createGrid(Connection connection, Value value, double deltaX, double deltaY, boolean upperOrder, Value valueAngleColumnsRows) throws SQLException {
        if (valueAngleColumnsRows instanceof ValueBoolean) {
            return buildGrid(connection, value, deltaX, deltaY, upperOrder, valueAngleColumnsRows.getBoolean(), 0);
        }
        return buildGrid(connection, value, deltaX, deltaY, upperOrder, false, toAngle(valueAngleColumnsRows));
    }

    /**
     * Read the rotation angle
     *
     * @param angle the rotation in radian
     * @return the angle, 0 if null
     */
    public static double toAngle(Value angle) throws SQLException {
        if (angle == null || angle == ValueNull.INSTANCE) {
            return 0;
        }
        if (DataType.isNumericType(angle.getValueType())) {
            double value = angle.getDouble();
            if (!Double.isFinite(value)) {
                throw new SQLException("The angle must be a finite value expressed in radian.");
            }
            return value;
        }
        throw new SQLException("The angle must be a numeric value expressed in radian.");
    }

    public static ResultSet buildGrid(Connection connection, Value value, double deltaX, double deltaY,
                                      boolean upperOrder, boolean isColumnsRowsMeasure, double angle) throws SQLException {
        return buildGrid(connection, value, deltaX, deltaY, upperOrder, isColumnsRowsMeasure, angle, false);
    }

    /**
     * Method to build the grid with all parameters
     *
     * @param connection database
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @param upperOrder start the cell from the upper left corner
     * @param isColumnsRowsMeasure deltaX and deltaY refer to the number of columns and rows
     * @param angle the rotation in radian
     * @param isCenterCell true to return the center of the cells as points
     * @return a resultset that contains all cells
     */
    public static ResultSet buildGrid(Connection connection, Value value, double deltaX, double deltaY,
                                      boolean upperOrder, boolean isColumnsRowsMeasure, double angle,
                                      boolean isCenterCell) throws SQLException {
        if (value == null || value == ValueNull.INSTANCE) {
            return null;
        }
        GridRowSet gridRowSet;
        if (DataType.isCharacterStringType(value.getValueType())) {
            gridRowSet = new GridRowSet(connection, deltaX, deltaY, value.getString(), angle);
        } else if (value instanceof ValueGeometry) {
            gridRowSet = new GridRowSet(connection, deltaX, deltaY, ((ValueGeometry) value).getGeometry(), angle);
        } else {
            throw new SQLException("This function supports only table name or geometry as first argument.");
        }
        gridRowSet.setCenterCell(isCenterCell);
        gridRowSet.setUpperOrder(upperOrder);
        gridRowSet.setIsRowColumnNumber(isColumnsRowsMeasure);
        return gridRowSet.getResultSet();
    }

}
