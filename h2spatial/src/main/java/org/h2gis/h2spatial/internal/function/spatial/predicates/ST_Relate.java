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

package org.h2gis.h2spatial.internal.function.spatial.predicates;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;


/**
 * This function is used to compute the relation between two geometries, as
 * described in the SFS specification. It can be used in two ways. First, if it is given two geometries,it returns a
 * 9-character String representation of the 2 geometries IntersectionMatrix. If it is given two geometries and a
 * IntersectionMatrix representation, it will return a boolean : true it the two geometries' IntersectionMatrix match
 * the given one, false otherwise.
 * @author Nicolas Fortin
 */
public class ST_Relate extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Relate() {
        addProperty(PROP_REMARKS, " This function is used to compute the relation between two geometries," +
                " as described in the SFS specification. It can be used in two ways. First, if it is given two geometries," +
                "it returns a 9-character String representation of the 2 geometries IntersectionMatrix." +
                " If it is given two geometries and an IntersectionMatrix representation, it will return a boolean :" +
                " true it the two geometries' IntersectionMatrix match the given one, false otherwise.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "relate";
    }

    /**
     * @param a Geometry Geometry.
     * @param b Geometry instance
     * @return 9-character String representation of the 2 geometries IntersectionMatrix
     */
    public static String relate(Geometry a,Geometry b) {
        if(a==null || b==null) {
            return null;
        }
        return a.relate(b).toString();
    }

    /**
     * @param a Geometry instance
     * @param b Geometry instance
     * @param iMatrix IntersectionMatrix representation
     * @return true it the two geometries' IntersectionMatrix match the given one, false otherwise.
     */
    public static Boolean relate(Geometry a,Geometry b,String iMatrix) {
        if(a==null || b==null || iMatrix==null) {
            return null;
        }
        return a.relate(b,iMatrix);
    }
}
