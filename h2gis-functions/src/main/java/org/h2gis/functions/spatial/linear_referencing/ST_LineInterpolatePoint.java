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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.linearref.LengthIndexedLine;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 *  Returns a point interpolate along the input LineString or MultiLineString starting at the given fraction.
 *  @author  Erwan Bocher, CNRS (2023)
 */
public class ST_LineInterpolatePoint extends DeterministicScalarFunction {

    public ST_LineInterpolatePoint(){
        addProperty(PROP_REMARKS, "Returns a point interpolate along the input LineString or MultiLineString starting at the given fraction.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Returns a point interpolate along the input LineString or MultiLineString starting at the given fractions.
     * @param geometry the input lines
     * @param start the start fraction between 0 and 1
     * @return single or multiparts lines
     */
    public static Geometry execute(Geometry geometry, double start) throws SQLException {
        if(geometry==null){
            return null;
        }
        if ( start < 0 || start > 1 ){
            throw new SQLException("Allowed between 0 and  1");
        }

        if(geometry.isEmpty()){
            return geometry;
        }

        if(geometry instanceof LineString){
            double length = geometry.getLength();
            LengthIndexedLine ll = new LengthIndexedLine(geometry);
            return geometry.getFactory().createPoint(ll.extractPoint(start*length));
        } else if (geometry instanceof MultiLineString) {
            int nb = geometry.getNumGeometries();
            ArrayList<Coordinate> points = new ArrayList<>();
            for (int i = 0; i < nb; i++) {
                Geometry line = geometry.getGeometryN(i);
                double length = line.getLength();
                LengthIndexedLine ll = new LengthIndexedLine(geometry.getGeometryN(i));
                points.add(ll.extractPoint(start*length));
            }
            return geometry.getFactory().createMultiPointFromCoords(points.toArray(new Coordinate[0]));
        }
        throw new SQLException("Only LineString or MultiLineString are supported");
    }
}
