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
package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Construct a geometry collection from a geometry
 * @author Erwan Bocher, CNRS
 */
public class ST_Multi extends DeterministicScalarFunction {


    public ST_Multi() {
        addProperty(PROP_REMARKS, "Returns the geometry as a geometry collection. \n" +
                "If the geometry is already a collection, it is returned unchanged.");
    }


    @Override
    public String getJavaStaticMethod() {
        return "toCollection";
    }

    /**
     * Construct a geometry collection from a geometry
     * @param geometry {@link Geometry}
     * @return a {@link org.locationtech.jts.geom.GeometryCollection}
     */
    public static  Geometry toCollection(Geometry geometry){
        if(geometry==null){
            return null;
        }
        else if(geometry instanceof Point){
            return geometry.getFactory().createMultiPoint(new Point[]{(Point) geometry});
        }
        else if(geometry instanceof LineString){
            return geometry.getFactory().createMultiLineString(new LineString[]{(LineString) geometry});
        }
        else if(geometry instanceof Polygon){
            return geometry.getFactory().createMultiPolygon(new Polygon[]{(Polygon) geometry});
        }
        else{
            return geometry;
        }
    }
}
