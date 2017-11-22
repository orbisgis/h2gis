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

package org.h2gis.functions.spatial.aggregate;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.h2gis.api.DeterministicScalarFunction;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Merges a collection of linear components to form maximal-length linestrings.
 * @author Nicolas Fortin
 */
public class ST_LineMerge extends DeterministicScalarFunction {
    public ST_LineMerge() {
        addProperty(PROP_REMARKS, "Merges a collection of LineString elements in order to make create a new collection" +
                " of maximal-length linestrings. If you provide something else than (multi)linestrings it returns an" +
                " empty multilinestring");
    }

    @Override
    public String getJavaStaticMethod() {
        return "merge";
    }

    public static Geometry merge(Geometry geometry) throws SQLException {
        if(geometry == null) {
            return null;
        }
        if(geometry.getDimension() != 1) {
            return geometry.getFactory().createMultiLineString(new LineString[0]);
        }
        LineMerger lineMerger = new LineMerger();
        lineMerger.add(geometry);
        Collection coll = lineMerger.getMergedLineStrings();
        return geometry.getFactory().createMultiLineString((LineString[])coll.toArray(new LineString[coll.size()]));
    }
}
