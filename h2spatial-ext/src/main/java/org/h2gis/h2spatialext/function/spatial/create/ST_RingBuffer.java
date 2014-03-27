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

    private static final String CAP_STYLE_SQUARE = "square";
    private static final String CAP_STYLE_BUTT = "butt";
    private static final String CAP_STYLE_ROUND = "round";
    private static final GeometryFactory GF = new GeometryFactory();

    public ST_RingBuffer() {
        addProperty(PROP_REMARKS, "Compute a ring buffer around a geometry.\n"
                + "Avalaible arguments are :\n"
                + " (1) the geometry, (2) the size of each ring, "
                + "(3) the number of rings, (4) the end cap style (square, butt, round)\n"
                + "This last argument is optional. Default is round.");
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
     */
    public static Geometry ringBuffer(Geometry geom, double bufferSize, int numBuffer) throws SQLException {
        return ringBuffer(geom, bufferSize, numBuffer, CAP_STYLE_ROUND);
    }

    /**
     * Compute a ring buffer around a geometry
     * @param geom
     * @param bufferDistance
     * @param numBuffer
     * @param endCapStyle
     * @throws SQLException 
     * @return 
     */
    public static Geometry ringBuffer(Geometry geom, double bufferDistance,
            int numBuffer, String endCapStyle) throws SQLException {
        Polygon[] buffers = new Polygon[numBuffer];
        Geometry previous = geom;
        double distance = 0;
        for (int i = 0; i < numBuffer; i++) {
            distance += bufferDistance;
            Geometry newBuffer = runBuffer(geom, distance, endCapStyle);
            Geometry geomBufferExternal = newBuffer.difference(previous);
            buffers[i] = (Polygon) geomBufferExternal;
            previous = newBuffer;
        }
        return GF.createMultiPolygon(buffers);
    }

    /**
     * Calculate the ring buffer
     * 
     * @param geom
     * @param bufferSize
     * @param endCapStyle
     * @return
     * @throws SQLException 
     */
    private static Geometry runBuffer(final Geometry geom, final double bufferSize,
            final String endCapStyle) throws SQLException {
        BufferOp bufOp;
        if (endCapStyle.equalsIgnoreCase(CAP_STYLE_SQUARE)) {
            bufOp = new BufferOp(geom, new BufferParameters(
                    BufferParameters.DEFAULT_QUADRANT_SEGMENTS,
                    BufferParameters.CAP_SQUARE));
            return bufOp.getResultGeometry(bufferSize);
        } else if (endCapStyle.equalsIgnoreCase(CAP_STYLE_BUTT)) {
            bufOp = new BufferOp(geom, new BufferParameters(
                    BufferParameters.DEFAULT_QUADRANT_SEGMENTS,
                    BufferParameters.CAP_FLAT));
            return bufOp.getResultGeometry(bufferSize);
        } else if (endCapStyle.equalsIgnoreCase(CAP_STYLE_ROUND)){
            bufOp = new BufferOp(geom, new BufferParameters(
                    BufferParameters.DEFAULT_QUADRANT_SEGMENTS,
                    BufferParameters.CAP_ROUND));
            return bufOp.getResultGeometry(bufferSize);
        }
        throw new SQLException("Invalid cap style value. Please use "+ CAP_STYLE_ROUND + " or "
                + CAP_STYLE_SQUARE+ " or "+ CAP_STYLE_BUTT);        
    }
}
