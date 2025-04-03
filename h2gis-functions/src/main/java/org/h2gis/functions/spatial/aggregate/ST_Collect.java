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

package org.h2gis.functions.spatial.aggregate;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Construct an array of Geometries.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS
 */
public class ST_Collect extends DeterministicScalarFunction {
   
    static GeometryFactory GF = new GeometryFactory();
    public ST_Collect() {
        addProperty(PROP_REMARKS, "Collects geometries into a geometry collection. " +
                "\nThe result is either a Multi* or a GeometryCollection, depending on whether the input geometries have the same or different types (homogeneous or heterogeneous). \n" +
                "The input geometries are left unchanged within the collection.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "collect";
    }

    /**
     * Create a geometry from an array of geometries
     * @param geometries array of geometries
     * @return org.locationtech.jts.geom.Geometry
     */
    public static Geometry collect(Geometry[] geometries){
        if(geometries==null){
            return null;
        }
        return GF.buildGeometry(Arrays.asList(geometries));
    }

    /**
     * Create a geometry from an array of geometries
     * @param geomA input geometry
     * @param geomB input geometry
     * @return org.locationtech.jts.geom.Geometry
     */
    public static Geometry collect(Geometry geomA, Geometry geomB){
        if(geomA==null && geomB==null){
           return null;
        }
        ArrayList geoms = new ArrayList<>();
        if(geomA!=null){
            geoms.add(geomA);
        }
        if(geomB!=null){
            geoms.add(geomB);
        }
        return GF.buildGeometry(geoms);
    }
}
