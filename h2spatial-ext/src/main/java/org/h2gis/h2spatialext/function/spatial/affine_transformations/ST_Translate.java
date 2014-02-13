/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
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
package org.h2gis.h2spatialext.function.spatial.affine_transformations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Translate extends DeterministicScalarFunction {

    public ST_Translate() {
        addProperty(PROP_REMARKS, "Translates the geometry to a new location using the numeric parameters as X and Y offsets.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "translate";
    }

    /**
     * Translates the geometry to a new location using the numeric parameters as
     * X and Y offsets.
     *
     * @param geom
     * @param x
     * @param y
     * @return
     */
    public static Geometry translate(Geometry geom, double x, double y) {
        if (geom != null) {
            return AffineTransformation.translationInstance(x, y).transform(geom);
        } else {
            return null;
        }
    }
}
