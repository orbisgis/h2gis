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

package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Removes any holes from a polygon or multipolygon
 *
 * @author Erwan Bocher
 */
public class ST_RemoveHoles extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    public ST_RemoveHoles() {
        addProperty(PROP_REMARKS, "Remove all holes in a polygon or a multipolygon. "
                + "\n If the geometry doesn't contain any hole return the input geometry."
                + "\n If the input geometry is not a polygon or multipolygon return null.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removeHoles";
    }

    /**
     * Remove any holes from the geometry. If the geometry doesn't contain any
     * holes, return it unchanged.
     *
     * @param geometry Geometry
     * @return Geometry with no holes *
     */
    public static Geometry removeHoles(Geometry geometry) {
        if(geometry == null){
            return null;
        }
        if (geometry instanceof Polygon) {
            return removeHolesPolygon((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            return removeHolesMultiPolygon((MultiPolygon) geometry);
        } else if (geometry instanceof GeometryCollection) {
            Geometry[] geometries = new Geometry[geometry.getNumGeometries()];
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry geom = geometry.getGeometryN(i);
                if (geometry instanceof Polygon) {
                    geometries[i] = removeHolesPolygon((Polygon) geom);
                } else if (geometry instanceof MultiPolygon) {
                    geometries[i] = removeHolesMultiPolygon((MultiPolygon) geom);
                } else {
                    geometries[i] = geom;
                }
            }
            return FACTORY.createGeometryCollection(geometries);
        }
        return null;
    }

    /**
     * Create a new multiPolygon without hole.
     *
     * @param multiPolygon
     * @return
     */
    public static MultiPolygon removeHolesMultiPolygon(MultiPolygon multiPolygon) {
        int num = multiPolygon.getNumGeometries();
        Polygon[] polygons = new Polygon[num];
        for (int i = 0; i < num; i++) {
            polygons[i] = removeHolesPolygon((Polygon) multiPolygon.getGeometryN(i));
        }
        return multiPolygon.getFactory().createMultiPolygon(polygons);
    }

    /**
     * Create a new polygon without hole.
     *
     * @param polygon
     * @return
     */
    public static Polygon removeHolesPolygon(Polygon polygon) {
        return new Polygon((LinearRing) polygon.getExteriorRing(), null, polygon.getFactory());
    }
}
