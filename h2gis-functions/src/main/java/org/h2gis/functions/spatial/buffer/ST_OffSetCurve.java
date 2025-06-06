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

package org.h2gis.functions.spatial.buffer;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurve;

/**
 * Return an offset line at a given distance and side from an input geometry.
 * @author Erwan Bocher, CNRS
 */
public class ST_OffSetCurve extends DeterministicScalarFunction {

    
    public ST_OffSetCurve() {
        addProperty(PROP_REMARKS, "Return an offset line or collection of lines at a given distance and side from an input geometry.\n"
                + "The optional third parameter can either specify number of segments used\n"
                + " to approximate a quarter circle (integer case, defaults to 8)\n"
                + " or a list of blank-separated key=value pairs (string case) to manage line style parameters :\n"
                + "'quad_segs=8' 'join=round|mitre|bevel' 'mitre_limit=5'");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "offsetCurve";
    }

    /**
     * Return an offset line at a given distance and side from an input geometry
     * @param geometry the geometry
     * @param offset the distance
     * @param parameters the buffer parameters
     * @return {@link Geometry}
     */
    public static Geometry offsetCurve(Geometry geometry, double offset, String parameters) {
        if(geometry == null){
            return null;
        }
        String[] buffParemeters = parameters.split("\\s+");
        BufferParameters bufferParameters = new BufferParameters();
        for (String params : buffParemeters) {
            String[] keyValue = params.split("=");
            if (keyValue[0].equalsIgnoreCase("join")) {
                String param = keyValue[1];
                if (param.equalsIgnoreCase("bevel")) {
                    bufferParameters.setJoinStyle(BufferParameters.JOIN_BEVEL);
                } else if (param.equalsIgnoreCase("mitre") || param.equalsIgnoreCase("miter")) {
                    bufferParameters.setJoinStyle(BufferParameters.JOIN_MITRE);
                } else if (param.equalsIgnoreCase("round")) {
                    bufferParameters.setJoinStyle(BufferParameters.JOIN_ROUND);
                } else {
                    throw new IllegalArgumentException("Supported join values are bevel, mitre, miter or round.");
                }
            } else if (keyValue[0].equalsIgnoreCase("mitre_limit") || keyValue[0].equalsIgnoreCase("miter_limit")) {
                bufferParameters.setMitreLimit(Double.valueOf(keyValue[1]));
            } else if (keyValue[0].equalsIgnoreCase("quad_segs")) {
                bufferParameters.setQuadrantSegments(Integer.valueOf(keyValue[1]));
            } else {
                throw new IllegalArgumentException("Unknown parameters. Please read the documentation.");
            }
        }
        return OffsetCurve.getCurve(geometry, offset, bufferParameters.getQuadrantSegments(),bufferParameters.getJoinStyle(),
                bufferParameters.getMitreLimit());
    }

    /**
     * Return an offset line at a given distance and side from an input geometry
     * without buffer parameters
     * @param geometry the geometry
     * @param offset the distance
     * @return {@link Geometry}
     */
    public static Geometry offsetCurve(Geometry geometry, double offset) {
        return OffsetCurve.getCurve(geometry, offset);
    }
}
