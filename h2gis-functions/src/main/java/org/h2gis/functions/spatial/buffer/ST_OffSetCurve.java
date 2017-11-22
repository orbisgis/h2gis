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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;
import org.locationtech.jts.operation.buffer.OffsetCurveSetBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Return an offset line at a given distance and side from an input geometry.
 * @author Erwan Bocher
 */
public class ST_OffSetCurve extends DeterministicScalarFunction {

    
    public ST_OffSetCurve() {
        addProperty(PROP_REMARKS, "Return an offset line or collection of lines at a given distance and side from an input geometry.\n"
                + "The optional third parameter can either specify number of segments used\n"
                + " to approximate a quarter circle (integer case, defaults to 8)\n"
                + " or a list of blank-separated key=value pairs (string case) to manage line style parameters :\n"
                + "'quad_segs=8' endcap=round|flat|square' 'join=round|mitre|bevel' 'mitre_limit=5'");
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
     * @return 
     */
    public static Geometry offsetCurve(Geometry geometry, double offset, String parameters) {
        if(geometry == null){
            return null;
        }
        String[] buffParemeters = parameters.split("\\s+");
        BufferParameters bufferParameters = new BufferParameters();
        for (String params : buffParemeters) {
            String[] keyValue = params.split("=");
            if (keyValue[0].equalsIgnoreCase("endcap")) {
                String param = keyValue[1];
                if (param.equalsIgnoreCase("round")) {
                    bufferParameters.setEndCapStyle(BufferParameters.CAP_ROUND);
                } else if (param.equalsIgnoreCase("flat") || param.equalsIgnoreCase("butt")) {
                    bufferParameters.setEndCapStyle(BufferParameters.CAP_FLAT);
                } else if (param.equalsIgnoreCase("square")) {
                    bufferParameters.setEndCapStyle(BufferParameters.CAP_SQUARE);
                } else {
                    throw new IllegalArgumentException("Supported join values are round, flat, butt or square.");
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
        return computeOffsetCurve(geometry, offset, bufferParameters);
    }

    /**
     * Return an offset line at a given distance and side from an input geometry
     * without buffer parameters
     * @param geometry the geometry
     * @param offset the distance
     * @return 
     */
    public static Geometry offsetCurve(Geometry geometry, double offset) {
        return computeOffsetCurve(geometry, offset, new BufferParameters());
    }

    /**
     * Method to compute the offset line
     * @param geometry
     * @param offset
     * @param bufferParameters
     * @return 
     */
    public static Geometry computeOffsetCurve(Geometry geometry, double offset, BufferParameters bufferParameters) {
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subGeom = geometry.getGeometryN(i);
            if (subGeom.getDimension() == 1) {
                lineStringOffSetCurve(lineStrings, (LineString) subGeom, offset, bufferParameters);
            } else {
                geometryOffSetCurve(lineStrings, subGeom, offset, bufferParameters);
            }
        }
        if (!lineStrings.isEmpty()) {
            if (lineStrings.size() == 1) {
                return lineStrings.get(0);
            } else {
                return geometry.getFactory().createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
            }
        }
        return null;
    }

    /**
     * Compute the offset curve for a linestring
     *
     * @param list
     * @param lineString
     * @param offset
     * @param bufferParameters
     */
    public static void lineStringOffSetCurve(ArrayList<LineString> list, LineString lineString, double offset, BufferParameters bufferParameters) {
        list.add(lineString.getFactory().createLineString(new OffsetCurveBuilder(lineString.getPrecisionModel(), bufferParameters).getOffsetCurve(lineString.getCoordinates(), offset)));
    }

    /**
     * Compute the offset curve for a polygon, a point or a collection of geometries
     * @param list
     * @param geometry
     * @param offset
     * @param bufferParameters
     */
    public static void geometryOffSetCurve(ArrayList<LineString> list, Geometry geometry, double offset, BufferParameters bufferParameters) {
        final List curves = new OffsetCurveSetBuilder(geometry, offset, new OffsetCurveBuilder(geometry.getFactory().getPrecisionModel(), bufferParameters)).getCurves();
        final Iterator<SegmentString> iterator = curves.iterator();
        while (iterator.hasNext()) {
            list.add(geometry.getFactory().createLineString(iterator.next().getCoordinates()));
        }
    }

}
