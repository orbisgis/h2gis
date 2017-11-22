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

package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

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
     * Init CoordinateSequenceDimensionFilter object.
     * @param geometry Geometry instance
     * @return CoordinateSequenceDimensionFilter instance
     */
    public static CoordinateSequenceDimensionFilter apply(Geometry geometry) {
        CoordinateSequenceDimensionFilter cd = new CoordinateSequenceDimensionFilter();
        geometry.apply(cd);
        return cd;
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

