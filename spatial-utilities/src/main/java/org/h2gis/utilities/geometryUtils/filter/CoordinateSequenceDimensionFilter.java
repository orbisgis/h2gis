/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.utilities.geometryUtils.filter;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

/**
 * Filter on the dimension of the coordinate sequence.
 *
 * @author Erwan Bocher
 */
public class CoordinateSequenceDimensionFilter implements CoordinateSequenceFilter {

    private boolean isDone = false;
    private int dimension = 0;
    private int lastDimen = 0;
    public static final int XY = 2;
    public static final int XYZ = 3;
    public static final int XYZM = 4;
    private int maxDim = XYZM;

    @Override
    public void filter(CoordinateSequence seq, int i) {
        int dim = seq.getDimension();
        // we have to check for both the dimension AND that the value is not NaN
        if (dim == XY) {
            dimension = XY;
        } else {
            double firstZ = seq.getOrdinate(i, CoordinateSequence.Z);
            if (!Double.isNaN(firstZ)) {
                if (dim == XYZ) {
                    dimension = XYZ;
                } else {
                    double firstM = seq.getOrdinate(i, CoordinateSequence.M);
                    if (!Double.isNaN(firstM)) {
                        dimension = XYZM;
                    } else {
                        dimension = XYZ;
                    }
                }
            } else {
                dimension = XY;
            }
        }
        if (dimension > lastDimen) {
            lastDimen = dimension;
        }
        if (i == seq.size() || lastDimen >= maxDim) {
            isDone = true;
        }
    }

    /**
     * Gets the dimension of the coordinate sequence.
     *
     * @return a integer between 2 and 4.
     */
    public int getDimension() {
        return lastDimen;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean isGeometryChanged() {
        return false;
    }
}
