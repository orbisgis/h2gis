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

package org.h2gis.functions.spatial.operators;

import java.sql.SQLException;
import java.util.HashSet;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

/**
 * Compute the union of two or more Geometries.
 *
 * @author Nicolas Fortin
 */
public class ST_Union extends DeterministicScalarFunction {

    static HashSet dims = new HashSet();

    /**
     * Default constructor
     */
    public ST_Union() {
        addProperty(PROP_REMARKS, "Compute the union of two or more Geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "union";
    }

    /**
     * @param a Geometry instance.
     * @param b Geometry instance
     * @return union of Geometries a and b
     * @throws java.sql.SQLException
     */
    public static Geometry union(Geometry a,Geometry b) throws SQLException {
        if(a==null || b==null) {
            return null;
        }
        if(a.isEmpty()){
            return a;
        }
        if(b.isEmpty()){
            findDim(a);
            if(dims.size()>1) {
                return OverlayNGRobust.union(a);
            }else if(dims.contains(2)){
                return CoverageUnion.union(a);
            }else{
                return OverlayNGRobust.union(a);
            }
        }
        if(a.getSRID()!=b.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return OverlayNGRobust.overlay(a,b, OverlayNG.UNION);
    }



    /**
     * @param geomList Geometry list
     * @return union of all Geometries in geomList
     */
    public static Geometry union(Geometry geomList) {
        if(geomList==null){
            return null;
        }
        if(geomList.isEmpty()){
            return geomList;
        }
        findDim(geomList);
        if(dims.size()>1) {
            return OverlayNGRobust.union(geomList);
        }else if(dims.contains(2)){
            return CoverageUnion.union(geomList);
        }else{
            return OverlayNGRobust.union(geomList);
        }
    }


    /**
     * Utilities to collect the dimmensions of geometry
     * @param geometry
     */
    public static void findDim(Geometry geometry){
        if (geometry instanceof GeometryCollection) {
            final int nbOfGeometries = geometry.getNumGeometries();
            for (int i = 0; i < nbOfGeometries; i++) {
                findDim(geometry.getGeometryN(i));
            }
        } else {
            dims.add(geometry.getDimension());
        }
    }
}
