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

package org.h2gis.functions.spatial.create;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;


/**
 * ST_MakeEllipse constructs an elliptical POLYGON with the given width and
 * height centered at the given point. Each ellipse contains 100 line segments.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_MakeEllipse extends DeterministicScalarFunction {

    private static final GeometricShapeFactory GSF = new GeometricShapeFactory();

    public ST_MakeEllipse() {
        addProperty(PROP_REMARKS, "Constructs an elliptical POLYGON with the " +
                "given width and height centered at the given point. Each " +
                "ellipse contains 100 line segments.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "makeEllipse";
    }

    /**
     * Make an ellipse centered at the given point with the given width and
     * height.
     *
     * @param p      Point
     * @param width  Width
     * @param height Height
     * @return An ellipse centered at the given point with the given width and height
     * @throws SQLException if the width or height is non-positive
     */
    public static Polygon makeEllipse(Point p, double width, double height) throws SQLException {
        if(p == null){
            return null;
        }
        if (height < 0 || width < 0) {
            throw new SQLException("Both width and height must be positive.");
        } else {
            GSF.setCentre(new Coordinate(p.getX(), p.getY()));
            GSF.setWidth(width);
            GSF.setHeight(height);
            return GSF.createEllipse();
        }
    }
}
