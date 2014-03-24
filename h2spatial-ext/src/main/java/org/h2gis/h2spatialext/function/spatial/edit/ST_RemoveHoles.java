/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
