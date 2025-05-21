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

import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueNull;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;

/**
 * Compute the amount of memory space (in bytes) for the input value
 */
public class ST_MemSize extends DeterministicScalarFunction {


    public ST_MemSize(){
        addProperty(PROP_REMARKS, "Returns the amount of memory space (in bytes) the geometry takes.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "memsize";
    }

    /**
     * @param value Geometry instance or null
     * @return  the amount of memory space (in bytes)
     */
    public static Long memsize(Value value) throws SQLException {
        if(value== ValueNull.INSTANCE ){
            return null;
        }else if(value instanceof ValueGeometry) {
            return value.octetLength();
        }
        throw new SQLException("ST_MemSize only support geometry value ");
    }
}
