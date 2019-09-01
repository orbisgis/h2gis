/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.crs;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.SFSUtilities;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Find the UTM SRID code 
 * @author Erwan Bocher
 */
public class ST_FindUTMSRID extends DeterministicScalarFunction {
    
    
    public ST_FindUTMSRID(){
        addProperty(PROP_REMARKS, "ST_FindUTMSRID takes a geometry and "
                + "find the UTM SRID code from its centroid.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "findSRID";
     }
    
    /**
     * Find UTM SRID from a geometry
     * @param connection
     * @param geometry
     * @return
     * @throws java.sql.SQLException 
     */
    public static int findSRID(Connection connection, Geometry geometry) throws SQLException {        
        if(geometry==null){
            return -1;
        }        
        Point coord = geometry.getCentroid();
        return SFSUtilities.getSRID(connection,(float)coord.getY(), (float)coord.getX());
    }
    
}
