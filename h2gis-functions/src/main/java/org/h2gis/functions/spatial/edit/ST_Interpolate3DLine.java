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

package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Interpolate a 1 dimension geometry according its start and end z values.
 *
 * @author Erwan Bocher
 */
public class ST_Interpolate3DLine extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    public ST_Interpolate3DLine() {
        addProperty(PROP_REMARKS, "Interpolate the z values of a linestring or multilinestring based on\n"
                + "the start and the end z values. If the z values are equal to NaN return the\n "
                + "input geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "interpolateLine";
    }

    /**
     *
     * @param geometry
     * @return
     */
    public static Geometry interpolateLine(Geometry geometry) {
        if(geometry == null){
            return null;
        }
        if (geometry instanceof LineString) {
            return linearZInterpolation((LineString) geometry);
        } else if (geometry instanceof MultiLineString) {
            return linearZInterpolation((MultiLineString) geometry);
        }
        return null;
    }

    /**
     * Interpolate a linestring according the start and the end coordinates z
     * value. If the start or the end z is NaN return the input linestring
     *
     * @param lineString
     * @return
     */
    private static LineString linearZInterpolation(LineString lineString) {
        double startz = lineString.getStartPoint().getCoordinate().z;
        double endz = lineString.getEndPoint().getCoordinate().z;
        if (Double.isNaN(startz) || Double.isNaN(endz)) {
            return lineString;
        } else {
            double length = lineString.getLength();
            lineString.apply(new LinearZInterpolationFilter(startz, endz, length));
            return lineString;
        }
    }

    /**
     * Interpolate each linestring of the multilinestring.
     *
     * @param multiLineString
     * @return
     */
    private static MultiLineString linearZInterpolation(MultiLineString multiLineString) {
        int nbGeom = multiLineString.getNumGeometries();
        LineString[] lines = new LineString[nbGeom];
        for (int i = 0; i < nbGeom; i++) {
            LineString subGeom = (LineString) multiLineString.getGeometryN(i);
            double startz = subGeom.getStartPoint().getCoordinates()[0].z;
            double endz = subGeom.getEndPoint().getCoordinates()[0].z;
            double length = subGeom.getLength();
            subGeom.apply(new LinearZInterpolationFilter(startz, endz, length));
            lines[i] = subGeom;

        }
        return FACTORY.createMultiLineString(lines);
    }

    /**
     * Interpolate the z values according a start and a end z values.
     *
     */
    private static class LinearZInterpolationFilter implements CoordinateSequenceFilter {

        private boolean done = false;
        private double startZ = 0;
        private double endZ = 0;
        private double dZ = 0;
        private final double length;
        private int seqSize = 0;
        private double sumLenght = 0;

        LinearZInterpolationFilter(double startZ, double endZ, double length) {
            this.startZ = startZ;
            this.endZ = endZ;
            this.length = length;

        }

        @Override
        public void filter(CoordinateSequence seq, int i) {
            if (i == 0) {
                seqSize = seq.size();
                dZ = endZ - startZ;
            } else if (i == seqSize) {
                done = true;
            } else {
                Coordinate coord = seq.getCoordinate(i);
                Coordinate previousCoord = seq.getCoordinate(i - 1);
                sumLenght += coord.distance(previousCoord);
                seq.setOrdinate(i, 2, startZ + dZ * sumLenght / length);
            }

        }

        @Override
        public boolean isGeometryChanged() {
            return true;
        }

        @Override
        public boolean isDone() {
            return done;
        }
    }
}
