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

package org.h2gis.functions.spatial.create;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import java.sql.SQLException;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

/**
 * Compute a ring buffer around a geometry.
 *
 * @author Erwan Bocher
 */
public class ST_RingBuffer extends AbstractFunction implements ScalarFunction {
   
    public ST_RingBuffer() {
        addProperty(PROP_REMARKS, "Compute a ring buffer around a geometry.\n"
                + "Avalaible arguments are :\n"
                + " (1) the geometry, (2) the size of each ring, "
                + " (3) the number of rings, (4) optional - the end cap style (square, round) Default is round\n"
                + "a list of blank-separated key=value pairs (string case) iso used t manage line style parameters.\n "
                + "Please read the ST_Buffer documention.\n"
                + " (5) optional - createHole True if you want to keep only difference between buffers Default is true.\n"
                + "Note : Holes are not supported by this function.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "ringBuffer";
    }

    /**
     * Compute a ring buffer around a geometry
     * @param geom
     * @param bufferSize
     * @param numBuffer
     * @return 
     * @throws java.sql.SQLException 
     */
    public static Geometry ringBuffer(Geometry geom, double bufferSize, int numBuffer) throws SQLException {
        return ringBuffer(geom, bufferSize, numBuffer, "endcap=round");
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
    public static Geometry ringBuffer(Geometry geom, double bufferDistance,
                                      int numBuffer, String parameters) throws SQLException {
        return ringBuffer(geom, bufferDistance, numBuffer, parameters, true);
    }

    /**
     * Compute a ring buffer around a geometry
     * @param geom
     * @param bufferDistance
     * @param numBuffer
     * @param parameters
     * @param doDifference
     * @throws SQLException 
     * @return 
     */
    public static Geometry ringBuffer(Geometry geom, double bufferDistance,
            int numBuffer, String parameters, boolean doDifference) throws SQLException {
        if(geom==null){
            return null;
        }
        if (geom.getNumGeometries() > 1) {
            throw new SQLException("This function supports only single geometry : point, linestring or polygon.");
        } else {            
            String[] buffParemeters = parameters.split("\\s+");
            BufferParameters bufferParameters = new BufferParameters();
            for (String params : buffParemeters) {
                String[] keyValue = params.split("=");
                if (keyValue[0].equalsIgnoreCase("endcap")) {
                    String param = keyValue[1];
                    if (param.equalsIgnoreCase("round")) {
                        bufferParameters.setEndCapStyle(BufferParameters.CAP_ROUND);
                    } else if (param.equalsIgnoreCase("square")) {
                        bufferParameters.setEndCapStyle(BufferParameters.CAP_SQUARE);
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
                return computePositiveRingBuffer(geom, bufferDistance, numBuffer, bufferParameters, doDifference);
            } else if (bufferDistance < 0) {
                if (geom instanceof Point) {
                    throw new SQLException("Cannot compute a negative ring buffer on a point.");
                } else {
                    return computeNegativeRingBuffer(geom, bufferDistance, numBuffer, bufferParameters, doDifference);
                }
            } else {
                return geom;
            }
        }
    }

    /**
     * Compute a ring buffer with a positive offset
     * 
     * @param geom
     * @param bufferDistance
     * @param numBuffer
     * @param bufferParameters
     * @param doDifference
     * @return
     * @throws SQLException 
     */
    public static Geometry computePositiveRingBuffer(Geometry geom, double bufferDistance,
            int numBuffer, BufferParameters bufferParameters, boolean doDifference) throws SQLException {
        Polygon[] buffers = new Polygon[numBuffer];        
        if (geom instanceof Polygon) {
            //Work arround to manage polygon with hole
            geom = geom.getFactory().createPolygon(((Polygon) geom).getExteriorRing().getCoordinateSequence());
        }
        Geometry previous = geom;
        double distance = 0;
        for (int i = 0; i < numBuffer; i++) {
            distance += bufferDistance;
            Geometry newBuffer = runBuffer(geom, distance, bufferParameters);
            if (doDifference) {
                buffers[i] = (Polygon) newBuffer.difference(previous);
            } else {
                buffers[i] = (Polygon) newBuffer;
            }
            previous = newBuffer;
        }
        return geom.getFactory().createMultiPolygon(buffers);
    }
    
    /**
     * Compute a ring buffer with a negative offset
     * 
     * @param geom
     * @param bufferDistance
     * @param numBuffer
     * @param bufferParameters
     * @param doDifference
     * @return
     * @throws SQLException 
     */
    public static Geometry computeNegativeRingBuffer(Geometry geom, double bufferDistance,
            int numBuffer, BufferParameters bufferParameters, boolean doDifference) throws SQLException {
        Polygon[] buffers = new Polygon[numBuffer];
        Geometry previous = geom;
        double distance = 0;
        if (geom instanceof Polygon) {
            geom = ((Polygon) geom).getExteriorRing();
            bufferParameters.setSingleSided(true);
        }
        for (int i = 0; i < numBuffer; i++) {
            distance += bufferDistance;
            Geometry newBuffer = runBuffer(geom, distance, bufferParameters);
            if (i == 0) {
                buffers[i] = (Polygon) newBuffer;
            } else {
                if (doDifference) {
                    buffers[i] = (Polygon) newBuffer.difference(previous);
                } else {
                    buffers[i] = (Polygon) newBuffer;
                }
            }
            previous = newBuffer;
        }
        return geom.getFactory().createMultiPolygon(buffers);
    }

    /**
     * Calculate the ring buffer
     * 
     * @param geom
     * @param bufferSize
     * @param bufferParameters
     * @return
     * @throws SQLException 
     */
    public static Geometry runBuffer(final Geometry geom, final double bufferSize,
            final BufferParameters bufferParameters) throws SQLException {
        return BufferOp.bufferOp(geom, bufferSize, bufferParameters);
    }
}
