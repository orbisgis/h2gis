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
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueString;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * ST_Buffer computes a buffer around a Geometry.  Circular arcs are
 * approximated using 8 segments per quadrant. In particular, circles contain
 * 32 line segments.
 *
 * @author Nicolas Fortin, Erwan Bocher
 */
public class ST_Buffer extends DeterministicScalarFunction {


    /**
     * Default constructor
     */
    public ST_Buffer() {
        addProperty(PROP_REMARKS, "Compute a buffer around a Geometry.\n"
                + "The optional third parameter can either specify number of segments used\n"
                + " to approximate a quarter circle (integer case, defaults to 8)\n"
                + " or a list of blank-separated key=value pairs (string case) to manage buffer style parameters :\n"
                + "'quad_segs=8' endcap=round|flat|square' 'join=round|mitre|bevel' 'mitre_limit=5'");
    }

    @Override
    public String getJavaStaticMethod() {
        return "buffer";
    }

    /**
     * @param geom Geometry instance
     * @param distance Buffer width in projection unit
     * @return geom buffer around geom geometry.
     */
    public static Geometry buffer(Geometry geom,Double distance) {
        if(geom==null || distance==null) {
            return null;
        }
        return geom.buffer(distance);
    }
    
    /**
     * @param geom Geometry instance
     * @param distance Buffer width in projection unit
     * @param value Int or varchar end type
     * @return a buffer around a geometry.
     */
    public static Geometry buffer(Geometry geom,Double distance, Value value) throws IllegalArgumentException {
        if(geom ==null){
            return null;
        }        
        if(value instanceof ValueString){
            String[] buffParemeters = value.getString().split("\\s+");  
            BufferParameters bufferParameters = new BufferParameters();
            for (String params : buffParemeters) {
                String[] keyValue = params.split("=");
                if(keyValue[0].equalsIgnoreCase("endcap")){
                    String param = keyValue[1];
                    if(param.equalsIgnoreCase("round")){
                        bufferParameters.setEndCapStyle(BufferParameters.CAP_ROUND);
                    }
                    else if(param.equalsIgnoreCase("flat") || param.equalsIgnoreCase("butt")){
                        bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
                    }
                    else if(param.equalsIgnoreCase("square")){
                        bufferParameters.setEndCapStyle(BufferParameters.CAP_SQUARE);
                    }
                    else{
                        throw new IllegalArgumentException("Supported join values are round, flat, butt or square.");
                    }
                }
                else if(keyValue[0].equalsIgnoreCase("join")){
                    String param = keyValue[1];
                    if(param.equalsIgnoreCase("bevel")){
                        bufferParameters.setJoinStyle(BufferParameters.JOIN_BEVEL);
                    }
                    else if(param.equalsIgnoreCase("mitre")||param.equalsIgnoreCase("miter")){
                        bufferParameters.setJoinStyle(BufferParameters.JOIN_MITRE);
                    }
                    else if(param.equalsIgnoreCase("round")){
                        bufferParameters.setJoinStyle(BufferParameters.JOIN_ROUND);
                    }
                    else{
                        throw new IllegalArgumentException("Supported join values are bevel, mitre, miter or round.");
                    }
                }
                else if(keyValue[0].equalsIgnoreCase("mitre_limit")||keyValue[0].equalsIgnoreCase("miter_limit")){
                    bufferParameters.setMitreLimit(Double.valueOf(keyValue[1]));
                }
                else if(keyValue[0].equalsIgnoreCase("quad_segs")){
                    bufferParameters.setQuadrantSegments(Integer.valueOf(keyValue[1]));
                }
                else{
                    throw new IllegalArgumentException("Unknown parameters. Please read the documentation.");
                }
            }            
            BufferOp bufOp  = new BufferOp(geom, bufferParameters);            
            return bufOp.getResultGeometry(distance);
        }
        else if (value instanceof ValueInt){
            BufferOp bufOp  = new BufferOp(geom, new BufferParameters(value.getInt()));
            return bufOp.getResultGeometry(distance);
        }
        else {
            throw new IllegalArgumentException("The third argument must be an int or a varchar.");
        }
    }
}
