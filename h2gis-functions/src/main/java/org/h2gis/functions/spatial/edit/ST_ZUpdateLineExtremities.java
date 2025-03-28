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
 *
 * @author Erwan Bocher CNRS
 */
public class ST_ZUpdateLineExtremities extends DeterministicScalarFunction {


    public ST_ZUpdateLineExtremities() {
        addProperty(PROP_REMARKS, "Replace the start and end z values of a linestring or multilinestring.\n"
                + "By default the other z values are interpolated according the length of the line.\n"
                + "Set false if you want to update only the start and end z values.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "updateZExtremities";
    }

    public static Geometry updateZExtremities(Geometry geometry, double startZ, double endZ) {
        return updateZExtremities(geometry, startZ, endZ, true);

    }

    /**
     * Update the start and end Z values. If the interpolate is true the
     * vertices are interpolated according the start and end z values.
     *
     * @param geometry {@link Geometry}
     * @param startZ start z
     * @param endZ end z
     * @param interpolate true to interpolate the intermediate values
     * @return Z {@link Geometry}
     */
    public static Geometry updateZExtremities(Geometry geometry, double startZ, double endZ, boolean interpolate) {
        if(geometry == null){
            return null;
        }
        if (geometry instanceof LineString) {
            return force3DStartEnd((LineString) geometry, startZ, endZ, interpolate);
        } else if (geometry instanceof MultiLineString) {
            int nbGeom = geometry.getNumGeometries();
            LineString[] lines = new LineString[nbGeom];
            for (int i = 0; i < nbGeom; i++) {
                LineString subGeom = (LineString) geometry.getGeometryN(i);
                lines[i] = (LineString) force3DStartEnd(subGeom, startZ, endZ, interpolate);
            }
            return geometry.getFactory().createMultiLineString(lines);
        } else {
            return null;
        }
    }

    /**
     * Updates all z values by a new value using the specified first and the
     * last coordinates.
     *
     * @param lineString {@link LineString}
     * @param startZ start z
     * @param endZ end z
     * @param interpolate is true the z value of the vertices are interpolate 
     * according the length of the line.
     * @return Z {@link Geometry}
     */
    private static Geometry force3DStartEnd(LineString lineString, final double startZ,
            final double endZ, final boolean interpolate) {
        final double bigD = lineString.getLength();
        final double z = endZ - startZ;
        final Coordinate coordEnd = lineString.getCoordinates()[lineString.getCoordinates().length - 1];
        lineString.apply(new CoordinateSequenceFilter() {
            boolean done = false;

            @Override
            public boolean isGeometryChanged() {
                return true;
            }

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                double x = seq.getX(i);
                double y = seq.getY(i);
                if (i == 0) {
                    seq.setOrdinate(i, 0, x);
                    seq.setOrdinate(i, 1, y);
                    seq.setOrdinate(i, 2, startZ);
                } else if (i == seq.size() - 1) {
                    seq.setOrdinate(i, 0, x);
                    seq.setOrdinate(i, 1, y);
                    seq.setOrdinate(i, 2, endZ);
                } else {
                    if (interpolate) {
                        double smallD = seq.getCoordinate(i).distance(coordEnd);
                        double factor = smallD / bigD;
                        seq.setOrdinate(i, 0, x);
                        seq.setOrdinate(i, 1, y);
                        seq.setOrdinate(i, 2, startZ + (factor * z));
                    }
                }
                if (i == seq.size()) {
                    done = true;
                }
            }
        });
        return lineString;
    }
}
