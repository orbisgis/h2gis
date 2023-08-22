/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.create;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;


/**
 * ST_MakeArcPolygon constructs an elliptical arc, as a Polygon centered at the given point.
 *
 * @author Erwan Bocher, CNRS, 2023
 */
public class ST_MakeArcPolygon extends DeterministicScalarFunction {

    private static final GeometricShapeFactory GSF = new GeometricShapeFactory();

    public ST_MakeArcPolygon() {
        addProperty(PROP_REMARKS, "Creates an elliptical arc, as a Polygon centered at the given point with " +
                " a distance from the center point, a start angle (in radians) , a angle offset to define its size (in radians). " +
                "The arc is always created in a counter-clockwise direction. ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Creates an elliptical arc, as a Polygon  centered at the given point with the given start angle and
     * end angle size.
     *
     * @param p      Point
     * @param startAngle  start angle in radians
     * @param angExtent size of angle in radians
     * @param distance in meters
     * @return An arc Polygon centered at the given point.
     */
    public static Polygon execute(Point p, double distance, double startAngle, double angExtent) {
        if (p == null) {
            return null;
        }
        GSF.setCentre(new Coordinate(p.getX(), p.getY()));
        GSF.setSize(distance);
        Polygon geom = GSF.createArcPolygon(startAngle, angExtent);
        geom.setSRID(p.getSRID());
        return geom;
    }
}
