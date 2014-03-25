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

package org.h2gis.h2spatial.internal.function.spatial.operators;

import com.vividsolutions.jts.geom.Geometry;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * ST_Buffer computes a buffer around a Geometry.  Circular arcs are
 * approximated using 8 segments per quadrant. In particular, circles contain
 * 32 line segments.
 *
 * @author Nicolas Fortin
 */
public class ST_Buffer extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Buffer() {
        addProperty(PROP_REMARKS, "Compute a buffer around a Geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "buffer";
    }

    /**
     * @param a Geometry instance.
     * @param distance Buffer width in projection unit
     * @return a buffer around a geometry.
     */
    public static Geometry buffer(Geometry a,Double distance) {
        if(a==null || distance==null) {
            return null;
        }
        return a.buffer(distance);
    }
}
