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
package org.h2gis.functions.spatial.edit;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Erwan Bocher, CNRS, 2023
 */
public class ST_ForcePolygonCW extends DeterministicScalarFunction {


    public ST_ForcePolygonCW() {
        addProperty(PROP_REMARKS, "Forces (Multi)Polygons to use a clockwise orientation for their exterior ring, and a counter-clockwise orientation for their interior rings.\n" +
                " Non-polygonal geometries are returned unchanged.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Forces (Multi)Polygons to use a clockwise orientation for their exterior ring, and a counter-clockwise orientation
     * for their interior rings.
     * Non-polygonal geometries are returned unchanged.
     * @param geom {@link Geometry}
     * @return Geometry
     */
    public static Geometry execute(Geometry geom) throws SQLException {
        if (geom != null) {
            final List<Geometry> geometries = new LinkedList<Geometry>();
            forcePolygonCW(geom, geometries);
            return geom.getFactory().buildGeometry(geometries);
        }
        return null;
    }


    private static void forcePolygonCW(final Geometry geometry,
                                          final List<Geometry> geometries) throws SQLException {
        if (geometry instanceof Polygon) {
            forcePolygonCW((Polygon) geometry, geometries);
        } else if (geometry instanceof GeometryCollection) {
            forcePolygonCW((GeometryCollection) geometry, geometries);
        }
        else{
            geometries.add(geometry);
        }
    }

    private static void forcePolygonCW(final Polygon polygon,
                                          final List<Geometry> geometries) {
        LinearRing ring = polygon.getExteriorRing();
        if(Orientation.isCCW(ring.getCoordinateSequence())){
            ring = ring.reverse();
        }
        int nb = polygon.getNumInteriorRing();
        List<LinearRing> holes = new ArrayList<>();
        for (int i = 0; i < nb; i++) {
            LinearRing hole = polygon.getInteriorRingN(i);
            if(!Orientation.isCCW(hole.getCoordinateSequence())){
                holes.add(hole.reverse());
            }else {
                holes.add(hole);
            }
        }
        geometries.add(polygon.getFactory().createPolygon(ring, holes.toArray(new LinearRing[0])));
    }

    private static void forcePolygonCW(final GeometryCollection geometryCollection,
                                          final List<Geometry> geometries) throws SQLException {
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            forcePolygonCW(geometryCollection.getGeometryN(i), geometries);
        }
    }

}
