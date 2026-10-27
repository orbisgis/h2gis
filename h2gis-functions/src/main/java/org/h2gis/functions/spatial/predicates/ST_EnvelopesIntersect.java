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

package org.h2gis.functions.spatial.predicates;

import org.h2.util.geometry.GeometryUtils;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueNull;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * Return true if the envelope of Geometry A intersects the envelope of
 * Geometry B.
 *
 * @author Nicolas Fortin
 */
public class ST_EnvelopesIntersect extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_EnvelopesIntersect() {
        addProperty(PROP_REMARKS, "Return true if the envelope of Geometry A intersects the envelope of Geometry B");
    }

    @Override
    public String getJavaStaticMethod() {
        return "envelopesIntersect";
    }

    /**
     * @param surface Surface Geometry.
     * @param testGeometry Geometry instance
     * @return true if the envelope of Geometry A intersects the envelope of
     * Geometry B
     */
    public static Boolean intersects(Geometry surface,Geometry testGeometry) throws SQLException {
        if(surface==null && testGeometry==null) {
            return null;
        }
        if(surface.isEmpty() || testGeometry.isEmpty()){
            return false;
        }

        if(surface.getSRID()!=testGeometry.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return !(testGeometry == null || surface == null) && surface.getEnvelopeInternal().intersects(testGeometry.getEnvelopeInternal());
    }

    /**
     * @param surface Surface Geometry.
     * @param testGeometry Geometry instance
     * @return true if the envelope of Geometry A intersects the envelope of
     * Geometry B
     */
    public static Boolean envelopesIntersect(Value surface, Value testGeometry) throws SQLException {
        if (surface == ValueNull.INSTANCE || testGeometry == ValueNull.INSTANCE) {
            return null;
        }
        // the envelopes are read from the header of the EWKB, the geometries are not decoded
        ValueGeometry geomA = surface.convertToGeometry(null);
        ValueGeometry geomB = testGeometry.convertToGeometry(null);
        double[] envelopeA = geomA.getEnvelopeNoCopy();
        double[] envelopeB = geomB.getEnvelopeNoCopy();
        if (envelopeA == null || envelopeB == null) {
            return false;
        }
        if (geomA.getSRID() != geomB.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        return GeometryUtils.intersects(envelopeA, envelopeB);
    }
}