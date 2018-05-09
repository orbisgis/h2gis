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
package org.h2gis.functions.spatial.clean;

import org.locationtech.jts.geom.Geometry;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Function to make a geometry valid.
 * @author Michaël Michaud
 * @author Erwan Bocher
 */
public class ST_MakeValid extends DeterministicScalarFunction{

    public static final String REMARKS = "Repair an invalid geometry.\n"
            + " If preserveGeomDim is true, makeValid will remove degenerated geometries from\n"
            + " the result, i.e geometries which dimension is lower than the input geometry\n"
            + "A multi-geometry will always produce a multi-geometry (eventually empty or made\n"
            + " of a single component).\n"
            + "A simple geometry may produce a multi-geometry (ex. polygon with self-intersection\n"
            + " will generally produce a multi-polygon). In this case, it is up to the client to\n"
            + " explode multi-geometries if he needs to.\n"
            + "If preserveGeomDim is off, it is up to the client to filter degenerate geometries.\n"
            + " WARNING : for geometries of dimension 1 (linear), duplicate coordinates are\n"
            + " preserved as much as possible. For geometries of dimension 2 (areal), duplicate\n"
            + " coordinates are generally removed due to the use of overlay operations.";
    
    public ST_MakeValid(){
        addProperty(PROP_REMARKS, REMARKS);
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "validGeom";
    }
    
    
    public static Geometry validGeom(Geometry geometry) throws SQLException{
        return validGeom(geometry, true, true, true);
    }
     
    
    public static Geometry validGeom(Geometry geometry, boolean preserveGeomDim) throws SQLException{
        return validGeom(geometry, preserveGeomDim, true, true);
    }
    
    public static Geometry validGeom(Geometry geometry, boolean preserveGeomDim,  boolean preserveDuplicateCoord) throws SQLException{
        return validGeom(geometry, preserveGeomDim, preserveDuplicateCoord, true);
    }
    
    
    /**
     *
     * @param geometry
     * @param preserveGeomDim
     * @param preserveDuplicateCoord
     * @param preserveCoordDim
     * @return
     * @throws SQLException
     */
    public static Geometry validGeom(Geometry geometry, boolean preserveGeomDim, boolean preserveDuplicateCoord, boolean preserveCoordDim) throws SQLException {
        if (geometry == null) {
            return null;
        }

        if (geometry.isEmpty()) {
            return geometry;
        }

        MakeValidOp op = new MakeValidOp();
        op.setPreserveGeomDim(preserveGeomDim);
        op.setPreserveDuplicateCoord(preserveDuplicateCoord);
        op.setPreserveCoordDim(preserveCoordDim);
        return op.makeValid(geometry);
    }
}
