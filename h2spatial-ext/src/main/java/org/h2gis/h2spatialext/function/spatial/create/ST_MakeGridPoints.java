/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueString;
import org.h2gis.h2spatialapi.AbstractFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.h2gis.h2spatialapi.ScalarFunction;
import static org.h2gis.h2spatialapi.ScalarFunction.PROP_NOCACHE;

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
        addProperty(PROP_NOCACHE, true);
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGridPoints";
    }

    /**
     * Create a regular grid of points using the first input value to compute
     * the full extent.
     *
     * @param connection
     * @param value could be the name of a table or a geometry.
     * @param deltaX the X cell size
     * @param deltaY the Y cell size
     * @return a resultset that contains all cells as a set of polygons
     * @throws SQLException
     */
    public static ResultSet createGridPoints(Connection connection, Value value, double deltaX, double deltaY) throws SQLException {
        if (value instanceof ValueString) {
            GridRowSet gridRowSet = new GridRowSet(connection, deltaX, deltaY, value.getString());
            gridRowSet.setCenterCell(true);
            return gridRowSet.getResultSet();
        } else if (value instanceof ValueGeometry) {
            ValueGeometry geom = (ValueGeometry) value;
            GridRowSet gridRowSet = new GridRowSet(connection, deltaX, deltaY, geom.getGeometry().getEnvelopeInternal());
            gridRowSet.setCenterCell(true);
            return gridRowSet.getResultSet();
        } else {
            throw new SQLException("This function supports only table name or geometry as first argument.");
        }
    }
}
