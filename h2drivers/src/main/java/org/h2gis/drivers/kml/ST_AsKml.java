/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.kml;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Convert a JTS geometry to a KML geometry representation.
 *
 * @author Erwan Bocher
 */
public class ST_AsKml extends DeterministicScalarFunction {

    public ST_AsKml() {
        addProperty(PROP_REMARKS, "Return the geometry as a Keyhole Markup Language (KML) element.\n"
                + "Note this function supports two arguments : extrude (boolean) and altitude mode (integer).\n"
                + "Available extrude values are true, false or none.\n"
                + "Supported altitude mode :\n"
                + "For KML profil : CLAMPTOGROUND = 1; RELATIVETOGROUND = 2; ABSOLUTE = 4;\n"
                + "For GX profil : CLAMPTOSEAFLOOR = 8; RELATIVETOSEAFLOOR = 16; \n"
                + " No altitude : NONE = 0;");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toKml";
    }

    /**
     * Generate a KML geometry
     *
     * @param geometry
     * @return
     * @throws SQLException
     */
    public static String toKml(Geometry geometry) throws SQLException {
        StringBuilder sb = new StringBuilder();
        KMLGeometry.toKMLGeometry(geometry, sb);
        return sb.toString();
    }

    /**
     * Generates a KML geometry. Specifies the extrude and altitudeMode.
     *
     * Available extrude values are true, false or none.
     *
     * Supported altitude mode :
     *
     * For KML profil
     *
     * CLAMPTOGROUND = 1; RELATIVETOGROUND = 2; ABSOLUTE = 4;
     *
     * For GX profil CLAMPTOSEAFLOOR = 8; RELATIVETOSEAFLOOR = 16;
     *
     * No altitude : NONE = 0;
     *
     * @param geometry
     * @param altitudeModeEnum
     * @param extrude
     * @return
     * @throws SQLException
     */
    public static String toKml(Geometry geometry, boolean extrude, int altitudeModeEnum) throws SQLException {
        StringBuilder sb = new StringBuilder();
        if (extrude) {
            KMLGeometry.toKMLGeometry(geometry, ExtrudeMode.TRUE, altitudeModeEnum, sb);
        } else {
            KMLGeometry.toKMLGeometry(geometry, ExtrudeMode.FALSE, altitudeModeEnum, sb);
        }
        return sb.toString();
    }
}
