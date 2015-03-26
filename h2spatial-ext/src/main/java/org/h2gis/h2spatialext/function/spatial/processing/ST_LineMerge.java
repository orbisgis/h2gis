package org.h2gis.h2spatialext.function.spatial.processing;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Merges a collection of linear components to form maximal-length linestrings.
 * @author Nicolas Fortin
 */
public class ST_LineMerge extends DeterministicScalarFunction {
    public ST_LineMerge() {
        addProperty(PROP_REMARKS, "Merges a collection of LineString elements in order to make create a new collection of maximal-length linestrings");
    }

    @Override
    public String getJavaStaticMethod() {
        return "merge";
    }

    public static Geometry merge(Geometry geometry) throws SQLException {
        if(geometry == null) {
            return null;
        }
        LineMerger lineMerger = new LineMerger();
        lineMerger.add(geometry);
        Collection coll = lineMerger.getMergedLineStrings();
        return geometry.getFactory().createMultiLineString((LineString[])coll.toArray(new LineString[coll.size()]));
    }
}
