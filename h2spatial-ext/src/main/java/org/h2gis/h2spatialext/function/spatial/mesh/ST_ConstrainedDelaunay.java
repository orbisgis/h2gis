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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import static org.h2gis.h2spatialapi.Function.PROP_REMARKS;
import org.jdelaunay.delaunay.ConstrainedMesh;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DEdge;

/**
 * Returns polygons or lines that represent a Delaunay triangulation constructed
 * from a geometry. Note that the triangulation computes
 * the intersections between lines.
 *
 * @author Erwan Bocher
 */
public class ST_ConstrainedDelaunay extends DeterministicScalarFunction {

 
    public ST_ConstrainedDelaunay() {
        addProperty(PROP_REMARKS, "Returns polygons that represent a Constrained Delaunay Triangulation from a geometry\n."
                + "Output is a COLLECTION of polygons, for flag=0 (default flag) or a MULTILINESTRING for flag=1");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createCDT";
    }

    /**
     * Build a delaunay constrained delaunay triangulation based on a
     * geometry (point, line, polygon)
     *
     * @param geometry
     * @return a set of polygons (triangles)
     * @throws SQLException,DelaunayError
     */
    public static GeometryCollection createCDT(Geometry geometry) throws SQLException, DelaunayError {        
        return createCDT(geometry, 0);
    }

    /**
     * Build a delaunay constrained delaunay triangulation based on a
     * geometry (point, line, polygon)
     *
     * @param geometry
     * @param flag
     * @return Output is a COLLECTION of polygons (for flag=0) or a MULTILINESTRING (for flag=1)
     * @throws SQLException, DelaunayError
     */
    public static GeometryCollection createCDT(Geometry geometry, int flag) throws SQLException, DelaunayError {
        if (flag == 0) {
            return DelaunayTools.toMultiPolygon(buildDelaunay(geometry).getTriangleList());
        } else if (flag == 1) {
            return DelaunayTools.toMultiLineString(buildDelaunay(geometry).getEdges());
        } else {
            throw new SQLException("Only flag 0 or 1 is supported.");
        }
    }

    /**
     * Compute a constrained delaunay triangulation
     * @param geometry
     * @return
     * @throws DelaunayError 
     */
    private static ConstrainedMesh buildDelaunay(Geometry geometry) throws DelaunayError {
        ConstrainedMesh mesh = new ConstrainedMesh();
        mesh.setVerbose(true);
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.put(geometry, true);
        //We actually fill the mesh
        mesh.setPoints(delaunayData.getDelaunayPoints());
        ArrayList<DEdge> edges = delaunayData.getDelaunayEdges();
        //We have filled the input of our mesh. We can close our source.
        Collections.sort(edges);
        mesh.setConstraintEdges(edges);
        //If needed, we use the intersection algorithm
        mesh.forceConstraintIntegrity();
        //we process delaunay
        mesh.processDelaunay();
        return mesh;
    }
}
