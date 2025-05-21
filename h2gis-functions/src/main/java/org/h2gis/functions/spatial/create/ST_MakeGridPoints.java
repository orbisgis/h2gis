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
import org.h2.value.ValueGeometry;
import org.h2.value.ValueVarchar;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Create a regular grid of points based on a table or a geometry envelope.
 *
 * @author Erwan Bocher
 */
public class ST_MakeGridPoints extends AbstractFunction implements ScalarFunction {

  
    public ST_MakeGridPoints() {
        addProperty(PROP_REMARKS, "Calculate a regular grid of points.\n"
                + "The first argument is either a geometry or a table.\n"
                + "The delta X and Y cell grid are expressed in a cartesian plane."
                + "Note :The geometry could be expressed using a subquery as\n"
                + " (SELECT the_geom from myTable)");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGridPoints";
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection database     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains all cells as a set of polygons
     */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY) throws SQLException {
            return createGridPoints(connection,  value, deltaX, deltaY, false);
    }
        /**
         * Create a regular grid of points using the first input value to compute
         * the full extent.
         *
         * @param connection database     * @param value could be the name of a table or a geometry.
         * @param deltaX the X cell size
         * @param deltaY the Y cell size
         * @return a resultset that contains all cells as a set of polygons
         */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY, boolean upperOrder) throws SQLException {
        if(value == null){
            return null;
        }
        if (value instanceof ValueVarchar) {
            GridRowSet gridRowSet = new GridRowSet(connection, deltaX, deltaY, value.getString());
            gridRowSet.setCenterCell(true);
            gridRowSet.setUpperOrder(upperOrder);
            return gridRowSet.getResultSet();
        } else if (value instanceof ValueGeometry) {
            ValueGeometry geom = (ValueGeometry) value;
            GridRowSet gridRowSet = new GridRowSet(connection, deltaX, deltaY, geom.getGeometry());
            gridRowSet.setCenterCell(true);
            gridRowSet.setUpperOrder(upperOrder);
            return gridRowSet.getResultSet();
        } else {
            throw new SQLException("This function supports only table name or geometry as first argument.");
        }
    }
}
