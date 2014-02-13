/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * 
 * @author Erwan Bocher
 */
public class ST_AddZToExtremities extends DeterministicScalarFunction {

    private static final GeometryFactory FACTORY = new GeometryFactory();
    
    @Override
    public String getJavaStaticMethod() {
        return "addZToExtremities";
    }

    /**
     * Update the start and end Z values
     * 
     * @param geometry
     * @param startZ
     * @param endZ
     * @return 
     */
    public static Geometry addZToExtremities(Geometry geometry, double startZ, double endZ) {
        if (geometry instanceof LineString) {
            return force3DStartEnd((LineString) geometry, startZ, endZ);
        } else if (geometry instanceof MultiLineString) {
            int nbGeom = geometry.getNumGeometries();
            LineString[] lines = new LineString[nbGeom];
            for (int i = 0; i < nbGeom; i++) {
                LineString subGeom = (LineString) geometry.getGeometryN(i);
                lines[i] = (LineString) force3DStartEnd(subGeom, startZ, endZ);
            }
            return FACTORY.createMultiLineString(lines);
        } else {
            return null;
        }
    }

    /**
     * Updates all z values by a new value using the specified first and the
     * last coordinates.
     *
     * @param geom
     * @param startZ
     * @param endZ
     * @return
     */
    private static Geometry force3DStartEnd(LineString lineString, final double startZ,
            final double endZ) {
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
                    double smallD = seq.getCoordinate(i).distance(coordEnd);
                    double factor = smallD / bigD;
                    seq.setOrdinate(i, 0, x);
                    seq.setOrdinate(i, 1, y);
                    seq.setOrdinate(i, 2, startZ + (factor * z));
                }

                if (i == seq.size()) {
                    done = true;
                }
            }
        });
        return lineString;
    }
}
