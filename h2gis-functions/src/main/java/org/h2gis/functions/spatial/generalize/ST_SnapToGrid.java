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

package org.h2gis.functions.spatial.generalize;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

import java.sql.SQLException;

/**
 * Function to snap a geometry according a grid size
 * @author Erwan Bocher, CNRS, 2024
 */
public class ST_SnapToGrid extends DeterministicScalarFunction {

    public ST_SnapToGrid() {
        addProperty(PROP_REMARKS, "Snap all points of the input geometry to the grid defined by its origin and cell size.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Reduce the geometry precision. cell_size is resolution of grid to snap the points
     *
     * @param geometry
     * @param cell_size
     * @return
     * @throws SQLException
     */
    public static Geometry execute(Geometry geometry, float cell_size) throws SQLException {
        if (geometry == null) {
            return null;
        }
        if(cell_size==0){
            return geometry;
        }
        if (cell_size < 0) {
            throw new SQLException("cell size has to be >= 0.");
        }
        PrecisionModel pm = new PrecisionModel(Math.round(1/cell_size));
        GeometryPrecisionReducer geometryPrecisionReducer = new GeometryPrecisionReducer(pm);
        try {
            return geometryPrecisionReducer.reduce(geometry);
        } catch (IllegalArgumentException ex) {
            return geometry;
        }
    }

    /**
     * Computes the scale factor for a given number of decimal places.
     *
     * @param decimalPlaces
     * @return the scale factor
     */
    public static double scaleFactorForDecimalPlaces(int decimalPlaces) {
        return Math.pow(10.0, decimalPlaces);
    }
}
