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
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.h2gis.api.DeterministicScalarFunction;

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
