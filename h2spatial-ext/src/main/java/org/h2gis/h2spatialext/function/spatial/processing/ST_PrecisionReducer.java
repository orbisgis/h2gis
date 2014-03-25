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
package org.h2gis.h2spatialext.function.spatial.processing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
