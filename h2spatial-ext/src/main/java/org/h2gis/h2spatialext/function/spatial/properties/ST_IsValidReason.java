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
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Returns text stating if a geometry is valid or not an if not valid, a reason why
 * 
 * @author Erwan Bocher
 */
public class ST_IsValidReason extends DeterministicScalarFunction{

    
    public ST_IsValidReason(){
        addProperty(PROP_REMARKS, " Returns text stating if a geometry is"
                + " valid or not and if not valid, a reason why.\n"
                + "The second argument is optional. It can have the following values (0 or 1)\n"
                + "1 = It will validate inverted shells and exverted holes according the ESRI SDE model.\n"
                + "0 = It will based on the OGC geometry model.");
    }
    @Override
    public String getJavaStaticMethod() {
       return "isValidReason";
    }
    
    /**
     * Returns text stating whether a geometry is valid. 
     * If not, returns a reason why.
     * 
     * @param geometry
     * @return 
     */
    public static String isValidReason(Geometry geometry) {
        return isValidReason(geometry, 0);
    }
    
    /**
     * Returns text stating whether a geometry is valid. 
     * If not, returns a reason why.
     * 
     * @param geometry
     * @param flag
     * @return 
     */
    public static String isValidReason(Geometry geometry, int flag) {
        if (geometry != null) {
            if (flag == 0) {
                return validReason(geometry, false);
            } else if (flag == 1) {
                return validReason(geometry, true);
            } else {
                throw new IllegalArgumentException("Supported arguments are 0 or 1.");
            }
        }
        return "Null Geometry";
    }
    
    /**
     *
     * @param geometry
     * @return
     */
    private static String validReason(Geometry geometry, boolean flag) {    
        IsValidOp validOP = new IsValidOp(geometry);
        validOP.setSelfTouchingRingFormingHoleValid(flag);
        TopologyValidationError error = validOP.getValidationError();
        if (error != null) {
            return error.toString();
        } else {
            return "Valid Geometry";
        }
    }
    
}
