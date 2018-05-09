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

package org.h2gis.functions.spatial.generalize;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_PrecisionReducer extends DeterministicScalarFunction {

    public ST_PrecisionReducer() {
        addProperty(PROP_REMARKS, "Reduce the geometry precision. Decimal_Place is the number of decimals to keep.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "precisionReducer";
    }

    /**
     * Reduce the geometry precision. Decimal_Place is the number of decimals to
     * keep.
     *
     * @param geometry
     * @param nbDec
     * @return
     * @throws SQLException
     */
    public static Geometry precisionReducer(Geometry geometry, int nbDec) throws SQLException {
        if(geometry == null){
            return null;
        }
        if (nbDec < 0) {
            throw new SQLException("Decimal_places has to be >= 0.");
        }
        PrecisionModel pm = new PrecisionModel(scaleFactorForDecimalPlaces(nbDec));
        GeometryPrecisionReducer geometryPrecisionReducer = new GeometryPrecisionReducer(pm);
        return geometryPrecisionReducer.reduce(geometry);
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
