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
import org.h2gis.functions.spatial.volume.GeometryExtrude;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.sql.SQLException;

/**
 * ST_Extrude takes a LINESTRING or POLYGON as input and extends it to a 3D
 * representation, returning  a geometry collection containing floor, ceiling
 * and wall geometries. In the case of a LINESTRING, the floor and ceiling are
 * LINESTRINGs; for a POLYGON, they are POLYGONs.
 *
 * @author Erwan Bocher
 */
public class ST_Extrude extends DeterministicScalarFunction {

    public ST_Extrude() {
        addProperty(PROP_REMARKS, "ST_Extrude takes a LINESTRING or POLYGON as input\n"
                + " and extends it to a 3D representation, returning a geometry collection\n"
                + " containing floor, ceiling and wall geometries.\n"
                + "Note: the NaN z value of the input geometry are replaced by a zero.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "extrudeGeometry";
    }

    /**
     * Extrudes a POLYGON or a LINESTRING into a GEOMETRYCOLLECTION containing
     * the floor (input geometry), walls and ceiling.
     * Note :  the NaN z value of the input geometry are replaced by a zero.
     *
     * @param geometry Input geometry
     * @param height   Desired height
     * @return Collection (floor, walls, ceiling)
     */
    public static GeometryCollection extrudeGeometry(Geometry geometry, double height) throws SQLException {
        if(geometry == null){
            return null;
        }
        if (geometry instanceof Polygon) {
            return GeometryExtrude.extrudePolygonAsGeometry((Polygon) geometry, height);
        } else if (geometry instanceof LineString) {
            return GeometryExtrude.extrudeLineStringAsGeometry((LineString) geometry, height);
        }
        throw new SQLException("Only LINESTRING and POLYGON inputs are accepted.");
    }

    /**
     *
     * Extrudes a POLYGON or a LINESTRING into a GEOMETRYCOLLECTION containing
     * the floor (input geometry), walls and ceiling.  A flag of 1 extracts
     * walls and a flag of 2 extracts the roof.
     *
     * @param geometry Input geometry
     * @param height   Desired height
     * @param flag     1 (walls), 2 (roof)
     * @return Walls or roof
     */
    public static Geometry extrudeGeometry(Geometry geometry, double height, int flag) throws SQLException {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof Polygon) {
            switch (flag) {
                case 1:
                    return GeometryExtrude.extractWalls((Polygon)geometry, height);
                case 2:
                    return GeometryExtrude.extractRoof((Polygon)geometry, height);
                default:
                    throw new SQLException("Incorrect flag value. Please set 1 to extract walls "
                            + "or 2 to extract roof.");
            }
        } else if (geometry instanceof LineString) {
            switch (flag) {
                case 1:
                    return GeometryExtrude.extractWalls((LineString)geometry, height);
                case 2:
                    return GeometryExtrude.extractRoof((LineString)geometry, height);
                default:
                    throw new SQLException("Incorrect flag value. Please set 1 to extract walls "
                            + "or 2 to extract roof.");
            }
        }
        throw new SQLException("Only LINESTRING and POLYGON inputs are accepted.");
    }
}
