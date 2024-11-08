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

package org.h2gis.functions.spatial.topology;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.Collection;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Polygonize extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    
    public ST_Polygonize(){
        addProperty(PROP_REMARKS, "Polygonizes a set of Geometry which contain linework "
                + "that represents the edges of a planar graph");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Creates a GeometryCollection containing possible polygons formed 
     * from the constituent linework of a set of geometries.
     * 
     * @param geometry {@link Geometry}
     * @return new polygons
     */
    public static Geometry execute(Geometry geometry) {
        if(geometry == null){
            return null;
        }
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(geometry);
        Collection pols = polygonizer.getPolygons();
        if(pols.isEmpty()){
            return null;
        }
        return geometry.getFactory().createMultiPolygon(GeometryFactory.toPolygonArray(pols));
    }
}
