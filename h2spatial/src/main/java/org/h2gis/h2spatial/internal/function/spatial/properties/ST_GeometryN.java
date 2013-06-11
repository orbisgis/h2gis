/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatial.internal.function.spatial.properties;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import java.sql.SQLException;

/**
 * Returns a Geometry instance or Null if parameter is not a GeometryCollection.
 * @author Nicolas Fortin
 */
public class ST_GeometryN extends DeterministicScalarFunction {
    private static final String OUT_OF_BOUNDS_ERR_MESSAGE = "ST_GeometryN index > ST_NumGeometries or index <= 0, Geometry index must be in the range [1-NbGeometry]";

    @Override
    public String getJavaStaticMethod() {
        return "getGeometryN";
    }

    /**
     * @param geometry Instance of Polygon
     * @param i Index of Interior ring [1-N]
     * @return LinearRing instance or Null if parameter is not a Geometry.
     */
    public static Geometry getGeometryN(Geometry geometry,Integer i) throws SQLException {
        if(geometry==null) {
            return null;
        }
        if(i>=1 && i<=geometry.getNumGeometries()) {
            return geometry.getGeometryN(i-1);
        } else {
            throw new SQLException(OUT_OF_BOUNDS_ERR_MESSAGE);
        }
    }
}
