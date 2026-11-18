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

package org.h2gis.functions.spatial.convert;

import org.h2.message.DbException;
import org.h2.value.ValueGeometry;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

import java.sql.SQLException;

/**
 * Convert a EWKT representation of a geometry to a geometry object
 * e.g : SRID=4326;POINT(-71.161144 42.25932)
 * @author Erwan Bocher, CNRS
 */
public class ST_GeomFromEWKT extends DeterministicScalarFunction {

    public ST_GeomFromEWKT(){
        addProperty(PROP_REMARKS, "Convert a EWKT representation of a geometry to a geometry object.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    public static Geometry execute(String ewkt) throws SQLException {
        if (ewkt == null) {
            return null;
        }
        try {
            return ValueGeometry.get(ewkt).getGeometry();
        } catch (DbException ex) {
            throw new SQLException("Cannot parse the EWKT format.", ex);
        }
    }
}
