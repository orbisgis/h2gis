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
package org.h2gis.h2spatialext.function.spatial.affine_transformations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;

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
        checkMixed(geom);
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
        checkMixed(geom);
        // For all 2D geometries, we only translate by (x, y).
        if (CoordinateUtils.is2D(geom.getCoordinates())) {
            return AffineTransformation.translationInstance(x, y).transform(geom);
        } else {
            geom.apply(new ZAffineTransformation(x, y, z));
            return geom;
        }
    }

    /**
     * Throws an exception if the geometry contains coordinates of mixed
     * dimension.
     *
     * @param geom Geometry
     */
    private static void checkMixed(Geometry geom) {
        if (CoordinateUtils.containsCoordsOfMixedDimension(geom.getCoordinates())) {
            throw new IllegalArgumentException(MIXED_DIM_ERROR);
        }
    }
}
