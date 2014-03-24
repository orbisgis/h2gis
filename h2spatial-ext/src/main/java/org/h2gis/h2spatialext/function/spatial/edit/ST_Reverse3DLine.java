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
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Returns a 1 dimension geometry with vertex order reversed according the start
 * and the end z values.
 *
 * @author Erwan Bocher
 */
public class ST_Reverse3DLine extends DeterministicScalarFunction {
    
    private static final GeometryFactory FACTORY = new GeometryFactory();
    
    public ST_Reverse3DLine() {
        addProperty(PROP_REMARKS, "Returns a 1 dimension geometry with vertex order reversed according \n"
                + " the start and the end z values. \nThe z of the first point must be lower than the z of the end point.\n"
                + " If the z values are equal to NaN return the input geometry. ");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "reverse3DLine";
    }

    /**
     * Returns a 1 dimension geometry with vertex order reversed according the
     * start and the end z values.
     *
     * @param geometry
     * @return
     */
    public static Geometry reverse3DLine(Geometry geometry) {
        if (geometry instanceof LineString) {
            return reverse3D((LineString) geometry);
        } else if (geometry instanceof MultiLineString) {
            return reverse3D((MultiLineString) geometry);
        }
        return null;
    }

    /**
     * Reverses a LineString according to the z value. The z of the first point
     * must be lower than the z of the end point.
     *
     * @param lineString
     * @return
     */
    private static LineString reverse3D(LineString lineString) {
        CoordinateSequence seq = lineString.getCoordinateSequence();
        double startZ = seq.getCoordinate(0).z;
        double endZ = seq.getCoordinate(seq.size() - 1).z;
        if (!Double.isNaN(startZ) && !Double.isNaN(endZ) && startZ < endZ) {
            CoordinateSequences.reverse(seq);
            return FACTORY.createLineString(seq);
        }
        return lineString;
    }

    /**
     * Reverses a multilinestring according to z value. The z first point must
     * be greater than the z end point
     *
     * @param multiLineString
     * @return
     */
    public static MultiLineString reverse3D(MultiLineString multiLineString) {
        int num = multiLineString.getNumGeometries();
        LineString[] lineStrings = new LineString[num];
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            lineStrings[i] = reverse3D((LineString) multiLineString.getGeometryN(i));
            
        }
        return FACTORY.createMultiLineString(lineStrings);
    }
}
