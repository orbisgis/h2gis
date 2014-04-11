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

import org.h2.value.ValueGeometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * ST_OrderingEquals compares two geometries and t (TRUE) if the geometries are equal 
 * and the coordinates are in the same order; otherwise it returns f (FALSE).
 * 
 * This method implements the SQL/MM specification: SQL-MM 3: 5.1.43 
 * 
 * @author Erwan Bocher
 */
public class ST_OrderingEquals extends DeterministicScalarFunction{

    
    public ST_OrderingEquals(){
        addProperty(PROP_REMARKS, "Returns true if the given geometries represent "
                + "the same geometry and points are in the same directional order.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "orderingEquals";
    }
    
    /**
     * Returns true if the given geometries represent the same 
     * geometry and points are in the same directional order.
     * 
     * @param geomA
     * @param geomB
     * @return 
     */
    public static boolean orderingEquals(ValueGeometry geomA, ValueGeometry geomB){        
        return geomA.equals(geomB);        
    }
}
