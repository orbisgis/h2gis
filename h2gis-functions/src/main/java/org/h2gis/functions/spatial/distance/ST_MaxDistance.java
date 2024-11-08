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

package org.h2gis.functions.spatial.distance;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * Compute the maximum distance between two geometries.
 * 
 * @author Erwan Bocher
 */
public class ST_MaxDistance extends DeterministicScalarFunction{
    
    
    public ST_MaxDistance() {
        addProperty(PROP_REMARKS, "Returns the 2-dimensional largest distance between two geometries in projected units.\n"
                + "If the geometry 1 and geometry 2 is the same geometry the function will \n "
                + "return the distance between the two vertices most far from each other in that geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "maxDistance";
    }

    /**
     * Return the maximum distance
     *
     * @param geomA {@link Geometry} A
     * @param geomB {@link Geometry} B
     * @return max distance
     */
    public static Double maxDistance(Geometry geomA, Geometry geomB) throws SQLException {
        if(geomA ==null || geomB==null){
            return null;
        }
        if(geomA.isEmpty()||geomB.isEmpty()){
            return null;
        }
        if(geomA.getSRID()!=geomB.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return new MaxDistanceOp(geomA, geomB).getDistance();
    }
    
}
