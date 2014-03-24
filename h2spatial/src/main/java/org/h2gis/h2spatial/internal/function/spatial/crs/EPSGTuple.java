/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatial.internal.function.spatial.crs;

import org.cts.cs.CoordinateSystem;

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
            if (this.targetEPSG != other.targetEPSG) {
                return false;
            }
            return true;
        }
        return false;
    }
}
