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

package org.h2gis.functions.spatial.properties;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

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
     * @param geometry geometry to validate
     * @return array with all valid details
     */
    public static String[] isValidDetail(Geometry geometry) {
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
     * @param geometry {@link Geometry}
     * @param flag  [0] = isvalid,[1] = reason, [2] = error location
     * @return array of valid details
     */
    public static String[] isValidDetail(Geometry geometry, int flag) {
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
     * TODO : change the return of this method when H2 will support row values
     * @param geometry {@link Geometry}
     * @return array of valid details
     */
    private static String[] detail(Geometry geometry, boolean flag) {
        String[] details = new String[3];
        IsValidOp validOP = new IsValidOp(geometry);
        validOP.setSelfTouchingRingFormingHoleValid(flag);
        TopologyValidationError error = validOP.getValidationError();
        if (error != null) {
            details[0] = "false" ;
            details[1] = error.getMessage();
            details[2] = GF.createPoint(error.getCoordinate()).toString();
        } else {
            details[0] = "true" ;
            details[1] = "Valid Geometry";
        }
        return details;
    }

}
