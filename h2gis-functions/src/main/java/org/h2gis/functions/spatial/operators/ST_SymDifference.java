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

package org.h2gis.functions.spatial.operators;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * Compute the symmetric difference between two Geometries.
 * @author Nicolas Fortin
 */
public class ST_SymDifference extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_SymDifference() {
        addProperty(PROP_REMARKS, "Compute the symmetric difference between two Geometries");
    }

    @Override
    public String getJavaStaticMethod() {
        return "symDifference";
    }

    /**
     * @param a Geometry instance.
     * @param b Geometry instance
     * @return the symmetric difference between two geometries
     * @throws java.sql.SQLException
     */
    public static Geometry symDifference(Geometry a,Geometry b) throws SQLException {
        if(a==null || b==null) {
            return null;
        }        
        if(a.getSRID()!=b.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return a.symDifference(b);
    }
}
