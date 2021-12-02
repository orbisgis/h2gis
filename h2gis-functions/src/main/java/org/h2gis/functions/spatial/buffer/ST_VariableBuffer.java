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

package org.h2gis.functions.spatial.buffer;

import org.h2.value.Value;
import org.h2.value.ValueInteger;
import org.h2.value.ValueVarchar;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.VariableBuffer;

import java.sql.SQLException;

/**
 * ST_VariableBuffer computes a buffer around a Geometry with a start and end distance
 *
 * @author Erwan Bocher
 */
public class ST_VariableBuffer extends DeterministicScalarFunction {


    /**
     * Default constructor
     */
    public ST_VariableBuffer() {
        addProperty(PROP_REMARKS, "Create a buffer polygon along a line with the buffer distance interpolated\n" +
                " between a start distance and an end distance.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "buffer";
    }

    /**
     * @param geom Geometry instance
     * @param startDistance start distance width in projection unit
     * @param endDistance end distance width in projection unit
     * @return geom buffer around geom geometry.
     */
    public static Geometry buffer(Geometry geom,Double startDistance, Double endDistance) throws SQLException {
        if(geom==null || startDistance==null||endDistance==null) {
            return null;
        }
        if(geom instanceof LineString){
            return VariableBuffer.buffer(geom, startDistance,endDistance);
        }
        throw new SQLException("ST_VariableBuffer supports only LineString");
    }
}
