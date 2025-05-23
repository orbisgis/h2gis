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

package org.h2gis.functions.spatial.edit;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;

/**
 * Returns a 1 dimension geometry with vertex order reversed according the start
 * and the end z values.
 *
 * @author Erwan Bocher CNRS
 */
public class ST_Reverse3DLine extends DeterministicScalarFunction {

    public ST_Reverse3DLine() {
        addProperty(PROP_REMARKS, "Returns a 1 dimension geometry with vertex order reversed according \n"
                + " the ascending z values. \n"
                + "The z of the first point must be lower than the z of the end point.\n"
                + " If the z values are equal to NaN return the input geometry. ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "reverse3DLine";
    }

    /**
     * Returns a 1 dimension geometry with vertex order reversed using the ascending 
     * value.
     *
     * @param geometry {@link Geometry}
     * @return Geometry
     */
    public static Geometry reverse3DLine(Geometry geometry) {
       return reverse3DLine(geometry, "asc");
    }

    /**
     * Returns a 1 dimension geometry with vertex order reversed according values 
     * ascending (asc) or descending (desc) 
     * 
     *
     * @param geometry {@link Geometry}
     * @param order asc to reverse in ascending , desc to reverse in descending
     * @return Geometry
     */
    public static Geometry reverse3DLine(Geometry geometry, String order) {
        if(geometry == null){
            return null;
        }
        if (geometry instanceof LineString) {
            return reverse3D((LineString) geometry, order);
        } else if (geometry instanceof MultiLineString) {
            return reverse3D((MultiLineString) geometry, order);
        }
        return null;
    }

    /**
     * Reverses a LineString according to the z value. The z of the first point
     * must be lower than the z of the end point.
     *
     * @param lineString {@link LineString}
     * @param order asc to reverse in ascending , desc to reverse in descending
     * @return LineString
     */
    private static LineString reverse3D(LineString lineString, String order) {
        CoordinateSequence seq = lineString.getCoordinateSequence();
        double startZ = seq.getCoordinate(0).z;
        double endZ = seq.getCoordinate(seq.size() - 1).z;
        if (order.equalsIgnoreCase("desc")) {
            if (!Double.isNaN(startZ) && !Double.isNaN(endZ) && startZ < endZ) {
                CoordinateSequences.reverse(seq);
                return lineString.getFactory().createLineString(seq);
            }
        } else if (order.equalsIgnoreCase("asc")) {
            if (!Double.isNaN(startZ) && !Double.isNaN(endZ) && startZ > endZ) {
                CoordinateSequences.reverse(seq);
                return lineString.getFactory().createLineString(seq);
            }
        }
        else {
            throw new IllegalArgumentException("Supported order values are asc or desc.");
        }
        return lineString;
    }

    /**
     * Reverses a multilinestring according to z value.If asc : the z first
     * point must be lower than the z end point if desc : the z first point must
     * be greater than the z end point
     *
     * @param multiLineString {@link MultiLineString}
     * @param order asc to reverse in ascending , desc to reverse in descending
     * @return MultiLineString
     */
    public static MultiLineString reverse3D(MultiLineString multiLineString, String order) {
        int num = multiLineString.getNumGeometries();
        LineString[] lineStrings = new LineString[num];
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            lineStrings[i] = reverse3D((LineString) multiLineString.getGeometryN(i), order);

        }
        return multiLineString.getFactory().createMultiLineString(lineStrings);
    }
}
