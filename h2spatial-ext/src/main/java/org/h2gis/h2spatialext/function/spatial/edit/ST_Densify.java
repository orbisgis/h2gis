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
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Densifies a geometry using the given distance tolerance.
 *
 * @see com.vividsolutions.jts.densify.Densifier
 * @author Erwan Bocher
 */
public class ST_Densify extends DeterministicScalarFunction {

    public ST_Densify() {
        addProperty(PROP_REMARKS, "Densifies a geometry using the given distance tolerance");
    }

    @Override
    public String getJavaStaticMethod() {
        return "densify";
    }

    /**
     * Densify a geometry using the given distance tolerance.
     *
     * @param geometry  Geometry
     * @param tolerance Distance tolerance
     * @return Densified geometry
     */
    public static Geometry densify(Geometry geometry, double tolerance) {
        return Densifier.densify(geometry, tolerance);
    }
}
