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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.buffer.BufferParameters;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.create.ST_RingBuffer;

/**
 *Compute a ring buffer on one side
 * 
 * @author Erwan Bocher
 */
public class ST_RingSideBuffer extends DeterministicScalarFunction{
    
    
    public ST_RingSideBuffer(){
        addProperty(PROP_REMARKS, "Return a ring buffer at a given distance on only one side of each input lines of the geometry.\n"
                + "Avalaible arguments are :\n"
                + " (1) the geometry, (2) the size of each ring, "
                + " (3) the number of rings, (4) optional - \n"
                + "a list of blank-separated key=value pairs (string case) iso used t manage line style parameters.\n "
                + " The end cap style for single-sided buffers is always ignored, and forced to the equivalent of flat.\n"
                + "Please read the ST_Buffer documention.\n"
                + " (5) optional - createHole True if you want to keep only difference between buffers Default is true.\n"
                + "Note : Holes are not supported by this function.");
   
    }

    @Override
    public String getJavaStaticMethod() {
        return "ringSideBuffer";  
    }
    
    /**
     * Compute a ring buffer on one side of the geometry
     * @param geom
     * @param bufferSize
     * @param numBuffer
     * @return 
     * @throws java.sql.SQLException 
     */
    public static Geometry ringSideBuffer(Geometry geom, double bufferSize, int numBuffer) throws SQLException {
        return ringSideBuffer(geom, bufferSize, numBuffer, "endcap=flat");
    }

    /**
     *
     * @param geom
     * @param bufferDistance
     * @param numBuffer
     * @param parameters
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry ringSideBuffer(Geometry geom, double bufferDistance,
                                      int numBuffer, String parameters) throws SQLException {
        return ringSideBuffer(geom, bufferDistance, numBuffer, parameters, true);
    }

    /**
     * Compute a ring buffer on one side of the geometry
     * @param geom
     * @param bufferDistance
     * @param numBuffer
     * @param parameters
     * @param doDifference
     * @throws SQLException 
     * @return 
     */
    public static Geometry ringSideBuffer(Geometry geom, double bufferDistance,
            int numBuffer, String parameters, boolean doDifference) throws SQLException {
        if(geom==null){
            return null;
        }
        if (geom.getNumGeometries() > 1) {
            throw new SQLException("This function supports only single geometry : point, linestring or polygon.");
        } else {
        String[] buffParemeters = parameters.split("\\s+");
        BufferParameters bufferParameters = new BufferParameters();        
        bufferParameters.setSingleSided(true);
        for (String params : buffParemeters) {
            String[] keyValue = params.split("=");
             if (keyValue[0].equalsIgnoreCase("endcap")) {
                 String param = keyValue[1];
                 if (param.equalsIgnoreCase("round")) {
                     bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
                 } else if (param.equalsIgnoreCase("square")) {
                     bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
                 } else if (param.equalsIgnoreCase("flat")) {
                     bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
                 } else {
                     throw new IllegalArgumentException("Supported join values are round or square.");
                 }
            } else if (keyValue[0].equalsIgnoreCase("join")) {
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
        
            if (bufferDistance > 0) {
                return ST_RingBuffer.computePositiveRingBuffer(geom, bufferDistance, numBuffer, bufferParameters, doDifference);
            } else if (bufferDistance < 0) {
                if (geom instanceof Point) {
                    throw new SQLException("Cannot compute a negative ring side buffer on a point.");
                } else {
                    return ST_RingBuffer.computeNegativeRingBuffer(geom, bufferDistance, numBuffer, bufferParameters, doDifference);
                }
            } else {
                return geom;
            }
        }
    }    
}
