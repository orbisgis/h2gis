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
 * Return true if the two Geometries are disjoint.
 *
 * @author Nicolas Fortin
 */
public class ST_Disjoint extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Disjoint() {
        addProperty(PROP_REMARKS, "Return true if the two Geometries are disjoint");
    }

    @Override
    public String getJavaStaticMethod() {
        return "geomDisjoint";
    }

    /**
     * Return true if the two Geometries are disjoint
     *
     * @param a Geometry Geometry.
     * @param b Geometry instance
     * @return true if the two Geometries are disjoint
     */
    public static Boolean geomDisjoint(Geometry a, Geometry b) throws SQLException {
        if(a==null || b==null) {
            return null;
        }
        if(a.isEmpty() || b.isEmpty()){
            return false;
        }
        if(a.getSRID()!=b.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return a.disjoint(b);
    }
}
