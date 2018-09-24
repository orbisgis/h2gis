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

package org.h2gis.functions.spatial.properties;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;

/**
 * Returns the <i>n</i>th point of a LINESTRING or a MULTILINESTRING containing
 * exactly one LINESTRING; NULL otherwise. As the OGC specifies, ST_PointN is
 * 1-N based.
 *
 * @author Nicolas Fortin
 */
public class ST_PointN extends DeterministicScalarFunction {
    private static final String OUT_OF_BOUNDS_ERR_MESSAGE =
            "Point index out of range. Must be between 1 and ST_NumPoints.";

    /**
     * Default constructor
     */
    public ST_PointN() {
        addProperty(PROP_REMARKS, "Returns the <i>n</i>th point of a LINESTRING " +
                "or a MULTILINESTRING containing exactly one LINESTRING; " +
                "NULL otherwise. As the OGC specifies, ST_PointN is 1-N based.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getPointN";
    }

    /**
     * @param geometry   Geometry instance
     * @param pointIndex Point index [1-NbPoints]
     * @return Returns the <i>n</i>th point of a LINESTRING or a
     * MULTILINESTRING containing exactly one LINESTRING; NULL otherwise. As
     * the OGC specifies, ST_PointN is 1-N based.
     * @throws SQLException if index is out of bound.
     */
    public static Geometry getPointN(Geometry geometry, int pointIndex) throws SQLException {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof MultiLineString) {
            if (geometry.getNumGeometries() == 1) {
                return getPointNFromLine((LineString) geometry.getGeometryN(0), pointIndex);
            }
        } else if (geometry instanceof LineString) {
            return getPointNFromLine((LineString) geometry, pointIndex);
        }
        return null;
    }

    private static Geometry getPointNFromLine(LineString line, int pointIndex) throws SQLException {
        if (pointIndex <= 0 || pointIndex <= line.getNumPoints()) {
            return line.getPointN(pointIndex - 1);
        } else {
            throw new SQLException(OUT_OF_BOUNDS_ERR_MESSAGE);
        }
    }
}
