/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.coverage;


import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.coverage.CoverageUnion;
import org.locationtech.jts.geom.Geometry;

/**
 * @author  Erwan Bocher, CNRS
 */
public class ST_CoverageUnion extends DeterministicScalarFunction {


    public ST_CoverageUnion() {
        addProperty(PROP_REMARKS, "A function which unions a set of polygons forming a polygonal coverage. \n" +
                "The result is a polygonal geometry covering the same area as the coverage.\n" +
                "It uses the CoverageUnion algorithm");
    }


    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Run the coverage union
     * @param geoms the input array of geometries
     * @return the union of the input array of geometries
     */
    public static Geometry execute(Geometry[] geoms){
        if(geoms==null){
            return null;
        }
        return CoverageUnion.union(geoms);
    }
}
