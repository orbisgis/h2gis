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

package org.h2gis.functions.spatial.mesh;

import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueVarchar;
import org.h2.value.ValueVarcharIgnoreCase;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import java.sql.SQLException;

/**
 * Returns polygons or lines that represent a Delaunay triangulation constructed
 * from a geometry. Note that the triangulation computes
 * the intersections between lines.
 *
 * @author Erwan Bocher
 */
public class ST_ConstrainedDelaunay extends DeterministicScalarFunction {


    /**
     * Name of the parameter that sets the merge distance between input points
     */
    public static final String MIN_POINT_SPACING = "minPointSpacing";
    /**
     * Name of the parameter that enables the Delaunay refinement
     */
    public static final String MIN_ANGLE = "minAngle";

    /**
     * Name of the parameter that stops the refinement of small triangles
     */
    public static final String MIN_TRIANGLE_AREA = "minTriangleArea";

    public ST_ConstrainedDelaunay() {
        addProperty(PROP_REMARKS, "Returns polygons that represent a Constrained Delaunay Triangulation from a geometry.\n"
                + "Output is a COLLECTION of polygons, for flag=0 (default flag) or a MULTILINESTRING for flag=1.\n"
                + "The last argument can either be the minPointSpacing (double) or a list of blank-separated \n" +
                "key=value pairs (string case) e.g. 'minPointSpacing=0.01 minAngle=30 minTriangleArea=100':\n" +
                "- minPointSpacing: merge distance between provided points, by default 1e-12\n" +
                "- minAngle: enables the Delaunay refinement (Ruppert algorithm). \n" +
                "- minTriangleArea: skinny triangles whose area is lower than this value do not receive new points (unit of the coordinates).\n");
    }

    @Override
    public String getJavaStaticMethod() {
        return "createCDT";
    }

    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry {@link Geometry}
     * @return a set of polygons (triangles)
     */
    public static GeometryCollection createCDT(Geometry geometry) throws SQLException {
        return createCDT(geometry, 0, ValueNull.INSTANCE);
    }
    
    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry Geometry
     * @param flagOrParameters the flag (integer, 0 = polygon, 1 = lines) or
     *                         the triangulation parameters (varchar, e.g. 'minAngle=30')
     * @return a set of geometries (flag 0 = polygon, flag 1 = lines)
     */
    public static GeometryCollection createCDT(Geometry geometry, Value flagOrParameters) throws SQLException {
        if (isText(flagOrParameters)) {
            return createCDT(geometry, 0, flagOrParameters);
        }
        int flag = flagOrParameters == null || flagOrParameters == ValueNull.INSTANCE ? 0 : flagOrParameters.getInt();
        return createCDT(geometry, flag, ValueNull.INSTANCE);
    }


    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon)
     *
     * @param geometry Geometry
     * @param flag 0 = polygon, 1 = lines
     * @param minPointSpacingOrParameters the merge distance between input points (double)
     *                                    or the triangulation parameters (varchar), a list of blank-separated
     *                                    key=value pairs: 'minPointSpacing=0.01 minAngle=30 minTriangleArea=100'
     * @return a set of geometries (flag 0 = polygon, flag 1 = lines)
     */
    public static GeometryCollection createCDT(Geometry geometry, int flag, Value minPointSpacingOrParameters) throws SQLException {
        double minPointSpacing = DelaunayData.DEFAULT_EPSILON;
        double minAngle = 0;
        double minTriangleArea = DelaunayData.MIN_TRIANGLE_AREA;
        if (isText(minPointSpacingOrParameters)) {
            String parameters = minPointSpacingOrParameters.getString().trim();
            if (!parameters.isEmpty()) {
                // Allow blanks around the = sign: 'minAngle = 30'
                for (String parameter : parameters.replaceAll("\\s*=\\s*", "=").split("\\s+")) {
                    String[] keyValue = parameter.split("=", -1);
                    if (keyValue.length != 2 || keyValue[0].isEmpty() || keyValue[1].isEmpty()) {
                        throw new SQLException("Invalid parameter '" + parameter + "', the expected syntax is key=value " +
                                "e.g. 'minPointSpacing=0.01 minAngle=30 minTriangleArea=100'");
                    }
                    String key = keyValue[0];
                    double value = parseDouble(key, keyValue[1]);
                    if (key.equalsIgnoreCase(MIN_POINT_SPACING)) {
                        minPointSpacing = value;
                    } else if (key.equalsIgnoreCase(MIN_ANGLE)) {
                        minAngle = value;
                    } else if (key.equalsIgnoreCase(MIN_TRIANGLE_AREA)) {
                        // A negative value asks for the default threshold
                        minTriangleArea = value < 0 ? DelaunayData.MIN_TRIANGLE_AREA : value;
                    } else {
                        throw new SQLException("Unknown parameter '" + key + "'. Supported parameters are "
                                + MIN_POINT_SPACING + ", " + MIN_ANGLE + " and " + MIN_TRIANGLE_AREA);
                    }
                }
            }
        } else if (minPointSpacingOrParameters != null && minPointSpacingOrParameters != ValueNull.INSTANCE) {
            minPointSpacing = minPointSpacingOrParameters.getDouble();
        }
        return createCDT(geometry, flag, minPointSpacing, minAngle, minTriangleArea);
    }


    /**
     * Build a constrained delaunay triangulation based on a geometry
     * (point, line, polygon) and refine it with the Ruppert algorithm
     *
     * @param geometry Geometry
     * @param flag 0 = polygon, 1 = lines
     * @param minPointSpacing Will merge points if the distance is inferior between this parameter
     * @param minAngle Minimum internal angle of the triangles in degrees, in the range ]0, 60[.
     *                 0 disables the Delaunay refinement
     * @param minTriangleArea Triangles with an area lower than this value are not refined.
     *                        0 disables this guard, {@link DelaunayData#MIN_TRIANGLE_AREA}
     *                        computes it from the data
     * @return a set of geometries (flag 0 = polygon, flag 1 = lines)
     */
    private static GeometryCollection createCDT(Geometry geometry, int flag, double minPointSpacing,
                                                double minAngle, double minTriangleArea) throws SQLException {
        if (geometry == null) {
            return null;
        }
        if (flag != 0 && flag != 1) {
            throw new SQLException("Only flag 0 or 1 is supported.");
        }
        DelaunayData delaunayData = new DelaunayData();
        delaunayData.setEpsilon(Math.max(0, minPointSpacing));
        try {
            delaunayData.setRefinement(minAngle, minTriangleArea);
        } catch (IllegalArgumentException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
        delaunayData.put(OverlayNGRobust.union(geometry), DelaunayData.MODE.CONSTRAINED);
        delaunayData.triangulate();
        if (flag == 0) {
            return delaunayData.getTrianglesAsMultiPolygon();
        } else {
            return delaunayData.getTrianglesSides();
        }
    }


    /**
     * Check if the value is a character string
     * @param value a SQL value
     * @return true if the value is a character string
     */
    private static boolean isText(Value value) {
        return value instanceof ValueVarchar || value instanceof ValueVarcharIgnoreCase
                || (value != null && value.getValueType() == Value.CHAR);
    }

    /**
     * Convert string value to double
     * @param key parameter name
     * @param value parameter value
     * @return the value as a double
     * @throws SQLException if the value is not a number
     */
    private static double parseDouble(String key, String value) throws SQLException {
        try {
            double result = Double.parseDouble(value);
            if (!Double.isFinite(result)) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new SQLException("The value of the parameter '" + key + "' must be a number, found '" + value + "'");
        }
    }
}
