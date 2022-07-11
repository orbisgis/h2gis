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

package org.h2gis.functions.spatial.crs;

import org.cts.cs.CoordinateSystem;
import org.cts.op.CoordinateOperation;

/**
 * A simple tuple to manage both input and output CRSes used to build a
 * {@link CoordinateOperation}
 *
 * @author Erwan Bocher
 */
public class EPSGTuple {

    private int intputEPSG;
    private int targetEPSG;

    /**
     * Create the tuple with the input and output epsg codes available in the
     * spatial_ref_sys table
     *
     * @param intputEPSG the epsg code for the input {@link CoordinateSystem}
     * @param targetEPSG the epsg code for the output {@link CoordinateSystem}
     */
    public EPSGTuple(int intputEPSG, int targetEPSG) {
        this.intputEPSG = intputEPSG;
        this.targetEPSG = targetEPSG;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.intputEPSG;
        hash = 67 * hash + this.targetEPSG;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj instanceof EPSGTuple) {
            final EPSGTuple other = (EPSGTuple) obj;
            if (this.intputEPSG != other.intputEPSG) {
                return false;
            }
            return this.targetEPSG == other.targetEPSG;
        }
        return false;
    }
}
