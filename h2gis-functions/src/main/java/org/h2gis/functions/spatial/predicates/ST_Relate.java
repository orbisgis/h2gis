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
     * @param a Geometry A.
     * @param b Geometry B
     * @return 9-character String representation of the 2 geometries IntersectionMatrix
     */
    public static String relate(Geometry a,Geometry b) throws SQLException {
        if(a==null || b==null) {
            return null;
        }
        if(a.isEmpty() || b.isEmpty()){
            return null;
        }
        if(a.getSRID()!=b.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
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
