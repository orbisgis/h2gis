/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

/**
 * Filter on the dimension of each coordinate of the CoordinateSequence to determine
 * the maximum coordinate dimension as well as whether the CoordinateSequence contains
 * only 2D coordinates or mixed 2D and >2D coordinates.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class CoordinateSequenceDimensionFilter implements CoordinateSequenceFilter {

    private boolean isDone = false;
    private int largestDimSoFar = 0;
    public static final int XY = 2;
    public static final int XYZ = 3;
    public static final int XYZM = 4;
    private int maxDim = XYZM;
    private boolean exists2D = false;
    private boolean exists3D = false;

    @Override
    public void filter(CoordinateSequence seq, int i) {
        // Here we find the largest possible dimension of the CoordinateSequence.
        int currentDim = 0;
        if (Double.isNaN(seq.getOrdinate(i, CoordinateSequence.Z))) {
            // Found a NaN z-coordinate
            exists2D = true;
            currentDim = XY;
        } else {
            // Found a non-NaN z-coordinate
            exists3D = true;
            if (Double.isNaN(seq.getOrdinate(i, CoordinateSequence.M))) {
                currentDim = XYZ;
            } else {
                currentDim = XYZM;
            }
        }
        if (currentDim > largestDimSoFar) {
            largestDimSoFar = currentDim;
        }
        if (i == seq.size() || (largestDimSoFar >= maxDim && isMixed())) {
            isDone = true;
        }
    }

    /**
     * Gets the dimension of the coordinate sequence.
     *
     * @return a integer between 2 and 4.
     */
    public int getDimension() {
        return largestDimSoFar;
    }

    public boolean isMixed() {
        return exists2D && exists3D;
    }

    public boolean is2D() {
        return !exists3D;
    }

    /**
     * Sets the maximum allowed dimension for the filter.
     *
     * The filter will stop after this dimension has been reached.
     * Possible values are:
     * <code>CoordinateSequenceDimensionFilter.XY</code>
     * <code>CoordinateSequenceDimensionFilter.XYZ</code>
     * <code>CoordinateSequenceDimensionFilter.XYZM</code>
     * Default value is:
     * <code>CoordinateSequenceDimensionFilter.XYZM</code>.
     *
     * @param maxDim a integer dimension
     */
    public void setMAXDim(int maxDim) {
        this.maxDim = maxDim;
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

