/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.create;

import org.h2.tools.SimpleResultSet;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueVarchar;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Computes the center point and radius of the smallest circle that contains a geometry
 * @author Erwan Bocher, CNRS, 2023
 */
public class ST_MinimumBoundingRadius extends DeterministicScalarFunction {

    /**
     * Constructor
     */
    public ST_MinimumBoundingRadius() {
        addProperty(PROP_REMARKS, "Computes the center point and radius of the smallest circle that contains a geometry. Returns a record with fields:\n" +
                "\n" +
                "center - center point of the circle\n" +
                "\n" +
                "radius - radius of the circle." +
                "\n The input argument should be a geometry, a table or a select query");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Computes the center point and radius of the smallest circle that contains a geometry
     * @param value Any geometry, select query or table name
     * @return Minimum bounding circle center point plus its radius
     */
    public static ResultSet execute(Connection connection, Value value) throws SQLException {
        if (value == null) {
            return null;
        }
        if (value instanceof ValueVarchar) {
            MinimumBoundingRadiusRowSet mbrd = new MinimumBoundingRadiusRowSet(connection, value.getString());
            return mbrd.getResultSet();

        } else if (value instanceof ValueGeometry) {
            ValueGeometry valueGeometry = (ValueGeometry) value;
            Geometry geometry = valueGeometry.getGeometry();
            if (geometry.getNumPoints() == 0) {
                return null;
            }
            MinimumBoundingCircle mbc = new MinimumBoundingCircle(geometry);
            SimpleResultSet srs = new SimpleResultSet();
            srs.addColumn("CENTER", Types.OTHER, "GEOMETRY", 0, 0);
            srs.addColumn("RADIUS", Types.DOUBLE, 10, 0);
            Geometry geom = geometry.getFactory().createPoint(mbc.getCentre());
            geom.setSRID(geometry.getSRID());
            srs.addRow(geom, mbc.getRadius());
            return srs;

        } else {
            throw new SQLException("This function supports only table name, select query or geometry as first argument.");
        }

    }
}
