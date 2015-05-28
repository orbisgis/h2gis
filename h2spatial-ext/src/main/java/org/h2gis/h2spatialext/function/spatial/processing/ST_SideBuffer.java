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
package org.h2gis.h2spatialext.function.spatial.processing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Compute a single buffer on one side.
 * 
 * 
 * @author Erwan Bocher
 */
public class ST_SideBuffer extends DeterministicScalarFunction{

    public ST_SideBuffer() {
        addProperty(PROP_REMARKS, "Return a buffer at a given distance on only one side of each input lines of the geometry.\n"
                + "The optional third parameter can either specify number of segments used\n"
                + " to approximate a quarter circle (integer case, defaults to 8)\n"
                + " or a list of blank-separated key=value pairs (string case) to manage line style parameters :\n"
                + "'quad_segs=8' 'join=round|mitre|bevel' 'mitre_limit=5'\n"
                + "The end cap style for single-sided buffers is always ignored, and forced to the equivalent of flat.");
    }

    
    
    @Override
    public String getJavaStaticMethod() {
        return "singleSideBuffer";
    }
    
    /**
     * Compute a single side buffer with default parameters
     * @param geometry
     * @param distance
     * @return 
     */
    public static Geometry singleSideBuffer(Geometry geometry, double distance){
        return computeSingleSideBuffer(geometry, distance, new BufferParameters());
    }
    
    /**
     * Compute a single side buffer with join and mitre parameters
     * Note :
     * The End Cap Style for single-sided buffers is always ignored, and forced to the equivalent of flat.   
     * @param geometry
     * @param distance
     * @param parameters
     * @return 
     */
    public static Geometry singleSideBuffer(Geometry geometry, double distance, String parameters){
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
        return computeSingleSideBuffer(geometry, distance, bufferParameters);
    }
    
    /**
     * Compute the buffer
     * @param geometry
     * @param distance
     * @param bufferParameters
     * @return 
     */
    private static Geometry computeSingleSideBuffer(Geometry geometry, double distance, BufferParameters bufferParameters){
        bufferParameters.setSingleSided(true);
        return BufferOp.bufferOp(geometry, distance, bufferParameters);
    }
}
