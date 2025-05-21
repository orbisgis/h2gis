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

package org.h2gis.functions.spatial.create;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.construct.LargestEmptyCircle;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;

/**
 * Compute the  largest circle that is contained within a (multi)polygon.
 * @author Erwan Bocher, CNRS
 */
public class ST_MaximumInscribedCircle extends DeterministicScalarFunction {

    public ST_MaximumInscribedCircle() {
        addProperty(PROP_REMARKS, "Compute the largest circle that is contained within a (multi)polygon or " +
                "the largest circle of a set of geometries constrained by obstacles.\n" +
                " The obstacles may be any combination of point, linear and polygonal geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    public static Geometry execute(Geometry geometry) throws SQLException {
        if (geometry == null) {
            return null;
        }
        if(geometry.isEmpty()){
            return geometry;
        }
        Envelope env = geometry.getEnvelopeInternal();
        double width = env.getWidth();
        double height = env.getHeight();
        double size = width > height ? width : height;
        double tolerance = size / 1000.0;
        if(geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            MaximumInscribedCircle mic = new MaximumInscribedCircle(geometry, tolerance);
            Geometry geom = geometry.getFactory().createPoint(mic.getCenter().getCoordinate()).buffer(mic.getRadiusLine().getLength());
            geom.setSRID(geometry.getSRID());
            return geom;
        }
        else{
            LargestEmptyCircle lec = new LargestEmptyCircle(geometry, null, tolerance);
            Geometry geom =  lec.getCenter().buffer(lec.getRadiusLine().getLength());
            geom.setSRID(geometry.getSRID());
            return geom;
        }
    }

}
