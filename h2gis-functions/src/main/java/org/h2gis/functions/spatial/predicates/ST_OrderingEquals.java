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

package org.h2gis.functions.spatial.predicates;

import java.sql.SQLException;
import org.h2.value.ValueGeometry;
import org.h2gis.api.DeterministicScalarFunction;

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
     * @throws java.sql.SQLException 
     */
    public static boolean orderingEquals(ValueGeometry geomA, ValueGeometry geomB) throws SQLException{ 
        if(geomA.getSRID()!=geomB.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return geomA.equals(geomB);        
    }
}
