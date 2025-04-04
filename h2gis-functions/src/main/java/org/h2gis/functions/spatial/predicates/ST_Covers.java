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

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * ST_Covers returns true if no point in geometry B is outside geometry A.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_Covers extends DeterministicScalarFunction {

    public ST_Covers() {
        addProperty(PROP_REMARKS, "Returns true if no point in geometry B is outside geometry A.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "covers";
    }

    /**
     * Returns true if no point in geometry B is outside geometry A.
     *
     * @param geomA Geometry A
     * @param geomB Geometry B
     * @return True if no point in geometry B is outside geometry A
     */
    public static Boolean covers(Geometry geomA, Geometry geomB) throws SQLException {
        if(geomA == null||geomB == null){
            return null;
        }
        if(geomA.isEmpty() || geomB.isEmpty()){
            return false;
        }
        
        if(geomA.getSRID()!=geomB.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return geomA.covers(geomB);
    }
}
