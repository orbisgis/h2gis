/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.function.spatial.mesh.DelaunayData;

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
     * 
     * @param geometry
     * @return 
     */
    public static Double st3darea(Geometry geometry){
        if(geometry==null){
            return null;
        }
        if(geometry.getDimension()<2){
            return 0d;
        }
        return compute3DArea(geometry);
    }

    /**
     * Compute the 3D area of a polygon or a multipolygon
     *
     * @param geometry
     * @return
     */
    private static Double compute3DArea(Geometry geometry) {
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.put(geometry, DelaunayData.MODE.TESSELLATION);
        // Do triangulation
        delaunayData.triangulate();
        return delaunayData.get3DArea();
    }    
}
