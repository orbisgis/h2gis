package org.h2gis.h2spatial.internal.function.spatial.crs;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;

import java.sql.SQLException;

/**
 * Return a new geometry with a replaced spatial reference id.
 * @author Nicolas Fortin
 */
public class ST_SetSRID  extends AbstractFunction implements ScalarFunction {
    public ST_SetSRID() {
        addProperty(PROP_REMARKS, "Return a new geometry with a replaced spatial reference id. Warning, use ST_Transform" +
                " if you want to change the coordinate reference system as this method does not update the coordinates." +
                " This function can take at first argument an instance of Geometry or Envelope");
    }

    @Override
    public String getJavaStaticMethod() {
        return "setSRID";
    }

    public static Geometry setSRID(Geometry geometry, int srid) throws SQLException {
        Geometry geom = (Geometry)geometry.clone();
        geom.setSRID(srid);
        return geom;
    }
}
