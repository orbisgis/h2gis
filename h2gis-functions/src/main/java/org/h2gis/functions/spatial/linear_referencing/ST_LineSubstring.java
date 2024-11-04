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

package org.h2gis.functions.spatial.linear_referencing;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.linearref.LengthIndexedLine;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author  Erwan Bocher, CNRS (2023)
 * Extract a section of the input line starting and ending at the given fractions.
 */
public class ST_LineSubstring extends DeterministicScalarFunction {


    public ST_LineSubstring(){
        addProperty(PROP_REMARKS, "Extract a section of the input LineString or MultiLineString starting and ending at the given fractions.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Extract a section of the input LineString or MultiLineString starting and ending at the given fractions.
     * @param geometry the input lines
     * @param start the start fraction between 0 and 1
     * @param end the end fraction between 0 and 1
     * @return single or multiparts lines
     */
    public static Geometry execute(Geometry geometry, double start, double end) throws SQLException {
        if(geometry==null){
            return null;
        }
        if ( start < 0 || start > 1 ){
            throw new SQLException("Allowed between 0 and  1");
        }

        if ( end < 0 || end > 1 ){
            throw new SQLException("Allowed between 0 and  1");
        }

        if(start> end){
            throw new SQLException("Start fraction must be smaller than end fraction");
        }
        if(geometry.isEmpty()){
            return geometry;
        }

        if(geometry instanceof LineString){
            double length = geometry.getLength();
            LengthIndexedLine ll = new LengthIndexedLine(geometry);
            return ll.extractLine(start*length, end*length);
        } else if (geometry instanceof MultiLineString) {
            int nb = geometry.getNumGeometries();
            ArrayList<LineString> lines = new ArrayList<>();
            for (int i = 0; i < nb; i++) {
                Geometry line = geometry.getGeometryN(i);
                double length = line.getLength();
                LengthIndexedLine ll = new LengthIndexedLine(geometry.getGeometryN(i));
                lines.add((LineString) ll.extractLine(start*length, end*length));
            }
            return geometry.getFactory().createMultiLineString(lines.toArray(new LineString[0]));
        }
        throw new SQLException("Only LineString or MultiLineString are supported");
    }
}
