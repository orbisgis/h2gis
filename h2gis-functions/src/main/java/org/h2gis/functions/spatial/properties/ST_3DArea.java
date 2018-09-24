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

package org.h2gis.functions.spatial.properties;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.mesh.DelaunayData;

/**
 * Compute the 3D area of a polygon or a multiolygon.
 * 
 * @author Erwan Bocher
 */
public class ST_3DArea extends DeterministicScalarFunction{

    public ST_3DArea(){
        addProperty(PROP_REMARKS, "Compute the 3D area of a polygon or a multipolygon derived from a 3D triangular decomposition.\n"
                + "Distance units are those of the geometry spatial reference system.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "st3darea";
    }
    
    /**
     * Compute the 3D area of a polygon a geometrycollection that contains
     * polygons
     *
     * @param geometry
     * @return
     */
    public static Double st3darea(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        double area = 0;
        for (int idPoly = 0; idPoly < geometry.getNumGeometries(); idPoly++) {
            Geometry subGeom = geometry.getGeometryN(idPoly);
            if (subGeom instanceof Polygon) {
                area += compute3DArea((Polygon) subGeom);
            }
        }
        return area;
    }

    /**
     * Compute the 3D area of a polygon
     *
     * @param geometry
     * @return
     */
    private static Double compute3DArea(Polygon geometry) {
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.put(geometry, DelaunayData.MODE.TESSELLATION);
        // Do triangulation
        delaunayData.triangulate();
        return delaunayData.get3DArea();
    }    
}
