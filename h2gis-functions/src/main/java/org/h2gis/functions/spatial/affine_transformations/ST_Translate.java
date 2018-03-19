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

package org.h2gis.functions.spatial.affine_transformations;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateSequenceDimensionFilter;

/**
 * Translates a geometry using X, Y (and possibly Z) offsets.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_Translate extends DeterministicScalarFunction {

    public static final String MIXED_DIM_ERROR =
            "Cannot translate geometries of mixed dimension";

    public ST_Translate() {
        addProperty(PROP_REMARKS,
                "Translates a geometry using X, Y (and possibly Z) offsets.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "translate";
    }

    /**
     * Translates a geometry using X and Y offsets.
     *
     * @param geom Geometry
     * @param x    X
     * @param y    Y
     * @return Translated geometry
     */
    public static Geometry translate(Geometry geom, double x, double y) {
        if (geom == null) {
            return null;
        }
        final CoordinateSequenceDimensionFilter filter =
                new CoordinateSequenceDimensionFilter();
        geom.apply(filter);
        checkMixed(filter);
        return AffineTransformation.translationInstance(x, y).transform(geom);
    }

    /**
     * Translates a geometry using X, Y and Z offsets.
     *
     * @param geom Geometry
     * @param x    X
     * @param y    Y
     * @param z    Z
     * @return Translated geometry
     */
    public static Geometry translate(Geometry geom, double x, double y, double z) {
        if (geom == null) {
            return null;
        }
        final CoordinateSequenceDimensionFilter filter =
                new CoordinateSequenceDimensionFilter();
        geom.apply(filter);
        checkMixed(filter);
        // For all 2D geometries, we only translate by (x, y).
        if (filter.is2D()) {
            return AffineTransformation.translationInstance(x, y).transform(geom);
        } else {
            final Geometry clone = geom.copy();
            clone.apply(new ZAffineTransformation(x, y, z));
            return clone;
        }
    }

    /**
     * Throws an exception if the geometry contains coordinates of mixed
     * dimension.
     *
     * @param filter Filter
     */
    private static void checkMixed(CoordinateSequenceDimensionFilter filter) {
        if (filter.isMixed()) {
            throw new IllegalArgumentException(MIXED_DIM_ERROR);
        }
    }
}
