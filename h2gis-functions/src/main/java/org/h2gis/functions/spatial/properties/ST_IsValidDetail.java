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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Returns a valid_detail (valid,reason,location) as an array of objects.
 * If a geometry is valid or not and if not valid, a reason why and a location where.
 * 
 * @author Erwan Bocher
 */
public class ST_IsValidDetail extends DeterministicScalarFunction{

    private static final GeometryFactory GF = new GeometryFactory();    
    
    public ST_IsValidDetail() {
        addProperty(PROP_REMARKS, " Returns a valid_detail as an array of objects\n"
                + " [0] = isvalid,[1] = reason, [2] = error location"
                + "The second argument is optional. It can have the following values (0 or 1)\n"
                + "1 = It will validate inverted shells and exverted holes according the ESRI SDE model.\n"
                + "0 = It will based on the OGC geometry model.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "isValidDetail";
    }
    
    /**
     * Returns a valid_detail as an array of objects
     * [0] = isvalid,[1] = reason, [2] = error location
     * 
     * @param geometry
     * @return 
     */
    public static Object[] isValidDetail(Geometry geometry) {
        return isValidDetail(geometry, 0);
    }
    
    /**
     * Returns a valid_detail as an array of objects
     * [0] = isvalid,[1] = reason, [2] = error location
     * 
     * isValid equals true if the geometry is valid.
     * reason correponds to an error message describing this error.
     * error returns the location of this error (on the {@link Geometry} 
     * containing the error. 
     * 
     * @param geometry
     * @param flag
     * @return 
     */
    public static Object[] isValidDetail(Geometry geometry, int flag) {        
        if (geometry != null) {            
            if (flag == 0) {
                return detail(geometry, false);
            } else if (flag == 1) {
                return detail(geometry, true);
            } else {
                throw new IllegalArgumentException("Supported arguments is 0 or 1.");
            }
        }
        return null;
    }
    
    /**
     *
     * @param geometry
     * @return
     */
    private static Object[] detail(Geometry geometry, boolean flag) {    
        Object[] details = new Object[3];
        IsValidOp validOP = new IsValidOp(geometry);
        validOP.setSelfTouchingRingFormingHoleValid(flag);
        TopologyValidationError error = validOP.getValidationError();
        if (error != null) {
            details[0] = false ;
            details[1] = error.getMessage();
            details[2] = GF.createPoint(error.getCoordinate());
        } else {
            details[0] = true ;
            details[1] = "Valid Geometry";
        }
        return details;
    }

}
