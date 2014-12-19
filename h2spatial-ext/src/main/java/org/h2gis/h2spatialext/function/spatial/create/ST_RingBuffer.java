/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;

/**
 * Compute a ring buffer around a geometry.
 *
 * @author Erwan Bocher
 */
public class ST_RingBuffer extends AbstractFunction implements ScalarFunction {

    private static final GeometryFactory GF = new GeometryFactory();

    public ST_RingBuffer() {
        addProperty(PROP_REMARKS, "Compute a ring buffer around a geometry.\n"
                + "Avalaible arguments are :\n"
                + " (1) the geometry, (2) the size of each ring, "
                + " (3) the number of rings, (4) optional - the end cap style (square, round) Default is round\n"+
                  " (5) optional - createHole True if you want to keep only difference between buffers Default is true");
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
        if(!(bufferDistance > 0)) {
            // If buffer distance is not superior than zero return the same geometry.
            return geom;
        }        
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
            } else if(keyValue[0].equalsIgnoreCase("join")) {
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
        
        Polygon[] buffers = new Polygon[numBuffer];
        Geometry previous = geom;
        double distance = 0;
        for (int i = 0; i < numBuffer; i++) {
            distance += bufferDistance;
            Geometry newBuffer = runBuffer(geom, distance, bufferParameters);
            if(doDifference) {
                buffers[i] = (Polygon) newBuffer.difference(previous);
            } else {
                buffers[i] = (Polygon) newBuffer;
            }
            previous = newBuffer;
        }
        return GF.createMultiPolygon(buffers);
    }

    /**
     * Calculate the ring buffer
     * 
     * @param geom
     * @param bufferSize
     * @return
     * @throws SQLException 
     */
    private static Geometry runBuffer(final Geometry geom, final double bufferSize,
            final BufferParameters bufferParameters) throws SQLException {
        return BufferOp.bufferOp(geom, bufferSize, bufferParameters);
    }
}
