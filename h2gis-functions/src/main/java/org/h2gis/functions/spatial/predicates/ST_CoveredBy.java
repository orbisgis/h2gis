/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.predicates;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

import java.sql.SQLException;

/**
 * ST_CoveredBy returns true if no point in geometry B is outside geometry A.
 *
 * @author Erwan Bocher
 */
public class ST_CoveredBy extends DeterministicScalarFunction {

    public ST_CoveredBy() {
        addProperty(PROP_REMARKS, "Returns true if this geomA is covered by geomB according the definitions : \n" +
                "Every point of this geometry is a point of the other geometry.\n" +
                "The DE-9IM Intersection Matrix for the two geometries matches\n" +
                " at least one of the following patterns:\n" +
                " [T*F**F***]\n" +
                " [*TF**F***]\n" +
                " [**FT*F***]\n" +
                " [**F*TF***]\n");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Returns true if this geomA is covered by geomB
     *
     * @param geomA Geometry A
     * @param geomB Geometry B
     * @return if this geomA is covered by geomB
     */
    public static Boolean execute(Geometry geomA, Geometry geomB) throws SQLException {
        if(geomA == null||geomB == null){
            return null;
        }
        if(geomA.isEmpty() || geomB.isEmpty()){
            return false;
        }
        
        if(geomA.getSRID()!=geomB.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return geomA.coveredBy(geomB);
    }
}
