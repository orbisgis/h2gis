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
package org.h2gis.h2spatialext.function.spatial.mesh;

import java.sql.SQLException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.evaluator.TriangleQuality;

/**
 * Returns polygons that represent a Delaunay triangulation constructed from a
 * collection of points. Note that the triangulation doesn't compute the
 * intersections between lines; it takes only existing coordinates.
 *
 *
 * @author Erwan Bocher
 */
public class ST_Delaunay extends DeterministicScalarFunction {

    public ST_Delaunay() {
        addProperty(PROP_REMARKS, "Returns polygons that represent a Delaunay Triangulation from a geometry.\n"
                + "Output is a COLLECTION of polygons, for flag=0 (default flag) or a MULTILINESTRING for flag=1\n"
                + "The last argument can be set to improve the quality of the triangulation. The value must be comprised"
                + " between 0 and 1 \n"                
                + "If value > 0.6 the triangle is of acceptable quality.\n"
         );
    }

    @Override
    public String getJavaStaticMethod() {
        return "createDT";
    }

    /**
     * Build a delaunay triangulation based on all coordinates of the geometry
     *
     * @param geometry
     * @return a set of polygons (triangles)
     * @throws SQLException, DelaunayError
     * @throws org.jdelaunay.delaunay.error.DelaunayError
     */
    public static GeometryCollection createDT(Geometry geometry) throws SQLException, DelaunayError {
        return createDT(geometry, 0);
    }
    
    /**
     * Build a delaunay triangulation based on all coordinates of the geometry
     *
     * @param geometry
     * @param flag
     * @return a set of polygons (triangles)
     * @throws SQLException, DelaunayError
     * @throws org.jdelaunay.delaunay.error.DelaunayError
     */
    public static GeometryCollection createDT(Geometry geometry, int flag) throws SQLException, DelaunayError {
        return createDT(geometry,  flag, -1);
    }
   

    /**
     * Build a delaunay triangulation based on all coordinates of the geometry
     *
     * @param geometry
     * @param flag
     * @param qualityRefinement
     * @return Output is a COLLECTION of polygons (for flag=0) or a
     * MULTILINESTRING (for flag=1)
     * @throws SQLException, DelaunayError
     * @throws org.jdelaunay.delaunay.error.DelaunayError
     */
    public static GeometryCollection createDT(Geometry geometry,  int flag,double qualityRefinement) throws SQLException, DelaunayError {
        if (geometry != null) {

        }
        return null;
    }


    /**
     * Compute a delaunay triangulation
     *
     * @param geometry
     * @return
     * @throws DelaunayError
     */
    private static ConstrainedMesh buildDelaunay(Geometry geometry, double qualityRefinement) throws DelaunayError, SQLException {
        ConstrainedMesh mesh = new ConstrainedMesh();
        mesh.setVerbose(true);
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.put(geometry);
        mesh.setPoints(delaunayData.getDelaunayPoints());
        mesh.processDelaunay();
        if(qualityRefinement!=-1){
            if(qualityRefinement>=0 && qualityRefinement<1){
            mesh.refineMesh(qualityRefinement,new TriangleQuality());
            }
            else{
                throw new SQLException("The quality value must be comprised between 0 and 1");
            }
        }
        return mesh;
    }
}
